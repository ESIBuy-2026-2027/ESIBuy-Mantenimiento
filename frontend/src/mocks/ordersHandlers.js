import { http, HttpResponse } from 'msw'
import { getUsuarioEnSesion } from './authHandlers'
import { rechazarSiNoEsCliente, retirarGrupoDelCarrito } from './cartHandlers'

let pedidosCreados = 0

export function resetOrdersMock() {
  pedidosCreados = 0
}

export const ordersHandlers = [
  // Crea el pedido de un proveedor con lo que hay de él en el carrito y lo quita del carrito (HU10).
  http.post('/api/orders', async ({ request }) => {
    const rechazo = rechazarSiNoEsCliente()
    if (rechazo) return rechazo

    const { vendedorId } = await request.json()
    if (typeof vendedorId !== 'string' || vendedorId === '') {
      return HttpResponse.json({ code: 'VALIDATION_ERROR' }, { status: 400 })
    }

    const grupo = retirarGrupoDelCarrito(getUsuarioEnSesion().id, vendedorId)
    if (!grupo) return HttpResponse.json({ code: 'CART_EMPTY' }, { status: 409 })

    pedidosCreados += 1
    return HttpResponse.json(
      {
        id: `o-${pedidosCreados}`,
        vendedor: grupo.vendedor,
        total: grupo.total,
        estado: 'PAGADO',
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
