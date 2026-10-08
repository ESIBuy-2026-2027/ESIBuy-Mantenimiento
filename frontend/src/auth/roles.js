import { PATHS } from '../routes/paths'

export const ROLES = {
  ADMIN: 'ADMINISTRADOR',
  VENDEDOR: 'VENDEDOR',
  CLIENTE: 'CLIENTE',
}

const ROLE_HOME = new Map([
  [ROLES.ADMIN, PATHS.ADMIN],
  [ROLES.VENDEDOR, PATHS.SELLER_PRODUCTS],
  [ROLES.CLIENTE, PATHS.PRODUCTS],
])

export function getHomePath(role) {
  return ROLE_HOME.get(role) ?? PATHS.HOME
}
