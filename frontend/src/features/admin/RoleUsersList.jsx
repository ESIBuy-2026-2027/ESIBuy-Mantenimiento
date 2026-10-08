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
import ActivateUserButton from './ActivateUserButton'
import UsersFilters from './UsersFilters'
import UsersTable from './UsersTable'
import { ACCOUNT_STATUS_LABELS, ACTIVATABLE_STATUSES, USERS_PAGE_SIZE } from './usersConfig'
import { useUsersList } from './useUsersList'

const useStyles = makeStyles({
  loading: {
    padding: tokens.spacingVerticalXXXL,
  },
})

// Listado de los usuarios de un rol (clientes o administradores) con búsqueda, filtro por estado y
// el botón "Activar" en las cuentas desactivadas o bloqueadas (HU24). Reutiliza el listado de la HU19.
export default function RoleUsersList({ role, itemLabel, errorMessage, loadingLabel }) {
  const styles = useStyles()
  const [search, setSearch] = useState('')
  const [status, setStatus] = useState('')
  const [page, setPage] = useState(1)
  const [resultado, setResultado] = useState(null)
  const debouncedSearch = useDebouncedValue(search)
  const { data, failed, refreshing, retry } = useUsersList({
    search: debouncedSearch,
    role,
    status,
    page,
    size: USERS_PAGE_SIZE,
  })

  const changeFilter = (setFilter) => (value) => {
    setFilter(value)
    setPage(1)
  }

  const clearFilters = () => {
    setSearch('')
    setStatus('')
    setPage(1)
  }

  const avisarActivado = (nombre) => {
    setResultado({ intent: 'success', texto: `${nombre} ya está activo y puede usar la plataforma.` })
    retry()
  }

  const avisarError = (nombre) =>
    setResultado({ intent: 'error', texto: `No se ha podido activar a ${nombre}. Inténtalo de nuevo.` })

  const renderActions = (user) =>
    ACTIVATABLE_STATUSES.has(user.estado) ? (
      <ActivateUserButton user={user} onActivated={avisarActivado} onError={avisarError} />
    ) : null

  return (
    <>
      <UsersFilters
        search={search}
        status={status}
        statusLabels={ACCOUNT_STATUS_LABELS}
        onSearchChange={changeFilter(setSearch)}
        onStatusChange={changeFilter(setStatus)}
      />

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
          description="Prueba con otro nombre, otro email u otro estado."
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
}
