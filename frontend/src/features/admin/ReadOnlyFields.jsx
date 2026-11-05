import PropTypes from 'prop-types'
import { Field, Input, makeStyles, tokens } from '@fluentui/react-components'

const useStyles = makeStyles({
  grid: {
    display: 'grid',
    gridTemplateColumns: 'repeat(auto-fit, minmax(240px, 1fr))',
    gap: tokens.spacingVerticalM,
    columnGap: tokens.spacingHorizontalL,
  },
})

// Rejilla de campos de solo lectura, más oscuros que los editables. La usan la ficha del vendedor
// (HU26) y la de clientes y administradores (HU20).
export default function ReadOnlyFields({ fields }) {
  const styles = useStyles()

  return (
    <div className={styles.grid}>
      {fields.map(([label, value]) => (
        <Field key={label} label={label}>
          <Input readOnly appearance="filled-darker" value={value} />
        </Field>
      ))}
    </div>
  )
}

ReadOnlyFields.propTypes = {
  fields: PropTypes.arrayOf(PropTypes.arrayOf(PropTypes.string)).isRequired,
}
