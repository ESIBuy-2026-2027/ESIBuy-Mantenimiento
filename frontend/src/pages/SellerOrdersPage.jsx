import { useCallback, useMemo, useState } from 'react'
import {
  Badge,
  Button,
  Field,
  Input,
  MessageBar,
  MessageBarBody,
  Select,
  Spinner,
  Table,
  TableBody,
  TableCell,
  TableHeader,
  TableHeaderCell,
  TableRow,
  Text,
  Title1,
  makeStyles,
  mergeClasses,
  tokens,
} from '@fluentui/react-components'
import { Receipt24Regular } from '@fluentui/react-icons'
import { getSellerOrders } from '../api/orders'
import EmptyState from '../components/EmptyState'
import LinkButton from '../components/LinkButton'
import { usePageStyles } from '../components/pageStyles'
import {
  CANCELADO,
  colorDeEstado,
  etiquetaDeEstado,
} from '../features/orders/orderStatus'
import { useApiQuery } from '../hooks/useApiQuery'
import { getSellerOrderDetailPath } from '../routes/paths'
import { formatDateTime, formatPrice } from '../utils/format'

// Solo estados persistidos por el pedido. EN_REPARTO es un paso visual del seguimiento del cliente.
const ESTADOS_TARJETAS = ['PAGADO', 'PREPARANDO', 'ENVIADO', 'ENTREGADO', CANCELADO]
const MAX_IMPORTE_DEFECTO = 200

