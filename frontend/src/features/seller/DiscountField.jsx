import PropTypes from 'prop-types'
import { Field, Input } from '@fluentui/react-components'

// Un porcentaje vacío equivale a no aplicar el descuento.
export default function DiscountField({ label, percent, error, onPercentChange }) {
  const handleKeyDown = (event) => {
    if (['-', '+', 'e', 'E', '.', ','].includes(event.key)) event.preventDefault()
  }

  return (
    <Field
      label={label}
      hint="Opcional"
      validationState={error ? 'error' : 'none'}
      validationMessage={error}
    >
      <Input
        type="number"
        min={1}
        max={99}
        step={1}
        inputMode="numeric"
        value={percent}
        onKeyDown={handleKeyDown}
        onChange={(_event, data) => onPercentChange(data.value)}
      />
    </Field>
  )
}

DiscountField.propTypes = {
  label: PropTypes.string.isRequired,
  percent: PropTypes.string.isRequired,
  error: PropTypes.string,
  onPercentChange: PropTypes.func.isRequired,
}
