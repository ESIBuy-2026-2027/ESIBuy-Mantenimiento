import PropTypes from 'prop-types'
import { useCallback, useId, useState } from 'react'
import {
  Button,
  Card,
  Checkbox,
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
import { Delete20Regular, Edit20Regular } from '@fluentui/react-icons'
import { getPreferences, savePreferences } from '../api/preferences'
import { getProfile, updateProfile } from '../api/profile'
import { useAuth } from '../auth/useAuth'
import { usePageStyles } from '../components/pageStyles'
import { UserAvatar } from '../features/account/AvatarSelector'
import DeleteAccountDialog from '../features/account/DeleteAccountDialog'
import ProfileEditor from '../features/admin/ProfileEditor'
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
  preferencesText: {
    color: tokens.colorNeutralForeground3,
  },
  categories: {
    display: 'flex',
    flexWrap: 'wrap',
    columnGap: tokens.spacingHorizontalL,
  },
  deletion: {
    gap: tokens.spacingVerticalM,
    padding: tokens.spacingVerticalXL,
  },
  deletionAction: {
    alignSelf: 'flex-start',
  },
  editAction: {
    alignSelf: 'flex-start',
  },
})

const CAMBIOS_GUARDADOS = 'Tus cambios se han guardado.'

// Eliminar la cuenta propia del vendedor (HU47) y del cliente (HU25): se confirma en un diálogo con la contraseña. El
// servidor cierra la sesión, así que después se cierra también en el navegador y el login lo avisa.
function AccountDeletion({ rol }) {
  const styles = useStyles()
  const titleId = useId()
  const { accountDeleted } = useAuth()
  const [abierto, setAbierto] = useState(false)

  return (
    <Card className={styles.deletion} aria-labelledby={titleId} role="region">
      <Title3 as="h2" id={titleId} className={styles.name}>
        Eliminar mi cuenta
      </Title3>
      <Text className={styles.preferencesText}>
        Dejarás de formar parte de ESIBuy. Tus pedidos y valoraciones se conservan.
      </Text>
      <Button className={styles.deletionAction} icon={<Delete20Regular />} onClick={() => setAbierto(true)}>
        Eliminar mi cuenta
      </Button>
      {abierto && <DeleteAccountDialog rol={rol} onClose={() => setAbierto(false)} onDeleted={accountDeleted} />}
    </Card>
  )
}

AccountDeletion.propTypes = {
  rol: PropTypes.oneOf(['CLIENTE', 'VENDEDOR']).isRequired,
}

// Preferencias del cliente (HU46): una casilla por cada categoría activa, marcadas las que ya le interesan. Cada
// cambio se guarda al momento, así que al salir del perfil ya están guardadas; mientras se guarda, las casillas
// quedan deshabilitadas, igual que mientras cargan. La respuesta del backend es lo que se muestra después; si no se
// pueden guardar, las casillas siguen como estaban. Si no cargan, no se muestran: guardar sin saber cuáles eligió
// borraría las demás.
function CustomerPreferences({ categories }) {
  const styles = useStyles()
  const titleId = useId()
  const fetcher = useCallback(() => getPreferences(), [])
  const { data: cargadas, failed, retry } = useApiQuery(fetcher)
  const [actualizadas, setActualizadas] = useState(null)
  const [guardando, setGuardando] = useState(false)
  const [aviso, setAviso] = useState(null)
  const elegidas = new Set((actualizadas ?? cargadas)?.categorias ?? [])

  const cambiar = async (categoryId, marcada) => {
    const nuevas = marcada ? [...elegidas, categoryId] : [...elegidas].filter((id) => id !== categoryId)
    setAviso(null)
    setGuardando(true)
    try {
      setActualizadas(await savePreferences(nuevas))
      setAviso({ intent: 'success', texto: 'Preferencias guardadas.' })
    } catch {
      setAviso({ intent: 'error', texto: 'No se han podido guardar tus preferencias. Inténtalo de nuevo.' })
    } finally {
      setGuardando(false)
    }
  }

  return (
    <Card className={styles.card}>
      <Title3 as="h2" id={titleId} className={styles.name}>
        Mis preferencias
      </Title3>
      <Text className={styles.preferencesText}>Elige las categorías que te interesan.</Text>
      {aviso && (
        <MessageBar intent={aviso.intent}>
          <MessageBarBody>{aviso.texto}</MessageBarBody>
        </MessageBar>
      )}
      {failed ? (
        <MessageBar intent="error">
          <MessageBarBody>No se han podido cargar tus preferencias. Inténtalo de nuevo.</MessageBarBody>
          <MessageBarActions>
            <Button onClick={retry}>Reintentar</Button>
          </MessageBarActions>
        </MessageBar>
      ) : (
        <div role="group" aria-labelledby={titleId} className={styles.categories}>
          {categories.map((category) => (
            <Checkbox
              key={category.id}
              label={category.nombre}
              checked={elegidas.has(category.id)}
              disabled={guardando || !(actualizadas ?? cargadas)}
              onChange={(_event, data) => cambiar(category.id, data.checked === true)}
            />
          ))}
        </div>
      )}
    </Card>
  )
}

