import PropTypes from 'prop-types'
import { useCallback, useRef, useState } from 'react'
import {
  Button,
  MessageBar,
  MessageBarActions,
  MessageBarBody,
  Spinner,
  Text,
  Title1,
  makeStyles,
  mergeClasses,
  tokens,
} from '@fluentui/react-components'
import {
  Alert24Regular,
  ArrowRight16Regular,
  ArrowTrendingDown16Filled,
  BoxCheckmark16Filled,
  Checkmark16Regular,
  Sparkle16Filled,
} from '@fluentui/react-icons'
import { Link } from 'react-router-dom'
import { getAlerts, markAlertAsRead } from '../api/alerts'
import EmptyState from '../components/EmptyState'
import LinkButton from '../components/LinkButton'
import { usePageStyles } from '../components/pageStyles'
import { textoSinLeer } from '../features/alerts/alertsText'
import { useAlerts } from '../features/alerts/useAlerts'
import { useApiQuery } from '../hooks/useApiQuery'
import { PATHS, getProductDetailPath } from '../routes/paths'
import { formatDateTime, formatPrice } from '../utils/format'
import {
  MORADO,
  NARANJA,
  ROJO,
  SIN_IMAGEN,
  VERDE,
  esBajadaConPrecios,
  porcentajeRebaja,
} from '../features/alerts/alertPresentation'

