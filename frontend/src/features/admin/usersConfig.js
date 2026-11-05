import { ROLES } from '../../auth/roles'

export const USERS_PAGE_SIZE = 10

export const USER_STATUS = {
  PENDIENTE: 'PENDIENTE',
  ACTIVO: 'ACTIVO',
  BLOQUEADO: 'BLOQUEADO',
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
}

// Estados que pueden tener clientes y administradores: pendientes hasta que un administrador los
// verifica (HU1), activos o bloqueados (los administradores no tienen el estado pendiente).
export const ACCOUNT_STATUS_LABELS = {
  [USER_STATUS.PENDIENTE]: STATUS_LABELS[USER_STATUS.PENDIENTE],
  [USER_STATUS.ACTIVO]: STATUS_LABELS[USER_STATUS.ACTIVO],
  [USER_STATUS.BLOQUEADO]: STATUS_LABELS[USER_STATUS.BLOQUEADO],
}

// Estados que se pueden elegir al filtrar por un rol. Los administradores no tienen el estado pendiente; los
// clientes sí (hasta que un administrador los verifica).
export function statusLabelsForRole(rol) {
  if (rol === ROLES.ADMIN) {
    return Object.fromEntries(
      Object.entries(ACCOUNT_STATUS_LABELS).filter(([estado]) => estado !== USER_STATUS.PENDIENTE),
    )
  }
  return rol === ROLES.CLIENTE ? ACCOUNT_STATUS_LABELS : STATUS_LABELS
}

// Opciones de ordenación de los listados de clientes y administradores.
export const ACCOUNT_SORT_OPTIONS = [
  { value: 'name', label: 'Nombre' },
  { value: 'status', label: 'Estado' },
]
