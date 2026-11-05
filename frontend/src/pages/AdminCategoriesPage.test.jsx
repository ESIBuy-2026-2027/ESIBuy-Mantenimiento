import { describe, it, expect } from 'vitest'
import { screen, waitFor, within } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { http, HttpResponse } from 'msw'
import { server } from '../mocks/server'
import { abrirSesionPorApi, renderApp } from '../test/renderApp'
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
})

describe('AdminCategoriesPage (HU40): crear', () => {
  it('HU40-CP11: la vista tiene el botón Nueva categoría, que abre el formulario de creación', async () => {
    const user = userEvent.setup()
    await abrirListado()

    await user.click(screen.getByRole('button', { name: 'Nueva categoría' }))

    expect(await screen.findByRole('dialog', { name: 'Nueva categoría' })).toBeInTheDocument()
  })

  it('HU40-CP11: sin ninguna categoría también se puede crear la primera', async () => {
    server.use(http.get(URL_CATEGORIAS, () => HttpResponse.json([])))
    await abrirSesionPorApi('admin@esibuy.test')
    renderWithProviders(<AdminCategoriesPage />)

    await screen.findByRole('heading', { name: /todavía no hay categorías/i })
    expect(screen.getByRole('button', { name: 'Nueva categoría' })).toBeInTheDocument()
  })

  it('HU40-CP13: al crearla, avisa y la categoría aparece en la lista con el estado elegido', async () => {
    const user = userEvent.setup()
    await abrirListado()

    await user.click(screen.getByRole('button', { name: 'Nueva categoría' }))
    const dialogo = await screen.findByRole('dialog', { name: 'Nueva categoría' })
    await user.type(within(dialogo).getByRole('textbox', { name: /nombre/i }), 'Jardín')
    await user.click(within(dialogo).getByRole('radio', { name: 'Inactiva' }))
    await user.click(within(dialogo).getByRole('button', { name: 'Crear' }))

    expect(await screen.findByText('Jardín se ha creado.')).toBeInTheDocument()
    await waitFor(() => expect(nombresEnPantalla()).toContain('Jardín'))
    expect(filasDeDatos()).toHaveLength(9)
    expect(celdasDe('Jardín')).toEqual(['Jardín', '—', 'Inactiva', '0', ''])
    await waitFor(() => expect(screen.queryByRole('dialog')).not.toBeInTheDocument())
  })

  it('HU40-CP14: no se puede crear una categoría repetida, aunque cambien las mayúsculas o las tildes', async () => {
    const user = userEvent.setup()
    await abrirListado()

    await user.click(screen.getByRole('button', { name: 'Nueva categoría' }))
    const dialogo = await screen.findByRole('dialog', { name: 'Nueva categoría' })
    await user.type(within(dialogo).getByRole('textbox', { name: /nombre/i }), 'TECNOLOGIA')
    await user.click(within(dialogo).getByRole('button', { name: 'Crear' }))

    expect(await within(dialogo).findByText('Ya existe una categoría con ese nombre.')).toBeInTheDocument()
    expect(filasDeDatos()).toHaveLength(8)
  })
})

describe('AdminCategoriesPage (HU41): editar', () => {
  it('HU41-CP16: cada fila de la lista tiene el lápiz para editar su categoría', async () => {
    await abrirListado()

    expect(screen.getAllByRole('button', { name: /^editar /i })).toHaveLength(8)
    expect(within(filaDe('Hogar')).getByRole('button', { name: 'Editar Hogar' })).toBeEnabled()
  })

  it('HU41-CP17: al guardar, avisa y la lista muestra el nombre y la descripción nuevos', async () => {
    const user = userEvent.setup()
    await abrirListado()

    await user.click(screen.getByRole('button', { name: 'Editar Libros' }))
    const dialogo = await screen.findByRole('dialog', { name: 'Editar categoría' })
    const nombre = within(dialogo).getByRole('textbox', { name: /nombre/i })
    await user.clear(nombre)
    await user.type(nombre, 'Libros y cómics')
    await user.type(within(dialogo).getByRole('textbox', { name: /descripción/i }), 'Novelas y tebeos.')
    await user.click(within(dialogo).getByRole('button', { name: 'Guardar' }))

    // Es el recorrido más largo (borrar y escribir dos campos): con toda la batería a la vez puede tardar más.
    expect(await screen.findByText('Libros y cómics se ha actualizado.', {}, { timeout: 3000 })).toBeInTheDocument()
    await waitFor(() => expect(nombresEnPantalla()).toContain('Libros y cómics'))
    expect(nombresEnPantalla()).not.toContain('Libros')
    expect(celdasDe('Libros y cómics').slice(0, 4)).toEqual(['Libros y cómics', 'Novelas y tebeos.', 'Activa', '11'])
  })

  it('HU41-CP18: no se puede poner el nombre de otra categoría, aunque cambien las mayúsculas o las tildes', async () => {
    const user = userEvent.setup()
    await abrirListado()

    await user.click(screen.getByRole('button', { name: 'Editar Libros' }))
    const dialogo = await screen.findByRole('dialog', { name: 'Editar categoría' })
    const nombre = within(dialogo).getByRole('textbox', { name: /nombre/i })
    await user.clear(nombre)
    await user.type(nombre, 'alimentacion')
    await user.click(within(dialogo).getByRole('button', { name: 'Guardar' }))

    expect(await within(dialogo).findByText('Ya existe una categoría con ese nombre.')).toBeInTheDocument()
    expect(nombresEnPantalla()).toContain('Libros')
  })
})

