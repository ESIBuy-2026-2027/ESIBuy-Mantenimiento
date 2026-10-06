import { Button, Field, Input } from '@fluentui/react-components'
import { useState } from 'react'

const OBLIGATORIOS = ['nombre', 'apellidos', 'fechaNacimiento', 'dni', 'email', 'password']

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
    fechaNacimiento: '',
    dni: '',
    email: '',
    telefono: '',
    password: '',
  })
  const [errores, setErrores] = useState({})

  const cambiar = (campo) => (e, data) => setDatos({ ...datos, [campo]: data.value })

  const enviar = () => {
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
    if (datos.email && !/^\S+@\S+\.\S+$/.test(datos.email)) {
      nuevosErrores.email = 'Introduce un email válido'
    }
    setErrores(nuevosErrores)
  }

  if (tipo === 'cliente') {
    return (
      <div>
        <h1>Registro de cliente</h1>
        <Field label="Nombre" validationMessage={errores.nombre}>
          <Input value={datos.nombre} onChange={cambiar('nombre')} />
        </Field>
        <Field label="Apellidos" validationMessage={errores.apellidos}>
          <Input value={datos.apellidos} onChange={cambiar('apellidos')} />
        </Field>
        <Field label="Fecha de nacimiento" validationMessage={errores.fechaNacimiento}>
          <Input type="date" value={datos.fechaNacimiento} onChange={cambiar('fechaNacimiento')} />
        </Field>
        <Field label="DNI" validationMessage={errores.dni}>
          <Input value={datos.dni} onChange={cambiar('dni')} />
        </Field>
        <Field label="Email" validationMessage={errores.email}>
          <Input type="email" value={datos.email} onChange={cambiar('email')} />
        </Field>
        <Field label="Teléfono">
          <Input type="tel" value={datos.telefono} onChange={cambiar('telefono')} />
        </Field>
        <Field label="Contraseña" validationMessage={errores.password}>
          <Input type="password" value={datos.password} onChange={cambiar('password')} />
        </Field>
        <Button appearance="primary" onClick={enviar}>Registrarse</Button>
      </div>
    )
  }

  if (tipo === 'vendedor') {
    return <h1>Registro de vendedor</h1>
  }

  return (
    <div>
      <h1>Crear cuenta</h1>
      <button onClick={() => setTipo('cliente')}>Cliente</button>
      <button onClick={() => setTipo('vendedor')}>Vendedor</button>
    </div>
  )
}