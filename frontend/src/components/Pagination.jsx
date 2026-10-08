import { Button, Text, makeStyles, tokens } from '@fluentui/react-components'
import { ChevronLeft20Regular, ChevronRight20Regular } from '@fluentui/react-icons'

const useStyles = makeStyles({
  nav: {
    display: 'flex',
    flexWrap: 'wrap',
    alignItems: 'center',
    justifyContent: 'space-between',
    gap: tokens.spacingHorizontalM,
  },
  summary: {
    color: tokens.colorNeutralForeground3,
  },
  controls: {
    display: 'flex',
    alignItems: 'center',
    gap: tokens.spacingHorizontalS,
  },
})

export default function Pagination({ page, totalPages, totalItems, pageSize, itemLabel = 'elementos', onPageChange }) {
  const styles = useStyles()

  if (totalPages <= 1) {
    return null
  }

  const first = (page - 1) * pageSize + 1
  const last = Math.min(page * pageSize, totalItems)

  return (
    <nav className={styles.nav} aria-label="Paginación">
      <Text className={styles.summary}>
        Mostrando {first}–{last} de {totalItems} {itemLabel}
      </Text>
      <div className={styles.controls}>
        <Button
          appearance="subtle"
          icon={<ChevronLeft20Regular />}
          disabled={page <= 1}
          onClick={() => onPageChange(page - 1)}
        >
          Anterior
        </Button>
        <Text>
          Página {page} de {totalPages}
        </Text>
        <Button
          appearance="subtle"
          icon={<ChevronRight20Regular />}
          iconPosition="after"
          disabled={page >= totalPages}
          onClick={() => onPageChange(page + 1)}
        >
          Siguiente
        </Button>
      </div>
    </nav>
  )
}
