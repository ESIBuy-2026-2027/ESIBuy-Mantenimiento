import { useCallback } from 'react'
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
import { Add20Regular, Payment24Regular, Phone24Regular, Wallet24Regular } from '@fluentui/react-icons'
import { getPaymentMethods, setDefaultPaymentMethod } from '../api/paymentMethods'
import EmptyState from '../components/EmptyState'
import { usePageStyles } from '../components/pageStyles'
import { useApiQuery } from '../hooks/useApiQuery'

// Cómo se presenta cada tipo de método que devuelve el backend
const TIPOS = {
  TARJETA: { etiqueta: 'Tarjeta', Icono: Payment24Regular },
  PAYPAL: { etiqueta: 'PayPal', Icono: Wallet24Regular },
  BIZUM: { etiqueta: 'Bizum', Icono: Phone24Regular },
}

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

  // Tras el cambio se vuelve a pedir la lista: el backend desmarca el anterior
  const marcarPredeterminado = async (id) => {
    await setDefaultPaymentMethod(id)
    retry()
  }

  return (
    <main className={pageStyles.page}>
      <div className={pageStyles.titleBlock}>
        <Title1 as="h1" className={pageStyles.title}>
          Métodos de pago
        </Title1>
        <Text className={pageStyles.subtitle}>Gestiona las tarjetas y cuentas con las que pagas tus compras.</Text>
      </div>
      {failed && (
        <MessageBar intent="error">
          <MessageBarBody>No se han podido cargar tus métodos de pago. Inténtalo de nuevo.</MessageBarBody>
          <MessageBarActions>
            <Button onClick={retry}>Reintentar</Button>
          </MessageBarActions>
        </MessageBar>
      )}
      {metodos?.length === 0 && (
        <EmptyState
          icon={<Payment24Regular />}
          title="Aún no tienes métodos de pago"
          description="Añade una tarjeta, una cuenta de PayPal o tu teléfono de Bizum para pagar tus compras."
        >
          <Button appearance="primary" icon={<Add20Regular />}>
            Añadir método de pago
          </Button>
        </EmptyState>
      )}
      {metodos?.length > 0 && (
        <ul aria-label="Tus métodos de pago" className={styles.list}>
          {metodos.map((metodo) => {
            const { etiqueta, Icono } = TIPOS[metodo.tipo]
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
                </Card>
              </li>
            )
          })}
        </ul>
      )}
    </main>
  )
}
