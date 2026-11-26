import PropTypes from 'prop-types'
import { useEffect, useMemo, useState } from 'react'
import { getAlerts } from '../../api/alerts'
import { ROLES } from '../../auth/roles'
import { useAuth } from '../../auth/useAuth'
import { AlertsContext } from './AlertsContext'

// Las alertas del cliente en sesión (AB#529), para el indicador de la cabecera y el aviso al iniciar sesión. Se piden
// una vez al empezar la sesión; `recargar` las vuelve a pedir (p. ej. cuando AB#531 las marque como leídas). Si no se
// pueden cargar, no se muestra ningún indicador: la página «Mis alertas» ya avisa del error. Los demás roles no tienen.
export default function AlertsProvider({ children }) {
  const { user } = useAuth()
  const clientId = user?.rol === ROLES.CLIENTE ? user.id : null
  // Ligadas al cliente al que pertenecen: si cambia la sesión, no se enseñan las del anterior
  const [stored, setStored] = useState({ clientId: null, alertas: [] })
  const [attempt, setAttempt] = useState(0)

  useEffect(() => {
    if (!clientId) return undefined
    let cancelled = false

    getAlerts()
      .then((respuesta) => {
        if (!cancelled) setStored({ clientId, alertas: respuesta.alertas })
      })
      .catch(() => {
        if (!cancelled) setStored({ clientId, alertas: [] })
      })

    return () => {
      cancelled = true
    }
  }, [clientId, attempt])

  const value = useMemo(() => {
    const alertas = clientId && stored.clientId === clientId ? stored.alertas : []
    return {
      alertas,
      sinLeer: alertas.filter((alerta) => !alerta.leida).length,
      recargar: () => setAttempt((valor) => valor + 1),
    }
  }, [clientId, stored])

  return <AlertsContext.Provider value={value}>{children}</AlertsContext.Provider>
}

AlertsProvider.propTypes = {
  children: PropTypes.node.isRequired,
}
