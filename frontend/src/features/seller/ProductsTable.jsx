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

const useStyles = makeStyles({
  categories: {
    display: 'flex',
    flexWrap: 'wrap',
    gap: tokens.spacingHorizontalXS,
  },
  noStock: {
    color: tokens.colorPaletteRedForeground1,
    fontWeight: tokens.fontWeightSemibold,
  },
})

export default function ProductsTable({ products, refreshing, label = 'Mis productos' }) {
  const styles = useStyles()
  const tableStyles = useTableStyles()

  return (
    <div className={mergeClasses(tableStyles.wrapper, refreshing && tableStyles.refreshing)} aria-busy={refreshing}>
      <Table aria-label={label}>
        <TableHeader>
          <TableRow>
            <TableHeaderCell>Producto</TableHeaderCell>
            <TableHeaderCell>Categorías</TableHeaderCell>
            <TableHeaderCell>Precio</TableHeaderCell>
            <TableHeaderCell>Stock</TableHeaderCell>
            <TableHeaderCell>Estado</TableHeaderCell>
          </TableRow>
        </TableHeader>
        <TableBody>
          {products.map((product) => (
            <TableRow key={product.id} className={tableStyles.row}>
              <TableCell className={tableStyles.name}>
                <Text weight="semibold">{product.nombre}</Text>
              </TableCell>
              <TableCell>
                <span className={styles.categories}>
                  {product.categorias.map((category) => (
                    <Badge key={category} appearance="outline" color="brand" size="medium">
                      {category}
                    </Badge>
                  ))}
                </span>
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
            </TableRow>
          ))}
        </TableBody>
      </Table>
    </div>
  )
}
