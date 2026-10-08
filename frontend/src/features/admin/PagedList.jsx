import { useCallback, useState } from 'react'
import {
  Button,
  MessageBar,
  MessageBarActions,
  MessageBarBody,
  Spinner,
  Text,
  makeStyles,
  tokens,
} from '@fluentui/react-components'
import Pagination from '../../components/Pagination'
import { useApiQuery } from '../../hooks/useApiQuery'

const useStyles = makeStyles({
  list: {
    display: 'flex',
    flexDirection: 'column',
    gap: tokens.spacingVerticalM,
  },
  empty: {
    padding: tokens.spacingVerticalXL,
    textAlign: 'center',
    color: tokens.colorNeutralForeground3,
  },
})

// Lista paginada dentro de una pestaña. `load` debe ser estable (useCallback) y recibe la página.
export default function PagedList({ load, itemLabel, errorMessage, emptyMessage, renderItems }) {
  const styles = useStyles()
  const [page, setPage] = useState(1)
  const fetcher = useCallback(() => load(page), [load, page])
  const { data, failed, refreshing, retry } = useApiQuery(fetcher)

  if (failed) {
    return (
      <MessageBar intent="error">
        <MessageBarBody>{errorMessage}</MessageBarBody>
        <MessageBarActions>
          <Button onClick={retry}>Reintentar</Button>
        </MessageBarActions>
      </MessageBar>
    )
  }

  if (!data) {
    return <Spinner label="Cargando…" />
  }

  if (data.totalItems === 0) {
    return <Text className={styles.empty}>{emptyMessage}</Text>
  }

  return (
    <div className={styles.list}>
      {renderItems(data.items, refreshing)}
      <Pagination
        page={data.page}
        totalPages={data.totalPages}
        totalItems={data.totalItems}
        pageSize={data.size}
        itemLabel={itemLabel}
        onPageChange={setPage}
      />
    </div>
  )
}
