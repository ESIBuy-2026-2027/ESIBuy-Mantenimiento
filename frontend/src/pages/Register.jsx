import {
  Button,
  Checkbox,
  Field,
  Input,
  Label,
  Radio,
  RadioGroup,
  Select,
  Text,
} from '@fluentui/react-components'
import { useEffect, useState } from 'react'
import { registrarCliente, registrarVendedor } from '../api/auth'
import { getCategorias } from '../api/categorias'

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

const EMAIL_YA_REGISTRADO = 'Este email ya está registrado'
const NOMBRE_COMERCIAL_YA_REGISTRADO = 'Este nombre comercial ya está registrado'
const ERRORES_VENDEDOR_YA_REGISTRADO = {
  EMAIL_ALREADY_REGISTERED: { email: EMAIL_YA_REGISTRADO },
  COMMERCIAL_NAME_ALREADY_REGISTERED: { nombreComercial: NOMBRE_COMERCIAL_YA_REGISTRADO },
  EMAIL_AND_COMMERCIAL_NAME_ALREADY_REGISTERED: {
    email: EMAIL_YA_REGISTRADO,
    nombreComercial: NOMBRE_COMERCIAL_YA_REGISTRADO,
  },
}

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
  if (datos.email && !/^[^\s@]+@[^\s@.]+(?:\.[^\s@.]+)+$/.test(datos.email)) {
    errores.email = 'Introduce un email válido'
  }
  if (datos.password && datos.repeticionPassword && datos.password !== datos.repeticionPassword) {
    errores.repeticionPassword = 'Las contraseñas no coinciden'
  }
  return errores
}

export default function Register() {
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
  })
  const [errores, setErrores] = useState({})
  const [registrado, setRegistrado] = useState(false)
  const [categoriasDisponibles, setCategoriasDisponibles] = useState([])

  useEffect(() => {
    if (tipo !== 'vendedor') return
    getCategorias().then(setCategoriasDisponibles)
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

  if (registrado) {
    return (
      <div>
        <h1>Cuenta creada</h1>
        <p>Tu cuenta se ha creado correctamente.</p>
      </div>
    )
  }

  const campo = (nombre, label, { type, required = true } = {}) => (
    <Field label={label} required={required} validationMessage={errores[nombre]}>
      <Input type={type} value={datos[nombre]} onChange={cambiar(nombre)} />
    </Field>
  )

  const camposComunes = (
    <>
      {campo('dni', 'DNI')}
      {campo('email', 'Email', { type: 'email' })}
      {campo('telefono', 'Teléfono', { type: 'tel', required: false })}
      {campo('password', 'Password', { type: 'password' })}
      {campo('repeticionPassword', 'Repetir password', { type: 'password' })}
    </>
  )

  const selectorAvatar = (
    <Field label="Imagen de perfil">
      <RadioGroup layout="horizontal" value={datos.avatar} onChange={cambiar('avatar')}>
        <Radio value="avatar1" label="Avatar 1" />
        <Radio value="avatar2" label="Avatar 2" />
        <Radio value="avatar3" label="Avatar 3" />
      </RadioGroup>
    </Field>
  )

  if (tipo === 'cliente') {
    return (
      <div>
        <h1>Registro de cliente</h1>
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
        <Button appearance="primary" onClick={enviarCliente}>Registrarse</Button>
      </div>
    )
  }

  if (tipo === 'vendedor') {
    return (
      <div>
        <h1>Registro de vendedor</h1>
        {campo('nombre', 'Nombre')}
        {campo('apellidos', 'Apellidos')}
        {campo('nombreComercial', 'Nombre comercial')}
        <fieldset>
          <legend>
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
          {errores.categorias && <Text>{errores.categorias}</Text>}
        </fieldset>
        {camposComunes}
        {selectorAvatar}
        <Button appearance="primary" onClick={enviarVendedor}>Registrarse</Button>
      </div>
    )
  }

  return (
    <div>
      <h1>Crear cuenta</h1>
      <button onClick={() => setTipo('cliente')}>Cliente</button>
      <button onClick={() => setTipo('vendedor')}>Vendedor</button>
    </div>
  )
}