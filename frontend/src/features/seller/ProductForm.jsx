import PropTypes from 'prop-types'
import { categoryPropType, productValuesPropType } from '../../utils/propTypes'
import { useState } from 'react'
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
import { Save20Regular } from '@fluentui/react-icons'
import CategoryCombobox from '../../components/CategoryCombobox'
import LinkButton from '../../components/LinkButton'
import { PATHS } from '../../routes/paths'
import DiscountField from './DiscountField'
import { toProductPayload, validateProduct } from './productValues'

const NOMBRE_EN_USO = 'Este nombre ya está en uso en tus productos. Usa uno distinto.'

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
  hint: {
    color: tokens.colorNeutralForeground3,
  },
  actions: {
    display: 'flex',
    flexWrap: 'wrap',
    gap: tokens.spacingHorizontalM,
  },
})

export default function ProductForm({ initialValues, categories, saving, saveError, takenName = null, onSubmit }) {
  const styles = useStyles()
  const [values, setValues] = useState(initialValues)
  const [attempted, setAttempted] = useState(false)
  // Hasta el primer intento de guardar no se muestra ningún error; después se validan en vivo.
  const errors = attempted ? validateProduct(values) : {}
  // El backend rechaza el nombre si el vendedor ya lo usa (409); el aviso se quita en cuanto se cambia el nombre.
  // Como el backend, sin distinguir mayúsculas ni contar los espacios de los extremos.
  const nameTaken = takenName !== null && values.nombre.trim().toLowerCase() === takenName.toLowerCase()
  const nameError = errors.nombre ?? (nameTaken ? NOMBRE_EN_USO : undefined)

  const setField = (field) => (value) => setValues((current) => ({ ...current, [field]: value }))
  const onText = (field) => (_event, data) => setField(field)(data.value)

  const handleSubmit = (event) => {
    event.preventDefault()
    if (saving) return

    setAttempted(true)
    if (Object.keys(validateProduct(values)).length > 0) return
    onSubmit(toProductPayload(values))
  }

  return (
    <form className={styles.form} onSubmit={handleSubmit} noValidate>
      {saveError && (
        <MessageBar intent="error">
          <MessageBarBody>{saveError}</MessageBarBody>
        </MessageBar>
      )}

      <Field label="Nombre" required validationState={nameError ? 'error' : 'none'} validationMessage={nameError}>
        <Input value={values.nombre} onChange={onText('nombre')} maxLength={120} />
      </Field>

      <Field
        label="Categoría"
        required
        validationState={errors.categoriaId ? 'error' : 'none'}
        validationMessage={errors.categoriaId}
      >
        <CategoryCombobox
          value={values.categoriaId}
          categories={categories}
          placeholder="Escribe o elige una categoría"
          onChange={setField('categoriaId')}
        />
      </Field>

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
  saveError: PropTypes.string,
  // Nombre que el servidor ha rechazado por estar ya en uso, si lo hay.
  takenName: PropTypes.string,
  onSubmit: PropTypes.func.isRequired,
}
