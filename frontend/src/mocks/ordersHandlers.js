import { http, HttpResponse } from 'msw'
import { direccionesDe } from './addressesHandlers'
import { getUsuarioEnSesion } from './authHandlers'
import { grupoDelCarrito, rechazarSiNoEsCliente, retirarGrupoDelCarrito } from './cartHandlers'
import { metodosDePagoDe } from './paymentMethodsHandlers'

const DIRECCION_DE_CARLOS = {
  destinatario: 'Carlos Cliente',
  calle: 'Calle Mayor 1',
  detalles: null,
  codigoPostal: '13001',
  ciudad: 'Ciudad Real',
  provincia: 'Ciudad Real',
  telefono: '611222333',
}

const linea = (productoId, nombre, cantidad, precioUnitario) => ({
  productoId,
  nombre,
  cantidad,
  precioUnitario,
  subtotal: Math.round(cantidad * precioUnitario * 100) / 100,
})

// Pedidos ya hechos (HU11), en estados distintos para ver la línea de estados del envío: tres de Carlos (u-3) y uno
// de Nora (u-7), que Carlos no puede ver. `historial`, `fechaEstimadaEntrega` y `fechaEntrega` son del seguimiento del
// envío (PBI 540): se sirven por /tracking, no en el pedido, como el backend.
const pedidosIniciales = () => [
  {
    id: 'o-103',
    clienteId: 'u-3',
    numeroPedido: 'ESB-2026-000103',
    vendedor: { id: 'u-2', nombreComercial: 'TecnoVera' },
    total: 197.32,
    estado: 'ENVIADO',
    paymentMethodId: 'mp-1',
    direccionEnvio: DIRECCION_DE_CARLOS,
    lineas: [linea('p-1', 'Auriculares', 2, 53.91), linea('p-2', 'Teclado', 1, 89.5)],
    createdAt: '2026-10-30T10:00:00Z',
    historial: [
      { estado: 'PAGADO', fecha: '2026-10-30T10:00:00Z' },
      { estado: 'PREPARANDO', fecha: '2026-10-30T15:00:00Z' },
      { estado: 'ENVIADO', fecha: '2026-11-01T09:00:00Z' },
    ],
    fechaEstimadaEntrega: '2026-11-05T00:00:00Z',
    fechaEntrega: null,
  },
  {
    id: 'o-102',
    clienteId: 'u-3',
    numeroPedido: 'ESB-2026-000102',
    vendedor: { id: 'u-99', nombreComercial: 'Otero Hogar y Tecnología' },
    total: 129,
    estado: 'ENTREGADO',
    paymentMethodId: 'mp-1',
    direccionEnvio: DIRECCION_DE_CARLOS,
    lineas: [linea('p-24', 'Smartwatch', 1, 129)],
    createdAt: '2026-10-20T10:00:00Z',
    historial: [
      { estado: 'PAGADO', fecha: '2026-10-20T10:00:00Z' },
      { estado: 'PREPARANDO', fecha: '2026-10-20T16:00:00Z' },
      { estado: 'ENVIADO', fecha: '2026-10-22T09:00:00Z' },
      { estado: 'ENTREGADO', fecha: '2026-10-25T12:30:00Z' },
    ],
    fechaEstimadaEntrega: '2026-10-26T00:00:00Z',
    fechaEntrega: '2026-10-25T12:30:00Z',
  },
  {
    id: 'o-101',
    clienteId: 'u-3',
    numeroPedido: 'ESB-2026-000101',
    vendedor: { id: 'u-2', nombreComercial: 'TecnoVera' },
    total: 19.99,
    estado: 'CANCELADO',
    paymentMethodId: 'mp-1',
    direccionEnvio: DIRECCION_DE_CARLOS,
    lineas: [linea('p-3', 'Ratón', 1, 19.99)],
    createdAt: '2026-10-10T10:00:00Z',
    historial: [{ estado: 'PAGADO', fecha: '2026-10-10T10:00:00Z' }],
    fechaEstimadaEntrega: null,
    fechaEntrega: null,
  },
  {
    id: 'o-201',
    clienteId: 'u-7',
    numeroPedido: 'ESB-2026-000201',
    vendedor: { id: 'u-2', nombreComercial: 'TecnoVera' },
    total: 89.5,
    estado: 'PAGADO',
    paymentMethodId: 'mp-9',
    direccionEnvio: { ...DIRECCION_DE_CARLOS, destinatario: 'Nora Nueva' },
    lineas: [linea('p-2', 'Teclado', 1, 89.5)],
    createdAt: '2026-10-25T10:00:00Z',
    historial: [{ estado: 'PAGADO', fecha: '2026-10-25T10:00:00Z' }],
    fechaEstimadaEntrega: null,
    fechaEntrega: null,
  },
]

