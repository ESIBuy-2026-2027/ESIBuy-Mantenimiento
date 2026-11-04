import { useMemo, useState } from 'react'
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
import { Add20Regular, ArrowLeft20Regular, SearchInfo24Regular, TagMultiple24Regular } from '@fluentui/react-icons'
import { getAdminCategories } from '../api/adminCategories'
import EmptyState from '../components/EmptyState'
import LinkButton from '../components/LinkButton'
import { usePageStyles } from '../components/pageStyles'
import CategoriesTable from '../features/admin/CategoriesTable'
import CategoryFormDialog from '../features/admin/CategoryFormDialog'
import CategoryRowActions from '../features/admin/CategoryRowActions'
import SortControls from '../features/admin/SortControls'
import UsersFilters from '../features/admin/UsersFilters'
import { CATEGORY_SORT_OPTIONS, filterAndSortCategories } from '../features/admin/categoriesList'
import { CATEGORY_STATUS_LABELS } from '../features/admin/categoryStatus'
import { useApiQuery } from '../hooks/useApiQuery'
import { PATHS } from '../routes/paths'

const useStyles = makeStyles({
  actions: {
    display: 'flex',
    flexWrap: 'wrap',
    gap: tokens.spacingHorizontalS,
  },
})

// Listado de las categorías de productos (HU39): todas, también las inactivas, con su descripción, su estado y su
// número de productos. El backend las devuelve sin paginar, así que la búsqueda, el filtro por estado y la
// ordenación se hacen aquí. «Nueva categoría» abre el formulario de alta (HU40); la lupa de cada fila abre su ficha y
// la papelera la elimina (HU42).
export default function AdminCategoriesPage() {
  const styles = usePageStyles()
  const own = useStyles()
  const [search, setSearch] = useState('')
  const [status, setStatus] = useState('')
  const [sort, setSort] = useState('name')
  const [order, setOrder] = useState('asc')
  const [feedback, setFeedback] = useState(null)
  const [creando, setCreando] = useState(false)
  const { data, failed, retry } = useApiQuery(getAdminCategories)

  const categories = useMemo(
    () => (data ? filterAndSortCategories(data, { search, status, sort, order }) : []),
    [data, search, status, sort, order],
  )

  // Limpiar filtros mantiene la ordenación elegida, como en los demás listados del panel.
  const clearFilters = () => {
    setSearch('')
    setStatus('')
  }

  const renderActions = (category) => (
    <CategoryRowActions category={category} onResult={setFeedback} onChanged={retry} />
  )

  return (
    <main className={styles.page}>
      <div className={styles.heading}>
        <div className={styles.titleBlock}>
          <Title1 as="h1" className={styles.title}>
            Categorías
          </Title1>
          <Text className={styles.subtitle}>Consulta las categorías de productos y cuántos productos tiene cada una.</Text>
        </div>
        <div className={own.actions}>
          <LinkButton to={PATHS.ADMIN} appearance="subtle" icon={<ArrowLeft20Regular />}>
            Volver al panel
          </LinkButton>
          <Button appearance="primary" icon={<Add20Regular />} onClick={() => setCreando(true)}>
            Nueva categoría
          </Button>
        </div>
      </div>

      {data?.length > 0 && (
        <UsersFilters
          search={search}
          searchLabel="Buscar por nombre"
          searchPlaceholder="Escribe un nombre"
          status={status}
          statusLabels={CATEGORY_STATUS_LABELS}
          onSearchChange={setSearch}
          onStatusChange={setStatus}
        >
          <SortControls
            options={CATEGORY_SORT_OPTIONS}
            sort={sort}
            order={order}
            onSortChange={setSort}
            onOrderChange={setOrder}
          />
        </UsersFilters>
      )}

      {feedback && (
        <MessageBar intent={feedback.intent}>
          <MessageBarBody>{feedback.texto}</MessageBarBody>
        </MessageBar>
      )}

      {failed && (
        <MessageBar intent="error">
          <MessageBarBody>No se han podido cargar las categorías. Inténtalo de nuevo.</MessageBarBody>
          <MessageBarActions>
            <Button onClick={retry}>Reintentar</Button>
          </MessageBarActions>
        </MessageBar>
      )}

      {!data && !failed && <Spinner className={styles.loading} label="Cargando categorías…" />}

      {data?.length === 0 && (
        <EmptyState
          icon={<TagMultiple24Regular />}
          title="Todavía no hay categorías"
          description="Cuando se cree una categoría aparecerá aquí."
        />
      )}

      {data?.length > 0 && categories.length === 0 && (
        <EmptyState
          icon={<SearchInfo24Regular />}
          title="No hay categorías que coincidan con los filtros"
          description="Prueba con otro nombre u otro estado."
        >
          <Button onClick={clearFilters}>Limpiar filtros</Button>
        </EmptyState>
      )}

      {categories.length > 0 && <CategoriesTable categories={categories} renderActions={renderActions} />}

      {creando && <CategoryFormDialog onClose={() => setCreando(false)} />}
    </main>
  )
}
