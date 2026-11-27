import { useCallback, useState } from 'react'
import PropTypes from 'prop-types'
import {
  Badge,
  Button,
  MessageBar,
  MessageBarActions,
  MessageBarBody,
  Spinner,
  Subtitle1,
  Table,
  TableBody,
  TableCell,
  TableHeader,
  TableHeaderCell,
  TableRow,
  Text,
  Title1,
  makeStyles,
  tokens,
  useId,
} from '@fluentui/react-components'
import {
  ArrowTrending24Regular,
  ArrowTrendingDown24Regular,
  ChartMultiple24Regular,
  ShoppingBag24Regular,
  Star24Filled,
  ThumbDislike24Regular,
} from '@fluentui/react-icons'
import { getSellerStatistics } from '../../api/statistics'
import EmptyState from '../../components/EmptyState'
import { usePageStyles } from '../../components/pageStyles'
import { useTableStyles } from '../../components/tableStyles'
import { useApiQuery } from '../../hooks/useApiQuery'
import { formatPrice, formatRating } from '../../utils/format'

const SIN_VALORACIONES = 'Sin valoraciones'
const ACCENT_ORANGE = '#EA580C'
const ACCENT_AMBER = '#D97706'

const useStyles = makeStyles({
  page: {
    width: '100%',
    minWidth: 0,
    maxWidth: '1320px',
    margin: '0 auto',
    boxSizing: 'border-box',
    overflowX: 'clip',
    display: 'flex',
    flexDirection: 'column',
    gap: tokens.spacingVerticalXL,
    padding: `${tokens.spacingVerticalXL} ${tokens.spacingHorizontalL} ${tokens.spacingVerticalXXXL}`,
    backgroundColor: 'transparent',
  },
  dashboard: {
    display: 'flex',
    flexDirection: 'column',
    gap: tokens.spacingVerticalXL,
    width: '100%',
    minWidth: 0,
    animationName: {
      '0%': { opacity: 0, transform: 'translateY(12px)' },
      '100%': { opacity: 1, transform: 'translateY(0)' },
    },
    animationDuration: '420ms',
    animationTimingFunction: 'cubic-bezier(0.2, 0.8, 0.2, 1)',
    animationFillMode: 'both',
    '@media (prefers-reduced-motion: reduce)': {
      animationName: 'none',
    },
  },
  heading: {
    margin: 0,
  },
  sectionHeader: {
    display: 'flex',
    alignItems: 'center',
    gap: tokens.spacingHorizontalS,
  },
  blockHeader: {
    display: 'flex',
    alignItems: 'center',
    justifyContent: 'space-between',
    gap: tokens.spacingHorizontalS,
  },
  iconBadge: {
    width: '36px',
    height: '36px',
    borderRadius: tokens.borderRadiusLarge,
    backgroundColor: '#FFF4EA',
    color: ACCENT_ORANGE,
    display: 'inline-flex',
    alignItems: 'center',
    justifyContent: 'center',
    flexShrink: 0,
  },
  iconBadgeMuted: {
    width: '36px',
    height: '36px',
    borderRadius: tokens.borderRadiusLarge,
    backgroundColor: '#FEF3C7',
    color: ACCENT_AMBER,
    display: 'inline-flex',
    alignItems: 'center',
    justifyContent: 'center',
    flexShrink: 0,
  },
  block: {
    display: 'flex',
    flexDirection: 'column',
    gap: tokens.spacingVerticalM,
    padding: tokens.spacingVerticalL,
    border: `1px solid ${tokens.colorNeutralStroke2}`,
    borderTop: `3px solid ${ACCENT_AMBER}`,
    borderRadius: tokens.borderRadiusXLarge,
    backgroundColor: tokens.colorNeutralBackground1,
    boxShadow: tokens.shadow8,
    height: '100%',
    boxSizing: 'border-box',
  },
  kpiGrid: {
    display: 'grid',
    gridTemplateColumns: 'repeat(4, minmax(0, 1fr))',
    gap: tokens.spacingHorizontalM,
    '@media (max-width: 1200px)': {
      gridTemplateColumns: 'repeat(2, minmax(0, 1fr))',
    },
    '@media (max-width: 560px)': {
      gridTemplateColumns: '1fr',
    },
  },
  kpiCard: {
    display: 'flex',
    alignItems: 'center',
    gap: tokens.spacingHorizontalM,
    minWidth: 0,
    padding: tokens.spacingVerticalL,
    border: `1px solid ${tokens.colorNeutralStroke2}`,
    borderTop: `3px solid ${ACCENT_ORANGE}`,
    borderRadius: tokens.borderRadiusXLarge,
    backgroundColor: tokens.colorNeutralBackground1,
    boxShadow: tokens.shadow8,
    transitionProperty: 'transform, box-shadow',
    transitionDuration: tokens.durationNormal,
    ':hover': {
      transform: 'translateY(-2px)',
      boxShadow: tokens.shadow16,
    },
  },
  kpiIcon: {
    width: '42px',
    height: '42px',
    borderRadius: tokens.borderRadiusLarge,
    backgroundColor: '#FFF4EA',
    color: ACCENT_ORANGE,
    display: 'inline-flex',
    alignItems: 'center',
    justifyContent: 'center',
    flexShrink: 0,
  },
  kpiMeta: {
    display: 'flex',
    flexDirection: 'column',
    gap: tokens.spacingVerticalXXS,
    minWidth: 0,
  },
  summaryLabel: {
    color: tokens.colorNeutralForeground2,
  },
  chartGrid: {
    display: 'grid',
    gridTemplateColumns: 'repeat(2, minmax(0, 1fr))',
    gap: tokens.spacingHorizontalL,
    minWidth: 0,
    alignItems: 'stretch',
    '@media (max-width: 1200px)': {
      gridTemplateColumns: '1fr',
    },
  },
  chartCard: {
    display: 'flex',
    flexDirection: 'column',
    gap: tokens.spacingVerticalM,
    minWidth: 0,
    padding: tokens.spacingVerticalL,
    border: `1px solid ${tokens.colorNeutralStroke2}`,
    borderRadius: tokens.borderRadiusXLarge,
    backgroundImage: 'linear-gradient(145deg, #FFFFFF 72%, #FFF8F1 100%)',
    boxShadow: tokens.shadow8,
  },
  chartHeader: {
    display: 'flex',
    alignItems: 'center',
    justifyContent: 'space-between',
    flexWrap: 'wrap',
    gap: tokens.spacingHorizontalS,
  },
  trendSvg: {
    display: 'block',
    width: '100%',
    height: 'auto',
    aspectRatio: '720 / 240',
    overflow: 'hidden',
  },
  chartGridLine: {
    stroke: tokens.colorNeutralStroke2,
    strokeDasharray: '3 5',
    strokeWidth: 1,
  },
  chartLabel: {
    fill: tokens.colorNeutralForeground3,
    fontSize: '12px',
    fontFamily: 'inherit',
  },
  chartArea: {
    fill: ACCENT_ORANGE,
    fillOpacity: 0.1,
  },
  chartLine: {
    fill: 'none',
    stroke: ACCENT_ORANGE,
    strokeWidth: 3,
    strokeLinecap: 'round',
    strokeLinejoin: 'round',
  },
  chartSinglePointLine: {
    stroke: ACCENT_ORANGE,
    strokeWidth: 3,
    strokeLinecap: 'round',
    opacity: 0.45,
  },
  chartValueLabel: {
    fill: ACCENT_ORANGE,
    fontSize: '13px',
    fontWeight: tokens.fontWeightSemibold,
    fontFamily: 'inherit',
  },
  distributionList: {
    display: 'flex',
    flexDirection: 'column',
    gap: tokens.spacingVerticalM,
  },
  distributionRow: {
    display: 'grid',
    gridTemplateColumns: '44px minmax(0, 1fr) 88px',
    alignItems: 'center',
    gap: tokens.spacingHorizontalS,
  },
  distributionTrack: {
    height: '10px',
    borderRadius: tokens.borderRadiusCircular,
    backgroundColor: '#F3F3F3',
    overflow: 'hidden',
  },
  distributionFill: {
    height: '100%',
    borderRadius: tokens.borderRadiusCircular,
    backgroundColor: ACCENT_AMBER,
    transitionProperty: 'width',
    transitionDuration: tokens.durationSlow,
    '@media (prefers-reduced-motion: reduce)': {
      transitionDuration: '0.01ms',
    },
  },
  rankingGrid: {
    display: 'grid',
    gridTemplateColumns: 'repeat(2, minmax(0, 1fr))',
    gap: tokens.spacingHorizontalL,
    alignItems: 'stretch',
    minWidth: 0,
    '@media (max-width: 1200px)': {
      gridTemplateColumns: '1fr',
    },
  },
  rankingRows: {
    display: 'flex',
    flexDirection: 'column',
    gap: tokens.spacingVerticalS,
    listStyleType: 'none',
    margin: 0,
    padding: 0,
  },
  rankingHeader: {
    display: 'grid',
    gridTemplateColumns: '26px 48px minmax(0, 1fr) auto',
    gap: tokens.spacingHorizontalS,
    padding: `${tokens.spacingVerticalS} ${tokens.spacingHorizontalS}`,
    color: tokens.colorNeutralForeground2,
    backgroundColor: '#F7F5FC',
    fontWeight: tokens.fontWeightSemibold,
    fontSize: tokens.fontSizeBase200,
  },
  rankingHeadingRight: {
    textAlign: 'right',
  },
  rankingRow: {
    display: 'grid',
    gridTemplateColumns: '26px 48px minmax(0, 1fr) auto',
    alignItems: 'center',
    gap: tokens.spacingHorizontalS,
    minWidth: 0,
    padding: tokens.spacingVerticalS,
    borderBottom: `1px solid ${tokens.colorNeutralStroke2}`,
  },
  rankNumber: {
    width: '26px',
    height: '26px',
    borderRadius: tokens.borderRadiusCircular,
    backgroundColor: '#FEF3C7',
    color: ACCENT_ORANGE,
    fontVariantNumeric: 'tabular-nums',
    textAlign: 'center',
    display: 'inline-flex',
    alignItems: 'center',
    justifyContent: 'center',
    fontWeight: tokens.fontWeightSemibold,
  },
  productThumb: {
    width: '44px',
    height: '44px',
    borderRadius: tokens.borderRadiusMedium,
    objectFit: 'cover',
    backgroundColor: '#FFF4EA',
  },
  productFallback: {
    width: '44px',
    height: '44px',
    borderRadius: tokens.borderRadiusMedium,
    display: 'inline-flex',
    alignItems: 'center',
    justifyContent: 'center',
    backgroundColor: '#FFF4EA',
    color: ACCENT_ORANGE,
  },
  rankingName: {
    minWidth: 0,
    overflow: 'hidden',
    textOverflow: 'ellipsis',
    whiteSpace: 'nowrap',
  },
  rankingMetric: {
    color: tokens.colorNeutralForeground2,
    textAlign: 'right',
    whiteSpace: 'nowrap',
  },
  chartTitleRow: {
    display: 'flex',
    alignItems: 'center',
    gap: tokens.spacingHorizontalXS,
    color: tokens.colorNeutralForeground2,
    fontSize: tokens.fontSizeBase400,
    fontWeight: tokens.fontWeightSemibold,
  },
  topTen: {
    color: ACCENT_ORANGE,
    backgroundColor: '#FFF4EA',
    borderRadius: tokens.borderRadiusCircular,
    padding: `2px ${tokens.spacingHorizontalS}`,
    fontSize: tokens.fontSizeBase200,
  },
  stockSummaryGrid: {
    display: 'grid',
    gridTemplateColumns: 'repeat(4, minmax(0, 1fr))',
    gap: tokens.spacingHorizontalM,
    '@media (max-width: 900px)': {
      gridTemplateColumns: 'repeat(2, minmax(0, 1fr))',
    },
    '@media (max-width: 520px)': {
      gridTemplateColumns: '1fr',
    },
  },
  stockSummaryItem: {
    minWidth: 0,
    margin: 0,
    padding: tokens.spacingVerticalM,
    border: `1px solid ${tokens.colorNeutralStroke2}`,
    borderRadius: tokens.borderRadiusLarge,
    backgroundColor: tokens.colorNeutralBackground2,
  },
  stockAlerts: {
    display: 'grid',
    gridTemplateColumns: 'repeat(2, minmax(0, 1fr))',
    gap: tokens.spacingHorizontalL,
    '@media (max-width: 900px)': {
      gridTemplateColumns: '1fr',
    },
  },
  stockAlert: {
    display: 'flex',
    flexDirection: 'column',
    gap: tokens.spacingVerticalM,
    minWidth: 0,
    padding: tokens.spacingVerticalM,
    border: `1px solid ${tokens.colorNeutralStroke2}`,
    borderRadius: tokens.borderRadiusLarge,
    backgroundColor: tokens.colorNeutralBackground2,
  },
  stockAlertHeading: {
    display: 'flex',
    alignItems: 'center',
    justifyContent: 'space-between',
    gap: tokens.spacingHorizontalS,
  },
  kpiValue: {
    margin: 0,
    color: '#17152B',
    fontSize: tokens.fontSizeBase600,
    fontWeight: tokens.fontWeightSemibold,
    overflowWrap: 'anywhere',
  },
})

