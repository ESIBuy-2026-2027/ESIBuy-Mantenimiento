import { useCallback } from 'react'
import { getCatalogProducts } from '../../api/products'
import { useApiQuery } from '../../hooks/useApiQuery'

export function useCatalogProducts({ page, size }) {
  const fetcher = useCallback(() => getCatalogProducts({ page, size }), [page, size])
  return useApiQuery(fetcher)
}
