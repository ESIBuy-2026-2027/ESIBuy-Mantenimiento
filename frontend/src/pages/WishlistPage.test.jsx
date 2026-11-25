import { describe, it, expect } from 'vitest'
import { cleanup, screen, waitFor, within } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { delay, http, HttpResponse } from 'msw'
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

    await waitFor(() => expect(screen.getByTestId('ruta-actual')).toHaveTextContent(new RegExp('^/seller/products$')))
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

describe('WishlistPage (HU14): carga, lista vacía y error', () => {
  it('HU14-CP34: mientras carga muestra un indicador', async () => {
    server.use(
      http.get(URL_LISTA, async () => {
        await delay('infinite')
      }),
    )
    await abrirSesionPorApi('cliente@esibuy.test')
    renderApp('/wishlist')

    expect(await screen.findByText('Cargando tus favoritos…')).toBeInTheDocument()
  })

  it('HU14-CP34: sin favoritos lo indica y ofrece ir a los productos', async () => {
    await abrirMisFavoritos({ items: [] })

    expect(await screen.findByText('Aún no tienes favoritos')).toBeInTheDocument()
    expect(screen.getByRole('link', { name: 'Ver los productos' })).toHaveAttribute('href', '/products')
    expect(screen.queryByRole('list', { name: 'Tus favoritos' })).not.toBeInTheDocument()
  })

  it('HU14-CP34: si no se pueden cargar los favoritos lo avisa y permite reintentar', async () => {
    const user = userEvent.setup()
    server.use(http.get(URL_LISTA, () => HttpResponse.json(LISTA)))
    server.use(http.get(URL_LISTA, () => new HttpResponse(null, { status: 500 }), { once: true }))
    await abrirSesionPorApi('cliente@esibuy.test')
    renderApp('/wishlist')

    expect(await screen.findByText('No se han podido cargar tus favoritos. Inténtalo de nuevo.')).toBeInTheDocument()
    await user.click(screen.getByRole('button', { name: 'Reintentar' }))

    await screen.findByRole('list', { name: 'Tus favoritos' })
    expect(filas()).toHaveLength(3)
  })
})

describe('WishlistPage (HU14): quitar de favoritos', () => {
  const sinProducto = (productoId) => ({ items: LISTA.items.filter((item) => item.productoId !== productoId) })

  it('HU14-CP35: cada favorito, también uno que ya no está disponible, tiene su botón para quitarlo', async () => {
    await abrirMisFavoritos()

    await screen.findByRole('list', { name: 'Tus favoritos' })
    expect(within(filas()[0]).getByRole('button', { name: 'Quitar Auriculares inalámbricos de favoritos' })).toBeEnabled()
    expect(within(filas()[2]).getByRole('button', { name: 'Quitar Webcam Full HD de favoritos' })).toBeEnabled()
  })

  it('HU14-CP35: al quitar un favorito pide quitar ese producto, avisa y desaparece de la lista', async () => {
    const user = userEvent.setup()
    let quitado = null
    server.use(
      http.delete(`${URL_LISTA}/items/:productoId`, ({ params }) => {
        quitado = params.productoId
        return HttpResponse.json(sinProducto(params.productoId))
      }),
    )
    await abrirMisFavoritos()
    await screen.findByRole('list', { name: 'Tus favoritos' })

    await user.click(screen.getByRole('button', { name: 'Quitar Ratón ergonómico de favoritos' }))

    expect(await screen.findByText('Ratón ergonómico se ha quitado de tus favoritos.')).toBeInTheDocument()
    expect(quitado).toBe('p-3')
    expect(filas()).toHaveLength(2)
    expect(screen.queryByRole('link', { name: 'Ratón ergonómico' })).not.toBeInTheDocument()
  })

  it('HU14-CP35: mientras se quita, su botón queda deshabilitado', async () => {
    const user = userEvent.setup()
    server.use(
      http.delete(`${URL_LISTA}/items/:productoId`, async () => {
        await delay('infinite')
      }),
    )
    await abrirMisFavoritos()
    await screen.findByRole('list', { name: 'Tus favoritos' })

    await user.click(screen.getByRole('button', { name: 'Quitar Webcam Full HD de favoritos' }))

    expect(screen.getByRole('button', { name: 'Quitar Webcam Full HD de favoritos' })).toBeDisabled()
  })

  it('HU14-CP35: al quitar el último favorito se muestra el estado vacío', async () => {
    const user = userEvent.setup()
    server.use(http.delete(`${URL_LISTA}/items/:productoId`, () => HttpResponse.json({ items: [] })))
    await abrirMisFavoritos({ items: [LISTA.items[0]] })
    await screen.findByRole('list', { name: 'Tus favoritos' })

    await user.click(screen.getByRole('button', { name: 'Quitar Auriculares inalámbricos de favoritos' }))

    expect(await screen.findByText('Aún no tienes favoritos')).toBeInTheDocument()
  })

  it('HU14-CP35: si no se puede quitar, lo avisa y el producto sigue en la lista', async () => {
    const user = userEvent.setup()
    server.use(http.delete(`${URL_LISTA}/items/:productoId`, () => new HttpResponse(null, { status: 500 })))
    await abrirMisFavoritos()
    await screen.findByRole('list', { name: 'Tus favoritos' })

    await user.click(screen.getByRole('button', { name: 'Quitar Ratón ergonómico de favoritos' }))

    expect(
      await screen.findByText('No se ha podido quitar Ratón ergonómico de tus favoritos. Inténtalo de nuevo.'),
    ).toBeInTheDocument()
    expect(filas()).toHaveLength(3)
    expect(screen.getByRole('button', { name: 'Quitar Ratón ergonómico de favoritos' })).toBeEnabled()
  })
})

