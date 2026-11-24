import PropTypes from 'prop-types'
import { useState } from 'react'
import { Button, Text, makeStyles, tokens } from '@fluentui/react-components'
import AddToCartButton from './AddToCartButton'
import QuantityStepper from './QuantityStepper'
import { useCart } from './useCart'

const useStyles = makeStyles({
  controls: {
    display: 'flex',
    flexDirection: 'column',
    gap: tokens.spacingVerticalS,
  },
  row: {
    display: 'flex',
    alignItems: 'center',
    flexWrap: 'wrap',
    gap: tokens.spacingHorizontalS,
  },
  muted: {
    color: tokens.colorNeutralForeground3,
  },
  error: {
    color: tokens.colorPaletteRedForeground1,
  },
})

const unidades = (n) => `${n} ${n === 1 ? 'unidad' : 'unidades'}`

// Lo que se hace con un producto en su tarjeta (HU9): cuántas unidades quedan en stock, elegir cuántas añadir al
// carrito (entre 1 y el stock) y, si el producto ya está en el carrito, cambiar cuántas hay o quitarlo. Un producto
// agotado o no disponible no se puede comprar y lo dice su botón.
export default function CartControls({ product }) {
  const styles = useStyles()
  const { cart, setQuantity, remove } = useCart()
  const [quantity, setQuantityToAdd] = useState(1)
  const [busy, setBusy] = useState(false)
  const [error, setError] = useState('')

  if (!product.disponible || product.stock <= 0) {
    return <AddToCartButton product={product} size="small" fullWidth />
  }

  const inCart =
    cart.grupos.flatMap(({ lineas }) => lineas).find(({ productoId }) => productoId === product.id)?.cantidad ?? 0

  const changeInCart = async (newQuantity) => {
    setBusy(true)
    setError('')
    try {
      if (newQuantity === 0) {
        await remove(product.id)
      } else {
        await setQuantity(product.id, newQuantity)
      }
    } catch {
      setError('No se ha podido actualizar el carrito.')
    } finally {
      setBusy(false)
    }
  }

  return (
    <div className={styles.controls}>
      <Text size={200} className={styles.muted}>
        {product.stock === 1 ? 'Queda 1 unidad' : `Quedan ${unidades(product.stock)}`} en stock
      </Text>

      {inCart === 0 ? (
        <>
          <QuantityStepper
            value={Math.min(quantity, product.stock)}
            max={product.stock}
            label={`Cantidad de ${product.nombre}`}
            onChange={setQuantityToAdd}
          />
          <AddToCartButton product={product} quantity={Math.min(quantity, product.stock)} size="small" fullWidth />
        </>
      ) : (
        <>
          <Text size={200}>En tu carrito: {inCart}</Text>
          <div className={styles.row}>
            <QuantityStepper
              value={inCart}
              min={0}
              max={product.stock}
              label={`Unidades de ${product.nombre} en el carrito`}
              disabled={busy}
              onChange={changeInCart}
            />
            <Button appearance="subtle" size="small" disabled={busy} onClick={() => changeInCart(0)}>
              Quitar del carrito
            </Button>
          </div>
          {inCart >= product.stock && (
            <Text size={200} className={styles.muted}>
              Ya tienes en el carrito todo el stock disponible.
            </Text>
          )}
        </>
      )}

      {error && (
        <Text role="alert" size={200} className={styles.error}>
          {error}
        </Text>
      )}
    </div>
  )
}

CartControls.propTypes = {
  product: PropTypes.shape({
    id: PropTypes.string.isRequired,
    nombre: PropTypes.string.isRequired,
    stock: PropTypes.number.isRequired,
    disponible: PropTypes.bool.isRequired,
  }).isRequired,
}
