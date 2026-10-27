import PropTypes from 'prop-types'
import { Link, useNavigate } from 'react-router-dom'
import { Subtitle1, Text, makeStyles, mergeClasses, tokens } from '@fluentui/react-components'
import { Star16Filled, Trophy20Filled } from '@fluentui/react-icons'
import TiltedGridHero from '../../components/TiltedGridHero'
import { getProductDetailPath } from '../../routes/paths'
import { catalogProductPropType } from '../../utils/propTypes'
import { formatRating } from '../../utils/format'

const useStyles = makeStyles({
  // A todo lo ancho de la página: solo el título se queda en la columna de contenido.
  section: {
    display: 'flex',
    flexDirection: 'column',
    gap: tokens.spacingVerticalM,
    width: '100%',
  },
  header: {
    display: 'flex',
    flexDirection: 'column',
    gap: tokens.spacingVerticalXXS,
    width: '100%',
    maxWidth: '860px',
    boxSizing: 'border-box',
    margin: '0 auto',
    padding: `0 ${tokens.spacingHorizontalL}`,
  },
  title: {
    display: 'flex',
    alignItems: 'center',
    gap: tokens.spacingHorizontalS,
    margin: 0,
  },
  trophy: {
    color: tokens.colorPaletteMarigoldForeground2,
  },
  subtitle: {
    color: tokens.colorNeutralForeground3,
  },
  stage: {
    position: 'relative',
  },
  // Sin marco: la banda se difumina por los lados sobre el fondo de la página.
  band: {
    height: '260px',
    '@media (min-width: 600px)': {
      height: '320px',
    },
    '@media (min-width: 1100px)': {
      height: '380px',
    },
  },
  // La misma lista de productos, para el teclado y los lectores de pantalla: la banda se mueve y solo se puede
  // pulsar con el ratón. Está siempre en la página, pero solo se ve cuando se navega por ella con el teclado.
  list: {
    position: 'absolute',
    left: 0,
    right: 0,
    bottom: 0,
    display: 'flex',
    flexWrap: 'wrap',
    justifyContent: 'center',
    gap: tokens.spacingHorizontalS,
    margin: 0,
    padding: tokens.spacingHorizontalM,
    listStyleType: 'none',
    backgroundColor: tokens.colorNeutralBackground1,
    boxShadow: tokens.shadow16,
    opacity: 0,
    pointerEvents: 'none',
    transitionProperty: 'opacity',
    transitionDuration: tokens.durationFast,
    ':focus-within': {
      opacity: 1,
      pointerEvents: 'auto',
    },
  },
  button: {
    textDecoration: 'none',
    display: 'flex',
    alignItems: 'center',
    gap: tokens.spacingHorizontalS,
    padding: `${tokens.spacingVerticalXS} ${tokens.spacingHorizontalM}`,
    font: 'inherit',
    color: 'inherit',
    cursor: 'pointer',
    backgroundColor: tokens.colorNeutralBackground1,
    border: `1px solid ${tokens.colorNeutralStroke2}`,
    borderRadius: tokens.borderRadiusCircular,
    ':focus-visible': {
      outline: `2px solid ${tokens.colorStrokeFocus2}`,
      outlineOffset: '2px',
    },
  },
  rank: {
    display: 'inline-flex',
    alignItems: 'center',
    justifyContent: 'center',
    flexShrink: 0,
    width: '24px',
    height: '24px',
    borderRadius: tokens.borderRadiusCircular,
    color: tokens.colorNeutralForegroundOnBrand,
    backgroundColor: tokens.colorBrandBackground,
    fontWeight: tokens.fontWeightBold,
    fontSize: tokens.fontSizeBase200,
  },
  rating: {
    display: 'inline-flex',
    alignItems: 'center',
    gap: tokens.spacingHorizontalXXS,
    color: tokens.colorPaletteMarigoldForeground2,
  },
  // Lo que se dibuja sobre cada tarjeta de la animación. Todo en `em`: la banda fija el tamaño de letra según el
  // tamaño de la tarjeta.
  captionRank: {
    position: 'absolute',
    top: '0.7em',
    left: '0.7em',
    display: 'inline-flex',
    alignItems: 'center',
    justifyContent: 'center',
    minWidth: '2em',
    height: '2em',
    borderRadius: '1em',
    backgroundColor: tokens.colorBrandBackground,
    fontWeight: tokens.fontWeightBold,
    fontSize: '0.9em',
    boxShadow: tokens.shadow4,
  },
  captionFirst: {
    backgroundColor: tokens.colorPaletteMarigoldForeground2,
  },
  captionInfo: {
    position: 'absolute',
    left: 0,
    right: 0,
    bottom: 0,
    padding: '2.4em 0.8em 0.7em',
    backgroundImage: 'linear-gradient(to top, rgba(0, 0, 0, 0.72), rgba(0, 0, 0, 0))',
  },
  captionName: {
    display: '-webkit-box',
    WebkitLineClamp: 2,
    WebkitBoxOrient: 'vertical',
    overflow: 'hidden',
    overflowWrap: 'anywhere',
    fontWeight: tokens.fontWeightSemibold,
    lineHeight: 1.2,
  },
  captionRating: {
    display: 'flex',
    alignItems: 'center',
    gap: '0.3em',
    marginTop: '0.3em',
    fontSize: '0.9em',
    fontWeight: tokens.fontWeightSemibold,
  },
  star: {
    color: tokens.colorPaletteMarigoldForeground3,
  },
})

