import { useCallback, useState } from 'react'
import PropTypes from 'prop-types'
import {
  Badge,
  Button,
  Card,
  MessageBar,
  MessageBarActions,
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
import { CheckmarkCircle20Filled, Circle20Regular, RecordStop20Regular } from '@fluentui/react-icons'
import { useParams } from 'react-router-dom'
import { getOrder } from '../api/orders'
import LinkButton from '../components/LinkButton'
import { usePageStyles } from '../components/pageStyles'
import { resumenDeDireccion } from '../features/addresses/direccionPropType'
import CancelOrderDialog from '../features/orders/CancelOrderDialog'
import {
  CANCELADO,
  PASOS_DEL_ENVIO,
  colorDeEstado,
  esCancelable,
  etiquetaDeEstado,
} from '../features/orders/orderStatus'
import { useApiQuery } from '../hooks/useApiQuery'
import { PATHS } from '../routes/paths'
import { formatDate, formatPrice } from '../utils/format'

const useStyles = makeStyles({
  section: {
    display: 'flex',
    flexDirection: 'column',
    gap: tokens.spacingVerticalM,
  },
  sectionTitle: {
    margin: 0,
  },
  steps: {
    listStyle: 'none',
    margin: 0,
    padding: 0,
    display: 'flex',
    flexWrap: 'wrap',
    gap: tokens.spacingHorizontalXL,
  },
  step: {
    display: 'flex',
    alignItems: 'center',
    gap: tokens.spacingHorizontalXS,
  },
  done: {
    color: tokens.colorPaletteGreenForeground1,
  },
  current: {
    color: tokens.colorBrandForeground1,
    fontWeight: tokens.fontWeightSemibold,
  },
  pending: {
    color: tokens.colorNeutralForeground3,
  },
  number: {
    textAlign: 'right',
  },
  muted: {
    color: tokens.colorNeutralForeground3,
  },
  headingActions: {
    display: 'flex',
    flexWrap: 'wrap',
    alignItems: 'center',
    gap: tokens.spacingHorizontalM,
  },
})

// Cómo está cada paso del envío respecto al estado actual del pedido
function estadoDelPaso(indice, indiceActual) {
  if (indice < indiceActual) return 'completado'
  if (indice === indiceActual) return 'estado actual'
  return 'pendiente'
}

// Línea de estados del envío (HU11): los pasos ya hechos, el actual y los que faltan.
function SeguimientoDelEnvio({ estado }) {
  const styles = useStyles()
  const indiceActual = PASOS_DEL_ENVIO.indexOf(estado)
  const estilos = { completado: styles.done, 'estado actual': styles.current, pendiente: styles.pending }
  const iconos = {
    completado: <CheckmarkCircle20Filled />,
    'estado actual': <RecordStop20Regular />,
    pendiente: <Circle20Regular />,
  }

  return (
    <ol aria-label="Estado del envío" className={styles.steps}>
      {PASOS_DEL_ENVIO.map((paso, indice) => {
        const situacion = estadoDelPaso(indice, indiceActual)
        const etiqueta = etiquetaDeEstado(paso)
        return (
          <li
            key={paso}
            aria-label={`${etiqueta}: ${situacion}`}
            aria-current={situacion === 'estado actual' ? 'step' : undefined}
            className={`${styles.step} ${estilos[situacion]}`}
          >
            {iconos[situacion]}
            <Text>{etiqueta}</Text>
          </li>
        )
      })}
    </ol>
  )
}

SeguimientoDelEnvio.propTypes = {
  estado: PropTypes.string.isRequired,
}

function DetalleDelPedido({ pedido, onCancelar }) {
  const pageStyles = usePageStyles()
  const styles = useStyles()

  return (
    <>
      <div className={pageStyles.heading}>
        <div className={pageStyles.titleBlock}>
          <Title1 as="h1" className={pageStyles.title}>
            Pedido {pedido.numeroPedido}
          </Title1>
          <Text className={pageStyles.subtitle}>
            {formatDate(pedido.createdAt)} · {pedido.vendedor.nombreComercial}
          </Text>
        </div>
        <div className={styles.headingActions}>
          <Badge appearance="tint" size="large" color={colorDeEstado(pedido.estado)}>
            {etiquetaDeEstado(pedido.estado)}
          </Badge>
          {/* HU12: solo mientras el pedido aún no se ha enviado */}
          {esCancelable(pedido.estado) && (
            <Button appearance="secondary" onClick={onCancelar}>
              Cancelar pedido
            </Button>
          )}
        </div>
      </div>

      <Card className={styles.section}>
        <Title3 as="h2" className={styles.sectionTitle}>
          Estado del envío
        </Title3>
        {pedido.estado === CANCELADO ? (
          <MessageBar intent="warning">
            <MessageBarBody>Este pedido está cancelado: no se enviará.</MessageBarBody>
          </MessageBar>
        ) : (
          <SeguimientoDelEnvio estado={pedido.estado} />
        )}
      </Card>

      <Card className={styles.section}>
        <Title3 as="h2" className={styles.sectionTitle}>
          Productos
        </Title3>
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
              <TableCell>
                <Text weight="semibold">Total</Text>
              </TableCell>
              <TableCell />
              <TableCell />
              <TableCell className={styles.number}>
                <Text weight="semibold">{formatPrice(pedido.total)}</Text>
              </TableCell>
            </TableRow>
          </TableBody>
        </Table>
      </Card>

      <Card className={styles.section} role="region" aria-label="Dirección de envío">
        <Title3 as="h2" className={styles.sectionTitle}>
          Dirección de envío
        </Title3>
        <Text weight="semibold">{pedido.direccionEnvio.destinatario}</Text>
        <Text>{resumenDeDireccion(pedido.direccionEnvio)}</Text>
        <Text className={styles.muted}>{pedido.direccionEnvio.telefono}</Text>
      </Card>
    </>
  )
}

DetalleDelPedido.propTypes = {
  onCancelar: PropTypes.func.isRequired,
  pedido: PropTypes.shape({
    numeroPedido: PropTypes.string.isRequired,
    createdAt: PropTypes.string.isRequired,
    vendedor: PropTypes.shape({ nombreComercial: PropTypes.string.isRequired }).isRequired,
    estado: PropTypes.string.isRequired,
    total: PropTypes.number.isRequired,
    lineas: PropTypes.arrayOf(
      PropTypes.shape({
        productoId: PropTypes.string.isRequired,
        nombre: PropTypes.string.isRequired,
        cantidad: PropTypes.number.isRequired,
        precioUnitario: PropTypes.number.isRequired,
        subtotal: PropTypes.number.isRequired,
      }),
    ).isRequired,
    direccionEnvio: PropTypes.shape({
      destinatario: PropTypes.string.isRequired,
      telefono: PropTypes.string,
    }).isRequired,
  }).isRequired,
}

// Detalle de un pedido del cliente (HU11): productos con el desglose de precios, estado del envío y dirección.
// El backend responde 400 ORDER_NOT_FOUND si el pedido no existe o es de otro cliente: las dos cosas se ven igual.
export default function OrderDetailPage() {
  const pageStyles = usePageStyles()
  const { id } = useParams()
  const fetcher = useCallback(() => getOrder(id), [id])
  const { data: pedido, error, failed, retry } = useApiQuery(fetcher)
  const [confirmando, setConfirmando] = useState(false)
  const noEncontrado = error?.code === 'ORDER_NOT_FOUND'

  return (
    <main className={pageStyles.page}>
      {noEncontrado && (
        <MessageBar intent="warning">
          <MessageBarBody>Pedido no encontrado.</MessageBarBody>
        </MessageBar>
      )}
      {failed && !noEncontrado && (
        <MessageBar intent="error">
          <MessageBarBody>No se ha podido cargar el pedido. Inténtalo de nuevo.</MessageBarBody>
          <MessageBarActions>
            <Button onClick={retry}>Reintentar</Button>
          </MessageBarActions>
        </MessageBar>
      )}
      {!pedido && !failed && <Spinner label="Cargando pedido…" className={pageStyles.loading} />}
      {pedido && <DetalleDelPedido pedido={pedido} onCancelar={() => setConfirmando(true)} />}
      {pedido && confirmando && <CancelOrderDialog pedido={pedido} onClose={() => setConfirmando(false)} />}
      <div>
        <LinkButton to={PATHS.ORDERS} appearance="secondary">
          Volver a mis pedidos
        </LinkButton>
      </div>
    </main>
  )
}
