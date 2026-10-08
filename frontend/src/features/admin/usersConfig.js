import { ROLES } from '../../auth/roles'

export const USERS_PAGE_SIZE = 10

export const USER_STATUS = {
  PENDIENTE: 'PENDIENTE',
  ACTIVO: 'ACTIVO',
  BLOQUEADO: 'BLOQUEADO',
  DESACTIVADO: 'DESACTIVADO',
}

export const ROLE_LABELS = {
  [ROLES.ADMIN]: 'Administrador',
  [ROLES.VENDEDOR]: 'Vendedor',
  [ROLES.CLIENTE]: 'Cliente',
}

export const STATUS_LABELS = {
  [USER_STATUS.PENDIENTE]: 'Pendiente',
  [USER_STATUS.ACTIVO]: 'Activo',
  [USER_STATUS.BLOQUEADO]: 'Bloqueado',
  [USER_STATUS.DESACTIVADO]: 'Desactivado',
}

// Estados que pueden tener clientes y administradores: solo los vendedores pasan por "Pendiente".
export const ACCOUNT_STATUS_LABELS = Object.fromEntries(
  Object.entries(STATUS_LABELS).filter(([status]) => status !== USER_STATUS.PENDIENTE),
)

// Estados desde los que se puede activar una cuenta (HU24). "Bloqueado" equivale en la práctica a desactivado.
export const ACTIVATABLE_STATUSES = new Set([USER_STATUS.BLOQUEADO, USER_STATUS.DESACTIVADO])
