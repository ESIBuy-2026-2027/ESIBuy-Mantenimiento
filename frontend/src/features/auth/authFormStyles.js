import { makeStyles, tokens } from '@fluentui/react-components'

export const useFormStyles = makeStyles({
  form: {
    display: 'flex',
    flexDirection: 'column',
    gap: tokens.spacingVerticalL,
  },
  actions: {
    display: 'flex',
    flexDirection: 'column',
    gap: tokens.spacingVerticalS,
  },
  captcha: {
    display: 'flex',
    flexDirection: 'column',
    alignItems: 'flex-start',
    gap: tokens.spacingVerticalS,
    overflowX: 'auto',
    maxWidth: '100%',
  },
  error: {
    color: tokens.colorPaletteRedForeground1,
  },
})
