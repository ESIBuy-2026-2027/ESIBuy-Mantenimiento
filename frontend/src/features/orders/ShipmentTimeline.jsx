import PropTypes from 'prop-types'
import { Text, makeStyles, mergeClasses, tokens } from '@fluentui/react-components'
import {
  Box24Regular,
  Checkmark24Regular,
  Home24Regular,
  Send24Regular,
  VehicleTruckProfile24Regular,
} from '@fluentui/react-icons'
import { formatDate } from '../../utils/format'
import { PASOS_DEL_ENVIO, etiquetaDelPaso, fechaDelPaso, indiceDelPaso } from './orderStatus'

const TAMANO_CIRCULO = '40px'
const GROSOR_LINEA = '3px'
const ANCHO_HORIZONTAL = '@media (min-width: 640px)'

const useStyles = makeStyles({
  root: {
    display: 'flex',
    flexDirection: 'column',
    gap: tokens.spacingVerticalL,
  },
  steps: {
    listStyle: 'none',
    margin: 0,
    padding: 0,
    display: 'flex',
    flexDirection: 'column',
    [ANCHO_HORIZONTAL]: {
      flexDirection: 'row',
    },
  },
  step: {
    position: 'relative',
    display: 'flex',
    alignItems: 'flex-start',
    gap: tokens.spacingHorizontalM,
    paddingBottom: tokens.spacingVerticalL,
    [ANCHO_HORIZONTAL]: {
      flex: 1,
      flexDirection: 'column',
      alignItems: 'center',
      textAlign: 'center',
      gap: tokens.spacingVerticalS,
      paddingBottom: 0,
    },
  },
  // Línea que une este paso con el siguiente: hacia abajo en pantallas estrechas, hacia la derecha en las anchas
  line: {
    '::after': {
      content: '""',
      position: 'absolute',
      left: `calc(${TAMANO_CIRCULO} / 2 - ${GROSOR_LINEA} / 2)`,
      top: TAMANO_CIRCULO,
      bottom: 0,
      width: GROSOR_LINEA,
      borderRadius: tokens.borderRadiusCircular,
      backgroundColor: tokens.colorNeutralStroke2,
    },
    [ANCHO_HORIZONTAL]: {
      '::after': {
        left: `calc(50% + ${TAMANO_CIRCULO} / 2 + ${tokens.spacingHorizontalXS})`,
        right: `calc(-50% + ${TAMANO_CIRCULO} / 2 + ${tokens.spacingHorizontalXS})`,
        top: `calc(${TAMANO_CIRCULO} / 2 - ${GROSOR_LINEA} / 2)`,
        bottom: 'auto',
        width: 'auto',
        height: GROSOR_LINEA,
      },
    },
  },
  lineDone: {
    '::after': {
      backgroundColor: tokens.colorPaletteGreenBackground3,
    },
  },
  circle: {
    flexShrink: 0,
    width: TAMANO_CIRCULO,
    height: TAMANO_CIRCULO,
    boxSizing: 'border-box',
    display: 'flex',
    alignItems: 'center',
    justifyContent: 'center',
    borderRadius: tokens.borderRadiusCircular,
    borderWidth: '2px',
    borderStyle: 'solid',
  },
  circleDone: {
    backgroundColor: tokens.colorPaletteGreenBackground3,
    borderColor: tokens.colorPaletteGreenBackground3,
    color: tokens.colorNeutralForegroundOnBrand,
  },
  circleCurrent: {
    backgroundColor: tokens.colorBrandBackground,
    borderColor: tokens.colorBrandBackground,
    color: tokens.colorNeutralForegroundOnBrand,
    boxShadow: `0 0 0 4px ${tokens.colorBrandBackground2}`,
  },
  circlePending: {
    backgroundColor: tokens.colorNeutralBackground1,
    borderColor: tokens.colorNeutralStroke1,
    color: tokens.colorNeutralForeground3,
  },
  text: {
    display: 'flex',
    flexDirection: 'column',
    gap: '2px',
    paddingTop: tokens.spacingVerticalXS,
    [ANCHO_HORIZONTAL]: {
      paddingTop: 0,
    },
  },
  label: {
    fontWeight: tokens.fontWeightSemibold,
  },
  labelPending: {
    color: tokens.colorNeutralForeground3,
  },
  detail: {
    color: tokens.colorNeutralForeground3,
  },
  detailCurrent: {
    color: tokens.colorBrandForeground1,
    fontWeight: tokens.fontWeightSemibold,
  },
  summary: {
    padding: `${tokens.spacingVerticalS} ${tokens.spacingHorizontalM}`,
    borderRadius: tokens.borderRadiusMedium,
    backgroundColor: tokens.colorNeutralBackground3,
  },
})

