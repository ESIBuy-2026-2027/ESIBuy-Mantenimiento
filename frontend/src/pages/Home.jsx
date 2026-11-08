import PropTypes from 'prop-types'
import { useEffect, useRef, useState } from 'react'
import {
  Button,
  Display,
  Subtitle1,
  Tab,
  TabList,
  Text,
  Title2,
  makeStyles,
  mergeClasses,
  tokens,
} from '@fluentui/react-components'
import {
  ArrowRight20Regular,
  BoxCheckmark24Regular,
  BuildingShop24Regular,
  Cart24Regular,
  CheckmarkCircle16Filled,
  DataBarVertical24Regular,
  Diamond24Regular,
  Filter24Regular,
  Heart24Regular,
  Key24Regular,
  LockClosed24Regular,
  Mail24Regular,
  Payment24Regular,
  PeopleTeam24Regular,
  PersonAdd24Regular,
  Search24Regular,
  ShieldCheckmark16Filled,
  ShieldCheckmark24Regular,
  Star16Filled,
  Star24Regular,
  Tag24Regular,
  VehicleTruckProfile24Regular,
  Warning24Regular,
} from '@fluentui/react-icons'
import { useAuth } from '../auth/useAuth'
import LinkButton from '../components/LinkButton'
import { PATHS } from '../routes/paths'

const DESKTOP = '@media (min-width: 900px)'
const NO_MOTION = '@media (prefers-reduced-motion: reduce)'

const stats = [
  { value: '3', label: 'Perfiles: compra, venta y administración' },
  { value: '3', label: 'Factores para iniciar sesión' },
  { value: '100 %', label: 'Datos de pago cifrados' },
]

// «Cómo funciona», por perfil. Solo cuenta lo que la plataforma hace de verdad.
const roles = [
  {
    value: 'comprar',
    tab: 'Quiero comprar',
    intro: 'Encuentra lo que buscas, págalo de forma segura y síguelo hasta tu puerta.',
    steps: [
      {
        icon: <PersonAdd24Regular />,
        title: 'Crea tu cuenta',
        text: 'Regístrate en un minuto como cuenta Estándar o Premium y entra con verificación segura.',
      },
      {
        icon: <Filter24Regular />,
        title: 'Busca y filtra',
        text: 'Explora el catálogo por categoría, precio, valoración y disponibilidad, y guarda tus favoritos.',
      },
      {
        icon: <Payment24Regular />,
        title: 'Compra en un paso',
        text: 'Añade al carrito, elige dirección y método de pago, y confirma tu pedido.',
      },
      {
        icon: <VehicleTruckProfile24Regular />,
        title: 'Sigue tu envío y valora',
        text: 'Mira cómo tu pedido pasa de pagado a entregado y valora lo que has comprado.',
      },
    ],
  },
  {
    value: 'vender',
    tab: 'Quiero vender',
    intro: 'Abre tu tienda, publica tu catálogo y controla tus ventas desde un solo panel.',
    steps: [
      {
        icon: <BuildingShop24Regular />,
        title: 'Registra tu tienda',
        text: 'Indica tu nombre comercial y tu categoría principal; la administración activa tu cuenta.',
      },
      {
        icon: <Tag24Regular />,
        title: 'Publica tus productos',
        text: 'Gestiona precios, stock, disponibilidad y ofertas, también exclusivas para Premium.',
      },
      {
        icon: <BoxCheckmark24Regular />,
        title: 'Prepara los pedidos',
        text: 'Recibe cada pedido de tus productos y actualiza su estado hasta la entrega.',
      },
      {
        icon: <DataBarVertical24Regular />,
        title: 'Mide tus resultados',
        text: 'Consulta tus estadísticas de ventas, valoraciones y stock para decidir mejor.',
      },
    ],
  },
  {
    value: 'administrar',
    tab: 'Administro',
    intro: 'Un equipo de administración cuida de que todo funcione y de que cada cuenta sea fiable.',
    steps: [
      {
        icon: <PeopleTeam24Regular />,
        title: 'Gestiona las cuentas',
        text: 'Activa, edita o desactiva cuentas, y da de alta a otras personas de administración.',
      },
      {
        icon: <Tag24Regular />,
        title: 'Ordena el catálogo',
        text: 'Mantiene las categorías con las que se organizan y se buscan los productos.',
      },
      {
        icon: <Warning24Regular />,
        title: 'Resuelve incidencias',
        text: 'Atiende cada incidencia que se abre y la cierra cuando está resuelta.',
      },
      {
        icon: <ShieldCheckmark24Regular />,
        title: 'Supervisa sin tocar',
        text: 'Consulta los productos y pedidos de cada tienda en modo solo lectura.',
      },
    ],
  },
]

