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
import { deleteAccount } from '../../api/profile'
import { PasswordField } from './PasswordField'

const useStyles = makeStyles({
  content: {
    display: 'flex',
    flexDirection: 'column',
    gap: tokens.spacingVerticalM,
  },
  consequences: {
    margin: 0,
    paddingLeft: tokens.spacingHorizontalXL,
  },
})

// Mensajes propios según el code del backend: nunca su texto ni la contraseña enviada
const MENSAJE_CONTRASENA = 'La contraseña actual no es correcta.'
const MENSAJE_FALLO = 'No se ha podido eliminar la cuenta. Inténtalo de nuevo.'
const mensajeDeFallo = (error) => (error?.code === 'WRONG_PASSWORD' ? MENSAJE_CONTRASENA : MENSAJE_FALLO)

// Confirmación para eliminar la cuenta propia del vendedor (HU47), con la contraseña actual. Las consecuencias son
// las del backend (PR #203): los productos con pedidos se conservan desactivados para no perder la trazabilidad.
export default function DeleteAccountDialog({ onClose, onDeleted }) {
  const styles = useStyles()
  const [password, setPassword] = useState('')
  const [eliminando, setEliminando] = useState(false)
  const [fallo, setFallo] = useState(null)

  const confirmar = async () => {
    setEliminando(true)
    setFallo(null)
    try {
      await deleteAccount(password)
      onDeleted()
    } catch (error) {
      setFallo(mensajeDeFallo(error))
      setEliminando(false)
    }
  }

  return (
    <Dialog open modalType="alert" onOpenChange={(_event, data) => !data.open && !eliminando && onClose()}>
      <DialogSurface>
        <DialogBody>
          <DialogTitle>Eliminar mi cuenta</DialogTitle>
          <DialogContent className={styles.content}>
            {fallo && (
              <MessageBar intent="error">
                <MessageBarBody>{fallo}</MessageBarBody>
              </MessageBar>
            )}
            <span>Esta acción no se puede deshacer. Al eliminar tu cuenta:</span>
            <ul className={styles.consequences}>
              <li>Los productos que no tienen pedidos se eliminan.</li>
              <li>Los productos con pedidos se desactivan y se conservan, para no perder el historial.</li>
              <li>Los pedidos y las valoraciones se conservan.</li>
              <li>Se cerrará la sesión y no podrás volver a entrar con esta cuenta.</li>
            </ul>
            <PasswordField
              label="Contraseña actual"
              value={password}
              onChange={(_event, data) => setPassword(data.value)}
              autoComplete="current-password"
            />
          </DialogContent>
          <DialogActions>
            <Button appearance="secondary" onClick={onClose} disabled={eliminando}>
              Cancelar
            </Button>
            <Button appearance="primary" onClick={confirmar} disabled={eliminando || !password}>
              Eliminar mi cuenta
            </Button>
          </DialogActions>
        </DialogBody>
      </DialogSurface>
    </Dialog>
  )
}

DeleteAccountDialog.propTypes = {
  onClose: PropTypes.func.isRequired,
  onDeleted: PropTypes.func.isRequired,
}