const useStyles = makeStyles({
  list: {
    listStyle: 'none',
    margin: 0,
    padding: 0,
    display: 'flex',
    flexDirection: 'column',
    gap: tokens.spacingVerticalL,
  },
  card: {
    position: 'relative',
    display: 'grid',
    gridTemplateColumns: '112px 1fr auto',
    alignItems: 'center',
    columnGap: tokens.spacingHorizontalL,
    padding: tokens.spacingHorizontalM,
    overflow: 'hidden',
    backgroundColor: tokens.colorNeutralBackground1,
    border: `1px solid ${tokens.colorNeutralStroke2}`,
    borderLeftWidth: '6px',
    borderRadius: '16px',
    boxShadow: '0 2px 8px rgba(16, 24, 40, 0.06)',
    transitionProperty: 'transform, box-shadow',
    transitionDuration: tokens.durationNormal,
    ':hover': {
      transform: 'translateY(-2px)',
      boxShadow: '0 10px 24px rgba(16, 24, 40, 0.12)',
    },
    '@media (prefers-reduced-motion: reduce)': {
      transitionDuration: '0.01ms',
      ':hover': { transform: 'none' },
    },
    // En el móvil la llamada a la acción baja bajo el texto
    '@media (max-width: 600px)': {
      gridTemplateColumns: '88px 1fr',
      rowGap: tokens.spacingVerticalM,
    },
  },
  bajada: {
    borderLeftColor: ROJO,
    backgroundImage: 'linear-gradient(100deg, #FEF3F2 0%, #FFFFFF 45%)',
  },
  disponible: {
    borderLeftColor: VERDE,
    backgroundImage: 'linear-gradient(100deg, #ECFDF3 0%, #FFFFFF 45%)',
  },
  novedad: {
    borderLeftColor: MORADO,
    backgroundImage: 'linear-gradient(100deg, #F4F3FF 0%, #FFFFFF 45%)',
  },
  // Una alerta leída (AB#531) sigue en la lista, pero pierde el color: franja gris, sin degradado y con la foto
  // atenuada. El texto conserva su contraste.
  leida: {
    borderLeftColor: tokens.colorNeutralStroke1,
    backgroundImage: 'none',
    backgroundColor: tokens.colorNeutralBackground2,
    boxShadow: 'none',
  },
  mediaLeida: {
    opacity: 0.7,
    filter: 'grayscale(60%)',
  },
  tags: {
    display: 'flex',
    flexWrap: 'wrap',
    alignItems: 'center',
    gap: tokens.spacingHorizontalS,
  },
  // Etiqueta «Nueva» de las alertas sin leer (AB#531)
  newBadge: {
    display: 'inline-flex',
    alignItems: 'center',
    gap: '5px',
    padding: '2px 9px',
    borderRadius: tokens.borderRadiusCircular,
    border: `1.5px solid ${NARANJA}`,
    color: NARANJA,
    backgroundColor: '#FFF6ED',
    fontWeight: tokens.fontWeightBold,
    fontSize: tokens.fontSizeBase200,
    letterSpacing: '0.04em',
    textTransform: 'uppercase',
  },
  newDot: {
    width: '7px',
    height: '7px',
    borderRadius: tokens.borderRadiusCircular,
    backgroundColor: NARANJA,
  },
  markButton: {
    borderRadius: tokens.borderRadiusCircular,
    color: '#93370D',
    backgroundColor: '#FFFFFF',
    border: '1px solid #F4D1B3',
    ':hover': { color: '#7A2E0E', backgroundColor: '#FFF1E3', border: '1px solid #EFAE79' },
    ':hover:active': { color: '#7A2E0E', backgroundColor: '#FCE4CC' },
  },
  unreadCount: {
    color: NARANJA,
    fontWeight: tokens.fontWeightSemibold,
  },
  // Oculto a la vista, pero no a los lectores de pantalla
  srOnly: {
    position: 'absolute',
    width: '1px',
    height: '1px',
    margin: '-1px',
    padding: 0,
    overflow: 'hidden',
    clip: 'rect(0 0 0 0)',
    whiteSpace: 'nowrap',
    border: 0,
  },
  media: {
    position: 'relative',
    width: '112px',
    height: '112px',
    borderRadius: '12px',
    overflow: 'hidden',
    backgroundColor: tokens.colorNeutralBackground3,
    '@media (max-width: 600px)': { width: '88px', height: '88px' },
  },
  image: {
    display: 'block',
    width: '100%',
    height: '100%',
    objectFit: 'cover',
  },
  placeholder: {
    display: 'flex',
    alignItems: 'center',
    justifyContent: 'center',
    width: '100%',
    height: '100%',
  },
  placeholderImage: {
    width: '32px',
    height: '32px',
  },
  // El porcentaje de rebaja, como una pegatina sobre la foto
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
    boxShadow: '0 2px 6px rgba(217, 45, 32, 0.4)',
    transform: 'rotate(-6deg)',
  },
  text: {
    display: 'flex',
    flexDirection: 'column',
    alignItems: 'flex-start',
    gap: tokens.spacingVerticalXS,
    minWidth: 0,
  },
  chip: {
    display: 'inline-flex',
    alignItems: 'center',
    gap: '4px',
    padding: '3px 10px',
    borderRadius: tokens.borderRadiusCircular,
    color: '#FFFFFF',
    fontWeight: tokens.fontWeightBold,
    fontSize: tokens.fontSizeBase200,
    letterSpacing: '0.02em',
  },
  chipBajada: { backgroundImage: `linear-gradient(90deg, ${ROJO}, #F97066)` },
  chipDisponible: { backgroundImage: `linear-gradient(90deg, ${VERDE}, #32D583)` },
  chipNovedad: { backgroundImage: `linear-gradient(90deg, ${MORADO}, #9B8AFB)` },
  name: {
    fontSize: tokens.fontSizeBase400,
    fontWeight: tokens.fontWeightSemibold,
    color: tokens.colorNeutralForeground1,
    textDecorationLine: 'none',
    overflowWrap: 'anywhere',
    ':hover': { color: tokens.colorBrandForeground1, textDecorationLine: 'underline' },
  },
  prices: {
    display: 'flex',
    flexWrap: 'wrap',
    alignItems: 'baseline',
    columnGap: tokens.spacingHorizontalS,
  },
  newPrice: {
    color: ROJO,
    fontSize: tokens.fontSizeBase600,
    fontWeight: tokens.fontWeightBold,
    lineHeight: tokens.lineHeightBase600,
  },
  oldPrice: {
    color: tokens.colorNeutralForeground3,
  },
  savings: {
    color: VERDE,
    fontWeight: tokens.fontWeightSemibold,
  },
  stockMessage: {
    color: VERDE,
    fontWeight: tokens.fontWeightSemibold,
  },
  date: {
    color: tokens.colorNeutralForeground3,
  },
  aside: {
    display: 'flex',
    flexDirection: 'column',
    alignItems: 'flex-end',
    gap: tokens.spacingVerticalS,
    '@media (max-width: 600px)': {
      gridColumn: '1 / -1',
      flexDirection: 'row',
      alignItems: 'center',
      justifyContent: 'space-between',
    },
  },
  cta: {
    color: '#FFFFFF',
    backgroundColor: '#B54708',
    borderRadius: tokens.borderRadiusCircular,
    ':hover': { color: '#FFFFFF', backgroundColor: '#93370D' },
    ':hover:active': { color: '#FFFFFF', backgroundColor: '#7A2E0E' },
  },
})

