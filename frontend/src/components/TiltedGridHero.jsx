import PropTypes from 'prop-types'
import { useEffect, useId, useRef, useState } from 'react'
import { makeStyles, mergeClasses, tokens } from '@fluentui/react-components'

// Banda de imágenes curva: una fila de imágenes que avanza de derecha a izquierda por el interior de un cilindro.
// El centro mira de frente, los extremos se curvan hacia quien mira y se difuminan con la página.
//
// Adaptado de «Tilted Grid Hero» (shadcn, TypeScript y Tailwind) a JavaScript y a los estilos de Fluent UI; la
// lógica y la geometría son las del original. Una caja CSS no se puede curvar, así que cada imagen se corta en
// SLICES tiras verticales, cada una una faceta plana del cilindro con su parte de la imagen. Las tiras están
// quietas dentro de su imagen, giradas unos grados cada una alrededor del eje del cilindro, y es la imagen
// (preserve-3d) la que se mueve: un único rotateY sobre ese mismo eje la lleva por el arco. Cada imagen es una
// animación, así que sus tiras nunca se separan ni enseñan imágenes distintas. Solo cambia el retardo de cada una,
// que fija en qué punto del arco está. La banda solo usa transformaciones (va en el compositor) y el difuminado
// es una máscara fija encima.
//
// El único JavaScript es un ResizeObserver que dimensiona el bucle y un manejador que da a cada imagen la
// siguiente cada vez que da la vuelta, fuera de la vista.

// Tiras por imagen. Cada una abarca unos pocos grados de arco, demasiado poco para verse como faceta; con menos
// empiezan a verse esquinas arriba y abajo. Una curva cerrada envuelve la imagen en un arco ancho y necesita tantas.
const SLICES = 16

// Distancia de la cámara a la pantalla, como múltiplo del radio del cilindro. Un poco más allá del eje, para que
// los extremos lleguen a la lente y se agranden; mucho más atrás y la fila se aplana por muy cerrado que sea.
const CAMERA = 1.6

// Radio del cilindro, como proporción del ancho, que lleva el borde del marco `bend` radianes por el arco visto
// por la cámara: r·sinθ · CAMERA / (CAMERA − 1 + cosθ) = ancho / 2, despejando r.
function arc(bend) {
  return (CAMERA - 1 + Math.cos(bend)) / (2 * CAMERA * Math.sin(bend))
}

// Una imagen nunca es más ancha que esta proporción del conjunto, para que un móvil enseñe también las vecinas en
// lugar de una sola imagen de borde a borde.
const MAX_WIDTH = 55

// Coloca el bucle en el cilindro. Los ángulos salen en grados: `unit` es el arco que abarca el alto de una imagen,
// `limit` hasta dónde puede ir el centro de una imagen antes de que toda ella quede fuera de la vista y `sweep`
// hasta dónde llegan los extremos del bucle. El bucle va más allá de `limit`, así que una imagen solo da la vuelta
// fuera de la vista, y se redondea a un número entero de imágenes para que el hueco nunca se estire.
function measure(width, height, tile, aspect, gap, bend) {
  const h = Math.min((tile / 100) * height, ((MAX_WIDTH / 100) * width) / aspect)
  const radius = width * arc(bend)
  if (!(h > 0) || !(radius > 0)) return { columns: 2, sweep: 0, unit: 0, limit: 0 }
  const deg = (rad) => (rad * 180) / Math.PI
  const unit = deg(h / radius)
  const pitch = (aspect + gap / 100) * unit
  const limit = deg(bend) + (aspect * unit) / 2
  // Con tope, para que un conjunto aplastado a una rendija no monte miles de imágenes.
  const columns = Math.min(60, Math.max(2, Math.ceil((2 * limit) / pitch)))
  const round = (n) => +n.toFixed(4)
  const sweep = round((columns * pitch) / 2)
  return { columns, sweep, unit: round(unit), limit: round(Math.min(sweep, limit)) }
}

