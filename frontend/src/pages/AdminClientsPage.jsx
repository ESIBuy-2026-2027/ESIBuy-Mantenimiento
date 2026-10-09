import { Text, Title1 } from '@fluentui/react-components'
import { ArrowLeft20Regular } from '@fluentui/react-icons'
import { ROLES } from '../auth/roles'
import { useAuth } from '../auth/useAuth'
import LinkButton from '../components/LinkButton'
import { usePageStyles } from '../components/pageStyles'
import RoleUsersList from '../features/admin/RoleUsersList'
import { PATHS } from '../routes/paths'

// Listado de los clientes de ESIBuy, con búsqueda, filtro por estado y "Activar" (HU24).
export default function AdminClientsPage() {
  const styles = usePageStyles()
  const { user } = useAuth()

  return (
    <main className={styles.page}>
      <div className={styles.heading}>
        <div className={styles.titleBlock}>
          <Title1 as="h1" className={styles.title}>
            Clientes
          </Title1>
          <Text className={styles.subtitle}>Consulta las cuentas de los clientes y activa las desactivadas.</Text>
        </div>
        <LinkButton to={PATHS.ADMIN} appearance="subtle" icon={<ArrowLeft20Regular />}>
          Volver al panel
        </LinkButton>
      </div>

      <RoleUsersList
        role={ROLES.CLIENTE}
        itemLabel="clientes"
        errorMessage="No se ha podido cargar el listado de clientes. Inténtalo de nuevo."
        loadingLabel="Cargando clientes…"
        currentUserId={user?.id}
      />
    </main>
  )
}