const useStyles = makeStyles({
  cards: {
    display: 'grid',
    gridTemplateColumns: 'repeat(auto-fit, minmax(150px, 1fr))',
    gap: tokens.spacingHorizontalM,
  },
  cardBtn: {
    backgroundColor: tokens.colorNeutralBackground1,
    borderTopWidth: '1px',
    borderRightWidth: '1px',
    borderBottomWidth: '1px',
    borderLeftWidth: '1px',
    borderStyle: 'solid',
    borderColor: tokens.colorNeutralStroke2,
    borderRadius: tokens.borderRadiusMedium,
    paddingTop: tokens.spacingVerticalM,
    paddingBottom: tokens.spacingVerticalM,
    paddingLeft: tokens.spacingHorizontalL,
    paddingRight: tokens.spacingHorizontalL,
    cursor: 'pointer',
    textAlign: 'left',
    color: tokens.colorNeutralForeground1,
    display: 'flex',
    flexDirection: 'column',
    gap: tokens.spacingVerticalXXS,
    transitionProperty: 'background-color, border-color, box-shadow',
    transitionDuration: '150ms',
    ':hover': {
      backgroundColor: tokens.colorNeutralBackground1Hover,
    },
  },
  cardPagado: {
    backgroundColor: '#EFF6FF',
    borderTopColor: '#93C5FD',
    borderRightColor: '#93C5FD',
    borderBottomColor: '#93C5FD',
    borderLeftColor: '#93C5FD',
    ':hover': {
      backgroundColor: '#DBEAFE',
    },
  },
  cardPreparando: {
    backgroundColor: '#FFFBEB',
    borderTopColor: '#FCD34D',
    borderRightColor: '#FCD34D',
    borderBottomColor: '#FCD34D',
    borderLeftColor: '#FCD34D',
    ':hover': {
      backgroundColor: '#FEF3C7',
    },
  },
  cardEnviado: {
    backgroundColor: '#FFF7ED',
    borderTopColor: '#FDBA74',
    borderRightColor: '#FDBA74',
    borderBottomColor: '#FDBA74',
    borderLeftColor: '#FDBA74',
    ':hover': {
      backgroundColor: '#FFEDD5',
    },
  },
  cardEntregado: {
    backgroundColor: '#F0FDF4',
    borderTopColor: '#86EFAC',
    borderRightColor: '#86EFAC',
    borderBottomColor: '#86EFAC',
    borderLeftColor: '#86EFAC',
    ':hover': {
      backgroundColor: '#DCFCE7',
    },
  },
  cardCancelado: {
    backgroundColor: '#FEF2F2',
    borderTopColor: '#FCA5A5',
    borderRightColor: '#FCA5A5',
    borderBottomColor: '#FCA5A5',
    borderLeftColor: '#FCA5A5',
    ':hover': {
      backgroundColor: '#FEE2E2',
    },
  },
  cardActive: {
    borderTopColor: '#EA580C',
    borderRightColor: '#EA580C',
    borderBottomColor: '#EA580C',
    borderLeftColor: '#EA580C',
    boxShadow: '0 0 0 2px #EA580C',
  },
  cardCount: {
    fontSize: tokens.fontSizeHero700,
    lineHeight: tokens.lineHeightHero700,
    fontWeight: tokens.fontWeightBold,
  },
  cardLabel: {
    fontSize: tokens.fontSizeBase200,
    color: tokens.colorNeutralForeground3,
    fontWeight: tokens.fontWeightSemibold,
  },
  filtersRow: {
    display: 'flex',
    gap: tokens.spacingHorizontalM,
    flexWrap: 'wrap',
    alignItems: 'flex-end',
  },
  searchField: {
    flexGrow: 2,
    flexBasis: '260px',
    minWidth: '200px',
  },
  filterField: {
    flexGrow: 1,
    flexBasis: '200px',
    minWidth: '180px',
  },
  rangeField: {
    flexGrow: 1,
    flexBasis: '280px',
    minWidth: '220px',
  },
  rangeControl: {
    position: 'relative',
    height: '36px',
  },
  rangeInput: {
    position: 'absolute',
    width: '25%',
    height: '36px',
    margin: 0,
    pointerEvents: 'none',
    accentColor: '#EA580C',
    '&::-webkit-slider-thumb': {
      pointerEvents: 'auto',
    },
    '&::-moz-range-thumb': {
      pointerEvents: 'auto',
    },
  },
  clearWrap: {
    display: 'flex',
    alignItems: 'flex-end',
    marginLeft: 'auto',
  },
  tableWrap: {
    backgroundColor: tokens.colorNeutralBackground1,
    borderTopWidth: '1px',
    borderRightWidth: '1px',
    borderBottomWidth: '1px',
    borderLeftWidth: '1px',
    borderStyle: 'solid',
    borderColor: tokens.colorNeutralStroke2,
    borderRadius: tokens.borderRadiusMedium,
    overflowX: 'auto',
  },
  table: {
    minWidth: '760px',
    width: '100%',
  },
  thRight: {
    textAlign: 'right',
    '& > button, & > div': {
      justifyContent: 'flex-end',
      width: '100%',
    },
  },
  tdRight: {
    textAlign: 'right',
  },
  bold: {
    fontWeight: tokens.fontWeightSemibold,
  },
  actions: {
    display: 'flex',
    gap: tokens.spacingHorizontalS,
    justifyContent: 'flex-end',
  },
  emptyCell: {
    textAlign: 'center',
    paddingTop: tokens.spacingVerticalXXL,
    paddingBottom: tokens.spacingVerticalXXL,
    color: tokens.colorNeutralForeground3,
  },
  pager: {
    display: 'flex',
    justifyContent: 'space-between',
    alignItems: 'center',
    color: tokens.colorNeutralForeground3,
  },
})

function contarArticulos(pedido) {
  if (typeof pedido.totalArticulos === 'number') return pedido.totalArticulos
  if (Array.isArray(pedido.lineas)) {
    return pedido.lineas.reduce((acc, linea) => acc + (linea.cantidad ?? 1), 0)
  }
  return '—'
}