const useStyles = makeStyles({
  root: {
    position: 'relative',
    overflow: 'hidden',
  },
  band: {
    pointerEvents: 'none',
    position: 'absolute',
    top: 0,
    right: 0,
    bottom: 0,
    left: 0,
    transitionProperty: 'opacity',
    transitionDuration: '500ms',
  },
  tile: {
    position: 'absolute',
  },
  strip: {
    position: 'absolute',
    top: 0,
    overflow: 'hidden',
    backgroundColor: tokens.colorNeutralBackground3,
  },
  firstStrip: {
    borderTopLeftRadius: tokens.borderRadiusLarge,
    borderBottomLeftRadius: tokens.borderRadiusLarge,
  },
  lastStrip: {
    borderTopRightRadius: tokens.borderRadiusLarge,
    borderBottomRightRadius: tokens.borderRadiusLarge,
  },
  image: {
    position: 'absolute',
    top: 0,
    maxWidth: 'none',
    objectFit: 'cover',
  },
})

// Las imágenes van por la fila en orden y cada una toma la siguiente cuando da la vuelta, así que toda la lista se
// recorre aunque el marco necesite pocas; las listas cortas se repiten.
export default function TiltedGridHero({
  images,
  speed = 4,
  tileHeight = 26,
  aspectRatio = 16 / 9,
  gap = 6,
  axis = 56,
  curve = 80,
  fade = 12,
  children,
  className,
  style,
  ...props
}) {
  const styles = useStyles()
  const ref = useRef(null)
  const id = useId().replace(/[^a-zA-Z0-9]/g, '')
  const orbit = `tgh-o-${id}`
  const tile = `tgh-t-${id}`
  const bend = (Math.min(85, Math.max(5, curve)) * Math.PI) / 180

  // Nulo hasta que el observador ha medido el conjunto. La banda sigue oculta hasta entonces, para que un primer
  // pintado calculado para otro tamaño no llegue a verse.
  const [layout, setLayout] = useState(null)

  // La imagen que enseña cada imagen de la fila, como posición en la tira de imágenes. Solo cambia cuando da la
  // vuelta, fuera de la vista, así que cambiar el tamaño tampoco cambia una imagen a la vista.
  const [shown, setShown] = useState({})

  useEffect(() => {
    const el = ref.current
    if (!el) return undefined
    const ro = new ResizeObserver(([entry]) => {
      const { width, height } = entry.contentRect
      const next = measure(width, height, tileHeight, aspectRatio, gap, bend)
      setLayout((prev) =>
        prev?.columns === next.columns && prev.sweep === next.sweep && prev.unit === next.unit ? prev : next,
      )
    })
    ro.observe(el)
    return () => ro.disconnect()
  }, [tileHeight, aspectRatio, gap, bend])

  // Se piden todas las imágenes de antemano: una imagen toma la siguiente justo antes de volver a la vista, tarde
  // para empezar a cargarla entonces.
  useEffect(() => {
    for (const { src } of images) new Image().src = src
  }, [images])

  const { columns, sweep, unit, limit } = layout ?? measure(1200, 560, tileHeight, aspectRatio, gap, bend)

  // Toda longitud es un múltiplo del alto de la imagen, que sigue al del conjunto hasta que MAX_WIDTH lo limita
  // en pantallas estrechas.
  const u = (n) => `calc(${+n.toFixed(4)} * min(${tileHeight}cqh, ${+(MAX_WIDTH / aspectRatio).toFixed(4)}cqw))`

  const r = 100 * arc(bend)
  const radius = `${+r.toFixed(3)}cqw`
  // Hasta el eje, giro y vuelta: una caja se apoya en el cilindro con el ángulo que le da rotateY.
  const turn = (deg) => `translateZ(${radius}) rotateY(${+deg.toFixed(4)}deg) translateZ(-${radius})`

  // Más allá de `limit` una imagen queda del todo fuera de la vista, así que se oculta. Los extremos del bucle,
  // donde da la vuelta, caen en ese tramo, y también las imágenes que giran por detrás de la cámara.
  const hide = +(((sweep - limit) / (2 * sweep || 1)) * 100).toFixed(4)
  const css =
    `@keyframes ${orbit}{` +
    `from{transform:${turn(-sweep)}}to{transform:${turn(sweep)}}` +
    `0%,${hide}%,${100 - hide}%,100%{visibility:hidden}` +
    `${hide + 0.001}%,${100 - hide - 0.001}%{visibility:visible}}` +
    // Pausar en lugar de quitar el movimiento mantiene la banda entera: cada imagen ya está colocada por su
    // retardo negativo, así que se queda como una imagen fija terminada.
    `@media(prefers-reduced-motion:reduce){.${tile}{animation-play-state:paused}}`

  const share = aspectRatio / SLICES
  const duration = columns * speed
  const mask = `linear-gradient(90deg,transparent,#000 ${fade}%,#000 ${100 - fade}%,transparent)`

  return (
    <div
      ref={ref}
      className={mergeClasses(styles.root, className)}
      {...props}
      style={{ containerType: 'size', ...style }}
    >
      <style>{css}</style>

      <div
        aria-hidden="true"
        className={styles.band}
        style={{
          opacity: layout ? 1 : 0,
          // Un punto de fuga común en el centro de la fila, para que los extremos crezcan por igual por encima y
          // por debajo al acercarse a quien mira.
          perspective: `${+(r * CAMERA).toFixed(3)}cqw`,
          perspectiveOrigin: `50% ${axis}%`,
          maskImage: mask,
          WebkitMaskImage: mask,
        }}
      >
        {Array.from({ length: columns }, (_, t) => {
          // La imagen `columns - 1` empieza más cerca de la salida y la 0 más atrás: así se numeran en el orden
          // en que van entrando.
          const first = columns - 1 - t
          const img = images[(shown[t] ?? first) % Math.max(images.length, 1)]
          return (
            <div
              key={t}
              className={mergeClasses(tile, styles.tile)}
              style={{
                left: `calc(50% - ${u(aspectRatio / 2)})`,
                top: `calc(${axis}% - ${u(0.5)})`,
                width: u(aspectRatio),
                height: u(1),
                transformStyle: 'preserve-3d',
                // La imagen t empieza t imágenes más allá en el bucle.
                animation: `${orbit} ${duration}s linear ${-t * speed}s infinite`,
              }}
              onAnimationIteration={(e) => {
                // elapsedTime cuenta vueltas enteras, también las de eventos perdidos.
                const lap = Math.round(e.elapsedTime / duration)
                setShown((prev) => {
                  const next = lap * columns + first
                  return prev[t] === next ? prev : { ...prev, [t]: next }
                })
              }}
            >
              {Array.from({ length: SLICES }, (_, k) => (
                <div
                  key={k}
                  className={mergeClasses(
                    styles.strip,
                    k === 0 && styles.firstStrip,
                    k === SLICES - 1 && styles.lastStrip,
                  )}
                  style={{
                    // Cada tira parte del centro de la imagen y se gira alrededor del eje hasta su sitio del arco.
                    left: u((aspectRatio - share) / 2),
                    // Un píxel más ancha que su parte, para que las facetas vecinas se solapen y no se abra
                    // ninguna línea fina entre ellas. La última no tiene vecina que cubrir.
                    width: k === SLICES - 1 ? u(share) : `calc(${u(share)} + 1px)`,
                    height: u(1),
                    transform: turn((aspectRatio / 2 - (k + 0.5) * share) * unit),
                  }}
                >
                  {img ? (
                    <img
                      src={img.src}
                      alt={k === 0 ? (img.alt ?? '') : ''}
                      draggable={false}
                      className={styles.image}
                      style={{ left: u(-k * share), width: u(aspectRatio), height: u(1) }}
                    />
                  ) : null}
                </div>
              ))}
            </div>
          )
        })}
      </div>

      {children}
    </div>
  )
}

