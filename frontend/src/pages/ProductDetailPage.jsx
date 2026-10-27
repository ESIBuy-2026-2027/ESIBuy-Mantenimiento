import { useCallback } from 'react'
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
import { getCatalogProduct } from '../api/products'
import EmptyState from '../components/EmptyState'
import LinkButton from '../components/LinkButton'
import ProductDetail from '../features/catalog/ProductDetail'
import { useApiQuery } from '../hooks/useApiQuery'
import { useCategories } from '../hooks/useCategories'
import { PATHS } from '../routes/paths'

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
    <div role="status" aria-busy="true">
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
    </div>
  )
}

// Detalle de un producto para el cliente (HU8). El producto se pide al backend por su identificador (404 si no existe
// o no está disponible); la categoría llega como identificador y se muestra con su nombre.
export default function ProductDetailPage() {
  const styles = useStyles()
  const { id } = useParams()
  const fetcher = useCallback(() => getCatalogProduct(id), [id])
  const { data: product, error, failed, retry } = useApiQuery(fetcher)
  const { categories } = useCategories()
  const notFound = failed && error?.status === NOT_FOUND
  const categoryName = categories.find((category) => category.id === product?.categoriaId)?.nombre

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

      {product && <ProductDetail key={product.id} product={product} categoryName={categoryName} />}
    </main>
  )
}
