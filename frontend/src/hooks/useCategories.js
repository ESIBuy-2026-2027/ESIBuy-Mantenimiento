import { useEffect, useState } from 'react'
import { getCategories } from '../api/products'

export function useCategories() {
  const [categories, setCategories] = useState([])

  useEffect(() => {
    let cancelled = false

    getCategories()
      .then((data) => {
        if (!cancelled) setCategories(data)
      })
      .catch(() => {
        // Sin categorías el filtro queda solo con "Todas las categorías"; el listado sigue funcionando.
      })

    return () => {
      cancelled = true
    }
  }, [])

  return categories
}
