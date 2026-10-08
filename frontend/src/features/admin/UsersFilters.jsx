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

export default function UsersFilters({ search, role, status, onSearchChange, onRoleChange, onStatusChange }) {
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
      <FilterSelect
        label="Rol"
        allLabel="Todos los roles"
        labels={ROLE_LABELS}
        value={role}
        onChange={onRoleChange}
        className={styles.select}
      />
      <FilterSelect
        label="Estado"
        allLabel="Todos los estados"
        labels={STATUS_LABELS}
        value={status}
        onChange={onStatusChange}
        className={styles.select}
      />
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
  role: PropTypes.string.isRequired,
  status: PropTypes.string.isRequired,
  onSearchChange: PropTypes.func.isRequired,
  onRoleChange: PropTypes.func.isRequired,
  onStatusChange: PropTypes.func.isRequired,
}
