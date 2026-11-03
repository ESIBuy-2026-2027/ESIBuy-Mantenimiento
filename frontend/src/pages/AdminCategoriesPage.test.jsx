import { describe, it, expect } from 'vitest'
import { screen, waitFor, within } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { http, HttpResponse } from 'msw'
import { server } from '../mocks/server'
import { abrirSesionPorApi } from '../test/renderApp'
import { renderWithProviders } from '../test/utils'
import AdminCategoriesPage from './AdminCategoriesPage'

// Del mock: 8 categorías ordenadas por nombre. Inactivas: Juguetes (3 productos) y Moda (sin descripción ni
// productos). Libros también va sin descripción y Mascotas, activa, sin productos. Hogar tiene 14 productos y una
// descripción larga; Tecnología es la que más tiene (18).
const URL_CATEGORIAS = '/api/admin/categorias'
const ERROR_DE_CARGA = 'No se han podido cargar las categorías. Inténtalo de nuevo.'

async function abrirListado() {
  await abrirSesionPorApi('admin@esibuy.test')
  renderWithProviders(<AdminCategoriesPage />)
  await screen.findByRole('table')
}

const filasDeDatos = () => within(screen.getByRole('table')).getAllByRole('row').slice(1)
const nombresEnPantalla = () => filasDeDatos().map((fila) => within(fila).getAllByRole('cell')[0].textContent)
const filaDe = (nombre) => filasDeDatos().find((fila) => within(fila).queryByText(nombre))
const celdasDe = (nombre) =>
  within(filaDe(nombre))
    .getAllByRole('cell')
    .map((celda) => celda.textContent)

describe('AdminCategoriesPage (HU39): listado', () => {
  it('HU39-CP12: muestra el título de la vista y mientras carga un indicador', async () => {
    await abrirSesionPorApi('admin@esibuy.test')
    renderWithProviders(<AdminCategoriesPage />)

    expect(screen.getByRole('heading', { level: 1, name: 'Categorías' })).toBeInTheDocument()
    expect(screen.getByText(/cargando categorías/i)).toBeInTheDocument()
    await screen.findByRole('table')
    expect(screen.queryByText(/cargando categorías/i)).not.toBeInTheDocument()
  })

  it('HU39-CP12: muestra todas las categorías, también las inactivas, ordenadas por nombre', async () => {
    await abrirListado()

    expect(nombresEnPantalla()).toEqual([
      'Alimentación',
      'Deportes',
      'Hogar',
      'Juguetes',
      'Libros',
      'Mascotas',
      'Moda',
      'Tecnología',
    ])
  })

  it('HU39-CP09: la tabla tiene las columnas Nombre, Descripción, Estado, Productos y Acciones', async () => {
    await abrirListado()

    expect(screen.getAllByRole('columnheader').map((cabecera) => cabecera.textContent)).toEqual([
      'Nombre',
      'Descripción',
      'Estado',
      'Productos',
      'Acciones',
    ])
  })

  it('HU39-CP09: cada fila muestra el nombre, la descripción, el estado y el número de productos', async () => {
    await abrirListado()

    expect(celdasDe('Hogar').slice(0, 4)).toEqual([
      'Hogar',
      'Muebles, decoración y menaje para dar vida a cualquier estancia. Incluye iluminación, textil del hogar y almacenamiento.',
      'Activa',
      '14',
    ])
    expect(celdasDe('Juguetes').slice(0, 4)).toEqual([
      'Juguetes',
      'Juegos y juguetes para todas las edades.',
      'Inactiva',
      '3',
    ])
  })

  it('HU39-CP10: una categoría sin descripción muestra un guion y una sin productos muestra 0', async () => {
    await abrirListado()

    expect(celdasDe('Moda').slice(0, 4)).toEqual(['Moda', '—', 'Inactiva', '0'])
  })

  it('HU39-CP12: sin categorías muestra el estado vacío y ningún filtro', async () => {
    server.use(http.get(URL_CATEGORIAS, () => HttpResponse.json([])))
    await abrirSesionPorApi('admin@esibuy.test')
    renderWithProviders(<AdminCategoriesPage />)

    expect(await screen.findByRole('heading', { name: /todavía no hay categorías/i })).toBeInTheDocument()
    expect(screen.queryByRole('table')).not.toBeInTheDocument()
    expect(screen.queryByLabelText('Buscar por nombre')).not.toBeInTheDocument()
  })
})

