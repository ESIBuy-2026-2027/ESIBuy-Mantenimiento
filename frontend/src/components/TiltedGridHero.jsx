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
//
// Añadidos respecto al original: cada imagen puede llevar un pie (`caption`, que se corta en tiras igual que la
// imagen) y avisar con `onSelect` al pulsarla; y, al cambiar el tamaño de la página, la banda se reinicia entera.

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

// Hasta el eje, giro y vuelta: una caja se apoya en el cilindro con el ángulo que le da rotateY.
function turn(bend, deg) {
  const radius = `${+(100 * arc(bend)).toFixed(3)}cqw`
  return `translateZ(${radius}) rotateY(${+deg.toFixed(4)}deg) translateZ(-${radius})`
}

// Milisegundos que se espera a que termine de cambiar el tamaño antes de reiniciar la banda.
const REINICIO_MS = 150

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
  },
  // Cada imagen va en su propia capa (will-change). Con el contorno transparente y backface-visibility los bordes
  // inclinados de las tiras se suavizan: sin ellos, el navegador los dibuja con escalones («dientes de sierra»).
  // No lleva borde ni marco: solo la imagen.
  tile: {
    position: 'absolute',
    willChange: 'transform',
  },
  clickable: {
    pointerEvents: 'auto',
    cursor: 'pointer',
  },
  strip: {
    position: 'absolute',
    top: 0,
    overflow: 'hidden',
    outline: '1px solid transparent',
    backfaceVisibility: 'hidden',
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
  caption: {
    position: 'absolute',
    top: 0,
    boxSizing: 'border-box',
    pointerEvents: 'none',
    color: tokens.colorNeutralForegroundOnBrand,
  },
})

// La fila de imágenes. Está aparte para poder reiniciarla entera (con `key`) cuando cambia el tamaño de la página:
// así vuelven a empezar a la vez todas las animaciones y la imagen que enseña cada una, y no se solapan.
function Band({ images, layout, aspectRatio, tileHeight, axis, bend, speed, fade, ids, onSelect }) {
  const styles = useStyles()
  const { columns, unit } = layout
  const { orbit, tile } = ids

  // La imagen que enseña cada imagen de la fila, como posición en la tira de imágenes. Solo cambia cuando da la
  // vuelta, fuera de la vista, así que cambiar el tamaño tampoco cambia una imagen a la vista.
  const [shown, setShown] = useState({})

  // Toda longitud es un múltiplo del alto de la imagen, que sigue al del conjunto hasta que MAX_WIDTH lo limita
  // en pantallas estrechas.
  const u = (n) => `calc(${+n.toFixed(4)} * min(${tileHeight}cqh, ${+(MAX_WIDTH / aspectRatio).toFixed(4)}cqw))`

  const r = 100 * arc(bend)

  const share = aspectRatio / SLICES
  const duration = columns * speed
  const mask = `linear-gradient(90deg,transparent,#000 ${fade}%,#000 ${100 - fade}%,transparent)`

  return (
    <div
      aria-hidden="true"
      className={styles.band}
      style={{
        // Al (re)montarse, la banda aparece poco a poco en lugar de dar un salto.
        animation: `${orbit}-in 400ms ease-out`,
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
            // Las imágenes de la banda son un botón solo para el ratón; para el teclado y los lectores de pantalla
            // está la lista que acompaña a la banda.
            role={onSelect ? 'button' : undefined}
            aria-label={onSelect ? img?.alt : undefined}
            className={mergeClasses(tile, styles.tile, onSelect && styles.clickable)}
            style={{
              left: `calc(50% - ${u(aspectRatio / 2)})`,
              top: `calc(${axis}% - ${u(0.5)})`,
              width: u(aspectRatio),
              height: u(1),
              transformStyle: 'preserve-3d',
              // La imagen t empieza t imágenes más allá en el bucle.
              animation: `${orbit} ${duration}s linear ${-t * speed}s infinite`,
            }}
            onClick={onSelect && img ? () => onSelect(img) : undefined}
            // Mismo efecto con Enter o Espacio, como cualquier botón (Sonar S1082)
            onKeyDown={
              onSelect && img
                ? (e) => {
                    if (e.key !== 'Enter' && e.key !== ' ') return
                    e.preventDefault()
                    onSelect(img)
                  }
                : undefined
            }
            onAnimationIteration={(e) => {
              // La animación de entrada de la banda no es la vuelta de una imagen.
              if (e.animationName !== orbit) return
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
                  transform: turn(bend, (aspectRatio / 2 - (k + 0.5) * share) * unit),
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
                {img?.caption ? (
                  // El pie ocupa toda la imagen y se corta en tiras igual que ella. Su tamaño de letra es una
                  // fracción del alto de la imagen, para que el texto crezca y encoja con ella (usar `em`).
                  <div
                    className={styles.caption}
                    style={{ left: u(-k * share), width: u(aspectRatio), height: u(1), fontSize: u(0.085) }}
                  >
                    {img.caption}
                  </div>
                ) : null}
              </div>
            ))}
          </div>
        )
      })}
    </div>
  )
}

