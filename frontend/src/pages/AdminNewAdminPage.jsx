import { useState } from 'react'
import {
  Button,
  Card,
  Field,
  Input,
  MessageBar,
  MessageBarBody,
  MessageBarTitle,
  Text,
  Title1,
  makeStyles,
  mergeClasses,
  tokens,
} from '@fluentui/react-components'
import { ArrowLeft20Regular } from '@fluentui/react-icons'
import { createAdmin } from '../api/admins'
import LinkButton from '../components/LinkButton'
import { usePageStyles } from '../components/pageStyles'
import AvatarSelector from '../features/account/AvatarSelector'
import { PasswordField, PasswordRequirements } from '../features/account/PasswordField'
import { esEmailValido, validarPasswords } from '../features/account/accountRules'
import { PATHS } from '../routes/paths'

const OBLIGATORIOS = ['nombre', 'apellidos', 'email', 'password', 'repeticionPassword']

const LONGITUD_MAXIMA = {
  nombre: 30,
  apellidos: 30,
  email: 100,
  sede: 30,
  password: 64,
  repeticionPassword: 64,
}

const DATOS_INICIALES = {
  nombre: '',
  apellidos: '',
  email: '',
  sede: '',
  password: '',
  repeticionPassword: '',
  avatar: '',
}

const formatoFecha = new Intl.DateTimeFormat('es-ES', { day: '2-digit', month: '2-digit', year: 'numeric' })

const useStyles = makeStyles({
  // Columna más estrecha que la de los listados; sigue centrada en la pantalla (margin auto).
  page: {
    maxWidth: '720px',
  },
  card: {
    padding: tokens.spacingVerticalXXL,
    gap: tokens.spacingVerticalL,
  },
  actions: {
    display: 'flex',
    flexWrap: 'wrap',
    gap: tokens.spacingHorizontalM,
  },
})

function validar(datos) {
  const errores = {}
  OBLIGATORIOS.forEach((campo) => {
    if (!datos[campo].trim()) errores[campo] = 'Este campo es obligatorio'
  })
  if (datos.email && !esEmailValido(datos.email)) {
    errores.email = 'Introduce un email válido'
  }
  return { ...errores, ...validarPasswords(datos) }
}

export default function AdminNewAdminPage() {
  const styles = usePageStyles()
  const own = useStyles()
  const [datos, setDatos] = useState(DATOS_INICIALES)
  const [errores, setErrores] = useState({})
  const [enviando, setEnviando] = useState(false)
  const [errorServidor, setErrorServidor] = useState(false)
  const [creado, setCreado] = useState(null)
  // La fecha de incorporación es la del alta: se muestra, pero no se edita ni se envía.
  const [fechaIncorporacion] = useState(() => formatoFecha.format(new Date()))

  const cambiar = (campo) => (_event, data) => setDatos({ ...datos, [campo]: data.value })

  const crear = async () => {
    const nuevosErrores = validar(datos)
    setErrores(nuevosErrores)
    setErrorServidor(false)
    if (Object.keys(nuevosErrores).length > 0) return

    setEnviando(true)
    try {
      const { nombre, apellidos, email, sede, avatar, password } = datos
      await createAdmin({ nombre, apellidos, email, sede, avatar, password })
      setCreado(`${nombre} ${apellidos}`)
    } catch (error) {
      if (error.status === 409) {
        setErrores({ email: 'Este email ya está registrado' })
      } else {
        setErrorServidor(true)
      }
    } finally {
      setEnviando(false)
    }
  }

  const crearOtro = () => {
    setDatos(DATOS_INICIALES)
    setErrores({})
    setCreado(null)
  }

  const campo = (nombre, label, { type, required = true } = {}) => (
    <Field label={label} required={required} validationMessage={errores[nombre]}>
      <Input type={type} maxLength={LONGITUD_MAXIMA[nombre]} value={datos[nombre]} onChange={cambiar(nombre)} />
    </Field>
  )

  const campoPassword = (nombre, label) => (
    <PasswordField
      label={label}
      value={datos[nombre]}
      onChange={cambiar(nombre)}
      error={errores[nombre]}
      maxLength={LONGITUD_MAXIMA[nombre]}
    />
  )

  return (
    <main className={mergeClasses(styles.page, own.page)}>
      <div className={styles.heading}>
        <div className={styles.titleBlock}>
          <Title1 as="h1" className={styles.title}>
            Nuevo administrador
          </Title1>
          <Text className={styles.subtitle}>Da de alta a una persona del equipo con acceso al panel de control.</Text>
        </div>
        <LinkButton to={PATHS.ADMIN_ADMINS} appearance="subtle" icon={<ArrowLeft20Regular />}>
          Volver a administradores
        </LinkButton>
      </div>

      {creado ? (
        <Card className={own.card}>
          <MessageBar intent="success">
            <MessageBarBody>
              <MessageBarTitle>Administrador creado</MessageBarTitle>
              {creado} ya puede iniciar sesión con su email y el password indicado.
            </MessageBarBody>
          </MessageBar>
          <div className={own.actions}>
            <LinkButton to={PATHS.ADMIN_ADMINS} appearance="primary">
              Ver administradores
            </LinkButton>
            <Button onClick={crearOtro}>Crear otro administrador</Button>
          </div>
        </Card>
      ) : (
        <Card className={own.card}>
          {errorServidor && (
            <MessageBar intent="error">
              <MessageBarBody>No se ha podido crear el administrador. Inténtalo de nuevo.</MessageBarBody>
            </MessageBar>
          )}
          {campo('nombre', 'Nombre')}
          {campo('apellidos', 'Apellidos')}
          {campo('email', 'Email', { type: 'email' })}
          {campo('sede', 'Sede', { required: false })}
          <Field label="Fecha de incorporación" hint="Se asigna automáticamente con la fecha de alta.">
            <Input value={fechaIncorporacion} readOnly />
          </Field>
          {campoPassword('password', 'Password')}
          <PasswordRequirements password={datos.password} />
          {campoPassword('repeticionPassword', 'Repetir password')}
          <AvatarSelector value={datos.avatar} onChange={cambiar('avatar')} />
          <div className={own.actions}>
            <Button appearance="primary" onClick={crear} disabled={enviando}>
              Crear administrador
            </Button>
          </div>
        </Card>
      )}
    </main>
  )
}
