import { describe, it, expect } from 'vitest'
import { http, HttpResponse } from 'msw'
import { server } from '../mocks/server'
import { ApiError } from './client'
import { listUsers } from './users'

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
})
