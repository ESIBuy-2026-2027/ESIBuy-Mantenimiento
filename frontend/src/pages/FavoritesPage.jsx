import PropTypes from 'prop-types'
import { useCallback, useId, useState } from 'react'
import {
  Badge,
  Button,
  Card,
  MessageBar,
  MessageBarActions,
  MessageBarBody,
  Spinner,
  Tab,
  TabList,
  Text,
  Title1,
  makeStyles,
  mergeClasses,
  tokens,
} from '@fluentui/react-components'
import { Delete20Regular, Heart24Regular } from '@fluentui/react-icons'
import { Link } from 'react-router-dom'
import { getWishlist, removeWishlistItem } from '../api/wishlist'
import EmptyState from '../components/EmptyState'
import LinkButton from '../components/LinkButton'
import { usePageStyles } from '../components/pageStyles'
import FavoriteCategoriesList from '../features/catalog/FavoriteCategoriesList'
import { useApiQuery } from '../hooks/useApiQuery'
import { PATHS, getProductDetailPath } from '../routes/paths'
import { formatDate, formatPrice } from '../utils/format'

const useStyles = makeStyles({
  panel: {
    display: 'flex',
    flexDirection: 'column',
    gap: tokens.spacingVerticalXL,
  },
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
  placeholderImage: {
    width: '24px',
    height: '24px',
    objectFit: 'contain',
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
    display: 'flex',
    alignItems: 'center',
    gap: tokens.spacingHorizontalM,
    marginLeft: 'auto',
  },
})

// Cómo se puede comprar ahora un favorito: un producto desactivado sigue en la lista, pero ya no se abre su detalle.
function availabilityBadge(item) {
  if (!item.disponible) return <Badge appearance="tint" color="informative">No disponible</Badge>
  if (item.stock <= 0) return <Badge appearance="tint" color="warning">Agotado</Badge>
  return null
}

function FavoriteItem({ item, removing, onRemove }) {
  const styles = useStyles()

  return (
    <Card className={mergeClasses(styles.item, !item.disponible && styles.unavailable)}>
      <div className={styles.media}>
        {item.imagen ? (
          <img className={styles.image} src={item.imagen} alt={item.nombre} loading="lazy" />
        ) : (
          <span className={styles.placeholder}>
            <img
              className={styles.placeholderImage}
              src="data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 24 24'%3E%3Cpath d='M3 3l18 18M10 12l-4 6h12l-3-4-2 2M6 6h11a2 2 0 0 1 2 2v8' fill='none' stroke='%23999999' stroke-width='1.5' stroke-linecap='round' stroke-linejoin='round'/%3E%3C/svg%3E"
              alt="Sin imagen"
            />
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
      <div className={styles.summary}>
        <Text weight="semibold">{formatPrice(item.precioFinal)}</Text>
        <Button
          appearance="subtle"
          icon={<Delete20Regular />}
          aria-label={`Quitar ${item.nombre} de favoritos`}
          title="Quitar de favoritos"
          disabled={removing}
          onClick={() => onRemove(item)}
        />
      </div>
    </Card>
  )
}

FavoriteItem.propTypes = {
  item: PropTypes.shape({
    productoId: PropTypes.string.isRequired,
    nombre: PropTypes.string.isRequired,
    imagen: PropTypes.string,
    precioFinal: PropTypes.number.isRequired,
    stock: PropTypes.number.isRequired,
    disponible: PropTypes.bool.isRequired,
    addedAt: PropTypes.string.isRequired,
  }).isRequired,
  removing: PropTypes.bool.isRequired,
  onRemove: PropTypes.func.isRequired,
}

// Los productos favoritos del cliente (HU14): su lista de deseos, en el orden en que añadió cada producto y con los
// datos actuales de cada uno (precio, si está agotado o si el vendedor lo ha desactivado). Cada uno se puede quitar sin
// confirmación, porque se puede volver a añadir desde el catálogo; el backend responde con la lista actualizada.
function FavoriteProducts() {
  const pageStyles = usePageStyles()
  const styles = useStyles()
  const fetcher = useCallback(() => getWishlist(), [])
  const { data: cargada, failed, retry } = useApiQuery(fetcher)
  const [actualizada, setActualizada] = useState(null)
  const [quitando, setQuitando] = useState(null)
  const [aviso, setAviso] = useState(null)
  const lista = actualizada ?? cargada

  const quitar = async (item) => {
    setAviso(null)
    setQuitando(item.productoId)
    try {
      setActualizada(await removeWishlistItem(item.productoId))
      setAviso({ intent: 'success', texto: `${item.nombre} se ha quitado de tus favoritos.` })
    } catch {
      setAviso({ intent: 'error', texto: `No se ha podido quitar ${item.nombre} de tus favoritos. Inténtalo de nuevo.` })
    } finally {
      setQuitando(null)
    }
  }

  return (
    <>
      {aviso && (
        <MessageBar intent={aviso.intent}>
          <MessageBarBody>{aviso.texto}</MessageBarBody>
        </MessageBar>
      )}
      {failed && (
        <MessageBar intent="error">
          <MessageBarBody>No se han podido cargar tus favoritos. Inténtalo de nuevo.</MessageBarBody>
          <MessageBarActions>
            <Button onClick={retry}>Reintentar</Button>
          </MessageBarActions>
        </MessageBar>
      )}
      {!lista && !failed && <Spinner label="Cargando tus favoritos…" className={pageStyles.loading} />}
      {lista?.items.length === 0 && (
        <EmptyState
          icon={<Heart24Regular />}
          title="Aún no tienes favoritos"
          description="Pulsa el corazón de un producto del catálogo para guardarlo aquí."
        >
          <LinkButton to={PATHS.PRODUCTS} appearance="primary">
            Ver los productos
          </LinkButton>
        </EmptyState>
      )}
      {lista?.items.length > 0 && (
        <ul aria-label="Tus favoritos" className={styles.list}>
          {lista.items.map((item) => (
            <li key={item.productoId}>
              <FavoriteItem item={item} removing={quitando === item.productoId} onRemove={quitar} />
            </li>
          ))}
        </ul>
      )}
    </>
  )
}

// «Mis favoritos» del cliente: en una pestaña sus productos (HU14) y en otra sus categorías (AB#528). Se abre en los
// productos; cada pestaña pide sus datos al abrirse.
export default function FavoritesPage() {
  const pageStyles = usePageStyles()
  const styles = useStyles()
  const id = useId()
  const [pestana, setPestana] = useState('products')

  return (
    <main className={pageStyles.page}>
      <div className={pageStyles.heading}>
        <div className={pageStyles.titleBlock}>
          <Title1 as="h1" className={pageStyles.title}>
            Mis favoritos
          </Title1>
          <Text className={pageStyles.subtitle}>Los productos y las categorías que has guardado.</Text>
        </div>
      </div>
      <TabList selectedValue={pestana} onTabSelect={(_event, data) => setPestana(data.value)}>
        <Tab id={`${id}-products`} value="products">
          Productos
        </Tab>
        <Tab id={`${id}-categories`} value="categories">
          Categorías
        </Tab>
      </TabList>
      <div role="tabpanel" aria-labelledby={`${id}-${pestana}`} className={styles.panel}>
        {pestana === 'products' ? <FavoriteProducts /> : <FavoriteCategoriesList />}
      </div>
    </main>
  )
}
