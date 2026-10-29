import PropTypes from 'prop-types'

// Forma de una dirección de envío tal como la devuelve el backend (HU5)
export const direccionPropType = PropTypes.shape({
  id: PropTypes.string.isRequired,
  destinatario: PropTypes.string.isRequired,
  calle: PropTypes.string.isRequired,
  detalles: PropTypes.string,
  codigoPostal: PropTypes.string.isRequired,
  ciudad: PropTypes.string.isRequired,
  provincia: PropTypes.string.isRequired,
  telefono: PropTypes.string.isRequired,
  principal: PropTypes.bool.isRequired,
})

// Una línea con la dirección, para la lista y para elegir la nueva principal
export const resumenDeDireccion = ({ calle, detalles, codigoPostal, ciudad, provincia }) =>
  `${[calle, detalles].filter(Boolean).join(', ')} · ${codigoPostal} ${ciudad} (${provincia})`