const ICONOS_DE_PASO = {
  PREPARANDO: Box24Regular,
  ENVIADO: Send24Regular,
  EN_REPARTO: VehicleTruckProfile24Regular,
  ENTREGADO: Home24Regular,
}

// Situación de cada paso respecto al estado del envío. Un envío entregado tiene los cuatro pasos completados.
function situacionDelPaso(indice, indiceActual, entregado) {
  if (indice < indiceActual || (entregado && indice === indiceActual)) return 'completado'
  if (indice === indiceActual) return 'actual'
  return 'pendiente'
}

// Mensaje sobre la fecha de entrega: la real si ya se entregó, la estimada si se conoce, o que no está disponible
function textoDeEntrega({ entregado, fechaEntrega, fechaEstimadaEntrega }) {
  if (entregado) {
    return fechaEntrega ? `Entregado el ${formatDate(fechaEntrega)}` : 'Pedido entregado'
  }
  return fechaEstimadaEntrega
    ? `Entrega estimada: ${formatDate(fechaEstimadaEntrega)}`
    : 'Fecha estimada de entrega no disponible todavía'
}

// Línea de tiempo del envío (PBI 540): preparación, enviado, en reparto y entregado. Cada paso se distingue por su
// icono (un tick si está completado), por la forma de su círculo y por un texto visible (completado, estado actual o
// pendiente), no solo por el color; los pasos con fecha la muestran. Debajo, la fecha estimada de entrega (o la real
// una vez entregado). En pantallas estrechas va en vertical y en las anchas en horizontal.
export default function ShipmentTimeline({ estado, historial, fechaEstimadaEntrega, fechaEntrega }) {
  const styles = useStyles()
  const indiceActual = indiceDelPaso(estado)
  const entregado = estado === 'ENTREGADO'

  if (indiceActual === -1) {
    return <Text>El estado del envío no está disponible en este momento.</Text>
  }

  const clasesDelCirculo = {
    completado: styles.circleDone,
    actual: styles.circleCurrent,
    pendiente: styles.circlePending,
  }
  const textos = { completado: 'Completado', actual: 'Estado actual', pendiente: 'Pendiente' }

  return (
    <div className={styles.root}>
      <ol aria-label="Pasos del envío" className={styles.steps}>
        {PASOS_DEL_ENVIO.map((paso, indice) => {
          const situacion = situacionDelPaso(indice, indiceActual, entregado)
          const fecha = situacion === 'pendiente' ? null : fechaDelPaso(paso, historial)
          const Icono = situacion === 'completado' ? Checkmark24Regular : ICONOS_DE_PASO[paso]
          const hayLinea = indice < PASOS_DEL_ENVIO.length - 1
          const lineaHecha = hayLinea && (indice < indiceActual || entregado)
          return (
            <li
              key={paso}
              aria-current={situacion === 'actual' ? 'step' : undefined}
              className={mergeClasses(styles.step, hayLinea && styles.line, lineaHecha && styles.lineDone)}
            >
              <span className={mergeClasses(styles.circle, clasesDelCirculo[situacion])}>
                <Icono aria-hidden="true" />
              </span>
              <span className={styles.text}>
                <Text className={mergeClasses(styles.label, situacion === 'pendiente' && styles.labelPending)}>
                  {etiquetaDelPaso(paso)}
                </Text>
                <Text size={200} className={situacion === 'actual' ? styles.detailCurrent : styles.detail}>
                  {textos[situacion]}
                  {fecha && (
                    <>
                      {' · '}
                      <time dateTime={fecha}>{formatDate(fecha)}</time>
                    </>
                  )}
                </Text>
              </span>
            </li>
          )
        })}
      </ol>
      <Text className={styles.summary}>{textoDeEntrega({ entregado, fechaEntrega, fechaEstimadaEntrega })}</Text>
    </div>
  )
}

ShipmentTimeline.propTypes = {
  estado: PropTypes.string.isRequired,
  historial: PropTypes.arrayOf(
    PropTypes.shape({
      estado: PropTypes.string.isRequired,
      fecha: PropTypes.string,
    }),
  ).isRequired,
  fechaEstimadaEntrega: PropTypes.string,
  fechaEntrega: PropTypes.string,
}
