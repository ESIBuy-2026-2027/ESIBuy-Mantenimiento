import { useCallback } from 'react'
import {
  Button,
  Card,
  MessageBar,
  MessageBarActions,
  MessageBarBody,
  Spinner,
  Text,
  Title1,
  Title3,
  makeStyles,
  tokens,
} from '@fluentui/react-components'
import { getProfile } from '../api/profile'
import { usePageStyles } from '../components/pageStyles'
import { UserAvatar } from '../features/account/AvatarSelector'
import ReadOnlyFields from '../features/admin/ReadOnlyFields'
import SellerStatistics from '../features/seller/SellerStatistics'
import { profileFieldsFor } from '../features/admin/profileFields'
import { useApiQuery } from '../hooks/useApiQuery'
import { useCategories } from '../hooks/useCategories'
import { formatDate } from '../utils/format'

const SIN_DATO = '—'
// El perfil no trae el estado de la cuenta, y la imagen se muestra como avatar, no como texto.
const CAMPOS_FUERA_DEL_PERFIL = new Set(['estado', 'avatar'])

const useStyles = makeStyles({
  card: {
    gap: tokens.spacingVerticalL,
    padding: tokens.spacingVerticalXL,
  },
  identity: {
    display: 'flex',
    alignItems: 'center',
    gap: tokens.spacingHorizontalL,
  },
  identityText: {
    display: 'flex',
    flexDirection: 'column',
    gap: tokens.spacingVerticalXXS,
  },
  name: {
    margin: 0,
  },
  email: {
    color: tokens.colorNeutralForeground3,
  },
})

// Pares [etiqueta, texto] del perfil: los mismos campos y formatos que la ficha del administrador (HU20 y HU26),
// sin el estado ni la imagen, y con la fecha de alta. La categoría del vendedor llega como identificador.
function profileFields(perfil, categoryName) {
  const usuario = { ...perfil, categoriaPrincipal: categoryName }
  const campos = profileFieldsFor(perfil.rol)
    .filter(({ name }) => !CAMPOS_FUERA_DEL_PERFIL.has(name))
    .map((field) => [field.label, field.read(usuario, perfil.rol)])
  return [...campos, ['Fecha de alta', perfil.fechaAlta ? formatDate(perfil.fechaAlta) : SIN_DATO]]
}

// Perfil propio del cliente o del vendedor (HU4), en solo lectura. Los datos se piden al backend, que los saca de
// la sesión. Modificarlos es de la HU45. El vendedor ve además sus estadísticas (HU17).
export default function ProfilePage() {
  const pageStyles = usePageStyles()
  const styles = useStyles()
  const fetcher = useCallback(() => getProfile(), [])
  const { data: perfil, failed, retry } = useApiQuery(fetcher)
  const { categories } = useCategories()
  const categoryName = categories.find(({ id }) => id === perfil?.categoriaPrincipalId)?.nombre

  return (
    <main className={pageStyles.page}>
      <div className={pageStyles.titleBlock}>
        <Title1 as="h1" className={pageStyles.title}>
          Mi perfil
        </Title1>
        <Text className={pageStyles.subtitle}>Consulta tus datos personales.</Text>
      </div>

      {failed && (
        <MessageBar intent="error">
          <MessageBarBody>No se ha podido cargar tu perfil. Inténtalo de nuevo.</MessageBarBody>
          <MessageBarActions>
            <Button onClick={retry}>Reintentar</Button>
          </MessageBarActions>
        </MessageBar>
      )}

      {!perfil && !failed && <Spinner className={pageStyles.loading} label="Cargando tu perfil…" />}

      {perfil && (
        <Card className={styles.card}>
          <div className={styles.identity}>
            <UserAvatar value={perfil.avatar} name={`${perfil.nombre} ${perfil.apellidos}`} />
            <div className={styles.identityText}>
              <Title3 as="h2" className={styles.name}>
                {perfil.nombre} {perfil.apellidos}
              </Title3>
              <Text className={styles.email}>{perfil.email}</Text>
            </div>
          </div>
          <ReadOnlyFields fields={profileFields(perfil, categoryName)} />
        </Card>
      )}

      {perfil?.rol === 'VENDEDOR' && <SellerStatistics />}
    </main>
  )
}