describe('AdminCategoriesPage (HU39): ficha de la categoría', () => {
  it('HU39-CP11: cada fila tiene un botón con lupa para ver la información de su categoría', async () => {
    await abrirListado()

    expect(screen.getAllByRole('button', { name: /^ver información de /i })).toHaveLength(8)
    expect(screen.getByRole('button', { name: 'Ver información de Hogar' })).toBeInTheDocument()
  })

  it('HU39-CP11: la lupa abre la ficha de esa categoría con su descripción completa y se puede cerrar', async () => {
    const user = userEvent.setup()
    await abrirListado()

    await user.click(screen.getByRole('button', { name: 'Ver información de Hogar' }))

    const dialogo = await screen.findByRole('dialog', { name: /información de la categoría/i })
    expect(within(dialogo).getByLabelText('Nombre')).toHaveValue('Hogar')
    expect(within(dialogo).getByLabelText('Descripción').value).toMatch(/almacenamiento\.$/)

    await user.click(within(dialogo).getByRole('button', { name: 'Cerrar' }))
    await waitFor(() => expect(screen.queryByRole('dialog')).not.toBeInTheDocument())
  })

  it('HU39-CP11: la vista no ofrece crear, editar ni eliminar categorías', async () => {
    await abrirListado()

    expect(screen.queryByRole('button', { name: /crear|nueva|editar|eliminar|borrar/i })).not.toBeInTheDocument()
    expect(screen.queryByRole('link', { name: /crear|nueva/i })).not.toBeInTheDocument()
  })
})

describe('AdminCategoriesPage (HU39): errores', () => {
  it('HU39-CP13: un 403 muestra el error genérico, sin el texto del servidor, y se puede reintentar', async () => {
    const user = userEvent.setup()
    server.use(
      http.get(URL_CATEGORIAS, () => HttpResponse.json({ code: 'ACCESS_DENIED' }, { status: 403 }), { once: true }),
    )
    await abrirSesionPorApi('admin@esibuy.test')
    renderWithProviders(<AdminCategoriesPage />)

    expect(await screen.findByText(ERROR_DE_CARGA)).toBeInTheDocument()
    expect(screen.queryByText(/ACCESS_DENIED|403/)).not.toBeInTheDocument()
    expect(screen.queryByRole('table')).not.toBeInTheDocument()

    await user.click(screen.getByRole('button', { name: 'Reintentar' }))

    expect(await screen.findByRole('table')).toBeInTheDocument()
    expect(screen.queryByText(ERROR_DE_CARGA)).not.toBeInTheDocument()
  })

  it('HU39-CP13: un error del servidor muestra el mismo mensaje genérico con Reintentar', async () => {
    server.use(http.get(URL_CATEGORIAS, () => new HttpResponse(null, { status: 500 })))
    await abrirSesionPorApi('admin@esibuy.test')
    renderWithProviders(<AdminCategoriesPage />)

    expect(await screen.findByText(ERROR_DE_CARGA)).toBeInTheDocument()
    expect(screen.getByRole('button', { name: 'Reintentar' })).toBeInTheDocument()
  })
})

describe('AdminCategoriesPage (HU39): búsqueda, estado y orden', () => {
  it('HU39-CP14: buscar por nombre no distingue mayúsculas ni tildes', async () => {
    const user = userEvent.setup()
    await abrirListado()

    await user.type(screen.getByLabelText('Buscar por nombre'), 'TECNOLOGIA')

    expect(nombresEnPantalla()).toEqual(['Tecnología'])
  })

  it('HU39-CP14: filtrar por estado deja solo las inactivas o solo las activas', async () => {
    const user = userEvent.setup()
    await abrirListado()

    await user.selectOptions(screen.getByLabelText('Estado'), 'Inactiva')
    expect(nombresEnPantalla()).toEqual(['Juguetes', 'Moda'])

    await user.selectOptions(screen.getByLabelText('Estado'), 'Activa')
    expect(filasDeDatos()).toHaveLength(6)
    expect(nombresEnPantalla()).not.toContain('Moda')
  })

  it('HU39-CP14: ordenar por productos y cambiar el sentido', async () => {
    const user = userEvent.setup()
    await abrirListado()

    await user.selectOptions(screen.getByLabelText('Ordenar por'), 'Productos')
    expect(nombresEnPantalla()[0]).toBe('Mascotas')
    expect(nombresEnPantalla().at(-1)).toBe('Tecnología')

    await user.click(screen.getByRole('button', { name: 'Orden ascendente' }))
    expect(nombresEnPantalla()[0]).toBe('Tecnología')
    expect(screen.getByRole('button', { name: 'Orden descendente' })).toBeInTheDocument()
  })

  it('HU39-CP14: sin coincidencias avisa y Limpiar filtros vuelve a mostrarlas todas, manteniendo el orden', async () => {
    const user = userEvent.setup()
    await abrirListado()
    await user.selectOptions(screen.getByLabelText('Ordenar por'), 'Productos')

    await user.type(screen.getByLabelText('Buscar por nombre'), 'zzzz')
    expect(
      await screen.findByRole('heading', { name: /no hay categorías que coincidan con los filtros/i }),
    ).toBeInTheDocument()
    expect(screen.queryByRole('table')).not.toBeInTheDocument()

    await user.click(screen.getByRole('button', { name: 'Limpiar filtros' }))

    expect(await screen.findByRole('table')).toBeInTheDocument()
    expect(filasDeDatos()).toHaveLength(8)
    expect(screen.getByLabelText('Ordenar por')).toHaveValue('products')
  })
})
