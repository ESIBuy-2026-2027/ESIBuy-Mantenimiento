import { describe, it, expect } from 'vitest'
import { http, HttpResponse } from 'msw'
import { server } from '../mocks/server'
import { getPaymentMethods } from './paymentMethods'

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

  it('HU6-CP25: addPaymentMethod envía los datos del método con un POST', () => {
    expect.fail('Funcionalidad no implementada')
  })

  it('HU6-CP25: setDefaultPaymentMethod marca el método con un PUT a /{id}/default', () => {
    expect.fail('Funcionalidad no implementada')
  })

  it('HU6-CP25: deletePaymentMethod elimina el método y envía el sustituto solo si se indica', () => {
    expect.fail('Funcionalidad no implementada')
  })

  it('HU6-CP25: un error del backend llega como ApiError con su código', () => {
    expect.fail('Funcionalidad no implementada')
  })
})
