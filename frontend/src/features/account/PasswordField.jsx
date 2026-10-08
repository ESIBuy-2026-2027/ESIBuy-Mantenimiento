import PropTypes from 'prop-types'
import { Button, Field, Input, Text, makeStyles, tokens } from '@fluentui/react-components'
import { CheckmarkCircle16Filled, Circle16Regular, Eye20Regular, EyeOff20Regular } from '@fluentui/react-icons'
import { useState } from 'react'
import { REQUISITOS_PASSWORD } from './accountRules'

const useStyles = makeStyles({
  requisitos: {
    display: 'flex',
    flexDirection: 'column',
    gap: tokens.spacingVerticalXXS,
    listStyleType: 'none',
    margin: 0,
    padding: 0,
  },
  requisito: {
    display: 'flex',
    alignItems: 'center',
    gap: tokens.spacingHorizontalXS,
  },
  cumplido: {
    color: tokens.colorPaletteGreenForeground1,
  },
  pendiente: {
    color: tokens.colorNeutralForeground3,
  },
})

// Campo de password obligatorio con un botón para mostrarlo u ocultarlo.
export function PasswordField({ label, value, onChange, error, maxLength }) {
  const [visible, setVisible] = useState(false)
  const accion = visible ? 'Ocultar' : 'Mostrar'

  return (
    <Field label={label} required validationMessage={error}>
      <Input
        type={visible ? 'text' : 'password'}
        maxLength={maxLength}
        value={value}
        onChange={onChange}
        contentAfter={
          <Button
            appearance="transparent"
            size="small"
            icon={visible ? <EyeOff20Regular /> : <Eye20Regular />}
            aria-label={`${accion} ${label.toLowerCase()}`}
            onClick={() => setVisible(!visible)}
          />
        }
      />
    </Field>
  )
}

// Lista de requisitos del password con un tick verde en los que ya se cumplen.
export function PasswordRequirements({ password }) {
  const styles = useStyles()

  return (
    <ul aria-label="Requisitos del password" className={styles.requisitos}>
      {REQUISITOS_PASSWORD.map(({ texto, cumple }) => (
        <li key={texto} className={styles.requisito}>
          {cumple(password) ? (
            <CheckmarkCircle16Filled aria-label="Cumplido" className={styles.cumplido} />
          ) : (
            <Circle16Regular aria-label="Pendiente" className={styles.pendiente} />
          )}
          <Text size={200}>{texto}</Text>
        </li>
      ))}
    </ul>
  )
}

PasswordField.propTypes = {
  label: PropTypes.string.isRequired,
  value: PropTypes.string.isRequired,
  onChange: PropTypes.func.isRequired,
  error: PropTypes.string,
  maxLength: PropTypes.number,
}

PasswordRequirements.propTypes = {
  password: PropTypes.string.isRequired,
}
