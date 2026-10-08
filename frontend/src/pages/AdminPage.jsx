import { Text, Title1, makeStyles, tokens } from '@fluentui/react-components'
import { BuildingShop24Regular, People24Regular, PersonKey24Regular } from '@fluentui/react-icons'
import { useNavigate } from 'react-router-dom'
import AdminSectionCard from '../features/admin/AdminSectionCard'
import { PATHS } from '../routes/paths'

const sections = [
  {
    id: 'customers',
    icon: <People24Regular />,
    title: 'Clientes',
    to: PATHS.ADMIN_CLIENTS,
    description: 'Consulta y gestiona las cuentas de los clientes de ESIBuy.',
  },
  {
    id: 'sellers',
    icon: <BuildingShop24Regular />,
    title: 'Vendedores',
    to: PATHS.ADMIN_SELLERS,
    description: 'Supervisa a los vendedores y consulta sus catálogos.',
  },
  {
    id: 'admins',
    icon: <PersonKey24Regular />,
    title: 'Administradores',
    to: PATHS.ADMIN_ADMINS,
    description: 'Gestiona al equipo con acceso al panel de control.',
  },
]

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
  grid: {
    display: 'grid',
    gridTemplateColumns: 'repeat(auto-fit, minmax(280px, 1fr))',
    gap: tokens.spacingHorizontalL,
  },
})

export default function AdminPage() {
  const styles = useStyles()
  const navigate = useNavigate()

  return (
    <main className={styles.page}>
      <div className={styles.heading}>
        <Title1 as="h1" className={styles.title}>
          Panel de control
        </Title1>
        <Text className={styles.subtitle}>Elige qué quieres gestionar.</Text>
      </div>

      <div className={styles.grid}>
        {sections.map((section) => (
          <AdminSectionCard
            key={section.id}
            icon={section.icon}
            title={section.title}
            description={section.description}
            onClick={section.to ? () => navigate(section.to) : undefined}
          />
        ))}
      </div>
    </main>
  )
}
