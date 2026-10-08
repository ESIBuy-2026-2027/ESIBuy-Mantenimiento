import PropTypes from 'prop-types'

export const categoryPropType = PropTypes.shape({
  id: PropTypes.string.isRequired,
  nombre: PropTypes.string.isRequired,
})

export const productPropType = PropTypes.shape({
  id: PropTypes.string.isRequired,
  nombre: PropTypes.string.isRequired,
  precio: PropTypes.number.isRequired,
  stock: PropTypes.number.isRequired,
  disponible: PropTypes.bool.isRequired,
  categorias: PropTypes.arrayOf(PropTypes.string).isRequired,
})

export const productValuesPropType = PropTypes.shape({
  nombre: PropTypes.string.isRequired,
  descripcion: PropTypes.string.isRequired,
  precio: PropTypes.string.isRequired,
  stock: PropTypes.string.isRequired,
  categoriaIds: PropTypes.arrayOf(PropTypes.string).isRequired,
  imagen: PropTypes.string.isRequired,
  disponible: PropTypes.bool.isRequired,
  descuentoActivo: PropTypes.bool.isRequired,
  descuentoPorcentaje: PropTypes.string.isRequired,
  descuentoPremiumActivo: PropTypes.bool.isRequired,
  descuentoPremiumPorcentaje: PropTypes.string.isRequired,
})
