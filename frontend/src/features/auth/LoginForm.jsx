import { useState } from 'react'
import { MessageBar, MessageBarBody, Subtitle1 } from '@fluentui/react-components'
import {
  enrollAuthenticator,
  getCurrentUser,
  login,
  verifyAuthenticatorCode,
  verifyEmailCode,
} from '../../api/auth'
import { useAuth } from '../../auth/useAuth'
import CodeStep from './CodeStep'
import CredentialsStep from './CredentialsStep'
import EnrollAuthenticatorStep from './EnrollAuthenticatorStep'
import { getLoginErrorMessage, isPendingAccountError } from './loginErrors'
import PendingAccountScreen from './PendingAccountScreen'

export default function LoginForm() {
  const { signIn } = useAuth()
  const [step, setStep] = useState('credentials')
  const [error, setError] = useState('')
  const [submitting, setSubmitting] = useState(false)
  const [pendingEmail, setPendingEmail] = useState('')
  const [qr, setQr] = useState('')

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
    setQr('')
  }

  const obtenerUsuario = async () => {
    const user = await getCurrentUser()
    if (!user?.rol) {
      throw new TypeError('Respuesta de sesión no válida')
    }
    return user
  }

  const handleCredentials = (credentials) => {
    setPendingEmail(credentials.email)

    return run(
      async () => {
        const respuesta = await login(credentials)
        if (respuesta?.nextStep === 'authenticated') {
          return { user: await obtenerUsuario() }
        }
        return { nextStep: respuesta?.nextStep }
      },
      (resultado) => {
        if (resultado.user) {
          signIn(resultado.user)
        } else if (resultado.nextStep === 'emailCode') {
          setStep('emailCode')
        } else if (resultado.nextStep === 'authenticator') {
          setStep('authenticator')
        } else {
          setError('El servidor ha devuelto un paso de autenticación no válido.')
        }
      },
    )
  }

  const handleEmailCode = (code) =>
    run(
      async () => {
        const respuesta = await verifyEmailCode(code)
        if (respuesta?.nextStep === 'authenticated') {
          return { user: await obtenerUsuario() }
        }
        if (respuesta?.nextStep === 'enrollAuthenticator') {
          const { qr: imagen } = await enrollAuthenticator()
          return { siguiente: 'enrollAuthenticator', imagen }
        }
        if (respuesta?.nextStep === 'authenticator') {
          return { siguiente: 'authenticator' }
        }
        throw new TypeError('Respuesta de autenticación no válida')
      },
      (resultado) => {
        if (resultado.user) {
          signIn(resultado.user)
          return
        }
        setQr(resultado.imagen ?? '')
        setStep(resultado.siguiente)
      },
    )

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

      {step === 'enrollAuthenticator' && (
        <EnrollAuthenticatorStep
          qr={qr}
          onSubmit={handleAuthenticatorCode}
          onBack={restart}
          submitting={submitting}
        />
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
