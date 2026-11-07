import { useState } from 'react'
import PropTypes from 'prop-types'
import {
  Button,
  Field,
  Input,
  MessageBar,
  MessageBarBody,
  Spinner,
  Textarea,
  makeStyles,
  tokens,
} from '@fluentui/react-components'
import { Send20Regular } from '@fluentui/react-icons'
import LinkButton from '../../components/LinkButton'
import { PATHS } from '../../routes/paths'
import { DESCRIPTION_MAX, SUBJECT_MAX, validateIncident } from './incidentRules'

const useStyles = makeStyles({
  form: {
    display: 'flex',
    flexDirection: 'column',
    gap: tokens.spacingVerticalL,
  },
  actions: {
    display: 'flex',
    flexWrap: 'wrap',
    gap: tokens.spacingHorizontalM,
  },
})

// Los límites cuentan sin los espacios de los extremos, así que el contador también.
const countOf = (value) => value.trim().length

// Formulario de una incidencia nueva (HU44): asunto y descripción. Los errores de «obligatorio» salen al intentar enviar;
// pasarse del máximo, en cuanto ocurre. `onSubmit` recibe los dos textos sin espacios en los extremos.
export default function IncidentForm({ saving, saveError = null, onSubmit }) {
  const styles = useStyles()
  const [values, setValues] = useState({ asunto: '', descripcion: '' })
  const [attempted, setAttempted] = useState(false)

  const errors = validateIncident(values)
  const visible = (field, max) => (attempted || countOf(values[field]) > max ? errors[field] : undefined)
  const subjectError = visible('asunto', SUBJECT_MAX)
  const descriptionError = visible('descripcion', DESCRIPTION_MAX)

  const onText = (field) => (_event, data) => setValues((current) => ({ ...current, [field]: data.value }))

  const handleSubmit = (event) => {
    event.preventDefault()
    if (saving) return

    setAttempted(true)
    if (Object.keys(errors).length > 0) return
    onSubmit({ asunto: values.asunto.trim(), descripcion: values.descripcion.trim() })
  }

  return (
    <form className={styles.form} onSubmit={handleSubmit} noValidate>
      {saveError && (
        <MessageBar intent="error">
          <MessageBarBody>{saveError}</MessageBarBody>
        </MessageBar>
      )}

      <Field
        label="Asunto"
        required
        hint={`${countOf(values.asunto)}/${SUBJECT_MAX}`}
        validationState={subjectError ? 'error' : 'none'}
        validationMessage={subjectError}
      >
        <Input value={values.asunto} onChange={onText('asunto')} />
      </Field>

      <Field
        label="Descripción"
        required
        hint={`${countOf(values.descripcion)}/${DESCRIPTION_MAX}`}
        validationState={descriptionError ? 'error' : 'none'}
        validationMessage={descriptionError}
      >
        <Textarea value={values.descripcion} onChange={onText('descripcion')} rows={6} resize="vertical" />
      </Field>

      <div className={styles.actions}>
        <Button
          type="submit"
          appearance="primary"
          disabled={saving}
          icon={saving ? <Spinner size="tiny" /> : <Send20Regular />}
        >
          Enviar incidencia
        </Button>
        <LinkButton to={PATHS.INCIDENTS} appearance="subtle">
          Cancelar
        </LinkButton>
      </div>
    </form>
  )
}

IncidentForm.propTypes = {
  saving: PropTypes.bool.isRequired,
  saveError: PropTypes.string,
  onSubmit: PropTypes.func.isRequired,
}
