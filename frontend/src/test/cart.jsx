import { vi } from 'vitest'
import { CartContext } from '../features/cart/CartContext'

export const CARRITO_VACIO = { grupos: [], totalProductos: 0, total: 0 }

// Pone un carrito de mentira alrededor de lo que se prueba: por defecto, vacío y con un `add` que siempre sale bien.
// Se le pueden cambiar partes (`add`, `cart`…) para probar cada caso sin servidor.
export function conCarrito(ui, valor = {}) {
  const value = {
    cart: CARRITO_VACIO,
    status: 'ready',
    add: vi.fn().mockResolvedValue(undefined),
    setQuantity: vi.fn().mockResolvedValue(undefined),
    remove: vi.fn().mockResolvedValue(undefined),
    clear: vi.fn().mockResolvedValue(undefined),
    reload: vi.fn(),
    ...valor,
  }
  return <CartContext.Provider value={value}>{ui}</CartContext.Provider>
}
