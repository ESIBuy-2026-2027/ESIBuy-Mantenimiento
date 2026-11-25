import { useCallback, useState } from 'react'
import PropTypes from 'prop-types'
import {
  Badge,
  Button,
  Card,
  Dialog,
  DialogActions,
  DialogBody,
  DialogContent,
  DialogSurface,
  DialogTitle,
  Field,
  Input,
  MessageBar,
  MessageBarActions,
  MessageBarBody,
  Spinner,
  Text,
  Title1,
  Title2,
  makeStyles,
  mergeClasses,
  tokens,
} from '@fluentui/react-components'
import { Add20Regular, Delete20Regular, Edit20Regular, Heart24Regular } from '@fluentui/react-icons'
import { Link } from 'react-router-dom'
import {
  createWishlist,
  deleteWishlist,
  getWishlists,
  removeItemFromWishlist,
  updateWishlistItemQuantity,
} from '../api/wishlist'
import EmptyState from '../components/EmptyState'
import { usePageStyles } from '../components/pageStyles'
import AddToCartButton from '../features/cart/AddToCartButton'
import EditWishlistDialog from '../features/wishlist/EditWishlistDialog'
import { useApiQuery } from '../hooks/useApiQuery'
import { getProductDetailPath } from '../routes/paths'
import { formatDate, formatPrice } from '../utils/format'

const useStyles = makeStyles({
  listsContainer: {
    display: 'flex',
    flexDirection: 'column',
    gap: tokens.spacingVerticalXL,
  },
  listCard: {
    display: 'flex',
    flexDirection: 'column',
    gap: tokens.spacingVerticalM,
  },
  listHeader: {
    display: 'flex',
    flexWrap: 'wrap',
    alignItems: 'center',
    justifyContent: 'space-between',
    gap: tokens.spacingHorizontalM,
  },
  listTitle: {
    display: 'flex',
    alignItems: 'center',
    gap: tokens.spacingHorizontalS,
  },
  listActions: {
    display: 'flex',
    flexWrap: 'wrap',
    gap: tokens.spacingHorizontalS,
  },
  list: {
    listStyle: 'none',
    margin: 0,
    padding: 0,
    display: 'flex',
    flexDirection: 'column',
    gap: tokens.spacingVerticalM,
  },
  item: {
    display: 'flex',
    flexDirection: 'row',
    flexWrap: 'wrap',
    alignItems: 'center',
    gap: tokens.spacingHorizontalL,
  },
  unavailable: {
    opacity: 0.6,
  },
  media: {
    flexShrink: 0,
    width: '72px',
    height: '72px',
    overflow: 'hidden',
    borderRadius: tokens.borderRadiusMedium,
    backgroundColor: tokens.colorNeutralBackground3,
  },
  image: {
    display: 'block',
    width: '100%',
    height: '100%',
    objectFit: 'cover',
  },
  placeholder: {
    display: 'flex',
    alignItems: 'center',
    justifyContent: 'center',
    width: '100%',
    height: '100%',
    color: tokens.colorNeutralForeground4,
  },
  placeholderImage: {
    width: '24px',
    height: '24px',
    objectFit: 'contain',
  },
  text: {
    display: 'flex',
    flexDirection: 'column',
    alignItems: 'flex-start',
    gap: tokens.spacingVerticalXXS,
    flexGrow: 1,
    minWidth: 0,
  },
  name: {
    fontWeight: tokens.fontWeightSemibold,
    color: tokens.colorBrandForeground1,
    overflowWrap: 'anywhere',
  },
  muted: {
    color: tokens.colorNeutralForeground3,
  },
  summary: {
    display: 'flex',
    alignItems: 'center',
    gap: tokens.spacingHorizontalM,
    marginLeft: 'auto',
  },
  qtyInput: {
    width: '72px',
  },
})

function availabilityBadge(item) {
  if (!item.disponible) return <Badge appearance="tint" color="informative">No disponible</Badge>
  if (item.stock <= 0) return <Badge appearance="tint" color="warning">Agotado</Badge>
  return null
}

