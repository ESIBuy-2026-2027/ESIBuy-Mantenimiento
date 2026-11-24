import { useState } from 'react'
import { makeStyles } from '@fluentui/react-components'
import {
  Button,
  Card,
  Field,
  Input,
  MessageBar,
  MessageBarBody,
  Spinner,
  Text,
  Title2,
} from '@fluentui/react-components'
import { Navigate, useNavigate } from 'react-router-dom'
import { requestPasswordRecovery, resetPassword } from '../api/auth'
import { getHomePath } from '../auth/roles'
import { useAuth } from '../auth/useAuth'
import LinkButton from '../components/LinkButton'
import { PasswordField, PasswordRequirements } from '../features/account/PasswordField'
import { esEmailValido, validarPasswords } from '../features/account/accountRules'
import { erroresDelServidor } from '../features/account/registroErrors'
import { useAuthPageStyles, useFormStyles } from '../features/auth/authFormStyles'
import { PATHS } from '../routes/paths'

const CODE_LENGTH = 6
const CODE_PATTERN = new RegExp(`^\\d{${CODE_LENGTH}}$`)
const LONGITUD_MAXIMA_EMAIL = 100
const LONGITUD_MAXIMA_PASSWORD = 64

// El aviso es el mismo exista o no la cuenta: el backend responde siempre igual para no revelar qué emails hay.
const AVISO_ENVIADO = 'Si el email está registrado, te hemos enviado un código de 6 dígitos. Caduca en 10 minutos.'
const AVISO_REENVIADO = 'Si el email está registrado, te hemos enviado otro código. El anterior ya no sirve.'
const ERROR_ENVIO = 'No se ha podido enviar el código. Inténtalo de nuevo más tarde.'
const ERROR_CODIGO = 'El código no es correcto o ha caducado. Revísalo o pide otro.'

const DATOS_VACIOS = { code: '', password: '', repeticionPassword: '' }

const useCodeInputStyles = makeStyles({
  container: {
    position: 'relative',
    display: 'grid',
    gridTemplateColumns: 'repeat(6, minmax(0, 1fr))',
    gap: '10px',
    width: '100%',
    minHeight: '58px',
  },
  digit: {
    display: 'grid',
    placeItems: 'center',
    minWidth: 0,
    border: '1px solid #d6d3d1',
    borderRadius: '9px',
    backgroundColor: '#ffffff',
    color: '#242424',
    fontSize: '24px',
    fontWeight: 600,
    fontVariantNumeric: 'tabular-nums',
    transitionProperty: 'border-color, box-shadow, background-color',
    transitionDuration: '120ms',
  },
  digitActive: {
    borderColor: '#f05a0a',
    boxShadow: '0 0 0 1px #f05a0a',
    backgroundColor: '#fffaf6',
  },
  input: {
    position: 'absolute',
    inset: 0,
    width: '100%',
    height: '100%',
    opacity: 0,
    cursor: 'text',
    border: 0,
    padding: 0,
  },
  focused: {
    outline: '3px solid rgba(240, 90, 10, .16)',
    outlineOffset: '3px',
  },
})

function VerificationCodeInput({ value, onChange }) {
  const styles = useCodeInputStyles()
  const [focused, setFocused] = useState(false)

  return (
    <div className={`${styles.container} ${focused ? styles.focused : ''}`}>
      {Array.from({ length: CODE_LENGTH }, (_, index) => (
        <span
          key={index}
          className={`${styles.digit} ${focused && index === value.length ? styles.digitActive : ''}`}
          aria-hidden="true"
        >
          {value[index] || ''}
        </span>
      ))}
      <input
        className={styles.input}
        type="text"
        value={value}
        onChange={(event) => onChange(event.target.value.replace(/\\D/g, '').slice(0, CODE_LENGTH))}
        onFocus={() => setFocused(true)}
        onBlur={() => setFocused(false)}
        inputMode="numeric"
        autoComplete="one-time-code"
        pattern="[0-9]*"
        maxLength={CODE_LENGTH}
        autoFocus
        aria-label="Código de verificación de seis dígitos"
      />
    </div>
  )
}

function validarEmail(email) {
  if (!email) return 'Introduce tu correo electrónico.'
  if (!esEmailValido(email)) return 'Introduce un correo electrónico válido.'
  return null
}

function validarCambio(datos) {
  const errores = validarPasswords(datos)
  if (!CODE_PATTERN.test(datos.code)) errores.code = `El código tiene ${CODE_LENGTH} dígitos.`
  if (!datos.password) errores.password = 'Introduce la contraseña nueva.'
  if (!datos.repeticionPassword) errores.repeticionPassword = 'Repite la contraseña nueva.'
  return errores
}

