import PropTypes from 'prop-types'
import { useState } from 'react'
import { Button, makeStyles, tokens } from '@fluentui/react-components'
import { Delete20Regular, Search20Regular } from '@fluentui/react-icons'
import CategoryDetailDialog from './CategoryDetailDialog'
import StatusConfirmDialog from './StatusConfirmDialog'

const useStyles = makeStyles({
  actions: {
    display: 'flex',
    alignItems: 'center',
    gap: tokens.spacingHorizontalXS,
  },
})

// Acciones de una fila de la tabla de categorías: la lupa que abre su ficha de solo lectura (HU39) y la papelera
// para eliminarla (HU42), que pide confirmación.
export default function CategoryRowActions({ category }) {
  const styles = useStyles()
  const [dialogo, setDialogo] = useState(null)
  const cerrar = () => setDialogo(null)

  return (
    <div className={styles.actions}>
      <Button
        appearance="subtle"
        icon={<Search20Regular />}
        aria-label={`Ver información de ${category.nombre}`}
        title="Ver información"
        onClick={() => setDialogo('ficha')}
      />
      <Button
        appearance="subtle"
        icon={<Delete20Regular />}
        aria-label={`Eliminar ${category.nombre}`}
        title="Eliminar"
        onClick={() => setDialogo('eliminar')}
      />

      {dialogo === 'ficha' && <CategoryDetailDialog category={category} onClose={cerrar} />}
      {dialogo === 'eliminar' && (
        <StatusConfirmDialog
          titulo="Eliminar categoría"
          pregunta={`¿Seguro que quieres eliminar la categoría ${category.nombre}? Esta acción no se puede deshacer.`}
          confirmar="Eliminar"
          enviando={false}
          onConfirm={cerrar}
          onCancel={cerrar}
        />
      )}
    </div>
  )
}

CategoryRowActions.propTypes = {
  category: PropTypes.shape({
    nombre: PropTypes.string.isRequired,
    descripcion: PropTypes.string.isRequired,
    estado: PropTypes.string.isRequired,
    numeroProductos: PropTypes.number.isRequired,
  }).isRequired,
}
