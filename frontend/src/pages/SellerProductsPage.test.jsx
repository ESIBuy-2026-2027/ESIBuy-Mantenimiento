import { describe, it, expect } from 'vitest'
import { screen, waitFor, within } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { http, HttpResponse } from 'msw'
import { server } from '../mocks/server'
import { abrirSesionPorApi, renderApp } from '../test/renderApp'
import { elegirCategoria } from '../test/utils'

const VENDEDOR = 'vendedor@esibuy.test'
const VENDEDOR_SIN_PRODUCTOS = 'vendedor-vacio@esibuy.test'

async function abrirListado(email = VENDEDOR) {
  await abrirSesionPorApi(email)
  renderApp('/seller/products')
  await screen.findByRole('heading', { level: 1, name: /mis productos/i })
}

function filasDeDatos() {
  return within(screen.getByRole('table')).getAllByRole('row').slice(1)
}

async function esperarFilas(cantidad) {
  await waitFor(() => expect(filasDeDatos()).toHaveLength(cantidad))
}

describe('SellerProductsPage (HU32): listado', () => {
  it('sellerProductsPage_conProductos_muestraLaPrimeraPaginaConDiezFilas', async () => {
    await abrirListado()

    await esperarFilas(10)
    expect(screen.getByText('Auriculares inalámbricos')).toBeInTheDocument()
    expect(screen.getByText(/mostrando 1–10 de 23/i)).toBeInTheDocument()
  })

  it('sellerProductsPage_muestraPrecioStockYEstadoDeCadaProducto', async () => {
    await abrirListado()

    const fila = (await screen.findByText('Teclado mecánico')).closest('tr')
    expect(within(fila).getByText(/89,50/)).toBeInTheDocument()
    expect(within(fila).getByText('12')).toBeInTheDocument()
    expect(within(fila).getByText('Disponible')).toBeInTheDocument()
  })

  it('sellerProductsPage_conProductoNoDisponible_loMarcaComoNoDisponible', async () => {
    await abrirListado()

    const fila = (await screen.findByText('Webcam Full HD')).closest('tr')
    expect(within(fila).getByText('No disponible')).toBeInTheDocument()
  })

  it('sellerProductsPage_traduceElIdentificadorDeCategoriaAlNombre', async () => {
    server.use(
      http.get('/api/seller/products', () =>
        HttpResponse.json({
          items: [
            { id: 'p-1', nombre: 'Teclado mecánico', precio: 89.5, stock: 12, disponible: true, categoriaId: 'cat-2' },
          ],
          page: 1,
          size: 10,
          totalItems: 1,
          totalPages: 1,
        }),
      ),
    )
    await abrirListado()

    const fila = (await screen.findByText('Teclado mecánico')).closest('tr')
    expect(await within(fila).findByText('Hogar')).toBeInTheDocument()
    expect(within(fila).queryByText('cat-2')).not.toBeInTheDocument()
    expect(screen.getByRole('columnheader', { name: 'Categoría' })).toBeInTheDocument()
  })

  it('sellerProductsPage_siLasCategoriasNoCargan_muestraUnGuionEnVezDelIdentificador', async () => {
    server.use(
      http.get('/api/categorias', () => new HttpResponse(null, { status: 500 })),
      http.get('/api/seller/products', () =>
        HttpResponse.json({
          items: [
            { id: 'p-1', nombre: 'Teclado mecánico', precio: 89.5, stock: 12, disponible: true, categoriaId: 'cat-2' },
          ],
          page: 1,
          size: 10,
          totalItems: 1,
          totalPages: 1,
        }),
      ),
    )
    await abrirListado()

    const fila = (await screen.findByText('Teclado mecánico')).closest('tr')
    expect(within(fila).getByText('—')).toBeInTheDocument()
    expect(within(fila).queryByText('cat-2')).not.toBeInTheDocument()
  })

  it('sellerProductsPage_soloMuestraLosProductosDelVendedorAutenticado', async () => {
    const user = userEvent.setup()
    await abrirListado()

    await user.type(await screen.findByLabelText(/buscar por nombre/i), 'smartwatch')

    expect(await screen.findByText(/no hay productos que coincidan/i)).toBeInTheDocument()
    expect(screen.queryByText('Smartwatch deportivo')).not.toBeInTheDocument()
  })

  it('sellerProductsPage_sinSesion_redirigeAlLogin', async () => {
    renderApp('/seller/products')

    await waitFor(() => expect(screen.getByTestId('ruta-actual')).toHaveTextContent(/^\/login$/))
  })

  it('sellerProductsPage_conSesionDeCliente_noMuestraElListado', async () => {
    await abrirSesionPorApi('cliente@esibuy.test')
    renderApp('/seller/products')

    await waitFor(() => expect(screen.getByTestId('ruta-actual')).toHaveTextContent(/^\/products$/))
    expect(screen.queryByRole('heading', { name: /mis productos/i })).not.toBeInTheDocument()
  })
})

