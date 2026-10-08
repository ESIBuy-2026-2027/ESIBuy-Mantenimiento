import PropTypes from 'prop-types'
import { Field, SearchBox, Select, makeStyles, tokens } from '@fluentui/react-components'
import { ROLE_LABELS, STATUS_LABELS } from './usersConfig'

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

function FilterSelect({ label, allLabel, labels, value, onChange, className }) {
  return (
    <Field label={label} className={className}>
      <Select value={value} onChange={(_event, data) => onChange(data.value)}>
        <option value="">{allLabel}</option>
        {Object.entries(labels).map(([optionValue, optionLabel]) => (
          <option key={optionValue} value={optionValue}>
            {optionLabel}
          </option>
        ))}
      </Select>
    </Field>
  )
}

// El filtro de rol solo aparece si se recibe onRoleChange; los listados de un único rol no lo necesitan.
// children permite añadir otros controles a la misma fila (p. ej. la ordenación).
export default function UsersFilters({
  search,
  searchLabel = 'Buscar por nombre o email',
  searchPlaceholder = 'Escribe un nombre o un email',
  role = '',
  status,
  statusLabels = STATUS_LABELS,
  onSearchChange,
  onRoleChange,
  onStatusChange,
  children,
}) {
  const styles = useStyles()

  return (
    <div className={styles.filters}>
      <Field label={searchLabel} className={styles.search}>
        <SearchBox
          value={search}
          placeholder={searchPlaceholder}
          onChange={(_event, data) => onSearchChange(data.value)}
        />
      </Field>
      {onRoleChange && (
        <FilterSelect
          label="Rol"
          allLabel="Todos los roles"
          labels={ROLE_LABELS}
          value={role}
          onChange={onRoleChange}
          className={styles.select}
        />
      )}
      <FilterSelect
        label="Estado"
        allLabel="Todos los estados"
        labels={statusLabels}
        value={status}
        onChange={onStatusChange}
        className={styles.select}
      />
      {children}
    </div>
  )
}

FilterSelect.propTypes = {
  label: PropTypes.string.isRequired,
  allLabel: PropTypes.string.isRequired,
  labels: PropTypes.objectOf(PropTypes.string).isRequired,
  value: PropTypes.string.isRequired,
  onChange: PropTypes.func.isRequired,
  className: PropTypes.string,
}

UsersFilters.propTypes = {
  search: PropTypes.string.isRequired,
  searchLabel: PropTypes.string,
  searchPlaceholder: PropTypes.string,
  role: PropTypes.string,
  status: PropTypes.string.isRequired,
  statusLabels: PropTypes.objectOf(PropTypes.string),
  onSearchChange: PropTypes.func.isRequired,
  onRoleChange: PropTypes.func,
  onStatusChange: PropTypes.func.isRequired,
  children: PropTypes.node,
}
