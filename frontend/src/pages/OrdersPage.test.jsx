import { describe, it, expect } from 'vitest'
import { screen, waitFor, within } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { http, HttpResponse } from 'msw'
import { server } from '../mocks/server'
import { abrirSesionPorApi, renderApp } from '../test/renderApp'

// «Mis pedidos» del cliente (HU11, T376) contra el contrato del backend (T377, PR #186):
// GET /api/customer/orders devuelve los pedidos del cliente de la sesión, del más reciente al más antiguo.
// Del mock de pedidos: Carlos (cliente@esibuy.test) tiene tres:
//   ESB-2026-000103 · TecnoVera · 30/10/2026 · 197,32 € · ENVIADO
//   ESB-2026-000102 · Otero Hogar y Tecnología · 20/10/2026 · 129,00 € · ENTREGADO
//   ESB-2026-000101 · TecnoVera · 10/10/2026 · 19,99 € · CANCELADO

const URL_PEDIDOS = '/api/customer/orders'

async function abrirMisPedidos(email = 'cliente@esibuy.test') {
  await abrirSesionPorApi(email)
  renderApp('/orders')
  await screen.findByRole('heading', { level: 1, name: /mis pedidos/i })
}

const filasDePedido = () => within(screen.getByRole('list', { name: /tus pedidos/i })).getAllByRole('listitem')

async function esperarRuta(ruta) {
  await waitFor(() => expect(screen.getByTestId('ruta-actual')).toHaveTextContent(new RegExp(`^${ruta}$`)))
}

describe('OrdersPage (HU11): historial', () => {
  it('HU11-CP11: muestra los pedidos del cliente, del más reciente al más antiguo, con número, fecha, vendedor, total y estado', async () => {
    await abrirMisPedidos()

    await screen.findByRole('list', { name: /tus pedidos/i })
    const filas = filasDePedido()
    expect(filas).toHaveLength(3)
    expect(filas[0]).toHaveTextContent('ESB-2026-000103')
    expect(filas[0]).toHaveTextContent('30/10/2026')
    expect(filas[0]).toHaveTextContent('TecnoVera')
    expect(filas[0]).toHaveTextContent('197,32')
    expect(filas[0]).toHaveTextContent('Enviado')
    expect(filas[1]).toHaveTextContent('ESB-2026-000102')
    expect(filas[1]).toHaveTextContent('Entregado')
    expect(filas[2]).toHaveTextContent('ESB-2026-000101')
    expect(filas[2]).toHaveTextContent('Cancelado')
  })

  it('HU11-CP11: sin pedidos lo indica y ofrece ir a los productos', async () => {
    server.use(http.get(URL_PEDIDOS, () => HttpResponse.json([])))
    await abrirMisPedidos()

    expect(await screen.findByText(/aún no has hecho ningún pedido/i)).toBeInTheDocument()
    expect(screen.getByRole('link', { name: /ver los productos/i })).toHaveAttribute('href', '/products')
  })

  it('HU11-CP11: si no se pueden cargar los pedidos lo avisa y permite reintentar', async () => {
    server.use(http.get(URL_PEDIDOS, () => new HttpResponse(null, { status: 500 }), { once: true }))
    const user = userEvent.setup()
    await abrirMisPedidos()

    expect(await screen.findByText(/no se han podido cargar tus pedidos/i)).toBeInTheDocument()
    await user.click(screen.getByRole('button', { name: /reintentar/i }))

    await screen.findByRole('list', { name: /tus pedidos/i })
    expect(filasDePedido()).toHaveLength(3)
  })

  it('HU11-CP12: al pulsar un pedido se abre su detalle', async () => {
    const user = userEvent.setup()
    await abrirMisPedidos()

    await user.click(await screen.findByRole('link', { name: /ESB-2026-000103/ }))

    await esperarRuta('/orders/o-103')
    expect(await screen.findByRole('heading', { level: 1, name: /pedido ESB-2026-000103/i })).toBeInTheDocument()
  })
})

describe('OrdersPage (HU11): acceso', () => {
  it('HU11-CP13: sin sesión, Mis pedidos lleva a iniciar sesión', async () => {
    renderApp('/orders')

    await esperarRuta('/login')
  })

  it('HU11-CP13: un vendedor no ve los pedidos de los clientes: va a su página', async () => {
    await abrirSesionPorApi('vendedor@esibuy.test')
    renderApp('/orders')

    await esperarRuta('/seller/products')
  })
})
