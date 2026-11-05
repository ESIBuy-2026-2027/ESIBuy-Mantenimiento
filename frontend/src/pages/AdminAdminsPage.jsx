import { Text, Title1, makeStyles, tokens } from '@fluentui/react-components'
import { ArrowLeft20Regular, PersonAdd20Regular } from '@fluentui/react-icons'
import { ROLES } from '../auth/roles'
import { useAuth } from '../auth/useAuth'
import LinkButton from '../components/LinkButton'
import { usePageStyles } from '../components/pageStyles'
import RoleUsersList from '../features/admin/RoleUsersList'
import { PATHS } from '../routes/paths'

const useStyles = makeStyles({
  actions: {
    display: 'flex',
    flexWrap: 'wrap',
    gap: tokens.spacingHorizontalS,
  },
})

// Listado de los administradores de ESIBuy (HU21), con búsqueda, filtro por estado y "Activar" (HU24).
export default function AdminAdminsPage() {
  const styles = usePageStyles()
  const { user } = useAuth()
  const own = useStyles()

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

      <RoleUsersList
        role={ROLES.ADMIN}
        itemLabel="administradores"
        errorMessage="No se ha podido cargar el listado de administradores. Inténtalo de nuevo."
        loadingLabel="Cargando administradores…"
        currentUserId={user?.id}
      />
    </main>
  )
}
