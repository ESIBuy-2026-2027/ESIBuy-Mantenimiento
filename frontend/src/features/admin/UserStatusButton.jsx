import PropTypes from 'prop-types'
import { useState } from 'react'
import { Button } from '@fluentui/react-components'
import { PersonAvailable20Regular, PersonProhibited20Regular } from '@fluentui/react-icons'
import { updateUserStatus } from '../../api/users'
import StatusConfirmDialog from './StatusConfirmDialog'

// Textos de cada acción: desbloquear (HU24, también acepta las cuentas pendientes) o bloquear (HU23).
const ACCIONES = {
  desbloquear: {
    boton: 'Desbloquear',
    icono: <PersonAvailable20Regular />,
    titulo: 'Desbloquear usuario',
    pregunta: (nombre) => `¿Seguro que quieres desbloquear a ${nombre}? Podrá volver a usar la plataforma.`,
    estado: 'ACTIVO',
  },
  bloquear: {
    boton: 'Bloquear',
    icono: <PersonProhibited20Regular />,
    titulo: 'Bloquear usuario',
    pregunta: (nombre) => `¿Seguro que quieres bloquear a ${nombre}? No podrá iniciar sesión.`,
    estado: 'BLOQUEADO',
  },
}

// Botón de una fila de usuarios que bloquea o desbloquea la cuenta, siempre con confirmación. Es solo un icono:
// su nombre accesible lleva el del usuario (o el `label` que se indique, como el nombre comercial de un vendedor).
// Deshabilitado, `reason` explica por qué no se puede usar.
export default function UserStatusButton({ user, accion, label, disabled = false, reason, onDone, onError }) {
  const [confirmando, setConfirmando] = useState(false)
  const [enviando, setEnviando] = useState(false)
  const nombre = label ?? `${user.nombre} ${user.apellidos}`
  const textos = ACCIONES[accion]

  const confirmar = async () => {
    setEnviando(true)
    try {
      await updateUserStatus(user.id, textos.estado)
      onDone(nombre)
    } catch (error) {
      onError(nombre, error)
    } finally {
      setEnviando(false)
      setConfirmando(false)
    }
  }

  return (
    <>
      <Button
        appearance="subtle"
        icon={textos.icono}
        aria-label={`${textos.boton} a ${nombre}`}
        title={disabled && reason ? reason : textos.boton}
        disabled={disabled}
        onClick={() => setConfirmando(true)}
      />
      {confirmando && (
        <StatusConfirmDialog
          titulo={textos.titulo}
          pregunta={textos.pregunta(nombre)}
          confirmar={textos.boton}
          enviando={enviando}
          onConfirm={confirmar}
          onCancel={() => setConfirmando(false)}
        />
      )}
    </>
  )
}

UserStatusButton.propTypes = {
  user: PropTypes.shape({
    id: PropTypes.string.isRequired,
    nombre: PropTypes.string.isRequired,
    apellidos: PropTypes.string.isRequired,
  }).isRequired,
  accion: PropTypes.oneOf(['desbloquear', 'bloquear']).isRequired,
  label: PropTypes.string,
  disabled: PropTypes.bool,
  reason: PropTypes.string,
  onDone: PropTypes.func.isRequired,
  onError: PropTypes.func.isRequired,
}