// Recuperación de contraseña (HU3, T398) en dos pasos: pedir un código al correo y cambiar la contraseña con él.
// Contrato del backend en T399 (PR #176): frontend/docs/hu3-recuperar-contrasena.md.
export default function ForgotPasswordPage() {
  const pageStyles = useAuthPageStyles()
  const styles = useFormStyles()
  const navigate = useNavigate()
  const { user, loading } = useAuth()

  const [email, setEmail] = useState('')
  const [emailEnviado, setEmailEnviado] = useState(null)
  const [datos, setDatos] = useState(DATOS_VACIOS)
  const [errores, setErrores] = useState({})
  const [aviso, setAviso] = useState(null)
  const [enviando, setEnviando] = useState(false)

  if (loading) {
    return <Spinner label="Comprobando sesión..." />
  }

  if (user) {
    return <Navigate to={getHomePath(user.rol)} replace />
  }

  // Pide el código para el email; sirve también para pedir otro desde el segundo paso.
  const pedirCodigo = async (destino, avisoEnviado) => {
    setEnviando(true)
    try {
      await requestPasswordRecovery(destino)
      setEmailEnviado(destino)
      setAviso({ intent: 'info', texto: avisoEnviado })
    } catch {
      setAviso({ intent: 'error', texto: ERROR_ENVIO })
    } finally {
      setEnviando(false)
    }
  }

  const enviarEmail = (event) => {
    event.preventDefault()
    if (enviando) return
    const limpio = email.trim()
    const error = validarEmail(limpio)
    setErrores(error ? { email: error } : {})
    if (error) return
    pedirCodigo(limpio, AVISO_ENVIADO)
  }

  const cambiar = (campo) => (_event, data) => {
    const valor = campo === 'code' ? data.value.replace(/\D/g, '').slice(0, CODE_LENGTH) : data.value
    setDatos((anteriores) => ({ ...anteriores, [campo]: valor }))
  }

  const cambiarContrasena = async (event) => {
    event.preventDefault()
    if (enviando) return
    const encontrados = validarCambio(datos)
    setErrores(encontrados)
    if (Object.keys(encontrados).length > 0) return

    setEnviando(true)
    try {
      await resetPassword({ email: emailEnviado, code: datos.code, password: datos.password })
      navigate(PATHS.LOGIN, { state: { passwordChanged: true } })
    } catch (error) {
      setEnviando(false)
      if (error?.status === 401) {
        setAviso({ intent: 'error', texto: ERROR_CODIGO })
        return
      }
      const { campos, general } = erroresDelServidor(error)
      if (campos) setErrores(campos)
      if (general) setAviso({ intent: 'error', texto: general })
    }
  }

  const pasoEmail = (
    <form className={styles.form} onSubmit={enviarEmail} noValidate>
      <Text>Escribe el correo de tu cuenta y te enviaremos un código para cambiar la contraseña.</Text>
      <Field
        label="Correo electrónico"
        validationState={errores.email ? 'error' : 'none'}
        validationMessage={errores.email}
      >
        <Input
          size="large"
          type="email"
          value={email}
          onChange={(_event, data) => setEmail(data.value)}
          autoComplete="username"
          maxLength={LONGITUD_MAXIMA_EMAIL}
        />
      </Field>
      <Button
        type="submit"
        appearance="primary"
        disabled={enviando}
        icon={enviando ? <Spinner size="tiny" /> : undefined}
      >
        Enviar código
      </Button>
    </form>
  )

  const pasoCambio = (
    <form className={styles.form} onSubmit={cambiarContrasena} noValidate>
      <Field
        label="Código recibido por correo"
        validationState={errores.code ? 'error' : 'none'}
        validationMessage={errores.code}
      >
        <VerificationCodeInput
          value={datos.code}
          onChange={(value) => setDatos((anteriores) => ({ ...anteriores, code: value }))}
        />
      </Field>
      <PasswordField
        label="Contraseña nueva"
        value={datos.password}
        onChange={cambiar('password')}
        error={errores.password}
        autoComplete="new-password"
        maxLength={LONGITUD_MAXIMA_PASSWORD}
        bloquearPegado
      />
      <PasswordRequirements password={datos.password} />
      <PasswordField
        label="Repetir contraseña nueva"
        value={datos.repeticionPassword}
        onChange={cambiar('repeticionPassword')}
        error={errores.repeticionPassword}
        autoComplete="new-password"
        maxLength={LONGITUD_MAXIMA_PASSWORD}
        bloquearPegado
      />
      <div className={styles.actions}>
        <Button
          type="submit"
          appearance="primary"
          size="large"
          disabled={enviando}
          icon={enviando ? <Spinner size="tiny" /> : undefined}
        >
          Cambiar contraseña
        </Button>
        <Button
          type="button"
          appearance="subtle"
          size="large"
          disabled={enviando}
          onClick={() => pedirCodigo(emailEnviado, AVISO_REENVIADO)}
        >
          Pedir otro código
        </Button>
      </div>
    </form>
  )

  return (
    <main className={pageStyles.page}>
      <Card className={pageStyles.card}>
        <Title2 as="h1" className={pageStyles.title}>
          Recuperar contraseña
        </Title2>
        {aviso && (
          <MessageBar intent={aviso.intent}>
            <MessageBarBody>{aviso.texto}</MessageBarBody>
          </MessageBar>
        )}
        {emailEnviado ? pasoCambio : pasoEmail}
        <div className={pageStyles.footer}>
          <LinkButton to={PATHS.LOGIN} appearance="transparent">
            Volver al inicio de sesión
          </LinkButton>
        </div>
      </Card>
    </main>
  )
}
