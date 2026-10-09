import { http, HttpResponse } from 'msw'
import { getUsuarioEnSesion } from './authHandlers'

const TAMANO_PAGINA_POR_DEFECTO = 10

// [nombre, apellidos, rol, estado, fechaAlta]. 25 usuarios (3 páginas de 10). La búsqueda "ana" coincide
// solo con Ana López García y Mariana Ruiz Pascual; ningún otro nombre, apellido o email contiene "ana".
// Los clientes solo pueden estar activos o desactivados: no necesitan que un administrador los acepte.
const USUARIOS_INICIALES = [
  ['Ana', 'López García', 'ADMINISTRADOR', 'ACTIVO', '2026-01-12T09:15:00'],
  ['Mariana', 'Ruiz Pascual', 'VENDEDOR', 'PENDIENTE', '2026-10-01T10:00:00'],
  ['Carlos', 'Moreno Gil', 'CLIENTE', 'ACTIVO', '2026-02-03T11:30:00'],
  ['Lucía', 'Torres Vega', 'CLIENTE', 'DESACTIVADO', '2026-09-28T17:45:00'],
  ['Javier', 'Romero Díaz', 'VENDEDOR', 'ACTIVO', '2026-03-14T08:20:00'],
  ['Elena', 'Muñoz Alonso', 'CLIENTE', 'DESACTIVADO', '2026-04-02T12:00:00'],
  ['Pablo', 'Gómez Martín', 'CLIENTE', 'ACTIVO', '2026-04-18T16:10:00'],
  ['Marta', 'Sanz Ramos', 'VENDEDOR', 'DESACTIVADO', '2026-05-06T10:40:00'],
  ['Sergio', 'Castro Ortega', 'CLIENTE', 'ACTIVO', '2026-05-21T19:05:00'],
  ['Laura', 'Rubio Molina', 'CLIENTE', 'DESACTIVADO', '2026-09-30T09:00:00'],
  ['Diego', 'Delgado Iglesias', 'ADMINISTRADOR', 'DESACTIVADO', '2026-01-20T13:25:00'],
  ['Paula', 'Medina Cruz', 'CLIENTE', 'ACTIVO', '2026-06-09T15:50:00'],
  ['Hugo', 'Herrera Blanco', 'VENDEDOR', 'BLOQUEADO', '2026-06-25T11:15:00'],
  ['Irene', 'Prieto Vidal', 'CLIENTE', 'ACTIVO', '2026-07-03T18:30:00'],
  ['Álvaro', 'Campos Serrano', 'CLIENTE', 'DESACTIVADO', '2026-07-11T08:45:00'],
  ['Nuria', 'Navarro Lozano', 'VENDEDOR', 'ACTIVO', '2026-07-19T14:20:00'],
  ['Raúl', 'Ortiz Fuentes', 'CLIENTE', 'ACTIVO', '2026-07-27T10:05:00'],
  ['Sofía', 'Cabrera Soler', 'CLIENTE', 'DESACTIVADO', '2026-09-15T12:35:00'],
  ['Iván', 'Morales Reyes', 'VENDEDOR', 'ACTIVO', '2026-08-04T09:55:00'],
  ['Claudia', 'Peña Calvo', 'CLIENTE', 'ACTIVO', '2026-08-12T17:00:00'],
  ['Óscar', 'León Garrido', 'CLIENTE', 'DESACTIVADO', '2026-08-20T13:40:00'],
  ['Beatriz', 'Vargas Méndez', 'VENDEDOR', 'PENDIENTE', '2026-09-05T11:10:00'],
  ['Rubén', 'Cortés Pastor', 'CLIENTE', 'ACTIVO', '2026-08-28T16:25:00'],
  ['Jorge', 'Benítez Sáez', 'CLIENTE', 'DESACTIVADO', '2026-09-10T15:15:00'],
]
  .map(([nombre, apellidos, rol, estado, fechaAlta], indice) => ({
    id: `usr-${indice + 1}`,
    nombre,
    apellidos,
    email: `${normalizar(nombre)}.${normalizar(apellidos.split(' ')[0])}@esibuy.test`,
    rol,
    estado,
    fechaAlta,
  }))
  // El administrador con el que se inicia sesión en el mock (mismo id que en authHandlers), para que
  // la regla "un administrador no puede desactivarse a sí mismo" (HU23) se vea en la lista.
  .concat({
    id: 'u-1',
    nombre: 'Ada',
    apellidos: 'Admin',
    email: 'admin@esibuy.test',
    rol: 'ADMINISTRADOR',
    estado: 'ACTIVO',
    fechaAlta: '2026-02-27T10:30:00',
  })
  .sort((a, b) => b.fechaAlta.localeCompare(a.fechaAlta))

// Lista que puede crecer durante la sesión (p. ej. con los administradores dados de alta en la HU21).
let usuarios = [...USUARIOS_INICIALES]

export function resetUsersMock() {
  usuarios = [...USUARIOS_INICIALES]
}

