import { useEffect, useState } from 'react'
import { Spinner, Text, Title1, makeStyles, tokens } from '@fluentui/react-components'
import { listUsers } from '../api/users'
import Pagination from '../components/Pagination'
import UsersTable from '../features/admin/UsersTable'
import { USERS_PAGE_SIZE } from '../features/admin/usersConfig'

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

  useEffect(() => {
    listUsers({ page: 1, size: USERS_PAGE_SIZE }).then(setData)
  }, [])

  return (
    <main className={styles.page}>
      <div className={styles.heading}>
        <Title1 as="h1" className={styles.title}>
          Usuarios
        </Title1>
        <Text className={styles.subtitle}>Busca y filtra las cuentas de ESIBuy.</Text>
      </div>

      {!data && <Spinner className={styles.loading} label="Cargando usuarios…" />}

      {data && (
        <>
          <UsersTable users={data.items} />
          <Pagination
            page={data.page}
            totalPages={data.totalPages}
            totalItems={data.totalItems}
            pageSize={data.size}
            itemLabel="usuarios"
            onPageChange={() => {}}
          />
        </>
      )}
    </main>
  )
}
