
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
import {
  BuildingShop24Regular,
  Cart24Regular,
  Crown24Regular,
  Person24Regular,
} from '@fluentui/react-icons'
import { useEffect, useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { registrarCliente, registrarVendedor } from '../api/auth'
import { getCategories } from '../api/products'
import CategoryCombobox from '../components/CategoryCombobox'
import LinkButton from '../components/LinkButton'
import AvatarSelector from '../features/account/AvatarSelector'
import {
  PasswordField,
  PasswordRequirements,
} from '../features/account/PasswordField'
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
  page: {
    minHeight: '100dvh',
    boxSizing: 'border-box',
    display: 'flex',
    justifyContent: 'center',
    alignItems: 'flex-start',
    padding: '28px 20px',
    backgroundColor: '#fffaf6',
    backgroundImage:
      'radial-gradient(ellipse at 2% 100%, rgba(255, 143, 75, 0.24) 0%, transparent 38%), radial-gradient(ellipse at 48% 0%, rgba(255, 179, 119, 0.18) 0%, transparent 34%), radial-gradient(ellipse at 100% 8%, rgba(255, 155, 82, 0.19) 0%, transparent 35%), linear-gradient(112deg, #fffaf6 0%, #ffffff 54%, #fffaf6 100%)',
    [MOBILE]: {
      alignItems: 'flex-start',
      padding: '12px 10px',
    },
  },

  card: {
    width: '100%',
    maxWidth: '1180px',
    boxSizing: 'border-box',
    minHeight: '860px',
    padding: '34px 44px',
    display: 'flex',
    flexDirection: 'column',
    gap: '16px',
    border: '1px solid rgba(228, 218, 209, .85)',
    borderRadius: '28px',
    backgroundColor: 'rgba(255, 255, 255, .92)',
    boxShadow: '0 22px 55px rgba(45, 35, 25, .12)',
    backdropFilter: 'blur(14px)',
    [MOBILE]: {
      minHeight: 'auto',
      padding: '22px 16px',
      borderRadius: '20px',
    },
  },

  brand: {
    display: 'inline-flex',
    alignItems: 'center',
    alignSelf: 'flex-start',
    marginBottom: '2px',
    color: tokens.colorNeutralForeground1,
    fontSize: '30px',
    fontWeight: tokens.fontWeightBold,
    letterSpacing: '-1px',
    textDecorationLine: 'none',
    ':hover': { color: tokens.colorNeutralForeground1 },
    ':focus-visible': {
      outline: `3px solid ${tokens.colorBrandStroke1}`,
      outlineOffset: '4px',
      borderRadius: tokens.borderRadiusSmall,
    },
  },

  logoAccent: { color: tokens.colorBrandForeground1 },

  heading: {
    display: 'flex',
    flexDirection: 'column',
    gap: '3px',
  },

  title: {
    margin: 0,
    fontSize: 'clamp(32px, 2.8vw, 40px)',
    lineHeight: 1.12,
    letterSpacing: '-.8px',
  },

  description: {
    color: tokens.colorNeutralForeground2,
    margin: 0,
    fontSize: tokens.fontSizeBase300,
  },

  fieldsGrid: {
    display: 'grid',
    gridTemplateColumns: 'repeat(2, minmax(0, 1fr))',
    columnGap: '28px',
    rowGap: '14px',
    [MOBILE]: {
      gridTemplateColumns: '1fr',
      rowGap: '10px',
    },
  },

  passwordFields: {
    display: 'grid',
    gridTemplateColumns: 'repeat(2, minmax(0, 1fr))',
    columnGap: '28px',
    rowGap: '12px',
    [MOBILE]: {
      gridTemplateColumns: '1fr',
    },
  },

  passwordColumn: {
    display: 'flex',
    flexDirection: 'column',
    gap: tokens.spacingVerticalS,
    minWidth: 0,
  },

  passwordRequirements: {
    marginTop: 0,
  },

  avatarSection: {
    marginTop: tokens.spacingVerticalL,
  },

  field: {
    minWidth: 0,
  },

  fullWidth: {
    gridColumn: '1 / -1',
  },

  opciones: {
    display: 'grid',
    gridTemplateColumns: 'repeat(2, minmax(0, 1fr))',
    gap: '12px',
    [MOBILE]: {
      gridTemplateColumns: '1fr',
    },
  },

  tipoCuenta: {
    display: 'flex',
    flexDirection: 'column',
    gap: '10px',
  },

  tipoCuentaOpciones: {
    display: 'grid',
    gridTemplateColumns: 'repeat(2, minmax(0, 1fr))',
    gap: '16px',
    [MOBILE]: {
      gridTemplateColumns: '1fr',
    },
  },

  // Reset de los estilos nativos de fieldset.
  grupoOpciones: {
    border: 0,
    padding: 0,
    margin: 0,
    minWidth: 0,
  },

  // Leyenda accesible sin alterar la apariencia visual.
  leyendaOpciones: {
    position: 'absolute',
    width: '1px',
    height: '1px',
    padding: 0,
    margin: '-1px',
    overflow: 'hidden',
    clip: 'rect(0, 0, 0, 0)',
    whiteSpace: 'nowrap',
    border: 0,
  },

  tipoCuentaCard: {
    minHeight: '82px',
    height: 'auto',
    display: 'flex',
    justifyContent: 'flex-start',
    alignItems: 'center',
    gap: '16px',
    padding: '16px 20px',
    whiteSpace: 'normal',
    textAlign: 'left',
    border: '1px solid #cfd6df',
    borderRadius: '12px',
    backgroundColor: '#fff',
    color: tokens.colorNeutralForeground1,

    ':hover': {
      border: '1px solid #f05a0a',
      backgroundColor: '#fffaf6',
    },

    '&[aria-pressed="true"]': {
      border: '1.5px solid #f05a0a',
      backgroundColor: '#fff5ed',
      padding: '11px 15px',
    },

    '&[aria-pressed="true"]:hover': {
      border: '1.5px solid #f05a0a',
      backgroundColor: '#fff5ed',
    },
  },

  tipoCuentaIcono: {
    color: tokens.colorBrandForeground1,
    flexShrink: 0,
    fontSize: '28px',
  },

  tipoCuentaTexto: {
    display: 'flex',
    flexDirection: 'column',
    alignItems: 'flex-start',
    gap: '2px',
    whiteSpace: 'normal',
  },

  tipoCuentaNombre: {
    fontWeight: tokens.fontWeightBold,
    fontSize: tokens.fontSizeBase400,
  },

  tipoCuentaDetalle: {
    color: tokens.colorNeutralForeground2,
    fontSize: tokens.fontSizeBase300,
    lineHeight: 1.4,
    fontWeight: tokens.fontWeightRegular,
  },

  privacidad: {
    display: 'flex',
    justifyContent: 'flex-start',
    alignItems: 'center',
    columnGap: tokens.spacingHorizontalS,
    rowGap: tokens.spacingVerticalXS,
    flexWrap: 'wrap',
    textAlign: 'left',
  },

  privacidadContenedor: {
    display: 'flex',
    justifyContent: 'flex-start',
    width: '100%',
  },

  zonaInferior: {
    display: 'flex',
    flexDirection: 'column',
    alignItems: 'center',
    justifyContent: 'center',
    gap: '7px',
    width: '100%',
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

  submit: {
    width: '220px',
    minWidth: '220px',
    minHeight: '58px',
    padding: '14px 28px',
    borderRadius: '12px',
    backgroundColor: '#f05a0a',
    color: '#ffffff',
    fontSize: '16px',
    fontWeight: tokens.fontWeightBold,
    boxShadow: '0 8px 18px rgba(240, 90, 10, 0.24)',
    transitionProperty: 'background-color, box-shadow, transform',
    transitionDuration: '160ms',
    ':hover': {
      backgroundColor: '#d94d06',
      boxShadow: '0 10px 22px rgba(240, 90, 10, 0.3)',
      transform: 'translateY(-1px)',
    },
    ':active': {
      transform: 'translateY(0)',
      boxShadow: '0 4px 10px rgba(240, 90, 10, 0.2)',
    },
    ':focus-visible': {
      outline: '3px solid #ffb27d',
      outlineOffset: '3px',
    },
    [MOBILE]: {
      width: '100%',
      minWidth: 0,
      maxWidth: '320px',
    },
  },

  loginFooter: {
    display: 'flex',
    justifyContent: 'center',
    alignItems: 'center',
    flexWrap: 'wrap',
    gap: tokens.spacingHorizontalXS,
    color: tokens.colorNeutralForeground2,
  },

  loginLink: {
    color: tokens.colorBrandForeground1,
    fontWeight: tokens.fontWeightBold,
  },
})

