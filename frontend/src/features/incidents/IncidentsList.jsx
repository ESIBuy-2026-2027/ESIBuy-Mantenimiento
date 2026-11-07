import PropTypes from 'prop-types'
import { Badge, Card, Text, makeStyles, tokens } from '@fluentui/react-components'
import { formatDateTime } from '../../utils/format'
import { colorDeEstado, etiquetaDeEstado } from './incidentStatus'

const SIN_RESPUESTA = 'Sin respuesta todavía'

const useStyles = makeStyles({
  list: {
    listStyle: 'none',
    margin: 0,
    padding: 0,
    display: 'flex',
    flexDirection: 'column',
    gap: tokens.spacingVerticalM,
    transitionProperty: 'opacity',
    transitionDuration: tokens.durationNormal,
    transitionTimingFunction: tokens.curveEasyEase,
    '@media (prefers-reduced-motion: reduce)': {
      transitionDuration: '0.01ms',
    },
  },
  refreshing: {
    opacity: 0.55,
  },
  // En pantallas estrechas el estado baja a una segunda línea
  header: {
    display: 'flex',
    flexWrap: 'wrap',
    alignItems: 'center',
    justifyContent: 'space-between',
    gap: tokens.spacingHorizontalM,
  },
  title: {
    display: 'flex',
    flexDirection: 'column',
    flexGrow: 1,
    minWidth: 0,
  },
  subject: {
    fontWeight: tokens.fontWeightSemibold,
    color: tokens.colorBrandForeground1,
    overflowWrap: 'anywhere',
  },
  muted: {
    color: tokens.colorNeutralForeground3,
  },
  text: {
    overflowWrap: 'anywhere',
    whiteSpace: 'pre-wrap',
  },
  response: {
    display: 'flex',
    flexDirection: 'column',
    gap: tokens.spacingVerticalXXS,
    padding: tokens.spacingVerticalS,
    borderRadius: tokens.borderRadiusMedium,
    backgroundColor: tokens.colorNeutralBackground2,
  },
})

// Las incidencias del usuario (HU44), una tarjeta cada una como los pedidos de la HU11, en el orden en que llegan del
// backend: de las más antiguas a las más recientes.
export default function IncidentsList({ incidents, refreshing = false }) {
  const styles = useStyles()

  return (
    <ul
      aria-label="Tus incidencias"
      aria-busy={refreshing}
      className={refreshing ? `${styles.list} ${styles.refreshing}` : styles.list}
    >
      {incidents.map((incident) => (
        <li key={incident.id}>
          <Card>
            <div className={styles.header}>
              <div className={styles.title}>
                <Text className={styles.subject}>{incident.asunto}</Text>
                <Text className={styles.muted}>
                  <time dateTime={incident.fechaCreacion}>{formatDateTime(incident.fechaCreacion)}</time>
                </Text>
              </div>
              <Badge appearance="tint" color={colorDeEstado(incident.estado)}>
                {etiquetaDeEstado(incident.estado)}
              </Badge>
            </div>
            <Text className={styles.text}>{incident.descripcion}</Text>
            <div className={styles.response}>
              <Text weight="semibold" size={200}>
                Respuesta del administrador
              </Text>
              <Text className={incident.respuesta ? styles.text : styles.muted}>
                {incident.respuesta || SIN_RESPUESTA}
              </Text>
            </div>
          </Card>
        </li>
      ))}
    </ul>
  )
}

IncidentsList.propTypes = {
  incidents: PropTypes.arrayOf(
    PropTypes.shape({
      id: PropTypes.string.isRequired,
      asunto: PropTypes.string.isRequired,
      descripcion: PropTypes.string.isRequired,
      estado: PropTypes.string.isRequired,
      respuesta: PropTypes.string.isRequired,
      fechaCreacion: PropTypes.string.isRequired,
    }),
  ).isRequired,
  refreshing: PropTypes.bool,
}
