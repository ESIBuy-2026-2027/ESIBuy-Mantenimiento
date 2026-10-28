import { describe, it, expect } from 'vitest'
import { http, HttpResponse } from 'msw'
import { server } from '../mocks/server'
import { ApiError } from './client'
import {
  addPaymentMethod,
  deletePaymentMethod,
  getPaymentMethods,
  setDefaultPaymentMethod,
} from './paymentMethods'

// Cliente de la API de métodos de pago del cliente (HU6): GET, POST, PUT /{id}/default y DELETE /{id} sobre
// /api/customer/payment-methods. Los mocks de MSW sustituyen al backend.

const URL_METODOS = '/api/customer/payment-methods'

// Lo que devuelve el backend: solo la máscara, nunca los datos de pago
const METODOS = [
  { id: 'm1', tipo: 'TARJETA', mascara: '•••• 1111', predeterminado: true },
  { id: 'm2', tipo: 'BIZUM', mascara: '•••• 678', predeterminado: false },
]

describe('api de métodos de pago', () => {
  it('HU6-CP25: getPaymentMethods pide la lista de métodos del cliente en sesión', async () => {
    server.use(http.get(URL_METODOS, () => HttpResponse.json(METODOS)))

    const metodos = await getPaymentMethods()

    expect(metodos).toEqual(METODOS)
  })

  it('HU6-CP25: addPaymentMethod envía los datos del método con un POST', async () => {
    const peticion = {}
    server.use(
      http.post(URL_METODOS, async ({ request }) => {
        peticion.cuerpo = await request.json()
        return HttpResponse.json(METODOS[0], { status: 201 })
      }),
    )
    // El CVV viaja en la petición para que el backend lo valide, pero no se guarda (HU6)
    const tarjeta = {
      tipo: 'TARJETA',
      numeroTarjeta: '4111111111111111',
      titular: 'Juan Pérez',
      caducidad: '12/27',
      cvv: '123',
    }

    const creado = await addPaymentMethod(tarjeta)

    expect(peticion.cuerpo).toEqual(tarjeta)
    expect(creado).toEqual(METODOS[0])
  })

  it('HU6-CP25: setDefaultPaymentMethod marca el método con un PUT a /{id}/default', async () => {
    const peticion = {}
    server.use(
      http.put(`${URL_METODOS}/:id/default`, ({ params }) => {
        peticion.id = params.id
        return new HttpResponse(null, { status: 204 })
      }),
    )

    const respuesta = await setDefaultPaymentMethod('m2')

    expect(peticion.id).toBe('m2')
    expect(respuesta).toBeNull()
  })

  it('HU6-CP25: deletePaymentMethod elimina el método y envía el sustituto solo si se indica', async () => {
    const peticiones = []
    server.use(
      http.delete(`${URL_METODOS}/:id`, ({ request, params }) => {
        peticiones.push({ id: params.id, url: new URL(request.url) })
        return new HttpResponse(null, { status: 204 })
      }),
    )

    // Al eliminar el predeterminado, el cliente elige cuál lo sustituye
    await deletePaymentMethod('m1', 'm2')
    // Un método que no es el predeterminado se elimina sin sustituto
    await deletePaymentMethod('m3')

    expect(peticiones[0].id).toBe('m1')
    expect(peticiones[0].url.searchParams.get('nuevoPredeterminado')).toBe('m2')
    expect(peticiones[1].id).toBe('m3')
    expect(peticiones[1].url.searchParams.has('nuevoPredeterminado')).toBe(false)
  })

  it('HU6-CP25: un error del backend llega como ApiError con su código', async () => {
    server.use(
      http.post(URL_METODOS, () =>
        HttpResponse.json(
          { mensaje: 'Se ha alcanzado el máximo de métodos de pago', code: 'PAYMENT_METHOD_LIMIT_REACHED' },
          { status: 409 },
        ),
      ),
    )

    const error = await addPaymentMethod({ tipo: 'BIZUM', telefonoBizum: '612345678' }).catch((e) => e)

    // La vista decide el mensaje a partir del código, no del texto del backend
    expect(error).toBeInstanceOf(ApiError)
    expect(error.status).toBe(409)
    expect(error.code).toBe('PAYMENT_METHOD_LIMIT_REACHED')
  })
})
