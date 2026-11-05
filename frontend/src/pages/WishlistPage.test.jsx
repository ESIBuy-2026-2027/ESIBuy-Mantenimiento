import { describe, it, expect } from 'vitest'
import { screen, waitFor, within } from '@testing-library/react'
import { http, HttpResponse } from 'msw'
import { server } from '../mocks/server'
import { abrirSesionPorApi, renderApp } from '../test/renderApp'

// «Mis favoritos» del cliente (HU14, T363 y T364) contra el contrato del backend (PR #205, #206, #208 y #209):
// GET /api/customer/wishlist devuelve la lista con los datos actuales de cada producto y
// DELETE /api/customer/wishlist/items/{productoId} quita uno y devuelve la lista actualizada.

const URL_LISTA = '/api/customer/wishlist'

// Uno disponible, uno agotado (se puede comprar más adelante) y uno que el vendedor ha desactivado.
const LISTA = {
  items: [
    {
      productoId: 'p-1',
      nombre: 'Auriculares inalámbricos',
      imagen: 'https://tienda.test/auriculares.png',
      precioFinal: 53.91,
      stock: 34,
      disponible: true,
      addedAt: '2026-11-01T10:00:00Z',
    },
    {
      productoId: 'p-3',
      nombre: 'Ratón ergonómico',
      imagen: '',
      precioFinal: 34.99,
      stock: 0,
      disponible: true,
      addedAt: '2026-11-03T10:00:00Z',
    },
    {
      productoId: 'p-6',
      nombre: 'Webcam Full HD',
      imagen: '',
      precioFinal: 49,
      stock: 0,
      disponible: false,
      addedAt: '2026-11-04T10:00:00Z',
    },
  ],
}

async function abrirMisFavoritos(lista = LISTA) {
  server.use(http.get(URL_LISTA, () => HttpResponse.json(lista)))
  await abrirSesionPorApi('cliente@esibuy.test')
  renderApp('/wishlist')
  await screen.findByRole('heading', { level: 1, name: 'Mis favoritos' })
}

const filas = () => within(screen.getByRole('list', { name: 'Tus favoritos' })).getAllByRole('listitem')

async function esperarRuta(ruta) {
  await waitFor(() => expect(screen.getByTestId('ruta-actual')).toHaveTextContent(new RegExp(`^${ruta}$`)))
}

describe('WishlistPage (HU14): acceso', () => {
  it('HU14-CP32: el cliente abre Mis favoritos en /wishlist', async () => {
    await abrirSesionPorApi('cliente@esibuy.test')
    renderApp('/wishlist')

    expect(await screen.findByRole('heading', { level: 1, name: 'Mis favoritos' })).toBeInTheDocument()
  })

  it('HU14-CP32: sin sesión, Mis favoritos lleva a iniciar sesión', async () => {
    renderApp('/wishlist')

    await esperarRuta('/login')
  })

  it('HU14-CP32: un vendedor no tiene lista de deseos: va a su página', async () => {
    await abrirSesionPorApi('vendedor@esibuy.test')
    renderApp('/wishlist')

    await esperarRuta('/seller/products')
  })
})

describe('WishlistPage (HU14): lista de favoritos', () => {
  it('HU14-CP33: muestra los favoritos en el orden en que se añadieron, con su nombre, precio y fecha', async () => {
    await abrirMisFavoritos()

    await screen.findByRole('list', { name: 'Tus favoritos' })
    expect(filas()).toHaveLength(3)
    expect(filas()[0]).toHaveTextContent('Auriculares inalámbricos')
    expect(filas()[0]).toHaveTextContent('53,91')
    expect(filas()[0]).toHaveTextContent('Añadido el 01/11/2026')
    expect(filas()[1]).toHaveTextContent('Ratón ergonómico')
    expect(filas()[2]).toHaveTextContent('Webcam Full HD')
  })

  it('HU14-CP33: cada favorito muestra su imagen o, si no tiene, un marcador', async () => {
    await abrirMisFavoritos()

    await screen.findByRole('list', { name: 'Tus favoritos' })
    expect(within(filas()[0]).getByRole('img', { name: 'Auriculares inalámbricos' })).toHaveAttribute(
      'src',
      'https://tienda.test/auriculares.png',
    )
    expect(within(filas()[1]).getByRole('img', { name: /sin imagen/i })).toBeInTheDocument()
  })

  it('HU14-CP33: el nombre de un producto disponible lleva a su detalle', async () => {
    await abrirMisFavoritos()

    await screen.findByRole('list', { name: 'Tus favoritos' })
    expect(within(filas()[0]).getByRole('link', { name: 'Auriculares inalámbricos' })).toHaveAttribute(
      'href',
      '/products/p-1',
    )
  })

  it('HU14-CP33: un producto agotado lo indica y sigue llevando a su detalle', async () => {
    await abrirMisFavoritos()

    await screen.findByRole('list', { name: 'Tus favoritos' })
    expect(within(filas()[1]).getByText('Agotado')).toBeInTheDocument()
    expect(within(filas()[1]).getByRole('link', { name: 'Ratón ergonómico' })).toHaveAttribute('href', '/products/p-3')
  })

  it('HU14-CP33: un producto que ya no está disponible lo indica y no lleva a su detalle', async () => {
    await abrirMisFavoritos()

    await screen.findByRole('list', { name: 'Tus favoritos' })
    expect(within(filas()[2]).getByText('No disponible')).toBeInTheDocument()
    expect(within(filas()[2]).queryByRole('link')).not.toBeInTheDocument()
    expect(within(filas()[0]).queryByText(/agotado|no disponible/i)).not.toBeInTheDocument()
  })
})
