import { useCallback } from 'react'
import { getCatalogProducts } from '../../api/products'
import { useApiQuery } from '../../hooks/useApiQuery'

export function useCatalogProducts({ search, category, minRating, sort, order, page, size }) {
  const fetcher = useCallback(
    () => getCatalogProducts({ search, category, minRating, sort, order, page, size }),
    [search, category, minRating, sort, order, page, size],
  )
  return useApiQuery(fetcher)
}