// Pestaña «Categorías» de Mis favoritos (AB#528): las mismas favoritas que el detalle del producto y el perfil. Del mock
// de categorías: Tecnología (cat-1) y Hogar (cat-2), las dos activas.
describe('WishlistPage (AB#528): categorías favoritas', () => {
  const URL_FAVORITAS = '/api/customer/preferences'
  const conFavoritas = (...categorias) => server.use(http.get(URL_FAVORITAS, () => HttpResponse.json({ categorias })))
  const categorias = () =>
    within(screen.getByRole('list', { name: 'Tus categorías favoritas' }))
      .getAllByRole('listitem')
      .map((fila) => fila.textContent)

  async function abrirCategorias(user) {
    await abrirMisFavoritos()
    await user.click(screen.getByRole('tab', { name: 'Categorías' }))
  }

  it('HU528-CP24: Mis favoritos tiene dos pestañas y se abre en Productos', async () => {
    conFavoritas('cat-1')
    await abrirMisFavoritos()

    expect(screen.getByRole('tab', { name: 'Productos' })).toHaveAttribute('aria-selected', 'true')
    expect(screen.getByRole('tab', { name: 'Categorías' })).toHaveAttribute('aria-selected', 'false')
    expect(await screen.findByRole('list', { name: 'Tus favoritos' })).toBeInTheDocument()
    expect(screen.queryByRole('list', { name: 'Tus categorías favoritas' })).not.toBeInTheDocument()
  })

  it('HU528-CP24: la pestaña Categorías muestra las favoritas por su nombre, en el orden en que se marcaron', async () => {
    const user = userEvent.setup()
    conFavoritas('cat-2', 'cat-1')
    await abrirCategorias(user)

    expect(await screen.findByRole('list', { name: 'Tus categorías favoritas' })).toBeInTheDocument()
    expect(categorias()).toEqual(['Hogar', 'Tecnología'])
    expect(screen.queryByRole('list', { name: 'Tus favoritos' })).not.toBeInTheDocument()
  })

  it('HU528-CP24: se puede volver a la pestaña Productos', async () => {
    const user = userEvent.setup()
    await abrirCategorias(user)

    await user.click(screen.getByRole('tab', { name: 'Productos' }))

    expect(await screen.findByRole('list', { name: 'Tus favoritos' })).toBeInTheDocument()
  })

  it('HU528-CP25: mientras cargan las categorías muestra un indicador', async () => {
    const user = userEvent.setup()
    server.use(
      http.get(URL_FAVORITAS, async () => {
        await delay('infinite')
      }),
    )
    await abrirCategorias(user)

    expect(screen.getByText('Cargando tus categorías favoritas…')).toBeInTheDocument()
  })

  it('HU528-CP25: sin categorías favoritas lo indica y ofrece ir a los productos', async () => {
    const user = userEvent.setup()
    await abrirCategorias(user)

    expect(await screen.findByText('Aún no tienes categorías favoritas')).toBeInTheDocument()
    await user.click(screen.getByRole('link', { name: 'Ver los productos' }))
    await esperarRuta('/products')
  })

  it.each([
    ['las favoritas', URL_FAVORITAS, { categorias: ['cat-1'] }],
    ['las categorías', '/api/categorias', [{ id: 'cat-1', nombre: 'Tecnología' }]],
  ])('HU528-CP25: si no se pueden cargar %s, lo avisa y permite reintentar', async (_caso, url, datos) => {
    const user = userEvent.setup()
    let falla = true
    conFavoritas('cat-1')
    server.use(http.get(url, () => (falla ? new HttpResponse(null, { status: 500 }) : HttpResponse.json(datos))))
    await abrirCategorias(user)

    expect(
      await screen.findByText('No se han podido cargar tus categorías favoritas. Inténtalo de nuevo.'),
    ).toBeInTheDocument()
    falla = false
    await user.click(screen.getByRole('button', { name: 'Reintentar' }))

    expect(await screen.findByRole('list', { name: 'Tus categorías favoritas' })).toBeInTheDocument()
    expect(categorias()).toEqual(['Tecnología'])
  })

  it('HU528-CP26: al quitar una categoría se pide quitar solo esa y desaparece de la lista', async () => {
    const user = userEvent.setup()
    let quitada = null
    conFavoritas('cat-1', 'cat-2')
    server.use(
      http.delete('/api/customer/preferences/categories/:categoriaId', ({ params }) => {
        quitada = params.categoriaId
        return HttpResponse.json({ categorias: ['cat-2'] })
      }),
    )
    await abrirCategorias(user)

    await user.click(await screen.findByRole('button', { name: 'Quitar Tecnología de tus categorías favoritas' }))

    await waitFor(() => expect(categorias()).toEqual(['Hogar']))
    expect(quitada).toBe('cat-1')
  })

  it('HU528-CP26: mientras se quita una, su botón queda deshabilitado', async () => {
    const user = userEvent.setup()
    conFavoritas('cat-1')
    server.use(
      http.delete('/api/customer/preferences/categories/:categoriaId', async () => {
        await delay('infinite')
      }),
    )
    await abrirCategorias(user)
    const boton = await screen.findByRole('button', { name: 'Quitar Tecnología de tus categorías favoritas' })

    await user.click(boton)

    expect(boton).toBeDisabled()
  })

  it('HU528-CP26: al quitar la última se muestra el estado vacío', async () => {
    const user = userEvent.setup()
    conFavoritas('cat-1')
    server.use(
      http.delete('/api/customer/preferences/categories/:categoriaId', () => HttpResponse.json({ categorias: [] })),
    )
    await abrirCategorias(user)

    await user.click(await screen.findByRole('button', { name: 'Quitar Tecnología de tus categorías favoritas' }))

    expect(await screen.findByText('Aún no tienes categorías favoritas')).toBeInTheDocument()
  })

  it('HU528-CP28: al quitar una categoría avisa con su nombre, también si era la última', async () => {
    const user = userEvent.setup()
    conFavoritas('cat-1')
    server.use(
      http.delete('/api/customer/preferences/categories/:categoriaId', () => HttpResponse.json({ categorias: [] })),
    )
    await abrirCategorias(user)

    await user.click(await screen.findByRole('button', { name: 'Quitar Tecnología de tus categorías favoritas' }))

    const aviso = await screen.findByText('Tecnología se ha quitado de tus categorías favoritas.')
    expect(aviso.closest('output')).toBeInTheDocument()
    expect(screen.getByText('Aún no tienes categorías favoritas')).toBeInTheDocument()
  })

  it('HU528-CP28: si no se puede quitar, lo avisa y la categoría sigue en la lista', async () => {
    const user = userEvent.setup()
    conFavoritas('cat-1')
    server.use(
      http.delete('/api/customer/preferences/categories/:categoriaId', () => new HttpResponse(null, { status: 500 })),
    )
    await abrirCategorias(user)
    const boton = await screen.findByRole('button', { name: 'Quitar Tecnología de tus categorías favoritas' })

    await user.click(boton)

    expect(
      await screen.findByText('No se ha podido quitar Tecnología de tus categorías favoritas. Inténtalo de nuevo.'),
    ).toBeInTheDocument()
    expect(categorias()).toEqual(['Tecnología'])
    expect(boton).toBeEnabled()
  })

  it('HU528-CP29: una categoría añadida desde un producto aparece en Mis favoritos y en el perfil, y al quitarla aquí deja de serlo', async () => {
    const user = userEvent.setup()
    await abrirSesionPorApi('cliente@esibuy.test')
    renderApp('/products/p-1')
    await user.click(await screen.findByRole('button', { name: 'Añadir a favoritas la categoría Tecnología' }))
    await screen.findByText('Tecnología se ha añadido a tus categorías favoritas.')
    cleanup()

    renderApp('/profile')
    const perfil = await screen.findByRole('group', { name: 'Mis categorías favoritas' })
    await waitFor(() => expect(within(perfil).getByRole('checkbox', { name: 'Tecnología' })).toBeChecked())
    cleanup()

    renderApp('/wishlist')
    await user.click(await screen.findByRole('tab', { name: 'Categorías' }))
    await user.click(await screen.findByRole('button', { name: 'Quitar Tecnología de tus categorías favoritas' }))
    await screen.findByText('Tecnología se ha quitado de tus categorías favoritas.')
    cleanup()

    renderApp('/products/p-1')
    expect(await screen.findByRole('button', { name: 'Añadir a favoritas la categoría Tecnología' })).toBeEnabled()
  })
})
