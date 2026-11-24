import { useState } from 'react'
import {
  Button,
  Field,
  MessageBar,
  MessageBarActions,
  MessageBarBody,
  Select,
  Spinner,
  Text,
  Title1,
  makeStyles,
  tokens,
} from '@fluentui/react-components'
import {
  Add20Regular,
  BoxMultiple24Regular,
  Dismiss20Regular,
  SearchInfo24Regular,
} from '@fluentui/react-icons'
import { useLocation } from 'react-router-dom'
import { updateSellerProductAvailability } from '../api/products'
import EmptyState from '../components/EmptyState'
import LinkButton from '../components/LinkButton'
import ListFilters from '../components/ListFilters'
import Pagination from '../components/Pagination'
import { usePageStyles } from '../components/pageStyles'
import DeleteProductDialog from '../features/seller/DeleteProductDialog'
import ProductDetailDialog from '../features/seller/ProductDetailDialog'
import ProductRowActions from '../features/seller/ProductRowActions'
import ProductsTable from '../features/seller/ProductsTable'
import { useSellerProducts } from '../features/seller/useSellerProducts'
import { useCategories } from '../hooks/useCategories'
import { useListFilters } from '../hooks/useListFilters'
import { PATHS } from '../routes/paths'

const PAGE_SIZE = 10

const useFilterStyles = makeStyles({
  range: {
    flexGrow: 1,
    flexBasis: '280px',
  },
  rangeControl: {
    position: 'relative',
    height: '36px',
  },
  rangeInput: {
    position: 'absolute',
    width: '25%',
    height: '36px',
    margin: 0,
    pointerEvents: 'none',
    accentColor: '#EA580C',
    '&::-webkit-slider-thumb': {
      pointerEvents: 'auto',
    },
    '&::-moz-range-thumb': {
      pointerEvents: 'auto',
    },
  },
  actions: {
    display: 'flex',
    alignItems: 'flex-end',
  },
})

