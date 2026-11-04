import { http, HttpResponse } from 'msw'
import { getUsuarioEnSesion } from './authHandlers'

// [id, nombre, descripción, estado, número de productos]. Ordenadas por nombre, como las devuelve el backend, e
// incluyen las inactivas, alguna sin descripción y alguna sin productos.
const CATEGORIAS_INICIALES = [
  ['cat-5', 'Alimentación', 'Comida y bebida envasadas.', 'ACTIVA', 6],
  ['cat-6', 'Deportes', 'Material y ropa deportiva.', 'ACTIVA', 9],
  [
    'cat-2',
    'Hogar',
    'Muebles, decoración y menaje para dar vida a cualquier estancia. Incluye iluminación, textil del hogar y almacenamiento.',
    'ACTIVA',
    14,
  ],
  ['cat-7', 'Juguetes', 'Juegos y juguetes para todas las edades.', 'INACTIVA', 3],
  ['cat-8', 'Libros', '', 'ACTIVA', 11],
  ['cat-9', 'Mascotas', 'Alimento y accesorios para perros, gatos y otras mascotas.', 'ACTIVA', 0],
  ['cat-10', 'Moda', '', 'INACTIVA', 0],
  ['cat-1', 'Tecnología', 'Ordenadores, móviles y accesorios.', 'ACTIVA', 18],
].map(([id, nombre, descripcion, estado, numeroProductos]) => ({
  id,
  nombre,
  descripcion,
  estado,
  numeroProductos,
}))

// Lista que puede cambiar durante la sesión: el administrador elimina categorías (HU42).
let categorias = [...CATEGORIAS_INICIALES]

export function resetAdminCategoriesMock() {
  categorias = [...CATEGORIAS_INICIALES]
}

function rechazarSiNoEsAdmin() {
  const usuario = getUsuarioEnSesion()
  if (!usuario) return new HttpResponse(null, { status: 401 })
  if (usuario.rol !== 'ADMINISTRADOR') return HttpResponse.json({ code: 'ACCESS_DENIED' }, { status: 403 })
  return null
}

export const adminCategoriesHandlers = [
  // Solo responde al administrador: 401 sin sesión y 403 ACCESS_DENIED con otro rol, como el backend (HU39).
  http.get('/api/admin/categorias', () => rechazarSiNoEsAdmin() ?? HttpResponse.json(categorias)),

  // Como el backend (HU42): 404 si no existe y 409 si tiene productos. El 409 CATEGORY_HAS_SELLERS (es la principal
  // de algún vendedor) no se simula aquí: los tests que lo necesitan ponen su propio handler.
  http.delete('/api/admin/categorias/:id', ({ params }) => {
    const rechazo = rechazarSiNoEsAdmin()
    if (rechazo) return rechazo

    const existente = categorias.find((categoria) => categoria.id === params.id)
    if (!existente) return HttpResponse.json({ code: 'CATEGORY_NOT_FOUND' }, { status: 404 })
    if (existente.numeroProductos > 0) return HttpResponse.json({ code: 'CATEGORY_HAS_PRODUCTS' }, { status: 409 })

    categorias = categorias.filter((categoria) => categoria.id !== params.id)
    return new HttpResponse(null, { status: 204 })
  }),
]
