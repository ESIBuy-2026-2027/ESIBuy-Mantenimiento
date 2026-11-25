import { useCallback } from 'react'
import PropTypes from 'prop-types'
import {
  Badge,
  Button,
  Card,
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
  Title3,
  makeStyles,
  tokens,
  useId,
} from '@fluentui/react-components'
import {
  ArrowTrending24Regular,
  ArrowTrendingDown24Regular,
  ChartMultiple24Regular,
  DataPie24Regular,
  ShoppingBag24Regular,
  Star24Filled,
  Star24Regular,
  ThumbDislike24Regular,
  TopSpeed24Regular,
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
const DONUT_PALETTE = [
  '#EA580C', // Naranja intenso
  '#2563EB', // Azul
  '#16A34A', // Verde
  '#9333EA', // Violeta
  '#E11D48', // Rosa / carmesí
  '#0891B2', // Cian / turquesa
  '#EAB308', // Amarillo
  '#4F46E5', // Índigo
  '#0D9488', // Verde azulado
  '#D97706', // Ámbar
]

const useStyles = makeStyles({
  card: {
    gap: tokens.spacingVerticalXL,
    padding: tokens.spacingVerticalXL,
    width: '100%',
  },
  heading: {
    margin: 0,
  },
  sectionHeader: {
    display: 'flex',
    alignItems: 'center',
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
    borderRadius: tokens.borderRadiusXLarge,
    backgroundColor: tokens.colorNeutralBackground2,
  },
  overviewGrid: {
    display: 'grid',
    gridTemplateColumns: 'repeat(auto-fit, minmax(260px, 1fr))',
    gap: tokens.spacingHorizontalL,
  },
  donutCard: {
    display: 'flex',
    alignItems: 'center',
    gap: tokens.spacingHorizontalL,
    padding: tokens.spacingVerticalL,
    border: `1px solid ${tokens.colorNeutralStroke2}`,
    borderRadius: tokens.borderRadiusXLarge,
    backgroundColor: tokens.colorNeutralBackground2,
    flexWrap: 'wrap',
  },
  donutInfo: {
    display: 'flex',
    flexDirection: 'column',
    gap: tokens.spacingVerticalXS,
    minWidth: 0,
    flex: 1,
  },
  starsRow: {
    display: 'flex',
    alignItems: 'center',
    gap: '2px',
    color: '#F59E0B',
  },
  legendList: {
    display: 'flex',
    flexDirection: 'column',
    gap: tokens.spacingVerticalXXS,
  },
  legendItem: {
    display: 'flex',
    alignItems: 'center',
    gap: tokens.spacingHorizontalXS,
  },
  legendDot: {
    width: '10px',
    height: '10px',
    borderRadius: tokens.borderRadiusCircular,
    flexShrink: 0,
  },
  topGrid: {
    display: 'grid',
    gridTemplateColumns: 'repeat(auto-fit, minmax(320px, 1fr))',
    gap: tokens.spacingHorizontalL,
  },
  chartCard: {
    display: 'flex',
    flexDirection: 'column',
    gap: tokens.spacingVerticalM,
    padding: tokens.spacingVerticalL,
    border: `1px solid ${tokens.colorNeutralStroke2}`,
    borderRadius: tokens.borderRadiusXLarge,
    backgroundColor: tokens.colorNeutralBackground2,
  },
  chartTitleRow: {
    display: 'flex',
    alignItems: 'center',
    gap: tokens.spacingHorizontalXS,
    color: tokens.colorNeutralForeground2,
  },
  barRow: {
    display: 'grid',
    gridTemplateColumns: 'minmax(100px, 150px) 1fr auto',
    alignItems: 'center',
    gap: tokens.spacingHorizontalS,
    '@media (max-width: 600px)': {
      gridTemplateColumns: '1fr auto',
      rowGap: tokens.spacingVerticalXXS,
    },
  },
  barLabel: {
    fontWeight: tokens.fontWeightSemibold,
    overflow: 'hidden',
    textOverflow: 'ellipsis',
    whiteSpace: 'nowrap',
    '@media (max-width: 600px)': {
      gridColumn: '1 / -1',
    },
  },
  barTrack: {
    width: '100%',
    height: '12px',
    borderRadius: tokens.borderRadiusCircular,
    backgroundColor: tokens.colorNeutralBackground4,
    overflow: 'hidden',
  },
  barFill: {
    height: '100%',
    borderRadius: tokens.borderRadiusCircular,
    transitionProperty: 'width',
    transitionDuration: '200ms',
  },
  barValue: {
    color: tokens.colorNeutralForeground2,
    whiteSpace: 'nowrap',
  },
  kpiValue: {
    margin: 0,
    color: tokens.colorBrandForeground1,
    fontSize: tokens.fontSizeBase600,
    fontWeight: tokens.fontWeightSemibold,
    overflowWrap: 'anywhere',
  },
  stockAlerts: {
    display: 'grid',
    gridTemplateColumns: 'repeat(auto-fit, minmax(240px, 1fr))',
    gap: tokens.spacingHorizontalM,
  },
  stockAlert: {
    display: 'flex',
    flexDirection: 'column',
    gap: tokens.spacingVerticalM,
    minWidth: 0,
    padding: tokens.spacingVerticalM,
    borderRadius: tokens.borderRadiusMedium,
  },
  lowStockAlert: {
    borderLeft: `4px solid ${tokens.colorPaletteYellowForeground2}`,
    backgroundColor: tokens.colorPaletteYellowBackground2,
  },
  outOfStockAlert: {
    borderLeft: `4px solid ${tokens.colorPaletteRedForeground1}`,
    backgroundColor: tokens.colorPaletteRedBackground1,
  },
  stockAlertHeading: {
    display: 'flex',
    alignItems: 'center',
    justifyContent: 'space-between',
    gap: tokens.spacingHorizontalS,
  },
})

function normalizeList(raw, limit = 10) {
  if (!Array.isArray(raw)) return []
  return raw.slice(0, limit).map((item, idx) => ({
    id: String(item.productoId ?? item.id ?? idx),
    nombre: item.nombre ?? item.productoNombre ?? 'Producto',
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

function AverageRatingCircleCard({ media }) {
  const styles = useStyles()
  const radius = 40
  const circumference = 2 * Math.PI * radius
  const ratio = media !== null ? Math.min(Math.max(media / 5, 0), 1) : 0
  const dashOffset = circumference * (1 - ratio)
  const filledStars = media !== null ? Math.round(media) : 0

  return (
    <div className={styles.donutCard} role="region" aria-label="Valoración media de mis productos">
      <svg width="104" height="104" viewBox="0 0 104 104" aria-hidden="true">
        <circle
          cx="52"
          cy="52"
          r={radius}
          fill="none"
          stroke={tokens.colorNeutralBackground4}
          strokeWidth="11"
        />
        <circle
          cx="52"
          cy="52"
          r={radius}
          fill="none"
          stroke={ACCENT_ORANGE}
          strokeWidth="11"
          strokeLinecap="round"
          strokeDasharray={circumference}
          strokeDashoffset={dashOffset}
          transform="rotate(-90 52 52)"
        />
        <text
          x="52"
          y="50"
          textAnchor="middle"
          fontSize="17"
          fontWeight="700"
          fill={tokens.colorNeutralForeground1}
        >
          {media === null ? '—' : formatRating(media)}
        </text>
        <text
          x="52"
          y="67"
          textAnchor="middle"
          fontSize="11"
          fill={tokens.colorNeutralForeground3}
        >
          de 5,0
        </text>
      </svg>

      <div className={styles.donutInfo}>
        <div className={styles.chartTitleRow}>
          <Star24Filled style={{ color: ACCENT_ORANGE }} />
          <Text weight="semibold">Valoración media de mis productos</Text>
        </div>
        <Text size={500} weight="bold">
          {media === null ? SIN_VALORACIONES : `${formatRating(media)} de 5`}
        </Text>
        <div className={styles.starsRow} aria-hidden="true">
          {[1, 2, 3, 4, 5].map((star) =>
            star <= filledStars ? <Star24Filled key={star} /> : <Star24Regular key={star} />,
          )}
        </div>
      </div>
    </div>
  )
}

AverageRatingCircleCard.propTypes = {
  media: PropTypes.number,
}

function SalesShareDonutCard({ items }) {
  const styles = useStyles()
  const totalUnidades = items.reduce((acc, item) => acc + item.unidades, 0)
  const radius = 40
  const circumference = 2 * Math.PI * radius
  const topSlice = items.slice(0, 5)

  let accumulated = 0

  return (
    <div className={styles.donutCard} role="region" aria-label="Distribución circular de ventas">
      <svg width="104" height="104" viewBox="0 0 104 104" aria-hidden="true">
        <circle
          cx="52"
          cy="52"
          r={radius}
          fill="none"
          stroke={tokens.colorNeutralBackground4}
          strokeWidth="12"
        />
        {totalUnidades > 0 &&
          topSlice.map((item, idx) => {
            const fraction = item.unidades / totalUnidades
            const dashLength = fraction * circumference
            const dashOffset = -accumulated * circumference
            accumulated += fraction
            return (
              <circle
                key={item.id}
                cx="52"
                cy="52"
                r={radius}
                fill="none"
                stroke={DONUT_PALETTE[idx % DONUT_PALETTE.length]}
                strokeWidth="12"
                strokeDasharray={`${dashLength} ${circumference}`}
                strokeDashoffset={dashOffset}
                transform="rotate(-90 52 52)"
              />
            )
          })}
        <text
          x="52"
          y="50"
          textAnchor="middle"
          fontSize="16"
          fontWeight="700"
          fill={tokens.colorNeutralForeground1}
        >
          {totalUnidades}
        </text>
        <text
          x="52"
          y="67"
          textAnchor="middle"
          fontSize="11"
          fill={tokens.colorNeutralForeground3}
        >
          ud. top
        </text>
      </svg>

      <div className={styles.donutInfo}>
        <div className={styles.chartTitleRow}>
          <DataPie24Regular style={{ color: ACCENT_ORANGE }} />
          <Text weight="semibold">Reparto de unidades (Top más vendidos)</Text>
        </div>
        {topSlice.length > 0 ? (
          <div className={styles.legendList}>
            {topSlice.map((item, idx) => {
              const pct = totalUnidades > 0 ? Math.round((item.unidades / totalUnidades) * 100) : 0
              return (
                <div key={item.id} className={styles.legendItem}>
                  <span
                    className={styles.legendDot}
                    style={{ backgroundColor: DONUT_PALETTE[idx % DONUT_PALETTE.length] }}
                  />
                  <Text size={200}>
                    {item.nombre}: <strong>{pct}%</strong> ({item.unidades} ud.)
                  </Text>
                </div>
              )
            })}
          </div>
        ) : (
          <Text size={200}>Aún no hay ventas registradas.</Text>
        )}
      </div>
    </div>
  )
}

SalesShareDonutCard.propTypes = {
  items: PropTypes.arrayOf(PropTypes.object).isRequired,
}

function StatsBlock({ title, icon, muted = false, children }) {
  const styles = useStyles()
  const titleId = useId('bloque')

  return (
    <section aria-labelledby={titleId} className={styles.block}>
      <div className={styles.sectionHeader}>
        <span className={muted ? styles.iconBadgeMuted : styles.iconBadge} aria-hidden="true">
          {icon}
        </span>
        <Subtitle1 as="h3" id={titleId} className={styles.heading}>
          {title}
        </Subtitle1>
      </div>
      {children}
    </section>
  )
}

StatsBlock.propTypes = {
  title: PropTypes.string.isRequired,
  icon: PropTypes.element.isRequired,
  muted: PropTypes.bool,
  children: PropTypes.node.isRequired,
}

function BarChartSection({ title, items, getMetric, formatMetric, color }) {
  const styles = useStyles()
  const maxVal = Math.max(...items.map(getMetric), 1)

  return (
    <div className={styles.chartCard} role="region" aria-label={`Gráfico de barras: ${title}`}>
      <div className={styles.chartTitleRow}>
        <ChartMultiple24Regular style={{ color }} />
        <Text weight="semibold" className={styles.heading}>
          {title}
        </Text>
      </div>
      {items.map((item) => {
        const val = getMetric(item)
        const pct = Math.max(4, Math.round((val / maxVal) * 100))

        return (
          <div key={item.id} className={styles.barRow}>
            <Text className={styles.barLabel} title={item.nombre}>
              {item.nombre}
            </Text>
            <div className={styles.barTrack} aria-hidden="true">
              <div
                className={styles.barFill}
                style={{ width: `${pct}%`, backgroundColor: color }}
              />
            </div>
            <Text size={200} className={styles.barValue}>
              {formatMetric(item)}
            </Text>
          </div>
        )
      })}
    </div>
  )
}

BarChartSection.propTypes = {
  title: PropTypes.string.isRequired,
  items: PropTypes.arrayOf(PropTypes.object).isRequired,
  getMetric: PropTypes.func.isRequired,
  formatMetric: PropTypes.func.isRequired,
  color: PropTypes.string.isRequired,
}

function RankingTable({ label, columns, rows }) {
  const tableStyles = useTableStyles()

  return (
    <div className={tableStyles.wrapper}>
      <Table aria-label={label}>
        <TableHeader>
          <TableRow>
            {columns.map((col) => (
              <TableHeaderCell key={col}>{col}</TableHeaderCell>
            ))}
          </TableRow>
        </TableHeader>
        <TableBody>
          {rows.map(({ id, cells: [nombre, ...resto] }) => (
            <TableRow key={id} className={tableStyles.row}>
              <TableCell className={tableStyles.name}>
                <Text weight="semibold">{nombre}</Text>
              </TableCell>
              {resto.map((valor, idx) => (
                <TableCell key={columns[idx + 1]}>{valor}</TableCell>
              ))}
            </TableRow>
          ))}
        </TableBody>
      </Table>
    </div>
  )
}

RankingTable.propTypes = {
  label: PropTypes.string.isRequired,
  columns: PropTypes.arrayOf(PropTypes.string).isRequired,
  rows: PropTypes.arrayOf(
    PropTypes.shape({
      id: PropTypes.string.isRequired,
      cells: PropTypes.arrayOf(PropTypes.node).isRequired,
    }),
  ).isRequired,
}

function SalesRankingBlock({ title, icon, muted = false, items, emptyTitle, emptyDescription, color }) {
  return (
    <StatsBlock title={title} icon={icon} muted={muted}>
      {items.length > 0 ? (
        <>
          <BarChartSection
            title={`Barras — ${title}`}
            items={items}
            getMetric={(item) => item.unidades}
            formatMetric={(item) => `${item.unidades} ud. · ${formatPrice(item.ingresos)}`}
            color={color}
          />
          <RankingTable
            label={title}
            columns={['Producto', 'Unidades', 'Ingresos']}
            rows={items.map(({ id, nombre, unidades, ingresos }) => ({
              id,
              cells: [nombre, unidades, formatPrice(ingresos)],
            }))}
          />
        </>
      ) : (
        <EmptyState
          icon={<ShoppingBag24Regular />}
          headingLevel="h4"
          title={emptyTitle}
          description={emptyDescription}
        />
      )}
    </StatsBlock>
  )
}

SalesRankingBlock.propTypes = {
  title: PropTypes.string.isRequired,
  icon: PropTypes.element.isRequired,
  muted: PropTypes.bool,
  items: PropTypes.arrayOf(PropTypes.object).isRequired,
  emptyTitle: PropTypes.string.isRequired,
  emptyDescription: PropTypes.string.isRequired,
  color: PropTypes.string.isRequired,
}

function RatingRankingBlock({ title, icon, muted = false, items, emptyTitle, emptyDescription, color }) {
  return (
    <StatsBlock title={title} icon={icon} muted={muted}>
      {items.length > 0 ? (
        <>
          <BarChartSection
            title={`Barras — ${title}`}
            items={items}
            getMetric={(item) => (item.media ?? 0) * 20}
            formatMetric={(item) =>
              item.media === null ? SIN_VALORACIONES : `${formatRating(item.media)} / 5 (${item.numero})`
            }
            color={color}
          />
          <RankingTable
            label={title}
            columns={['Producto', 'Media', 'Valoraciones']}
            rows={items.map(({ id, nombre, media, numero }) => ({
              id,
              cells: [nombre, media === null ? SIN_VALORACIONES : formatRating(media), numero],
            }))}
          />
        </>
      ) : (
        <EmptyState
          icon={<Star24Regular />}
          headingLevel="h4"
          title={emptyTitle}
          description={emptyDescription}
        />
      )}
    </StatsBlock>
  )
}

RatingRankingBlock.propTypes = {
  title: PropTypes.string.isRequired,
  icon: PropTypes.element.isRequired,
  muted: PropTypes.bool,
  items: PropTypes.arrayOf(PropTypes.object).isRequired,
  emptyTitle: PropTypes.string.isRequired,
  emptyDescription: PropTypes.string.isRequired,
  color: PropTypes.string.isRequired,
}

export default function SellerStatistics() {
  const pageStyles = usePageStyles()
  const styles = useStyles()
  const titleId = useId('estadisticas')
  const fetcher = useCallback(() => getSellerStatistics(), [])
  const { data: estadisticas, failed, retry } = useApiQuery(fetcher)

  const masVendidos = normalizeList(
    estadisticas?.topMasVendidos ?? estadisticas?.masVendidos ?? estadisticas?.ventas?.masVendidos,
  )
  const menosVendidos = normalizeList(
    estadisticas?.topMenosVendidos ?? estadisticas?.menosVendidos ?? estadisticas?.ventas?.menosVendidos,
  )
  const mejorValorados = normalizeList(
    estadisticas?.topMejorValorados ??
      estadisticas?.mejorValorados ??
      estadisticas?.valoraciones?.mejorValorados ??
      estadisticas?.valoraciones?.porProducto,
  )
  const peorValorados = normalizeList(
    estadisticas?.topPeorValorados ??
      estadisticas?.peorValorados ??
      estadisticas?.valoraciones?.peorValorados,
  )
  const mediaGeneral = estadisticas ? resolveAverageRating(estadisticas) : null

  return (
    <main className={pageStyles.page}>
      <div className={pageStyles.titleBlock}>
        <Title1 as="h1" className={pageStyles.title}>
          Mis estadísticas
        </Title1>
        <Text className={pageStyles.subtitle}>
          Consulta tus rankings de ventas y valoraciones y la valoración media de tus productos.
        </Text>
      </div>

      <section aria-labelledby={titleId}>
        <Card className={styles.card}>
          <div className={styles.sectionHeader}>
            <span className={styles.iconBadge} aria-hidden="true">
              <TopSpeed24Regular />
            </span>
            <Title3 as="h2" id={titleId} className={styles.heading}>
              Indicadores clave
            </Title3>
          </div>

          {failed && (
            <MessageBar intent="error">
              <MessageBarBody>No se han podido cargar tus estadísticas. Inténtalo de nuevo.</MessageBarBody>
              <MessageBarActions>
                <Button onClick={retry}>Reintentar</Button>
              </MessageBarActions>
            </MessageBar>
          )}

          {!estadisticas && !failed && <Spinner label="Cargando tus estadísticas…" />}

          {estadisticas && (
            <>
              <div className={styles.overviewGrid}>
                <AverageRatingCircleCard media={mediaGeneral} />
                <SalesShareDonutCard items={masVendidos} />
              </div>

              <div className={styles.topGrid}>
                <SalesRankingBlock
                  title="Top 10 más ventas"
                  icon={<ArrowTrending24Regular />}
                  items={masVendidos}
                  emptyTitle="Sin datos de ventas"
                  emptyDescription="Aún no hay productos vendidos."
                  color={ACCENT_ORANGE}
                />
                <SalesRankingBlock
                  title="Top 10 menos vendidos"
                  icon={<ArrowTrendingDown24Regular />}
                  muted
                  items={menosVendidos}
                  emptyTitle="Sin datos de ventas"
                  emptyDescription="Aún no hay productos con ventas registradas."
                  color={ACCENT_AMBER}
                />
              </div>

              <div className={styles.topGrid}>
                <RatingRankingBlock
                  title="Top 10 mejor valorados"
                  icon={<Star24Filled />}
                  items={mejorValorados}
                  emptyTitle="Sin valoraciones"
                  emptyDescription="Aún no hay productos valorados."
                  color={ACCENT_ORANGE}
                />
                <RatingRankingBlock
                  title="Top 10 peor valorados"
                  icon={<ThumbDislike24Regular />}
                  muted
                  items={peorValorados}
                  emptyTitle="Sin valoraciones"
                  emptyDescription="Aún no hay productos valorados."
                  color={ACCENT_AMBER}
                />
              </div>
            </>
          )}
        </Card>
      </section>
    </main>
  )
}