function validarCamposComunes(datos, obligatorios) {
  const errores = {}

  obligatorios.forEach((campo) => {
    if (!datos[campo].trim()) {
      errores[campo] = 'Este campo es obligatorio'
    }
  })

  for (const campo of ['nombre', 'apellidos']) {
    if (datos[campo] && !esNombreValido(datos[campo])) {
      errores[campo] =
        'Introduce entre 2 y 30 caracteres usando solo letras, espacios, guiones o apóstrofos'
    }
  }

  if (datos.email && !esEmailValido(datos.email.trim())) {
    errores.email =
      'Introduce un correo electrónico válido. Ejemplo: email@dominio.es'
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

// Lo que el formulario muestra al volver de la pasarela Premium (HU45).
function erroresTrasLaPasarela(resultado) {
  return resultado?.error ? erroresDelServidor(resultado.error) : {}
}

// Registro de cliente y vendedor (HU1).
// El cliente Premium pasa primero por la pasarela simulada (HU45).
export default function Register() {
  const styles = useStyles()
  const navigate = useNavigate()
  const flujo = useRegistroPremium()

  const [tipo, setTipo] = useState('cliente')

  const [datos, setDatos] = useState(() => ({
    ...DATOS_INICIALES,
    ...(flujo.pendiente ?? {}),
    avatar: flujo.pendiente?.avatar || DATOS_INICIALES.avatar,
  }))

  const [errores, setErrores] = useState(
    () => erroresTrasLaPasarela(flujo.resultado).campos ?? {},
  )

  const [errorGeneral, setErrorGeneral] = useState(
    () => erroresTrasLaPasarela(flujo.resultado).general ?? null,
  )

  const [registrado, setRegistrado] = useState(
    flujo.resultado?.registrado === true,
  )

  const [categoriasDisponibles, setCategoriasDisponibles] = useState([])
  const [politicaAbierta, setPoliticaAbierta] = useState(false)

  useEffect(() => {
    if (tipo !== 'vendedor') return

    getCategories().then(setCategoriasDisponibles)
  }, [tipo])

  useEffect(() => {
    const primerCampoConError = document.querySelector(
      '[data-register-error="true"]',
    )

    const destino =
      primerCampoConError ??
      document.querySelector('[data-register-error-message="true"]')

    destino?.scrollIntoView?.({
      behavior: 'smooth',
      block: 'center',
    })
  }, [errores, errorGeneral])

  const cambiar = (campo) => (_e, data) =>
    setDatos((actuales) => ({
      ...actuales,
      [campo]: ['nombre', 'apellidos'].includes(campo)
        ? filtrarNombre(data.value)
        : data.value,
    }))

  const cambiarCategoria = (categoriaPrincipalId) =>
    setDatos((actuales) => ({
      ...actuales,
      categoriaPrincipalId,
    }))

  // El backend indica mediante un código qué dato rechaza.
  const mostrarErroresDelServidor = (error) => {
    const { campos, general } = erroresDelServidor(error)

    if (campos) {
      setErrores(campos)
    }

    setErrorGeneral(general ?? null)
  }

  const enviarCliente = async () => {
    setErrorGeneral(null)

    const nuevosErrores = validarCamposComunes(
      datos,
      OBLIGATORIOS_CLIENTE,
    )

    if (
      datos.fechaNacimiento &&
      !esMayorDeEdad(datos.fechaNacimiento)
    ) {
      nuevosErrores.fechaNacimiento =
        'Debes ser mayor de edad para registrarte'
    }

    setErrores(nuevosErrores)

    if (Object.keys(nuevosErrores).length > 0) return

    if (datos.tipoCliente === 'PREMIUM') {
      flujo.iniciarPago(datos)
      navigate(PATHS.REGISTER_PAYMENT)
      return
    }

    try {
      await registrarCliente(
        sinTelefonoVacio({
          ...datos,
          avatar: datos.avatar || 'avatar1',
        }),
      )

      setRegistrado(true)
    } catch (error) {
      mostrarErroresDelServidor(error)
    }
  }

  const enviarVendedor = async () => {
    setErrorGeneral(null)

    const nuevosErrores = validarCamposComunes(
      datos,
      OBLIGATORIOS_VENDEDOR,
    )

    if (!datos.categoriaPrincipalId) {
      nuevosErrores.categoriaPrincipalId = 'Elige una categoría'
    }

    setErrores(nuevosErrores)

    if (Object.keys(nuevosErrores).length > 0) return

    try {
      await registrarVendedor(
        sinTelefonoVacio({
          ...datos,
          avatar: datos.avatar || 'avatar1',
        }),
      )

      setRegistrado(true)
    } catch (error) {
      mostrarErroresDelServidor(error)
    }
  }

  const pantalla = (
    titulo,
    contenido,
    subtitulo = 'Crea tu cuenta y empieza a disfrutar de ESIBuy.',
  ) => (
    <main className={styles.page}>
      <Card className={styles.card}>
        <Link to={PATHS.HOME} className={styles.brand} aria-label="ESIBuy, volver al inicio">
          ESI<span className={styles.logoAccent}>Buy</span>
        </Link>

        <div className={styles.heading}>
          <Title2 as="h1" className={styles.title}>
            {titulo}
          </Title2>

          <Text className={styles.description}>
            {subtitulo}
          </Text>
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
          Tu cuenta se ha creado correctamente y está pendiente de que
          un administrador la verifique. Podrás iniciar sesión cuando
          esté activada.
        </Text>

        <LinkButton to={PATHS.LOGIN} appearance="primary">
          Ir a iniciar sesión
        </LinkButton>
      </>,
    )
  }

  const campo = (
    nombre,
    label,
    { type, required = true, fullWidth = false } = {},
  ) => (
    <div
      className={fullWidth ? `${styles.field} ${styles.fullWidth}` : styles.field}
      data-register-error={
        errores[nombre] ? 'true' : undefined
      }
    >
      <Field
        label={label}
        required={required}
        validationMessage={errores[nombre]}
      >
        <Input
          size="large"
          type={type}
          maxLength={LONGITUD_MAXIMA[nombre]}
          value={datos[nombre]}
          onChange={cambiar(nombre)}
        />
      </Field>
    </div>
  )

  const campoPassword = (nombre, label) => (
    <div
      className={styles.field}
      data-register-error={
        errores[nombre] ? 'true' : undefined
      }
    >
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
    <div
      className={styles.privacidadContenedor}
      data-register-error={
        errores.aceptaPrivacidad ? 'true' : undefined
      }
    >
      <Field validationMessage={errores.aceptaPrivacidad}>
        <div className={styles.privacidad}>
          <Checkbox
            required
            aria-label="Acepto la política de privacidad"
            checked={datos.aceptaPrivacidad}
            onChange={(_event, data) =>
              setDatos((actuales) => ({
                ...actuales,
                aceptaPrivacidad: data.checked,
              }))
            }
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
    <Dialog
      open={politicaAbierta}
      onOpenChange={(_event, data) =>
        setPoliticaAbierta(data.open)
      }
    >
      <DialogSurface>
        <DialogBody>
          <DialogTitle>Política de privacidad</DialogTitle>

          <DialogContent>
            <div className={styles.contenidoPolitica}>
              <Text
                className={styles.avisoPolitica}
                size={200}
              >
                Texto informativo de ejemplo para el proyecto
                académico ESIBuy. No constituye una política
                jurídica oficial y deberá revisarse y completarse
                antes de utilizar la aplicación en un entorno real.
              </Text>

              <section className={styles.seccionPolitica}>
                <Text weight="semibold" as="h3">
                  1. Datos que se recogen
                </Text>

                <Text className={styles.parrafoPolitica}>
                  Durante el registro se solicitan datos de
                  identificación y contacto, como nombre, apellidos
                  y correo electrónico. Según el tipo de cuenta,
                  también pueden solicitarse otros datos necesarios
                  para crear y gestionar el perfil.
                </Text>
              </section>

              <section className={styles.seccionPolitica}>
                <Text weight="semibold" as="h3">
                  2. Finalidad del tratamiento
                </Text>

                <Text className={styles.parrafoPolitica}>
                  Los datos se utilizan en el contexto de este
                  proyecto para gestionar el registro, identificar
                  a la persona usuaria, administrar su cuenta y
                  permitir el uso de las funcionalidades de ESIBuy
                  correspondientes a su perfil.
                </Text>
              </section>

              <section className={styles.seccionPolitica}>
                <Text weight="semibold" as="h3">
                  3. Conservación y protección
                </Text>

                <Text className={styles.parrafoPolitica}>
                  Los datos deben conservarse únicamente durante el
                  tiempo necesario para las finalidades indicadas
                  y tratarse conforme a las medidas de seguridad y
                  obligaciones aplicables. Los plazos y medidas
                  concretos quedan pendientes de definir en la
                  política oficial.
                </Text>
              </section>

              <section className={styles.seccionPolitica}>
                <Text weight="semibold" as="h3">
                  4. Derechos y contacto
                </Text>

                <Text className={styles.parrafoPolitica}>
                  La política definitiva deberá identificar al
                  responsable del tratamiento, facilitar sus datos
                  de contacto e indicar cómo ejercer los derechos
                  reconocidos por la normativa de protección de
                  datos. Esta información todavía no está
                  concretada en esta versión de ejemplo.
                </Text>
              </section>
            </div>
          </DialogContent>

          <DialogActions>
            <Button
              appearance="primary"
              onClick={() => setPoliticaAbierta(false)}
            >
              Cerrar
            </Button>
          </DialogActions>
        </DialogBody>
      </DialogSurface>
    </Dialog>
  )

  const selectorAvatar = (
    <AvatarSelector
      value={datos.avatar}
      onChange={cambiar('avatar')}
    />
  )

  const selectorTipoCliente = (
    <div className={styles.tipoCuenta}>
      <Text weight="semibold">Tipo de cuenta</Text>

      <fieldset className={styles.grupoOpciones}>
        <legend className={styles.leyendaOpciones}>
          Tipo de cuenta
        </legend>

        <div className={styles.tipoCuentaOpciones}>
          <Button
            type="button"
            appearance="outline"
            aria-pressed={datos.tipoCliente === 'ESTANDAR'}
            className={styles.tipoCuentaCard}
            onClick={() =>
              setDatos((actuales) => ({
                ...actuales,
                tipoCliente: 'ESTANDAR',
              }))
            }
          >
            <Person24Regular className={styles.tipoCuentaIcono} />

            <span className={styles.tipoCuentaTexto}>
              <span className={styles.tipoCuentaNombre}>
                Estándar
              </span>

              <span className={styles.tipoCuentaDetalle}>
                Compra y vende en el marketplace y gestiona tus
                pedidos.
              </span>
            </span>
          </Button>

          <Button
            type="button"
            appearance="outline"
            aria-pressed={datos.tipoCliente === 'PREMIUM'}
            className={styles.tipoCuentaCard}
            onClick={() =>
              setDatos((actuales) => ({
                ...actuales,
                tipoCliente: 'PREMIUM',
              }))
            }
          >
            <Crown24Regular className={styles.tipoCuentaIcono} />

            <span className={styles.tipoCuentaTexto}>
              <span className={styles.tipoCuentaNombre}>
                Premium
              </span>

              <span className={styles.tipoCuentaDetalle}>
                Accede a las ventajas Premium disponibles en
                ESIBuy.
              </span>
            </span>
          </Button>
        </div>
      </fieldset>

      {datos.tipoCliente === 'PREMIUM' && (
        <Text
          size={200}
          className={styles.description}
        >
          Antes de crear la cuenta pasarás por una pasarela de
          pago simulada.
        </Text>
      )}
    </div>
  )

  const camposIdentidad = (
    <div className={styles.fieldsGrid}>
      {campo('nombre', 'Nombre')}
      {campo('apellidos', 'Apellidos')}

      {tipo === 'cliente'
        ? campo('fechaNacimiento', 'Fecha de nacimiento', {
          type: 'date',
        })
        : campo('nombreComercial', 'Nombre comercial')}

      {tipo === 'cliente' ? (
        campo('dni', 'DNI')
      ) : (
        <div
          className={styles.field}
          data-register-error={
            errores.categoriaPrincipalId
              ? 'true'
              : undefined
          }
        >
          <Field
            label="Categoría principal"
            required
            validationState={
              errores.categoriaPrincipalId ? 'error' : 'none'
            }
            validationMessage={errores.categoriaPrincipalId}
          >
            <CategoryCombobox
              value={datos.categoriaPrincipalId}
              categories={categoriasDisponibles}
              searchable={false}
              size="large"
              placeholder="Selecciona una categoría"
              onChange={cambiarCategoria}
            />
          </Field>
        </div>
      )}

      {tipo === 'vendedor' && campo('dni', 'DNI')}

      {tipo === 'vendedor' && campo('telefono', 'Teléfono', {
        type: 'tel',
        required: false,
      })}

      {campo('email', 'Correo electrónico', {
        type: 'email',
        fullWidth: tipo === 'vendedor',
      })}

      {tipo === 'cliente' && campo('telefono', 'Teléfono', {
        type: 'tel',
        required: false,
      })}
    </div>
  )

  const camposSeguridad = (
    <div className={styles.passwordFields}>
      <div className={styles.passwordColumn}>
        {campoPassword('password', 'Contraseña')}

        <div className={styles.passwordRequirements}>
          <PasswordRequirements
            password={datos.password}
            vertical
          />
        </div>
      </div>

      {campoPassword(
        'repeticionPassword',
        'Repetir contraseña',
      )}
    </div>
  )

  const contenidoRegistro = (esVendedor) => (
    <>
      <div className={styles.tipoCuenta}>
        <Text weight="semibold">
          Quiero registrarme como
        </Text>

        <fieldset className={styles.grupoOpciones}>
          <legend className={styles.leyendaOpciones}>
            Tipo de registro
          </legend>

          <div className={styles.tipoCuentaOpciones}>
            <Button
              type="button"
              appearance="outline"
              aria-pressed={!esVendedor}
              className={styles.tipoCuentaCard}
              onClick={() => setTipo('cliente')}
            >
              <Cart24Regular
                className={styles.tipoCuentaIcono}
              />

              <span className={styles.tipoCuentaTexto}>
                <span className={styles.tipoCuentaNombre}>
                  Cliente
                </span>

                <span className={styles.tipoCuentaDetalle}>
                  Descubre productos y gestiona tus compras.
                </span>
              </span>
            </Button>

            <Button
              type="button"
              appearance="outline"
              aria-pressed={esVendedor}
              className={styles.tipoCuentaCard}
              onClick={() => setTipo('vendedor')}
            >
              <BuildingShop24Regular
                className={styles.tipoCuentaIcono}
              />

              <span className={styles.tipoCuentaTexto}>
                <span className={styles.tipoCuentaNombre}>
                  Vendedor
                </span>

                <span className={styles.tipoCuentaDetalle}>
                  Publica productos y gestiona tu tienda.
                </span>
              </span>
            </Button>
          </div>
        </fieldset>
      </div>

      {camposIdentidad}

      {!esVendedor && selectorTipoCliente}

      {camposSeguridad}

      <div className={styles.avatarSection}>
        {selectorAvatar}
      </div>

      {casillaPrivacidad}

      {dialogoPoliticaPrivacidad}

      {errorGeneral && (
        <div data-register-error-message="true">
          <MessageBar intent="error">
            <MessageBarBody>
              {errorGeneral}
            </MessageBarBody>
          </MessageBar>
        </div>
      )}

      <div className={styles.zonaInferior}>
        <Button
          appearance="primary"
          size="medium"
          className={styles.submit}
          onClick={
            esVendedor ? enviarVendedor : enviarCliente
          }
        >
          Registrarse
        </Button>

        <div className={styles.loginFooter}>
          ¿Ya tienes una cuenta?

          <LinkButton
            to={PATHS.LOGIN}
            appearance="transparent"
            className={styles.loginLink}
          >
            Iniciar sesión
          </LinkButton>
        </div>
      </div>
    </>
  )

  return pantalla(
    tipo === 'vendedor'
      ? 'Registro de vendedor'
      : 'Registro de cliente',
    contenidoRegistro(tipo === 'vendedor'),
    tipo === 'vendedor'
      ? 'Crea tu cuenta de vendedor y empieza a publicar tus productos en ESIBuy.'
      : 'Crea tu cuenta y empieza a disfrutar de ESIBuy.',
  )
}