describe('AdminCategoriesPage (HU41): activar y desactivar', () => {
  it('HU41-CP28: cada fila ofrece activar o desactivar su categoría según su estado', async () => {
    await abrirListado()

    expect(within(filaDe('Hogar')).getByRole('button', { name: 'Desactivar Hogar' })).toBeEnabled()
    expect(within(filaDe('Juguetes')).getByRole('button', { name: 'Activar Juguetes' })).toBeEnabled()
    expect(screen.getAllByRole('button', { name: /^(activar|desactivar) /i })).toHaveLength(8)
  })

  it('HU41-CP29: al desactivar una categoría, avisa y la lista la muestra inactiva', async () => {
    const user = userEvent.setup()
    await abrirListado()

    await user.click(screen.getByRole('button', { name: 'Desactivar Hogar' }))
    const confirmacion = (await screen.findByText(/seguro que quieres desactivar la categoría hogar/i)).closest(
      '[role="alertdialog"]',
    )
    await user.click(within(confirmacion).getByText('Desactivar', { selector: 'button' }))

    expect(await screen.findByText('Hogar se ha desactivado.', {}, { timeout: 3000 })).toBeInTheDocument()
    await waitFor(() => expect(celdasDe('Hogar')[2]).toBe('Inactiva'))
    expect(within(filaDe('Hogar')).getByRole('button', { name: 'Activar Hogar' })).toBeEnabled()
  })
})

describe('AdminCategoriesPage (HU42): eliminar', () => {
  it('HU42-CP17: cada fila de la lista tiene la papelera para eliminar su categoría', async () => {
    await abrirListado()

    expect(screen.getAllByRole('button', { name: /^eliminar /i })).toHaveLength(8)
    expect(within(filaDe('Mascotas')).getByRole('button', { name: 'Eliminar Mascotas' })).toBeInTheDocument()
  })

  it('HU42-CP18: al confirmar, avisa de que se ha eliminado y la categoría desaparece de la lista', async () => {
    const user = userEvent.setup()
    await abrirListado()

    await user.click(screen.getByRole('button', { name: 'Eliminar Mascotas' }))
    const confirmacion = (await screen.findByText(/seguro que quieres eliminar la categoría mascotas/i)).closest(
      '[role="alertdialog"]',
    )
    await user.click(within(confirmacion).getByText('Eliminar', { selector: 'button' }))

    expect(await screen.findByText('Mascotas se ha eliminado.')).toBeInTheDocument()
    await waitFor(() => expect(nombresEnPantalla()).not.toContain('Mascotas'))
    expect(filasDeDatos()).toHaveLength(7)
  })

  it('HU42-CP19: solo se pueden eliminar las categorías sin productos', async () => {
    await abrirListado()

    expect(within(filaDe('Tecnología')).getByRole('button', { name: 'Eliminar Tecnología' })).toBeDisabled()
    expect(within(filaDe('Juguetes')).getByRole('button', { name: 'Eliminar Juguetes' })).toBeDisabled()
    expect(within(filaDe('Mascotas')).getByRole('button', { name: 'Eliminar Mascotas' })).toBeEnabled()
    expect(within(filaDe('Moda')).getByRole('button', { name: 'Eliminar Moda' })).toBeEnabled()
  })

  it('HU42-CP20: si el backend no deja eliminarla, avisa del motivo y la categoría sigue en la lista', async () => {
    const user = userEvent.setup()
    server.use(
      http.delete(`${URL_CATEGORIAS}/:id`, () => HttpResponse.json({ code: 'CATEGORY_HAS_SELLERS' }, { status: 409 })),
    )
    await abrirListado()

    await user.click(screen.getByRole('button', { name: 'Eliminar Mascotas' }))
    const confirmacion = (await screen.findByText(/seguro que quieres eliminar la categoría mascotas/i)).closest(
      '[role="alertdialog"]',
    )
    await user.click(within(confirmacion).getByText('Eliminar', { selector: 'button' }))

    expect(
      await screen.findByText('No se puede eliminar Mascotas porque es la categoría principal de algún vendedor.'),
    ).toBeInTheDocument()
    expect(nombresEnPantalla()).toContain('Mascotas')
    expect(filasDeDatos()).toHaveLength(8)
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

describe('AdminCategoriesPage (HU39): acceso', () => {
  it('HU39-CP15: Volver al panel regresa al panel de control', async () => {
    const user = userEvent.setup()
    await abrirSesionPorApi('admin@esibuy.test')
    renderApp('/admin/categories')
    await screen.findByRole('table')

    await user.click(screen.getByRole('link', { name: /volver al panel/i }))

    await waitFor(() => expect(screen.getByTestId('ruta-actual')).toHaveTextContent(/^\/admin$/))
  })

  it('HU39-CP16: sin sesión redirige al login', async () => {
    renderApp('/admin/categories')

    await waitFor(() => expect(screen.getByTestId('ruta-actual')).toHaveTextContent(/^\/login$/))
  })

  it('HU39-CP16: un vendedor no ve el listado y va a su zona', async () => {
    await abrirSesionPorApi('vendedor@esibuy.test')
    renderApp('/admin/categories')

    await waitFor(() => expect(screen.getByTestId('ruta-actual')).toHaveTextContent(/^\/seller\/products$/))
    expect(screen.queryByRole('heading', { name: 'Categorías' })).not.toBeInTheDocument()
  })

  it('HU39-CP16: un cliente no ve el listado y va a su zona', async () => {
    await abrirSesionPorApi('cliente@esibuy.test')
    renderApp('/admin/categories')

    await waitFor(() => expect(screen.getByTestId('ruta-actual')).toHaveTextContent(/^\/products$/))
    expect(screen.queryByRole('heading', { name: 'Categorías' })).not.toBeInTheDocument()
  })
})
