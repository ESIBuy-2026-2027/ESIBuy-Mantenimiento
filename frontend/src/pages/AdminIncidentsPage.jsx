import { useCallback, useState } from 'react'
import { Button, MessageBar, MessageBarActions, MessageBarBody, Spinner, Text, Title1 } from '@fluentui/react-components'
import { ChatWarning24Regular, CheckmarkCircle20Regular, Open20Regular } from '@fluentui/react-icons'
import { getAdminIncidents } from '../api/adminIncidents'
import EmptyState from '../components/EmptyState'
import Pagination from '../components/Pagination'
import { usePageStyles } from '../components/pageStyles'
import CloseIncidentDialog from '../features/incidents/CloseIncidentDialog'
import IncidentsList from '../features/incidents/IncidentsList'
import OpenIncidentDialog from '../features/incidents/OpenIncidentDialog'
import { useApiQuery } from '../hooks/useApiQuery'

const PAGE_SIZE = 10

const AVISO_ABIERTA = { intent: 'success', texto: 'Incidencia abierta.' }
const AVISO_CERRADA = { intent: 'success', texto: 'Incidencia cerrada.' }
const AVISO_CAMBIADA = {
  intent: 'warning',
  texto: 'Otro administrador ya ha cambiado esa incidencia. Hemos actualizado la lista.',
}

// Supervisión de incidencias del administrador (HU49): las de todos los clientes y vendedores, cada una con quién la
// reportó. Abre en la primera página, la de las más antiguas, que son las que más tiempo llevan esperando. Desde aquí
// el administrador abre las pendientes con su respuesta y cierra (elimina) cualquiera (HU50).
export default function AdminIncidentsPage() {
  const styles = usePageStyles()
  const [page, setPage] = useState(1)
  const fetcher = useCallback(() => getAdminIncidents({ page, size: PAGE_SIZE }), [page])
  const { data, failed, refreshing, retry } = useApiQuery(fetcher)

  const [aviso, setAviso] = useState(null)
  // null: ningún diálogo; { tipo: 'abrir' | 'cerrar', incident }: el diálogo de esa acción sobre esa incidencia
  const [accion, setAccion] = useState(null)

  const empezar = (tipo, incident) => {
    setAviso(null)
    setAccion({ tipo, incident })
  }

  // Tras abrir, cerrar o un cambio de otro administrador se vuelve a pedir la página. Si se ha cerrado la única
  // incidencia de la última página, esa página ya no existe y se pasa a la anterior.
  const terminar = (nuevoAviso) => {
    const paginaVacia = accion.tipo === 'cerrar' && data.items.length === 1 && page > 1
    setAccion(null)
    setAviso(nuevoAviso)
    if (paginaVacia) setPage(page - 1)
    else retry()
  }

  const acciones = (incident) => (
    <>
      {incident.estado === 'PENDIENTE' && (
        <Button
          appearance="primary"
          icon={<Open20Regular />}
          aria-label={`Abrir «${incident.asunto}»`}
          onClick={() => empezar('abrir', incident)}
        >
          Abrir
        </Button>
      )}
      <Button
        icon={<CheckmarkCircle20Regular />}
        aria-label={`Cerrar «${incident.asunto}»`}
        onClick={() => empezar('cerrar', incident)}
      >
        Cerrar
      </Button>
    </>
  )

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

      {aviso && (
        <MessageBar intent={aviso.intent}>
          <MessageBarBody>{aviso.texto}</MessageBarBody>
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
            renderActions={acciones}
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

      {accion?.tipo === 'abrir' && (
        <OpenIncidentDialog
          incident={accion.incident}
          onClose={() => setAccion(null)}
          onOpened={() => terminar(AVISO_ABIERTA)}
          onConflict={() => terminar(AVISO_CAMBIADA)}
        />
      )}
      {accion?.tipo === 'cerrar' && (
        <CloseIncidentDialog
          incident={accion.incident}
          onClose={() => setAccion(null)}
          onClosed={() => terminar(AVISO_CERRADA)}
          onConflict={() => terminar(AVISO_CAMBIADA)}
        />
      )}
    </main>
  )
}
