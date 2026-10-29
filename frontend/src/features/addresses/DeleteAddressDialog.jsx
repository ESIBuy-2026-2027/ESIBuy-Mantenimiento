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
  MessageBar,
  MessageBarBody,
  Select,
  makeStyles,
  tokens,
} from '@fluentui/react-components'
import { deleteAddress } from '../../api/addresses'
import { direccionPropType, resumenDeDireccion } from './direccionPropType'

const useStyles = makeStyles({
  content: {
    display: 'flex',
    flexDirection: 'column',
    gap: tokens.spacingVerticalM,
  },
})

// Confirmación para eliminar una dirección de envío (HU5), como DeletePaymentMethodDialog. Si es la principal y quedan
// otras, el cliente elige cuál pasa a serlo (el backend lo exige).
export default function DeleteAddressDialog({ direccion, direcciones, onClose, onDeleted }) {
  const styles = useStyles()
  const [eliminando, setEliminando] = useState(false)
  const [sustituta, setSustituta] = useState('')
  const [fallo, setFallo] = useState(false)
  const otras = direcciones.filter(({ id }) => id !== direccion.id)
  const eligeSustituta = direccion.principal && otras.length > 0

  const confirmar = async () => {
    setEliminando(true)
    setFallo(false)
    try {
      await deleteAddress(direccion.id, eligeSustituta ? sustituta : undefined)
      onDeleted()
    } catch {
      setFallo(true)
      setEliminando(false)
    }
  }

  return (
    <Dialog open modalType="alert" onOpenChange={(_event, data) => !data.open && !eliminando && onClose()}>
      <DialogSurface>
        <DialogBody>
          <DialogTitle>Eliminar dirección</DialogTitle>
          <DialogContent className={styles.content}>
            {fallo && (
              <MessageBar intent="error">
                <MessageBarBody>No se ha podido eliminar la dirección. Inténtalo de nuevo.</MessageBarBody>
              </MessageBar>
            )}
            <span>¿Seguro que quieres eliminar «{resumenDeDireccion(direccion)}»?</span>
            {eligeSustituta && (
              <Field label="Nueva dirección principal" hint="Es tu dirección principal: elige cuál la sustituye.">
                <Select value={sustituta} onChange={(_event, data) => setSustituta(data.value)}>
                  <option value="" disabled>
                    Elige una dirección
                  </option>
                  {otras.map((otra) => (
                    <option key={otra.id} value={otra.id}>
                      {resumenDeDireccion(otra)}
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
            <Button appearance="primary" onClick={confirmar} disabled={eliminando || (eligeSustituta && !sustituta)}>
              Eliminar
            </Button>
          </DialogActions>
        </DialogBody>
      </DialogSurface>
    </Dialog>
  )
}

DeleteAddressDialog.propTypes = {
  direccion: direccionPropType.isRequired,
  direcciones: PropTypes.arrayOf(direccionPropType).isRequired,
  onClose: PropTypes.func.isRequired,
  onDeleted: PropTypes.func.isRequired,
}
