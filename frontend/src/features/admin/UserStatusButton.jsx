import PropTypes from 'prop-types'
import { useState } from 'react'
import { Button } from '@fluentui/react-components'
import { PersonAvailable20Regular, PersonProhibited20Regular } from '@fluentui/react-icons'
import { updateUserStatus } from '../../api/users'
import StatusConfirmDialog from './StatusConfirmDialog'

// Textos de cada acción: activar (HU24) o desactivar (HU23).
const ACCIONES = {
  activar: {
    boton: 'Activar',
    icono: <PersonAvailable20Regular />,
    titulo: 'Activar usuario',
    pregunta: (nombre) => `¿Seguro que quieres activar a ${nombre}? Podrá volver a usar la plataforma.`,
    estado: 'ACTIVO',
  },
  desactivar: {
    boton: 'Desactivar',
    icono: <PersonProhibited20Regular />,
    titulo: 'Desactivar usuario',
    pregunta: (nombre) => `¿Seguro que quieres desactivar a ${nombre}? No podrá iniciar sesión.`,
    estado: 'DESACTIVADO',
  },
}

// Botón de una fila de usuarios que activa o desactiva la cuenta, siempre con confirmación.
export default function UserStatusButton({ user, accion, onDone, onError }) {
  const [confirmando, setConfirmando] = useState(false)
  const [enviando, setEnviando] = useState(false)
  const nombre = `${user.nombre} ${user.apellidos}`
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
        size="small"
        icon={textos.icono}
        aria-label={`${textos.boton} a ${nombre}`}
        onClick={() => setConfirmando(true)}
      >
        {textos.boton}
      </Button>
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
  accion: PropTypes.oneOf(['activar', 'desactivar']).isRequired,
  onDone: PropTypes.func.isRequired,
  onError: PropTypes.func.isRequired,
}
