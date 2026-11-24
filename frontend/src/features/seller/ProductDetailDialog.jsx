import PropTypes from 'prop-types'
import { useCallback } from 'react'
import {
  Button,
  Dialog,
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
import { Dismiss24Regular } from '@fluentui/react-icons'
import { getSellerProduct } from '../../api/products'
import { useApiQuery } from '../../hooks/useApiQuery'
import { useCategories } from '../../hooks/useCategories'
import { formatPrice } from '../../utils/format'
import ReadOnlyFields from '../admin/ReadOnlyFields'

const SIN_CATEGORIA = '—'
// El backend responde 404 a un producto ajeno; 403 se trata igual por si cambia (criterio de la HU33).
const NOT_FOUND_STATUSES = new Set([403, 404])

const fallbackImage = (nombre = '') => {
  const inicial = nombre.trim().charAt(0).toUpperCase() || 'P'
  const svg =
    `<svg xmlns="http://www.w3.org/2000/svg" width="220" height="220" viewBox="0 0 220 220">` +
    `<rect width="220" height="220" rx="12" fill="#FFF4EA"/>` +
    `<text x="110" y="138" text-anchor="middle" font-family="Segoe UI, Arial" font-size="92" ` +
    `font-weight="700" fill="#EA580C">${inicial}</text></svg>`
  return `data:image/svg+xml,${encodeURIComponent(svg)}`
}

const useStyles = makeStyles({
  surface: {
    width: '100%',
    maxWidth: '780px',
  },
  content: {
    display: 'flex',
    flexDirection: 'column',
    gap: tokens.spacingVerticalL,
  },
  topLayout: {
    display: 'flex',
    alignItems: 'flex-start',
    gap: tokens.spacingHorizontalL,
    '@media (max-width: 600px)': {
      flexDirection: 'column',
    },
  },
  previewFrame: {
    width: '190px',
    height: '220px',
    flexShrink: 0,
    borderRadius: tokens.borderRadiusMedium,
    border: `1px solid ${tokens.colorNeutralStroke2}`,
    backgroundColor: tokens.colorNeutralBackground2,
    overflow: 'hidden',
    display: 'flex',
    alignItems: 'center',
    justifyContent: 'center',
  },
  previewImage: {
    width: '100%',
    height: '100%',
    objectFit: 'cover',
  },
  sideFields: {
    flex: 1,
    minWidth: 0,
    display: 'flex',
    flexDirection: 'column',
    gap: tokens.spacingVerticalM,
  },
  bottomFields: {
    display: 'flex',
    flexDirection: 'column',
    gap: tokens.spacingVerticalM,
  },
})

const formatDiscount = (descuento) => (descuento?.activo ? `${descuento.porcentaje} %` : 'Sin descuento')

function sideProductFields(product, categoryName) {
  return [
    ['Nombre', product.nombre],
    ['Descripción', product.descripcion || '—'],
    ['Precio', formatPrice(product.precio)],
    ['Stock', String(product.stock)],
    ['Categoría', categoryName ?? SIN_CATEGORIA],
    ['Estado', product.disponible ? 'Disponible' : 'No disponible'],
  ]
}

function bottomProductFields(product) {
  return [
    ['Descuento', formatDiscount(product.descuento)],
    ['Descuento premium', formatDiscount(product.descuentoPremium)],
  ]
}

// Ficha de un producto propio del vendedor (HU33), en solo lectura. El producto se pide al backend, que solo
// devuelve los del vendedor en sesión; la categoría llega como identificador y se muestra con su nombre.
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

            {product && (
              <>
                <div className={styles.topLayout}>
                  <div className={styles.previewFrame}>
                    <img
                      className={styles.previewImage}
                      src={product.imagen || fallbackImage(product.nombre)}
                      alt={`Imagen de ${product.nombre}`}
                      onError={(event) => {
                        event.currentTarget.onerror = null
                        event.currentTarget.src = fallbackImage(product.nombre)
                      }}
                    />
                  </div>
                  <div className={styles.sideFields}>
                    <ReadOnlyFields fields={sideProductFields(product, categoryName)} />
                  </div>
                </div>
                <div className={styles.bottomFields}>
                  <ReadOnlyFields fields={bottomProductFields(product)} />
                </div>
              </>
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