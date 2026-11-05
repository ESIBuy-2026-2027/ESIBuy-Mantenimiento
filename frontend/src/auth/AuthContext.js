import { createContext } from 'react'

export const AuthContext = createContext(null)

// Por qué terminó la última sesión (HU43), para que el login lo avise: el usuario la cerró con el botón o el
// servidor la dio por caducada.
export const SESSION_END = {
  SIGNED_OUT: 'SIGNED_OUT',
  EXPIRED: 'EXPIRED',
}
