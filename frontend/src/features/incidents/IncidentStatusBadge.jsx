import PropTypes from 'prop-types'
import { Badge } from '@fluentui/react-components'
import { CheckmarkCircle16Filled, Clock16Regular, FolderOpen16Regular } from '@fluentui/react-icons'
import { ESTADO_INCIDENCIA, colorDeEstado, etiquetaDeEstado } from './incidentStatus'

const ICONOS = {
  [ESTADO_INCIDENCIA.PENDIENTE]: <Clock16Regular />,
  [ESTADO_INCIDENCIA.ABIERTA]: <FolderOpen16Regular />,
  [ESTADO_INCIDENCIA.RESUELTA]: <CheckmarkCircle16Filled />,
}

// El estado de una incidencia. Una resuelta (HU192) no se distingue solo por el color: además del texto, tiene su
// propio icono, va rellena y es rectangular, mientras que las demás son píldoras.
export default function IncidentStatusBadge({ estado }) {
  const resuelta = estado === ESTADO_INCIDENCIA.RESUELTA

  return (
    <Badge
      appearance={resuelta ? 'filled' : 'tint'}
      shape={resuelta ? 'square' : 'circular'}
      color={colorDeEstado(estado)}
      icon={ICONOS[estado]}
    >
      {etiquetaDeEstado(estado)}
    </Badge>
  )
}

IncidentStatusBadge.propTypes = {
  estado: PropTypes.string.isRequired,
}
