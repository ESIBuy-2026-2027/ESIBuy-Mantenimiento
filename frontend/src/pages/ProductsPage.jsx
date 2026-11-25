
import { useEffect, useRef, useState } from 'react'
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
import {
  ArrowDown20Regular,
  Box24Regular,
  CheckmarkCircle20Regular,
  SearchInfo24Regular,
} from '@fluentui/react-icons'
import { getWishlists } from '../api/wishlist'
import EmptyState from '../components/EmptyState'
import { usePageStyles } from '../components/pageStyles'
import CatalogFilters from '../features/catalog/CatalogFilters'
import OffersStrip from '../features/catalog/OffersStrip'
import ProductCard from '../features/catalog/ProductCard'
import { useCatalogFeed } from '../features/catalog/useCatalogFeed'
import { useCatalogFilters } from '../features/catalog/useCatalogFilters'
import WishlistPickerDialog from '../features/wishlist/WishlistPickerDialog'
import { useCategories } from '../hooks/useCategories'
import { useOnReachEnd } from '../hooks/useOnReachEnd'

const PAGE_SIZE = 10
const ENTRADA_ESCALONADA_MS = 60

const useStyles = makeStyles({
  // El catálogo utiliza el ancho disponible para distribuir cuatro tarjetas por fila.
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
    maxWidth: '1320px',
    boxSizing: 'border-box',
    margin: '0 auto',
    padding: `0 ${tokens.spacingHorizontalL}`,
  },
  grid: {
    display: 'grid',
    gridTemplateColumns: 'repeat(4, minmax(0, 1fr))',
    '@media (max-width: 1100px)': {
      gridTemplateColumns: 'repeat(3, minmax(0, 1fr))',
    },
    '@media (max-width: 760px)': {
      gridTemplateColumns: 'repeat(2, minmax(0, 1fr))',
    },
    '@media (max-width: 420px)': {
      gridTemplateColumns: 'minmax(0, 1fr)',
    },
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

// Catálogo del cliente: ofertas en la parte superior y listado completo de productos, con filtros y carga progresiva.
export default function ProductsPage() {
  const pageStyles = usePageStyles()
  const styles = useStyles()
  const { categories } = useCategories()
  const filters = useCatalogFilters()

  const {
    search,
    category,
    minRating,
    minPrice,
    maxPrice,
    priceCeiling,
    sort,
    order,
    debouncedSearch,
    inStock,
    offersOnly,
    filtersActive,
  } = filters

  const feed = useCatalogFeed({
    search: debouncedSearch,
    category,
    minRating,
    inStock,
    offersOnly,
    minPrice,
    maxPrice,
    sort,
    order,
    size: PAGE_SIZE,
    enabled: true,
  })

  const endRef = useOnReachEnd(
    feed.loadMore,
    feed.hasMore && !feed.loadingMore && !feed.failedMore,
  )

  useEffect(() => {
    if (feed.availableMaxPrice !== null) {
      filters.changeAvailableMaxPrice(feed.availableMaxPrice)
    }
  }, [feed.availableMaxPrice, filters.changeAvailableMaxPrice])

  const categoryNames = new Map(
    categories.map(({ id, nombre }) => [id, nombre]),
  )

  // Las listas del cliente se cargan al entrar; el corazón abre un selector para guardar el producto en una o varias.
  const [listasDeseos, setListasDeseos] = useState([])
  const [productoSelector, setProductoSelector] = useState(null)
  const cambiados = useRef(false)

  const favoritos = new Set(
    listasDeseos.flatMap((lista) =>
      lista.items.map((item) => item.productoId),
    ),
  )

  useEffect(() => {
    let cancelled = false

    getWishlists()
      .then((listas) => {
        if (!cancelled && !cambiados.current) {
          setListasDeseos(listas)
        }
      })
      .catch(() => {})

    return () => {
      cancelled = true
    }
  }, [])

  const abrirSelectorLista = (producto) => setProductoSelector(producto)

  // Sin ningún filtro, que no haya productos significa que el catálogo está vacío; con filtros, que no coinciden.
  const catalogEmpty =
    feed.loaded &&
    feed.totalItems === 0 &&
    !debouncedSearch &&
    !category &&
    !minRating &&
    minPrice === 0 &&
    maxPrice === null &&
    !inStock &&
    !offersOnly

  const noMatches = feed.loaded && feed.totalItems === 0 && !catalogEmpty
  const hasItems = feed.items.length > 0
  const changeSort = filters.changeSort
  const changeOrder = filters.changeOrder

  return (
    <main className={styles.main}>
      <div className={mergeClasses(styles.column, pageStyles.titleBlock)}>
        <Title1 as="h1" className={pageStyles.title}>
          Productos
        </Title1>
        <Text className={pageStyles.subtitle}>
          Descubre lo que ofrecen los vendedores de ESIBuy.
        </Text>
      </div>

      <div className={styles.column}>
        <CatalogFilters
          search={search}
          category={category}
          categories={categories}
          minRating={minRating}
          minPrice={minPrice}
          maxPrice={maxPrice}
          priceCeiling={priceCeiling}
          inStock={inStock}
          offersOnly={offersOnly}
          filtersActive={filtersActive}
          sort={sort}
          order={order}
          onSearchChange={filters.changeSearch}
          onCategoryChange={filters.changeCategory}
          onMinRatingChange={filters.changeMinRating}
          onPriceChange={filters.changePriceRange}
          onInStockChange={filters.changeInStock}
          onOffersOnlyChange={filters.changeOffersOnly}
          onClearFilters={filters.clearFilters}
          onSortChange={changeSort}
          onOrderChange={changeOrder}
        />

        <OffersStrip
          categoryNames={categoryNames}
          favoritos={favoritos}
          onAddFavorite={abrirSelectorLista}
          onRemoveFavorite={abrirSelectorLista}
        />

        {feed.failed && (
          <MessageBar intent="error">
            <MessageBarBody>
              No se han podido cargar los productos. Inténtalo de nuevo.
            </MessageBarBody>
            <MessageBarActions>
              <Button onClick={feed.retry}>Reintentar</Button>
            </MessageBarActions>
          </MessageBar>
        )}

        {!feed.loaded && !feed.failed && (
          <Spinner
            className={pageStyles.loading}
            label="Cargando productos…"
          />
        )}

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
          />
        )}

        {hasItems && (
          <>
            <Title1 as="h2" className={pageStyles.title}>
              Productos generales
            </Title1>

            <ul
              className={mergeClasses(
                styles.grid,
                feed.refreshing && styles.refreshing,
              )}
              aria-label="Productos"
              aria-busy={feed.refreshing || feed.loadingMore}
            >
              {feed.items.map((product, index) => (
                <li
                  key={product.id}
                  className={styles.entry}
                  style={{
                    animationDelay: `${(index % PAGE_SIZE) * ENTRADA_ESCALONADA_MS}ms`,
                  }}
                >
                  <ProductCard
                    product={product}
                    categoryName={categoryNames.get(product.categoriaId)}
                    favorite={favoritos.has(product.id)}
                    onAddFavorite={() => abrirSelectorLista(product)}
                    onRemoveFavorite={() => abrirSelectorLista(product)}
                  />
                </li>
              ))}
            </ul>

            <div className={styles.footer}>
              {feed.loadingMore && (
                <Spinner label="Cargando más productos…" />
              )}

              {feed.failedMore && (
                <MessageBar intent="error">
                  <MessageBarBody>
                    No se han podido cargar más productos. Inténtalo de nuevo.
                  </MessageBarBody>
                  <MessageBarActions>
                    <Button onClick={feed.retry}>Reintentar</Button>
                  </MessageBarActions>
                </MessageBar>
              )}

              {feed.hasMore && !feed.loadingMore && !feed.failedMore && (
                <Button
                  appearance="subtle"
                  icon={<ArrowDown20Regular />}
                  onClick={feed.loadMore}
                >
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
      </div>

      {productoSelector && (
        <WishlistPickerDialog
          product={productoSelector}
          wishlists={listasDeseos}
          onClose={() => setProductoSelector(null)}
          onWishlistsChange={(nuevas) => {
            cambiados.current = true
            setListasDeseos(nuevas)
          }}
        />
      )}
    </main>
  )
}
