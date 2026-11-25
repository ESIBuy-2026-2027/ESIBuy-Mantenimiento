import PropTypes from 'prop-types'
import { useCallback, useId, useState } from 'react'
import {
  Badge,
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
import { Delete20Regular, Diamond16Filled, Diamond20Regular, Edit20Regular } from '@fluentui/react-icons'
import { addFavoriteCategory, getPreferences, removeFavoriteCategory } from '../api/preferences'
import { getProfile, updateProfile } from '../api/profile'
import { useAuth } from '../auth/useAuth'
import LinkButton from '../components/LinkButton'
import { usePageStyles } from '../components/pageStyles'
import { UserAvatar } from '../features/account/AvatarSelector'
import DeleteAccountDialog from '../features/account/DeleteAccountDialog'
import UnsavedChangesDialog from '../features/account/UnsavedChangesDialog'
import ProfileEditor from '../features/admin/ProfileEditor'
import ReadOnlyFields from '../features/admin/ReadOnlyFields'
import SellerStatistics from '../features/seller/SellerStatistics'
import { profileFieldsFor } from '../features/admin/profileFields'
import { useApiQuery } from '../hooks/useApiQuery'
import { useCategories } from '../hooks/useCategories'
import { useUnsavedChangesGuard } from '../hooks/useUnsavedChangesGuard'
import { PATHS } from '../routes/paths'
import { formatDate } from '../utils/format'
import SecurityFactors from '../features/account/SecurityFactors'

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
    flexWrap: 'wrap',
    alignItems: 'center',
    gap: tokens.spacingHorizontalL,
  },
  identityText: {
    display: 'flex',
    flexDirection: 'column',
    alignItems: 'flex-start',
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
  // Sin aviso no ocupa sitio en la tarjeta, pero sigue ahí para que los lectores de pantalla anuncien el siguiente
  favoritesNotice: {
    ':empty': { position: 'absolute' },
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
    <section aria-labelledby={titleId}>
      <Card className={styles.deletion}>
      <Title3 as="h2" id={titleId} className={styles.name}>
        Eliminar mi cuenta
      </Title3>
      <Text className={styles.preferencesText}>
        Dejarás de formar parte de ESIBuy. Tus pedidos y valoraciones se conservan.
      </Text>
      <Button className={styles.deletionAction} icon={<Delete20Regular />} onClick={() => setAbierto(true)}>
        Eliminar mi cuenta
      </Button>
      {abierto && (
        <DeleteAccountDialog
          rol={rol}
          confirmBeforeDelete
          onClose={() => setAbierto(false)}
          onDeleted={accountDeleted}
        />
      )}
      </Card>
    </section>
  )
}

AccountDeletion.propTypes = {
  rol: PropTypes.oneOf(['CLIENTE', 'VENDEDOR']).isRequired,
}

// Mensajes de las categorías favoritas, con el nombre de la categoría: nunca el texto del backend. Al marcar una que ya
// no existe o no está activa (404), reintentarlo no serviría.
const FAVORITAS_NO_CARGADAS = 'No se han podido cargar tus categorías favoritas. Inténtalo de nuevo.'
const avisoDeExito = (nombre, marcada) =>
  marcada
    ? `${nombre} se ha añadido a tus categorías favoritas.`
    : `${nombre} se ha quitado de tus categorías favoritas.`
const avisoDeFallo = (nombre, marcada, error) => {
  if (error?.code === 'CATEGORY_NOT_FOUND') return `${nombre} ya no está disponible.`
  return marcada
    ? `No se ha podido añadir ${nombre} a tus categorías favoritas. Inténtalo de nuevo.`
    : `No se ha podido quitar ${nombre} de tus categorías favoritas. Inténtalo de nuevo.`
}

// Categorías favoritas del cliente (AB#528, sobre las preferencias de la HU46): una casilla por cada categoría activa,
// marcadas sus favoritas. Cada una se marca o se desmarca por separado y se guarda al momento, así que al salir del
// perfil ya están guardadas; mientras se guarda, las casillas quedan deshabilitadas, igual que mientras cargan. La
// respuesta del backend es lo que se muestra después; si no se puede guardar, las casillas siguen como estaban. Si no
// cargan (las favoritas o las propias categorías), no se muestran y se puede reintentar.
function FavoriteCategories({ categories, categoriesLoaded, categoriesFailed, onRetryCategories }) {
  const styles = useStyles()
  const titleId = useId()
  const fetcher = useCallback(() => getPreferences(), [])
  const { data: cargadas, failed, retry } = useApiQuery(fetcher)
  const [actualizadas, setActualizadas] = useState(null)
  const [guardando, setGuardando] = useState(false)
  const [aviso, setAviso] = useState(null)
  const elegidas = new Set((actualizadas ?? cargadas)?.categorias ?? [])

  const cambiar = async ({ id, nombre }, marcada) => {
    setAviso(null)
    setGuardando(true)
    try {
      setActualizadas(await (marcada ? addFavoriteCategory(id) : removeFavoriteCategory(id)))
      setAviso({ intent: 'success', texto: avisoDeExito(nombre, marcada) })
    } catch (error) {
      setAviso({ intent: 'error', texto: avisoDeFallo(nombre, marcada, error) })
    } finally {
      setGuardando(false)
    }
  }

  const reintentar = () => {
    if (failed) retry()
    if (categoriesFailed) onRetryCategories()
  }

  const contenido = () => {
    if (failed || categoriesFailed) {
      return (
        <MessageBar intent="error">
          <MessageBarBody>{FAVORITAS_NO_CARGADAS}</MessageBarBody>
          <MessageBarActions>
            <Button onClick={reintentar}>Reintentar</Button>
          </MessageBarActions>
        </MessageBar>
      )
    }
    if (categoriesLoaded && categories.length === 0) {
      return <Text>Ahora mismo no hay categorías disponibles.</Text>
    }
    return (
      <fieldset aria-labelledby={titleId} className={styles.categories}>
        {categories.map((category) => (
          <Checkbox
            key={category.id}
            label={category.nombre}
            checked={elegidas.has(category.id)}
            disabled={guardando || !(actualizadas ?? cargadas)}
            onChange={(_event, data) => cambiar(category, data.checked === true)}
          />
        ))}
      </fieldset>
    )
  }

  return (
    <Card className={styles.card}>
      <Title3 as="h2" id={titleId} className={styles.name}>
        Mis categorías favoritas
      </Title3>
      <Text className={styles.preferencesText}>Marca las categorías que más te interesan.</Text>
      <output className={styles.favoritesNotice}>
        {aviso && (
          <MessageBar intent={aviso.intent}>
            <MessageBarBody>{aviso.texto}</MessageBarBody>
          </MessageBar>
        )}
      </output>
      {contenido()}
    </Card>
  )
}

