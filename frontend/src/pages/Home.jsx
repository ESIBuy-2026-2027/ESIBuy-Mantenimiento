import PropTypes from 'prop-types'
import { Display, Text, makeStyles, mergeClasses, tokens } from '@fluentui/react-components'
import {
  ArrowRight20Regular,
  ShieldCheckmark24Regular,
  Tag24Regular,
  VehicleTruckProfile24Regular,
} from '@fluentui/react-icons'
import { useAuth } from '../auth/useAuth'
import LinkButton from '../components/LinkButton'
import FeaturedProducts from '../features/catalog/FeaturedProducts'
import { PATHS } from '../routes/paths'

const DESKTOP = '@media (min-width: 900px)'
const MOBILE = '@media (max-width: 599px)'

const useStyles = makeStyles({
  page: {
    width: '100%',
    flexGrow: 1,
    overflow: 'hidden',
    color: tokens.colorNeutralForeground1,
    backgroundImage: 'radial-gradient(ellipse at 10% 15%, rgba(255, 191, 145, 0.20), transparent 34%), radial-gradient(ellipse at 88% 10%, rgba(255, 225, 155, 0.24), transparent 30%), linear-gradient(180deg, #fff8f2 0%, #fffaf7 58%, #fff4ed 100%)',
  },
  container: {
    width: '100%',
    maxWidth: '1240px',
    marginLeft: 'auto',
    marginRight: 'auto',
    paddingLeft: 'clamp(20px, 5vw, 64px)',
    paddingRight: 'clamp(20px, 5vw, 64px)',
    boxSizing: 'border-box',
  },
  hero: {
    display: 'grid',
    gridTemplateColumns: '1fr',
    alignItems: 'center',
    gap: tokens.spacingVerticalXXL,
    paddingTop: 'clamp(36px, 6vh, 76px)',
    paddingBottom: '30px',
    [DESKTOP]: {
      gridTemplateColumns: '1.02fr 0.98fr',
      gap: 'clamp(28px, 4vw, 64px)',
      paddingTop: 'clamp(42px, 6vh, 76px)',
    },
  },
  copy: {
    display: 'flex',
    flexDirection: 'column',
    alignItems: 'flex-start',
    gap: tokens.spacingVerticalL,
    minWidth: 0,
  },
  eyebrow: {
    display: 'inline-flex',
    alignItems: 'center',
    gap: tokens.spacingHorizontalS,
    color: '#d94b08',
    fontSize: tokens.fontSizeBase300,
    fontWeight: tokens.fontWeightSemibold,
  },
  headline: {
    margin: 0,
    maxWidth: '680px',
    fontSize: 'clamp(36px, 4vw, 54px)',
    lineHeight: 1.12,
    letterSpacing: '-1.8px',
    fontWeight: tokens.fontWeightBold,
    textWrap: 'balance',
    [MOBILE]: { fontSize: 'clamp(34px, 9vw, 43px)', letterSpacing: '-1.2px' },
  },
  accent: { color: '#e95a0c' },
  description: {
    margin: 0,
    maxWidth: '560px',
    color: tokens.colorNeutralForeground2,
    fontSize: tokens.fontSizeBase400,
    lineHeight: 1.6,
  },
  actions: {
    display: 'flex',
    flexWrap: 'wrap',
    alignItems: 'center',
    gap: tokens.spacingHorizontalM,
  },
  primaryAction: {
    minHeight: '46px',
    paddingLeft: tokens.spacingHorizontalXL,
    paddingRight: tokens.spacingHorizontalXL,
    boxShadow: '0 8px 18px rgba(226, 79, 12, 0.18)',
    transitionProperty: 'transform, box-shadow',
    transitionDuration: tokens.durationNormal,
    ':hover': { transform: 'translateY(-2px)' },
  },
  secondaryAction: {
    minHeight: '46px',
    backgroundColor: 'rgba(255,255,255,0.78)',
  },
  illustrationStage: {
    display: 'grid',
    placeItems: 'center',
    minWidth: 0,
    minHeight: '300px',
    [DESKTOP]: { minHeight: '390px' },
  },
  illustration: {
    display: 'block',
    width: '100%',
    maxWidth: '610px',
    maxHeight: '440px',
    height: 'auto',
    objectFit: 'contain',
    filter: 'drop-shadow(0 18px 24px rgba(124, 45, 18, 0.12))',
  },
  benefits: {
    display: 'grid',
    gridTemplateColumns: '1fr',
    gap: tokens.spacingHorizontalM,
    paddingTop: tokens.spacingVerticalM,
    paddingBottom: '52px',
    [DESKTOP]: { gridTemplateColumns: 'repeat(3, minmax(0, 1fr))' },
  },
  benefit: {
    display: 'flex',
    alignItems: 'center',
    gap: tokens.spacingHorizontalM,
    minWidth: 0,
    padding: '17px 20px',
    border: '1px solid rgba(255,255,255,0.88)',
    borderRadius: '18px',
    backgroundColor: 'rgba(255,255,255,0.76)',
    boxShadow: '0 7px 22px rgba(91, 54, 25, 0.055)',
    backdropFilter: 'blur(8px)',
  },
  benefitIconWrap: {
    flexShrink: 0,
    display: 'grid',
    placeItems: 'center',
    width: '54px',
    height: '54px',
    borderRadius: tokens.borderRadiusCircular,
    color: '#e95a0c',
    backgroundColor: '#fff1e7',
    '& svg': { width: '30px', height: '30px' },
  },
  benefitText: {
    display: 'flex',
    flexDirection: 'column',
    gap: tokens.spacingVerticalXXS,
    minWidth: 0,
  },
  benefitTitle: { fontWeight: tokens.fontWeightSemibold },
  benefitDescription: { color: tokens.colorNeutralForeground2 },
  featured: {
    position: 'relative',
    paddingTop: '34px',
    paddingBottom: '24px',
    backgroundImage: 'linear-gradient(180deg, rgba(255, 239, 229, 0.55), rgba(255, 248, 243, 0.18))',
  },
  featuredInner: {
    width: '100%',
    maxWidth: '1400px',
    margin: '0 auto',
  },
  catalogLink: {
    display: 'flex',
    justifyContent: 'center',
    paddingTop: tokens.spacingVerticalL,
  },
})

