import { describe, it, expect, vi } from 'vitest'
import { screen, waitFor } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { http, HttpResponse } from 'msw'
import { server } from '../../mocks/server'
import { renderWithProviders } from '../../test/utils'
import CloseIncidentDialog from './CloseIncidentDialog'

// Confirmación para cerrar una incidencia (HU50, T305): cerrarla la elimina con DELETE /api/admin/incidencias/{id}, y
// no se puede deshacer.

const URL_CERRAR = '/api/admin/incidencias/:id'
const ABIERTA = {
  id: 'inc-2',
  asunto: 'Cobro duplicado',
  descripcion: 'Me han cobrado dos veces el pedido.',
  estado: 'ABIERTA',
  respuesta: 'Estamos revisando el cobro con el banco.',
  fechaCreacion: '2026-11-06T10:15:30Z',
}

// Guarda los cierres que llegan al backend
function registrarCierres() {
  const cierres = []
  server.use(
    http.delete(URL_CERRAR, ({ params }) => {
      cierres.push(params.id)
      return new HttpResponse(null, { status: 204 })
    }),
  )
  return cierres
}

function abrirDialogo() {
  const props = { onClose: vi.fn(), onClosed: vi.fn(), onConflict: vi.fn() }
  renderWithProviders(<CloseIncidentDialog incident={ABIERTA} {...props} />)
  return props
}

const botonCerrar = () => screen.getByRole('button', { name: 'Sí, cerrar la incidencia' })

describe('CloseIncidentDialog (HU50): cerrar', () => {
  it('HU50-CP21: pide confirmación con el asunto y avisa de que se elimina', () => {
    abrirDialogo()

    const dialogo = screen.getByRole('alertdialog', { name: 'Cerrar incidencia' })
    expect(dialogo).toHaveTextContent('Cobro duplicado')
    expect(dialogo).toHaveTextContent(/se eliminará y no se puede deshacer/i)
  })

  it('HU50-CP21: al confirmar cierra esa incidencia y lo comunica', async () => {
    const cierres = registrarCierres()
    const user = userEvent.setup()
    const { onClosed } = abrirDialogo()

    await user.click(botonCerrar())

    await waitFor(() => expect(onClosed).toHaveBeenCalled())
    expect(cierres).toEqual(['inc-2'])
  })

  it('HU50-CP21: al volver no cierra nada', async () => {
    const cierres = registrarCierres()
    const user = userEvent.setup()
    const { onClose, onClosed } = abrirDialogo()

    await user.click(screen.getByRole('button', { name: 'Volver' }))

    expect(onClose).toHaveBeenCalled()
    expect(onClosed).not.toHaveBeenCalled()
    expect(cierres).toEqual([])
  })
})

describe('CloseIncidentDialog (HU50): errores', () => {
  it('HU50-CP21: si no se puede cerrar lo avisa y permite volver a intentarlo', async () => {
    const user = userEvent.setup()
    const { onClosed } = abrirDialogo()
    registrarCierres()
    server.use(http.delete(URL_CERRAR, () => new HttpResponse(null, { status: 500 }), { once: true }))

    await user.click(botonCerrar())

    expect(await screen.findByText('No se ha podido cerrar la incidencia. Inténtalo de nuevo.')).toBeInTheDocument()
    expect(onClosed).not.toHaveBeenCalled()

    await user.click(botonCerrar())
    await waitFor(() => expect(onClosed).toHaveBeenCalled())
  })

  it('HU50-CP21: si otro administrador ya la ha cerrado (404), lo comunica a la página', async () => {
    server.use(
      http.delete(URL_CERRAR, () =>
        HttpResponse.json({ mensaje: 'La incidencia no existe', code: 'INCIDENT_NOT_FOUND' }, { status: 404 }),
      ),
    )
    const user = userEvent.setup()
    const { onConflict, onClosed } = abrirDialogo()

    await user.click(botonCerrar())

    await waitFor(() => expect(onConflict).toHaveBeenCalled())
    expect(onClosed).not.toHaveBeenCalled()
    expect(screen.queryByText(/no se ha podido cerrar la incidencia/i)).not.toBeInTheDocument()
  })
})
