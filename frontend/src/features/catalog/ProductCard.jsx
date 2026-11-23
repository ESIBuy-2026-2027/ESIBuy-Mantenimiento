
import PropTypes from 'prop-types'
import { useEffect, useRef, useState } from 'react'
import { Link } from 'react-router-dom'
import { Button, Text, makeStyles, tokens } from '@fluentui/react-components'
import { Heart20Filled, Heart20Regular } from '@fluentui/react-icons'
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
  error: {
    color: tokens.colorPaletteRedForeground1,
  },
  favorite: {
    position: 'absolute',
    top: tokens.spacingVerticalS,
    right: tokens.spacingHorizontalS,
    backgroundColor: tokens.colorNeutralBackground1,
    boxShadow: tokens.shadow4,
  },
  // El corazón relleno en rojo es la señal de que ya está en favoritos, también mientras se quita.
  favoriteIcon: {
    color: tokens.colorPaletteRedForeground1,
  },
  srOnly: {
    position: 'absolute',
    width: '1px',
    height: '1px',
    padding: 0,
    margin: '-1px',
    overflow: 'hidden',
    clip: 'rect(0, 0, 0, 0)',
    whiteSpace: 'nowrap',
    border: 0,
  },
  placeholderImage: {
    width: '24px',
    height: '24px',
    objectFit: 'contain',
  },
})

// Tarjeta de un producto del catálogo: lo que se ve desde fuera (imagen, nombre, categoría, valoración y precio) y
// el botón para añadirlo al carrito (HU9). `categoryName` lo traduce la página a partir del identificador de
// categoría del producto. La imagen, el nombre y la valoración llevan al detalle (HU8). Con `onAddFavorite`, la
// tarjeta tiene además un corazón para añadir el producto a favoritos (HU14), salvo si no está disponible, porque el
// backend no lo aceptaría; uno agotado sí se puede añadir. Con `favorite`, el corazón sale relleno y, con
// `onRemoveFavorite`, al pulsarlo se quita de favoritos. Si no se puede añadir o quitar, la tarjeta lo avisa.
export default function ProductCard({
  product,
  categoryName,
  favorite = false,
  onAddFavorite,
  onRemoveFavorite,
}) {
  const styles = useStyles()

  // Si la imagen no carga (dirección rota), se enseña el mismo marcador que cuando no hay imagen.
  const [imageFailed, setImageFailed] = useState(false)
  const imageRef = useRef(null)
  const hasImage = Boolean(product.imagen) && !imageFailed
  const rated = product.valoracion !== null && product.numValoraciones > 0

  useEffect(() => {
    const image = imageRef.current
    if (!image) return undefined

    const handleError = () => setImageFailed(true)
    image.addEventListener('error', handleError)
    if (image.complete && image.naturalWidth === 0) handleError()

    return () => image.removeEventListener('error', handleError)
  }, [])

  const [savingFavorite, setSavingFavorite] = useState(false)
  const [favoriteError, setFavoriteError] = useState('')

  const favoriteFailure = (failure) => {
    if (favorite) return 'No se ha podido quitar de favoritos. Inténtalo de nuevo.'

    return failure?.code === 'PRODUCT_NOT_FOUND'
      ? 'Este producto ya no está disponible.'
      : 'No se ha podido añadir a favoritos. Inténtalo de nuevo.'
  }

  const toggleFavorite = async () => {
    setFavoriteError('')
    setSavingFavorite(true)

    try {
      await (favorite ? onRemoveFavorite(product.id) : onAddFavorite(product.id))
    } catch (failure) {
      setFavoriteError(favoriteFailure(failure))
    } finally {
      setSavingFavorite(false)
    }
  }

  return (
    <div className={styles.card}>
      <Link to={getProductDetailPath(product.id)} className={styles.link}>
        <span className={styles.media}>
          {hasImage ? (
            <img
              ref={imageRef}
              className={styles.image}
              src={product.imagen}
              alt={product.nombre}
              loading="lazy"
            />
          ) : (
            <span className={styles.placeholder}>
              <img
                className={styles.placeholderImage}
                src="data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 24 24'%3E%3Cpath d='M3 3l18 18M10 12l-4 6h12l-3-4-2 2M6 6h11a2 2 0 0 1 2 2v8' fill='none' stroke='%23999999' stroke-width='1.5' stroke-linecap='round' stroke-linejoin='round'/%3E%3C/svg%3E"
                alt="Sin imagen"
              />
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
                <span>
                  <img
                    className={styles.srOnly}
                    src="data:image/gif;base64,R0lGODlhAQABAAD/ACwAAAAAAQABAAACADs="
                    alt={`Valoración: ${formatRating(product.valoracion)} de 5`}
                    aria-label={`Valoración: ${formatRating(product.valoracion)} de 5`}
                  />
                  <span aria-hidden="true">
                    <RatingStars value={product.valoracion} />
                  </span>
                </span>
                <Text weight="semibold">
                  {formatRating(product.valoracion)}
                </Text>

                <Text className={styles.muted}>
                  ({product.numValoraciones})
                </Text>
              </>
            ) : (
              <Text className={styles.muted}>Sin valoraciones</Text>
            )}
          </span>
        </span>
      </Link>

      {onAddFavorite && product.disponible && (
        <Button
          as="button"
          shape="circular"
          className={styles.favorite}
          icon={favorite ? <Heart20Filled className={styles.favoriteIcon} /> : <Heart20Regular />}
          aria-label={
            favorite
              ? `Quitar ${product.nombre} de favoritos`
              : `Añadir ${product.nombre} a favoritos`
          }
          title={favorite ? 'Quitar de favoritos' : 'Añadir a favoritos'}
          disabled={savingFavorite || (favorite && !onRemoveFavorite)}
          onClick={toggleFavorite}
        />
      )}

      <div className={styles.footer}>
        <PriceBlock
          compact
          precio={product.precio}
          descuento={product.descuento}
          precioFinal={product.precioFinal}
        />

        <AddToCartButton product={product} size="small" fullWidth />

        {favoriteError && (
          <Text role="alert" size={200} className={styles.error}>
            {favoriteError}
          </Text>
        )}
      </div>
    </div>
  )
}

ProductCard.propTypes = {
  product: catalogProductPropType.isRequired,
  categoryName: PropTypes.string,
  favorite: PropTypes.bool,
  onAddFavorite: PropTypes.func,
  onRemoveFavorite: PropTypes.func,
}
