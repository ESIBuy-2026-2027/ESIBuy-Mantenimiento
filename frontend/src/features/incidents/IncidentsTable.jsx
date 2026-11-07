import PropTypes from 'prop-types'
import {
  Badge,
  Button,
  Table,
  TableBody,
  TableCell,
  TableHeader,
  TableHeaderCell,
  TableRow,
  Text,
  Tooltip,
  makeStyles,
  mergeClasses,
  tokens,
} from '@fluentui/react-components'
import { Dismiss20Regular } from '@fluentui/react-icons'
import { useTableStyles } from '../../components/tableStyles'
import { formatDateTime } from '../../utils/format'
import { colorDeEstado, etiquetaDeEstado } from './incidentStatus'

const SIN_RESPUESTA = 'Sin respuesta todavía'

const useStyles = makeStyles({
  // En pantallas estrechas las columnas no se aprietan hasta no poder leerse: la tabla mantiene un ancho mínimo y se
  // desplaza dentro de su contenedor (sin scroll horizontal de la página).
  table: {
    minWidth: '860px',
  },
  subject: {
    minWidth: '160px',
    overflowWrap: 'anywhere',
  },
  // Las celdas de la descripción y de la respuesta son más anchas que las demás para que el texto no se monte sobre las
  // columnas de al lado; si no cabe, la tabla se desplaza dentro de su contenedor.
  wide: {
    minWidth: '220px',
    flexGrow: 2,
  },
  // Se recortan a tres líneas en pantalla; el texto completo sigue en el documento
  text: {
    overflowWrap: 'anywhere',
    display: '-webkit-box',
    WebkitBoxOrient: 'vertical',
    WebkitLineClamp: 3,
    overflow: 'hidden',
  },
  muted: {
    color: tokens.colorNeutralForeground3,
  },
  date: {
    whiteSpace: 'nowrap',
  },
})

// Tabla «Mis incidencias» (HU44). Las incidencias se muestran en el orden en que llegan del backend.
export default function IncidentsTable({ incidents, refreshing = false }) {
  const styles = useStyles()
  const tableStyles = useTableStyles()

  return (
    <div className={mergeClasses(tableStyles.wrapper, refreshing && tableStyles.refreshing)} aria-busy={refreshing}>
      <Table aria-label="Mis incidencias" className={styles.table}>
        <TableHeader>
          <TableRow>
            <TableHeaderCell>Asunto</TableHeaderCell>
            <TableHeaderCell>Descripción</TableHeaderCell>
            <TableHeaderCell>Estado</TableHeaderCell>
            <TableHeaderCell>Respuesta</TableHeaderCell>
            <TableHeaderCell>Fecha de creación</TableHeaderCell>
            <TableHeaderCell>Acciones</TableHeaderCell>
          </TableRow>
        </TableHeader>
        <TableBody>
          {incidents.map((incident) => (
            <TableRow key={incident.id} className={tableStyles.row}>
              <TableCell className={styles.subject}>
                <Text weight="semibold">{incident.asunto}</Text>
              </TableCell>
              <TableCell className={styles.wide}>
                <Text className={styles.text} title={incident.descripcion}>
                  {incident.descripcion}
                </Text>
              </TableCell>
              <TableCell>
                <Badge appearance="tint" color={colorDeEstado(incident.estado)}>
                  {etiquetaDeEstado(incident.estado)}
                </Badge>
              </TableCell>
              <TableCell className={styles.wide}>
                {incident.respuesta ? (
                  <Text className={styles.text} title={incident.respuesta}>
                    {incident.respuesta}
                  </Text>
                ) : (
                  <Text className={styles.muted}>{SIN_RESPUESTA}</Text>
                )}
              </TableCell>
              <TableCell className={styles.date}>
                <time dateTime={incident.fechaCreacion}>{formatDateTime(incident.fechaCreacion)}</time>
              </TableCell>
              <TableCell>
                {/* Cerrar una incidencia todavía no está disponible: se conectará con la HU50. Mientras tanto el botón
                    se ve, se enfoca con el teclado y lo explica, pero no hace nada. */}
                <Tooltip content="Disponible pronto" relationship="description">
                  <Button
                    size="small"
                    icon={<Dismiss20Regular />}
                    disabledFocusable
                    aria-label={`Cerrar incidencia: ${incident.asunto}`}
                  >
                    Cerrar
                  </Button>
                </Tooltip>
              </TableCell>
            </TableRow>
          ))}
        </TableBody>
      </Table>
    </div>
  )
}

IncidentsTable.propTypes = {
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
