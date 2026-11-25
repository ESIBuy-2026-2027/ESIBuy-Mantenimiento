import { afterEach, beforeEach, describe, expect, it } from 'vitest'
import { act, fireEvent, screen, waitFor, within } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { http, HttpResponse } from 'msw'
import { server } from '../mocks/server'
import { abrirSesionPorApi, renderApp } from '../test/renderApp'

const CLIENTE = 'cliente@esibuy.test'
const TAMANO_PAGINA = '10'

// IntersectionObserver de mentira: no avisa solo, los tests dicen cuándo se llega al final de la página.
let observadores = []
class IntersectionObserverFalso {
  constructor(callback) {
    this.callback = callback
    this.elementos = new Set()
    observadores.push(this)
  }

  observe(elemento) {
    this.elementos.add(elemento)
  }

  unobserve(elemento) {
    this.elementos.delete(elemento)
  }

  disconnect() {
    this.elementos.clear()
    observadores = observadores.filter((observador) => observador !== this)
  }
}

async function llegarAlFinal() {
  await waitFor(() => expect(observadores.some((observador) => observador.elementos.size > 0)).toBe(true))
  act(() => {
    for (const observador of [...observadores]) {
      if (observador.elementos.size > 0) observador.callback([{ isIntersecting: true }])
    }
  })
}

const original = globalThis.IntersectionObserver
beforeEach(() => {
  observadores = []
  globalThis.IntersectionObserver = IntersectionObserverFalso
})
afterEach(() => {
  globalThis.IntersectionObserver = original
})

// El catálogo abre directamente el listado completo; el argumento se conserva para simplificar los casos existentes.
async function abrirCatalogo() {
  await abrirSesionPorApi(CLIENTE)
  renderApp('/products')
  await screen.findByRole('heading', { level: 1, name: /productos/i })
  await screen.findByRole('searchbox', { name: /buscar por nombre/i })
}

const tarjetas = () => within(screen.getByRole('list', { name: /productos/i })).getAllByRole('listitem')

async function esperarTarjetas(cantidad) {
  await waitFor(() => expect(tarjetas()).toHaveLength(cantidad))
}

