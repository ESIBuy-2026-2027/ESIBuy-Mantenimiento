import { useCallback } from 'react'
import { getSellerProducts } from '../../api/products'
import { useApiQuery } from '../../hooks/useApiQuery'

export function useSellerProducts({ search, category, status, sort, minPrice, maxPrice, page, size }) {
  const fetcher = useCallback(
    () => getSellerProducts({ search, category, status, sort, minPrice, maxPrice, page, size }),
    [search, category, status, sort, minPrice, maxPrice, page, size],
  )
  return useApiQuery(fetcher)
}
