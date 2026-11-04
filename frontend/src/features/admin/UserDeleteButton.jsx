import PropTypes from 'prop-types'
import { useState } from 'react'
import { Button } from '@fluentui/react-components'
import { Delete20Regular } from '@fluentui/react-icons'
import { deleteUser } from '../../api/users'
import StatusConfirmDialog from './StatusConfirmDialog'

// Errores del backend al eliminar que tienen un mensaje propio.
const MENSAJES_CONFLICTO_ELIMINACION = {
  CANNOT_DELETE_SELF: 'No puedes eliminar tu propia cuenta.',
  LAST_ACTIVE_ADMIN: 'No se puede eliminar al último administrador activo.',
}

// Botón para eliminar a un administrador (HU37), siempre con confirmación. Lo usan la fila de la tabla (solo el
// icono de la papelera) y la ficha (`withText`, con el texto Eliminar). Un administrador no puede eliminarse a sí
// mismo ni se puede eliminar al último administrador activo (`lastAdminId`): el botón queda deshabilitado y su
// texto emergente explica por qué.
// `label` es el nombre con el que se nombra a la cuenta (por defecto, nombre y apellidos). `onResult` recibe el aviso
// que debe ver el administrador; `onChanged`, que la lista se actualice; `onDeleted`, que la cuenta ya no existe
// (la ficha se cierra). Un 404 (otra persona la eliminó antes) también actualiza la lista y cierra la ficha.
export default function UserDeleteButton({
  user,
  label,
  currentUserId,
  lastAdminId = null,
  withText = false,
  onResult,
  onChanged,
  onDeleted,
}) {
  const [confirmando, setConfirmando] = useState(false)
  const [enviando, setEnviando] = useState(false)
  const nombre = label ?? `${user.nombre} ${user.apellidos}`
  const esPropia = user.id === currentUserId
  const esUltimoAdministrador = user.id === lastAdminId
  const protegida = esPropia || esUltimoAdministrador
  const motivo = MENSAJES_CONFLICTO_ELIMINACION[esPropia ? 'CANNOT_DELETE_SELF' : 'LAST_ACTIVE_ADMIN']

  const avisarYActualizar = (aviso) => {
    onResult(aviso)
    onChanged()
    onDeleted?.()
  }

  const eliminar = async () => {
    setEnviando(true)
    try {
      await deleteUser(user.id)
      avisarYActualizar({ intent: 'success', texto: `${nombre} se ha eliminado.` })
    } catch (error) {
      if (error?.status === 404) {
        avisarYActualizar({ intent: 'error', texto: `${nombre} ya no existe. Se ha actualizado la lista.` })
      } else {
        onResult({
          intent: 'error',
          texto: MENSAJES_CONFLICTO_ELIMINACION[error?.code] ?? `No se ha podido eliminar a ${nombre}. Inténtalo de nuevo.`,
        })
      }
    } finally {
      setEnviando(false)
      setConfirmando(false)
    }
  }

  return (
    <>
      <Button
        appearance="subtle"
        icon={<Delete20Regular />}
        aria-label={`Eliminar a ${nombre}`}
        title={protegida ? motivo : 'Eliminar'}
        disabled={protegida}
        onClick={() => setConfirmando(true)}
      >
        {withText ? 'Eliminar' : undefined}
      </Button>
      {confirmando && (
        <StatusConfirmDialog
          titulo="Eliminar usuario"
          pregunta={`¿Seguro que quieres eliminar a ${nombre}? Esta acción no se puede deshacer.`}
          confirmar="Eliminar"
          enviando={enviando}
          onConfirm={eliminar}
          onCancel={() => setConfirmando(false)}
        />
      )}
    </>
  )
}

UserDeleteButton.propTypes = {
  user: PropTypes.shape({
    id: PropTypes.string.isRequired,
    nombre: PropTypes.string.isRequired,
    apellidos: PropTypes.string.isRequired,
  }).isRequired,
  label: PropTypes.string,
  currentUserId: PropTypes.string,
  lastAdminId: PropTypes.string,
  withText: PropTypes.bool,
  onResult: PropTypes.func.isRequired,
  onChanged: PropTypes.func.isRequired,
  onDeleted: PropTypes.func,
}