// Las imágenes van por la fila en orden y cada una toma la siguiente cuando da la vuelta, así que toda la lista se
// recorre aunque el marco necesite pocas; las listas cortas se repiten.
export default function TiltedGridHero({
  images,
  onSelect,
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
  // pintado calculado para otro tamaño no llegue a verse. `size` identifica el tamaño medido: cada tamaño nuevo
  // reinicia la banda.
  const [layout, setLayout] = useState(null)

  useEffect(() => {
    const el = ref.current
    if (!el) return undefined
    let timer = null
    let measured = false

    const apply = (width, height) => {
      const size = `${Math.round(width)}x${Math.round(height)}`
      const next = { ...measure(width, height, tileHeight, aspectRatio, gap, bend), size }
      setLayout((prev) => (prev?.size === size ? prev : next))
    }

    const ro = new ResizeObserver(([entry]) => {
      const { width, height } = entry.contentRect
      // La primera medida es inmediata; las siguientes esperan a que termine de cambiar el tamaño.
      if (!measured) {
        measured = true
        apply(width, height)
        return
      }
      clearTimeout(timer)
      timer = setTimeout(() => apply(width, height), REINICIO_MS)
    })
    ro.observe(el)
    return () => {
      clearTimeout(timer)
      ro.disconnect()
    }
  }, [tileHeight, aspectRatio, gap, bend])

  // Se piden todas las imágenes de antemano: una imagen toma la siguiente justo antes de volver a la vista, tarde
  // para empezar a cargarla entonces.
  useEffect(() => {
    for (const { src } of images) new Image().src = src
  }, [images])

  const measures = layout ?? { ...measure(1200, 560, tileHeight, aspectRatio, gap, bend), size: 'inicial' }
  const { sweep, limit } = measures

  // Más allá de `limit` una imagen queda del todo fuera de la vista, así que se oculta. Los extremos del bucle,
  // donde da la vuelta, caen en ese tramo, y también las imágenes que giran por detrás de la cámara.
  const hide = +(((sweep - limit) / (2 * sweep || 1)) * 100).toFixed(4)
  const css =
    `@keyframes ${orbit}{` +
    `from{transform:${turn(bend, -sweep)}}to{transform:${turn(bend, sweep)}}` +
    `0%,${hide}%,${100 - hide}%,100%{visibility:hidden}` +
    `${hide + 0.001}%,${100 - hide - 0.001}%{visibility:visible}}` +
    `@keyframes ${orbit}-in{from{opacity:0}to{opacity:1}}` +
    // Pausar en lugar de quitar el movimiento mantiene la banda entera: cada imagen ya está colocada por su
    // retardo negativo, así que se queda como una imagen fija terminada.
    `@media(prefers-reduced-motion:reduce){.${tile}{animation-play-state:paused}}`

  return (
    <div
      ref={ref}
      className={mergeClasses(styles.root, className)}
      {...props}
      style={{ containerType: 'size', ...style }}
    >
      <style>{css}</style>

      <div
        // Sin medir todavía, la banda no se ve. Es un contenedor aparte para no mezclar este opacity con la
        // animación de entrada de la banda, que se reinicia con cada tamaño.
        style={{ opacity: layout ? 1 : 0, transition: 'opacity 500ms' }}
        className={styles.band}
      >
        <Band
          key={measures.size}
          images={images}
          layout={measures}
          aspectRatio={aspectRatio}
          tileHeight={tileHeight}
          axis={axis}
          bend={bend}
          speed={speed}
          fade={fade}
          ids={{ orbit, tile }}
          onSelect={onSelect}
        />
      </div>

      {children}
    </div>
  )
}

const imagePropType = PropTypes.shape({
  src: PropTypes.string.isRequired,
  // Nombre de la imagen: su texto alternativo y, si se puede pulsar, el nombre del botón.
  alt: PropTypes.string,
  // Contenido que se dibuja sobre la imagen (nombre, valoración…); usa `em` para los tamaños.
  caption: PropTypes.node,
})

Band.propTypes = {
  images: PropTypes.arrayOf(imagePropType).isRequired,
  layout: PropTypes.shape({
    columns: PropTypes.number.isRequired,
    unit: PropTypes.number.isRequired,
  }).isRequired,
  aspectRatio: PropTypes.number.isRequired,
  tileHeight: PropTypes.number.isRequired,
  axis: PropTypes.number.isRequired,
  bend: PropTypes.number.isRequired,
  speed: PropTypes.number.isRequired,
  fade: PropTypes.number.isRequired,
  ids: PropTypes.shape({ orbit: PropTypes.string.isRequired, tile: PropTypes.string.isRequired }).isRequired,
  onSelect: PropTypes.func,
}

TiltedGridHero.propTypes = {
  // Imágenes de la fila, en orden. La banda es decorativa (aria-hidden): quien use `onSelect` debe ofrecer otra
  // forma, accesible, de llegar a lo mismo.
  images: PropTypes.arrayOf(imagePropType).isRequired,
  // Si se indica, las imágenes de la banda se pueden pulsar con el ratón y avisan con la imagen pulsada.
  onSelect: PropTypes.func,
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
