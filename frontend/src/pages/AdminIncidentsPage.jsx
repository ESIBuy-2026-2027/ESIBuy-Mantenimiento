import { useCallback, useEffect, useRef, useState } from 'react'
import {
  Button,
  MessageBar,
  MessageBarActions,
  MessageBarBody,
  Spinner,
  Text,
  Title1,
  makeStyles,
  useRestoreFocusTarget,
} from '@fluentui/react-components'
import {
  ChatWarning24Regular,
  CheckboxChecked20Regular,
  CheckmarkCircle20Regular,
  Eye20Regular,
  Open20Regular,
} from '@fluentui/react-icons'
import { getAdminIncidents } from '../api/adminIncidents'
import EmptyState from '../components/EmptyState'
import Pagination from '../components/Pagination'
import { usePageStyles } from '../components/pageStyles'
import SortControls from '../features/admin/SortControls'
import UsersFilters from '../features/admin/UsersFilters'
import CloseIncidentDialog from '../features/incidents/CloseIncidentDialog'
import IncidentDetailDialog from '../features/incidents/IncidentDetailDialog'
import IncidentsList from '../features/incidents/IncidentsList'
import OpenIncidentDialog from '../features/incidents/OpenIncidentDialog'
import ResolveIncidentDialog from '../features/incidents/ResolveIncidentDialog'
import { ESTADO_INCIDENCIA, ETIQUETAS_DE_ESTADO } from '../features/incidents/incidentStatus'
import { useApiQuery } from '../hooks/useApiQuery'
import { useDebouncedValue } from '../hooks/useDebouncedValue'

const PAGE_SIZE = 10

// Sin avisos, la región no ocupa sitio ni separación en la página, pero sigue en ella para anunciar el siguiente
const useStyles = makeStyles({
  notices: {
    ':empty': {
      position: 'absolute',
    },
  },
})

const INCIDENT_SORT_OPTIONS = [
  { value: 'fechaCreacion', label: 'Fecha' },
  { value: 'asunto', label: 'Nombre' },
  { value: 'estado', label: 'Estado' },
]

const AVISO_ABIERTA = { intent: 'success', texto: 'Incidencia abierta.' }
const AVISO_CERRADA = { intent: 'success', texto: 'Incidencia cerrada.' }
const AVISO_RESUELTA = { intent: 'success', texto: 'Incidencia marcada como resuelta.' }
const AVISO_YA_RESUELTA = {
  intent: 'warning',
  texto: 'Esa incidencia ya estaba resuelta. Hemos actualizado la lista.',
}
const AVISO_CAMBIADA = {
  intent: 'warning',
  texto: 'Otro administrador ya ha cambiado esa incidencia. Hemos actualizado la lista.',
}