// Cómo se presenta cada tipo de alerta. PRODUCTO_NUEVO es de AB#530 (categorías favoritas).
const TIPOS = {
  BAJADA_PRECIO: { etiqueta: '¡Bajada de precio!', icono: <ArrowTrendingDown16Filled />, tarjeta: 'bajada', chip: 'chipBajada' },
  DISPONIBILIDAD: { etiqueta: '¡De nuevo en stock!', icono: <BoxCheckmark16Filled />, tarjeta: 'disponible', chip: 'chipDisponible' },
  PRODUCTO_NUEVO: { etiqueta: '¡Novedad!', icono: <Sparkle16Filled />, tarjeta: 'novedad', chip: 'chipNovedad' },
}

// Una alerta como un cartel de tienda. Si es nueva (AB#531), conserva su color, lleva la etiqueta «Nueva» y el botón
// para marcarla como leída; si ya está leída, se queda en la lista sin color.
function AlertItem({ alerta, marcando, onMarcarLeida }) {
  const styles = useStyles()
  const verProducto = useRef(null)
  const tipo = TIPOS[alerta.tipo] ?? TIPOS.PRODUCTO_NUEVO
  const { producto, leida } = alerta
  const esBajada = esBajadaConPrecios(alerta)
  const detalle = getProductDetailPath(producto.id)

  // Al marcarla, el botón desaparece: el foco pasa a «Ver producto» de la misma alerta para no perderse
  const marcar = async () => {
    if (await onMarcarLeida(alerta)) verProducto.current?.focus()
  }

  return (
    <article className={mergeClasses(styles.card, styles[tipo.tarjeta], leida && styles.leida)}>
      <div className={mergeClasses(styles.media, leida && styles.mediaLeida)}>
        {producto.imagen ? (
          <img className={styles.image} src={producto.imagen} alt={producto.nombre} loading="lazy" />
        ) : (
          <span className={styles.placeholder}>
            <img className={styles.placeholderImage} src={SIN_IMAGEN} alt="Sin imagen" />
          </span>
        )}
        {esBajada && (
          <span className={styles.discount}>{`-${porcentajeRebaja(producto.precioAnterior, producto.precioNuevo)}%`}</span>
        )}
      </div>
      <div className={styles.text}>
        <span className={styles.tags}>
          {!leida && (
            <span className={styles.newBadge}>
              <span className={styles.newDot} aria-hidden="true" />
              Nueva
            </span>
          )}
          <span className={mergeClasses(styles.chip, styles[tipo.chip])}>
            <span aria-hidden="true">{tipo.icono}</span>
            {tipo.etiqueta}
          </span>
          {leida && <span className={styles.srOnly}>Leída</span>}
        </span>
        <Link to={detalle} className={styles.name}>
          {producto.nombre}
        </Link>
        {esBajada && (
          <>
            <span className={styles.prices}>
              <span className={styles.newPrice}>{formatPrice(producto.precioNuevo)}</span>
              <Text className={styles.oldPrice}>
                Antes <del>{formatPrice(producto.precioAnterior)}</del>
              </Text>
            </span>
            <Text size={200} className={styles.savings}>
              {`Ahorras ${formatPrice(producto.precioAnterior - producto.precioNuevo)}`}
            </Text>
          </>
        )}
        {alerta.tipo === 'DISPONIBILIDAD' && (
          <Text size={200} className={styles.stockMessage}>
            Ya puedes comprarlo otra vez. ¡Que no se te escape!
          </Text>
        )}
      </div>
      <div className={styles.aside}>
        <Text size={200} className={styles.date}>
          {formatDateTime(alerta.fecha)}
        </Text>
        {!leida && (
          <Button
            className={styles.markButton}
            icon={<Checkmark16Regular />}
            disabled={marcando}
            onClick={marcar}
            aria-label={`Marcar como leída la alerta de ${producto.nombre}`}
          >
            {marcando ? 'Marcando…' : 'Marcar como leída'}
          </Button>
        )}
        <LinkButton
          ref={verProducto}
          to={detalle}
          className={styles.cta}
          icon={<ArrowRight16Regular />}
          iconPosition="after"
          aria-label={`Ver ${producto.nombre}`}
        >
          Ver producto
        </LinkButton>
      </div>
    </article>
  )
}

AlertItem.propTypes = {
  alerta: PropTypes.shape({
    tipo: PropTypes.string.isRequired,
    fecha: PropTypes.string.isRequired,
    leida: PropTypes.bool.isRequired,
    producto: PropTypes.shape({
      id: PropTypes.string.isRequired,
      nombre: PropTypes.string,
      imagen: PropTypes.string,
      precioAnterior: PropTypes.number,
      precioNuevo: PropTypes.number,
    }).isRequired,
  }).isRequired,
  marcando: PropTypes.bool.isRequired,
  onMarcarLeida: PropTypes.func.isRequired,
}

