import PropTypes from 'prop-types'
import { useState } from 'react'
import { Link } from 'react-router-dom'
import { Button, Text, makeStyles, tokens } from '@fluentui/react-components'
import { Heart20Filled, Heart20Regular, ImageOff24Regular } from '@fluentui/react-icons'
import { getProductDetailPath } from '../../routes/paths'
import { catalogProductPropType } from '../../utils/propTypes'
import { formatRating } from '../../utils/format'
import AddToCartButton from '../cart/AddToCartButton'
import PriceBlock from './PriceBlock'
import RatingStars from './RatingStars'

const SIN_CATEGORIA = '—'

const useStyles = makeStyles({
  // La tarjeta es un contenedor con dos partes: el enlace al detalle del producto (imagen, categoría, nombre y
  // valoración) y, debajo, el precio y el botón de añadir al carrito, que no puede ir dentro del enlace. El corazón de
  // favoritos tampoco: se coloca encima de la esquina de la imagen.
  card: {
    position: 'relative',
    display: 'flex',
    flexDirection: 'column',
    width: '100%',
    height: '100%',
    overflow: 'hidden',
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
  },
  link: {
    textDecoration: 'none',
    display: 'flex',
    flexDirection: 'column',
    flexGrow: 1,
    font: 'inherit',
    color: 'inherit',
    cursor: 'pointer',
    ':focus-visible': {
      outline: `2px solid ${tokens.colorStrokeFocus2}`,
      outlineOffset: '-2px',
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
    padding: `${tokens.spacingVerticalM} ${tokens.spacingHorizontalM} ${tokens.spacingVerticalXS}`,
    '@media (min-width: 600px)': {
      padding: `${tokens.spacingVerticalL} ${tokens.spacingHorizontalL} ${tokens.spacingVerticalS}`,
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
  footer: {
    display: 'flex',
    flexDirection: 'column',
    gap: tokens.spacingVerticalS,
    padding: `${tokens.spacingVerticalXS} ${tokens.spacingHorizontalM} ${tokens.spacingVerticalM}`,
    '@media (min-width: 600px)': {
      padding: `${tokens.spacingVerticalXS} ${tokens.spacingHorizontalL} ${tokens.spacingVerticalL}`,
    },
  },
  muted: {
    color: tokens.colorNeutralForeground3,
  },
  favorite: {
    position: 'absolute',
    top: tokens.spacingVerticalS,
    right: tokens.spacingHorizontalS,
    backgroundColor: tokens.colorNeutralBackground1,
    boxShadow: tokens.shadow4,
  },
})

// Tarjeta de un producto del catálogo: lo que se ve desde fuera (imagen, nombre, categoría, valoración y precio) y
// el botón para añadirlo al carrito (HU9). `categoryName` lo traduce la página a partir del identificador de
// categoría del producto. La imagen, el nombre y la valoración llevan al detalle (HU8). Con `onAddFavorite`, la
// tarjeta tiene además un corazón para añadir el producto a favoritos (HU14), salvo si no está disponible, porque el
// backend no lo aceptaría; uno agotado sí se puede añadir. Con `favorite`, el corazón sale relleno y deshabilitado:
// quitarlo de favoritos todavía no se puede desde aquí.
export default function ProductCard({ product, categoryName, favorite = false, onAddFavorite }) {
  const styles = useStyles()
  // Si la imagen no carga (dirección rota), se enseña el mismo marcador que cuando no hay imagen.
  const [imageFailed, setImageFailed] = useState(false)
  const hasImage = Boolean(product.imagen) && !imageFailed
  const rated = product.valoracion !== null && product.numValoraciones > 0
  const [addingFavorite, setAddingFavorite] = useState(false)

  const addFavorite = async () => {
    setAddingFavorite(true)
    try {
      await onAddFavorite(product.id)
    } finally {
      setAddingFavorite(false)
    }
  }

  return (
    <div className={styles.card}>
      <Link to={getProductDetailPath(product.id)} className={styles.link}>
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
      </Link>
      {onAddFavorite && product.disponible && (
        <Button
          shape="circular"
          className={styles.favorite}
          icon={favorite ? <Heart20Filled /> : <Heart20Regular />}
          aria-label={favorite ? `${product.nombre} está en tus favoritos` : `Añadir ${product.nombre} a favoritos`}
          title={favorite ? 'Ya está en tus favoritos' : 'Añadir a favoritos'}
          disabled={favorite || addingFavorite}
          onClick={addFavorite}
        />
      )}
      <div className={styles.footer}>
        <PriceBlock compact precio={product.precio} descuento={product.descuento} precioFinal={product.precioFinal} />
        <AddToCartButton product={product} size="small" fullWidth />
      </div>
    </div>
  )
}

ProductCard.propTypes = {
  product: catalogProductPropType.isRequired,
  categoryName: PropTypes.string,
  favorite: PropTypes.bool,
  onAddFavorite: PropTypes.func,
}