export default function SellerOrdersPage() {
  const styles = usePageStyles()
  const local = useStyles()

  const cardColorClass = useMemo(
    () => ({
      PAGADO: local.cardPagado,
      PREPARANDO: local.cardPreparando,
      ENVIADO: local.cardEnviado,
      ENTREGADO: local.cardEntregado,
      [CANCELADO]: local.cardCancelado,
    }),
    [local],
  )

  const fetcher = useCallback(() => getSellerOrders(), [])
  const { data: pedidos, failed, retry } = useApiQuery(fetcher)

  const maxImporteSlider = useMemo(() => {
    if (!pedidos || pedidos.length === 0) return MAX_IMPORTE_DEFECTO
    const mayorTotal = pedidos.reduce((max, p) => Math.max(max, Number(p.total) || 0), 0)
    return Math.max(MAX_IMPORTE_DEFECTO, Math.ceil(mayorTotal))
  }, [pedidos])

  const [query, setQuery] = useState('')
  const [estadoFiltro, setEstadoFiltro] = useState('')
  const [orden, setOrden] = useState('')
  const [importeMin, setImporteMin] = useState(0)
  const [importeMax, setImporteMax] = useState(null)

  const importeMaxEfectivo = importeMax ?? maxImporteSlider

  const hayFiltrosActivos =
    Boolean(query.trim()) ||
    Boolean(estadoFiltro) ||
    Boolean(orden) ||
    importeMin > 0 ||
    importeMaxEfectivo < maxImporteSlider

  const limpiarFiltros = () => {
    setQuery('')
    setEstadoFiltro('')
    setOrden('')
    setImporteMin(0)
    setImporteMax(maxImporteSlider)
  }

  const pedidosFiltrados = useMemo(() => {
    if (!pedidos) return []
    const q = query.trim().toLowerCase()

    const filtrados = pedidos.filter((pedido) => {
      const coincideEstado = !estadoFiltro || pedido.estado === estadoFiltro
      const numero = String(pedido.numeroPedido ?? '').toLowerCase()
      const destinatario = String(pedido.direccionEnvio?.destinatario ?? '').toLowerCase()
      const coincideTexto = !q || numero.includes(q) || destinatario.includes(q)

      const totalNum = Number(pedido.total) || 0
      const coincideMin = totalNum >= importeMin
      const coincideMax =
        importeMaxEfectivo >= maxImporteSlider || totalNum <= importeMaxEfectivo

      return coincideEstado && coincideTexto && coincideMin && coincideMax
    })

    if (!orden) return filtrados

    return [...filtrados].sort((a, b) => {
      if (orden === 'total-asc') return (Number(a.total) || 0) - (Number(b.total) || 0)
      if (orden === 'total-desc') return (Number(b.total) || 0) - (Number(a.total) || 0)
      const fechaA = Date.parse(a.createdAt ?? '') || 0
      const fechaB = Date.parse(b.createdAt ?? '') || 0
      if (orden === 'date-asc') return fechaA - fechaB
      if (orden === 'date-desc') return fechaB - fechaA
      return 0
    })
  }, [pedidos, query, estadoFiltro, orden, importeMin, importeMaxEfectivo, maxImporteSlider])

  const toggleTarjeta = (estado) => {
    setEstadoFiltro((prev) => (prev === estado ? '' : estado))
  }

  return (
    <main className={styles.page}>
      <div className={styles.heading}>
        <div className={styles.titleBlock}>
          <Title1 as="h1" className={styles.title}>
            Gestión de pedidos
          </Title1>
          <Text className={styles.subtitle}>
            Revisa los pedidos que recibes y consulta o avanza su envío desde el detalle.
          </Text>
        </div>
      </div>

      {failed && (
        <MessageBar intent="error">
          <MessageBarBody>
            No se han podido cargar tus pedidos.{' '}
            <Button appearance="transparent" size="small" onClick={retry}>
              Reintentar
            </Button>
          </MessageBarBody>
        </MessageBar>
      )}

      {!pedidos && !failed && <Spinner label="Cargando pedidos…" className={styles.loading} />}

      {pedidos?.length === 0 && (
        <EmptyState
          icon={<Receipt24Regular />}
          title="Todavía no tienes pedidos"
          description="Los pedidos que incluyan tus productos aparecerán aquí."
        />
      )}

      {pedidos && pedidos.length > 0 && (
        <>
          <div className={local.cards}>
            {ESTADOS_TARJETAS.map((estado) => {
              const count = pedidos.filter((p) => p.estado === estado).length
              const activo = estadoFiltro === estado
              return (
                <button
                  key={estado}
                  type="button"
                  className={mergeClasses(
                    local.cardBtn,
                    cardColorClass[estado],
                    activo && local.cardActive,
                  )}
                  onClick={() => toggleTarjeta(estado)}
                >
                  <span className={local.cardCount}>{count}</span>
                  <span className={local.cardLabel}>{etiquetaDeEstado(estado)}</span>
                </button>
              )
            })}
          </div>

          <div className={local.filtersRow}>
            <Field label="Buscar" className={local.searchField}>
              <Input
                type="search"
                value={query}
                onChange={(_, data) => setQuery(data.value)}
                placeholder="Nº de pedido o cliente"
                aria-label="Buscar pedidos"
              />
            </Field>

            <Field label="Ordenar por" className={local.filterField}>
              <Select
                value={orden}
                onChange={(_, data) => setOrden(data.value)}
                aria-label="Ordenar pedidos"
              >
                <option value="">Sin ordenar</option>
                <option value="date-desc">Fecha: más recientes</option>
                <option value="date-asc">Fecha: más antiguos</option>
                <option value="total-asc">Importe: menor a mayor</option>
                <option value="total-desc">Importe: mayor a menor</option>
              </Select>
            </Field>
          </div>

          <div className={local.filtersRow}>
            <Field
              label={`Rango de importe: ${importeMin} € – ${importeMaxEfectivo} €`}
              className={local.rangeField}
            >
              <div className={local.rangeControl}>
                <input
                  className={local.rangeInput}
                  aria-label="Importe mínimo"
                  type="range"
                  min="0"
                  max={maxImporteSlider}
                  step="1"
                  value={importeMin}
                  onChange={(event) => {
                    const next = Math.min(Number(event.target.value), importeMaxEfectivo)
                    setImporteMin(next)
                  }}
                />
                <input
                  className={local.rangeInput}
                  aria-label="Importe máximo"
                  type="range"
                  min="0"
                  max={maxImporteSlider}
                  step="1"
                  value={importeMaxEfectivo}
                  onChange={(event) => {
                    const next = Math.max(Number(event.target.value), importeMin)
                    setImporteMax(next)
                  }}
                />
              </div>
            </Field>

            <div className={local.clearWrap}>
              <Button appearance="subtle" onClick={limpiarFiltros} disabled={!hayFiltrosActivos}>
                Limpiar filtros
              </Button>
            </div>
          </div>

          <div className={local.tableWrap}>
            <Table aria-label="Pedidos de tus productos" className={local.table}>
              <TableHeader>
                <TableRow>
                  <TableHeaderCell>Pedido</TableHeaderCell>
                  <TableHeaderCell>Fecha</TableHeaderCell>
                  <TableHeaderCell>Cliente</TableHeaderCell>
                  <TableHeaderCell className={local.thRight}>Artículos</TableHeaderCell>
                  <TableHeaderCell className={local.thRight}>Total</TableHeaderCell>
                  <TableHeaderCell>Estado</TableHeaderCell>
                  <TableHeaderCell aria-label="Acciones" />
                </TableRow>
              </TableHeader>
              <TableBody>
                {pedidosFiltrados.length > 0 ? (
                  pedidosFiltrados.map((pedido) => (
                    <TableRow key={pedido.id}>
                      <TableCell>
                        <Text className={local.bold}>{pedido.numeroPedido}</Text>
                      </TableCell>
                      <TableCell>{formatDateTime(pedido.createdAt)}</TableCell>
                      <TableCell>{pedido.direccionEnvio?.destinatario ?? '—'}</TableCell>
                      <TableCell className={local.tdRight}>{contarArticulos(pedido)}</TableCell>
                      <TableCell className={local.tdRight}>
                        <Text className={local.bold}>{formatPrice(pedido.total)}</Text>
                      </TableCell>
                      <TableCell>
                        <Badge appearance="tint" color={colorDeEstado(pedido.estado)}>
                          {etiquetaDeEstado(pedido.estado)}
                        </Badge>
                      </TableCell>
                      <TableCell>
                        <div className={local.actions}>
                          <LinkButton to={getSellerOrderDetailPath(pedido.id)} appearance="secondary">
                            Ver detalle
                          </LinkButton>
                        </div>
                      </TableCell>
                    </TableRow>
                  ))
                ) : (
                  <TableRow>
                    <TableCell colSpan={7} className={local.emptyCell}>
                      No hay pedidos con estos filtros.
                    </TableCell>
                  </TableRow>
                )}
              </TableBody>
            </Table>
          </div>

          <div className={local.pager}>
            <Text size={200}>
              {pedidosFiltrados.length} pedido{pedidosFiltrados.length === 1 ? '' : 's'}
            </Text>
          </div>
        </>
      )}
    </main>
  )
}
