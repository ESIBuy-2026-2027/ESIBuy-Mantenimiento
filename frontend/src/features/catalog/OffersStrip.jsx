import PropTypes from 'prop-types'
import { useCallback, useId, useState } from 'react'
import { Button, makeStyles, tokens } from '@fluentui/react-components'
import { Pause20Regular, Play20Regular } from '@fluentui/react-icons'
import { getCatalogProducts } from '../../api/products'
import { useApiQuery } from '../../hooks/useApiQuery'
import { ANCHO_TARJETA } from './CatalogCarousel'
import ProductCard from './ProductCard'

// Cuántos productos se piden para encontrar las ofertas, y cuántas ofertas hacen falta para que la cinta tenga sentido.
const PEDIDOS = 50
const MINIMO_OFERTAS = 3
// Para que la cinta no tenga huecos, cada mitad ocupa más que la pantalla más ancha (una tarjeta con su separación).
const ANCHO_PANTALLA_PX = 1600
const ANCHO_TARJETA_PX = 288

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
  viewport: {
    overflow: 'hidden',
    paddingTop: tokens.spacingVerticalXS,
    paddingBottom: tokens.spacingVerticalM,
    maskImage: 'linear-gradient(90deg, transparent, #000 4%, #000 96%, transparent)',
    '@media (prefers-reduced-motion: reduce)': {
      overflowX: 'auto',
      maskImage: 'none',
    },
  },
  row: {
    display: 'flex',
    gap: tokens.spacingHorizontalL,
    width: 'max-content',
    margin: 0,
    padding: 0,
    listStyleType: 'none',
    // Avanza hacia la derecha: la fila lleva dos mitades iguales y se desplaza media fila (más media separación).
    animationName: {
      from: { transform: 'translateX(calc(-50% - 8px))' },
      to: { transform: 'translateX(0)' },
    },
    animationDuration: '70s',
    animationTimingFunction: 'linear',
    animationIterationCount: 'infinite',
    '@media (prefers-reduced-motion: reduce)': {
      animationName: 'none',
    },
  },
  item: {
    flexShrink: 0,
    width: ANCHO_TARJETA,
    display: 'flex',
  },
})

// Las ofertas y los productos con descuento, en una cinta que avanza sola hacia la derecha (HU7). Se para al poner el
// ratón encima, al usar el teclado dentro de ella o con el botón de pausa; con «reducir movimiento» no se mueve.
// Solo se pinta si hay suficientes productos con descuento; si no cargan, el catálogo sigue sin ella.
export default function OffersStrip({ categoryNames, favoritos, onAddFavorite, onRemoveFavorite }) {
  const styles = useStyles()
  const headingId = useId()
  const fetcher = useCallback(() => getCatalogProducts({ sort: 'nombre', order: 'asc', page: 1, size: PEDIDOS }), [])
  const { data } = useApiQuery(fetcher)
  const [pausedByUser, setPausedByUser] = useState(false)
  const [pointerInside, setPointerInside] = useState(false)
  const [focusInside, setFocusInside] = useState(false)

  const offers = (data?.items ?? []).filter((product) => product.descuento)

  if (offers.length < MINIMO_OFERTAS) return null

  const paused = pausedByUser || pointerInside || focusInside
  const copies = Math.max(1, Math.ceil(ANCHO_PANTALLA_PX / (offers.length * ANCHO_TARJETA_PX)))
  // Dos mitades iguales. Solo la primera copia de las ofertas se puede usar con el teclado y los lectores de pantalla;
  // el resto repite lo mismo para que la cinta no tenga huecos.
  const cards = [0, 1].flatMap((half) =>
    Array.from({ length: copies }, (_, copy) => offers.map((product) => ({ product, copy, half }))).flat(),
  )

  return (
    <section aria-labelledby={headingId} className={styles.section}>
      <div className={styles.head}>
        <h2 id={headingId} className={styles.title}>
          Ofertas
        </h2>
        <Button
          appearance="subtle"
          icon={pausedByUser ? <Play20Regular /> : <Pause20Regular />}
          aria-pressed={pausedByUser}
          onClick={() => setPausedByUser((value) => !value)}
        >
          {pausedByUser ? 'Reanudar' : 'Pausar'}
        </Button>
      </div>

      <div
        className={styles.viewport}
        onPointerEnter={() => setPointerInside(true)}
        onPointerLeave={() => setPointerInside(false)}
        onFocus={() => setFocusInside(true)}
        onBlur={() => setFocusInside(false)}
      >
        <ul
          aria-label="Productos en oferta"
          className={styles.row}
          style={{ animationPlayState: paused ? 'paused' : 'running' }}
        >
          {cards.map(({ product, copy, half }) => {
            const repeated = half > 0 || copy > 0
            return (
              <li
                key={`${half}-${copy}-${product.id}`}
                className={styles.item}
                aria-hidden={repeated ? true : undefined}
                inert={repeated}
              >
                <ProductCard
                  product={product}
                  categoryName={categoryNames.get(product.categoriaId)}
                  favorite={favoritos.has(product.id)}
                  onAddFavorite={onAddFavorite}
                  onRemoveFavorite={onRemoveFavorite}
                />
              </li>
            )
          })}
        </ul>
      </div>
    </section>
  )
}

OffersStrip.propTypes = {
  categoryNames: PropTypes.instanceOf(Map).isRequired,
  favoritos: PropTypes.instanceOf(Set).isRequired,
  onAddFavorite: PropTypes.func.isRequired,
  onRemoveFavorite: PropTypes.func.isRequired,
}
