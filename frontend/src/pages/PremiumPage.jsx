import PropTypes from 'prop-types'
import { useId, useState } from 'react'
import {
  Button,
  Dialog,
  DialogActions,
  DialogBody,
  DialogContent,
  DialogSurface,
  DialogTitle,
  MessageBar,
  MessageBarActions,
  MessageBarBody,
  Spinner,
  Text,
  makeStyles,
  mergeClasses,
  tokens,
} from '@fluentui/react-components'
import { Diamond20Regular, Diamond48Regular, CheckmarkStarburst20Filled } from '@fluentui/react-icons'
import { useLocation } from 'react-router-dom'
import { cancelPremium, getProfile } from '../api/profile'
import LinkButton from '../components/LinkButton'
import { usePageStyles } from '../components/pageStyles'
import { PREMIUM_BENEFITS } from '../features/premium/premiumBenefits'
import { TEXTO_AVISO_PREMIUM } from '../features/premium/premiumNotices'
import { useApiQuery } from '../hooks/useApiQuery'
import { PATHS } from '../routes/paths'
import { notifyPremiumStatusChanged } from '../features/premium/premiumEvents'

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
    display: 'grid',
    gridTemplateColumns: 'minmax(0, 1fr)',
    alignItems: 'flex-start',
    gap: tokens.spacingVerticalM,
    padding: `${tokens.spacingVerticalXXL} ${tokens.spacingHorizontalXL}`,
    '@media (min-width: 900px)': {
      gridTemplateColumns: 'minmax(0, 1fr) minmax(280px, 0.85fr)',
      columnGap: tokens.spacingHorizontalXXL,
    },
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
  standardPromo: {
    border: '1px solid #F4B183',
    backgroundImage: 'linear-gradient(115deg, #FFF0E3 0%, #FFF9F4 55%, #FCE7F3 100%)',
    boxShadow: '0 4px 14px rgba(194, 65, 12, 0.12)',
  },
  premiumMember: {
    border: '1px solid #EA8A3A',
    backgroundImage: 'linear-gradient(120deg, #FFE2C2 0%, #FFF1DF 48%, #FFD7B5 100%)',
    boxShadow: '0 6px 20px rgba(194, 65, 12, 0.22)',
  },
  premiumHeadline: {
    color: '#C2410C',
    backgroundImage: 'linear-gradient(90deg, #C2410C 0%, #EA580C 55%, #9A3412 100%)',
    backgroundSize: '100% auto',
    animationName: 'none',
    WebkitTextFillColor: 'transparent',
  },
  brandRow: {
    display: 'flex',
    alignItems: 'center',
    gap: tokens.spacingHorizontalM,
  },
  brandIcon: {
    flexShrink: 0,
    width: '38px',
    height: '38px',
    animation: 'none',
  },
  premiumMemberStatus: {
    display: 'inline-flex',
    alignItems: 'center',
    gap: tokens.spacingHorizontalS,
    padding: `${tokens.spacingVerticalS} ${tokens.spacingHorizontalL}`,
    borderRadius: tokens.borderRadiusCircular,
    color: '#8A2C08',
    backgroundImage: 'linear-gradient(100deg, #FFC078 0%, #FF9A45 100%)',
    border: '1px solid #E97824',
    fontWeight: tokens.fontWeightBold,
    letterSpacing: '0.02em',
    boxShadow: tokens.shadow4,
  },
  premiumStatusCopy: {
    color: tokens.colorNeutralForeground1,
    fontWeight: tokens.fontWeightSemibold,
  },
  benefitsIntro: {
    margin: 0,
    color: '#9A3412',
    fontWeight: tokens.fontWeightSemibold,
    fontSize: tokens.fontSizeBase400,
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
  cancellationSection: {
    display: 'flex',
    justifyContent: 'flex-end',
    alignItems: 'flex-start',
    width: '100%',
    boxSizing: 'border-box',
  },
  cancellationAction: {
    alignSelf: 'flex-start',
  },
  confirmationButton: {
    minWidth: '210px',
    whiteSpace: 'nowrap',
    flexShrink: 0,
  },
  lossList: {
    margin: 0,
    paddingLeft: tokens.spacingHorizontalXL,
    display: 'flex',
    flexDirection: 'column',
    gap: tokens.spacingVerticalXS,
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
  embeddedPage: {
    display: 'block',
    width: '100%',
  },
  heroMain: {
    display: 'flex',
    flexDirection: 'column',
    alignItems: 'flex-start',
    gap: tokens.spacingVerticalM,
    minWidth: 0,
  },
  embeddedBenefits: {
    gridColumn: '1 / -1',
    marginTop: tokens.spacingVerticalM,
  },
  embeddedBenefitsList: {
    gridTemplateColumns: 'minmax(0, 1fr)',
    gap: tokens.spacingHorizontalM,
    '@media (min-width: 720px)': {
      gridTemplateColumns: 'repeat(3, minmax(0, 1fr))',
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
  premiumBenefit: {
    backgroundImage: 'linear-gradient(145deg, #FFF7ED 0%, #FFEDD5 100%)',
    border: '1px solid #FDBA74',
    boxShadow: '0 4px 12px rgba(194, 65, 12, 0.12)',
    ':hover': {
      transform: 'translateY(-6px)',
      boxShadow: '0 8px 20px rgba(194, 65, 12, 0.2)',
      borderColor: '#F97316',
    },
  },
  premiumBenefitIcon: {
    backgroundImage: 'linear-gradient(135deg, #F97316 0%, #C2410C 100%)',
    boxShadow: '0 3px 10px rgba(194, 65, 12, 0.24)',
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
        <div className={styles.premiumMemberStatus}>
          <CheckmarkStarburst20Filled aria-hidden="true" />
          MIEMBRO ESIBUY PREMIUM
        </div>
        <Text className={styles.premiumStatusCopy}>Tu cuenta tiene acceso a todas las ventajas Premium.</Text>
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
export default function PremiumPage({ embedded = false }) {
  const pageStyles = usePageStyles()
  const styles = useStyles()
  const benefitsTitleId = useId()
  const location = useLocation()
  const profile = useApiQuery(getProfile)
  const [faseCancelacion, setFaseCancelacion] = useState(null)
  const [cancelandoPremium, setCancelandoPremium] = useState(false)
  const [errorCancelacion, setErrorCancelacion] = useState(false)
  const aviso = TEXTO_AVISO_PREMIUM[location.state?.aviso] ?? null
  const esPremium = profile.data?.tipoCliente === 'PREMIUM'
  const esEstandar = profile.data?.tipoCliente === 'ESTANDAR'

  const confirmarCancelacionPremium = async () => {
    setCancelandoPremium(true)
    setErrorCancelacion(false)
    try {
      await cancelPremium()
      notifyPremiumStatusChanged()
      setFaseCancelacion(null)
      profile.retry()
    } catch {
      setErrorCancelacion(true)
    } finally {
      setCancelandoPremium(false)
    }
  }

  const Root = embedded ? 'div' : 'main'

  return (
    <Root className={embedded ? styles.embeddedPage : pageStyles.page}>
      <section className={mergeClasses(styles.hero, embedded && esEstandar && styles.standardPromo, embedded && esPremium && styles.premiumMember)}>
        <div className={styles.heroMain}>
          <div className={styles.brandRow}>
            <span className={mergeClasses(styles.heroIcon, embedded && esPremium && styles.brandIcon)} aria-hidden="true">
              <Diamond48Regular />
            </span>
            {embedded ? (
              <h2 className={mergeClasses(styles.headline, esPremium && styles.premiumHeadline)}>{esEstandar ? 'Da el salto a ESIBuy Premium' : 'ESIBuy Premium'}</h2>
            ) : (
              <h1 className={styles.headline}>ESIBuy Premium</h1>
            )}
          </div>
          <Text size={400} className={styles.lead}>
            {embedded && esEstandar
              ? 'Disfruta de descuentos exclusivos, envíos sin coste y prioridad para recibir tus pedidos antes.'
              : 'Compra con más ventajas: descuentos exclusivos, envíos sin coste y pedidos que llegan antes.'}
          </Text>
          <div aria-live="polite">
            {errorCancelacion && (
              <MessageBar intent="error">
                <MessageBarBody>No se ha podido cancelar Premium. Inténtalo de nuevo.</MessageBarBody>
              </MessageBar>
            )}
            {aviso && (
              <MessageBar intent={aviso.intent}>
                <MessageBarBody>{aviso.texto}</MessageBarBody>
              </MessageBar>
            )}
          </div>
          <PremiumStatus query={profile} esPremium={esPremium} styles={styles} />
        </div>

        {esPremium && (
          <section aria-label="Cancelar suscripción" className={styles.cancellationSection}>
            <Button
              appearance="secondary"
              className={styles.cancellationAction}
              onClick={() => setFaseCancelacion('inicial')}
            >
              Cancelar mi suscripción
            </Button>
          </section>
        )}

        <section aria-label="Ventajas de ESIBuy Premium" className={mergeClasses(styles.benefitsSection, embedded && styles.embeddedBenefits)}>
          {!embedded && <h2 id={benefitsTitleId} className={styles.sectionTitle}>Ventajas de ESIBuy Premium</h2>}
          {embedded && esEstandar && (
            <p className={styles.benefitsIntro}>Al hacerte Premium, obtendrás las siguientes ventajas:</p>
          )}
          <ul aria-labelledby={embedded ? undefined : benefitsTitleId} aria-label={embedded ? "Ventajas de ESIBuy Premium" : undefined} className={mergeClasses(styles.benefits, embedded && styles.embeddedBenefitsList)}>
            {PREMIUM_BENEFITS.map(({ id, titulo, texto, Icono }, index) => (
              <li key={id} className={styles.benefitItem} style={{ animationDelay: `${(index + 1) * RETRASO_ENTRADA_MS}ms` }}>
                <article className={mergeClasses(styles.benefit, embedded && esPremium && styles.premiumBenefit)}>
                  <span className={mergeClasses(styles.benefitIcon, embedded && esPremium && styles.premiumBenefitIcon)} aria-hidden="true"><Icono /></span>
                  <h3 className={styles.benefitTitle}>{titulo}</h3>
                  <p className={styles.benefitText}>{texto}</p>
                </article>
              </li>
            ))}
          </ul>
        </section>
      </section>

      <Dialog
        open={faseCancelacion !== null}
        modalType="alert"
        onOpenChange={(_event, data) => !data.open && !cancelandoPremium && setFaseCancelacion(null)}
      >
        <DialogSurface>
          <DialogBody>
            {faseCancelacion === 'inicial' ? (
              <>
                <DialogTitle>¿Estás seguro?</DialogTitle>
                <DialogContent>
                  ¿Quieres continuar con la cancelación de tu suscripción ESIBuy Premium?
                </DialogContent>
                <DialogActions>
                  <Button
                    appearance="secondary"
                    className={styles.confirmationButton}
                    onClick={() => setFaseCancelacion(null)}
                  >
                    No, mantener Premium
                  </Button>
                  <Button
                    appearance="primary"
                    className={styles.confirmationButton}
                    onClick={() => {
                      setErrorCancelacion(false)
                      setFaseCancelacion('final')
                    }}
                  >
                    Continuar
                  </Button>
                </DialogActions>
              </>
            ) : (
              <>
                <DialogTitle>Ventajas que perderás</DialogTitle>
                <DialogContent>
                  <Text>Al cancelar Premium, tu cuenta pasará a ser Estándar y dejarás de disfrutar de:</Text>
                  <ul className={styles.lossList}>
                    <li>Descuentos exclusivos para clientes Premium.</li>
                    <li>Envíos sin coste.</li>
                    <li>Prioridad en los envíos.</li>
                  </ul>
                  <Text className={styles.cancellationText}>
                    ¿Confirmas que quieres cancelar tu suscripción?
                  </Text>
                </DialogContent>
                <DialogActions>
                  <Button
                    appearance="secondary"
                    className={styles.confirmationButton}
                    disabled={cancelandoPremium}
                    onClick={() => setFaseCancelacion(null)}
                  >
                    Mantener Premium
                  </Button>
                  <Button
                    appearance="primary"
                    className={styles.confirmationButton}
                    disabled={cancelandoPremium}
                    onClick={confirmarCancelacionPremium}
                  >
                    {cancelandoPremium ? 'Cancelando…' : 'Sí, cancelar Premium'}
                  </Button>
                </DialogActions>
              </>
            )}
          </DialogBody>
        </DialogSurface>
      </Dialog>

    </Root>
  )
}

PremiumPage.propTypes = {
  embedded: PropTypes.bool,
}