CustomerPreferences.propTypes = {
  categories: PropTypes.arrayOf(PropTypes.shape({ id: PropTypes.string.isRequired, nombre: PropTypes.string.isRequired }))
    .isRequired,
}

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
// la sesión. Con «Editar perfil» (HU45) los datos modificables se editan en este mismo panel; al guardar, el perfil
// pasa a ser el que devuelve el backend y se avisa. El cliente ve además sus preferencias (HU46) y el vendedor, sus
// estadísticas (HU17). Los dos pueden eliminar su cuenta desde aquí (HU47 y HU25).
export default function ProfilePage() {
  const pageStyles = usePageStyles()
  const styles = useStyles()
  const fetcher = useCallback(() => getProfile(), [])
  const { data: cargado, failed, retry } = useApiQuery(fetcher)
  const { categories } = useCategories()
  const [editando, setEditando] = useState(false)
  const [guardado, setGuardado] = useState(null)
  const [aviso, setAviso] = useState('')
  const perfil = guardado ?? cargado
  const categoryName = categories.find(({ id }) => id === perfil?.categoriaPrincipalId)?.nombre

  const empezarAEditar = () => {
    setAviso('')
    setEditando(true)
  }

  const alGuardar = (_enviado, actualizado) => {
    setGuardado(actualizado)
    setAviso(CAMBIOS_GUARDADOS)
    setEditando(false)
  }

  return (
    <main className={pageStyles.page}>
      <div className={pageStyles.titleBlock}>
        <Title1 as="h1" className={pageStyles.title}>
          Mi perfil
        </Title1>
        <Text className={pageStyles.subtitle}>Consulta y edita tus datos personales.</Text>
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
        <div role="status">
          {aviso && (
            <MessageBar intent="success">
              <MessageBarBody>{aviso}</MessageBarBody>
            </MessageBar>
          )}
        </div>
      )}

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
          {editando ? (
            <ProfileEditor
              own
              rol={perfil.rol}
              usuario={perfil}
              categories={categories}
              save={updateProfile}
              onSaved={alGuardar}
              onCancel={() => setEditando(false)}
            />
          ) : (
            <>
              <ReadOnlyFields fields={profileFields(perfil, categoryName)} />
              <Button className={styles.editAction} icon={<Edit20Regular />} onClick={empezarAEditar}>
                Editar perfil
              </Button>
            </>
          )}
        </Card>
      )}

      {perfil?.rol === 'CLIENTE' && <CustomerPreferences categories={categories} />}
      {perfil?.rol === 'VENDEDOR' && <SellerStatistics />}
      {(perfil?.rol === 'CLIENTE' || perfil?.rol === 'VENDEDOR') && <AccountDeletion rol={perfil.rol} />}
    </main>
  )
}
