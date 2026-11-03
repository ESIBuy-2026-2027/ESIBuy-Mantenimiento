import { http, HttpResponse } from 'msw'
import { getUsuarioEnSesion } from './authHandlers'

// Direcciones de envío de los clientes de ejemplo (HU5), con la forma que devuelve el backend (PR #161). Carlos (u-3)
// tiene dos: la principal en Ciudad Real y otra en Madrid; el resto de clientes, ninguna.
const DIRECCIONES_INICIALES = {
  'u-3': [
    {
      id: 'd-1',
      destinatario: 'Carlos Cliente',
      calle: 'Calle Mayor 1',
      detalles: null,
      codigoPostal: '13071',
      ciudad: 'Ciudad Real',
      provincia: 'Ciudad Real',
      telefono: '612345678',
      principal: true,
    },
    {
      id: 'd-2',
      destinatario: 'Carlos Cliente',
      calle: 'Calle Toledo 5',
      detalles: '2º B',
      codigoPostal: '28005',
      ciudad: 'Madrid',
      provincia: 'Madrid',
      telefono: '612345678',
      principal: false,
    },
  ],
}

let direccionesPorCliente = {}

export function resetAddressesMock() {
  direccionesPorCliente = structuredClone(DIRECCIONES_INICIALES)
}

resetAddressesMock()

// Para comprobar que un pedido (HU10) se envía a una dirección del cliente
export function direccionesDe(usuarioId) {
  return direccionesPorCliente[usuarioId] ?? []
}

const URL_DIRECCIONES = '/api/customer/addresses'

const MAXIMO_DIRECCIONES = 5
const LIMITE_ALCANZADO = { mensaje: 'Se ha alcanzado el máximo de direcciones de envío', code: 'ADDRESS_LIMIT_REACHED' }
const NO_ENCONTRADA = { mensaje: 'La dirección de envío no existe', code: 'ADDRESS_NOT_FOUND' }
const SUSTITUTA_OBLIGATORIA = {
  mensaje: 'Hay que elegir la nueva dirección de envío principal',
  code: 'PRINCIPAL_ADDRESS_REQUIRED',
}
let siguienteId = 100

// Solo los campos que guarda el backend; unos detalles vacíos se guardan como ausentes
function datosDe(cuerpo) {
  const { destinatario, calle, detalles, codigoPostal, ciudad, provincia, telefono } = cuerpo
  return { destinatario, calle, detalles: detalles?.trim() ? detalles : null, codigoPostal, ciudad, provincia, telefono }
}

// El cliente en sesión, o la respuesta de error si no hay sesión (401) o no es cliente (403)
function clienteEnSesion() {
  const usuario = getUsuarioEnSesion()
  if (!usuario) return { error: new HttpResponse(null, { status: 401 }) }
  if (usuario.rol !== 'CLIENTE') return { error: new HttpResponse(null, { status: 403 }) }
  direccionesPorCliente[usuario.id] ??= []
  return { direcciones: direccionesPorCliente[usuario.id] }
}

// Como el backend: /api/customer/** es solo del rol CLIENTE, y el cliente sale de la sesión. Una dirección de otro
// cliente se responde igual que una inexistente (404).
export const addressesHandlers = [
  http.get(URL_DIRECCIONES, () => {
    const { error, direcciones } = clienteEnSesion()
    return error ?? HttpResponse.json(direcciones)
  }),

  // Como el backend: máximo 5 y la primera queda como principal
  http.post(URL_DIRECCIONES, async ({ request }) => {
    const { error, direcciones } = clienteEnSesion()
    if (error) return error
    if (direcciones.length >= MAXIMO_DIRECCIONES) return HttpResponse.json(LIMITE_ALCANZADO, { status: 409 })
    siguienteId += 1
    const direccion = { id: `d-${siguienteId}`, ...datosDe(await request.json()), principal: direcciones.length === 0 }
    direcciones.push(direccion)
    return HttpResponse.json(direccion, { status: 201 })
  }),

  // Editar no cambia cuál es la principal
  http.put(`${URL_DIRECCIONES}/:id`, async ({ request, params }) => {
    const { error, direcciones } = clienteEnSesion()
    if (error) return error
    const direccion = direcciones.find(({ id }) => id === params.id)
    if (!direccion) return HttpResponse.json(NO_ENCONTRADA, { status: 404 })
    Object.assign(direccion, datosDe(await request.json()))
    return HttpResponse.json(direccion)
  }),

  http.put(`${URL_DIRECCIONES}/:id/principal`, ({ params }) => {
    const { error, direcciones } = clienteEnSesion()
    if (error) return error
    if (!direcciones.some(({ id }) => id === params.id)) return HttpResponse.json(NO_ENCONTRADA, { status: 404 })
    direcciones.forEach((direccion) => {
      direccion.principal = direccion.id === params.id
    })
    return new HttpResponse(null, { status: 204 })
  }),

  // Como el backend: al eliminar la principal, si quedan otras, hay que elegir la sustituta (no la misma)
  http.delete(`${URL_DIRECCIONES}/:id`, ({ request, params }) => {
    const { error, direcciones } = clienteEnSesion()
    if (error) return error
    const eliminada = direcciones.find(({ id }) => id === params.id)
    if (!eliminada) return HttpResponse.json(NO_ENCONTRADA, { status: 404 })
    const restantes = direcciones.filter(({ id }) => id !== params.id)
    if (eliminada.principal && restantes.length > 0) {
      const sustituta = restantes.find(({ id }) => id === new URL(request.url).searchParams.get('nuevaPrincipal'))
      if (!sustituta) return HttpResponse.json(SUSTITUTA_OBLIGATORIA, { status: 409 })
      sustituta.principal = true
    }
    direcciones.splice(direcciones.indexOf(eliminada), 1)
    return new HttpResponse(null, { status: 204 })
  }),
]