const respuesta = (items) =>
  HttpResponse.json({
    items,
    page: 1,
    size: Number(TAMANO_PAGINA),
    totalItems: items.length,
    totalPages: items.length ? 1 : 0,
  })

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

    await esperarTarjetas(10)
    const tarjeta = within(screen.getByRole('list', { name: /productos/i })).getByText('Auriculares inalámbricos').closest('li')
    expect(within(tarjeta).getByText('Tecnología')).toBeInTheDocument()
    expect(within(tarjeta).getByRole('img', { name: /valoración: \d,\d de 5/i })).toBeInTheDocument()
    expect(within(tarjeta).getByRole('img', { name: 'Auriculares inalámbricos' })).toBeInTheDocument()
  })

  it('HU7-CP03: muestra las diez primeras tarjetas y solo los productos disponibles', async () => {
    await abrirCatalogo()

    await esperarTarjetas(10)
    expect(screen.queryByText('Webcam Full HD')).not.toBeInTheDocument()
    expect(screen.queryByRole('navigation', { name: /paginación/i })).not.toBeInTheDocument()
  })

  it('HU7-CP03: las tarjetas se colocan en cuatro columnas en escritorio', async () => {
    await abrirCatalogo()

    await esperarTarjetas(10)
    expect(screen.getByRole('list', { name: /productos/i })).toHaveStyle({
      gridTemplateColumns: 'repeat(4, minmax(0, 1fr))',
    })
  })

  it('HU7-CP03: al llegar al final de la página carga otras diez debajo de las anteriores', async () => {
    await abrirCatalogo()
    await esperarTarjetas(10)
    const primera = tarjetas()[0]

    await llegarAlFinal()

    await esperarTarjetas(20)
    expect(tarjetas()[0]).toBe(primera)
  })

  it('HU7-CP03: al llegar otra vez al final carga las últimas y avisa de que no hay más', async () => {
    await abrirCatalogo()
    await esperarTarjetas(10)
    await llegarAlFinal()
    await esperarTarjetas(20)

    await llegarAlFinal()

    await esperarTarjetas(23)
    expect(screen.getByText(/has visto todos los productos/i)).toBeInTheDocument()
    expect(screen.queryByRole('button', { name: /ver más productos/i })).not.toBeInTheDocument()
  })

  it('HU7-CP03: el botón «Ver más productos» también carga más, para quien no use el scroll', async () => {
    const user = userEvent.setup()
    await abrirCatalogo()
    await esperarTarjetas(10)

    await user.click(screen.getByRole('button', { name: /ver más productos/i }))

    await waitFor(() => expect(tarjetas()).toHaveLength(20))
  })

  it('HU7-CP03: si falla al cargar más, avisa sin perder las tarjetas y permite reintentar', async () => {
    const user = userEvent.setup()
    let fallado = false
    server.use(
      http.get('/api/products', ({ request }) => {
        if (new URL(request.url).searchParams.get('page') === '2' && !fallado) {
          fallado = true
          return new HttpResponse(null, { status: 500 })
        }
        return undefined
      }),
    )
    await abrirCatalogo()
    await esperarTarjetas(10)

    await llegarAlFinal()

    expect(await screen.findByText(/no se han podido cargar más productos/i)).toBeInTheDocument()
    expect(tarjetas()).toHaveLength(10)
    await user.click(screen.getByRole('button', { name: /reintentar/i }))
    await esperarTarjetas(20)
  })

  it('HU7-CP03: si falla la carga avisa y permite reintentar', async () => {
    const user = userEvent.setup()
    let fallado = false
    server.use(
      http.get('/api/products', ({ request }) => {
        // Solo falla el listado completo: ni la cinta de ofertas ni las filas por categoría
        const parametros = new URL(request.url).searchParams
        const esListadoCompleto = parametros.get('sort') === 'nombre' && !parametros.has('category') && parametros.get('size') === '10'
        if (esListadoCompleto && !fallado) {
          fallado = true
          return new HttpResponse(null, { status: 500 })
        }
        return undefined
      }),
    )
    await abrirSesionPorApi(CLIENTE)
    renderApp('/products')
    expect(await screen.findByText(/no se han podido cargar los productos/i)).toBeInTheDocument()
    await user.click(screen.getByRole('button', { name: /reintentar/i }))

    await esperarTarjetas(10)
  })

  it('HU7-CP03: si todavía no hay productos lo indica', async () => {
    server.use(http.get('/api/products', () => respuesta([])))
    await abrirCatalogo()

    expect(await screen.findByText(/todavía no hay productos/i)).toBeInTheDocument()
  })
})

const PRODUCTOS_DE_EJEMPLO = [
  { id: 'p-1', nombre: 'Teclado mecánico', imagen: '', categoriaId: 'cat-1', valoracion: 4.5, numValoraciones: 12 },
  { id: 'p-2', nombre: 'Cafetera de cápsulas', imagen: '', categoriaId: 'cat-2', valoracion: null, numValoraciones: 0 },
]

// Guarda los parámetros de cada petición al catálogo (las del listado y las de la selección de los mejores) y
// responde con unos productos de ejemplo. La selección se pide al montar la página, antes de cualquier interacción.
function capturarPeticiones(items = PRODUCTOS_DE_EJEMPLO) {
  const peticiones = []
  server.use(
    http.get('/api/products', ({ request }) => {
      peticiones.push(new URL(request.url).searchParams)
      return respuesta(items)
    }),
  )
  return peticiones
}

