import PropTypes from 'prop-types'
import { useState } from 'react'
import { Button, makeStyles, tokens } from '@fluentui/react-components'
import { Heart16Filled, Heart16Regular } from '@fluentui/react-icons'
import FavoriteCategoryNotice from './FavoriteCategoryNotice'
import { avisoDeExito, avisoDeFallo } from './favoriteCategoryNotices'

const useStyles = makeStyles({
  // El corazón relleno en rojo es la señal de que la categoría ya es favorita, como en los productos.
  icon: {
    color: tokens.colorPaletteRedForeground1,
  },
})

// Botón para añadir una categoría a las favoritas del cliente (AB#528) y, si ya lo es (`favorite`), para quitarla. Solo
// presenta: la página es la que la guarda con `onAdd` y `onRemove`. Mientras se guarda queda deshabilitado y después lo
// avisa; si no se puede, sigue como estaba.
export default function FavoriteCategoryButton({ category, favorite, onAdd, onRemove }) {
  const styles = useStyles()
  const [saving, setSaving] = useState(false)
  const [aviso, setAviso] = useState(null)
  const texto = favorite ? 'Quitar de favoritas' : 'Añadir a favoritas'

  const toggle = async () => {
    setAviso(null)
    setSaving(true)
    try {
      await (favorite ? onRemove(category.id) : onAdd(category.id))
      setAviso(avisoDeExito(category.nombre, !favorite))
    } catch (error) {
      setAviso(avisoDeFallo(category.nombre, !favorite, error))
    } finally {
      setSaving(false)
    }
  }

  return (
    <>
      <Button
        size="small"
        appearance="subtle"
        icon={favorite ? <Heart16Filled className={styles.icon} /> : <Heart16Regular />}
        aria-label={`${texto} la categoría ${category.nombre}`}
        disabled={saving}
        onClick={toggle}
      >
        {texto}
      </Button>
      <FavoriteCategoryNotice aviso={aviso} />
    </>
  )
}

FavoriteCategoryButton.propTypes = {
  category: PropTypes.shape({ id: PropTypes.string.isRequired, nombre: PropTypes.string.isRequired }).isRequired,
  favorite: PropTypes.bool.isRequired,
  onAdd: PropTypes.func.isRequired,
  onRemove: PropTypes.func.isRequired,
}
