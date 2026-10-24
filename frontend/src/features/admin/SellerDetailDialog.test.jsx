import { describe, it, expect, vi } from 'vitest'
import { render, screen, waitFor, within } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { FluentProvider, webLightTheme } from '@fluentui/react-components'
import { http, HttpResponse } from 'msw'
import { server } from '../../mocks/server'
import { abrirSesionPorApi } from '../../test/renderApp'
import SellerDetailDialog from './SellerDetailDialog'

const VENDEDOR_CON_DATOS = 'u-2'
const VENDEDOR_SIN_NADA = 'u-5'
const VENDEDOR_BLOQUEADO = 's-07'
const VENDEDOR_PENDIENTE = 's-05'

async function abrirDialogo(sellerId = VENDEDOR_CON_DATOS) {
  await abrirSesionPorApi('admin@esibuy.test')
  const onClose = vi.fn()
  const onStatusChange = vi.fn()
  render(
    <FluentProvider theme={webLightTheme}>
      <SellerDetailDialog sellerId={sellerId} onClose={onClose} onStatusChange={onStatusChange} />
    </FluentProvider>,
  )
  const dialogo = await screen.findByRole('dialog', { name: /información del vendedor/i })
  return { dialogo, onClose, onStatusChange }
}

// Cambiar el estado encadena dos peticiones (el cambio y la recarga del vendedor): con toda la
// suite en marcha pueden tardar más del segundo que espera Testing Library por defecto.
const ESPERA = { timeout: 3000 }

async function pedirCambioDeEstado(user, dialogo, boton) {
  await user.click(await within(dialogo).findByRole('button', { name: boton }))
  return screen.findByRole('alertdialog')
}

function botonesDeEdicion(dialogo) {
  return within(dialogo).queryAllByRole('button', { name: /editar|eliminar|borrar|cancelar pedido/i })
}

async function abrirPestana(user, dialogo, nombre) {
  await user.click(within(dialogo).getByRole('tab', { name: nombre }))
}