const features = [
  {
    icon: <Search24Regular />,
    title: 'Búsqueda que encuentra',
    text: 'Filtros por categoría, precio, valoración y disponibilidad para llegar antes a lo que quieres.',
    wide: true,
  },
  {
    icon: <Heart24Regular />,
    title: 'Favoritos y listas de deseos',
    text: 'Guarda lo que te gusta y vuelve cuando quieras.',
  },
  {
    icon: <Star24Regular />,
    title: 'Valoraciones reales',
    text: 'Solo valora quien ha comprado el producto.',
  },
  {
    icon: <Diamond24Regular />,
    title: 'ESIBuy Premium',
    text: 'Descuentos exclusivos, sin gastos de envío y envíos más rápidos para quien da el salto a Premium.',
    wide: true,
    premium: true,
  },
  {
    icon: <VehicleTruckProfile24Regular />,
    title: 'Envíos con seguimiento',
    text: 'Sabes en todo momento en qué punto está tu pedido.',
  },
  {
    icon: <Cart24Regular />,
    title: 'Carrito y pedidos',
    text: 'Consulta tu historial y cancela un pedido mientras aún se pueda.',
  },
  {
    icon: <Warning24Regular />,
    title: 'Ayuda cuando la necesitas',
    text: 'Abre una incidencia y la administración se encarga de resolverla.',
  },
]

const factors = [
  { icon: <Key24Regular />, title: 'Tu contraseña', text: 'Con una política segura y guardada con hash, nunca en claro.' },
  { icon: <Mail24Regular />, title: 'Un código por correo', text: 'Cada inicio de sesión se confirma con un código que caduca.' },
  {
    icon: <ShieldCheckmark24Regular />,
    title: 'Tu app de autenticación',
    text: 'Un tercer factor con Microsoft Authenticator.',
  },
  {
    icon: <LockClosed24Regular />,
    title: 'Pagos cifrados',
    text: 'Tus métodos de pago se guardan cifrados en la base de datos.',
  },
]

const ORDER_STATES = ['Pagado', 'Preparando', 'Enviado', 'Entregado']

// Animaciones (Griffel admite los keyframes como objeto en animationName)
const floatY = {
  '0%': { transform: 'translateY(0)' },
  '50%': { transform: 'translateY(-12px)' },
  '100%': { transform: 'translateY(0)' },
}
const drift = {
  '0%': { transform: 'translate(0, 0) scale(1)' },
  '33%': { transform: 'translate(40px, -30px) scale(1.08)' },
  '66%': { transform: 'translate(-30px, 20px) scale(0.95)' },
  '100%': { transform: 'translate(0, 0) scale(1)' },
}
const shimmer = {
  '0%': { backgroundPosition: '0% 50%' },
  '50%': { backgroundPosition: '100% 50%' },
  '100%': { backgroundPosition: '0% 50%' },
}
const riseIn = {
  from: { opacity: 0, transform: 'translateY(16px)' },
  to: { opacity: 1, transform: 'translateY(0)' },
}
const fillTrack = {
  '0%': { width: '0%' },
  '15%': { width: '0%' },
  '85%': { width: '100%' },
  '100%': { width: '100%' },
}
const pulse = {
  '0%': { boxShadow: `0 0 0 0 ${tokens.colorBrandStroke2}` },
  '70%': { boxShadow: '0 0 0 18px transparent' },
  '100%': { boxShadow: '0 0 0 0 transparent' },
}

