// Avisos con los que la pasarela de Premium (HU45) vuelve a la vista Premium, en el estado de la navegación (nunca en
// la URL): se ha hecho Premium ahora o el servidor responde que ya lo era.
export const AVISOS_PREMIUM = {
  ACTIVADO: 'ACTIVADO',
  YA_ERA_PREMIUM: 'YA_ERA_PREMIUM',
}

export const TEXTO_AVISO_PREMIUM = {
  [AVISOS_PREMIUM.ACTIVADO]: { intent: 'success', texto: '¡Bienvenido a ESIBuy Premium! Ya disfrutas de sus ventajas.' },
  [AVISOS_PREMIUM.YA_ERA_PREMIUM]: { intent: 'info', texto: 'Ya eras cliente ESIBuy Premium: no hacía falta pagar.' },
}
