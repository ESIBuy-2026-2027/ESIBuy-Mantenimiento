import { afterEach, describe, it, expect, vi } from 'vitest'
import { screen, waitFor } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { http, HttpResponse } from 'msw'
import { server } from '../../mocks/server'
import { abrirSesionPorApi } from '../../test/renderApp'
import { renderWithProviders } from '../../test/utils'
import CancelOrderDialog from './CancelOrderDialog'

// Confirmación para cancelar un pedido (HU12, T373) contra el contrato del backend (T374, PR #207):
// POST /api/customer/orders/{id}/cancel responde 200 con el pedido cancelado, 409 ORDER_NOT_CANCELLABLE si ya no se
// puede cancelar (p. ej. se acaba de enviar) o 400 ORDER_NOT_FOUND. Del mock: o-201 es de Nora y está PAGADO.

const RUTA = '/api/customer/orders/o-201/cancel'
const PEDIDO = { id: 'o-201', numeroPedido: 'ESB-2026-000201' }

function espiarCancelaciones() {
  const peticiones = []
  server.events.on('request:start', ({ request }) => {
    if (request.method === 'POST' && new URL(request.url).pathname === RUTA) peticiones.push(request)
  })
  return peticiones
}

afterEach(() => server.events.removeAllListeners())

async function abrirDialogo() {
  await abrirSesionPorApi('nuevo@esibuy.test')
  const callbacks = { onClose: vi.fn(), onCancelled: vi.fn(), onNotCancellable: vi.fn() }
  renderWithProviders(<CancelOrderDialog pedido={PEDIDO} {...callbacks} />)
  await screen.findByRole('alertdialog', { name: /cancelar pedido/i })
  return callbacks
}

describe('CancelOrderDialog (HU12)', () => {
  it('HU12-CP15: pregunta por el pedido que se va a cancelar y avisa de que no se puede deshacer', async () => {
    await abrirDialogo()

    expect(screen.getByRole('alertdialog')).toHaveTextContent('ESB-2026-000201')
    expect(screen.getByRole('alertdialog')).toHaveTextContent(/no se puede deshacer/i)
  })

  it('HU12-CP15: «Volver» cierra la confirmación sin cancelar el pedido', async () => {
    const user = userEvent.setup()
    const cancelaciones = espiarCancelaciones()
    const { onClose, onCancelled } = await abrirDialogo()

    await user.click(screen.getByRole('button', { name: /volver/i }))

    expect(onClose).toHaveBeenCalled()
    expect(onCancelled).not.toHaveBeenCalled()
    expect(cancelaciones).toHaveLength(0)
  })

  it('HU12-CP16: al confirmar cancela el pedido y entrega el pedido ya cancelado', async () => {
    const user = userEvent.setup()
    const cancelaciones = espiarCancelaciones()
    const { onCancelled } = await abrirDialogo()

    await user.click(screen.getByRole('button', { name: /sí, cancelar el pedido/i }))

    await waitFor(() => expect(onCancelled).toHaveBeenCalled())
    expect(onCancelled.mock.calls[0][0]).toMatchObject({ id: 'o-201', estado: 'CANCELADO' })
    expect(cancelaciones).toHaveLength(1)
  })

  it('HU12-CP17: si el pedido ya no se puede cancelar (409), lo comunica sin darlo por cancelado', async () => {
    server.use(http.post(RUTA, () => HttpResponse.json({ code: 'ORDER_NOT_CANCELLABLE' }, { status: 409 })))
    const user = userEvent.setup()
    const { onCancelled, onNotCancellable } = await abrirDialogo()

    await user.click(screen.getByRole('button', { name: /sí, cancelar el pedido/i }))

    await waitFor(() => expect(onNotCancellable).toHaveBeenCalled())
    expect(onCancelled).not.toHaveBeenCalled()
  })

  it('HU12-CP18: si falla por otro motivo avisa dentro de la confirmación y se puede reintentar', async () => {
    server.use(http.post(RUTA, () => new HttpResponse(null, { status: 500 }), { once: true }))
    const user = userEvent.setup()
    const { onClose, onCancelled } = await abrirDialogo()

    await user.click(screen.getByRole('button', { name: /sí, cancelar el pedido/i }))

    expect(await screen.findByText(/no se ha podido cancelar el pedido/i)).toBeInTheDocument()
    expect(onClose).not.toHaveBeenCalled()

    await user.click(screen.getByRole('button', { name: /sí, cancelar el pedido/i }))
    await waitFor(() => expect(onCancelled).toHaveBeenCalled())
  })
})