const useStyles = makeStyles({
  // Ocupa el alto que deja la cabecera para que el pie siga abajo, y recorta las manchas del fondo
  page: {
    position: 'relative',
    display: 'flex',
    flexDirection: 'column',
    flexGrow: 1,
    overflowX: 'hidden',
  },
  container: {
    width: '100%',
    maxWidth: '1200px',
    margin: '0 auto',
    boxSizing: 'border-box',
    paddingLeft: tokens.spacingHorizontalL,
    paddingRight: tokens.spacingHorizontalL,
  },

  // Fondo del hero: manchas de color que se mueven despacio
  backdrop: {
    position: 'absolute',
    inset: 0,
    zIndex: 0,
    pointerEvents: 'none',
    overflow: 'hidden',
    // Se difumina hacia abajo para que el color no acabe en un corte
    maskImage: 'linear-gradient(to bottom, black 60%, transparent)',
    WebkitMaskImage: 'linear-gradient(to bottom, black 60%, transparent)',
  },
  blob: {
    position: 'absolute',
    width: '420px',
    height: '420px',
    borderRadius: '50%',
    filter: 'blur(70px)',
    opacity: 0.55,
    animationName: drift,
    animationDuration: '18s',
    animationIterationCount: 'infinite',
    animationTimingFunction: 'ease-in-out',
    [NO_MOTION]: { animationName: 'none' },
  },
  blobBrand: { top: '-120px', left: '-80px', backgroundColor: tokens.colorBrandBackground2 },
  blobWarm: {
    top: '60px',
    right: '-120px',
    backgroundColor: tokens.colorPaletteYellowBackground2,
    animationDelay: '-6s',
  },
  blobRose: {
    bottom: '-160px',
    left: '35%',
    backgroundColor: tokens.colorPaletteRedBackground2,
    animationDelay: '-12s',
  },

  hero: {
    position: 'relative',
    zIndex: 1,
    display: 'grid',
    gridTemplateColumns: '1fr',
    gap: tokens.spacingVerticalXXXL,
    alignItems: 'center',
    paddingTop: tokens.spacingVerticalXXL,
    paddingBottom: tokens.spacingVerticalXXXL,
    [DESKTOP]: {
      gridTemplateColumns: '1.1fr 0.9fr',
      gap: tokens.spacingHorizontalXXXL,
      paddingTop: '72px',
      paddingBottom: '72px',
    },
  },
  heroText: {
    display: 'flex',
    flexDirection: 'column',
    alignItems: 'flex-start',
    gap: tokens.spacingVerticalL,
  },
  enter: {
    animationName: riseIn,
    animationDuration: '700ms',
    animationTimingFunction: 'cubic-bezier(0.2, 0.8, 0.2, 1)',
    animationFillMode: 'both',
    [NO_MOTION]: { animationName: 'none' },
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
    backgroundImage: `linear-gradient(90deg, ${tokens.colorBrandForeground1}, ${tokens.colorPaletteRedForeground1}, ${tokens.colorPaletteMarigoldForeground2}, ${tokens.colorBrandForeground1})`,
    backgroundSize: '300% 100%',
    backgroundClip: 'text',
    WebkitBackgroundClip: 'text',
    color: 'transparent',
    animationName: shimmer,
    animationDuration: '8s',
    animationIterationCount: 'infinite',
    animationTimingFunction: 'ease-in-out',
    [NO_MOTION]: { animationName: 'none' },
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
  primaryCta: {
    transitionProperty: 'transform, box-shadow',
    transitionDuration: tokens.durationNormal,
    ':hover': { transform: 'translateY(-2px)', boxShadow: tokens.shadow16 },
    [NO_MOTION]: { transitionProperty: 'none', ':hover': { transform: 'none' } },
  },
  stats: {
    display: 'grid',
    gridTemplateColumns: 'repeat(auto-fit, minmax(150px, 1fr))',
    gap: tokens.spacingHorizontalM,
    width: '100%',
    margin: 0,
  },
  stat: {
    display: 'flex',
    flexDirection: 'column-reverse',
    gap: tokens.spacingVerticalXXS,
    padding: tokens.spacingVerticalM,
    borderRadius: tokens.borderRadiusLarge,
    backgroundColor: tokens.colorNeutralBackground1,
    border: `1px solid ${tokens.colorNeutralStroke2}`,
    boxShadow: tokens.shadow4,
  },
  statValue: {
    margin: 0,
    fontSize: tokens.fontSizeBase600,
    fontWeight: tokens.fontWeightBold,
    color: tokens.colorBrandForeground1,
  },
  statLabel: {
    color: tokens.colorNeutralForeground3,
    fontSize: tokens.fontSizeBase200,
  },

  // Escena del hero: tarjetas ilustrativas que flotan
  scene: {
    position: 'relative',
    display: 'flex',
    flexDirection: 'column',
    gap: tokens.spacingVerticalL,
    maxWidth: '420px',
    width: '100%',
    justifySelf: 'center',
  },
  floating: {
    animationName: floatY,
    animationDuration: '6s',
    animationIterationCount: 'infinite',
    animationTimingFunction: 'ease-in-out',
    [NO_MOTION]: { animationName: 'none' },
  },
  sceneCard: {
    display: 'flex',
    flexDirection: 'column',
    gap: tokens.spacingVerticalS,
    padding: tokens.spacingVerticalL,
    borderRadius: tokens.borderRadiusXLarge,
    backgroundColor: tokens.colorNeutralBackground1,
    boxShadow: tokens.shadow28,
    border: `1px solid ${tokens.colorNeutralStroke2}`,
  },
  shiftRight: { marginLeft: '12%', animationDelay: '-2s' },
  shiftLeft: { marginRight: '14%', animationDelay: '-4s' },
  productRow: {
    display: 'flex',
    alignItems: 'center',
    gap: tokens.spacingHorizontalM,
  },
  productImage: {
    flexShrink: 0,
    width: '64px',
    height: '64px',
    borderRadius: tokens.borderRadiusLarge,
    backgroundImage: `linear-gradient(135deg, ${tokens.colorBrandBackground}, ${tokens.colorPaletteRedBackground3})`,
    display: 'flex',
    alignItems: 'center',
    justifyContent: 'center',
    color: tokens.colorNeutralForegroundOnBrand,
  },
  lines: {
    display: 'flex',
    flexDirection: 'column',
    gap: tokens.spacingVerticalXS,
    flexGrow: 1,
  },
  line: {
    height: '10px',
    borderRadius: tokens.borderRadiusCircular,
    backgroundColor: tokens.colorNeutralBackground5,
  },
  stars: {
    display: 'flex',
    gap: '2px',
    color: tokens.colorPaletteMarigoldForeground1,
  },
  cartPill: {
    alignSelf: 'flex-end',
    display: 'inline-flex',
    alignItems: 'center',
    gap: tokens.spacingHorizontalXS,
    paddingTop: tokens.spacingVerticalXS,
    paddingBottom: tokens.spacingVerticalXS,
    paddingLeft: tokens.spacingHorizontalM,
    paddingRight: tokens.spacingHorizontalM,
    borderRadius: tokens.borderRadiusCircular,
    backgroundColor: tokens.colorBrandBackground,
    color: tokens.colorNeutralForegroundOnBrand,
    fontSize: tokens.fontSizeBase200,
    fontWeight: tokens.fontWeightSemibold,
  },
  trackHeader: {
    display: 'flex',
    alignItems: 'center',
    gap: tokens.spacingHorizontalS,
    color: tokens.colorBrandForeground1,
  },
  track: {
    position: 'relative',
    height: '6px',
    borderRadius: tokens.borderRadiusCircular,
    backgroundColor: tokens.colorNeutralBackground5,
    overflow: 'hidden',
  },
  trackFill: {
    position: 'absolute',
    inset: '0 auto 0 0',
    width: '100%',
    borderRadius: tokens.borderRadiusCircular,
    backgroundImage: `linear-gradient(90deg, ${tokens.colorBrandBackground}, ${tokens.colorPaletteGreenBackground3})`,
    animationName: fillTrack,
    animationDuration: '5s',
    animationIterationCount: 'infinite',
    animationTimingFunction: 'ease-in-out',
    [NO_MOTION]: { animationName: 'none' },
  },
  trackStates: {
    display: 'flex',
    justifyContent: 'space-between',
    color: tokens.colorNeutralForeground3,
    fontSize: tokens.fontSizeBase100,
  },
  verified: {
    display: 'flex',
    alignItems: 'center',
    gap: tokens.spacingHorizontalS,
  },
  codeDigits: {
    display: 'flex',
    gap: tokens.spacingHorizontalXS,
  },
  digit: {
    width: '28px',
    height: '34px',
    borderRadius: tokens.borderRadiusMedium,
    backgroundColor: tokens.colorBrandBackground2,
    color: tokens.colorBrandForeground1,
    display: 'flex',
    alignItems: 'center',
    justifyContent: 'center',
    fontWeight: tokens.fontWeightBold,
  },
  okIcon: { color: tokens.colorPaletteGreenForeground1 },

  // Secciones
  section: {
    position: 'relative',
    zIndex: 1,
    display: 'flex',
    flexDirection: 'column',
    gap: tokens.spacingVerticalXL,
    paddingTop: '48px',
    paddingBottom: '48px',
    scrollMarginTop: tokens.spacingVerticalL,
  },
  sectionHead: {
    display: 'flex',
    flexDirection: 'column',
    gap: tokens.spacingVerticalS,
    maxWidth: '68ch',
  },
  eyebrow: {
    color: tokens.colorBrandForeground1,
    fontWeight: tokens.fontWeightSemibold,
    textTransform: 'uppercase',
    letterSpacing: '0.08em',
    fontSize: tokens.fontSizeBase200,
  },
  sectionText: {
    margin: 0,
    color: tokens.colorNeutralForeground2,
    fontSize: tokens.fontSizeBase400,
    lineHeight: tokens.lineHeightBase400,
  },

  // Aparición al hacer scroll
  reveal: {
    opacity: 0,
    transform: 'translateY(28px)',
    transitionProperty: 'opacity, transform',
    transitionDuration: '700ms',
    transitionTimingFunction: 'cubic-bezier(0.2, 0.8, 0.2, 1)',
    [NO_MOTION]: { opacity: 1, transform: 'none', transitionProperty: 'none' },
  },
  revealed: {
    opacity: 1,
    transform: 'none',
  },

  // Cómo funciona
  tabs: {
    alignSelf: 'flex-start',
    flexWrap: 'wrap',
    padding: tokens.spacingVerticalXXS,
    borderRadius: tokens.borderRadiusXLarge,
    backgroundColor: tokens.colorNeutralBackground3,
  },
  roleIntro: {
    margin: 0,
    color: tokens.colorNeutralForeground2,
    fontSize: tokens.fontSizeBase400,
  },
  steps: {
    position: 'relative',
    display: 'grid',
    gridTemplateColumns: '1fr',
    gap: tokens.spacingHorizontalL,
    listStyleType: 'none',
    margin: 0,
    padding: 0,
    [DESKTOP]: { gridTemplateColumns: 'repeat(4, 1fr)' },
  },
  step: {
    position: 'relative',
    display: 'flex',
    flexDirection: 'column',
    gap: tokens.spacingVerticalS,
    padding: tokens.spacingVerticalL,
    borderRadius: tokens.borderRadiusXLarge,
    backgroundColor: tokens.colorNeutralBackground1,
    border: `1px solid ${tokens.colorNeutralStroke2}`,
    boxShadow: tokens.shadow4,
    animationName: riseIn,
    animationDuration: '550ms',
    animationTimingFunction: 'cubic-bezier(0.2, 0.8, 0.2, 1)',
    animationFillMode: 'both',
    transitionProperty: 'transform, box-shadow',
    transitionDuration: tokens.durationNormal,
    ':hover': { transform: 'translateY(-4px)', boxShadow: tokens.shadow16 },
    [NO_MOTION]: { animationName: 'none', transitionProperty: 'none', ':hover': { transform: 'none' } },
  },
  stepTop: {
    display: 'flex',
    alignItems: 'center',
    justifyContent: 'space-between',
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
  stepIcon: {
    color: tokens.colorBrandForeground1,
    display: 'flex',
  },
  stepTitle: { margin: 0 },
  stepText: {
    margin: 0,
    color: tokens.colorNeutralForeground3,
  },

  // Funcionalidades
  bento: {
    display: 'grid',
    gridTemplateColumns: '1fr',
    gap: tokens.spacingHorizontalL,
    [DESKTOP]: { gridTemplateColumns: 'repeat(3, 1fr)' },
  },
  feature: {
    display: 'flex',
    flexDirection: 'column',
    gap: tokens.spacingVerticalS,
    padding: tokens.spacingVerticalXL,
    borderRadius: tokens.borderRadiusXLarge,
    backgroundColor: tokens.colorNeutralBackground1,
    border: `1px solid ${tokens.colorNeutralStroke2}`,
    boxShadow: tokens.shadow4,
    transitionProperty: 'transform, box-shadow, border-color, opacity',
    transitionDuration: '700ms, 250ms, 250ms, 700ms',
    ':hover': {
      transform: 'translateY(-4px)',
      boxShadow: tokens.shadow16,
      borderTopColor: tokens.colorBrandStroke1,
      borderRightColor: tokens.colorBrandStroke1,
      borderBottomColor: tokens.colorBrandStroke1,
      borderLeftColor: tokens.colorBrandStroke1,
    },
  },
  featureWide: {
    [DESKTOP]: { gridColumn: 'span 2' },
  },
  featurePremium: {
    backgroundImage: `linear-gradient(135deg, ${tokens.colorBrandBackground2}, ${tokens.colorNeutralBackground1} 70%)`,
  },
  featureIcon: {
    display: 'flex',
    alignItems: 'center',
    justifyContent: 'center',
    width: '48px',
    height: '48px',
    borderRadius: tokens.borderRadiusLarge,
    backgroundColor: tokens.colorBrandBackground2,
    color: tokens.colorBrandForeground1,
  },
  featureTitle: { margin: 0 },
  featureText: {
    margin: 0,
    color: tokens.colorNeutralForeground3,
  },

  // Seguridad
  securityBand: {
    display: 'grid',
    gridTemplateColumns: '1fr',
    gap: tokens.spacingVerticalXXL,
    alignItems: 'center',
    padding: tokens.spacingVerticalXXL,
    borderRadius: '24px',
    backgroundColor: tokens.colorNeutralBackground2,
    border: `1px solid ${tokens.colorNeutralStroke2}`,
    [DESKTOP]: {
      gridTemplateColumns: '0.8fr 1.2fr',
      padding: '48px',
    },
  },
  shield: {
    justifySelf: 'center',
    display: 'flex',
    alignItems: 'center',
    justifyContent: 'center',
    width: '120px',
    height: '120px',
    borderRadius: '50%',
    backgroundColor: tokens.colorBrandBackground,
    color: tokens.colorNeutralForegroundOnBrand,
    animationName: pulse,
    animationDuration: '2.4s',
    animationIterationCount: 'infinite',
    [NO_MOTION]: { animationName: 'none' },
    '& svg': { width: '56px', height: '56px' },
  },
  factors: {
    display: 'grid',
    gridTemplateColumns: 'repeat(auto-fit, minmax(220px, 1fr))',
    gap: tokens.spacingHorizontalL,
    listStyleType: 'none',
    margin: 0,
    padding: 0,
  },
  factor: {
    display: 'flex',
    alignItems: 'flex-start',
    gap: tokens.spacingHorizontalM,
  },
  factorIcon: {
    flexShrink: 0,
    display: 'flex',
    alignItems: 'center',
    justifyContent: 'center',
    width: '40px',
    height: '40px',
    borderRadius: tokens.borderRadiusLarge,
    backgroundColor: tokens.colorNeutralBackground1,
    color: tokens.colorBrandForeground1,
    boxShadow: tokens.shadow4,
  },
  factorBody: {
    display: 'flex',
    flexDirection: 'column',
    gap: tokens.spacingVerticalXXS,
  },
  factorText: { color: tokens.colorNeutralForeground3 },

  // Llamada final
  finalCta: {
    position: 'relative',
    overflow: 'hidden',
    display: 'flex',
    flexDirection: 'column',
    alignItems: 'center',
    gap: tokens.spacingVerticalL,
    padding: '56px 24px',
    borderRadius: '28px',
    textAlign: 'center',
    color: tokens.colorNeutralForegroundOnBrand,
    backgroundImage: `linear-gradient(120deg, ${tokens.colorBrandBackground}, ${tokens.colorPaletteRedBackground3}, ${tokens.colorBrandBackgroundPressed}, ${tokens.colorBrandBackground})`,
    backgroundSize: '300% 300%',
    animationName: shimmer,
    animationDuration: '14s',
    animationIterationCount: 'infinite',
    animationTimingFunction: 'ease-in-out',
    [NO_MOTION]: { animationName: 'none' },
  },
  finalTitle: {
    margin: 0,
    color: tokens.colorNeutralForegroundOnBrand,
  },
  finalText: {
    margin: 0,
    maxWidth: '56ch',
    fontSize: tokens.fontSizeBase400,
    lineHeight: tokens.lineHeightBase400,
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

// Devuelve una referencia y si el elemento ya ha entrado en pantalla. Sin IntersectionObserver (navegadores
// antiguos o tests) se muestra todo desde el principio, para que el contenido nunca quede oculto.
function useReveal() {
  const ref = useRef(null)
  const [visible, setVisible] = useState(() => typeof IntersectionObserver === 'undefined')

  useEffect(() => {
    if (visible || !ref.current) return undefined
    const observer = new IntersectionObserver(
      ([entry]) => {
        if (entry.isIntersecting) {
          setVisible(true)
          observer.disconnect()
        }
      },
      { threshold: 0.15 },
    )
    observer.observe(ref.current)
    return () => observer.disconnect()
  }, [visible])

  return [ref, visible]
}

function Reveal({ as: Component = 'div', className, delay = 0, children, ...rest }) {
  const styles = useStyles()
  const [ref, visible] = useReveal()

  return (
    <Component
      ref={ref}
      className={mergeClasses(styles.reveal, visible && styles.revealed, className)}
      style={delay ? { transitionDelay: `${delay}ms` } : undefined}
      {...rest}
    >
      {children}
    </Component>
  )
}

Reveal.propTypes = {
  as: PropTypes.elementType,
  className: PropTypes.string,
  delay: PropTypes.number,
  children: PropTypes.node,
}

// Ilustración del hero: un producto, el seguimiento de un pedido y el acceso verificado. Es decorativa.
function HeroScene() {
  const styles = useStyles()

  return (
    <div className={styles.scene} aria-hidden="true">
      <div className={mergeClasses(styles.sceneCard, styles.floating)}>
        <div className={styles.productRow}>
          <span className={styles.productImage}>
            <Cart24Regular />
          </span>
          <div className={styles.lines}>
            <span className={styles.line} style={{ width: '85%' }} />
            <span className={styles.line} style={{ width: '55%' }} />
            <span className={styles.stars}>
              {[1, 2, 3, 4, 5].map((n) => (
                <Star16Filled key={n} />
              ))}
            </span>
          </div>
        </div>
        <span className={styles.cartPill}>
          <Cart24Regular style={{ width: 16, height: 16 }} />
          Añadido al carrito
        </span>
      </div>

      <div className={mergeClasses(styles.sceneCard, styles.floating, styles.shiftRight)}>
        <span className={styles.trackHeader}>
          <VehicleTruckProfile24Regular />
          <Text weight="semibold">Tu pedido está en camino</Text>
        </span>
        <span className={styles.track}>
          <span className={styles.trackFill} />
        </span>
        <span className={styles.trackStates}>
          {ORDER_STATES.map((state) => (
            <span key={state}>{state}</span>
          ))}
        </span>
      </div>

      <div className={mergeClasses(styles.sceneCard, styles.floating, styles.shiftLeft)}>
        <span className={styles.verified}>
          <CheckmarkCircle16Filled className={styles.okIcon} />
          <Text weight="semibold">Acceso verificado</Text>
        </span>
        <span className={styles.codeDigits}>
          {['4', '8', '1', '5', '9', '2'].map((digit, index) => (
            <span key={index} className={styles.digit}>
              {digit}
            </span>
          ))}
        </span>
      </div>
    </div>
  )
}

function HowItWorks() {
  const styles = useStyles()
  const [selected, setSelected] = useState(roles[0].value)
  const role = roles.find((r) => r.value === selected)

  return (
    <section
      id="como-funciona"
      className={mergeClasses(styles.container, styles.section)}
      aria-labelledby="como-funciona-titulo"
    >
      <Reveal className={styles.sectionHead}>
        <span className={styles.eyebrow}>Paso a paso</span>
        <Title2 as="h2" id="como-funciona-titulo">
          Cómo funciona
        </Title2>
        <p className={styles.sectionText}>
          ESIBuy se adapta a lo que vienes a hacer. Elige tu perfil y descubre el recorrido completo.
        </p>
      </Reveal>

      <TabList
        className={styles.tabs}
        selectedValue={selected}
        onTabSelect={(_, data) => setSelected(data.value)}
        appearance="subtle-circular"
        size="large"
      >
        {roles.map((r) => (
          <Tab key={r.value} value={r.value} id={`rol-${r.value}`}>
            {r.tab}
          </Tab>
        ))}
      </TabList>

      <div role="tabpanel" aria-labelledby={`rol-${role.value}`}>
        <p className={styles.roleIntro}>{role.intro}</p>
        {/* La key reinicia la animación de entrada al cambiar de perfil */}
        <ol key={role.value} className={styles.steps} style={{ marginTop: tokens.spacingVerticalL }}>
          {role.steps.map((step, index) => (
            <li key={step.title} className={styles.step} style={{ animationDelay: `${index * 110}ms` }}>
              <div className={styles.stepTop}>
                <span className={styles.stepNumber} aria-hidden="true">
                  {index + 1}
                </span>
                <span className={styles.stepIcon} aria-hidden="true">
                  {step.icon}
                </span>
              </div>
              <Subtitle1 as="h3" className={styles.stepTitle}>
                {step.title}
              </Subtitle1>
              <p className={styles.stepText}>{step.text}</p>
            </li>
          ))}
        </ol>
      </div>
    </section>
  )
}

export default function Home() {
  const styles = useStyles()
  const { user } = useAuth()

  return (
    <div className={styles.page}>
      <main>
        <div style={{ position: 'relative' }}>
          <div className={styles.backdrop} aria-hidden="true">
            <span className={mergeClasses(styles.blob, styles.blobBrand)} />
            <span className={mergeClasses(styles.blob, styles.blobWarm)} />
            <span className={mergeClasses(styles.blob, styles.blobRose)} />
          </div>

          <section className={mergeClasses(styles.container, styles.hero)}>
            <div className={styles.heroText}>
              <span className={mergeClasses(styles.badge, styles.enter)}>
                <ShieldCheckmark16Filled aria-hidden="true" />
                Compra y vende con total confianza
              </span>

              <Display as="h1" className={mergeClasses(styles.title, styles.enter)} style={{ animationDelay: '80ms' }}>
                Bienvenido a ESIBuy: compra, vende y descubre con{' '}
                <span className={styles.highlight}>garantía total</span>
              </Display>

              <p className={mergeClasses(styles.description, styles.enter)} style={{ animationDelay: '160ms' }}>
                ESIBuy es el marketplace donde clientes, vendedores y administradores comparten un mismo espacio
                seguro: encuentra productos, gestiona tu catálogo y sigue tus pedidos con total tranquilidad.
              </p>

              <div className={mergeClasses(styles.ctas, styles.enter)} style={{ animationDelay: '240ms' }}>
                {/* Con la sesión iniciada no se ofrece crear otra cuenta (fallos de la demo del Sprint 1) */}
                {!user && (
                  <LinkButton
                    to={PATHS.REGISTER}
                    appearance="primary"
                    size="large"
                    icon={<ArrowRight20Regular />}
                    iconPosition="after"
                    className={styles.primaryCta}
                  >
                    Crear mi cuenta
                  </LinkButton>
                )}
                <Button as="a" href="#como-funciona" size="large">
                  Cómo funciona
                </Button>
              </div>

              <dl className={mergeClasses(styles.stats, styles.enter)} style={{ animationDelay: '320ms' }}>
                {stats.map((stat) => (
                  <div key={stat.label} className={styles.stat}>
                    <dt className={styles.statLabel}>{stat.label}</dt>
                    <dd className={styles.statValue}>{stat.value}</dd>
                  </div>
                ))}
              </dl>
            </div>

            <HeroScene />
          </section>
        </div>

        <HowItWorks />

        <section
          className={mergeClasses(styles.container, styles.section)}
          aria-labelledby="funcionalidades-titulo"
        >
          <Reveal className={styles.sectionHead}>
            <span className={styles.eyebrow}>Todo en un sitio</span>
            <Title2 as="h2" id="funcionalidades-titulo">
              Lo que puedes hacer en ESIBuy
            </Title2>
          </Reveal>
          <div className={styles.bento}>
            {features.map((feature, index) => (
              <Reveal
                key={feature.title}
                className={mergeClasses(
                  styles.feature,
                  feature.wide && styles.featureWide,
                  feature.premium && styles.featurePremium,
                )}
                delay={index * 90}
              >
                <span className={styles.featureIcon} aria-hidden="true">
                  {feature.icon}
                </span>
                <Subtitle1 as="h3" className={styles.featureTitle}>
                  {feature.title}
                </Subtitle1>
                <p className={styles.featureText}>{feature.text}</p>
              </Reveal>
            ))}
          </div>
        </section>

        <section
          className={mergeClasses(styles.container, styles.section)}
          aria-labelledby="seguridad-titulo"
        >
          <Reveal className={styles.securityBand}>
            <div className={styles.sectionHead}>
              <span className={styles.shield} aria-hidden="true">
                <ShieldCheckmark24Regular />
              </span>
              <span className={styles.eyebrow}>Seguridad</span>
              <Title2 as="h2" id="seguridad-titulo">
                Tu cuenta, protegida por tres llaves
              </Title2>
              <p className={styles.sectionText}>
                Para entrar en ESIBuy no basta con una contraseña: cada acceso se confirma con tres factores.
              </p>
            </div>
            <ul className={styles.factors}>
              {factors.map((factor) => (
                <li key={factor.title} className={styles.factor}>
                  <span className={styles.factorIcon} aria-hidden="true">
                    {factor.icon}
                  </span>
                  <div className={styles.factorBody}>
                    <Text weight="semibold">{factor.title}</Text>
                    <Text className={styles.factorText}>{factor.text}</Text>
                  </div>
                </li>
              ))}
            </ul>
          </Reveal>
        </section>

        {!user && (
          <section
            className={mergeClasses(styles.container, styles.section)}
            aria-labelledby="empezar-titulo"
          >
            <Reveal className={styles.finalCta}>
              <Title2 as="h2" id="empezar-titulo" className={styles.finalTitle}>
                ¿Empezamos?
              </Title2>
              <p className={styles.finalText}>
                Abre tu cuenta gratis y descubre una forma más segura de comprar y vender.
              </p>
              <LinkButton
                to={PATHS.REGISTER}
                size="large"
                icon={<ArrowRight20Regular />}
                iconPosition="after"
                className={styles.primaryCta}
              >
                Empezar ahora
              </LinkButton>
            </Reveal>
          </section>
        )}
      </main>

      <footer className={styles.footer}>
        <Text size={200}>© {new Date().getFullYear()} ESIBuy · Práctica Integrada ISO</Text>
      </footer>
    </div>
  )
}
