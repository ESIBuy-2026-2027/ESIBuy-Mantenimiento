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

const MAXIMO_METODOS = 5
const LIMITE_ALCANZADO = { mensaje: 'Se ha alcanzado el máximo de métodos de pago', code: 'PAYMENT_METHOD_LIMIT_REACHED' }
let siguienteId = 100

// La misma máscara que calcula el backend para cada tipo
function mascaraDe(metodo) {
  if (metodo.tipo === 'TARJETA') return `•••• ${metodo.numeroTarjeta.slice(-4)}`
  if (metodo.tipo === 'PAYPAL') return new URL(metodo.urlPayPal).hostname
  return `•••• ${metodo.telefonoBizum.slice(-3)}`
}

const SUSTITUTO_OBLIGATORIO = {
  mensaje: 'Hay que elegir el nuevo método de pago predeterminado',
  code: 'DEFAULT_PAYMENT_METHOD_REQUIRED',
}

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

  // Como el backend: máximo 5, el primero queda predeterminado y solo se devuelve la máscara (nunca se guarda el CVV)
  http.post(URL_METODOS, async ({ request }) => {
    const { error, metodos } = clienteEnSesion()
    if (error) return error
    if (metodos.length >= MAXIMO_METODOS) return HttpResponse.json(LIMITE_ALCANZADO, { status: 409 })
    const datos = await request.json()
    siguienteId += 1
    const metodo = { id: `m-${siguienteId}`, tipo: datos.tipo, mascara: mascaraDe(datos), predeterminado: metodos.length === 0 }
    metodos.push(metodo)
    return HttpResponse.json(metodo, { status: 201 })
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

  // Como el backend: al eliminar el predeterminado, si quedan otros, hay que elegir el sustituto (no el mismo)
  http.delete(`${URL_METODOS}/:id`, ({ request, params }) => {
    const { error, metodos } = clienteEnSesion()
    if (error) return error
    const eliminado = metodos.find(({ id }) => id === params.id)
    if (!eliminado) return HttpResponse.json(NO_ENCONTRADO, { status: 404 })
    const restantes = metodos.filter(({ id }) => id !== params.id)
    if (eliminado.predeterminado && restantes.length > 0) {
      const sustituto = restantes.find(({ id }) => id === new URL(request.url).searchParams.get('nuevoPredeterminado'))
      if (!sustituto) return HttpResponse.json(SUSTITUTO_OBLIGATORIO, { status: 409 })
      sustituto.predeterminado = true
    }
    metodos.splice(metodos.indexOf(eliminado), 1)
    return new HttpResponse(null, { status: 204 })
  }),
]
