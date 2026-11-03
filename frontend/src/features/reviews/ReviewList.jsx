import PropTypes from 'prop-types'
import { Button, MessageBar, MessageBarActions, MessageBarBody, Text, makeStyles, tokens } from '@fluentui/react-components'
import RatingStars from '../catalog/RatingStars'
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
  comment: {
    whiteSpace: 'pre-line',
    overflowWrap: 'anywhere',
    color: tokens.colorNeutralForeground2,
  },
  muted: {
    color: tokens.colorNeutralForeground3,
  },
})

// Valoraciones de un producto (HU13, T368): cada una con sus estrellas y, si lo tiene, su comentario. No se sabe quién
// la escribió ni cuándo: el backend no lo devuelve. Mientras llegan no se muestra nada.
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
