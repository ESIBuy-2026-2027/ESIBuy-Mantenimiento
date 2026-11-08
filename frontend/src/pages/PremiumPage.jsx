import PropTypes from 'prop-types'
import { useId } from 'react'
import {
  Button,
  MessageBar,
  MessageBarActions,
  MessageBarBody,
  Spinner,
  Text,
  makeStyles,
  tokens,
} from '@fluentui/react-components'
import { Diamond20Regular, Diamond48Regular, Sparkle24Regular } from '@fluentui/react-icons'
import { useLocation } from 'react-router-dom'
import { getProfile } from '../api/profile'
import LinkButton from '../components/LinkButton'
import { usePageStyles } from '../components/pageStyles'
import { PREMIUM_BENEFITS } from '../features/premium/premiumBenefits'
import { TEXTO_AVISO_PREMIUM } from '../features/premium/premiumNotices'
import { useApiQuery } from '../hooks/useApiQuery'
import { PATHS } from '../routes/paths'

// Retraso entre la entrada de una tarjeta y la siguiente
const RETRASO_ENTRADA_MS = 120

const SIN_MOVIMIENTO = {
  animationName: 'none',
  transitionDuration: '0.01ms',
}

const entrada = {
  from: { opacity: 0, transform: 'translateY(24px) scale(0.97)' },
  to: { opacity: 1, transform: 'none' },
}

