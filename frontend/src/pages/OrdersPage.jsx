import { useCallback } from 'react'
import {
  Badge,
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
import { Receipt24Regular } from '@fluentui/react-icons'
import { Link } from 'react-router-dom'
import { getOrders } from '../api/orders'
import EmptyState from '../components/EmptyState'
import LinkButton from '../components/LinkButton'
import { usePageStyles } from '../components/pageStyles'
import { colorDeEstado, etiquetaDeEstado } from '../features/orders/orderStatus'
import { useApiQuery } from '../hooks/useApiQuery'
import { PATHS, getOrderDetailPath } from '../routes/paths'
import { formatDate, formatPrice } from '../utils/format'

const useStyles = makeStyles({
  list: {
    listStyle: 'none',
    margin: 0,
    padding: 0,
    display: 'flex',
    flexDirection: 'column',
    gap: tokens.spacingVerticalM,
  },
  // En pantallas estrechas el total y el estado bajan a una segunda línea
  order: {
    display: 'flex',
    flexDirection: 'row',
    flexWrap: 'wrap',
    alignItems: 'center',
    gap: tokens.spacingHorizontalL,
  },
  orderText: {
    display: 'flex',
    flexDirection: 'column',
    flexGrow: 1,
    minWidth: 0,
  },
  number: {
    fontWeight: tokens.fontWeightSemibold,
    color: tokens.colorBrandForeground1,
  },
  muted: {
    color: tokens.colorNeutralForeground3,
  },
  summary: {
    display: 'flex',
    alignItems: 'center',
    gap: tokens.spacingHorizontalM,
    marginLeft: 'auto',
  },
})

// Historial de pedidos del cliente en sesión (HU11), del más reciente al más antiguo (así llega del backend).
// Cada pedido lleva a su detalle, con los productos y el estado del envío.
export default function OrdersPage() {
  const pageStyles = usePageStyles()
  const styles = useStyles()
  const fetcher = useCallback(() => getOrders(), [])
  const { data: pedidos, failed, retry } = useApiQuery(fetcher)

  return (
    <main className={pageStyles.page}>
      <div className={pageStyles.heading}>
        <div className={pageStyles.titleBlock}>
          <Title1 as="h1" className={pageStyles.title}>
            Mis pedidos
          </Title1>
          <Text className={pageStyles.subtitle}>Consulta tus compras y en qué punto está cada envío.</Text>
        </div>
      </div>
      {failed && (
        <MessageBar intent="error">
          <MessageBarBody>No se han podido cargar tus pedidos. Inténtalo de nuevo.</MessageBarBody>
          <MessageBarActions>
            <Button onClick={retry}>Reintentar</Button>
          </MessageBarActions>
        </MessageBar>
      )}
      {!pedidos && !failed && <Spinner label="Cargando pedidos…" className={pageStyles.loading} />}
      {pedidos?.length === 0 && (
        <EmptyState
          icon={<Receipt24Regular />}
          title="Aún no has hecho ningún pedido"
          description="Cuando compres algo, lo verás aquí con el estado de su envío."
        >
          <LinkButton to={PATHS.PRODUCTS} appearance="primary">
            Ver los productos
          </LinkButton>
        </EmptyState>
      )}
      {pedidos?.length > 0 && (
        <ul aria-label="Tus pedidos" className={styles.list}>
          {pedidos.map((pedido) => (
            <li key={pedido.id}>
              <Card className={styles.order}>
                <div className={styles.orderText}>
                  <Link to={getOrderDetailPath(pedido.id)} className={styles.number}>
                    {pedido.numeroPedido}
                  </Link>
                  <Text className={styles.muted}>
                    {formatDate(pedido.createdAt)} · {pedido.vendedor.nombreComercial}
                  </Text>
                </div>
                <div className={styles.summary}>
                  <Text weight="semibold">{formatPrice(pedido.total)}</Text>
                  <Badge appearance="tint" color={colorDeEstado(pedido.estado)}>
                    {etiquetaDeEstado(pedido.estado)}
                  </Badge>
                </div>
              </Card>
            </li>
          ))}
        </ul>
      )}
    </main>
  )
}
