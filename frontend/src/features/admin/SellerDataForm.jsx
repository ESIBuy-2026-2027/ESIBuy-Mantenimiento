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
    nombreComercial: PropTypes.string.isRequired,
    nif: PropTypes.string.isRequired,
    categoriaPrincipal: PropTypes.string.isRequired,
    estado: PropTypes.string.isRequired,
  }).isRequired,
}
