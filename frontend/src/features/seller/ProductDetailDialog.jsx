import PropTypes from 'prop-types'
import { useCallback } from 'react'
import {
  Button,
  Dialog,
  DialogActions,
  DialogBody,
  DialogContent,
  DialogSurface,
  DialogTitle,
  MessageBar,
  MessageBarActions,
  MessageBarBody,
  Spinner,
  makeStyles,
  tokens,
} from '@fluentui/react-components'
import { Delete20Regular, Dismiss24Regular, Edit20Regular } from '@fluentui/react-icons'
import { getSellerProduct } from '../../api/products'
import { useApiQuery } from '../../hooks/useApiQuery'
import { useCategories } from '../../hooks/useCategories'
import LinkButton from '../../components/LinkButton'
import { getSellerProductEditPath } from '../../routes/paths'
import { formatPrice } from '../../utils/format'
import ReadOnlyFields from '../admin/ReadOnlyFields'

const SIN_CATEGORIA = '—'
// El backend responde 404 a un producto ajeno; 403 se trata igual por si cambia (criterio de la HU33).
const NOT_FOUND_STATUSES = new Set([403, 404])

const useStyles = makeStyles({
  surface: {
    width: '100%',
    maxWidth: '760px',
  },
  content: {
    display: 'flex',
    flexDirection: 'column',
    gap: tokens.spacingVerticalL,
  },
})

const formatDiscount = (descuento) => (descuento?.activo ? `${descuento.porcentaje} %` : 'Sin descuento')

function productFields(product, categoryName) {
  return [
    ['Nombre', product.nombre],
    ['Descripción', product.descripcion],
    ['Precio', formatPrice(product.precio)],
    ['Stock', String(product.stock)],
    ['Categoría', categoryName ?? SIN_CATEGORIA],
    ['Imagen', product.imagen || 'Sin imagen'],
    ['Estado', product.disponible ? 'Disponible' : 'No disponible'],
    ['Descuento', formatDiscount(product.descuento)],
    ['Descuento premium', formatDiscount(product.descuentoPremium)],
  ]
}

// Ficha de un producto propio del vendedor (HU33), en solo lectura. El producto se pide al backend, que solo
// devuelve los del vendedor en sesión; la categoría llega como identificador y se muestra con su nombre.
// Editar lleva al formulario de edición (HU34); Eliminar se ve pero está deshabilitado hasta la HU35.
export default function ProductDetailDialog({ productId, onClose }) {
  const styles = useStyles()
  const fetcher = useCallback(() => getSellerProduct(productId), [productId])
  const { data: product, error, failed, retry } = useApiQuery(fetcher)
  const notFound = failed && NOT_FOUND_STATUSES.has(error?.status)
  const { categories } = useCategories()
  const categoryName = categories.find(({ id }) => id === product?.categoriaId)?.nombre

  return (
    <Dialog open modalType="modal" onOpenChange={(_event, data) => !data.open && onClose()}>
      <DialogSurface className={styles.surface}>
        <DialogBody>
          <DialogTitle
            action={
              <Button appearance="subtle" aria-label="Cerrar" icon={<Dismiss24Regular />} onClick={onClose} />
            }
          >
            Información del producto
          </DialogTitle>
          <DialogContent className={styles.content}>
            {notFound && (
              <MessageBar intent="warning">
                <MessageBarBody>No se ha encontrado el producto.</MessageBarBody>
              </MessageBar>
            )}

            {failed && !notFound && (
              <MessageBar intent="error">
                <MessageBarBody>No se ha podido cargar el producto.</MessageBarBody>
                <MessageBarActions>
                  <Button onClick={retry}>Reintentar</Button>
                </MessageBarActions>
              </MessageBar>
            )}

            {!product && !failed && <Spinner label="Cargando producto…" />}

            {product && <ReadOnlyFields fields={productFields(product, categoryName)} />}
          </DialogContent>
          {product && (
            <DialogActions>
              <LinkButton to={getSellerProductEditPath(product.id)} icon={<Edit20Regular />}>
                Editar
              </LinkButton>
              <Button icon={<Delete20Regular />} disabled>
                Eliminar
              </Button>
            </DialogActions>
          )}
        </DialogBody>
      </DialogSurface>
    </Dialog>
  )
}

ProductDetailDialog.propTypes = {
  productId: PropTypes.string.isRequired,
  onClose: PropTypes.func.isRequired,
}
