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

// Guarda los parámetros de cada petición al catálogo y responde con unos productos de ejemplo.
const PRODUCTOS_DE_EJEMPLO = [
  { id: 'p-1', nombre: 'Teclado mecánico', imagen: '', categoriaId: 'cat-1', valoracion: 4.5, numValoraciones: 12 },
  { id: 'p-2', nombre: 'Cafetera de cápsulas', imagen: '', categoriaId: 'cat-2', valoracion: null, numValoraciones: 0 },
]

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
const nombresDeLasTarjetas = () => tarjetas().map((tarjeta) => within(tarjeta).getAllByText(/./)[0].textContent)
const valoracionesDeLasTarjetas = () =>
  tarjetas().map((tarjeta) => {
    const estrellas = within(tarjeta).queryByRole('img', { name: /valoración: /i })
    return estrellas ? Number(estrellas.getAttribute('aria-label').match(/(\d),(\d)/).slice(1).join('.')) : 0
  })

describe('ProductsPage (HU7): búsqueda, filtros y orden', () => {
  it('HU7-CP06: por defecto pide los productos ordenados por nombre de la A a la Z y sin filtros', async () => {
    const peticiones = capturarPeticiones()
    await abrirCatalogo()

    await waitFor(() => expect(peticiones.length).toBeGreaterThan(0))
    const peticion = peticiones[0]
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
    await esperarTarjetas(12)

    await user.type(screen.getByRole('searchbox', { name: /buscar por nombre/i }), 'cafetera')

    await waitFor(() => expect(tarjetas()).toHaveLength(1))
    expect(screen.getByText('Cafetera de cápsulas')).toBeInTheDocument()
  })

  it('HU7-CP04: la búsqueda no distingue mayúsculas ni tildes', async () => {
    const user = userEvent.setup()
    await abrirCatalogo()
    await esperarTarjetas(12)

    await user.type(screen.getByRole('searchbox', { name: /buscar por nombre/i }), 'LAMPARA')

    await waitFor(() => expect(nombresDeLasTarjetas()).toEqual(['Lámpara de escritorio LED']))
  })

  it('HU7-CP04: al elegir una categoría pide solo los productos de esa categoría', async () => {
    const user = userEvent.setup()
    const peticiones = capturarPeticiones()
    await abrirCatalogo()
    await screen.findByRole('option', { name: 'Hogar' })

    await user.selectOptions(screen.getByRole('combobox', { name: /categoría/i }), 'Hogar')

    await waitFor(() => expect(ultima(peticiones).get('category')).toBe('cat-2'))
  })

  it('HU7-CP04: al filtrar vuelve a la primera página', async () => {
    const user = userEvent.setup()
    const peticiones = capturarPeticiones()
    await abrirCatalogo()
    await screen.findByRole('option', { name: 'Hogar' })

    await user.selectOptions(screen.getByRole('combobox', { name: /categoría/i }), 'Hogar')

    await waitFor(() => expect(ultima(peticiones).get('page')).toBe('1'))
  })

  it('HU7-CP04: si ningún producto coincide lo indica y permite limpiar los filtros', async () => {
    const user = userEvent.setup()
    await abrirCatalogo()
    await esperarTarjetas(12)

    await user.type(screen.getByRole('searchbox', { name: /buscar por nombre/i }), 'zzzz')

    expect(await screen.findByText(/no hay productos que coincidan/i)).toBeInTheDocument()
    await user.click(screen.getByRole('button', { name: /limpiar filtros/i }))

    await esperarTarjetas(12)
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
    await esperarTarjetas(12)

    await user.selectOptions(screen.getByRole('combobox', { name: /valoración mínima/i }), '4 estrellas o más')

    await waitFor(() => expect(tarjetas().length).toBeLessThan(12))
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

  it('HU7-CP06: permite ordenar por nombre, categoría y valoración', async () => {
    const user = userEvent.setup()
    const peticiones = capturarPeticiones()
    await abrirCatalogo()
    const selector = await screen.findByRole('combobox', { name: /ordenar por/i })

    expect(within(selector).getAllByRole('option').map((opcion) => opcion.textContent)).toEqual([
      'Nombre',
      'Categoría',
      'Valoración',
    ])
    await user.selectOptions(selector, 'Categoría')
    await waitFor(() => expect(ultima(peticiones).get('sort')).toBe('categoria'))
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

    await esperarTarjetas(12)
    const nombres = nombresDeLasTarjetas()
    expect(nombres).toEqual([...nombres].sort((a, b) => a.localeCompare(b, 'es')))
  })

  it('HU7-CP06: ordenados por valoración descendente, el mejor valorado sale primero', async () => {
    const user = userEvent.setup()
    await abrirCatalogo()
    await esperarTarjetas(12)

    await user.selectOptions(screen.getByRole('combobox', { name: /ordenar por/i }), 'Valoración')
    await user.click(screen.getByRole('button', { name: /orden ascendente/i }))

    await waitFor(() => expect(screen.getByRole('button', { name: /orden descendente/i })).toBeInTheDocument())
    await waitFor(() => {
      const valoraciones = valoracionesDeLasTarjetas()
      expect(valoraciones[0]).toBeGreaterThanOrEqual(4.5)
      expect(valoraciones).toEqual([...valoraciones].sort((a, b) => b - a))
    })
  })

  it('HU7-CP06: ordenados por categoría, los de Hogar salen antes que los de Tecnología', async () => {
    const user = userEvent.setup()
    await abrirCatalogo()
    await esperarTarjetas(12)

    await user.selectOptions(screen.getByRole('combobox', { name: /ordenar por/i }), 'Categoría')

    await waitFor(() => {
      const categorias = tarjetas().map((tarjeta) => (within(tarjeta).queryByText('Hogar') ? 'Hogar' : 'Tecnología'))
      expect(categorias[0]).toBe('Hogar')
      expect(categorias).toEqual([...categorias].sort((a, b) => a.localeCompare(b, 'es')))
    })
  })
})
