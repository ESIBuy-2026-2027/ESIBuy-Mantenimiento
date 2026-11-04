import PropTypes from 'prop-types'
import { Button } from '@fluentui/react-components'
import { Delete20Regular } from '@fluentui/react-icons'

// Esqueleto: la papelera todavía no hace nada.
export default function UserDeleteButton({ user, label }) {
  const nombre = label ?? `${user.nombre} ${user.apellidos}`

  return <Button appearance="subtle" icon={<Delete20Regular />} aria-label={`Eliminar a ${nombre}`} title="Eliminar" />
}

UserDeleteButton.propTypes = {
  user: PropTypes.shape({
    id: PropTypes.string.isRequired,
    nombre: PropTypes.string.isRequired,
    apellidos: PropTypes.string.isRequired,
  }).isRequired,
  label: PropTypes.string,
  currentUserId: PropTypes.string,
  lastAdminId: PropTypes.string,
  withText: PropTypes.bool,
  onResult: PropTypes.func.isRequired,
  onChanged: PropTypes.func.isRequired,
  onDeleted: PropTypes.func,
}
