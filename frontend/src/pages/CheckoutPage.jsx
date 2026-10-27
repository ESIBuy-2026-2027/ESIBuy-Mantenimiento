import { useState } from 'react'
import {
  Button,
  MessageBar,
  MessageBarActions,
  MessageBarBody,
  Spinner,
  Text,
  Title1,
  makeStyles,
  tokens,
} from '@fluentui/react-components'
import {
  BuildingShop20Regular,
  CheckmarkCircle20Filled,
  Cart24Regular,
  LockClosed20Regular,
} from '@fluentui/react-icons'
import { getCart } from '../api/cart'
import { createOrder } from '../api/orders'
import EmptyState from '../components/EmptyState'
import LinkButton from '../components/LinkButton'
import { useCart } from '../features/cart/useCart'
import { PATHS } from '../routes/paths'
import { formatPrice } from '../utils/format'

// Lo que dura el pago simulado antes de crear los pedidos.
const PAGO_SIMULADO_MS = 700

const esperar = (ms) => new Promise((resolver) => setTimeout(resolver, ms))
const pedidos = (n) => `${n} ${n === 1 ? 'pedido' : 'pedidos'}`
const productos = (n) => `${n} ${n === 1 ? 'producto' : 'productos'}`

const useStyles = makeStyles({
  page: {
    width: '100%',
    maxWidth: '1000px',
    margin: '0 auto',
    boxSizing: 'border-box',
    display: 'flex',
    flexDirection: 'column',
    gap: tokens.spacingVerticalXL,
    padding: `${tokens.spacingVerticalXL} ${tokens.spacingHorizontalL} ${tokens.spacingVerticalXXXL}`,
  },
  heading: {
    display: 'flex',
    flexDirection: 'column',
    gap: tokens.spacingVerticalXS,
  },
  title: {
    margin: 0,
  },
  subtitle: {
    color: tokens.colorNeutralForeground3,
  },
  layout: {
    display: 'grid',
    gridTemplateColumns: 'minmax(0, 1fr)',
    gap: tokens.spacingVerticalXL,
    alignItems: 'start',
    '@media (min-width: 860px)': {
      gridTemplateColumns: 'minmax(0, 1.5fr) minmax(0, 1fr)',
      gap: tokens.spacingHorizontalXXL,
    },
  },
  orders: {
    display: 'flex',
    flexDirection: 'column',
    gap: tokens.spacingVerticalL,
  },
  card: {
    display: 'flex',
    flexDirection: 'column',
    overflow: 'hidden',
    backgroundColor: tokens.colorNeutralBackground1,
    border: `1px solid ${tokens.colorNeutralStroke2}`,
    borderRadius: tokens.borderRadiusXLarge,
    boxShadow: tokens.shadow4,
  },
  cardHeader: {
    display: 'flex',
    alignItems: 'center',
    gap: tokens.spacingHorizontalS,
    margin: 0,
    padding: `${tokens.spacingVerticalM} ${tokens.spacingHorizontalL}`,
    fontSize: tokens.fontSizeBase400,
    fontWeight: tokens.fontWeightSemibold,
    color: tokens.colorBrandForeground1,
    backgroundColor: tokens.colorBrandBackground2,
  },
  lines: {
    display: 'flex',
    flexDirection: 'column',
    margin: 0,
    padding: 0,
    listStyleType: 'none',
  },
  line: {
    display: 'flex',
    alignItems: 'center',
    justifyContent: 'space-between',
    gap: tokens.spacingHorizontalM,
    padding: `${tokens.spacingVerticalM} ${tokens.spacingHorizontalL}`,
    ':not(:first-child)': {
      borderTop: `1px solid ${tokens.colorNeutralStroke2}`,
    },
  },
  lineInfo: {
    display: 'flex',
    flexDirection: 'column',
    gap: tokens.spacingVerticalXXS,
    minWidth: 0,
  },
  lineName: {
    fontWeight: tokens.fontWeightSemibold,
    overflowWrap: 'anywhere',
  },
  amount: {
    fontWeight: tokens.fontWeightBold,
    fontVariantNumeric: 'tabular-nums',
  },
  muted: {
    color: tokens.colorNeutralForeground3,
  },
  summary: {
    display: 'flex',
    flexDirection: 'column',
    gap: tokens.spacingVerticalM,
    padding: tokens.spacingHorizontalXL,
    backgroundColor: tokens.colorNeutralBackground1,
    border: `1px solid ${tokens.colorNeutralStroke2}`,
    borderRadius: tokens.borderRadiusXLarge,
    boxShadow: tokens.shadow8,
    '@media (min-width: 860px)': {
      position: 'sticky',
      top: tokens.spacingVerticalXXL,
    },
  },
  summaryTitle: {
    margin: 0,
    fontSize: tokens.fontSizeBase400,
    fontWeight: tokens.fontWeightSemibold,
  },
  total: {
    display: 'flex',
    alignItems: 'baseline',
    justifyContent: 'space-between',
    gap: tokens.spacingHorizontalM,
  },
  totalAmount: {
    fontSize: tokens.fontSizeHero700,
    lineHeight: tokens.lineHeightHero700,
    fontWeight: tokens.fontWeightBold,
    fontVariantNumeric: 'tabular-nums',
  },
  done: {
    display: 'flex',
    flexDirection: 'column',
    gap: tokens.spacingVerticalL,
  },
  confirmed: {
    display: 'flex',
    flexDirection: 'column',
    margin: 0,
    padding: 0,
    listStyleType: 'none',
    gap: tokens.spacingVerticalS,
  },
  confirmedItem: {
    display: 'flex',
    alignItems: 'center',
    gap: tokens.spacingHorizontalM,
    padding: tokens.spacingHorizontalL,
    backgroundColor: tokens.colorNeutralBackground1,
    border: `1px solid ${tokens.colorNeutralStroke2}`,
    borderRadius: tokens.borderRadiusXLarge,
  },
  check: {
    color: tokens.colorPaletteGreenForeground1,
    flexShrink: 0,
  },
  grow: {
    flexGrow: 1,
    display: 'flex',
    flexDirection: 'column',
    minWidth: 0,
  },
  failure: {
    display: 'flex',
    flexDirection: 'column',
    gap: tokens.spacingVerticalXS,
    padding: tokens.spacingHorizontalL,
    color: tokens.colorPaletteRedForeground1,
    backgroundColor: tokens.colorPaletteRedBackground1,
    border: `1px solid ${tokens.colorPaletteRedBorder1}`,
    borderRadius: tokens.borderRadiusXLarge,
  },
  actions: {
    display: 'flex',
    flexWrap: 'wrap',
    gap: tokens.spacingHorizontalM,
  },
})

