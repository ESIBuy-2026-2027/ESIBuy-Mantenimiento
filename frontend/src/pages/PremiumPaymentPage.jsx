import { useState } from 'react'
import { Button, MessageBar, MessageBarActions, MessageBarBody, Spinner } from '@fluentui/react-components'
import { Navigate, useNavigate } from 'react-router-dom'
import { ApiError } from '../api/client'
import { becomePremium, getProfile } from '../api/profile'
import { usePageStyles } from '../components/pageStyles'
import { useCart } from '../features/cart/useCart'
import PremiumCheckout from '../features/premium/PremiumCheckout'
import { AVISOS_PREMIUM } from '../features/premium/premiumNotices'
import { useApiQuery } from '../hooks/useApiQuery'
import { PATHS } from '../routes/paths'

const ERROR_PERMISO = 'No tienes permiso para realizar esta operación.'
const ERROR_GENERICO = 'No se ha podido completar el pago. Inténtalo de nuevo más tarde.'

const esYaPremium = (error) => error instanceof ApiError && error.status === 400 && error.code === 'ALREADY_PREMIUM'

// Paso a ESIBuy Premium del cliente Estándar (HU45): la misma pasarela simulada que el registro Premium. Al simular el
// pago se llama a POST /api/perfil/premium y se vuelve a la vista Premium, que lo confirma. Un cliente que ya es
// Premium no tiene nada que pagar: vuelve a la vista Premium. Nunca se muestra el texto que envía el backend.
export default function PremiumPaymentPage() {
  const pageStyles = usePageStyles()
  const navigate = useNavigate()
  const { reload: reloadCart } = useCart()
  const { data: perfil, failed, retry } = useApiQuery(getProfile)
  const [error, setError] = useState(null)

  if (failed) {
    return (
      <main className={pageStyles.page}>
        <MessageBar intent="error">
          <MessageBarBody>No se ha podido cargar tu perfil. Inténtalo de nuevo.</MessageBarBody>
          <MessageBarActions>
            <Button onClick={retry}>Reintentar</Button>
          </MessageBarActions>
        </MessageBar>
      </main>
    )
  }

  if (!perfil) {
    return <Spinner className={pageStyles.loading} label="Cargando…" />
  }

  if (perfil.tipoCliente === 'PREMIUM') {
    return <Navigate to={PATHS.PREMIUM} replace />
  }

  const volverAPremium = (aviso) => navigate(PATHS.PREMIUM, { replace: true, state: aviso ? { aviso } : null })

  const pay = async () => {
    setError(null)
    try {
      await becomePremium()
      // Los precios del carrito dependen del tipo de cliente: se piden de nuevo para que reflejen el descuento Premium
      reloadCart()
      volverAPremium(AVISOS_PREMIUM.ACTIVADO)
    } catch (fallo) {
      if (esYaPremium(fallo)) {
        volverAPremium(AVISOS_PREMIUM.YA_ERA_PREMIUM)
        return
      }
      // Con la sesión caducada (401), AuthProvider la cierra y la ruta protegida lleva al login, que lo avisa (HU43)
      if (fallo instanceof ApiError && fallo.status === 401) return
      setError(fallo instanceof ApiError && fallo.status === 403 ? ERROR_PERMISO : ERROR_GENERICO)
    }
  }

  return (
    <PremiumCheckout
      description="Al simular el pago tu cuenta pasará a ser ESIBuy Premium."
      cancelLabel="Volver"
      onPay={pay}
      onCancel={() => volverAPremium(null)}
      error={error}
    />
  )
}