TiltedGridHero.propTypes = {
  // Imágenes de la fila, en orden. `alt` solo se usa si se quita el tratamiento decorativo: la banda es aria-hidden.
  images: PropTypes.arrayOf(
    PropTypes.shape({
      src: PropTypes.string.isRequired,
      alt: PropTypes.string,
    }),
  ).isRequired,
  // Segundos que tarda la fila en avanzar una imagen. Es por imagen, no por cruce, así que un móvil y una pantalla
  // ultraancha se mueven al mismo ritmo.
  speed: PropTypes.number,
  // Alto de cada imagen, como porcentaje del alto del conjunto.
  tileHeight: PropTypes.number,
  // Ancho de cada imagen dividido por su alto.
  aspectRatio: PropTypes.number,
  // Espacio entre imágenes, como porcentaje del alto de una imagen.
  gap: PropTypes.number,
  // Centro vertical de la fila, como porcentaje del alto del conjunto.
  axis: PropTypes.number,
  // Cuánto se ha curvado la fila en el borde del marco, en grados (de 5 a 85). Más curva los extremos con más fuerza.
  curve: PropTypes.number,
  // Ancho del difuminado en cada extremo, como porcentaje del ancho del conjunto.
  fade: PropTypes.number,
  // Contenido que se pinta por encima de la banda.
  children: PropTypes.node,
  className: PropTypes.string,
  style: PropTypes.object,
}
