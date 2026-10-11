import PropTypes from 'prop-types'
import { Button, MessageBar, MessageBarActions, MessageBarBody, Spinner } from '@fluentui/react-components'
import { ROLES } from '../../auth/roles'
import { useCategories } from '../../hooks/useCategories'
import ProfileEditor from './ProfileEditor'

// Formulario de edición de la ficha de cualquier usuario (HU22). Solo un vendedor necesita las categorías, para
// elegir la principal; como el rol de una cuenta no cambia nunca, el resto de cuentas no las cargan.
export default function EditableProfile({ rol, ...props }) {
  if (rol === ROLES.VENDEDOR) return <EditableSellerProfile {...props} />
  return <ProfileEditor rol={rol} {...props} />
}

// Carga las categorías, solo cuando se entra en modo edición, y muestra el formulario cuando ya están.
function EditableSellerProfile(props) {
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

  return <ProfileEditor rol={ROLES.VENDEDOR} categories={categories} {...props} />
}

const editorPropTypes = {
  usuario: PropTypes.shape({ id: PropTypes.string.isRequired, estado: PropTypes.string }).isRequired,
  save: PropTypes.func.isRequired,
  onSaved: PropTypes.func.isRequired,
  onCancel: PropTypes.func.isRequired,
}

EditableProfile.propTypes = { rol: PropTypes.string.isRequired, ...editorPropTypes }
EditableSellerProfile.propTypes = editorPropTypes
