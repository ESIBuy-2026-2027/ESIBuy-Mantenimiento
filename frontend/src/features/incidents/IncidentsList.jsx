import PropTypes from 'prop-types'
import { Badge, Card, Text, makeStyles, tokens } from '@fluentui/react-components'
import { formatDateTime } from '../../utils/format'
import IncidentStatusBadge from './IncidentStatusBadge'
import { ESTADO_INCIDENCIA } from './incidentStatus'

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
  // Una incidencia resuelta (HU192) lleva además una banda en el borde izquierdo de su tarjeta
  resolved: {
    borderLeftWidth: tokens.strokeWidthThickest,
    borderLeftStyle: 'solid',
    borderLeftColor: tokens.colorPaletteGreenBorderActive,
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
  // Acciones del administrador sobre la incidencia (HU50), alineadas a la derecha; en pantallas estrechas se apilan
  actions: {
    display: 'flex',
    flexWrap: 'wrap',
    justifyContent: 'flex-end',
    gap: tokens.spacingHorizontalS,
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
// administrador (HU49), cada una con quién la reportó. Con `renderActions`, cada tarjeta acaba con las acciones que
// devuelve para su incidencia (abrir y cerrar, HU50, y marcar como resuelta, HU192).
export default function IncidentsList({
  incidents,
  refreshing = false,
  showAuthor = false,
  compact = false,
  label = 'Tus incidencias',
  renderActions,
}) {
  const styles = useStyles()

  return (
    <ul
      aria-label={label}
      aria-busy={refreshing}
      className={refreshing ? `${styles.list} ${styles.refreshing}` : styles.list}
    >
      {incidents.map((incident) => (
        <li key={incident.id}>
          <Card className={incident.estado === ESTADO_INCIDENCIA.RESUELTA ? styles.resolved : undefined}>
            <div className={styles.header}>
              <div className={styles.title}>
                <Text className={styles.subject}>{incident.asunto}</Text>
                {!compact && (
                  <Text className={styles.muted}>
                    <time dateTime={incident.fechaCreacion}>{formatDateTime(incident.fechaCreacion)}</time>
                  </Text>
                )}
                {!compact && incident.fechaResolucion && (
                  <Text className={styles.muted}>
                    Resuelta el{' '}
                    <time dateTime={incident.fechaResolucion}>{formatDateTime(incident.fechaResolucion)}</time>
                  </Text>
                )}
                {!compact && showAuthor && <IncidentAuthor author={incident.autor} />}
              </div>
              <IncidentStatusBadge estado={incident.estado} />
            </div>
            <Text className={styles.text}>{incident.descripcion}</Text>
            {!compact && (
              <div className={styles.response}>
                <Text weight="semibold" size={200}>
                  Respuesta del administrador
                </Text>
                <Text className={incident.respuesta ? styles.text : styles.muted}>
                  {incident.respuesta || SIN_RESPUESTA}
                </Text>
              </div>
            )}
            {renderActions && <div className={styles.actions}>{renderActions(incident)}</div>}
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
      // Solo las resueltas (HU192); null en las demás
      fechaResolucion: PropTypes.string,
      // Solo en la supervisión del administrador (HU49); null si la cuenta del autor se ha eliminado
      autor: PropTypes.shape({
        email: PropTypes.string.isRequired,
        rol: PropTypes.string.isRequired,
      }),
    }),
  ).isRequired,
  refreshing: PropTypes.bool,
  showAuthor: PropTypes.bool,
  // En resumen solo muestra asunto, descripción y estado; el resto va en el diálogo de detalle (PBI 239)
  compact: PropTypes.bool,
  // Nombre accesible de la lista
  label: PropTypes.string,
  // Recibe la incidencia y devuelve sus acciones (HU50); sin él, las tarjetas no tienen acciones
  renderActions: PropTypes.func,
}
