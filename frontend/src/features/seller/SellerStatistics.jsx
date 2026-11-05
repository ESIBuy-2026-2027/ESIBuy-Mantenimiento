import { useCallback } from 'react'
import PropTypes from 'prop-types'
import {
  Button,
  Caption1,
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
  Title2,
  Title3,
  makeStyles,
  tokens,
  useId,
} from '@fluentui/react-components'
import { ShoppingBag24Regular, Star24Regular } from '@fluentui/react-icons'
import { getSellerStatistics } from '../../api/statistics'
import EmptyState from '../../components/EmptyState'
import { useTableStyles } from '../../components/tableStyles'
import { useApiQuery } from '../../hooks/useApiQuery'
import { formatPrice, formatRating } from '../../utils/format'

// Sin valoraciones el backend no manda media (`null`): no se muestra como un 0.
const SIN_VALORACIONES = 'Sin valoraciones'

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

// Una cifra con su nombre: el grupo toma el nombre de la etiqueta para que se lea como «Pedidos: 6». `hint` aclara
// qué se cuenta, debajo de la cifra.
function Stat({ label, value, hint }) {
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
      {hint && (
        <Caption1 className={styles.statLabel}>
          {hint}
        </Caption1>
      )}
    </div>
  )
}

Stat.propTypes = {
  label: PropTypes.string.isRequired,
  value: PropTypes.node.isRequired,
  hint: PropTypes.string,
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

// Tabla de un bloque: la primera columna es el nombre del producto, en seminegrita.
function StatsTable({ label, columns, rows }) {
  const tableStyles = useTableStyles()

  return (
    <div className={tableStyles.wrapper}>
      <Table aria-label={label}>
        <TableHeader>
          <TableRow>
            {columns.map((column) => (
              <TableHeaderCell key={column}>{column}</TableHeaderCell>
            ))}
          </TableRow>
        </TableHeader>
        <TableBody>
          {rows.map(({ id, cells: [nombre, ...resto] }) => (
            <TableRow key={id} className={tableStyles.row}>
              <TableCell className={tableStyles.name}>
                <Text weight="semibold">{nombre}</Text>
              </TableCell>
              {resto.map((valor, indice) => (
                // Cada celda se identifica por el nombre de su columna.
                <TableCell key={columns[indice + 1]}>{valor}</TableCell>
              ))}
            </TableRow>
          ))}
        </TableBody>
      </Table>
    </div>
  )
}

StatsTable.propTypes = {
  label: PropTypes.string.isRequired,
  columns: PropTypes.arrayOf(PropTypes.string).isRequired,
  rows: PropTypes.arrayOf(
    PropTypes.shape({
      id: PropTypes.string.isRequired,
      cells: PropTypes.arrayOf(PropTypes.node).isRequired,
    }),
  ).isRequired,
}

function SalesBlock({ ventas }) {
  const styles = useStyles()

  return (
    <StatsBlock title="Ventas">
      <div className={styles.stats}>
        <Stat label="Pedidos" value={ventas.pedidos} />
        <Stat label="Unidades vendidas" value={ventas.unidadesVendidas} />
        <Stat label="Ingresos" value={formatPrice(ventas.ingresos)} />
      </div>
      {ventas.masVendidos.length > 0 ? (
        <StatsTable
          label="Los más vendidos"
          columns={['Producto', 'Unidades', 'Ingresos']}
          rows={ventas.masVendidos.map(({ productoId, nombre, unidades, ingresos }) => ({
            id: productoId,
            cells: [nombre, unidades, formatPrice(ingresos)],
          }))}
        />
      ) : (
        <EmptyState
          icon={<ShoppingBag24Regular />}
          headingLevel="h4"
          title="Aún no tienes ventas"
          description="Cuando un cliente compre alguno de tus productos, aquí verás los más vendidos."
        />
      )}
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

function RatingsBlock({ valoraciones }) {
  const styles = useStyles()

  return (
    <StatsBlock title="Valoraciones">
      <div className={styles.stats}>
        <Stat
          label="Valoración media"
          value={valoraciones.media === null ? SIN_VALORACIONES : `${formatRating(valoraciones.media)} de 5`}
        />
        <Stat label="Valoraciones recibidas" value={valoraciones.total} />
      </div>
      {valoraciones.porProducto.length > 0 ? (
        <StatsTable
          label="Valoración por producto"
          columns={['Producto', 'Media', 'Valoraciones']}
          rows={valoraciones.porProducto.map(({ productoId, nombre, media, numero }) => ({
            id: productoId,
            cells: [nombre, formatRating(media), numero],
          }))}
        />
      ) : (
        <EmptyState
          icon={<Star24Regular />}
          headingLevel="h4"
          title="Aún no tienes valoraciones"
          description="Cuando un cliente valore alguno de tus productos, aquí verás su media."
        />
      )}
    </StatsBlock>
  )
}

RatingsBlock.propTypes = {
  valoraciones: PropTypes.shape({
    media: PropTypes.number,
    total: PropTypes.number.isRequired,
    porProducto: PropTypes.arrayOf(
      PropTypes.shape({
        productoId: PropTypes.string.isRequired,
        nombre: PropTypes.string.isRequired,
        media: PropTypes.number.isRequired,
        numero: PropTypes.number.isRequired,
      }),
    ).isRequired,
  }).isRequired,
}

function StockBlock({ stock }) {
  const styles = useStyles()

  return (
    <StatsBlock title="Stock">
      <div className={styles.stats}>
        <Stat label="Productos" value={stock.productos} />
        <Stat label="Sin stock" value={stock.sinStock} />
        <Stat label="Stock bajo" value={stock.stockBajo} hint="De 1 a 5 unidades" />
        <Stat label="No disponibles" value={stock.noDisponibles} />
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
  }).isRequired,
}

// Estadísticas básicas del vendedor en sesión (HU17), como sección de «Mi perfil». El backend las calcula desde
// siempre y sin los pedidos cancelados.
export default function SellerStatistics() {
  const styles = useStyles()
  const titleId = useId('estadisticas')
  const fetcher = useCallback(() => getSellerStatistics(), [])
  const { data: estadisticas, failed, retry } = useApiQuery(fetcher)

  return (
    <section aria-labelledby={titleId}>
      <Card className={styles.card}>
        <Title3 as="h2" id={titleId} className={styles.heading}>
          Mis estadísticas
        </Title3>
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
            <SalesBlock ventas={estadisticas.ventas} />
            <RatingsBlock valoraciones={estadisticas.valoraciones} />
            <StockBlock stock={estadisticas.stock} />
          </>
        )}
      </Card>
    </section>
  )
}
