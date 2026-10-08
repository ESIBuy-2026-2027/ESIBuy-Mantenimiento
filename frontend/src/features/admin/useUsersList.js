import { useCallback } from 'react'
import { listUsers } from '../../api/users'
import { useApiQuery } from '../../hooks/useApiQuery'

export function useUsersList({ search, role, status, page, size }) {
  const fetcher = useCallback(
    () => listUsers({ search, role, status, page, size }),
    [search, role, status, page, size],
  )
  return useApiQuery(fetcher)
}
