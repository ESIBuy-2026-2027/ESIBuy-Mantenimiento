import PropTypes from 'prop-types'
import { Button, Checkbox, Field, Select, makeStyles, tokens } from '@fluentui/react-components'
import ListFilters from '../../components/ListFilters'
import SortControls from '../admin/SortControls'
import { categoryPropType } from '../../utils/propTypes'

const SORT_OPTIONS = [
  { value: 'nombre', label: 'Nombre' },
  { value: 'valoracion', label: 'Valoración' },
]

const RATING_OPTIONS = [
  { value: '', label: 'Cualquiera' },
  { value: '4', label: '4 estrellas o más' },
  { value: '3', label: '3 estrellas o más' },
  { value: '2', label: '2 estrellas o más' },
]

const useStyles = makeStyles({
  bar: {
    display: 'flex',
    flexDirection: 'column',
    gap: tokens.spacingVerticalM,
    padding: tokens.spacingHorizontalL,
    backgroundColor: tokens.colorNeutralBackground1,
    border: `1px solid ${tokens.colorNeutralStroke2}`,
    borderRadius: tokens.borderRadiusXLarge,
    boxShadow: tokens.shadow2,
  },
  filters: {
    display: 'flex',
    flexWrap: 'wrap',
    alignItems: 'end',
    gap: tokens.spacingHorizontalL,
  },
  rating: {
    flexGrow: 1,
    flexBasis: '160px',
  },
  options: {
    display: 'flex',
    flexWrap: 'wrap',
    alignItems: 'center',
    gap: tokens.spacingHorizontalXL,
    minHeight: '32px',
  },
  clear: {
    marginLeft: 'auto',
  },
})

// Búsqueda, categoría, valoración, disponibilidad, ofertas y orden del catálogo.
export default function CatalogFilters({
  search,
  category,
  categories,
  minRating,
  inStock,
  offersOnly,
  filtersActive,
  sort,
  order,
  onSearchChange,
  onCategoryChange,
  onMinRatingChange,
  onInStockChange,
  onOffersOnlyChange,
  onClearFilters,
  onSortChange,
  onOrderChange,
}) {
  const styles = useStyles()

  return (
    <section className={styles.bar} aria-label="Filtros del catálogo">
      <div className={styles.filters}>
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
      </div>
      <div className={styles.options}>
        <Checkbox
          checked={inStock}
          label="Solo en stock"
          onChange={(_event, data) => onInStockChange(data.checked)}
        />
        <Checkbox
          checked={offersOnly}
          label="Solo ofertas"
          onChange={(_event, data) => onOffersOnlyChange(data.checked)}
        />
        {filtersActive && (
          <Button className={styles.clear} appearance="subtle" onClick={onClearFilters}>
            Limpiar filtros
          </Button>
        )}
      </div>
    </section>
  )
}

CatalogFilters.propTypes = {
  search: PropTypes.string.isRequired,
  category: PropTypes.string.isRequired,
  categories: PropTypes.arrayOf(categoryPropType).isRequired,
  minRating: PropTypes.string.isRequired,
  inStock: PropTypes.bool.isRequired,
  offersOnly: PropTypes.bool.isRequired,
  filtersActive: PropTypes.bool.isRequired,
  sort: PropTypes.string.isRequired,
  order: PropTypes.oneOf(['asc', 'desc']).isRequired,
  onSearchChange: PropTypes.func.isRequired,
  onCategoryChange: PropTypes.func.isRequired,
  onMinRatingChange: PropTypes.func.isRequired,
  onInStockChange: PropTypes.func.isRequired,
  onOffersOnlyChange: PropTypes.func.isRequired,
  onClearFilters: PropTypes.func.isRequired,
  onSortChange: PropTypes.func.isRequired,
  onOrderChange: PropTypes.func.isRequired,
}