const ultima = (peticiones) => peticiones[peticiones.length - 1]
// En cada tarjeta el primer texto es la categoría y el segundo, el nombre del producto.
const nombresDeLasTarjetas = () => tarjetas().map((tarjeta) => within(tarjeta).getAllByText(/./)[1].textContent)
const valoracionesDeLasTarjetas = () =>
  tarjetas().map((tarjeta) => {
    const estrellas = within(tarjeta).queryByRole('img', { name: /valoración: /i })
    return estrellas ? Number(estrellas.getAttribute('aria-label').match(/(\d),(\d)/).slice(1).join('.')) : 0
  })

async function elegirCategoria(user, nombre) {
  await user.click(await screen.findByRole('combobox', { name: /categoría/i }))
  await user.click(await screen.findByRole('option', { name: nombre }))
}

describe('ProductsPage (HU7): búsqueda, filtros y orden', () => {
  it('HU7-CP06: por defecto pide diez productos ordenados por nombre de la A a la Z y sin filtros', async () => {
    const peticiones = capturarPeticiones()
    await abrirCatalogo()

    await waitFor(() => expect(peticiones.some((peticion) => peticion.get('sort') === 'nombre')).toBe(true))
    const peticion = peticiones.find((candidata) => candidata.get('sort') === 'nombre')
    expect(peticion.get('sort')).toBe('nombre')
    expect(peticion.get('order')).toBe('asc')
    expect(peticion.get('page')).toBe('1')
    expect(peticion.has('search')).toBe(false)
    expect(peticion.has('category')).toBe(false)
    expect(peticion.has('minRating')).toBe(false)
  })

  it('HU7-CP04: al escribir en el buscador pide los productos con ese nombre', async () => {
    const user = userEvent.setup()
    await abrirCatalogo()
    await esperarTarjetas(10)

    await user.type(screen.getByRole('searchbox', { name: /buscar por nombre/i }), 'cafetera')

    await waitFor(() => expect(tarjetas()).toHaveLength(1))
    expect(screen.getByText('Cafetera de cápsulas')).toBeInTheDocument()
  })

  it('HU7-CP04: la búsqueda no distingue mayúsculas ni tildes', async () => {
    const user = userEvent.setup()
    await abrirCatalogo()
    await esperarTarjetas(10)

    await user.type(screen.getByRole('searchbox', { name: /buscar por nombre/i }), 'LAMPARA')

    await waitFor(() => expect(nombresDeLasTarjetas()).toEqual(['Lámpara de escritorio LED']))
  })

  it('HU7-CP04: al elegir una categoría pide solo los productos de esa categoría', async () => {
    const user = userEvent.setup()
    const peticiones = capturarPeticiones()
    await abrirCatalogo()

    await elegirCategoria(user, 'Hogar')

    await waitFor(() => expect(ultima(peticiones).get('category')).toBe('cat-2'))
  })

  it('HU7-CP04: al filtrar vuelve a empezar desde la primera página, sin conservar lo cargado', async () => {
    const user = userEvent.setup()
    await abrirCatalogo()
    await esperarTarjetas(10)
    await llegarAlFinal()
    await esperarTarjetas(20)

    await elegirCategoria(user, 'Hogar')

    await waitFor(() => expect(tarjetas()).toHaveLength(9))
  })

  it('HU7-CP04: si ningún producto coincide lo indica y permite limpiar los filtros', async () => {
    const user = userEvent.setup()
    await abrirCatalogo()
    await esperarTarjetas(10)

    await user.type(screen.getByRole('searchbox', { name: /buscar por nombre/i }), 'zzzz')

    expect(await screen.findByText(/no hay productos que coincidan/i)).toBeInTheDocument()
    await user.click(screen.getByRole('button', { name: /limpiar filtros/i }))

    await esperarTarjetas(10)
    expect(screen.getByRole('searchbox', { name: /buscar por nombre/i })).toHaveValue('')
  })

  it('HU7-CP05: al elegir una valoración mínima pide solo los productos con esa valoración o más', async () => {
    const user = userEvent.setup()
    const peticiones = capturarPeticiones()
    await abrirCatalogo()

    await user.selectOptions(await screen.findByRole('combobox', { name: /valoración mínima/i }), '4 estrellas o más')

    await waitFor(() => expect(ultima(peticiones).get('minRating')).toBe('4'))
  })

  it('HU7-CP05: con la valoración mínima, todos los productos mostrados la alcanzan', async () => {
    const user = userEvent.setup()
    await abrirCatalogo()
    await esperarTarjetas(10)

    await user.selectOptions(screen.getByRole('combobox', { name: /valoración mínima/i }), '4 estrellas o más')

    await waitFor(() => expect(tarjetas().length).toBeLessThan(10))
    expect(valoracionesDeLasTarjetas().every((valoracion) => valoracion >= 4)).toBe(true)
  })

  it('HU7-CP05: al volver a «Cualquiera» deja de filtrar por valoración', async () => {
    const user = userEvent.setup()
    const peticiones = capturarPeticiones()
    await abrirCatalogo()
    const selector = await screen.findByRole('combobox', { name: /valoración mínima/i })

    await user.selectOptions(selector, '3 estrellas o más')
    await waitFor(() => expect(ultima(peticiones).get('minRating')).toBe('3'))
    await user.selectOptions(selector, 'Cualquiera')

    await waitFor(() => expect(ultima(peticiones).has('minRating')).toBe(false))
  })

  it('HU7-CP06: permite ordenar por nombre y por valoración, y no por categoría', async () => {
    const user = userEvent.setup()
    const peticiones = capturarPeticiones()
    await abrirCatalogo()
    const selector = await screen.findByRole('combobox', { name: /ordenar por/i })

    expect(within(selector).getAllByRole('option').map((opcion) => opcion.textContent)).toEqual([
      'Nombre',
      'Valoración',
    ])
    await user.selectOptions(selector, 'Valoración')
    await waitFor(() => expect(ultima(peticiones).get('sort')).toBe('valoracion'))
  })

  it('HU7-CP06: el botón de sentido alterna entre ascendente y descendente', async () => {
    const user = userEvent.setup()
    const peticiones = capturarPeticiones()
    await abrirCatalogo()

    await user.click(await screen.findByRole('button', { name: /orden ascendente/i }))

    await waitFor(() => expect(ultima(peticiones).get('order')).toBe('desc'))
    expect(screen.getByRole('button', { name: /orden descendente/i })).toBeInTheDocument()
  })

  it('HU7-CP06: ordenados por nombre, los productos salen de la A a la Z', async () => {
    await abrirCatalogo()

    await esperarTarjetas(10)
    const nombres = nombresDeLasTarjetas()
    expect(nombres).toEqual([...nombres].sort((a, b) => a.localeCompare(b, 'es')))
  })

  it('HU7-CP06: ordenados por valoración descendente, el mejor valorado sale primero', async () => {
    const user = userEvent.setup()
    await abrirCatalogo()
    await esperarTarjetas(10)

    await user.selectOptions(screen.getByRole('combobox', { name: /ordenar por/i }), 'Valoración')
    await user.click(screen.getByRole('button', { name: /orden ascendente/i }))

    await waitFor(() => expect(screen.getByRole('button', { name: /orden descendente/i })).toBeInTheDocument())
    await waitFor(() => {
      const valoraciones = valoracionesDeLasTarjetas()
      expect(valoraciones[0]).toBeGreaterThanOrEqual(4.5)
      expect(valoraciones).toEqual([...valoraciones].sort((a, b) => b - a))
    })
  })
})

