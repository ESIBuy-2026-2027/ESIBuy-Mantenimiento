import { useContext } from 'react'
import { AlertsContext } from './AlertsContext'

export function useAlerts() {
  return useContext(AlertsContext)
}
