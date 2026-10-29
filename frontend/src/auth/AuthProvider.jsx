import PropTypes from 'prop-types'
import { useCallback, useEffect, useMemo, useRef, useState } from 'react'
import { getCurrentUser, logout } from '../api/auth'
import { onUnauthorized } from '../api/client'
import { AuthContext, SESSION_END } from './AuthContext'

export default function AuthProvider({ children }) {
  const [user, setUser] = useState(null)
  const [loading, setLoading] = useState(true)
  const [sessionEnd, setSessionEnd] = useState(null)
  const userRef = useRef(null)

  useEffect(() => {
    userRef.current = user
  }, [user])

  // Un 401 con la sesión abierta es que el servidor la ha dado por caducada (HU43): se cierra también aquí y las rutas
  // protegidas llevan al login, que lo avisa. Sin sesión (p. ej. una contraseña incorrecta) un 401 no significa eso.
  useEffect(
    () =>
      onUnauthorized(() => {
        if (!userRef.current) return
        setUser(null)
        setSessionEnd(SESSION_END.EXPIRED)
      }),
    [],
  )

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