describe('ProductsPage (HU8): detalle de un producto', () => {
  const abrirAuriculares = (user) =>
    user.click(within(screen.getByRole('list', { name: /productos/i })).getByRole('link', { name: /auriculares/i }))

  it('HU8-CP06: al pulsar una tarjeta del listado se abre el detalle de ese producto', async () => {
    const user = userEvent.setup()
    await abrirCatalogo()
    await esperarTarjetas(10)

    await abrirAuriculares(user)

    expect(await screen.findByRole('heading', { level: 1, name: 'Auriculares inalámbricos' })).toBeInTheDocument()
    expect(screen.getByTestId('ruta-actual')).toHaveTextContent(/^\/products\/p-1$/)
  })

  it('HU8-CP06: desde el detalle, «Volver a los productos» lleva otra vez al listado', async () => {
    const user = userEvent.setup()
    await abrirCatalogo()
    await esperarTarjetas(10)
    await abrirAuriculares(user)
    await screen.findByRole('heading', { level: 1, name: 'Auriculares inalámbricos' })

    await user.click(screen.getByRole('link', { name: /volver a los productos/i }))

    expect(await screen.findByRole('heading', { level: 1, name: /productos/i })).toBeInTheDocument()
    expect(screen.getByTestId('ruta-actual')).toHaveTextContent(/^\/products$/)
  })
})

