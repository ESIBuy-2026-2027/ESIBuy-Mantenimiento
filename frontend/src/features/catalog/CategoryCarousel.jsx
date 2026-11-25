import PropTypes from 'prop-types'
import { Button, MessageBar, MessageBarActions, MessageBarBody, Spinner } from '@fluentui/react-components'
import CatalogCarousel from './CatalogCarousel'
import ProductCard from './ProductCard'
import { useCatalogFeed } from './useCatalogFeed'

const TAMANO = 10

// Los productos de una categoría en una fila que se desliza (HU7): de diez en diez, por nombre. Pide los siguientes
// al llegar cerca del final de la fila. Una categoría sin productos no se pinta.
export default function CategoryCarousel({ category, favoritos, onAddFavorite, onRemoveFavorite }) {
  const feed = useCatalogFeed({
    search: '',
    category: category.id,
    minRating: '',
    sort: 'nombre',
    order: 'asc',
    size: TAMANO,
  })

  if (feed.failed) {
    return (
      <MessageBar intent="error">
        <MessageBarBody>No se han podido cargar los productos de {category.nombre}.</MessageBarBody>
        <MessageBarActions>
          <Button onClick={feed.retry}>Reintentar</Button>
        </MessageBarActions>
      </MessageBar>
    )
  }

  if (!feed.loaded) return <Spinner size="small" label={`Cargando ${category.nombre}…`} />

  if (feed.items.length === 0) return null

  return (
    <CatalogCarousel
      title={category.nombre}
      hasMore={feed.hasMore}
      loadingMore={feed.loadingMore}
      onLoadMore={feed.loadMore}
    >
      {feed.items.map((product) => (
        <ProductCard
          key={product.id}
          product={product}
          categoryName={category.nombre}
          favorite={favoritos.has(product.id)}
          onAddFavorite={() => onAddFavorite(product)}
          onRemoveFavorite={() => onRemoveFavorite(product)}
        />
      ))}
    </CatalogCarousel>
  )
}

CategoryCarousel.propTypes = {
  category: PropTypes.shape({ id: PropTypes.string.isRequired, nombre: PropTypes.string.isRequired }).isRequired,
  favoritos: PropTypes.instanceOf(Set).isRequired,
  onAddFavorite: PropTypes.func.isRequired,
  onRemoveFavorite: PropTypes.func.isRequired,
}
