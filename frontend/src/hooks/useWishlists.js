import { useState } from 'react'
import { getWishlists } from '../api/wishlist'
import { useApiQuery } from './useApiQuery'

// Listas de deseos del cliente (PBI 534): se leen al entrar y el selector las actualiza con `cambiarListas`. `listas` es
// null mientras no se han podido cargar; `enAlgunaLista` son los productos guardados en al menos una.
export function useWishlists() {
  const { data, failed, retry } = useApiQuery(getWishlists)
  const [actualizadas, setActualizadas] = useState(null)
  const listas = actualizadas ?? data

  return {
    listas,
    listasFallidas: failed,
    recargarListas: retry,
    cambiarListas: setActualizadas,
    enAlgunaLista: new Set((listas ?? []).flatMap((lista) => lista.items.map((item) => item.productoId))),
  }
}
