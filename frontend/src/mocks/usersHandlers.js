import { http, HttpResponse } from 'msw'
import { getUsuarioEnSesion } from './authHandlers'

const TAMANO_PAGINA_POR_DEFECTO = 10

// [nombre, apellidos, rol, estado, fechaAlta]. 25 usuarios (3 páginas de 10). La búsqueda "ana" coincide
// solo con Ana López García y Mariana Ruiz Pascual; ningún otro nombre, apellido o email contiene "ana".
const USUARIOS = [
  ['Ana', 'López García', 'ADMINISTRADOR', 'ACTIVO', '2026-01-12T09:15:00'],
  ['Mariana', 'Ruiz Pascual', 'VENDEDOR', 'PENDIENTE', '2026-10-01T10:00:00'],
  ['Carlos', 'Moreno Gil', 'CLIENTE', 'ACTIVO', '2026-02-03T11:30:00'],
  ['Lucía', 'Torres Vega', 'CLIENTE', 'PENDIENTE', '2026-09-28T17:45:00'],
  ['Javier', 'Romero Díaz', 'VENDEDOR', 'ACTIVO', '2026-03-14T08:20:00'],
  ['Elena', 'Muñoz Alonso', 'CLIENTE', 'BLOQUEADO', '2026-04-02T12:00:00'],
  ['Pablo', 'Gómez Martín', 'CLIENTE', 'ACTIVO', '2026-04-18T16:10:00'],
  ['Marta', 'Sanz Ramos', 'VENDEDOR', 'DESACTIVADO', '2026-05-06T10:40:00'],
  ['Sergio', 'Castro Ortega', 'CLIENTE', 'ACTIVO', '2026-05-21T19:05:00'],
  ['Laura', 'Rubio Molina', 'CLIENTE', 'PENDIENTE', '2026-09-30T09:00:00'],
  ['Diego', 'Delgado Iglesias', 'ADMINISTRADOR', 'ACTIVO', '2026-01-20T13:25:00'],
  ['Paula', 'Medina Cruz', 'CLIENTE', 'ACTIVO', '2026-06-09T15:50:00'],
  ['Hugo', 'Herrera Blanco', 'VENDEDOR', 'BLOQUEADO', '2026-06-25T11:15:00'],
  ['Irene', 'Prieto Vidal', 'CLIENTE', 'ACTIVO', '2026-07-03T18:30:00'],
  ['Álvaro', 'Campos Serrano', 'CLIENTE', 'DESACTIVADO', '2026-07-11T08:45:00'],
  ['Nuria', 'Navarro Lozano', 'VENDEDOR', 'ACTIVO', '2026-07-19T14:20:00'],
  ['Raúl', 'Ortiz Fuentes', 'CLIENTE', 'ACTIVO', '2026-07-27T10:05:00'],
  ['Sofía', 'Cabrera Soler', 'CLIENTE', 'PENDIENTE', '2026-09-15T12:35:00'],
  ['Iván', 'Morales Reyes', 'VENDEDOR', 'ACTIVO', '2026-08-04T09:55:00'],
  ['Claudia', 'Peña Calvo', 'CLIENTE', 'ACTIVO', '2026-08-12T17:00:00'],
  ['Óscar', 'León Garrido', 'CLIENTE', 'BLOQUEADO', '2026-08-20T13:40:00'],
  ['Beatriz', 'Vargas Méndez', 'VENDEDOR', 'PENDIENTE', '2026-09-05T11:10:00'],
  ['Rubén', 'Cortés Pastor', 'CLIENTE', 'ACTIVO', '2026-08-28T16:25:00'],
  ['Carmen', 'Hidalgo Rojas', 'ADMINISTRADOR', 'ACTIVO', '2026-02-27T10:30:00'],
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
  .sort((a, b) => b.fechaAlta.localeCompare(a.fechaAlta))

function normalizar(texto) {
  return texto
    .normalize('NFD')
    .replace(/\p{Diacritic}/gu, '')
    .toLowerCase()
}

const coincideBusqueda = (usuario, busqueda) =>
  [usuario.nombre, usuario.apellidos, usuario.email].some((campo) => normalizar(campo).includes(busqueda))

export const usersHandlers = [
  http.get('/api/admin/usuarios', ({ request }) => {
    const usuario = getUsuarioEnSesion()
    if (!usuario) return new HttpResponse(null, { status: 401 })
    if (usuario.rol !== 'ADMINISTRADOR') return new HttpResponse(null, { status: 403 })

    const params = new URL(request.url).searchParams
    const busqueda = normalizar(params.get('search') ?? '')
    const rol = params.get('role')
    const estado = params.get('status')
    const pagina = Math.max(1, Number(params.get('page')) || 1)
    const tamano = Math.max(1, Number(params.get('size')) || TAMANO_PAGINA_POR_DEFECTO)

    const coincidentes = USUARIOS.filter(
      (candidato) =>
        coincideBusqueda(candidato, busqueda) &&
        (!rol || candidato.rol === rol) &&
        (!estado || candidato.estado === estado),
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
]
