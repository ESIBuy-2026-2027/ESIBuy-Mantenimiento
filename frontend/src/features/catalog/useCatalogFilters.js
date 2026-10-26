import { useState } from 'react'
import { useListFilters } from '../../hooks/useListFilters'

// Búsqueda y categoría son los de los demás listados; el catálogo añade la valoración mínima y el orden. El
// catálogo no tiene páginas que recordar (carga más al llegar al final), así que un cambio de filtro u orden basta
// para que `useCatalogFeed` empiece de nuevo.
export function useCatalogFilters() {
  const base = useListFilters()
  const [minRating, setMinRating] = useState('')
  const [sort, setSort] = useState('nombre')
  const [order, setOrder] = useState('asc')

  return {
    ...base,
    minRating,
    sort,
    order,
    filtersActive: base.filtersActive || Boolean(minRating),
    changeMinRating: setMinRating,
    changeSort: setSort,
    changeOrder: setOrder,
    clearFilters: () => {
      base.clearFilters()
      setMinRating('')
    },
  }
}
