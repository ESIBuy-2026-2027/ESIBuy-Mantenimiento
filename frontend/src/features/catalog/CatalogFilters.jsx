import PropTypes from 'prop-types'
import {
  ArrowSortDown20Regular,
  ArrowSortUp20Regular,
} from '@fluentui/react-icons'
import {
  Button,
  Checkbox,
  Field,
  SearchBox,
  Select,
  makeStyles,
  tokens,
} from '@fluentui/react-components'
import CategoryCombobox from '../../components/CategoryCombobox'
import { categoryPropType } from '../../utils/propTypes'

const SORT_OPTIONS = [
  { value: 'nombre', label: 'Nombre' },
  { value: 'valoracion', label: 'Valoración' },
  { value: 'precio', label: 'Precio' },
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
    padding: '16px',
    backgroundColor: tokens.colorNeutralBackground1,
    border: `1px solid ${tokens.colorNeutralStroke2}`,
    borderRadius: tokens.borderRadiusXLarge,
    boxShadow: tokens.shadow2,
  },
  fields: {
    display: 'grid',
    gridTemplateColumns: 'minmax(190px, 1.55fr) minmax(170px, 1.2fr) minmax(150px, 1.1fr) minmax(150px, 1fr) minmax(150px, 1fr)',
    alignItems: 'start',
    columnGap: tokens.spacingHorizontalL,
    rowGap: tokens.spacingVerticalM,
    '@media (max-width: 1050px)': {
      gridTemplateColumns: 'repeat(3, minmax(0, 1fr))',
    },
    '@media (max-width: 700px)': {
      gridTemplateColumns: 'repeat(2, minmax(0, 1fr))',
    },
    '@media (max-width: 480px)': {
      gridTemplateColumns: 'minmax(0, 1fr)',
    },
  },
  field: {
    minWidth: 0,
  },
  priceField: {
    minWidth: 0,
  },
  range: {
    position: 'relative',
    height: '32px',
    display: 'flex',
    alignItems: 'center',
    marginTop: '1px',
    padding: '0 8px',
  },
  rangeTrack: {
    position: 'absolute',
    left: '8px',
    right: '8px',
    height: '4px',
    borderRadius: tokens.borderRadiusCircular,
    backgroundColor: tokens.colorNeutralStroke2,
  },
  rangeInput: {
    appearance: 'none',
    position: 'absolute',
    left: '0',
    top: '0',
    width: '100%',
    height: '32px',
    margin: 0,
    background: 'transparent',
    pointerEvents: 'none',
    outlineOffset: '3px',
    '&::-webkit-slider-runnable-track': {
      height: '4px',
      background: 'transparent',
    },
    '&::-moz-range-track': {
      height: '4px',
      background: 'transparent',
    },
    '&::-webkit-slider-thumb': {
      appearance: 'none',
      width: '16px',
      height: '16px',
      marginTop: '-6px',
      border: '2px solid white',
      borderRadius: tokens.borderRadiusCircular,
      backgroundColor: tokens.colorBrandBackground,
      boxShadow: tokens.shadow2,
      pointerEvents: 'auto',
      cursor: 'pointer',
    },
    '&::-moz-range-thumb': {
      width: '13px',
      height: '13px',
      border: '2px solid white',
      borderRadius: tokens.borderRadiusCircular,
      backgroundColor: tokens.colorBrandBackground,
      boxShadow: tokens.shadow2,
      pointerEvents: 'auto',
      cursor: 'pointer',
    },
  },
  priceValues: {
    display: 'flex',
    justifyContent: 'space-between',
    alignItems: 'center',
    marginTop: '-2px',
    fontSize: tokens.fontSizeBase300,
    color: tokens.colorNeutralForeground1,
  },
  options: {
    display: 'flex',
    flexWrap: 'wrap',
    alignItems: 'center',
    gap: tokens.spacingHorizontalXL,
    minHeight: '28px',
    marginTop: '2px',
  },
  sort: {
    display: 'flex',
    alignItems: 'end',
    gap: tokens.spacingHorizontalXS,
    minWidth: 0,
  },
  sortField: {
    flex: 1,
    minWidth: 0,
  },
  direction: {
    minWidth: '36px',
    width: '36px',
    height: '32px',
    padding: 0,
    alignSelf: 'end',
  },
  clear: {
    marginLeft: 'auto',
  },
})

