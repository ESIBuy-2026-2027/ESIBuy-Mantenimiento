import { useCallback, useState } from 'react'
import {
  Button,
  Card,
  MessageBar,
  MessageBarActions,
  MessageBarBody,
  Spinner,
  Text,
  makeStyles,
  tokens,
} from '@fluentui/react-components'
import { Delete20Regular, Heart24Regular } from '@fluentui/react-icons'
import { getPreferences, removeFavoriteCategory } from '../../api/preferences'
import EmptyState from '../../components/EmptyState'
import LinkButton from '../../components/LinkButton'
import { usePageStyles } from '../../components/pageStyles'
import { useApiQuery } from '../../hooks/useApiQuery'
import { useCategories } from '../../hooks/useCategories'
import { PATHS } from '../../routes/paths'
import FavoriteCategoryNotice from './FavoriteCategoryNotice'
import { FAVORITAS_NO_CARGADAS, avisoDeExito, avisoDeFallo } from './favoriteCategoryNotices'

const useStyles = makeStyles({
  list: {
    listStyle: 'none',
    margin: 0,
    padding: 0,
    display: 'flex',
    flexDirection: 'column',
    gap: tokens.spacingVerticalM,
  },
  item: {
    display: 'flex',
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
    gap: tokens.spacingHorizontalL,
  },
  name: {
    minWidth: 0,
    overflowWrap: 'anywhere',
  },
})

// Las categorías favoritas del cliente en «Mis favoritos» (AB#528), en el orden en que las marcó. El backend devuelve
// los identificadores de las que siguen activas y el nombre sale del listado público de categorías. Cada una se puede
// quitar sin confirmación, porque se puede volver a añadir desde un producto; el backend responde con las que quedan.
export default function FavoriteCategoriesList() {
  const pageStyles = usePageStyles()
  const styles = useStyles()
  const fetcher = useCallback(() => getPreferences(), [])
  const { data: cargadas, failed, retry } = useApiQuery(fetcher)
  const { categories, loaded, failed: categoriesFailed, retry: retryCategories } = useCategories()
  const [actualizadas, setActualizadas] = useState(null)
  const [quitando, setQuitando] = useState(null)
  const [aviso, setAviso] = useState(null)
  const favoritas = actualizadas ?? cargadas

  const quitar = async ({ id, nombre }) => {
    setAviso(null)
    setQuitando(id)
    try {
      setActualizadas(await removeFavoriteCategory(id))
      setAviso(avisoDeExito(nombre, false))
    } catch (error) {
      setAviso(avisoDeFallo(nombre, false, error))
    } finally {
      setQuitando(null)
    }
  }

  const reintentar = () => {
    if (failed) retry()
    if (categoriesFailed) retryCategories()
  }

  if (failed || categoriesFailed) {
    return (
      <MessageBar intent="error">
        <MessageBarBody>{FAVORITAS_NO_CARGADAS}</MessageBarBody>
        <MessageBarActions>
          <Button onClick={reintentar}>Reintentar</Button>
        </MessageBarActions>
      </MessageBar>
    )
  }

  if (!favoritas || !loaded) {
    return <Spinner label="Cargando tus categorías favoritas…" className={pageStyles.loading} />
  }

  const porId = new Map(categories.map((category) => [category.id, category]))
  const lista = favoritas.categorias.map((id) => porId.get(id)).filter(Boolean)

  return (
    <>
      <FavoriteCategoryNotice aviso={aviso} />
      {lista.length === 0 ? (
        <EmptyState
          icon={<Heart24Regular />}
          title="Aún no tienes categorías favoritas"
          description="Entra en un producto y pulsa «Añadir a favoritas» junto a su categoría."
        >
          <LinkButton to={PATHS.PRODUCTS} appearance="primary">
            Ver los productos
          </LinkButton>
        </EmptyState>
      ) : (
        <ul aria-label="Tus categorías favoritas" className={styles.list}>
          {lista.map((category) => (
            <li key={category.id}>
              <Card className={styles.item}>
                <Text weight="semibold" className={styles.name}>
                  {category.nombre}
                </Text>
                <Button
                  appearance="subtle"
                  icon={<Delete20Regular />}
                  aria-label={`Quitar ${category.nombre} de tus categorías favoritas`}
                  title="Quitar de favoritas"
                  disabled={quitando === category.id}
                  onClick={() => quitar(category)}
                />
              </Card>
            </li>
          ))}
        </ul>
      )}
    </>
  )
}
