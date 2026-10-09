import { describe, it, expect } from 'vitest'
import { http, HttpResponse } from 'msw'
import { server } from '../mocks/server'
import { ApiError } from './client'
import { getUser, listUsers, updateUserStatus } from './users'

const RESPUESTA = { items: [], page: 1, size: 10, totalItems: 0, totalPages: 0 }

function capturarPeticion() {
  const peticion = {}
  server.use(
    http.get('/api/admin/usuarios', ({ request }) => {
      peticion.url = new URL(request.url)
      return HttpResponse.json(RESPUESTA)
    }),
  )
  return peticion
}

describe('listUsers', () => {
  it('HU19-CP05: pide la página y el tamaño indicados', async () => {
    const peticion = capturarPeticion()

    const respuesta = await listUsers({ page: 1, size: 10 })

    expect(peticion.url.searchParams.get('page')).toBe('1')
    expect(peticion.url.searchParams.get('size')).toBe('10')
    expect(respuesta).toEqual(RESPUESTA)
  })

  it('HU19-CP02: envía la búsqueda solo cuando tiene texto', async () => {
    const peticion = capturarPeticion()

    await listUsers({ search: 'ana', page: 1, size: 10 })
    expect(peticion.url.searchParams.get('search')).toBe('ana')

    await listUsers({ search: '   ', page: 1, size: 10 })
    expect(peticion.url.searchParams.has('search')).toBe(false)
  })

  it('HU19-CP03: envía el rol cuando se filtra por rol', async () => {
    const peticion = capturarPeticion()

    await listUsers({ role: 'VENDEDOR', page: 1, size: 10 })
    expect(peticion.url.searchParams.get('role')).toBe('VENDEDOR')

    await listUsers({ role: '', page: 1, size: 10 })
    expect(peticion.url.searchParams.has('role')).toBe(false)
  })

  it('HU19-CP04: envía el estado cuando se filtra por estado', async () => {
    const peticion = capturarPeticion()

    await listUsers({ status: 'PENDIENTE', page: 1, size: 10 })
    expect(peticion.url.searchParams.get('status')).toBe('PENDIENTE')

    await listUsers({ status: '', page: 1, size: 10 })
    expect(peticion.url.searchParams.has('status')).toBe(false)
  })

  it('HU19-CP08: propaga un ApiError si la respuesta no es correcta', async () => {
    server.use(
      http.get('/api/admin/usuarios', () => new HttpResponse(null, { status: 500 })),
    )

    const error = await listUsers({ page: 1, size: 10 }).catch((e) => e)

    expect(error).toBeInstanceOf(ApiError)
    expect(error.status).toBe(500)
  })

describe('updateUserStatus', () => {
  function capturarCambio() {
    const peticion = {}
    server.use(
      http.patch('/api/admin/usuarios/:id/status', async ({ params, request }) => {
        peticion.id = params.id
        peticion.metodo = request.method
        peticion.cuerpo = await request.json()
        return HttpResponse.json({ id: params.id, ...peticion.cuerpo })
      }),
    )
    return peticion
  }

  it('updateUserStatus_paraActivar_enviaActivoPorPatchALaRutaDelUsuario', async () => {
    const peticion = capturarCambio()

    const respuesta = await updateUserStatus('usr-4', 'ACTIVO')

    expect(peticion).toEqual({ id: 'usr-4', metodo: 'PATCH', cuerpo: { estado: 'ACTIVO' } })
    expect(respuesta).toEqual({ id: 'usr-4', estado: 'ACTIVO' })
  })

  it('updateUserStatus_paraDesactivar_enviaDesactivado', async () => {
    const peticion = capturarCambio()

    await updateUserStatus('usr-3', 'DESACTIVADO')

    expect(peticion.cuerpo).toEqual({ estado: 'DESACTIVADO' })
  })

  it('updateUserStatus_conConflicto_lanzaApiErrorConSuCodigo', async () => {
    server.use(
      http.patch('/api/admin/usuarios/:id/status', () =>
        HttpResponse.json({ code: 'CANNOT_DEACTIVATE_SELF' }, { status: 409 }),
      ),
    )

    const error = await updateUserStatus('u-1', 'DESACTIVADO').catch((excepcion) => excepcion)

    expect(error).toBeInstanceOf(ApiError)
    expect(error.status).toBe(409)
    expect(error.code).toBe('CANNOT_DEACTIVATE_SELF')
  })
})

describe('listUsers con búsqueda por nombre y ordenación (HU24)', () => {
  it('listUsers_conSearchBySortYOrder_losEnviaEnLaConsulta', async () => {
    const peticion = capturarPeticion()

    await listUsers({ search: 'ana', page: 1, size: 10, searchBy: 'name', sort: 'status', order: 'desc' })

    expect(peticion.url.searchParams.get('searchBy')).toBe('name')
    expect(peticion.url.searchParams.get('sort')).toBe('status')
    expect(peticion.url.searchParams.get('order')).toBe('desc')
  })

  it('listUsers_sinSearchBySortNiOrder_noLosEnvia', async () => {
    const peticion = capturarPeticion()

    await listUsers({ page: 1, size: 10 })

    expect(peticion.url.searchParams.has('searchBy')).toBe(false)
    expect(peticion.url.searchParams.has('sort')).toBe(false)
    expect(peticion.url.searchParams.has('order')).toBe(false)
  })
})
})

describe('getUser', () => {
  it('getUser_pideLaFichaALaRutaDelUsuario', async () => {
    let ruta = null
    server.use(
      http.get('/api/admin/usuarios/:id', ({ request, params }) => {
        ruta = new URL(request.url).pathname
        return HttpResponse.json({ id: params.id, nombre: 'Lucía' })
      }),
    )

    const usuario = await getUser('usr-4')

    expect(ruta).toBe('/api/admin/usuarios/usr-4')
    expect(usuario).toEqual({ id: 'usr-4', nombre: 'Lucía' })
  })

  it('getUser_conUsuarioInexistente_lanzaApiErrorConEstado404', async () => {
    server.use(http.get('/api/admin/usuarios/:id', () => new HttpResponse(null, { status: 404 })))

    const error = await getUser('no-existe').catch((excepcion) => excepcion)

    expect(error).toBeInstanceOf(ApiError)
    expect(error.status).toBe(404)
  })
})
