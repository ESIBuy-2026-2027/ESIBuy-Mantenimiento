import PropTypes from 'prop-types'
import { useState } from 'react'
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
  Spinner,
} from '@fluentui/react-components'
import { deleteSellerProduct } from '../../api/products'

export default function DeleteProductDialog({ product, onClose, onDeleted }) {
  const [deleting, setDeleting] = useState(false)
  const [failed, setFailed] = useState(false)

  const confirm = async () => {
    setDeleting(true)
    setFailed(false)
    try {
      await deleteSellerProduct(product.id)
      onDeleted()
    } catch {
      setFailed(true)
      setDeleting(false)
    }
  }

  return (
    <Dialog open modalType="alert" onOpenChange={(_event, data) => !data.open && !deleting && onClose()}>
      <DialogSurface>
        <DialogBody>
          <DialogTitle>Eliminar producto</DialogTitle>
          <DialogContent>
            {failed && (
              <MessageBar intent="error">
                <MessageBarBody>No se ha podido eliminar el producto. Inténtalo de nuevo.</MessageBarBody>
              </MessageBar>
            )}
            ¿Seguro que quieres eliminar «{product.nombre}»? Esta acción no se puede deshacer.
          </DialogContent>
          <DialogActions>
            <Button appearance="secondary" onClick={onClose} disabled={deleting}>
              Cancelar
            </Button>
            <Button
              appearance="primary"
              onClick={confirm}
              disabled={deleting}
              icon={deleting ? <Spinner size="tiny" /> : undefined}
            >
              Eliminar
            </Button>
          </DialogActions>
        </DialogBody>
      </DialogSurface>
    </Dialog>
  )
}

DeleteProductDialog.propTypes = {
  product: PropTypes.shape({
    id: PropTypes.string.isRequired,
    nombre: PropTypes.string.isRequired,
  }).isRequired,
  onClose: PropTypes.func.isRequired,
  onDeleted: PropTypes.func.isRequired,
}
