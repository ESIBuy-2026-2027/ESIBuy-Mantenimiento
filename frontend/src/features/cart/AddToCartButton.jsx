import PropTypes from 'prop-types'
import { useEffect, useRef, useState } from 'react'
import { Button, Text, makeStyles, tokens } from '@fluentui/react-components'
import { Checkmark16Regular, Cart20Regular } from '@fluentui/react-icons'
import { useCart } from './useCart'

// Cuánto tiempo se enseña «Añadido» antes de volver a «Añadir al carrito».
const CONFIRMACION_MS = 2000

const useStyles = makeStyles({
  wrapper: {
    display: 'flex',
    flexDirection: 'column',
    gap: tokens.spacingVerticalXS,
  },
  error: {
    color: tokens.colorPaletteRedForeground1,
  },
})

// Botón para añadir un producto al carrito (HU9): muestra que está añadiendo, confirma con «Añadido» y avisa si el
// servidor lo rechaza (sin existencias suficientes u otro fallo). Un producto agotado o no disponible no se puede añadir.
export default function AddToCartButton({ product, quantity = 1, appearance = 'primary', size = 'medium', fullWidth = false }) {
  const styles = useStyles()
  const { add } = useCart()
  const [phase, setPhase] = useState('idle')
  const [error, setError] = useState('')
  const timer = useRef(null)

  useEffect(() => () => clearTimeout(timer.current), [])

  const buttonStyle = fullWidth ? { width: '100%' } : undefined

  if (!product.disponible || product.stock <= 0) {
    return (
      <Button disabled appearance="outline" size={size} style={buttonStyle}>
        {!product.disponible ? 'No disponible' : 'Agotado'}
      </Button>
    )
  }

  const handleClick = async () => {
    setError('')
    setPhase('adding')
    try {
      await add(product.id, quantity)
      setPhase('added')
      timer.current = setTimeout(() => setPhase('idle'), CONFIRMACION_MS)
    } catch (failure) {
      setPhase('idle')
      setError(
        failure?.code === 'INSUFFICIENT_STOCK'
          ? 'No hay existencias suficientes.'
          : 'No se ha podido añadir al carrito.',
      )
    }
  }

  return (
    <div className={styles.wrapper}>
      <Button
        appearance={appearance}
        size={size}
        style={buttonStyle}
        icon={phase === 'added' ? <Checkmark16Regular /> : <Cart20Regular />}
        disabled={phase === 'adding'}
        onClick={handleClick}
      >
        {phase === 'adding' ? 'Añadiendo…' : phase === 'added' ? 'Añadido' : 'Añadir al carrito'}
      </Button>
      {error && (
        <Text role="alert" size={200} className={styles.error}>
          {error}
        </Text>
      )}
    </div>
  )
}

AddToCartButton.propTypes = {
  product: PropTypes.shape({
    id: PropTypes.string.isRequired,
    stock: PropTypes.number.isRequired,
    disponible: PropTypes.bool.isRequired,
  }).isRequired,
  quantity: PropTypes.number,
  appearance: PropTypes.string,
  size: PropTypes.string,
  fullWidth: PropTypes.bool,
}
