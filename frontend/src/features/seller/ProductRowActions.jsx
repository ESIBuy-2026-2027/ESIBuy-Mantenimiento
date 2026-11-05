import PropTypes from 'prop-types'
import { Button } from '@fluentui/react-components'
import {
  CheckmarkCircle20Regular,
  Delete20Regular,
  Edit20Regular,
  Eye20Regular,
  Prohibited20Regular,
} from '@fluentui/react-icons'
import LinkButton from '../../components/LinkButton'
import { getSellerProductEditPath } from '../../routes/paths'
import { productPropType } from '../../utils/propTypes'

// Acciones de una fila del listado de productos del vendedor: ver su ficha (HU33), editar (abre el formulario),
// activar o desactivar (HU16; un producto disponible se desactiva y uno no disponible se activa) y eliminar.
export default function ProductRowActions({ product, onView, onToggleAvailability, onDelete }) {
  const availabilityAction = product.disponible ? 'Desactivar' : 'Activar'

  return (
    <>
      <Button
        appearance="subtle"
        icon={<Eye20Regular />}
        aria-label={`Ver ${product.nombre}`}
        onClick={() => onView(product)}
      />
      <LinkButton
        to={getSellerProductEditPath(product.id)}
        appearance="subtle"
        icon={<Edit20Regular />}
        aria-label={`Editar ${product.nombre}`}
      />
      <Button
        appearance="subtle"
        icon={product.disponible ? <Prohibited20Regular /> : <CheckmarkCircle20Regular />}
        aria-label={`${availabilityAction} ${product.nombre}`}
        title={availabilityAction}
        onClick={() => onToggleAvailability(product)}
      />
      <Button
        appearance="subtle"
        icon={<Delete20Regular />}
        aria-label={`Eliminar ${product.nombre}`}
        onClick={() => onDelete(product)}
      />
    </>
  )
}

ProductRowActions.propTypes = {
  product: productPropType.isRequired,
  onView: PropTypes.func.isRequired,
  onToggleAvailability: PropTypes.func.isRequired,
  onDelete: PropTypes.func.isRequired,
}
