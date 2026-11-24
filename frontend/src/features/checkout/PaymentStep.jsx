import { useState } from 'react'
import PropTypes from 'prop-types'
import {
  Badge,
  Button,
  Field,
  MessageBar,
  MessageBarActions,
  MessageBarBody,
  Radio,
  RadioGroup,
  Spinner,
  Text,
} from '@fluentui/react-components'
import { Add20Regular, LockClosed20Regular } from '@fluentui/react-icons'
import { formatPrice } from '../../utils/format'
import AddPaymentMethodPanel from '../paymentMethods/AddPaymentMethodPanel'
import { TIPOS_DE_METODO } from '../paymentMethods/tiposDeMetodo'
import { useCheckoutStyles } from './checkoutStyles'

// Métodos de pago guardados del cliente (HU6).
function MethodList({ query, selectedId, onSelect, onAdd }) {
  const styles = useCheckoutStyles()

  if (query.failed) {
    return (
      <MessageBar intent="error">
        <MessageBarBody>No se han podido cargar los métodos de pago.</MessageBarBody>
        <MessageBarActions>
          <Button onClick={query.retry}>Reintentar</Button>
        </MessageBarActions>
      </MessageBar>
    )
  }

  if (!query.data) return <Spinner size="small" label="Cargando métodos de pago…" />

  if (query.data.length === 0) {
    return (
      <MessageBar intent="warning">
        <MessageBarBody>No tienes ningún método de pago guardado.</MessageBarBody>
        <MessageBarActions>
          <Button onClick={onAdd}>Añadir un método de pago</Button>
        </MessageBarActions>
      </MessageBar>
    )
  }

  return (
    <Field label="Método de pago">
      <RadioGroup value={selectedId} onChange={(_, data) => onSelect(data.value)}>
        {query.data.map((method) => {
          const { etiqueta, Icono } = TIPOS_DE_METODO[method.tipo]
          return (
            <Radio
              key={method.id}
              value={method.id}
              label={
                <span className={styles.methodLabel}>
                  <span className={styles.methodIcon} aria-hidden="true">
                    <Icono />
                  </span>
                  <span>{etiqueta}</span>
                  <span className={styles.mask}>{method.mascara}</span>
                  {method.predeterminado && (
                    <Badge appearance="tint" color="brand" size="small">
                      Predeterminado
                    </Badge>
                  )}
                </span>
              }
            />
          )
        })}
      </RadioGroup>
    </Field>
  )
}

MethodList.propTypes = {
  query: PropTypes.shape({
    failed: PropTypes.bool.isRequired,
    retry: PropTypes.func.isRequired,
    data: PropTypes.arrayOf(
      PropTypes.shape({
        id: PropTypes.string.isRequired,
        tipo: PropTypes.string.isRequired,
        mascara: PropTypes.string.isRequired,
        predeterminado: PropTypes.bool.isRequired,
      }),
    ),
  }).isRequired,
  selectedId: PropTypes.string,
  onSelect: PropTypes.func.isRequired,
  onAdd: PropTypes.func.isRequired,
}

// Paso 3 de la pasarela (HU10): con qué método guardado se paga, o uno nuevo que se añade aquí mismo (HU6), y el pago,
// que es simulado: no se cobra nada.
export default function PaymentStep({ group, address, methods, selectedId, paying, error, canPay, onSelect, onBack, onPay }) {
  const styles = useCheckoutStyles()
  const [adding, setAdding] = useState(false)
  const [added, setAdded] = useState(false)

  const openForm = () => {
    setAdded(false)
    setAdding(true)
  }

  // El método nuevo queda guardado en su cuenta y elegido para este pago
  const handleSaved = (created) => {
    setAdding(false)
    setAdded(true)
    methods.retry()
    onSelect(created.id)
  }

  return (
    <section aria-label="Pago" className={styles.panel}>
      <h2 className={styles.panelTitle}>Elige cómo pagar</h2>

      <div className={styles.review}>
        <Text>Vendedor: {group.vendedor.nombreComercial}</Text>
        {address && (
          <Text size={200} className={styles.muted}>
            Envío a {address.destinatario}, {address.calle}, {address.codigoPostal} {address.ciudad}
          </Text>
        )}
      </div>

      {adding ? (
        <AddPaymentMethodPanel onSaved={handleSaved} onCancel={() => setAdding(false)} />
      ) : (
        <>
          {added && (
            <MessageBar intent="success">
              <MessageBarBody>Método de pago añadido y elegido para este pedido.</MessageBarBody>
            </MessageBar>
          )}
          <MethodList query={methods} selectedId={selectedId} onSelect={onSelect} onAdd={openForm} />
          {methods.data?.length > 0 && (
            <div>
              <Button appearance="subtle" icon={<Add20Regular />} onClick={openForm}>
                Añadir un método de pago nuevo
              </Button>
            </div>
          )}
        </>
      )}

      {error && (
        <MessageBar intent="error" role="alert">
          <MessageBarBody>{error}</MessageBarBody>
        </MessageBar>
      )}

      <div className={styles.total}>
        <Text className={styles.muted}>Total a pagar</Text>
        <span className={styles.totalAmount}>{formatPrice(group.total)}</span>
      </div>

      <div className={styles.actions}>
        <Button appearance="secondary" disabled={paying} onClick={onBack}>
          Volver al envío
        </Button>
        <Button
          appearance="primary"
          size="large"
          icon={paying ? <Spinner size="tiny" /> : <LockClosed20Regular />}
          disabled={paying || adding || !canPay}
          onClick={onPay}
        >
          {paying ? 'Procesando pago…' : 'Simular pago'}
        </Button>
      </div>

      <Text size={200} className={styles.muted}>
        Es una simulación: no se realiza ningún cobro real.
      </Text>
    </section>
  )
}

PaymentStep.propTypes = {
  group: PropTypes.shape({
    vendedor: PropTypes.shape({ nombreComercial: PropTypes.string.isRequired }).isRequired,
    total: PropTypes.number.isRequired,
  }).isRequired,
  address: PropTypes.shape({
    destinatario: PropTypes.string.isRequired,
    calle: PropTypes.string.isRequired,
    codigoPostal: PropTypes.string.isRequired,
    ciudad: PropTypes.string.isRequired,
  }),
  methods: MethodList.propTypes.query,
  selectedId: PropTypes.string,
  paying: PropTypes.bool.isRequired,
  error: PropTypes.string,
  canPay: PropTypes.bool.isRequired,
  onSelect: PropTypes.func.isRequired,
  onBack: PropTypes.func.isRequired,
  onPay: PropTypes.func.isRequired,
}
