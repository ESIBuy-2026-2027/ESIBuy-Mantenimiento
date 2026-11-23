import PropTypes from 'prop-types'
import { Field, Input } from '@fluentui/react-components'

// Un porcentaje vacío equivale a no aplicar el descuento.
export default function DiscountField({ label, percent, error, inputClassName, onPercentChange }) {
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
        className={inputClassName}
        type="text"
        inputMode="numeric"
        maxLength={2}
        pattern="[0-9]*"
        value={percent}
        onKeyDown={handleKeyDown}
        onChange={(_event, data) => onPercentChange(data.value.replace(/\\D/g, '').slice(0, 2))}
      />
    </Field>
  )
}

DiscountField.propTypes = {
  label: PropTypes.string.isRequired,
  percent: PropTypes.string.isRequired,
  error: PropTypes.string,
  inputClassName: PropTypes.string,
  onPercentChange: PropTypes.func.isRequired,
}
