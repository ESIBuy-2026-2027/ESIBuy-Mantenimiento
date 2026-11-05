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
})

// Página de acceso con la tarjeta centrada: inicio de sesión y recuperación de contraseña (HU3).
export const useAuthPageStyles = makeStyles({
  page: {
    minHeight: '100vh',
    boxSizing: 'border-box',
    display: 'flex',
    alignItems: 'center',
    justifyContent: 'center',
    padding: tokens.spacingHorizontalL,
    backgroundImage: `linear-gradient(180deg, ${tokens.colorBrandBackground2} 0%, ${tokens.colorNeutralBackground1} 60%)`,
  },
  card: {
    width: '100%',
    maxWidth: '420px',
    padding: tokens.spacingVerticalXXL,
    gap: tokens.spacingVerticalL,
    borderRadius: tokens.borderRadiusXLarge,
    boxShadow: tokens.shadow28,
  },
  title: {
    margin: 0,
  },
  footer: {
    display: 'flex',
    flexWrap: 'wrap',
    alignItems: 'center',
    justifyContent: 'center',
    color: tokens.colorNeutralForeground3,
  },
})
