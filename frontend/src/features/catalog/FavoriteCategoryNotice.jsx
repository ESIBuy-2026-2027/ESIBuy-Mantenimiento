import PropTypes from 'prop-types'
import { MessageBar, MessageBarBody, makeStyles } from '@fluentui/react-components'

const useStyles = makeStyles({
  // Sin aviso no ocupa sitio, pero sigue ahí para que los lectores de pantalla anuncien el siguiente
  notice: {
    width: '100%',
    ':empty': { position: 'absolute' },
  },
})

// Aviso de éxito o de error al añadir o quitar una categoría favorita (AB#528), en una región que se anuncia.
export default function FavoriteCategoryNotice({ aviso }) {
  const styles = useStyles()

  return (
    <output className={styles.notice}>
      {aviso && (
        <MessageBar intent={aviso.intent}>
          <MessageBarBody>{aviso.texto}</MessageBarBody>
        </MessageBar>
      )}
    </output>
  )
}

FavoriteCategoryNotice.propTypes = {
  aviso: PropTypes.shape({
    intent: PropTypes.oneOf(['success', 'error']).isRequired,
    texto: PropTypes.string.isRequired,
  }),
}
