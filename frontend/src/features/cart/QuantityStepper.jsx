import PropTypes from 'prop-types'
import { Button, makeStyles, tokens } from '@fluentui/react-components'
import { Add16Regular, Subtract16Regular } from '@fluentui/react-icons'

const useStyles = makeStyles({
  stepper: {
    display: 'inline-flex',
    alignItems: 'center',
    border: `1px solid ${tokens.colorNeutralStroke1}`,
    borderRadius: tokens.borderRadiusMedium,
    backgroundColor: tokens.colorNeutralBackground1,
  },
  value: {
    minWidth: '2.2em',
    textAlign: 'center',
    fontWeight: tokens.fontWeightSemibold,
    fontVariantNumeric: 'tabular-nums',
  },
})

// Selector de cantidad con botones de menos y de más, entre `min` y `max`. `label` es el nombre de la cantidad para
// los lectores de pantalla (por ejemplo, «Cantidad de Teclado mecánico»).
export default function QuantityStepper({ value, min = 1, max, label = 'Cantidad', disabled = false, onChange }) {
  const styles = useStyles()

  return (
    <span className={styles.stepper}>
      <Button
        appearance="subtle"
        size="small"
        icon={<Subtract16Regular />}
        aria-label="Disminuir cantidad"
        disabled={disabled || value <= min}
        onClick={() => onChange(value - 1)}
      />
      <output className={styles.value} aria-label={label}>
        {value}
      </output>
      <Button
        appearance="subtle"
        size="small"
        icon={<Add16Regular />}
        aria-label="Aumentar cantidad"
        disabled={disabled || value >= max}
        onClick={() => onChange(value + 1)}
      />
    </span>
  )
}

QuantityStepper.propTypes = {
  value: PropTypes.number.isRequired,
  min: PropTypes.number,
  max: PropTypes.number.isRequired,
  label: PropTypes.string,
  disabled: PropTypes.bool,
  onChange: PropTypes.func.isRequired,
}
