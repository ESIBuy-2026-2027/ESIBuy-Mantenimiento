import PropTypes from 'prop-types'
import { categoryPropType, productValuesPropType } from '../../utils/propTypes'
import { useEffect, useRef, useState } from 'react'
import {
  Button,
  Field,
  Input,
  MessageBar,
  MessageBarBody,
  Spinner,
  Switch,
  Text,
  Textarea,
  makeStyles,
  tokens,
} from '@fluentui/react-components'
import { Camera20Regular, Save20Regular } from '@fluentui/react-icons'
import CategoryCombobox from '../../components/CategoryCombobox'
import LinkButton from '../../components/LinkButton'
import { PATHS } from '../../routes/paths'
import DiscountField from './DiscountField'
import { toProductPayload, validateProduct } from './productValues'

const NOMBRE_EN_USO = 'Este nombre ya está en uso en tus productos. Usa uno distinto.'
const TAMANO_MAXIMO_IMAGEN = 5 * 1024 * 1024
const TIPOS_IMAGEN = ['image/jpeg', 'image/png']

const useStyles = makeStyles({
  form: {
    display: 'flex', flexDirection: 'column', gap: tokens.spacingVerticalL, width: '100%',
    boxSizing: 'border-box', padding: tokens.spacingVerticalXXL,
    backgroundColor: tokens.colorNeutralBackground1, border: `1px solid ${tokens.colorNeutralStroke2}`,
    borderRadius: tokens.borderRadiusXLarge, boxShadow: tokens.shadow4,
  },
  control: {
    borderRadius: tokens.borderRadiusLarge,
    boxShadow: tokens.shadow2,
    transitionProperty: 'border-color, box-shadow',
    transitionDuration: tokens.durationFaster,
    '&:focus-within': {
      borderColor: tokens.colorBrandStroke1,
      boxShadow: `0 0 0 1px ${tokens.colorBrandStroke1}`,
    },
  },
  pair: {
    display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(200px, 1fr))',
    alignItems: 'start', gap: tokens.spacingHorizontalL,
  },
  hint: { color: tokens.colorNeutralForeground3 },
  discountHint: {
    color: tokens.colorNeutralForeground3,
    fontSize: tokens.fontSizeBase200,
    lineHeight: tokens.lineHeightBase200,
  },
  discountHelp: {
    display: 'flex',
    flexDirection: 'column',
    gap: tokens.spacingVerticalXS,
  },
  actions: { display: 'flex', flexWrap: 'wrap', gap: tokens.spacingHorizontalM },
  preview: { maxWidth: '240px', maxHeight: '180px', objectFit: 'contain', borderRadius: tokens.borderRadiusMedium },
  imageDropzone: {
    position: 'relative',
    display: 'flex',
    flexDirection: 'column',
    alignItems: 'center',
    justifyContent: 'center',
    gap: tokens.spacingVerticalS,
    minHeight: '150px',
    padding: tokens.spacingVerticalXL,
    border: `1px dashed ${tokens.colorNeutralStroke1}`,
    borderRadius: tokens.borderRadiusXLarge,
    backgroundColor: tokens.colorNeutralBackground2,
    color: tokens.colorNeutralForeground2,
    textAlign: 'center',
    cursor: 'pointer',
    transitionProperty: 'border-color, background-color, box-shadow',
    transitionDuration: tokens.durationFaster,
    '&:hover, &:focus-within': {
      borderColor: tokens.colorBrandStroke1,
      boxShadow: tokens.shadow2,
    },
  },
  imageDropzoneActive: {
    borderColor: tokens.colorBrandStroke1,
    backgroundColor: tokens.colorBrandBackground2,
    boxShadow: `0 0 0 1px ${tokens.colorBrandStroke1}`,
  },
  imageInput: {
    position: 'absolute',
    inset: 0,
    width: '100%',
    height: '100%',
    opacity: 0,
    cursor: 'pointer',
  },
  imageIcon: { color: tokens.colorBrandForeground1 },
  imageTitle: { fontWeight: tokens.fontWeightSemibold },
})

