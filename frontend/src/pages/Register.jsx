import {
  Button,
  Card,
  Checkbox,
  Field,
  Input,
  Label,
  Select,
  Text,
  Title2,
  makeStyles,
  tokens,
} from '@fluentui/react-components'
import { BuildingShop24Regular, Cart24Regular } from '@fluentui/react-icons'
import { useEffect, useState } from 'react'
import { registrarCliente, registrarVendedor } from '../api/auth'
import { getCategories } from '../api/products'
import AvatarSelector from '../features/account/AvatarSelector'
import { PasswordField, PasswordRequirements } from '../features/account/PasswordField'
import { esEmailValido, validarPasswords } from '../features/account/accountRules'

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

const EMAIL_YA_REGISTRADO ='Este email ya está registrado'
const NOMBRE_COMERCIAL_YA_REGISTRADO = 'Este nombre comercial ya está registrado'
const ERRORES_VENDEDOR_YA_REGISTRADO = {
  EMAIL_ALREADY_REGISTERED: { email: EMAIL_YA_REGISTRADO },
  COMMERCIAL_NAME_ALREADY_REGISTERED: { nombreComercial: NOMBRE_COMERCIAL_YA_REGISTRADO },
  EMAIL_AND_COMMERCIAL_NAME_ALREADY_REGISTERED: {
    email: EMAIL_YA_REGISTRADO,
    nombreComercial: NOMBRE_COMERCIAL_YA_REGISTRADO,
  },
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
  categorias: {
    display: 'flex',
    flexDirection: 'column',
    gap: tokens.spacingVerticalXS,
    margin: 0,
    padding: 0,
    border: 'none',
  },
  leyenda: {
    padding: 0,
  },
  error: {
    color: tokens.colorPaletteRedForeground1,
    fontSize: tokens.fontSizeBase200,
  },
})

function esMayorDeEdad(fecha) {
  const nacimiento = new Date(fecha)
  const hoy = new Date()
  let edad = hoy.getFullYear() - nacimiento.getFullYear()
  const cumpleEsteAnio =
    hoy.getMonth() > nacimiento.getMonth() ||
    (hoy.getMonth() === nacimiento.getMonth() && hoy.getDate() >= nacimiento.getDate())
  if (!cumpleEsteAnio) edad--
  return edad >= 18
}

function validarCamposComunes(datos, obligatorios) {
  const errores = {}
  obligatorios.forEach((campo) => {
    if (!datos[campo].trim()) errores[campo] = 'Este campo es obligatorio'
  })
  if (datos.dni && !/^\d{8}[A-Za-z]$/.test(datos.dni)) {
    errores.dni = 'Introduce un DNI válido (8 números y una letra)'
  }
  if (datos.email && !esEmailValido(datos.email)) {
    errores.email = 'Introduce un email válido'
  }
  if (datos.telefono && !/^\d{9}$/.test(datos.telefono)) {
    errores.telefono = 'Introduce un teléfono válido (9 números)'
  }
  Object.assign(errores, validarPasswords(datos))
  if (!datos.aceptaPrivacidad) {
    errores.aceptaPrivacidad = 'Debes aceptar la política de privacidad'
  }
  return errores
}

export default function Register() {
  const styles = useStyles()
  const [tipo, setTipo] = useState(null)
  const [datos, setDatos] = useState({
    nombre: '',
    apellidos: '',
    nombreComercial: '',
    categorias: [],
    fechaNacimiento: '',
    dni: '',
    email: '',
    telefono: '',
    password: '',
    repeticionPassword: '',
    tipoCliente: 'normal',
    avatar: '',
    aceptaPrivacidad: false,
  })
  const [errores, setErrores] = useState({})
  const [registrado, setRegistrado] = useState(false)
  const [categoriasDisponibles, setCategoriasDisponibles] = useState([])

  useEffect(() => {
    if (tipo !== 'vendedor') return
    getCategories().then(setCategoriasDisponibles)
  }, [tipo])

  const cambiar = (campo) => (e, data) => setDatos({ ...datos, [campo]: data.value })

  const cambiarCategoria = (id) => (e, data) =>
    setDatos({
      ...datos,
      categorias: data.checked
        ? [...datos.categorias, id]
        : datos.categorias.filter((categoria) => categoria !== id),
    })

  const enviarCliente = async () => {
    const nuevosErrores = validarCamposComunes(datos, OBLIGATORIOS_CLIENTE)
    if (datos.fechaNacimiento && !esMayorDeEdad(datos.fechaNacimiento)) {
      nuevosErrores.fechaNacimiento = 'Debes ser mayor de edad para registrarte'
    }
    setErrores(nuevosErrores)

    if (Object.keys(nuevosErrores).length > 0) return

    try {
      await registrarCliente(datos)
      setRegistrado(true)
    } catch (error) {
      if (error.status === 409) {
        setErrores({ email: EMAIL_YA_REGISTRADO })
      }
    }
  }

  const enviarVendedor = async () => {
    const nuevosErrores = validarCamposComunes(datos, OBLIGATORIOS_VENDEDOR)
    if (datos.categorias.length === 0) {
      nuevosErrores.categorias = 'Elige al menos una categoría'
    }
    setErrores(nuevosErrores)

    if (Object.keys(nuevosErrores).length > 0) return

    try {
      await registrarVendedor(datos)
      setRegistrado(true)
    } catch (error) {
      if (error.status === 409 && ERRORES_VENDEDOR_YA_REGISTRADO[error.code]) {
        setErrores(ERRORES_VENDEDOR_YA_REGISTRADO[error.code])
      }
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
      <Text className={styles.description}>Tu cuenta se ha creado correctamente.</Text>
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
    />
  )

  const camposComunes = (
    <>
      {campo('dni', 'DNI')}
      {campo('email', 'Email', { type: 'email' })}
      {campo('telefono', 'Teléfono', { type: 'tel', required: false })}
      {campoPassword('password', 'Password')}
      <PasswordRequirements password={datos.password} />
      {campoPassword('repeticionPassword', 'Repetir password')}
    </>
  )

  const casillaPrivacidad = (
    <Field validationMessage={errores.aceptaPrivacidad}>
      <Checkbox
        required
        label="Acepto la política de privacidad"
        checked={datos.aceptaPrivacidad}
        onChange={(e, data) => setDatos({ ...datos, aceptaPrivacidad: data.checked })}
      />
    </Field>
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
        <Field label="Tipo de cliente" required>
          <Select value={datos.tipoCliente} onChange={cambiar('tipoCliente')}>
            <option value="normal">Normal</option>
            <option value="premium">Premium</option>
          </Select>
        </Field>
        {selectorAvatar}
        {casillaPrivacidad}
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
        <fieldset className={styles.categorias}>
          <legend className={styles.leyenda}>
            <Label required>Categorías</Label>
          </legend>
          {categoriasDisponibles.map((categoria) => (
            <Checkbox
              key={categoria.id}
              label={categoria.nombre}
              checked={datos.categorias.includes(categoria.id)}
              onChange={cambiarCategoria(categoria.id)}
            />
          ))}
          {errores.categorias && <Text className={styles.error}>{errores.categorias}</Text>}
        </fieldset>
        {camposComunes}
        {selectorAvatar}
        {casillaPrivacidad}
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