function normalizeList(raw, limit = 10) {
  if (!Array.isArray(raw)) return []
  return raw.slice(0, limit).map((item, idx) => ({
    id: String(item.productoId ?? item.id ?? idx),
    nombre: item.nombre ?? item.productoNombre ?? 'Producto',
    imagen: item.imagen ?? '',
    unidades: Number(item.unidades ?? item.unidadesVendidas ?? item.cantidad ?? item.ventas ?? 0),
    ingresos: Number(item.ingresos ?? item.totalIngresos ?? item.importe ?? 0),
    media: item.media !== undefined && item.media !== null ? Number(item.media) : null,
    numero: Number(item.numero ?? item.totalValoraciones ?? item.valoraciones ?? 0),
  }))
}

function resolveAverageRating(stats) {
  const candidates = [
    stats?.valoracionMedia,
    stats?.mediaValoraciones,
    stats?.valoraciones?.media,
    stats?.ratingMedio,
  ]
  for (const val of candidates) {
    if (typeof val === 'number' && !Number.isNaN(val)) return val
  }
  return null
}

function StatsBlock({ title, icon, muted = false, aside, children }) {
  const styles = useStyles()
  const titleId = useId('bloque')

  return (
    <section aria-labelledby={titleId} className={styles.block}>
      <div className={styles.blockHeader}>
        <div className={styles.sectionHeader}>
          <span className={muted ? styles.iconBadgeMuted : styles.iconBadge} aria-hidden="true">
            {icon}
          </span>
          <Subtitle1 as="h3" id={titleId} className={styles.heading}>
            {title}
          </Subtitle1>
        </div>
        {aside}
      </div>
      {children}
    </section>
  )
}

