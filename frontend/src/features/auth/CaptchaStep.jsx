import { useState } from 'react'
import { Button, Spinner, Text } from '@fluentui/react-components'
import Captcha from '../../components/Captcha'
import { useFormStyles } from './authFormStyles'

export default function CaptchaStep({ onSubmit, onBack, submitting }) {
  const styles = useFormStyles()
  const [token, setToken] = useState('')
  const [widgetKey, setWidgetKey] = useState(0)
  const [error, setError] = useState('')

  const handleChange = (value) => {
    setToken(value)
    if (value) setError('')
  }

  const handleSubmit = (event) => {
    event.preventDefault()
    if (submitting) return

    if (!token) {
      setError('Marca la casilla "No soy un robot" para continuar.')
      return
    }

    const value = token
    setToken('')
    setError('')
    setWidgetKey((key) => key + 1)
    onSubmit(value)
  }

  return (
    <form className={styles.form} onSubmit={handleSubmit} noValidate>
      <div className={styles.captcha}>
        <Text>Confirma que eres una persona.</Text>
        <Captcha key={widgetKey} onChange={handleChange} />
        {error && (
          <Text role="alert" className={styles.error}>
            {error}
          </Text>
        )}
      </div>
      <div className={styles.actions}>
        <Button
          type="submit"
          appearance="primary"
          disabled={submitting}
          icon={submitting ? <Spinner size="tiny" /> : undefined}
        >
          Entrar
        </Button>
        <Button type="button" appearance="subtle" onClick={onBack} disabled={submitting}>
          Volver al inicio
        </Button>
      </div>
    </form>
  )
}
