import { useEffect, useState } from 'react'
import { getSellerProducts } from '../../api/products'

export function useSellerProducts({ search, category, page, size }) {
  const [attempt, setAttempt] = useState(0)
  const [result, setResult] = useState(null)
  const key = `${search}|${category}|${page}|${size}|${attempt}`

  useEffect(() => {
    let cancelled = false
    const filtered = Boolean(search || category)

    getSellerProducts({ search, category, page, size })
      .then((data) => {
        if (!cancelled) setResult({ key, data, failed: false, filtered })
      })
      .catch(() => {
        if (!cancelled) setResult({ key, data: null, failed: true, filtered })
      })

    return () => {
      cancelled = true
    }
  }, [key, search, category, page, size])

  const isCurrent = result?.key === key

  return {
    data: result?.data ?? null,
    failed: isCurrent && result.failed,
    refreshing: Boolean(result) && !isCurrent,
    retry: () => setAttempt((value) => value + 1),
  }
}