describe('SellerProductsPage (HU32): añadir producto', () => {
  it('sellerProductsPage_conProductos_muestraElBotonAnadirProducto', async () => {
    await abrirListado()

    expect(screen.getByRole('link', { name: /añadir producto/i })).toHaveAttribute('href', '/seller/products/new')
  })

  it('sellerProductsPage_sinProductos_tambienMuestraElBotonAnadirProductoEnLaCabeceraDelListado', async () => {
    await abrirListado(VENDEDOR_SIN_PRODUCTOS)

    const cabecera = screen.getByRole('heading', { level: 1, name: /mis productos/i }).parentElement.parentElement
    expect(within(cabecera).getByRole('link', { name: /añadir producto/i })).toBeInTheDocument()
  })

  it('sellerProductsPage_alPulsarAnadirProducto_navegaAlFormularioDeAlta', async () => {
    const user = userEvent.setup()
    await abrirListado()

    await user.click(screen.getByRole('link', { name: /añadir producto/i }))

    await waitFor(() => expect(screen.getByTestId('ruta-actual')).toHaveTextContent(/^\/seller\/products\/new$/))
  })

  it('sellerProductsPage_laCabeceraDeLaAplicacion_noContieneElBotonAnadirProducto', async () => {
    await abrirListado()

    expect(within(screen.getByRole('banner')).queryByText(/añadir producto/i)).not.toBeInTheDocument()
  })
})

describe('SellerProductsPage (HU32): estados', () => {
  it('sellerProductsPage_sinProductos_muestraElEstadoVacioConAccesoDirecto', async () => {
    await abrirListado(VENDEDOR_SIN_PRODUCTOS)

    expect(await screen.findByRole('heading', { name: /todavía no tienes productos/i })).toBeInTheDocument()
    expect(screen.queryByRole('table')).not.toBeInTheDocument()
    expect(screen.getAllByRole('link', { name: /añadir producto/i })).toHaveLength(2)
  })

  it('sellerProductsPage_sinProductos_noMuestraFiltrosNiPaginacion', async () => {
    await abrirListado(VENDEDOR_SIN_PRODUCTOS)

    await screen.findByRole('heading', { name: /todavía no tienes productos/i })
    expect(screen.queryByLabelText(/buscar por nombre/i)).not.toBeInTheDocument()
    expect(screen.queryByRole('navigation', { name: /paginación/i })).not.toBeInTheDocument()
  })

  it('sellerProductsPage_mientrasCarga_indicaQueEstaCargando', async () => {
    await abrirSesionPorApi(VENDEDOR)
    renderApp('/seller/products')

    expect(await screen.findByText(/cargando tus productos/i)).toBeInTheDocument()
    await esperarFilas(10)
    expect(screen.queryByText(/cargando tus productos/i)).not.toBeInTheDocument()
  })

  it('sellerProductsPage_conErrorDeServidor_muestraMensajeSinDetallesInternosYPermiteReintentar', async () => {
    server.use(
      http.get(
        '/api/seller/products',
        () => HttpResponse.json({ message: 'MongoTimeoutException' }, { status: 500 }),
        { once: true },
      ),
    )
    const user = userEvent.setup()
    await abrirSesionPorApi(VENDEDOR)
    renderApp('/seller/products')

    expect(await screen.findByText(/no se han podido cargar tus productos/i)).toBeInTheDocument()
    expect(screen.queryByText(/mongotimeout/i)).not.toBeInTheDocument()

    await user.click(screen.getByRole('button', { name: /reintentar/i }))

    await esperarFilas(10)
  })
})

