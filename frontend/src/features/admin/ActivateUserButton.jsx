import PropTypes from 'prop-types'
import { useState } from 'react'
import { Button } from '@fluentui/react-components'
import { PersonAvailable20Regular } from '@fluentui/react-icons'
import { activateUser } from '../../api/users'
import StatusConfirmDialog from './StatusConfirmDialog'

// Botón "Activar" de una fila de usuarios (HU24): pide confirmación y activa la cuenta.
export default function ActivateUserButton({ user, onActivated, onError }) {
  const [confirmando, setConfirmando] = useState(false)
  const [enviando, setEnviando] = useState(false)
  const nombre = `${user.nombre} ${user.apellidos}`

  const activar = async () => {
    setEnviando(true)
    try {
      await activateUser(user.id)
      onActivated(nombre)
    } catch {
      onError(nombre)
    } finally {
      setEnviando(false)
      setConfirmando(false)
    }
  }

  return (
    <>
      <Button
        size="small"
        icon={<PersonAvailable20Regular />}
        aria-label={`Activar a ${nombre}`}
        onClick={() => setConfirmando(true)}
      >
        Activar
      </Button>
      {confirmando && (
        <StatusConfirmDialog
          titulo="Activar usuario"
          pregunta={`¿Seguro que quieres activar a ${nombre}? Podrá volver a usar la plataforma.`}
          confirmar="Activar"
          enviando={enviando}
          onConfirm={activar}
          onCancel={() => setConfirmando(false)}
        />
      )}
    </>
  )
}

ActivateUserButton.propTypes = {
  user: PropTypes.shape({
    id: PropTypes.string.isRequired,
    nombre: PropTypes.string.isRequired,
    apellidos: PropTypes.string.isRequired,
  }).isRequired,
  onActivated: PropTypes.func.isRequired,
  onError: PropTypes.func.isRequired,
}