describe('ProductsPage (HU9): precio y carrito', () => {
  const CARRITO = {
    grupos: [
      {
        vendedor: { id: 'u-2', nombreComercial: 'TecnoVera' },
        total: 53.91,
        lineas: [
          { productoId: 'p-1', nombre: 'Auriculares inalámbricos', imagen: '', cantidad: 1, precioUnitario: 53.91, subtotal: 53.91, stock: 34 },
        ],
      },
    ],
    totalProductos: 1,
    total: 53.91,
  }

  const tarjetaDeAuriculares = () =>
    within(screen.getByRole('list', { name: /productos/i })).getByRole('link', { name: /auriculares/i }).closest('li')

  it('HU9-CP01: las tarjetas del catálogo muestran el precio de cada producto, con su descuento', async () => {
    await abrirCatalogo()
    await esperarTarjetas(10)

    const tarjeta = tarjetaDeAuriculares()
    expect(within(tarjeta).getByText(/53,91/)).toBeInTheDocument()
    expect(within(tarjeta).getByText(/59,90/).closest('s')).not.toBeNull()
    expect(within(tarjeta).getByText('-10 %')).toBeInTheDocument()
  })

  it('HU9-CP02: al pulsar «Añadir al carrito» en una tarjeta se envía ese producto al carrito', async () => {
    const user = userEvent.setup()
    let cuerpo = null
    server.use(
      http.post('/api/customer/cart/items', async ({ request }) => {
        cuerpo = await request.json()
        return HttpResponse.json(CARRITO)
      }),
    )
    await abrirCatalogo()
    await esperarTarjetas(10)

    await user.click(within(tarjetaDeAuriculares()).getByRole('button', { name: /añadir al carrito/i }))

    await waitFor(() => expect(cuerpo).toEqual({ productoId: 'p-1', cantidad: 1 }))
    // La confirmación es que el producto pasa a estar en el carrito, con sus unidades
    expect(await within(tarjetaDeAuriculares()).findByText(/en tu carrito: \d+/i)).toBeInTheDocument()
    expect(screen.getByTestId('ruta-actual')).toHaveTextContent(/^\/products$/)
  })

  it('HU9-CP02: si el servidor dice que no hay existencias, la tarjeta lo avisa', async () => {
    const user = userEvent.setup()
    server.use(
      http.post('/api/customer/cart/items', () =>
        HttpResponse.json({ code: 'INSUFFICIENT_STOCK' }, { status: 409 }),
      ),
    )
    await abrirCatalogo()
    await esperarTarjetas(10)

    await user.click(within(tarjetaDeAuriculares()).getByRole('button', { name: /añadir al carrito/i }))

    expect(await within(tarjetaDeAuriculares()).findByRole('alert')).toHaveTextContent(/no hay existencias suficientes/i)
  })
})

