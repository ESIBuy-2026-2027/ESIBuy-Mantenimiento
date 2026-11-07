import { describe, it, expect, vi } from 'vitest'
import { screen, waitFor } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { http, HttpResponse } from 'msw'
import { server } from '../../mocks/server'
import { renderWithProviders } from '../../test/utils'
import OpenIncidentDialog from './OpenIncidentDialog'

// Diálogo para abrir una incidencia (HU50, T305): el administrador escribe la respuesta, obligatoria y de hasta 1000
// caracteres, y se envía a PATCH /api/admin/incidencias/{id}/abrir.

const URL_ABRIR = '/api/admin/incidencias/:id/abrir'
const PENDIENTE = {
  id: 'inc-1',
  asunto: 'No me llega el pedido',
  descripcion: 'Lo pedí hace una semana',
  estado: 'PENDIENTE',
  respuesta: '',
  fechaCreacion: '2026-11-06T10:15:30Z',
}

// Guarda las aperturas que llegan al backend y responde con la incidencia abierta
function registrarAperturas() {
  const aperturas = []
  server.use(
    http.patch(URL_ABRIR, async ({ request, params }) => {
      const { respuesta } = await request.json()
      aperturas.push({ id: params.id, respuesta })
      return HttpResponse.json({ ...PENDIENTE, estado: 'ABIERTA', respuesta })
    }),
  )
  return aperturas
}

function abrirDialogo() {
  const props = { onClose: vi.fn(), onOpened: vi.fn(), onConflict: vi.fn() }
  renderWithProviders(<OpenIncidentDialog incident={PENDIENTE} {...props} />)
  return props
}

const dialogo = () => screen.getByRole('dialog', { name: 'Abrir incidencia' })
const campoRespuesta = () => screen.getByRole('textbox', { name: /respuesta/i })
const botonAbrir = () => screen.getByRole('button', { name: 'Abrir incidencia' })

describe('OpenIncidentDialog (HU50): abrir', () => {
  it('HU50-CP20: muestra el asunto de la incidencia y el contador de la respuesta', () => {
    abrirDialogo()

    expect(dialogo()).toHaveTextContent('No me llega el pedido')
    expect(dialogo()).toHaveTextContent('0/1000')
  })

  it('HU50-CP20: envía la respuesta sin espacios en los extremos y entrega la incidencia abierta', async () => {
    const aperturas = registrarAperturas()
    const user = userEvent.setup()
    const { onOpened } = abrirDialogo()

    await user.type(campoRespuesta(), '  Lo estamos revisando  ')
    expect(dialogo()).toHaveTextContent('20/1000')
    await user.click(botonAbrir())

    await waitFor(() => expect(onOpened).toHaveBeenCalled())
    expect(aperturas).toEqual([{ id: 'inc-1', respuesta: 'Lo estamos revisando' }])
    expect(onOpened).toHaveBeenCalledWith(expect.objectContaining({ id: 'inc-1', estado: 'ABIERTA' }))
  })

  it('HU50-CP20: sin respuesta no envía nada y dice que es obligatoria', async () => {
    const aperturas = registrarAperturas()
    const user = userEvent.setup()
    const { onOpened } = abrirDialogo()

    expect(screen.queryByText('La respuesta es obligatoria.')).not.toBeInTheDocument()
    await user.type(campoRespuesta(), '   ')
    await user.click(botonAbrir())

    expect(await screen.findByText('La respuesta es obligatoria.')).toBeInTheDocument()
    expect(aperturas).toEqual([])
    expect(onOpened).not.toHaveBeenCalled()
  })

  it('HU50-CP20: con más de 1000 caracteres lo avisa en cuanto ocurre y no envía nada', async () => {
    const aperturas = registrarAperturas()
    const user = userEvent.setup()
    abrirDialogo()

    await user.click(campoRespuesta())
    await user.paste('r'.repeat(1001))

    expect(screen.getByText('La respuesta no puede superar los 1000 caracteres.')).toBeInTheDocument()
    await user.click(botonAbrir())
    expect(aperturas).toEqual([])
  })

  it('HU50-CP20: al cancelar no envía nada', async () => {
    const aperturas = registrarAperturas()
    const user = userEvent.setup()
    const { onClose, onOpened } = abrirDialogo()

    await user.type(campoRespuesta(), 'Lo estamos revisando')
    await user.click(screen.getByRole('button', { name: 'Cancelar' }))

    expect(onClose).toHaveBeenCalled()
    expect(onOpened).not.toHaveBeenCalled()
    expect(aperturas).toEqual([])
  })
})

describe('OpenIncidentDialog (HU50): errores', () => {
  it('HU50-CP20: si no se puede abrir lo avisa y permite volver a intentarlo', async () => {
    const user = userEvent.setup()
    const { onOpened } = abrirDialogo()
    registrarAperturas()
    server.use(http.patch(URL_ABRIR, () => new HttpResponse(null, { status: 500 }), { once: true }))

    await user.type(campoRespuesta(), 'Lo estamos revisando')
    await user.click(botonAbrir())

    expect(await screen.findByText('No se ha podido abrir la incidencia. Inténtalo de nuevo.')).toBeInTheDocument()
    expect(onOpened).not.toHaveBeenCalled()

    await user.click(botonAbrir())
    await waitFor(() => expect(onOpened).toHaveBeenCalled())
  })

  it.each([
    ['ya está abierta (409)', 409, 'INCIDENT_ALREADY_OPEN'],
    ['ya no existe (404)', 404, 'INCIDENT_NOT_FOUND'],
  ])('HU50-CP20: si otro administrador la ha cambiado porque %s, lo comunica a la página', async (_caso, status, code) => {
    server.use(http.patch(URL_ABRIR, () => HttpResponse.json({ mensaje: 'x', code }, { status })))
    const user = userEvent.setup()
    const { onConflict, onOpened } = abrirDialogo()

    await user.type(campoRespuesta(), 'Lo estamos revisando')
    await user.click(botonAbrir())

    await waitFor(() => expect(onConflict).toHaveBeenCalled())
    expect(onOpened).not.toHaveBeenCalled()
    expect(screen.queryByText(/no se ha podido abrir la incidencia/i)).not.toBeInTheDocument()
  })
})
