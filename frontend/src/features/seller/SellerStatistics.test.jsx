import { describe, it, expect } from 'vitest'
import { screen, waitFor, within } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { http, HttpResponse } from 'msw'
import { server } from '../../mocks/server'
import { abrirSesionPorApi, renderApp } from '../../test/renderApp'

// Del mock de estadísticas: Vera (vendedor@esibuy.test) tiene 6 pedidos con 16 unidades, 8 valoraciones y 23
// productos; Vicente (vendedor-vacio@esibuy.test) no tiene ventas, valoraciones ni productos.
async function abrirEstadisticas(email) {
  await abrirSesionPorApi(email)
  renderApp('/profile')
  return screen.findByRole('region', { name: 'Mis estadísticas' })
}

const cifra = (bloque, nombre) => within(bloque).getByRole('group', { name: nombre })

const filasDe = (tabla) =>
  within(tabla)
    .getAllByRole('row')
    .slice(1)
    .map((fila) => within(fila).getAllByRole('cell').map((celda) => celda.textContent))

describe('SellerStatistics (HU17): ventas', () => {
  it('HU17-CP14: el vendedor ve sus pedidos, unidades e ingresos y los más vendidos en orden', async () => {
    await abrirEstadisticas('vendedor@esibuy.test')

    const ventas = await screen.findByRole('region', { name: 'Ventas' })
    expect(cifra(ventas, 'Pedidos')).toHaveTextContent('6')
    expect(cifra(ventas, 'Unidades vendidas')).toHaveTextContent('16')
    expect(cifra(ventas, 'Ingresos')).toHaveTextContent('1095,83')

    const masVendidos = within(ventas).getByRole('table', { name: 'Los más vendidos' })
    const filas = filasDe(masVendidos)
    expect(filas.map(([producto, unidades]) => [producto, unidades])).toEqual([
      ['Teclado mecánico', '5'],
      ['Auriculares inalámbricos', '4'],
      ['Cargador USB-C 65 W', '3'],
      ['Memoria USB 128 GB', '2'],
      ['Monitor 27 pulgadas', '1'],
    ])
    expect(filas[0][2]).toMatch(/^447,50\s€$/)
  })
})

describe('SellerStatistics (HU17): valoraciones', () => {
  it('HU17-CP15: el vendedor ve la media global, el total y la valoración de cada producto, del mejor al peor', async () => {
    await abrirEstadisticas('vendedor@esibuy.test')

    const valoraciones = await screen.findByRole('region', { name: 'Valoraciones' })
    expect(cifra(valoraciones, 'Valoración media')).toHaveTextContent('4,4 de 5')
    expect(cifra(valoraciones, 'Valoraciones recibidas')).toHaveTextContent('8')

    const porProducto = within(valoraciones).getByRole('table', { name: 'Valoración por producto' })
    expect(filasDe(porProducto)).toEqual([
      ['Teclado mecánico', '4,5', '6'],
      ['Auriculares inalámbricos', '4,0', '2'],
    ])
  })
})

describe('SellerStatistics (HU17): stock', () => {
  it('HU17-CP16: el vendedor ve cuántos productos tiene, sin stock, con stock bajo y no disponibles', async () => {
    await abrirEstadisticas('vendedor@esibuy.test')

    const stock = await screen.findByRole('region', { name: 'Stock' })
    expect(cifra(stock, 'Productos')).toHaveTextContent('23')
    expect(cifra(stock, 'Sin stock')).toHaveTextContent('3')
    expect(cifra(stock, 'Stock bajo')).toHaveTextContent('2')
    expect(within(stock).getByText(/de 1 a 5 unidades/i)).toBeInTheDocument()
    expect(cifra(stock, 'No disponibles')).toHaveTextContent('3')
  })
})

