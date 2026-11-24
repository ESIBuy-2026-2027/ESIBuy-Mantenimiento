import { useState } from 'react'
import PropTypes from 'prop-types'
import { useSearchParams } from 'react-router-dom'
import {
  Button,
  MessageBar,
  MessageBarActions,
  MessageBarBody,
  Spinner,
  Text,
  Title1,
  mergeClasses,
} from '@fluentui/react-components'
import { BuildingShop20Regular, Cart24Regular } from '@fluentui/react-icons'
import { getAddresses } from '../api/addresses'
import { createOrder } from '../api/orders'
import { getPaymentMethods } from '../api/paymentMethods'
import EmptyState from '../components/EmptyState'
import LinkButton from '../components/LinkButton'
import { useCart } from '../features/cart/useCart'
import CartSummaryStep from '../features/checkout/CartSummaryStep'
import PaymentStep from '../features/checkout/PaymentStep'
import PaymentTicket from '../features/checkout/PaymentTicket'
import ShippingStep from '../features/checkout/ShippingStep'
import { mensajeDeError, pideRevisarLaCompra } from '../features/checkout/checkoutErrors'
import { useCheckoutStyles } from '../features/checkout/checkoutStyles'
import { useApiQuery } from '../hooks/useApiQuery'
import { PATHS } from '../routes/paths'
import { formatPrice } from '../utils/format'

// Lo que dura el pago simulado antes de crear el pedido.
const PAGO_SIMULADO_MS = 700

const PASOS = [
  { id: 'resumen', etiqueta: 'Compra' },
  { id: 'envio', etiqueta: 'Envío' },
  { id: 'pago', etiqueta: 'Pago' },
]

const esperar = (ms) => new Promise((resolver) => setTimeout(resolver, ms))

// El carrito puede tener productos de varios vendedores, pero cada pedido es de un solo vendedor: aquí se elige a
// cuál se paga ahora. Lo que queda de los demás sigue en el carrito.
function VendorChoice({ groups }) {
  const styles = useCheckoutStyles()

  return (
    <section aria-label="Elegir vendedor" className={styles.panel}>
      <h2 className={styles.panelTitle}>¿A qué vendedor quieres pagar?</h2>
      <Text className={styles.muted}>
        Cada pedido es de un solo vendedor. Paga uno ahora; lo de los demás se queda en tu carrito.
      </Text>
      <ul className={styles.vendorList}>
        {groups.map((group) => (
          <li key={group.vendedor.id} className={styles.vendorItem}>
            <span className={styles.vendor}>
              <BuildingShop20Regular aria-hidden="true" />
              <span>
                {group.vendedor.nombreComercial}: {formatPrice(group.total)}
              </span>
            </span>
            <LinkButton
              to={`${PATHS.CHECKOUT}?vendedor=${encodeURIComponent(group.vendedor.id)}`}
              appearance="primary"
            >
              {`Pagar a ${group.vendedor.nombreComercial}`}
            </LinkButton>
          </li>
        ))}
      </ul>
    </section>
  )
}

VendorChoice.propTypes = {
  groups: PropTypes.arrayOf(
    PropTypes.shape({
      vendedor: PropTypes.shape({
        id: PropTypes.string.isRequired,
        nombreComercial: PropTypes.string.isRequired,
      }).isRequired,
      total: PropTypes.number.isRequired,
    }),
  ).isRequired,
}

// El vendedor del pedido: el que pide el enlace del carrito o, si el carrito solo tiene uno, ese.
function elegirGrupo(groups, vendorId) {
  if (vendorId) return groups.find(({ vendedor }) => vendedor.id === vendorId)
  return groups.length === 1 ? groups[0] : undefined
}