export const existeEmailDeUsuario = (email) => usuarios.some((usuario) => usuario.email === email)

// Añade un usuario al principio: el listado va del alta más reciente a la más antigua.
export function agregarUsuario(usuario) {
  usuarios = [usuario, ...usuarios]
}

function normalizar(texto) {
  return texto
    .normalize('NFD')
    .replace(/\p{Diacritic}/gu, '')
    .toLowerCase()
}

// Con searchBy=name (listados de clientes y administradores) no se busca en el email.
const coincideBusqueda = (usuario, busqueda, soloNombre) =>
  [usuario.nombre, usuario.apellidos, ...(soloNombre ? [] : [usuario.email])].some((campo) =>
    normalizar(campo).includes(busqueda),
  )

const ORDEN_ESTADOS = ['PENDIENTE', 'ACTIVO', 'BLOQUEADO', 'DESACTIVADO']
const nombreCompleto = (usuario) => `${usuario.nombre} ${usuario.apellidos}`

const COMPARADORES = new Map([
  ['name', (a, b) => nombreCompleto(a).localeCompare(nombreCompleto(b), 'es')],
  [
    'status',
    (a, b) =>
      ORDEN_ESTADOS.indexOf(a.estado) - ORDEN_ESTADOS.indexOf(b.estado) ||
      nombreCompleto(a).localeCompare(nombreCompleto(b), 'es'),
  ],
])

// Sin sort se mantiene el orden por fecha de alta, de la más reciente a la más antigua (HU19).
function ordenar(lista, criterio, sentido) {
  const comparar = COMPARADORES.get(criterio)
  if (!comparar) return lista
  const signo = sentido === 'desc' ? -1 : 1
  return [...lista].sort((a, b) => signo * comparar(a, b))
}

const ESTADOS_ADMITIDOS = new Set(['ACTIVO', 'DESACTIVADO'])

const esAdminActivo = (usuario) => usuario.rol === 'ADMINISTRADOR' && usuario.estado === 'ACTIVO'

// Código de error si la cuenta no se puede desactivar, o null si se puede.
function conflictoAlDesactivar(usuario) {
  if (usuario.id === getUsuarioEnSesion()?.id) return 'CANNOT_DEACTIVATE_SELF'
  if (esAdminActivo(usuario) && usuarios.filter(esAdminActivo).length === 1) return 'LAST_ACTIVE_ADMIN'
  return null
}

// Devuelve la respuesta de error si quien pregunta no es administrador, o null si puede continuar.
function rechazarSiNoEsAdmin() {
  const usuario = getUsuarioEnSesion()
  if (!usuario) return new HttpResponse(null, { status: 401 })
  if (usuario.rol !== 'ADMINISTRADOR') return new HttpResponse(null, { status: 403 })
  return null
}

export const usersHandlers = [
  http.get('/api/admin/usuarios', ({ request }) => {
    const rechazo = rechazarSiNoEsAdmin()
    if (rechazo) return rechazo

    const params = new URL(request.url).searchParams
    const busqueda = normalizar(params.get('search') ?? '')
    const rol = params.get('role')
    const estado = params.get('status')
    const soloNombre = params.get('searchBy') === 'name'
    const pagina = Math.max(1, Number(params.get('page')) || 1)
    const tamano = Math.max(1, Number(params.get('size')) || TAMANO_PAGINA_POR_DEFECTO)

    const coincidentes = ordenar(
      usuarios.filter(
        (candidato) =>
          coincideBusqueda(candidato, busqueda, soloNombre) &&
          (!rol || candidato.rol === rol) &&
          (!estado || candidato.estado === estado),
      ),
      params.get('sort'),
      params.get('order'),
    )
    const inicio = (pagina - 1) * tamano

    return HttpResponse.json({
      items: coincidentes.slice(inicio, inicio + tamano),
      page: pagina,
      size: tamano,
      totalItems: coincidentes.length,
      totalPages: Math.ceil(coincidentes.length / tamano),
    })
  }),

  // Activar (HU24) o desactivar (HU23) una cuenta. Un administrador no puede desactivarse a sí mismo
  // ni desactivar al último administrador activo.
  http.patch('/api/admin/usuarios/:id/status', async ({ params, request }) => {
    const rechazo = rechazarSiNoEsAdmin()
    if (rechazo) return rechazo

    const { estado } = await request.json()
    if (!ESTADOS_ADMITIDOS.has(estado)) return new HttpResponse(null, { status: 400 })

    const existente = usuarios.find((candidato) => candidato.id === params.id)
    if (!existente) return new HttpResponse(null, { status: 404 })

    if (estado === 'DESACTIVADO') {
      const conflicto = conflictoAlDesactivar(existente)
      if (conflicto) return HttpResponse.json({ code: conflicto }, { status: 409 })
    }

    const actualizado = { ...existente, estado }
    usuarios = usuarios.map((candidato) => (candidato.id === params.id ? actualizado : candidato))
    return HttpResponse.json(actualizado)
  }),
]
