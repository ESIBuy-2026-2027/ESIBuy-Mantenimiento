import PropTypes from 'prop-types'
import { useCallback } from 'react'
import {
  Button,
  Dialog,
  DialogBody,
  DialogContent,
  DialogSurface,
  DialogTitle,
  Spinner,
  makeStyles,
  tokens,
} from '@fluentui/react-components'
import { Dismiss24Regular } from '@fluentui/react-icons'
import { getSellerProduct } from '../../api/products'
import { useApiQuery } from '../../hooks/useApiQuery'
import { useCategories } from '../../hooks/useCategories'
import { formatPrice } from '../../utils/format'
import ReadOnlyFields from '../admin/ReadOnlyFields'

const SIN_CATEGORIA = '—'

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
export default function ProductDetailDialog({ productId, onClose }) {
  const styles = useStyles()
  const fetcher = useCallback(() => getSellerProduct(productId), [productId])
  const { data: product } = useApiQuery(fetcher)
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
            {product ? (
              <ReadOnlyFields fields={productFields(product, categoryName)} />
            ) : (
              <Spinner label="Cargando producto…" />
            )}
          </DialogContent>
        </DialogBody>
      </DialogSurface>
    </Dialog>
  )
}

ProductDetailDialog.propTypes = {
  productId: PropTypes.string.isRequired,
  onClose: PropTypes.func.isRequired,
}
