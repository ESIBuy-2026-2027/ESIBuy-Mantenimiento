import { useEffect, useRef } from 'react'
import PropTypes from 'prop-types'
import {
  Button,
  Dialog,
  DialogActions,
  DialogBody,
  DialogContent,
  DialogSurface,
  DialogTitle,
} from '@fluentui/react-components'

// Confirmación para salir de una pantalla con cambios sin guardar (HU45): al navegar a otra pantalla o al cancelar la
// edición. La opción segura, seguir editando, es la que tiene el foco al abrirse; Escape también la elige.
export default function UnsavedChangesDialog({ onStay, onLeave }) {
  const seguir = useRef(null)

  // El diálogo de Fluent enfoca su superficie: el foco se lleva a mano a la opción segura
  useEffect(() => {
    seguir.current?.focus()
  }, [])

  return (
    <Dialog open modalType="alert" onOpenChange={(_event, data) => !data.open && onStay()}>
      <DialogSurface>
        <DialogBody>
          <DialogTitle>¿Salir sin guardar?</DialogTitle>
          <DialogContent>Tienes cambios sin guardar. Si sales, se perderán.</DialogContent>
          <DialogActions>
            <Button ref={seguir} appearance="primary" onClick={onStay}>
              Seguir editando
            </Button>
            <Button appearance="secondary" onClick={onLeave}>
              Salir sin guardar
            </Button>
          </DialogActions>
        </DialogBody>
      </DialogSurface>
    </Dialog>
  )
}

UnsavedChangesDialog.propTypes = {
  onStay: PropTypes.func.isRequired,
  onLeave: PropTypes.func.isRequired,
}
