import { describe, it, expect } from 'vitest'
import { screen, waitFor, within } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { http, HttpResponse } from 'msw'
import { server } from '../mocks/server'
import { abrirSesionPorApi, renderApp } from '../test/renderApp'

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

    await user.selectOptions(await screen.findByLabelText(/categoría/i), 'Hogar')

    await waitFor(() => expect(screen.getByText(/mostrando 1–10 de 12/i)).toBeInTheDocument())
  })

  it('sellerProductsPage_combinaBusquedaYCategoria', async () => {
    const user = userEvent.setup()
    await abrirListado()

    await user.selectOptions(await screen.findByLabelText(/categoría/i), 'Hogar')
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
    await user.selectOptions(screen.getByLabelText(/categoría/i), 'Hogar')

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
