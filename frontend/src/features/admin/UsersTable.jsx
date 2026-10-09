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
  tokens,
} from '@fluentui/react-components'
import { ROLE_LABELS, STATUS_LABELS, USER_STATUS } from './usersConfig'

const COLUMNS = ['Nombre', 'Email', 'Rol', 'Estado', 'Fecha de alta']

const STATUS_BADGE_COLORS = {
  [USER_STATUS.PENDIENTE]: 'warning',
  [USER_STATUS.ACTIVO]: 'success',
  [USER_STATUS.BLOQUEADO]: 'danger',
  [USER_STATUS.DESACTIVADO]: 'subtle',
}

const dateFormatter = new Intl.DateTimeFormat('es-ES', { day: '2-digit', month: '2-digit', year: 'numeric' })

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
    '@media (prefers-reduced-motion: reduce)': {
      transitionDuration: '0.01ms',
    },
  },
  name: {
    minWidth: '180px',
  },
  date: {
    fontVariantNumeric: 'tabular-nums',
  },
})

// renderActions es opcional: si se recibe, se añade la columna "Información" con lo que devuelva para
// cada usuario (la lupa que abre su ficha, como en el listado de vendedores).
export default function UsersTable({ users, refreshing = false, renderActions }) {
  const styles = useStyles()
  const columns = renderActions ? [...COLUMNS, 'Información'] : COLUMNS

  return (
    <div className={mergeClasses(styles.wrapper, refreshing && styles.refreshing)} aria-busy={refreshing}>
      <Table aria-label="Usuarios">
        <TableHeader>
          <TableRow>
            {columns.map((column) => (
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
              <TableCell>
                <Badge appearance="tint" color={STATUS_BADGE_COLORS[user.estado]}>
                  {STATUS_LABELS[user.estado]}
                </Badge>
              </TableCell>
              <TableCell className={styles.date}>{dateFormatter.format(new Date(user.fechaAlta))}</TableCell>
              {renderActions && <TableCell>{renderActions(user)}</TableCell>}
            </TableRow>
          ))}
        </TableBody>
      </Table>
    </div>
  )
}

UsersTable.propTypes = {
  users: PropTypes.arrayOf(
    PropTypes.shape({
      id: PropTypes.string.isRequired,
      nombre: PropTypes.string.isRequired,
      apellidos: PropTypes.string.isRequired,
      email: PropTypes.string.isRequired,
      rol: PropTypes.string.isRequired,
      estado: PropTypes.string.isRequired,
      fechaAlta: PropTypes.string.isRequired,
    }),
  ).isRequired,
  refreshing: PropTypes.bool,
  renderActions: PropTypes.func,
}
