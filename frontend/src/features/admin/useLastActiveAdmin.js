import { useCallback } from 'react'
import { listUsers } from '../../api/users'
import { ROLES } from '../../auth/roles'
import { useApiQuery } from '../../hooks/useApiQuery'
import { USER_STATUS } from './usersConfig'

// Dice cuál es el último administrador activo (HU19, AB#250), si solo queda uno: un administrador no puede
// bloquearse ni eliminarse si es el último, y la tabla pagina y filtra, así que no lo sabe por sí sola. Pide al
// listado cuántos administradores activos hay (basta una fila y el total). El backend lo sigue imponiendo con un
// 409 LAST_ACTIVE_ADMIN. `enabled=false` no pide nada: los listados que no tienen administradores no lo necesitan.
// `refresh` vuelve a calcularlo tras bloquear o eliminar a alguien.
export function useLastActiveAdmin(enabled = true) {
  const fetcher = useCallback(
    () =>
      enabled
        ? listUsers({ role: ROLES.ADMIN, status: USER_STATUS.ACTIVO, page: 1, size: 1 })
        : Promise.resolve(null),
    [enabled],
  )
  const { data, retry } = useApiQuery(fetcher)

  return { lastAdminId: data?.totalItems === 1 ? (data.items[0]?.id ?? null) : null, refresh: retry }
}
