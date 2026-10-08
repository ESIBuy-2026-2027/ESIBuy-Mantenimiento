import {
  Table,
  TableBody,
  TableCell,
  TableHeader,
  TableHeaderCell,
  TableRow,
  Text,
  makeStyles,
  tokens,
} from '@fluentui/react-components'
import { ROLE_LABELS, STATUS_LABELS } from './usersConfig'

const COLUMNS = ['Nombre', 'Email', 'Rol', 'Estado', 'Fecha de alta']

const dateFormatter = new Intl.DateTimeFormat('es-ES', { day: '2-digit', month: '2-digit', year: 'numeric' })

const useStyles = makeStyles({
  wrapper: {
    overflowX: 'auto',
    backgroundColor: tokens.colorNeutralBackground1,
    border: `1px solid ${tokens.colorNeutralStroke2}`,
    borderRadius: tokens.borderRadiusXLarge,
    boxShadow: tokens.shadow4,
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
})

export default function UsersTable({ users }) {
  const styles = useStyles()

  return (
    <div className={styles.wrapper}>
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
            <TableRow key={user.id} className={styles.row}>
              <TableCell className={styles.name}>
                <Text weight="semibold">{`${user.nombre} ${user.apellidos}`}</Text>
              </TableCell>
              <TableCell>{user.email}</TableCell>
              <TableCell>{ROLE_LABELS[user.rol]}</TableCell>
              <TableCell>{STATUS_LABELS[user.estado]}</TableCell>
              <TableCell>{dateFormatter.format(new Date(user.fechaAlta))}</TableCell>
            </TableRow>
          ))}
        </TableBody>
      </Table>
    </div>
  )
}
