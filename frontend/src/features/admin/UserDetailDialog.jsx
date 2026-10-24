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
import { getUser } from '../../api/users'
import { useApiQuery } from '../../hooks/useApiQuery'
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

// Ficha de un cliente o de un administrador (HU20), como la del vendedor (HU26): sus datos en solo lectura y
// nada más. Editar, bloquear, desbloquear y eliminar la cuenta se hacen desde los botones de su fila en la tabla
// (HU19, HU22, HU23 y HU24).
export default function UserDetailDialog({ userId, onClose }) {
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
        </DialogBody>
      </DialogSurface>
    </Dialog>
  )
}

UserDetailDialog.propTypes = {
  userId: PropTypes.string.isRequired,
  onClose: PropTypes.func.isRequired,
}
