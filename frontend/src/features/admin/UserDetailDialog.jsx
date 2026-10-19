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
import { readFields } from './profileFields'

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

// Ficha de un cliente o de un administrador (HU20), como la del vendedor (HU26): datos en solo lectura y el
// botón Editar (HU22), que convierte en modificables los campos que se pueden cambiar y deja el resto en solo
// lectura (el rol, siempre). Bloquear y desbloquear la cuenta ya no se hace aquí, sino desde la fila de la tabla
// (HU23 y HU24). `onStatusChange` avisa al listado de que los datos de la cuenta han cambiado.
export default function UserDetailDialog({ userId, onClose, onStatusChange }) {
  const styles = useStyles()
  const fetcher = useCallback(() => getUser(userId), [userId])
  const { data: usuario, failed, retry } = useApiQuery(fetcher)
  const [resultado, setResultado] = useState(null)
  const [editando, setEditando] = useState(false)

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
            </DialogActions>
          )}
        </DialogBody>
      </DialogSurface>
    </Dialog>
  )
}

UserDetailDialog.propTypes = {
  userId: PropTypes.string.isRequired,
  onClose: PropTypes.func.isRequired,
  onStatusChange: PropTypes.func.isRequired,
}