describe('SellerProductsPage (HU32): filtros', () => {
  it('sellerProductsPage_alBuscarPorNombre_filtraElListadoSinPulsarNingunBoton', async () => {
    const user = userEvent.setup()
    await abrirListado()

    await user.type(await screen.findByLabelText(/buscar por nombre/i), 'teclado')

    await esperarFilas(1)
    expect(screen.getByText('Teclado mecánico')).toBeInTheDocument()
  })

  it('sellerProductsPage_laBusqueda_ignoraMayusculasYAcentos', async () => {
    const user = userEvent.setup()
    await abrirListado()

    await user.type(await screen.findByLabelText(/buscar por nombre/i), 'LAMPARA')

    await esperarFilas(1)
    expect(screen.getByText('Lámpara de escritorio LED')).toBeInTheDocument()
  })

  it('sellerProductsPage_alFiltrarPorCategoria_muestraSoloLosProductosDeEsaCategoria', async () => {
    const user = userEvent.setup()
    await abrirListado()

    await elegirCategoria(user, 'Hogar')

    await waitFor(() => expect(filasDeDatos()).toHaveLength(9))
  })

  it('sellerProductsPage_combinaBusquedaYCategoria', async () => {
    const user = userEvent.setup()
    await abrirListado()

    await elegirCategoria(user, 'Hogar')
    await user.type(screen.getByLabelText(/buscar por nombre/i), 'lámpara')

    await esperarFilas(1)
    expect(screen.getByText('Lámpara de escritorio LED')).toBeInTheDocument()
  })

  it('sellerProductsPage_sinCoincidencias_ofreceLimpiarLosFiltros', async () => {
    const user = userEvent.setup()
    await abrirListado()

    await user.type(await screen.findByLabelText(/buscar por nombre/i), 'zzzz')
    expect(await screen.findByText(/no hay productos que coincidan/i)).toBeInTheDocument()
    await user.click(screen.getByRole('button', { name: /limpiar filtros/i }))

    await esperarFilas(10)
    expect(screen.getByLabelText(/buscar por nombre/i)).toHaveValue('')
  })

  it('sellerProductsPage_sinCoincidencias_noMuestraElEstadoVacioDeAltaDeProductos', async () => {
    const user = userEvent.setup()
    await abrirListado()

    await user.type(await screen.findByLabelText(/buscar por nombre/i), 'zzzz')

    await screen.findByText(/no hay productos que coincidan/i)
    expect(screen.queryByRole('heading', { name: /todavía no tienes productos/i })).not.toBeInTheDocument()
  })
})

