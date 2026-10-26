import PropTypes from 'prop-types'
import { useState } from 'react'
import {
  Badge,
  Button,
  Dialog,
  DialogActions,
  DialogBody,
  DialogContent,
  DialogSurface,
  DialogTitle,
  Text,
  makeStyles,
  tokens,
} from '@fluentui/react-components'
import { ImageOff24Regular } from '@fluentui/react-icons'
import { catalogProductPropType } from '../../utils/propTypes'
import { formatRating } from '../../utils/format'
import RatingStars from './RatingStars'

const SIN_CATEGORIA = '—'

const useStyles = makeStyles({
  surface: {
    width: 'calc(100% - 32px)',
    maxWidth: '520px',
    overflow: 'hidden',
    padding: 0,
  },
  media: {
    display: 'block',
    aspectRatio: '4 / 3',
    backgroundColor: tokens.colorNeutralBackground3,
  },
  image: {
    display: 'block',
    width: '100%',
    height: '100%',
    objectFit: 'cover',
  },
  placeholder: {
    display: 'flex',
    alignItems: 'center',
    justifyContent: 'center',
    width: '100%',
    height: '100%',
    color: tokens.colorNeutralForeground4,
  },
  body: {
    padding: `${tokens.spacingVerticalL} ${tokens.spacingHorizontalXL} ${tokens.spacingVerticalXL}`,
  },
  content: {
    display: 'flex',
    flexDirection: 'column',
    alignItems: 'flex-start',
    gap: tokens.spacingVerticalM,
  },
  rating: {
    display: 'flex',
    flexWrap: 'wrap',
    alignItems: 'center',
    gap: tokens.spacingHorizontalS,
  },
  muted: {
    color: tokens.colorNeutralForeground3,
  },
})

// Ficha de un producto del catálogo: lo mismo que enseña la tarjeta, en grande. El precio, la descripción y la
// compra llegarán con las historias del detalle del producto y del carrito.
export default function ProductDialog({ product, categoryName, onClose }) {
  const styles = useStyles()
  const [imageFailed, setImageFailed] = useState(false)
  const hasImage = Boolean(product.imagen) && !imageFailed
  const rated = product.valoracion !== null && product.numValoraciones > 0

  return (
    <Dialog open modalType="modal" onOpenChange={(_event, data) => !data.open && onClose()}>
      <DialogSurface className={styles.surface}>
        <span className={styles.media}>
          {hasImage ? (
            <img className={styles.image} src={product.imagen} alt={product.nombre} onError={() => setImageFailed(true)} />
          ) : (
            <span className={styles.placeholder} role="img" aria-label="Sin imagen">
              <ImageOff24Regular />
            </span>
          )}
        </span>
        <DialogBody className={styles.body}>
          <DialogTitle>{product.nombre}</DialogTitle>
          <DialogContent className={styles.content}>
            <Badge appearance="tint" color="brand" size="large">
              {categoryName ?? SIN_CATEGORIA}
            </Badge>
            <div className={styles.rating}>
              {rated ? (
                <>
                  <RatingStars value={product.valoracion} />
                  <Text weight="semibold">{formatRating(product.valoracion)}</Text>
                  <Text className={styles.muted}>
                    ({product.numValoraciones} {product.numValoraciones === 1 ? 'valoración' : 'valoraciones'})
                  </Text>
                </>
              ) : (
                <Text className={styles.muted}>Sin valoraciones</Text>
              )}
            </div>
          </DialogContent>
          <DialogActions>
            <Button appearance="primary" onClick={onClose}>
              Cerrar
            </Button>
          </DialogActions>
        </DialogBody>
      </DialogSurface>
    </Dialog>
  )
}

ProductDialog.propTypes = {
  product: catalogProductPropType.isRequired,
  categoryName: PropTypes.string,
  onClose: PropTypes.func.isRequired,
}
