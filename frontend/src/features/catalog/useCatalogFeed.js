import { useCallback, useEffect, useState } from 'react'
import { getCatalogProducts } from '../../api/products'

const SIN_CARGAR = { key: null, items: [], page: 0, totalItems: 0, totalPages: 0, error: null }

// Catálogo con scroll infinito: pide la primera página y, cada vez que se llama a `loadMore`, la siguiente, y las
// va acumulando. Cualquier cambio de filtro u orden (`key`) empieza otra vez desde la primera página.
export function useCatalogFeed({ search, category, minRating, sort, order, size }) {
  const key = JSON.stringify([search, category, minRating, sort, order, size])
  const [feed, setFeed] = useState(SIN_CARGAR)
  // La página que se quiere, ligada a la búsqueda para la que se pidió: con otra búsqueda vuelve a la primera.
  const [wanted, setWanted] = useState({ key, page: 1 })
  const [attempt, setAttempt] = useState(0)
  const page = wanted.key === key ? wanted.page : 1

  useEffect(() => {
    let cancelled = false

    getCatalogProducts({ search, category, minRating, sort, order, page, size })
      .then((data) => {
        if (cancelled) return
        setFeed((prev) => {
          const append = page > 1 && prev.key === key && prev.page === page - 1
          return {
            key,
            items: append ? [...prev.items, ...data.items] : data.items,
            page: data.page,
            totalItems: data.totalItems,
            totalPages: data.totalPages,
            error: null,
          }
        })
      })
      .catch(() => {
        if (!cancelled) setFeed((prev) => ({ ...prev, error: { key, page } }))
      })

    return () => {
      cancelled = true
    }
  }, [key, page, attempt, search, category, minRating, sort, order, size])

  const current = feed.key === key
  const failed = feed.error?.key === key && feed.error.page === page
  const hasMore = current && feed.page < feed.totalPages

  const loadMore = useCallback(() => {
    if (current && feed.page < feed.totalPages && page === feed.page) {
      setWanted({ key, page: feed.page + 1 })
    }
  }, [current, feed.page, feed.totalPages, page, key])

  return {
    // Mientras llegan los resultados de otra búsqueda se siguen viendo los anteriores (atenuados), salvo que falle.
    items: current || !failed ? feed.items : [],
    loaded: feed.key !== null,
    // Falla la primera página: no hay nada que enseñar. Falla otra: se conserva lo ya cargado.
    failed: failed && page === 1,
    failedMore: failed && page > 1,
    refreshing: !current && !failed,
    loadingMore: current && page > feed.page && !failed,
    hasMore,
    totalItems: current ? feed.totalItems : null,
    loadMore,
    retry: () => setAttempt((value) => value + 1),
  }
}
