import PropTypes from 'prop-types'
import { formatDate } from '../../utils/format'
import ReadOnlyFields from './ReadOnlyFields'
import { getSellerStatus } from './sellerStatus'

export default function SellerDataForm({ seller }) {
  const fields = [
    ['Nombre', seller.nombre],
    ['Apellidos', seller.apellidos],
    ['Correo electrónico', seller.email],
    ['Nombre comercial', seller.nombreComercial],
    ['NIF/DNI', seller.nif],
    ['Categoría principal', seller.categoriaPrincipal],
    ['Estado', getSellerStatus(seller.estado).label],
    ['Fecha de alta', formatDate(seller.fechaAlta)],
  ]

  return <ReadOnlyFields fields={fields} />
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
    fechaAlta: PropTypes.string.isRequired,
  }).isRequired,
}
