import PropTypes from 'prop-types'
import { useState } from 'react'
import { Text, makeStyles, mergeClasses, tokens } from '@fluentui/react-components'
import { ArrowRight20Regular } from '@fluentui/react-icons'

const useStyles = makeStyles({
  card: {
    display: 'flex',
    alignItems: 'center',
    gap: tokens.spacingHorizontalL,
    width: '100%',
    boxSizing: 'border-box',
    padding: tokens.spacingVerticalXL,
    font: 'inherit',
    color: 'inherit',
    textAlign: 'left',
    cursor: 'pointer',
    backgroundColor: tokens.colorNeutralBackground1,
    border: `1px solid ${tokens.colorNeutralStroke2}`,
    borderRadius: tokens.borderRadiusXLarge,
    boxShadow: tokens.shadow4,
    transitionProperty: 'transform, box-shadow, border-color',
    transitionDuration: tokens.durationNormal,
    transitionTimingFunction: tokens.curveEasyEase,
    ':hover': {
      transform: 'translateY(-4px)',
      boxShadow: tokens.shadow16,
      borderColor: tokens.colorBrandStroke1,
    },
    ':active': {
      transform: 'translateY(-1px)',
    },
    ':focus-visible': {
      outline: `2px solid ${tokens.colorStrokeFocus2}`,
      outlineOffset: '2px',
    },
    '@media (prefers-reduced-motion: reduce)': {
      transitionDuration: '0.01ms',
      ':hover': { transform: 'none' },
    },
  },
  icon: {
    display: 'flex',
    alignItems: 'center',
    justifyContent: 'center',
    flexShrink: 0,
    width: '56px',
    height: '56px',
    borderRadius: tokens.borderRadiusCircular,
    backgroundColor: tokens.colorBrandBackground2,
    color: tokens.colorBrandForeground1,
    transitionProperty: 'background-color, color, transform',
    transitionDuration: tokens.durationNormal,
    transitionTimingFunction: tokens.curveEasyEase,
    '@media (prefers-reduced-motion: reduce)': {
      transitionDuration: '0.01ms',
    },
  },
  iconActive: {
    backgroundColor: tokens.colorBrandBackground,
    color: tokens.colorNeutralForegroundOnBrand,
    transform: 'scale(1.08)',
  },
  body: {
    display: 'flex',
    flexDirection: 'column',
    flexGrow: 1,
    gap: tokens.spacingVerticalXXS,
  },
  description: {
    color: tokens.colorNeutralForeground3,
  },
  arrow: {
    flexShrink: 0,
    color: tokens.colorBrandForeground1,
    opacity: 0.4,
    transitionProperty: 'opacity, transform',
    transitionDuration: tokens.durationNormal,
    transitionTimingFunction: tokens.curveEasyEase,
    '@media (prefers-reduced-motion: reduce)': {
      transitionDuration: '0.01ms',
    },
  },
  arrowActive: {
    opacity: 1,
    transform: 'translateX(6px)',
  },
})

export default function AdminSectionCard({ icon, title, description, onClick }) {
  const styles = useStyles()
  const [active, setActive] = useState(false)

  return (
    <button
      type="button"
      className={styles.card}
      onClick={onClick}
      onPointerEnter={() => setActive(true)}
      onPointerLeave={() => setActive(false)}
      onFocus={() => setActive(true)}
      onBlur={() => setActive(false)}
    >
      <span className={mergeClasses(styles.icon, active && styles.iconActive)} aria-hidden="true">
        {icon}
      </span>
      <span className={styles.body}>
        <Text size={500} weight="semibold">
          {title}
        </Text>
        <Text className={styles.description}>{description}</Text>
      </span>
      <ArrowRight20Regular
        className={mergeClasses(styles.arrow, active && styles.arrowActive)}
        aria-hidden="true"
      />
    </button>
  )
}

AdminSectionCard.propTypes = {
  icon: PropTypes.node.isRequired,
  title: PropTypes.string.isRequired,
  description: PropTypes.string.isRequired,
  onClick: PropTypes.func,
}
