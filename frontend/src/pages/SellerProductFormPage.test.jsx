import { describe, it, expect } from 'vitest'
import { screen, waitFor } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { http, HttpResponse } from 'msw'
import { server } from '../mocks/server'
import { abrirSesionPorApi, renderApp } from '../test/renderApp'

const VENDEDOR = 'vendedor@esibuy.test'
const RUTA_NUEVO = '/seller/products/new'

async function abrirFormulario(ruta = RUTA_NUEVO, titulo = /añadir producto/i) {
  await abrirSesionPorApi(VENDEDOR)
  renderApp(ruta)
  await screen.findByRole('heading', { level: 1, name: titulo })
  await screen.findByRole('checkbox', { name: 'Tecnología' })
}

function capturarEnvio(metodo, ruta, respuesta) {
  const envio = { cuerpo: null, llamadas: 0 }
  server.use(
    http[metodo](ruta, async ({ request }) => {
      envio.llamadas += 1
      envio.cuerpo = await request.json()
      return respuesta()
    }),
  )
  return envio
}

async function rellenarFormularioValido(user) {
  await user.type(screen.getByLabelText(/^nombre/i), 'Lámpara de pie')
  await user.click(screen.getByRole('checkbox', { name: 'Hogar' }))
  await user.type(screen.getByLabelText(/^precio/i), '45.5')
}

async function esperarRuta(ruta) {
  await waitFor(() => expect(screen.getByTestId('ruta-actual')).toHaveTextContent(new RegExp(`^${ruta}$`)))
}

describe('SellerProductFormPage (HU31): alta, campos', () => {
  it('sellerProductFormPage_muestraTodosLosCamposDelProducto', async () => {
    await abrirFormulario()

    expect(screen.getByLabelText(/^nombre/i)).toBeInTheDocument()
    expect(screen.getByLabelText(/^descripción/i)).toBeInTheDocument()
    expect(screen.getByLabelText(/^precio/i)).toBeInTheDocument()
    expect(screen.getByLabelText(/^stock/i)).toBeInTheDocument()
    expect(screen.getByLabelText(/^url de la imagen/i)).toBeInTheDocument()
    expect(screen.getByRole('group', { name: /categorías/i })).toBeInTheDocument()
    expect(screen.getByRole('checkbox', { name: 'Tecnología' })).toBeInTheDocument()
    expect(screen.getByRole('checkbox', { name: 'Hogar' })).toBeInTheDocument()
    expect(screen.getByRole('switch', { name: 'Disponible para la venta' })).toBeChecked()
    expect(screen.getByRole('switch', { name: 'Activar descuento' })).not.toBeChecked()
    expect(screen.getByRole('switch', { name: 'Activar descuento premium' })).not.toBeChecked()
    expect(screen.getByRole('button', { name: /^guardar$/i })).toBeInTheDocument()
  })

  it('sellerProductFormPage_enAlta_empiezaConElFormularioVacio', async () => {
    await abrirFormulario()

    expect(screen.getByLabelText(/^nombre/i)).toHaveValue('')
    expect(screen.getByLabelText(/^precio/i)).toHaveValue(null)
    expect(screen.getByRole('checkbox', { name: 'Tecnología' })).not.toBeChecked()
  })

  it('sellerProductFormPage_explicaQueLosDescuentosSeAcumulan', async () => {
    await abrirFormulario()

    expect(screen.getByText(/los descuentos se acumulan/i)).toBeInTheDocument()
    expect(screen.queryByText(/no se acumulan/i)).not.toBeInTheDocument()
  })

  it('sellerProductFormPage_losCamposDeDescuento_aparecenAlActivarElDescuento', async () => {
    const user = userEvent.setup()
    await abrirFormulario()
    expect(screen.queryByLabelText(/^descuento \(%\)/i)).not.toBeInTheDocument()

    await user.click(screen.getByRole('switch', { name: 'Activar descuento' }))
    expect(screen.getByLabelText(/^descuento \(%\)/i)).toBeInTheDocument()

    await user.click(screen.getByRole('switch', { name: 'Activar descuento premium' }))
    expect(screen.getByLabelText(/^descuento premium \(%\)/i)).toBeInTheDocument()
  })

  it('sellerProductFormPage_alPulsarCancelar_vuelveAlListadoSinGuardar', async () => {
    const user = userEvent.setup()
    const envio = capturarEnvio('post', '/api/seller/products', () => HttpResponse.json({}, { status: 201 }))
    await abrirFormulario()

    await user.click(screen.getByRole('link', { name: /cancelar/i }))

    await esperarRuta('/seller/products')
    expect(envio.llamadas).toBe(0)
  })

  it('sellerProductFormPage_sinSesion_redirigeAlLogin', async () => {
    renderApp(RUTA_NUEVO)

    await waitFor(() => expect(screen.getByTestId('ruta-actual')).toHaveTextContent(new RegExp('^/login$')))
  })

  it('sellerProductFormPage_conSesionDeCliente_noMuestraElFormulario', async () => {
    await abrirSesionPorApi('cliente@esibuy.test')
    renderApp(RUTA_NUEVO)

    await esperarRuta('/products')
    expect(screen.queryByRole('button', { name: /^guardar$/i })).not.toBeInTheDocument()
  })

  it('sellerProductFormPage_conErrorAlCargarLasCategorias_avisaYPermiteReintentar', async () => {
    server.use(http.get('/api/categorias', () => new HttpResponse(null, { status: 500 }), { once: true }))
    const user = userEvent.setup()
    await abrirSesionPorApi(VENDEDOR)
    renderApp(RUTA_NUEVO)

    expect(await screen.findByText(/no se han podido cargar las categorías/i)).toBeInTheDocument()
    await user.click(screen.getByRole('button', { name: /reintentar/i }))

    expect(await screen.findByRole('checkbox', { name: 'Tecnología' })).toBeInTheDocument()
  })
})