FavoriteCategories.propTypes = {
  categories: PropTypes.arrayOf(PropTypes.shape({ id: PropTypes.string.isRequired, nombre: PropTypes.string.isRequired }))
    .isRequired,
  categoriesLoaded: PropTypes.bool.isRequired,
  categoriesFailed: PropTypes.bool.isRequired,
  onRetryCategories: PropTypes.func.isRequired,
}

// ESIBuy Premium en el perfil del cliente (HU45): el Estándar tiene el acceso a la vista Premium y el Premium, su
// distintivo.
function PremiumAccess({ tipoCliente }) {
  if (tipoCliente === 'PREMIUM') {
    return (
      <Badge appearance="filled" color="brand" size="large" icon={<Diamond16Filled />}>
        Premium
      </Badge>
    )
  }
  return (
    <LinkButton to={PATHS.PREMIUM} appearance="primary" size="small" icon={<Diamond20Regular />}>
      Hazte Premium
    </LinkButton>
  )
}

PremiumAccess.propTypes = {
  tipoCliente: PropTypes.string,
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
// pasa a ser el que devuelve el backend y se avisa. Si hay cambios sin guardar, se pregunta antes de cancelar, de ir
// a otra pantalla y de cerrar o recargar la pestaña. El cliente ve además sus categorías favoritas (AB#528) y su acceso
// a ESIBuy Premium (HU45), y el vendedor, sus estadísticas (HU17). Los dos pueden eliminar su cuenta desde aquí (HU47 y HU25).
export default function ProfilePage() {
  const pageStyles = usePageStyles()
  const styles = useStyles()
  const fetcher = useCallback(() => getProfile(), [])
  const { data: cargado, failed, retry } = useApiQuery(fetcher)
  const {
    categories,
    loaded: categoriesLoaded,
    failed: categoriesFailed,
    retry: retryCategories,
  } = useCategories()
  const [editando, setEditando] = useState(false)
  const [conCambios, setConCambios] = useState(false)
  const [confirmandoCancelar, setConfirmandoCancelar] = useState(false)
  const [guardado, setGuardado] = useState(null)
  const [aviso, setAviso] = useState('')
  const guard = useUnsavedChangesGuard(editando && conCambios)
  const perfil = guardado ?? cargado
  const categoryName = categories.find(({ id }) => id === perfil?.categoriaPrincipalId)?.nombre

  const empezarAEditar = () => {
    setAviso('')
    setEditando(true)
  }

  const dejarDeEditar = () => {
    setEditando(false)
    setConCambios(false)
    setConfirmandoCancelar(false)
  }

  const alGuardar = (_enviado, actualizado) => {
    setGuardado(actualizado)
    setAviso(CAMBIOS_GUARDADOS)
    dejarDeEditar()
  }

  const cancelar = () => (conCambios ? setConfirmandoCancelar(true) : dejarDeEditar())

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
        <output>
          {aviso && (
            <MessageBar intent="success">
              <MessageBarBody>{aviso}</MessageBarBody>
            </MessageBar>
          )}
        </output>
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
              {perfil.rol === 'CLIENTE' && <PremiumAccess tipoCliente={perfil.tipoCliente} />}
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
              onCancel={cancelar}
              onDirtyChange={setConCambios}
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

      {confirmandoCancelar && <UnsavedChangesDialog onStay={() => setConfirmandoCancelar(false)} onLeave={dejarDeEditar} />}
      {guard.blocked && <UnsavedChangesDialog onStay={guard.reset} onLeave={guard.proceed} />}

      {perfil?.rol === 'CLIENTE' && (
        <FavoriteCategories
          categories={categories}
          categoriesLoaded={categoriesLoaded}
          categoriesFailed={categoriesFailed}
          onRetryCategories={retryCategories}
        />
      )}
      {perfil?.rol === 'VENDEDOR' && <SellerStatistics />}

      {(perfil?.rol === 'CLIENTE' || perfil?.rol === 'VENDEDOR') && <SecurityFactors />}

      {(perfil?.rol === 'CLIENTE' || perfil?.rol === 'VENDEDOR') && <AccountDeletion rol={perfil.rol} />}
    </main>
  )
}
