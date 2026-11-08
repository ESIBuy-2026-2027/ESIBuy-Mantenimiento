import { useCallback, useMemo, useState } from 'react'
import { Outlet } from 'react-router-dom'
import { registrarCliente } from '../../api/auth'
import { sinTelefonoVacio } from './accountRules'
import { RegistroPremiumContext } from './RegistroPremiumContext'

// Envuelve el registro y su pasarela (HU45). Mientras el cliente está en estas dos rutas, los datos del formulario
// (contraseña incluida) viven solo en este estado de React: al salir del registro o al recargar se pierden.
//
// El registro como PREMIUM solo se envía desde `pagarYRegistrar`, que llama el botón «Simular pago» de la pasarela.
// El resultado (cuenta creada o el error del servidor) lo recoge después el formulario de registro.
export default function RegistroPremiumFlow() {
  const [pendiente, setPendiente] = useState(null)
  const [resultado, setResultado] = useState(null)

  const iniciarPago = useCallback((datos) => {
    setPendiente(datos)
    setResultado(null)
  }, [])

  const cancelarPago = useCallback(() => setResultado(null), [])

  const pagarYRegistrar = useCallback(async () => {
    if (!pendiente) return
    try {
      await registrarCliente(sinTelefonoVacio({ ...pendiente, tipoCliente: 'PREMIUM' }))
      setPendiente(null)
      setResultado({ registrado: true })
    } catch (error) {
      setResultado({ error })
    }
  }, [pendiente])

  const value = useMemo(
    () => ({ pendiente, resultado, iniciarPago, cancelarPago, pagarYRegistrar }),
    [pendiente, resultado, iniciarPago, cancelarPago, pagarYRegistrar],
  )

  return (
    <RegistroPremiumContext.Provider value={value}>
      <Outlet />
    </RegistroPremiumContext.Provider>
  )
}
