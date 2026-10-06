import { Field, Input } from '@fluentui/react-components'
import { useState } from 'react'

export default function Register() {
  const [tipo, setTipo] = useState(null)

  if (tipo === 'cliente') {
    return (
      <div>
        <h1>Registro de cliente</h1>
        <Field label="Nombre"><Input /></Field>
        <Field label="Apellidos"><Input /></Field>
        <Field label="Fecha de nacimiento"><Input type="date" /></Field>
        <Field label="DNI"><Input /></Field>
        <Field label="Email"><Input type="email" /></Field>
        <Field label="Teléfono"><Input type="tel" /></Field>
        <Field label="Contraseña"><Input type="password" /></Field>
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