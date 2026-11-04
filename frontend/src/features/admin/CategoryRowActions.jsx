import PropTypes from 'prop-types'
import { useState } from 'react'
import { Button, makeStyles, tokens } from '@fluentui/react-components'
import { Delete20Regular, Search20Regular } from '@fluentui/react-icons'
import { deleteAdminCategory } from '../../api/adminCategories'
import CategoryDetailDialog from './CategoryDetailDialog'
import StatusConfirmDialog from './StatusConfirmDialog'

// El backend no deja eliminar una categoría con productos (409 CATEGORY_HAS_PRODUCTS, HU42).
const MENSAJE_CON_PRODUCTOS = 'No se puede eliminar una categoría que tiene productos.'

// Errores del backend al eliminar que tienen un mensaje propio. `actualiza`: la lista que ve el administrador está
// desfasada (alguien ha añadido productos a la categoría o ya la ha eliminado) y se pide recargarla.
const ERRORES_AL_ELIMINAR = {
  CATEGORY_HAS_PRODUCTS: {
    texto: (nombre) => `No se puede eliminar ${nombre} porque tiene productos.`,
    actualiza: true,
  },
  CATEGORY_HAS_SELLERS: {
    texto: (nombre) => `No se puede eliminar ${nombre} porque es la categoría principal de algún vendedor.`,
    actualiza: false,
  },
  CATEGORY_NOT_FOUND: { texto: (nombre) => `${nombre} ya no existe.`, actualiza: true },
}

const useStyles = makeStyles({
  actions: {
    display: 'flex',
    alignItems: 'center',
    gap: tokens.spacingHorizontalXS,
  },
})

// Acciones de una fila de la tabla de categorías: la lupa que abre su ficha de solo lectura (HU39) y la papelera
// para eliminarla (HU42), que pide confirmación. Si la categoría tiene productos, la papelera queda deshabilitada y
// explica por qué. `onResult` recibe el aviso que debe ver el administrador; `onChanged`, que la lista se actualice.
export default function CategoryRowActions({ category, onResult, onChanged }) {
  const styles = useStyles()
  const [dialogo, setDialogo] = useState(null)
  const [eliminando, setEliminando] = useState(false)
  const tieneProductos = category.numeroProductos > 0
  const cerrar = () => setDialogo(null)

  const eliminar = async () => {
    setEliminando(true)
    try {
      await deleteAdminCategory(category.id)
      onResult({ intent: 'success', texto: `${category.nombre} se ha eliminado.` })
      onChanged()
    } catch (error) {
      const conocido = ERRORES_AL_ELIMINAR[error?.code]
      onResult({
        intent: 'error',
        texto: conocido?.texto(category.nombre) ?? `No se ha podido eliminar ${category.nombre}. Inténtalo de nuevo.`,
      })
      if (conocido?.actualiza) onChanged()
    } finally {
      setEliminando(false)
      cerrar()
    }
  }

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
        title={tieneProductos ? MENSAJE_CON_PRODUCTOS : 'Eliminar'}
        disabled={tieneProductos}
        onClick={() => setDialogo('eliminar')}
      />

      {dialogo === 'ficha' && <CategoryDetailDialog category={category} onClose={cerrar} />}
      {dialogo === 'eliminar' && (
        <StatusConfirmDialog
          titulo="Eliminar categoría"
          pregunta={`¿Seguro que quieres eliminar la categoría ${category.nombre}? Esta acción no se puede deshacer.`}
          confirmar="Eliminar"
          enviando={eliminando}
          onConfirm={eliminar}
          onCancel={cerrar}
        />
      )}
    </div>
  )
}

CategoryRowActions.propTypes = {
  category: PropTypes.shape({
    id: PropTypes.string.isRequired,
    nombre: PropTypes.string.isRequired,
    descripcion: PropTypes.string.isRequired,
    estado: PropTypes.string.isRequired,
    numeroProductos: PropTypes.number.isRequired,
  }).isRequired,
  onResult: PropTypes.func.isRequired,
  onChanged: PropTypes.func.isRequired,
}
