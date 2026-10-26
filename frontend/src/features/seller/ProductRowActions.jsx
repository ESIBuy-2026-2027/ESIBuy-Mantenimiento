import PropTypes from 'prop-types'
import { Button } from '@fluentui/react-components'
import { Delete20Regular, Edit20Regular, Eye20Regular } from '@fluentui/react-icons'
import LinkButton from '../../components/LinkButton'
import { getSellerProductEditPath } from '../../routes/paths'
import { productPropType } from '../../utils/propTypes'

// Acciones de una fila del listado de productos del vendedor: ver su ficha (HU33), editar (abre el formulario)
// y eliminar.
export default function ProductRowActions({ product, onView, onDelete }) {
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
  onDelete: PropTypes.func.isRequired,
}
