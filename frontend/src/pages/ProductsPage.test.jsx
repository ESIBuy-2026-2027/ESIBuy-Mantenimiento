import { describe, expect, it } from 'vitest'
import { screen, waitFor, within } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { http, HttpResponse } from 'msw'
import { server } from '../mocks/server'
import { abrirSesionPorApi, renderApp } from '../test/renderApp'

const CLIENTE = 'cliente@esibuy.test'

async function abrirCatalogo() {
  await abrirSesionPorApi(CLIENTE)
  renderApp('/products')
  await screen.findByRole('heading', { level: 1, name: /productos/i })
}

const tarjetas = () => within(screen.getByRole('list', { name: /productos/i })).getAllByRole('listitem')

async function esperarTarjetas(cantidad) {
  await waitFor(() => expect(tarjetas()).toHaveLength(cantidad))
}

const respuesta = (items) =>
  HttpResponse.json({ items, page: 1, size: 12, totalItems: items.length, totalPages: items.length ? 1 : 0 })

describe('ProductsPage (HU7): vista de productos del cliente', () => {
  it('HU7-CP01: tras iniciar sesión, el cliente aterriza en la vista de productos', async () => {
    await abrirSesionPorApi(CLIENTE)
    renderApp('/login')

    expect(await screen.findByRole('heading', { level: 1, name: /productos/i })).toBeInTheDocument()
    expect(screen.getByTestId('ruta-actual')).toHaveTextContent(/^\/products$/)
  })

  it('HU7-CP01: sin sesión, la vista de productos lleva a iniciar sesión', async () => {
    renderApp('/products')

    await waitFor(() => expect(screen.getByTestId('ruta-actual')).toHaveTextContent(/^\/login$/))
  })

  it('HU7-CP01: un vendedor no ve la vista de productos del cliente: va a la suya', async () => {
    await abrirSesionPorApi('vendedor@esibuy.test')
    renderApp('/products')

    await waitFor(() => expect(screen.getByTestId('ruta-actual')).toHaveTextContent(/^\/seller\/products$/))
  })

  it('HU7-CP02: cada tarjeta muestra el nombre, la categoría y la valoración del producto', async () => {
    await abrirCatalogo()

    await esperarTarjetas(12)
    const tarjeta = screen.getByText('Auriculares inalámbricos').closest('li')
    expect(within(tarjeta).getByText('Tecnología')).toBeInTheDocument()
    expect(within(tarjeta).getByRole('img', { name: /valoración: \d,\d de 5/i })).toBeInTheDocument()
    expect(within(tarjeta).getByRole('img', { name: 'Auriculares inalámbricos' })).toBeInTheDocument()
  })

  it('HU7-CP03: muestra la primera página con doce tarjetas y solo los productos disponibles', async () => {
    await abrirCatalogo()

    await esperarTarjetas(12)
    expect(screen.getByText(/mostrando 1–12 de 23/i)).toBeInTheDocument()
    expect(screen.queryByText('Webcam Full HD')).not.toBeInTheDocument()
  })

  it('HU7-CP03: las tarjetas se colocan de dos en dos', async () => {
    await abrirCatalogo()

    await esperarTarjetas(12)
    expect(screen.getByRole('list', { name: /productos/i })).toHaveStyle({
      gridTemplateColumns: 'repeat(2, minmax(0, 1fr))',
    })
  })

  it('HU7-CP03: al pasar de página muestra las tarjetas siguientes', async () => {
    const user = userEvent.setup()
    await abrirCatalogo()
    await esperarTarjetas(12)

    await user.click(screen.getByRole('button', { name: /siguiente/i }))

    await waitFor(() => expect(screen.getByText(/mostrando 13–23 de 23/i)).toBeInTheDocument())
    expect(tarjetas()).toHaveLength(11)
  })

  it('HU7-CP03: si falla la carga avisa y permite reintentar', async () => {
    const user = userEvent.setup()
    server.use(http.get('/api/products', () => new HttpResponse(null, { status: 500 }), { once: true }))
    await abrirSesionPorApi(CLIENTE)
    renderApp('/products')

    expect(await screen.findByText(/no se han podido cargar los productos/i)).toBeInTheDocument()
    await user.click(screen.getByRole('button', { name: /reintentar/i }))

    await esperarTarjetas(12)
  })

  it('HU7-CP03: si todavía no hay productos lo indica', async () => {
    server.use(http.get('/api/products', () => respuesta([])))
    await abrirCatalogo()

    expect(await screen.findByText(/todavía no hay productos/i)).toBeInTheDocument()
  })
})