const ERROR_AL_MARCAR = 'No se ha podido marcar la alerta como leída. Inténtalo de nuevo.'
const ALERTA_YA_NO_EXISTE = 'Esa alerta ya no existe. Hemos actualizado tu lista.'

// «Mis alertas» del cliente (AB#529): las bajadas de precio y las vueltas a estar disponibles de sus productos
// favoritos, de la más reciente a la más antigua, presentadas como los carteles de una tienda. No se envían por correo:
// se ven aquí. Desde AB#531 cada alerta nueva se puede marcar como leída: sigue en la lista, pero deja de ser nueva, y
// el contador de la cabecera se actualiza. Eliminarlas es de AB#532.
export default function AlertsPage() {
  const pageStyles = usePageStyles()
  const styles = useStyles()
  const fetcher = useCallback(() => getAlerts(), [])
  const { data, failed, retry } = useApiQuery(fetcher)
  const { recargar } = useAlerts()
  // Las que se marcan en esta visita, sin volver a pedir la lista; `marcando` es la que espera respuesta
  const [leidas, setLeidas] = useState(() => new Set())
  const [marcando, setMarcando] = useState(null)
  const [aviso, setAviso] = useState(null)
  const alertas = data?.alertas.map((alerta) => (leidas.has(alerta.id) ? { ...alerta, leida: true } : alerta))
  const sinLeer = alertas?.filter((alerta) => !alerta.leida).length ?? 0

  // Devuelve si se ha marcado, para que la alerta mueva el foco
  const marcarComoLeida = async (alerta) => {
    setMarcando(alerta.id)
    setAviso(null)
    try {
      const respuesta = await markAlertAsRead(alerta.id)
      setLeidas((anteriores) => new Set(anteriores).add(alerta.id))
      setAviso({ intent: 'success', texto: respuesta?.mensaje ?? 'Alerta marcada como leída' })
      recargar()
      return true
    } catch (error) {
      const yaNoExiste = error?.code === 'ALERT_NOT_FOUND'
      setAviso({ intent: 'error', texto: yaNoExiste ? ALERTA_YA_NO_EXISTE : ERROR_AL_MARCAR })
      if (yaNoExiste) {
        retry()
        recargar()
      }
      return false
    } finally {
      setMarcando(null)
    }
  }

  return (
    <main className={pageStyles.page}>
      <div className={pageStyles.heading}>
        <div className={pageStyles.titleBlock}>
          <Title1 as="h1" className={pageStyles.title}>
            Mis alertas
          </Title1>
          <Text className={pageStyles.subtitle}>Las rebajas y las vueltas al stock de tus productos favoritos.</Text>
          {sinLeer > 0 && <Text className={styles.unreadCount}>{textoSinLeer(sinLeer)}</Text>}
        </div>
      </div>
      {/* Región viva siempre presente: así se anuncia el resultado de marcar una alerta como leída */}
      <div aria-live="polite">
        {aviso && (
          <MessageBar intent={aviso.intent}>
            <MessageBarBody>{aviso.texto}</MessageBarBody>
          </MessageBar>
        )}
      </div>
      {failed && (
        <MessageBar intent="error">
          <MessageBarBody>No se han podido cargar tus alertas. Inténtalo de nuevo.</MessageBarBody>
          <MessageBarActions>
            <Button onClick={retry}>Reintentar</Button>
          </MessageBarActions>
        </MessageBar>
      )}
      {!alertas && !failed && <Spinner label="Cargando tus alertas…" className={pageStyles.loading} />}
      {alertas?.length === 0 && (
        <EmptyState
          icon={<Alert24Regular />}
          title="No tienes alertas"
          description="Te avisaremos aquí cuando baje el precio de un producto favorito o vuelva a estar disponible."
        >
          <LinkButton to={PATHS.FAVORITES} appearance="primary">
            Ver mis favoritos
          </LinkButton>
        </EmptyState>
      )}
      {alertas?.length > 0 && (
        <ul aria-label="Tus alertas" className={styles.list}>
          {alertas.map((alerta) => (
            <li key={alerta.id}>
              <AlertItem alerta={alerta} marcando={marcando === alerta.id} onMarcarLeida={marcarComoLeida} />
            </li>
          ))}
        </ul>
      )}
    </main>
  )
}
