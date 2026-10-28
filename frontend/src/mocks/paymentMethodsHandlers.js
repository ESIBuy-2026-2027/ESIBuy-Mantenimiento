import { http, HttpResponse } from 'msw'
import { getUsuarioEnSesion } from './authHandlers'

// Métodos de pago de los clientes de ejemplo (HU6), con la forma que devuelve el backend (PR #148): solo la máscara,
// nunca los datos de pago. Carlos (u-3) tiene una tarjeta predeterminada y un Bizum; el resto de clientes, ninguno.
const METODOS_INICIALES = {
  'u-3': [
    { id: 'm-1', tipo: 'TARJETA', mascara: '•••• 1111', predeterminado: true },
    { id: 'm-2', tipo: 'BIZUM', mascara: '•••• 678', predeterminado: false },
  ],
}

let metodosPorCliente = {}

export function resetPaymentMethodsMock() {
  metodosPorCliente = structuredClone(METODOS_INICIALES)
}

resetPaymentMethodsMock()

const URL_METODOS = '/api/customer/payment-methods'

// Como el backend: /api/customer/** es solo del rol CLIENTE, y el cliente sale de la sesión
export const paymentMethodsHandlers = [
  http.get(URL_METODOS, () => {
    const usuario = getUsuarioEnSesion()
    if (!usuario) return new HttpResponse(null, { status: 401 })
    if (usuario.rol !== 'CLIENTE') return new HttpResponse(null, { status: 403 })
    return HttpResponse.json(metodosPorCliente[usuario.id] ?? [])
  }),
]
