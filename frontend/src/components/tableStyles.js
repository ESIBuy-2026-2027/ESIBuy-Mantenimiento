import { makeStyles, tokens } from '@fluentui/react-components'

export const useTableStyles = makeStyles({
  wrapper: {
    overflowX: 'auto',
    backgroundColor: tokens.colorNeutralBackground1,
    border: `1px solid ${tokens.colorNeutralStroke2}`,
    borderRadius: tokens.borderRadiusXLarge,
    boxShadow: tokens.shadow4,
    transitionProperty: 'opacity',
    transitionDuration: tokens.durationNormal,
    transitionTimingFunction: tokens.curveEasyEase,
    '@media (prefers-reduced-motion: reduce)': {
      transitionDuration: '0.01ms',
    },
  },
  refreshing: {
    opacity: 0.55,
  },
  row: {
    transitionProperty: 'background-color',
    transitionDuration: tokens.durationFast,
    ':hover': {
      backgroundColor: tokens.colorBrandBackground2,
    },
  },
  name: {
    minWidth: '180px',
  },
  // Un email largo se parte dentro de su celda en vez de montarse sobre las columnas de al lado
  // (fallos de la demo del Sprint 1)
  email: {
    minWidth: '200px',
    overflowWrap: 'anywhere',
    wordBreak: 'break-word',
  },
})
