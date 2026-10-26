import PropTypes from 'prop-types'
import { Subtitle1, Text, makeStyles, mergeClasses, tokens } from '@fluentui/react-components'
import { Star16Filled, Trophy20Filled } from '@fluentui/react-icons'
import TiltedGridHero from '../../components/TiltedGridHero'
import { catalogProductPropType } from '../../utils/propTypes'
import { formatRating } from '../../utils/format'

const useStyles = makeStyles({
  section: {
    display: 'flex',
    flexDirection: 'column',
    gap: tokens.spacingVerticalM,
  },
  header: {
    display: 'flex',
    flexDirection: 'column',
    gap: tokens.spacingVerticalXXS,
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
  // Sin marco: la banda se difumina por los lados sobre el fondo de la página.
  band: {
    height: '220px',
    '@media (min-width: 600px)': {
      height: '280px',
    },
  },
  // Cinco por fila en pantallas anchas; en estrechas, una fila que se desliza hacia los lados.
  list: {
    display: 'grid',
    gridAutoFlow: 'column',
    gridAutoColumns: 'minmax(168px, 1fr)',
    gap: tokens.spacingHorizontalM,
    margin: 0,
    padding: `${tokens.spacingVerticalXS} 0 ${tokens.spacingVerticalS}`,
    overflowX: 'auto',
    listStyleType: 'none',
    scrollSnapType: 'x proximity',
    '@media (min-width: 760px)': {
      gridAutoFlow: 'row',
      gridAutoColumns: 'unset',
      gridTemplateColumns: 'repeat(5, minmax(0, 1fr))',
      overflowX: 'visible',
    },
  },
  item: {
    scrollSnapAlign: 'start',
  },
  button: {
    display: 'flex',
    alignItems: 'center',
    gap: tokens.spacingHorizontalS,
    width: '100%',
    height: '100%',
    boxSizing: 'border-box',
    padding: tokens.spacingHorizontalM,
    textAlign: 'left',
    font: 'inherit',
    color: 'inherit',
    cursor: 'pointer',
    backgroundColor: tokens.colorNeutralBackground1,
    border: `1px solid ${tokens.colorNeutralStroke2}`,
    borderRadius: tokens.borderRadiusXLarge,
    transitionProperty: 'box-shadow, transform, border-color',
    transitionDuration: tokens.durationNormal,
    ':hover': {
      transform: 'translateY(-2px)',
      boxShadow: tokens.shadow8,
      borderColor: tokens.colorBrandStroke1,
    },
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
    width: '28px',
    height: '28px',
    borderRadius: tokens.borderRadiusCircular,
    color: tokens.colorNeutralForegroundOnBrand,
    backgroundColor: tokens.colorBrandBackground,
    fontWeight: tokens.fontWeightBold,
    fontSize: tokens.fontSizeBase200,
  },
  first: {
    backgroundColor: tokens.colorPaletteMarigoldForeground2,
  },
  text: {
    display: 'flex',
    flexDirection: 'column',
    minWidth: 0,
  },
  name: {
    display: '-webkit-box',
    WebkitLineClamp: 2,
    WebkitBoxOrient: 'vertical',
    overflow: 'hidden',
    overflowWrap: 'anywhere',
  },
  rating: {
    display: 'inline-flex',
    alignItems: 'center',
    gap: tokens.spacingHorizontalXXS,
    color: tokens.colorPaletteMarigoldForeground2,
  },
})

// Los productos mejor valorados: sus imágenes en una banda curva (decorativa) y, debajo, la lista numerada, cuyos
// productos se pueden pulsar. `products` llega ya ordenado de mejor a peor.
export default function FeaturedShowcase({ products, onSelect }) {
  const styles = useStyles()
  const images = products.filter((product) => product.imagen).map((product) => ({ src: product.imagen, alt: product.nombre }))

  return (
    <section className={styles.section} aria-labelledby="mejor-valorados">
      <div className={styles.header}>
        <Subtitle1 as="h2" id="mejor-valorados" className={styles.title}>
          <Trophy20Filled className={styles.trophy} />
          Los {products.length} mejor valorados
        </Subtitle1>
        <Text className={styles.subtitle}>La selección de ESIBuy, con las mejores opiniones de los clientes.</Text>
      </div>

      {images.length > 0 && (
        <TiltedGridHero
          className={styles.band}
          images={images}
          tileHeight={56}
          aspectRatio={4 / 3}
          gap={4}
          axis={50}
          curve={66}
          speed={4}
        />
      )}

      <ul className={styles.list} aria-label="Selección destacada">
        {products.map((product, index) => (
          <li key={product.id} className={styles.item}>
            <button type="button" className={styles.button} onClick={() => onSelect(product)}>
              <span className={mergeClasses(styles.rank, index === 0 && styles.first)}>{index + 1}</span>
              <span className={styles.text}>
                <Text weight="semibold" size={300} className={styles.name}>
                  {product.nombre}
                </Text>
                <span className={styles.rating}>
                  <Star16Filled />
                  <Text weight="semibold" size={200}>
                    {formatRating(product.valoracion)}
                  </Text>
                </span>
              </span>
            </button>
          </li>
        ))}
      </ul>
    </section>
  )
}

FeaturedShowcase.propTypes = {
  products: PropTypes.arrayOf(catalogProductPropType).isRequired,
  onSelect: PropTypes.func.isRequired,
}
