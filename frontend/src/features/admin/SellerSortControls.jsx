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

export default function SellerSortControls({ sort, order, onSortChange, onOrderChange }) {
  const styles = useStyles()
  const ascending = order === 'asc'

  return (
    <>
      <Field label="Ordenar por" className={styles.field}>
        <Select value={sort} onChange={(_event, data) => onSortChange(data.value)}>
          <option value="status">Estado</option>
          <option value="category">Categoría</option>
          <option value="name">Nombre</option>
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

SellerSortControls.propTypes = {
  sort: PropTypes.oneOf(['status', 'category', 'name']).isRequired,
  order: PropTypes.oneOf(['asc', 'desc']).isRequired,
  onSortChange: PropTypes.func.isRequired,
  onOrderChange: PropTypes.func.isRequired,
}
