import { useCallback } from 'react'
import { getCatalogProducts } from '../../api/products'
import { useApiQuery } from '../../hooks/useApiQuery'
import FeaturedShowcase from './FeaturedShowcase'

// Cuántos productos se piden y cuántos hacen falta con valoración para que la selección tenga sentido.
const PEDIDOS = 10
const MINIMO_VALORADOS = 3

// Los diez productos mejor valorados (HU7). Pide los mejores al mismo endpoint del catálogo, por valoración
// descendente, y solo se pinta si hay suficientes con valoración; si falla, la vista sigue sin ella.
export default function FeaturedProducts() {
  const fetcher = useCallback(() => getCatalogProducts({ sort: 'valoracion', order: 'desc', page: 1, size: PEDIDOS }), [])
  const { data } = useApiQuery(fetcher)
  const rated = (data?.items ?? []).filter((product) => product.valoracion !== null)

  if (rated.length < MINIMO_VALORADOS) return null

  return <FeaturedShowcase products={rated} />
}