function WishlistItem({ item, removing, updatingQty, onQuantityChange, onRemove }) {
  const styles = useStyles()
  const sinStock = !item.disponible || item.stock <= 0
  const cantidad = item.cantidad ?? 1
  const [draftCantidad, setDraftCantidad] = useState(null)

  const manejarCantidad = (_e, data) => {
    if (sinStock) return
    if (data.value === '') {
      setDraftCantidad('')
      return
    }
    const numero = Number.parseInt(data.value, 10)
    if (Number.isNaN(numero)) return
    const acotado = Math.max(1, Math.min(item.stock, numero))
    setDraftCantidad(null)
    if (acotado !== cantidad) onQuantityChange(item, acotado)
  }

  return (
    <Card className={mergeClasses(styles.item, !item.disponible && styles.unavailable)}>
      <div className={styles.media}>
        {item.imagen ? (
          <img className={styles.image} src={item.imagen} alt={item.nombre} loading="lazy" />
        ) : (
          <span className={styles.placeholder}>
            <img
              className={styles.placeholderImage}
              src="data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 24 24'%3E%3Cpath d='M3 3l18 18M10 12l-4 6h12l-3-4-2 2M6 6h11a2 2 0 0 1 2 2v8' fill='none' stroke='%23999999' stroke-width='1.5' stroke-linecap='round' stroke-linejoin='round'/%3E%3C/svg%3E"
              alt="Sin imagen"
            />
          </span>
        )}
      </div>
      <div className={styles.text}>
        {item.disponible ? (
          <Link to={getProductDetailPath(item.productoId)} className={styles.name}>
            {item.nombre}
          </Link>
        ) : (
          <Text className={styles.name}>{item.nombre}</Text>
        )}
        <Text size={200} className={styles.muted}>
          Añadido el {formatDate(item.addedAt)}
        </Text>
        {availabilityBadge(item)}
      </div>
      <div className={styles.summary}>
        <Input
          type="number"
          min={1}
          max={Math.max(1, item.stock)}
          aria-label={`Cantidad de ${item.nombre}`}
          className={styles.qtyInput}
          value={draftCantidad ?? String(cantidad)}
          disabled={sinStock || updatingQty}
          onChange={manejarCantidad}
          onBlur={() => setDraftCantidad(null)}
        />
        <Text weight="semibold">{formatPrice(item.precioFinal * cantidad)}</Text>
        <AddToCartButton product={{ ...item, id: item.productoId }} quantity={cantidad} size="small" />
        <Button
          appearance="subtle"
          icon={<Delete20Regular />}
          aria-label={`Quitar ${item.nombre} de la lista`}
          title="Quitar de la lista"
          disabled={removing}
          onClick={() => onRemove(item)}
        />
      </div>
    </Card>
  )
}

WishlistItem.propTypes = {
  item: PropTypes.shape({
    productoId: PropTypes.string.isRequired,
    nombre: PropTypes.string.isRequired,
    imagen: PropTypes.string,
    precioFinal: PropTypes.number.isRequired,
    stock: PropTypes.number.isRequired,
    disponible: PropTypes.bool.isRequired,
    addedAt: PropTypes.string.isRequired,
    cantidad: PropTypes.number,
  }).isRequired,
  removing: PropTypes.bool.isRequired,
  updatingQty: PropTypes.bool.isRequired,
  onQuantityChange: PropTypes.func.isRequired,
  onRemove: PropTypes.func.isRequired,
}

