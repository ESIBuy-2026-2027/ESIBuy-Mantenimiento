import PropTypes from 'prop-types'
import { Button, MessageBar, MessageBarActions, MessageBarBody, Spinner } from '@fluentui/react-components'
import { useCategories } from '../../hooks/useCategories'
import ProfileEditor from './ProfileEditor'

// Formulario de edición de la ficha de cualquier usuario (HU22). Como el administrador puede cambiar el rol de
// una cuenta a vendedor, siempre necesita las categorías para elegir la principal: las carga aquí, solo cuando
// se entra en modo edición, y muestra el formulario cuando ya están.
export default function EditableProfile({ rol, usuario, lockRol, save, onSaved, onCancel }) {
  const { categories, loaded, failed, retry } = useCategories()

  if (failed) {
    return (
      <MessageBar intent="error">
        <MessageBarBody>No se han podido cargar las categorías.</MessageBarBody>
        <MessageBarActions>
          <Button onClick={retry}>Reintentar</Button>
        </MessageBarActions>
      </MessageBar>
    )
  }
  if (!loaded) return <Spinner label="Cargando el formulario…" />

  return (
    <ProfileEditor
      rol={rol}
      usuario={usuario}
      categories={categories}
      lockRol={lockRol}
      save={save}
      onSaved={onSaved}
      onCancel={onCancel}
    />
  )
}

EditableProfile.propTypes = {
  rol: PropTypes.string.isRequired,
  usuario: PropTypes.shape({ id: PropTypes.string.isRequired, estado: PropTypes.string }).isRequired,
  lockRol: PropTypes.bool,
  save: PropTypes.func.isRequired,
  onSaved: PropTypes.func.isRequired,
  onCancel: PropTypes.func.isRequired,
}
