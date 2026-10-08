import PropTypes from 'prop-types'
import { useState } from 'react'
import { Button, Field, Input, Spinner } from '@fluentui/react-components'
import { useFormStyles } from './authFormStyles'

const EMAIL_PATTERN = /^[^\s@]+@[^\s@]+$/

function validate({ email, password }) {
  const errors = {}
  if (!email.trim()) {
    errors.email = 'Introduce tu correo electrónico.'
  } else if (!EMAIL_PATTERN.test(email.trim())) {
    errors.email = 'Introduce un correo electrónico válido.'
  }
  if (!password) {
    errors.password = 'Introduce tu contraseña.'
  }
  return errors
}

export default function CredentialsStep({ onSubmit, submitting }) {
  const styles = useFormStyles()
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [errors, setErrors] = useState({})

  const handleSubmit = (event) => {
    event.preventDefault()
    if (submitting) return

    const foundErrors = validate({ email, password })
    setErrors(foundErrors)
    if (Object.keys(foundErrors).length > 0) return

    const credentials = { email: email.trim(), password }
    setPassword('')
    onSubmit(credentials)
  }

  return (
    <form className={styles.form} onSubmit={handleSubmit} noValidate>
      <Field
        label="Correo electrónico"
        validationState={errors.email ? 'error' : 'none'}
        validationMessage={errors.email}
      >
        <Input
          type="email"
          value={email}
          onChange={(_event, data) => setEmail(data.value)}
          autoComplete="username"
          maxLength={254}
        />
      </Field>
      <Field
        label="Contraseña"
        validationState={errors.password ? 'error' : 'none'}
        validationMessage={errors.password}
      >
        <Input
          type="password"
          value={password}
          onChange={(_event, data) => setPassword(data.value)}
          autoComplete="current-password"
          maxLength={128}
        />
      </Field>
      <Button
        type="submit"
        appearance="primary"
        disabled={submitting}
        icon={submitting ? <Spinner size="tiny" /> : undefined}
      >
        Continuar
      </Button>
    </form>
  )
}

CredentialsStep.propTypes = {
  onSubmit: PropTypes.func.isRequired,
  submitting: PropTypes.bool.isRequired,
}
