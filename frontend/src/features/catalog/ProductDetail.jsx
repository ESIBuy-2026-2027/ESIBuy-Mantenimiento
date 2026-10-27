import PropTypes from 'prop-types'
import { useState } from 'react'
import { Text, Title1, makeStyles, mergeClasses, tokens } from '@fluentui/react-components'
import { BuildingShop20Regular, ImageOff24Regular } from '@fluentui/react-icons'
import { formatRating } from '../../utils/format'
import { productDetailPropType } from '../../utils/propTypes'
import PriceBlock from './PriceBlock'
import RatingStars from './RatingStars'
import { stockStatus } from './stockStatus'

const SIN_CATEGORIA = '—'
const SIN_DESCRIPCION = 'Este producto no tiene descripción.'

const aparece = {
  animationName: {
    from: { opacity: 0, transform: 'translateY(14px)' },
    to: { opacity: 1, transform: 'translateY(0)' },
  },
  animationDuration: tokens.durationSlow,
  animationTimingFunction: tokens.curveDecelerateMid,
  animationFillMode: 'both',
  '@media (prefers-reduced-motion: reduce)': {
    animationName: 'none',
  },
}

const useStyles = makeStyles({
  layout: {
    display: 'grid',
    gridTemplateColumns: 'minmax(0, 1fr)',
    gap: tokens.spacingVerticalXXL,
    alignItems: 'start',
    '@media (min-width: 860px)': {
      gridTemplateColumns: 'minmax(0, 1.1fr) minmax(0, 1fr)',
      gap: tokens.spacingHorizontalXXXL,
    },
  },
  // En pantallas anchas la imagen se queda a la vista mientras se lee el resto.
  media: {
    ...aparece,
    position: 'relative',
    aspectRatio: '4 / 3',
    overflow: 'hidden',
    backgroundColor: tokens.colorNeutralBackground3,
    border: `1px solid ${tokens.colorNeutralStroke2}`,
    borderRadius: tokens.borderRadiusXXLarge,
    boxShadow: tokens.shadow16,
    '@media (min-width: 860px)': {
      position: 'sticky',
      top: tokens.spacingVerticalXXL,
    },
  },
  image: {
    display: 'block',
    width: '100%',
    height: '100%',
    objectFit: 'cover',
    transitionProperty: 'transform',
    transitionDuration: tokens.durationUltraSlow,
    transitionTimingFunction: tokens.curveEasyEase,
    ':hover': {
      transform: 'scale(1.05)',
    },
  },
  placeholder: {
    display: 'flex',
    alignItems: 'center',
    justifyContent: 'center',
    width: '100%',
    height: '100%',
    color: tokens.colorNeutralForeground4,
  },
  info: {
    display: 'flex',
    flexDirection: 'column',
    gap: tokens.spacingVerticalL,
    minWidth: 0,
  },
  header: {
    ...aparece,
    animationDelay: '80ms',
    display: 'flex',
    flexDirection: 'column',
    gap: tokens.spacingVerticalS,
  },
  category: {
    color: tokens.colorBrandForeground1,
    textTransform: 'uppercase',
    letterSpacing: '0.06em',
    fontWeight: tokens.fontWeightSemibold,
  },
  title: {
    margin: 0,
    overflowWrap: 'anywhere',
  },
  vendor: {
    display: 'flex',
    alignItems: 'center',
    gap: tokens.spacingHorizontalS,
    color: tokens.colorNeutralForeground2,
  },
  rating: {
    display: 'flex',
    flexWrap: 'wrap',
    alignItems: 'center',
    gap: tokens.spacingHorizontalS,
  },
  muted: {
    color: tokens.colorNeutralForeground3,
  },
  panel: {
    ...aparece,
    animationDelay: '160ms',
    display: 'flex',
    flexDirection: 'column',
    gap: tokens.spacingVerticalM,
    padding: tokens.spacingHorizontalXL,
    backgroundColor: tokens.colorNeutralBackground1,
    border: `1px solid ${tokens.colorNeutralStroke2}`,
    borderRadius: tokens.borderRadiusXLarge,
    boxShadow: tokens.shadow4,
  },
  stock: {
    display: 'inline-flex',
    alignItems: 'center',
    gap: tokens.spacingHorizontalS,
    fontWeight: tokens.fontWeightSemibold,
  },
  dot: {
    width: '10px',
    height: '10px',
    borderRadius: tokens.borderRadiusCircular,
  },
  success: {
    backgroundColor: tokens.colorPaletteGreenBackground3,
  },
  warning: {
    backgroundColor: tokens.colorPaletteMarigoldBackground3,
  },
  danger: {
    backgroundColor: tokens.colorPaletteRedBackground3,
  },
  subtle: {
    backgroundColor: tokens.colorNeutralForeground4,
  },
  section: {
    ...aparece,
    animationDelay: '240ms',
    display: 'flex',
    flexDirection: 'column',
    gap: tokens.spacingVerticalS,
  },
  sectionTitle: {
    margin: 0,
    fontSize: tokens.fontSizeBase400,
    fontWeight: tokens.fontWeightSemibold,
  },
  description: {
    whiteSpace: 'pre-line',
    overflowWrap: 'anywhere',
    lineHeight: tokens.lineHeightBase400,
    color: tokens.colorNeutralForeground2,
  },
  facts: {
    display: 'grid',
    gridTemplateColumns: 'auto minmax(0, 1fr)',
    columnGap: tokens.spacingHorizontalXL,
    rowGap: tokens.spacingVerticalS,
    margin: 0,
  },
  term: {
    color: tokens.colorNeutralForeground3,
  },
  detail: {
    margin: 0,
    fontWeight: tokens.fontWeightSemibold,
    overflowWrap: 'anywhere',
  },
  summary: {
    display: 'flex',
    alignItems: 'center',
    gap: tokens.spacingHorizontalXL,
  },
  average: {
    fontSize: tokens.fontSizeHero900,
    lineHeight: tokens.lineHeightHero900,
    fontWeight: tokens.fontWeightBold,
  },
  summaryRating: {
    display: 'flex',
    flexDirection: 'column',
    gap: tokens.spacingVerticalXXS,
  },
})

