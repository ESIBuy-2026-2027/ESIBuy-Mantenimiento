import { useEffect, useState } from 'react'
import { listUsers } from '../../api/users'

export function useUsersList({ search, role, status, page, size }) {
  const [attempt, setAttempt] = useState(0)
  const [result, setResult] = useState(null)
  const key = `${search}|${role}|${status}|${page}|${size}|${attempt}`

  useEffect(() => {
    let cancelled = false

    listUsers({ search, role, status, page, size })
      .then((data) => {
        if (!cancelled) setResult({ key, data, failed: false })
      })
      .catch(() => {
        if (!cancelled) setResult({ key, data: null, failed: true })
      })

    return () => {
      cancelled = true
    }
  }, [key, search, role, status, page, size])

  return {
    data: result?.data ?? null,
    failed: result?.key === key && result.failed,
    retry: () => setAttempt((value) => value + 1),
  }
}