describe('SellerDetailDialog (HU26): datos', () => {
  it('sellerDetailDialog_alAbrirse_muestraTodosLosDatosDelVendedor', async () => {
    const { dialogo } = await abrirDialogo()

    expect(await within(dialogo).findByLabelText('Nombre')).toHaveValue('Vera')
    expect(within(dialogo).getByLabelText('Apellidos')).toHaveValue('Vendedora')
    expect(within(dialogo).getByLabelText('Correo electrónico')).toHaveValue('vendedor@esibuy.test')
    expect(within(dialogo).getByLabelText('Nombre comercial')).toHaveValue('TecnoVera')
    expect(within(dialogo).getByLabelText('DNI')).toHaveValue('12345601V')
    expect(within(dialogo).getByLabelText('Categoría principal')).toHaveValue('Tecnología')
    expect(within(dialogo).getByLabelText('Estado')).toHaveValue('Activo')
  })

  it('sellerDetailDialog_noMuestraLaFechaDeAlta', async () => {
    const { dialogo } = await abrirDialogo()
    await within(dialogo).findByLabelText('Nombre')

    expect(within(dialogo).queryByLabelText('Fecha de alta')).not.toBeInTheDocument()
  })

  it('sellerDetailDialog_todosLosCamposSonDeSoloLectura', async () => {
    const { dialogo } = await abrirDialogo()
    await within(dialogo).findByLabelText('Nombre')

    const campos = within(dialogo).getAllByRole('textbox')
    expect(campos).toHaveLength(10)   // con el teléfono y la imagen del vendedor
    campos.forEach((campo) => expect(campo).toHaveAttribute('readonly'))
  })

  it('sellerDetailDialog_alEscribirEnUnCampo_noCambiaSuValor', async () => {
    const user = userEvent.setup()
    const { dialogo } = await abrirDialogo()

    const campo = await within(dialogo).findByLabelText('Nombre comercial')
    await user.type(campo, 'cambio')

    expect(campo).toHaveValue('TecnoVera')
  })

  it('sellerDetailDialog_muestraAvisoDeModoSoloLectura', async () => {
    const { dialogo } = await abrirDialogo()

    expect(within(dialogo).getByText(/modo solo lectura/i)).toBeInTheDocument()
  })

  it('sellerDetailDialog_noOfreceBotonesDeEliminarVendedor', async () => {
    const { dialogo } = await abrirDialogo()
    await within(dialogo).findByLabelText('Nombre')

    expect(within(dialogo).queryByRole('button', { name: /eliminar|borrar/i })).not.toBeInTheDocument()
  })

  it('sellerDetailDialog_enLaPestanaDatos_ofreceElBotonEditar', async () => {
    const { dialogo } = await abrirDialogo()

    expect(await within(dialogo).findByRole('button', { name: 'Editar' })).toBeEnabled()
  })

  it('sellerDetailDialog_conVendedorActivo_muestraElBotonDesactivarUsuarioHabilitado', async () => {
    const { dialogo } = await abrirDialogo()

    expect(await within(dialogo).findByRole('button', { name: /^desactivar usuario$/i })).toBeEnabled()
    expect(within(dialogo).queryByRole('button', { name: /^activar usuario$/i })).not.toBeInTheDocument()
  })

  it('sellerDetailDialog_conVendedorBloqueado_muestraElBotonActivarUsuarioHabilitado', async () => {
    const { dialogo } = await abrirDialogo(VENDEDOR_BLOQUEADO)

    expect(await within(dialogo).findByRole('button', { name: /^activar usuario$/i })).toBeEnabled()
    expect(within(dialogo).queryByRole('button', { name: /^desactivar usuario$/i })).not.toBeInTheDocument()
  })

  it('sellerDetailDialog_conVendedorPendiente_muestraElBotonActivarUsuario', async () => {
    const { dialogo } = await abrirDialogo(VENDEDOR_PENDIENTE)

    expect(await within(dialogo).findByRole('button', { name: /^activar usuario$/i })).toBeEnabled()
    expect(within(dialogo).queryByRole('button', { name: /^desactivar usuario$/i })).not.toBeInTheDocument()
  })

  it('sellerDetailDialog_alPulsarCerrar_avisaDelCierre', async () => {
    const user = userEvent.setup()
    const { dialogo, onClose } = await abrirDialogo()

    await user.click(within(dialogo).getByRole('button', { name: /cerrar/i }))

    expect(onClose).toHaveBeenCalled()
  })

  it('sellerDetailDialog_alPulsarEscape_avisaDelCierre', async () => {
    const user = userEvent.setup()
    const { onClose } = await abrirDialogo()

    await user.keyboard('{Escape}')

    expect(onClose).toHaveBeenCalled()
  })

  it('sellerDetailDialog_conErrorDeServidor_muestraMensajeSinDetallesInternosYPermiteReintentar', async () => {
    server.use(
      http.get(
        '/api/admin/sellers/:id',
        () => HttpResponse.json({ message: 'MongoTimeoutException' }, { status: 500 }),
        { once: true },
      ),
    )
    const user = userEvent.setup()
    const { dialogo } = await abrirDialogo()

    expect(await within(dialogo).findByText(/no se ha podido cargar la información del vendedor/i)).toBeInTheDocument()
    expect(within(dialogo).queryByText(/mongotimeout/i)).not.toBeInTheDocument()

    await user.click(within(dialogo).getByRole('button', { name: /reintentar/i }))

    expect(await within(dialogo).findByLabelText('Nombre comercial')).toHaveValue('TecnoVera')
  })
})

