import { useState } from 'react'
import PropTypes from 'prop-types'
import {
  Button,
  Dialog,
  DialogActions,
  DialogBody,
  DialogContent,
  DialogSurface,
  DialogTitle,
  MessageBar,
  MessageBarBody,
  makeStyles,
  tokens,
} from '@fluentui/react-components'
import { closeIncident } from '../../api/adminIncidents'

const useStyles = makeStyles({
  content: {
    display: 'flex',
    flexDirection: 'column',
    gap: tokens.spacingVerticalM,
  },
  text: {
    overflowWrap: 'anywhere',
  },
})

// Confirmación para cerrar una incidencia, pendiente o abierta (HU50), como la de cancelar un pedido
// (CancelOrderDialog): cerrarla la elimina y no se puede deshacer. Si otro administrador ya la ha cerrado (404), lo
// comunica a la página, que vuelve a cargar la lista; cualquier otro fallo se avisa aquí para poder reintentar.
export default function CloseIncidentDialog({ incident, onClose, onClosed, onConflict }) {
  const styles = useStyles()
  const [cerrando, setCerrando] = useState(false)
  const [fallo, setFallo] = useState(false)

  const confirmar = async () => {
    setCerrando(true)
    setFallo(false)
    try {
      await closeIncident(incident.id)
      onClosed()
    } catch (error) {
      if (error?.code === 'INCIDENT_NOT_FOUND') {
        onConflict()
        return
      }
      setFallo(true)
      setCerrando(false)
    }
  }

  return (
    <Dialog open modalType="alert" onOpenChange={(_event, data) => !data.open && !cerrando && onClose()}>
      <DialogSurface>
        <DialogBody>
          <DialogTitle>Cerrar incidencia</DialogTitle>
          <DialogContent className={styles.content}>
            {fallo && (
              <MessageBar intent="error">
                <MessageBarBody>No se ha podido cerrar la incidencia. Inténtalo de nuevo.</MessageBarBody>
              </MessageBar>
            )}
            <span className={styles.text}>
              ¿Seguro que quieres cerrar «{incident.asunto}»? Se eliminará y no se puede deshacer.
            </span>
          </DialogContent>
          <DialogActions>
            <Button appearance="secondary" onClick={onClose} disabled={cerrando}>
              Volver
            </Button>
            <Button appearance="primary" onClick={confirmar} disabled={cerrando}>
              Sí, cerrar la incidencia
            </Button>
          </DialogActions>
        </DialogBody>
      </DialogSurface>
    </Dialog>
  )
}

CloseIncidentDialog.propTypes = {
  incident: PropTypes.shape({
    id: PropTypes.string.isRequired,
    asunto: PropTypes.string.isRequired,
  }).isRequired,
  onClose: PropTypes.func.isRequired,
  onClosed: PropTypes.func.isRequired,
  onConflict: PropTypes.func.isRequired,
}
