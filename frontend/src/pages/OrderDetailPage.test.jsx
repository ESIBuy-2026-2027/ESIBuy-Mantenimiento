import { afterEach, describe, it, expect } from 'vitest'
import { screen, waitFor, within } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { http, HttpResponse } from 'msw'
import { server } from '../mocks/server'
import { abrirSesionPorApi, renderApp } from '../test/renderApp'

// Detalle de un pedido del cliente (HU11, T376) contra el contrato del backend (T377, PR #186):
// GET /api/customer/orders/{id} devuelve el pedido, o 400 con el code ORDER_NOT_FOUND si no existe o es de otro
// cliente. Del mock de pedidos: o-103 es de Carlos, de TecnoVera, ENVIADO, con 2 auriculares a 53,91 € y un teclado
// a 89,50 € (total 197,32 €), para Calle Mayor 1 (Ciudad Real); o-102 está ENTREGADO, o-101 CANCELADO, y o-201 es
// de otra clienta (Nora).

const CLIENTE = 'cliente@esibuy.test'

async function abrirPedido(id) {
  await abrirSesionPorApi(CLIENTE)
  renderApp(`/orders/${id}`)
}

const pasoDelEnvio = (nombre) => within(screen.getByRole('list', { name: /estado del envío/i })).getByRole('listitem', { name: nombre })

describe('OrderDetailPage (HU11): detalle', () => {
  it('HU11-CP14: muestra el número, la fecha, el vendedor y la dirección de envío del pedido', async () => {
    await abrirPedido('o-103')

    expect(await screen.findByRole('heading', { level: 1, name: /pedido ESB-2026-000103/i })).toBeInTheDocument()
    expect(screen.getByText(/30\/10\/2026/)).toBeInTheDocument()
    expect(screen.getByText(/tecnovera/i)).toBeInTheDocument()
    const envio = screen.getByRole('region', { name: /dirección de envío/i })
    expect(envio).toHaveTextContent('Carlos Cliente')
    expect(envio).toHaveTextContent('Calle Mayor 1')
    expect(envio).toHaveTextContent('13001 Ciudad Real (Ciudad Real)')
  })

  it('HU11-CP14: desglosa cada producto con su cantidad, precio unitario y subtotal, y el total', async () => {
    await abrirPedido('o-103')

    const productos = within(await screen.findByRole('table', { name: /productos del pedido/i })).getAllByRole('row')
    // Cabecera, dos productos y la fila del total
    expect(productos).toHaveLength(4)
    expect(productos[1]).toHaveTextContent('Auriculares')
    expect(productos[1]).toHaveTextContent('2')
    expect(productos[1]).toHaveTextContent('53,91')
    expect(productos[1]).toHaveTextContent('107,82')
    expect(productos[2]).toHaveTextContent('Teclado')
    expect(productos[2]).toHaveTextContent('89,50')
    expect(productos[3]).toHaveTextContent('Total')
    expect(productos[3]).toHaveTextContent('197,32')
  })

  it('HU11-CP15: la línea de estados del envío marca los pasos hechos, el actual y los pendientes', async () => {
    await abrirPedido('o-103')

    await screen.findByRole('list', { name: /estado del envío/i })
    expect(pasoDelEnvio('Pagado: completado')).toBeInTheDocument()
    expect(pasoDelEnvio('Preparando: completado')).toBeInTheDocument()
    expect(pasoDelEnvio('Enviado: estado actual')).toHaveAttribute('aria-current', 'step')
    expect(pasoDelEnvio('Entregado: pendiente')).toBeInTheDocument()
  })

  it('HU11-CP15: un pedido entregado tiene todos los pasos hechos y el último como actual', async () => {
    await abrirPedido('o-102')

    await screen.findByRole('list', { name: /estado del envío/i })
    expect(pasoDelEnvio('Enviado: completado')).toBeInTheDocument()
    expect(pasoDelEnvio('Entregado: estado actual')).toHaveAttribute('aria-current', 'step')
  })

  it('HU11-CP16: un pedido cancelado lo indica en lugar de la línea de estados del envío', async () => {
    await abrirPedido('o-101')

    expect(await screen.findByText(/este pedido está cancelado/i)).toBeInTheDocument()
    expect(screen.queryByRole('list', { name: /estado del envío/i })).not.toBeInTheDocument()
  })
})

describe('OrderDetailPage (HU11): errores', () => {
  it('HU11-CP17: un pedido que no existe o es de otro cliente se muestra como no encontrado', async () => {
    await abrirSesionPorApi(CLIENTE)
    for (const id of ['o-no-existe', 'o-201']) {
      const { unmount } = renderApp(`/orders/${id}`)

      expect(await screen.findByText(/pedido no encontrado/i)).toBeInTheDocument()
      expect(screen.getByRole('link', { name: /volver a mis pedidos/i })).toHaveAttribute('href', '/orders')
      unmount()
    }
  })

  it('HU11-CP17: si falla por otro motivo lo avisa y permite reintentar', async () => {
    server.use(http.get('/api/customer/orders/o-103', () => new HttpResponse(null, { status: 500 }), { once: true }))
    const user = userEvent.setup()
    await abrirPedido('o-103')

    expect(await screen.findByText(/no se ha podido cargar el pedido/i)).toBeInTheDocument()
    await user.click(screen.getByRole('button', { name: /reintentar/i }))

    expect(await screen.findByRole('heading', { level: 1, name: /pedido ESB-2026-000103/i })).toBeInTheDocument()
  })

  it('HU11-CP17: desde el detalle se vuelve a Mis pedidos', async () => {
    const user = userEvent.setup()
    await abrirPedido('o-103')

    await screen.findByRole('heading', { level: 1, name: /pedido ESB-2026-000103/i })
    await user.click(screen.getByRole('link', { name: /volver a mis pedidos/i }))

    await waitFor(() => expect(screen.getByTestId('ruta-actual')).toHaveTextContent(/^\/orders$/))
  })
})