StatsBlock.propTypes = {
  title: PropTypes.string.isRequired,
  icon: PropTypes.element.isRequired,
  muted: PropTypes.bool,
  aside: PropTypes.node,
  children: PropTypes.node.isRequired,
}

function SalesTrendChart({ items }) {
  const styles = useStyles()
  const valores = items.map((punto) => punto.unidades)
  const maximo = Math.max(4, Math.ceil(Math.max(...valores, 1) / 4) * 4)
  const alto = 240
  const ancho = 720
  const margenIzquierdo = 48
  const margenDerecho = 18
  const margenSuperior = 18
  const margenInferior = 38
  const anchoGrafico = ancho - margenIzquierdo - margenDerecho
  const altoGrafico = alto - margenSuperior - margenInferior
  const coords = valores.map((valor, indice) => ({
    x: items.length < 2
      ? margenIzquierdo + anchoGrafico / 2
      : margenIzquierdo + (indice * anchoGrafico) / (items.length - 1),
    y: margenSuperior + altoGrafico - (valor / maximo) * altoGrafico,
  }))
  const linea = coords.map(({ x, y }, indice) => `${indice === 0 ? 'M' : 'L'} ${x} ${y}`).join(' ')
  const area = coords.length > 0
    ? `${linea} L ${coords.at(-1).x} ${margenSuperior + altoGrafico} L ${coords[0].x} ${margenSuperior + altoGrafico} Z`
    : ''
  const puntosConDatos = items.length > 0
  const fecha = (valor) => new Date(`${valor}T00:00:00`).toLocaleDateString('es-ES', {
    day: 'numeric',
    month: 'short',
  })
  const indicesFecha = items.map((_, indice) => indice)

  return (
    <div className={styles.chartCard} role="region" aria-label="Evolución de ventas">
      <div className={styles.chartHeader}>
        <div className={styles.chartTitleRow}>
          <ChartMultiple24Regular style={{ color: ACCENT_ORANGE }} />
          <Text weight="semibold">Evolución de ventas</Text>
        </div>
      </div>
      {puntosConDatos ? (
        <>
          <svg
            className={styles.trendSvg}
            viewBox={`0 0 ${ancho} ${alto}`}
            role="img"
            aria-label="Gráfico de unidades vendidas por día"
          >
            {[0, 1, 2, 3, 4].map((indice) => {
              const y = margenSuperior + (altoGrafico * indice) / 4
              const etiqueta = Math.round(maximo * (1 - indice / 4))
              return (
                <g key={indice}>
                  <line
                    className={styles.chartGridLine}
                    x1={margenIzquierdo}
                    x2={ancho - margenDerecho}
                    y1={y}
                    y2={y}
                  />
                  <text className={styles.chartLabel} x={margenIzquierdo - 10} y={y + 4} textAnchor="end">
                    {etiqueta}
                  </text>
                </g>
              )
            })}
            <path className={styles.chartArea} d={area} />
            {coords.length === 1 && valores[0] > 0 && (
              <line
                className={styles.chartSinglePointLine}
                x1={coords[0].x}
                x2={coords[0].x}
                y1={coords[0].y}
                y2={margenSuperior + altoGrafico}
              />
            )}
            <path className={styles.chartLine} d={linea} />
            {coords.length === 1 && valores[0] > 0 && (
              <text
                className={styles.chartValueLabel}
                x={coords[0].x}
                y={coords[0].y - 12}
                textAnchor="middle"
              >
                {valores[0]}
              </text>
            )}
            {coords.map(({ x, y }, indice) => (
              <circle key={items[indice].fecha} cx={x} cy={y} r="4" fill="white" stroke={ACCENT_ORANGE} strokeWidth="2" />
            ))}
            {indicesFecha.map((indice) => (
              <text
                key={items[indice].fecha}
                className={styles.chartLabel}
                x={coords[indice].x}
                y={alto - 8}
                textAnchor={items.length === 1
                  ? 'middle'
                  : indice === 0 ? 'start' : indice === items.length - 1 ? 'end' : 'middle'}
              >
                {fecha(items[indice].fecha)}
              </text>
            ))}
          </svg>
        </>
      ) : (
        <EmptyState
          icon={<ShoppingBag24Regular />}
          headingLevel="h4"
          title="Sin ventas en este período"
          description="Cuando registres ventas, aparecerán aquí."
        />
      )}
    </div>
  )
}

