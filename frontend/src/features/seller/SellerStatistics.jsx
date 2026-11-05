import { useCallback } from 'react'
import PropTypes from 'prop-types'
import {
  Card,
  Subtitle1,
  Table,
  TableBody,
  TableCell,
  TableHeader,
  TableHeaderCell,
  TableRow,
  Text,
  Title2,
  Title3,
  makeStyles,
  tokens,
  useId,
} from '@fluentui/react-components'
import { getSellerStatistics } from '../../api/statistics'
import { useTableStyles } from '../../components/tableStyles'
import { useApiQuery } from '../../hooks/useApiQuery'
import { formatPrice } from '../../utils/format'

const useStyles = makeStyles({
  card: {
    gap: tokens.spacingVerticalXL,
    padding: tokens.spacingVerticalXL,
  },
  heading: {
    margin: 0,
  },
  block: {
    display: 'flex',
    flexDirection: 'column',
    gap: tokens.spacingVerticalM,
  },
  stats: {
    display: 'grid',
    gridTemplateColumns: 'repeat(auto-fit, minmax(160px, 1fr))',
    gap: tokens.spacingHorizontalM,
  },
  stat: {
    display: 'flex',
    flexDirection: 'column',
    gap: tokens.spacingVerticalXXS,
    padding: tokens.spacingVerticalM,
    border: `1px solid ${tokens.colorNeutralStroke2}`,
    borderRadius: tokens.borderRadiusXLarge,
  },
  statLabel: {
    color: tokens.colorNeutralForeground3,
  },
})

// Una cifra con su nombre: el grupo toma el nombre de la etiqueta para que se lea como «Pedidos: 6».
function Stat({ label, value }) {
  const styles = useStyles()
  const labelId = useId('cifra')

  return (
    <div role="group" aria-labelledby={labelId} className={styles.stat}>
      <Text id={labelId} className={styles.statLabel}>
        {label}
      </Text>
      <Title2 as="p" className={styles.heading}>
        {value}
      </Title2>
    </div>
  )
}

Stat.propTypes = {
  label: PropTypes.string.isRequired,
  value: PropTypes.node.isRequired,
}

function StatsBlock({ title, children }) {
  const styles = useStyles()
  const titleId = useId('bloque')

  return (
    <section aria-labelledby={titleId} className={styles.block}>
      <Subtitle1 as="h3" id={titleId} className={styles.heading}>
        {title}
      </Subtitle1>
      {children}
    </section>
  )
}

StatsBlock.propTypes = {
  title: PropTypes.string.isRequired,
  children: PropTypes.node.isRequired,
}

function SalesBlock({ ventas }) {
  const styles = useStyles()
  const tableStyles = useTableStyles()

  return (
    <StatsBlock title="Ventas">
      <div className={styles.stats}>
        <Stat label="Pedidos" value={ventas.pedidos} />
        <Stat label="Unidades vendidas" value={ventas.unidadesVendidas} />
        <Stat label="Ingresos" value={formatPrice(ventas.ingresos)} />
      </div>
      <div className={tableStyles.wrapper}>
        <Table aria-label="Los más vendidos">
          <TableHeader>
            <TableRow>
              <TableHeaderCell>Producto</TableHeaderCell>
              <TableHeaderCell>Unidades</TableHeaderCell>
              <TableHeaderCell>Ingresos</TableHeaderCell>
            </TableRow>
          </TableHeader>
          <TableBody>
            {ventas.masVendidos.map((producto) => (
              <TableRow key={producto.productoId} className={tableStyles.row}>
                <TableCell className={tableStyles.name}>
                  <Text weight="semibold">{producto.nombre}</Text>
                </TableCell>
                <TableCell>{producto.unidades}</TableCell>
                <TableCell>{formatPrice(producto.ingresos)}</TableCell>
              </TableRow>
            ))}
          </TableBody>
        </Table>
      </div>
    </StatsBlock>
  )
}

SalesBlock.propTypes = {
  ventas: PropTypes.shape({
    pedidos: PropTypes.number.isRequired,
    unidadesVendidas: PropTypes.number.isRequired,
    ingresos: PropTypes.number.isRequired,
    masVendidos: PropTypes.arrayOf(
      PropTypes.shape({
        productoId: PropTypes.string.isRequired,
        nombre: PropTypes.string.isRequired,
        unidades: PropTypes.number.isRequired,
        ingresos: PropTypes.number.isRequired,
      }),
    ).isRequired,
  }).isRequired,
}

// Estadísticas básicas del vendedor en sesión (HU17), como sección de «Mi perfil». El backend las calcula desde
// siempre y sin los pedidos cancelados.
export default function SellerStatistics() {
  const styles = useStyles()
  const titleId = useId('estadisticas')
  const fetcher = useCallback(() => getSellerStatistics(), [])
  const { data: estadisticas } = useApiQuery(fetcher)

  return (
    <section aria-labelledby={titleId}>
      <Card className={styles.card}>
        <Title3 as="h2" id={titleId} className={styles.heading}>
          Estadísticas de ventas
        </Title3>
        {estadisticas && <SalesBlock ventas={estadisticas.ventas} />}
      </Card>
    </section>
  )
}
