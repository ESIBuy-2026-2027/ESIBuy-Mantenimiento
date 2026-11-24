import PropTypes from 'prop-types'
import { useCallback, useEffect, useId, useRef, useState } from 'react'
import { Button, makeStyles, tokens } from '@fluentui/react-components'
import { ArrowLeft20Regular, ArrowRight20Regular } from '@fluentui/react-icons'

// A qué distancia del final (en píxeles) se piden más productos.
const MARGEN_FINAL_PX = 320

export const ANCHO_TARJETA = 'min(272px, 78vw)'

const useStyles = makeStyles({
  section: {
    display: 'flex',
    flexDirection: 'column',
    gap: tokens.spacingVerticalM,
    width: '100%',
    boxSizing: 'border-box',
    margin: '0 auto',
    maxWidth: '1120px',
    padding: `0 ${tokens.spacingHorizontalL}`,
  },
  head: {
    display: 'flex',
    alignItems: 'center',
    justifyContent: 'space-between',
    gap: tokens.spacingHorizontalM,
  },
  title: {
    margin: 0,
    fontSize: tokens.fontSizeBase600,
    lineHeight: tokens.lineHeightBase600,
    fontWeight: tokens.fontWeightSemibold,
  },
  tools: {
    display: 'flex',
    gap: tokens.spacingHorizontalS,
  },
  track: {
    display: 'flex',
    gap: tokens.spacingHorizontalL,
    margin: 0,
    padding: `${tokens.spacingVerticalXS} ${tokens.spacingHorizontalXS} ${tokens.spacingVerticalM}`,
    listStyleType: 'none',
    overflowX: 'auto',
    scrollSnapType: 'x mandatory',
    scrollbarWidth: 'none',
    overscrollBehaviorX: 'contain',
    '::-webkit-scrollbar': { display: 'none' },
  },
  dragging: {
    scrollSnapType: 'none',
    cursor: 'grabbing',
    userSelect: 'none',
  },
  item: {
    flexShrink: 0,
    width: ANCHO_TARJETA,
    scrollSnapAlign: 'start',
    display: 'flex',
    // Cada tarjeta entra deslizándose desde la derecha, una detrás de otra.
    animationName: {
      from: { opacity: 0, transform: 'translateX(48px)' },
      to: { opacity: 1, transform: 'translateX(0)' },
    },
    animationDuration: tokens.durationUltraSlow,
    animationTimingFunction: tokens.curveDecelerateMid,
    animationFillMode: 'both',
    '@media (prefers-reduced-motion: reduce)': {
      animationName: 'none',
    },
  },
  more: {
    flexShrink: 0,
    display: 'flex',
    alignItems: 'center',
    scrollSnapAlign: 'start',
  },
  progress: {
    height: '4px',
    borderRadius: tokens.borderRadiusSmall,
    backgroundColor: tokens.colorNeutralBackground5,
    overflow: 'hidden',
    margin: `0 ${tokens.spacingHorizontalXS}`,
  },
  progressBar: {
    height: '100%',
    borderRadius: tokens.borderRadiusSmall,
    backgroundColor: tokens.colorBrandBackground,
    transitionProperty: 'width, margin-left',
    transitionDuration: tokens.durationFast,
    transitionTimingFunction: 'linear',
  },
})

