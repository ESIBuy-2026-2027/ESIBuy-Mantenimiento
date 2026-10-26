import { useState } from 'react'
import { useListFilters } from '../../hooks/useListFilters'

// Búsqueda, categoría y paginación son los de los demás listados; el catálogo añade la valoración mínima y el orden.
// Cualquier cambio de filtro o de orden vuelve a la primera página.
export function useCatalogFilters() {
  const base = useListFilters()
  const { setPage } = base
  const [minRating, setMinRating] = useState('')
  const [sort, setSort] = useState('nombre')
  const [order, setOrder] = useState('asc')

  const volverALaPrimera = (setter) => (value) => {
    setter(value)
    setPage(1)
  }

  return {
    ...base,
    minRating,
    sort,
    order,
    filtersActive: base.filtersActive || Boolean(minRating),
    changeMinRating: volverALaPrimera(setMinRating),
    changeSort: volverALaPrimera(setSort),
    changeOrder: volverALaPrimera(setOrder),
    clearFilters: () => {
      base.clearFilters()
      setMinRating('')
    },
  }
}
