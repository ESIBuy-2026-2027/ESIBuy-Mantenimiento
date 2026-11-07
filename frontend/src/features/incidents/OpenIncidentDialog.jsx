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
  Field,
  MessageBar,
  MessageBarBody,
  Spinner,
  Text,
  Textarea,
  makeStyles,
  tokens,
} from '@fluentui/react-components'
import { openIncident } from '../../api/adminIncidents'
import { RESPONSE_MAX, validateResponse } from './incidentRules'

const useStyles = makeStyles({
  content: {
    display: 'flex',
    flexDirection: 'column',
    gap: tokens.spacingVerticalM,
  },
  subject: {
    fontWeight: tokens.fontWeightSemibold,
    overflowWrap: 'anywhere',
  },
})

// Códigos con los que el backend dice que otro administrador ya ha cambiado la incidencia (HU50)
const CAMBIADA_POR_OTRO = new Set(['INCIDENT_ALREADY_OPEN', 'INCIDENT_NOT_FOUND'])

// Diálogo para abrir una incidencia pendiente con la respuesta del administrador (HU50), que es lo que verá su autor.
// Como IncidentForm, «obligatoria» sale al intentar enviar y pasarse del máximo, en cuanto ocurre. Si sale bien
// entrega la incidencia abierta; si otro administrador ya la ha abierto o cerrado (409 o 404), lo comunica a la
// página, que vuelve a cargar la lista; cualquier otro fallo se avisa aquí para poder reintentar.
export default function OpenIncidentDialog({ incident, onClose, onOpened, onConflict }) {
  const styles = useStyles()
  const [respuesta, setRespuesta] = useState('')
  const [attempted, setAttempted] = useState(false)
  const [abriendo, setAbriendo] = useState(false)
  const [fallo, setFallo] = useState(false)

  const longitud = respuesta.trim().length
  const error = validateResponse(respuesta)
  const errorVisible = attempted || longitud > RESPONSE_MAX ? error : null

  const confirmar = async (event) => {
    event.preventDefault()
    if (abriendo) return

    setAttempted(true)
    if (error) return

    setAbriendo(true)
    setFallo(false)
    try {
      onOpened(await openIncident(incident.id, respuesta.trim()))
    } catch (err) {
      if (CAMBIADA_POR_OTRO.has(err?.code)) {
        onConflict()
        return
      }
      setFallo(true)
      setAbriendo(false)
    }
  }

  return (
    <Dialog open onOpenChange={(_event, data) => !data.open && !abriendo && onClose()}>
      <DialogSurface>
        <form onSubmit={confirmar} noValidate>
          <DialogBody>
            <DialogTitle>Abrir incidencia</DialogTitle>
            <DialogContent className={styles.content}>
              {fallo && (
                <MessageBar intent="error">
                  <MessageBarBody>No se ha podido abrir la incidencia. Inténtalo de nuevo.</MessageBarBody>
                </MessageBar>
              )}
              <Text className={styles.subject}>{incident.asunto}</Text>
              <Field
                label="Respuesta"
                required
                hint={`${longitud}/${RESPONSE_MAX} · La verá quien reportó la incidencia.`}
                validationState={errorVisible ? 'error' : 'none'}
                validationMessage={errorVisible}
              >
                <Textarea
                  value={respuesta}
                  onChange={(_event, data) => setRespuesta(data.value)}
                  rows={5}
                  resize="vertical"
                />
              </Field>
            </DialogContent>
            <DialogActions>
              <Button appearance="secondary" onClick={onClose} disabled={abriendo}>
                Cancelar
              </Button>
              <Button
                type="submit"
                appearance="primary"
                disabled={abriendo}
                icon={abriendo ? <Spinner size="tiny" /> : undefined}
              >
                Abrir incidencia
              </Button>
            </DialogActions>
          </DialogBody>
        </form>
      </DialogSurface>
    </Dialog>
  )
}

OpenIncidentDialog.propTypes = {
  incident: PropTypes.shape({
    id: PropTypes.string.isRequired,
    asunto: PropTypes.string.isRequired,
  }).isRequired,
  onClose: PropTypes.func.isRequired,
  onOpened: PropTypes.func.isRequired,
  onConflict: PropTypes.func.isRequired,
}
