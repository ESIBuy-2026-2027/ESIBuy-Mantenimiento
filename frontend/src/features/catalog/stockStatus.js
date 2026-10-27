// A partir de cuántas unidades se avisa de que quedan pocas.
const POCAS_UNIDADES = 5

// Disponibilidad de un producto para el cliente (HU8): `tone` es el aspecto con el que se pinta (success, warning o
// danger; subtle si el vendedor lo ha retirado).
export function stockStatus({ stock, disponible }) {
  if (!disponible) return { key: 'unavailable', label: 'No disponible', tone: 'subtle' }
  if (stock <= 0) return { key: 'out', label: 'Agotado', tone: 'danger' }
  if (stock <= POCAS_UNIDADES) {
    return { key: 'low', label: stock === 1 ? 'Solo queda 1 unidad' : `Solo quedan ${stock} unidades`, tone: 'warning' }
  }
  return { key: 'in', label: 'En stock', tone: 'success' }
}
