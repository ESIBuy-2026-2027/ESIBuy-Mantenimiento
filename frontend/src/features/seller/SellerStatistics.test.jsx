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
  renderApp('/seller/statistics')
  await screen.findByRole('heading', { name: 'Mis estadísticas' })
  return screen.getByRole('main')
}

const cifra = (bloque, nombre) =>
  within(bloque).getAllByText(nombre).find((element) => element.tagName === 'DT').closest('dl')

const filasDe = (tabla) =>
  within(tabla)
    .getAllByRole('row')
    .slice(1)
    .map((fila) => within(fila).getAllByRole('cell').map((celda) => celda.textContent))

describe('SellerStatistics (HU17): ventas', () => {
  it('HU17-CP14: muestra las ventas resumidas y los rankings más y menos vendidos', async () => {
    await abrirEstadisticas('vendedor@esibuy.test')

    const resumen = await screen.findByRole('region', { name: 'Resumen' })
    expect(cifra(resumen, 'Pedidos')).toHaveTextContent('6')
    expect(cifra(resumen, 'Unidades vendidas')).toHaveTextContent('16')
    expect(cifra(resumen, 'Ingresos')).toHaveTextContent('1095,83')
    expect(cifra(resumen, 'Valoraciones recibidas')).toHaveTextContent('8')

    const ventas = await screen.findByRole('region', { name: 'Ventas' })

    const masVendidos = within(ventas).getByRole('table', { name: 'Top 10 más vendidos' })
    const filas = filasDe(masVendidos)
    expect(filas.map(([producto, unidades]) => [producto, unidades])).toEqual([
      ['Teclado mecánico', '5'],
      ['Auriculares inalámbricos', '4'],
      ['Cargador USB-C 65 W', '3'],
      ['Memoria USB 128 GB', '2'],
      ['Monitor 27 pulgadas', '1'],
    ])
    expect(filas[0][2]).toMatch(/^447,50\s€$/)

    const menosVendidos = within(ventas).getByRole('table', { name: 'Top 10 menos vendidos' })
    expect(filasDe(menosVendidos)).toEqual([
      ['Alfombra de salón', '0', '0,00\u00a0€'],
      ['Altavoz Bluetooth', '0', '0,00\u00a0€'],
      ['Aspirador de mano', '0', '0,00\u00a0€'],
    ])
  })
})

describe('SellerStatistics (HU17): valoraciones', () => {
  it('HU17-CP15: muestra los productos valorados del mejor al peor y la media global', async () => {
    await abrirEstadisticas('vendedor@esibuy.test')

    const resumen = await screen.findByRole('region', { name: 'Resumen' })
    expect(cifra(resumen, 'Valoración media')).toHaveTextContent('4,4 de 5')

    const valoraciones = await screen.findByRole('region', { name: 'Valoraciones' })
    const mejorValorados = within(valoraciones).getByRole('table', { name: 'Top 10 mejor valorados' })
    expect(filasDe(mejorValorados)).toEqual([
      ['Teclado mecánico', '4,5', '6'],
      ['Auriculares inalámbricos', '4,0', '2'],
    ])
    const peorValorados = within(valoraciones).getByRole('table', { name: 'Top 10 peor valorados' })
    expect(filasDe(peorValorados)).toEqual([
      ['Auriculares inalámbricos', '4,0', '2'],
      ['Teclado mecánico', '4,5', '6'],
    ])
  })
})

describe('SellerStatistics (HU17): stock', () => {
  it('HU17-CP16: identifica visualmente sus productos con stock bajo o sin stock', async () => {
    await abrirEstadisticas('vendedor@esibuy.test')

    const stock = await screen.findByRole('region', { name: 'Stock' })
    expect(cifra(stock, 'Productos')).toHaveTextContent('23')
    expect(cifra(stock, 'Sin stock')).toHaveTextContent('3')
    expect(cifra(stock, 'Stock bajo')).toHaveTextContent('2')
    expect(within(stock).getByText(/de 1 a 5 unidades/i)).toBeInTheDocument()
    const productosConStockBajo = within(stock).getByRole('table', { name: 'Productos con stock bajo' })
    expect(filasDe(productosConStockBajo)).toEqual([
      ['Aspirador de mano', '3 unidades'],
      ['Robot de cocina', '5 unidades'],
    ])
    const productosSinStock = within(stock).getByRole('table', { name: 'Productos sin stock' })
    expect(filasDe(productosSinStock)).toEqual([
      ['Freidora de aire', 'Sin stock'],
      ['Ratón ergonómico', 'Sin stock'],
      ['Webcam Full HD', 'Sin stock'],
    ])
    expect(cifra(stock, 'No disponibles')).toHaveTextContent('3')
  })
})

