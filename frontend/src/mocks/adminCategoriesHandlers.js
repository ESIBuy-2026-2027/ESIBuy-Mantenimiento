import { http, HttpResponse } from 'msw'
import { getUsuarioEnSesion } from './authHandlers'

// [id, nombre, descripción, estado, número de productos]. Ordenadas por nombre, como las devuelve el backend, e
// incluyen las inactivas, alguna sin descripción y alguna sin productos.
const CATEGORIAS_ADMIN = [
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

export const adminCategoriesHandlers = [
  // Solo responde al administrador: 401 sin sesión y 403 ACCESS_DENIED con otro rol, como el backend (HU39).
  http.get('/api/admin/categorias', () => {
    const usuario = getUsuarioEnSesion()
    if (!usuario) return new HttpResponse(null, { status: 401 })
    if (usuario.rol !== 'ADMINISTRADOR') {
      return HttpResponse.json({ code: 'ACCESS_DENIED' }, { status: 403 })
    }
    return HttpResponse.json(CATEGORIAS_ADMIN)
  }),
]
