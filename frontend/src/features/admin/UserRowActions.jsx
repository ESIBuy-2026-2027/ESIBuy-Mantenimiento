import PropTypes from 'prop-types'
import { useState } from 'react'
import { Button, makeStyles, tokens } from '@fluentui/react-components'
import { Delete20Regular, Edit20Regular, Search20Regular } from '@fluentui/react-icons'
import { deleteUser } from '../../api/users'
import StatusConfirmDialog from './StatusConfirmDialog'
import UserDetailDialog from './UserDetailDialog'
import UserEditDialog from './UserEditDialog'
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
const MENSAJES_CONFLICTO_BLOQUEO = {
  CANNOT_BLOCK_SELF: 'No puedes bloquear tu propia cuenta.',
  LAST_ACTIVE_ADMIN: 'No se puede bloquear al último administrador activo.',
}

// Errores del backend al eliminar que tienen un mensaje propio.
const MENSAJES_CONFLICTO_ELIMINACION = {
  CANNOT_DELETE_SELF: 'No puedes eliminar tu propia cuenta.',
  LAST_ACTIVE_ADMIN: 'No se puede eliminar al último administrador activo.',
}

const MOTIVO_PROPIA_CUENTA = 'No puedes bloquear tu propia cuenta.'

// Acciones de una fila de la tabla de usuarios (HU19): la lupa que abre su ficha de solo lectura (HU20), el lápiz
// que abre el formulario de edición (HU22), el botón para bloquear o desbloquear la cuenta (HU23 y HU24) y la
// papelera para eliminarla. Una cuenta activa se bloquea; una bloqueada o pendiente (HU1) se desbloquea, que es
// como se acepta. Bloquear y eliminar piden confirmación.
// `onResult` recibe el aviso que debe ver el administrador; `onChanged`, que la lista se actualice.
export default function UserRowActions({ user, currentUserId, onResult, onChanged }) {
  const styles = useStyles()
  const [dialogo, setDialogo] = useState(null)
  const [eliminando, setEliminando] = useState(false)
  const nombre = `${user.nombre} ${user.apellidos}`
  const accion = user.estado === USER_STATUS.ACTIVO ? 'bloquear' : 'desbloquear'
  const esPropia = user.id === currentUserId
  const cerrar = () => setDialogo(null)

  const avisarEstadoCambiado = (nombreDelUsuario) => {
    onResult({ intent: 'success', texto: MENSAJES_EXITO[accion](nombreDelUsuario) })
    onChanged()
  }

  const avisarErrorDeEstado = (nombreDelUsuario, error) =>
    onResult({
      intent: 'error',
      texto:
        MENSAJES_CONFLICTO_BLOQUEO[error?.code] ??
        `No se ha podido ${accion} a ${nombreDelUsuario}. Inténtalo de nuevo.`,
    })

  const avisarEdicionGuardada = () => {
    cerrar()
    onResult({ intent: 'success', texto: 'Los cambios se han guardado.' })
    onChanged()
  }

  const eliminar = async () => {
    setEliminando(true)
    try {
      await deleteUser(user.id)
      onResult({ intent: 'success', texto: `${nombre} se ha eliminado.` })
      onChanged()
    } catch (error) {
      onResult({
        intent: 'error',
        texto: MENSAJES_CONFLICTO_ELIMINACION[error?.code] ?? `No se ha podido eliminar a ${nombre}. Inténtalo de nuevo.`,
      })
    } finally {
      setEliminando(false)
      cerrar()
    }
  }

  return (
    <div className={styles.actions}>
      <Button
        appearance="subtle"
        icon={<Search20Regular />}
        aria-label={`Ver información de ${nombre}`}
        title="Ver información"
        onClick={() => setDialogo('ficha')}
      />
      <Button
        appearance="subtle"
        icon={<Edit20Regular />}
        aria-label={`Editar a ${nombre}`}
        title="Editar"
        onClick={() => setDialogo('editar')}
      />
      <UserStatusButton
        user={user}
        accion={accion}
        disabled={accion === 'bloquear' && esPropia}
        reason={MOTIVO_PROPIA_CUENTA}
        onDone={avisarEstadoCambiado}
        onError={avisarErrorDeEstado}
      />
      <Button
        appearance="subtle"
        icon={<Delete20Regular />}
        aria-label={`Eliminar a ${nombre}`}
        title="Eliminar"
        onClick={() => setDialogo('eliminar')}
      />

      {dialogo === 'ficha' && <UserDetailDialog userId={user.id} onClose={cerrar} />}
      {dialogo === 'editar' && <UserEditDialog userId={user.id} onClose={cerrar} onSaved={avisarEdicionGuardada} />}
      {dialogo === 'eliminar' && (
        <StatusConfirmDialog
          titulo="Eliminar usuario"
          pregunta={`¿Seguro que quieres eliminar a ${nombre}? Esta acción no se puede deshacer.`}
          confirmar="Eliminar"
          enviando={eliminando}
          onConfirm={eliminar}
          onCancel={cerrar}
        />
      )}
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
