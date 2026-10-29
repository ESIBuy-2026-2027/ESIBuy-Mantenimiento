import PropTypes from 'prop-types'
import { useCallback, useEffect, useMemo, useState } from 'react'
import { getCurrentUser, logout } from '../api/auth'
import { AuthContext, SESSION_END } from './AuthContext'

export default function AuthProvider({ children }) {
  const [user, setUser] = useState(null)
  const [loading, setLoading] = useState(true)
  const [sessionEnd, setSessionEnd] = useState(null)

  useEffect(() => {
    let cancelled = false

    getCurrentUser()
      .then((currentUser) => {
        if (!cancelled) setUser(currentUser)
      })
      .catch(() => {
        if (!cancelled) setUser(null)
      })
      .finally(() => {
        if (!cancelled) setLoading(false)
      })

    return () => {
      cancelled = true
    }
  }, [])

  const signIn = useCallback((authenticatedUser) => {
    setUser(authenticatedUser)
    setSessionEnd(null)
  }, [])

  // La cookie de sesión es HttpOnly: la borra el servidor al cerrar sesión (HU43). Si no responde, la sesión del
  // navegador se cierra igualmente.
  const signOut = useCallback(async () => {
    await logout().catch(() => null)
    setUser(null)
    setSessionEnd(SESSION_END.SIGNED_OUT)
  }, [])

  const value = useMemo(
    () => ({ user, loading, sessionEnd, signIn, signOut }),
    [user, loading, sessionEnd, signIn, signOut],
  )

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>
}

AuthProvider.propTypes = {
  children: PropTypes.node.isRequired,
}
