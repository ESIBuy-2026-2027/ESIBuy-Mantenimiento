import { useCallback, useState } from 'react'
import {
  Badge,
  Button,
  Card,
  MessageBar,
  MessageBarBody,
  Spinner,
  Table,
  TableBody,
  TableCell,
  TableHeader,
  TableHeaderCell,
  TableRow,
  Text,
  Title1,
  Title3,
  makeStyles,
  tokens,
} from '@fluentui/react-components'
import { useParams } from 'react-router-dom'
import { advanceSellerOrder, getSellerOrder } from '../api/orders'
import LinkButton from '../components/LinkButton'
import { usePageStyles } from '../components/pageStyles'
import { resumenDeDireccion } from '../features/addresses/direccionPropType'
import { CANCELADO, PASOS_DEL_ENVIO, colorDeEstado, etiquetaDeEstado } from '../features/orders/orderStatus'
import { useApiQuery } from '../hooks/useApiQuery'
import { PATHS } from '../routes/paths'
import { formatDateTime, formatPrice } from '../utils/format'

const useStyles = makeStyles({
  section: {
    display: 'flex',
    flexDirection: 'column',
    gap: tokens.spacingVerticalM,
  },
  headingActions: {
    display: 'flex',
    flexWrap: 'wrap',
    alignItems: 'center',
    gap: tokens.spacingHorizontalM,
  },
  number: { textAlign: 'right' },
  muted: { color: tokens.colorNeutralForeground3 },
})

const SIGUIENTE_ESTADO = {
  PAGADO: 'PREPARANDO',
  PREPARANDO: 'ENVIADO',
  ENVIADO: 'ENTREGADO',
}

export default function SellerOrderDetailPage() {
  const pageStyles = usePageStyles()
  const styles = useStyles()
  const { id } = useParams()
  const fetcher = useCallback(() => getSellerOrder(id), [id])
  const { data: pedidoCargado, error, failed, retry } = useApiQuery(fetcher)
  const [pedidoActualizado, setPedidoActualizado] = useState(null)
  const [avanzando, setAvanzando] = useState(false)
  const [aviso, setAviso] = useState(null)
  const pedido = pedidoActualizado ?? pedidoCargado
  const noEncontrado = error?.code === 'ORDER_NOT_FOUND'

  const avanzar = async () => {
    setAvanzando(true)
    setAviso(null)
    try {
      setPedidoActualizado(await advanceSellerOrder(id))
      setAviso({ intent: 'success', texto: 'Estado del pedido actualizado.' })
    } catch (caught) {
      setAviso({
        intent: 'warning',
        texto:
          caught?.code === 'ORDER_STATUS_NOT_ADVANCEABLE'
            ? 'Este pedido ya no admite más cambios de estado.'
            : 'No se ha podido actualizar el estado del pedido.',
      })
      retry()
    } finally {
      setAvanzando(false)
    }
  }

  return (
    <main className={pageStyles.page}>
      {aviso && (
        <MessageBar intent={aviso.intent}>
          <MessageBarBody>{aviso.texto}</MessageBarBody>
        </MessageBar>
      )}
      {noEncontrado && (
        <MessageBar intent="warning">
          <MessageBarBody>Pedido no encontrado.</MessageBarBody>
        </MessageBar>
      )}
      {failed && !noEncontrado && (
        <MessageBar intent="error">
          <MessageBarBody>No se ha podido cargar el pedido. Inténtalo de nuevo.</MessageBarBody>
        </MessageBar>
      )}
      {!pedido && !failed && <Spinner label="Cargando pedido…" className={pageStyles.loading} />}

      {pedido && (
        <>
          <div className={pageStyles.heading}>
            <div className={pageStyles.titleBlock}>
              <Title1 as="h1" className={pageStyles.title}>Pedido {pedido.numeroPedido}</Title1>
              <Text className={pageStyles.subtitle}>{formatDateTime(pedido.createdAt)}</Text>
            </div>
            <div className={styles.headingActions}>
              <Badge appearance="tint" size="large" color={colorDeEstado(pedido.estado)}>
                {etiquetaDeEstado(pedido.estado)}
              </Badge>
              {SIGUIENTE_ESTADO[pedido.estado] && (
                <Button appearance="primary" onClick={avanzar} disabled={avanzando}>
                  Marcar como {etiquetaDeEstado(SIGUIENTE_ESTADO[pedido.estado]).toLocaleLowerCase('es')}
                </Button>
              )}
            </div>
          </div>

          <Card className={styles.section}>
            <Title3 as="h2">Cliente y envío</Title3>
            <Text weight="semibold">{pedido.direccionEnvio.destinatario}</Text>
            <Text>{resumenDeDireccion(pedido.direccionEnvio)}</Text>
            <Text className={styles.muted}>{pedido.direccionEnvio.telefono}</Text>
          </Card>

          <Card className={styles.section}>
            <Title3 as="h2">Productos</Title3>
            <Table aria-label="Productos del pedido">
              <TableHeader>
                <TableRow>
                  <TableHeaderCell>Producto</TableHeaderCell>
                  <TableHeaderCell className={styles.number}>Cantidad</TableHeaderCell>
                  <TableHeaderCell className={styles.number}>Precio unitario</TableHeaderCell>
                  <TableHeaderCell className={styles.number}>Subtotal</TableHeaderCell>
                </TableRow>
              </TableHeader>
              <TableBody>
                {pedido.lineas.map((linea) => (
                  <TableRow key={linea.productoId}>
                    <TableCell>{linea.nombre}</TableCell>
                    <TableCell className={styles.number}>{linea.cantidad}</TableCell>
                    <TableCell className={styles.number}>{formatPrice(linea.precioUnitario)}</TableCell>
                    <TableCell className={styles.number}>{formatPrice(linea.subtotal)}</TableCell>
                  </TableRow>
                ))}
                <TableRow>
                  <TableCell><Text weight="semibold">Total</Text></TableCell>
                  <TableCell />
                  <TableCell />
                  <TableCell className={styles.number}>
                    <Text weight="semibold">{formatPrice(pedido.total)}</Text>
                  </TableCell>
                </TableRow>
              </TableBody>
            </Table>
          </Card>

          {pedido.estado === CANCELADO && (
            <MessageBar intent="warning">
              <MessageBarBody>Este pedido está cancelado y no admite cambios de estado.</MessageBarBody>
            </MessageBar>
          )}
          {PASOS_DEL_ENVIO.includes(pedido.estado) && (
            <Card className={styles.section}>
              <Title3 as="h2">Estado del envío</Title3>
              <Text>{etiquetaDeEstado(pedido.estado)}</Text>
            </Card>
          )}
        </>
      )}

      <div>
        <LinkButton to={PATHS.SELLER_ORDERS} appearance="secondary">
          Volver a mis pedidos
        </LinkButton>
      </div>
    </main>
  )
}
