import { Spinner } from '@fluentui/react-components'
import { Navigate, Outlet } from 'react-router-dom'
import { getHomePath } from './roles'
import { useAuth } from './useAuth'

// Rutas solo para quien no ha iniciado sesión (el registro). Con sesión, lleva a la página de inicio de su rol
// (fallos de la demo del Sprint 1). Es la contraria de ProtectedRoute.
export default function GuestOnlyRoute() {
  const { user, loading } = useAuth()

  if (loading) {
    return <Spinner label="Comprobando sesión..." />
  }

  if (user) {
    return <Navigate to={getHomePath(user.rol)} replace />
  }

  return <Outlet />
}
