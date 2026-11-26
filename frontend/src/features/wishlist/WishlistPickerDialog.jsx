import { useState } from 'react'
import PropTypes from 'prop-types'
import {
  Button,
  Checkbox,
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
  makeStyles,
  tokens,
} from '@fluentui/react-components'
import { Add20Regular } from '@fluentui/react-icons'
import { addItemToWishlist, createWishlist, removeItemFromWishlist } from '../../api/wishlist'

const useStyles = makeStyles({
  content: {
    display: 'flex',
    flexDirection: 'column',
    gap: tokens.spacingVerticalM,
  },
  lists: {
    display: 'flex',
    flexDirection: 'column',
    gap: tokens.spacingVerticalXS,
  },
  newForm: {
    display: 'flex',
    gap: tokens.spacingHorizontalS,
    alignItems: 'flex-end',
  },
  newField: {
    flexGrow: 1,
  },
  muted: {
    color: tokens.colorNeutralForeground3,
  },
})

// Diálogo para elegir en qué lista(s) guardar un producto o crear una nueva lista desde ahí (PBI 534). `wishlists` es null
// mientras no se han cargado; si no se han podido cargar (`failed`), se puede reintentar.
export default function WishlistPickerDialog({ product, wishlists, failed, onRetry, onClose, onWishlistsChange }) {
  const styles = useStyles()
  const [busyId, setBusyId] = useState(null)
  const [nombreNueva, setNombreNueva] = useState('')
  const [creando, setCreando] = useState(false)
  const [error, setError] = useState(null)

  const toggleLista = async (lista) => {
    if (busyId || creando) return
    setError(null)
    setBusyId(lista.id)
    const yaEsta = lista.items.some((item) => item.productoId === product.id)
    try {
      const actualizada = yaEsta
        ? await removeItemFromWishlist(lista.id, product.id)
        : await addItemToWishlist(lista.id, product.id)
      onWishlistsChange(wishlists.map((l) => (l.id === lista.id ? actualizada : l)))
    } catch {
      setError('No se ha podido actualizar la lista. Inténtalo de nuevo.')
    } finally {
      setBusyId(null)
    }
  }

  const crearYGuardar = async (event) => {
    event.preventDefault()
    const nombre = nombreNueva.trim()
    if (!nombre) {
      setError('Escribe un nombre para la nueva lista.')
      return
    }
    if (creando || busyId) return

    setError(null)
    setCreando(true)
    try {
      const creada = await createWishlist(nombre)
      const conProducto = await addItemToWishlist(creada.id, product.id)
      onWishlistsChange([...wishlists, conProducto])
      setNombreNueva('')
    } catch {
      setError('No se ha podido crear la lista. Inténtalo de nuevo.')
    } finally {
      setCreando(false)
    }
  }

  return (
    <Dialog open onOpenChange={(_event, data) => !data.open && !busyId && !creando && onClose()}>
      <DialogSurface>
        <DialogBody>
          <DialogTitle>Guardar en lista de deseos</DialogTitle>
          <DialogContent className={styles.content}>
            <Text weight="semibold">{product.nombre}</Text>
            {error && (
              <MessageBar intent="error">
                <MessageBarBody>{error}</MessageBarBody>
              </MessageBar>
            )}
            {!wishlists && failed && (
              <MessageBar intent="error">
                <MessageBarBody>No se han podido cargar tus listas. Inténtalo de nuevo.</MessageBarBody>
                <MessageBarActions>
                  <Button onClick={onRetry}>Reintentar</Button>
                </MessageBarActions>
              </MessageBar>
            )}
            {!wishlists && !failed && <Spinner label="Cargando tus listas…" />}
            {wishlists?.length === 0 && (
              <Text size={200} className={styles.muted}>
                Aún no tienes listas de deseos. Crea una abajo para guardar este producto.
              </Text>
            )}
            {wishlists?.length > 0 && (
              <div className={styles.lists}>
                {wishlists.map((lista) => {
                  const checked = lista.items.some((item) => item.productoId === product.id)
                  return (
                    <Checkbox
                      key={lista.id}
                      label={`${lista.nombre} (${lista.items.length})`}
                      checked={checked}
                      disabled={busyId === lista.id || creando}
                      onChange={() => toggleLista(lista)}
                    />
                  )
                })}
              </div>
            )}
            {wishlists && (
              <form onSubmit={crearYGuardar} className={styles.newForm} noValidate>
                <Field label="Nueva lista" className={styles.newField}>
                  <Input
                    value={nombreNueva}
                    placeholder="Ej. Regalos"
                    maxLength={80}
                    disabled={creando}
                    onChange={(_e, data) => setNombreNueva(data.value)}
                  />
                </Field>
                <Button
                  type="submit"
                  appearance="primary"
                  disabled={creando || Boolean(busyId)}
                  icon={creando ? <Spinner size="tiny" /> : <Add20Regular />}
                >
                  Crear y guardar
                </Button>
              </form>
            )}
          </DialogContent>
          <DialogActions>
            <Button appearance="secondary" onClick={onClose} disabled={Boolean(busyId) || creando}>
              Cerrar
            </Button>
          </DialogActions>
        </DialogBody>
      </DialogSurface>
    </Dialog>
  )
}

WishlistPickerDialog.propTypes = {
  product: PropTypes.shape({
    id: PropTypes.string.isRequired,
    nombre: PropTypes.string.isRequired,
  }).isRequired,
  wishlists: PropTypes.arrayOf(
    PropTypes.shape({
      id: PropTypes.string.isRequired,
      nombre: PropTypes.string.isRequired,
      items: PropTypes.arrayOf(PropTypes.shape({ productoId: PropTypes.string.isRequired })).isRequired,
    }),
  ),
  failed: PropTypes.bool.isRequired,
  onRetry: PropTypes.func.isRequired,
  onClose: PropTypes.func.isRequired,
  onWishlistsChange: PropTypes.func.isRequired,
}
