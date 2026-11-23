import {
  Button,
  Card,
  Checkbox,
  Dialog,
  DialogActions,
  DialogBody,
  DialogContent,
  DialogSurface,
  DialogTitle,
  Field,
  Input,
  MessageBar,
  MessageBarBody,
  Select,
  Text,
  Title2,
  makeStyles,
  tokens,
} from '@fluentui/react-components'
import { BuildingShop24Regular, Cart24Regular } from '@fluentui/react-icons'
import { useEffect, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { registrarCliente, registrarVendedor } from '../api/auth'
import { getCategories } from '../api/products'
import CategoryCombobox from '../components/CategoryCombobox'
import LinkButton from '../components/LinkButton'
import AvatarSelector from '../features/account/AvatarSelector'
import { PasswordField, PasswordRequirements } from '../features/account/PasswordField'
import { erroresDelServidor } from '../features/account/registroErrors'
import { useRegistroPremium } from '../features/account/RegistroPremiumContext'
import { PATHS } from '../routes/paths'
import {
  esEmailValido,
  esNombreValido,
  filtrarNombre,
  esMayorDeEdad,
  esTelefonoValido,
  sinTelefonoVacio,
  validarPasswords,
} from '../features/account/accountRules'

const OBLIGATORIOS_CLIENTE = [
  'nombre',
  'apellidos',
  'fechaNacimiento',
  'dni',
  'email',
  'password',
  'repeticionPassword',
]
const OBLIGATORIOS_VENDEDOR = [
  'nombre',
  'apellidos',
  'nombreComercial',
  'dni',
  'email',
  'password',
  'repeticionPassword',
]

const LONGITUD_MAXIMA = {
  nombre: 30,
  apellidos: 30,
  nombreComercial: 30,
  email: 100,
  password: 64,
  repeticionPassword: 64,
}

const DATOS_INICIALES = {
  nombre: '',
  apellidos: '',
  nombreComercial: '',
  categoriaPrincipalId: '',
  fechaNacimiento: '',
  dni: '',
  email: '',
  telefono: '',
  password: '',
  repeticionPassword: '',
  tipoCliente: 'ESTANDAR',
  avatar: 'avatar1',
  aceptaPrivacidad: false,
}

const useStyles = makeStyles({
  page: {
    flexGrow: 1,
    boxSizing: 'border-box',
    display: 'flex',
    justifyContent: 'center',
    alignItems: 'flex-start',
    paddingTop: tokens.spacingVerticalXXXL,
    paddingBottom: tokens.spacingVerticalXXXL,
    paddingLeft: tokens.spacingHorizontalL,
    paddingRight: tokens.spacingHorizontalL,
  },
  card: {
    width: '100%',
    maxWidth: '560px',
    padding: tokens.spacingVerticalXXL,
    gap: tokens.spacingVerticalL,
    borderRadius: tokens.borderRadiusXLarge,
    boxShadow: tokens.shadow28,
  },
  title: {
    margin: 0,
  },
  description: {
    color: tokens.colorNeutralForeground2,
  },
  opciones: {
    display: 'grid',
    gridTemplateColumns: 'repeat(auto-fit, minmax(180px, 1fr))',
    gap: tokens.spacingHorizontalM,
  },
  privacidad: {
    display: 'flex',
    alignItems: 'center',
    columnGap: tokens.spacingHorizontalS,
    rowGap: tokens.spacingVerticalXS,
    flexWrap: 'wrap',
  },
  textoPrivacidad: {
    display: 'inline-flex',
    alignItems: 'baseline',
    flexWrap: 'wrap',
    columnGap: tokens.spacingHorizontalXXS,
  },
  enlacePrivacidad: {
    minWidth: 'auto',
    padding: 0,
    height: 'auto',
    verticalAlign: 'baseline',
    color: tokens.colorBrandForegroundLink,
    textDecorationLine: 'underline',
  },
  contenidoPolitica: {
    display: 'flex',
    flexDirection: 'column',
    gap: tokens.spacingVerticalL,
  },
  seccionPolitica: {
    display: 'flex',
    flexDirection: 'column',
    gap: tokens.spacingVerticalXS,
  },
  parrafoPolitica: {
    marginTop: 0,
    marginBottom: 0,
    lineHeight: tokens.lineHeightBase300,
  },
  avisoPolitica: {
    color: tokens.colorNeutralForeground2,
  },
})

function validarCamposComunes(datos, obligatorios) {
  const errores = {}
  obligatorios.forEach((campo) => {
    if (!datos[campo].trim()) errores[campo] = 'Este campo es obligatorio'
  })
  for (const campo of ['nombre', 'apellidos']) {
    if (datos[campo] && !esNombreValido(datos[campo])) {
      errores[campo] = 'Introduce entre 2 y 30 caracteres usando solo letras, espacios, guiones o apóstrofos'
    }
  }
  if (datos.email && !esEmailValido(datos.email.trim())) {
    errores.email = 'Introduce un correo electrónico válido. Ejemplo: email@dominio.es'
  }
  if (datos.telefono && !esTelefonoValido(datos.telefono)) {
    errores.telefono = 'Introduce un teléfono válido (9 números)'
  }
  Object.assign(errores, validarPasswords(datos))
  if (!datos.aceptaPrivacidad) {
    errores.aceptaPrivacidad = 'Debes aceptar la política de privacidad'
  }
  return errores
}

// Lo que el formulario muestra al volver de la pasarela Premium (HU45): si el servidor rechazó el registro, los
// errores en sus campos o el mensaje general.
function erroresTrasLaPasarela(resultado) {
  return resultado?.error ? erroresDelServidor(resultado.error) : {}
}

// Registro de cliente y de vendedor (HU1). El cliente Premium (HU45) no se registra al pulsar Registrarse: sus datos
// pasan a la memoria del flujo de registro y se le lleva a la pasarela de pago simulada, que es la única que envía el
// registro como PREMIUM. Al volver de ella, el formulario recupera sus datos y muestra el resultado.
export default function Register() {
  const styles = useStyles()
  const navigate = useNavigate()
  const flujo = useRegistroPremium()
  const [tipo, setTipo] = useState(flujo.pendiente ? 'cliente' : null)
  const [datos, setDatos] = useState(() => ({ ...DATOS_INICIALES, ...(flujo.pendiente ?? {}), avatar: flujo.pendiente?.avatar || DATOS_INICIALES.avatar }))
  const [errores, setErrores] = useState(() => erroresTrasLaPasarela(flujo.resultado).campos ?? {})
  const [errorGeneral, setErrorGeneral] = useState(() => erroresTrasLaPasarela(flujo.resultado).general ?? null)
  const [registrado, setRegistrado] = useState(flujo.resultado?.registrado === true)
  const [categoriasDisponibles, setCategoriasDisponibles] = useState([])
  const [politicaAbierta, setPoliticaAbierta] = useState(false)

  useEffect(() => {
    if (tipo !== 'vendedor') return
    getCategories().then(setCategoriasDisponibles)
  }, [tipo])

  const cambiar = (campo) => (_e, data) =>
    setDatos((actuales) => ({ ...actuales, [campo]: ['nombre', 'apellidos'].includes(campo) ? filtrarNombre(data.value) : data.value }))

  const cambiarCategoria = (categoriaPrincipalId) => setDatos({ ...datos, categoriaPrincipalId })

  // El backend indica con un code qué dato rechaza (PR #104): se marca en su campo o, si no tiene, encima del botón
  const mostrarErroresDelServidor = (error) => {
    const { campos, general } = erroresDelServidor(error)
    if (campos) setErrores(campos)
    setErrorGeneral(general ?? null)
  }

  const enviarCliente = async () => {
    setErrorGeneral(null)
    const nuevosErrores = validarCamposComunes(datos, OBLIGATORIOS_CLIENTE)
    if (datos.fechaNacimiento && !esMayorDeEdad(datos.fechaNacimiento)) {
      nuevosErrores.fechaNacimiento = 'Debes ser mayor de edad para registrarte'
    }
    setErrores(nuevosErrores)

    if (Object.keys(nuevosErrores).length > 0) return

    if (datos.tipoCliente === 'PREMIUM') {
      flujo.iniciarPago(datos)
      navigate(PATHS.REGISTER_PAYMENT)
      return
    }

    try {
      await registrarCliente(sinTelefonoVacio({ ...datos, avatar: datos.avatar || 'avatar1' }))
      setRegistrado(true)
    } catch (error) {
      mostrarErroresDelServidor(error)
    }
  }

  const enviarVendedor = async () => {
    setErrorGeneral(null)
    const nuevosErrores = validarCamposComunes(datos, OBLIGATORIOS_VENDEDOR)
    if (!datos.categoriaPrincipalId) {
      nuevosErrores.categoriaPrincipalId = 'Elige una categoría'
    }
    setErrores(nuevosErrores)

    if (Object.keys(nuevosErrores).length > 0) return

    try {
      await registrarVendedor(sinTelefonoVacio({ ...datos, avatar: datos.avatar || 'avatar1' }))
      setRegistrado(true)
    } catch (error) {
      mostrarErroresDelServidor(error)
    }
  }

  const pantalla = (titulo, contenido) => (
    <main className={styles.page}>
      <Card className={styles.card}>
        <Title2 as="h1" className={styles.title}>
          {titulo}
        </Title2>
        {contenido}
      </Card>
    </main>
  )

  if (registrado) {
    return pantalla(
      'Cuenta creada',
      <>
        <Text className={styles.description}>
          Tu cuenta se ha creado correctamente y está pendiente de que un administrador la verifique.
          Podrás iniciar sesión cuando esté activada.
        </Text>
        {/* Sin redirección automática, para que dé tiempo a leer el aviso (fallos de la demo del Sprint 1) */}
        <LinkButton to={PATHS.LOGIN} appearance="primary">
          Ir a iniciar sesión
        </LinkButton>
      </>
    )
  }

  const campo = (nombre, label, { type, required = true } = {}) => (
    <Field label={label} required={required} validationMessage={errores[nombre]}>
      <Input
        type={type}
        maxLength={LONGITUD_MAXIMA[nombre]}
        value={datos[nombre]}
        onChange={cambiar(nombre)}
      />
    </Field>
  )

  const campoPassword = (nombre, label) => (
    <PasswordField
      label={label}
      value={datos[nombre]}
      onChange={cambiar(nombre)}
      error={errores[nombre]}
      maxLength={LONGITUD_MAXIMA[nombre]}
      bloquearPegado
    />
  )

  const camposComunes = (
    <>
      {campo('dni', 'DNI')}
      {campo('email', 'Correo electrónico', { type: 'email' })}
      {campo('telefono', 'Teléfono', { type: 'tel', required: false })}
      {campoPassword('password', 'Contraseña')}
      <PasswordRequirements password={datos.password} />
      {campoPassword('repeticionPassword', 'Repetir contraseña')}
    </>
  )

  const casillaPrivacidad = (
    <Field validationMessage={errores.aceptaPrivacidad}>
      <div className={styles.privacidad}>
        <Checkbox
          required
          aria-label="Acepto la política de privacidad"
          checked={datos.aceptaPrivacidad}
          onChange={(_event, data) => setDatos({ ...datos, aceptaPrivacidad: data.checked })}
        />
        <Text className={styles.textoPrivacidad}>
          Acepto la{' '}
          <Button
            appearance="transparent"
            className={styles.enlacePrivacidad}
            onClick={() => setPoliticaAbierta(true)}
          >
            política de privacidad
          </Button>
          *
        </Text>
      </div>
    </Field>
  )

  const dialogoPoliticaPrivacidad = (
    <Dialog open={politicaAbierta} onOpenChange={(_event, data) => setPoliticaAbierta(data.open)}>
      <DialogSurface>
        <DialogBody>
          <DialogTitle>Política de privacidad</DialogTitle>
          <DialogContent>
            <div className={styles.contenidoPolitica}>
              <Text className={styles.avisoPolitica} size={200}>
                Texto informativo de ejemplo para el proyecto académico ESIBuy. No constituye una
                política jurídica oficial y deberá revisarse y completarse antes de utilizar la
                aplicación en un entorno real.
              </Text>
              <section className={styles.seccionPolitica}>
                <Text weight="semibold" as="h3">1. Datos que se recogen</Text>
                <Text className={styles.parrafoPolitica}>
                  Durante el registro se solicitan datos de identificación y contacto, como nombre,
                  apellidos y correo electrónico. Según el tipo de cuenta, también pueden solicitarse
                  otros datos necesarios para crear y gestionar el perfil.
                </Text>
              </section>
              <section className={styles.seccionPolitica}>
                <Text weight="semibold" as="h3">2. Finalidad del tratamiento</Text>
                <Text className={styles.parrafoPolitica}>
                  Los datos se utilizan en el contexto de este proyecto para gestionar el registro,
                  identificar a la persona usuaria, administrar su cuenta y permitir el uso de las
                  funcionalidades de ESIBuy correspondientes a su perfil.
                </Text>
              </section>
              <section className={styles.seccionPolitica}>
                <Text weight="semibold" as="h3">3. Conservación y protección</Text>
                <Text className={styles.parrafoPolitica}>
                  Los datos deben conservarse únicamente durante el tiempo necesario para las
                  finalidades indicadas y tratarse conforme a las medidas de seguridad y obligaciones
                  aplicables. Los plazos y medidas concretos quedan pendientes de definir en la
                  política oficial.
                </Text>
              </section>
              <section className={styles.seccionPolitica}>
                <Text weight="semibold" as="h3">4. Derechos y contacto</Text>
                <Text className={styles.parrafoPolitica}>
                  La política definitiva deberá identificar al responsable del tratamiento, facilitar
                  sus datos de contacto e indicar cómo ejercer los derechos reconocidos por la
                  normativa de protección de datos. Esta información todavía no está concretada en
                  esta versión de ejemplo.
                </Text>
              </section>
            </div>
          </DialogContent>
          <DialogActions>
            <Button appearance="primary" onClick={() => setPoliticaAbierta(false)}>
              Cerrar
            </Button>
          </DialogActions>
        </DialogBody>
      </DialogSurface>
    </Dialog>
  )

  const selectorAvatar = <AvatarSelector value={datos.avatar} onChange={cambiar('avatar')} />

  if (tipo === 'cliente') {
    return pantalla(
      'Registro de cliente',
      <>
        {campo('nombre', 'Nombre')}
        {campo('apellidos', 'Apellidos')}
        {campo('fechaNacimiento', 'Fecha de nacimiento', { type: 'date' })}
        {camposComunes}
        <Field
          label="Tipo de cliente"
          required
          hint={
            datos.tipoCliente === 'PREMIUM'
              ? 'Antes de crear la cuenta pasarás por una pasarela de pago simulada.'
              : undefined
          }
        >
          <Select value={datos.tipoCliente} onChange={cambiar('tipoCliente')}>
            <option value="ESTANDAR">Estándar</option>
            <option value="PREMIUM">Premium</option>
          </Select>
        </Field>
        {selectorAvatar}
        {casillaPrivacidad}
        {dialogoPoliticaPrivacidad}
        {errorGeneral && (
          <MessageBar intent="error">
            <MessageBarBody>{errorGeneral}</MessageBarBody>
          </MessageBar>
        )}
        <Button appearance="primary" size="large" onClick={enviarCliente}>
          Registrarse
        </Button>
      </>
    )
  }

  if (tipo === 'vendedor') {
    return pantalla(
      'Registro de vendedor',
      <>
        {campo('nombre', 'Nombre')}
        {campo('apellidos', 'Apellidos')}
        {campo('nombreComercial', 'Nombre comercial')}
        <Field
          label="Categoría"
          required
          validationState={errores.categoriaPrincipalId ? 'error' : 'none'}
          validationMessage={errores.categoriaPrincipalId}
        >
          <CategoryCombobox
            value={datos.categoriaPrincipalId}
            categories={categoriasDisponibles}
            searchable={false}
            placeholder="Selecciona una categoría"
            onChange={cambiarCategoria}
          />
        </Field>
        {camposComunes}
        {selectorAvatar}
        {casillaPrivacidad}
        {dialogoPoliticaPrivacidad}
        {errorGeneral && (
          <MessageBar intent="error">
            <MessageBarBody>{errorGeneral}</MessageBarBody>
          </MessageBar>
        )}
        <Button appearance="primary" size="large" onClick={enviarVendedor}>
          Registrarse
        </Button>
      </>
    )
  }

  return pantalla(
    'Crear cuenta',
    <>
      <Text className={styles.description}>Elige el tipo de cuenta que quieres crear.</Text>
      <div className={styles.opciones}>
        <Button size="large" icon={<Cart24Regular />} onClick={() => setTipo('cliente')}>
          Cliente
        </Button>
        <Button size="large" icon={<BuildingShop24Regular />} onClick={() => setTipo('vendedor')}>
          Vendedor
        </Button>
      </div>
    </>
  )
}