const valoraciones = (n) => `${n} ${n === 1 ? 'valoración' : 'valoraciones'}`

// Detalle de un producto para el cliente (HU8): imagen, nombre, proveedor, valoración, precio, disponibilidad,
// descripción y una ficha con los datos del producto. Solo presenta: la página es la que pide el producto.
export default function ProductDetail({ product, categoryName }) {
  const styles = useStyles()
  // Si la imagen no carga (dirección rota), se enseña el mismo marcador que cuando no hay imagen.
  const [imageFailed, setImageFailed] = useState(false)
  const hasImage = Boolean(product.imagen) && !imageFailed
  const rated = product.valoracion !== null && product.numValoraciones > 0
  const status = stockStatus(product)
  const category = categoryName ?? SIN_CATEGORIA

  return (
    <div className={styles.layout}>
      <div className={styles.media}>
        {hasImage ? (
          <img className={styles.image} src={product.imagen} alt={product.nombre} onError={() => setImageFailed(true)} />
        ) : (
          <span className={styles.placeholder} role="img" aria-label="Sin imagen">
            <ImageOff24Regular fontSize={48} />
          </span>
        )}
      </div>

      <div className={styles.info}>
        <div className={styles.header}>
          <Text size={200} className={styles.category}>
            {category}
          </Text>
          <Title1 as="h1" className={styles.title}>
            {product.nombre}
          </Title1>
          <Text className={styles.vendor}>
            <BuildingShop20Regular />
            <span>
              Vendido por <strong>{product.vendedor.nombreComercial}</strong>
            </span>
          </Text>
          <div className={styles.rating}>
            {rated ? (
              <>
                <RatingStars value={product.valoracion} size={22} />
                <Text weight="semibold">{formatRating(product.valoracion)}</Text>
                <Text className={styles.muted}>{valoraciones(product.numValoraciones)}</Text>
              </>
            ) : (
              <Text className={styles.muted}>Sin valoraciones</Text>
            )}
          </div>
        </div>

        <div className={styles.panel}>
          <PriceBlock precio={product.precio} descuento={product.descuento} precioFinal={product.precioFinal} />
          <span className={styles.stock}>
            <span className={mergeClasses(styles.dot, styles[status.tone])} aria-hidden="true" />
            {status.label}
          </span>
        </div>

        <section className={styles.section} aria-labelledby="descripcion-producto">
          <h2 id="descripcion-producto" className={styles.sectionTitle}>
            Descripción
          </h2>
          <Text className={styles.description}>{product.descripcion || SIN_DESCRIPCION}</Text>
        </section>

        <section className={styles.section} aria-labelledby="informacion-producto">
          <h2 id="informacion-producto" className={styles.sectionTitle}>
            Información del producto
          </h2>
          <dl className={styles.facts}>
            <dt className={styles.term}>Categoría</dt>
            <dd className={styles.detail}>{category}</dd>
            <dt className={styles.term}>Proveedor</dt>
            <dd className={styles.detail}>{product.vendedor.nombreComercial}</dd>
            <dt className={styles.term}>Disponibilidad</dt>
            <dd className={styles.detail}>{status.label}</dd>
            <dt className={styles.term}>Referencia</dt>
            <dd className={styles.detail}>{product.id}</dd>
          </dl>
        </section>

        {rated && (
          <section className={styles.section} aria-labelledby="valoracion-producto">
            <h2 id="valoracion-producto" className={styles.sectionTitle}>
              Valoración de los clientes
            </h2>
            <div className={styles.summary}>
              <span className={styles.average}>{formatRating(product.valoracion)}</span>
              <div className={styles.summaryRating}>
                <RatingStars value={product.valoracion} size={24} />
                <Text className={styles.muted}>Basada en {valoraciones(product.numValoraciones)}</Text>
              </div>
            </div>
          </section>
        )}
      </div>
    </div>
  )
}

ProductDetail.propTypes = {
  product: productDetailPropType.isRequired,
  categoryName: PropTypes.string,
}
