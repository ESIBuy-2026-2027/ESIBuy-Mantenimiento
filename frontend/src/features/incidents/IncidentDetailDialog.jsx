import PropTypes from 'prop-types'
import {
  Badge,
  Button,
  Dialog,
  DialogActions,
  DialogBody,
  DialogContent,
  DialogSurface,
  DialogTitle,
  Text,
  makeStyles,
  tokens,
} from '@fluentui/react-components'
import { formatDateTime } from '../../utils/format'
import { colorDeEstado, etiquetaDeEstado } from './incidentStatus'

const ROLES = { CLIENTE: 'Cliente', VENDEDOR: 'Vendedor' }

const useStyles = makeStyles({
  content: {
    display: 'flex',
    flexDirection: 'column',
    gap: tokens.spacingVerticalM,
  },
  header: {
    display: 'flex',
    flexWrap: 'wrap',
    alignItems: 'center',
    justifyContent: 'space-between',
    gap: tokens.spacingHorizontalM,
  },
  subject: {
    fontWeight: tokens.fontWeightSemibold,
    overflowWrap: 'anywhere',
  },
  meta: {
    display: 'flex',
    flexDirection: 'column',
    gap: tokens.spacingVerticalXXS,
  },
  muted: {
    color: tokens.colorNeutralForeground3,
  },
  section: {
    display: 'flex',
    flexDirection: 'column',
    gap: tokens.spacingVerticalXXS,
  },
  response: {
    display: 'flex',
    flexDirection: 'column',
    gap: tokens.spacingVerticalXXS,
    padding: tokens.spacingVerticalS,
    borderRadius: tokens.borderRadiusMedium,
    backgroundColor: tokens.colorNeutralBackground2,
  },
  text: {
    overflowWrap: 'anywhere',
    whiteSpace: 'pre-wrap',
  },
})

function textoAutor(autor) {
  if (!autor) return 'Cuenta eliminada'
  return `${autor.email} (${ROLES[autor.rol] ?? autor.rol})`
}

// Diálogo de detalle de una incidencia para el administrador (PBI 239).
export default function IncidentDetailDialog({ incident, onClose }) {
  const styles = useStyles()

  return (
    <Dialog open onOpenChange={(_event, data) => !data.open && onClose()}>
      <DialogSurface>
        <DialogBody>
          <DialogTitle>Detalle de la incidencia</DialogTitle>
          <DialogContent className={styles.content}>
            <div className={styles.header}>
              <Text size={400} className={styles.subject}>
                {incident.asunto}
              </Text>
              <Badge appearance="tint" color={colorDeEstado(incident.estado)}>
                {etiquetaDeEstado(incident.estado)}
              </Badge>
            </div>
            <div className={styles.meta}>
              <Text size={200} className={styles.muted}>
                Fecha: <time dateTime={incident.fechaCreacion}>{formatDateTime(incident.fechaCreacion)}</time>
              </Text>
              <Text size={200} className={styles.muted}>
                Autor: {textoAutor(incident.autor)}
              </Text>
            </div>
            <div className={styles.section}>
              <Text weight="semibold" size={200}>
                Descripción
              </Text>
              <Text className={styles.text}>{incident.descripcion}</Text>
            </div>
            <div className={styles.response}>
              <Text weight="semibold" size={200}>
                Respuesta del administrador
              </Text>
              <Text className={incident.respuesta ? styles.text : styles.muted}>
                {incident.respuesta || 'Sin respuesta todavía'}
              </Text>
            </div>
          </DialogContent>
          <DialogActions>
            <Button appearance="primary" onClick={onClose}>
              Volver
            </Button>
          </DialogActions>
        </DialogBody>
      </DialogSurface>
    </Dialog>
  )
}

IncidentDetailDialog.propTypes = {
  incident: PropTypes.shape({
    id: PropTypes.string.isRequired,
    asunto: PropTypes.string.isRequired,
    descripcion: PropTypes.string.isRequired,
    estado: PropTypes.string.isRequired,
    respuesta: PropTypes.string.isRequired,
    fechaCreacion: PropTypes.string.isRequired,
    autor: PropTypes.shape({
      email: PropTypes.string.isRequired,
      rol: PropTypes.string.isRequired,
    }),
  }).isRequired,
  onClose: PropTypes.func.isRequired,
}
