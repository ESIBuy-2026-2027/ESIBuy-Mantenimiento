import PropTypes from 'prop-types'
import { useCallback, useState } from 'react'
import {
  Button,
  Dialog,
  DialogActions,
  DialogBody,
  DialogContent,
  DialogSurface,
  DialogTitle,
  MessageBar,
  MessageBarActions,
  MessageBarBody,
  MessageBarTitle,
  Spinner,
  Tab,
  TabList,
  makeStyles,
  tokens,
} from '@fluentui/react-components'
import {
  Dismiss24Regular,
  Edit20Regular,
  PersonAvailable20Regular,
  PersonProhibited20Regular,
} from '@fluentui/react-icons'
import {
  getAdminSeller,
  getAdminSellerOrders,
  getAdminSellerProducts,
  updateAdminSellerStatus,
} from '../../api/adminSellers'
import { updateUser } from '../../api/users'
import { ROLES } from '../../auth/roles'
import { useApiQuery } from '../../hooks/useApiQuery'
import { useCategories } from '../../hooks/useCategories'
import ProductsTable from '../seller/ProductsTable'
import OrdersTable from './OrdersTable'
import PagedList from './PagedList'
import ProfileEditor from './ProfileEditor'
import SellerDataForm from './SellerDataForm'
import StatusConfirmDialog from './StatusConfirmDialog'

const useStyles = makeStyles({
  surface: {
    width: '100%',
    maxWidth: '760px',
  },
  content: {
    display: 'flex',
    flexDirection: 'column',
    gap: tokens.spacingVerticalL,
    minHeight: '320px',
  },
})

function SellerProductsTab({ sellerId }) {
  const load = useCallback((page) => getAdminSellerProducts(sellerId, { page }), [sellerId])

  return (
    <PagedList
      load={load}
      itemLabel="productos"
      errorMessage="No se han podido cargar los productos del vendedor."
      emptyMessage="Este vendedor todavía no tiene productos."
      renderItems={(items, refreshing) => (
        <ProductsTable products={items} refreshing={refreshing} label="Productos del vendedor" />
      )}
    />
  )
}

function SellerOrdersTab({ sellerId }) {
  const load = useCallback((page) => getAdminSellerOrders(sellerId, { page }), [sellerId])

  return (
    <PagedList
      load={load}
      itemLabel="pedidos"
      errorMessage="No se han podido cargar los pedidos del vendedor."
      emptyMessage="Este vendedor todavía no tiene pedidos."
      renderItems={(items, refreshing) => <OrdersTable orders={items} refreshing={refreshing} />}
    />
  )
}

// Edición de los datos del vendedor (HU22). Necesita las categorías para elegir la principal.
function SellerEditor({ seller, onSaved, onCancel }) {
  const { categories, loaded, failed, retry } = useCategories()

  if (failed) {
    return (
      <MessageBar intent="error">
        <MessageBarBody>No se han podido cargar las categorías.</MessageBarBody>
        <MessageBarActions>
          <Button onClick={retry}>Reintentar</Button>
        </MessageBarActions>
      </MessageBar>
    )
  }
  if (!loaded) return <Spinner label="Cargando el formulario…" />

  return (
    <ProfileEditor
      rol={ROLES.VENDEDOR}
      usuario={seller}
      categories={categories}
      save={(datos) => updateUser(seller.id, datos)}
      onSaved={onSaved}
      onCancel={onCancel}
    />
  )
}

// Texto de cada acción según el estado al que se quiere pasar al vendedor.
const ACCIONES_DE_ESTADO = {
  ACTIVO: {
    boton: 'Activar usuario',
    icono: <PersonAvailable20Regular />,
    confirmar: 'Activar',
    pregunta: (nombre) => `¿Seguro que quieres activar a ${nombre}? Podrá usar la plataforma como vendedor.`,
    exito: 'El vendedor está activo y ya puede usar la plataforma.',
  },
  DESACTIVADO: {
    boton: 'Desactivar usuario',
    icono: <PersonProhibited20Regular />,
    confirmar: 'Desactivar',
    pregunta: (nombre) => `¿Seguro que quieres desactivar a ${nombre}? No podrá iniciar sesión.`,
    exito: 'El vendedor está desactivado.',
  },
}

