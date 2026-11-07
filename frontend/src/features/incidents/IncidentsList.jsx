import PropTypes from 'prop-types'
import { Badge, Card, Text, makeStyles, tokens } from '@fluentui/react-components'
import { formatDateTime } from '../../utils/format'
import { colorDeEstado, etiquetaDeEstado } from './incidentStatus'

const SIN_RESPUESTA = 'Sin respuesta todavía'
const CUENTA_ELIMINADA = 'Cuenta eliminada'

// Roles que pueden reportar una incidencia; si llega otro, se muestra tal cual
const ROLES = { CLIENTE: 'Cliente', VENDEDOR: 'Vendedor' }

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
  author: {
    display: 'flex',
    flexWrap: 'wrap',
    alignItems: 'center',
    gap: tokens.spacingHorizontalS,
  },
  email: {
    overflowWrap: 'anywhere',
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

// Quién reportó la incidencia, para el administrador (HU49): su correo y su rol, o «Cuenta eliminada» si el backend
// la envía sin autor
function IncidentAuthor({ author }) {
  const styles = useStyles()

  if (!author) {
    return <Text className={styles.muted}>{CUENTA_ELIMINADA}</Text>
  }
  return (
    <div className={styles.author}>
      <Text className={styles.email}>{author.email}</Text>
      <Badge appearance="outline" color="informative">
        {ROLES[author.rol] ?? author.rol}
      </Badge>
    </div>
  )
}

IncidentAuthor.propTypes = {
  author: PropTypes.shape({
    email: PropTypes.string.isRequired,
    rol: PropTypes.string.isRequired,
  }),
}

// Las incidencias, una tarjeta cada una como los pedidos de la HU11, en el orden en que llegan del backend: de las más
// antiguas a las más recientes. Las del usuario en sesión (HU44) y, con `showAuthor`, las de todos para el
// administrador (HU49), cada una con quién la reportó.
export default function IncidentsList({ incidents, refreshing = false, showAuthor = false, label = 'Tus incidencias' }) {
  const styles = useStyles()

  return (
    <ul
      aria-label={label}
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
                {showAuthor && <IncidentAuthor author={incident.autor} />}
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
      // Solo en la supervisión del administrador (HU49); null si la cuenta del autor se ha eliminado
      autor: PropTypes.shape({
        email: PropTypes.string.isRequired,
        rol: PropTypes.string.isRequired,
      }),
    }),
  ).isRequired,
  refreshing: PropTypes.bool,
  showAuthor: PropTypes.bool,
  // Nombre accesible de la lista
  label: PropTypes.string,
}
