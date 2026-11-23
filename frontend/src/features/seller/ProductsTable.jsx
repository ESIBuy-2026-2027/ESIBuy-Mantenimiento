import PropTypes from 'prop-types'
import { categoryPropType, productPropType } from '../../utils/propTypes'
import {
  Badge,
  Table,
  TableBody,
  TableCell,
  TableHeader,
  TableHeaderCell,
  TableRow,
  Text,
  makeStyles,
  mergeClasses,
  tokens,
} from '@fluentui/react-components'
import { useTableStyles } from '../../components/tableStyles'
import { formatPrice } from '../../utils/format'

const SIN_CATEGORIA = '—'

const fallbackImage = (nombre) => {
  const inicial = nombre.trim().charAt(0).toUpperCase() || 'P'
  const svg =
    `<svg xmlns="http://www.w3.org/2000/svg" width="96" height="96" viewBox="0 0 96 96">` +
    `<rect width="96" height="96" rx="8" fill="#FFF4EA"/>` +
    `<text x="48" y="61" text-anchor="middle" font-family="Segoe UI, Arial" font-size="42" ` +
    `font-weight="700" fill="#EA580C">${inicial}</text></svg>`
  return `data:image/svg+xml,${encodeURIComponent(svg)}`
}

const useStyles = makeStyles({
  product: {
    display: 'flex',
    alignItems: 'center',
    gap: tokens.spacingHorizontalM,
  },
  thumbnailWrap: {
    position: 'relative',
    width: '48px',
    height: '48px',
    flexShrink: 0,
  },
  thumbnail: {
    position: 'relative',
    width: '48px',
    height: '48px',
    borderRadius: tokens.borderRadiusMedium,
    objectFit: 'cover',
  },
  noStock: {
    color: tokens.colorPaletteRedForeground1,
    fontWeight: tokens.fontWeightSemibold,
  },
})

// El backend devuelve el identificador de la categoría de cada producto (`categoriaId`), no su nombre: la tabla lo
// traduce con la lista de categorías que recibe en `categories`. Si no la tiene (todavía carga o ha fallado) o la
// categoría no existe, muestra un guion en lugar de un identificador.
// `renderActions` es opcional: si se recibe, añade una columna con las acciones de cada fila.
export default function ProductsTable({ products, categories = [], refreshing, label = 'Mis productos', renderActions }) {
  const styles = useStyles()
  const tableStyles = useTableStyles()
  const categoryNames = new Map(categories.map(({ id, nombre }) => [id, nombre]))

  return (
    <div className={mergeClasses(tableStyles.wrapper, refreshing && tableStyles.refreshing)} aria-busy={refreshing}>
      <Table aria-label={label}>
        <TableHeader>
          <TableRow>
            <TableHeaderCell>Producto</TableHeaderCell>
            <TableHeaderCell>Categoría</TableHeaderCell>
            <TableHeaderCell>Precio</TableHeaderCell>
            <TableHeaderCell>Stock</TableHeaderCell>
            <TableHeaderCell>Estado</TableHeaderCell>
            {renderActions && <TableHeaderCell>Acciones</TableHeaderCell>}
          </TableRow>
        </TableHeader>
        <TableBody>
          {products.map((product) => (
            <TableRow key={product.id} className={tableStyles.row}>
              <TableCell className={tableStyles.name}>
                <div className={styles.product}>
                  <div className={styles.thumbnailWrap}>
                    <img
                      className={styles.thumbnail}
                      src={product.imagen || fallbackImage(product.nombre)}
                      alt={`Miniatura de ${product.nombre}`}
                      onError={(event) => {
                        event.currentTarget.onerror = null
                        event.currentTarget.src = fallbackImage(product.nombre)
                      }}
                    />
                  </div>
                  <Text weight="semibold">{product.nombre}</Text>
                </div>
              </TableCell>
              <TableCell>
                {categoryNames.has(product.categoriaId) ? (
                  <Badge appearance="outline" color="brand" size="medium">
                    {categoryNames.get(product.categoriaId)}
                  </Badge>
                ) : (
                  <Text>{SIN_CATEGORIA}</Text>
                )}
              </TableCell>
              <TableCell>{formatPrice(product.precio)}</TableCell>
              <TableCell>
                <span className={product.stock === 0 ? styles.noStock : undefined}>{product.stock}</span>
              </TableCell>
              <TableCell>
                <Badge appearance="tint" color={product.disponible ? 'success' : 'subtle'}>
                  {product.disponible ? 'Disponible' : 'No disponible'}
                </Badge>
              </TableCell>
              {renderActions && <TableCell>{renderActions(product)}</TableCell>}
            </TableRow>
          ))}
        </TableBody>
      </Table>
    </div>
  )
}

ProductsTable.propTypes = {
  products: PropTypes.arrayOf(productPropType).isRequired,
  categories: PropTypes.arrayOf(categoryPropType),
  refreshing: PropTypes.bool,
  label: PropTypes.string,
  renderActions: PropTypes.func,
}
