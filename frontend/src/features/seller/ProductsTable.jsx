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

const useStyles = makeStyles({
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
                <Text weight="semibold">{product.nombre}</Text>
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
