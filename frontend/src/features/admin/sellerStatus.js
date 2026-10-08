const SELLER_STATUS = new Map([
  ['ACTIVO', { label: 'Activo', color: 'success' }],
  ['PENDIENTE', { label: 'Pendiente', color: 'warning' }],
  ['DESACTIVADO', { label: 'Desactivado', color: 'subtle' }],
])

const ORDER_STATUS = new Map([
  ['PENDIENTE', { label: 'Pendiente', color: 'warning' }],
  ['ENVIADO', { label: 'Enviado', color: 'informative' }],
  ['ENTREGADO', { label: 'Entregado', color: 'success' }],
  ['CANCELADO', { label: 'Cancelado', color: 'danger' }],
])

export const getSellerStatusOptions = () =>
  [...SELLER_STATUS].map(([value, { label }]) => ({ value, label }))

const UNKNOWN_STATUS = { label: 'Desconocido', color: 'subtle' }

export const getSellerStatus = (status) => SELLER_STATUS.get(status) ?? UNKNOWN_STATUS
export const getOrderStatus = (status) => ORDER_STATUS.get(status) ?? UNKNOWN_STATUS
