import PropTypes from 'prop-types'
import { useState } from 'react'
import { Combobox, Option } from '@fluentui/react-components'
import { categoryPropType } from '../utils/propTypes'

// Valor interno de la opción «todas»: Fluent no admite una opción con valor vacío. Fuera del componente es ''.
const TODAS = '__todas__'

// Sin distinguir mayúsculas ni tildes: «jardin» encuentra «Jardín».
const normalizar = (texto) => texto.normalize('NFD').replace(/\p{Diacritic}/gu, '').toLowerCase()

// Desplegable con búsqueda para elegir una categoría (HU31): con muchas categorías se puede escribir para filtrar.
// Va dentro de un Field, que le da la etiqueta, el asterisco de obligatorio y el mensaje de error.
export default function CategoryCombobox({ value, categories, onChange, allLabel, placeholder, ...fieldProps }) {
  // null mientras no se escribe: entonces se muestra la categoría elegida y se ofrecen todas.
  const [busqueda, setBusqueda] = useState(null)

  const opciones = [
    ...(allLabel ? [{ value: TODAS, label: allLabel }] : []),
    ...categories.map(({ id, nombre }) => ({ value: id, label: nombre })),
  ]
  const valorInterno = value === '' && allLabel ? TODAS : value
  const elegida = opciones.find((opcion) => opcion.value === valorInterno)
  const visibles = busqueda
    ? opciones.filter((opcion) => normalizar(opcion.label).includes(normalizar(busqueda)))
    : opciones

  const elegir = (_event, data) => {
    setBusqueda(null)
    if (!data.optionValue) return
    onChange(data.optionValue === TODAS ? '' : data.optionValue)
  }

  return (
    <Combobox
      {...fieldProps}
      value={busqueda ?? elegida?.label ?? ''}
      selectedOptions={elegida ? [elegida.value] : []}
      placeholder={placeholder}
      onChange={(event) => setBusqueda(event.target.value)}
      onOptionSelect={elegir}
      // Al cerrar sin elegir se descarta lo escrito y vuelve a verse la categoría elegida.
      onOpenChange={(_event, data) => {
        if (!data.open) setBusqueda(null)
      }}
    >
      {visibles.map((opcion) => (
        <Option key={opcion.value} value={opcion.value}>
          {opcion.label}
        </Option>
      ))}
      {busqueda && visibles.length === 0 && (
        <Option disabled value="" text="">
          No hay categorías que coincidan
        </Option>
      )}
    </Combobox>
  )
}

CategoryCombobox.propTypes = {
  value: PropTypes.string.isRequired,
  categories: PropTypes.arrayOf(categoryPropType).isRequired,
  onChange: PropTypes.func.isRequired,
  // Si se indica, la primera opción es «todas» (para los filtros) y equivale a no elegir ninguna.
  allLabel: PropTypes.string,
  placeholder: PropTypes.string,
}