// Listas de deseos del cliente (HU14, PBI 534): puede crear una o varias listas, consultarlas,
// editarlas (nombre y productos) y eliminarlas.
export default function WishlistPage() {
  const pageStyles = usePageStyles()
  const styles = useStyles()
  const fetcher = useCallback(() => getWishlists(), [])
  const { data: cargadas, failed, retry } = useApiQuery(fetcher)
  const [actualizadas, setActualizadas] = useState(null)
  const [quitando, setQuitando] = useState(null)
  const [actualizandoCantidad, setActualizandoCantidad] = useState(null)
  const [aviso, setAviso] = useState(null)
  const [creando, setCreando] = useState(false)
  const [nuevoNombre, setNuevoNombre] = useState('')
  const [enviandoNueva, setEnviandoNueva] = useState(false)
  const [errorNueva, setErrorNueva] = useState(null)
  const [editandoId, setEditandoId] = useState(null)
  const [eliminando, setEliminando] = useState(null)
  const [borrando, setBorrando] = useState(false)

  const listas = actualizadas ?? cargadas
  const listaEditando = listas?.find((l) => l.id === editandoId) ?? null

  const quitar = async (listaId, item) => {
    setAviso(null)
    setQuitando(`${listaId}:${item.productoId}`)
    try {
      const actualizada = await removeItemFromWishlist(listaId, item.productoId)
      setActualizadas((prev) => (prev ?? cargadas).map((l) => (l.id === listaId ? actualizada : l)))
      setAviso({ intent: 'success', texto: `${item.nombre} se ha quitado de la lista.` })
    } catch {
      setAviso({ intent: 'error', texto: `No se ha podido quitar ${item.nombre}. Inténtalo de nuevo.` })
    } finally {
      setQuitando(null)
    }
  }

  const crear = async (event) => {
    event.preventDefault()
    const nombre = nuevoNombre.trim()
    if (!nombre) {
      setErrorNueva('El nombre de la lista es obligatorio.')
      return
    }
    setErrorNueva(null)
    setEnviandoNueva(true)
    try {
      const creada = await createWishlist(nombre)
      setActualizadas((prev) => [...(prev ?? cargadas ?? []), creada])
      setCreando(false)
      setNuevoNombre('')
      setAviso({ intent: 'success', texto: `Lista «${creada.nombre}» creada.` })
    } catch {
      setErrorNueva('No se ha podido crear la lista. Inténtalo de nuevo.')
    } finally {
      setEnviandoNueva(false)
    }
  }

  const cambiarCantidad = async (listaId, item, cantidad) => {
    setAviso(null)
    setActualizandoCantidad(`${listaId}:${item.productoId}`)
    try {
      const actualizada = await updateWishlistItemQuantity(listaId, item.productoId, cantidad)
      setActualizadas((prev) => (prev ?? cargadas).map((l) => (l.id === listaId ? actualizada : l)))
    } catch {
      setAviso({ intent: 'error', texto: `No se ha podido actualizar la cantidad de ${item.nombre}.` })
    } finally {
      setActualizandoCantidad(null)
    }
  }

  const confirmarEliminar = async () => {
    if (!eliminando || borrando) return
    setBorrando(true)
    try {
      await deleteWishlist(eliminando.id)
      setActualizadas(await getWishlists())
      setAviso({ intent: 'success', texto: `Lista «${eliminando.nombre}» eliminada.` })
      setEliminando(null)
    } catch {
      setAviso({ intent: 'error', texto: 'No se ha podido eliminar la lista. Inténtalo de nuevo.' })
      setEliminando(null)
    } finally {
      setBorrando(false)
    }
  }

  return (
    <main className={pageStyles.page}>
      <div className={pageStyles.heading}>
        <div className={pageStyles.titleBlock}>
          <Title1 as="h1" className={pageStyles.title}>
            Mis listas de deseos
          </Title1>
          <Text className={pageStyles.subtitle}>Organiza los productos que te interesan en tus listas.</Text>
        </div>
        <Button appearance="primary" icon={<Add20Regular />} onClick={() => setCreando(true)}>
          Nueva lista
        </Button>
      </div>

      {aviso && (
        <MessageBar intent={aviso.intent}>
          <MessageBarBody>{aviso.texto}</MessageBarBody>
        </MessageBar>
      )}
      {failed && (
        <MessageBar intent="error">
          <MessageBarBody>No se han podido cargar tus listas de deseos. Inténtalo de nuevo.</MessageBarBody>
          <MessageBarActions>
            <Button onClick={retry}>Reintentar</Button>
          </MessageBarActions>
        </MessageBar>
      )}
      {!listas && !failed && <Spinner label="Cargando tus listas de deseos…" className={pageStyles.loading} />}

      {listas?.length === 0 && (
        <EmptyState
          icon={<Heart24Regular />}
          title="Aún no tienes listas de deseos"
          description="Crea tu primera lista de deseos o pulsa el corazón de un producto del catálogo."
        >
          <Button appearance="primary" icon={<Add20Regular />} onClick={() => setCreando(true)}>
            Nueva lista
          </Button>
        </EmptyState>
      )}

      {listas && listas.length > 0 && (
        <div className={styles.listsContainer}>
          {listas.map((lista) => (
            <Card key={lista.id} className={styles.listCard}>
              <div className={styles.listHeader}>
                <div className={styles.listTitle}>
                  <Title2 as="h2">{lista.nombre}</Title2>
                  <Badge appearance="tint">{lista.items.length}</Badge>
                </div>
                <div className={styles.listActions}>
                  <Button
                    icon={<Edit20Regular />}
                    aria-label={`Editar «${lista.nombre}»`}
                    onClick={() => setEditandoId(lista.id)}
                  >
                    Editar
                  </Button>
                  <Button
                    icon={<Delete20Regular />}
                    aria-label={`Eliminar «${lista.nombre}»`}
                    onClick={() => setEliminando(lista)}
                  >
                    Eliminar
                  </Button>
                </div>
              </div>
              {lista.items.length === 0 ? (
                <Text size={200} className={styles.muted}>
                  Esta lista aún no tiene productos.
                </Text>
              ) : (
                <ul aria-label={`Productos de ${lista.nombre}`} className={styles.list}>
                  {lista.items.map((item) => (
                    <li key={item.productoId}>
                      <WishlistItem
                        item={item}
                        removing={quitando === `${lista.id}:${item.productoId}`}
                        updatingQty={actualizandoCantidad === `${lista.id}:${item.productoId}`}
                        onQuantityChange={(it, cantidad) => cambiarCantidad(lista.id, it, cantidad)}
                        onRemove={(it) => quitar(lista.id, it)}
                      />
                    </li>
                  ))}
                </ul>
              )}
            </Card>
          ))}
        </div>
      )}

      <Dialog open={creando} onOpenChange={(_e, data) => !data.open && !enviandoNueva && setCreando(false)}>
        <DialogSurface>
          <form onSubmit={crear} noValidate>
            <DialogBody>
              <DialogTitle>Nueva lista de deseos</DialogTitle>
              <DialogContent>
                {errorNueva && (
                  <MessageBar intent="error">
                    <MessageBarBody>{errorNueva}</MessageBarBody>
                  </MessageBar>
                )}
                <Field label="Nombre de la lista" required>
                  <Input
                    value={nuevoNombre}
                    placeholder="Ej. Regalos de cumpleaños"
                    maxLength={80}
                    disabled={enviandoNueva}
                    onChange={(_e, data) => setNuevoNombre(data.value)}
                  />
                </Field>
              </DialogContent>
              <DialogActions>
                <Button appearance="secondary" onClick={() => setCreando(false)} disabled={enviandoNueva}>
                  Cancelar
                </Button>
                <Button type="submit" appearance="primary" disabled={enviandoNueva}>
                  Crear lista
                </Button>
              </DialogActions>
            </DialogBody>
          </form>
        </DialogSurface>
      </Dialog>

      {listaEditando && (
        <EditWishlistDialog
          wishlist={listaEditando}
          onClose={() => setEditandoId(null)}
          onUpdated={(actualizada) =>
            setActualizadas((prev) => (prev ?? cargadas).map((l) => (l.id === actualizada.id ? actualizada : l)))
          }
        />
      )}

      {eliminando && (
        <Dialog open modalType="alert" onOpenChange={(_e, data) => !data.open && !borrando && setEliminando(null)}>
          <DialogSurface>
            <DialogBody>
              <DialogTitle>Eliminar lista</DialogTitle>
              <DialogContent>
                <Text>¿Seguro que quieres eliminar la lista «{eliminando.nombre}»?</Text>
              </DialogContent>
              <DialogActions>
                <Button appearance="secondary" onClick={() => setEliminando(null)} disabled={borrando}>
                  Cancelar
                </Button>
                <Button appearance="primary" onClick={confirmarEliminar} disabled={borrando}>
                  Sí, eliminar
                </Button>
              </DialogActions>
            </DialogBody>
          </DialogSurface>
        </Dialog>
      )}
    </main>
  )
}
