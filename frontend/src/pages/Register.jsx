import { useState } from 'react'

export default function Register() {
  const [tipo, setTipo] = useState(null)

  if (tipo === 'cliente') {
    return <h1>Registro de cliente</h1>
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