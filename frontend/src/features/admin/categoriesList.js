// Opciones de ordenación del listado de categorías.
export const CATEGORY_SORT_OPTIONS = [
  { value: 'name', label: 'Nombre' },
  { value: 'products', label: 'Productos' },
  { value: 'status', label: 'Estado' },
]

const normalize = (text) => text.normalize('NFD').replace(/\p{Diacritic}/gu, '').toLowerCase()

const byName = (a, b) => a.nombre.localeCompare(b.nombre, 'es', { sensitivity: 'base' })

// Las activas van antes que las inactivas; a igualdad, por nombre.
const STATUS_RANK = { ACTIVA: 0, INACTIVA: 1 }
const byStatus = (a, b) => (STATUS_RANK[a.estado] ?? 2) - (STATUS_RANK[b.estado] ?? 2) || byName(a, b)

const byProducts = (a, b) => a.numeroProductos - b.numeroProductos || byName(a, b)

const COMPARATORS = { name: byName, products: byProducts, status: byStatus }

// El backend devuelve todas las categorías sin paginar y ordenadas por nombre, así que la búsqueda por nombre
// (sin distinguir mayúsculas ni tildes), el filtro por estado y la ordenación se hacen aquí, en el cliente.
export function filterAndSortCategories(categories, { search, status, sort, order }) {
  const query = normalize(search.trim())
  const comparator = COMPARATORS[sort] ?? byName
  const direction = order === 'desc' ? -1 : 1

  return categories
    .filter((category) => !status || category.estado === status)
    .filter((category) => !query || normalize(category.nombre).includes(query))
    .sort((a, b) => direction * comparator(a, b))
}
