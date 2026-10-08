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

// Ventana de confirmación antes de cambiar el estado de un usuario (activar o desactivar).
export default function StatusConfirmDialog({ titulo, pregunta, confirmar, enviando, onConfirm, onCancel }) {
  return (
    <Dialog open modalType="alert" onOpenChange={(_event, data) => !data.open && onCancel()}>
      <DialogSurface>
        <DialogBody>
          <DialogTitle>{titulo}</DialogTitle>
          <DialogContent>{pregunta}</DialogContent>
          <DialogActions>
            <Button onClick={onCancel} disabled={enviando}>
              Cancelar
            </Button>
            <Button appearance="primary" onClick={onConfirm} disabled={enviando}>
              {confirmar}
            </Button>
          </DialogActions>
        </DialogBody>
      </DialogSurface>
    </Dialog>
  )
}

StatusConfirmDialog.propTypes = {
  titulo: PropTypes.string.isRequired,
  pregunta: PropTypes.string.isRequired,
  confirmar: PropTypes.string.isRequired,
  enviando: PropTypes.bool.isRequired,
  onConfirm: PropTypes.func.isRequired,
  onCancel: PropTypes.func.isRequired,
}
