import { useCallback, useState } from 'react'
import {
  Badge,
  Button,
  Card,
  MessageBar,
  MessageBarActions,
  MessageBarBody,
  Text,
  Title1,
  makeStyles,
  tokens,
} from '@fluentui/react-components'
import { Add20Regular, Delete20Regular, Edit20Regular, Location24Regular } from '@fluentui/react-icons'
import { getAddresses, setPrincipalAddress } from '../api/addresses'
import EmptyState from '../components/EmptyState'
import { usePageStyles } from '../components/pageStyles'
import AddressFormPanel from '../features/addresses/AddressFormPanel'
import DeleteAddressDialog from '../features/addresses/DeleteAddressDialog'
import { resumenDeDireccion } from '../features/addresses/direccionPropType'
import { useApiQuery } from '../hooks/useApiQuery'

// Como el backend: un cliente tiene como máximo 5 direcciones de envío (HU5)
const MAXIMO_DIRECCIONES = 5

// Misma composición que la lista de métodos de pago (HU6)
const useStyles = makeStyles({
  list: {
    listStyle: 'none',
    margin: 0,
    padding: 0,
    display: 'flex',
    flexDirection: 'column',
    gap: tokens.spacingVerticalM,
  },
  // En pantallas estrechas los botones bajan a una segunda línea en lugar de apretar la dirección
  address: {
    display: 'flex',
    flexDirection: 'row',
    flexWrap: 'wrap',
    alignItems: 'center',
    gap: tokens.spacingHorizontalL,
  },
  icon: {
    flexShrink: 0,
    color: tokens.colorBrandForeground1,
  },
  addressText: {
    display: 'flex',
    flexDirection: 'column',
    flexGrow: 1,
    minWidth: 0,
  },
  addressActions: {
    display: 'flex',
    flexWrap: 'wrap',
    alignItems: 'center',
    gap: tokens.spacingHorizontalS,
    marginLeft: 'auto',
  },
  action: {
    display: 'flex',
    flexDirection: 'column',
    alignItems: 'flex-end',
    gap: tokens.spacingVerticalXS,
  },
  muted: {
    color: tokens.colorNeutralForeground3,
  },
})

// Direcciones de envío del cliente en sesión (HU5). La principal es la que el checkout (HU10) usa por defecto.
export default function AddressesPage() {
  const pageStyles = usePageStyles()
  const styles = useStyles()
  const fetcher = useCallback(() => getAddresses(), [])
  const { data: direcciones, failed, retry } = useApiQuery(fetcher)

  const [aviso, setAviso] = useState(null)
  // null: panel cerrado; {}: dirección nueva; { direccion }: se edita esa
  const [enPanel, setEnPanel] = useState(null)
  const [aEliminar, setAEliminar] = useState(null)
  const limiteAlcanzado = (direcciones?.length ?? 0) >= MAXIMO_DIRECCIONES

  const abrirPanel = (direccion) => {
    setAviso(null)
    setEnPanel({ direccion })
  }

  const alGuardar = () => {
    setAviso({ intent: 'success', texto: enPanel.direccion ? 'Dirección guardada.' : 'Dirección añadida.' })
    setEnPanel(null)
    retry()
  }

  const alEliminar = () => {
    setAEliminar(null)
    setAviso({ intent: 'success', texto: 'Dirección eliminada.' })
    retry()
  }

  // Tras el cambio se vuelve a pedir la lista: el backend desmarca la anterior. Si la dirección ya no existe (p. ej.
  // se eliminó desde otra pestaña), se avisa y la lista recargada ya no la muestra.
  const marcarPrincipal = async (id) => {
    setAviso(null)
    try {
      await setPrincipalAddress(id)
    } catch {
      setAviso({ intent: 'warning', texto: 'Esa dirección ya no existe. Hemos actualizado la lista.' })
    }
    retry()
  }

  return (
    <main className={pageStyles.page}>
      <div className={pageStyles.heading}>
        <div className={pageStyles.titleBlock}>
          <Title1 as="h1" className={pageStyles.title}>
            Direcciones de envío
          </Title1>
          <Text className={pageStyles.subtitle}>
            Gestiona dónde recibes tus pedidos. La principal es la que se propone al pagar.
          </Text>
        </div>
        {direcciones?.length > 0 && (
          <div className={styles.action}>
            <Button
              appearance="primary"
              icon={<Add20Regular />}
              disabled={limiteAlcanzado}
              onClick={() => abrirPanel()}
            >
              Añadir dirección
            </Button>
            {limiteAlcanzado && (
              <Text size={200} className={styles.muted}>
                Has alcanzado el máximo de {MAXIMO_DIRECCIONES} direcciones.
              </Text>
            )}
          </div>
        )}
      </div>
      {failed && (
        <MessageBar intent="error">
          <MessageBarBody>No se han podido cargar tus direcciones. Inténtalo de nuevo.</MessageBarBody>
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
      {enPanel && (
        <AddressFormPanel
          key={enPanel.direccion?.id ?? 'nueva'}
          direccion={enPanel.direccion}
          onSaved={alGuardar}
          onCancel={() => setEnPanel(null)}
        />
      )}
      {direcciones?.length === 0 && !enPanel && (
        <EmptyState
          icon={<Location24Regular />}
          title="Aún no tienes direcciones de envío"
          description="Añade la dirección donde quieres recibir tus pedidos."
        >
          <Button appearance="primary" icon={<Add20Regular />} onClick={() => abrirPanel()}>
            Añadir dirección
          </Button>
        </EmptyState>
      )}
      {direcciones?.length > 0 && (
        <ul aria-label="Tus direcciones" className={styles.list}>
          {direcciones.map((direccion) => (
            <li key={direccion.id}>
              <Card className={styles.address}>
                <Location24Regular className={styles.icon} aria-hidden />
                <div className={styles.addressText}>
                  <Text weight="semibold">{direccion.destinatario}</Text>
                  <Text>{resumenDeDireccion(direccion)}</Text>
                  <Text className={styles.muted}>Teléfono: {direccion.telefono}</Text>
                </div>
                <div className={styles.addressActions}>
                  {direccion.principal ? (
                    <Badge appearance="tint" color="brand">
                      Principal
                    </Badge>
                  ) : (
                    <Button appearance="subtle" onClick={() => marcarPrincipal(direccion.id)}>
                      Marcar como principal
                    </Button>
                  )}
                  <Button appearance="subtle" icon={<Edit20Regular />} onClick={() => abrirPanel(direccion)}>
                    Editar
                  </Button>
                  <Button appearance="subtle" icon={<Delete20Regular />} onClick={() => setAEliminar(direccion)}>
                    Eliminar
                  </Button>
                </div>
              </Card>
            </li>
          ))}
        </ul>
      )}
      {aEliminar && (
        <DeleteAddressDialog
          direccion={aEliminar}
          direcciones={direcciones ?? []}
          onClose={() => setAEliminar(null)}
          onDeleted={alEliminar}
        />
      )}
    </main>
  )
}
