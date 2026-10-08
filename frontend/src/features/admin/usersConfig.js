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

// Estados que pueden tener clientes y administradores: solo activos o desactivados.
export const ACCOUNT_STATUS_LABELS = {
  [USER_STATUS.ACTIVO]: STATUS_LABELS[USER_STATUS.ACTIVO],
  [USER_STATUS.DESACTIVADO]: STATUS_LABELS[USER_STATUS.DESACTIVADO],
}

// Opciones de ordenación de los listados de clientes y administradores.
export const ACCOUNT_SORT_OPTIONS = [
  { value: 'name', label: 'Nombre' },
  { value: 'status', label: 'Estado' },
]
