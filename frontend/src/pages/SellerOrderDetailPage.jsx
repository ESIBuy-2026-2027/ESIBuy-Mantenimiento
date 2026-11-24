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
  mergeClasses,
  tokens,
} from '@fluentui/react-components'
import { useParams } from 'react-router-dom'
import {
  advanceSellerOrder,
  cancelSellerOrder,
  getSellerOrder,
  revertSellerOrder,
} from '../api/orders'
import LinkButton from '../components/LinkButton'
import { usePageStyles } from '../components/pageStyles'
import { resumenDeDireccion } from '../features/addresses/direccionPropType'
import {
  CANCELADO,
  PASOS_DEL_ENVIO,
  colorDeEstado,
  etiquetaDeEstado,
} from '../features/orders/orderStatus'
import { useApiQuery } from '../hooks/useApiQuery'
import { PATHS } from '../routes/paths'
import { formatDateTime, formatPrice } from '../utils/format'

const SIGUIENTE_ESTADO_VENDEDOR = {
  PAGADO: 'PREPARANDO',
  PREPARANDO: 'ENVIADO',
}

const ACCION_AVANZAR_VENDEDOR = {
  PAGADO: 'Preparar pedido',
  PREPARANDO: 'Marcar como enviado',
}

const ESTADOS_CANCELABLES_VENDEDOR = new Set(['PAGADO', 'PREPARANDO'])
const ESTADOS_RETROCEDIBLES_VENDEDOR = new Set(['PREPARANDO'])

const fallbackImage = (nombre = '') => {
  const inicial = nombre.trim().charAt(0).toUpperCase() || 'P'
  const svg =
    `<svg xmlns="http://www.w3.org/2000/svg" width="96" height="96" viewBox="0 0 96 96">` +
    `<rect width="96" height="96" rx="8" fill="#FFF4EA"/>` +
    `<text x="48" y="61" text-anchor="middle" font-family="Segoe UI, Arial" font-size="42" ` +
    `font-weight="700" fill="#EA580C">${inicial}</text></svg>`
  return `data:image/svg+xml,${encodeURIComponent(svg)}`
}

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
  steps: {
    display: 'grid',
    gridTemplateColumns: 'repeat(auto-fit, minmax(110px, 1fr))',
    gap: tokens.spacingHorizontalS,
  },
  step: {
    textAlign: 'center',
    fontSize: tokens.fontSizeBase200,
    lineHeight: tokens.lineHeightBase200,
    paddingTop: tokens.spacingVerticalXS,
    paddingBottom: tokens.spacingVerticalXS,
    paddingLeft: tokens.spacingHorizontalS,
    paddingRight: tokens.spacingHorizontalS,
    borderRadius: tokens.borderRadiusMedium,
    backgroundColor: tokens.colorNeutralBackground3,
    color: tokens.colorNeutralForeground3,
  },
  stepDone: {
    backgroundColor: tokens.colorPaletteGreenBackground1,
    color: tokens.colorPaletteGreenForeground1,
    fontWeight: tokens.fontWeightSemibold,
  },
  stepCurrent: {
    backgroundColor: '#EA580C',
    color: '#ffffff',
    fontWeight: tokens.fontWeightSemibold,
  },
  stepCurrentDone: {
    backgroundColor: tokens.colorPaletteGreenBackground3,
    color: '#ffffff',
    fontWeight: tokens.fontWeightSemibold,
  },
  grid2: {
    display: 'grid',
    gridTemplateColumns: 'repeat(auto-fit, minmax(220px, 1fr))',
    gap: tokens.spacingHorizontalL,
  },
  fieldBlock: {
    display: 'flex',
    flexDirection: 'column',
    gap: tokens.spacingVerticalXXS,
  },
  lbl: {
    fontSize: tokens.fontSizeBase200,
    fontWeight: tokens.fontWeightSemibold,
    color: tokens.colorNeutralForeground3,
  },
  productCell: {
    display: 'flex',
    alignItems: 'center',
    gap: tokens.spacingHorizontalM,
  },
  thumbnail: {
    width: '48px',
    height: '48px',
    borderRadius: tokens.borderRadiusMedium,
    objectFit: 'cover',
    flexShrink: 0,
  },
  thRight: {
    textAlign: 'right',
    '& > button, & > div': {
      justifyContent: 'flex-end',
      width: '100%',
    },
  },
  tdRight: {
    textAlign: 'right',
  },
  muted: {
    color: tokens.colorNeutralForeground3,
  },
})

