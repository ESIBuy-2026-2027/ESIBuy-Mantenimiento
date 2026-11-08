import { useEffect } from 'react'
import { useBlocker } from 'react-router-dom'

// Avisa antes de perder cambios sin guardar (HU45). Con `hasChanges`:
// - navegar a otra pantalla de la aplicación se bloquea hasta que se decida: `blocked` pasa a true, y `proceed`
//   completa la navegación (se descartan los cambios) y `reset` la cancela (se sigue en la pantalla);
// - cerrar o recargar la pestaña hace que el navegador pida confirmación, con su propio texto: los navegadores no
//   permiten personalizarlo.
// Sin cambios no se bloquea ni se pregunta nada. Necesita un router de datos (createBrowserRouter).
export function useUnsavedChangesGuard(hasChanges) {
  const blocker = useBlocker(
    ({ currentLocation, nextLocation }) => hasChanges && currentLocation.pathname !== nextLocation.pathname,
  )

  useEffect(() => {
    if (!hasChanges) return undefined

    const avisarAlCerrar = (event) => {
      event.preventDefault()
      event.returnValue = ''
    }
    window.addEventListener('beforeunload', avisarAlCerrar)
    return () => window.removeEventListener('beforeunload', avisarAlCerrar)
  }, [hasChanges])

  return {
    blocked: blocker.state === 'blocked',
    proceed: () => blocker.proceed?.(),
    reset: () => blocker.reset?.(),
  }
}