// Supervisión de incidencias del administrador (HU49, PBI 239): las de todos los clientes y vendedores.
// Las tarjetas muestran asunto, descripción y estado; el detalle completo se abre en un diálogo modal.
// Desde las tarjetas el administrador abre las pendientes con su respuesta y cierra (elimina) cualquiera (HU50), y
// marca como resueltas las abiertas (HU192).
export default function AdminIncidentsPage() {
  const styles = usePageStyles()
  const ownStyles = useStyles()
  const [page, setPage] = useState(1)
  const [search, setSearch] = useState('')
  const [estado, setEstado] = useState('')
  const [sort, setSort] = useState('fechaCreacion')
  const [order, setOrder] = useState('asc')
  const debouncedSearch = useDebouncedValue(search)

  const fetcher = useCallback(
    () =>
      getAdminIncidents({
        page,
        size: PAGE_SIZE,
        estado: estado || undefined,
        search: debouncedSearch || undefined,
        sort: sort !== 'fechaCreacion' ? sort : undefined,
        order: order !== 'asc' ? order : undefined,
      }),
    [page, estado, debouncedSearch, sort, order],
  )
  const { data, failed, refreshing, retry } = useApiQuery(fetcher)

  const [aviso, setAviso] = useState(null)
  // null: ningún diálogo; { tipo: 'detalle' | 'abrir' | 'cerrar' | 'resolver', incident }
  const [accion, setAccion] = useState(null)

  // Teclado y lector de pantalla (HU192): al cancelar, el foco vuelve al botón que abrió el diálogo; al resolver, ese
  // botón desaparece y el foco pasa al aviso
  const restoreFocusTarget = useRestoreFocusTarget()
  const avisosRef = useRef(null)
  useEffect(() => {
    if (aviso?.enfocar) avisosRef.current.focus()
  }, [aviso])

  const cambiarFiltro = (setter) => (value) => {
    setter(value)
    setPage(1)
    setAviso(null)
  }

  const empezar = (tipo, incident) => {
    setAviso(null)
    setAccion({ tipo, incident })
  }

  // Tras abrir, cerrar, resolver o un cambio de otro administrador se vuelve a pedir la página. Si la única incidencia
  // de la última página sale de la lista (se cierra, o se resuelve viendo solo las abiertas), se pasa a la anterior.
  const terminar = (nuevoAviso) => {
    const resolviendo = accion.tipo === 'resolver'
    const saleDeLaLista = accion.tipo === 'cerrar' || (resolviendo && estado === ESTADO_INCIDENCIA.ABIERTA)
    const paginaVacia = saleDeLaLista && data.items.length === 1 && page > 1
    setAccion(null)
    setAviso({ ...nuevoAviso, enfocar: resolviendo })
    if (paginaVacia) setPage(page - 1)
    else retry()
  }

  const hayFiltrosActivos = Boolean(debouncedSearch.trim() || estado)

  const acciones = (incident) => (
    <>
      <Button
        icon={<Eye20Regular />}
        aria-label={`Ver detalle de «${incident.asunto}»`}
        onClick={() => empezar('detalle', incident)}
      >
        Ver detalle
      </Button>
      {incident.estado === ESTADO_INCIDENCIA.PENDIENTE && (
        <Button
          appearance="primary"
          icon={<Open20Regular />}
          aria-label={`Abrir incidencia «${incident.asunto}»`}
          onClick={() => empezar('abrir', incident)}
        >
          Abrir incidencia
        </Button>
      )}
      {incident.estado === ESTADO_INCIDENCIA.ABIERTA && (
        <Button
          {...restoreFocusTarget}
          appearance="primary"
          icon={<CheckboxChecked20Regular />}
          aria-label={`Marcar como resuelta la incidencia «${incident.asunto}»`}
          onClick={() => empezar('resolver', incident)}
        >
          Marcar como resuelta
        </Button>
      )}
      <Button
        icon={<CheckmarkCircle20Regular />}
        aria-label={`Cerrar incidencia «${incident.asunto}»`}
        onClick={() => empezar('cerrar', incident)}
      >
        Cerrar incidencia
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

      <UsersFilters
        search={search}
        searchLabel="Buscar por nombre"
        searchPlaceholder="Escribe un nombre"
        status={estado}
        statusLabels={ETIQUETAS_DE_ESTADO}
        onSearchChange={cambiarFiltro(setSearch)}
        onStatusChange={cambiarFiltro(setEstado)}
      >
        <SortControls
          options={INCIDENT_SORT_OPTIONS}
          sort={sort}
          order={order}
          onSortChange={cambiarFiltro(setSort)}
          onOrderChange={cambiarFiltro(setOrder)}
        />
      </UsersFilters>

      {failed && (
        <MessageBar intent="error">
          <MessageBarBody>No se han podido cargar las incidencias. Inténtalo de nuevo.</MessageBarBody>
          <MessageBarActions>
            <Button onClick={retry}>Reintentar</Button>
          </MessageBarActions>
        </MessageBar>
      )}

      <div ref={avisosRef} tabIndex={-1} aria-live="polite" className={ownStyles.notices}>
        {aviso && (
          <MessageBar intent={aviso.intent}>
            <MessageBarBody>{aviso.texto}</MessageBarBody>
          </MessageBar>
        )}
      </div>

      {!data && !failed && <Spinner className={styles.loading} label="Cargando incidencias…" />}

      {data?.totalItems === 0 && (
        <EmptyState
          icon={<ChatWarning24Regular />}
          title="No hay incidencias"
          description={
            hayFiltrosActivos
              ? 'No hay incidencias que coincidan con los filtros seleccionados.'
              : 'Cuando un cliente o un vendedor reporte una incidencia, aparecerá aquí.'
          }
        />
      )}

      {data && data.totalItems > 0 && (
        <>
          <IncidentsList
            incidents={data.items}
            refreshing={refreshing}
            compact
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

      {accion?.tipo === 'detalle' && (
        // Con los datos más recientes: si la lista se recarga con el detalle abierto, se actualiza
        <IncidentDetailDialog
          incident={data?.items.find((item) => item.id === accion.incident.id) ?? accion.incident}
          onClose={() => setAccion(null)}
        />
      )}
      {accion?.tipo === 'abrir' && (
        <OpenIncidentDialog
          incident={accion.incident}
          onClose={() => setAccion(null)}
          onOpened={() => terminar(AVISO_ABIERTA)}
          onConflict={() => terminar(AVISO_CAMBIADA)}
        />
      )}
      {accion?.tipo === 'resolver' && (
        <ResolveIncidentDialog
          incident={accion.incident}
          onClose={() => setAccion(null)}
          onResolved={() => terminar(AVISO_RESUELTA)}
          onConflict={(code) =>
            terminar(code === 'INCIDENT_ALREADY_RESOLVED' ? AVISO_YA_RESUELTA : AVISO_CAMBIADA)
          }
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
