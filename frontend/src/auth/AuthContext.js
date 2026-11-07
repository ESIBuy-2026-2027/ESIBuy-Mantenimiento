import { createContext } from 'react'

export const AuthContext = createContext(null)

// Por qué terminó la última sesión (HU43), para que el login lo avise: el usuario la cerró con el botón, el
// servidor la dio por caducada o el usuario eliminó su cuenta (HU47).
export const SESSION_END = {
  SIGNED_OUT: 'SIGNED_OUT',
  EXPIRED: 'EXPIRED',
  ACCOUNT_DELETED: 'ACCOUNT_DELETED',
}
