import PropTypes from 'prop-types'
import { useEffect, useId, useRef, useState } from 'react'
import { Link, useLocation } from 'react-router-dom'
import {
  Badge,
  Button,
  MessageBar,
  MessageBarActions,
  MessageBarBody,
  Spinner,
  Text,
  makeStyles,
  tokens,
} from '@fluentui/react-components'
import {
  BuildingShop20Regular,
  Cart24Filled,
  Cart24Regular,
  Delete20Regular,
  Dismiss20Regular,
} from '@fluentui/react-icons'
import LinkButton from '../../components/LinkButton'
import { PATHS, getProductDetailPath } from '../../routes/paths'
import { formatPrice } from '../../utils/format'
import QuantityStepper from './QuantityStepper'
import { useCart } from './useCart'

const useStyles = makeStyles({
  // Botón flotante: siempre a la vista, abajo a la izquierda.
  fab: {
    position: 'fixed',
    left: tokens.spacingHorizontalL,
    bottom: tokens.spacingVerticalL,
    zIndex: 1000,
  },
  fabButton: {
    width: '56px',
    height: '56px',
    minWidth: '56px',
    boxShadow: tokens.shadow16,
  },
  badge: {
    position: 'absolute',
    top: '-6px',
    right: '-6px',
    animationName: {
      '0%': { transform: 'scale(0.4)' },
      '60%': { transform: 'scale(1.3)' },
      '100%': { transform: 'scale(1)' },
    },
    animationDuration: tokens.durationSlow,
    '@media (prefers-reduced-motion: reduce)': {
      animationName: 'none',
    },
  },
  panel: {
    position: 'fixed',
    left: tokens.spacingHorizontalL,
    bottom: '88px',
    zIndex: 1000,
    display: 'flex',
    flexDirection: 'column',
    width: 'min(400px, calc(100vw - 32px))',
    maxHeight: 'min(72vh, 580px)',
    backgroundColor: tokens.colorNeutralBackground1,
    border: `1px solid ${tokens.colorNeutralStroke2}`,
    borderRadius: tokens.borderRadiusXLarge,
    boxShadow: tokens.shadow64,
    overflow: 'hidden',
    outlineStyle: 'none',
    animationName: {
      from: { opacity: 0, transform: 'translateY(12px) scale(0.98)' },
      to: { opacity: 1, transform: 'translateY(0) scale(1)' },
    },
    animationDuration: tokens.durationNormal,
    animationTimingFunction: tokens.curveDecelerateMid,
    '@media (prefers-reduced-motion: reduce)': {
      animationName: 'none',
    },
  },
  header: {
    display: 'flex',
    alignItems: 'center',
    justifyContent: 'space-between',
    gap: tokens.spacingHorizontalS,
    padding: `${tokens.spacingVerticalM} ${tokens.spacingHorizontalL}`,
    borderBottom: `1px solid ${tokens.colorNeutralStroke2}`,
  },
  title: {
    margin: 0,
    fontSize: tokens.fontSizeBase400,
    fontWeight: tokens.fontWeightSemibold,
  },
  count: {
    color: tokens.colorNeutralForeground3,
    marginLeft: tokens.spacingHorizontalS,
    fontWeight: tokens.fontWeightRegular,
  },
  body: {
    display: 'flex',
    flexDirection: 'column',
    gap: tokens.spacingVerticalM,
    padding: tokens.spacingHorizontalL,
    minHeight: 0,
    overflowY: 'auto',
  },
  group: {
    display: 'flex',
    flexDirection: 'column',
    flexShrink: 0,
    overflow: 'hidden',
    border: `1px solid ${tokens.colorNeutralStroke2}`,
    borderRadius: tokens.borderRadiusLarge,
  },
  groupHeader: {
    display: 'flex',
    alignItems: 'center',
    gap: tokens.spacingHorizontalS,
    margin: 0,
    padding: `${tokens.spacingVerticalS} ${tokens.spacingHorizontalM}`,
    fontSize: tokens.fontSizeBase300,
    fontWeight: tokens.fontWeightSemibold,
    backgroundColor: tokens.colorBrandBackground2,
    color: tokens.colorBrandForeground1,
  },
  lines: {
    display: 'flex',
    flexDirection: 'column',
    margin: 0,
    padding: 0,
    listStyleType: 'none',
    maxHeight: '236px',
    overflowY: 'auto',
  },
  line: {
    display: 'grid',
    gridTemplateColumns: '48px minmax(0, 1fr) auto',
    columnGap: tokens.spacingHorizontalM,
    alignItems: 'center',
    padding: tokens.spacingHorizontalM,
    ':not(:first-child)': {
      borderTop: `1px solid ${tokens.colorNeutralStroke2}`,
    },
  },
  thumb: {
    width: '48px',
    height: '48px',
    objectFit: 'cover',
    borderRadius: tokens.borderRadiusMedium,
    backgroundColor: tokens.colorNeutralBackground3,
  },
  lineInfo: {
    display: 'flex',
    flexDirection: 'column',
    gap: tokens.spacingVerticalXXS,
    minWidth: 0,
  },
  lineName: {
    color: tokens.colorNeutralForeground1,
    fontWeight: tokens.fontWeightSemibold,
    textDecoration: 'none',
    overflowWrap: 'anywhere',
    ':hover': {
      textDecoration: 'underline',
    },
  },
  lineEnd: {
    display: 'flex',
    flexDirection: 'column',
    alignItems: 'flex-end',
    gap: tokens.spacingVerticalXXS,
  },
  subtotal: {
    fontWeight: tokens.fontWeightBold,
    fontVariantNumeric: 'tabular-nums',
  },
  muted: {
    color: tokens.colorNeutralForeground3,
  },
  footer: {
    display: 'flex',
    alignItems: 'center',
    justifyContent: 'space-between',
    gap: tokens.spacingHorizontalS,
    padding: `${tokens.spacingVerticalM} ${tokens.spacingHorizontalL}`,
    borderTop: `1px solid ${tokens.colorNeutralStroke2}`,
    backgroundColor: tokens.colorNeutralBackground2,
  },
  empty: {
    display: 'flex',
    flexDirection: 'column',
    alignItems: 'center',
    gap: tokens.spacingVerticalS,
    padding: `${tokens.spacingVerticalXXL} ${tokens.spacingHorizontalL}`,
    color: tokens.colorNeutralForeground3,
    textAlign: 'center',
  },
  error: {
    color: tokens.colorPaletteRedForeground1,
  },
})

