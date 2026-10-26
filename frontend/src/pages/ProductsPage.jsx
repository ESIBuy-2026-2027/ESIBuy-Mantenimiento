import {
  Button,
  MessageBar,
  MessageBarActions,
  MessageBarBody,
  Spinner,
  Text,
  Title1,
  makeStyles,
  mergeClasses,
  tokens,
} from '@fluentui/react-components'
import { ArrowDown20Regular, Box24Regular, CheckmarkCircle20Regular, SearchInfo24Regular } from '@fluentui/react-icons'
import { useState } from 'react'
import EmptyState from '../components/EmptyState'
import { usePageStyles } from '../components/pageStyles'
import CatalogFilters from '../features/catalog/CatalogFilters'
import FeaturedProducts from '../features/catalog/FeaturedProducts'
import ProductCard from '../features/catalog/ProductCard'
import ProductDialog from '../features/catalog/ProductDialog'
import { useCatalogFeed } from '../features/catalog/useCatalogFeed'
import { useCatalogFilters } from '../features/catalog/useCatalogFilters'
import { useCategories } from '../hooks/useCategories'
import { useOnReachEnd } from '../hooks/useOnReachEnd'

const PAGE_SIZE = 10
const ENTRADA_ESCALONADA_MS = 60

const useStyles = makeStyles({
  // La página ocupa todo el ancho (la selección de los mejores, también); el resto del contenido va en una columna.
  main: {
    display: 'flex',
    flexDirection: 'column',
    gap: tokens.spacingVerticalXL,
    width: '100%',
    boxSizing: 'border-box',
    paddingTop: tokens.spacingVerticalXL,
    paddingBottom: tokens.spacingVerticalXXXL,
  },
  column: {
    display: 'flex',
    flexDirection: 'column',
    gap: tokens.spacingVerticalXL,
    width: '100%',
    maxWidth: '860px',
    boxSizing: 'border-box',
    margin: '0 auto',
    padding: `0 ${tokens.spacingHorizontalL}`,
  },
  // Las tarjetas se colocan siempre de dos en dos, también en pantallas estrechas.
  grid: {
    display: 'grid',
    gridTemplateColumns: 'repeat(2, minmax(0, 1fr))',
    gap: tokens.spacingHorizontalL,
    margin: 0,
    padding: 0,
    listStyleType: 'none',
    transitionProperty: 'opacity',
    transitionDuration: tokens.durationNormal,
  },
  refreshing: {
    opacity: 0.55,
  },
  // Cada tarjeta entra subiendo y apareciendo, una detrás de otra dentro de cada tanda de diez.
  entry: {
    animationName: {
      from: { opacity: 0, transform: 'translateY(16px)' },
      to: { opacity: 1, transform: 'translateY(0)' },
    },
    animationDuration: tokens.durationSlow,
    animationTimingFunction: tokens.curveDecelerateMid,
    animationFillMode: 'both',
    '@media (prefers-reduced-motion: reduce)': {
      animationName: 'none',
    },
  },
  footer: {
    display: 'flex',
    flexDirection: 'column',
    alignItems: 'center',
    gap: tokens.spacingVerticalM,
    minHeight: '64px',
  },
  end: {
    display: 'flex',
    alignItems: 'center',
    gap: tokens.spacingHorizontalS,
    color: tokens.colorNeutralForeground3,
  },
  endIcon: {
    color: tokens.colorPaletteGreenForeground1,
  },
})