SalesTrendChart.propTypes = {
  items: PropTypes.arrayOf(PropTypes.object).isRequired,
}

function RatingDistributionCard({ items, total }) {
  const styles = useStyles()
  const distribution = Array.isArray(items) ? items : []

  return (
    <div className={styles.chartCard} role="region" aria-label="Distribución de valoraciones">
      <div className={styles.chartTitleRow}>
        <Star24Filled style={{ color: ACCENT_ORANGE }} />
        <Text weight="semibold">Distribución de valoraciones</Text>
      </div>
      {total > 0 ? (
        <>
          <div className={styles.distributionList}>
            {[5, 4, 3, 2, 1].map((estrellas) => {
              const puntuacion = distribution.find((item) => item.puntuacion === estrellas)
              const cantidad = puntuacion?.numero ?? 0
              const porcentaje = puntuacion?.porcentaje ?? 0
              return (
                <div key={estrellas} className={styles.distributionRow}>
                  <Text size={200}>{estrellas} ★</Text>
                  <div
                    className={styles.distributionTrack}
                    role="progressbar"
                    aria-label={`${estrellas} estrellas`}
                    aria-valuemin={0}
                    aria-valuemax={100}
                    aria-valuenow={porcentaje}
                  >
                    <div className={styles.distributionFill} style={{ width: `${porcentaje}%` }} />
                  </div>
                  <Text size={200} className={styles.rankingMetric}>
                    {cantidad} ({Math.round(porcentaje)}%)
                  </Text>
                </div>
              )
            })}
          </div>
          <Text size={200}>{total} valoraciones en total</Text>
        </>
      ) : (
        <EmptyState
          icon={<Star24Filled />}
          headingLevel="h4"
          title="Sin valoraciones en este período"
          description="Las valoraciones aparecerán aquí cuando tus productos reciban opiniones."
        />
      )}
    </div>
  )
}