describe('ProductsPage (HU14): favoritos', () => {
  it('HU14-CP21: cada tarjeta del catálogo tiene el corazón para añadir su producto a favoritos', async () => {
    await abrirCatalogo()
    await esperarTarjetas(10)

    for (const tarjeta of tarjetas()) {
      expect(within(tarjeta).getByRole('button', { name: /^añadir .+ a favoritos$/i })).toBeEnabled()
    }
  })

  it('HU14-CP22: al pulsar el corazón se añade el producto a favoritos y el corazón queda relleno', async () => {
    const user = userEvent.setup()
    let cuerpo = null
    server.use(
      http.post('/api/customer/wishlist/items', async ({ request }) => {
        cuerpo = await request.json()
        return HttpResponse.json({ items: [{ productoId: 'p-1', addedAt: '2026-11-04T10:00:00Z' }] })
      }),
    )
    await abrirCatalogo()
    await esperarTarjetas(10)

    await user.click(screen.getByRole('button', { name: 'Añadir Auriculares inalámbricos a favoritos' }))

    expect(
      await screen.findByRole('button', { name: 'Quitar Auriculares inalámbricos de favoritos' }),
    ).toBeEnabled()
    expect(cuerpo).toEqual({ productoId: 'p-1' })
    expect(screen.getByTestId('ruta-actual')).toHaveTextContent(/^\/products$/)
  })

  it('HU14-CP23: al añadir, se rellenan también los corazones de los productos que ya estaban en la lista del cliente', async () => {
    const user = userEvent.setup()
    server.use(
      http.post('/api/customer/wishlist/items', () =>
        HttpResponse.json({
          items: [
            { productoId: 'p-10', addedAt: '2026-11-01T09:00:00Z' },
            { productoId: 'p-1', addedAt: '2026-11-04T10:00:00Z' },
          ],
        }),
      ),
    )
    await abrirCatalogo()
    await esperarTarjetas(10)

    await user.click(screen.getByRole('button', { name: 'Añadir Auriculares inalámbricos a favoritos' }))

    expect(await screen.findByRole('button', { name: 'Quitar Cafetera de cápsulas de favoritos' })).toBeEnabled()
    expect(screen.getByRole('button', { name: 'Quitar Auriculares inalámbricos de favoritos' })).toBeEnabled()
    expect(screen.getByRole('button', { name: 'Añadir Altavoz Bluetooth a favoritos' })).toBeEnabled()
  })

  it('HU14-CP36: al entrar en el catálogo, los productos que ya están en favoritos tienen el corazón relleno', async () => {
    server.use(
      http.get('/api/customer/wishlist', () =>
        HttpResponse.json({
          items: [
            {
              productoId: 'p-10',
              nombre: 'Cafetera de cápsulas',
              imagen: '',
              precioFinal: 79.99,
              stock: 9,
              disponible: true,
              addedAt: '2026-11-01T09:00:00Z',
            },
          ],
        }),
      ),
    )
    await abrirCatalogo()
    await esperarTarjetas(10)

    expect(await screen.findByRole('button', { name: 'Quitar Cafetera de cápsulas de favoritos' })).toBeInTheDocument()
    expect(screen.getByRole('button', { name: 'Añadir Auriculares inalámbricos a favoritos' })).toBeEnabled()
  })

  it('HU14-CP36: si no se pueden cargar los favoritos, el catálogo funciona con los corazones vacíos', async () => {
    server.use(http.get('/api/customer/wishlist', () => new HttpResponse(null, { status: 500 })))
    await abrirCatalogo()
    await esperarTarjetas(10)

    for (const tarjeta of tarjetas()) {
      expect(within(tarjeta).getByRole('button', { name: /^añadir .+ a favoritos$/i })).toBeEnabled()
    }
    expect(screen.queryByRole('alert')).not.toBeInTheDocument()
  })

  it('HU14-CP24: si el producto ya no está disponible, su tarjeta lo avisa y el corazón sigue vacío', async () => {
    const user = userEvent.setup()
    server.use(
      http.post('/api/customer/wishlist/items', () =>
        HttpResponse.json({ code: 'PRODUCT_NOT_FOUND' }, { status: 404 }),
      ),
    )
    await abrirCatalogo()
    await esperarTarjetas(10)
    const tarjeta = within(screen.getByRole('list', { name: /productos/i }))
      .getByRole('link', { name: /auriculares/i })
      .closest('li')

    await user.click(within(tarjeta).getByRole('button', { name: 'Añadir Auriculares inalámbricos a favoritos' }))

    expect(await within(tarjeta).findByRole('alert')).toHaveTextContent('Este producto ya no está disponible.')
    expect(within(tarjeta).getByRole('button', { name: 'Añadir Auriculares inalámbricos a favoritos' })).toBeEnabled()
  })

  it('HU14-CP37: al pulsar el corazón relleno se quita el producto de favoritos y el corazón queda vacío', async () => {
    const user = userEvent.setup()
    let quitado = null
    const cafetera = {
      productoId: 'p-10',
      nombre: 'Cafetera de cápsulas',
      imagen: '',
      precioFinal: 79.99,
      stock: 9,
      disponible: true,
      addedAt: '2026-11-01T09:00:00Z',
    }
    server.use(
      http.get('/api/customer/wishlist', () => HttpResponse.json({ items: [cafetera] })),
      http.delete('/api/customer/wishlist/items/:productoId', ({ params }) => {
        quitado = params.productoId
        return HttpResponse.json({ items: [] })
      }),
    )
    await abrirCatalogo()
    await esperarTarjetas(10)

    await user.click(await screen.findByRole('button', { name: 'Quitar Cafetera de cápsulas de favoritos' }))

    expect(await screen.findByRole('button', { name: 'Añadir Cafetera de cápsulas a favoritos' })).toBeEnabled()
    expect(quitado).toBe('p-10')
    expect(screen.getByTestId('ruta-actual')).toHaveTextContent(/^\/products$/)
  })
})

