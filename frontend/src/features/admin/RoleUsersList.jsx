import PropTypes from 'prop-types'
import { useState } from 'react'
import {
  Button,
  MessageBar,
  MessageBarActions,
  MessageBarBody,
  Spinner,
  makeStyles,
  tokens,
} from '@fluentui/react-components'
import { SearchInfo24Regular } from '@fluentui/react-icons'
import EmptyState from '../../components/EmptyState'
import Pagination from '../../components/Pagination'
import { useDebouncedValue } from '../../hooks/useDebouncedValue'
import SortControls from './SortControls'
import UsersFilters from './UsersFilters'
import UserStatusButton from './UserStatusButton'
import UsersTable from './UsersTable'
import { ACCOUNT_SORT_OPTIONS, ACCOUNT_STATUS_LABELS, USERS_PAGE_SIZE, USER_STATUS } from './usersConfig'
import { useUsersList } from './useUsersList'

const MENSAJES_EXITO = {
  activar: (nombre) => `${nombre} ya está activo y puede usar la plataforma.`,
  desactivar: (nombre) => `${nombre} está desactivado y ya no puede iniciar sesión.`,
}

// Errores del backend al desactivar que tienen un mensaje propio (HU23).
const MENSAJES_CONFLICTO = {
  CANNOT_DEACTIVATE_SELF: 'No puedes desactivar tu propia cuenta.',
  LAST_ACTIVE_ADMIN: 'No se puede desactivar al último administrador activo.',
}

const useStyles = makeStyles({
  loading: {
    padding: tokens.spacingVerticalXXXL,
  },
})

// Listado de los usuarios de un rol (clientes o administradores) con búsqueda por nombre, filtro por
// estado, ordenación por nombre o estado y los botones "Activar" (cuentas desactivadas, HU24) y
// "Desactivar" (cuentas activas salvo la propia, HU23). Reutiliza el listado de la HU19.
export default function RoleUsersList({ role, itemLabel, errorMessage, loadingLabel, currentUserId }) {
  const styles = useStyles()
  const [search, setSearch] = useState('')
  const [status, setStatus] = useState('')
  const [sort, setSort] = useState('name')
  const [order, setOrder] = useState('asc')
  const [page, setPage] = useState(1)
  const [resultado, setResultado] = useState(null)
  const debouncedSearch = useDebouncedValue(search)
  const { data, failed, refreshing, retry } = useUsersList({
    search: debouncedSearch,
    role,
    status,
    page,
    size: USERS_PAGE_SIZE,
    searchBy: 'name',
    sort,
    order,
  })

  const changeFilter = (setFilter) => (value) => {
    setFilter(value)
    setPage(1)
  }

  // Limpiar filtros mantiene la ordenación elegida, como en el listado de vendedores (HU26).
  const clearFilters = () => {
    setSearch('')
    setStatus('')
    setPage(1)
  }

  const accionPara = (user) => {
    if (user.estado === USER_STATUS.DESACTIVADO) return 'activar'
    if (user.estado === USER_STATUS.ACTIVO && user.id !== currentUserId) return 'desactivar'
    return null
  }

  const avisarHecho = (accion) => (nombre) => {
    setResultado({ intent: 'success', texto: MENSAJES_EXITO[accion](nombre) })
    retry()
  }

  const avisarError = (accion) => (nombre, error) =>
    setResultado({
      intent: 'error',
      texto: MENSAJES_CONFLICTO[error?.code] ?? `No se ha podido ${accion} a ${nombre}. Inténtalo de nuevo.`,
    })

  const renderActions = (user) => {
    const accion = accionPara(user)
    return accion ? (
      <UserStatusButton user={user} accion={accion} onDone={avisarHecho(accion)} onError={avisarError(accion)} />
    ) : null
  }

  return (
    <>
      <UsersFilters
        search={search}
        searchLabel="Buscar por nombre"
        searchPlaceholder="Escribe un nombre"
        status={status}
        statusLabels={ACCOUNT_STATUS_LABELS}
        onSearchChange={changeFilter(setSearch)}
        onStatusChange={changeFilter(setStatus)}
      >
        <SortControls
          options={ACCOUNT_SORT_OPTIONS}
          sort={sort}
          order={order}
          onSortChange={changeFilter(setSort)}
          onOrderChange={changeFilter(setOrder)}
        />
      </UsersFilters>

      {resultado && (
        <MessageBar intent={resultado.intent}>
          <MessageBarBody>{resultado.texto}</MessageBarBody>
        </MessageBar>
      )}

      {failed && (
        <MessageBar intent="error">
          <MessageBarBody>{errorMessage}</MessageBarBody>
          <MessageBarActions>
            <Button onClick={retry}>Reintentar</Button>
          </MessageBarActions>
        </MessageBar>
      )}

      {!data && !failed && <Spinner className={styles.loading} label={loadingLabel} />}

      {data?.totalItems === 0 && (
        <EmptyState
          icon={<SearchInfo24Regular />}
          title={`No se han encontrado ${itemLabel}`}
          description="Prueba con otro nombre u otro estado."
        >
          <Button onClick={clearFilters}>Limpiar filtros</Button>
        </EmptyState>
      )}

      {data?.totalItems > 0 && (
        <>
          <UsersTable users={data.items} refreshing={refreshing} renderActions={renderActions} />
          <Pagination
            page={data.page}
            totalPages={data.totalPages}
            totalItems={data.totalItems}
            pageSize={data.size}
            itemLabel={itemLabel}
            onPageChange={setPage}
          />
        </>
      )}
    </>
  )
}

RoleUsersList.propTypes = {
  role: PropTypes.string.isRequired,
  itemLabel: PropTypes.string.isRequired,
  errorMessage: PropTypes.string.isRequired,
  loadingLabel: PropTypes.string.isRequired,
  currentUserId: PropTypes.string,
}
