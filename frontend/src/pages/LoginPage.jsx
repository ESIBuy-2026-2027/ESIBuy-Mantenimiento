import {
  Card,
  MessageBar,
  MessageBarBody,
  Spinner,
  Text,
  Title2,
} from '@fluentui/react-components'
import {
  BoxCheckmark24Regular,
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

function MarketplaceIllustration() {
  return (
    <svg viewBox="0 0 620 390" role="img" aria-label="Caja de ESIBuy con productos tecnológicos" className="marketplace-illustration">
      <defs>
        <linearGradient id="boxFront" x1="0" y1="0" x2="1" y2="1">
          <stop offset="0" stopColor="#F8C18F" />
          <stop offset="1" stopColor="#D98545" />
        </linearGradient>
        <linearGradient id="boxSide" x1="0" y1="0" x2="1" y2="1">
          <stop offset="0" stopColor="#EAA66D" />
          <stop offset="1" stopColor="#B96732" />
        </linearGradient>
        <linearGradient id="laptop" x1="0" y1="0" x2="1" y2="1">
          <stop offset="0" stopColor="#667085" />
          <stop offset="1" stopColor="#202A36" />
        </linearGradient>
        <filter id="softShadow" x="-30%" y="-30%" width="160%" height="170%">
          <feGaussianBlur stdDeviation="12" />
        </filter>
      </defs>
      <ellipse cx="290" cy="342" rx="245" ry="22" fill="#9A3412" opacity=".16" filter="url(#softShadow)" />
      <path d="M80 180 L220 120 L485 155 L350 225 Z" fill="#F7D3B0" />
      <path d="M80 180 L350 225 L350 350 L80 295 Z" fill="url(#boxFront)" />
      <path d="M350 225 L485 155 L485 285 L350 350 Z" fill="url(#boxSide)" />
      <path d="M80 180 L155 125 L260 205 L190 226 Z" fill="#E9B17F" stroke="#C27B45" strokeWidth="3" />
      <path d="M350 225 L425 135 L510 155 L485 155 Z" fill="#F6D0A9" stroke="#C27B45" strokeWidth="3" />
      <path d="M195 212 L250 76 Q260 57 282 61 L395 82 Q411 85 414 105 L435 220 Z" fill="#303A46" />
      <path d="M207 204 L259 84 L392 105 L414 215 Z" fill="url(#laptop)" />
      <path d="M185 218 L435 220 L455 238 L165 238 Z" fill="#AAB2BC" />
      <path d="M185 218 L435 220 L455 238 L165 238 Z" fill="#596575" opacity=".55" />
      <path d="M300 124 Q300 76 348 76 Q396 76 396 124" fill="none" stroke="#242B34" strokeWidth="17" strokeLinecap="round" />
      <rect x="289" y="118" width="23" height="62" rx="10" fill="#252D37" />
      <rect x="384" y="118" width="23" height="62" rx="10" fill="#252D37" />
      <path d="M430 160 Q454 135 477 155" fill="none" stroke="#4B7C3A" strokeWidth="8" strokeLinecap="round" />
      <path d="M464 157 Q455 112 480 93 Q503 124 464 157" fill="#78A65A" />
      <path d="M466 162 Q493 117 520 126 Q512 157 466 162" fill="#91B96C" />
      <path d="M456 165 Q428 125 443 103 Q471 125 456 165" fill="#6E9A50" />
      <path d="M350 350 L350 225 L485 155 L485 285 Z" fill="none" stroke="#A85C2B" strokeWidth="3" opacity=".55" />
      <path d="M80 180 L350 225 L350 350 L80 295 Z" fill="none" stroke="#B96D38" strokeWidth="3" opacity=".55" />
      <g transform="translate(405 285)">
        <path d="M0 18 L38 0 L75 17 L37 37 Z" fill="#E8EAF0" />
        <path d="M0 18 L37 37 L37 70 L0 51 Z" fill="#C9CED8" />
        <path d="M37 37 L75 17 L75 50 L37 70 Z" fill="#AAB2C0" />
        <circle cx="19" cy="35" r="6" fill="#454D5A" /><circle cx="55" cy="35" r="6" fill="#454D5A" />
        <path d="M38 40 L38 58" stroke="#454D5A" strokeWidth="5" strokeLinecap="round" />
      </g>
      <g transform="translate(130 260)">
        <path d="M0 0 L190 31 L190 53 L0 22 Z" fill="#F3C08D" opacity=".7" />
      </g>
      <g transform="translate(180 270)" fill="#783B1D" opacity=".9">
        <path d="M0 0 h18 v-10 q0-8 8-8 q8 0 8 8 v10 h8 v27 h-42z M23-10 q0-5 5-5 q5 0 5 5 v10 h-10z" />
        <text x="53" y="22" fontSize="27" fontWeight="700" fontFamily="Arial">ESIBuy</text>
      </g>
    </svg>
  )
}

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
            Tu marketplace universitario
          </div>
          <h1 className={styles.headline}>
            Compra, vende y descubre con <span className={styles.headlineAccent}>garantía total.</span>
          </h1>
          <p className={styles.description}>
            El marketplace de la UCLM donde encontrarás productos de calidad, gestionarás tus pedidos
            y venderás de forma segura y sencilla.
          </p>
        </div>

        <div className={styles.visual}>
          <div className={styles.visualStage}>
            <MarketplaceIllustration />
          </div>
          <div className={styles.benefits}>
            <div className={styles.benefit}>
              <ShieldCheckmark24Regular className={styles.benefitIcon} />
              <span>Compras seguras</span>
            </div>
            <div className={styles.benefit}>
              <VehicleTruckProfile24Regular className={styles.benefitIcon} />
              <span>Seguimiento de pedidos</span>
            </div>
            <div className={styles.benefit}>
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
            <LinkButton to={PATHS.REGISTER} appearance="transparent">
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