describe('SellerProductFormPage (HU31): validación', () => {
  it('sellerProductFormPage_alGuardarVacio_muestraTodosLosErroresALaVezYNoLlamaAlServidor', async () => {
    const user = userEvent.setup()
    const envio = capturarEnvio('post', '/api/seller/products', () => HttpResponse.json({}, { status: 201 }))
    await abrirFormulario()

    await user.click(screen.getByRole('button', { name: /^guardar$/i }))

    expect(screen.getByText(/introduce el nombre del producto/i)).toBeInTheDocument()
    expect(screen.getByText(/elige al menos una categoría/i)).toBeInTheDocument()
    expect(screen.getByText(/introduce el precio/i)).toBeInTheDocument()
    expect(envio.llamadas).toBe(0)
    expect(screen.getByTestId('ruta-actual')).toHaveTextContent(new RegExp(`^${RUTA_NUEVO}$`))
  })

  it('sellerProductFormPage_conPrecioCero_muestraQueDebeSerMayorQueCero', async () => {
    const user = userEvent.setup()
    await abrirFormulario()

    await rellenarFormularioValido(user)
    await user.clear(screen.getByLabelText(/^precio/i))
    await user.type(screen.getByLabelText(/^precio/i), '0')
    await user.click(screen.getByRole('button', { name: /^guardar$/i }))

    expect(screen.getByText(/el precio debe ser mayor que 0/i)).toBeInTheDocument()
  })

  it('sellerProductFormPage_conStockNegativo_muestraQueDebeSerMayorOIgualQueCero', async () => {
    const user = userEvent.setup()
    await abrirFormulario()

    await rellenarFormularioValido(user)
    await user.type(screen.getByLabelText(/^stock/i), '-1')
    await user.click(screen.getByRole('button', { name: /^guardar$/i }))

    expect(screen.getByText(/stock debe ser un número entero mayor o igual que 0/i)).toBeInTheDocument()
  })

  it('sellerProductFormPage_conDescuentoActivoSinPorcentaje_muestraError', async () => {
    const user = userEvent.setup()
    await abrirFormulario()

    await rellenarFormularioValido(user)
    await user.click(screen.getByRole('switch', { name: 'Activar descuento' }))
    await user.click(screen.getByRole('button', { name: /^guardar$/i }))

    expect(screen.getByText(/el descuento debe ser mayor que 0 y no superar 100/i)).toBeInTheDocument()
  })

  it('sellerProductFormPage_alCorregirUnCampoTrasUnIntentoFallido_loValidaEnVivo', async () => {
    const user = userEvent.setup()
    await abrirFormulario()
    await user.click(screen.getByRole('button', { name: /^guardar$/i }))
    expect(screen.getByText(/introduce el nombre del producto/i)).toBeInTheDocument()

    await user.type(screen.getByLabelText(/^nombre/i), 'Lámpara')

    expect(screen.queryByText(/introduce el nombre del producto/i)).not.toBeInTheDocument()
    expect(screen.getByText(/introduce el precio/i)).toBeInTheDocument()
  })

  it('sellerProductFormPage_antesDelPrimerIntento_noMuestraErrores', async () => {
    const user = userEvent.setup()
    await abrirFormulario()

    await user.type(screen.getByLabelText(/^nombre/i), 'a')
    await user.clear(screen.getByLabelText(/^nombre/i))

    expect(screen.queryByText(/introduce el nombre del producto/i)).not.toBeInTheDocument()
  })

  it('sellerProductFormPage_conUrlDeImagenNoValida_muestraError', async () => {
    const user = userEvent.setup()
    await abrirFormulario()

    await rellenarFormularioValido(user)
    await user.type(screen.getByLabelText(/^url de la imagen/i), 'esto no es una url')
    await user.click(screen.getByRole('button', { name: /^guardar$/i }))

    expect(screen.getByText(/dirección web válida/i)).toBeInTheDocument()
  })
})