describe('ProductsPage (HU7): catálogo con ofertas y listado único', () => {
  it('muestra las ofertas en la parte superior y no muestra carruseles por categoría ni top 10', async () => {
    await abrirCatalogo()

    expect(await screen.findByRole('list', { name: /productos en oferta/i })).toBeInTheDocument()
    expect(screen.getByRole('heading', { level: 2, name: 'Ofertas' })).toBeInTheDocument()
    expect(screen.queryByRole('heading', { name: /los 10 mejor valorados/i })).not.toBeInTheDocument()
    expect(screen.queryByRole('list', { name: /productos de tecnología/i })).not.toBeInTheDocument()
    expect(screen.getByRole('list', { name: /^productos$/i })).toBeInTheDocument()
  })

  it('permite pausar la cinta de ofertas', async () => {
    const user = userEvent.setup()
    await abrirCatalogo()
    const ofertas = await screen.findByRole('list', { name: /productos en oferta/i })

    await user.click(screen.getByRole('button', { name: 'Pausar' }))

    expect(screen.getByRole('button', { name: 'Reanudar' })).toHaveAttribute('aria-pressed', 'true')
    expect(ofertas).toHaveStyle({ animationPlayState: 'paused' })
  })

  it('mantiene visibles los filtros y el listado completo al entrar', async () => {
    await abrirCatalogo()

    expect(screen.getByRole('searchbox', { name: /buscar por nombre/i })).toBeInTheDocument()
    expect(screen.getByRole('list', { name: /^productos$/i })).toBeInTheDocument()
    expect(screen.queryByRole('button', { name: /ver todos los productos/i })).not.toBeInTheDocument()
    expect(screen.queryByRole('button', { name: /ver por categorías/i })).not.toBeInTheDocument()
  })

  it('coloca el panel de filtros antes de las ofertas', async () => {
    await abrirCatalogo()

    const filtros = screen.getByRole('region', { name: /filtros del catálogo/i })
    const ofertas = screen.getByRole('heading', { level: 2, name: 'Ofertas' })
    expect(filtros.compareDocumentPosition(ofertas) & Node.DOCUMENT_POSITION_FOLLOWING).toBeTruthy()
  })

  it('permite filtrar por un rango de precio y envía ambos límites', async () => {
    const peticiones = capturarPeticiones()
    await abrirCatalogo()

    fireEvent.change(screen.getByRole('slider', { name: 'Precio mínimo' }), { target: { value: '25' } })
    fireEvent.change(screen.getByRole('slider', { name: 'Precio máximo' }), { target: { value: '100' } })

    await waitFor(() => {
      expect(peticiones.some((peticion) => peticion.get('minPrice') === '25' && peticion.get('maxPrice') === '100')).toBe(true)
    })
  })

  it('permite ordenar por precio y envía el criterio al catálogo', async () => {
    const peticiones = capturarPeticiones()
    await abrirCatalogo()

    await userEvent.selectOptions(screen.getByRole('combobox', { name: 'Ordenar por' }), 'precio')

    await waitFor(() => {
      expect(peticiones.some((peticion) => peticion.get('sort') === 'precio')).toBe(true)
    })
  })

  it('permite filtrar por stock y envía el criterio al catálogo', async () => {
    const user = userEvent.setup()
    const peticiones = capturarPeticiones()
    await abrirCatalogo()

    await user.click(screen.getByRole('checkbox', { name: 'Solo en stock' }))

    await waitFor(() => {
      expect(peticiones.some((peticion) => peticion.get('size') === '10' && peticion.get('inStock') === 'true')).toBe(true)
    })
    expect(screen.getByRole('checkbox', { name: 'Solo en stock' })).toBeChecked()
  })

  it('permite filtrar por ofertas y envía el criterio al catálogo', async () => {
    const user = userEvent.setup()
    const peticiones = capturarPeticiones()
    await abrirCatalogo()

    await user.click(screen.getByRole('checkbox', { name: 'Solo ofertas' }))

    await waitFor(() => {
      expect(peticiones.some((peticion) => peticion.get('size') === '10' && peticion.get('offersOnly') === 'true')).toBe(true)
    })
    expect(screen.getByRole('checkbox', { name: 'Solo ofertas' })).toBeChecked()
  })

  it('limpia conjuntamente búsqueda, valoración, stock y ofertas', async () => {
    const user = userEvent.setup()
    await abrirCatalogo()

    await user.type(screen.getByRole('searchbox', { name: /buscar por nombre/i }), 'teclado')
    await user.selectOptions(screen.getByRole('combobox', { name: /valoración mínima/i }), '4 estrellas o más')
    await user.click(screen.getByRole('checkbox', { name: 'Solo en stock' }))
    await user.click(screen.getByRole('checkbox', { name: 'Solo ofertas' }))
    fireEvent.change(screen.getByRole('slider', { name: 'Precio mínimo' }), { target: { value: '25' } })
    await user.click(screen.getByRole('button', { name: /limpiar filtros/i }))

    expect(screen.getByRole('searchbox', { name: /buscar por nombre/i })).toHaveValue('')
    expect(screen.getByRole('combobox', { name: /valoración mínima/i })).toHaveValue('')
    expect(screen.getByRole('checkbox', { name: 'Solo en stock' })).not.toBeChecked()
    expect(screen.getByRole('checkbox', { name: 'Solo ofertas' })).not.toBeChecked()
    expect(screen.getByRole('slider', { name: 'Precio mínimo' })).toHaveValue('0')
    expect(screen.getByRole('slider', { name: 'Precio máximo' })).toHaveValue('150')
    expect(screen.getByRole('slider', { name: 'Precio mínimo' })).toHaveValue('0')
    expect(screen.getByRole('slider', { name: 'Precio máximo' })).toHaveValue('150')
  })

  it('permite interactuar con las tarjetas repetidas del carrusel de ofertas', async () => {
    await abrirCatalogo()
    const ofertas = await screen.findByRole('list', { name: /productos en oferta/i })
    const tarjetasOferta = within(ofertas).getAllByRole('listitem')

    expect(tarjetasOferta.length).toBeGreaterThan(3)
    for (const tarjeta of tarjetasOferta) {
      expect(within(tarjeta).getByRole('link')).toBeEnabled()
    }
  })
})
