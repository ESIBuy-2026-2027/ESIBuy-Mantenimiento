
import PropTypes from 'prop-types'
import { useEffect, useId, useRef, useState } from 'react'
import { makeStyles, mergeClasses, tokens } from '@fluentui/react-components'

// Banda de imágenes curva: una fila de imágenes que avanza de derecha a izquierda por el interior de un cilindro.
// El centro mira de frente, los extremos se curvan hacia quien mira y se difuminan con la página.
//
// Adaptado de «Tilted Grid Hero» (shadcn, TypeScript y Tailwind) a JavaScript y a los estilos de Fluent UI; la
// lógica y la geometría son las del original. Cada imagen se corta en SLICES tiras verticales que forman facetas
// planas del cilindro. Las tiras permanecen quietas dentro de su imagen y es la imagen la que se mueve.
//
// Añadidos respecto al original: cada imagen puede llevar un pie (`caption`) y avisar con `onSelect` al pulsarla;
// al cambiar el tamaño de la página, la banda se reinicia entera.

const SLICES = 16
const CAMERA = 1.6
const MAX_WIDTH = 55
const REINICIO_MS = 150

function arc(bend) {
  return (CAMERA - 1 + Math.cos(bend)) / (2 * CAMERA * Math.sin(bend))
}

function turn(bend, deg) {
  const radius = `${+(100 * arc(bend)).toFixed(3)}cqw`
  return `translateZ(${radius}) rotateY(${+deg.toFixed(4)}deg) translateZ(-${radius})`
}

function measure(width, height, tile, aspect, gap, bend) {
  const h = Math.min((tile / 100) * height, ((MAX_WIDTH / 100) * width) / aspect)
  const radius = width * arc(bend)

  if (h <= 0 || radius <= 0) {
    return { columns: 2, sweep: 0, unit: 0, limit: 0 }
  }

  const deg = (rad) => (rad * 180) / Math.PI
  const unit = deg(h / radius)
  const pitch = (aspect + gap / 100) * unit
  const limit = deg(bend) + (aspect * unit) / 2
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

function Band({ images, layout, aspectRatio, tileHeight, axis, bend, speed, fade, ids, onSelect }) {
  const styles = useStyles()
  const { columns, unit } = layout
  const { orbit, tile } = ids

  const [shown, setShown] = useState({})

  const u = (n) =>
    `calc(${+n.toFixed(4)} * min(${tileHeight}cqh, ${+(MAX_WIDTH / aspectRatio).toFixed(4)}cqw))`

  const r = 100 * arc(bend)
  const share = aspectRatio / SLICES
  const duration = columns * speed
  const mask = `linear-gradient(90deg,transparent,#000 ${fade}%,#000 ${100 - fade}%,transparent)`
  const Tile = onSelect ? 'button' : 'div'

  return (
    <div
      aria-hidden="true"
      className={styles.band}
      style={{
        animation: `${orbit}-in 400ms ease-out`,
        perspective: `${+(r * CAMERA).toFixed(3)}cqw`,
        perspectiveOrigin: `50% ${axis}%`,
        maskImage: mask,
        WebkitMaskImage: mask,
      }}
    >
      {Array.from({ length: columns }, (_, t) => {
        const first = columns - 1 - t
        const img = images[(shown[t] ?? first) % Math.max(images.length, 1)]

        return (
          <Tile
            key={t}
            aria-label={img?.alt}
            {...(onSelect ? { type: 'button', tabIndex: -1 } : {})}
            className={mergeClasses(tile, styles.tile, onSelect && styles.clickable)}
            style={{
              ...(onSelect
                ? {
                  appearance: 'none',
                  border: 0,
                  padding: 0,
                  margin: 0,
                  background: 'transparent',
                  color: 'inherit',
                  textAlign: 'inherit',
                }
                : {}),
              left: `calc(50% - ${u(aspectRatio / 2)})`,
              top: `calc(${axis}% - ${u(0.5)})`,
              width: u(aspectRatio),
              height: u(1),
              transformStyle: 'preserve-3d',
              animation: `${orbit} ${duration}s linear ${-t * speed}s infinite`,
            }}
            onClick={onSelect && img ? () => onSelect(img) : undefined}
            onAnimationIteration={(e) => {
              if (e.animationName !== orbit) return

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
                  left: u((aspectRatio - share) / 2),
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
                    style={{
                      left: u(-k * share),
                      width: u(aspectRatio),
                      height: u(1),
                    }}
                  />
                ) : null}

                {img?.caption ? (
                  <div
                    className={styles.caption}
                    style={{
                      left: u(-k * share),
                      width: u(aspectRatio),
                      height: u(1),
                      fontSize: u(0.085),
                    }}
                  >
                    {img.caption}
                  </div>
                ) : null}
              </div>
            ))}
          </Tile>
        )
      })}
    </div>
  )
}

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

  useEffect(() => {
    for (const { src } of images) {
      new Image().src = src
    }
  }, [images])

  const measures = layout ?? {
    ...measure(1200, 560, tileHeight, aspectRatio, gap, bend),
    size: 'inicial',
  }

  const { sweep, limit } = measures
  const hide = +(((sweep - limit) / (2 * sweep || 1)) * 100).toFixed(4)

  const css =
    `@keyframes ${orbit}{` +
    `from{transform:${turn(bend, -sweep)}}to{transform:${turn(bend, sweep)}}` +
    `0%,${hide}%,${100 - hide}%,100%{visibility:hidden}` +
    `${hide + 0.001}%,${100 - hide - 0.001}%{visibility:visible}}` +
    `@keyframes ${orbit}-in{from{opacity:0}to{opacity:1}}` +
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
  alt: PropTypes.string,
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
  ids: PropTypes.shape({
    orbit: PropTypes.string.isRequired,
    tile: PropTypes.string.isRequired,
  }).isRequired,
  onSelect: PropTypes.func,
}

TiltedGridHero.propTypes = {
  images: PropTypes.arrayOf(imagePropType).isRequired,
  onSelect: PropTypes.func,
  speed: PropTypes.number,
  tileHeight: PropTypes.number,
  aspectRatio: PropTypes.number,
  gap: PropTypes.number,
  axis: PropTypes.number,
  curve: PropTypes.number,
  fade: PropTypes.number,
  children: PropTypes.node,
  className: PropTypes.string,
  style: PropTypes.object,
}