describe('SellerDetailDialog (HU21): activar y desactivar', () => {
  it('sellerDetailDialog_alPulsarDesactivarUsuario_pideConfirmacionSinCambiarNada', async () => {
    const user = userEvent.setup()
    const { dialogo, onStatusChange } = await abrirDialogo()

    const confirmacion = await pedirCambioDeEstado(user, dialogo, /^desactivar usuario$/i)

    expect(within(confirmacion).getByText(/seguro que quieres desactivar a tecnovera/i)).toBeInTheDocument()
    expect(within(dialogo).getByLabelText('Estado')).toHaveValue('Activo')
    expect(onStatusChange).not.toHaveBeenCalled()
  })

  it('sellerDetailDialog_alCancelarLaConfirmacion_noCambiaElEstado', async () => {
    const user = userEvent.setup()
    const { dialogo, onStatusChange } = await abrirDialogo()

    const confirmacion = await pedirCambioDeEstado(user, dialogo, /^desactivar usuario$/i)
    await user.click(within(confirmacion).getByRole('button', { name: /^cancelar$/i }))

    expect(screen.queryByRole('alertdialog')).not.toBeInTheDocument()
    expect(within(dialogo).getByLabelText('Estado')).toHaveValue('Activo')
    expect(within(dialogo).getByRole('button', { name: /^desactivar usuario$/i })).toBeInTheDocument()
    expect(onStatusChange).not.toHaveBeenCalled()
  })

  it('sellerDetailDialog_alConfirmarDesactivar_desactivaAlVendedorYAvisaAlListado', async () => {
    const user = userEvent.setup()
    const { dialogo, onStatusChange } = await abrirDialogo()

    const confirmacion = await pedirCambioDeEstado(user, dialogo, /^desactivar usuario$/i)
    await user.click(within(confirmacion).getByRole('button', { name: /^desactivar$/i }))

    expect(await within(dialogo).findByText(/el vendedor está desactivado/i, {}, ESPERA)).toBeInTheDocument()
    await waitFor(() => expect(within(dialogo).getByLabelText('Estado')).toHaveValue('Bloqueado'), ESPERA)
    expect(within(dialogo).getByRole('button', { name: /^activar usuario$/i })).toBeInTheDocument()
    expect(screen.queryByRole('alertdialog')).not.toBeInTheDocument()
    expect(onStatusChange).toHaveBeenCalledTimes(1)
  })

  it('sellerDetailDialog_alConfirmarActivarUnVendedorPendiente_loDaDeAlta', async () => {
    const user = userEvent.setup()
    const { dialogo, onStatusChange } = await abrirDialogo(VENDEDOR_PENDIENTE)

    const confirmacion = await pedirCambioDeEstado(user, dialogo, /^activar usuario$/i)
    expect(within(confirmacion).getByText(/seguro que quieres activar a hogar hugo/i)).toBeInTheDocument()
    await user.click(within(confirmacion).getByRole('button', { name: /^activar$/i }))

    expect(await within(dialogo).findByText(/ya puede usar la plataforma/i, {}, ESPERA)).toBeInTheDocument()
    await waitFor(() => expect(within(dialogo).getByLabelText('Estado')).toHaveValue('Activo'), ESPERA)
    expect(within(dialogo).getByRole('button', { name: /^desactivar usuario$/i })).toBeInTheDocument()
    expect(onStatusChange).toHaveBeenCalledTimes(1)
  })

  it('sellerDetailDialog_alConfirmarActivarUnVendedorBloqueado_loActiva', async () => {
    const user = userEvent.setup()
    const { dialogo } = await abrirDialogo(VENDEDOR_BLOQUEADO)

    const confirmacion = await pedirCambioDeEstado(user, dialogo, /^activar usuario$/i)
    await user.click(within(confirmacion).getByRole('button', { name: /^activar$/i }))

    await waitFor(() => expect(within(dialogo).getByLabelText('Estado')).toHaveValue('Activo'), ESPERA)
  })

  it('sellerDetailDialog_siFallaElCambioDeEstado_muestraErrorYNoCambiaNada', async () => {
    server.use(
      http.patch('/api/admin/sellers/:id/status', () => new HttpResponse(null, { status: 500 }), { once: true }),
    )
    const user = userEvent.setup()
    const { dialogo, onStatusChange } = await abrirDialogo()

    const confirmacion = await pedirCambioDeEstado(user, dialogo, /^desactivar usuario$/i)
    await user.click(within(confirmacion).getByRole('button', { name: /^desactivar$/i }))

    expect(await within(dialogo).findByText(/no se ha podido cambiar el estado del vendedor/i, {}, ESPERA)).toBeInTheDocument()
    expect(within(dialogo).getByLabelText('Estado')).toHaveValue('Activo')
    expect(onStatusChange).not.toHaveBeenCalled()
  })
})

