import PropTypes from 'prop-types'
import { Badge, Text, makeStyles, mergeClasses, tokens } from '@fluentui/react-components'
import { formatPrice } from '../../utils/format'

const useStyles = makeStyles({
  block: {
    display: 'flex',
    flexDirection: 'column',
    gap: tokens.spacingVerticalXXS,
  },
  line: {
    display: 'flex',
    flexWrap: 'wrap',
    alignItems: 'baseline',
    gap: tokens.spacingHorizontalM,
  },
  final: {
    fontSize: tokens.fontSizeHero800,
    lineHeight: tokens.lineHeightHero800,
    fontWeight: tokens.fontWeightBold,
    letterSpacing: '-0.01em',
  },
  discounted: {
    color: tokens.colorPaletteRedForeground1,
  },
  original: {
    color: tokens.colorNeutralForeground3,
    fontSize: tokens.fontSizeBase400,
  },
  savings: {
    color: tokens.colorPaletteGreenForeground1,
    fontWeight: tokens.fontWeightSemibold,
  },
})

// Precio de un producto: el final, destacado, y, si hay descuento, el original tachado, el porcentaje y lo que se
// ahorra. Todo llega calculado por el servidor para quien consulta (cliente Estándar o Premium).
export default function PriceBlock({ precio, descuento, precioFinal }) {
  const styles = useStyles()
  const saving = precio - precioFinal

  return (
    <div className={styles.block}>
      <div className={styles.line}>
        <span className={mergeClasses(styles.final, descuento && styles.discounted)}>
          {formatPrice(precioFinal)}
        </span>
        {descuento && (
          <>
            <Text className={styles.original}>
              <s>{formatPrice(precio)}</s>
            </Text>
            <Badge appearance="filled" color="danger" size="large">
              {`-${descuento.porcentaje} %`}
            </Badge>
          </>
        )}
      </div>
      {descuento && saving > 0 && <Text className={styles.savings}>Ahorras {formatPrice(saving)}</Text>}
    </div>
  )
}

PriceBlock.propTypes = {
  precio: PropTypes.number.isRequired,
  descuento: PropTypes.shape({ porcentaje: PropTypes.number.isRequired }),
  precioFinal: PropTypes.number.isRequired,
}