const useStyles = makeStyles({
  hero: {
    position: 'relative',
    overflow: 'hidden',
    display: 'flex',
    flexDirection: 'column',
    alignItems: 'flex-start',
    gap: tokens.spacingVerticalM,
    padding: `${tokens.spacingVerticalXXL} ${tokens.spacingHorizontalXL}`,
    borderRadius: tokens.borderRadiusXLarge,
    border: `1px solid ${tokens.colorBrandStroke2}`,
    backgroundColor: tokens.colorNeutralBackground1,
    backgroundImage: `linear-gradient(120deg, ${tokens.colorBrandBackground2} 0%, ${tokens.colorNeutralBackground1} 45%, ${tokens.colorPaletteBerryBackground1} 100%)`,
    backgroundSize: '200% 200%',
    boxShadow: tokens.shadow8,
    animationName: [entrada, { '0%': { backgroundPosition: '0% 50%' }, '100%': { backgroundPosition: '100% 50%' } }],
    animationDuration: `${tokens.durationSlower}, 12s`,
    animationTimingFunction: `${tokens.curveDecelerateMid}, ease-in-out`,
    animationIterationCount: '1, infinite',
    animationDirection: 'normal, alternate',
    animationFillMode: 'both',
    '@media (prefers-reduced-motion: reduce)': SIN_MOVIMIENTO,
  },
  heroIcon: {
    display: 'flex',
    color: tokens.colorBrandForeground1,
    animationName: {
      '0%, 100%': { transform: 'translateY(0) rotate(0deg)' },
      '50%': { transform: 'translateY(-6px) rotate(-6deg)' },
    },
    animationDuration: '4s',
    animationTimingFunction: 'ease-in-out',
    animationIterationCount: 'infinite',
    '@media (prefers-reduced-motion: reduce)': SIN_MOVIMIENTO,
  },
  // Titular con un brillo que recorre el degradado. Los colores son los de primer plano de la marca, que cumplen el
  // contraste sobre el fondo claro; con colores forzados se usa el color del sistema.
  headline: {
    margin: 0,
    fontSize: tokens.fontSizeHero800,
    lineHeight: tokens.lineHeightHero800,
    fontWeight: tokens.fontWeightBold,
    color: tokens.colorBrandForeground1,
    backgroundImage: `linear-gradient(90deg, ${tokens.colorBrandForeground1} 0%, ${tokens.colorPaletteBerryForeground1} 25%, ${tokens.colorBrandForeground2} 50%, ${tokens.colorPaletteBerryForeground1} 75%, ${tokens.colorBrandForeground1} 100%)`,
    backgroundSize: '200% auto',
    backgroundClip: 'text',
    WebkitBackgroundClip: 'text',
    WebkitTextFillColor: 'transparent',
    animationName: { from: { backgroundPosition: '0% center' }, to: { backgroundPosition: '200% center' } },
    animationDuration: '6s',
    animationTimingFunction: 'linear',
    animationIterationCount: 'infinite',
    '@media (max-width: 480px)': {
      fontSize: tokens.fontSizeHero700,
      lineHeight: tokens.lineHeightHero700,
    },
    '@media (prefers-reduced-motion: reduce)': SIN_MOVIMIENTO,
    '@media (forced-colors: active)': {
      backgroundImage: 'none',
      WebkitTextFillColor: 'CanvasText',
      color: 'CanvasText',
    },
  },
  lead: {
    maxWidth: '60ch',
    color: tokens.colorNeutralForeground2,
  },
  status: {
    display: 'flex',
    flexDirection: 'column',
    alignItems: 'flex-start',
    gap: tokens.spacingVerticalS,
  },
  statusTitle: {
    display: 'flex',
    alignItems: 'center',
    gap: tokens.spacingHorizontalS,
    margin: 0,
    fontSize: tokens.fontSizeBase500,
    fontWeight: tokens.fontWeightSemibold,
    color: tokens.colorBrandForeground1,
  },
  cta: {
    transitionProperty: 'transform, box-shadow',
    transitionDuration: tokens.durationNormal,
    transitionTimingFunction: tokens.curveEasyEase,
    ':hover': {
      transform: 'translateY(-2px)',
      boxShadow: tokens.shadow16,
    },
    '@media (prefers-reduced-motion: reduce)': {
      ...SIN_MOVIMIENTO,
      ':hover': { transform: 'none' },
    },
  },
  benefitsSection: {
    display: 'flex',
    flexDirection: 'column',
    gap: tokens.spacingVerticalL,
  },
  sectionTitle: {
    margin: 0,
    fontSize: tokens.fontSizeBase500,
    fontWeight: tokens.fontWeightSemibold,
  },
  benefits: {
    display: 'grid',
    gridTemplateColumns: 'minmax(0, 1fr)',
    gap: tokens.spacingHorizontalL,
    margin: 0,
    padding: 0,
    listStyleType: 'none',
    '@media (min-width: 720px)': {
      gridTemplateColumns: 'repeat(3, minmax(0, 1fr))',
    },
  },
  // La entrada escalonada va en el elemento de la lista y la elevación al pasar por encima, en la tarjeta: así la
  // animación de entrada no anula la transformación del hover.
  benefitItem: {
    display: 'flex',
    animationName: entrada,
    animationDuration: tokens.durationSlower,
    animationTimingFunction: tokens.curveDecelerateMid,
    animationFillMode: 'both',
    '@media (prefers-reduced-motion: reduce)': SIN_MOVIMIENTO,
  },
  benefit: {
    flexGrow: 1,
    display: 'flex',
    flexDirection: 'column',
    gap: tokens.spacingVerticalS,
    padding: tokens.spacingHorizontalXL,
    backgroundColor: tokens.colorNeutralBackground1,
    border: `1px solid ${tokens.colorNeutralStroke2}`,
    borderRadius: tokens.borderRadiusXLarge,
    boxShadow: tokens.shadow4,
    transitionProperty: 'transform, box-shadow, border-color',
    transitionDuration: tokens.durationNormal,
    transitionTimingFunction: tokens.curveEasyEase,
    ':hover': {
      transform: 'translateY(-6px)',
      boxShadow: tokens.shadow28,
      borderColor: tokens.colorBrandStroke1,
    },
    '@media (prefers-reduced-motion: reduce)': {
      ...SIN_MOVIMIENTO,
      ':hover': { transform: 'none' },
    },
  },
  benefitIcon: {
    display: 'flex',
    alignItems: 'center',
    justifyContent: 'center',
    width: '48px',
    height: '48px',
    borderRadius: tokens.borderRadiusCircular,
    color: tokens.colorNeutralForegroundOnBrand,
    backgroundImage: `linear-gradient(135deg, ${tokens.colorBrandBackground} 0%, ${tokens.colorPaletteBerryForeground1} 100%)`,
    boxShadow: tokens.shadow8,
  },
  benefitTitle: {
    margin: 0,
    fontSize: tokens.fontSizeBase400,
    fontWeight: tokens.fontWeightSemibold,
  },
  benefitText: {
    margin: 0,
    color: tokens.colorNeutralForeground2,
  },
})

