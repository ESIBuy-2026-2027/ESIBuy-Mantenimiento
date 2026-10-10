import { useState } from 'react'
import { MessageBar, MessageBarBody, Subtitle1 } from '@fluentui/react-components'
import { login, verifyAuthenticatorCode, verifyEmailCode } from '../../api/auth'
import { useAuth } from '../../auth/useAuth'
import CodeStep from './CodeStep'
import CredentialsStep from './CredentialsStep'
import { getLoginErrorMessage, isPendingAccountError } from './loginErrors'
import PendingAccountScreen from './PendingAccountScreen'

export default function LoginForm() {
  const { signIn } = useAuth()
  const [step, setStep] = useState('credentials')
  const [error, setError] = useState('')
  const [submitting, setSubmitting] = useState(false)
  const [pendingEmail, setPendingEmail] = useState('')

  const run = async (action, onSuccess) => {
    setSubmitting(true)
    setError('')
    try {
      onSuccess(await action())
    } catch (err) {
      if (isPendingAccountError(err)) {
        setStep('pending')
      } else {
        setError(getLoginErrorMessage(err, step))
      }
    } finally {
      setSubmitting(false)
    }
  }

  const restart = () => {
    setError('')
    setStep('credentials')
  }

  const handleCredentials = (credentials) => {
    setPendingEmail(credentials.email)
    return run(() => login(credentials), () => setStep('emailCode'))
  }

  const handleEmailCode = (code) =>
    run(() => verifyEmailCode(code), () => setStep('authenticator'))

  const handleAuthenticatorCode = (code) =>
    run(async () => {
      const user = await verifyAuthenticatorCode(code)
      if (!user?.rol) {
        throw new TypeError('Respuesta de sesión no válida')
      }
      return user
    }, signIn)

  return (
    <>
      {error && (
        <MessageBar intent="error">
          <MessageBarBody>{error}</MessageBarBody>
        </MessageBar>
      )}

      {step === 'credentials' && (
        <CredentialsStep onSubmit={handleCredentials} submitting={submitting} />
      )}

      {step === 'pending' && <PendingAccountScreen email={pendingEmail} onBack={restart} />}

      {step === 'emailCode' && (
        <>
          <Subtitle1 as="h2">Verificación por correo</Subtitle1>
          <CodeStep
            label="Código recibido por correo"
            hint="Introduce el código de 6 dígitos que te hemos enviado por correo electrónico."
            onSubmit={handleEmailCode}
            onBack={restart}
            submitting={submitting}
          />
        </>
      )}

      {step === 'authenticator' && (
        <>
          <Subtitle1 as="h2">Verificación con Microsoft Authenticator</Subtitle1>
          <CodeStep
            label="Código de Microsoft Authenticator"
            hint="Abre Microsoft Authenticator e introduce el código de 6 dígitos de tu cuenta de ESIBuy."
            onSubmit={handleAuthenticatorCode}
            onBack={restart}
            submitting={submitting}
          />
        </>
      )}
    </>
  )
}
