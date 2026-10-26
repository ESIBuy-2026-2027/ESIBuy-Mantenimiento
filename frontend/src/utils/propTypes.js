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
  categoriaId: PropTypes.string.isRequired,
})

// Producto tal y como lo ve el cliente en el catálogo; `valoracion` es la media de 0 a 5, o null si nadie lo ha valorado.
export const catalogProductPropType = PropTypes.shape({
  id: PropTypes.string.isRequired,
  nombre: PropTypes.string.isRequired,
  imagen: PropTypes.string,
  categoriaId: PropTypes.string.isRequired,
  valoracion: PropTypes.number,
  numValoraciones: PropTypes.number.isRequired,
})

export const productValuesPropType = PropTypes.shape({
  nombre: PropTypes.string.isRequired,
  descripcion: PropTypes.string.isRequired,
  precio: PropTypes.string.isRequired,
  stock: PropTypes.string.isRequired,
  categoriaId: PropTypes.string.isRequired,
  imagen: PropTypes.string.isRequired,
  disponible: PropTypes.bool.isRequired,
  descuentoActivo: PropTypes.bool.isRequired,
  descuentoPorcentaje: PropTypes.string.isRequired,
  descuentoPremiumActivo: PropTypes.bool.isRequired,
  descuentoPremiumPorcentaje: PropTypes.string.isRequired,
})
