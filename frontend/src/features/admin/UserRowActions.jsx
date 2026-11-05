import PropTypes from 'prop-types'
import { useState } from 'react'
import { Button, makeStyles, tokens } from '@fluentui/react-components'
import { Edit20Regular, Search20Regular } from '@fluentui/react-icons'
import UserDeleteButton from './UserDeleteButton'
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

// Acciones de una fila de la tabla de usuarios (HU19): la lupa que abre su ficha de solo lectura (HU20), el lápiz
// que abre el formulario de edición (HU22), el botón para bloquear o desbloquear la cuenta (HU23 y HU24) y la
// papelera para eliminarla, que solo sale con `canDelete`: el backend solo elimina administradores (HU37), así que
// los listados la activan únicamente para las filas de administradores. Una cuenta activa se bloquea; una bloqueada o pendiente (HU1) se desbloquea, que es
// como se acepta. Bloquear y eliminar piden confirmación. Un administrador no puede bloquearse ni eliminarse a sí
// mismo, ni se puede bloquear o eliminar al último administrador activo (`lastAdminId`, AB#250): esos botones
// quedan deshabilitados y explican por qué.
// `label` es el nombre con el que se nombra a la cuenta en los botones y en los avisos (por defecto, nombre y
// apellidos; en la tabla de vendedores, su nombre comercial). `renderDetail(onClose)` devuelve la ficha que abre
// la lupa (por defecto, la de cualquier usuario; los vendedores tienen la suya, con sus productos y pedidos).
// `onResult` recibe el aviso que debe ver el administrador; `onChanged`, que la lista se actualice.
export default function UserRowActions({
  user,
  currentUserId,
  lastAdminId = null,
  canDelete = false,
  label,
  renderDetail,
  onResult,
  onChanged,
}) {
  const styles = useStyles()
  const [dialogo, setDialogo] = useState(null)
  const nombre = label ?? `${user.nombre} ${user.apellidos}`
  const accion = user.estado === USER_STATUS.ACTIVO ? 'bloquear' : 'desbloquear'
  const esPropia = user.id === currentUserId
  const esUltimoAdministrador = user.id === lastAdminId
  const protegida = esPropia || esUltimoAdministrador
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
        label={nombre}
        disabled={accion === 'bloquear' && protegida}
        reason={esPropia ? MENSAJES_CONFLICTO_BLOQUEO.CANNOT_BLOCK_SELF : MENSAJES_CONFLICTO_BLOQUEO.LAST_ACTIVE_ADMIN}
        onDone={avisarEstadoCambiado}
        onError={avisarErrorDeEstado}
      />
      {canDelete && (
        <UserDeleteButton
          user={user}
          label={label}
          currentUserId={currentUserId}
          lastAdminId={lastAdminId}
          onResult={onResult}
          onChanged={onChanged}
        />
      )}

      {dialogo === 'ficha' && (renderDetail ? (
          renderDetail(cerrar)
        ) : (
          <UserDetailDialog
            userId={user.id}
            onClose={cerrar}
            canDelete={canDelete}
            currentUserId={currentUserId}
            lastAdminId={lastAdminId}
            onResult={onResult}
            onChanged={onChanged}
          />
        ))}
      {dialogo === 'editar' && <UserEditDialog userId={user.id} onClose={cerrar} onSaved={avisarEdicionGuardada} />}
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
  lastAdminId: PropTypes.string,
  canDelete: PropTypes.bool,
  label: PropTypes.string,
  renderDetail: PropTypes.func,
  onResult: PropTypes.func.isRequired,
  onChanged: PropTypes.func.isRequired,
}
