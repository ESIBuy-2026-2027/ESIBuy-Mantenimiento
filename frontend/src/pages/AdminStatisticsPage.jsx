import { useCallback } from 'react'
import {
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
  ArrowLeft20Regular,
  ArrowTrending24Regular,
  ChartMultiple24Regular,
  DataPie24Regular,
  ShoppingBag24Regular,
  Star24Filled,
  Star24Regular,
  TopSpeed24Regular,
} from '@fluentui/react-icons'
import { getAdminStatistics } from '../api/statistics'
import EmptyState from '../components/EmptyState'
import LinkButton from '../components/LinkButton'
import { usePageStyles } from '../components/pageStyles'
import { useTableStyles } from '../components/tableStyles'
import { useApiQuery } from '../hooks/useApiQuery'
import { formatPrice } from '../utils/format'
import { PATHS } from '../routes/paths'

const ACCENT_ORANGE = '#EA580C'
const ACCENT_AMBER = '#D97706'
const DONUT_PALETTE = ['#EA580C', '#2563EB', '#16A34A', '#9333EA', '#E11D48']

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
    minWidth: 0,
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
    minWidth: 0,
  },
  donutInfo: {
    display: 'flex',
    flexDirection: 'column',
    gap: tokens.spacingVerticalXS,
    minWidth: 0,
    flex: 1,
  },
  chartTitleRow: {
    display: 'flex',
    alignItems: 'center',
    gap: tokens.spacingHorizontalXS,
    color: tokens.colorNeutralForeground2,
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
    minWidth: 0,
  },
  legendDot: {
    width: '10px',
    height: '10px',
    borderRadius: tokens.borderRadiusCircular,
    flexShrink: 0,
  },
  starsRow: {
    display: 'flex',
    alignItems: 'center',
    gap: '2px',
    color: '#F59E0B',
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
    minWidth: 0,
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
})

const RANKINGS = [
  {
    key: 'productosMasVendidos',
    title: 'Top 10 productos más vendidos',
    icon: <ArrowTrending24Regular />,
    color: ACCENT_ORANGE,
    columns: ['Producto', 'Unidades', 'Ingresos'],
    emptyTitle: 'Sin datos de ventas',
    emptyDescription: 'Aún no hay productos vendidos.',
    metric: (item) => item.unidades,
    metricLabel: (item) => `${item.unidades} unidades · ${formatPrice(item.ingresos)}`,
    cells: (item) => [item.unidades, formatPrice(item.ingresos)],
    getName: (item) => item.nombre,
    getId: (item) => item.productoId,
    emptyIcon: <ShoppingBag24Regular />,
  },
  {
    key: 'productosMejorValorados',
    title: 'Top 10 productos mejor valorados',
    icon: <Star24Filled />,
    color: ACCENT_ORANGE,
    columns: ['Producto', 'Media', 'Valoraciones'],
    emptyTitle: 'Sin valoraciones',
    emptyDescription: 'Aún no hay productos valorados.',
    metric: (item) => item.media * 20,
    metricLabel: (item) => `${item.media.toFixed(1)} / 5 (${item.numeroValoraciones})`,
    cells: (item) => [`${item.media.toFixed(1)} / 5`, item.numeroValoraciones],
    getName: (item) => item.nombre,
    getId: (item) => item.productoId,
    emptyIcon: <Star24Regular />,
  },
  {
    key: 'vendedoresMejorValorados',
    title: 'Top 10 vendedores mejor valorados',
    icon: <Star24Filled />,
    color: ACCENT_AMBER,
    muted: true,
    columns: ['Vendedor', 'Media', 'Valoraciones'],
    emptyTitle: 'Sin valoraciones',
    emptyDescription: 'Aún no hay vendedores con productos valorados.',
    metric: (item) => item.media * 20,
    metricLabel: (item) => `${item.media.toFixed(1)} / 5 (${item.numeroValoraciones})`,
    cells: (item) => [`${item.media.toFixed(1)} / 5`, item.numeroValoraciones],
    getName: (item) => item.nombreComercial,
    getId: (item) => item.vendedorId,
    emptyIcon: <Star24Regular />,
  },
]