describe('SellerProductFormPage (HU31): alta, guardado', () => {
  it('sellerProductFormPage_alGuardarUnProductoValido_enviaSusDatosSinIdentificarAlVendedor', async () => {
    const user = userEvent.setup()
    const envio = capturarEnvio('post', '/api/seller/products', () => HttpResponse.json({ id: 'p-nuevo' }, { status: 201 }))
    await abrirFormulario()

    await rellenarFormularioValido(user)
    await user.type(screen.getByLabelText(/^stock/i), '7')
    await user.click(screen.getByRole('switch', { name: 'Activar descuento' }))
    await user.type(screen.getByLabelText(/^descuento \(%\)/i), '10')
    await user.click(screen.getByRole('button', { name: /^guardar$/i }))

    await esperarRuta('/seller/products')
    expect(envio.cuerpo).toEqual({
      nombre: 'Lámpara de pie',
      descripcion: '',
      precio: 45.5,
      stock: 7,
      categoriaIds: ['cat-2'],
      imagen: '',
      disponible: true,
      descuento: { activo: true, porcentaje: 10 },
      descuentoPremium: { activo: false, porcentaje: null },
    })
  })

  it('sellerProductFormPage_conStockVacio_loEnviaComoCero', async () => {
    const user = userEvent.setup()
    const envio = capturarEnvio('post', '/api/seller/products', () => HttpResponse.json({ id: 'p-nuevo' }, { status: 201 }))
    await abrirFormulario()

    await rellenarFormularioValido(user)
    await user.click(screen.getByRole('button', { name: /^guardar$/i }))

    await esperarRuta('/seller/products')
    expect(envio.cuerpo.stock).toBe(0)
  })

  it('sellerProductFormPage_alDesmarcarDisponible_loEnviaComoNoDisponible', async () => {
    const user = userEvent.setup()
    const envio = capturarEnvio('post', '/api/seller/products', () => HttpResponse.json({ id: 'p-nuevo' }, { status: 201 }))
    await abrirFormulario()

    await rellenarFormularioValido(user)
    await user.click(screen.getByRole('switch', { name: 'Disponible para la venta' }))
    await user.click(screen.getByRole('button', { name: /^guardar$/i }))

    await esperarRuta('/seller/products')
    expect(envio.cuerpo.disponible).toBe(false)
  })

  it('sellerProductFormPage_tras_guardar_elListadoMuestraElAvisoDeProductoGuardado', async () => {
    const user = userEvent.setup()
    await abrirFormulario()

    await rellenarFormularioValido(user)
    await user.click(screen.getByRole('button', { name: /^guardar$/i }))

    expect(await screen.findByText(/producto guardado correctamente/i)).toBeInTheDocument()
    expect(await screen.findByRole('heading', { level: 1, name: /mis productos/i })).toBeInTheDocument()
  })

  it('sellerProductFormPage_conErrorDeServidor_muestraMensajeSinDetallesInternosYNoSale', async () => {
    server.use(
      http.post('/api/seller/products', () => HttpResponse.json({ message: 'MongoTimeoutException' }, { status: 500 })),
    )
    const user = userEvent.setup()
    await abrirFormulario()

    await rellenarFormularioValido(user)
    await user.click(screen.getByRole('button', { name: /^guardar$/i }))

    expect(await screen.findByText(/no se ha podido guardar el producto/i)).toBeInTheDocument()
    expect(screen.queryByText(/mongotimeout/i)).not.toBeInTheDocument()
    expect(screen.getByTestId('ruta-actual')).toHaveTextContent(new RegExp(`^${RUTA_NUEVO}$`))
    expect(screen.getByLabelText(/^nombre/i)).toHaveValue('Lámpara de pie')
  })

  it('sellerProductFormPage_cuandoElServidorRechazaLosDatos_muestraElMismoMensajeGenerico', async () => {
    server.use(http.post('/api/seller/products', () => HttpResponse.json({ code: 'VALIDATION_ERROR' }, { status: 400 })))
    const user = userEvent.setup()
    await abrirFormulario()

    await rellenarFormularioValido(user)
    await user.click(screen.getByRole('button', { name: /^guardar$/i }))

    expect(await screen.findByText(/no se ha podido guardar el producto/i)).toBeInTheDocument()
  })

  it('sellerProductFormPage_mientrasGuarda_deshabilitaElBotonParaNoDuplicar', async () => {
    const user = userEvent.setup()
    const envio = capturarEnvio('post', '/api/seller/products', () => HttpResponse.json({ id: 'p-nuevo' }, { status: 201 }))
    await abrirFormulario()

    await rellenarFormularioValido(user)
    const guardar = screen.getByRole('button', { name: /^guardar$/i })
    await user.dblClick(guardar)

    await esperarRuta('/seller/products')
    expect(envio.llamadas).toBe(1)
  })
})