export default function SellerProductsPage() {
  const styles = usePageStyles()
  const filterStyles = useFilterStyles()
  const location = useLocation()
  // Aviso que deja el formulario de producto al guardar (llega en el estado de la navegación).
  const [notice, setNotice] = useState(
    location.state?.notice ? { intent: 'success', text: location.state.notice } : null,
  )
  const [productToView, setProductToView] = useState(null)
  const [productToDelete, setProductToDelete] = useState(null)
  const [togglingProductId, setTogglingProductId] = useState(null)
  const { categories } = useCategories()
  const {
    search, category, status, sort, minPrice, maxPrice, page, debouncedSearch,
    debouncedMinPrice, debouncedMaxPrice, filtersActive, setPage, changeSearch,
    changeCategory, changeStatus, changeSort, changeMinPrice, changeMaxPrice, clearFilters,
  } = useListFilters()
  const { data, failed, refreshing, retry } = useSellerProducts({
    search: debouncedSearch,
    category,
    status,
    sort,
    minPrice: debouncedMinPrice,
    maxPrice: debouncedMaxPrice,
    page,
    size: PAGE_SIZE,
  })

  // Activa un producto no disponible o desactiva uno disponible (HU16), sin confirmación: se deshace con otro clic.
  const toggleAvailability = async (product) => {
    setTogglingProductId(product.id)
    try {
      await updateSellerProductAvailability(product.id, !product.disponible)
      setNotice({ intent: 'success', text: product.disponible ? 'Producto desactivado.' : 'Producto activado.' })
      retry()
    } catch {
      setNotice({ intent: 'error', text: 'No se ha podido cambiar la disponibilidad del producto. Inténtalo de nuevo.' })
    } finally {
      setTogglingProductId(null)
    }
  }

  const hasNoProducts = data?.totalItems === 0 && !debouncedSearch && !category
  const showFilters = filtersActive || (data?.totalItems ?? 0) > 0

  return (
    <main className={styles.page}>
      <div className={styles.heading}>
        <div className={styles.titleBlock}>
          <Title1 as="h1" className={styles.title}>
            Mis productos
          </Title1>
          <Text className={styles.subtitle}>Consulta y busca en tu catálogo.</Text>
        </div>
        <LinkButton to={PATHS.SELLER_PRODUCT_NEW} appearance="primary" icon={<Add20Regular />}>
          Añadir producto
        </LinkButton>
      </div>

      {notice && (
        <MessageBar intent={notice.intent}>
          <MessageBarBody>{notice.text}</MessageBarBody>
          <MessageBarActions
            containerAction={
              <Button
                appearance="transparent"
                aria-label="Cerrar aviso"
                icon={<Dismiss20Regular />}
                onClick={() => setNotice(null)}
              />
            }
          />
        </MessageBar>
      )}

      {showFilters && (
        <ListFilters
          search={search}
          category={category}
          categories={categories}
          searchPlaceholder="Escribe el nombre de un producto"
          onSearchChange={changeSearch}
          onCategoryChange={changeCategory}
        >
          <Field label="Estado" style={{ flexGrow: 1, flexBasis: '180px' }}>
            <Select value={status} onChange={(_event, data) => changeStatus(data.value)}>
              <option value="">Todos los estados</option>
              <option value="visible">Visible</option>
              <option value="hidden">No visible</option>
            </Select>
          </Field>
          <Field label="Ordenar por" style={{ flexGrow: 1, flexBasis: '200px' }}>
            <Select value={sort} onChange={(_event, data) => changeSort(data.value)}>
              <option value="">Sin ordenar</option>
              <option value="price-asc">Precio: menor a mayor</option>
              <option value="price-desc">Precio: mayor a menor</option>
              <option value="stock-asc">Stock: menor a mayor</option>
              <option value="stock-desc">Stock: mayor a menor</option>
            </Select>
          </Field>
          <Field
            label={`Rango de precio: ${minPrice || 0} € – ${maxPrice || 200} €`}
            className={filterStyles.range}
          >
            <div className={filterStyles.rangeControl}>
              <input
                className={filterStyles.rangeInput}
                aria-label="Precio mínimo"
                type="range"
                min="0"
                max="200"
                step="1"
                value={minPrice}
                onChange={(event) => {
                  const next = Math.min(Number(event.target.value), Number(maxPrice))
                  changeMinPrice(String(next))
                }}
              />
              <input
                className={filterStyles.rangeInput}
                aria-label="Precio máximo"
                type="range"
                min="0"
                max="200"
                step="1"
                value={maxPrice}
                onChange={(event) => {
                  const next = Math.max(Number(event.target.value), Number(minPrice))
                  changeMaxPrice(String(next))
                }}
              />
            </div>
          </Field>
          <div className={filterStyles.actions}>
            <Button appearance="subtle" onClick={clearFilters} disabled={!filtersActive}>
              Limpiar filtros
            </Button>
          </div>
        </ListFilters>
      )}

      {failed && (
        <MessageBar intent="error">
          <MessageBarBody>No se han podido cargar tus productos. Inténtalo de nuevo.</MessageBarBody>
          <MessageBarActions>
            <Button onClick={retry}>Reintentar</Button>
          </MessageBarActions>
        </MessageBar>
      )}

      {!data && !failed && <Spinner className={styles.loading} label="Cargando tus productos…" />}

      {hasNoProducts && (
        <EmptyState
          icon={<BoxMultiple24Regular />}
          title="Todavía no tienes productos"
          description="Añade tu primer producto para empezar a vender en ESIBuy."
        >
          <LinkButton to={PATHS.SELLER_PRODUCT_NEW} appearance="primary" icon={<Add20Regular />}>
            Añadir producto
          </LinkButton>
        </EmptyState>
      )}

      {data && data.totalItems === 0 && !hasNoProducts && (
        <EmptyState
          icon={<SearchInfo24Regular />}
          title="No hay productos que coincidan con los filtros"
          description="Prueba con otro nombre u otra categoría."
        />
      )}

      {data && data.totalItems > 0 && (
        <>
          <ProductsTable
            products={data.items}
            categories={categories}
            refreshing={refreshing}
            onToggleAvailability={toggleAvailability}
            togglingProductId={togglingProductId}
            renderActions={(product) => (
              <ProductRowActions
                product={product}
                onView={setProductToView}
                onDelete={setProductToDelete}
              />
            )}
          />
          <Pagination
            page={data.page}
            totalPages={data.totalPages}
            totalItems={data.totalItems}
            pageSize={data.size}
            itemLabel="productos"
            onPageChange={setPage}
          />
        </>
      )}

      {productToView && (
        <ProductDetailDialog
          productId={productToView.id}
          onClose={() => setProductToView(null)}
        />
      )}

      {productToDelete && (
        <DeleteProductDialog
          product={productToDelete}
          onClose={() => setProductToDelete(null)}
          onDeleted={() => {
            setProductToDelete(null)
            setNotice({ intent: 'success', text: 'Producto eliminado.' })
            retry()
          }}
        />
      )}
    </main>
  )
}