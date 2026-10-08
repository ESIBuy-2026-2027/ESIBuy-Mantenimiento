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

const priceFormatter = new Intl.NumberFormat('es-ES', { style: 'currency', currency: 'EUR' })

const useStyles = makeStyles({
  wrapper: {
    overflowX: 'auto',
    backgroundColor: tokens.colorNeutralBackground1,
    border: `1px solid ${tokens.colorNeutralStroke2}`,
    borderRadius: tokens.borderRadiusXLarge,
    boxShadow: tokens.shadow4,
    transitionProperty: 'opacity',
    transitionDuration: tokens.durationNormal,
    transitionTimingFunction: tokens.curveEasyEase,
    '@media (prefers-reduced-motion: reduce)': {
      transitionDuration: '0.01ms',
    },
  },
  refreshing: {
    opacity: 0.55,
  },
  row: {
    transitionProperty: 'background-color',
    transitionDuration: tokens.durationFast,
    ':hover': {
      backgroundColor: tokens.colorBrandBackground2,
    },
  },
  name: {
    minWidth: '180px',
  },
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

export default function ProductsTable({ products, refreshing }) {
  const styles = useStyles()

  return (
    <div className={mergeClasses(styles.wrapper, refreshing && styles.refreshing)} aria-busy={refreshing}>
      <Table aria-label="Mis productos">
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
            <TableRow key={product.id} className={styles.row}>
              <TableCell className={styles.name}>
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
              <TableCell>{priceFormatter.format(product.precio)}</TableCell>
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
