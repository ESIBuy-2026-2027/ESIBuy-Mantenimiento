import { Button, Field, Input, Radio, RadioGroup, Select } from '@fluentui/react-components'
import { useState } from 'react'
import { registrarCliente } from '../api/auth'

const OBLIGATORIOS = ['nombre', 'apellidos', 'fechaNacimiento', 'dni', 'email', 'password', 'repeticionPassword']

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

export default function Register() {
  const [tipo, setTipo] = useState(null)
  const [datos, setDatos] = useState({
    nombre: '',
    apellidos: '',
    nombreComercial: '',
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

  const cambiar = (campo) => (e, data) => setDatos({ ...datos, [campo]: data.value })

  const enviar = async () => {
    const nuevosErrores = {}
    OBLIGATORIOS.forEach((campo) => {
      if (!datos[campo].trim()) nuevosErrores[campo] = 'Este campo es obligatorio'
    })
    if (datos.fechaNacimiento && !esMayorDeEdad(datos.fechaNacimiento)) {
      nuevosErrores.fechaNacimiento = 'Debes ser mayor de edad para registrarte'
    }
    if (datos.dni && !/^\d{8}[A-Za-z]$/.test(datos.dni)) {
      nuevosErrores.dni = 'Introduce un DNI válido (8 números y una letra)'
    }
    if (datos.email && !/^[^\s@]+@[^\s@.]+(?:\.[^\s@.]+)+$/.test(datos.email)) {
      nuevosErrores.email = 'Introduce un email válido'
    }
    if (datos.password && datos.repeticionPassword && datos.password !== datos.repeticionPassword) {
      nuevosErrores.repeticionPassword = 'Las contraseñas no coinciden'
    }
    setErrores(nuevosErrores)

    if (Object.keys(nuevosErrores).length > 0) return

    try {
      await registrarCliente(datos)
      setRegistrado(true)
    } catch (error) {
      if (error.status === 409) {
        setErrores({ email: 'Este email ya está registrado' })
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

  if (tipo === 'cliente') {
    return (
      <div>
        <h1>Registro de cliente</h1>
        <Field label="Nombre" required validationMessage={errores.nombre}>
          <Input value={datos.nombre} onChange={cambiar('nombre')} />
        </Field>
        <Field label="Apellidos" required validationMessage={errores.apellidos}>
          <Input value={datos.apellidos} onChange={cambiar('apellidos')} />
        </Field>
        <Field label="Fecha de nacimiento" required validationMessage={errores.fechaNacimiento}>
          <Input type="date" value={datos.fechaNacimiento} onChange={cambiar('fechaNacimiento')} />
        </Field>
        <Field label="DNI" required validationMessage={errores.dni}>
          <Input value={datos.dni} onChange={cambiar('dni')} />
        </Field>
        <Field label="Email" required validationMessage={errores.email}>
          <Input type="email" value={datos.email} onChange={cambiar('email')} />
        </Field>
        <Field label="Teléfono">
          <Input type="tel" value={datos.telefono} onChange={cambiar('telefono')} />
        </Field>
        <Field label="Password" required validationMessage={errores.password}>
          <Input type="password" value={datos.password} onChange={cambiar('password')} />
        </Field>
        <Field label="Repetir password" required validationMessage={errores.repeticionPassword}>
          <Input
            type="password"
            value={datos.repeticionPassword}
            onChange={cambiar('repeticionPassword')}
          />
        </Field>
        <Field label="Tipo de cliente" required>
          <Select value={datos.tipoCliente} onChange={cambiar('tipoCliente')}>
            <option value="normal">Normal</option>
            <option value="premium">Premium</option>
          </Select>
        </Field>
        <Field label="Imagen de perfil">
          <RadioGroup layout="horizontal" value={datos.avatar} onChange={cambiar('avatar')}>
            <Radio value="avatar1" label="Avatar 1" />
            <Radio value="avatar2" label="Avatar 2" />
            <Radio value="avatar3" label="Avatar 3" />
          </RadioGroup>
        </Field>
        <Button appearance="primary" onClick={enviar}>Registrarse</Button>
      </div>
    )
  }

  if (tipo === 'vendedor') {
    return (
      <div>
        <h1>Registro de vendedor</h1>
        <Field label="Nombre" required>
          <Input value={datos.nombre} onChange={cambiar('nombre')} />
        </Field>
        <Field label="Apellidos" required>
          <Input value={datos.apellidos} onChange={cambiar('apellidos')} />
        </Field>
        <Field label="Nombre comercial" required>
          <Input value={datos.nombreComercial} onChange={cambiar('nombreComercial')} />
        </Field>
        <Field label="DNI" required>
          <Input value={datos.dni} onChange={cambiar('dni')} />
        </Field>
        <Field label="Email" required>
          <Input type="email" value={datos.email} onChange={cambiar('email')} />
        </Field>
        <Field label="Teléfono">
          <Input type="tel" value={datos.telefono} onChange={cambiar('telefono')} />
        </Field>
        <Field label="Password" required>
          <Input type="password" value={datos.password} onChange={cambiar('password')} />
        </Field>
        <Field label="Repetir password" required>
          <Input
            type="password"
            value={datos.repeticionPassword}
            onChange={cambiar('repeticionPassword')}
          />
        </Field>
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