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
} from '@fluentui/react-components'
import { deletePaymentMethod } from '../../api/paymentMethods'
import { nombreDeMetodo } from './tiposDeMetodo'

// Confirmación para eliminar un método de pago (HU6), como DeleteProductDialog.
export default function DeletePaymentMethodDialog({ metodo, onClose, onDeleted }) {
  const [eliminando, setEliminando] = useState(false)

  const confirmar = async () => {
    setEliminando(true)
    await deletePaymentMethod(metodo.id)
    onDeleted()
  }

  return (
    <Dialog open modalType="alert" onOpenChange={(_event, data) => !data.open && !eliminando && onClose()}>
      <DialogSurface>
        <DialogBody>
          <DialogTitle>Eliminar método de pago</DialogTitle>
          <DialogContent>¿Seguro que quieres eliminar «{nombreDeMetodo(metodo)}»?</DialogContent>
          <DialogActions>
            <Button appearance="secondary" onClick={onClose} disabled={eliminando}>
              Cancelar
            </Button>
            <Button appearance="primary" onClick={confirmar} disabled={eliminando}>
              Eliminar
            </Button>
          </DialogActions>
        </DialogBody>
      </DialogSurface>
    </Dialog>
  )
}

const metodoPropType = PropTypes.shape({
  id: PropTypes.string.isRequired,
  tipo: PropTypes.string.isRequired,
  mascara: PropTypes.string.isRequired,
  predeterminado: PropTypes.bool.isRequired,
})

DeletePaymentMethodDialog.propTypes = {
  metodo: metodoPropType.isRequired,
  metodos: PropTypes.arrayOf(metodoPropType).isRequired,
  onClose: PropTypes.func.isRequired,
  onDeleted: PropTypes.func.isRequired,
}
