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
  mergeClasses,
} from '@fluentui/react-components'
import { useTableStyles } from '../../components/tableStyles'
import { formatDate, formatPrice } from '../../utils/format'
import { getOrderStatus } from './sellerStatus'

export default function OrdersTable({ orders, refreshing }) {
  const tableStyles = useTableStyles()

  return (
    <div className={mergeClasses(tableStyles.wrapper, refreshing && tableStyles.refreshing)} aria-busy={refreshing}>
      <Table aria-label="Pedidos del vendedor">
        <TableHeader>
          <TableRow>
            <TableHeaderCell>Pedido</TableHeaderCell>
            <TableHeaderCell>Fecha</TableHeaderCell>
            <TableHeaderCell>Artículos</TableHeaderCell>
            <TableHeaderCell>Total</TableHeaderCell>
            <TableHeaderCell>Estado</TableHeaderCell>
          </TableRow>
        </TableHeader>
        <TableBody>
          {orders.map((order) => {
            const status = getOrderStatus(order.estado)
            return (
              <TableRow key={order.id} className={tableStyles.row}>
                <TableCell>
                  <Text weight="semibold">{order.id}</Text>
                </TableCell>
                <TableCell>{formatDate(order.fecha)}</TableCell>
                <TableCell>{order.articulos}</TableCell>
                <TableCell>{formatPrice(order.total)}</TableCell>
                <TableCell>
                  <Badge appearance="tint" color={status.color}>
                    {status.label}
                  </Badge>
                </TableCell>
              </TableRow>
            )
          })}
        </TableBody>
      </Table>
    </div>
  )
}

OrdersTable.propTypes = {
  orders: PropTypes.arrayOf(
    PropTypes.shape({
      id: PropTypes.string.isRequired,
      fecha: PropTypes.string.isRequired,
      articulos: PropTypes.number.isRequired,
      total: PropTypes.number.isRequired,
      estado: PropTypes.string.isRequired,
    }),
  ).isRequired,
  refreshing: PropTypes.bool,
}
