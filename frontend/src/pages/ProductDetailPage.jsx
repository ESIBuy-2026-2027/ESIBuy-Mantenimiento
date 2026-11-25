import { useCallback, useEffect, useRef, useState } from 'react'
import { useParams } from 'react-router-dom'
import {
  Button,
  MessageBar,
  MessageBarActions,
  MessageBarBody,
  SkeletonItem,
  Skeleton,
  makeStyles,
  tokens,
} from '@fluentui/react-components'
import { ArrowLeft20Regular, BoxSearch24Regular } from '@fluentui/react-icons'
import { getCatalogProduct, getProductReviews, getProductReviewStatus } from '../api/products'
import { getWishlists } from '../api/wishlist'
import EmptyState from '../components/EmptyState'
import LinkButton from '../components/LinkButton'
import ProductDetail from '../features/catalog/ProductDetail'
import WishlistPickerDialog from '../features/wishlist/WishlistPickerDialog'
import { useApiQuery } from '../hooks/useApiQuery'
import { useCategories } from '../hooks/useCategories'
import { PATHS } from '../routes/paths'
import PropTypes from 'prop-types'

const NOT_FOUND = 404

const useStyles = makeStyles({
  page: {
    width: '100%',
    maxWidth: '1100px',
    margin: '0 auto',
    boxSizing: 'border-box',
    display: 'flex',
    flexDirection: 'column',
    gap: tokens.spacingVerticalXL,
    padding: `${tokens.spacingVerticalXL} ${tokens.spacingHorizontalL} ${tokens.spacingVerticalXXXL}`,
  },
  back: {
    alignSelf: 'flex-start',
  },
  skeleton: {
    display: 'grid',
    gridTemplateColumns: 'minmax(0, 1fr)',
    gap: tokens.spacingVerticalXXL,
    '@media (min-width: 860px)': {
      gridTemplateColumns: 'minmax(0, 1.1fr) minmax(0, 1fr)',
      gap: tokens.spacingHorizontalXXXL,
    },
  },
  skeletonImage: {
    aspectRatio: '4 / 3',
    height: 'auto',
    borderRadius: tokens.borderRadiusXXLarge,
  },
  skeletonInfo: {
    display: 'flex',
    flexDirection: 'column',
    gap: tokens.spacingVerticalM,
  },
  hidden: {
    position: 'absolute',
    width: '1px',
    height: '1px',
    overflow: 'hidden',
    clip: 'rect(0 0 0 0)',
    whiteSpace: 'nowrap',
  },
})

// Esqueleto de la página mientras llega el producto: respeta el reparto de la vista para que no salte al cargar.
function DetailSkeleton({ styles }) {
  return (
    <output aria-busy="true">
      <span className={styles.hidden}>Cargando producto…</span>
      <Skeleton aria-hidden="true" className={styles.skeleton}>
        <SkeletonItem className={styles.skeletonImage} />
        <div className={styles.skeletonInfo}>
          <SkeletonItem size={12} style={{ width: '30%' }} />
          <SkeletonItem size={40} style={{ width: '85%' }} />
          <SkeletonItem size={16} style={{ width: '45%' }} />
          <SkeletonItem size={16} style={{ width: '35%' }} />
          <SkeletonItem size={72} />
          <SkeletonItem size={16} />
          <SkeletonItem size={16} style={{ width: '90%' }} />
          <SkeletonItem size={16} style={{ width: '70%' }} />
        </div>
      </Skeleton>
    </output>
  )
}

DetailSkeleton.propTypes = {
  styles: PropTypes.objectOf(PropTypes.string).isRequired,
}

// Detalle de un producto para el cliente (HU8). El producto se pide al backend por su identificador (404 si no existe
// o no está disponible); la categoría llega como identificador y se muestra con su nombre.
export default function ProductDetailPage() {
  const styles = useStyles()
  const { id } = useParams()
  const fetcher = useCallback(() => getCatalogProduct(id), [id])
  const { data: product, error, failed, retry } = useApiQuery(fetcher)
  // Las valoraciones (HU13) se piden aparte: si fallan, el producto se sigue viendo
  const reviewsFetcher = useCallback(() => getProductReviews(id), [id])
  const { data: reviews, failed: reviewsFailed, retry: retryReviews } = useApiQuery(reviewsFetcher)
  // El estado determina si el cliente puede enviar una valoración (CLI-04).
  const reviewStatusFetcher = useCallback(() => getProductReviewStatus(id), [id])
  const { data: reviewStatus, failed: reviewStatusFailed, retry: retryReviewStatus } = useApiQuery(reviewStatusFetcher)
  const { categories } = useCategories()
  // Listas de deseos del cliente (HU14, PBI 534).
  const [listasDeseos, setListasDeseos] = useState([])
  const [selectorAbierto, setSelectorAbierto] = useState(false)
  const cambiado = useRef(false)
  const favorito = listasDeseos.some((lista) => lista.items.some((item) => item.productoId === id))

  const notFound = failed && error?.status === NOT_FOUND
  const categoryName = categories.find((category) => category.id === product?.categoriaId)?.nombre

  useEffect(() => {
    let cancelled = false
    getWishlists()
      .then((listas) => {
        if (!cancelled && !cambiado.current) setListasDeseos(listas)
      })
      .catch(() => { })
    return () => {
      cancelled = true
    }
  }, [id])

  return (
    <main className={styles.page}>
      {!notFound && (
        <LinkButton to={PATHS.PRODUCTS} appearance="subtle" icon={<ArrowLeft20Regular />} className={styles.back}>
          Volver a los productos
        </LinkButton>
      )}

      {notFound && (
        <EmptyState
          icon={<BoxSearch24Regular />}
          title="No se ha encontrado el producto"
          description="Puede que ya no esté disponible o que la dirección no sea correcta."
        >
          <LinkButton to={PATHS.PRODUCTS} appearance="primary" icon={<ArrowLeft20Regular />}>
            Volver a los productos
          </LinkButton>
        </EmptyState>
      )}

      {failed && !notFound && (
        <MessageBar intent="error">
          <MessageBarBody>No se ha podido cargar el producto. Inténtalo de nuevo.</MessageBarBody>
          <MessageBarActions>
            <Button onClick={retry}>Reintentar</Button>
          </MessageBarActions>
        </MessageBar>
      )}

      {!product && !failed && <DetailSkeleton styles={styles} />}

      {product && (
        <ProductDetail
          key={product.id}
          product={product}
          categoryName={categoryName}
          reviews={reviews}
          reviewsFailed={reviewsFailed}
          onRetryReviews={retryReviews}
          reviewStatus={reviewStatus}
          reviewStatusFailed={reviewStatusFailed}
          onRetryReviewStatus={retryReviewStatus}
          onReviewCreated={() => {
            retryReviews()
            retryReviewStatus()
          }}
          favorite={favorito}
          onAddFavorite={() => setSelectorAbierto(true)}
          onRemoveFavorite={() => setSelectorAbierto(true)}
        />
      )}
      {selectorAbierto && product && (
        <WishlistPickerDialog
          product={product}
          wishlists={listasDeseos}
          onClose={() => setSelectorAbierto(false)}
          onWishlistsChange={(nuevas) => {
            cambiado.current = true
            setListasDeseos(nuevas)
          }}
        />
      )}
    </main>
  )
}