// El pie de cada tarjeta de la animación: su puesto, su nombre y su valoración.
function CardCaption({ rank, product, styles }) {
  return (
    <>
      <span className={mergeClasses(styles.captionRank, rank === 1 && styles.captionFirst)}>{rank}</span>
      <div className={styles.captionInfo}>
        <div className={styles.captionName}>{product.nombre}</div>
        <div className={styles.captionRating}>
          <Star16Filled className={styles.star} fontSize="1em" />
          {formatRating(product.valoracion)}
        </div>
      </div>
    </>
  )
}

// Los productos mejor valorados: una banda curva de tarjetas (a todo el ancho de la página) que se pueden pulsar
// con el ratón y, para el teclado y los lectores de pantalla, la lista numerada de los mismos productos. Todas llevan
// al detalle del producto (HU8). `products` llega ya ordenado de mejor a peor.
export default function FeaturedShowcase({ products }) {
  const styles = useStyles()
  const navigate = useNavigate()
  // Solo entran en la banda los que tienen imagen; el puesto es el de la clasificación completa.
  const cards = products
    .map((product, index) => ({ product, rank: index + 1 }))
    .filter(({ product }) => product.imagen)
    .map(({ product, rank }) => ({
      src: product.imagen,
      alt: product.nombre,
      caption: <CardCaption rank={rank} product={product} styles={styles} />,
      product,
    }))

  return (
    <section className={styles.section} aria-labelledby="mejor-valorados">
      <div className={styles.header}>
        <Subtitle1 as="h2" id="mejor-valorados" className={styles.title}>
          <Trophy20Filled className={styles.trophy} />
          Los {products.length} mejor valorados
        </Subtitle1>
        <Text className={styles.subtitle}>La selección de ESIBuy, con las mejores opiniones de los clientes.</Text>
      </div>

      <div className={styles.stage}>
        {cards.length > 0 && (
          <TiltedGridHero
            role="group"
            aria-label="Los mejor valorados en movimiento"
            className={styles.band}
            images={cards}
            onSelect={(card) => navigate(getProductDetailPath(card.product.id))}
            tileHeight={56}
            aspectRatio={4 / 3}
            gap={5}
            axis={50}
            curve={68}
            speed={4}
          />
        )}

        <ul className={styles.list} aria-label="Selección destacada">
          {products.map((product, index) => (
            <li key={product.id}>
              <Link to={getProductDetailPath(product.id)} className={styles.button}>
                <span className={styles.rank}>{index + 1}</span>
                <Text weight="semibold" size={300}>
                  {product.nombre}
                </Text>
                <span className={styles.rating}>
                  <Star16Filled />
                  <Text weight="semibold" size={200}>
                    {formatRating(product.valoracion)}
                  </Text>
                </span>
              </Link>
            </li>
          ))}
        </ul>
      </div>
    </section>
  )
}

CardCaption.propTypes = {
  rank: PropTypes.number.isRequired,
  product: catalogProductPropType.isRequired,
  styles: PropTypes.object.isRequired,
}

FeaturedShowcase.propTypes = {
  products: PropTypes.arrayOf(catalogProductPropType).isRequired,
}
