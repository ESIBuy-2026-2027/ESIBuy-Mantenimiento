import { useState } from 'react'
import {
  Button,
  MessageBar,
  MessageBarActions,
  MessageBarBody,
  Spinner,
  Text,
  Title1,
} from '@fluentui/react-components'
import {
  Add20Regular,
  BoxMultiple24Regular,
  Dismiss20Regular,
  SearchInfo24Regular,
} from '@fluentui/react-icons'
import { useLocation } from 'react-router-dom'
import EmptyState from '../components/EmptyState'
import LinkButton from '../components/LinkButton'
import ListFilters from '../components/ListFilters'
import Pagination from '../components/Pagination'
import { usePageStyles } from '../components/pageStyles'
import DeleteProductDialog from '../features/seller/DeleteProductDialog'
import ProductRowActions from '../features/seller/ProductRowActions'
import ProductsTable from '../features/seller/ProductsTable'
import { useSellerProducts } from '../features/seller/useSellerProducts'
import { useCategories } from '../hooks/useCategories'
import { useListFilters } from '../hooks/useListFilters'
import { PATHS } from '../routes/paths'

const PAGE_SIZE = 10

export default function SellerProductsPage() {
  const styles = usePageStyles()
  const location = useLocation()
  // Aviso que deja el formulario de producto al guardar (llega en el estado de la navegación).
  const [notice, setNotice] = useState(location.state?.notice ?? '')
  const [productToDelete, setProductToDelete] = useState(null)
  const { categories } = useCategories()
  const { search, category, page, debouncedSearch, filtersActive, setPage, changeSearch, changeCategory, clearFilters } =
    useListFilters()
  const { data, failed, refreshing, retry } = useSellerProducts({
    search: debouncedSearch,
    category,
    page,
    size: PAGE_SIZE,
  })

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
        <MessageBar intent="success">
          <MessageBarBody>{notice}</MessageBarBody>
          <MessageBarActions
            containerAction={
              <Button
                appearance="transparent"
                aria-label="Cerrar aviso"
                icon={<Dismiss20Regular />}
                onClick={() => setNotice('')}
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
        />
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
        >
          <Button onClick={clearFilters}>Limpiar filtros</Button>
        </EmptyState>
      )}

      {data && data.totalItems > 0 && (
        <>
          <ProductsTable
            products={data.items}
            refreshing={refreshing}
            renderActions={(product) => <ProductRowActions product={product} onDelete={setProductToDelete} />}
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

      {productToDelete && (
        <DeleteProductDialog
          product={productToDelete}
          onClose={() => setProductToDelete(null)}
          onDeleted={() => {
            setProductToDelete(null)
            setNotice('Producto eliminado.')
            retry()
          }}
        />
      )}
    </main>
  )
}
