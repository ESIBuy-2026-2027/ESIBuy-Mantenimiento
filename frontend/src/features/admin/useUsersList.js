import { useCallback } from 'react'
import { listUsers } from '../../api/users'
import { useApiQuery } from '../../hooks/useApiQuery'

export function useUsersList({ search, role, status, page, size, searchBy, sort, order }) {
  const fetcher = useCallback(
    () => listUsers({ search, role, status, page, size, searchBy, sort, order }),
    [search, role, status, page, size, searchBy, sort, order],
  )
  return useApiQuery(fetcher)
}
