import { useEffect, useState } from 'react'
import { Button, Text, makeStyles, mergeClasses, tokens } from '@fluentui/react-components'
import {
  ArrowRight16Regular,
  ArrowTrendingDown20Filled,
  BoxCheckmark20Filled,
  Dismiss20Regular,
  Sparkle20Filled,
} from '@fluentui/react-icons'
import LinkButton from '../../components/LinkButton'
import { PATHS } from '../../routes/paths'
import { formatPrice } from '../../utils/format'
import { MORADO, NARANJA, ROJO, SIN_IMAGEN, VERDE, esBajadaConPrecios, porcentajeRebaja } from './alertPresentation'
import { textoSinLeer } from './alertsText'
import { useAlerts } from './useAlerts'
import { useAvisoTrasIniciarSesion } from './useAvisoTrasIniciarSesion'

const MOVIL = '@media (max-width: 600px)'

const useStyles = makeStyles({
  // Fija abajo a la derecha, por encima de la página y por debajo del carrito; en el móvil ocupa el ancho
  notification: {
    position: 'fixed',
    right: '24px',
    bottom: '24px',
    zIndex: 1150,
    width: 'min(400px, calc(100vw - 24px))',
    boxSizing: 'border-box',
    overflow: 'hidden',
    backgroundColor: '#FFFFFF',
    borderRadius: '20px',
    boxShadow: '0 20px 48px rgba(16, 24, 40, 0.24), 0 4px 12px rgba(16, 24, 40, 0.1)',
    animationName: {
      from: { opacity: 0, transform: 'translateY(32px) scale(0.96)' },
      to: { opacity: 1, transform: 'translateY(0) scale(1)' },
    },
    animationDuration: '450ms',
    animationTimingFunction: 'cubic-bezier(0.2, 0.9, 0.3, 1.2)',
    '@media (prefers-reduced-motion: reduce)': { animationName: 'none' },
    [MOVIL]: { right: '12px', bottom: '12px', left: '12px', width: 'auto' },
  },
  // Franja de color con el tipo de alerta, como el cartel de la tienda
  band: {
    display: 'flex',
    alignItems: 'center',
    gap: tokens.spacingHorizontalS,
    padding: `${tokens.spacingVerticalS} ${tokens.spacingHorizontalS} ${tokens.spacingVerticalS} ${tokens.spacingHorizontalL}`,
    color: '#FFFFFF',
    fontWeight: tokens.fontWeightBold,
    letterSpacing: '0.02em',
  },
  bandBajada: { backgroundImage: `linear-gradient(90deg, ${ROJO}, #F97066)` },
  bandDisponible: { backgroundImage: `linear-gradient(90deg, ${VERDE}, #32D583)` },
  bandNovedad: { backgroundImage: `linear-gradient(90deg, ${MORADO}, #9B8AFB)` },
  bandText: { flexGrow: 1 },
  close: {
    minWidth: 'auto',
    color: '#FFFFFF',
    ':hover': { color: '#FFFFFF', backgroundColor: 'rgba(255, 255, 255, 0.2)' },
    ':hover:active': { color: '#FFFFFF', backgroundColor: 'rgba(255, 255, 255, 0.3)' },
  },
  body: {
    display: 'grid',
    gridTemplateColumns: '104px 1fr',
    columnGap: tokens.spacingHorizontalL,
    alignItems: 'center',
    padding: tokens.spacingHorizontalL,
    [MOVIL]: { gridTemplateColumns: '80px 1fr' },
  },
  media: {
    position: 'relative',
    width: '104px',
    height: '104px',
    borderRadius: '14px',
    overflow: 'hidden',
    backgroundColor: tokens.colorNeutralBackground3,
    [MOVIL]: { width: '80px', height: '80px' },
  },
  image: { display: 'block', width: '100%', height: '100%', objectFit: 'cover' },
  placeholder: { display: 'flex', alignItems: 'center', justifyContent: 'center', width: '100%', height: '100%' },
  placeholderImage: { width: '32px', height: '32px' },
  discount: {
    position: 'absolute',
    top: '6px',
    left: '6px',
    padding: '2px 8px',
    borderRadius: tokens.borderRadiusCircular,
    backgroundColor: ROJO,
    color: '#FFFFFF',
    fontWeight: tokens.fontWeightBold,
    fontSize: tokens.fontSizeBase200,
    transform: 'rotate(-6deg)',
  },
  text: { display: 'flex', flexDirection: 'column', gap: '2px', minWidth: 0 },
  name: {
    color: tokens.colorNeutralForeground2,
    fontWeight: tokens.fontWeightSemibold,
    overflowWrap: 'anywhere',
  },
  // El porqué, en grande y con el color del tipo
  reason: {
    fontSize: tokens.fontSizeBase600,
    lineHeight: tokens.lineHeightBase600,
    fontWeight: tokens.fontWeightBold,
  },
  reasonBajada: { color: ROJO },
  reasonDisponible: { color: VERDE },
  reasonNovedad: { color: MORADO },
  prices: { display: 'flex', flexWrap: 'wrap', alignItems: 'baseline', columnGap: tokens.spacingHorizontalS },
  newPrice: { color: tokens.colorNeutralForeground1, fontSize: tokens.fontSizeBase400, fontWeight: tokens.fontWeightBold },
  oldPrice: { color: tokens.colorNeutralForeground3 },
  footer: {
    display: 'flex',
    flexWrap: 'wrap',
    alignItems: 'center',
    justifyContent: 'space-between',
    gap: tokens.spacingHorizontalM,
    padding: `${tokens.spacingVerticalM} ${tokens.spacingHorizontalL}`,
    borderTop: `1px solid ${tokens.colorNeutralStroke2}`,
    backgroundColor: '#FFF8F1',
  },
  more: { color: '#65432F', fontWeight: tokens.fontWeightSemibold },
  cta: {
    color: '#FFFFFF',
    backgroundColor: NARANJA,
    borderRadius: tokens.borderRadiusCircular,
    ':hover': { color: '#FFFFFF', backgroundColor: '#93370D' },
    ':hover:active': { color: '#FFFFFF', backgroundColor: '#7A2E0E' },
  },
})

