import PropTypes from 'prop-types'
import { useCallback, useEffect, useMemo, useState } from 'react'
import { addCartItem, clearCart, getCart, removeCartItem, setCartItemQuantity } from '../../api/cart'
import { ROLES } from '../../auth/roles'
import { useAuth } from '../../auth/useAuth'
import { CartContext } from './CartContext'

const CARRITO_VACIO = { grupos: [], totalProductos: 0, total: 0 }

// El carrito del cliente en sesión (HU9). Vive en el backend: aquí solo se guarda la última copia que ha enviado el
// servidor, que responde con el carrito completo a cada operación. Los demás roles no tienen carrito.
export default function CartProvider({ children }) {
  const { user } = useAuth()
  const clientId = user?.rol === ROLES.CLIENTE ? user.id : null
  // La copia va ligada al cliente al que pertenece: si cambia la sesión, no se enseña el carrito del anterior.
  const [stored, setStored] = useState({ clientId: null, cart: CARRITO_VACIO, failed: false })
  const [attempt, setAttempt] = useState(0)

  useEffect(() => {
    if (!clientId) return undefined
    let cancelled = false

    getCart()
      .then((cart) => {
        if (!cancelled) setStored({ clientId, cart, failed: false })
      })
      .catch(() => {
        if (!cancelled) setStored({ clientId, cart: CARRITO_VACIO, failed: true })
      })

    return () => {
      cancelled = true
    }
  }, [clientId, attempt])

  // Se aplica una operación sobre el carrito del servidor y se guarda su respuesta. Los errores los trata quien la pide.
  const apply = useCallback(
    async (operation) => {
      const cart = await operation()
      setStored({ clientId, cart, failed: false })
    },
    [clientId],
  )

  const value = useMemo(() => {
    const ownCart = stored.clientId === clientId
    return {
      cart: clientId && ownCart ? stored.cart : CARRITO_VACIO,
      status: !clientId ? 'idle' : !ownCart ? 'loading' : stored.failed ? 'failed' : 'ready',
      add: (productId, quantity) => apply(() => addCartItem(productId, quantity)),
      setQuantity: (productId, quantity) => apply(() => setCartItemQuantity(productId, quantity)),
      remove: (productId) => apply(() => removeCartItem(productId)),
      clear: () => apply(() => clearCart()),
      reload: () => setAttempt((value) => value + 1),
    }
  }, [apply, clientId, stored])

  return <CartContext.Provider value={value}>{children}</CartContext.Provider>
}

CartProvider.propTypes = {
  children: PropTypes.node.isRequired,
}
