import PropTypes from 'prop-types'
import { makeStyles, tokens } from '@fluentui/react-components'
import { Star20Filled, Star20Regular, StarHalf20Filled } from '@fluentui/react-icons'
import { formatRating } from '../../utils/format'

const TOTAL_ESTRELLAS = 5

const useStyles = makeStyles({
  stars: {
    display: 'inline-flex',
    alignItems: 'center',
    color: tokens.colorPaletteMarigoldForeground2,
  },
  empty: {
    color: tokens.colorNeutralForeground4,
  },
})

// Una estrella llena por cada punto, media si sobra al menos medio punto y vacía en el resto.
function estrellaDe(posicion, valoracion) {
  if (valoracion >= posicion) return Star20Filled
  if (valoracion >= posicion - 0.5) return StarHalf20Filled
  return Star20Regular
}

// Valoración media (de 0 a 5) como cinco estrellas. Para los lectores de pantalla es una sola imagen con su valor.
export default function RatingStars({ value }) {
  const styles = useStyles()

  return (
    <span className={styles.stars} role="img" aria-label={`Valoración: ${formatRating(value)} de ${TOTAL_ESTRELLAS}`}>
      {Array.from({ length: TOTAL_ESTRELLAS }, (_, indice) => {
        const Estrella = estrellaDe(indice + 1, value)
        return <Estrella key={indice} className={Estrella === Star20Regular ? styles.empty : undefined} />
      })}
    </span>
  )
}

RatingStars.propTypes = {
  value: PropTypes.number.isRequired,
}
