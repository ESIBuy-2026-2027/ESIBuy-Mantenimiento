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
  Spinner,
  makeStyles,
  tokens,
} from '@fluentui/react-components'
import { resolveIncident } from '../../api/adminIncidents'

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

// Códigos con los que el backend dice que la incidencia ha cambiado desde que se cargó la lista (HU192)
const CAMBIADA_ENTRETANTO = new Set(['INCIDENT_ALREADY_RESOLVED', 'INCIDENT_NOT_OPEN', 'INCIDENT_NOT_FOUND'])

// Mensajes genéricos: nunca se muestra el detalle que devuelva el servidor
const SIN_PERMISO = 'No tienes permiso para resolver incidencias.'
const NO_SE_HA_PODIDO = 'No se ha podido marcar la incidencia como resuelta. Inténtalo de nuevo.'

// Confirmación para marcar como resuelta una incidencia abierta (HU192), como la de cerrarla (CloseIncidentDialog).
// Mientras responde el backend muestra la carga sin perder el foco. Si sale bien entrega la incidencia resuelta; si
// ha cambiado entretanto (404 o 409), comunica el código a la página, que vuelve a cargar la lista; cualquier otro
// fallo se anuncia aquí para poder reintentar.
export default function ResolveIncidentDialog({ incident, onClose, onResolved, onConflict }) {
  const styles = useStyles()
  const [resolviendo, setResolviendo] = useState(false)
  const [error, setError] = useState(null)

  const confirmar = async () => {
    setResolviendo(true)
    setError(null)
    try {
      onResolved(await resolveIncident(incident.id))
    } catch (err) {
      if (CAMBIADA_ENTRETANTO.has(err?.code)) {
        onConflict(err.code)
        return
      }
      setError(err?.status === 403 ? SIN_PERMISO : NO_SE_HA_PODIDO)
      setResolviendo(false)
    }
  }

  return (
    <Dialog open modalType="alert" onOpenChange={(_event, data) => !data.open && !resolviendo && onClose()}>
      <DialogSurface>
        <DialogBody>
          <DialogTitle>Marcar incidencia como resuelta</DialogTitle>
          <DialogContent className={styles.content}>
            {error && (
              <MessageBar intent="error" role="alert">
                <MessageBarBody>{error}</MessageBarBody>
              </MessageBar>
            )}
            <span className={styles.text}>
              ¿Seguro que quieres marcar «{incident.asunto}» como resuelta? Se guardará la fecha de resolución y no se
              puede deshacer.
            </span>
          </DialogContent>
          <DialogActions>
            <Button appearance="secondary" onClick={onClose} disabled={resolviendo}>
              Volver
            </Button>
            <Button
              appearance="primary"
              onClick={confirmar}
              disabledFocusable={resolviendo}
              icon={resolviendo ? <Spinner size="tiny" /> : undefined}
            >
              {resolviendo ? 'Resolviendo…' : 'Sí, marcar como resuelta'}
            </Button>
          </DialogActions>
        </DialogBody>
      </DialogSurface>
    </Dialog>
  )
}

ResolveIncidentDialog.propTypes = {
  incident: PropTypes.shape({
    id: PropTypes.string.isRequired,
    asunto: PropTypes.string.isRequired,
  }).isRequired,
  onClose: PropTypes.func.isRequired,
  onResolved: PropTypes.func.isRequired,
  onConflict: PropTypes.func.isRequired,
}