// --- Cancelar un pedido (HU12) ---------------------------------------------------------------------------------------
// Solo se cancela un pedido que aún no se ha enviado (PAGADO o PREPARANDO), como en el backend (T375 y T374, PR #200 y
// #207). Del mock: Nora (nuevo@esibuy.test) tiene o-201, PAGADO; los de Carlos están enviado, entregado y cancelado.

const NORA = 'nuevo@esibuy.test'

// Guarda las peticiones que llegan a una ruta, sin cambiar la respuesta del mock.
function espiarPeticiones(metodo, ruta) {
  const peticiones = []
  server.events.on('request:start', ({ request }) => {
    if (request.method === metodo && new URL(request.url).pathname === ruta) peticiones.push(request)
  })
  return peticiones
}

afterEach(() => server.events.removeAllListeners())

async function abrirPedidoDe(email, id) {
  await abrirSesionPorApi(email)
  renderApp(`/orders/${id}`)
  await screen.findByRole('heading', { level: 1, name: /pedido ESB-/i })
}

describe('OrderDetailPage (HU12): botón de cancelar', () => {
  it('HU12-CP13: un pedido pagado o en preparación, que aún no se ha enviado, tiene el botón «Cancelar pedido»', async () => {
    await abrirPedidoDe(NORA, 'o-201')
    expect(screen.getByRole('button', { name: /cancelar pedido/i })).toBeInTheDocument()
  })

  it('HU12-CP13: un pedido en preparación también se puede cancelar', async () => {
    server.use(
      http.get('/api/customer/orders/o-103', () =>
        HttpResponse.json({
          id: 'o-103',
          numeroPedido: 'ESB-2026-000103',
          vendedor: { id: 'u-2', nombreComercial: 'TecnoVera' },
          total: 89.5,
          estado: 'PREPARANDO',
          paymentMethodId: 'mp-1',
          direccionEnvio: {
            destinatario: 'Carlos Cliente',
            calle: 'Calle Mayor 1',
            detalles: null,
            codigoPostal: '13001',
            ciudad: 'Ciudad Real',
            provincia: 'Ciudad Real',
            telefono: '611222333',
          },
          lineas: [{ productoId: 'p-2', nombre: 'Teclado', cantidad: 1, precioUnitario: 89.5, subtotal: 89.5 }],
          createdAt: '2026-10-30T10:00:00Z',
        }),
      ),
    )
    await abrirPedidoDe(CLIENTE, 'o-103')

    expect(screen.getByRole('button', { name: /cancelar pedido/i })).toBeInTheDocument()
  })

  it('HU12-CP13: un pedido enviado, entregado o ya cancelado no tiene el botón', async () => {
    await abrirSesionPorApi(CLIENTE)
    for (const id of ['o-103', 'o-102', 'o-101']) {
      const { unmount } = renderApp(`/orders/${id}`)
      await screen.findByRole('heading', { level: 1, name: /pedido ESB-/i })

      expect(screen.queryByRole('button', { name: /cancelar pedido/i })).not.toBeInTheDocument()
      unmount()
    }
  })

  it('HU12-CP14: al pulsar «Cancelar pedido» pide confirmación y todavía no cancela nada', async () => {
    const user = userEvent.setup()
    const cancelaciones = espiarPeticiones('POST', '/api/customer/orders/o-201/cancel')
    await abrirPedidoDe(NORA, 'o-201')

    await user.click(screen.getByRole('button', { name: /cancelar pedido/i }))

    expect(await screen.findByRole('alertdialog', { name: /cancelar pedido/i })).toBeInTheDocument()
    expect(cancelaciones).toHaveLength(0)
  })
})

describe('OrderDetailPage (HU12): confirmar la cancelación', () => {
  it('HU12-CP16: al confirmar, el detalle muestra el pedido cancelado y avisa de que se ha cancelado', async () => {
    const user = userEvent.setup()
    await abrirPedidoDe(NORA, 'o-201')

    await user.click(screen.getByRole('button', { name: /cancelar pedido/i }))
    await user.click(await screen.findByRole('button', { name: /sí, cancelar el pedido/i }))

    expect(await screen.findByText('Pedido cancelado.')).toBeInTheDocument()
    expect(screen.getByText(/este pedido está cancelado/i)).toBeInTheDocument()
    expect(screen.queryByRole('list', { name: /estado del envío/i })).not.toBeInTheDocument()
    expect(screen.queryByRole('button', { name: /cancelar pedido/i })).not.toBeInTheDocument()
    expect(screen.queryByRole('alertdialog')).not.toBeInTheDocument()
  })

  it('HU12-CP17: si el pedido ya no se puede cancelar, cierra la confirmación y lo avisa en el detalle', async () => {
    server.use(
      http.post('/api/customer/orders/o-201/cancel', () =>
        HttpResponse.json({ code: 'ORDER_NOT_CANCELLABLE' }, { status: 409 }),
      ),
    )
    const user = userEvent.setup()
    await abrirPedidoDe(NORA, 'o-201')

    await user.click(screen.getByRole('button', { name: /cancelar pedido/i }))
    await user.click(await screen.findByRole('button', { name: /sí, cancelar el pedido/i }))

    expect(await screen.findByText(/este pedido ya no se puede cancelar/i)).toBeInTheDocument()
    await waitFor(() => expect(screen.queryByRole('alertdialog')).not.toBeInTheDocument())
  })
})
