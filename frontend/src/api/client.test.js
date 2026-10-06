import { describe, it, expect } from 'vitest'
import { http, HttpResponse } from 'msw'
import { server } from '../mocks/server'
import { ApiError, request } from './client'

describe('request', () => {
  it('request_conRespuestaCorrecta_devuelveElJson', async () => {
    const categorias = await request('/api/categorias')

    expect(categorias).toHaveLength(2)
    expect(categorias[0]).toHaveProperty('nombre')
  })

  it('request_sinSesion_lanzaApiErrorConEstado401', async () => {
    await expect(request('/api/auth/me')).rejects.toMatchObject({
      name: 'ApiError',
      status: 401,
    })
  })

  it('request_conRespuesta204_devuelveNull', async () => {
    server.use(http.post('/api/auth/logout', () => new HttpResponse(null, { status: 204 })))

    const resultado = await request('/api/auth/logout', { method: 'POST' })

    expect(resultado).toBeNull()
  })

  it('request_conBody_enviaJsonYCookies', async () => {
    let recibido
    server.use(
      http.post('/api/auth/login', async ({ request: req }) => {
        recibido = { contentType: req.headers.get('content-type'), body: await req.json(), credentials: req.credentials }
        return HttpResponse.json({ siguientePaso: 'totp' })
      }),
    )

    await request('/api/auth/login', { method: 'POST', body: { email: 'a@b.es' } })

    expect(recibido.contentType).toBe('application/json')
    expect(recibido.body).toEqual({ email: 'a@b.es' })
    expect(recibido.credentials).toBe('include')
  })

  it('request_conError_esInstanciaDeApiError', async () => {
    server.use(http.get('/api/x', () => new HttpResponse(null, { status: 403 })))

    await expect(request('/api/x')).rejects.toBeInstanceOf(ApiError)
  })
})
