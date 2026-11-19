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
import { getSellerOrders } from '../api/orders'
import EmptyState from '../components/EmptyState'
import { usePageStyles } from '../components/pageStyles'
import { colorDeEstado, etiquetaDeEstado } from '../features/orders/orderStatus'
import { useApiQuery } from '../hooks/useApiQuery'
import { getSellerOrderDetailPath } from '../routes/paths'
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
  order: {
    display: 'flex',
    flexWrap: 'wrap',
    alignItems: 'center',
    gap: tokens.spacingHorizontalL,
  },
  orderText: {
    display: 'flex',
    flexDirection: 'column',
    flexGrow: 1,
  },
  number: {
    fontWeight: tokens.fontWeightSemibold,
  },
  muted: {
    color: tokens.colorNeutralForeground3,
  },
})

export default function SellerOrdersPage() {
  const styles = usePageStyles()
  const localStyles = useStyles()
  const fetcher = useCallback(() => getSellerOrders(), [])
  const { data: pedidos, failed, retry } = useApiQuery(fetcher)

  return (
    <main className={styles.page}>
      <div className={styles.heading}>
        <div className={styles.titleBlock}>
          <Title1 as="h1" className={styles.title}>
            Mis pedidos
          </Title1>
          <Text className={styles.subtitle}>Consulta los pedidos de tus productos.</Text>
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
      {!pedidos && !failed && <Spinner label="Cargando pedidos…" className={styles.loading} />}
      {pedidos?.length === 0 && (
        <EmptyState
          icon={<Receipt24Regular />}
          title="Todavía no tienes pedidos"
          description="Los pedidos que incluyan tus productos aparecerán aquí."
        />
      )}
      {pedidos?.length > 0 && (
        <ul aria-label="Pedidos de tus productos" className={localStyles.list}>
          {pedidos.map((pedido) => (
            <li key={pedido.id}>
              <Card className={localStyles.order}>
                <div className={localStyles.orderText}>
                  <Link to={getSellerOrderDetailPath(pedido.id)} className={localStyles.number}>
                    {pedido.numeroPedido}
                  </Link>
                  <Text className={localStyles.muted}>
                    {pedido.direccionEnvio.destinatario} · {formatDateTime(pedido.createdAt)}
                  </Text>
                </div>
                <Text weight="semibold">{formatPrice(pedido.total)}</Text>
                <Badge appearance="tint" color={colorDeEstado(pedido.estado)}>
                  {etiquetaDeEstado(pedido.estado)}
                </Badge>
              </Card>
            </li>
          ))}
        </ul>
      )}
    </main>
  )
}