describe('SellerProductFormPage (HU31): edición', () => {
  const RUTA_EDITAR = '/seller/products/p-1/edit'

  it('sellerProductFormPage_enEdicion_cargaLosDatosDelProducto', async () => {
    await abrirFormulario(RUTA_EDITAR, /editar producto/i)

    await waitFor(() => expect(screen.getByLabelText(/^nombre/i)).toHaveValue('Auriculares inalámbricos'))
    expect(screen.getByLabelText(/^precio/i)).toHaveValue(59.9)
    expect(screen.getByLabelText(/^stock/i)).toHaveValue(34)
    expect(screen.getByRole('checkbox', { name: 'Tecnología' })).toBeChecked()
    expect(screen.getByRole('checkbox', { name: 'Hogar' })).not.toBeChecked()
    expect(screen.getByLabelText(/^descripción/i)).toHaveValue('Descripción de Auriculares inalámbricos.')
  })

  it('sellerProductFormPage_enEdicion_muestraLosDescuentosActivosDelProducto', async () => {
    await abrirFormulario(RUTA_EDITAR, /editar producto/i)

    await waitFor(() => expect(screen.getByRole('switch', { name: 'Activar descuento' })).toBeChecked())
    expect(screen.getByLabelText(/^descuento \(%\)/i)).toHaveValue(10)
    expect(screen.getByLabelText(/^descuento premium \(%\)/i)).toHaveValue(15)
  })

  it('sellerProductFormPage_enEdicion_alGuardar_enviaLosCambiosAlProductoIndicado', async () => {
    const user = userEvent.setup()
    const envio = capturarEnvio('put', '/api/seller/products/:id', () => HttpResponse.json({ id: 'p-1' }))
    await abrirFormulario(RUTA_EDITAR, /editar producto/i)
    await waitFor(() => expect(screen.getByLabelText(/^nombre/i)).toHaveValue('Auriculares inalámbricos'))

    await user.clear(screen.getByLabelText(/^precio/i))
    await user.type(screen.getByLabelText(/^precio/i), '49.99')
    await user.click(screen.getByRole('button', { name: /^guardar$/i }))

    await esperarRuta('/seller/products')
    expect(envio.cuerpo).toMatchObject({ nombre: 'Auriculares inalámbricos', precio: 49.99, stock: 34 })
    expect(await screen.findByText(/cambios guardados/i)).toBeInTheDocument()
  })

  it('sellerProductFormPage_enEdicion_tambienValidaLosCampos', async () => {
    const user = userEvent.setup()
    await abrirFormulario(RUTA_EDITAR, /editar producto/i)
    await waitFor(() => expect(screen.getByLabelText(/^nombre/i)).toHaveValue('Auriculares inalámbricos'))

    await user.clear(screen.getByLabelText(/^nombre/i))
    await user.click(screen.getByRole('button', { name: /^guardar$/i }))

    expect(screen.getByText(/introduce el nombre del producto/i)).toBeInTheDocument()
    expect(screen.getByTestId('ruta-actual')).toHaveTextContent(new RegExp(`^${RUTA_EDITAR}$`))
  })

  it('sellerProductFormPage_enEdicion_conProductoDeOtroVendedor_noMuestraElFormulario', async () => {
    await abrirSesionPorApi(VENDEDOR)
    renderApp('/seller/products/p-24/edit')

    expect(await screen.findByText(/no se ha encontrado el producto/i)).toBeInTheDocument()
    expect(screen.queryByRole('button', { name: /^guardar$/i })).not.toBeInTheDocument()
  })

  it('sellerProductFormPage_enEdicion_conProductoInexistente_noMuestraElFormulario', async () => {
    await abrirSesionPorApi(VENDEDOR)
    renderApp('/seller/products/no-existe/edit')

    expect(await screen.findByText(/no se ha encontrado el producto/i)).toBeInTheDocument()
  })

  it('sellerProductFormPage_enEdicion_conErrorDeServidor_permiteReintentar', async () => {
    server.use(http.get('/api/seller/products/:id', () => new HttpResponse(null, { status: 500 }), { once: true }))
    const user = userEvent.setup()
    await abrirSesionPorApi(VENDEDOR)
    renderApp(RUTA_EDITAR)

    expect(await screen.findByText(/no se ha podido cargar el producto/i)).toBeInTheDocument()
    await user.click(screen.getByRole('button', { name: /reintentar/i }))

    expect(await screen.findByRole('heading', { level: 1, name: /editar producto/i })).toBeInTheDocument()
    await waitFor(() => expect(screen.getByLabelText(/^nombre/i)).toHaveValue('Auriculares inalámbricos'))
  })
})
