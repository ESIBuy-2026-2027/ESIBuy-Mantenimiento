import { useEffect, useRef, useState } from 'react'
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

// Consecuencias de eliminar la cuenta de cada rol, las que hace el backend: el vendedor (HU47, PR #203) conserva
// los productos con pedidos desactivados; el cliente (HU25, PR #235) pierde todo lo que depende de él y conserva
// los pedidos y las valoraciones para las estadísticas y los históricos.
const CIERRE_DE_SESION = 'Se cerrará la sesión y no podrás volver a entrar con esta cuenta.'
const CONSECUENCIAS = {
  VENDEDOR: [
    'Los productos que no tienen pedidos se eliminan.',
    'Los productos con pedidos se desactivan y se conservan, para no perder el historial.',
    'Los pedidos y las valoraciones se conservan.',
    CIERRE_DE_SESION,
  ],
  CLIENTE: [
    'Se eliminan tus direcciones, tus métodos de pago, tu carrito, tu lista de deseos y tus preferencias.',
    'Se elimina la configuración del doble factor de autenticación.',
    'Los pedidos y las valoraciones se conservan, para las estadísticas y los históricos.',
    CIERRE_DE_SESION,
  ],
}

// Mensajes propios según el code del backend: nunca su texto ni la contraseña enviada
const MENSAJE_CONTRASENA = 'La contraseña actual no es correcta.'
const MENSAJE_FALLO = 'No se ha podido eliminar la cuenta. Inténtalo de nuevo.'
const mensajeDeFallo = (error) => (error?.code === 'WRONG_PASSWORD' ? MENSAJE_CONTRASENA : MENSAJE_FALLO)

// Confirmación para eliminar la cuenta propia del vendedor (HU47) o del cliente (HU25), con la contraseña actual. Las
// consecuencias que explica dependen del rol.
export default function DeleteAccountDialog({ rol, onClose, onDeleted }) {
  const styles = useStyles()
  const contenido = useRef(null)
  const [password, setPassword] = useState('')
  const [eliminando, setEliminando] = useState(false)
  const [fallo, setFallo] = useState(null)

  // Al abrirse el foco va al campo de la contraseña, que es lo único que hay que rellenar
  useEffect(() => {
    contenido.current?.querySelector('input')?.focus()
  }, [])

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
          <DialogContent ref={contenido} className={styles.content}>
            {fallo && (
              <MessageBar intent="error">
                <MessageBarBody>{fallo}</MessageBarBody>
              </MessageBar>
            )}
            <span>Esta acción no se puede deshacer. Al eliminar tu cuenta:</span>
            <ul className={styles.consequences}>
              {CONSECUENCIAS[rol].map((consecuencia) => (
                <li key={consecuencia}>{consecuencia}</li>
              ))}
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
            <Button appearance="primary" onClick={confirmar} disabled={eliminando || !password.trim()}>
              Eliminar mi cuenta
            </Button>
          </DialogActions>
        </DialogBody>
      </DialogSurface>
    </Dialog>
  )
}

DeleteAccountDialog.propTypes = {
  rol: PropTypes.oneOf(Object.keys(CONSECUENCIAS)).isRequired,
  onClose: PropTypes.func.isRequired,
  onDeleted: PropTypes.func.isRequired,
}
