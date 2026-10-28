import { http, HttpResponse } from 'msw'
import { getUsuarioEnSesion } from './authHandlers'
import { rechazarSiNoEsCliente, retirarGrupoDelCarrito } from './cartHandlers'
import { metodosDePagoDe } from './paymentMethodsHandlers'

let pedidosCreados = 0

export function resetOrdersMock() {
  pedidosCreados = 0
}

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
    return HttpResponse.json(
      {
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
      },
      { status: 201 },
    )
  }),
]
