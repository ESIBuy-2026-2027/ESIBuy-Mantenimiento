import { Field, Select, SearchBox, makeStyles, tokens } from '@fluentui/react-components'

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
  category: {
    flexGrow: 1,
    flexBasis: '200px',
  },
})

export default function ProductFilters({ search, category, categories, onSearchChange, onCategoryChange }) {
  const styles = useStyles()

  return (
    <div className={styles.filters}>
      <Field label="Buscar por nombre" className={styles.search}>
        <SearchBox
          value={search}
          placeholder="Escribe el nombre de un producto"
          onChange={(_event, data) => onSearchChange(data.value)}
        />
      </Field>
      <Field label="Categoría" className={styles.category}>
        <Select value={category} onChange={(_event, data) => onCategoryChange(data.value)}>
          <option value="">Todas las categorías</option>
          {categories.map((item) => (
            <option key={item.id} value={item.id}>
              {item.nombre}
            </option>
          ))}
        </Select>
      </Field>
    </div>
  )
}
