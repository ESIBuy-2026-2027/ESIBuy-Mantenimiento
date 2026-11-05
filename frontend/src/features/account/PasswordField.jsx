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

// Cancela el evento para que el navegador no copie, corte, pegue ni suelte nada en el campo.
const cancelar = (event) => event.preventDefault()

// Eventos que se cancelan en cada modo de bloqueo: la copia saca la contraseña del campo y el pegado la mete.
const SIN_COPIA = { onCopy: cancelar, onCut: cancelar }
const SIN_PEGADO = { onPaste: cancelar, onDrop: cancelar }

// Campo de password (obligatorio por defecto) con un botón para mostrarlo u ocultarlo.
// Con bloquearPegado no se puede copiar, cortar, pegar ni soltar texto: hay que escribirlo a mano (T242).
// Con bloquearCopia solo se impide copiar y cortar (R9): se puede pegar, para los gestores de contraseñas.
export function PasswordField({
  label,
  value,
  onChange,
  error,
  maxLength,
  autoComplete,
  required = true,
  bloquearPegado = false,
  bloquearCopia = false,
}) {
  const [visible, setVisible] = useState(false)
  const accion = visible ? 'Ocultar' : 'Mostrar'
  // bloquearPegado ya incluye no copiar ni cortar
  const sinPortapapeles = {
    ...(bloquearPegado || bloquearCopia ? SIN_COPIA : {}),
    ...(bloquearPegado ? SIN_PEGADO : {}),
  }

  return (
    <Field label={label} required={required} validationMessage={error}>
      <Input
        type={visible ? 'text' : 'password'}
        maxLength={maxLength}
        autoComplete={autoComplete}
        value={value}
        onChange={onChange}
        {...sinPortapapeles}
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
  // El login lo usa sin asterisco y con el autocompletado de la contraseña guardada (fallos de la demo del Sprint 1)
  autoComplete: PropTypes.string,
  required: PropTypes.bool,
  // El registro lo activa para obligar a escribir el password a mano, también en el campo de repetirlo
  bloquearPegado: PropTypes.bool,
  // El login lo activa (R9): la contraseña no se puede copiar ni cortar, pero sí pegar, por los gestores de contraseñas
  bloquearCopia: PropTypes.bool,
}

PasswordRequirements.propTypes = {
  password: PropTypes.string.isRequired,
}