// El pedido de un proveedor: su cabecera («Proveedor: total») y sus productos. Cada pedido tiene un solo proveedor.
function VendorOrder({ group, styles }) {
  return (
    <section aria-label={`Pedido de ${group.vendedor.nombreComercial}`} className={styles.card}>
      <h2 className={styles.cardHeader}>
        <BuildingShop20Regular aria-hidden="true" />
        <span>
          {group.vendedor.nombreComercial}: {formatPrice(group.total)}
        </span>
      </h2>
      <ul className={styles.lines}>
        {group.lineas.map((line) => (
          <li key={line.productoId} className={styles.line}>
            <div className={styles.lineInfo}>
              <Text className={styles.lineName}>{line.nombre}</Text>
              <Text size={200} className={styles.muted}>
                {line.cantidad} × {formatPrice(line.precioUnitario)}
              </Text>
            </div>
            <span className={styles.amount}>{formatPrice(line.subtotal)}</span>
          </li>
        ))}
      </ul>
    </section>
  )
}

// Pasarela de pago (HU10): resume el pedido de cada proveedor y, al simular el pago, crea un pedido por proveedor
// (cada uno con un solo proveedor). Si alguno falla, se confirman los demás, se avisa de cuál y se puede reintentar.
export default function CheckoutPage() {
  const styles = useStyles()
  const { cart, status, reload } = useCart()
  const [phase, setPhase] = useState('review')
  const [orders, setOrders] = useState([])
  const [failures, setFailures] = useState([])

  const pay = async () => {
    setPhase('paying')
    setFailures([])
    await esperar(PAGO_SIMULADO_MS)

    let groups
    try {
      groups = (await getCart()).grupos
    } catch {
      setPhase(orders.length > 0 ? 'done' : 'review')
      reload()
      return
    }

    // Un pedido por proveedor, uno detrás de otro. Lo que falla se queda en el carrito.
    const created = []
    const failed = []
    for (const group of groups) {
      try {
        created.push(await createOrder(group.vendedor.id))
      } catch {
        failed.push(group.vendedor)
      }
    }

    reload()
    setOrders((previous) => [...previous, ...created])
    setFailures(failed)
    setPhase(groups.length === 0 ? 'review' : 'done')
  }

  if (phase === 'done') {
    const paid = orders.length > 0

    return (
      <main className={styles.page}>
        <Title1 as="h1" className={styles.title}>
          {paid ? 'Pago realizado' : 'No se ha podido completar el pago'}
        </Title1>
        <div className={styles.done}>
          {paid && (
            <ul aria-label="Pedidos realizados" className={styles.confirmed}>
              {orders.map((order) => (
                <li key={order.id} aria-label={`Pedido ${order.id}`} className={styles.confirmedItem}>
                  <CheckmarkCircle20Filled className={styles.check} aria-hidden="true" />
                  <div className={styles.grow}>
                    <Text weight="semibold">{order.vendedor.nombreComercial}</Text>
                    <Text size={200} className={styles.muted}>
                      Pedido {order.id} · {productos(order.lineas.length)}
                    </Text>
                  </div>
                  <span className={styles.amount}>{formatPrice(order.total)}</span>
                </li>
              ))}
            </ul>
          )}

          {failures.length > 0 && (
            <div role="alert" className={styles.failure}>
              {failures.map((vendedor) => (
                <Text key={vendedor.id}>No se ha podido crear el pedido de {vendedor.nombreComercial}.</Text>
              ))}
              <Text size={200}>Su compra sigue en el carrito, sin pagar.</Text>
            </div>
          )}

          <div className={styles.actions}>
            {failures.length > 0 && (
              <Button appearance="primary" onClick={pay}>
                Reintentar el pago
              </Button>
            )}
            <LinkButton to={PATHS.PRODUCTS} appearance={failures.length > 0 ? 'secondary' : 'primary'}>
              Seguir comprando
            </LinkButton>
          </div>
        </div>
      </main>
    )
  }

  const empty = status === 'ready' && cart.grupos.length === 0

  return (
    <main className={styles.page}>
      <div className={styles.heading}>
        <Title1 as="h1" className={styles.title}>
          Pasarela de pago
        </Title1>
        <Text className={styles.subtitle}>Revisa tu compra antes de pagar.</Text>
      </div>

      {status === 'loading' && <Spinner label="Cargando carrito…" />}

      {status === 'failed' && (
        <MessageBar intent="error">
          <MessageBarBody>No se ha podido cargar el carrito.</MessageBarBody>
          <MessageBarActions>
            <Button onClick={reload}>Reintentar</Button>
          </MessageBarActions>
        </MessageBar>
      )}

      {empty && (
        <EmptyState
          icon={<Cart24Regular />}
          title="Tu carrito está vacío"
          description="Añade productos para poder pagar."
        >
          <LinkButton to={PATHS.PRODUCTS} appearance="primary">
            Ver los productos
          </LinkButton>
        </EmptyState>
      )}

      {status === 'ready' && cart.grupos.length > 0 && (
        <div className={styles.layout}>
          <div className={styles.orders}>
            {cart.grupos.map((group) => (
              <VendorOrder key={group.vendedor.id} group={group} styles={styles} />
            ))}
          </div>

          <section aria-label="Resumen del pago" className={styles.summary}>
            <h2 className={styles.summaryTitle}>Resumen del pago</h2>
            <div className={styles.total}>
              <Text className={styles.muted}>Total a pagar</Text>
              <span className={styles.totalAmount}>{formatPrice(cart.total)}</span>
            </div>
            <Text size={200} className={styles.muted}>
              Se creará un pedido por cada proveedor: {pedidos(cart.grupos.length)} en total.
            </Text>
            <Button
              appearance="primary"
              size="large"
              icon={phase === 'paying' ? <Spinner size="tiny" /> : <LockClosed20Regular />}
              disabled={phase === 'paying'}
              onClick={pay}
            >
              {phase === 'paying' ? 'Procesando pago…' : 'Simular pago'}
            </Button>
            <Text size={200} className={styles.muted}>
              Es una simulación: no se realiza ningún cobro real.
            </Text>
          </section>
        </div>
      )}
    </main>
  )
}
