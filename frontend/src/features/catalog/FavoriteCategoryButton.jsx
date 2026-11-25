import PropTypes from 'prop-types'
import { useState } from 'react'
import { Button, makeStyles, mergeClasses, tokens } from '@fluentui/react-components'
import { Add16Filled, Checkmark16Filled } from '@fluentui/react-icons'
import FavoriteCategoryNotice from './FavoriteCategoryNotice'
import { avisoDeExito, avisoDeFallo } from './favoriteCategoryNotices'

const useStyles = makeStyles({
  // Pastilla de marca (DESIGN.md). No lleva corazón, para no confundirla con los favoritos de productos.
  pill: {
    color: tokens.colorBrandForeground2,
    backgroundColor: tokens.colorNeutralBackground1,
    // El borde la distingue del fondo de la página, que es del mismo tono
    border: `1px solid ${tokens.colorBrandStroke1}`,
    textTransform: 'uppercase',
    letterSpacing: '0.06em',
    ':hover': {
      color: tokens.colorBrandForeground2Hover,
      backgroundColor: tokens.colorBrandBackground2Hover,
    },
  },
  // El naranja oscuro, y no el de marca, para que el texto pequeño en blanco tenga contraste suficiente
  selected: {
    color: tokens.colorNeutralForegroundOnBrand,
    backgroundColor: tokens.colorBrandBackgroundHover,
    ':hover': {
      color: tokens.colorNeutralForegroundOnBrand,
      backgroundColor: tokens.colorBrandBackgroundPressed,
    },
  },
})

// La categoría de un producto como pastilla: al pulsarla se añade a las favoritas del cliente (AB#528) y, si ya lo es
// (`favorite`), se quita. Con `+` está sin añadir y con `✓` y rellena, añadida. Solo presenta: la página es la que la guarda
// con `onAdd` y `onRemove`. Mientras se guarda queda deshabilitada y después lo avisa; si no se puede, sigue como estaba.
export default function FavoriteCategoryButton({ category, favorite, onAdd, onRemove }) {
  const styles = useStyles()
  const [saving, setSaving] = useState(false)
  const [aviso, setAviso] = useState(null)

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
        shape="circular"
        appearance="transparent"
        className={mergeClasses(styles.pill, favorite && styles.selected)}
        icon={favorite ? <Checkmark16Filled /> : <Add16Filled />}
        aria-label={`${favorite ? 'Quitar de' : 'Añadir a'} favoritas la categoría ${category.nombre}`}
        title={favorite ? 'Quitar de tus categorías favoritas' : 'Añadir a tus categorías favoritas'}
        data-marcada={favorite}
        disabled={saving}
        onClick={toggle}
      >
        {category.nombre}
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
