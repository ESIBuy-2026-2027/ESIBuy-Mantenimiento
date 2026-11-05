import PropTypes from 'prop-types'
import { Subtitle1, Text, makeStyles, tokens } from '@fluentui/react-components'

const useStyles = makeStyles({
  state: {
    display: 'flex',
    flexDirection: 'column',
    alignItems: 'center',
    gap: tokens.spacingVerticalM,
    padding: `${tokens.spacingVerticalXXXL} ${tokens.spacingHorizontalL}`,
    textAlign: 'center',
    backgroundColor: tokens.colorNeutralBackground1,
    border: `1px dashed ${tokens.colorNeutralStroke1}`,
    borderRadius: tokens.borderRadiusXLarge,
  },
  icon: {
    display: 'flex',
    alignItems: 'center',
    justifyContent: 'center',
    width: '72px',
    height: '72px',
    borderRadius: tokens.borderRadiusCircular,
    backgroundColor: tokens.colorBrandBackground2,
    color: tokens.colorBrandForeground1,
  },
  description: {
    maxWidth: '420px',
    color: tokens.colorNeutralForeground3,
  },
})

// `headingLevel` permite meterlo dentro de una sección con su propio título sin romper la jerarquía (h2 por defecto).
export default function EmptyState({ icon, title, description, headingLevel = 'h2', children }) {
  const styles = useStyles()

  return (
    <section className={styles.state}>
      {icon && (
        <span className={styles.icon} aria-hidden="true">
          {icon}
        </span>
      )}
      <Subtitle1 as={headingLevel}>{title}</Subtitle1>
      <Text className={styles.description}>{description}</Text>
      {children}
    </section>
  )
}

EmptyState.propTypes = {
  icon: PropTypes.node,
  title: PropTypes.string.isRequired,
  description: PropTypes.string.isRequired,
  headingLevel: PropTypes.oneOf(['h2', 'h3', 'h4']),
  children: PropTypes.node,
}
