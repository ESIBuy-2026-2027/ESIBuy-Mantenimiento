import {
  Card,
  MessageBar,
  MessageBarBody,
  Spinner,
  Text,
  Title2,
} from '@fluentui/react-components'
import {
  Cart24Regular,
  ShieldCheckmark24Regular,
  Tag24Regular,
  VehicleTruckProfile24Regular,
} from '@fluentui/react-icons'
import { Link, Navigate, useLocation } from 'react-router-dom'
import LinkButton from '../components/LinkButton'
import { SESSION_END } from '../auth/AuthContext'
import { getHomePath } from '../auth/roles'
import { useAuth } from '../auth/useAuth'
import LoginForm from '../features/auth/LoginForm'
import { useLoginPageStyles } from '../features/auth/authFormStyles'
import { PATHS } from '../routes/paths'

// Aviso de cómo terminó la sesión anterior (HU43).
const SESSION_END_NOTICES = {
  [SESSION_END.SIGNED_OUT]: { intent: 'success', text: 'Has cerrado sesión.' },
  [SESSION_END.EXPIRED]: { intent: 'warning', text: 'Tu sesión ha caducado. Vuelve a iniciar sesión.' },
  [SESSION_END.ACCOUNT_DELETED]: { intent: 'success', text: 'Tu cuenta se ha eliminado.' },
}

// Aviso al volver de la recuperación de contraseña (HU3).
const PASSWORD_CHANGED_NOTICE = { intent: 'success', text: 'Contraseña cambiada. Ya puedes iniciar sesión.' }

export default function LoginPage() {
  const styles = useLoginPageStyles()
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
      <section className={styles.brandPanel} aria-label="Presentación de ESIBuy">
        <div className={styles.brandTop}>
          <Link to={PATHS.HOME} className={styles.logo} aria-label="ESIBuy, volver al inicio">
            <span className={styles.logoMark} aria-hidden="true">
              <Cart24Regular />
            </span>
            <span>ESI<span className={styles.logoAccent}>Buy</span></span>
          </Link>
        </div>

        <div className={styles.brandCopy}>
          <div className={styles.eyebrow}>
            <span className={styles.eyebrowLine} />
            {' '}
            <span>Tu marketplace de confianza</span>
          </div>
          <h1 className={styles.headline}>
            Compra, vende y descubre con <span className={styles.headlineAccent}>garantía total.</span>
          </h1>
          <p className={styles.description}>
            El marketplace donde encontrarás productos de calidad, podrás gestionar tus pedidos
            y venderás de forma segura y sencilla.
          </p>
        </div>

        <div className={styles.visual}>
          <div className={styles.visualStage}>
            <img className={styles.visualIllustration} src="/images/esibuy-login-illustration.webp" alt="Caja de ESIBuy con un portátil, auriculares y productos tecnológicos" />
          </div>
          <div className={styles.benefits}>
            <div className={styles.benefit}>
              <ShieldCheckmark24Regular className={styles.benefitIcon} />
              <span>Compras seguras</span>
            </div>
            <div className={`${styles.benefit} ${styles.benefitSecond}`}>
              <VehicleTruckProfile24Regular className={styles.benefitIcon} />
              <span>Seguimiento de pedidos</span>
            </div>
            <div className={`${styles.benefit} ${styles.benefitThird}`}>
              <Tag24Regular className={styles.benefitIcon} />
              <span>Gran variedad de productos</span>
            </div>
          </div>
        </div>

        <Text className={styles.copyright}>ESIBuy · Marketplace universitario UCLM</Text>
      </section>

      <section className={styles.formPanel} aria-label="Acceso a tu cuenta">
        <Card className={styles.card}>
          <div className={styles.formHeading}>
            <Title2 as="h2" className={styles.title}>Iniciar sesión</Title2>
            <p className={styles.subtitle}>Accede a tu cuenta para continuar.</p>
          </div>

          {notice && (
            <MessageBar intent={notice.intent}>
              <MessageBarBody>{notice.text}</MessageBarBody>
            </MessageBar>
          )}

          <LoginForm />

          <LinkButton to={PATHS.FORGOT_PASSWORD} appearance="transparent" className={styles.recovery}>
            ¿Has olvidado tu contraseña?
          </LinkButton>

          <div className={styles.footer}>
            <Text>¿Todavía no tienes una cuenta?</Text>
            <LinkButton to={PATHS.REGISTER} appearance="transparent" className={styles.registerLink}>
              Regístrate
            </LinkButton>
          </div>

          <div className={styles.security}>
            <ShieldCheckmark24Regular />
            <span>Tus datos están protegidos con cifrado seguro</span>
          </div>
        </Card>
      </section>
    </main>
  )
}
