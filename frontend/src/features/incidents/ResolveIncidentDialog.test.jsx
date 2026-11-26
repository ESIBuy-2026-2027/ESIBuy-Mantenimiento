import { describe, it, expect, vi } from 'vitest'
import { screen, waitFor } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { delay, http, HttpResponse } from 'msw'
import { server } from '../../mocks/server'
import { renderWithProviders } from '../../test/utils'
import ResolveIncidentDialog from './ResolveIncidentDialog'

// Confirmación para marcar como resuelta una incidencia abierta (HU192) con
// PATCH /api/admin/incidencias/{id}/resolver. Muestra la carga mientras responde y anuncia los errores con mensajes
// genéricos; si la incidencia ha cambiado entretanto, lo comunica a la página.

const URL_RESOLVER = '/api/admin/incidencias/:id/resolver'
const ABIERTA = {
  id: 'inc-2',
  asunto: 'Cobro duplicado',
  descripcion: 'Me han cobrado dos veces el pedido.',
  estado: 'ABIERTA',
  respuesta: 'Estamos revisando el cobro con el banco.',
  fechaCreacion: '2026-11-06T10:15:30Z',
  fechaResolucion: null,
}
const RESUELTA = { ...ABIERTA, estado: 'RESUELTA', fechaResolucion: '2026-11-25T12:00:00Z' }
const ERROR_GENERICO = 'No se ha podido marcar la incidencia como resuelta. Inténtalo de nuevo.'

// Guarda las resoluciones que llegan al backend
function registrarResoluciones(espera = 0) {
  const resoluciones = []
  server.use(
    http.patch(URL_RESOLVER, async ({ params }) => {
      resoluciones.push(params.id)
      await delay(espera)
      return HttpResponse.json(RESUELTA)
    }),
  )
  return resoluciones
}

function abrirDialogo() {
  const props = { onClose: vi.fn(), onResolved: vi.fn(), onConflict: vi.fn() }
  renderWithProviders(<ResolveIncidentDialog incident={ABIERTA} {...props} />)
  return props
}

const botonResolver = () => screen.getByRole('button', { name: 'Sí, marcar como resuelta' })

describe('ResolveIncidentDialog (HU192): resolver', () => {
  it('HU192-CP23: pide confirmación con el asunto y avisa de que no se puede deshacer', () => {
    abrirDialogo()

    const dialogo = screen.getByRole('alertdialog', { name: 'Marcar incidencia como resuelta' })
    expect(dialogo).toHaveTextContent('Cobro duplicado')
    expect(dialogo).toHaveTextContent(/no se puede deshacer/i)
  })

  it('HU192-CP23: al confirmar resuelve esa incidencia y entrega la resuelta', async () => {
    const resoluciones = registrarResoluciones()
    const user = userEvent.setup()
    const { onResolved } = abrirDialogo()

    await user.click(botonResolver())

    await waitFor(() => expect(onResolved).toHaveBeenCalledWith(RESUELTA))
    expect(resoluciones).toEqual(['inc-2'])
  })

  it('HU192-CP23: al volver no envía nada', async () => {
    const resoluciones = registrarResoluciones()
    const user = userEvent.setup()
    const { onClose, onResolved } = abrirDialogo()

    await user.click(screen.getByRole('button', { name: 'Volver' }))

    expect(onClose).toHaveBeenCalled()
    expect(onResolved).not.toHaveBeenCalled()
    expect(resoluciones).toEqual([])
  })

  it('HU192-CP24: mientras resuelve muestra la carga, no repite la petición y no se puede cerrar', async () => {
    const resoluciones = registrarResoluciones(100)
    const user = userEvent.setup()
    const { onClose, onResolved } = abrirDialogo()

    await user.click(botonResolver())

    const cargando = screen.getByRole('button', { name: /resolviendo/i })
    expect(cargando).toHaveAttribute('aria-disabled', 'true')
    expect(cargando).toHaveFocus()
    expect(screen.getByRole('button', { name: 'Volver' })).toBeDisabled()
    await user.click(cargando)
    await user.keyboard('{Escape}')
    expect(onClose).not.toHaveBeenCalled()
    await waitFor(() => expect(onResolved).toHaveBeenCalledTimes(1))
    expect(resoluciones).toEqual(['inc-2'])
  })
})

describe('ResolveIncidentDialog (HU192): errores', () => {
  it('HU192-CP25: si no se puede resolver lo anuncia sin detalles internos y permite reintentar', async () => {
    const user = userEvent.setup()
    const { onResolved } = abrirDialogo()
    registrarResoluciones()
    server.use(
      http.patch(URL_RESOLVER, () => HttpResponse.json({ mensaje: 'detalle interno' }, { status: 500 }), { once: true }),
    )

    await user.click(botonResolver())

    expect(await screen.findByRole('alert')).toHaveTextContent(ERROR_GENERICO)
    expect(screen.queryByText(/detalle interno/i)).not.toBeInTheDocument()
    expect(onResolved).not.toHaveBeenCalled()

    await user.click(botonResolver())
    await waitFor(() => expect(onResolved).toHaveBeenCalled())
  })

  it('HU192-CP25: sin permiso (403) lo anuncia', async () => {
    server.use(http.patch(URL_RESOLVER, () => HttpResponse.json({ code: 'ACCESS_DENIED' }, { status: 403 })))
    const user = userEvent.setup()
    const { onResolved, onConflict } = abrirDialogo()

    await user.click(botonResolver())

    expect(await screen.findByRole('alert')).toHaveTextContent('No tienes permiso para resolver incidencias.')
    expect(onResolved).not.toHaveBeenCalled()
    expect(onConflict).not.toHaveBeenCalled()
  })

  it.each([
    ['ya está resuelta (409)', 409, 'INCIDENT_ALREADY_RESOLVED'],
    ['no está abierta (409)', 409, 'INCIDENT_NOT_OPEN'],
    ['ya no existe (404)', 404, 'INCIDENT_NOT_FOUND'],
  ])('HU192-CP25: si la incidencia %s, lo comunica a la página con su código', async (_caso, status, code) => {
    server.use(http.patch(URL_RESOLVER, () => HttpResponse.json({ mensaje: 'x', code }, { status })))
    const user = userEvent.setup()
    const { onConflict, onResolved } = abrirDialogo()

    await user.click(botonResolver())

    await waitFor(() => expect(onConflict).toHaveBeenCalledWith(code))
    expect(onResolved).not.toHaveBeenCalled()
    expect(screen.queryByRole('alert')).not.toBeInTheDocument()
  })
})
