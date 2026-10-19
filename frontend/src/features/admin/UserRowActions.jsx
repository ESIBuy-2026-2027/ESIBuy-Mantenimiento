import PropTypes from 'prop-types'
import { useState } from 'react'
import { Button, makeStyles, tokens } from '@fluentui/react-components'
import { Search20Regular } from '@fluentui/react-icons'
import UserDetailDialog from './UserDetailDialog'
import UserStatusButton from './UserStatusButton'
import { USER_STATUS } from './usersConfig'

const useStyles = makeStyles({
  actions: {
    display: 'flex',
    alignItems: 'center',
    gap: tokens.spacingHorizontalXS,
  },
})

const MENSAJES_EXITO = {
  desbloquear: (nombre) => `${nombre} ya está activo y puede usar la plataforma.`,
  bloquear: (nombre) => `${nombre} está bloqueado y ya no puede iniciar sesión.`,
}

// Errores del backend al bloquear que tienen un mensaje propio (HU23).
const MENSAJES_CONFLICTO = {
  CANNOT_BLOCK_SELF: 'No puedes bloquear tu propia cuenta.',
  LAST_ACTIVE_ADMIN: 'No se puede bloquear al último administrador activo.',
}

const MOTIVO_PROPIA_CUENTA = 'No puedes bloquear tu propia cuenta.'

// Acciones de una fila de la tabla de usuarios: la lupa que abre su ficha (HU20) y el botón para bloquear o
// desbloquear la cuenta (HU23 y HU24). Una cuenta activa se bloquea; una bloqueada o pendiente (HU1) se
// desbloquea, que es como se acepta. Un administrador no puede bloquearse a sí mismo.
// `onResult` recibe el aviso que debe ver el administrador; `onChanged`, que la lista se actualice.
export default function UserRowActions({ user, currentUserId, onResult, onChanged }) {
  const styles = useStyles()
  const [verFicha, setVerFicha] = useState(false)
  const nombre = `${user.nombre} ${user.apellidos}`
  const accion = user.estado === USER_STATUS.ACTIVO ? 'bloquear' : 'desbloquear'
  const esPropia = user.id === currentUserId

  const avisarHecho = (nombreDelUsuario) => {
    onResult({ intent: 'success', texto: MENSAJES_EXITO[accion](nombreDelUsuario) })
    onChanged()
  }

  const avisarError = (nombreDelUsuario, error) =>
    onResult({
      intent: 'error',
      texto: MENSAJES_CONFLICTO[error?.code] ?? `No se ha podido ${accion} a ${nombreDelUsuario}. Inténtalo de nuevo.`,
    })

  return (
    <div className={styles.actions}>
      <Button
        appearance="subtle"
        icon={<Search20Regular />}
        aria-label={`Ver información de ${nombre}`}
        title="Ver información"
        onClick={() => setVerFicha(true)}
      />
      <UserStatusButton
        user={user}
        accion={accion}
        disabled={accion === 'bloquear' && esPropia}
        reason={MOTIVO_PROPIA_CUENTA}
        onDone={avisarHecho}
        onError={avisarError}
      />
      {verFicha && <UserDetailDialog userId={user.id} onClose={() => setVerFicha(false)} onStatusChange={onChanged} />}
    </div>
  )
}

UserRowActions.propTypes = {
  user: PropTypes.shape({
    id: PropTypes.string.isRequired,
    nombre: PropTypes.string.isRequired,
    apellidos: PropTypes.string.isRequired,
    estado: PropTypes.string.isRequired,
  }).isRequired,
  currentUserId: PropTypes.string,
  onResult: PropTypes.func.isRequired,
  onChanged: PropTypes.func.isRequired,
}
