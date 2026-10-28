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
import { Add20Regular, Delete20Regular, Payment24Regular } from '@fluentui/react-icons'
import { getPaymentMethods, setDefaultPaymentMethod } from '../api/paymentMethods'
import EmptyState from '../components/EmptyState'
import { usePageStyles } from '../components/pageStyles'
import AddPaymentMethodPanel from '../features/paymentMethods/AddPaymentMethodPanel'
import DeletePaymentMethodDialog from '../features/paymentMethods/DeletePaymentMethodDialog'
import { TIPOS_DE_METODO } from '../features/paymentMethods/tiposDeMetodo'
import { useApiQuery } from '../hooks/useApiQuery'

// Como el backend: un cliente tiene como máximo 5 métodos de pago (HU6)
const MAXIMO_METODOS = 5

const useStyles = makeStyles({
  list: {
    listStyle: 'none',
    margin: 0,
    padding: 0,
    display: 'flex',
    flexDirection: 'column',
    gap: tokens.spacingVerticalM,
  },
  method: {
    display: 'flex',
    flexDirection: 'row',
    alignItems: 'center',
    gap: tokens.spacingHorizontalL,
  },
  icon: {
    color: tokens.colorBrandForeground1,
  },
  methodText: {
    display: 'flex',
    flexDirection: 'column',
    flexGrow: 1,
  },
  action: {
    display: 'flex',
    flexDirection: 'column',
    alignItems: 'flex-end',
    gap: tokens.spacingVerticalXS,
  },
  limit: {
    color: tokens.colorNeutralForeground3,
  },
  mask: {
    fontFamily: tokens.fontFamilyMonospace,
    color: tokens.colorNeutralForeground3,
  },
})

// Métodos de pago del cliente en sesión (HU6): solo se muestran con su máscara.
export default function PaymentMethodsPage() {
  const pageStyles = usePageStyles()
  const styles = useStyles()
  const fetcher = useCallback(() => getPaymentMethods(), [])
  const { data: metodos, failed, retry } = useApiQuery(fetcher)

  const [aviso, setAviso] = useState(null)
  const [anadiendo, setAnadiendo] = useState(false)
  const [aEliminar, setAEliminar] = useState(null)
  const limiteAlcanzado = (metodos?.length ?? 0) >= MAXIMO_METODOS

  const abrirPanel = () => {
    setAviso(null)
    setAnadiendo(true)
  }

  const alGuardar = () => {
    setAnadiendo(false)
    setAviso({ intent: 'success', texto: 'Método de pago añadido.' })
    retry()
  }

  const alEliminar = () => {
    setAEliminar(null)
    setAviso({ intent: 'success', texto: 'Método de pago eliminado.' })
    retry()
  }

  // Tras el cambio se vuelve a pedir la lista: el backend desmarca el anterior. Si el método ya no existe (p. ej.
  // se eliminó desde otra pestaña), se avisa y la lista recargada ya no lo muestra.
  const marcarPredeterminado = async (id) => {
    setAviso(null)
    try {
      await setDefaultPaymentMethod(id)
    } catch {
      setAviso({ intent: 'warning', texto: 'Ese método de pago ya no existe. Hemos actualizado la lista.' })
    }
    retry()
  }

  return (
    <main className={pageStyles.page}>
      <div className={pageStyles.heading}>
        <div className={pageStyles.titleBlock}>
          <Title1 as="h1" className={pageStyles.title}>
            Métodos de pago
          </Title1>
          <Text className={pageStyles.subtitle}>Gestiona las tarjetas y cuentas con las que pagas tus compras.</Text>
        </div>
        {metodos?.length > 0 && (
          <div className={styles.action}>
            <Button appearance="primary" icon={<Add20Regular />} disabled={limiteAlcanzado} onClick={abrirPanel}>
              Añadir método de pago
            </Button>
            {limiteAlcanzado && (
              <Text size={200} className={styles.limit}>
                Has alcanzado el máximo de {MAXIMO_METODOS} métodos de pago.
              </Text>
            )}
          </div>
        )}
      </div>
      {failed && (
        <MessageBar intent="error">
          <MessageBarBody>No se han podido cargar tus métodos de pago. Inténtalo de nuevo.</MessageBarBody>
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
      {anadiendo && <AddPaymentMethodPanel onSaved={alGuardar} onCancel={() => setAnadiendo(false)} />}
      {metodos?.length === 0 && (
        <EmptyState
          icon={<Payment24Regular />}
          title="Aún no tienes métodos de pago"
          description="Añade una tarjeta, una cuenta de PayPal o tu teléfono de Bizum para pagar tus compras."
        >
          <Button appearance="primary" icon={<Add20Regular />} onClick={abrirPanel}>
            Añadir método de pago
          </Button>
        </EmptyState>
      )}
      {metodos?.length > 0 && (
        <ul aria-label="Tus métodos de pago" className={styles.list}>
          {metodos.map((metodo) => {
            const { etiqueta, Icono } = TIPOS_DE_METODO[metodo.tipo]
            return (
              <li key={metodo.id}>
                <Card className={styles.method}>
                  <Icono className={styles.icon} aria-hidden />
                  <div className={styles.methodText}>
                    <Text weight="semibold">{etiqueta}</Text>
                    <Text className={styles.mask}>{metodo.mascara}</Text>
                  </div>
                  {metodo.predeterminado ? (
                    <Badge appearance="tint" color="brand">
                      Predeterminado
                    </Badge>
                  ) : (
                    <Button appearance="subtle" onClick={() => marcarPredeterminado(metodo.id)}>
                      Marcar como predeterminado
                    </Button>
                  )}
                  <Button appearance="subtle" icon={<Delete20Regular />} onClick={() => setAEliminar(metodo)}>
                    Eliminar
                  </Button>
                </Card>
              </li>
            )
          })}
        </ul>
      )}
      {aEliminar && (
        <DeletePaymentMethodDialog
          metodo={aEliminar}
          metodos={metodos ?? []}
          onClose={() => setAEliminar(null)}
          onDeleted={alEliminar}
        />
      )}
    </main>
  )
}