describe('SellerStatistics (HU17): sin datos', () => {
  it('HU17-CP17: sin ventas ni valoraciones, avisa de que aún no hay en vez de mostrar tablas vacías', async () => {
    await abrirEstadisticas('vendedor-vacio@esibuy.test')

    const ventas = await screen.findByRole('region', { name: 'Ventas' })
    expect(cifra(ventas, 'Pedidos')).toHaveTextContent('0')
    expect(within(ventas).getByText(/aún no tienes ventas/i)).toBeInTheDocument()
    expect(within(ventas).queryByRole('table')).not.toBeInTheDocument()

    const valoraciones = screen.getByRole('region', { name: 'Valoraciones' })
    expect(cifra(valoraciones, 'Valoración media')).toHaveTextContent('Sin valoraciones')
    expect(within(valoraciones).getByText(/aún no tienes valoraciones/i)).toBeInTheDocument()
    expect(within(valoraciones).queryByRole('table')).not.toBeInTheDocument()

    const stock = screen.getByRole('region', { name: 'Stock' })
    expect(cifra(stock, 'Productos')).toHaveTextContent('0')
  })
})

describe('SellerStatistics (HU17): acceso', () => {
  it('HU17-CP18: el cliente no ve las estadísticas y no se le piden al backend', async () => {
    let pedidas = false
    server.use(
      http.get('/api/seller/statistics', () => {
        pedidas = true
        return new HttpResponse(null, { status: 403 })
      }),
    )
    await abrirSesionPorApi('cliente@esibuy.test')
    renderApp('/profile')

    await screen.findByLabelText('Nombre')
    expect(screen.queryByRole('region', { name: 'Mis estadísticas' })).not.toBeInTheDocument()
    expect(pedidas).toBe(false)
  })
})

describe('SellerStatistics (HU17): errores', () => {
  it('HU17-CP19: si las estadísticas no cargan, avisa sin detalles internos, permite reintentar y el perfil sigue visible', async () => {
    server.use(
      http.get('/api/seller/statistics', () => HttpResponse.json({ message: 'MongoTimeoutException' }, { status: 500 }), {
        once: true,
      }),
    )
    const user = userEvent.setup()
    const seccion = await abrirEstadisticas('vendedor@esibuy.test')

    const aviso = await within(seccion).findByText(/no se han podido cargar tus estadísticas/i)
    expect(aviso).toBeInTheDocument()
    expect(screen.queryByText(/mongotimeout/i)).not.toBeInTheDocument()
    expect(screen.getByLabelText('Nombre')).toHaveValue('Vera')
    await user.click(within(seccion).getByRole('button', { name: /reintentar/i }))

    const ventas = await screen.findByRole('region', { name: 'Ventas' })
    expect(cifra(ventas, 'Pedidos')).toHaveTextContent('6')
    expect(within(seccion).queryByText(/no se han podido cargar/i)).not.toBeInTheDocument()
  })
})

describe('SellerStatistics (HU17): carga', () => {
  it('HU17-CP20: mientras cargan las estadísticas, la sección muestra que se están cargando', async () => {
    let responder
    const respuesta = new Promise((resolve) => {
      responder = resolve
    })
    server.use(
      http.get('/api/seller/statistics', async () => {
        await respuesta
        return HttpResponse.json({
          ventas: { pedidos: 0, unidadesVendidas: 0, ingresos: 0, masVendidos: [] },
          valoraciones: { media: null, total: 0, porProducto: [] },
          stock: { productos: 0, sinStock: 0, stockBajo: 0, noDisponibles: 0 },
        })
      }),
    )
    const seccion = await abrirEstadisticas('vendedor@esibuy.test')

    const cargando = await within(seccion).findByRole('progressbar', { name: /cargando tus estadísticas/i })
    expect(cargando).toBeInTheDocument()
    responder()

    await within(seccion).findByRole('region', { name: 'Ventas' })
    await waitFor(() => expect(within(seccion).queryByRole('progressbar')).not.toBeInTheDocument())
  })
})
