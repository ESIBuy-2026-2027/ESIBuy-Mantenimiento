import { useCallback } from 'react'
import { getFeaturedProducts } from '../../api/products'
import { useApiQuery } from '../../hooks/useApiQuery'
import FeaturedShowcase from './FeaturedShowcase'

const LIMITE_DESTACADOS = 10
const MINIMO_VALORADOS = 3

// Los diez productos mejor valorados son públicos para que la portada funcione sin iniciar sesión.
export default function FeaturedProducts() {
  const fetcher = useCallback(() => getFeaturedProducts(LIMITE_DESTACADOS), [])
  const { data } = useApiQuery(fetcher)
  const rated = (data ?? []).filter((product) => product.valoracion !== null)

  if (rated.length < MINIMO_VALORADOS) return null

  return <FeaturedShowcase products={rated} />
}