// Cómo se cuenta cada tipo: la franja y el porqué en grande. PRODUCTO_NUEVO es de AB#530.
const TIPOS = {
  BAJADA_PRECIO: { etiqueta: '¡Bajada de precio!', icono: <ArrowTrendingDown20Filled />, color: 'Bajada' },
  DISPONIBILIDAD: { etiqueta: '¡De nuevo en stock!', icono: <BoxCheckmark20Filled />, color: 'Disponible' },
  PRODUCTO_NUEVO: { etiqueta: '¡Novedad!', icono: <Sparkle20Filled />, color: 'Novedad' },
}

function motivo(alerta) {
  if (esBajadaConPrecios(alerta)) {
    return `¡Baja un ${porcentajeRebaja(alerta.producto.precioAnterior, alerta.producto.precioNuevo)} %!`
  }
  if (alerta.tipo === 'BAJADA_PRECIO') return '¡Ha bajado de precio!'
  if (alerta.tipo === 'DISPONIBILIDAD') return '¡Vuelve a estar disponible!'
  return '¡Nuevo en tu categoría favorita!'
}

// Notificación de alertas sin leer justo al iniciar sesión (AB#529). Desde AB#531 sale abajo a la derecha y destaca la
// alerta sin leer más reciente: la foto del producto, el porqué en grande con el color de su tipo y, si es una rebaja,
// los precios; si hay más, lo dice. Mientras, la campanita de la cabecera suena. Se cierra con la ✕ o con Escape, y
// desaparece al navegar; después sigue avisando el número de la campanita.
export default function AlertsLoginNotice() {
  const styles = useStyles()
  const { alertas, sinLeer } = useAlerts()
  const toca = useAvisoTrasIniciarSesion()
  const [cerrado, setCerrado] = useState(false)
  const visible = toca && !cerrado

  useEffect(() => {
    if (!visible) return undefined
    const alPulsar = (evento) => {
      if (evento.key === 'Escape') setCerrado(true)
    }
    document.addEventListener('keydown', alPulsar)
    return () => document.removeEventListener('keydown', alPulsar)
  }, [visible])

  // La lista viene de la más reciente a la más antigua
  const alerta = alertas.find((candidata) => !candidata.leida)
  if (!visible || !alerta) return null

  const tipo = TIPOS[alerta.tipo] ?? TIPOS.PRODUCTO_NUEVO
  const { producto } = alerta
  const esBajada = esBajadaConPrecios(alerta)
  const mas = sinLeer - 1

  return (
    <div role="status" aria-label="Notificación de alertas" className={styles.notification}>
      <div className={mergeClasses(styles.band, styles[`band${tipo.color}`])}>
        <span aria-hidden="true">{tipo.icono}</span>
        <span className={styles.bandText}>{tipo.etiqueta}</span>
        <Button
          appearance="transparent"
          size="small"
          icon={<Dismiss20Regular />}
          aria-label="Cerrar el aviso de alertas"
          className={styles.close}
          onClick={() => setCerrado(true)}
        />
      </div>
      <div className={styles.body}>
        <div className={styles.media}>
          {producto.imagen ? (
            <img className={styles.image} src={producto.imagen} alt={producto.nombre} />
          ) : (
            <span className={styles.placeholder}>
              <img className={styles.placeholderImage} src={SIN_IMAGEN} alt="Sin imagen" />
            </span>
          )}
          {esBajada && (
            <span className={styles.discount} aria-hidden="true">
              {`-${porcentajeRebaja(producto.precioAnterior, producto.precioNuevo)}%`}
            </span>
          )}
        </div>
        <div className={styles.text}>
          <Text className={styles.name}>{producto.nombre}</Text>
          <Text className={mergeClasses(styles.reason, styles[`reason${tipo.color}`])}>{motivo(alerta)}</Text>
          {esBajada && (
            <span className={styles.prices}>
              <span className={styles.newPrice}>{formatPrice(producto.precioNuevo)}</span>
              <Text size={200} className={styles.oldPrice}>
                Antes <del>{formatPrice(producto.precioAnterior)}</del>
              </Text>
            </span>
          )}
        </div>
      </div>
      <div className={styles.footer}>
        <Text size={200} className={styles.more}>
          {mas > 0 ? `+${mas} ${mas === 1 ? 'alerta más' : 'alertas más'} sin leer` : textoSinLeer(sinLeer)}
        </Text>
        <LinkButton to={PATHS.ALERTS} className={styles.cta} icon={<ArrowRight16Regular />} iconPosition="after">
          Ver alertas
        </LinkButton>
      </div>
    </div>
  )
}
