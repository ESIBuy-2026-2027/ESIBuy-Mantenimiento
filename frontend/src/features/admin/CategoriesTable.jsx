import PropTypes from 'prop-types'
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
} from '@fluentui/react-components'
import { useTableStyles } from '../../components/tableStyles'
import { getCategoryStatus } from './categoryStatus'

const COLUMNS = ['Nombre', 'Descripción', 'Estado', 'Productos']

const useStyles = makeStyles({
  // Una descripción larga se corta con puntos suspensivos; la ficha de la categoría la muestra entera.
  description: {
    display: 'block',
    maxWidth: '360px',
    overflow: 'hidden',
    textOverflow: 'ellipsis',
    whiteSpace: 'nowrap',
  },
  count: {
    fontVariantNumeric: 'tabular-nums',
  },
})

// renderActions es opcional: si se recibe, se añade la columna "Acciones" con lo que devuelva para cada categoría
// (la lupa que abre su ficha).
export default function CategoriesTable({ categories, refreshing = false, renderActions }) {
  const tableStyles = useTableStyles()
  const styles = useStyles()
  const columns = renderActions ? [...COLUMNS, 'Acciones'] : COLUMNS

  return (
    <div className={mergeClasses(tableStyles.wrapper, refreshing && tableStyles.refreshing)} aria-busy={refreshing}>
      <Table aria-label="Categorías">
        <TableHeader>
          <TableRow>
            {columns.map((column) => (
              <TableHeaderCell key={column}>{column}</TableHeaderCell>
            ))}
          </TableRow>
        </TableHeader>
        <TableBody>
          {categories.map((category) => {
            const status = getCategoryStatus(category.estado)
            return (
              <TableRow key={category.id} className={tableStyles.row}>
                <TableCell className={tableStyles.name}>
                  <Text weight="semibold">{category.nombre}</Text>
                </TableCell>
                <TableCell>
                  <Text className={styles.description} title={category.descripcion || undefined}>
                    {category.descripcion || '—'}
                  </Text>
                </TableCell>
                <TableCell>
                  <Badge appearance="tint" color={status.color}>
                    {status.label}
                  </Badge>
                </TableCell>
                <TableCell className={styles.count}>{category.numeroProductos}</TableCell>
                {renderActions && <TableCell>{renderActions(category)}</TableCell>}
              </TableRow>
            )
          })}
        </TableBody>
      </Table>
    </div>
  )
}

CategoriesTable.propTypes = {
  categories: PropTypes.arrayOf(
    PropTypes.shape({
      id: PropTypes.string.isRequired,
      nombre: PropTypes.string.isRequired,
      descripcion: PropTypes.string.isRequired,
      estado: PropTypes.string.isRequired,
      numeroProductos: PropTypes.number.isRequired,
    }),
  ).isRequired,
  refreshing: PropTypes.bool,
  renderActions: PropTypes.func,
}
