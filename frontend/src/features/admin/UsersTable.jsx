import { Table, TableBody, TableHeader, TableHeaderCell, TableRow } from '@fluentui/react-components'

const COLUMNS = ['Nombre', 'Email', 'Rol', 'Estado', 'Fecha de alta']

export default function UsersTable() {
  return (
    <Table aria-label="Usuarios">
      <TableHeader>
        <TableRow>
          {COLUMNS.map((column) => (
            <TableHeaderCell key={column}>{column}</TableHeaderCell>
          ))}
        </TableRow>
      </TableHeader>
      <TableBody />
    </Table>
  )
}
