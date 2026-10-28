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

const NO_ENCONTRADO = { mensaje: 'El método de pago no existe', code: 'PAYMENT_METHOD_NOT_FOUND' }

// El cliente en sesión, o la respuesta de error si no hay sesión (401) o no es cliente (403)
function clienteEnSesion() {
  const usuario = getUsuarioEnSesion()
  if (!usuario) return { error: new HttpResponse(null, { status: 401 }) }
  if (usuario.rol !== 'CLIENTE') return { error: new HttpResponse(null, { status: 403 }) }
  metodosPorCliente[usuario.id] ??= []
  return { metodos: metodosPorCliente[usuario.id] }
}

// Como el backend: /api/customer/** es solo del rol CLIENTE, y el cliente sale de la sesión
export const paymentMethodsHandlers = [
  http.get(URL_METODOS, () => {
    const { error, metodos } = clienteEnSesion()
    return error ?? HttpResponse.json(metodos)
  }),

  // Solo uno predeterminado; uno de otro cliente se responde igual que uno inexistente (404)
  http.put(`${URL_METODOS}/:id/default`, ({ params }) => {
    const { error, metodos } = clienteEnSesion()
    if (error) return error
    if (!metodos.some(({ id }) => id === params.id)) return HttpResponse.json(NO_ENCONTRADO, { status: 404 })
    metodos.forEach((metodo) => {
      metodo.predeterminado = metodo.id === params.id
    })
    return new HttpResponse(null, { status: 204 })
  }),
]
