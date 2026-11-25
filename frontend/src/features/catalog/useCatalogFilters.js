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
  const [priceRange, setPriceRange] = useState([0, null])
  const [availableMaxPrice, setAvailableMaxPrice] = useState(0)

  return {
    ...base,
    minRating,
    sort,
    order,
    inStock,
    offersOnly,
    minPrice: priceRange[0],
    maxPrice: priceRange[1],
    priceCeiling: availableMaxPrice,
    filtersActive: base.filtersActive || Boolean(
      minRating ||
      inStock ||
      offersOnly ||
      priceRange[0] > 0 ||
      (priceRange[1] !== null && priceRange[1] < availableMaxPrice) ||
      sort !== 'nombre' ||
      order !== 'asc',
    ),
    changeMinRating: setMinRating,
    changeSort: setSort,
    changeOrder: setOrder,
    changeInStock: setInStock,
    changeOffersOnly: setOffersOnly,
    changePriceRange: (min, max) => setPriceRange([min, max]),
    changeAvailableMaxPrice: setAvailableMaxPrice,
    clearFilters: () => {
      base.clearFilters()
      setMinRating('')
      setSort('nombre')
      setOrder('asc')
      setInStock(false)
      setOffersOnly(false)
      setPriceRange([0, null])
    },
  }
}
