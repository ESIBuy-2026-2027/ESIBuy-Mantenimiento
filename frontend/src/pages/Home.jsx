import {
  Button,
  Card,
  Display,
  Subtitle1,
  Text,
  Title2,
  makeStyles,
  tokens,
} from '@fluentui/react-components'
import {
  ArrowRight20Regular,
  BuildingShop24Regular,
  Cart24Regular,
  PersonAdd20Regular,
  ShieldCheckmark16Filled,
  ShieldCheckmark24Regular,
} from '@fluentui/react-icons'
import { Link } from 'react-router-dom'
import LinkButton from '../components/LinkButton'

const DESKTOP = '@media (min-width: 900px)'

const stats = [
  { value: '3', label: 'Perfiles de usuario' },
  { value: '2FA', label: 'Acceso con doble verificación' },
  { value: 'RGPD', label: 'Privacidad de tus datos' },
]

const features = [
  {
    icon: <Cart24Regular />,
    title: 'Compra con confianza',
    text: 'Busca productos, guarda tus favoritos y sigue cada pedido.',
  },
  {
    icon: <BuildingShop24Regular />,
    title: 'Vende a tu manera',
    text: 'Publica y gestiona tu propio catálogo de productos.',
  },
  {
    icon: <ShieldCheckmark24Regular />,
    title: 'Plataforma supervisada',
    text: 'Un equipo de administración vela por la seguridad de cada cuenta.',
  },
]

const steps = [
  {
    title: 'Crea tu cuenta',
    text: 'Regístrate en un minuto con tus datos básicos.',
  },
  {
    title: 'Elige tu perfil',
    text: 'Decide si quieres comprar o vender',
  },
  {
    title: 'Empieza a usar ESIBuy',
    text: 'Accede con verificación segura y explora la plataforma.',
  },
]

const useStyles = makeStyles({
  page: {
    minHeight: '100vh',
    display: 'flex',
    flexDirection: 'column',
    backgroundImage: `linear-gradient(180deg, ${tokens.colorBrandBackground2} 0%, ${tokens.colorNeutralBackground1} 55%)`,
  },
  container: {
    width: '100%',
    maxWidth: '1200px',
    margin: '0 auto',
    boxSizing: 'border-box',
    paddingLeft: tokens.spacingHorizontalL,
    paddingRight: tokens.spacingHorizontalL,
  },
  header: {
    display: 'flex',
    alignItems: 'center',
    justifyContent: 'space-between',
    gap: tokens.spacingHorizontalM,
    paddingTop: tokens.spacingVerticalL,
    paddingBottom: tokens.spacingVerticalL,
  },
  logo: {
    color: tokens.colorNeutralForeground1,
    textDecorationLine: 'none',
    fontSize: tokens.fontSizeBase500,
    fontWeight: tokens.fontWeightBold,
    ':hover': { color: tokens.colorBrandForeground1 },
  },
  logoAccent: {
    color: tokens.colorBrandForeground1,
  },
  actions: {
    display: 'flex',
    alignItems: 'center',
    gap: tokens.spacingHorizontalS,
  },
  hero: {
    display: 'grid',
    gridTemplateColumns: '1fr',
    gap: tokens.spacingVerticalXXXL,
    alignItems: 'center',
    paddingTop: tokens.spacingVerticalXXL,
    paddingBottom: tokens.spacingVerticalXXXL,
    [DESKTOP]: {
      gridTemplateColumns: '1.15fr 0.85fr',
      gap: tokens.spacingHorizontalXXXL,
      paddingTop: tokens.spacingVerticalXXXL,
    },
  },
  heroText: {
    display: 'flex',
    flexDirection: 'column',
    alignItems: 'flex-start',
    gap: tokens.spacingVerticalL,
  },
  badge: {
    display: 'inline-flex',
    alignItems: 'center',
    gap: tokens.spacingHorizontalXS,
    paddingTop: tokens.spacingVerticalXS,
    paddingBottom: tokens.spacingVerticalXS,
    paddingLeft: tokens.spacingHorizontalM,
    paddingRight: tokens.spacingHorizontalM,
    borderRadius: tokens.borderRadiusCircular,
    backgroundColor: tokens.colorBrandBackground2,
    color: tokens.colorBrandForeground2,
    fontSize: tokens.fontSizeBase200,
    fontWeight: tokens.fontWeightSemibold,
  },
  title: {
    margin: 0,
    fontSize: tokens.fontSizeHero800,
    lineHeight: tokens.lineHeightHero800,
    [DESKTOP]: {
      fontSize: tokens.fontSizeHero900,
      lineHeight: tokens.lineHeightHero900,
    },
  },
  highlight: {
    color: tokens.colorBrandForeground1,
  },
  description: {
    margin: 0,
    maxWidth: '60ch',
    color: tokens.colorNeutralForeground2,
    fontSize: tokens.fontSizeBase400,
    lineHeight: tokens.lineHeightBase400,
  },
  ctas: {
    display: 'flex',
    flexWrap: 'wrap',
    gap: tokens.spacingHorizontalM,
  },
  stats: {
    display: 'grid',
    gridTemplateColumns: 'repeat(auto-fit, minmax(140px, 1fr))',
    gap: tokens.spacingHorizontalM,
    width: '100%',
  },
  statCard: {
    gap: tokens.spacingVerticalXXS,
  },
  statValue: {
    fontSize: tokens.fontSizeBase600,
    fontWeight: tokens.fontWeightBold,
    color: tokens.colorBrandForeground1,
  },
  statLabel: {
    color: tokens.colorNeutralForeground3,
    fontSize: tokens.fontSizeBase200,
  },
  panel: {
    padding: tokens.spacingVerticalXL,
    gap: tokens.spacingVerticalL,
    borderRadius: tokens.borderRadiusXLarge,
    boxShadow: tokens.shadow28,
  },
  feature: {
    display: 'flex',
    alignItems: 'flex-start',
    gap: tokens.spacingHorizontalM,
  },
  featureIcon: {
    display: 'flex',
    alignItems: 'center',
    justifyContent: 'center',
    flexShrink: 0,
    width: '44px',
    height: '44px',
    borderRadius: tokens.borderRadiusLarge,
    backgroundColor: tokens.colorBrandBackground2,
    color: tokens.colorBrandForeground1,
  },
  featureBody: {
    display: 'flex',
    flexDirection: 'column',
    gap: tokens.spacingVerticalXXS,
  },
  featureText: {
    color: tokens.colorNeutralForeground3,
  },
  section: {
    display: 'flex',
    flexDirection: 'column',
    gap: tokens.spacingVerticalXL,
    paddingTop: tokens.spacingVerticalXXXL,
    paddingBottom: tokens.spacingVerticalXXXL,
    scrollMarginTop: tokens.spacingVerticalL,
  },
  steps: {
    display: 'grid',
    gridTemplateColumns: 'repeat(auto-fit, minmax(240px, 1fr))',
    gap: tokens.spacingHorizontalL,
  },
  stepNumber: {
    display: 'flex',
    alignItems: 'center',
    justifyContent: 'center',
    width: '36px',
    height: '36px',
    borderRadius: tokens.borderRadiusCircular,
    backgroundColor: tokens.colorBrandBackground,
    color: tokens.colorNeutralForegroundOnBrand,
    fontWeight: tokens.fontWeightBold,
  },
  stepText: {
    margin: 0,
    color: tokens.colorNeutralForeground3,
  },
  footer: {
    marginTop: 'auto',
    paddingTop: tokens.spacingVerticalL,
    paddingBottom: tokens.spacingVerticalL,
    textAlign: 'center',
    color: tokens.colorNeutralForeground3,
    borderTop: `1px solid ${tokens.colorNeutralStroke2}`,
  },
})

