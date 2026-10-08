import { Field, SearchBox, makeStyles, tokens } from '@fluentui/react-components'

const useStyles = makeStyles({
  filters: {
    display: 'flex',
    flexWrap: 'wrap',
    gap: tokens.spacingHorizontalL,
  },
  search: {
    flexGrow: 2,
    flexBasis: '240px',
  },
})

export default function UsersFilters({ search, onSearchChange }) {
  const styles = useStyles()

  return (
    <div className={styles.filters}>
      <Field label="Buscar por nombre o email" className={styles.search}>
        <SearchBox
          value={search}
          placeholder="Escribe un nombre o un email"
          onChange={(_event, data) => onSearchChange(data.value)}
        />
      </Field>
    </div>
  )
}
