import PropTypes from 'prop-types'
import { Field, Select, makeStyles } from '@fluentui/react-components'
import ListFilters from '../../components/ListFilters'
import SortControls from '../admin/SortControls'
import { categoryPropType } from '../../utils/propTypes'

const SORT_OPTIONS = [
  { value: 'nombre', label: 'Nombre' },
  { value: 'categoria', label: 'Categoría' },
  { value: 'valoracion', label: 'Valoración' },
]

const RATING_OPTIONS = [
  { value: '', label: 'Cualquiera' },
  { value: '4', label: '4 estrellas o más' },
  { value: '3', label: '3 estrellas o más' },
  { value: '2', label: '2 estrellas o más' },
]

const useStyles = makeStyles({
  rating: {
    flexGrow: 1,
    flexBasis: '160px',
  },
})

// Búsqueda por nombre, categoría y valoración mínima, más el orden de las tarjetas.
export default function CatalogFilters({
  search,
  category,
  categories,
  minRating,
  sort,
  order,
  onSearchChange,
  onCategoryChange,
  onMinRatingChange,
  onSortChange,
  onOrderChange,
}) {
  const styles = useStyles()

  return (
    <ListFilters
      search={search}
      category={category}
      categories={categories}
      searchPlaceholder="Escribe el nombre de un producto"
      onSearchChange={onSearchChange}
      onCategoryChange={onCategoryChange}
    >
      <Field label="Valoración mínima" className={styles.rating}>
        <Select value={minRating} onChange={(_event, data) => onMinRatingChange(data.value)}>
          {RATING_OPTIONS.map(({ value, label }) => (
            <option key={value} value={value}>
              {label}
            </option>
          ))}
        </Select>
      </Field>
      <SortControls
        options={SORT_OPTIONS}
        sort={sort}
        order={order}
        onSortChange={onSortChange}
        onOrderChange={onOrderChange}
      />
    </ListFilters>
  )
}

CatalogFilters.propTypes = {
  search: PropTypes.string.isRequired,
  category: PropTypes.string.isRequired,
  categories: PropTypes.arrayOf(categoryPropType).isRequired,
  minRating: PropTypes.string.isRequired,
  sort: PropTypes.string.isRequired,
  order: PropTypes.oneOf(['asc', 'desc']).isRequired,
  onSearchChange: PropTypes.func.isRequired,
  onCategoryChange: PropTypes.func.isRequired,
  onMinRatingChange: PropTypes.func.isRequired,
  onSortChange: PropTypes.func.isRequired,
  onOrderChange: PropTypes.func.isRequired,
}