// Vista de productos: lo primero que ve el cliente al iniciar sesión (HU7). Arriba, los mejor valorados; debajo, los
// productos de diez en diez, que se cargan solos al llegar al final de la página.
export default function ProductsPage() {
  const pageStyles = usePageStyles()
  const styles = useStyles()
  const [selected, setSelected] = useState(null)
  const { categories } = useCategories()
  const filters = useCatalogFilters()
  const { search, category, minRating, sort, order, debouncedSearch, filtersActive } = filters
  const feed = useCatalogFeed({
    search: debouncedSearch,
    category,
    minRating,
    sort,
    order,
    size: PAGE_SIZE,
  })
  const endRef = useOnReachEnd(feed.loadMore, feed.hasMore && !feed.loadingMore && !feed.failedMore)
  const categoryNames = new Map(categories.map(({ id, nombre }) => [id, nombre]))
  // Sin ningún filtro, que no haya productos significa que el catálogo está vacío; con filtros, que no coinciden.
  const catalogEmpty = feed.loaded && feed.totalItems === 0 && !debouncedSearch && !category && !minRating
  const noMatches = feed.loaded && feed.totalItems === 0 && !catalogEmpty
  const hasItems = feed.items.length > 0
  const showFilters = filtersActive || hasItems

  return (
    <main className={styles.main}>
      <div className={mergeClasses(styles.column, pageStyles.titleBlock)}>
        <Title1 as="h1" className={pageStyles.title}>
          Productos
        </Title1>
        <Text className={pageStyles.subtitle}>Descubre lo que ofrecen los vendedores de ESIBuy.</Text>
      </div>

      {!filtersActive && <FeaturedProducts onSelect={setSelected} />}

      <div className={styles.column}>
        {showFilters && (
          <CatalogFilters
            search={search}
            category={category}
            categories={categories}
            minRating={minRating}
            sort={sort}
            order={order}
            onSearchChange={filters.changeSearch}
            onCategoryChange={filters.changeCategory}
            onMinRatingChange={filters.changeMinRating}
            onSortChange={filters.changeSort}
            onOrderChange={filters.changeOrder}
          />
        )}

        {feed.failed && (
          <MessageBar intent="error">
            <MessageBarBody>No se han podido cargar los productos. Inténtalo de nuevo.</MessageBarBody>
            <MessageBarActions>
              <Button onClick={feed.retry}>Reintentar</Button>
            </MessageBarActions>
          </MessageBar>
        )}

        {!feed.loaded && !feed.failed && <Spinner className={pageStyles.loading} label="Cargando productos…" />}

        {catalogEmpty && (
          <EmptyState
            icon={<Box24Regular />}
            title="Todavía no hay productos"
            description="Cuando los vendedores publiquen sus productos, los verás aquí."
          />
        )}

        {noMatches && (
          <EmptyState
            icon={<SearchInfo24Regular />}
            title="No hay productos que coincidan con los filtros"
            description="Prueba con otro nombre, otra categoría u otra valoración."
          >
            <Button onClick={filters.clearFilters}>Limpiar filtros</Button>
          </EmptyState>
        )}

        {hasItems && (
          <>
            <ul
              className={mergeClasses(styles.grid, feed.refreshing && styles.refreshing)}
              aria-label="Productos"
              aria-busy={feed.refreshing || feed.loadingMore}
            >
              {feed.items.map((product, index) => (
                <li
                  key={product.id}
                  className={styles.entry}
                  style={{ animationDelay: `${(index % PAGE_SIZE) * ENTRADA_ESCALONADA_MS}ms` }}
                >
                  <ProductCard product={product} categoryName={categoryNames.get(product.categoriaId)} onSelect={setSelected} />
                </li>
              ))}
            </ul>

            <div className={styles.footer}>
              {feed.loadingMore && <Spinner label="Cargando más productos…" />}

              {feed.failedMore && (
                <MessageBar intent="error">
                  <MessageBarBody>No se han podido cargar más productos. Inténtalo de nuevo.</MessageBarBody>
                  <MessageBarActions>
                    <Button onClick={feed.retry}>Reintentar</Button>
                  </MessageBarActions>
                </MessageBar>
              )}

              {feed.hasMore && !feed.loadingMore && !feed.failedMore && (
                <Button appearance="subtle" icon={<ArrowDown20Regular />} onClick={feed.loadMore}>
                  Ver más productos
                </Button>
              )}

              {!feed.hasMore && feed.loaded && !feed.refreshing && (
                <Text className={styles.end}>
                  <CheckmarkCircle20Regular className={styles.endIcon} />
                  Has visto todos los productos
                </Text>
              )}

              {/* Cuando esta marca llega a la vista (o casi), se cargan los diez siguientes. */}
              <div ref={endRef} aria-hidden="true" />
            </div>
          </>
        )}
        {selected && (
          <ProductDialog
            product={selected}
            categoryName={categoryNames.get(selected.categoriaId)}
            onClose={() => setSelected(null)}
          />
        )}
      </div>
    </main>
  )
}
