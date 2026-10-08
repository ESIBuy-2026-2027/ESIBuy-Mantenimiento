import { useCallback, useState } from 'react'
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
} from '@fluentui/react-components'
import { useNavigate, useParams } from 'react-router-dom'
import { createSellerProduct, getSellerProduct, updateSellerProduct } from '../api/products'
import { usePageStyles } from '../components/pageStyles'
import ProductForm from '../features/seller/ProductForm'
import { emptyProductValues, valuesFromProduct } from '../features/seller/productValues'
import { useApiQuery } from '../hooks/useApiQuery'
import { useCategories } from '../hooks/useCategories'
import { PATHS } from '../routes/paths'

const NOT_FOUND = 404

const useStyles = makeStyles({
  // Columna más estrecha que la de los listados; sigue centrada en la pantalla (margin auto).
  page: {
    maxWidth: '720px',
  },
})

// Una sola pantalla para dar de alta (sin :id en la ruta) y para editar (con :id).
export default function SellerProductFormPage() {
  const styles = usePageStyles()
  const narrow = useStyles()
  const { id } = useParams()
  const navigate = useNavigate()
  const isEdit = Boolean(id)
  const [saving, setSaving] = useState(false)
  const [saveFailed, setSaveFailed] = useState(false)
  const { categories, loaded: categoriesLoaded, failed: categoriesFailed, retry: retryCategories } = useCategories()
  const loadProduct = useCallback(() => (id ? getSellerProduct(id) : Promise.resolve(null)), [id])
  const { data: product, error: productError, failed: productFailed, retry: retryProduct } = useApiQuery(loadProduct)

  const handleSubmit = async (payload) => {
    setSaving(true)
    setSaveFailed(false)
    try {
      await (isEdit ? updateSellerProduct(id, payload) : createSellerProduct(payload))
      navigate(PATHS.SELLER_PRODUCTS, {
        state: { notice: isEdit ? 'Cambios guardados.' : 'Producto guardado correctamente.' },
      })
    } catch {
      setSaveFailed(true)
      setSaving(false)
    }
  }

  const productNotFound = productFailed && productError?.status === NOT_FOUND
  const productReady = !isEdit || product !== null
  const formReady = categoriesLoaded && productReady

  return (
    <main className={mergeClasses(styles.page, narrow.page)}>
      <div className={styles.titleBlock}>
        <Title1 as="h1" className={styles.title}>
          {isEdit ? 'Editar producto' : 'Añadir producto'}
        </Title1>
        <Text className={styles.subtitle}>Los campos marcados con * son obligatorios.</Text>
      </div>

      {categoriesFailed && (
        <MessageBar intent="error">
          <MessageBarBody>No se han podido cargar las categorías.</MessageBarBody>
          <MessageBarActions>
            <Button onClick={retryCategories}>Reintentar</Button>
          </MessageBarActions>
        </MessageBar>
      )}

      {productNotFound && (
        <MessageBar intent="error">
          <MessageBarBody>No se ha encontrado el producto.</MessageBarBody>
        </MessageBar>
      )}

      {productFailed && !productNotFound && (
        <MessageBar intent="error">
          <MessageBarBody>No se ha podido cargar el producto.</MessageBarBody>
          <MessageBarActions>
            <Button onClick={retryProduct}>Reintentar</Button>
          </MessageBarActions>
        </MessageBar>
      )}

      {!formReady && !categoriesFailed && !productFailed && (
        <Spinner className={styles.loading} label="Cargando el formulario…" />
      )}

      {formReady && (
        <ProductForm
          key={product?.id ?? 'nuevo'}
          initialValues={product ? valuesFromProduct(product) : emptyProductValues()}
          categories={categories}
          saving={saving}
          saveFailed={saveFailed}
          onSubmit={handleSubmit}
        />
      )}
    </main>
  )
}
