import { useCallback } from 'react'
import { getSellerProducts } from '../../api/products'
import { useApiQuery } from '../../hooks/useApiQuery'

export function useSellerProducts({ search, category, page, size }) {
  const fetcher = useCallback(
    () => getSellerProducts({ search, category, page, size }),
    [search, category, page, size],
  )
  return useApiQuery(fetcher)
}