describe('SellerDetailDialog (HU26): productos', () => {
  it('sellerDetailDialog_pestanaProductos_muestraProductosYPreciosDelVendedor', async () => {
    const user = userEvent.setup()
    const { dialogo } = await abrirDialogo()

    await abrirPestana(user, dialogo, 'Productos')

    expect(await within(dialogo).findByText('Auriculares inalámbricos')).toBeInTheDocument()
    expect(within(dialogo).getByText(/59,90/)).toBeInTheDocument()
    expect(within(dialogo).getByText(/mostrando 1–5 de 23 productos/i)).toBeInTheDocument()
  })

  it('sellerDetailDialog_pestanaProductos_muestraElNombreDeLaCategoriaDeCadaProducto', async () => {
    server.use(
      http.get('/api/admin/sellers/:id/products', () =>
        HttpResponse.json({
          items: [
            { id: 'p-1', nombre: 'Teclado mecánico', precio: 89.5, stock: 12, disponible: true, categoriaId: 'cat-1' },
          ],
          page: 1,
          size: 5,
          totalItems: 1,
          totalPages: 1,
        }),
      ),
    )
    const user = userEvent.setup()
    const { dialogo } = await abrirDialogo()

    await abrirPestana(user, dialogo, 'Productos')

    const fila = (await within(dialogo).findByText('Teclado mecánico')).closest('tr')
    await waitFor(() => expect(within(fila).getByText('Tecnología')).toBeInTheDocument(), ESPERA)
    expect(within(fila).queryByText('cat-1')).not.toBeInTheDocument()
  })

  it('sellerDetailDialog_pestanaProductos_sinAccionesDeEdicionNiBorrado', async () => {
    const user = userEvent.setup()
    const { dialogo } = await abrirDialogo()

    await abrirPestana(user, dialogo, 'Productos')
    await within(dialogo).findByText('Auriculares inalámbricos')

    expect(botonesDeEdicion(dialogo)).toHaveLength(0)
  })

  it('sellerDetailDialog_pestanaProductos_permitePasarDePagina', async () => {
    const user = userEvent.setup()
    const { dialogo } = await abrirDialogo()

    await abrirPestana(user, dialogo, 'Productos')
    await within(dialogo).findByText('Auriculares inalámbricos')
    await user.click(within(dialogo).getByRole('button', { name: /siguiente/i }))

    expect(await within(dialogo).findByText('Webcam Full HD')).toBeInTheDocument()
    expect(within(dialogo).getByText(/página 2 de 5/i)).toBeInTheDocument()
  })

  it('sellerDetailDialog_pestanaProductos_sinProductos_muestraMensajeVacio', async () => {
    const user = userEvent.setup()
    const { dialogo } = await abrirDialogo(VENDEDOR_SIN_NADA)

    await abrirPestana(user, dialogo, 'Productos')

    expect(await within(dialogo).findByText(/este vendedor todavía no tiene productos/i)).toBeInTheDocument()
  })

  it('sellerDetailDialog_pestanaProductos_conError_permiteReintentar', async () => {
    server.use(
      http.get('/api/admin/sellers/:id/products', () => new HttpResponse(null, { status: 500 }), { once: true }),
    )
    const user = userEvent.setup()
    const { dialogo } = await abrirDialogo()

    await abrirPestana(user, dialogo, 'Productos')
    expect(await within(dialogo).findByText(/no se han podido cargar los productos/i)).toBeInTheDocument()
    await user.click(within(dialogo).getByRole('button', { name: /reintentar/i }))

    expect(await within(dialogo).findByText('Auriculares inalámbricos')).toBeInTheDocument()
  })
})