RatingDistributionCard.propTypes = {
  items: PropTypes.arrayOf(PropTypes.object).isRequired,
  total: PropTypes.number.isRequired,
}

function RankingBlock({ title, icon, muted = false, items, tipo, emptyTitle, emptyDescription }) {
  const styles = useStyles()
  const [mostrarTodos, setMostrarTodos] = useState(false)
  const visible = mostrarTodos ? items : items.slice(0, 3)

  return (
    <StatsBlock
      title={title}
      icon={icon}
      muted={muted}
      aside={<span className={styles.topTen}>Top 10</span>}
    >
      {items.length > 0 ? (
        <>
          <div className={styles.rankingHeader} aria-hidden="true">
            <span>#</span>
            <span />
            <span>Producto</span>
            <span className={styles.rankingHeadingRight}>{tipo === 'ventas' ? 'Unidades' : 'Valoración'}</span>
          </div>
          <ol className={styles.rankingRows}>
            {visible.map((item, indice) => (
              <li key={item.id} className={styles.rankingRow}>
                <span className={styles.rankNumber}>{indice + 1}</span>
                {item.imagen ? (
                  <img className={styles.productThumb} src={item.imagen} alt="" loading="lazy" />
                ) : (
                  <span className={styles.productFallback} aria-hidden="true">
                    <ShoppingBag24Regular />
                  </span>
                )}
                <div className={styles.rankingName} title={item.nombre}>
                  <Text weight="semibold">{item.nombre}</Text>
                </div>
                <Text size={200} className={styles.rankingMetric}>
                  {tipo === 'ventas'
                    ? item.unidades
                    : `${formatRating(item.media)} (${item.numero})`}
                </Text>
              </li>
            ))}
          </ol>
          {items.length > 3 && (
            <Button
              appearance="subtle"
              aria-expanded={mostrarTodos}
              onClick={() => setMostrarTodos((actual) => !actual)}
            >
              {mostrarTodos ? 'Mostrar menos' : 'Ver ranking completo'}
            </Button>
          )}
        </>
      ) : (
        <EmptyState
          icon={tipo === 'ventas' ? <ShoppingBag24Regular /> : <Star24Filled />}
          headingLevel="h4"
          title={emptyTitle}
          description={emptyDescription}
        />
      )}
    </StatsBlock>
  )
}

