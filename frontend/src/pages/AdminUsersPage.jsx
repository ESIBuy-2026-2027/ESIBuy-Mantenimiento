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
import { SearchInfo24Regular } from '@fluentui/react-icons'
import EmptyState from '../components/EmptyState'
import Pagination from '../components/Pagination'
import UserRowActions from '../features/admin/UserRowActions'
import UsersFilters from '../features/admin/UsersFilters'
import UsersTable from '../features/admin/UsersTable'
import { USER_STATUS, USERS_PAGE_SIZE, statusLabelsForRole } from '../features/admin/usersConfig'
import { useLastActiveAdmin } from '../features/admin/useLastActiveAdmin'
import { useUsersList } from '../features/admin/useUsersList'
import { ROLES } from '../auth/roles'
import { useAuth } from '../auth/useAuth'
import { useDebouncedValue } from '../hooks/useDebouncedValue'

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

export default function AdminUsersPage() {
  const styles = useStyles()
  const { user: currentUser } = useAuth()
  const [feedback, setFeedback] = useState(null)
  const [search, setSearch] = useState('')
  const [role, setRole] = useState('')
  const [status, setStatus] = useState('')
  const [page, setPage] = useState(1)
  const debouncedSearch = useDebouncedValue(search)
  const { data, failed, refreshing, retry } = useUsersList({
    search: debouncedSearch,
    role,
    status,
    page,
    size: USERS_PAGE_SIZE,
  })

  const changeFilter = (setFilter) => (value) => {
    setFilter(value)
    setPage(1)
  }

  // Al elegir administradores se quita el estado pendiente, que ellos no pueden tener.
  const changeRole = (value) => {
    setRole(value)
    if (value === ROLES.ADMIN && status === USER_STATUS.PENDIENTE) setStatus('')
    setPage(1)
  }

  const clearFilters = () => {
    setSearch('')
    setRole('')
    setStatus('')
    setPage(1)
  }

  const { lastAdminId, refresh: refreshLastAdmin } = useLastActiveAdmin()
  const changed = () => {
    retry()
    refreshLastAdmin()
  }

  const renderActions = (user) => (
    <UserRowActions
      user={user}
      currentUserId={currentUser?.id}
      lastAdminId={lastAdminId}
      onResult={setFeedback}
      onChanged={changed}
    />
  )

  return (
    <main className={styles.page}>
      <div className={styles.heading}>
        <Title1 as="h1" className={styles.title}>
          Usuarios
        </Title1>
        <Text className={styles.subtitle}>Busca y filtra las cuentas de ESIBuy.</Text>
      </div>

      <UsersFilters
        search={search}
        role={role}
        status={status}
        onSearchChange={changeFilter(setSearch)}
        onRoleChange={changeRole}
        statusLabels={statusLabelsForRole(role)}
        onStatusChange={changeFilter(setStatus)}
      />

      {feedback && (
        <MessageBar intent={feedback.intent}>
          <MessageBarBody>{feedback.texto}</MessageBarBody>
        </MessageBar>
      )}

      {failed && (
        <MessageBar intent="error">
          <MessageBarBody>No se ha podido cargar el listado de usuarios. Inténtalo de nuevo.</MessageBarBody>
          <MessageBarActions>
            <Button onClick={retry}>Reintentar</Button>
          </MessageBarActions>
        </MessageBar>
      )}

      {!data && !failed && <Spinner className={styles.loading} label="Cargando usuarios…" />}

      {data?.totalItems === 0 && (
        <EmptyState
          icon={<SearchInfo24Regular />}
          title="No se han encontrado usuarios"
          description="Prueba con otro nombre, otro email u otros filtros."
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
            itemLabel="usuarios"
            onPageChange={setPage}
          />
        </>
      )}
    </main>
  )
}