function PremiumStatus({ query, esPremium, styles }) {
  if (esPremium) {
    return (
      <div className={styles.status}>
        <h2 className={styles.statusTitle}>
          <Sparkle24Regular aria-hidden="true" />
          Ya eres ESIBuy Premium
        </h2>
        <Text>Disfrutas de todas sus ventajas en cada compra.</Text>
      </div>
    )
  }

  if (query.failed) {
    return (
      <MessageBar intent="error">
        <MessageBarBody>No se ha podido comprobar tu tipo de cliente. Inténtalo de nuevo.</MessageBarBody>
        <MessageBarActions>
          <Button onClick={query.retry}>Reintentar</Button>
        </MessageBarActions>
      </MessageBar>
    )
  }

  if (!query.data) return <Spinner size="small" label="Comprobando tu tipo de cliente…" />

  return (
    <LinkButton
      to={PATHS.PREMIUM_PAYMENT}
      appearance="primary"
      size="large"
      icon={<Diamond20Regular />}
      className={styles.cta}
    >
      Hazte Premium
    </LinkButton>
  )
}

PremiumStatus.propTypes = {
  query: PropTypes.shape({
    data: PropTypes.shape({ tipoCliente: PropTypes.string }),
    failed: PropTypes.bool.isRequired,
    retry: PropTypes.func.isRequired,
  }).isRequired,
  esPremium: PropTypes.bool.isRequired,
  styles: PropTypes.objectOf(PropTypes.string).isRequired,
}

// Vista de ventajas de ESIBuy Premium (HU45), solo para clientes. El cliente Estándar puede hacerse Premium desde aquí
// (por la pasarela de pago simulada); el Premium ve que ya lo es. Sin precio: la suscripción no tiene un importe
// definido. Las animaciones se desactivan si el usuario prefiere reducir el movimiento.
export default function PremiumPage() {
  const pageStyles = usePageStyles()
  const styles = useStyles()
  const benefitsTitleId = useId()
  const location = useLocation()
  const profile = useApiQuery(getProfile)
  const aviso = TEXTO_AVISO_PREMIUM[location.state?.aviso] ?? null
  const esPremium = profile.data?.tipoCliente === 'PREMIUM' || aviso !== null

  return (
    <main className={pageStyles.page}>
      <section className={styles.hero}>
        <span className={styles.heroIcon} aria-hidden="true">
          <Diamond48Regular />
        </span>
        <h1 className={styles.headline}>ESIBuy Premium</h1>
        <Text size={400} className={styles.lead}>
          Compra con más ventajas: descuentos exclusivos, envíos sin coste y pedidos que llegan antes.
        </Text>
        <div aria-live="polite">
          {aviso && (
            <MessageBar intent={aviso.intent}>
              <MessageBarBody>{aviso.texto}</MessageBarBody>
            </MessageBar>
          )}
        </div>
        <PremiumStatus query={profile} esPremium={esPremium} styles={styles} />
      </section>

      <section aria-labelledby={benefitsTitleId} className={styles.benefitsSection}>
        <h2 id={benefitsTitleId} className={styles.sectionTitle}>
          Ventajas de ESIBuy Premium
        </h2>
        <ul aria-labelledby={benefitsTitleId} className={styles.benefits}>
          {PREMIUM_BENEFITS.map(({ id, titulo, texto, Icono }, index) => (
            <li
              key={id}
              className={styles.benefitItem}
              style={{ animationDelay: `${(index + 1) * RETRASO_ENTRADA_MS}ms` }}
            >
              <article className={styles.benefit}>
                <span className={styles.benefitIcon} aria-hidden="true">
                  <Icono />
                </span>
                <h3 className={styles.benefitTitle}>{titulo}</h3>
                <p className={styles.benefitText}>{texto}</p>
              </article>
            </li>
          ))}
        </ul>
      </section>
    </main>
  )
}
