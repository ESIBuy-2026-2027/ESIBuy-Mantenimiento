import { useState } from 'react'
import {
  Button,
  MessageBar,
  MessageBarActions,
  MessageBarBody,
  Spinner,
  Text,
  Title1,
  makeStyles,
  tokens,
} from '@fluentui/react-components'
import { Add20Regular, BoxMultiple24Regular, SearchInfo24Regular } from '@fluentui/react-icons'
import EmptyState from '../components/EmptyState'
import LinkButton from '../components/LinkButton'
import Pagination from '../components/Pagination'
import ProductFilters from '../features/seller/ProductFilters'
import ProductsTable from '../features/seller/ProductsTable'
import { useCategories } from '../features/seller/useCategories'
import { useDebouncedValue } from '../features/seller/useDebouncedValue'
import { useSellerProducts } from '../features/seller/useSellerProducts'
import { PATHS } from '../routes/paths'

const PAGE_SIZE = 10

const useStyles = makeStyles({
  page: {
    width: '100%',
    maxWidth: '1000px',
    margin: '0 auto',
    boxSizing: 'border-box',
    display: 'flex',
    flexDirection: 'column',
    gap: tokens.spacingVerticalXL,
    padding: `${tokens.spacingVerticalXL} ${tokens.spacingHorizontalL} ${tokens.spacingVerticalXXXL}`,
  },
  heading: {
    display: 'flex',
    flexWrap: 'wrap',
    alignItems: 'center',
    justifyContent: 'space-between',
    gap: tokens.spacingHorizontalL,
  },
  titleBlock: {
    display: 'flex',
    flexDirection: 'column',
    gap: tokens.spacingVerticalXS,
  },
  title: {
    margin: 0,
  },
  subtitle: {
    color: tokens.colorNeutralForeground3,
  },
  loading: {
    padding: tokens.spacingVerticalXXXL,
  },
})

export default function SellerProductsPage() {
  const styles = useStyles()
  const [search, setSearch] = useState('')
  const [category, setCategory] = useState('')
  const [page, setPage] = useState(1)
  const debouncedSearch = useDebouncedValue(search)
  const categories = useCategories()
  const { data, failed, refreshing, retry } = useSellerProducts({
    search: debouncedSearch,
    category,
    page,
    size: PAGE_SIZE,
  })

  const filtersActive = Boolean(search || category)
  const hasNoProducts = data?.totalItems === 0 && !debouncedSearch && !category
  const showFilters = filtersActive || (data?.totalItems ?? 0) > 0

  const changeSearch = (value) => {
    setSearch(value)
    setPage(1)
  }

  const changeCategory = (value) => {
    setCategory(value)
    setPage(1)
  }

  const clearFilters = () => {
    setSearch('')
    setCategory('')
    setPage(1)
  }

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

      {showFilters && (
        <ProductFilters
          search={search}
          category={category}
          categories={categories}
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
          <ProductsTable products={data.items} refreshing={refreshing} />
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
    </main>
  )
}
