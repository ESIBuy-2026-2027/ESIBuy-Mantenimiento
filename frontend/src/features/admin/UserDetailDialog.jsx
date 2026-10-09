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
import { Dismiss24Regular } from '@fluentui/react-icons'
import { getUser } from '../../api/users'
import { ROLES } from '../../auth/roles'
import { useApiQuery } from '../../hooks/useApiQuery'
import { formatDate } from '../../utils/format'
import ReadOnlyFields from './ReadOnlyFields'
import UserStatusButton from './UserStatusButton'
import { STATUS_LABELS, USER_STATUS } from './usersConfig'

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

const TIPOS_CLIENTE = { ESTANDAR: 'Estándar', PREMIUM: 'Premium' }
const SIN_DATO = '—'

const MENSAJES_EXITO = {
  activar: (nombre) => `${nombre} ya está activo y puede usar la plataforma.`,
  desactivar: (nombre) => `${nombre} está desactivado y ya no puede iniciar sesión.`,
}

// Errores del backend al desactivar que tienen un mensaje propio (HU23).
const MENSAJES_CONFLICTO = {
  CANNOT_DEACTIVATE_SELF: 'No puedes desactivar tu propia cuenta.',
  LAST_ACTIVE_ADMIN: 'No se puede desactivar al último administrador activo.',
}

const imagenDePerfil = (avatar) => (avatar ? `Avatar ${avatar.replace('avatar', '')}` : 'Sin elegir')

// Campos de la ficha según el rol. Nunca se muestra la contraseña.
function camposDe(usuario) {
  const comunes = [
    ['Nombre', usuario.nombre],
    ['Apellidos', usuario.apellidos],
    ['Email', usuario.email],
  ]
  const finales = [
    ['Estado', STATUS_LABELS[usuario.estado] ?? usuario.estado],
    ['Fecha de alta', formatDate(usuario.fechaAlta)],
  ]

  if (usuario.rol === ROLES.CLIENTE) {
    return [
      ...comunes,
      ['DNI', usuario.dni ?? SIN_DATO],
      ['Teléfono', usuario.telefono || SIN_DATO],
      ['Fecha de nacimiento', usuario.fechaNacimiento ? formatDate(usuario.fechaNacimiento) : SIN_DATO],
      ['Tipo de cliente', TIPOS_CLIENTE[usuario.tipoCliente] ?? SIN_DATO],
      ['Imagen de perfil', imagenDePerfil(usuario.avatar)],
      ...finales,
    ]
  }
  if (usuario.rol === ROLES.ADMIN) {
    return [
      ...comunes,
      ['Sede', usuario.sede || SIN_DATO],
      ['Fecha de incorporación', usuario.fechaIncorporacion ? formatDate(usuario.fechaIncorporacion) : SIN_DATO],
      ['Imagen de perfil', imagenDePerfil(usuario.avatar)],
      ...finales,
    ]
  }
  return [...comunes, ...finales]
}

// Ficha de un cliente o de un administrador (HU20), como la del vendedor (HU26): datos en solo lectura
// y el botón para activar o desactivar la cuenta (HU24 y HU23), salvo la propia.
export default function UserDetailDialog({ userId, currentUserId, onClose, onStatusChange }) {
  const styles = useStyles()
  const fetcher = useCallback(() => getUser(userId), [userId])
  const { data: usuario, failed, retry } = useApiQuery(fetcher)
  const [resultado, setResultado] = useState(null)

  let accion = null
  if (usuario?.estado === USER_STATUS.DESACTIVADO) accion = 'activar'
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
                Puedes consultar esta información, pero no modificarla.
              </MessageBarBody>
            </MessageBar>

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

            {usuario && <ReadOnlyFields fields={camposDe(usuario)} />}
          </DialogContent>
          {usuario && accion && (
            <DialogActions>
              <UserStatusButton user={usuario} accion={accion} onDone={avisarHecho} onError={avisarError} />
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
