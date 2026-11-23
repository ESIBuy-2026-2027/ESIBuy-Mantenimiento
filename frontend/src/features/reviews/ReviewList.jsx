import PropTypes from 'prop-types'
import { Button, MessageBar, MessageBarActions, MessageBarBody, Text, makeStyles, tokens } from '@fluentui/react-components'
import RatingStars from '../catalog/RatingStars'

const formatearFecha = (fecha) => new Intl.DateTimeFormat('es-ES', {
  day: 'numeric', month: 'long', year: 'numeric',
}).format(new Date(fecha))
import { reviewPropType } from '../../utils/propTypes'

const useStyles = makeStyles({
  list: {
    display: 'flex',
    flexDirection: 'column',
    gap: tokens.spacingVerticalM,
    margin: 0,
    padding: 0,
    listStyleType: 'none',
  },
  item: {
    display: 'flex',
    flexDirection: 'column',
    gap: tokens.spacingVerticalXS,
    padding: tokens.spacingHorizontalL,
    backgroundColor: tokens.colorNeutralBackground1,
    border: `1px solid ${tokens.colorNeutralStroke2}`,
    borderRadius: tokens.borderRadiusXLarge,
  },
  author: {
    color: tokens.colorNeutralForeground2,
    fontWeight: tokens.fontWeightSemibold,
  },
  comment: {
    whiteSpace: 'pre-line',
    overflowWrap: 'anywhere',
    color: tokens.colorNeutralForeground2,
  },
  muted: {
    color: tokens.colorNeutralForeground3,
  },
})

// Valoraciones de un producto (CLI-03): cada una muestra puntuación, autor, fecha y comentario opcional.
export default function ReviewList({ reviews, failed, onRetry }) {
  const styles = useStyles()

  if (failed) {
    return (
      <MessageBar intent="error">
        <MessageBarBody>No se han podido cargar las valoraciones. Inténtalo de nuevo.</MessageBarBody>
        <MessageBarActions>
          <Button onClick={onRetry}>Reintentar</Button>
        </MessageBarActions>
      </MessageBar>
    )
  }
  if (!reviews) return null
  if (reviews.length === 0) {
    return <Text className={styles.muted}>Todavía no hay valoraciones de este producto.</Text>
  }

  return (
    <ul className={styles.list} aria-label="Valoraciones de los clientes">
      {reviews.map((review) => (
        <li key={review.id} className={styles.item}>
          <RatingStars value={review.puntuacion} size={18} />
          <Text className={styles.author}>{review.nombreCliente}</Text>
          <Text size={200} className={styles.muted}>{formatearFecha(review.fechaCreacion)}</Text>
          {review.comentario && <Text className={styles.comment}>{review.comentario}</Text>}
        </li>
      ))}
    </ul>
  )
}

ReviewList.propTypes = {
  reviews: PropTypes.arrayOf(reviewPropType),
  failed: PropTypes.bool.isRequired,
  onRetry: PropTypes.func.isRequired,
}
