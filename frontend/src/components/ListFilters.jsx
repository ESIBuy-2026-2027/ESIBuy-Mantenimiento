import PropTypes from 'prop-types'
import { categoryPropType } from '../utils/propTypes'
import { Field, SearchBox, makeStyles, tokens } from '@fluentui/react-components'
import CategoryCombobox from './CategoryCombobox'

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

export default function ListFilters({
  search,
  category,
  categories,
  searchPlaceholder,
  onSearchChange,
  onCategoryChange,
  children,
}) {
  const styles = useStyles()

  return (
    <div className={styles.filters}>
      <Field label="Buscar por nombre" className={styles.search}>
        <SearchBox
          value={search}
          placeholder={searchPlaceholder}
          onChange={(_event, data) => onSearchChange(data.value)}
        />
      </Field>
      <Field label="Categoría" className={styles.category}>
        <CategoryCombobox
          value={category}
          categories={categories}
          allLabel="Todas las categorías"
          onChange={onCategoryChange}
        />
      </Field>
      {children}
    </div>
  )
}

ListFilters.propTypes = {
  search: PropTypes.string.isRequired,
  category: PropTypes.string.isRequired,
  categories: PropTypes.arrayOf(categoryPropType).isRequired,
  searchPlaceholder: PropTypes.string,
  onSearchChange: PropTypes.func.isRequired,
  onCategoryChange: PropTypes.func.isRequired,
  children: PropTypes.node,
}
