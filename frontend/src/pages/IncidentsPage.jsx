import { useCallback, useState } from 'react'
import {
  Button,
  MessageBar,
  MessageBarActions,
  MessageBarBody,
  Spinner,
  Text,
  Title1,
} from '@fluentui/react-components'
import { Add20Regular, ChatWarning24Regular, Dismiss20Regular } from '@fluentui/react-icons'
import { useLocation } from 'react-router-dom'
import { getIncidents } from '../api/incidents'
import EmptyState from '../components/EmptyState'
import LinkButton from '../components/LinkButton'
import Pagination from '../components/Pagination'
import { usePageStyles } from '../components/pageStyles'
import IncidentsList from '../features/incidents/IncidentsList'
import { useApiQuery } from '../hooks/useApiQuery'
import { PATHS } from '../routes/paths'

const PAGE_SIZE = 10

// Sin página elegida (`null`) la vista abre en la última, la de las incidencias más recientes: el backend las devuelve
// de las más antiguas a las más recientes, así que pide la primera para saber cuántas páginas hay y, si hay más de una,
// pide la última. Desde ahí, `page` es la que elige el usuario con la paginación.
async function loadIncidents(page) {
  const requested = await getIncidents({ page: page ?? 1, size: PAGE_SIZE })
  if (page === null && requested.totalPages > 1) {
    return getIncidents({ page: requested.totalPages, size: PAGE_SIZE })
  }
  return requested
}

// «Mis incidencias» del cliente y del vendedor en sesión (HU44).
export default function IncidentsPage() {
  const styles = usePageStyles()
  const location = useLocation()
  // Aviso que deja el formulario al crear una incidencia (llega en el estado de la navegación).
  const [notice, setNotice] = useState(
    location.state?.notice ? { intent: 'success', text: location.state.notice } : null,
  )
  const [page, setPage] = useState(null)
  const fetcher = useCallback(() => loadIncidents(page), [page])
  const { data, failed, refreshing, retry } = useApiQuery(fetcher)

  return (
    <main className={styles.page}>
      <div className={styles.heading}>
        <div className={styles.titleBlock}>
          <Title1 as="h1" className={styles.title}>
            Mis incidencias
          </Title1>
          <Text className={styles.subtitle}>Reporta un problema y consulta la respuesta de un administrador.</Text>
        </div>
        <LinkButton to={PATHS.INCIDENT_NEW} appearance="primary" icon={<Add20Regular />}>
          Nueva incidencia
        </LinkButton>
      </div>

      {notice && (
        <MessageBar intent={notice.intent}>
          <MessageBarBody>{notice.text}</MessageBarBody>
          <MessageBarActions
            containerAction={
              <Button
                appearance="transparent"
                aria-label="Cerrar aviso"
                icon={<Dismiss20Regular />}
                onClick={() => setNotice(null)}
              />
            }
          />
        </MessageBar>
      )}

      {failed && (
        <MessageBar intent="error">
          <MessageBarBody>No se han podido cargar tus incidencias. Inténtalo de nuevo.</MessageBarBody>
          <MessageBarActions>
            <Button onClick={retry}>Reintentar</Button>
          </MessageBarActions>
        </MessageBar>
      )}

      {!data && !failed && <Spinner className={styles.loading} label="Cargando incidencias…" />}

      {data?.totalItems === 0 && (
        <EmptyState
          icon={<ChatWarning24Regular />}
          title="Todavía no has reportado ninguna incidencia"
          description="Si tienes algún problema, cuéntanoslo: un administrador la revisará y te responderá aquí."
        >
          <LinkButton to={PATHS.INCIDENT_NEW} appearance="primary" icon={<Add20Regular />}>
            Reportar mi primera incidencia
          </LinkButton>
        </EmptyState>
      )}

      {data && data.totalItems > 0 && (
        <>
          <IncidentsList incidents={data.items} refreshing={refreshing} />
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
