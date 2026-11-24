import { useCallback, useMemo, useState } from 'react'
import {
  Badge,
  Button,
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
  PASOS_DEL_ENVIO,
  colorDeEstado,
  etiquetaDeEstado,
} from '../features/orders/orderStatus'
import { useApiQuery } from '../hooks/useApiQuery'
import { getSellerOrderDetailPath } from '../routes/paths'
import { formatDateTime, formatPrice } from '../utils/format'

const ESTADOS_FILTRO = [...PASOS_DEL_ENVIO, CANCELADO]

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
    ':hover': {
      backgroundColor: tokens.colorNeutralBackground1Hover,
    },
  },
  cardActive: {
    borderTopColor: '#EA580C',
    borderRightColor: '#EA580C',
    borderBottomColor: '#EA580C',
    borderLeftColor: '#EA580C',
    boxShadow: '0 0 0 1px #EA580C',
  },
  cardCount: {
    fontSize: tokens.fontSizeHero700,
    lineHeight: tokens.lineHeightHero700,
    fontWeight: tokens.fontWeightBold,
  },
  cardLabel: {
    fontSize: tokens.fontSizeBase200,
    color: tokens.colorNeutralForeground3,
  },
  filters: {
    display: 'flex',
    gap: tokens.spacingHorizontalM,
    flexWrap: 'wrap',
  },
  searchInput: {
    flexGrow: 1,
    minWidth: '220px',
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

  const fetcher = useCallback(() => getSellerOrders(), [])
  const { data: pedidos, failed, retry } = useApiQuery(fetcher)

  const [query, setQuery] = useState('')
  const [estadoFiltro, setEstadoFiltro] = useState('')

  const pedidosFiltrados = useMemo(() => {
    if (!pedidos) return []
    const q = query.trim().toLowerCase()
    return pedidos.filter((pedido) => {
      const coincideEstado = !estadoFiltro || pedido.estado === estadoFiltro
      const numero = String(pedido.numeroPedido ?? '').toLowerCase()
      const destinatario = String(pedido.direccionEnvio?.destinatario ?? '').toLowerCase()
      const coincideTexto = !q || numero.includes(q) || destinatario.includes(q)
      return coincideEstado && coincideTexto
    })
  }, [pedidos, query, estadoFiltro])

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
            {PASOS_DEL_ENVIO.map((estado) => {
              const count = pedidos.filter((p) => p.estado === estado).length
              const activo = estadoFiltro === estado
              return (
                <button
                  key={estado}
                  type="button"
                  className={mergeClasses(local.cardBtn, activo && local.cardActive)}
                  onClick={() => toggleTarjeta(estado)}
                >
                  <span className={local.cardCount}>{count}</span>
                  <span className={local.cardLabel}>{etiquetaDeEstado(estado)}</span>
                </button>
              )
            })}
          </div>

          <div className={local.filters}>
            <Input
              type="search"
              value={query}
              onChange={(_, data) => setQuery(data.value)}
              placeholder="Buscar por nº de pedido o cliente"
              aria-label="Buscar pedidos"
              className={local.searchInput}
            />
            <Select
              value={estadoFiltro}
              onChange={(_, data) => setEstadoFiltro(data.value)}
              aria-label="Filtrar por estado"
            >
              <option value="">Todos los estados</option>
              {ESTADOS_FILTRO.map((estado) => (
                <option key={estado} value={estado}>
                  {etiquetaDeEstado(estado)}
                </option>
              ))}
            </Select>
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