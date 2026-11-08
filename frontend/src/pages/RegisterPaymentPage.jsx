import { Navigate, useNavigate } from 'react-router-dom'
import { useRegistroPremium } from '../features/account/RegistroPremiumContext'
import PremiumCheckout from '../features/premium/PremiumCheckout'
import { PATHS } from '../routes/paths'

// Pasarela obligatoria del registro como cliente Premium (HU45). Sin datos del formulario en memoria (se ha llegado
// escribiendo la dirección o se ha recargado la página) no hay nada que pagar: vuelve al registro, vacío. Tanto al
// simular el pago como al cancelar se vuelve al registro sin dejar la pasarela en el historial.
export default function RegisterPaymentPage() {
  const navigate = useNavigate()
  const { pendiente, cancelarPago, pagarYRegistrar } = useRegistroPremium()

  if (!pendiente) {
    return <Navigate to={PATHS.REGISTER} replace />
  }

  const pay = async () => {
    await pagarYRegistrar()
    navigate(PATHS.REGISTER, { replace: true })
  }

  const cancel = () => {
    cancelarPago()
    navigate(PATHS.REGISTER, { replace: true })
  }

  return (
    <PremiumCheckout
      description="Al simular el pago se creará tu cuenta de cliente ESIBuy Premium."
      cancelLabel="Volver al registro"
      onPay={pay}
      onCancel={cancel}
    />
  )
}