function RatingHighlightCard({ title, name, media, number, muted = false }) {
  const styles = useStyles()
  const radius = 40
  const circumference = 2 * Math.PI * radius
  const ratio = media === null ? 0 : Math.min(Math.max(media / 5, 0), 1)
  const filledStars = media === null ? 0 : Math.round(media)

  return (
    <div className={styles.donutCard} role="region" aria-label={title}>
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
          stroke={muted ? ACCENT_AMBER : ACCENT_ORANGE}
          strokeWidth="11"
          strokeLinecap="round"
          strokeDasharray={circumference}
          strokeDashoffset={circumference * (1 - ratio)}
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
          {media === null ? '—' : media.toFixed(1)}
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
          <Star24Filled style={{ color: muted ? ACCENT_AMBER : ACCENT_ORANGE }} />
          <Text weight="semibold">{title}</Text>
        </div>
        <Text weight="semibold" className={styles.heading}>
          {name ?? 'Sin valoraciones'}
        </Text>
        <Text size={200}>
          {media === null ? 'Aún no hay valoraciones.' : `${number} valoraciones`}
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

function SalesShareDonutCard({ items }) {
  const styles = useStyles()
  const totalUnits = items.reduce((total, item) => total + item.unidades, 0)
  const topItems = items.slice(0, 5)
  const radius = 40
  const circumference = 2 * Math.PI * radius
  let accumulated = 0

  return (
    <div className={styles.donutCard} role="region" aria-label="Reparto de unidades del Top 10">
      <svg width="104" height="104" viewBox="0 0 104 104" aria-hidden="true">
        <circle
          cx="52"
          cy="52"
          r={radius}
          fill="none"
          stroke={tokens.colorNeutralBackground4}
          strokeWidth="12"
        />
        {totalUnits > 0 &&
          topItems.map((item, index) => {
            const fraction = item.unidades / totalUnits
            const dashLength = fraction * circumference
            const dashOffset = -accumulated * circumference
            accumulated += fraction
            return (
              <circle
                key={item.productoId}
                cx="52"
                cy="52"
                r={radius}
                fill="none"
                stroke={DONUT_PALETTE[index % DONUT_PALETTE.length]}
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
          {totalUnits}
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
          <Text weight="semibold">Reparto de unidades (Top 10)</Text>
        </div>
        {topItems.length > 0 ? (
          <div className={styles.legendList}>
            {topItems.map((item, index) => {
              const percentage = totalUnits > 0 ? Math.round((item.unidades / totalUnits) * 100) : 0
              return (
                <div key={item.productoId} className={styles.legendItem}>
                  <span
                    className={styles.legendDot}
                    style={{ backgroundColor: DONUT_PALETTE[index % DONUT_PALETTE.length] }}
                  />
                  <Text size={200}>
                    {item.nombre}: <strong>{percentage}%</strong> ({item.unidades} ud.)
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

function RankingBlock({ definition, items }) {
  const styles = useStyles()
  const tableStyles = useTableStyles()
  const titleId = useId('ranking')
  const maxValue = Math.max(...items.map(definition.metric), 1)

  return (
    <section aria-labelledby={titleId} className={styles.block}>
      <div className={styles.sectionHeader}>
        <span className={definition.muted ? styles.iconBadgeMuted : styles.iconBadge} aria-hidden="true">
          {definition.icon}
        </span>
        <Subtitle1 as="h3" id={titleId} className={styles.heading}>
          {definition.title}
        </Subtitle1>
      </div>
      {items.length > 0 ? (
        <>
          <div className={styles.chartCard} role="region" aria-label={`Gráfico de barras: ${definition.title}`}>
            <div className={styles.chartTitleRow}>
              <ChartMultiple24Regular style={{ color: definition.color }} />
              <Text weight="semibold">Barras — {definition.title}</Text>
            </div>
            {items.map((item) => {
              const value = definition.metric(item)
              const percentage = Math.max(4, Math.round((value / maxValue) * 100))
              return (
                <div key={definition.getId(item)} className={styles.barRow}>
                  <Text className={styles.barLabel} title={definition.getName(item)}>
                    {definition.getName(item)}
                  </Text>
                  <div className={styles.barTrack} aria-hidden="true">
                    <div
                      className={styles.barFill}
                      style={{ width: `${percentage}%`, backgroundColor: definition.color }}
                    />
                  </div>
                  <Text size={200} className={styles.barValue}>
                    {definition.metricLabel(item)}
                  </Text>
                </div>
              )
            })}
          </div>
          <div className={tableStyles.wrapper}>
            <Table aria-label={definition.title}>
              <TableHeader>
                <TableRow>
                  {definition.columns.map((column) => (
                    <TableHeaderCell key={column}>{column}</TableHeaderCell>
                  ))}
                </TableRow>
              </TableHeader>
              <TableBody>
                {items.map((item) => (
                  <TableRow key={definition.getId(item)} className={tableStyles.row}>
                    <TableCell className={tableStyles.name}>
                      <Text weight="semibold">{definition.getName(item)}</Text>
                    </TableCell>
                    {definition.cells(item).map((cell, index) => (
                      <TableCell key={definition.columns[index + 1]}>{cell}</TableCell>
                    ))}
                  </TableRow>
                ))}
              </TableBody>
            </Table>
          </div>
        </>
      ) : (
        <EmptyState
          icon={definition.emptyIcon}
          headingLevel="h4"
          title={definition.emptyTitle}
          description={definition.emptyDescription}
        />
      )}
    </section>
  )
}

export default function AdminStatisticsPage() {
  const pageStyles = usePageStyles()
  const styles = useStyles()
  const fetcher = useCallback(() => getAdminStatistics(), [])
  const { data, failed, retry } = useApiQuery(fetcher)
  const topProduct = data?.productosMejorValorados[0]
  const topSeller = data?.vendedoresMejorValorados[0]

  return (
    <main className={pageStyles.page}>
      <div className={pageStyles.heading}>
        <div className={pageStyles.titleBlock}>
          <Title1 as="h1" className={pageStyles.title}>
            Estadísticas de la plataforma
          </Title1>
          <Text className={pageStyles.subtitle}>Rankings globales de ventas y valoraciones.</Text>
        </div>
        <LinkButton to={PATHS.ADMIN} appearance="subtle" icon={<ArrowLeft20Regular />}>
          Volver al panel
        </LinkButton>
      </div>

      <section aria-label="Rankings de estadísticas de la plataforma">
        <Card className={styles.card}>
          <div className={styles.sectionHeader}>
            <span className={styles.iconBadge} aria-hidden="true">
              <TopSpeed24Regular />
            </span>
            <Title3 as="h2" className={styles.heading}>
              Indicadores clave
            </Title3>
          </div>

          {failed && (
            <MessageBar intent="error">
              <MessageBarBody>No se han podido cargar las estadísticas. Inténtalo de nuevo.</MessageBarBody>
              <MessageBarActions>
                <Button onClick={retry}>Reintentar</Button>
              </MessageBarActions>
            </MessageBar>
          )}

          {!data && !failed && <Spinner label="Cargando estadísticas…" />}

          {data && (
            <>
              <div className={styles.overviewGrid}>
                <SalesShareDonutCard items={data.productosMasVendidos} />
                <RatingHighlightCard
                  title="Producto mejor valorado"
                  name={topProduct?.nombre ?? null}
                  media={topProduct?.media ?? null}
                  number={topProduct?.numeroValoraciones ?? 0}
                />
                <RatingHighlightCard
                  title="Vendedor mejor valorado"
                  name={topSeller?.nombreComercial ?? null}
                  media={topSeller?.media ?? null}
                  number={topSeller?.numeroValoraciones ?? 0}
                  muted
                />
              </div>
              <div className={styles.topGrid}>
                {RANKINGS.map((definition) => (
                  <RankingBlock
                    key={definition.key}
                    definition={definition}
                    items={data[definition.key]}
                  />
                ))}
              </div>
            </>
          )}
        </Card>
      </section>
    </main>
  )
}
