import PropTypes from 'prop-types'
import { Spinner } from '@fluentui/react-components'
import { Navigate, Outlet } from 'react-router-dom'
import { PATHS } from '../routes/paths'
import { getHomePath } from './roles'
import { useAuth } from './useAuth'

export default function ProtectedRoute({ allowedRoles }) {
  const { user, loading } = useAuth()

  if (loading) {
    return <Spinner label="Comprobando sesión..." />
  }

  if (!user) {
    return <Navigate to={PATHS.LOGIN} replace />
  }

  if (allowedRoles && !allowedRoles.includes(user.rol)) {
    return <Navigate to={getHomePath(user.rol)} replace />
  }

  return <Outlet />
}

ProtectedRoute.propTypes = {
  allowedRoles: PropTypes.arrayOf(PropTypes.string),
}
