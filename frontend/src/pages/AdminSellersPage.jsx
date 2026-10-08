import { useCallback, useState } from 'react'
import {
  Button,
  MessageBar,
  MessageBarActions,
  MessageBarBody,
  Spinner,
  Text,
  Title1,
} from '@fluentui/react-components'
import { ArrowLeft20Regular, BuildingShop24Regular, SearchInfo24Regular } from '@fluentui/react-icons'
import { getAdminSellers } from '../api/adminSellers'
import EmptyState from '../components/EmptyState'
import LinkButton from '../components/LinkButton'
import ListFilters from '../components/ListFilters'
import Pagination from '../components/Pagination'
import { usePageStyles } from '../components/pageStyles'
import SellerDetailDialog from '../features/admin/SellerDetailDialog'
import SortControls from '../features/admin/SortControls'
import SellerStatusFilter from '../features/admin/SellerStatusFilter'
import SellersTable from '../features/admin/SellersTable'
import { useApiQuery } from '../hooks/useApiQuery'
import { useCategories } from '../hooks/useCategories'
import { useListFilters } from '../hooks/useListFilters'
import { PATHS } from '../routes/paths'

const PAGE_SIZE = 10

const SELLER_SORT_OPTIONS = [
  { value: 'status', label: 'Estado' },
  { value: 'category', label: 'Categoría' },
  { value: 'name', label: 'Nombre' },
]

export default function AdminSellersPage() {
  const styles = usePageStyles()
  const [selectedSellerId, setSelectedSellerId] = useState(null)
  const [status, setStatus] = useState('')
  const [sort, setSort] = useState('status')
  const [order, setOrder] = useState('asc')
  const { categories } = useCategories()
  const { search, category, page, debouncedSearch, filtersActive, setPage, changeSearch, changeCategory, clearFilters } =
    useListFilters()
  const fetcher = useCallback(
    () => getAdminSellers({ search: debouncedSearch, category, status, sort, order, page, size: PAGE_SIZE }),
    [debouncedSearch, category, status, sort, order, page],
  )
  const { data, failed, refreshing, retry } = useApiQuery(fetcher)

  const hasNoSellers = data?.totalItems === 0 && !debouncedSearch && !category && !status
  const showFilters = filtersActive || Boolean(status) || (data?.totalItems ?? 0) > 0

  const changeAndGoToFirstPage = (setter) => (value) => {
    setter(value)
    setPage(1)
  }

  const clearAllFilters = () => {
    clearFilters()
    setStatus('')
  }

  return (
    <main className={styles.page}>
      <div className={styles.heading}>
        <div className={styles.titleBlock}>
          <Title1 as="h1" className={styles.title}>
            Vendedores
          </Title1>
          <Text className={styles.subtitle}>Supervisa a los vendedores en modo solo lectura.</Text>
        </div>
        <LinkButton to={PATHS.ADMIN} appearance="subtle" icon={<ArrowLeft20Regular />}>
          Volver al panel
        </LinkButton>
      </div>

      {showFilters && (
        <ListFilters
          search={search}
          category={category}
          categories={categories}
          searchPlaceholder="Nombre, nombre comercial o correo"
          onSearchChange={changeSearch}
          onCategoryChange={changeCategory}
        >
          <SellerStatusFilter status={status} onChange={changeAndGoToFirstPage(setStatus)} />
          <SortControls
            options={SELLER_SORT_OPTIONS}
            sort={sort}
            order={order}
            onSortChange={changeAndGoToFirstPage(setSort)}
            onOrderChange={changeAndGoToFirstPage(setOrder)}
          />
        </ListFilters>
      )}

      {failed && (
        <MessageBar intent="error">
          <MessageBarBody>No se han podido cargar los vendedores. Inténtalo de nuevo.</MessageBarBody>
          <MessageBarActions>
            <Button onClick={retry}>Reintentar</Button>
          </MessageBarActions>
        </MessageBar>
      )}

      {!data && !failed && <Spinner className={styles.loading} label="Cargando vendedores…" />}

      {hasNoSellers && (
        <EmptyState
          icon={<BuildingShop24Regular />}
          title="Todavía no hay vendedores"
          description="Cuando se registre un vendedor aparecerá aquí."
        />
      )}

      {data && data.totalItems === 0 && !hasNoSellers && (
        <EmptyState
          icon={<SearchInfo24Regular />}
          title="No hay vendedores que coincidan con los filtros"
          description="Prueba con otro nombre u otra categoría."
        >
          <Button onClick={clearAllFilters}>Limpiar filtros</Button>
        </EmptyState>
      )}

      {data && data.totalItems > 0 && (
        <>
          <SellersTable sellers={data.items} refreshing={refreshing} onView={setSelectedSellerId} />
          <Pagination
            page={data.page}
            totalPages={data.totalPages}
            totalItems={data.totalItems}
            pageSize={data.size}
            itemLabel="vendedores"
            onPageChange={setPage}
          />
        </>
      )}

      {selectedSellerId && (
        <SellerDetailDialog
          sellerId={selectedSellerId}
          onClose={() => setSelectedSellerId(null)}
          onStatusChange={retry}
        />
      )}
    </main>
  )
}