export default function ProductForm({ initialValues, categories, saving, saveError, takenName = null, onSubmit }) {
  const styles = useStyles()
  const [values, setValues] = useState(initialValues)
  const [attempted, setAttempted] = useState(false)
  const [imageFile, setImageFile] = useState(null)
  const [imageError, setImageError] = useState(null)
  const [previewUrl, setPreviewUrl] = useState(null)
  const [draggingImage, setDraggingImage] = useState(false)
  const previewUrlRef = useRef(null)
  const imageInputRef = useRef(null)
  const errors = attempted ? validateProduct(values) : {}
  const nameTaken = takenName !== null && values.nombre.trim().toLowerCase() === takenName.toLowerCase()
  const nameError = errors.nombre ?? (nameTaken ? NOMBRE_EN_USO : undefined)

  useEffect(() => () => {
    if (previewUrlRef.current && typeof URL.revokeObjectURL === 'function') {
      URL.revokeObjectURL(previewUrlRef.current)
    }
  }, [])

  const updatePreview = (file) => {
    if (previewUrlRef.current && typeof URL.revokeObjectURL === 'function') {
      URL.revokeObjectURL(previewUrlRef.current)
    }
    previewUrlRef.current = file && typeof URL.createObjectURL === 'function'
      ? URL.createObjectURL(file)
      : null
    setPreviewUrl(previewUrlRef.current)
  }

  const setField = (field) => (value) => setValues((current) => ({ ...current, [field]: value }))
  const onText = (field) => (_event, data) => setField(field)(data.value)

  const handleImageFile = (file, inputElement = null) => {
    setImageError(null)
    if (!file) return
    if (!TIPOS_IMAGEN.includes(file.type)) {
      setImageFile(null)
      updatePreview(null)
      setImageError('Selecciona una imagen JPG o PNG.')
      if (inputElement) inputElement.value = ''
      return
    }
    if (file.size > TAMANO_MAXIMO_IMAGEN) {
      setImageFile(null)
      updatePreview(null)
      setImageError('La imagen no puede superar los 5 MB.')
      if (inputElement) inputElement.value = ''
      return
    }
    setImageFile(file)
    updatePreview(file)
    setField('imagen')('')
  }

  const handleImageChange = (event) => {
    handleImageFile(event.target.files?.[0] ?? null, event.target)
  }

  const handleImageDrop = (event) => {
    event.preventDefault()
    setDraggingImage(false)
    handleImageFile(event.dataTransfer.files?.[0] ?? null, imageInputRef.current)
  }

  const handleSubmit = (event) => {
    event.preventDefault()
    if (saving) return
    setAttempted(true)
    if (imageError || Object.keys(validateProduct(values)).length > 0) return
    onSubmit(toProductPayload(values), imageFile)
  }

  return (
    <form className={styles.form} onSubmit={handleSubmit} noValidate>
      {saveError && <MessageBar intent="error"><MessageBarBody>{saveError}</MessageBarBody></MessageBar>}
      <Field label="Nombre" required validationState={nameError ? 'error' : 'none'} validationMessage={nameError}>
        <Input className={styles.control} value={values.nombre} onChange={onText('nombre')} maxLength={120} />
      </Field>
      <Field label="Categoría" required validationState={errors.categoriaId ? 'error' : 'none'} validationMessage={errors.categoriaId}>
        <CategoryCombobox className={styles.control} value={values.categoriaId} categories={categories}
          searchable={false} placeholder="Selecciona una categoría" onChange={setField('categoriaId')} />
      </Field>
      <div className={styles.pair}>
        <Field label="Precio (€)" required validationState={errors.precio ? 'error' : 'none'} validationMessage={errors.precio}>
          <Input className={styles.control} type="text" inputMode="numeric" pattern="[0-9]*" value={values.precio}
            onChange={(_event, data) => setField('precio')(data.value.replace(/\D/g, ''))} />
        </Field>
        <Field label="Stock" hint="Si lo dejas vacío, el stock será 0."
          validationState={errors.stock ? 'error' : 'none'} validationMessage={errors.stock}>
          <Input className={styles.control} type="text" inputMode="numeric" pattern="[0-9]*" value={values.stock}
            onChange={(_event, data) => setField('stock')(data.value.replace(/\D/g, ''))} />
        </Field>
      </div>
      <Field label="Descripción" validationState={errors.descripcion ? 'error' : 'none'} validationMessage={errors.descripcion}>
        <Textarea className={styles.control} value={values.descripcion} onChange={onText('descripcion')} rows={4} resize="vertical" />
      </Field>
      <Field label="Añade una imagen" hint="Formatos JPG o PNG. Tamaño máximo: 5 MB."
        validationState={imageError ? 'error' : 'none'} validationMessage={imageError}>
        <label
          className={`${styles.imageDropzone} ${draggingImage ? styles.imageDropzoneActive : ''}`}
          onDragEnter={(event) => { event.preventDefault(); setDraggingImage(true) }}
          onDragOver={(event) => { event.preventDefault(); setDraggingImage(true) }}
          onDragLeave={(event) => {
            if (!event.currentTarget.contains(event.relatedTarget)) setDraggingImage(false)
          }}
          onDrop={handleImageDrop}
        >
          <Camera20Regular className={styles.imageIcon} fontSize={28} />
          <Text className={styles.imageTitle}>Arrastra una imagen aquí</Text>
          <Text>o haz clic para seleccionar un archivo</Text>
          <input
            ref={imageInputRef}
            className={styles.imageInput}
            aria-label="Archivo de imagen"
            type="file"
            accept="image/jpeg,image/png"
            onChange={handleImageChange}
          />
        </label>
      </Field>
      {(previewUrl || values.imagen) && (
        <div>
          <Text weight="semibold">Vista previa</Text>
          <div><img className={styles.preview} src={previewUrl || values.imagen} alt="Vista previa de la imagen del producto" /></div>
        </div>
      )}
      <div className={styles.pair}>
        <DiscountField label="Descuento general (%)" percent={values.descuentoPorcentaje}
          error={errors.descuentoPorcentaje} inputClassName={styles.control} onPercentChange={setField('descuentoPorcentaje')} />
        <DiscountField label="Descuento adicional Premium (%)" percent={values.descuentoPremiumPorcentaje}
          error={errors.descuentoPremiumPorcentaje} inputClassName={styles.control} onPercentChange={setField('descuentoPremiumPorcentaje')} />
      </div>
      <div className={styles.discountHelp}>
        <Text className={styles.discountHint}>
          Introduce un porcentaje entero entre 1 y 99. Si dejas un campo vacío, ese descuento no se aplicará.
        </Text>
        <Text className={styles.discountHint}>
          El descuento general se aplica a todos los clientes y el descuento Premium solo a clientes Premium.
        </Text>
        <Text className={styles.discountHint}>
          Ambos descuentos se acumulan.
        </Text>
      </div>
      <Switch label="Disponible para la venta" checked={values.disponible}
        onChange={(_event, data) => setField('disponible')(data.checked)} />
      <div className={styles.actions}>
        <Button type="submit" appearance="primary" disabled={saving}
          icon={saving ? <Spinner size="tiny" /> : <Save20Regular />}>Guardar</Button>
        <LinkButton to={PATHS.SELLER_PRODUCTS} appearance="subtle">Cancelar</LinkButton>
      </div>
    </form>
  )
}

ProductForm.propTypes = {
  initialValues: productValuesPropType.isRequired,
  categories: PropTypes.arrayOf(categoryPropType).isRequired,
  saving: PropTypes.bool.isRequired,
  saveError: PropTypes.string,
  takenName: PropTypes.string,
  onSubmit: PropTypes.func.isRequired,
}
