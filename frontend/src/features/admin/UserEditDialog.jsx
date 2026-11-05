import PropTypes from 'prop-types'
import { useCallback } from 'react'
import {
  Button,
  Dialog,
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
import { Dismiss24Regular } from '@fluentui/react-icons'
import { getUser, updateUser } from '../../api/users'
import { useApiQuery } from '../../hooks/useApiQuery'
import EditableProfile from './EditableProfile'

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

// Formulario para modificar los datos de un cliente, un administrador o un vendedor (HU22), que se abre desde
// el botón Editar de su fila en la tabla. Solo se pueden cambiar los campos con fondo claro; el rol, el email y
// la contraseña nunca. `onSaved` avisa de que se han guardado los cambios; `onClose`, de que se cierra sin más.
export default function UserEditDialog({ userId, onClose, onSaved }) {
  const styles = useStyles()
  const fetcher = useCallback(() => getUser(userId), [userId])
  const { data: usuario, failed, retry } = useApiQuery(fetcher)

  return (
    <Dialog open modalType="modal" onOpenChange={(_event, data) => !data.open && onClose()}>
      <DialogSurface className={styles.surface}>
        <DialogBody>
          <DialogTitle
            action={
              <Button appearance="subtle" aria-label="Cerrar" icon={<Dismiss24Regular />} onClick={onClose} />
            }
          >
            Editar usuario
          </DialogTitle>
          <DialogContent className={styles.content}>
            <MessageBar intent="info">
              <MessageBarBody>
                <MessageBarTitle>Modo edición</MessageBarTitle>
                Solo se pueden modificar los campos con fondo claro. Los oscuros no se pueden cambiar.
              </MessageBarBody>
            </MessageBar>

            {failed && (
              <MessageBar intent="error">
                <MessageBarBody>No se ha podido cargar la información del usuario.</MessageBarBody>
                <MessageBarActions>
                  <Button onClick={retry}>Reintentar</Button>
                </MessageBarActions>
              </MessageBar>
            )}

            {!usuario && !failed && <Spinner label="Cargando usuario…" />}

            {usuario && (
              <EditableProfile
                rol={usuario.rol}
                usuario={usuario}
                save={(datos) => updateUser(usuario.id, datos)}
                onSaved={onSaved}
                onCancel={onClose}
              />
            )}
          </DialogContent>
        </DialogBody>
      </DialogSurface>
    </Dialog>
  )
}

UserEditDialog.propTypes = {
  userId: PropTypes.string.isRequired,
  onClose: PropTypes.func.isRequired,
  onSaved: PropTypes.func.isRequired,
}
