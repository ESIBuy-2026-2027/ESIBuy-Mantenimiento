import PropTypes from 'prop-types'
import { Button, Tooltip } from '@fluentui/react-components'
import { Delete20Regular, Edit20Regular, Info20Regular } from '@fluentui/react-icons'
import LinkButton from '../../components/LinkButton'
import { getSellerProductEditPath } from '../../routes/paths'
import { productPropType } from '../../utils/propTypes'

// Acciones de cada fila en "Mis productos": ficha en modal, editar (HU34) y eliminar (HU35).
export default function ProductRowActions({ product, onView, onDelete }) {
  return (
    <>
      <Tooltip content="Información del producto" relationship="label">
        <Button
          appearance="subtle"
          aria-label={`Información de ${product.nombre}`}
          icon={<Info20Regular />}
          onClick={() => onView(product)}
        />
      </Tooltip>
      <Tooltip content="Editar" relationship="label">
        <LinkButton
          appearance="subtle"
          aria-label={`Editar ${product.nombre}`}
          to={getSellerProductEditPath(product.id)}
          icon={<Edit20Regular />}
        />
      </Tooltip>
      <Tooltip content="Eliminar" relationship="label">
        <Button
          appearance="subtle"
          aria-label={`Eliminar ${product.nombre}`}
          icon={<Delete20Regular />}
          onClick={() => onDelete(product)}
        />
      </Tooltip>
    </>
  )
}

ProductRowActions.propTypes = {
  product: productPropType.isRequired,
  onView: PropTypes.func.isRequired,
  onDelete: PropTypes.func.isRequired,
}