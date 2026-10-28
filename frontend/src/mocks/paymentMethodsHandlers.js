import { http, HttpResponse } from 'msw'
import { getUsuarioEnSesion } from './authHandlers'
import { rechazarSiNoEsCliente } from './cartHandlers'

// Métodos de pago de los clientes de ejemplo (HU6), ya con la máscara que devuelve el backend. Carlos
// (cliente@esibuy.test) tiene una tarjeta predeterminada y un Bizum; el resto de clientes, ninguno.
const METODOS_DE_PAGO = {
  'u-3': [
    { id: 'mp-1', tipo: 'TARJETA', mascara: '**** **** **** 4242', predeterminado: true },
    { id: 'mp-2', tipo: 'BIZUM', mascara: '*** *** 678', predeterminado: false },
  ],
}

export const metodosDePagoDe = (usuarioId) => METODOS_DE_PAGO[usuarioId] ?? []

export const paymentMethodsHandlers = [
  http.get('/api/customer/payment-methods', () => {
    const rechazo = rechazarSiNoEsCliente()
    if (rechazo) return rechazo
    return HttpResponse.json(metodosDePagoDe(getUsuarioEnSesion().id))
  }),
]
