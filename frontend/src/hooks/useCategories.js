import { useCallback } from 'react'
import { getCategories } from '../api/products'
import { useApiQuery } from './useApiQuery'

// Si las categorías no cargan, los filtros se quedan solo con "Todas las categorías" y el
// listado sigue funcionando; el formulario de producto sí avisa con `failed` y ofrece `retry`.
export function useCategories() {
  const fetcher = useCallback(() => getCategories(), [])
  const { data, failed, retry } = useApiQuery(fetcher)

  return { categories: data ?? [], loaded: data !== null, failed, retry }
}
