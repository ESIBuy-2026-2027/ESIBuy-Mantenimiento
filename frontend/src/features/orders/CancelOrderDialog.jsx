import PropTypes from 'prop-types'
import {
  Button,
  Dialog,
  DialogActions,
  DialogBody,
  DialogContent,
  DialogSurface,
  DialogTitle,
} from '@fluentui/react-components'

// Confirmación para cancelar un pedido (HU12), como la de eliminar una dirección (DeleteAddressDialog).
export default function CancelOrderDialog({ pedido, onClose }) {
  return (
    <Dialog open modalType="alert" onOpenChange={(_event, data) => !data.open && onClose()}>
      <DialogSurface>
        <DialogBody>
          <DialogTitle>Cancelar pedido</DialogTitle>
          <DialogContent>
            ¿Seguro que quieres cancelar el pedido {pedido.numeroPedido}? No se puede deshacer.
          </DialogContent>
          <DialogActions>
            <Button appearance="secondary" onClick={onClose}>
              Volver
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
}
