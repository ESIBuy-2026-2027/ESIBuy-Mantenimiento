import { useState } from 'react'
import PropTypes from 'prop-types'
import { Text, makeStyles, tokens } from '@fluentui/react-components'
import { Checkmark28Filled } from '@fluentui/react-icons'
import { formatPrice } from '../../utils/format'
import { TIPOS_DE_METODO } from '../paymentMethods/tiposDeMetodo'

const PIEZAS_DE_CONFETI = 14
const NOTCH = '24px'

const dateTimeFormatter = new Intl.DateTimeFormat('es-ES', { dateStyle: 'long', timeStyle: 'short' })
const redondear = (importe) => Math.round(importe * 100) / 100
const productos = (n) => `${n} ${n === 1 ? 'producto' : 'productos'}`

// jsdom y algunos navegadores antiguos no tienen matchMedia: entonces se muestra el confeti.
const prefiereMenosMovimiento = () =>
  typeof window.matchMedia === 'function' && window.matchMedia('(prefers-reduced-motion: reduce)').matches

// Barras del código de barras decorativo: anchos fijos para que no cambie entre renderizados.
const BARRAS = [3, 1, 2, 1, 3, 2, 1, 1, 3, 1, 2, 2, 1, 3, 1, 1, 2, 3, 1, 2, 1, 1, 3, 2, 1, 2, 1, 3, 1, 1, 2, 1]

// Posición, retraso, giro y deriva de cada pieza de confeti, repartidos de forma determinista.
const CONFETI = Array.from({ length: PIEZAS_DE_CONFETI }, (_, i) => ({
  left: `${6 + ((i * 37) % 88)}%`,
  delay: `${(i % 5) * 60}ms`,
  giro: `${(i % 2 === 0 ? 1 : -1) * (180 + i * 25)}deg`,
  deriva: `${((i * 13) % 40) - 20}px`,
  tono: i % 3,
}))

const useStyles = makeStyles({
  ticket: {
    position: 'relative',
    width: '100%',
    maxWidth: '440px',
    boxSizing: 'border-box',
    overflow: 'hidden',
    display: 'flex',
    flexDirection: 'column',
    backgroundColor: tokens.colorNeutralBackground1,
    border: `1px solid ${tokens.colorNeutralStroke2}`,
    borderRadius: tokens.borderRadiusXLarge,
    boxShadow: tokens.shadow4,
  },
  header: {
    display: 'flex',
    flexDirection: 'column',
    alignItems: 'center',
    gap: tokens.spacingVerticalS,
    padding: `${tokens.spacingVerticalXXL} ${tokens.spacingHorizontalXL} ${tokens.spacingVerticalL}`,
    textAlign: 'center',
  },
  medallion: {
    display: 'flex',
    alignItems: 'center',
    justifyContent: 'center',
    width: '56px',
    height: '56px',
    borderRadius: tokens.borderRadiusCircular,
    backgroundColor: tokens.colorBrandBackground2,
    color: tokens.colorBrandForeground1,
  },
  muted: {
    color: tokens.colorNeutralForeground3,
  },
  total: {
    fontSize: tokens.fontSizeHero800,
    lineHeight: tokens.lineHeightHero800,
    fontWeight: tokens.fontWeightBold,
    fontVariantNumeric: 'tabular-nums',
  },
  // Línea discontinua con un troquel semicircular a cada lado, como el corte de un ticket.
  perforation: {
    position: 'relative',
    height: NOTCH,
    flexShrink: 0,
    '::before': {
      content: '""',
      position: 'absolute',
      top: '50%',
      left: NOTCH,
      right: NOTCH,
      borderTop: `2px dashed ${tokens.colorNeutralStroke2}`,
    },
  },
  notch: {
    position: 'absolute',
    top: 0,
    width: NOTCH,
    height: NOTCH,
    boxSizing: 'border-box',
    borderRadius: tokens.borderRadiusCircular,
    backgroundColor: tokens.colorNeutralBackground1,
    border: `1px solid ${tokens.colorNeutralStroke2}`,
  },
  notchStart: {
    left: `calc(${NOTCH} / -2)`,
  },
  notchEnd: {
    right: `calc(${NOTCH} / -2)`,
  },
  body: {
    display: 'flex',
    flexDirection: 'column',
    gap: tokens.spacingVerticalL,
    padding: `${tokens.spacingVerticalM} ${tokens.spacingHorizontalXL}`,
  },
  orders: {
    display: 'flex',
    flexDirection: 'column',
    gap: tokens.spacingVerticalM,
    margin: 0,
    padding: 0,
    listStyleType: 'none',
  },
  order: {
    display: 'flex',
    alignItems: 'baseline',
    justifyContent: 'space-between',
    gap: tokens.spacingHorizontalM,
  },
  orderInfo: {
    display: 'flex',
    flexDirection: 'column',
    minWidth: 0,
    overflowWrap: 'anywhere',
  },
  amount: {
    fontWeight: tokens.fontWeightSemibold,
    fontVariantNumeric: 'tabular-nums',
  },
  method: {
    display: 'flex',
    alignItems: 'center',
    gap: tokens.spacingHorizontalM,
    padding: tokens.spacingHorizontalM,
    backgroundColor: tokens.colorNeutralBackground2,
    borderRadius: tokens.borderRadiusLarge,
  },
  methodIcon: {
    display: 'flex',
    alignItems: 'center',
    justifyContent: 'center',
    flexShrink: 0,
    width: '36px',
    height: '36px',
    borderRadius: tokens.borderRadiusCircular,
    backgroundColor: tokens.colorBrandBackground2,
    color: tokens.colorBrandForeground1,
  },
  methodText: {
    display: 'flex',
    flexDirection: 'column',
    minWidth: 0,
  },
  mask: {
    fontVariantNumeric: 'tabular-nums',
    letterSpacing: '0.04em',
  },
  barcode: {
    display: 'flex',
    justifyContent: 'center',
    gap: '2px',
    height: '44px',
    padding: `0 ${tokens.spacingHorizontalXL} ${tokens.spacingVerticalXL}`,
  },
  bar: {
    height: '100%',
    backgroundColor: tokens.colorNeutralForeground1,
  },
  confetti: {
    position: 'absolute',
    inset: 0,
    overflow: 'hidden',
    pointerEvents: 'none',
    '@media (prefers-reduced-motion: reduce)': {
      display: 'none',
    },
  },
  piece: {
    position: 'absolute',
    top: '-12px',
    width: '8px',
    height: '12px',
    borderRadius: tokens.borderRadiusSmall,
    opacity: 0,
    animationName: {
      '0%': { opacity: 1, transform: 'translate(0, 0) rotate(0deg)' },
      '100%': { opacity: 0, transform: 'translate(var(--deriva), 200px) rotate(var(--giro))' },
    },
    animationDuration: '1200ms',
    animationTimingFunction: tokens.curveDecelerateMid,
    animationFillMode: 'both',
  },
  // Solo tonos de la rampa naranja de marca: el naranja es el único acento (DESIGN.md).
  tone0: { backgroundColor: tokens.colorBrandBackground },
  tone1: { backgroundColor: tokens.colorBrandBackgroundHover },
  tone2: { backgroundColor: tokens.colorBrandStroke2 },
})