export default function SellerDetailDialog({ sellerId, onClose, onStatusChange }) {
  const styles = useStyles()
  const [tab, setTab] = useState('data')
  const fetcher = useCallback(() => getAdminSeller(sellerId), [sellerId])
  const { data: seller, failed, retry } = useApiQuery(fetcher)
  const [confirmando, setConfirmando] = useState(false)
  const [enviando, setEnviando] = useState(false)
  const [resultado, setResultado] = useState(null)
  const [editando, setEditando] = useState(false)

  const nuevoEstado = seller?.estado === 'ACTIVO' ? 'DESACTIVADO' : 'ACTIVO'
  const accion = ACCIONES_DE_ESTADO[nuevoEstado]

  const cambiarEstado = async () => {
    setEnviando(true)
    try {
      await updateAdminSellerStatus(seller.id, nuevoEstado)
      setResultado({ intent: 'success', texto: accion.exito })
      retry()
      onStatusChange?.()
    } catch {
      setResultado({ intent: 'error', texto: 'No se ha podido cambiar el estado del vendedor. Inténtalo de nuevo.' })
    } finally {
      setEnviando(false)
      setConfirmando(false)
    }
  }

  const avisarGuardado = (datos) => {
    onStatusChange?.()
    // Si deja de ser vendedor, ya no tiene ficha de vendedor: se cierra y el listado se actualiza.
    if (datos.rol !== ROLES.VENDEDOR) {
      onClose()
      return
    }
    setEditando(false)
    setResultado({ intent: 'success', texto: 'Los cambios se han guardado.' })
    retry()
  }

  const empezarAEditar = () => {
    setResultado(null)
    setEditando(true)
  }

  return (
    <Dialog open modalType="modal" onOpenChange={(_event, data) => !data.open && onClose()}>
      <DialogSurface className={styles.surface}>
        <DialogBody>
          <DialogTitle
            action={
              <Button appearance="subtle" aria-label="Cerrar" icon={<Dismiss24Regular />} onClick={onClose} />
            }
          >
            Información del vendedor
          </DialogTitle>
          <DialogContent className={styles.content}>
            <MessageBar intent="info">
              <MessageBarBody>
                <MessageBarTitle>{editando ? 'Modo edición' : 'Modo solo lectura'}</MessageBarTitle>
                {editando
                  ? 'Solo se pueden modificar los datos del vendedor. Sus productos, precios y pedidos no se pueden cambiar.'
                  : 'Puedes consultar esta información. Los datos del vendedor se cambian con Editar; sus productos, precios y pedidos, no.'}
              </MessageBarBody>
            </MessageBar>

            {resultado && (
              <MessageBar intent={resultado.intent}>
                <MessageBarBody>{resultado.texto}</MessageBarBody>
              </MessageBar>
            )}

            {failed && (
              <MessageBar intent="error">
                <MessageBarBody>No se ha podido cargar la información del vendedor.</MessageBarBody>
                <MessageBarActions>
                  <Button onClick={retry}>Reintentar</Button>
                </MessageBarActions>
              </MessageBar>
            )}

            {!seller && !failed && <Spinner label="Cargando vendedor…" />}

            {seller && (
              <>
                <TabList selectedValue={tab} onTabSelect={(_event, data) => setTab(data.value)}>
                  <Tab value="data">Datos</Tab>
                  <Tab value="products" disabled={editando}>
                    Productos
                  </Tab>
                  <Tab value="orders" disabled={editando}>
                    Pedidos
                  </Tab>
                </TabList>
                {tab === 'data' && !editando && <SellerDataForm seller={seller} />}
                {tab === 'data' && editando && (
                  <SellerEditor seller={seller} onSaved={avisarGuardado} onCancel={() => setEditando(false)} />
                )}
                {tab === 'products' && <SellerProductsTab sellerId={seller.id} />}
                {tab === 'orders' && <SellerOrdersTab sellerId={seller.id} />}
              </>
            )}
          </DialogContent>
          {seller && !editando && (
            <DialogActions>
              {tab === 'data' && (
                <Button icon={<Edit20Regular />} onClick={empezarAEditar}>
                  Editar
                </Button>
              )}
              <Button icon={accion.icono} onClick={() => setConfirmando(true)}>
                {accion.boton}
              </Button>
            </DialogActions>
          )}
        </DialogBody>
        {confirmando && (
          <StatusConfirmDialog
            titulo={accion.boton}
            pregunta={accion.pregunta(seller.nombreComercial)}
            confirmar={accion.confirmar}
            enviando={enviando}
            onConfirm={cambiarEstado}
            onCancel={() => setConfirmando(false)}
          />
        )}
      </DialogSurface>
    </Dialog>
  )
}

SellerEditor.propTypes = {
  seller: PropTypes.shape({ id: PropTypes.string.isRequired }).isRequired,
  onSaved: PropTypes.func.isRequired,
  onCancel: PropTypes.func.isRequired,
}

SellerProductsTab.propTypes = {
  sellerId: PropTypes.string.isRequired,
}

SellerOrdersTab.propTypes = {
  sellerId: PropTypes.string.isRequired,
}

SellerDetailDialog.propTypes = {
  sellerId: PropTypes.string.isRequired,
  onClose: PropTypes.func.isRequired,
  onStatusChange: PropTypes.func,
}
