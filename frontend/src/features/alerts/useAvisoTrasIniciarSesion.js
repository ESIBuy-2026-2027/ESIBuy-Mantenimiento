import { useLocation } from 'react-router-dom'
import { useAlerts } from './useAlerts'

// Si toca avisar de las alertas sin leer: solo en la primera página tras iniciar sesión (el login navega con
// `state.trasIniciarSesion`, que se pierde al navegar) y si hay alguna sin leer. Lo usan la campanita, que se balancea,
// y la notificación que sale de ella (AB#531).
export function useAvisoTrasIniciarSesion() {
  const { state } = useLocation()
  const { sinLeer } = useAlerts()
  return Boolean(state?.trasIniciarSesion) && sinLeer > 0
}
