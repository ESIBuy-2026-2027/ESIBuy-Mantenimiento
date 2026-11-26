import PropTypes from 'prop-types'
import { useCallback } from 'react'
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
  Sparkle16Filled,
} from '@fluentui/react-icons'
import { Link } from 'react-router-dom'
import { getAlerts } from '../api/alerts'
import EmptyState from '../components/EmptyState'
import LinkButton from '../components/LinkButton'
import { usePageStyles } from '../components/pageStyles'
import { useApiQuery } from '../hooks/useApiQuery'
import { PATHS, getProductDetailPath } from '../routes/paths'
import { formatDateTime, formatPrice } from '../utils/format'

// Colores de cada tipo de alerta, como los carteles de una tienda: rojo para las rebajas, verde para el stock y morado
// para las novedades (AB#530)
const ROJO = '#D92D20'
const VERDE = '#079455'
const MORADO = '#7A5AF8'

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

const SIN_IMAGEN =
  "data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 24 24'%3E%3Cpath d='M3 3l18 18M10 12l-4 6h12l-3-4-2 2M6 6h11a2 2 0 0 1 2 2v8' fill='none' stroke='%23999999' stroke-width='1.5' stroke-linecap='round' stroke-linejoin='round'/%3E%3C/svg%3E"

// Porcentaje de rebaja, redondeado: de 59,90 € a 53,91 € es un 10 %
const porcentajeRebaja = (anterior, nuevo) => Math.round((1 - nuevo / anterior) * 100)

function AlertItem({ alerta }) {
  const styles = useStyles()
  const tipo = TIPOS[alerta.tipo] ?? TIPOS.PRODUCTO_NUEVO
  const { producto } = alerta
  const esBajada =
    alerta.tipo === 'BAJADA_PRECIO' && producto.precioAnterior != null && producto.precioNuevo != null
  const detalle = getProductDetailPath(producto.id)

  return (
    <article className={mergeClasses(styles.card, styles[tipo.tarjeta])}>
      <div className={styles.media}>
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
        <span className={mergeClasses(styles.chip, styles[tipo.chip])}>
          <span aria-hidden="true">{tipo.icono}</span>
          {tipo.etiqueta}
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
        <LinkButton
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
    producto: PropTypes.shape({
      id: PropTypes.string.isRequired,
      nombre: PropTypes.string,
      imagen: PropTypes.string,
      precioAnterior: PropTypes.number,
      precioNuevo: PropTypes.number,
    }).isRequired,
  }).isRequired,
}

// «Mis alertas» del cliente (AB#529): las bajadas de precio y las vueltas a estar disponibles de sus productos
// favoritos, de la más reciente a la más antigua, presentadas como los carteles de una tienda. No se envían por correo:
// se ven aquí. Será la bandeja de AB#531 (marcar como leídas) y AB#532 (eliminarlas).
export default function AlertsPage() {
  const pageStyles = usePageStyles()
  const styles = useStyles()
  const fetcher = useCallback(() => getAlerts(), [])
  const { data, failed, retry } = useApiQuery(fetcher)
  const alertas = data?.alertas

  return (
    <main className={pageStyles.page}>
      <div className={pageStyles.heading}>
        <div className={pageStyles.titleBlock}>
          <Title1 as="h1" className={pageStyles.title}>
            Mis alertas
          </Title1>
          <Text className={pageStyles.subtitle}>Las rebajas y las vueltas al stock de tus productos favoritos.</Text>
        </div>
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
              <AlertItem alerta={alerta} />
            </li>
          ))}
        </ul>
      )}
    </main>
  )
}
