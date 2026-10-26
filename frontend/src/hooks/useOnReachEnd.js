import { useEffect, useRef } from 'react'

// Avisa con `onReach` cuando el elemento al que se pone el `ref` que devuelve está a la vista, o a punto de estarlo
// (400 px antes): se coloca al final de una lista para cargar más al llegar. Con `active` en falso no observa nada.
// Se vuelve a observar cada vez que cambia `onReach`, así que, si tras cargar el elemento sigue a la vista, avisa otra vez.
export function useOnReachEnd(onReach, active) {
  const ref = useRef(null)

  useEffect(() => {
    const element = ref.current
    if (!active || !element || typeof IntersectionObserver === 'undefined') return undefined

    const observer = new IntersectionObserver(
      (entries) => {
        if (entries.some((entry) => entry.isIntersecting)) onReach()
      },
      { rootMargin: '400px 0px' },
    )
    observer.observe(element)
    return () => observer.disconnect()
  }, [onReach, active])

  return ref
}