const productos = (n) => `${n} ${n === 1 ? 'producto' : 'productos'}`

function buttonLabel(status, count) {
  if (status !== 'ready') return 'Carrito'
  return count === 0 ? 'Carrito, vacío' : `Carrito, ${productos(count)}`
}

// Un proveedor del carrito: su cabecera («Proveedor: total»), sus productos y los controles de cada uno.
function VendorGroup({ group, busy, onQuantity, onRemove, styles }) {
  const headingId = useId()

  return (
    <div role="group" aria-labelledby={headingId} className={styles.group}>
      <h3 id={headingId} className={styles.groupHeader}>
        <BuildingShop20Regular aria-hidden="true" />
        <span>
          {group.vendedor.nombreComercial}: {formatPrice(group.total)}
        </span>
      </h3>
      <ul className={styles.lines}>
        {group.lineas.map((line) => (
          <li key={line.productoId} className={styles.line}>
            <img className={styles.thumb} src={line.imagen || undefined} alt="" />
            <div className={styles.lineInfo}>
              <Link to={getProductDetailPath(line.productoId)} className={styles.lineName}>
                {line.nombre}
              </Link>
              <Text size={200} className={styles.muted}>
                {formatPrice(line.precioUnitario)} por unidad
              </Text>
              <QuantityStepper
                value={line.cantidad}
                max={line.stock}
                label={`Cantidad de ${line.nombre}`}
                disabled={busy}
                onChange={(quantity) => onQuantity(line.productoId, quantity)}
              />
            </div>
            <div className={styles.lineEnd}>
              <span className={styles.subtotal}>{formatPrice(line.subtotal)}</span>
              <Button
                appearance="subtle"
                size="small"
                icon={<Delete20Regular />}
                aria-label={`Quitar ${line.nombre}`}
                disabled={busy}
                onClick={() => onRemove(line.productoId)}
              />
            </div>
          </li>
        ))}
      </ul>
    </div>
  )
}

