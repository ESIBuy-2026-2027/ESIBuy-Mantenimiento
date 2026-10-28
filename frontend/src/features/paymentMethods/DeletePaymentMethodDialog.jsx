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
  Field,
  Select,
  makeStyles,
  tokens,
} from '@fluentui/react-components'
import { deletePaymentMethod } from '../../api/paymentMethods'
import { nombreDeMetodo } from './tiposDeMetodo'

const useStyles = makeStyles({
  content: {
    display: 'flex',
    flexDirection: 'column',
    gap: tokens.spacingVerticalM,
  },
})

// Confirmación para eliminar un método de pago (HU6), como DeleteProductDialog. Si es el predeterminado y quedan
// otros, el cliente elige cuál pasa a serlo (el backend lo exige).
export default function DeletePaymentMethodDialog({ metodo, metodos, onClose, onDeleted }) {
  const styles = useStyles()
  const [eliminando, setEliminando] = useState(false)
  const [sustituto, setSustituto] = useState('')
  const otros = metodos.filter(({ id }) => id !== metodo.id)
  const eligeSustituto = metodo.predeterminado && otros.length > 0

  const confirmar = async () => {
    setEliminando(true)
    await deletePaymentMethod(metodo.id, eligeSustituto ? sustituto : undefined)
    onDeleted()
  }

  return (
    <Dialog open modalType="alert" onOpenChange={(_event, data) => !data.open && !eliminando && onClose()}>
      <DialogSurface>
        <DialogBody>
          <DialogTitle>Eliminar método de pago</DialogTitle>
          <DialogContent className={styles.content}>
            <span>¿Seguro que quieres eliminar «{nombreDeMetodo(metodo)}»?</span>
            {eligeSustituto && (
              <Field label="Nuevo método predeterminado" hint="Es el método predeterminado: elige cuál lo sustituye.">
                <Select value={sustituto} onChange={(_event, data) => setSustituto(data.value)}>
                  <option value="" disabled>
                    Elige un método
                  </option>
                  {otros.map((otro) => (
                    <option key={otro.id} value={otro.id}>
                      {nombreDeMetodo(otro)}
                    </option>
                  ))}
                </Select>
              </Field>
            )}
          </DialogContent>
          <DialogActions>
            <Button appearance="secondary" onClick={onClose} disabled={eliminando}>
              Cancelar
            </Button>
            <Button appearance="primary" onClick={confirmar} disabled={eliminando || (eligeSustituto && !sustituto)}>
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
