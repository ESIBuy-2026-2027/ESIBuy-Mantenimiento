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
  Text,
  Title2,
  makeStyles,
  tokens,
} from '@fluentui/react-components'
import { BuildingShop24Regular, Cart24Regular, Crown24Regular, Person24Regular } from '@fluentui/react-icons'
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

const MOBILE = '@media (max-width: 760px)'
const useStyles = makeStyles({
  page: { minHeight: '100dvh', boxSizing: 'border-box', display: 'flex', justifyContent: 'center', alignItems: 'center', padding: '10px 20px', backgroundColor: '#fffaf6', backgroundImage: 'radial-gradient(ellipse at 0% 100%, rgba(255, 139, 64, .18), transparent 35%), radial-gradient(ellipse at 100% 0%, rgba(255, 153, 82, .18), transparent 32%), linear-gradient(135deg, #fffaf6 0%, #ffffff 52%, #fff7f0 100%)', [MOBILE]: { alignItems: 'flex-start', padding: '12px 10px' } },
  card: { width: '100%', maxWidth: '1060px', boxSizing: 'border-box', padding: '16px 34px', display: 'flex', flexDirection: 'column', gap: '8px', border: '1px solid rgba(228, 218, 209, .85)', borderRadius: '22px', backgroundColor: 'rgba(255, 255, 255, .96)', boxShadow: '0 16px 42px rgba(76, 48, 27, .11)', [MOBILE]: { padding: '20px 16px', borderRadius: '18px' } },
  heading: { display: 'flex', flexDirection: 'column', gap: '2px' },
  title: { margin: 0, fontSize: 'clamp(26px, 2.6vw, 34px)', lineHeight: 1.12, letterSpacing: '-.8px' },
  description: { color: tokens.colorNeutralForeground2, margin: 0, fontSize: tokens.fontSizeBase200 },
  fieldsGrid: { display: 'grid', gridTemplateColumns: 'repeat(2, minmax(0, 1fr))', columnGap: '22px', rowGap: '7px', [MOBILE]: { gridTemplateColumns: '1fr', rowGap: '10px' } },
  passwordFields: { display: 'grid', gridTemplateColumns: 'repeat(2, minmax(0, 1fr))', columnGap: '22px', rowGap: '8px', [MOBILE]: { gridTemplateColumns: '1fr' } },
  passwordRequirements: { gridColumn: '1 / -1' },
  field: { minWidth: 0 }, fullWidth: { gridColumn: '1 / -1' },
  opciones: { display: 'grid', gridTemplateColumns: 'repeat(2, minmax(0, 1fr))', gap: '12px', [MOBILE]: { gridTemplateColumns: '1fr' } },
  tipoCuenta: { display: 'flex', flexDirection: 'column', gap: '7px' },
  tipoCuentaOpciones: { display: 'grid', gridTemplateColumns: 'repeat(2, minmax(0, 1fr))', gap: '12px', [MOBILE]: { gridTemplateColumns: '1fr' } },
  tipoCuentaCard: { minHeight: '68px', height: 'auto', display: 'flex', justifyContent: 'flex-start', alignItems: 'center', gap: '12px', padding: '12px 16px', whiteSpace: 'normal', textAlign: 'left', border: '1px solid #cfd6df', borderRadius: '12px', backgroundColor: '#fff', color: tokens.colorNeutralForeground1, ':hover': { borderColor: '#f05a0a', backgroundColor: '#fffaf6' } },
  tipoCuentaSeleccionada: { border: '2px solid #f05a0a', outline: '1px solid #f05a0a', outlineOffset: '-3px', boxShadow: '0 0 0 2px rgba(240, 90, 10, .12)', backgroundColor: '#fff5ed', padding: '11px 15px' },
  tipoCuentaIcono: { color: tokens.colorBrandForeground1, flexShrink: 0, fontSize: '25px' },
  tipoCuentaTexto: { display: 'flex', flexDirection: 'column', alignItems: 'flex-start', gap: '2px', whiteSpace: 'normal' },
  tipoCuentaNombre: { fontWeight: tokens.fontWeightBold, fontSize: tokens.fontSizeBase300 },
  tipoCuentaDetalle: { color: tokens.colorNeutralForeground2, fontSize: tokens.fontSizeBase200, lineHeight: 1.3, fontWeight: tokens.fontWeightRegular },
  privacidad: { display: 'flex', justifyContent: 'flex-start', alignItems: 'center', columnGap: tokens.spacingHorizontalS, rowGap: tokens.spacingVerticalXS, flexWrap: 'wrap', textAlign: 'left' },
  privacidadContenedor: { display: 'flex', justifyContent: 'flex-start', width: '100%' },
  tipoCuentaReserva: { minHeight: '94px', display: 'flex', flexDirection: 'column', justifyContent: 'flex-start' },
  tipoCuentaReservaVacia: { minHeight: '104px' },
  zonaInferior: { display: 'flex', flexDirection: 'column', alignItems: 'center', justifyContent: 'center', gap: '7px', width: '100%' },
  textoPrivacidad: { display: 'inline-flex', alignItems: 'baseline', flexWrap: 'wrap', columnGap: tokens.spacingHorizontalXXS },
  enlacePrivacidad: { minWidth: 'auto', padding: 0, height: 'auto', verticalAlign: 'baseline', color: tokens.colorBrandForegroundLink, textDecorationLine: 'underline' },
  contenidoPolitica: { display: 'flex', flexDirection: 'column', gap: tokens.spacingVerticalL },
  seccionPolitica: { display: 'flex', flexDirection: 'column', gap: tokens.spacingVerticalXS },
  parrafoPolitica: { marginTop: 0, marginBottom: 0, lineHeight: tokens.lineHeightBase300 },
  avisoPolitica: { color: tokens.colorNeutralForeground2 },
  submit: { width: '220px', minWidth: '220px', minHeight: '40px', backgroundColor: '#f05a0a', ':hover': { backgroundColor: '#d94d06' } },
  loginFooter: { display: 'flex', justifyContent: 'center', alignItems: 'center', flexWrap: 'wrap', gap: tokens.spacingHorizontalXS, color: tokens.colorNeutralForeground2 },
  loginLink: { color: tokens.colorBrandForeground1, fontWeight: tokens.fontWeightBold },
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
  const [tipo, setTipo] = useState('cliente')
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

  useEffect(() => {
    const primerCampoConError = document.querySelector('[data-register-error="true"]')
    const destino = primerCampoConError ?? document.querySelector('[data-register-error-message="true"]')
    destino?.scrollIntoView?.({ behavior: 'smooth', block: 'center' })
  }, [errores, errorGeneral])

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

  const pantalla = (titulo, contenido, subtitulo = 'Crea tu cuenta y empieza a disfrutar de ESIBuy.') => (
    <main className={styles.page}>
      <Card className={styles.card}>
        <div className={styles.heading}>
          <Title2 as="h1" className={styles.title}>{titulo}</Title2>
          <Text className={styles.description}>{subtitulo}</Text>
        </div>
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
    <div className={styles.field} data-register-error={errores[nombre] ? 'true' : undefined}>
      <Field label={label} required={required} validationMessage={errores[nombre]}>
        <Input
          type={type}
          maxLength={LONGITUD_MAXIMA[nombre]}
          value={datos[nombre]}
          onChange={cambiar(nombre)}
        />
      </Field>
    </div>
  )

  const campoPassword = (nombre, label) => (
    <div className={styles.field} data-register-error={errores[nombre] ? 'true' : undefined}>
      <PasswordField
        label={label}
        value={datos[nombre]}
        onChange={cambiar(nombre)}
        error={errores[nombre]}
        maxLength={LONGITUD_MAXIMA[nombre]}
        bloquearPegado
      />
    </div>
  )



  const casillaPrivacidad = (
    <div className={styles.privacidadContenedor} data-register-error={errores.aceptaPrivacidad ? 'true' : undefined}>
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
    </div>
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

  const selectorTipoRegistro = (
    <div className={styles.tipoCuenta}>
      <Text weight="semibold">Quiero registrarme como</Text>
      <div className={styles.tipoCuentaOpciones} role="group" aria-label="Tipo de registro">
        <Button type="button" appearance="outline" aria-pressed={tipo === 'cliente'} className={tipo === 'cliente' ? styles.tipoCuentaCard + ' ' + styles.tipoCuentaSeleccionada : styles.tipoCuentaCard} onClick={() => setTipo('cliente')}><Cart24Regular className={styles.tipoCuentaIcono} /><span className={styles.tipoCuentaTexto}><span className={styles.tipoCuentaNombre}>Cliente</span><span className={styles.tipoCuentaDetalle}>Descubre productos y gestiona tus compras.</span></span></Button>
        <Button type="button" appearance="outline" aria-pressed={tipo === 'vendedor'} className={tipo === 'vendedor' ? styles.tipoCuentaCard + ' ' + styles.tipoCuentaSeleccionada : styles.tipoCuentaCard} onClick={() => setTipo('vendedor')}><BuildingShop24Regular className={styles.tipoCuentaIcono} /><span className={styles.tipoCuentaTexto}><span className={styles.tipoCuentaNombre}>Vendedor</span><span className={styles.tipoCuentaDetalle}>Publica productos y gestiona tu tienda.</span></span></Button>
      </div>
    </div>
  )

  const selectorTipoCliente = (
    <div className={styles.tipoCuenta}>
      <Text weight="semibold">Tipo de cuenta</Text>
      <div className={styles.tipoCuentaOpciones} role="group" aria-label="Tipo de cuenta">
        <Button type="button" appearance="outline" aria-pressed={datos.tipoCliente === 'ESTANDAR'} className={datos.tipoCliente === 'ESTANDAR' ? styles.tipoCuentaCard + ' ' + styles.tipoCuentaSeleccionada : styles.tipoCuentaCard} onClick={() => setDatos((actuales) => ({ ...actuales, tipoCliente: 'ESTANDAR' }))}>
          <Person24Regular className={styles.tipoCuentaIcono} />
          <span className={styles.tipoCuentaTexto}><span className={styles.tipoCuentaNombre}>Estándar</span><span className={styles.tipoCuentaDetalle}>Compra y vende en el marketplace y gestiona tus pedidos.</span></span>
        </Button>
        <Button type="button" appearance="outline" aria-pressed={datos.tipoCliente === 'PREMIUM'} className={datos.tipoCliente === 'PREMIUM' ? styles.tipoCuentaCard + ' ' + styles.tipoCuentaSeleccionada : styles.tipoCuentaCard} onClick={() => setDatos((actuales) => ({ ...actuales, tipoCliente: 'PREMIUM' }))}>
          <Crown24Regular className={styles.tipoCuentaIcono} />
          <span className={styles.tipoCuentaTexto}><span className={styles.tipoCuentaNombre}>Premium</span><span className={styles.tipoCuentaDetalle}>Accede a las ventajas Premium disponibles en ESIBuy.</span></span>
        </Button>
      </div>
      {datos.tipoCliente === 'PREMIUM' && <Text size={200} className={styles.description}>Antes de crear la cuenta pasarás por una pasarela de pago simulada.</Text>}
    </div>
  )

  const camposIdentidad = (
    <div className={styles.fieldsGrid}>
      {campo('nombre', 'Nombre')}
      {campo('apellidos', 'Apellidos')}
      {tipo === 'cliente' ? campo('fechaNacimiento', 'Fecha de nacimiento', { type: 'date' }) : campo('nombreComercial', 'Nombre comercial')}
      {tipo === 'cliente' ? campo('dni', 'DNI') : (
        <div className={styles.field} data-register-error={errores.categoriaPrincipalId ? 'true' : undefined}>
          <Field label="Categoría principal" required validationState={errores.categoriaPrincipalId ? 'error' : 'none'} validationMessage={errores.categoriaPrincipalId}>
            <CategoryCombobox value={datos.categoriaPrincipalId} categories={categoriasDisponibles} searchable={false} placeholder="Selecciona una categoría" onChange={cambiarCategoria} />
          </Field>
        </div>
      )}
      {tipo === 'vendedor' && campo('dni', 'DNI')}
      {campo('email', 'Correo electrónico', { type: 'email' })}
      {campo('telefono', 'Teléfono', { type: 'tel', required: false })}
    </div>
  )

  const camposSeguridad = (
    <>
      <div className={styles.passwordFields}>
        {campoPassword('password', 'Contraseña')}
        {campoPassword('repeticionPassword', 'Repetir contraseña')}
      </div>
      <div className={styles.passwordRequirements}>
        <PasswordRequirements password={datos.password} />
      </div>
    </>
  )

  const contenidoRegistro = (esVendedor) => (
    <>
      <div className={styles.tipoCuenta}>
        <Text weight="semibold">Quiero registrarme como</Text>
        <div className={styles.tipoCuentaOpciones} role="group" aria-label="Tipo de registro">
          <Button type="button" appearance="outline" aria-pressed={!esVendedor} className={!esVendedor ? styles.tipoCuentaCard + ' ' + styles.tipoCuentaSeleccionada : styles.tipoCuentaCard} onClick={() => setTipo('cliente')}>
            <Cart24Regular className={styles.tipoCuentaIcono} />
            <span className={styles.tipoCuentaTexto}><span className={styles.tipoCuentaNombre}>Cliente</span><span className={styles.tipoCuentaDetalle}>Descubre productos y gestiona tus compras.</span></span>
          </Button>
          <Button type="button" appearance="outline" aria-pressed={esVendedor} className={esVendedor ? styles.tipoCuentaCard + ' ' + styles.tipoCuentaSeleccionada : styles.tipoCuentaCard} onClick={() => setTipo('vendedor')}>
            <BuildingShop24Regular className={styles.tipoCuentaIcono} />
            <span className={styles.tipoCuentaTexto}><span className={styles.tipoCuentaNombre}>Vendedor</span><span className={styles.tipoCuentaDetalle}>Publica productos y gestiona tu tienda.</span></span>
          </Button>
        </div>
      </div>
      {camposIdentidad}
      <div className={styles.tipoCuentaReserva}>{esVendedor ? <div className={styles.tipoCuentaReservaVacia} aria-hidden="true" /> : selectorTipoCliente}</div>
      {camposSeguridad}
      {selectorAvatar}
      {casillaPrivacidad}
      {dialogoPoliticaPrivacidad}
      {errorGeneral && <div data-register-error-message="true"><MessageBar intent="error"><MessageBarBody>{errorGeneral}</MessageBarBody></MessageBar></div>}
      <div className={styles.zonaInferior}>
        <Button appearance="primary" size="medium" className={styles.submit} onClick={esVendedor ? enviarVendedor : enviarCliente}>Registrarse</Button>
        <div className={styles.loginFooter}>¿Ya tienes una cuenta? <LinkButton to={PATHS.LOGIN} appearance="transparent" className={styles.loginLink}>Iniciar sesión</LinkButton></div>
      </div>
    </>
  )

  return pantalla(tipo === 'vendedor' ? 'Registro de vendedor' : 'Registro de cliente', contenidoRegistro(tipo === 'vendedor'), tipo === 'vendedor' ? 'Crea tu cuenta de vendedor y empieza a publicar tus productos en ESIBuy.' : 'Crea tu cuenta y empieza a disfrutar de ESIBuy.')
}