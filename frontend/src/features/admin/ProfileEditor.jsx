import PropTypes from 'prop-types'
import { useState } from 'react'
import { Button, Field, Input, MessageBar, MessageBarBody, Select, Spinner, makeStyles, tokens } from '@fluentui/react-components'
import { Save20Regular } from '@fluentui/react-icons'
import { categoryPropType } from '../../utils/propTypes'
import AvatarSelector from '../account/AvatarSelector'
import { TIPOS_CLIENTE, initialValues, profileFieldsFor, profilePayload, validateProfile } from './profileFields'

const useStyles = makeStyles({
  form: {
    display: 'flex',
    flexDirection: 'column',
    gap: tokens.spacingVerticalL,
  },
  grid: {
    display: 'grid',
    gridTemplateColumns: 'repeat(auto-fit, minmax(240px, 1fr))',
    gap: tokens.spacingVerticalM,
    columnGap: tokens.spacingHorizontalL,
  },
  wide: {
    gridColumn: '1 / -1',
  },
  actions: {
    display: 'flex',
    flexWrap: 'wrap',
    gap: tokens.spacingHorizontalM,
  },
})

// Errores del backend que se muestran junto al campo que los provoca.
const ERRORES_DE_CAMPO = {
  EMAIL_ALREADY_REGISTERED: ['email', 'Este email ya está registrado'],
  COMMERCIAL_NAME_ALREADY_REGISTERED: ['nombreComercial', 'Este nombre comercial ya está registrado'],
}

// Formulario de edición de la ficha de un usuario (HU22). Muestra todos los campos de la ficha: los que se
// pueden modificar, como campos normales, y el resto en solo lectura (más oscuros). Hasta el primer intento
// de guardar no se muestra ningún error; después se validan en vivo, todos a la vez.
export default function ProfileEditor({ rol, usuario, categories = [], save, onSaved, onCancel }) {
  const styles = useStyles()
  const [values, setValues] = useState(() => initialValues(rol, usuario))
  const [attempted, setAttempted] = useState(false)
  const [saving, setSaving] = useState(false)
  const [saveFailed, setSaveFailed] = useState(false)
  const [serverErrors, setServerErrors] = useState({})
  const errors = { ...serverErrors, ...(attempted ? validateProfile(rol, values) : {}) }

  const change = (name, value) => {
    setValues((current) => ({ ...current, [name]: value }))
    setServerErrors((current) => Object.fromEntries(Object.entries(current).filter(([key]) => key !== name)))
  }

  const submit = async (event) => {
    event.preventDefault()
    if (saving) return

    setAttempted(true)
    if (Object.keys(validateProfile(rol, values)).length > 0) return

    setSaving(true)
    setSaveFailed(false)
    try {
      await save(profilePayload(rol, values))
      onSaved()
    } catch (error) {
      const [field, message] = ERRORES_DE_CAMPO[error?.code] ?? []
      if (field) setServerErrors({ [field]: message })
      else setSaveFailed(true)
      setSaving(false)
    }
  }

  const optionsFor = (name) =>
    name === 'tipoCliente'
      ? Object.entries(TIPOS_CLIENTE).map(([value, label]) => ({ value, label }))
      : categories.map(({ id, nombre }) => ({ value: id, label: nombre }))

  const renderField = (field) => {
    const edit = field.edit
    if (!edit) {
      return (
        <Field key={field.name} label={field.label}>
          <Input readOnly appearance="filled-darker" value={field.read(usuario)} />
        </Field>
      )
    }
    if (edit.type === 'avatar') {
      return (
        <div key={field.name} className={styles.wide}>
          <AvatarSelector value={values[field.name]} onChange={(_event, data) => change(field.name, data.value)} />
        </div>
      )
    }

    const fieldProps = {
      label: field.label,
      required: Boolean(edit.required),
      validationState: errors[field.name] ? 'error' : 'none',
      validationMessage: errors[field.name],
    }
    if (edit.type === 'select') {
      return (
        <Field key={field.name} {...fieldProps}>
          <Select value={values[field.name]} onChange={(_event, data) => change(field.name, data.value)}>
            {!edit.required && <option value="" />}
            {optionsFor(field.name).map(({ value, label }) => (
              <option key={value} value={value}>
                {label}
              </option>
            ))}
          </Select>
        </Field>
      )
    }
    return (
      <Field key={field.name} {...fieldProps}>
        <Input
          type={edit.type}
          maxLength={edit.maxLength}
          value={values[field.name]}
          onChange={(_event, data) => change(field.name, data.value)}
        />
      </Field>
    )
  }

  return (
    <form className={styles.form} onSubmit={submit} noValidate>
      {saveFailed && (
        <MessageBar intent="error">
          <MessageBarBody>No se han podido guardar los cambios. Inténtalo de nuevo.</MessageBarBody>
        </MessageBar>
      )}
      <div className={styles.grid}>{profileFieldsFor(rol).map(renderField)}</div>
      <div className={styles.actions}>
        <Button
          type="submit"
          appearance="primary"
          disabled={saving}
          icon={saving ? <Spinner size="tiny" /> : <Save20Regular />}
        >
          Guardar
        </Button>
        <Button type="button" onClick={onCancel} disabled={saving}>
          Cancelar
        </Button>
      </div>
    </form>
  )
}

ProfileEditor.propTypes = {
  rol: PropTypes.string.isRequired,
  usuario: PropTypes.shape({ id: PropTypes.string.isRequired }).isRequired,
  categories: PropTypes.arrayOf(categoryPropType),
  save: PropTypes.func.isRequired,
  onSaved: PropTypes.func.isRequired,
  onCancel: PropTypes.func.isRequired,
}
