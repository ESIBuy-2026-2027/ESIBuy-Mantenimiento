import { describe, it, expect } from 'vitest'
import { http, HttpResponse } from 'msw'
import { server } from '../mocks/server'
import { listUsers } from './users'

const RESPUESTA = { content: [], page: 1, size: 10, totalElements: 0 }

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

    await listUsers({ search: 'ana', page: 0, size: 10 })
    expect(peticion.url.searchParams.get('busqueda')).toBe('ana')

    await listUsers({ search: '   ', page: 0, size: 10 })
    expect(peticion.url.searchParams.has('busqueda')).toBe(false)
  })

  it('HU19-CP03: envía el rol cuando se filtra por rol', () => {
    // TODO ciclo 3
    expect.fail('Funcionalidad no implementada')
  })

  it('HU19-CP04: envía el estado cuando se filtra por estado', () => {
    // TODO ciclo 4
    expect.fail('Funcionalidad no implementada')
  })

  it('HU19-CP08: propaga un ApiError si la respuesta no es correcta', () => {
    // TODO ciclo 5
    expect.fail('Funcionalidad no implementada')
  })
})