const BENEFITS = [
  { icon: <ShieldCheckmark24Regular />, title: 'Compras seguras', description: 'Tu confianza es lo primero' },
  { icon: <VehicleTruckProfile24Regular />, title: 'Seguimiento de pedidos', description: 'Conoce el estado de tus compras' },
  { icon: <Tag24Regular />, title: 'Gran variedad', description: 'Tecnología, moda, hogar y más' },
]

function Benefit({ icon, title, description, styles }) {
  return (
    <div className={styles.benefit}>
      <span className={styles.benefitIconWrap} aria-hidden="true">{icon}</span>
      <span className={styles.benefitText}>
        <Text className={styles.benefitTitle}>{title}</Text>
        <Text className={styles.benefitDescription} size={300}>{description}</Text>
      </span>
    </div>
  )
}

Benefit.propTypes = {
  icon: PropTypes.node.isRequired,
  title: PropTypes.string.isRequired,
  description: PropTypes.string.isRequired,
  styles: PropTypes.object.isRequired,
}

export default function Home() {
  const styles = useStyles()
  const { user } = useAuth()

  return (
    <div className={styles.page}>
      <main>
        <section className={mergeClasses(styles.container, styles.hero)} aria-labelledby="home-title">
          <div className={styles.copy}>
            <Display as="h1" id="home-title" className={styles.headline}>
              Bienvenido a ESI{''}
              <span className={styles.accent}>Buy</span>
            </Display>
            <p className={styles.description}>
              El marketplace donde encontrarás productos de calidad, podrás gestionar tus pedidos y venderás de forma segura y sencilla.
            </p>
            {!user && (
              <div className={styles.actions}>
                <LinkButton
                  to={PATHS.LOGIN}
                  appearance="primary"
                  size="large"
                  icon={<ArrowRight20Regular />}
                  iconPosition="after"
                  className={styles.primaryAction}
                >
                  Iniciar sesión
                </LinkButton>
                <LinkButton to={PATHS.REGISTER} appearance="outline" size="large" className={styles.secondaryAction}>
                  Crear cuenta
                </LinkButton>
              </div>
            )}
          </div>

          <div className={styles.illustrationStage}>
            <img
              className={styles.illustration}
              src="/images/esibuy-home-illustration.png"
              alt="Ilustración de un portátil con un carrito de compra, un paquete y productos tecnológicos"
              fetchPriority="high"
            />
          </div>
        </section>

        <section className={mergeClasses(styles.container, styles.benefits)} aria-label="Ventajas de ESIBuy">
          {BENEFITS.map((benefit) => (
            <Benefit key={benefit.title} {...benefit} styles={styles} />
          ))}
        </section>

        <section className={styles.featured} aria-label="Productos mejor valorados">
          <div className={styles.featuredInner}>
            <div className={mergeClasses(styles.container, styles.catalogLink)}>
              <LinkButton to={PATHS.PRODUCTS} appearance="transparent" icon={<ArrowRight20Regular />} iconPosition="after">
                Ver catálogo
              </LinkButton>
            </div>
            <FeaturedProducts />
          </div>
        </section>

      </main>
    </div>
  )
}
