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
