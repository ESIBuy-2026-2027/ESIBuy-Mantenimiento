import { Payment24Regular, Phone24Regular, Wallet24Regular } from '@fluentui/react-icons'

// Cómo se presenta cada tipo de método de pago que devuelve el backend (HU6)
export const TIPOS_DE_METODO = {
  TARJETA: { etiqueta: 'Tarjeta', Icono: Payment24Regular },
  PAYPAL: { etiqueta: 'PayPal', Icono: Wallet24Regular },
  BIZUM: { etiqueta: 'Bizum', Icono: Phone24Regular },
}

// «Bizum •••• 678»: el método tal como se nombra en los textos
export const nombreDeMetodo = (metodo) => `${TIPOS_DE_METODO[metodo.tipo].etiqueta} ${metodo.mascara}`
