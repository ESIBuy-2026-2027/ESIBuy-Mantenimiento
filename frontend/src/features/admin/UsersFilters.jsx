import { Field, SearchBox, Select, makeStyles, tokens } from '@fluentui/react-components'
import { ROLE_LABELS } from './usersConfig'

const useStyles = makeStyles({
  filters: {
    display: 'flex',
    flexWrap: 'wrap',
    gap: tokens.spacingHorizontalL,
  },
  search: {
    flexGrow: 2,
    flexBasis: '240px',
  },
  select: {
    flexGrow: 1,
    flexBasis: '200px',
  },
})

export default function UsersFilters({ search, role, onSearchChange, onRoleChange }) {
  const styles = useStyles()

  return (
    <div className={styles.filters}>
      <Field label="Buscar por nombre o email" className={styles.search}>
        <SearchBox
          value={search}
          placeholder="Escribe un nombre o un email"
          onChange={(_event, data) => onSearchChange(data.value)}
        />
      </Field>
      <Field label="Rol" className={styles.select}>
        <Select value={role} onChange={(_event, data) => onRoleChange(data.value)}>
          <option value="">Todos los roles</option>
          {Object.entries(ROLE_LABELS).map(([value, label]) => (
            <option key={value} value={value}>
              {label}
            </option>
          ))}
        </Select>
      </Field>
    </div>
  )
}
