import { useCallback, useState } from 'react'
import PropTypes from 'prop-types'
import {
  Button,
  Card,
  MessageBar,
  MessageBarActions,
  MessageBarBody,
  Spinner,
  Text,
  Title3,
  makeStyles,
  tokens,
} from '@fluentui/react-components'
import { getOrderTracking } from '../../api/orders'
import { useApiQuery } from '../../hooks/useApiQuery'
import ConfirmDeliveryDialog from './ConfirmDeliveryDialog'
import { puedeConfirmarEntrega } from './orderStatus'
import ShipmentTimeline from './ShipmentTimeline'

const useStyles = makeStyles({
  section: {
    display: 'flex',
    flexDirection: 'column',
    gap: tokens.spacingVerticalM,
  },
  sectionTitle: {
    margin: 0,
  },
  actions: {
    display: 'flex',
    flexWrap: 'wrap',
    alignItems: 'center',
    gap: tokens.spacingHorizontalM,
  },
  muted: {
    color: tokens.colorNeutralForeground3,
  },
})

// Sección «Estado del envío» del detalle de un pedido (PBI 540): pide el seguimiento (estado, fechas e histórico),
// lo muestra en la línea de tiempo y, mientras el envío está en «enviado», deja al cliente confirmar que lo ha recibido.
// Tiene sus propios estados de carga y error: el resto del detalle no depende de ella. `onDelivered` avisa a la página
// de que el pedido ya está entregado; `onStale` de que su estado ha cambiado por otro lado y hay que recargarlo.
export default function ShipmentTrackingSection({ pedido, onDelivered, onStale }) {
  const styles = useStyles()
  const fetcher = useCallback(() => getOrderTracking(pedido.id), [pedido.id])
  const { data, failed, retry } = useApiQuery(fetcher)
  const [entregado, setEntregado] = useState(null)
  const [confirmando, setConfirmando] = useState(false)
  const [aviso, setAviso] = useState(null)
  const seguimiento = entregado ?? data

  const alConfirmar = (nuevo) => {
    setConfirmando(false)
    setEntregado(nuevo)
    setAviso({ intent: 'success', texto: 'Recepción confirmada. El pedido figura como entregado.' })
    onDelivered()
  }

  // 409: ya no estaba enviado (p. ej. ya se había entregado). Se recarga para mostrar su estado real.
  const alNoPoderEntregar = () => {
    setConfirmando(false)
    setAviso({ intent: 'warning', texto: 'Este pedido ya no se puede marcar como entregado.' })
    setEntregado(null)
    retry()
    onStale()
  }

  return (
    <Card className={styles.section}>
      <Title3 as="h2" className={styles.sectionTitle}>
        Estado del envío
      </Title3>
      {aviso && (
        <MessageBar intent={aviso.intent}>
          <MessageBarBody>{aviso.texto}</MessageBarBody>
        </MessageBar>
      )}
      {failed && !seguimiento && (
        <MessageBar intent="error">
          <MessageBarBody>No se ha podido cargar el seguimiento del envío. Inténtalo de nuevo.</MessageBarBody>
          <MessageBarActions>
            <Button onClick={retry}>Reintentar</Button>
          </MessageBarActions>
        </MessageBar>
      )}
      {!seguimiento && !failed && <Spinner label="Cargando seguimiento…" />}
      {seguimiento && (
        <ShipmentTimeline
          estado={seguimiento.estado}
          historial={seguimiento.historial}
          fechaEstimadaEntrega={seguimiento.fechaEstimadaEntrega}
          fechaEntrega={seguimiento.fechaEntrega}
        />
      )}
      {seguimiento && puedeConfirmarEntrega(seguimiento.estado) && (
        <div className={styles.actions}>
          <Button appearance="primary" onClick={() => setConfirmando(true)}>
            Confirmar recepción
          </Button>
          <Text className={styles.muted}>Confírmalo cuando hayas recibido tu pedido.</Text>
        </div>
      )}
      {confirmando && (
        <ConfirmDeliveryDialog
          pedido={pedido}
          onClose={() => setConfirmando(false)}
          onConfirmed={alConfirmar}
          onNotDeliverable={alNoPoderEntregar}
        />
      )}
    </Card>
  )
}

ShipmentTrackingSection.propTypes = {
  pedido: PropTypes.shape({
    id: PropTypes.string.isRequired,
    numeroPedido: PropTypes.string.isRequired,
  }).isRequired,
  onDelivered: PropTypes.func.isRequired,
  onStale: PropTypes.func.isRequired,
}