// El carrito del cliente (HU9), siempre a la vista abajo a la izquierda: un botón con el número de productos que abre
// un panel con ellos separados por proveedor, cada uno con su total, y con el acceso a la pasarela de pago. Solo lo
// tienen los clientes.
export default function CartWidget() {
  const styles = useStyles()
  const { cart, status, setQuantity, remove, clear, reload } = useCart()
  // El panel está abierto en la página en la que se abrió: al cambiar de página se cierra.
  const { pathname } = useLocation()
  const [openPath, setOpenPath] = useState(null)
  const open = openPath === pathname
  const [busy, setBusy] = useState(false)
  const [error, setError] = useState('')
  const panel = useRef(null)
  const button = useRef(null)

  // Al abrir el panel, el foco pasa a él (para que Escape lo cierre); al cerrarlo, vuelve al botón.
  useEffect(() => {
    if (open) panel.current?.focus()
  }, [open])

  if (status === 'idle') return null

  const close = () => {
    setOpenPath(null)
    button.current?.focus()
  }

  // Lanza una operación sobre el carrito y avisa si el servidor la rechaza.
  const run = async (operation) => {
    setBusy(true)
    setError('')
    try {
      await operation()
    } catch (failure) {
      setError(
        failure?.code === 'INSUFFICIENT_STOCK'
          ? 'No hay existencias suficientes.'
          : 'No se ha podido actualizar el carrito.',
      )
    } finally {
      setBusy(false)
    }
  }

  const count = cart.totalProductos

  return (
    <>
      {open && (
        <section
          ref={panel}
          role="region"
          aria-label="Tu carrito"
          tabIndex={-1}
          className={styles.panel}
          onKeyDown={(event) => event.key === 'Escape' && close()}
        >
          <div className={styles.header}>
            <h2 className={styles.title}>
              Tu carrito
              {status === 'ready' && count > 0 && <span className={styles.count}>{productos(count)}</span>}
            </h2>
            <Button appearance="subtle" size="small" icon={<Dismiss20Regular />} aria-label="Cerrar carrito" onClick={close} />
          </div>

          <div className={styles.body}>
            {status === 'loading' && <Spinner label="Cargando carrito…" />}

            {status === 'failed' && (
              <MessageBar intent="error">
                <MessageBarBody>No se ha podido cargar el carrito.</MessageBarBody>
                <MessageBarActions>
                  <Button onClick={reload}>Reintentar</Button>
                </MessageBarActions>
              </MessageBar>
            )}

            {status === 'ready' && cart.grupos.length === 0 && (
              <div className={styles.empty}>
                <Cart24Regular fontSize={40} aria-hidden="true" />
                <Text weight="semibold">Tu carrito está vacío</Text>
                <Text size={200}>Añade productos desde el listado o desde su detalle.</Text>
              </div>
            )}

            {status === 'ready' &&
              cart.grupos.map((group) => (
                <VendorGroup
                  key={group.vendedor.id}
                  group={group}
                  busy={busy}
                  styles={styles}
                  onQuantity={(productId, quantity) => run(() => setQuantity(productId, quantity))}
                  onRemove={(productId) => run(() => remove(productId))}
                />
              ))}

            {error && (
              <Text role="alert" size={200} className={styles.error}>
                {error}
              </Text>
            )}
          </div>

          {status === 'ready' && cart.grupos.length > 0 && (
            <div className={styles.footer}>
              <Button appearance="subtle" disabled={busy} onClick={() => run(clear)}>
                Vaciar carrito
              </Button>
              <LinkButton to={PATHS.CHECKOUT} appearance="primary">
                Proceder al pago
              </LinkButton>
            </div>
          )}
        </section>
      )}

      <div className={styles.fab}>
        <Button
          ref={button}
          appearance="primary"
          shape="circular"
          size="large"
          className={styles.fabButton}
          icon={<Cart24Filled />}
          aria-label={buttonLabel(status, count)}
          aria-expanded={open}
          onClick={() => setOpenPath(open ? null : pathname)}
        />
        {status === 'ready' && count > 0 && (
          <Badge key={count} className={styles.badge} appearance="filled" color="danger" size="medium" aria-hidden="true">
            {count}
          </Badge>
        )}
      </div>
    </>
  )
}

VendorGroup.propTypes = {
  group: PropTypes.shape({
    vendedor: PropTypes.shape({ id: PropTypes.string.isRequired, nombreComercial: PropTypes.string.isRequired }).isRequired,
    total: PropTypes.number.isRequired,
    lineas: PropTypes.arrayOf(
      PropTypes.shape({
        productoId: PropTypes.string.isRequired,
        nombre: PropTypes.string.isRequired,
        imagen: PropTypes.string,
        cantidad: PropTypes.number.isRequired,
        precioUnitario: PropTypes.number.isRequired,
        subtotal: PropTypes.number.isRequired,
        stock: PropTypes.number.isRequired,
      }),
    ).isRequired,
  }).isRequired,
  busy: PropTypes.bool.isRequired,
  onQuantity: PropTypes.func.isRequired,
  onRemove: PropTypes.func.isRequired,
  styles: PropTypes.object.isRequired,
}
