import PropTypes from 'prop-types'
import { categoryPropType, productValuesPropType } from '../../utils/propTypes'
import { useState } from 'react'
import {
  Button,
  Checkbox,
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
import { Save20Regular } from '@fluentui/react-icons'
import LinkButton from '../../components/LinkButton'
import { PATHS } from '../../routes/paths'
import DiscountField from './DiscountField'
import { toProductPayload, validateProduct } from './productValues'

const useStyles = makeStyles({
  form: {
    display: 'flex',
    flexDirection: 'column',
    gap: tokens.spacingVerticalL,
    width: '100%',
    boxSizing: 'border-box',
    padding: tokens.spacingVerticalXXL,
    backgroundColor: tokens.colorNeutralBackground1,
    border: `1px solid ${tokens.colorNeutralStroke2}`,
    borderRadius: tokens.borderRadiusXLarge,
    boxShadow: tokens.shadow4,
  },
  pair: {
    display: 'grid',
    gridTemplateColumns: 'repeat(auto-fit, minmax(200px, 1fr))',
    // Cada campo conserva su altura: así la ayuda del stock no descuadra el campo del precio.
    alignItems: 'start',
    gap: tokens.spacingHorizontalL,
  },
  categories: {
    display: 'flex',
    flexDirection: 'column',
    gap: tokens.spacingVerticalXS,
    margin: 0,
    padding: 0,
    border: 'none',
  },
  legend: {
    padding: 0,
    fontWeight: tokens.fontWeightSemibold,
  },
  error: {
    color: tokens.colorPaletteRedForeground1,
    fontSize: tokens.fontSizeBase200,
  },
  hint: {
    color: tokens.colorNeutralForeground3,
  },
  actions: {
    display: 'flex',
    flexWrap: 'wrap',
    gap: tokens.spacingHorizontalM,
  },
})

export default function ProductForm({ initialValues, categories, saving, saveFailed, onSubmit }) {
  const styles = useStyles()
  const [values, setValues] = useState(initialValues)
  const [attempted, setAttempted] = useState(false)
  // Hasta el primer intento de guardar no se muestra ningún error; después se validan en vivo.
  const errors = attempted ? validateProduct(values) : {}

  const setField = (field) => (value) => setValues((current) => ({ ...current, [field]: value }))
  const onText = (field) => (_event, data) => setField(field)(data.value)

  const toggleCategory = (categoryId, checked) =>
    setField('categoriaIds')(
      checked ? [...values.categoriaIds, categoryId] : values.categoriaIds.filter((id) => id !== categoryId),
    )

  const handleSubmit = (event) => {
    event.preventDefault()
    if (saving) return

    setAttempted(true)
    if (Object.keys(validateProduct(values)).length > 0) return
    onSubmit(toProductPayload(values))
  }

  return (
    <form className={styles.form} onSubmit={handleSubmit} noValidate>
      {saveFailed && (
        <MessageBar intent="error">
          <MessageBarBody>No se ha podido guardar el producto. Inténtalo de nuevo.</MessageBarBody>
        </MessageBar>
      )}

      <Field label="Nombre" required validationState={errors.nombre ? 'error' : 'none'} validationMessage={errors.nombre}>
        <Input value={values.nombre} onChange={onText('nombre')} maxLength={120} />
      </Field>

      <fieldset className={styles.categories}>
        <legend className={styles.legend}>Categorías *</legend>
        {categories.map((category) => (
          <Checkbox
            key={category.id}
            label={category.nombre}
            checked={values.categoriaIds.includes(category.id)}
            onChange={(_event, data) => toggleCategory(category.id, Boolean(data.checked))}
          />
        ))}
        {errors.categoriaIds && (
          <Text role="alert" className={styles.error}>
            {errors.categoriaIds}
          </Text>
        )}
      </fieldset>

      <div className={styles.pair}>
        <Field
          label="Precio (€)"
          required
          validationState={errors.precio ? 'error' : 'none'}
          validationMessage={errors.precio}
        >
          <Input type="number" step="0.01" value={values.precio} onChange={onText('precio')} />
        </Field>
        <Field
          label="Stock"
          hint="Si lo dejas vacío, el stock será 0."
          validationState={errors.stock ? 'error' : 'none'}
          validationMessage={errors.stock}
        >
          <Input type="number" step="1" value={values.stock} onChange={onText('stock')} />
        </Field>
      </div>

      <Field
        label="Descripción"
        validationState={errors.descripcion ? 'error' : 'none'}
        validationMessage={errors.descripcion}
      >
        <Textarea value={values.descripcion} onChange={onText('descripcion')} rows={4} resize="vertical" />
      </Field>

      <Field
        label="URL de la imagen"
        validationState={errors.imagen ? 'error' : 'none'}
        validationMessage={errors.imagen}
      >
        <Input type="url" value={values.imagen} onChange={onText('imagen')} />
      </Field>

      <DiscountField
        switchLabel="Activar descuento"
        percentLabel="Descuento (%)"
        active={values.descuentoActivo}
        percent={values.descuentoPorcentaje}
        error={errors.descuentoPorcentaje}
        onActiveChange={setField('descuentoActivo')}
        onPercentChange={setField('descuentoPorcentaje')}
      />
      <DiscountField
        switchLabel="Activar descuento premium"
        percentLabel="Descuento premium (%)"
        active={values.descuentoPremiumActivo}
        percent={values.descuentoPremiumPorcentaje}
        error={errors.descuentoPremiumPorcentaje}
        onActiveChange={setField('descuentoPremiumActivo')}
        onPercentChange={setField('descuentoPremiumPorcentaje')}
      />
      <Text className={styles.hint}>
        El descuento se aplica a todos los clientes y el descuento premium, solo a los clientes premium. Los
        descuentos se acumulan.
      </Text>

      <Switch
        label="Disponible para la venta"
        checked={values.disponible}
        onChange={(_event, data) => setField('disponible')(data.checked)}
      />

      <div className={styles.actions}>
        <Button
          type="submit"
          appearance="primary"
          disabled={saving}
          icon={saving ? <Spinner size="tiny" /> : <Save20Regular />}
        >
          Guardar
        </Button>
        <LinkButton to={PATHS.SELLER_PRODUCTS} appearance="subtle">
          Cancelar
        </LinkButton>
      </div>
    </form>
  )
}

ProductForm.propTypes = {
  initialValues: productValuesPropType.isRequired,
  categories: PropTypes.arrayOf(categoryPropType).isRequired,
  saving: PropTypes.bool.isRequired,
  saveFailed: PropTypes.bool.isRequired,
  onSubmit: PropTypes.func.isRequired,
}
