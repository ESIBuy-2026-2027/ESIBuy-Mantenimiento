import { afterEach, beforeEach, describe, expect, it } from 'vitest'
import { act, screen, waitFor, within } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { http, HttpResponse } from 'msw'
import { server } from '../mocks/server'
import { abrirSesionPorApi, renderApp } from '../test/renderApp'

const CLIENTE = 'cliente@esibuy.test'
const TAMANO_PAGINA = '10'
const TAMANO_DESTACADOS = '8'

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

function llegarAlFinal() {
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
    const tarjeta = screen.getByText('Auriculares inalámbricos').closest('li')
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

  it('HU7-CP03: las tarjetas se colocan de dos en dos', async () => {
    await abrirCatalogo()

    await esperarTarjetas(10)
    expect(screen.getByRole('list', { name: /productos/i })).toHaveStyle({
      gridTemplateColumns: 'repeat(2, minmax(0, 1fr))',
    })
  })

  it('HU7-CP03: al llegar al final de la página carga otras diez debajo de las anteriores', async () => {
    await abrirCatalogo()
    await esperarTarjetas(10)
    const primera = tarjetas()[0]

    llegarAlFinal()

    await esperarTarjetas(20)
    expect(tarjetas()[0]).toBe(primera)
  })

  it('HU7-CP03: al llegar otra vez al final carga las últimas y avisa de que no hay más', async () => {
    await abrirCatalogo()
    await esperarTarjetas(10)
    llegarAlFinal()
    await esperarTarjetas(20)

    llegarAlFinal()

    await esperarTarjetas(23)
    expect(screen.getByText(/has visto todos los productos/i)).toBeInTheDocument()
    expect(screen.queryByRole('button', { name: /ver más productos/i })).not.toBeInTheDocument()
  })

  it('HU7-CP03: el botón «Ver más productos» también carga más, para quien no use el scroll', async () => {
    const user = userEvent.setup()
    await abrirCatalogo()
    await esperarTarjetas(10)

    await user.click(screen.getByRole('button', { name: /ver más productos/i }))

    await esperarTarjetas(20)
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

    llegarAlFinal()

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
        if (new URL(request.url).searchParams.get('size') === TAMANO_PAGINA && !fallado) {
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

// Guarda los parámetros de cada petición al catálogo (las del listado y las de la selección de los mejores,
// por separado) y responde con unos productos de ejemplo.
function capturarPeticiones(items = PRODUCTOS_DE_EJEMPLO) {
  const peticiones = { listado: [], destacados: [] }
  server.use(
    http.get('/api/products', ({ request }) => {
      const params = new URL(request.url).searchParams
      const destino = params.get('size') === TAMANO_DESTACADOS ? peticiones.destacados : peticiones.listado
      destino.push(params)
      return respuesta(items)
    }),
  )
  return peticiones
}

const ultima = (peticiones) => peticiones[peticiones.length - 1]
const nombresDeLasTarjetas = () => tarjetas().map((tarjeta) => within(tarjeta).getAllByText(/./)[0].textContent)
const valoracionesDeLasTarjetas = () =>
  tarjetas().map((tarjeta) => {
    const estrellas = within(tarjeta).queryByRole('img', { name: /valoración: /i })
    return estrellas ? Number(estrellas.getAttribute('aria-label').match(/(\d),(\d)/).slice(1).join('.')) : 0
  })

describe('ProductsPage (HU7): búsqueda, filtros y orden', () => {
  it('HU7-CP06: por defecto pide diez productos ordenados por nombre de la A a la Z y sin filtros', async () => {
    const { listado } = capturarPeticiones()
    await abrirCatalogo()

    await waitFor(() => expect(listado.length).toBeGreaterThan(0))
    const peticion = listado[0]
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
    const { listado } = capturarPeticiones()
    await abrirCatalogo()
    await screen.findByRole('option', { name: 'Hogar' })

    await user.selectOptions(screen.getByRole('combobox', { name: /categoría/i }), 'Hogar')

    await waitFor(() => expect(ultima(listado).get('category')).toBe('cat-2'))
  })

  it('HU7-CP04: al filtrar vuelve a empezar desde la primera página, sin conservar lo cargado', async () => {
    const user = userEvent.setup()
    await abrirCatalogo()
    await esperarTarjetas(10)
    llegarAlFinal()
    await esperarTarjetas(20)

    await user.selectOptions(screen.getByRole('combobox', { name: /categoría/i }), 'Hogar')

    await esperarTarjetas(9)
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
    const { listado } = capturarPeticiones()
    await abrirCatalogo()

    await user.selectOptions(await screen.findByRole('combobox', { name: /valoración mínima/i }), '4 estrellas o más')

    await waitFor(() => expect(ultima(listado).get('minRating')).toBe('4'))
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
    const { listado } = capturarPeticiones()
    await abrirCatalogo()
    const selector = await screen.findByRole('combobox', { name: /valoración mínima/i })

    await user.selectOptions(selector, '3 estrellas o más')
    await waitFor(() => expect(ultima(listado).get('minRating')).toBe('3'))
    await user.selectOptions(selector, 'Cualquiera')

    await waitFor(() => expect(ultima(listado).has('minRating')).toBe(false))
  })

  it('HU7-CP06: permite ordenar por nombre y por valoración, y no por categoría', async () => {
    const user = userEvent.setup()
    const { listado } = capturarPeticiones()
    await abrirCatalogo()
    const selector = await screen.findByRole('combobox', { name: /ordenar por/i })

    expect(within(selector).getAllByRole('option').map((opcion) => opcion.textContent)).toEqual([
      'Nombre',
      'Valoración',
    ])
    await user.selectOptions(selector, 'Valoración')
    await waitFor(() => expect(ultima(listado).get('sort')).toBe('valoracion'))
  })

  it('HU7-CP06: el botón de sentido alterna entre ascendente y descendente', async () => {
    const user = userEvent.setup()
    const { listado } = capturarPeticiones()
    await abrirCatalogo()

    await user.click(await screen.findByRole('button', { name: /orden ascendente/i }))

    await waitFor(() => expect(ultima(listado).get('order')).toBe('desc'))
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

describe('ProductsPage (HU7): selección de los mejores productos', () => {
  const MEJORES = /mejor valorados/i

  it('HU7-CP07: arriba muestra una selección de los productos mejor valorados con su valoración', async () => {
    await abrirCatalogo()

    expect(await screen.findByRole('heading', { level: 2, name: MEJORES })).toBeInTheDocument()
    const seleccion = screen.getByRole('list', { name: /selección destacada/i })
    const destacados = within(seleccion).getAllByRole('listitem')
    expect(destacados).toHaveLength(3)
    expect(within(destacados[0]).getByText('Teclado mecánico')).toBeInTheDocument()
    expect(within(destacados[0]).getByText('4,7')).toBeInTheDocument()
  })

  it('HU7-CP07: la selección pide los mejor valorados: valoración descendente y de 4 estrellas en adelante', async () => {
    const { destacados } = capturarPeticiones()
    await abrirCatalogo()

    await waitFor(() => expect(destacados.length).toBeGreaterThan(0))
    expect(destacados[0].get('sort')).toBe('valoracion')
    expect(destacados[0].get('order')).toBe('desc')
    expect(destacados[0].get('minRating')).toBe('4')
    expect(destacados[0].get('page')).toBe('1')
  })

  it('HU7-CP07: con algún filtro activo la selección se oculta para centrarse en los resultados', async () => {
    const user = userEvent.setup()
    await abrirCatalogo()
    await screen.findByRole('heading', { level: 2, name: MEJORES })

    await user.type(screen.getByRole('searchbox', { name: /buscar por nombre/i }), 'lampara')

    await waitFor(() => expect(screen.queryByRole('heading', { level: 2, name: MEJORES })).not.toBeInTheDocument())
  })

  it('HU7-CP07: si hay pocos productos con imagen no se muestra la selección', async () => {
    capturarPeticiones()
    await abrirCatalogo()

    await screen.findByRole('searchbox', { name: /buscar por nombre/i })
    expect(screen.queryByRole('heading', { level: 2, name: MEJORES })).not.toBeInTheDocument()
  })

  it('HU7-CP07: si la selección no carga, el listado sigue funcionando sin ella', async () => {
    server.use(
      http.get('/api/products', ({ request }) =>
        new URL(request.url).searchParams.get('size') === TAMANO_DESTACADOS
          ? new HttpResponse(null, { status: 500 })
          : undefined,
      ),
    )
    await abrirCatalogo()

    await esperarTarjetas(10)
    expect(screen.queryByRole('heading', { level: 2, name: MEJORES })).not.toBeInTheDocument()
  })
})
