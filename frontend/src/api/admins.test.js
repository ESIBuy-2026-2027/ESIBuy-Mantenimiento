import { describe, it, expect } from 'vitest'
import { http, HttpResponse } from 'msw'
import { server } from '../mocks/server'
import { createAdmin } from './admins'
import { ApiError } from './client'

const DATOS = {
  nombre: 'Nora',
  apellidos: 'Díaz Ruiz',
  email: 'nora@esibuy.test',
  sede: '',
  avatar: '',
  password: 'Password123!',
}

describe('createAdmin', () => {
  it('createAdmin_enviaLosDatosPorPostYDevuelveElAdministradorCreado', async () => {
    const peticion = {}
    server.use(
      http.post('/api/admin/admins', async ({ request }) => {
        peticion.metodo = request.method
        peticion.cuerpo = await request.json()
        return HttpResponse.json({ id: 'adm-1', email: DATOS.email }, { status: 201 })
      }),
    )

    const respuesta = await createAdmin(DATOS)

    expect(peticion.metodo).toBe('POST')
    expect(peticion.cuerpo).toEqual(DATOS)
    expect(respuesta).toEqual({ id: 'adm-1', email: DATOS.email })
  })

  it('createAdmin_conEmailRepetido_lanzaApiErrorConEstado409YSuCodigo', async () => {
    server.use(
      http.post('/api/admin/admins', () =>
        HttpResponse.json({ code: 'EMAIL_ALREADY_REGISTERED' }, { status: 409 }),
      ),
    )

    const error = await createAdmin(DATOS).catch((excepcion) => excepcion)

    expect(error).toBeInstanceOf(ApiError)
    expect(error.status).toBe(409)
    expect(error.code).toBe('EMAIL_ALREADY_REGISTERED')
  })
})
