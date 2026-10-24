import PropTypes from 'prop-types'
import { useState } from 'react'
import {
  Button,
  MessageBar,
  MessageBarActions,
  MessageBarBody,
  Spinner,
  makeStyles,
  tokens,
} from '@fluentui/react-components'
import { SearchInfo24Regular } from '@fluentui/react-icons'
import { ROLES } from '../../auth/roles'
import EmptyState from '../../components/EmptyState'
import Pagination from '../../components/Pagination'
import { useDebouncedValue } from '../../hooks/useDebouncedValue'
import SortControls from './SortControls'
import UsersFilters from './UsersFilters'
import UserRowActions from './UserRowActions'
import UsersTable from './UsersTable'
import { ACCOUNT_SORT_OPTIONS, USERS_PAGE_SIZE, statusLabelsForRole } from './usersConfig'
import { useLastActiveAdmin } from './useLastActiveAdmin'
import { useUsersList } from './useUsersList'

const useStyles = makeStyles({
  loading: {
    padding: tokens.spacingVerticalXXXL,
  },
})

// Listado de los usuarios de un rol (clientes o administradores) con búsqueda por nombre, filtro por
// estado y ordenación por nombre o estado. Cada fila tiene la lupa que abre su ficha (HU20) y el botón para
// bloquear (HU23) o desbloquear (HU24) la cuenta. Reutiliza el listado de la HU19.
export default function RoleUsersList({ role, itemLabel, errorMessage, loadingLabel, currentUserId }) {
  const styles = useStyles()
  const [search, setSearch] = useState('')
  const [status, setStatus] = useState('')
  const [sort, setSort] = useState('name')
  const [order, setOrder] = useState('asc')
  const [page, setPage] = useState(1)
  const [feedback, setFeedback] = useState(null)
  const debouncedSearch = useDebouncedValue(search)
  const { data, failed, refreshing, retry } = useUsersList({
    search: debouncedSearch,
    role,
    status,
    page,
    size: USERS_PAGE_SIZE,
    searchBy: 'name',
    sort,
    order,
  })

  const changeFilter = (setFilter) => (value) => {
    setFilter(value)
    setPage(1)
  }

  // Limpiar filtros mantiene la ordenación elegida, como en el listado de vendedores (HU26).
  const clearFilters = () => {
    setSearch('')
    setStatus('')
    setPage(1)
  }

  // Solo los administradores pueden ser el último administrador activo.
  const { lastAdminId, refresh: refreshLastAdmin } = useLastActiveAdmin(role === ROLES.ADMIN)
  const changed = () => {
    retry()
    refreshLastAdmin()
  }

  const renderActions = (user) => (
    <UserRowActions
      user={user}
      currentUserId={currentUserId}
      lastAdminId={lastAdminId}
      onResult={setFeedback}
      onChanged={changed}
    />
  )

  return (
    <>
      <UsersFilters
        search={search}
        searchLabel="Buscar por nombre"
        searchPlaceholder="Escribe un nombre"
        status={status}
        statusLabels={statusLabelsForRole(role)}
        onSearchChange={changeFilter(setSearch)}
        onStatusChange={changeFilter(setStatus)}
      >
        <SortControls
          options={ACCOUNT_SORT_OPTIONS}
          sort={sort}
          order={order}
          onSortChange={changeFilter(setSort)}
          onOrderChange={changeFilter(setOrder)}
        />
      </UsersFilters>

      {feedback && (
        <MessageBar intent={feedback.intent}>
          <MessageBarBody>{feedback.texto}</MessageBarBody>
        </MessageBar>
      )}

      {failed && (
        <MessageBar intent="error">
          <MessageBarBody>{errorMessage}</MessageBarBody>
          <MessageBarActions>
            <Button onClick={retry}>Reintentar</Button>
          </MessageBarActions>
        </MessageBar>
      )}

      {!data && !failed && <Spinner className={styles.loading} label={loadingLabel} />}

      {data?.totalItems === 0 && (
        <EmptyState
          icon={<SearchInfo24Regular />}
          title={`No se han encontrado ${itemLabel}`}
          description="Prueba con otro nombre u otro estado."
        >
          <Button onClick={clearFilters}>Limpiar filtros</Button>
        </EmptyState>
      )}

      {data?.totalItems > 0 && (
        <>
          <UsersTable users={data.items} refreshing={refreshing} renderActions={renderActions} />
          <Pagination
            page={data.page}
            totalPages={data.totalPages}
            totalItems={data.totalItems}
            pageSize={data.size}
            itemLabel={itemLabel}
            onPageChange={setPage}
          />
        </>
      )}
    </>
  )
}

RoleUsersList.propTypes = {
  role: PropTypes.string.isRequired,
  itemLabel: PropTypes.string.isRequired,
  errorMessage: PropTypes.string.isRequired,
  loadingLabel: PropTypes.string.isRequired,
  currentUserId: PropTypes.string,
}