describe('SellerProductsPage (HU32): paginación', () => {
  it('sellerProductsPage_alPulsarSiguiente_muestraLaSegundaPagina', async () => {
    const user = userEvent.setup()
    await abrirListado()
    await esperarFilas(10)

    await user.click(screen.getByRole('button', { name: /siguiente/i }))

    expect(await screen.findByText('Disco duro externo 2 TB')).toBeInTheDocument()
    expect(screen.queryByText('Auriculares inalámbricos')).not.toBeInTheDocument()
    expect(screen.getByText(/página 2 de 3/i)).toBeInTheDocument()
  })

  it('sellerProductsPage_laUltimaPagina_muestraLosProductosRestantes', async () => {
    const user = userEvent.setup()
    await abrirListado()
    await esperarFilas(10)

    await user.click(screen.getByRole('button', { name: /siguiente/i }))
    await screen.findByText(/página 2 de 3/i)
    await user.click(screen.getByRole('button', { name: /siguiente/i }))

    await esperarFilas(3)
    expect(screen.getByRole('button', { name: /siguiente/i })).toBeDisabled()
  })

  it('sellerProductsPage_alFiltrarDesdeOtraPagina_vuelveALaPrimera', async () => {
    const user = userEvent.setup()
    await abrirListado()
    await esperarFilas(10)

    await user.click(screen.getByRole('button', { name: /siguiente/i }))
    await screen.findByText(/página 2 de 3/i)
    await elegirCategoria(user, 'Tecnología')

    await waitFor(() => expect(screen.getByText(/página 1 de 2/i)).toBeInTheDocument())
  })

  it('sellerProductsPage_conPocosProductos_noMuestraLaPaginacion', async () => {
    const user = userEvent.setup()
    await abrirListado()

    await user.type(await screen.findByLabelText(/buscar por nombre/i), 'teclado')

    await esperarFilas(1)
    expect(screen.queryByRole('navigation', { name: /paginación/i })).not.toBeInTheDocument()
  })
})

