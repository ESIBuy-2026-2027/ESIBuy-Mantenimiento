import { useState } from 'react'
import PropTypes from 'prop-types'
import {
  Button,
  Dialog,
  DialogActions,
  DialogBody,
  DialogContent,
  DialogSurface,
  DialogTitle,
  Field,
  Input,
  MessageBar,
  MessageBarBody,
  Spinner,
  makeStyles,
  tokens,
} from '@fluentui/react-components'
import { renameWishlist } from '../../api/wishlist'

const useStyles = makeStyles({
  content: {
    display: 'flex',
    flexDirection: 'column',
    gap: tokens.spacingVerticalM,
  },
})

// Diálogo para editar el nombre de una lista de deseos (PBI 534).
export default function EditWishlistDialog({ wishlist, onClose, onUpdated }) {
  const styles = useStyles()
  const [nombre, setNombre] = useState(wishlist.nombre)
  const [guardando, setGuardando] = useState(false)
  const [error, setError] = useState(null)

  const guardar = async (event) => {
    event.preventDefault()
    const recortado = nombre.trim()
    if (!recortado) {
      setError('El nombre de la lista es obligatorio.')
      return
    }
    setError(null)
    setGuardando(true)
    try {
      const actualizada = await renameWishlist(wishlist.id, recortado)
      onUpdated(actualizada)
      onClose()
    } catch {
      setError('No se ha podido cambiar el nombre de la lista.')
      setGuardando(false)
    }
  }

  return (
    <Dialog open onOpenChange={(_event, data) => !data.open && !guardando && onClose()}>
      <DialogSurface>
        <form onSubmit={guardar} noValidate>
          <DialogBody>
            <DialogTitle>Editar lista</DialogTitle>
            <DialogContent className={styles.content}>
              {error && (
                <MessageBar intent="error">
                  <MessageBarBody>{error}</MessageBarBody>
                </MessageBar>
              )}
              <Field label="Nombre de la lista" required>
                <Input
                  value={nombre}
                  maxLength={80}
                  disabled={guardando}
                  onChange={(_e, data) => setNombre(data.value)}
                />
              </Field>
            </DialogContent>
            <DialogActions>
              <Button appearance="secondary" onClick={onClose} disabled={guardando}>
                Cancelar
              </Button>
              <Button
                type="submit"
                appearance="primary"
                disabled={guardando}
                icon={guardando ? <Spinner size="tiny" /> : undefined}
              >
                Guardar nombre
              </Button>
            </DialogActions>
          </DialogBody>
        </form>
      </DialogSurface>
    </Dialog>
  )
}

EditWishlistDialog.propTypes = {
  wishlist: PropTypes.shape({
    id: PropTypes.string.isRequired,
    nombre: PropTypes.string.isRequired,
  }).isRequired,
  onClose: PropTypes.func.isRequired,
  onUpdated: PropTypes.func.isRequired,
}
