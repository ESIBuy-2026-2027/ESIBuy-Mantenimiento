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
  // Lo que hace falta para el precio y para añadirlo al carrito (HU9). El precio lo calcula el servidor para el cliente.
  vendedor: PropTypes.shape({
    id: PropTypes.string.isRequired,
    nombreComercial: PropTypes.string.isRequired,
  }).isRequired,
  precio: PropTypes.number.isRequired,
  descuento: PropTypes.shape({ porcentaje: PropTypes.number.isRequired }),
  precioFinal: PropTypes.number.isRequired,
  stock: PropTypes.number.isRequired,
  disponible: PropTypes.bool.isRequired,
  valoracion: PropTypes.number,
  numValoraciones: PropTypes.number.isRequired,
})

// Producto tal y como lo ve el cliente en su página de detalle (HU8). `precioFinal` y `descuento` son los que le
// corresponden a quien consulta; `valoracion` es la media de 0 a 5, o null si nadie lo ha valorado.
export const productDetailPropType = PropTypes.shape({
  id: PropTypes.string.isRequired,
  nombre: PropTypes.string.isRequired,
  descripcion: PropTypes.string,
  imagen: PropTypes.string,
  categoriaId: PropTypes.string.isRequired,
  vendedor: PropTypes.shape({
    id: PropTypes.string.isRequired,
    nombreComercial: PropTypes.string.isRequired,
  }).isRequired,
  precio: PropTypes.number.isRequired,
  descuento: PropTypes.shape({ porcentaje: PropTypes.number.isRequired }),
  precioFinal: PropTypes.number.isRequired,
  stock: PropTypes.number.isRequired,
  disponible: PropTypes.bool.isRequired,
  valoracion: PropTypes.number,
  numValoraciones: PropTypes.number.isRequired,
})

// Valoración de un producto (HU13), como la devuelve GET /api/products/{id}/valoraciones
export const reviewPropType = PropTypes.shape({
  id: PropTypes.string.isRequired,
  puntuacion: PropTypes.number.isRequired,
  comentario: PropTypes.string,
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