RankingBlock.propTypes = {
  title: PropTypes.string.isRequired,
  icon: PropTypes.element.isRequired,
  muted: PropTypes.bool,
  items: PropTypes.arrayOf(PropTypes.object).isRequired,
  tipo: PropTypes.oneOf(['ventas', 'valoraciones']).isRequired,
  emptyTitle: PropTypes.string.isRequired,
  emptyDescription: PropTypes.string.isRequired,
}

function SummaryBlock({ ventas, media, totalValoraciones }) {
  const styles = useStyles()
  const indicadores = [
    { etiqueta: 'Pedidos', valor: ventas.pedidos, icono: <ShoppingBag24Regular /> },
    { etiqueta: 'Unidades vendidas', valor: ventas.unidadesVendidas, icono: <ShoppingBag24Regular /> },
    { etiqueta: 'Ingresos', valor: formatPrice(ventas.ingresos), icono: <ArrowTrending24Regular /> },
    {
      etiqueta: 'Valoración media',
      valor: media === null ? SIN_VALORACIONES : `${formatRating(media)} de 5`,
      icono: <Star24Filled />,
      detalle: `${totalValoraciones} valoraciones`,
    },
  ]

  return (
    <div className={styles.kpiGrid} role="region" aria-label="Indicadores clave">
      {indicadores.map(({ etiqueta, valor, icono, detalle }) => (
        <div key={etiqueta} className={styles.kpiCard}>
          <span className={styles.kpiIcon} aria-hidden="true">{icono}</span>
          <dl className={styles.kpiMeta}>
            <dt className={styles.summaryLabel}>{etiqueta}</dt>
            <dd className={styles.kpiValue}>{valor}</dd>
            {detalle && <dd><Text size={200}>{detalle}</Text></dd>}
          </dl>
        </div>
      ))}
    </div>
  )
}