describe('SellerDetailDialog (HU26): pedidos', () => {
  it('sellerDetailDialog_pestanaPedidos_muestraLosPedidosDelVendedor', async () => {
    const user = userEvent.setup()
    const { dialogo } = await abrirDialogo()

    await abrirPestana(user, dialogo, 'Pedidos')

    expect(await within(dialogo).findByText('PED-1012')).toBeInTheDocument()
    expect(within(dialogo).getByText(/94,40/)).toBeInTheDocument()
    expect(within(dialogo).getAllByText('Pendiente').length).toBeGreaterThan(0)
    expect(within(dialogo).getByText(/mostrando 1–5 de 12 pedidos/i)).toBeInTheDocument()
  })

  it('sellerDetailDialog_pestanaPedidos_permitePasarDePagina', async () => {
    const user = userEvent.setup()
    const { dialogo } = await abrirDialogo()

    await abrirPestana(user, dialogo, 'Pedidos')
    await within(dialogo).findByText('PED-1012')
    await user.click(within(dialogo).getByRole('button', { name: /siguiente/i }))

    expect(await within(dialogo).findByText('PED-1007')).toBeInTheDocument()
  })

  it('sellerDetailDialog_pestanaPedidos_sinPedidos_muestraMensajeVacio', async () => {
    const user = userEvent.setup()
    const { dialogo } = await abrirDialogo(VENDEDOR_SIN_NADA)

    await abrirPestana(user, dialogo, 'Pedidos')

    expect(await within(dialogo).findByText(/este vendedor todavía no tiene pedidos/i)).toBeInTheDocument()
  })

  it('sellerDetailDialog_pestanaPedidos_sinAccionesDeEdicionNiCancelacion', async () => {
    const user = userEvent.setup()
    const { dialogo } = await abrirDialogo()

    await abrirPestana(user, dialogo, 'Pedidos')
    await within(dialogo).findByText('PED-1012')

    expect(botonesDeEdicion(dialogo)).toHaveLength(0)
  })
})