// Fila de productos que se desliza en horizontal (HU7): los productos entran deslizándose desde la derecha y se mueven
// con los botones de flecha, arrastrando con el ratón o con el dedo, y cada tarjeta se detiene bien
// encajada. Los hijos son las tarjetas; con `hasMore`, al final hay un botón para cargar más y se piden más solos al
// llegar cerca del final.
export default function CatalogCarousel({ title, hasMore = false, loadingMore = false, onLoadMore, children }) {
  const styles = useStyles()
  const headingId = useId()
  const track = useRef(null)
  const bar = useRef(null)
  const [edges, setEdges] = useState({ start: true, end: true })

  const update = useCallback(() => {
    const element = track.current
    if (!element) return
    const max = element.scrollWidth - element.clientWidth
    setEdges({ start: element.scrollLeft <= 2, end: element.scrollLeft >= max - 2 })

    if (bar.current) {
      const visible = Math.min(element.clientWidth / (element.scrollWidth || 1), 1)
      bar.current.style.width = `${visible * 100}%`
      bar.current.style.marginLeft = `${max > 0 ? (element.scrollLeft / max) * (1 - visible) * 100 : 0}%`
    }

    if (hasMore && !loadingMore && element.scrollLeft + element.clientWidth >= element.scrollWidth - MARGEN_FINAL_PX) {
      onLoadMore?.()
    }
  }, [hasMore, loadingMore, onLoadMore])

  useEffect(() => {
    update()
    window.addEventListener('resize', update)
    return () => window.removeEventListener('resize', update)
  }, [update, children])

  const move = (direction) => {
    const element = track.current
    const left = direction * Math.max(element.clientWidth * 0.8, 240)
    if (typeof element.scrollBy === 'function') {
      element.scrollBy({ left, behavior: 'smooth' })
    } else {
      element.scrollLeft += left
    }
  }

  // Arrastre con el ratón; con el dedo la fila ya se desliza sola
  const drag = useRef(null)
  const [dragging, setDragging] = useState(false)

  const handlePointerDown = (event) => {
    if (event.pointerType !== 'mouse' || event.target.closest('button, a, input, output')) return
    drag.current = { x: event.clientX, left: track.current.scrollLeft }
    setDragging(true)
  }

  useEffect(() => {
    if (!dragging) return undefined
    const handleMove = (event) => {
      track.current.scrollLeft = drag.current.left - (event.clientX - drag.current.x)
    }
    const handleUp = () => setDragging(false)
    window.addEventListener('pointermove', handleMove)
    window.addEventListener('pointerup', handleUp)
    return () => {
      window.removeEventListener('pointermove', handleMove)
      window.removeEventListener('pointerup', handleUp)
    }
  }, [dragging])

  const items = Array.isArray(children) ? children : [children]

  return (
    <section aria-labelledby={headingId} className={styles.section}>
      <div className={styles.head}>
        <h2 id={headingId} className={styles.title}>
          {title}
        </h2>
        <div className={styles.tools}>
          <Button
            shape="circular"
            icon={<ArrowLeft20Regular />}
            aria-label={`Productos anteriores de ${title}`}
            disabled={edges.start}
            onClick={() => move(-1)}
          />
          <Button
            shape="circular"
            icon={<ArrowRight20Regular />}
            aria-label={`Más productos de ${title}`}
            disabled={edges.end && !hasMore}
            onClick={() => move(1)}
          />
        </div>
      </div>

      <ul
        ref={track}
        aria-label={`Productos de ${title}`}
        className={dragging ? `${styles.track} ${styles.dragging}` : styles.track}
        onScroll={update}
        onPointerDown={handlePointerDown}
      >
        {items.map((child, index) => (
          <li key={child.key ?? index} className={styles.item} style={{ animationDelay: `${(index % 10) * 70}ms` }}>
            {child}
          </li>
        ))}
        {hasMore && (
          <li className={styles.more}>
            <Button appearance="subtle" disabled={loadingMore} onClick={onLoadMore}>
              {loadingMore ? 'Cargando…' : `Ver más de ${title}`}
            </Button>
          </li>
        )}
      </ul>

      <div className={styles.progress} aria-hidden="true">
        <div ref={bar} className={styles.progressBar} />
      </div>
    </section>
  )
}

CatalogCarousel.propTypes = {
  title: PropTypes.string.isRequired,
  hasMore: PropTypes.bool,
  loadingMore: PropTypes.bool,
  onLoadMore: PropTypes.func,
  children: PropTypes.node,
}
