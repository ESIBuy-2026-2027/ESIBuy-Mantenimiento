import PropTypes from 'prop-types'
import { useState } from 'react'
import { Button, Field, Input, Spinner } from '@fluentui/react-components'
import { PasswordField } from '../account/PasswordField'
import { esEmailValido } from '../account/accountRules'
import { useFormStyles } from './authFormStyles'

function validate({ email, password }) {
  const errors = {}
  if (!email.trim()) {
    errors.email = 'Introduce tu correo electrónico.'
  } else if (!esEmailValido(email.trim())) {
    errors.email = 'Introduce un correo electrónico válido. Ejemplo: email@dominio.es'
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
          size="large"
          style={{ width: '100%' }}
          type="email"
          value={email}
          onChange={(_event, data) => setEmail(data.value)}
          autoComplete="username"
          maxLength={254}
        />
      </Field>
      <PasswordField
        label="Contraseña"
        value={password}
        onChange={(_event, data) => setPassword(data.value)}
        error={errors.password}
        autoComplete="current-password"
        maxLength={128}
        required={false}
        // R9: la contraseña no se copia ni se corta, pero se puede pegar, por los gestores de contraseñas
        bloquearCopia
      />
      <Button
        type="submit"
        appearance="primary"
        size="large"
        style={{ width: '100%', minHeight: '52px', fontSize: '16px', fontWeight: 600 }}
        disabled={submitting}
        icon={submitting ? <Spinner size="tiny" /> : undefined}
      >
        Entrar
      </Button>
    </form>
  )
}

CredentialsStep.propTypes = {
  onSubmit: PropTypes.func.isRequired,
  submitting: PropTypes.bool.isRequired,
}
