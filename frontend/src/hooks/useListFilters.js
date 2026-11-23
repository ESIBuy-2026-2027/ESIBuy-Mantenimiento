import { useState } from 'react'
import { useDebouncedValue } from './useDebouncedValue'

export function useListFilters() {
  const [search, setSearch] = useState('')
  const [category, setCategory] = useState('')
  const [status, setStatus] = useState('')
  const [sort, setSort] = useState('')
  const [minPrice, setMinPrice] = useState('0')
  const [maxPrice, setMaxPrice] = useState('200')
  const [page, setPage] = useState(1)
  const debouncedSearch = useDebouncedValue(search)
  const debouncedMinPrice = useDebouncedValue(minPrice)
  const debouncedMaxPrice = useDebouncedValue(maxPrice)

  return {
    search,
    category,
    status,
    sort,
    minPrice,
    maxPrice,
    page,
    debouncedSearch,
    debouncedMinPrice,
    debouncedMaxPrice,
    filtersActive: Boolean(
      search || category || status || sort || minPrice !== '0' || maxPrice !== '200',
    ),
    setPage,
    changeSearch: (value) => {
      setSearch(value)
      setPage(1)
    },
    changeCategory: (value) => {
      setCategory(value)
      setPage(1)
    },
    changeStatus: (value) => {
      setStatus(value)
      setPage(1)
    },
    changeSort: (value) => {
      setSort(value)
      setPage(1)
    },
    changeMinPrice: (value) => {
      setMinPrice(value)
      setPage(1)
    },
    changeMaxPrice: (value) => {
      setMaxPrice(value)
      setPage(1)
    },
    clearFilters: () => {
      setSearch('')
      setCategory('')
      setStatus('')
      setSort('')
      setMinPrice('0')
      setMaxPrice('200')
      setPage(1)
    },
  }
}
