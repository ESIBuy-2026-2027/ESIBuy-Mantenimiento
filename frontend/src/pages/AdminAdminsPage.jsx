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
import { ArrowLeft20Regular, PersonAdd20Regular } from '@fluentui/react-icons'
import { ROLES } from '../auth/roles'
import LinkButton from '../components/LinkButton'
import Pagination from '../components/Pagination'
import { usePageStyles } from '../components/pageStyles'
import UsersTable from '../features/admin/UsersTable'
import { USERS_PAGE_SIZE } from '../features/admin/usersConfig'
import { useUsersList } from '../features/admin/useUsersList'
import { PATHS } from '../routes/paths'

const useStyles = makeStyles({
  actions: {
    display: 'flex',
    flexWrap: 'wrap',
    gap: tokens.spacingHorizontalS,
  },
})

// Listado de los administradores de ESIBuy (HU21). Reutiliza el listado de usuarios de la HU19
// filtrado por rol.
export default function AdminAdminsPage() {
  const styles = usePageStyles()
  const own = useStyles()
  const [page, setPage] = useState(1)
  const { data, failed, refreshing, retry } = useUsersList({
    search: '',
    role: ROLES.ADMIN,
    status: '',
    page,
    size: USERS_PAGE_SIZE,
  })

  return (
    <main className={styles.page}>
      <div className={styles.heading}>
        <div className={styles.titleBlock}>
          <Title1 as="h1" className={styles.title}>
            Administradores
          </Title1>
          <Text className={styles.subtitle}>Personas del equipo con acceso al panel de control.</Text>
        </div>
        <div className={own.actions}>
          <LinkButton to={PATHS.ADMIN} appearance="subtle" icon={<ArrowLeft20Regular />}>
            Volver al panel
          </LinkButton>
          <LinkButton to={PATHS.ADMIN_NEW_ADMIN} appearance="primary" icon={<PersonAdd20Regular />}>
            Crear administrador
          </LinkButton>
        </div>
      </div>

      {failed && (
        <MessageBar intent="error">
          <MessageBarBody>No se ha podido cargar el listado de administradores. Inténtalo de nuevo.</MessageBarBody>
          <MessageBarActions>
            <Button onClick={retry}>Reintentar</Button>
          </MessageBarActions>
        </MessageBar>
      )}

      {!data && !failed && <Spinner className={styles.loading} label="Cargando administradores…" />}

      {data && (
        <>
          <UsersTable users={data.items} refreshing={refreshing} />
          <Pagination
            page={data.page}
            totalPages={data.totalPages}
            totalItems={data.totalItems}
            pageSize={data.size}
            itemLabel="administradores"
            onPageChange={setPage}
          />
        </>
      )}
    </main>
  )
}
