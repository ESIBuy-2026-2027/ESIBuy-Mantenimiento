import {
  Table,
  TableBody,
  TableCell,
  TableHeader,
  TableHeaderCell,
  TableRow,
} from '@fluentui/react-components'
import { ROLE_LABELS, STATUS_LABELS } from './usersConfig'

const COLUMNS = ['Nombre', 'Email', 'Rol', 'Estado', 'Fecha de alta']

const dateFormatter = new Intl.DateTimeFormat('es-ES', { day: '2-digit', month: '2-digit', year: 'numeric' })

export default function UsersTable({ users }) {
  return (
    <Table aria-label="Usuarios">
      <TableHeader>
        <TableRow>
          {COLUMNS.map((column) => (
            <TableHeaderCell key={column}>{column}</TableHeaderCell>
          ))}
        </TableRow>
      </TableHeader>
      <TableBody>
        {users.map((user) => (
          <TableRow key={user.id}>
            <TableCell>{`${user.nombre} ${user.apellidos}`}</TableCell>
            <TableCell>{user.email}</TableCell>
            <TableCell>{ROLE_LABELS[user.rol]}</TableCell>
            <TableCell>{STATUS_LABELS[user.estado]}</TableCell>
            <TableCell>{dateFormatter.format(new Date(user.fechaAlta))}</TableCell>
          </TableRow>
        ))}
      </TableBody>
    </Table>
  )
}
