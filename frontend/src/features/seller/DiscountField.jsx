import PropTypes from 'prop-types'
import { Field, Input, Switch, makeStyles, tokens } from '@fluentui/react-components'

const useStyles = makeStyles({
  discount: {
    display: 'flex',
    flexDirection: 'column',
    gap: tokens.spacingVerticalXS,
  },
})

// Interruptor para activar un descuento y, solo cuando está activo, el campo de su porcentaje.
export default function DiscountField({
  switchLabel,
  percentLabel,
  active,
  percent,
  error,
  onActiveChange,
  onPercentChange,
}) {
  const styles = useStyles()

  return (
    <div className={styles.discount}>
      <Switch label={switchLabel} checked={active} onChange={(_event, data) => onActiveChange(data.checked)} />
      {active && (
        <Field
          label={percentLabel}
          required
          validationState={error ? 'error' : 'none'}
          validationMessage={error}
        >
          <Input
            type="number"
            min={1}
            max={100}
            step="any"
            value={percent}
            onChange={(_event, data) => onPercentChange(data.value)}
          />
        </Field>
      )}
    </div>
  )
}

DiscountField.propTypes = {
  switchLabel: PropTypes.string.isRequired,
  percentLabel: PropTypes.string.isRequired,
  active: PropTypes.bool.isRequired,
  percent: PropTypes.string.isRequired,
  error: PropTypes.string,
  onActiveChange: PropTypes.func.isRequired,
  onPercentChange: PropTypes.func.isRequired,
}
