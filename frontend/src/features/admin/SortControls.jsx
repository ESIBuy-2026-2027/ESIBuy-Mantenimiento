import PropTypes from 'prop-types'
import { Button, Field, Select, makeStyles } from '@fluentui/react-components'
import { ArrowSortDown20Regular, ArrowSortUp20Regular } from '@fluentui/react-icons'

const useStyles = makeStyles({
  field: {
    flexGrow: 1,
    flexBasis: '160px',
  },
  direction: {
    alignSelf: 'flex-end',
  },
})

// Criterio de ordenación (las opciones las da cada listado) y botón para alternar el sentido.
export default function SortControls({ options, sort, order, onSortChange, onOrderChange }) {
  const styles = useStyles()
  const ascending = order === 'asc'

  return (
    <>
      <Field label="Ordenar por" className={styles.field}>
        <Select value={sort} onChange={(_event, data) => onSortChange(data.value)}>
          {options.map(({ value, label }) => (
            <option key={value} value={value}>
              {label}
            </option>
          ))}
        </Select>
      </Field>
      <Button
        className={styles.direction}
        appearance="subtle"
        icon={ascending ? <ArrowSortUp20Regular /> : <ArrowSortDown20Regular />}
        aria-label={ascending ? 'Orden ascendente' : 'Orden descendente'}
        onClick={() => onOrderChange(ascending ? 'desc' : 'asc')}
      >
        {ascending ? 'Ascendente' : 'Descendente'}
      </Button>
    </>
  )
}

SortControls.propTypes = {
  options: PropTypes.arrayOf(
    PropTypes.shape({
      value: PropTypes.string.isRequired,
      label: PropTypes.string.isRequired,
    }),
  ).isRequired,
  sort: PropTypes.string.isRequired,
  order: PropTypes.oneOf(['asc', 'desc']).isRequired,
  onSortChange: PropTypes.func.isRequired,
  onOrderChange: PropTypes.func.isRequired,
}
