import PropTypes from 'prop-types'
import { useState } from 'react'
import {
  Button,
  Field,
  Input,
  MessageBar,
  MessageBarBody,
  Select,
  Spinner,
  makeStyles,
  tokens,
} from '@fluentui/react-components'
import { Save20Regular } from '@fluentui/react-icons'
import { ROLES } from '../../auth/roles'
import { categoryPropType } from '../../utils/propTypes'
import AvatarSelector from '../account/AvatarSelector'
import {
  TIPOS_CLIENTE,
  editFieldsFor,
  initialValues,
  profilePayload,
  validateProfile,
} from './profileFields'
import { ROLE_LABELS, STATUS_LABELS, USER_STATUS, effectiveStatus } from './usersConfig'

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
  COMMERCIAL_NAME_ALREADY_REGISTERED: ['nombreComercial', 'Este nombre comercial ya está registrado'],
}

// Errores del backend sobre el cambio de rol, que no son de un campo concreto.
const MENSAJES_DE_ROL = {
  CANNOT_CHANGE_OWN_ROLE: 'No puedes cambiar tu propio rol.',
  LAST_ACTIVE_ADMIN: 'No se puede degradar al último administrador.',
}

const MENSAJE_GENERICO = 'No se han podido guardar los cambios. Inténtalo de nuevo.'
const AYUDA_ROL_PROPIO = 'No puedes cambiar tu propio rol.'
const PLACEHOLDER_OPCION = 'Elige una opción'

const OPCIONES = {
  rol: ROLE_LABELS,
  tipoCliente: TIPOS_CLIENTE,
}

// Formulario de edición de la ficha de un usuario (HU22). Muestra todos los campos de la ficha: los que se
// pueden modificar, como campos normales, y el resto en solo lectura (más oscuros). El administrador puede
// cambiar el rol (salvo el suyo): al hacerlo, el formulario pasa a los campos del rol nuevo. Hasta el primer
// intento de guardar no se muestra ningún error; después se validan en vivo, todos a la vez.
export default function ProfileEditor({ rol, usuario, categories = [], lockRol = false, save, onSaved, onCancel }) {
  const styles = useStyles()
  const [values, setValues] = useState(() => initialValues(rol, usuario))
  const [attempted, setAttempted] = useState(false)
  const [saving, setSaving] = useState(false)
  const [saveError, setSaveError] = useState('')
  const [serverErrors, setServerErrors] = useState({})
  const rolDestino = values.rol
  const errors = { ...serverErrors, ...(attempted ? validateProfile(rol, values) : {}) }
  const pasaDePendienteAAdministrador =
    rolDestino === ROLES.ADMIN && usuario.estado === USER_STATUS.PENDIENTE

  const change = (name, value) => {
    setValues((current) => ({ ...current, [name]: value }))
    setServerErrors((current) => Object.fromEntries(Object.entries(current).filter(([key]) => key !== name)))
  }

  const submit = async (event) => {
    event.preventDefault()
    if (saving) return

    setAttempted(true)
    if (Object.keys(validateProfile(rol, values)).length > 0) return

    const payload = profilePayload(rol, values)
    setSaving(true)
    setSaveError('')
    try {
      await save(payload)
      onSaved(payload)
    } catch (error) {
      const [field, message] = ERRORES_DE_CAMPO[error?.code] ?? []
      if (field) setServerErrors({ [field]: message })
      else setSaveError(MENSAJES_DE_ROL[error?.code] ?? MENSAJE_GENERICO)
      setSaving(false)
    }
  }

  const optionsFor = (name) => {
    if (name === 'categoriaPrincipalId') return categories.map(({ id, nombre }) => ({ value: id, label: nombre }))
    return Object.entries(OPCIONES[name]).map(([value, label]) => ({ value, label }))
  }

  const renderSelect = (field, fieldProps) => (
    <Field
      key={field.name}
      {...fieldProps}
      hint={field.name === 'rol' && lockRol ? AYUDA_ROL_PROPIO : undefined}
    >
      <Select
        value={values[field.name]}
        disabled={field.name === 'rol' && lockRol}
        onChange={(_event, data) => change(field.name, data.value)}
      >
        {values[field.name] === '' && (
          <option value="" disabled>
            {PLACEHOLDER_OPCION}
          </option>
        )}
        {optionsFor(field.name).map(({ value, label }) => (
          <option key={value} value={value}>
            {label}
          </option>
        ))}
      </Select>
    </Field>
  )

  const renderField = (field) => {
    const edit = field.edit
    if (!edit) {
      const texto =
        field.name === 'estado'
          ? (STATUS_LABELS[effectiveStatus(usuario.estado, rolDestino)] ?? usuario.estado)
          : field.read(usuario, rol)
      return (
        <Field key={field.name} label={field.label}>
          <Input readOnly appearance="filled-darker" value={texto} />
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
    if (edit.type === 'select') return renderSelect(field, fieldProps)

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
      {saveError && (
        <MessageBar intent="error">
          <MessageBarBody>{saveError}</MessageBarBody>
        </MessageBar>
      )}
      {rolDestino !== rol && (
        <MessageBar intent="warning">
          <MessageBarBody>
            Vas a cambiar el rol de esta cuenta a {ROLE_LABELS[rolDestino]}. Rellena los datos que pide el rol
            nuevo.
          </MessageBarBody>
        </MessageBar>
      )}
      {pasaDePendienteAAdministrador && (
        <MessageBar intent="info">
          <MessageBarBody>
            Los administradores no pueden estar pendientes: al guardar, la cuenta quedará activa.
          </MessageBarBody>
        </MessageBar>
      )}
      <div className={styles.grid}>{editFieldsFor(rolDestino).map(renderField)}</div>
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
  usuario: PropTypes.shape({ id: PropTypes.string.isRequired, estado: PropTypes.string }).isRequired,
  categories: PropTypes.arrayOf(categoryPropType),
  lockRol: PropTypes.bool,
  save: PropTypes.func.isRequired,
  onSaved: PropTypes.func.isRequired,
  onCancel: PropTypes.func.isRequired,
}
