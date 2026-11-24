import { useState } from 'react'
import PropTypes from 'prop-types'
import { Button, Text } from '@fluentui/react-components'
import { BuildingShop20Regular, Delete20Regular } from '@fluentui/react-icons'
import { ApiError } from '../../api/client'
import LinkButton from '../../components/LinkButton'
import { PATHS } from '../../routes/paths'
import { formatPrice } from '../../utils/format'
import PriceBlock from '../catalog/PriceBlock'
import QuantityStepper from '../cart/QuantityStepper'
import { useCart } from '../cart/useCart'
import { useCheckoutStyles } from './checkoutStyles'

const SIN_STOCK = 'No hay existencias suficientes de ese producto.'
const NO_ACTUALIZADO = 'No se ha podido actualizar tu compra. Inténtalo de nuevo.'

// Paso 1 de la pasarela (HU10): la compra de UN vendedor, con las cantidades y los productos editables, el precio de
// cada uno con su descuento (el descuento va en el precio de cada producto, no en el total) y el total a pagar.
export default function CartSummaryStep({ group, onContinue }) {
  const styles = useCheckoutStyles()
  const { setQuantity, remove } = useCart()
  const [busy, setBusy] = useState(false)
  const [error, setError] = useState(null)

  const run = async (operation) => {
    setBusy(true)
    setError(null)
    try {
      await operation()
    } catch (failure) {
      setError(failure instanceof ApiError && failure.code === 'INSUFFICIENT_STOCK' ? SIN_STOCK : NO_ACTUALIZADO)
    } finally {
      setBusy(false)
    }
  }

  return (
    <section aria-label="Resumen de la compra" className={styles.panel}>
      <h2 className={styles.panelTitle}>Revisa tu compra</h2>
      <div className={styles.vendor}>
        <BuildingShop20Regular aria-hidden="true" />
        <span>{group.vendedor.nombreComercial}</span>
      </div>

      <ul className={styles.lines}>
        {group.lineas.map((line) => (
          <li key={line.productoId} className={styles.line}>
            <div className={styles.lineInfo}>
              <Text className={styles.lineName}>{line.nombre}</Text>
              <PriceBlock
                compact
                precio={line.precioOriginal}
                precioFinal={line.precioUnitario}
                descuento={line.porcentajeDescuento == null ? undefined : { porcentaje: line.porcentajeDescuento }}
              />
              <QuantityStepper
                value={line.cantidad}
                max={line.stock}
                label={`Cantidad de ${line.nombre}`}
                disabled={busy}
                onChange={(quantity) => run(() => setQuantity(line.productoId, quantity))}
              />
            </div>
            <div className={styles.lineEnd}>
              <span className={styles.amount}>{formatPrice(line.subtotal)}</span>
              <Button
                appearance="subtle"
                icon={<Delete20Regular />}
                aria-label={`Quitar ${line.nombre}`}
                disabled={busy}
                onClick={() => run(() => remove(line.productoId))}
              />
            </div>
          </li>
        ))}
      </ul>

      {error && (
        <Text role="alert" size={200} className={styles.muted}>
          {error}
        </Text>
      )}

      <div className={styles.total}>
        <Text className={styles.muted}>Total a pagar</Text>
        <span className={styles.totalAmount}>{formatPrice(group.total)}</span>
      </div>

      <div className={styles.actions}>
        <LinkButton to={PATHS.PRODUCTS} appearance="secondary">
          Seguir comprando
        </LinkButton>
        <Button appearance="primary" disabled={busy} onClick={onContinue}>
          Continuar con el envío
        </Button>
      </div>
    </section>
  )
}

CartSummaryStep.propTypes = {
  group: PropTypes.shape({
    vendedor: PropTypes.shape({
      id: PropTypes.string.isRequired,
      nombreComercial: PropTypes.string.isRequired,
    }).isRequired,
    total: PropTypes.number.isRequired,
    lineas: PropTypes.arrayOf(
      PropTypes.shape({
        productoId: PropTypes.string.isRequired,
        nombre: PropTypes.string.isRequired,
        cantidad: PropTypes.number.isRequired,
        stock: PropTypes.number.isRequired,
        precioUnitario: PropTypes.number.isRequired,
        precioOriginal: PropTypes.number.isRequired,
        porcentajeDescuento: PropTypes.number,
        subtotal: PropTypes.number.isRequired,
      }),
    ).isRequired,
  }).isRequired,
  onContinue: PropTypes.func.isRequired,
}
