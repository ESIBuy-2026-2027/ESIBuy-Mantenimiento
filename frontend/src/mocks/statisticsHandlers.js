import { http, HttpResponse } from 'msw'
import { getUsuarioEnSesion } from './authHandlers'

const SIN_ESTADISTICAS = {
  periodo: 'TODO_EL_PERIODO',
  ventas: {
    pedidos: 0,
    unidadesVendidas: 0,
    ingresos: 0,
    masVendidos: [],
    menosVendidos: [],
    granularidad: 'DIA',
    evolucion: [],
  },
  valoraciones: {
    media: null,
    total: 0,
    porProducto: [],
    mejorValorados: [],
    peorValorados: [],
    distribucion: [1, 2, 3, 4, 5].map((puntuacion) => ({ puntuacion, numero: 0, porcentaje: 0 })),
  },
  stock: {
    productos: 0,
    sinStock: 0,
    stockBajo: 0,
    noDisponibles: 0,
    productosConStockBajo: [],
    productosSinStock: [],
  },
}

const fechaMock = new Date()
const mesesHistoricos = fechaMock.getMonth() + 1
const valoresMensuales = mesesHistoricos >= 3
  ? [
      { pedidos: 2, unidades: 4, ingresos: 268.6 },
      { pedidos: 2, unidades: 5, ingresos: 165.15 },
      { pedidos: 2, unidades: 7, ingresos: 662.08 },
    ]
  : [{ pedidos: 6, unidades: 16, ingresos: 1095.83 }]
const indicesMeses = mesesHistoricos >= 3
  ? [0, Math.floor((mesesHistoricos - 1) / 2), mesesHistoricos - 1]
  : [mesesHistoricos - 1]
const diasHistoricos = Math.floor(Date.UTC(fechaMock.getFullYear(), fechaMock.getMonth(), fechaMock.getDate())
  - Date.UTC(fechaMock.getFullYear(), 0, 1)) / 86400000 + 1
const ventasPorDia = new Map(indicesMeses.map((indice, posicion) => {
  const diaDelMes = indice === fechaMock.getMonth()
    ? fechaMock.getDate()
    : new Date(fechaMock.getFullYear(), indice + 1, 0).getDate()
  const fecha = `${fechaMock.getFullYear()}-${String(indice + 1).padStart(2, '0')}-${String(diaDelMes).padStart(2, '0')}`
  return [fecha, valoresMensuales[posicion]]
}))
const EVOLUCION_TODO_EL_PERIODO = Array.from({ length: diasHistoricos }, (_, indice) => {
  const fecha = new Date(Date.UTC(fechaMock.getFullYear(), 0, indice + 1)).toISOString().slice(0, 10)
  return { fecha, ...(ventasPorDia.get(fecha) ?? { pedidos: 0, unidades: 0, ingresos: 0 }) }
})
const evolucionPorFecha = new Map(EVOLUCION_TODO_EL_PERIODO.map((punto) => [punto.fecha, punto]))
const EVOLUCION_ULTIMOS_SIETE_DIAS = Array.from({ length: 7 }, (_, indice) => {
  const fecha = new Date(fechaMock.getFullYear(), fechaMock.getMonth(), fechaMock.getDate() - 6 + indice)
  const fechaISO = [fecha.getFullYear(), String(fecha.getMonth() + 1).padStart(2, '0'),
    String(fecha.getDate()).padStart(2, '0')].join('-')
  return evolucionPorFecha.get(fechaISO) ?? { fecha: fechaISO, pedidos: 0, unidades: 0, ingresos: 0 }
})

