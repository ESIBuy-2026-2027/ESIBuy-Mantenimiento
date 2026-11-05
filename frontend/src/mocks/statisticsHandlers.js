import { http, HttpResponse } from 'msw'
import { getUsuarioEnSesion } from './authHandlers'

const SIN_ESTADISTICAS = {
  ventas: { pedidos: 0, unidadesVendidas: 0, ingresos: 0, masVendidos: [] },
  valoraciones: { media: null, total: 0, porProducto: [] },
  stock: { productos: 0, sinStock: 0, stockBajo: 0, noDisponibles: 0 },
}

// Estadísticas fijas de los vendedores de ejemplo (HU17), con la forma que devuelve el backend (PR #212). Las de
// Vera (u-2) usan sus productos del mock de productos y su stock; Vicente (u-5) no tiene ventas, valoraciones ni
// productos. Vera ha vendido 6 productos distintos, pero el backend solo manda los 5 más vendidos.
const ESTADISTICAS = {
  'u-2': {
    ventas: {
      pedidos: 6,
      unidadesVendidas: 16,
      ingresos: 1095.83,
      masVendidos: [
        { productoId: 'p-2', nombre: 'Teclado mecánico', unidades: 5, ingresos: 447.5 },
        { productoId: 'p-1', nombre: 'Auriculares inalámbricos', unidades: 4, ingresos: 239.6 },
        { productoId: 'p-5', nombre: 'Cargador USB-C 65 W', unidades: 3, ingresos: 89.85 },
        { productoId: 'p-16', nombre: 'Memoria USB 128 GB', unidades: 2, ingresos: 29.98 },
        { productoId: 'p-4', nombre: 'Monitor 27 pulgadas', unidades: 1, ingresos: 249 },
      ],
    },
    valoraciones: {
      media: 4.375,
      total: 8,
      porProducto: [
        { productoId: 'p-2', nombre: 'Teclado mecánico', media: 4.5, numero: 6 },
        { productoId: 'p-1', nombre: 'Auriculares inalámbricos', media: 4, numero: 2 },
      ],
    },
    stock: { productos: 23, sinStock: 3, stockBajo: 2, noDisponibles: 3 },
  },
}

// Como el backend (PR #212): solo el vendedor, sacado de la sesión. Sin sesión, 401; con otro rol, 403.
export const statisticsHandlers = [
  http.get('/api/seller/statistics', () => {
    const usuario = getUsuarioEnSesion()
    if (!usuario) return new HttpResponse(null, { status: 401 })
    if (usuario.rol !== 'VENDEDOR') return new HttpResponse(null, { status: 403 })

    return HttpResponse.json(ESTADISTICAS[usuario.id] ?? SIN_ESTADISTICAS)
  }),
]
