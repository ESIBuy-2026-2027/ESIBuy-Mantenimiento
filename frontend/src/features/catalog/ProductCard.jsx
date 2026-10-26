import PropTypes from 'prop-types'
import { useState } from 'react'
import { Badge, Card, CardPreview, Text, makeStyles, tokens } from '@fluentui/react-components'
import { ImageOff24Regular } from '@fluentui/react-icons'
import { catalogProductPropType } from '../../utils/propTypes'
import { formatRating } from '../../utils/format'
import RatingStars from './RatingStars'

const SIN_CATEGORIA = '—'

const useStyles = makeStyles({
  card: {
    height: '100%',
    transitionProperty: 'box-shadow, transform',
    transitionDuration: tokens.durationNormal,
    ':hover': {
      boxShadow: tokens.shadow16,
      transform: 'translateY(-2px)',
    },
  },
  preview: {
    aspectRatio: '16 / 10',
    overflow: 'hidden',
    backgroundColor: tokens.colorNeutralBackground3,
  },
  image: {
    display: 'block',
    width: '100%',
    height: '100%',
    objectFit: 'cover',
    transitionProperty: 'transform',
    transitionDuration: tokens.durationSlow,
    ':hover': {
      transform: 'scale(1.06)',
    },
  },
  placeholder: {
    display: 'flex',
    flexDirection: 'column',
    alignItems: 'center',
    justifyContent: 'center',
    gap: tokens.spacingVerticalXS,
    width: '100%',
    height: '100%',
    color: tokens.colorNeutralForeground4,
  },
  body: {
    display: 'flex',
    flexDirection: 'column',
    gap: tokens.spacingVerticalS,
    padding: `0 ${tokens.spacingHorizontalM} ${tokens.spacingVerticalM}`,
  },
  name: {
    display: '-webkit-box',
    WebkitLineClamp: 2,
    WebkitBoxOrient: 'vertical',
    overflow: 'hidden',
    overflowWrap: 'anywhere',
  },
  category: {
    alignSelf: 'flex-start',
  },
  rating: {
    display: 'flex',
    alignItems: 'center',
    gap: tokens.spacingHorizontalXS,
    marginTop: 'auto',
  },
  muted: {
    color: tokens.colorNeutralForeground3,
  },
})

// Tarjeta de un producto del catálogo: solo lo que se ve desde fuera (imagen, nombre, categoría y valoración).
// `categoryName` lo traduce la página a partir del identificador de categoría del producto.
export default function ProductCard({ product, categoryName }) {
  const styles = useStyles()
  // Si la imagen no carga (dirección rota), se enseña el mismo marcador que cuando no hay imagen.
  const [imageFailed, setImageFailed] = useState(false)
  const hasImage = Boolean(product.imagen) && !imageFailed
  const rated = product.valoracion !== null && product.numValoraciones > 0

  return (
    <Card className={styles.card}>
      <CardPreview className={styles.preview}>
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
      </CardPreview>
      <div className={styles.body}>
        <Text weight="semibold" size={400} className={styles.name}>
          {product.nombre}
        </Text>
        <Badge className={styles.category} appearance="outline" color="brand" size="medium">
          {categoryName ?? SIN_CATEGORIA}
        </Badge>
        <div className={styles.rating}>
          {rated ? (
            <>
              <RatingStars value={product.valoracion} />
              <Text weight="semibold">{formatRating(product.valoracion)}</Text>
              <Text className={styles.muted}>({product.numValoraciones})</Text>
            </>
          ) : (
            <Text className={styles.muted}>Sin valoraciones</Text>
          )}
        </div>
      </div>
    </Card>
  )
}

ProductCard.propTypes = {
  product: catalogProductPropType.isRequired,
  categoryName: PropTypes.string,
}
