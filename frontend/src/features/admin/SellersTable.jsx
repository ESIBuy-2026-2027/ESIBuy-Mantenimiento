import PropTypes from 'prop-types'
import {
  Badge,
  Button,
  Table,
  TableBody,
  TableCell,
  TableHeader,
  TableHeaderCell,
  TableRow,
  Text,
  mergeClasses,
} from '@fluentui/react-components'
import { Search20Regular } from '@fluentui/react-icons'
import { useTableStyles } from '../../components/tableStyles'
import { getSellerStatus } from './sellerStatus'

export default function SellersTable({ sellers, refreshing, onView }) {
  const tableStyles = useTableStyles()

  return (
    <div className={mergeClasses(tableStyles.wrapper, refreshing && tableStyles.refreshing)} aria-busy={refreshing}>
      <Table aria-label="Vendedores">
        <TableHeader>
          <TableRow>
            <TableHeaderCell>Vendedor</TableHeaderCell>
            <TableHeaderCell>Nombre comercial</TableHeaderCell>
            <TableHeaderCell>Correo electrónico</TableHeaderCell>
            <TableHeaderCell>Categoría</TableHeaderCell>
            <TableHeaderCell>Estado</TableHeaderCell>
            <TableHeaderCell>Información</TableHeaderCell>
          </TableRow>
        </TableHeader>
        <TableBody>
          {sellers.map((seller) => {
            const status = getSellerStatus(seller.estado)
            return (
              <TableRow key={seller.id} className={tableStyles.row}>
                <TableCell className={tableStyles.name}>
                  <Text>{`${seller.nombre} ${seller.apellidos}`}</Text>
                </TableCell>
                <TableCell>
                  <Text weight="semibold">{seller.nombreComercial}</Text>
                </TableCell>
                <TableCell>{seller.email}</TableCell>
                <TableCell>
                  <Badge appearance="outline" color="brand" size="medium">
                    {seller.categoriaPrincipal}
                  </Badge>
                </TableCell>
                <TableCell>
                  <Badge appearance="tint" color={status.color}>
                    {status.label}
                  </Badge>
                </TableCell>
                <TableCell>
                  <Button
                    appearance="subtle"
                    icon={<Search20Regular />}
                    aria-label={`Ver información de ${seller.nombreComercial}`}
                    onClick={() => onView(seller.id)}
                  />
                </TableCell>
              </TableRow>
            )
          })}
        </TableBody>
      </Table>
    </div>
  )
}

SellersTable.propTypes = {
  sellers: PropTypes.arrayOf(
    PropTypes.shape({
      id: PropTypes.string.isRequired,
      nombre: PropTypes.string.isRequired,
      apellidos: PropTypes.string.isRequired,
      nombreComercial: PropTypes.string.isRequired,
      email: PropTypes.string.isRequired,
      categoriaPrincipal: PropTypes.string.isRequired,
      estado: PropTypes.string.isRequired,
    }),
  ).isRequired,
  refreshing: PropTypes.bool,
  onView: PropTypes.func.isRequired,
}
