import PropTypes from 'prop-types'
import { Dialog, DialogBody, DialogSurface, DialogTitle } from '@fluentui/react-components'

// Confirmación para eliminar la cuenta propia del vendedor (HU47)
export default function DeleteAccountDialog({ onClose }) {
  return (
    <Dialog open modalType="alert" onOpenChange={(_event, data) => !data.open && onClose()}>
      <DialogSurface>
        <DialogBody>
          <DialogTitle>Eliminar mi cuenta</DialogTitle>
        </DialogBody>
      </DialogSurface>
    </Dialog>
  )
}

DeleteAccountDialog.propTypes = {
  onClose: PropTypes.func.isRequired,
  onDeleted: PropTypes.func.isRequired,
}