describe('SellerProductsPage (HU31 y HU35): editar y eliminar desde el listado', () => {
  async function abrirListadoConFilas() {
    await abrirListado()
    await esperarFilas(10)
  }

  function capturarBorrado(respuesta = () => new HttpResponse(null, { status: 204 })) {
    const borrado = { id: null, llamadas: 0 }
    server.use(
      http.delete('/api/seller/products/:id', ({ params }) => {
        borrado.llamadas += 1
        borrado.id = params.id
        return respuesta()
      }),
    )
    return borrado
  }

  it('sellerProductsPage_cadaFilaTieneUnBotonParaEditarElProducto', async () => {
    await abrirListadoConFilas()

    expect(screen.getAllByRole('link', { name: /^editar /i })).toHaveLength(10)
    expect(screen.getByRole('link', { name: 'Editar Auriculares inalámbricos' })).toHaveAttribute(
      'href',
      '/seller/products/p-1/edit',
    )
  })

  it('HU35-CP08: cada fila tiene un botón para eliminar el producto', async () => {
    await abrirListadoConFilas()

    expect(screen.getAllByRole('button', { name: /^eliminar /i })).toHaveLength(10)
  })

  it('sellerProductsPage_alPulsarEditar_abreElFormularioConLosDatosDelProducto', async () => {
    const user = userEvent.setup()
    await abrirListadoConFilas()

    await user.click(screen.getByRole('link', { name: 'Editar Teclado mecánico' }))

    expect(await screen.findByRole('heading', { level: 1, name: /editar producto/i })).toBeInTheDocument()
    await waitFor(() => expect(screen.getByLabelText(/^nombre/i)).toHaveValue('Teclado mecánico'))
    expect(screen.getByTestId('ruta-actual')).toHaveTextContent(/^\/seller\/products\/p-2\/edit$/)
  })

  it('HU35-CP09: al pulsar Eliminar pide confirmación sin borrar nada', async () => {
    const user = userEvent.setup()
    const borrado = capturarBorrado()
    await abrirListadoConFilas()

    await user.click(screen.getByRole('button', { name: 'Eliminar Auriculares inalámbricos' }))

    const alerta = await screen.findByRole('alertdialog', { name: /eliminar producto/i })
    expect(within(alerta).getByText(/auriculares inalámbricos/i)).toBeInTheDocument()
    expect(within(alerta).getByText(/no se puede deshacer/i)).toBeInTheDocument()
    expect(borrado.llamadas).toBe(0)
  })

  it('HU35-CP10: al cancelar la eliminación, cierra la alerta sin borrar', async () => {
    const user = userEvent.setup()
    const borrado = capturarBorrado()
    await abrirListadoConFilas()
    await user.click(screen.getByRole('button', { name: 'Eliminar Auriculares inalámbricos' }))
    const alerta = await screen.findByRole('alertdialog', { name: /eliminar producto/i })

    await user.click(within(alerta).getByRole('button', { name: /^cancelar$/i }))

    await waitFor(() => expect(screen.queryByRole('alertdialog')).not.toBeInTheDocument())
    expect(borrado.llamadas).toBe(0)
    expect(screen.queryByText(/producto eliminado/i)).not.toBeInTheDocument()
  })

  it('HU35-CP11: al confirmar, pide borrar el producto indicado y avisa de que se ha eliminado', async () => {
    const user = userEvent.setup()
    const borrado = capturarBorrado()
    await abrirListadoConFilas()
    await user.click(screen.getByRole('button', { name: 'Eliminar Teclado mecánico' }))
    const alerta = await screen.findByRole('alertdialog', { name: /eliminar producto/i })

    await user.click(within(alerta).getByRole('button', { name: /^eliminar$/i }))

    await waitFor(() => expect(screen.queryByRole('alertdialog')).not.toBeInTheDocument())
    expect(borrado).toMatchObject({ llamadas: 1, id: 'p-2' })
    expect(await screen.findByText(/producto eliminado/i)).toBeInTheDocument()
  })

  it('HU35-CP12: tras eliminar, vuelve a cargar el listado', async () => {
    const user = userEvent.setup()
    let peticionesDelListado = 0
    server.events.on('request:start', ({ request }) => {
      if (request.method === 'GET' && new URL(request.url).pathname === '/api/seller/products') peticionesDelListado += 1
    })
    capturarBorrado()
    await abrirListadoConFilas()
    const antes = peticionesDelListado
    await user.click(screen.getByRole('button', { name: 'Eliminar Teclado mecánico' }))
    const alerta = await screen.findByRole('alertdialog', { name: /eliminar producto/i })

    await user.click(within(alerta).getByRole('button', { name: /^eliminar$/i }))

    try {
      await waitFor(() => expect(peticionesDelListado).toBeGreaterThan(antes))
    } finally {
      server.events.removeAllListeners()
    }
  })

  it('HU35-CP13: si falla la eliminación, avisa sin detalles internos y mantiene la alerta', async () => {
    const user = userEvent.setup()
    capturarBorrado(() => HttpResponse.json({ message: 'MongoTimeoutException' }, { status: 500 }))
    await abrirListadoConFilas()
    await user.click(screen.getByRole('button', { name: 'Eliminar Auriculares inalámbricos' }))
    const alerta = await screen.findByRole('alertdialog', { name: /eliminar producto/i })

    await user.click(within(alerta).getByRole('button', { name: /^eliminar$/i }))

    expect(await within(alerta).findByText(/no se ha podido eliminar el producto/i)).toBeInTheDocument()
    expect(screen.queryByText(/mongotimeout/i)).not.toBeInTheDocument()
    expect(screen.queryByText(/producto eliminado/i)).not.toBeInTheDocument()
  })

  it('HU35-CP10: al pulsar Escape en la alerta, la cierra sin borrar', async () => {
    const user = userEvent.setup()
    const borrado = capturarBorrado()
    await abrirListadoConFilas()
    await user.click(screen.getByRole('button', { name: 'Eliminar Auriculares inalámbricos' }))
    await screen.findByRole('alertdialog', { name: /eliminar producto/i })

    await user.keyboard('{Escape}')

    await waitFor(() => expect(screen.queryByRole('alertdialog')).not.toBeInTheDocument())
    expect(borrado.llamadas).toBe(0)
  })

  it('sellerProductsPage_laTablaDeProductosDelAdministrador_noTieneEsosBotones', async () => {
    await abrirSesionPorApi('admin@esibuy.test')
    renderApp('/admin/sellers')
    const user = userEvent.setup()
    await user.click(await screen.findByRole('button', { name: /ver información de casa vicente/i }))
    const dialogo = await screen.findByRole('dialog', { name: /información del vendedor/i })
    await user.click(within(dialogo).getByRole('tab', { name: 'Productos' }))

    expect(await within(dialogo).findByText(/todavía no tiene productos/i)).toBeInTheDocument()
    expect(within(dialogo).queryByRole('link', { name: /^editar /i })).not.toBeInTheDocument()
    expect(within(dialogo).queryByRole('button', { name: /^eliminar /i })).not.toBeInTheDocument()
  })
})

