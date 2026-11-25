import PropTypes from 'prop-types'
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
  Title3,
  makeStyles,
  mergeClasses,
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
import LinkButton from '../components/LinkButton'
import { PATHS } from '../routes/paths'

// Como el backend: un cliente tiene como máximo 5 direcciones de envío (HU5)
const MAXIMO_DIRECCIONES = 5

// Misma composición que la lista de métodos de pago (HU6)
const useStyles = makeStyles({
  embeddedPage: {
    display: 'flex',
    flexDirection: 'column',
    gap: tokens.spacingVerticalM,
    width: '100%',
    minWidth: 0,
  },
  heading: {
    display: 'flex',
    flexDirection: 'column',
    alignItems: 'stretch',
    justifyContent: 'space-between',
    minHeight: '126px',
  },
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
    minHeight: '104px',
    padding: tokens.spacingHorizontalM,
    boxSizing: 'border-box',
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
    justifyContent: 'flex-end',
    gap: tokens.spacingHorizontalS,
    flexBasis: '100%',
    width: '100%',
    marginLeft: 'auto',
    paddingTop: tokens.spacingVerticalXS,
    borderTop: `1px solid ${tokens.colorNeutralStroke2}`,
  },
  action: {
    display: 'flex',
    flexDirection: 'column',
    alignItems: 'flex-start',
    gap: tokens.spacingVerticalXS,
    minHeight: '40px',
  },
  addButton: {
    minHeight: '40px',
    width: '100%',
  },
  seeAll: {
    alignSelf: 'flex-start',
  },
  addressText: {
    minHeight: '58px',
    justifyContent: 'center',
  },
  muted: {
    color: tokens.colorNeutralForeground3,
  },
})

// Direcciones de envío del cliente en sesión (HU5). La principal es la que el checkout (HU10) usa por defecto.
export default function AddressesPage({ embedded = false }) {
  const pageStyles = usePageStyles()
  const styles = useStyles()
  const fetcher = useCallback(() => getAddresses(), [])
  const { data: direcciones, failed, retry } = useApiQuery(fetcher)

  const [aviso, setAviso] = useState(null)
  // null: panel cerrado; {}: dirección nueva; { direccion }: se edita esa
  const [enPanel, setEnPanel] = useState(null)
  const [aEliminar, setAEliminar] = useState(null)
  const limiteAlcanzado = (direcciones?.length ?? 0) >= MAXIMO_DIRECCIONES
  const direccionesVisibles = embedded ? (direcciones ?? []).slice(0, 3) : direcciones

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

  const Root = embedded ? 'section' : 'main'
  const SectionTitle = embedded ? Title3 : Title1

  return (
    <Root className={embedded ? styles.embeddedPage : pageStyles.page} aria-label={embedded ? 'Direcciones de envío' : undefined}>
      <div className={mergeClasses(pageStyles.heading, embedded && styles.heading)}>
        <div className={pageStyles.titleBlock}>
          <SectionTitle as={embedded ? 'h2' : 'h1'} className={pageStyles.title}>
            Direcciones de envío
          </SectionTitle>
          <Text className={pageStyles.subtitle}>
            Gestiona dónde recibes tus pedidos. La principal es la que se propone al pagar.
          </Text>
        </div>
        <div className={styles.action}>
            <Button
              className={styles.addButton}
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
          {direccionesVisibles.map((direccion) => (
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
      {embedded && (direcciones?.length ?? 0) > 3 && (
        <LinkButton to={PATHS.ADDRESSES} appearance="subtle" className={styles.seeAll}>
          Ver todas las direcciones ({direcciones.length})
        </LinkButton>
      )}
      {aEliminar && (
        <DeleteAddressDialog
          direccion={aEliminar}
          direcciones={direcciones ?? []}
          onClose={() => setAEliminar(null)}
          onDeleted={alEliminar}
        />
      )}
    </Root>
  )
}

AddressesPage.propTypes = {
  embedded: PropTypes.bool,
}
