import { Payment20Regular, Phone20Regular, Wallet20Regular } from '@fluentui/react-icons'

// Nombre e icono de cada tipo de método de pago de la HU6, tal y como llega del backend (`tipo`).
const TIPOS = {
  TARJETA: { label: 'Tarjeta', Icon: Payment20Regular },
  PAYPAL: { label: 'PayPal', Icon: Wallet20Regular },
  BIZUM: { label: 'Bizum', Icon: Phone20Regular },
}

export const paymentMethodType = (tipo) => TIPOS[tipo] ?? { label: tipo, Icon: Payment20Regular }
