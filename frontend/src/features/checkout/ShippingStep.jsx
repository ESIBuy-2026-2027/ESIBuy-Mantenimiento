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
  mergeClasses,
} from '@fluentui/react-components'
import LinkButton from '../../components/LinkButton'
import { PATHS } from '../../routes/paths'
import { useCheckoutStyles } from './checkoutStyles'

// Las direcciones de envío del cliente (HU5).
function AddressList({ query, selectedId, onSelect }) {
  const styles = useCheckoutStyles()

  if (query.failed) {
    return (
      <MessageBar intent="error">
        <MessageBarBody>No se han podido cargar las direcciones de envío.</MessageBarBody>
        <MessageBarActions>
          <Button onClick={query.retry}>Reintentar</Button>
        </MessageBarActions>
      </MessageBar>
    )
  }

  if (!query.data) return <Spinner size="small" label="Cargando direcciones de envío…" />

  if (query.data.length === 0) {
    return (
      <MessageBar intent="warning">
        <MessageBarBody>No tienes ninguna dirección de envío guardada.</MessageBarBody>
        <MessageBarActions>
          <LinkButton to={PATHS.ADDRESSES}>Añadir una dirección</LinkButton>
        </MessageBarActions>
      </MessageBar>
    )
  }

  return (
    <Field label="Dirección de envío">
      <RadioGroup value={selectedId} onChange={(_, data) => onSelect(data.value)}>
        {query.data.map((address) => (
          <Radio
            key={address.id}
            value={address.id}
            label={
              <span className={styles.addressLabel}>
                <span className={styles.methodLabel}>
                  <span className={styles.lineName}>{address.destinatario}</span>
                  {address.principal && (
                    <Badge appearance="tint" color="brand" size="small">
                      Principal
                    </Badge>
                  )}
                </span>
                <span className={styles.addressLine}>
                  {address.detalles ? `${address.calle}, ${address.detalles}` : address.calle}
                </span>
                <span className={mergeClasses(styles.addressLine, styles.muted)}>
                  {address.codigoPostal} {address.ciudad} ({address.provincia})
                </span>
              </span>
            }
          />
        ))}
      </RadioGroup>
    </Field>
  )
}

AddressList.propTypes = {
  query: PropTypes.shape({
    failed: PropTypes.bool.isRequired,
    retry: PropTypes.func.isRequired,
    data: PropTypes.arrayOf(
      PropTypes.shape({
        id: PropTypes.string.isRequired,
        destinatario: PropTypes.string.isRequired,
        principal: PropTypes.bool.isRequired,
        detalles: PropTypes.string,
        calle: PropTypes.string.isRequired,
        codigoPostal: PropTypes.string.isRequired,
        ciudad: PropTypes.string.isRequired,
        provincia: PropTypes.string.isRequired,
      }),
    ),
  }).isRequired,
  selectedId: PropTypes.string,
  onSelect: PropTypes.func.isRequired,
}

// Paso 2 de la pasarela (HU10): a qué dirección guardada se envía el pedido.
export default function ShippingStep({ addresses, selectedId, canContinue, onSelect, onBack, onContinue }) {
  const styles = useCheckoutStyles()

  return (
    <section aria-label="Envío" className={styles.panel}>
      <h2 className={styles.panelTitle}>¿Dónde lo enviamos?</h2>
      <AddressList query={addresses} selectedId={selectedId} onSelect={onSelect} />
      <div className={styles.actions}>
        <Button appearance="secondary" onClick={onBack}>
          Volver a la compra
        </Button>
        <Button appearance="primary" disabled={!canContinue} onClick={onContinue}>
          Continuar con el pago
        </Button>
      </div>
    </section>
  )
}

ShippingStep.propTypes = {
  addresses: AddressList.propTypes.query,
  selectedId: PropTypes.string,
  canContinue: PropTypes.bool.isRequired,
  onSelect: PropTypes.func.isRequired,
  onBack: PropTypes.func.isRequired,
  onContinue: PropTypes.func.isRequired,
}
