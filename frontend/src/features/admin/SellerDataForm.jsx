import PropTypes from 'prop-types'
import { ROLES } from '../../auth/roles'
import ReadOnlyFields from './ReadOnlyFields'
import { readFields } from './profileFields'

export default function SellerDataForm({ seller }) {
  return <ReadOnlyFields fields={readFields(ROLES.VENDEDOR, seller)} />
}

SellerDataForm.propTypes = {
  seller: PropTypes.shape({
    nombre: PropTypes.string.isRequired,
    apellidos: PropTypes.string.isRequired,
    email: PropTypes.string.isRequired,
    // Un vendedor anterior a su registro (T241) no tiene datos de vendedor.
    nombreComercial: PropTypes.string,
    dni: PropTypes.string,
    telefono: PropTypes.string,
    categoriaPrincipal: PropTypes.string,
    avatar: PropTypes.string,
    estado: PropTypes.string.isRequired,
  }).isRequired,
}
