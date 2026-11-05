import PropTypes from 'prop-types'
import { useState } from 'react'
import { Button, makeStyles, tokens } from '@fluentui/react-components'
import { Delete20Regular, Edit20Regular, Eye20Regular, EyeOff20Regular, Search20Regular } from '@fluentui/react-icons'
import { deleteAdminCategory } from '../../api/adminCategories'
import CategoryDetailDialog from './CategoryDetailDialog'
import CategoryFormDialog from './CategoryFormDialog'
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

// Textos de cada cambio de estado (HU41): una categoría activa se desactiva y una inactiva se activa.
const CAMBIOS_DE_ESTADO = {
  ACTIVA: {
    boton: 'Desactivar',
    icono: <EyeOff20Regular />,
    titulo: 'Desactivar categoría',
    pregunta: (nombre) =>
      `¿Seguro que quieres desactivar la categoría ${nombre}? Dejará de mostrarse a los clientes y a los vendedores.`,
  },
  INACTIVA: {
    boton: 'Activar',
    icono: <Eye20Regular />,
    titulo: 'Activar categoría',
    pregunta: (nombre) =>
      `¿Seguro que quieres activar la categoría ${nombre}? Volverá a mostrarse a los clientes y a los vendedores.`,
  },
}

const useStyles = makeStyles({
  actions: {
    display: 'flex',
    alignItems: 'center',
    gap: tokens.spacingHorizontalXS,
  },
})

// Acciones de una fila de la tabla de categorías: la lupa que abre su ficha de solo lectura (HU39), el lápiz que abre
// su formulario de edición (HU41), el botón para activarla o desactivarla (HU41) y la papelera para eliminarla (HU42).
// Activar, desactivar y eliminar piden confirmación. Si la categoría tiene productos, la papelera queda deshabilitada y
// explica por qué. `onResult` recibe el aviso que debe ver el administrador; `onChanged`, que la lista se actualice.
export default function CategoryRowActions({ category, onResult, onChanged }) {
  const styles = useStyles()
  const [dialogo, setDialogo] = useState(null)
  const [eliminando, setEliminando] = useState(false)
  const tieneProductos = category.numeroProductos > 0
  const cambioDeEstado = CAMBIOS_DE_ESTADO[category.estado]
  const cerrar = () => setDialogo(null)

  const avisarEditada = (editada) => {
    cerrar()
    onResult({ intent: 'success', texto: `${editada.nombre} se ha actualizado.` })
    onChanged()
  }

  // Otro administrador la ha eliminado mientras se editaba: la lista está desfasada y se recarga.
  const avisarQueYaNoExiste = () => {
    cerrar()
    onResult({ intent: 'error', texto: ERRORES_AL_ELIMINAR.CATEGORY_NOT_FOUND.texto(category.nombre) })
    onChanged()
  }

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
        icon={<Edit20Regular />}
        aria-label={`Editar ${category.nombre}`}
        title="Editar"
        onClick={() => setDialogo('editar')}
      />
      {cambioDeEstado && (
        <Button
          appearance="subtle"
          icon={cambioDeEstado.icono}
          aria-label={`${cambioDeEstado.boton} ${category.nombre}`}
          title={cambioDeEstado.boton}
          onClick={() => setDialogo('estado')}
        />
      )}
      <Button
        appearance="subtle"
        icon={<Delete20Regular />}
        aria-label={`Eliminar ${category.nombre}`}
        title={tieneProductos ? MENSAJE_CON_PRODUCTOS : 'Eliminar'}
        disabled={tieneProductos}
        onClick={() => setDialogo('eliminar')}
      />

      {dialogo === 'ficha' && <CategoryDetailDialog category={category} onClose={cerrar} />}
      {dialogo === 'editar' && (
        <CategoryFormDialog
          category={category}
          onClose={cerrar}
          onSaved={avisarEditada}
          onNotFound={avisarQueYaNoExiste}
        />
      )}
      {dialogo === 'estado' && (
        <StatusConfirmDialog
          titulo={cambioDeEstado.titulo}
          pregunta={cambioDeEstado.pregunta(category.nombre)}
          confirmar={cambioDeEstado.boton}
          enviando={false}
          onConfirm={cerrar}
          onCancel={cerrar}
        />
      )}
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
