import { useState } from 'react'
import { useDebouncedValue } from './useDebouncedValue'

export function useListFilters() {
  const [search, setSearch] = useState('')
  const [category, setCategory] = useState('')
  const [page, setPage] = useState(1)
  const debouncedSearch = useDebouncedValue(search)

  return {
    search,
    category,
    page,
    debouncedSearch,
    filtersActive: Boolean(search || category),
    setPage,
    changeSearch: (value) => {
      setSearch(value)
      setPage(1)
    },
    changeCategory: (value) => {
      setCategory(value)
      setPage(1)
    },
    clearFilters: () => {
      setSearch('')
      setCategory('')
      setPage(1)
    },
  }
}
