import { useCallback, useState } from 'react'
import { Button, MessageBar, MessageBarActions, MessageBarBody, Spinner, Text, Title1 } from '@fluentui/react-components'
import { ChatWarning24Regular } from '@fluentui/react-icons'
import { getAdminIncidents } from '../api/adminIncidents'
import EmptyState from '../components/EmptyState'
import Pagination from '../components/Pagination'
import { usePageStyles } from '../components/pageStyles'
import IncidentsList from '../features/incidents/IncidentsList'
import { useApiQuery } from '../hooks/useApiQuery'

const PAGE_SIZE = 10

// Supervisión de incidencias del administrador (HU49): las de todos los clientes y vendedores, cada una con quién la
// reportó. Abre en la primera página, la de las más antiguas, que son las que más tiempo llevan esperando.
export default function AdminIncidentsPage() {
  const styles = usePageStyles()
  const [page, setPage] = useState(1)
  const fetcher = useCallback(() => getAdminIncidents({ page, size: PAGE_SIZE }), [page])
  const { data, failed, refreshing, retry } = useApiQuery(fetcher)

  return (
    <main className={styles.page}>
      <div className={styles.heading}>
        <div className={styles.titleBlock}>
          <Title1 as="h1" className={styles.title}>
            Incidencias
          </Title1>
          <Text className={styles.subtitle}>
            Revisa las incidencias que reportan los clientes y los vendedores, de las más antiguas a las más recientes.
          </Text>
        </div>
      </div>

      {failed && (
        <MessageBar intent="error">
          <MessageBarBody>No se han podido cargar las incidencias. Inténtalo de nuevo.</MessageBarBody>
          <MessageBarActions>
            <Button onClick={retry}>Reintentar</Button>
          </MessageBarActions>
        </MessageBar>
      )}

      {!data && !failed && <Spinner className={styles.loading} label="Cargando incidencias…" />}

      {data?.totalItems === 0 && (
        <EmptyState
          icon={<ChatWarning24Regular />}
          title="No hay incidencias"
          description="Cuando un cliente o un vendedor reporte una incidencia, aparecerá aquí."
        />
      )}

      {data && data.totalItems > 0 && (
        <>
          <IncidentsList
            incidents={data.items}
            refreshing={refreshing}
            showAuthor
            label="Incidencias de los usuarios"
          />
          <Pagination
            page={data.page}
            totalPages={data.totalPages}
            totalItems={data.totalItems}
            pageSize={data.size}
            itemLabel="incidencias"
            onPageChange={setPage}
          />
        </>
      )}
    </main>
  )
}