function RankingTable({ label, rows }) {
  const tableStyles = useTableStyles()

  return (
    <div className={tableStyles.wrapper}>
      <Table aria-label={label}>
        <TableHeader>
          <TableRow>
            <TableHeaderCell>Producto</TableHeaderCell>
            <TableHeaderCell>Stock</TableHeaderCell>
          </TableRow>
        </TableHeader>
        <TableBody>
          {rows.map((producto) => (
            <TableRow key={producto.productoId} className={tableStyles.row}>
              <TableCell className={tableStyles.name}>
                <Text weight="semibold">{producto.nombre}</Text>
              </TableCell>
              <TableCell>{producto.stock === 0 ? 'Sin stock' : `${producto.stock} unidades`}</TableCell>
            </TableRow>
          ))}
        </TableBody>
      </Table>
    </div>
  )
}

RankingTable.propTypes = {
  label: PropTypes.string.isRequired,
  rows: PropTypes.arrayOf(PropTypes.shape({
    productoId: PropTypes.string.isRequired,
    nombre: PropTypes.string.isRequired,
    stock: PropTypes.number.isRequired,
  })).isRequired,
}

function StockBlock({ stock }) {
  const styles = useStyles()
  const indicadores = [
    ['Productos', stock.productos],
    ['Sin stock', stock.sinStock],
    ['Stock bajo', stock.stockBajo],
    ['No disponibles', stock.noDisponibles],
  ]
  const alertas = [
    {
      titulo: 'Productos con stock bajo',
      nombreTabla: 'Productos con stock bajo',
      productos: stock.productosConStockBajo,
      vacio: 'No tienes productos con stock bajo.',
    },
    {
      titulo: 'Productos sin stock',
      nombreTabla: 'Productos sin stock',
      productos: stock.productosSinStock,
      vacio: 'No tienes productos sin stock.',
    },
  ]

  return (
    <StatsBlock title="Stock" icon={<ShoppingBag24Regular />}>
      <div className={styles.stockSummaryGrid}>
        {indicadores.map(([etiqueta, valor]) => (
          <dl key={etiqueta} className={styles.stockSummaryItem}>
            <dt className={styles.summaryLabel}>{etiqueta}</dt>
            <dd className={styles.kpiValue}>{valor}</dd>
          </dl>
        ))}
      </div>
      <Text size={200}>Se considera stock bajo tener entre 1 y 5 unidades.</Text>
      <div className={styles.stockAlerts}>
        {alertas.map((alerta) => (
          <section key={alerta.titulo} className={styles.stockAlert} aria-label={alerta.titulo}>
            <div className={styles.stockAlertHeading}>
              <Subtitle1 as="h4" className={styles.heading}>{alerta.titulo}</Subtitle1>
              <Badge>{alerta.productos.length}</Badge>
            </div>
            {alerta.productos.length > 0 ? (
              <RankingTable label={alerta.nombreTabla} rows={alerta.productos} />
            ) : (
              <Text>{alerta.vacio}</Text>
            )}
          </section>
        ))}
      </div>
    </StatsBlock>
  )
}

StockBlock.propTypes = {
  stock: PropTypes.shape({
    productos: PropTypes.number.isRequired,
    sinStock: PropTypes.number.isRequired,
    stockBajo: PropTypes.number.isRequired,
    noDisponibles: PropTypes.number.isRequired,
    productosConStockBajo: PropTypes.arrayOf(PropTypes.object).isRequired,
    productosSinStock: PropTypes.arrayOf(PropTypes.object).isRequired,
  }).isRequired,
}

