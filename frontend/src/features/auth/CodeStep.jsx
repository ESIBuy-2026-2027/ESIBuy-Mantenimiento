import PropTypes from 'prop-types'
import { useState } from 'react'
import { Button, Field, Input, Spinner } from '@fluentui/react-components'
import { useFormStyles } from './authFormStyles'

const CODE_LENGTH = 6
const CODE_PATTERN = new RegExp(`^\\d{${CODE_LENGTH}}$`)

export default function CodeStep({ label, hint, onSubmit, onBack, submitting }) {
  const styles = useFormStyles()
  const [code, setCode] = useState('')
  const [error, setError] = useState('')

  const handleChange = (_event, data) => {
    setCode(data.value.replace(/\D/g, '').slice(0, CODE_LENGTH))
  }

  const handleSubmit = (event) => {
    event.preventDefault()
    if (submitting) return

    if (!CODE_PATTERN.test(code)) {
      setError(`El código tiene ${CODE_LENGTH} dígitos.`)
      return
    }

    const value = code
    setError('')
    setCode('')
    onSubmit(value)
  }

  return (
    <form className={styles.form} onSubmit={handleSubmit} noValidate>
      <Field
        label={label}
        hint={hint}
        validationState={error ? 'error' : 'none'}
        validationMessage={error}
      >
        <Input
          value={code}
          onChange={handleChange}
          inputMode="numeric"
          autoComplete="one-time-code"
          maxLength={CODE_LENGTH}
          autoFocus
        />
      </Field>
      <div className={styles.actions}>
        <Button
          type="submit"
          appearance="primary"
          disabled={submitting}
          icon={submitting ? <Spinner size="tiny" /> : undefined}
        >
          Verificar
        </Button>
        <Button type="button" appearance="subtle" onClick={onBack} disabled={submitting}>
          Volver al inicio
        </Button>
      </div>
    </form>
  )
}

CodeStep.propTypes = {
  label: PropTypes.string.isRequired,
  hint: PropTypes.string,
  onSubmit: PropTypes.func.isRequired,
  onBack: PropTypes.func.isRequired,
  submitting: PropTypes.bool.isRequired,
}
