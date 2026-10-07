import { Card, Spinner, Text, Title2, makeStyles, tokens } from '@fluentui/react-components'
import { Navigate } from 'react-router-dom'
import LinkButton from '../components/LinkButton'
import { getHomePath } from '../auth/roles'
import { useAuth } from '../auth/useAuth'
import LoginForm from '../features/auth/LoginForm'
import { PATHS } from '../routes/paths'

const useStyles = makeStyles({
  page: {
    minHeight: '100vh',
    boxSizing: 'border-box',
    display: 'flex',
    alignItems: 'center',
    justifyContent: 'center',
    padding: tokens.spacingHorizontalL,
    backgroundImage: `linear-gradient(180deg, ${tokens.colorBrandBackground2} 0%, ${tokens.colorNeutralBackground1} 60%)`,
  },
  card: {
    width: '100%',
    maxWidth: '420px',
    padding: tokens.spacingVerticalXXL,
    gap: tokens.spacingVerticalL,
    borderRadius: tokens.borderRadiusXLarge,
    boxShadow: tokens.shadow28,
  },
  title: {
    margin: 0,
  },
  footer: {
    display: 'flex',
    flexWrap: 'wrap',
    alignItems: 'center',
    justifyContent: 'center',
    color: tokens.colorNeutralForeground3,
  },
})

export default function LoginPage() {
  const styles = useStyles()
  const { user, loading } = useAuth()

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
        <LoginForm />
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
