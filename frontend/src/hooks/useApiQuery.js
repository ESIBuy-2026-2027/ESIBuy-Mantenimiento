import { useEffect, useState } from 'react'

// `fetcher` debe ser estable (useCallback con los parámetros como dependencias): cada
// cambio de identidad lanza una petición nueva y descarta la respuesta de la anterior.
export function useApiQuery(fetcher) {
  const [attempt, setAttempt] = useState(0)
  const [result, setResult] = useState(null)

  useEffect(() => {
    let cancelled = false

    fetcher()
      .then((data) => {
        if (!cancelled) setResult({ fetcher, attempt, data, error: null })
      })
      .catch((error) => {
        if (!cancelled) setResult({ fetcher, attempt, data: null, error })
      })

    return () => {
      cancelled = true
    }
  }, [fetcher, attempt])

  const isCurrent = result?.fetcher === fetcher && result.attempt === attempt

  return {
    data: result?.data ?? null,
    error: isCurrent ? result.error : null,
    failed: isCurrent && result.error !== null,
    refreshing: Boolean(result) && !isCurrent,
    retry: () => setAttempt((value) => value + 1),
  }
}