describe('SellerDetailDialog (HU22): edición', () => {
  async function entrarEnEdicion(sellerId = VENDEDOR_CON_DATOS) {
    const ficha = await abrirDialogo(sellerId)
    const user = userEvent.setup()
    await user.click(await within(ficha.dialogo).findByRole('button', { name: 'Editar' }))
    await within(ficha.dialogo).findByText('Modo edición', undefined, ESPERA)
    await within(ficha.dialogo).findByLabelText(/^categoría principal/i, undefined, ESPERA)
    return { ...ficha, user }
  }

  function capturarGuardado() {
    const envio = { cuerpo: null, llamadas: 0 }
    server.use(
      http.put('/api/admin/usuarios/:id', async ({ params, request }) => {
        envio.llamadas += 1
        envio.id = params.id
        envio.cuerpo = await request.json()
        return HttpResponse.json({ id: params.id })
      }),
    )
    return envio
  }

  const campo = (dialogo, etiqueta) => within(dialogo).getByLabelText(etiqueta)
  const esDeSoloLectura = (dialogo, etiqueta) => campo(dialogo, etiqueta).hasAttribute('readonly')
  const guardar = (user, dialogo) => user.click(within(dialogo).getByRole('button', { name: /^guardar$/i }))

  async function escribir(user, dialogo, etiqueta, texto) {
    await user.clear(campo(dialogo, etiqueta))
    if (texto) await user.type(campo(dialogo, etiqueta), texto)
  }

  it('sellerDetailDialog_alPulsarEditar_pasaAlModoEdicionConLosDatosDelVendedor', async () => {
    const { dialogo } = await entrarEnEdicion()

    expect(campo(dialogo, /^nombre comercial/i)).toHaveValue('TecnoVera')
    expect(campo(dialogo, /^categoría principal/i)).toHaveValue('cat-1')
    expect(within(dialogo).getByRole('button', { name: /^guardar$/i })).toBeInTheDocument()
    expect(within(dialogo).queryByRole('button', { name: 'Editar' })).not.toBeInTheDocument()
    expect(within(dialogo).queryByRole('button', { name: /desactivar usuario/i })).not.toBeInTheDocument()
  })

  it('sellerDetailDialog_enEdicion_dejaModificarTodoMenosElEmailYElEstado', async () => {
    const { dialogo } = await entrarEnEdicion()

    for (const etiqueta of [/^nombre(?! comercial)/i, /^apellidos/i, /^nombre comercial/i, /^dni/i]) {
      expect(esDeSoloLectura(dialogo, etiqueta)).toBe(false)
    }
    expect(campo(dialogo, /^categoría principal/i)).not.toBeDisabled()
    expect(esDeSoloLectura(dialogo, /^correo electrónico/i)).toBe(true)
    expect(esDeSoloLectura(dialogo, 'Estado')).toBe(true)
    expect(within(dialogo).queryByLabelText('Fecha de alta')).not.toBeInTheDocument()
  })

  it('sellerDetailDialog_enEdicion_losProductosYLosPedidosNoSePuedenTocar', async () => {
    const { dialogo } = await entrarEnEdicion()

    expect(within(dialogo).getByRole('tab', { name: 'Productos' })).toBeDisabled()
    expect(within(dialogo).getByRole('tab', { name: 'Pedidos' })).toBeDisabled()
    expect(within(dialogo).getByText(/productos, precios y pedidos no se pueden cambiar/i)).toBeInTheDocument()
  })

  it('sellerDetailDialog_enLasPestanasDeProductosYPedidos_noOfreceEditar', async () => {
    const user = userEvent.setup()
    const { dialogo } = await abrirDialogo()
    await within(dialogo).findByLabelText('Nombre')

    await abrirPestana(user, dialogo, 'Productos')
    await within(dialogo).findByText('Auriculares inalámbricos')
    expect(within(dialogo).queryByRole('button', { name: 'Editar' })).not.toBeInTheDocument()

    await abrirPestana(user, dialogo, 'Pedidos')
    await within(dialogo).findByText('PED-1012')
    expect(within(dialogo).queryByRole('button', { name: 'Editar' })).not.toBeInTheDocument()
  })

  it('sellerDetailDialog_alGuardar_enviaSoloLosDatosDelVendedor', async () => {
    const envio = capturarGuardado()
    const { dialogo, user } = await entrarEnEdicion()

    await escribir(user, dialogo, /^nombre comercial/i, 'TecnoVera Plus')
    await guardar(user, dialogo)

    await waitFor(() => expect(envio.llamadas).toBe(1), ESPERA)
    expect(envio.id).toBe('u-2')
    expect(envio.cuerpo).toEqual({
      nombre: 'Vera',
      apellidos: 'Vendedora',
      nombreComercial: 'TecnoVera Plus',
      dni: '12345601V',
      telefono: '',
      categoriaPrincipalId: 'cat-1',
      avatar: '',
    })
  })

  it('sellerDetailDialog_tras_guardar_vuelveALaFichaConLosDatosNuevosYAvisaAlListado', async () => {
    const { dialogo, user, onStatusChange } = await entrarEnEdicion()

    await escribir(user, dialogo, /^nombre comercial/i, 'TecnoVera Plus')
    await guardar(user, dialogo)

    expect(await within(dialogo).findByText('Los cambios se han guardado.', undefined, ESPERA)).toBeInTheDocument()
    await waitFor(() => expect(campo(dialogo, 'Nombre comercial')).toHaveValue('TecnoVera Plus'), ESPERA)
    expect(within(dialogo).getByText('Modo solo lectura')).toBeInTheDocument()
    expect(onStatusChange).toHaveBeenCalled()
  })

  it('sellerDetailDialog_alCambiarLaCategoriaPrincipal_laFichaMuestraLaNueva', async () => {
    const { dialogo, user } = await entrarEnEdicion()

    await user.selectOptions(campo(dialogo, /^categoría principal/i), 'cat-2')
    await guardar(user, dialogo)

    await waitFor(() => expect(campo(dialogo, 'Categoría principal')).toHaveValue('Hogar'), ESPERA)
  })

  it('sellerDetailDialog_conElNombreComercialDeOtroVendedor_muestraElErrorYNoSale', async () => {
    const { dialogo, user } = await entrarEnEdicion()

    await escribir(user, dialogo, /^nombre comercial/i, 'Casa Vicente')
    await guardar(user, dialogo)

    expect(await within(dialogo).findByText('Este nombre comercial ya está registrado', undefined, ESPERA)).toBeInTheDocument()
    expect(within(dialogo).getByText('Modo edición')).toBeInTheDocument()
  })

  it('sellerDetailDialog_alGuardarSinCambiarElNombreComercial_noLoTomaPorRepetido', async () => {
    const { dialogo, user } = await entrarEnEdicion()

    await escribir(user, dialogo, /^apellidos/i, 'Vendedora Nueva')
    await guardar(user, dialogo)

    expect(await within(dialogo).findByText('Los cambios se han guardado.', undefined, ESPERA)).toBeInTheDocument()
  })

  it('sellerDetailDialog_conElNombreComercialDeOtroVendedor_muestraElErrorEnElCampo', async () => {
    const { dialogo, user } = await entrarEnEdicion()

    await escribir(user, dialogo, /^nombre comercial/i, 'Casa Vicente')
    await guardar(user, dialogo)

    expect(await within(dialogo).findByText('Este nombre comercial ya está registrado', undefined, ESPERA)).toBeInTheDocument()
  })

  it('sellerDetailDialog_conCamposVaciosOIncorrectos_losMuestraTodosALaVezYNoGuarda', async () => {
    const envio = capturarGuardado()
    const { dialogo, user } = await entrarEnEdicion()

    await escribir(user, dialogo, /^nombre comercial/i, '')
    await escribir(user, dialogo, /^dni/i, 'ABC')
    await guardar(user, dialogo)

    expect(within(dialogo).getByText(/este campo es obligatorio/i)).toBeInTheDocument()
    expect(within(dialogo).getByText(/introduce un dni válido/i)).toBeInTheDocument()
    expect(envio.llamadas).toBe(0)
  })

  it('sellerDetailDialog_alPulsarCancelar_vuelveALaFichaSinGuardarNada', async () => {
    const envio = capturarGuardado()
    const { dialogo, user } = await entrarEnEdicion()

    await escribir(user, dialogo, /^nombre comercial/i, 'Cambio descartado')
    await user.click(within(dialogo).getByRole('button', { name: /^cancelar$/i }))

    expect(await within(dialogo).findByText('Modo solo lectura')).toBeInTheDocument()
    expect(campo(dialogo, 'Nombre comercial')).toHaveValue('TecnoVera')
    expect(envio.llamadas).toBe(0)
    expect(within(dialogo).getByRole('tab', { name: 'Productos' })).toBeEnabled()
  })

  it('sellerDetailDialog_conErrorDelServidor_muestraElMensajeYSigueEnEdicion', async () => {
    server.use(http.put('/api/admin/usuarios/:id', () => HttpResponse.json({ message: 'Mongo' }, { status: 500 })))
    const { dialogo, user } = await entrarEnEdicion()

    await guardar(user, dialogo)

    expect(await within(dialogo).findByText(/no se han podido guardar los cambios/i, undefined, ESPERA)).toBeInTheDocument()
    expect(within(dialogo).queryByText(/mongo/i)).not.toBeInTheDocument()
    expect(within(dialogo).getByText('Modo edición')).toBeInTheDocument()
  })

  it('sellerDetailDialog_siNoCarganLasCategorias_avisaYPermiteReintentar', async () => {
    server.use(http.get('/api/categorias', () => new HttpResponse(null, { status: 500 }), { once: true }))
    const user = userEvent.setup()
    const { dialogo } = await abrirDialogo()
    await user.click(await within(dialogo).findByRole('button', { name: 'Editar' }))

    expect(await within(dialogo).findByText(/no se han podido cargar las categorías/i, undefined, ESPERA)).toBeInTheDocument()
    await user.click(within(dialogo).getByRole('button', { name: /reintentar/i }))

    expect(await within(dialogo).findByLabelText(/^categoría principal/i, undefined, ESPERA)).toBeInTheDocument()
  })

  it('sellerDetailDialog_conUnVendedorPendiente_tambienSePuedeEditar', async () => {
    const { dialogo } = await entrarEnEdicion(VENDEDOR_PENDIENTE)

    expect(campo(dialogo, /^nombre comercial/i)).toHaveValue('Hogar Hugo')
  })
})

