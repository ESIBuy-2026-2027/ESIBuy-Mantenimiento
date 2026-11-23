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
  srOnly: {
    position: 'absolute',
    width: '1px',
    height: '1px',
    padding: 0,
    margin: '-1px',
    overflow: 'hidden',
    clip: 'rect(0, 0, 0, 0)',
    whiteSpace: 'nowrap',
    border: 0,
  },
})

// Una estrella llena por cada punto, media si sobra al menos medio punto y vacía en el resto.
function estrellaDe(posicion, valoracion) {
  if (valoracion >= posicion) return Star20Filled
  if (valoracion >= posicion - 0.5) return StarHalf20Filled
  return Star20Regular
}

// Valoración media (de 0 a 5) como cinco estrellas. Para los lectores de pantalla es una sola imagen con su valor.
export default function RatingStars({ value, size = 20 }) {
  const styles = useStyles()

  return (
    <>
      <img className={styles.srOnly} src="data:image/gif;base64,R0lGODlhAQABAAD/ACwAAAAAAQABAAACADs=" alt={`Valoraci\u00f3n: ${formatRating(value)} de ${TOTAL_ESTRELLAS}`} />
      <span className={styles.stars} aria-hidden="true">
        {Array.from({ length: TOTAL_ESTRELLAS }, (_, indice) => {
          const Estrella = estrellaDe(indice + 1, value)
          return (
            <Estrella
              key={indice}
              fontSize={size}
              className={Estrella === Star20Regular ? styles.empty : undefined}
            />
          )
        })}
      </span>
    </>
  )
}

RatingStars.propTypes = {
  value: PropTypes.number.isRequired,
  // Tamaño de las estrellas, en píxeles.
  size: PropTypes.number,
}
