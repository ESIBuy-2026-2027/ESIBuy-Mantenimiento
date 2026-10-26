import { useCallback } from 'react'
import { Subtitle1, Text, makeStyles, tokens } from '@fluentui/react-components'
import { Trophy20Filled } from '@fluentui/react-icons'
import TiltedGridHero from '../../components/TiltedGridHero'
import { getCatalogProducts } from '../../api/products'
import { useApiQuery } from '../../hooks/useApiQuery'
import { formatRating } from '../../utils/format'

// Cuántos productos se piden y cuántos hacen falta con imagen para que la banda tenga sentido.
const PEDIDOS = 8
const MINIMO_CON_IMAGEN = 3
const DESTACADOS_EN_LISTA = 3
const VALORACION_MINIMA = '4'

const useStyles = makeStyles({
  hero: {
    height: '380px',
    borderRadius: tokens.borderRadiusXLarge,
    backgroundImage: `linear-gradient(135deg, ${tokens.colorBrandBackground2} 0%, ${tokens.colorNeutralBackground1} 70%)`,
    border: `1px solid ${tokens.colorNeutralStroke2}`,
    boxShadow: tokens.shadow8,
  },
  content: {
    position: 'relative',
    zIndex: 1,
    display: 'flex',
    flexDirection: 'column',
    justifyContent: 'space-between',
    alignItems: 'center',
    height: '100%',
    boxSizing: 'border-box',
    padding: `${tokens.spacingVerticalXL} ${tokens.spacingHorizontalL}`,
    textAlign: 'center',
  },
  title: {
    display: 'flex',
    alignItems: 'center',
    justifyContent: 'center',
    gap: tokens.spacingHorizontalS,
    margin: 0,
  },
  trophy: {
    color: tokens.colorPaletteMarigoldForeground2,
  },
  subtitle: {
    color: tokens.colorNeutralForeground3,
  },
  top: {
    display: 'flex',
    flexDirection: 'column',
    gap: tokens.spacingVerticalXS,
  },
  list: {
    display: 'flex',
    flexWrap: 'wrap',
    justifyContent: 'center',
    gap: tokens.spacingHorizontalS,
    margin: 0,
    padding: 0,
    listStyleType: 'none',
  },
  item: {
    display: 'inline-flex',
    alignItems: 'baseline',
    gap: tokens.spacingHorizontalXS,
    padding: `${tokens.spacingVerticalXXS} ${tokens.spacingHorizontalM}`,
    borderRadius: tokens.borderRadiusCircular,
    backgroundColor: tokens.colorNeutralBackground1,
    boxShadow: tokens.shadow4,
  },
  rating: {
    color: tokens.colorPaletteMarigoldForeground2,
  },
})

// Selección de los productos mejor valorados, con sus imágenes en una banda curva (HU7). Pide los mejores al mismo
// endpoint del catálogo (por valoración descendente) y solo se pinta si hay imágenes suficientes; si falla, la vista
// sigue sin ella.
export default function FeaturedProducts() {
  const styles = useStyles()
  const fetcher = useCallback(
    () => getCatalogProducts({ sort: 'valoracion', order: 'desc', minRating: VALORACION_MINIMA, page: 1, size: PEDIDOS }),
    [],
  )
  const { data } = useApiQuery(fetcher)
  const featured = (data?.items ?? []).filter((product) => product.imagen)

  if (featured.length < MINIMO_CON_IMAGEN) return null

  return (
    <section aria-labelledby="mejor-valorados">
      <TiltedGridHero
        className={styles.hero}
        images={featured.map((product) => ({ src: product.imagen, alt: product.nombre }))}
        tileHeight={40}
        aspectRatio={4 / 3}
        axis={55}
        curve={70}
        speed={5}
      >
        <div className={styles.content}>
          <div className={styles.top}>
            <Subtitle1 as="h2" id="mejor-valorados" className={styles.title}>
              <Trophy20Filled className={styles.trophy} />
              Los mejor valorados
            </Subtitle1>
            <Text className={styles.subtitle}>La selección de ESIBuy, con las mejores opiniones.</Text>
          </div>
          <ul className={styles.list} aria-label="Selección destacada">
            {featured.slice(0, DESTACADOS_EN_LISTA).map((product) => (
              <li key={product.id} className={styles.item}>
                <Text weight="semibold">{product.nombre}</Text>
                <Text weight="semibold" className={styles.rating}>
                  {formatRating(product.valoracion)}
                </Text>
              </li>
            ))}
          </ul>
        </div>
      </TiltedGridHero>
    </section>
  )
}