describe('SellerStatistics (HU17): sin datos', () => {
  it('HU17-CP17: sin ventas, valoraciones ni alertas de stock, muestra los estados vacíos', async () => {
    await abrirEstadisticas('vendedor-vacio@esibuy.test')

    const ventas = await screen.findByRole('region', { name: 'Ventas' })
    expect(within(ventas).getByText(/aún no hay productos vendidos/i)).toBeInTheDocument()
    expect(within(ventas).getByText(/aún no hay productos en tu inventario/i)).toBeInTheDocument()
    expect(within(ventas).queryByRole('table')).not.toBeInTheDocument()

    const resumen = screen.getByRole('region', { name: 'Resumen' })
    expect(cifra(resumen, 'Pedidos')).toHaveTextContent('0')
    expect(cifra(resumen, 'Unidades vendidas')).toHaveTextContent('0')
    expect(cifra(resumen, 'Ingresos')).toHaveTextContent('0,00')
    expect(cifra(resumen, 'Valoración media')).toHaveTextContent('Sin valoraciones')

    const valoraciones = screen.getByRole('region', { name: 'Valoraciones' })
    expect(within(valoraciones).getAllByText(/aún no hay productos valorados/i)).toHaveLength(2)
    expect(within(valoraciones).queryByRole('table')).not.toBeInTheDocument()

    const stock = screen.getByRole('region', { name: 'Stock' })
    expect(cifra(stock, 'Productos')).toHaveTextContent('0')
    expect(within(stock).getByText(/no tienes productos con stock bajo/i)).toBeInTheDocument()
    expect(within(stock).getByText(/no tienes productos sin stock/i)).toBeInTheDocument()
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
    renderApp('/seller/statistics')

    await waitFor(() => expect(screen.getByTestId('ruta-actual')).toHaveTextContent('/products'))
    expect(screen.queryByRole('heading', { name: 'Mis estadísticas' })).not.toBeInTheDocument()
    expect(pedidas).toBe(false)
  })
})

describe('SellerStatistics (HU17): errores', () => {
  it('HU17-CP19: si falla la petición o la respuesta no cumple el contrato, avisa y permite reintentar', async () => {
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
    expect(within(seccion).getByRole('heading', { name: 'Mis estadísticas' })).toBeInTheDocument()

    let respuestaIncompatibleRecibida = false
    server.use(
      http.get(
        '/api/seller/statistics',
        () => {
          respuestaIncompatibleRecibida = true
          return HttpResponse.json({
            ventas: { pedidos: 6, unidadesVendidas: 16, ingresos: 1095.83, masVendidos: [] },
            valoraciones: { media: 4.375, total: 8, porProducto: [] },
            stock: {
              productos: 23,
              sinStock: 3,
              stockBajo: 2,
              noDisponibles: 3,
              productosConStockBajo: [],
            },
          })
        },
        { once: true },
      ),
    )
    await user.click(within(seccion).getByRole('button', { name: /reintentar/i }))
    await waitFor(() => expect(respuestaIncompatibleRecibida).toBe(true))
    await waitFor(() => {
      expect(within(seccion).getByText(/no se han podido cargar tus estadísticas/i)).toBeInTheDocument()
      expect(screen.queryByRole('heading', { name: 'Unexpected Application Error!' })).not.toBeInTheDocument()
      expect(within(seccion).getByRole('heading', { name: 'Mis estadísticas' })).toBeInTheDocument()
    })

    await user.click(within(seccion).getByRole('button', { name: /reintentar/i }))
    const resumen = await screen.findByRole('region', { name: 'Resumen' })
    expect(cifra(resumen, 'Pedidos')).toHaveTextContent('6')
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
          ventas: { pedidos: 0, unidadesVendidas: 0, ingresos: 0, masVendidos: [], menosVendidos: [] },
          valoraciones: {
            media: null,
            total: 0,
            porProducto: [],
            mejorValorados: [],
            peorValorados: [],
          },
          stock: {
            productos: 0,
            sinStock: 0,
            stockBajo: 0,
            productosConStockBajo: [],
            productosSinStock: [],
            noDisponibles: 0,
          },
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
