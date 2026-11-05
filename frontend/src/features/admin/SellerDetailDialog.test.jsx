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
  render(
    <FluentProvider theme={webLightTheme}>
      <SellerDetailDialog sellerId={sellerId} onClose={onClose} />
    </FluentProvider>,
  )
  const dialogo = await screen.findByRole('dialog', { name: /información del vendedor/i })
  return { dialogo, onClose }
}

// Cambiar el estado encadena dos peticiones (el cambio y la recarga del vendedor): con toda la
// suite en marcha pueden tardar más del segundo que espera Testing Library por defecto.
const ESPERA = { timeout: 3000 }

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

describe('SellerDetailDialog (HU19): solo lectura', () => {
  it('sellerDetailDialog_enLaPestanaDatos_noOfreceEditarNiCambiarElEstado', async () => {
    const { dialogo } = await abrirDialogo()
    await within(dialogo).findByLabelText('Nombre')

    expect(within(dialogo).queryByRole('button', { name: /editar|bloquear|desbloquear|activar|desactivar/i })).not.toBeInTheDocument()
  })

  it('sellerDetailDialog_conVendedorBloqueado_tampocoOfreceDesbloquear', async () => {
    const { dialogo } = await abrirDialogo(VENDEDOR_BLOQUEADO)
    await within(dialogo).findByLabelText('Nombre')

    expect(within(dialogo).queryByRole('button', { name: /bloquear|desbloquear|activar|desactivar/i })).not.toBeInTheDocument()
  })

  it('sellerDetailDialog_conVendedorPendiente_tampocoOfreceActivar', async () => {
    const { dialogo } = await abrirDialogo(VENDEDOR_PENDIENTE)
    await within(dialogo).findByLabelText('Nombre')

    expect(within(dialogo).queryByRole('button', { name: /bloquear|desbloquear|activar|desactivar/i })).not.toBeInTheDocument()
  })

  it('sellerDetailDialog_elAvisoRemiteALosBotonesDeLaTabla', async () => {
    const { dialogo } = await abrirDialogo()

    expect(within(dialogo).getByText(/usa el botón editar de la tabla/i)).toBeInTheDocument()
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

  it('sellerDetailDialog_muestraElRolVendedor', async () => {
    const { dialogo } = await abrirDialogo()

    expect(await within(dialogo).findByLabelText('Rol')).toHaveValue('Vendedor')
  })
})
