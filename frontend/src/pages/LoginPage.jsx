import {
  Card,
  MessageBar,
  MessageBarBody,
  Spinner,
  Text,
  Title2,
} from '@fluentui/react-components'
import { Navigate, useLocation } from 'react-router-dom'
import LinkButton from '../components/LinkButton'
import { SESSION_END } from '../auth/AuthContext'
import { getHomePath } from '../auth/roles'
import { useAuth } from '../auth/useAuth'
import LoginForm from '../features/auth/LoginForm'
import { useAuthPageStyles } from '../features/auth/authFormStyles'
import { PATHS } from '../routes/paths'

// Aviso de cómo terminó la sesión anterior (HU43).
const SESSION_END_NOTICES = {
  [SESSION_END.SIGNED_OUT]: { intent: 'success', text: 'Has cerrado sesión.' },
  [SESSION_END.EXPIRED]: { intent: 'warning', text: 'Tu sesión ha caducado. Vuelve a iniciar sesión.' },
}

// Aviso al volver de la recuperación de contraseña (HU3).
const PASSWORD_CHANGED_NOTICE = { intent: 'success', text: 'Contraseña cambiada. Ya puedes iniciar sesión.' }

export default function LoginPage() {
  const styles = useAuthPageStyles()
  const location = useLocation()
  const { user, loading, sessionEnd } = useAuth()
  const notice = location.state?.passwordChanged ? PASSWORD_CHANGED_NOTICE : SESSION_END_NOTICES[sessionEnd]

  if (loading) {
    return <Spinner label="Comprobando sesión..." />
  }

  if (user) {
    return <Navigate to={getHomePath(user.rol)} replace />
  }

  return (
    <main className={styles.page}>
      <Card className={styles.card}>
        <Title2 as="h1" className={styles.title}>
          Iniciar sesión
        </Title2>
        {notice && (
          <MessageBar intent={notice.intent}>
            <MessageBarBody>{notice.text}</MessageBarBody>
          </MessageBar>
        )}
        <LoginForm />
        <LinkButton to={PATHS.FORGOT_PASSWORD} appearance="transparent">
          ¿Has olvidado tu contraseña?
        </LinkButton>
        <div className={styles.footer}>
          <Text>¿Aún no tienes cuenta?</Text>
          <LinkButton to={PATHS.REGISTER} appearance="transparent">
            Regístrate
          </LinkButton>
        </div>
      </Card>
    </main>
  )
}