SummaryBlock.propTypes = {
  ventas: PropTypes.shape({
    pedidos: PropTypes.number.isRequired,
    unidadesVendidas: PropTypes.number.isRequired,
    ingresos: PropTypes.number.isRequired,
  }).isRequired,
  media: PropTypes.number,
  totalValoraciones: PropTypes.number.isRequired,
}

export default function SellerStatistics() {
  const pageStyles = usePageStyles()
  const styles = useStyles()
  const titleId = useId('estadisticas')
  const fetcher = useCallback(() => getSellerStatistics(), [])
  const { data: estadisticas, failed, retry } = useApiQuery(fetcher)
  const estadisticasActuales = estadisticas?.periodo === 'TODO_EL_PERIODO' ? estadisticas : null

  const masVendidos = normalizeList(
    estadisticasActuales?.ventas?.masVendidos,
  )
  const menosVendidos = normalizeList(
    estadisticasActuales?.ventas?.menosVendidos,
  )
  const mejorValorados = normalizeList(estadisticasActuales?.valoraciones?.mejorValorados)
  const peorValorados = normalizeList(estadisticasActuales?.valoraciones?.peorValorados)
  const mediaGeneral = estadisticasActuales ? resolveAverageRating(estadisticasActuales) : null

  return (
    <main className={styles.page}>
      <div className={pageStyles.titleBlock}>
        <Title1 as="h1" id={titleId} className={pageStyles.title}>Mis estadísticas</Title1>
        <Text className={pageStyles.subtitle}>Analiza el rendimiento de tus productos, ventas y valoraciones.</Text>
      </div>

      <section aria-labelledby={titleId} className={styles.dashboard}>

          {failed && (
            <MessageBar intent="error">
              <MessageBarBody>No se han podido cargar tus estadísticas. Inténtalo de nuevo.</MessageBarBody>
              <MessageBarActions>
                <Button onClick={retry}>Reintentar</Button>
              </MessageBarActions>
            </MessageBar>
          )}

          {!estadisticasActuales && !failed && <Spinner label="Cargando tus estadísticas…" />}

          {estadisticasActuales && (
            <>
              <section role="region" aria-label="Panel de rendimiento">
                <SummaryBlock
                  ventas={estadisticasActuales.ventas}
                  media={mediaGeneral}
                  totalValoraciones={estadisticasActuales.valoraciones.total}
                />
              </section>
              <div className={styles.chartGrid}>
                <SalesTrendChart
                  items={estadisticasActuales.ventas.evolucion}
                />
                <RatingDistributionCard
                  items={estadisticasActuales.valoraciones.distribucion}
                  total={estadisticasActuales.valoraciones.total}
                />
              </div>

              <div className={styles.rankingGrid} role="region" aria-label="Ventas">
                <RankingBlock
                  title="Top 10 más vendidos"
                  icon={<ArrowTrending24Regular />}
                  items={masVendidos}
                  tipo="ventas"
                  emptyTitle="Sin datos de ventas"
                  emptyDescription="Aún no hay productos vendidos."
                />
                <RankingBlock
                  title="Top 10 menos vendidos"
                  icon={<ArrowTrendingDown24Regular />}
                  muted
                  items={menosVendidos}
                  tipo="ventas"
                  emptyTitle="Sin datos de ventas"
                  emptyDescription="Aún no hay productos en tu inventario."
                />
              </div>

              <div className={styles.rankingGrid} role="region" aria-label="Valoraciones">
                <RankingBlock
                  title="Top 10 mejor valorados"
                  icon={<Star24Filled />}
                  items={mejorValorados}
                  tipo="valoraciones"
                  emptyTitle="Sin valoraciones"
                  emptyDescription="Aún no hay productos valorados."
                />
                <RankingBlock
                  title="Top 10 peor valorados"
                  icon={<ThumbDislike24Regular />}
                  muted
                  items={peorValorados}
                  tipo="valoraciones"
                  emptyTitle="Sin valoraciones"
                  emptyDescription="Aún no hay productos valorados."
                />
              </div>
              <StockBlock stock={estadisticasActuales.stock} />
            </>
          )}
      </section>
    </main>
  )
}
