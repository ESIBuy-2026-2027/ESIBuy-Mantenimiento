import { createContext } from 'react'

// Sin AlertsProvider (p. ej. fuera de la zona con cabecera) no hay alertas: la cabecera y el aviso no muestran nada.
export const AlertsContext = createContext({ alertas: [], sinLeer: 0, recargar: () => {} })
