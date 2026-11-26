import { useState } from 'react'
import { addWishlistItem, getWishlist, removeWishlistItem } from '../api/wishlist'
import { useApiQuery } from './useApiQuery'

// Productos favoritos del cliente (HU14): se leen al entrar y, después, cada cambio responde con la lista completa, que
// manda sobre la lectura inicial. Si no se pueden leer, se ofrece añadirlos todos, que no es un error si ya lo eran.
export function useFavoriteProducts() {
  const { data } = useApiQuery(getWishlist)
  const [actualizados, setActualizados] = useState(null)
  const items = (actualizados ?? data)?.items ?? []

  return {
    favoritos: new Set(items.map((item) => item.productoId)),
    anadirFavorito: async (productId) => setActualizados(await addWishlistItem(productId)),
    quitarFavorito: async (productId) => setActualizados(await removeWishlistItem(productId)),
  }
}
