import PropTypes from 'prop-types'
import { useCallback, useState } from 'react'
import {
  Button,
  Dialog,
  DialogActions,
  DialogBody,
  DialogContent,
  DialogSurface,
  DialogTitle,
  MessageBar,
  MessageBarActions,
  MessageBarBody,
  MessageBarTitle,
  Spinner,
  makeStyles,
  tokens,
} from '@fluentui/react-components'
import { Dismiss24Regular, Edit20Regular } from '@fluentui/react-icons'
import { getUser, updateUser } from '../../api/users'
import { useApiQuery } from '../../hooks/useApiQuery'
import EditableProfile from './EditableProfile'
import ReadOnlyFields from './ReadOnlyFields'
import UserStatusButton from './UserStatusButton'
import { readFields } from './profileFields'
import { USER_STATUS } from './usersConfig'

const useStyles = makeStyles({
  surface: {
    width: '100%',
    maxWidth: '760px',
  },
  content: {
    display: 'flex',
    flexDirection: 'column',
    gap: tokens.spacingVerticalL,
    minHeight: '240px',
  },
})

const MENSAJES_EXITO = {
  activar: (nombre) => `${nombre} ya está activo y puede usar la plataforma.`,
  desactivar: (nombre) => `${nombre} está desactivado y ya no puede iniciar sesión.`,
}

// Errores del backend al desactivar que tienen un mensaje propio (HU23).
const MENSAJES_CONFLICTO = {
  CANNOT_DEACTIVATE_SELF: 'No puedes desactivar tu propia cuenta.',
  LAST_ACTIVE_ADMIN: 'No se puede desactivar al último administrador activo.',
}

// Ficha de un cliente o de un administrador (HU20), como la del vendedor (HU26): datos en solo lectura,
// el botón para activar o desactivar la cuenta (HU24 y HU23), salvo la propia, y el botón Editar (HU22), que
// convierte en modificables los campos que se pueden cambiar y deja el resto en solo lectura (el rol, siempre).
// `onStatusChange` avisa al listado de que la cuenta ha cambiado (de estado o de datos).
export default function UserDetailDialog({ userId, currentUserId, onClose, onStatusChange }) {
  const styles = useStyles()
  const fetcher = useCallback(() => getUser(userId), [userId])
  const { data: usuario, failed, retry } = useApiQuery(fetcher)
  const [resultado, setResultado] = useState(null)
  const [editando, setEditando] = useState(false)

  // Una cuenta pendiente (HU1) se acepta activándola, como los vendedores pendientes.
  let accion = null
  if (usuario?.estado === USER_STATUS.BLOQUEADO || usuario?.estado === USER_STATUS.PENDIENTE) accion = 'activar'
  else if (usuario?.estado === USER_STATUS.ACTIVO && usuario.id !== currentUserId) accion = 'desactivar'

  const avisarHecho = (nombre) => {
    setResultado({ intent: 'success', texto: MENSAJES_EXITO[accion](nombre) })
    retry()
    onStatusChange()
  }

  const avisarError = (nombre, error) =>
    setResultado({
      intent: 'error',
      texto: MENSAJES_CONFLICTO[error?.code] ?? `No se ha podido ${accion} a ${nombre}. Inténtalo de nuevo.`,
    })

  const avisarGuardado = () => {
    setEditando(false)
    setResultado({ intent: 'success', texto: 'Los cambios se han guardado.' })
    retry()
    onStatusChange()
  }

  const empezarAEditar = () => {
    setResultado(null)
    setEditando(true)
  }

  return (
    <Dialog open modalType="modal" onOpenChange={(_event, data) => !data.open && onClose()}>
      <DialogSurface className={styles.surface}>
        <DialogBody>
          <DialogTitle
            action={
              <Button appearance="subtle" aria-label="Cerrar" icon={<Dismiss24Regular />} onClick={onClose} />
            }
          >
            Información del usuario
          </DialogTitle>
          <DialogContent className={styles.content}>
            {editando ? (
              <MessageBar intent="info">
                <MessageBarBody>
                  <MessageBarTitle>Modo edición</MessageBarTitle>
                  Solo se pueden modificar los campos con fondo claro. Los oscuros no se pueden cambiar.
                </MessageBarBody>
              </MessageBar>
            ) : (
              <MessageBar intent="info">
                <MessageBarBody>
                  <MessageBarTitle>Modo solo lectura</MessageBarTitle>
                  Puedes consultar esta información. Para cambiarla, pulsa Editar.
                </MessageBarBody>
              </MessageBar>
            )}

            {resultado && (
              <MessageBar intent={resultado.intent}>
                <MessageBarBody>{resultado.texto}</MessageBarBody>
              </MessageBar>
            )}

            {failed && (
              <MessageBar intent="error">
                <MessageBarBody>No se ha podido cargar la información del usuario.</MessageBarBody>
                <MessageBarActions>
                  <Button onClick={retry}>Reintentar</Button>
                </MessageBarActions>
              </MessageBar>
            )}

            {!usuario && !failed && <Spinner label="Cargando usuario…" />}

            {usuario && !editando && <ReadOnlyFields fields={readFields(usuario.rol, usuario)} />}
            {usuario && editando && (
              <EditableProfile
                rol={usuario.rol}
                usuario={usuario}
                save={(datos) => updateUser(usuario.id, datos)}
                onSaved={avisarGuardado}
                onCancel={() => setEditando(false)}
              />
            )}
          </DialogContent>
          {usuario && !editando && (
            <DialogActions>
              <Button icon={<Edit20Regular />} onClick={empezarAEditar}>
                Editar
              </Button>
              {accion && <UserStatusButton user={usuario} accion={accion} onDone={avisarHecho} onError={avisarError} />}
            </DialogActions>
          )}
        </DialogBody>
      </DialogSurface>
    </Dialog>
  )
}

UserDetailDialog.propTypes = {
  userId: PropTypes.string.isRequired,
  currentUserId: PropTypes.string,
  onClose: PropTypes.func.isRequired,
  onStatusChange: PropTypes.func.isRequired,
}