export default function SellerOrderDetailPage() {
  const pageStyles = usePageStyles()
  const styles = useStyles()
  const { id } = useParams()
  const fetcher = useCallback(() => getSellerOrder(id), [id])
  const { data: pedidoCargado, error, failed, retry } = useApiQuery(fetcher)
  const [pedidoActualizado, setPedidoActualizado] = useState(null)
  const [ocupado, setOcupado] = useState(false)
  const [aviso, setAviso] = useState(null)

  const pedido = pedidoActualizado ?? pedidoCargado
  const noEncontrado = error?.code === 'ORDER_NOT_FOUND'

  const ejecutarAccion = async (accion, mensajeExito, codigoNoPermitido, mensajeNoPermitido) => {
    setOcupado(true)
    setAviso(null)
    try {
      setPedidoActualizado(await accion(id))
      setAviso({ intent: 'success', texto: mensajeExito })
    } catch (caught) {
      setAviso({
        intent: 'warning',
        texto:
          caught?.code === codigoNoPermitido
            ? mensajeNoPermitido
            : 'No se ha podido actualizar el estado del pedido.',
      })
      retry()
    } finally {
      setOcupado(false)
    }
  }

  const avanzar = () =>
    ejecutarAccion(
      advanceSellerOrder,
      'Estado del pedido actualizado.',
      'ORDER_STATUS_NOT_ADVANCEABLE',
      'Este pedido ya no admite más cambios de estado desde el vendedor.'
    )

  const retroceder = () =>
    ejecutarAccion(
      revertSellerOrder,
      'Estado del pedido retrocedido.',
      'ORDER_STATUS_NOT_ADVANCEABLE',
      'Solo se puede retroceder un pedido en preparación.'
    )

  const cancelar = () =>
    ejecutarAccion(
      cancelSellerOrder,
      'Pedido cancelado y existencias repuestas.',
      'ORDER_NOT_CANCELLABLE',
      'El pedido ya no se puede cancelar.'
    )

  const pasoActualIdx = pedido ? PASOS_DEL_ENVIO.indexOf(pedido.estado) : -1
  const puedeAvanzar = pedido && Boolean(SIGUIENTE_ESTADO_VENDEDOR[pedido.estado])
  const puedeRetroceder = pedido && ESTADOS_RETROCEDIBLES_VENDEDOR.has(pedido.estado)
  const puedeCancelar = pedido && ESTADOS_CANCELABLES_VENDEDOR.has(pedido.estado)

  return (
    <main className={pageStyles.page}>
      <div>
        <LinkButton to={PATHS.SELLER_ORDERS} appearance="secondary">
          ← Volver a mis pedidos
        </LinkButton>
      </div>
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
              <Title1 as="h1" className={pageStyles.title}>
                Pedido {pedido.numeroPedido}
              </Title1>
              <Text className={pageStyles.subtitle}>{formatDateTime(pedido.createdAt)}</Text>
            </div>
            <div className={styles.headingActions}>
              <Badge appearance="tint" size="large" color={colorDeEstado(pedido.estado)}>
                {etiquetaDeEstado(pedido.estado)}
              </Badge>
              {puedeRetroceder && (
                <Button appearance="secondary" onClick={retroceder} disabled={ocupado}>
                  Retroceder estado
                </Button>
              )}
              {puedeCancelar && (
                <Button appearance="secondary" onClick={cancelar} disabled={ocupado}>
                  Cancelar pedido
                </Button>
              )}
              {puedeAvanzar && (
                <Button appearance="primary" onClick={avanzar} disabled={ocupado}>
                  {ACCION_AVANZAR_VENDEDOR[pedido.estado]}
                </Button>
              )}
            </div>
          </div>

          {PASOS_DEL_ENVIO.includes(pedido.estado) && (
            <Card className={styles.section}>
              <Title3 as="h2">Estado del envío</Title3>
              <div className={styles.steps}>
                {PASOS_DEL_ENVIO.map((paso, idx) => (
                  <div
                    key={paso}
                    className={mergeClasses(
                      styles.step,
                      idx < pasoActualIdx && styles.stepDone,
                      idx === pasoActualIdx &&
                        (pedido.estado === 'ENTREGADO' ? styles.stepCurrentDone : styles.stepCurrent)
                    )}
                  >
                    {etiquetaDeEstado(paso)}
                  </div>
                ))}
              </div>
            </Card>
          )}

          {pedido.estado === CANCELADO && (
            <MessageBar intent="warning">
              <MessageBarBody>
                Este pedido está cancelado y no admite cambios de estado.
              </MessageBarBody>
            </MessageBar>
          )}

          <Card className={styles.section}>
            <Title3 as="h2">Cliente y envío</Title3>
            <div className={styles.grid2}>
              <div className={styles.fieldBlock}>
                <span className={styles.lbl}>Destinatario</span>
                <Text weight="semibold">{pedido.direccionEnvio.destinatario}</Text>
                <Text className={styles.muted}>{pedido.direccionEnvio.telefono}</Text>
              </div>
              <div className={styles.fieldBlock}>
                <span className={styles.lbl}>Dirección de entrega</span>
                <Text>{resumenDeDireccion(pedido.direccionEnvio)}</Text>
              </div>
            </div>
          </Card>

          <Card className={styles.section}>
            <Title3 as="h2">Productos</Title3>
            <Table aria-label="Productos del pedido">
              <TableHeader>
                <TableRow>
                  <TableHeaderCell>Producto</TableHeaderCell>
                  <TableHeaderCell className={styles.thRight}>Cantidad</TableHeaderCell>
                  <TableHeaderCell className={styles.thRight}>Precio unitario</TableHeaderCell>
                  <TableHeaderCell className={styles.thRight}>Subtotal</TableHeaderCell>
                </TableRow>
              </TableHeader>
              <TableBody>
                {pedido.lineas.map((linea) => (
                  <TableRow key={linea.productoId}>
                    <TableCell>
                      <div className={styles.productCell}>
                        <img
                          className={styles.thumbnail}
                          src={linea.imagen || fallbackImage(linea.nombre)}
                          alt={`Miniatura de ${linea.nombre}`}
                          onError={(event) => {
                            event.currentTarget.onerror = null
                            event.currentTarget.src = fallbackImage(linea.nombre)
                          }}
                        />
                        <Text>{linea.nombre}</Text>
                      </div>
                    </TableCell>
                    <TableCell className={styles.tdRight}>{linea.cantidad}</TableCell>
                    <TableCell className={styles.tdRight}>
                      {formatPrice(linea.precioUnitario)}
                    </TableCell>
                    <TableCell className={styles.tdRight}>{formatPrice(linea.subtotal)}</TableCell>
                  </TableRow>
                ))}
                <TableRow>
                  <TableCell>
                    <Text weight="semibold">Total</Text>
                  </TableCell>
                  <TableCell />
                  <TableCell />
                  <TableCell className={styles.tdRight}>
                    <Text weight="semibold">{formatPrice(pedido.total)}</Text>
                  </TableCell>
                </TableRow>
              </TableBody>
            </Table>
          </Card>
        </>
      )}
    </main>
  )
}