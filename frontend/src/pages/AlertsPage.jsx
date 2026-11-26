import PropTypes from 'prop-types'
import { useCallback } from 'react'
import {
  Button,
  Card,
  MessageBar,
  MessageBarActions,
  MessageBarBody,
  Spinner,
  Text,
  Title1,
  makeStyles,
  tokens,
} from '@fluentui/react-components'
import {
  Alert24Regular,
  ArrowTrendingDown20Regular,
  BoxCheckmark20Regular,
  Sparkle20Regular,
} from '@fluentui/react-icons'
import { Link } from 'react-router-dom'
import { getAlerts } from '../api/alerts'
import EmptyState from '../components/EmptyState'
import LinkButton from '../components/LinkButton'
import { usePageStyles } from '../components/pageStyles'
import { useApiQuery } from '../hooks/useApiQuery'
import { PATHS, getProductDetailPath } from '../routes/paths'
import { formatDateTime, formatPrice } from '../utils/format'

const useStyles = makeStyles({
  list: {
    listStyle: 'none',
    margin: 0,
    padding: 0,
    display: 'flex',
    flexDirection: 'column',
    gap: tokens.spacingVerticalM,
  },
  // En pantallas estrechas la fecha baja a una segunda línea
  item: {
    display: 'flex',
    flexDirection: 'row',
    flexWrap: 'wrap',
    alignItems: 'center',
    gap: tokens.spacingHorizontalL,
  },
  icon: {
    display: 'flex',
    alignItems: 'center',
    justifyContent: 'center',
    flexShrink: 0,
    width: '40px',
    height: '40px',
    borderRadius: tokens.borderRadiusCircular,
    backgroundColor: tokens.colorBrandBackground2,
    color: tokens.colorBrandForeground1,
  },
  text: {
    display: 'flex',
    flexDirection: 'column',
    alignItems: 'flex-start',
    gap: tokens.spacingVerticalXXS,
    flexGrow: 1,
    minWidth: 0,
  },
  name: {
    fontWeight: tokens.fontWeightSemibold,
    color: tokens.colorBrandForeground1,
    overflowWrap: 'anywhere',
  },
  prices: {
    display: 'flex',
    flexWrap: 'wrap',
    alignItems: 'baseline',
    gap: tokens.spacingHorizontalS,
  },
  muted: {
    color: tokens.colorNeutralForeground3,
  },
  date: {
    marginLeft: 'auto',
    color: tokens.colorNeutralForeground3,
  },
})

// Cómo se presenta cada tipo de alerta. PRODUCTO_NUEVO es de AB#530 (categorías favoritas).
const TIPOS = {
  BAJADA_PRECIO: { titulo: 'Ha bajado de precio', icono: <ArrowTrendingDown20Regular /> },
  DISPONIBILIDAD: { titulo: 'Vuelve a estar disponible', icono: <BoxCheckmark20Regular /> },
  PRODUCTO_NUEVO: { titulo: 'Producto nuevo en una de tus categorías favoritas', icono: <Sparkle20Regular /> },
}

function AlertItem({ alerta }) {
  const styles = useStyles()
  const { titulo, icono } = TIPOS[alerta.tipo] ?? { titulo: 'Alerta', icono: <Alert24Regular /> }
  const { producto } = alerta
  const conPrecios = alerta.tipo === 'BAJADA_PRECIO' && producto.precioAnterior != null && producto.precioNuevo != null

  return (
    <Card className={styles.item}>
      <span className={styles.icon} aria-hidden="true">
        {icono}
      </span>
      <div className={styles.text}>
        <Text weight="semibold">{titulo}</Text>
        <Link to={getProductDetailPath(producto.id)} className={styles.name}>
          {producto.nombre}
        </Link>
        {conPrecios && (
          <span className={styles.prices}>
            <Text size={200} className={styles.muted}>
              Antes <del>{formatPrice(producto.precioAnterior)}</del>
            </Text>
            <Text weight="semibold">Ahora {formatPrice(producto.precioNuevo)}</Text>
          </span>
        )}
      </div>
      <Text size={200} className={styles.date}>
        {formatDateTime(alerta.fecha)}
      </Text>
    </Card>
  )
}

AlertItem.propTypes = {
  alerta: PropTypes.shape({
    tipo: PropTypes.string.isRequired,
    fecha: PropTypes.string.isRequired,
    producto: PropTypes.shape({
      id: PropTypes.string.isRequired,
      nombre: PropTypes.string,
      precioAnterior: PropTypes.number,
      precioNuevo: PropTypes.number,
    }).isRequired,
  }).isRequired,
}

// «Mis alertas» del cliente (AB#529): las bajadas de precio y las vueltas a estar disponibles de sus productos
// favoritos, de la más reciente a la más antigua. No se envían por correo: se ven aquí. Será la bandeja de AB#531
// (marcar como leídas) y AB#532 (eliminarlas).
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
          <Text className={pageStyles.subtitle}>Los cambios de precio y disponibilidad de tus productos favoritos.</Text>
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
