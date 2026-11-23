import { useCallback, useState } from 'react'
import {
  Button, MessageBar, MessageBarActions, MessageBarBody, Spinner, Text, Title1, makeStyles, mergeClasses,
} from '@fluentui/react-components'
import { useNavigate, useParams } from 'react-router-dom'
import {
  createSellerProduct, getSellerProduct, updateSellerProduct, uploadSellerProductImage,
} from '../api/products'
import { usePageStyles } from '../components/pageStyles'
import ProductForm from '../features/seller/ProductForm'
import { emptyProductValues, valuesFromProduct } from '../features/seller/productValues'
import { useApiQuery } from '../hooks/useApiQuery'
import { useCategories } from '../hooks/useCategories'
import { PATHS } from '../routes/paths'

const NOT_FOUND = 404
const DUPLICATE_NAME = 'PRODUCT_NAME_ALREADY_EXISTS'
const GENERIC_SAVE_ERROR = 'No se ha podido guardar el producto. Inténtalo de nuevo.'
const IMAGE_UPLOAD_ERROR = 'El producto se ha guardado, pero no se ha podido subir la imagen. Comprueba el archivo o inténtalo de nuevo.'

const useStyles = makeStyles({ page: { maxWidth: '720px' } })

// Una sola pantalla para dar de alta (sin :id en la ruta) y para editar (con :id).
export default function SellerProductFormPage() {
  const styles = usePageStyles()
  const narrow = useStyles()
  const { id } = useParams()
  const navigate = useNavigate()
  const isEdit = Boolean(id)
  const [saving, setSaving] = useState(false)
  const [saveError, setSaveError] = useState(null)
  const [takenName, setTakenName] = useState(null)
  // Si el alta se completa pero falla la subida binaria, se conserva el id para reintentar mediante PUT.
  const [createdProductId, setCreatedProductId] = useState(null)
  const { categories, loaded: categoriesLoaded, failed: categoriesFailed, retry: retryCategories } = useCategories()
  const loadProduct = useCallback(() => (id ? getSellerProduct(id) : Promise.resolve(null)), [id])
  const { data: product, error: productError, failed: productFailed, retry: retryProduct } = useApiQuery(loadProduct)

  const guardarProducto = async (payload, productId) => {
    if (productId) {
      return updateSellerProduct(productId, payload)
    }

    const saved = await createSellerProduct(payload)
    if (saved?.id) setCreatedProductId(saved.id)

    return saved
  }

  const subirImagen = async (imageFile, savedId) => {
    if (!imageFile) return true

    if (!savedId) {
      setSaveError(IMAGE_UPLOAD_ERROR)
      return false
    }

    try {
      await uploadSellerProductImage(savedId, imageFile)
      return true
    } catch {
      setSaveError(IMAGE_UPLOAD_ERROR)
      return false
    }
  }

  const handleSubmit = async (payload, imageFile) => {
    if (saving) return

    setSaving(true)
    setSaveError(null)
    setTakenName(null)

    try {
      const productId = id || createdProductId
      const saved = await guardarProducto(payload, productId)
      const savedId = productId || saved?.id

      const imagenSubida = await subirImagen(imageFile, savedId)
      if (!imagenSubida) return

      navigate(PATHS.SELLER_PRODUCTS, {
        state: {
          notice: isEdit || productId
            ? 'Cambios guardados.'
            : 'Producto guardado correctamente.',
        },
      })
    } catch (error) {
      if (error?.code === DUPLICATE_NAME) {
        setTakenName(payload.nombre)
      } else {
        setSaveError(GENERIC_SAVE_ERROR)
      }
    } finally {
      setSaving(false)
    }
  }

  const productNotFound = productFailed && productError?.status === NOT_FOUND
  const productReady = !isEdit || product !== null
  const formReady = categoriesLoaded && productReady

  return (
    <main className={mergeClasses(styles.page, narrow.page)}>
      <div className={styles.titleBlock}>
        <Title1 as="h1" className={styles.title}>{isEdit ? 'Editar producto' : 'Añadir producto'}</Title1>
        <Text className={styles.subtitle}>Los campos marcados con * son obligatorios.</Text>
      </div>
      {categoriesFailed && (
        <MessageBar intent="error">
          <MessageBarBody>No se han podido cargar las categorías.</MessageBarBody>
          <MessageBarActions><Button onClick={retryCategories}>Reintentar</Button></MessageBarActions>
        </MessageBar>
      )}
      {productNotFound && <MessageBar intent="error"><MessageBarBody>No se ha encontrado el producto.</MessageBarBody></MessageBar>}
      {productFailed && !productNotFound && (
        <MessageBar intent="error">
          <MessageBarBody>No se ha podido cargar el producto.</MessageBarBody>
          <MessageBarActions><Button onClick={retryProduct}>Reintentar</Button></MessageBarActions>
        </MessageBar>
      )}
      {!formReady && !categoriesFailed && !productFailed && <Spinner className={styles.loading} label="Cargando el formulario…" />}
      {formReady && (
        <ProductForm
          key={product?.id ?? 'nuevo'}
          initialValues={product ? valuesFromProduct(product) : emptyProductValues()}
          categories={categories}
          saving={saving}
          saveError={saveError}
          takenName={takenName}
          onSubmit={handleSubmit}
        />
      )}
    </main>
  )
}
