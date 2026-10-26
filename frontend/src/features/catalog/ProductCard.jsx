import PropTypes from 'prop-types'
import { useState } from 'react'
import { Text, makeStyles, tokens } from '@fluentui/react-components'
import { ImageOff24Regular } from '@fluentui/react-icons'
import { catalogProductPropType } from '../../utils/propTypes'
import { formatRating } from '../../utils/format'
import RatingStars from './RatingStars'

const SIN_CATEGORIA = '—'

const useStyles = makeStyles({
  // Toda la tarjeta es un botón: se puede pulsar, o activar con el teclado, para abrir la ficha del producto.
  card: {
    display: 'flex',
    flexDirection: 'column',
    width: '100%',
    height: '100%',
    margin: 0,
    padding: 0,
    overflow: 'hidden',
    textAlign: 'left',
    font: 'inherit',
    color: 'inherit',
    cursor: 'pointer',
    backgroundColor: tokens.colorNeutralBackground1,
    border: `1px solid ${tokens.colorNeutralStroke2}`,
    borderRadius: tokens.borderRadiusXLarge,
    boxShadow: tokens.shadow2,
    transitionProperty: 'box-shadow, transform, border-color',
    transitionDuration: tokens.durationNormal,
    transitionTimingFunction: tokens.curveEasyEase,
    ':hover': {
      transform: 'translateY(-3px)',
      boxShadow: tokens.shadow16,
      borderColor: tokens.colorNeutralStroke1,
    },
    ':active': {
      transform: 'translateY(-1px)',
    },
    ':focus-visible': {
      outline: `2px solid ${tokens.colorStrokeFocus2}`,
      outlineOffset: '2px',
    },
  },
  media: {
    position: 'relative',
    display: 'block',
    aspectRatio: '4 / 3',
    overflow: 'hidden',
    backgroundColor: tokens.colorNeutralBackground3,
  },
  image: {
    display: 'block',
    width: '100%',
    height: '100%',
    objectFit: 'cover',
    transitionProperty: 'transform',
    transitionDuration: tokens.durationUltraSlow,
    transitionTimingFunction: tokens.curveEasyEase,
  },
  placeholder: {
    display: 'flex',
    alignItems: 'center',
    justifyContent: 'center',
    width: '100%',
    height: '100%',
    color: tokens.colorNeutralForeground4,
  },
  body: {
    display: 'flex',
    flexDirection: 'column',
    flexGrow: 1,
    gap: tokens.spacingVerticalXS,
    padding: `${tokens.spacingVerticalM} ${tokens.spacingHorizontalM} ${tokens.spacingVerticalM}`,
    '@media (min-width: 600px)': {
      padding: `${tokens.spacingVerticalL} ${tokens.spacingHorizontalL}`,
    },
  },
  category: {
    color: tokens.colorBrandForeground1,
    textTransform: 'uppercase',
    letterSpacing: '0.04em',
    fontWeight: tokens.fontWeightSemibold,
  },
  name: {
    display: '-webkit-box',
    WebkitLineClamp: 2,
    WebkitBoxOrient: 'vertical',
    overflow: 'hidden',
    overflowWrap: 'anywhere',
    lineHeight: tokens.lineHeightBase300,
    minHeight: `calc(2 * ${tokens.lineHeightBase300})`,
  },
  rating: {
    display: 'flex',
    flexWrap: 'wrap',
    alignItems: 'center',
    gap: tokens.spacingHorizontalXS,
    marginTop: 'auto',
    paddingTop: tokens.spacingVerticalXS,
  },
  muted: {
    color: tokens.colorNeutralForeground3,
  },
})

// Tarjeta de un producto del catálogo: solo lo que se ve desde fuera (imagen, nombre, categoría y valoración).
// `categoryName` lo traduce la página a partir del identificador de categoría del producto; `onSelect` recibe el
// producto al pulsarla.
export default function ProductCard({ product, categoryName, onSelect }) {
  const styles = useStyles()
  // Si la imagen no carga (dirección rota), se enseña el mismo marcador que cuando no hay imagen.
  const [imageFailed, setImageFailed] = useState(false)
  const hasImage = Boolean(product.imagen) && !imageFailed
  const rated = product.valoracion !== null && product.numValoraciones > 0

  return (
    <button type="button" className={styles.card} onClick={() => onSelect?.(product)}>
      <span className={styles.media}>
        {hasImage ? (
          <img
            className={styles.image}
            src={product.imagen}
            alt={product.nombre}
            loading="lazy"
            onError={() => setImageFailed(true)}
          />
        ) : (
          <span className={styles.placeholder} role="img" aria-label="Sin imagen">
            <ImageOff24Regular />
          </span>
        )}
      </span>
      <span className={styles.body}>
        <Text size={200} className={styles.category}>
          {categoryName ?? SIN_CATEGORIA}
        </Text>
        <Text weight="semibold" size={400} className={styles.name}>
          {product.nombre}
        </Text>
        <span className={styles.rating}>
          {rated ? (
            <>
              <RatingStars value={product.valoracion} />
              <Text weight="semibold">{formatRating(product.valoracion)}</Text>
              <Text className={styles.muted}>({product.numValoraciones})</Text>
            </>
          ) : (
            <Text className={styles.muted}>Sin valoraciones</Text>
          )}
        </span>
      </span>
    </button>
  )
}

ProductCard.propTypes = {
  product: catalogProductPropType.isRequired,
  categoryName: PropTypes.string,
  onSelect: PropTypes.func,
}
