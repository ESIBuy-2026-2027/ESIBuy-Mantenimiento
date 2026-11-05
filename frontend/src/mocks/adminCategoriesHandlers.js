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

// Lista que puede cambiar durante la sesión: el administrador crea (HU40), edita (HU41) y elimina (HU42) categorías.
let categorias = [...CATEGORIAS_INICIALES]
let siguienteId = 11

export function resetAdminCategoriesMock() {
  categorias = [...CATEGORIAS_INICIALES]
  siguienteId = 11
}

// Como el backend: el nombre no puede repetirse sin distinguir mayúsculas ni tildes.
const normalizar = (texto) =>
  texto
    .trim()
    .normalize('NFD')
    .replaceAll(/\p{M}/gu, '')
    .toLowerCase()
// `excepto`: al editar, la propia categoría no cuenta como repetida.
const nombreRepetido = (nombre, excepto = null) =>
  categorias.some((categoria) => categoria.id !== excepto && normalizar(categoria.nombre) === normalizar(nombre))
const ordenarPorNombre = (lista) => [...lista].sort((a, b) => a.nombre.localeCompare(b.nombre, 'es'))

function rechazarSiNoEsAdmin() {
  const usuario = getUsuarioEnSesion()
  if (!usuario) return new HttpResponse(null, { status: 401 })
  if (usuario.rol !== 'ADMINISTRADOR') return HttpResponse.json({ code: 'ACCESS_DENIED' }, { status: 403 })
  return null
}

export const adminCategoriesHandlers = [
  // Solo responde al administrador: 401 sin sesión y 403 ACCESS_DENIED con otro rol, como el backend (HU39).
  http.get('/api/admin/categorias', () => rechazarSiNoEsAdmin() ?? HttpResponse.json(categorias)),

  // Como el backend (HU40): 201 con la fila nueva, sin productos; 400 sin nombre y 409 si el nombre ya existe.
  http.post('/api/admin/categorias', async ({ request }) => {
    const rechazo = rechazarSiNoEsAdmin()
    if (rechazo) return rechazo

    const { nombre, descripcion, estado } = await request.json()
    if (!nombre?.trim()) return HttpResponse.json({ code: 'VALIDATION_ERROR' }, { status: 400 })
    if (nombreRepetido(nombre)) return HttpResponse.json({ code: 'CATEGORY_NAME_ALREADY_EXISTS' }, { status: 409 })

    const creada = {
      id: `cat-${siguienteId++}`,
      nombre: nombre.trim(),
      descripcion: descripcion ?? '',
      estado,
      numeroProductos: 0,
    }
    categorias = ordenarPorNombre([...categorias, creada])
    return HttpResponse.json(creada, { status: 201 })
  }),

  // Como el backend (HU41): la fila actualizada, sin cambiar el estado; 400 sin nombre, 404 si no existe y 409 si el
  // nombre es el de otra.
  http.put('/api/admin/categorias/:id', async ({ request, params }) => {
    const rechazo = rechazarSiNoEsAdmin()
    if (rechazo) return rechazo

    const existente = categorias.find((categoria) => categoria.id === params.id)
    if (!existente) return HttpResponse.json({ code: 'CATEGORY_NOT_FOUND' }, { status: 404 })
    const { nombre, descripcion } = await request.json()
    if (!nombre?.trim()) return HttpResponse.json({ code: 'VALIDATION_ERROR' }, { status: 400 })
    if (nombreRepetido(nombre, params.id)) {
      return HttpResponse.json({ code: 'CATEGORY_NAME_ALREADY_EXISTS' }, { status: 409 })
    }

    const editada = { ...existente, nombre: nombre.trim(), descripcion: descripcion ?? '' }
    categorias = ordenarPorNombre(categorias.map((categoria) => (categoria.id === params.id ? editada : categoria)))
    return HttpResponse.json(editada)
  }),

  // Activar o desactivar (HU41): 404 si no existe y 400 si el estado no es ACTIVA ni INACTIVA.
  http.patch('/api/admin/categorias/:id/status', async ({ request, params }) => {
    const rechazo = rechazarSiNoEsAdmin()
    if (rechazo) return rechazo

    const existente = categorias.find((categoria) => categoria.id === params.id)
    if (!existente) return HttpResponse.json({ code: 'CATEGORY_NOT_FOUND' }, { status: 404 })
    const { estado } = await request.json()
    if (estado !== 'ACTIVA' && estado !== 'INACTIVA') {
      return HttpResponse.json({ code: 'VALIDATION_ERROR' }, { status: 400 })
    }

    const cambiada = { ...existente, estado }
    categorias = categorias.map((categoria) => (categoria.id === params.id ? cambiada : categoria))
    return HttpResponse.json(cambiada)
  }),

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