export default function Home() {
  const styles = useStyles()

  return (
    <div className={styles.page}>
      <header className={`${styles.container} ${styles.header}`}>
        <Link to="/" className={styles.logo}>
          ESI<span className={styles.logoAccent}>Buy</span>
        </Link>
        <nav className={styles.actions} aria-label="Acceso a la cuenta">
          <LinkButton to="/login" appearance="subtle">
            Iniciar sesión
          </LinkButton>
          <LinkButton
            to="/register"
            appearance="primary"
            icon={<PersonAdd20Regular />}
          >
            Registrarse
          </LinkButton>
        </nav>
      </header>

      <main>
        <section className={`${styles.container} ${styles.hero}`}>
          <div className={styles.heroText}>
            <span className={styles.badge}>
              <ShieldCheckmark16Filled aria-hidden="true" />
              Compra y vende con total confianza
            </span>

            <Display as="h1" className={styles.title}>
              Bienvenido a ESIBuy: compra, vende y descubre con{' '}
              <span className={styles.highlight}>garantía total</span>
            </Display>

            <p className={styles.description}>
              ESIBuy es el marketplace donde clientes, vendedores y
              administradores comparten un mismo espacio seguro: encuentra
              productos, gestiona tu catálogo y sigue tus pedidos con total
              tranquilidad.
            </p>

            <div className={styles.ctas}>
              <LinkButton
                to="/register"
                appearance="primary"
                size="large"
                icon={<ArrowRight20Regular />}
                iconPosition="after"
              >
                Crear mi cuenta
              </LinkButton>
              <Button as="a" href="#como-funciona" size="large">
                Cómo funciona
              </Button>
            </div>

            <div className={styles.stats}>
              {stats.map((stat) => (
                <Card key={stat.value} className={styles.statCard}>
                  <Text className={styles.statValue}>{stat.value}</Text>
                  <Text className={styles.statLabel}>{stat.label}</Text>
                </Card>
              ))}
            </div>
          </div>

          <Card className={styles.panel}>
            {features.map((feature) => (
              <div key={feature.title} className={styles.feature}>
                <span className={styles.featureIcon} aria-hidden="true">
                  {feature.icon}
                </span>
                <div className={styles.featureBody}>
                  <Text weight="semibold">{feature.title}</Text>
                  <Text className={styles.featureText}>{feature.text}</Text>
                </div>
              </div>
            ))}
          </Card>
        </section>

        <section
          id="como-funciona"
          className={`${styles.container} ${styles.section}`}
          aria-labelledby="como-funciona-titulo"
        >
          <Title2 as="h2" id="como-funciona-titulo">
            Cómo funciona
          </Title2>
          <div className={styles.steps}>
            {steps.map((step, index) => (
              <Card key={step.title}>
                <span className={styles.stepNumber} aria-hidden="true">
                  {index + 1}
                </span>
                <Subtitle1 as="h3">{step.title}</Subtitle1>
                <p className={styles.stepText}>{step.text}</p>
              </Card>
            ))}
          </div>
        </section>
      </main>

      <footer className={styles.footer}>
        <Text size={200}>
          © {new Date().getFullYear()} ESIBuy · Práctica Integrada ISO
        </Text>
      </footer>
    </div>
  )
}
