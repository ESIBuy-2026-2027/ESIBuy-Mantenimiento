import { useState } from 'react'
import PropTypes from 'prop-types'
import {
  Button,
  Dialog,
  DialogActions,
  DialogBody,
  DialogContent,
  DialogSurface,
  DialogTitle,
  MessageBar,
  MessageBarBody,
  makeStyles,
  tokens,
} from '@fluentui/react-components'
import { confirmDelivery } from '../../api/orders'

const useStyles = makeStyles({
  content: {
    display: 'flex',
    flexDirection: 'column',
    gap: tokens.spacingVerticalM,
  },
})

// Confirmación de que el cliente ha recibido el pedido (PBI 540), como la de cancelarlo (CancelOrderDialog). Si sale
// bien entrega el seguimiento ya entregado; si el pedido ya no se puede marcar como entregado (409, p. ej. ya estaba
// entregado) lo comunica a la sección, que vuelve a cargarlo; cualquier otro fallo se avisa aquí para poder reintentar.
export default function ConfirmDeliveryDialog({ pedido, onClose, onConfirmed, onNotDeliverable }) {
  const styles = useStyles()
  const [confirmando, setConfirmando] = useState(false)
  const [fallo, setFallo] = useState(false)

  const confirmar = async () => {
    setConfirmando(true)
    setFallo(false)
    try {
      onConfirmed(await confirmDelivery(pedido.id))
    } catch (error) {
      if (error?.code === 'ORDER_NOT_DELIVERABLE') {
        onNotDeliverable()
        return
      }
      setFallo(true)
      setConfirmando(false)
    }
  }

  return (
    <Dialog open modalType="alert" onOpenChange={(_event, data) => !data.open && !confirmando && onClose()}>
      <DialogSurface>
        <DialogBody>
          <DialogTitle>Confirmar recepción</DialogTitle>
          <DialogContent className={styles.content}>
            {fallo && (
              <MessageBar intent="error">
                <MessageBarBody>No se ha podido confirmar la recepción. Inténtalo de nuevo.</MessageBarBody>
              </MessageBar>
            )}
            <span>
              ¿Confirmas que has recibido el pedido {pedido.numeroPedido}? Pasará a «Entregado» y no se puede
              deshacer.
            </span>
          </DialogContent>
          <DialogActions>
            <Button appearance="secondary" onClick={onClose} disabled={confirmando}>
              Volver
            </Button>
            <Button appearance="primary" onClick={confirmar} disabled={confirmando}>
              Sí, he recibido el pedido
            </Button>
          </DialogActions>
        </DialogBody>
      </DialogSurface>
    </Dialog>
  )
}

ConfirmDeliveryDialog.propTypes = {
  pedido: PropTypes.shape({
    id: PropTypes.string.isRequired,
    numeroPedido: PropTypes.string.isRequired,
  }).isRequired,
  onClose: PropTypes.func.isRequired,
  onConfirmed: PropTypes.func.isRequired,
  onNotDeliverable: PropTypes.func.isRequired,
}
