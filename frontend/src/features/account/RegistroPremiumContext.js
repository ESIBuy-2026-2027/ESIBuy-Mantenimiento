import { createContext, useContext } from 'react'

// Flujo del registro Premium (HU45): guarda en memoria los datos del formulario mientras el cliente pasa por la
// pasarela de pago simulada. Nunca van a la URL ni al almacenamiento del navegador; al recargar se pierden.
export const RegistroPremiumContext = createContext(null)

export function useRegistroPremium() {
  const context = useContext(RegistroPremiumContext)
  if (!context) {
    throw new Error('useRegistroPremium debe usarse dentro de RegistroPremiumFlow')
  }
  return context
}