// Filtros del catálogo organizados en una fila principal y una fila compacta de opciones.
export default function CatalogFilters({
  search,
  category,
  categories,
  minRating,
  minPrice,
  maxPrice,
  priceCeiling,
  inStock,
  offersOnly,
  filtersActive,
  sort,
  order,
  onSearchChange,
  onCategoryChange,
  onMinRatingChange,
  onPriceChange,
  onInStockChange,
  onOffersOnlyChange,
  onClearFilters,
  onSortChange,
  onOrderChange,
}) {
  const styles = useStyles()
  const ascending = order === 'asc'
  const sliderMax = Math.max(1, Math.ceil(priceCeiling))
  const displayedMaxPrice = maxPrice === null ? priceCeiling : Math.min(maxPrice, priceCeiling)
  const minPercent = (minPrice / sliderMax) * 100
  const maxPercent = (displayedMaxPrice / sliderMax) * 100

  const changeMin = (event) => {
    const next = Math.min(Number(event.target.value), displayedMaxPrice)
    onPriceChange(next, maxPrice)
  }
  const changeMax = (event) => {
    const next = Math.max(Number(event.target.value), minPrice)
    onPriceChange(minPrice, next >= priceCeiling ? null : next)
  }

  return (
    <section className={styles.bar} aria-label="Filtros del catálogo">
      <div className={styles.fields}>
        <Field label="Buscar por nombre" className={styles.field}>
          <SearchBox
            value={search}
            placeholder="Escribe el nombre de un producto"
            onChange={(_event, data) => onSearchChange(data.value)}
          />
        </Field>

        <Field label="Categoría" className={styles.field}>
          <CategoryCombobox
            value={category}
            categories={categories}
            allLabel="Todas las categorías"
            onChange={onCategoryChange}
          />
        </Field>

        <Field label="Precio" className={styles.priceField}>
          <div className={styles.range}>
            <div
              className={styles.rangeTrack}
              style={{
                background: `linear-gradient(to right, ${tokens.colorNeutralStroke2} ${minPercent}%, ${tokens.colorBrandBackground} ${minPercent}%, ${tokens.colorBrandBackground} ${maxPercent}%, ${tokens.colorNeutralStroke2} ${maxPercent}%)`,
              }}
            />
            <input
              className={styles.rangeInput}
              type="range"
              min="0"
              max={sliderMax}
              step="1"
              value={minPrice}
              aria-label="Precio mínimo"
              onChange={changeMin}
            />
            <input
              className={styles.rangeInput}
              type="range"
              min="0"
              max={sliderMax}
              step="1"
              value={displayedMaxPrice}
              aria-label="Precio máximo"
              onChange={changeMax}
            />
          </div>
          <div className={styles.priceValues} aria-live="polite">
            <span>{minPrice.toLocaleString('es-ES')} €</span>
            <span>{displayedMaxPrice.toLocaleString('es-ES')} €</span>
          </div>
        </Field>

        <Field label="Valoración mínima" className={styles.field}>
          <Select value={minRating} onChange={(_event, data) => onMinRatingChange(data.value)}>
            {RATING_OPTIONS.map(({ value, label }) => (
              <option key={value} value={value}>
                {label}
              </option>
            ))}
          </Select>
        </Field>

        <div className={styles.sort}>
          <Field label="Ordenar por" className={styles.sortField}>
            <Select value={sort} onChange={(_event, data) => onSortChange(data.value)}>
              {SORT_OPTIONS.map(({ value, label }) => (
                <option key={value} value={value}>
                  {label}
                </option>
              ))}
            </Select>
          </Field>
          <Button
            className={styles.direction}
            appearance="subtle"
            icon={ascending ? <ArrowSortUp20Regular /> : <ArrowSortDown20Regular />}
            aria-label={ascending ? 'Orden ascendente' : 'Orden descendente'}
            title={ascending ? 'Orden ascendente' : 'Orden descendente'}
            onClick={() => onOrderChange(ascending ? 'desc' : 'asc')}
          />
        </div>
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
  minPrice: PropTypes.number.isRequired,
  maxPrice: PropTypes.number, 
  priceCeiling: PropTypes.number.isRequired,
  inStock: PropTypes.bool.isRequired,
  offersOnly: PropTypes.bool.isRequired,
  filtersActive: PropTypes.bool.isRequired,
  sort: PropTypes.string.isRequired,
  order: PropTypes.oneOf(['asc', 'desc']).isRequired,
  onSearchChange: PropTypes.func.isRequired,
  onCategoryChange: PropTypes.func.isRequired,
  onMinRatingChange: PropTypes.func.isRequired,
  onPriceChange: PropTypes.func.isRequired,
  onInStockChange: PropTypes.func.isRequired,
  onOffersOnlyChange: PropTypes.func.isRequired,
  onClearFilters: PropTypes.func.isRequired,
  onSortChange: PropTypes.func.isRequired,
  onOrderChange: PropTypes.func.isRequired,
}
