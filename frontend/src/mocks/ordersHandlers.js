import { http, HttpResponse } from 'msw'
import { getUsuarioEnSesion } from './authHandlers'
import { rechazarSiNoEsCliente, retirarGrupoDelCarrito } from './cartHandlers'
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
// de Nora (u-7), que Carlos no puede ver.
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
  },
]

let pedidosCreados = 0
let pedidos = pedidosIniciales()

export function resetOrdersMock() {
  pedidosCreados = 0
  pedidos = pedidosIniciales()
}

// Lo que el backend devuelve de un pedido: sin el cliente, que es el de la sesión
const aRespuesta = (pedido) => Object.fromEntries(Object.entries(pedido).filter(([campo]) => campo !== 'clienteId'))

export const ordersHandlers = [
  // Crea el pedido de un proveedor con lo que hay de él en el carrito y lo quita del carrito (HU10). El método de pago
  // tiene que ser uno de los del cliente (HU6).
  http.post('/api/orders', async ({ request }) => {
    const rechazo = rechazarSiNoEsCliente()
    if (rechazo) return rechazo

    const { vendedorId, paymentMethodId } = await request.json()
    const usuarioId = getUsuarioEnSesion().id
    const metodoDelCliente = metodosDePagoDe(usuarioId).some(({ id }) => id === paymentMethodId)
    if (typeof vendedorId !== 'string' || vendedorId === '' || !metodoDelCliente) {
      return HttpResponse.json({ code: 'VALIDATION_ERROR' }, { status: 400 })
    }

    const grupo = retirarGrupoDelCarrito(usuarioId, vendedorId)
    if (!grupo) return HttpResponse.json({ code: 'CART_EMPTY' }, { status: 409 })

    pedidosCreados += 1
    const creado = {
      id: `o-${pedidosCreados}`,
      vendedor: grupo.vendedor,
      total: grupo.total,
      estado: 'PAGADO',
      paymentMethodId,
      lineas: grupo.lineas.map(({ productoId, nombre, cantidad, precioUnitario, subtotal }) => ({
        productoId,
        nombre,
        cantidad,
        precioUnitario,
        subtotal,
      })),
    }
    // También aparece en el historial (HU11), como el más reciente
    pedidos.unshift({
      ...creado,
      clienteId: usuarioId,
      numeroPedido: `ESB-2026-${String(pedidosCreados).padStart(6, '0')}`,
      direccionEnvio: DIRECCION_DE_CARLOS,
      createdAt: new Date().toISOString(),
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
]
