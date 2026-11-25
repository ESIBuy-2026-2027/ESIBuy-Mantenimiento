import { useState } from 'react'
import { useListFilters } from '../../hooks/useListFilters'

// El catálogo añade valoración, stock, ofertas y orden a la búsqueda y categoría compartidas.
export function useCatalogFilters() {
  const base = useListFilters()
  const [minRating, setMinRating] = useState('')
  const [sort, setSort] = useState('nombre')
  const [order, setOrder] = useState('asc')
  const [inStock, setInStock] = useState(false)
  const [offersOnly, setOffersOnly] = useState(false)

  return {
    ...base,
    minRating,
    sort,
    order,
    inStock,
    offersOnly,
    filtersActive: base.filtersActive || Boolean(
      minRating || inStock || offersOnly || sort !== 'nombre' || order !== 'asc',
    ),
    changeMinRating: setMinRating,
    changeSort: setSort,
    changeOrder: setOrder,
    changeInStock: setInStock,
    changeOffersOnly: setOffersOnly,
    clearFilters: () => {
      base.clearFilters()
      setMinRating('')
      setSort('nombre')
      setOrder('asc')
      setInStock(false)
      setOffersOnly(false)
    },
  }
}