describe('SellerProductsPage (HU33): ver la ficha desde el listado', () => {
  it('HU33-CP07: cada fila tiene un botón para ver la ficha del producto', async () => {
    await abrirListado()
    await esperarFilas(10)

    expect(screen.getAllByRole('button', { name: /^ver /i })).toHaveLength(10)
  })

  it('HU33-CP07: al pulsar Ver se abre la ficha de ese producto', async () => {
    const user = userEvent.setup()
    await abrirListado()
    await esperarFilas(10)

    await user.click(screen.getByRole('button', { name: 'Ver Teclado mecánico' }))

    const dialogo = await screen.findByRole('dialog', { name: /información del producto/i })
    expect(await within(dialogo).findByLabelText('Nombre')).toHaveValue('Teclado mecánico')
  })
})

describe('SellerProductsPage (HU34): editar desde la ficha', () => {
  it('HU34-CP17: desde la ficha, Editar abre el formulario de edición con los datos del producto', async () => {
    const user = userEvent.setup()
    await abrirListado()
    await esperarFilas(10)

    await user.click(screen.getByRole('button', { name: 'Ver Teclado mecánico' }))
    const dialogo = await screen.findByRole('dialog', { name: /información del producto/i })
    await within(dialogo).findByLabelText('Nombre')
    await user.click(within(dialogo).getByRole('link', { name: 'Editar' }))

    expect(await screen.findByRole('heading', { level: 1, name: /editar producto/i })).toBeInTheDocument()
    expect(screen.getByTestId('ruta-actual')).toHaveTextContent(/^\/seller\/products\/p-2\/edit$/)
    await waitFor(() => expect(screen.getByLabelText(/^nombre/i)).toHaveValue('Teclado mecánico'))
    expect(screen.queryByRole('dialog', { name: /información del producto/i })).not.toBeInTheDocument()
  })
})

describe('SellerProductsPage (HU35): eliminar desde la ficha', () => {
  it('HU35-CP14: desde la ficha, Eliminar pide confirmación y al confirmar borra el producto y avisa', async () => {
    const user = userEvent.setup()
    const borrado = { id: null, llamadas: 0 }
    server.use(
      http.delete('/api/seller/products/:id', ({ params }) => {
        borrado.llamadas += 1
        borrado.id = params.id
        return new HttpResponse(null, { status: 204 })
      }),
    )
    await abrirListado()
    await esperarFilas(10)

    await user.click(screen.getByRole('button', { name: 'Ver Teclado mecánico' }))
    const ficha = await screen.findByRole('dialog', { name: /información del producto/i })
    await within(ficha).findByLabelText('Nombre')
    await user.click(within(ficha).getByRole('button', { name: 'Eliminar' }))

    const alerta = await screen.findByRole('alertdialog', { name: /eliminar producto/i })
    expect(within(alerta).getByText(/teclado mecánico/i)).toBeInTheDocument()
    expect(screen.queryByRole('dialog', { name: /información del producto/i })).not.toBeInTheDocument()
    expect(borrado.llamadas).toBe(0)

    await user.click(within(alerta).getByRole('button', { name: /^eliminar$/i }))

    await waitFor(() => expect(screen.queryByRole('alertdialog')).not.toBeInTheDocument())
    expect(borrado).toMatchObject({ llamadas: 1, id: 'p-2' })
    expect(await screen.findByText(/producto eliminado/i)).toBeInTheDocument()
  })
})
