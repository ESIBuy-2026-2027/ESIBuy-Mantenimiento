import PropTypes from 'prop-types'
import { useCallback } from 'react'
import { Badge, Card, Text, Title1, makeStyles, mergeClasses, tokens } from '@fluentui/react-components'
import { ImageOff24Regular } from '@fluentui/react-icons'
import { Link } from 'react-router-dom'
import { getWishlist } from '../api/wishlist'
import { usePageStyles } from '../components/pageStyles'
import { useApiQuery } from '../hooks/useApiQuery'
import { getProductDetailPath } from '../routes/paths'
import { formatDate, formatPrice } from '../utils/format'

const useStyles = makeStyles({
  list: {
    listStyle: 'none',
    margin: 0,
    padding: 0,
    display: 'flex',
    flexDirection: 'column',
    gap: tokens.spacingVerticalM,
  },
  // En pantallas estrechas el precio baja a una segunda línea
  item: {
    display: 'flex',
    flexDirection: 'row',
    flexWrap: 'wrap',
    alignItems: 'center',
    gap: tokens.spacingHorizontalL,
  },
  // Un producto que ya no está disponible se ve atenuado: sigue en la lista, pero no se puede comprar
  unavailable: {
    opacity: 0.6,
  },
  media: {
    flexShrink: 0,
    width: '72px',
    height: '72px',
    overflow: 'hidden',
    borderRadius: tokens.borderRadiusMedium,
    backgroundColor: tokens.colorNeutralBackground3,
  },
  image: {
    display: 'block',
    width: '100%',
    height: '100%',
    objectFit: 'cover',
  },
  placeholder: {
    display: 'flex',
    alignItems: 'center',
    justifyContent: 'center',
    width: '100%',
    height: '100%',
    color: tokens.colorNeutralForeground4,
  },
  text: {
    display: 'flex',
    flexDirection: 'column',
    alignItems: 'flex-start',
    gap: tokens.spacingVerticalXXS,
    flexGrow: 1,
    minWidth: 0,
  },
  name: {
    fontWeight: tokens.fontWeightSemibold,
    color: tokens.colorBrandForeground1,
    overflowWrap: 'anywhere',
  },
  muted: {
    color: tokens.colorNeutralForeground3,
  },
  summary: {
    marginLeft: 'auto',
  },
})

// Cómo se puede comprar ahora un favorito: un producto desactivado sigue en la lista, pero ya no se abre su detalle.
function availabilityBadge(item) {
  if (!item.disponible) return <Badge appearance="tint" color="informative">No disponible</Badge>
  if (item.stock <= 0) return <Badge appearance="tint" color="warning">Agotado</Badge>
  return null
}

function WishlistItem({ item }) {
  const styles = useStyles()

  return (
    <Card className={mergeClasses(styles.item, !item.disponible && styles.unavailable)}>
      <div className={styles.media}>
        {item.imagen ? (
          <img className={styles.image} src={item.imagen} alt={item.nombre} loading="lazy" />
        ) : (
          <span className={styles.placeholder} role="img" aria-label="Sin imagen">
            <ImageOff24Regular />
          </span>
        )}
      </div>
      <div className={styles.text}>
        {item.disponible ? (
          <Link to={getProductDetailPath(item.productoId)} className={styles.name}>
            {item.nombre}
          </Link>
        ) : (
          <Text className={styles.name}>{item.nombre}</Text>
        )}
        <Text size={200} className={styles.muted}>
          Añadido el {formatDate(item.addedAt)}
        </Text>
        {availabilityBadge(item)}
      </div>
      <Text weight="semibold" className={styles.summary}>
        {formatPrice(item.precioFinal)}
      </Text>
    </Card>
  )
}

WishlistItem.propTypes = {
  item: PropTypes.shape({
    productoId: PropTypes.string.isRequired,
    nombre: PropTypes.string.isRequired,
    imagen: PropTypes.string,
    precioFinal: PropTypes.number.isRequired,
    stock: PropTypes.number.isRequired,
    disponible: PropTypes.bool.isRequired,
    addedAt: PropTypes.string.isRequired,
  }).isRequired,
}

// «Mis favoritos» del cliente (HU14): su lista de deseos, en el orden en que añadió cada producto y con los datos
// actuales de cada uno (precio, si está agotado o si el vendedor lo ha desactivado).
export default function WishlistPage() {
  const pageStyles = usePageStyles()
  const styles = useStyles()
  const fetcher = useCallback(() => getWishlist(), [])
  const { data: lista } = useApiQuery(fetcher)

  return (
    <main className={pageStyles.page}>
      <div className={pageStyles.heading}>
        <div className={pageStyles.titleBlock}>
          <Title1 as="h1" className={pageStyles.title}>
            Mis favoritos
          </Title1>
          <Text className={pageStyles.subtitle}>Los productos que has guardado para más adelante.</Text>
        </div>
      </div>
      {lista?.items.length > 0 && (
        <ul aria-label="Tus favoritos" className={styles.list}>
          {lista.items.map((item) => (
            <li key={item.productoId}>
              <WishlistItem item={item} />
            </li>
          ))}
        </ul>
      )}
    </main>
  )
}
