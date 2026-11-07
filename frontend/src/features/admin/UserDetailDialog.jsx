import PropTypes from 'prop-types'
import { useCallback } from 'react'
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
import { Dismiss24Regular } from '@fluentui/react-icons'
import { getUser } from '../../api/users'
import { useApiQuery } from '../../hooks/useApiQuery'
import ReadOnlyFields from './ReadOnlyFields'
import UserDeleteButton from './UserDeleteButton'
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

// Ficha de un cliente o de un administrador (HU20), como la del vendedor (HU26): sus datos en solo lectura.
// Editar, bloquear y desbloquear la cuenta se hacen desde los botones de su fila en la tabla (HU19, HU22, HU23 y
// HU24). Con `canDelete` (la fila de un administrador, HU37) la ficha añade además el botón Eliminar, con la misma
// confirmación y reglas que la papelera de la fila: `currentUserId` y `lastAdminId` deshabilitan el botón para la
// propia cuenta y para el último administrador activo; `onResult` y `onChanged` avisan a la tabla, y al eliminar
// (o si ya no existía) la ficha se cierra.
export default function UserDetailDialog({
  userId,
  onClose,
  canDelete = false,
  currentUserId,
  lastAdminId = null,
  onResult,
  onChanged,
}) {
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
            Información del usuario
          </DialogTitle>
          <DialogContent className={styles.content}>
            <MessageBar intent="info">
              <MessageBarBody>
                <MessageBarTitle>Modo solo lectura</MessageBarTitle>
                Puedes consultar esta información. Para cambiarla, usa el botón Editar de la tabla.
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

            {usuario && <ReadOnlyFields fields={readFields(usuario.rol, usuario)} />}
          </DialogContent>
          {canDelete && usuario && (
            <DialogActions>
              <UserDeleteButton
                user={usuario}
                currentUserId={currentUserId}
                lastAdminId={lastAdminId}
                withText
                onResult={onResult}
                onChanged={onChanged}
                onDeleted={onClose}
              />
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
  canDelete: PropTypes.bool,
  currentUserId: PropTypes.string,
  lastAdminId: PropTypes.string,
  onResult: PropTypes.func,
  onChanged: PropTypes.func,
}