describe('SellerDetailDialog (HU22): el rol no se puede cambiar', () => {
  async function entrarEnEdicion(sellerId = VENDEDOR_CON_DATOS) {
    const ficha = await abrirDialogo(sellerId)
    const user = userEvent.setup()
    await user.click(await within(ficha.dialogo).findByRole('button', { name: 'Editar' }))
    await within(ficha.dialogo).findByLabelText(/^rol/i, undefined, ESPERA)
    return { ...ficha, user }
  }

  const guardar = (user, dialogo) => user.click(within(dialogo).getByRole('button', { name: /^guardar$/i }))

  function capturarGuardado() {
    const envio = { cuerpo: null, llamadas: 0, id: null }
    server.use(
      http.put('/api/admin/usuarios/:id', async ({ params, request }) => {
        envio.llamadas += 1
        envio.id = params.id
        envio.cuerpo = await request.json()
        return HttpResponse.json({ id: params.id })
      }),
    )
    return envio
  }

  it('sellerDetailDialog_enModoLectura_muestraElRolVendedor', async () => {
    const { dialogo } = await abrirDialogo()

    expect(await within(dialogo).findByLabelText('Rol')).toHaveValue('Vendedor')
  })

  it('sellerDetailDialog_enEdicion_elRolEsDeSoloLectura', async () => {
    const { dialogo } = await entrarEnEdicion()

    expect(within(dialogo).getByLabelText('Rol')).toHaveValue('Vendedor')
    expect(within(dialogo).getByLabelText('Rol')).toHaveAttribute('readonly')
  })

  it('sellerDetailDialog_alEditarSeUsaElMismoEndpointQueConElRestoDeUsuariosSinEnviarElRol', async () => {
    const envio = capturarGuardado()
    const { dialogo, user } = await entrarEnEdicion()

    await guardar(user, dialogo)

    await waitFor(() => expect(envio.llamadas).toBe(1), ESPERA)
    expect(envio.id).toBe('u-2')
    expect(envio.cuerpo).not.toHaveProperty('rol')
  })

  it('sellerDetailDialog_alGuardar_noSeCierraYAvisaDeQueSeHaGuardado', async () => {
    const { dialogo, user, onClose } = await entrarEnEdicion()

    await guardar(user, dialogo)

    expect(await within(dialogo).findByText('Los cambios se han guardado.', undefined, ESPERA)).toBeInTheDocument()
    expect(onClose).not.toHaveBeenCalled()
  })
})
