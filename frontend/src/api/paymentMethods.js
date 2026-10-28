import { request } from './client'

// Métodos de pago del cliente en sesión (HU6, backend del PR #148). Solo llega la máscara, nunca los datos en claro:
//   [{ id, tipo: 'TARJETA' | 'PAYPAL' | 'BIZUM', mascara, predeterminado }]
export function listPaymentMethods() {
  return request('/api/customer/payment-methods')
}
