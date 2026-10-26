import { useState } from 'react'
import {
  Button,
  MessageBar,
  MessageBarActions,
  MessageBarBody,
  Spinner,
  Text,
  Title1,
  makeStyles,
  mergeClasses,
  tokens,
} from '@fluentui/react-components'
import { Box24Regular } from '@fluentui/react-icons'
import EmptyState from '../components/EmptyState'
import Pagination from '../components/Pagination'
import { usePageStyles } from '../components/pageStyles'
import ProductCard from '../features/catalog/ProductCard'
import { useCatalogProducts } from '../features/catalog/useCatalogProducts'
import { useCategories } from '../hooks/useCategories'

const PAGE_SIZE = 12

const useStyles = makeStyles({
  page: {
    maxWidth: '860px',
  },
  // Las tarjetas se colocan siempre de dos en dos, también en pantallas estrechas.
  grid: {
    display: 'grid',
    gridTemplateColumns: 'repeat(2, minmax(0, 1fr))',
    gap: tokens.spacingHorizontalL,
    margin: 0,
    padding: 0,
    listStyleType: 'none',
  },
  refreshing: {
    opacity: 0.6,
  },
})

// Vista de productos: lo primero que ve el cliente al iniciar sesión (HU7).
export default function ProductsPage() {
  const pageStyles = usePageStyles()
  const styles = useStyles()
  const [page, setPage] = useState(1)
  const { categories } = useCategories()
  const { data, failed, refreshing, retry } = useCatalogProducts({ page, size: PAGE_SIZE })
  const categoryNames = new Map(categories.map(({ id, nombre }) => [id, nombre]))

  return (
    <main className={mergeClasses(pageStyles.page, styles.page)}>
      <div className={pageStyles.titleBlock}>
        <Title1 as="h1" className={pageStyles.title}>
          Productos
        </Title1>
        <Text className={pageStyles.subtitle}>Descubre lo que ofrecen los vendedores de ESIBuy.</Text>
      </div>

      {failed && (
        <MessageBar intent="error">
          <MessageBarBody>No se han podido cargar los productos. Inténtalo de nuevo.</MessageBarBody>
          <MessageBarActions>
            <Button onClick={retry}>Reintentar</Button>
          </MessageBarActions>
        </MessageBar>
      )}

      {!data && !failed && <Spinner className={pageStyles.loading} label="Cargando productos…" />}

      {data && data.totalItems === 0 && (
        <EmptyState
          icon={<Box24Regular />}
          title="Todavía no hay productos"
          description="Cuando los vendedores publiquen sus productos, los verás aquí."
        />
      )}

      {data && data.totalItems > 0 && (
        <>
          <ul
            className={mergeClasses(styles.grid, refreshing && styles.refreshing)}
            aria-label="Productos"
            aria-busy={refreshing}
          >
            {data.items.map((product) => (
              <li key={product.id}>
                <ProductCard product={product} categoryName={categoryNames.get(product.categoriaId)} />
              </li>
            ))}
          </ul>
          <Pagination
            page={data.page}
            totalPages={data.totalPages}
            totalItems={data.totalItems}
            pageSize={data.size}
            itemLabel="productos"
            onPageChange={setPage}
          />
        </>
      )}
    </main>
  )
}
