import { useEffect, useState } from 'react'
import { Button, Spinner, Text, Title1, makeStyles, tokens } from '@fluentui/react-components'
import { SearchInfo24Regular } from '@fluentui/react-icons'
import { listUsers } from '../api/users'
import EmptyState from '../components/EmptyState'
import Pagination from '../components/Pagination'
import UsersFilters from '../features/admin/UsersFilters'
import UsersTable from '../features/admin/UsersTable'
import { USERS_PAGE_SIZE } from '../features/admin/usersConfig'
import { useDebouncedValue } from '../features/seller/useDebouncedValue'

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
  const [data, setData] = useState(null)
  const [search, setSearch] = useState('')
  const [role, setRole] = useState('')
  const [status, setStatus] = useState('')
  const [page, setPage] = useState(1)
  const debouncedSearch = useDebouncedValue(search)

  useEffect(() => {
    let cancelled = false

    listUsers({ search: debouncedSearch, role, status, page, size: USERS_PAGE_SIZE }).then((result) => {
      if (!cancelled) setData(result)
    })

    return () => {
      cancelled = true
    }
  }, [debouncedSearch, role, status, page])

  const changeFilter = (setFilter) => (value) => {
    setFilter(value)
    setPage(1)
  }

  const clearFilters = () => {
    setSearch('')
    setRole('')
    setStatus('')
    setPage(1)
  }

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
        onRoleChange={changeFilter(setRole)}
        onStatusChange={changeFilter(setStatus)}
      />

      {!data && <Spinner className={styles.loading} label="Cargando usuarios…" />}

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
          <UsersTable users={data.items} />
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