let pedidosCreados = 0
let pedidos = pedidosIniciales()

export function resetOrdersMock() {
  pedidosCreados = 0
  pedidos = pedidosIniciales()
}

// Lo que el backend devuelve de un pedido: sin el cliente, que es el de la sesión, ni el seguimiento del envío
const CAMPOS_FUERA_DEL_PEDIDO = ['clienteId', 'historial', 'fechaEstimadaEntrega', 'fechaEntrega']
const aRespuesta = (pedido) =>
  Object.fromEntries(Object.entries(pedido).filter(([campo]) => !CAMPOS_FUERA_DEL_PEDIDO.includes(campo)))

// Lo que el backend devuelve del seguimiento del envío de un pedido
const aSeguimiento = ({ id, estado, fechaEstimadaEntrega, fechaEntrega, historial }) => ({
  pedidoId: id,
  estado,
  fechaEstimadaEntrega,
  fechaEntrega,
  historial,
})

export const ordersHandlers = [
  // Crea el pedido de un proveedor con lo que hay de él en el carrito y lo quita del carrito (HU10). La dirección (HU5)
  // y el método de pago (HU6) tienen que ser del cliente; si no, 400 como en el backend, sin decir cuál falla. La
  // dirección se copia en el pedido.
  http.post('/api/customer/orders', async ({ request }) => {
    const rechazo = rechazarSiNoEsCliente()
    if (rechazo) return rechazo

    const { vendedorId, direccionId, paymentMethodId, totalEsperado } = await request.json()
    const usuarioId = getUsuarioEnSesion().id
    const metodo = metodosDePagoDe(usuarioId).find(({ id }) => id === paymentMethodId)
    const direccion = direccionesDe(usuarioId).find(({ id }) => id === direccionId)
    if (typeof vendedorId !== 'string' || vendedorId === '' || !metodo || !direccion || typeof totalEsperado !== 'number') {
      return HttpResponse.json({ code: 'VALIDATION_ERROR' }, { status: 400 })
    }

    const grupo = grupoDelCarrito(usuarioId, vendedorId)
    if (!grupo) return HttpResponse.json({ code: 'CART_EMPTY' }, { status: 409 })
    // El cliente confirma el total que vio: si ya no es el real, no se hace nada (como el backend)
    if (Math.round(totalEsperado * 100) !== Math.round(grupo.total * 100)) {
      return HttpResponse.json({ code: 'TOTAL_MISMATCH', mensaje: 'El total del pedido ha cambiado' }, { status: 409 })
    }
    // Pago simulado, como el backend: la tarjeta de prueba acabada en 0000 se rechaza
    if (metodo.tipo === 'TARJETA' && metodo.mascara.endsWith('0000')) {
      return HttpResponse.json({ code: 'PAYMENT_REJECTED', mensaje: 'El pago ha sido rechazado' }, { status: 402 })
    }
    retirarGrupoDelCarrito(usuarioId, vendedorId)

    pedidosCreados += 1
    const { destinatario, calle, detalles, codigoPostal, ciudad, provincia, telefono } = direccion
    const creado = {
      id: `o-${pedidosCreados}`,
      numeroPedido: `ESB-2026-${String(pedidosCreados).padStart(6, '0')}`,
      vendedor: grupo.vendedor,
      total: grupo.total,
      estado: 'PAGADO',
      paymentMethodId,
      direccionEnvio: { destinatario, calle, detalles, codigoPostal, ciudad, provincia, telefono },
      lineas: grupo.lineas.map(({ productoId, nombre, cantidad, precioUnitario, subtotal }) => ({
        productoId,
        nombre,
        cantidad,
        precioUnitario,
        subtotal,
      })),
      createdAt: new Date().toISOString(),
    }
    // También aparece en el historial (HU11), como el más reciente; su seguimiento empieza en pagado
    pedidos.unshift({
      ...creado,
      clienteId: usuarioId,
      historial: [{ estado: 'PAGADO', fecha: creado.createdAt }],
      fechaEstimadaEntrega: null,
      fechaEntrega: null,
    })
    return HttpResponse.json(creado, { status: 201 })
  }),

  // Historial del cliente en sesión (HU11), del más reciente al más antiguo
  http.get('/api/customer/orders', () => {
    const rechazo = rechazarSiNoEsCliente()
    if (rechazo) return rechazo

    const usuarioId = getUsuarioEnSesion().id
    const suyos = pedidos
      .filter((pedido) => pedido.clienteId === usuarioId)
      .sort((a, b) => b.createdAt.localeCompare(a.createdAt))
    return HttpResponse.json(suyos.map(aRespuesta))
  }),

  // Detalle de un pedido (HU11): uno que no existe o es de otro cliente responde 400, como el backend
  http.get('/api/customer/orders/:id', ({ params }) => {
    const rechazo = rechazarSiNoEsCliente()
    if (rechazo) return rechazo

    const pedido = pedidos.find(({ id, clienteId }) => id === params.id && clienteId === getUsuarioEnSesion().id)
    return pedido
      ? HttpResponse.json(aRespuesta(pedido))
      : HttpResponse.json({ mensaje: 'El pedido no existe', code: 'ORDER_NOT_FOUND' }, { status: 400 })
  }),

  // Cancelación (HU12), como el backend: solo un pedido del cliente que aún no se ha enviado (PAGADO o PREPARANDO)
  http.post('/api/customer/orders/:id/cancel', ({ params }) => {
    const rechazo = rechazarSiNoEsCliente()
    if (rechazo) return rechazo

    const pedido = pedidos.find(({ id, clienteId }) => id === params.id && clienteId === getUsuarioEnSesion().id)
    if (!pedido) {
      return HttpResponse.json({ mensaje: 'El pedido no existe', code: 'ORDER_NOT_FOUND' }, { status: 400 })
    }
    if (pedido.estado !== 'PAGADO' && pedido.estado !== 'PREPARANDO') {
      return HttpResponse.json(
        { mensaje: 'El pedido ya no se puede cancelar', code: 'ORDER_NOT_CANCELLABLE' },
        { status: 409 },
      )
    }
    pedido.estado = 'CANCELADO'
    return HttpResponse.json(aRespuesta(pedido))
  }),

  // Seguimiento del envío (PBI 540): estado, fechas e histórico; uno ajeno o inexistente responde 400, como el backend
  http.get('/api/customer/orders/:id/tracking', ({ params }) => {
    const rechazo = rechazarSiNoEsCliente()
    if (rechazo) return rechazo

    const pedido = pedidos.find(({ id, clienteId }) => id === params.id && clienteId === getUsuarioEnSesion().id)
    return pedido
      ? HttpResponse.json(aSeguimiento(pedido))
      : HttpResponse.json({ mensaje: 'El pedido no existe', code: 'ORDER_NOT_FOUND' }, { status: 400 })
  }),

  // Confirmación de recepción por el cliente (PBI 540), como el backend: solo desde ENVIADO, y no se deshace
  http.post('/api/customer/orders/:id/confirm-delivery', ({ params }) => {
    const rechazo = rechazarSiNoEsCliente()
    if (rechazo) return rechazo

    const pedido = pedidos.find(({ id, clienteId }) => id === params.id && clienteId === getUsuarioEnSesion().id)
    if (!pedido) {
      return HttpResponse.json({ mensaje: 'El pedido no existe', code: 'ORDER_NOT_FOUND' }, { status: 400 })
    }
    if (pedido.estado !== 'ENVIADO') {
      return HttpResponse.json(
        { mensaje: 'El pedido no se puede marcar como entregado', code: 'ORDER_NOT_DELIVERABLE' },
        { status: 409 },
      )
    }
    const ahora = new Date().toISOString()
    pedido.estado = 'ENTREGADO'
    pedido.fechaEntrega = ahora
    pedido.historial = [...pedido.historial, { estado: 'ENTREGADO', fecha: ahora }]
    return HttpResponse.json(aSeguimiento(pedido))
  }),
]
