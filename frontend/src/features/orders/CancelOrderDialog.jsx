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
import { cancelOrder } from '../../api/orders'

const useStyles = makeStyles({
  content: {
    display: 'flex',
    flexDirection: 'column',
    gap: tokens.spacingVerticalM,
  },
})

// Confirmación para cancelar un pedido (HU12), como la de eliminar una dirección (DeleteAddressDialog). Si sale bien
// entrega el pedido ya cancelado; si el pedido ya no se puede cancelar (409, p. ej. se acaba de enviar) lo comunica
// a la página, que vuelve a cargarlo; cualquier otro fallo se avisa aquí para poder reintentar.
export default function CancelOrderDialog({ pedido, onClose, onCancelled, onNotCancellable }) {
  const styles = useStyles()
  const [cancelando, setCancelando] = useState(false)
  const [fallo, setFallo] = useState(false)

  const confirmar = async () => {
    setCancelando(true)
    setFallo(false)
    try {
      onCancelled(await cancelOrder(pedido.id))
    } catch (error) {
      if (error?.code === 'ORDER_NOT_CANCELLABLE') {
        onNotCancellable()
        return
      }
      setFallo(true)
      setCancelando(false)
    }
  }

  return (
    <Dialog open modalType="alert" onOpenChange={(_event, data) => !data.open && !cancelando && onClose()}>
      <DialogSurface>
        <DialogBody>
          <DialogTitle>Cancelar pedido</DialogTitle>
          <DialogContent className={styles.content}>
            {fallo && (
              <MessageBar intent="error">
                <MessageBarBody>No se ha podido cancelar el pedido. Inténtalo de nuevo.</MessageBarBody>
              </MessageBar>
            )}
            <span>¿Seguro que quieres cancelar el pedido {pedido.numeroPedido}? No se puede deshacer.</span>
          </DialogContent>
          <DialogActions>
            <Button appearance="secondary" onClick={onClose} disabled={cancelando}>
              Volver
            </Button>
            <Button appearance="primary" onClick={confirmar} disabled={cancelando}>
              Sí, cancelar el pedido
            </Button>
          </DialogActions>
        </DialogBody>
      </DialogSurface>
    </Dialog>
  )
}

CancelOrderDialog.propTypes = {
  pedido: PropTypes.shape({
    id: PropTypes.string.isRequired,
    numeroPedido: PropTypes.string.isRequired,
  }).isRequired,
  onClose: PropTypes.func.isRequired,
  onCancelled: PropTypes.func.isRequired,
  onNotCancellable: PropTypes.func.isRequired,
}