// Pasarela de pago (HU10): un pedido de un solo vendedor, en pasos: revisar la compra, elegir el envío, pagar (simulado) y
// el recibo.
export default function CheckoutPage() {
  const styles = useCheckoutStyles()
  const { cart, status, reload } = useCart()
  const [params] = useSearchParams()
  const addresses = useApiQuery(getAddresses)
  const paymentMethods = useApiQuery(getPaymentMethods)
  const [step, setStep] = useState('resumen')
  const [selectedAddressId, setSelectedAddressId] = useState(null)
  const [selectedMethodId, setSelectedMethodId] = useState(null)
  const [paying, setPaying] = useState(false)
  const [failure, setFailure] = useState(null)
  const [receipt, setReceipt] = useState(null)

  const groups = cart.grupos
  const group = elegirGrupo(groups, params.get('vendedor'))

  // Mientras el cliente no elige, se usan su dirección principal y su método predeterminado.
  const addressList = addresses.failed ? [] : (addresses.data ?? [])
  const selectedAddress =
    addressList.find(({ id }) => id === selectedAddressId) ??
    addressList.find(({ principal }) => principal) ??
    addressList[0]

  const methods = paymentMethods.failed ? [] : (paymentMethods.data ?? [])
  const selectedMethod =
    methods.find(({ id }) => id === selectedMethodId) ??
    methods.find(({ predeterminado }) => predeterminado) ??
    methods[0]

  const pay = async () => {
    setPaying(true)
    setFailure(null)

    await esperar(PAGO_SIMULADO_MS)

    try {
      const order = await createOrder(group.vendedor.id, selectedAddress.id, selectedMethod.id, group.total)
      setReceipt({ order, method: selectedMethod, paidAt: new Date() })
      setStep('hecho')
    } catch (error) {
      setFailure(error)
      // Si lo que ha cambiado es la compra, el cliente tiene que volver a verla antes de pagar.
      if (pideRevisarLaCompra(error)) setStep('resumen')
    } finally {
      reload()
      setPaying(false)
    }
  }

  if (step === 'hecho') {
    return (
      <main className={styles.page}>
        <Title1 as="h1" className={styles.title}>
          Pago realizado
        </Title1>

        <div className={styles.done}>
          <PaymentTicket orders={[receipt.order]} paymentMethod={receipt.method} paidAt={receipt.paidAt} />

          <div className={styles.actions}>
            <LinkButton to={PATHS.PRODUCTS} appearance="secondary">
              Seguir comprando
            </LinkButton>
            {groups.length > 0 && (
              <LinkButton to={PATHS.CHECKOUT} appearance="secondary">
                Pagar a otro vendedor
              </LinkButton>
            )}
            <LinkButton to={PATHS.ORDERS} appearance="primary">
              Ver mis pedidos
            </LinkButton>
          </div>
        </div>
      </main>
    )
  }

  const empty = status === 'ready' && groups.length === 0
  const needsChoice = status === 'ready' && groups.length > 0 && !group

  return (
    <main className={styles.page}>
      <div className={styles.heading}>
        <Title1 as="h1" className={styles.title}>
          Pasarela de pago
        </Title1>
        <Text className={styles.subtitle}>Un pedido es de un solo vendedor. Revisa tu compra antes de pagar.</Text>
      </div>

      {status === 'loading' && <Spinner label="Cargando carrito…" />}

      {status === 'failed' && (
        <MessageBar intent="error">
          <MessageBarBody>No se ha podido cargar el carrito.</MessageBarBody>
          <MessageBarActions>
            <Button onClick={reload}>Reintentar</Button>
          </MessageBarActions>
        </MessageBar>
      )}

      {empty && (
        <EmptyState
          icon={<Cart24Regular />}
          title="Tu carrito está vacío"
          description="Añade productos para poder pagar."
        >
          <LinkButton to={PATHS.PRODUCTS} appearance="primary">
            Ver los productos
          </LinkButton>
        </EmptyState>
      )}

      {needsChoice && <VendorChoice groups={groups} />}

      {group && (
        <>
          <ol aria-label="Pasos de la compra" className={styles.steps}>
            {PASOS.map(({ id, etiqueta }, index) => (
              <li
                key={id}
                aria-current={id === step ? 'step' : undefined}
                className={mergeClasses(styles.step, id === step && styles.stepCurrent)}
              >
                {`${index + 1}. ${etiqueta}`}
              </li>
            ))}
          </ol>

          {failure && step === 'resumen' && (
            <MessageBar intent="error" role="alert">
              <MessageBarBody>{mensajeDeError(failure)}</MessageBarBody>
            </MessageBar>
          )}

          {step === 'resumen' && (
            <CartSummaryStep
              group={group}
              onContinue={() => {
                setFailure(null)
                setStep('envio')
              }}
            />
          )}

          {step === 'envio' && (
            <ShippingStep
              addresses={addresses}
              selectedId={selectedAddress?.id ?? null}
              canContinue={Boolean(selectedAddress)}
              onSelect={setSelectedAddressId}
              onBack={() => setStep('resumen')}
              onContinue={() => setStep('pago')}
            />
          )}

          {step === 'pago' && (
            <PaymentStep
              group={group}
              address={selectedAddress}
              methods={paymentMethods}
              selectedId={selectedMethod?.id ?? null}
              paying={paying}
              error={failure ? mensajeDeError(failure) : null}
              canPay={Boolean(selectedMethod && selectedAddress)}
              onSelect={setSelectedMethodId}
              onBack={() => setStep('envio')}
              onPay={pay}
            />
          )}
        </>
      )}
    </main>
  )
}