function Perforation({ styles }) {
  return (
    <div className={styles.perforation} aria-hidden="true">
      <span className={`${styles.notch} ${styles.notchStart}`} />
      <span className={`${styles.notch} ${styles.notchEnd}`} />
    </div>
  )
}

Perforation.propTypes = {
  styles: PropTypes.object.isRequired,
}

// Recibo del pago (HU10) con forma de ticket: el total cobrado, la fecha, un pedido por proveedor y el método de pago
// usado, solo con su máscara. El confeti y el código de barras son decorativos y no llegan a los lectores de pantalla;
// el confeti no aparece si el usuario prefiere reducir el movimiento.
export default function PaymentTicket({ orders, paymentMethod, paidAt }) {
  const styles = useStyles()
  const [conConfeti] = useState(() => !prefiereMenosMovimiento())
  const total = redondear(orders.reduce((suma, order) => suma + order.total, 0))
  const { etiqueta, Icono } = TIPOS_DE_METODO[paymentMethod.tipo]

  return (
    <section aria-label="Recibo del pago" className={styles.ticket}>
      {conConfeti && (
        <div data-testid="confeti" aria-hidden="true" className={styles.confetti}>
          {CONFETI.map((pieza, i) => (
            <span
              key={i}
              className={`${styles.piece} ${styles[`tone${pieza.tono}`]}`}
              style={{ left: pieza.left, animationDelay: pieza.delay, '--giro': pieza.giro, '--deriva': pieza.deriva }}
            />
          ))}
        </div>
      )}

      <div className={styles.header}>
        <span className={styles.medallion} aria-hidden="true">
          <Checkmark28Filled />
        </span>
        <Text className={styles.muted}>Total pagado</Text>
        <span className={styles.total}>{formatPrice(total)}</span>
        <Text size={200} className={styles.muted}>
          <time dateTime={paidAt.toISOString()}>{dateTimeFormatter.format(paidAt)}</time>
        </Text>
      </div>

      <Perforation styles={styles} />

      <div className={styles.body}>
        <ul aria-label="Pedidos realizados" className={styles.orders}>
          {orders.map((order) => (
            <li key={order.id} aria-label={`Pedido ${order.id}`} className={styles.order}>
              <div className={styles.orderInfo}>
                <Text weight="semibold">{order.vendedor.nombreComercial}</Text>
                <Text size={200} className={styles.muted}>
                  Pedido {order.id} · {productos(order.lineas.length)}
                </Text>
              </div>
              <span className={styles.amount}>{formatPrice(order.total)}</span>
            </li>
          ))}
        </ul>

        <div role="group" aria-label="Pagado con" className={styles.method}>
          <span className={styles.methodIcon} aria-hidden="true">
            <Icono />
          </span>
          <div className={styles.methodText}>
            <Text size={200} className={styles.muted}>
              Pagado con {etiqueta}
            </Text>
            <Text weight="semibold" className={styles.mask}>
              {paymentMethod.mascara}
            </Text>
          </div>
        </div>
      </div>

      <Perforation styles={styles} />

      <div data-testid="codigo-de-barras" aria-hidden="true" className={styles.barcode}>
        {BARRAS.map((ancho, i) => (
          <span key={i} className={styles.bar} style={{ width: `${ancho}px` }} />
        ))}
      </div>
    </section>
  )
}

PaymentTicket.propTypes = {
  orders: PropTypes.arrayOf(
    PropTypes.shape({
      id: PropTypes.string.isRequired,
      vendedor: PropTypes.shape({ nombreComercial: PropTypes.string.isRequired }).isRequired,
      total: PropTypes.number.isRequired,
      lineas: PropTypes.array.isRequired,
    }),
  ).isRequired,
  paymentMethod: PropTypes.shape({
    tipo: PropTypes.string.isRequired,
    mascara: PropTypes.string.isRequired,
  }).isRequired,
  paidAt: PropTypes.instanceOf(Date).isRequired,
}