// Estadísticas fijas de los vendedores de ejemplo (HU17), con la forma que devuelve el backend (PR #212). Las de
// Vera (u-2) usa productos del mock y su stock; Vicente (u-5) no tiene ventas, valoraciones ni productos.
const ESTADISTICAS = {
  'u-2': {
    periodo: 'TODO_EL_PERIODO',
    ventas: {
      pedidos: 6,
      unidadesVendidas: 16,
      ingresos: 1095.83,
      masVendidos: [
        { productoId: 'p-2', nombre: 'Teclado mecánico', imagen: '', unidades: 5, ingresos: 447.5 },
        { productoId: 'p-1', nombre: 'Auriculares inalámbricos', imagen: '', unidades: 4, ingresos: 239.6 },
        { productoId: 'p-5', nombre: 'Cargador USB-C 65 W', imagen: '', unidades: 3, ingresos: 89.85 },
        { productoId: 'p-16', nombre: 'Memoria USB 128 GB', imagen: '', unidades: 2, ingresos: 29.98 },
        { productoId: 'p-4', nombre: 'Monitor 27 pulgadas', imagen: '', unidades: 1, ingresos: 249 },
      ],
      menosVendidos: [
        { productoId: 'p-20', nombre: 'Alfombra de salón', imagen: '', unidades: 0, ingresos: 0 },
        { productoId: 'p-7', nombre: 'Altavoz Bluetooth', imagen: '', unidades: 0, ingresos: 0 },
        { productoId: 'p-11', nombre: 'Aspirador de mano', imagen: '', unidades: 0, ingresos: 0 },
      ],
      granularidad: 'DIA',
      evolucion: EVOLUCION_TODO_EL_PERIODO,
    },
    valoraciones: {
      media: 4.375,
      total: 8,
      porProducto: [
        { productoId: 'p-2', nombre: 'Teclado mecánico', media: 4.5, numero: 6 },
        { productoId: 'p-1', nombre: 'Auriculares inalámbricos', media: 4, numero: 2 },
      ],
      mejorValorados: [
        {
          productoId: 'p-2',
          nombre: 'Teclado mecánico',
          imagen: '',
          media: 4.5,
          numero: 6,
        },
        {
          productoId: 'p-1',
          nombre: 'Auriculares inalámbricos',
          imagen: '',
          media: 4,
          numero: 2,
        },
      ],
      peorValorados: [
        {
          productoId: 'p-1',
          nombre: 'Auriculares inalámbricos',
          imagen: '',
          media: 4,
          numero: 2,
        },
        {
          productoId: 'p-2',
          nombre: 'Teclado mecánico',
          imagen: '',
          media: 4.5,
          numero: 6,
        },
      ],
      distribucion: [
        { puntuacion: 1, numero: 0, porcentaje: 0 },
        { puntuacion: 2, numero: 1, porcentaje: 12.5 },
        { puntuacion: 3, numero: 1, porcentaje: 12.5 },
        { puntuacion: 4, numero: 2, porcentaje: 25 },
        { puntuacion: 5, numero: 4, porcentaje: 50 },
      ],
    },
    stock: {
      productos: 23,
      sinStock: 3,
      stockBajo: 2,
      noDisponibles: 3,
      productosConStockBajo: [
        { productoId: 'p-11', nombre: 'Aspirador de mano', stock: 3 },
        { productoId: 'p-13', nombre: 'Robot de cocina', stock: 5 },
      ],
      productosSinStock: [
        { productoId: 'p-14', nombre: 'Freidora de aire', stock: 0 },
        { productoId: 'p-3', nombre: 'Ratón ergonómico', stock: 0 },
        { productoId: 'p-6', nombre: 'Webcam Full HD', stock: 0 },
      ],
    },
  },
}

const ESTADISTICAS_ADMINISTRADOR = {
  productosMasVendidos: [
    { productoId: 'p-1', nombre: 'Auriculares inalámbricos', unidades: 12, ingresos: 719.4 },
    { productoId: 'p-2', nombre: 'Teclado mecánico', unidades: 9, ingresos: 805.5 },
  ],
  productosMejorValorados: [
    { productoId: 'p-2', nombre: 'Teclado mecánico', media: 4.8, numeroValoraciones: 6 },
    { productoId: 'p-1', nombre: 'Auriculares inalámbricos', media: 4.5, numeroValoraciones: 4 },
  ],
  vendedoresMejorValorados: [
    { vendedorId: 'u-2', nombreComercial: 'TecnoVera', media: 4.6, numeroValoraciones: 10 },
  ],
}

// Como el backend (PR #212): solo el vendedor, sacado de la sesión. Sin sesión, 401; con otro rol, 403.
export const statisticsHandlers = [
  http.get('/api/admin/statistics', () => {
    const usuario = getUsuarioEnSesion()
    if (!usuario) return new HttpResponse(null, { status: 401 })
    if (usuario.rol !== 'ADMINISTRADOR') return new HttpResponse(null, { status: 403 })

    return HttpResponse.json(ESTADISTICAS_ADMINISTRADOR)
  }),
  http.get('/api/seller/statistics', ({ request }) => {
    const usuario = getUsuarioEnSesion()
    if (!usuario) return new HttpResponse(null, { status: 401 })
    if (usuario.rol !== 'VENDEDOR') return new HttpResponse(null, { status: 403 })

    const periodo = new URL(request.url).searchParams.get('periodo') ?? 'TODO_EL_PERIODO'
    if (periodo !== 'TODO_EL_PERIODO') {
      return HttpResponse.json({ mensaje: 'Periodo no válido' }, { status: 400 })
    }
    const stats = ESTADISTICAS[usuario.id] ?? SIN_ESTADISTICAS
    return HttpResponse.json({
      ...stats,
      periodo,
      ventas: {
        ...stats.ventas,
        granularidad: 'DIA',
        evolucion: EVOLUCION_ULTIMOS_SIETE_DIAS,
      },
    })
  }),
]
