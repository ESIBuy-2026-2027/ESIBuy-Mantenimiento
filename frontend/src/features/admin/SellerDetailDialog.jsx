import PropTypes from 'prop-types'
import { useCallback, useState } from 'react'
import {
  Button,
  Dialog,
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
import { Dismiss24Regular } from '@fluentui/react-icons'
import { getAdminSeller, getAdminSellerOrders, getAdminSellerProducts } from '../../api/adminSellers'
import { useApiQuery } from '../../hooks/useApiQuery'
import { useCategories } from '../../hooks/useCategories'
import ProductsTable from '../seller/ProductsTable'
import OrdersTable from './OrdersTable'
import PagedList from './PagedList'
import SellerDataForm from './SellerDataForm'

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
  const { categories } = useCategories()

  return (
    <PagedList
      load={load}
      itemLabel="productos"
      errorMessage="No se han podido cargar los productos del vendedor."
      emptyMessage="Este vendedor todavía no tiene productos."
      renderItems={(items, refreshing) => (
        <ProductsTable
          products={items}
          categories={categories}
          refreshing={refreshing}
          label="Productos del vendedor"
        />
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

// Ficha de un vendedor (HU26): sus datos, sus productos y sus pedidos, en solo lectura y nada más. Editar,
// bloquear, desbloquear y eliminar al vendedor se hacen desde los botones de su fila en la tabla (HU19).
export default function SellerDetailDialog({ sellerId, onClose }) {
  const styles = useStyles()
  const [tab, setTab] = useState('data')
  const fetcher = useCallback(() => getAdminSeller(sellerId), [sellerId])
  const { data: seller, failed, retry } = useApiQuery(fetcher)

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
                <MessageBarTitle>Modo solo lectura</MessageBarTitle>
                Puedes consultar esta información. Para cambiar los datos del vendedor, usa el botón Editar de la tabla;
                sus productos, precios y pedidos no se pueden cambiar.
              </MessageBarBody>
            </MessageBar>

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
                  <Tab value="products">Productos</Tab>
                  <Tab value="orders">Pedidos</Tab>
                </TabList>
                {tab === 'data' && <SellerDataForm seller={seller} />}
                {tab === 'products' && <SellerProductsTab sellerId={seller.id} />}
                {tab === 'orders' && <SellerOrdersTab sellerId={seller.id} />}
              </>
            )}
          </DialogContent>
        </DialogBody>
      </DialogSurface>
    </Dialog>
  )
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
}
