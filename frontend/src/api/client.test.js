import { afterEach, describe, it, expect } from 'vitest'
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

  // Protección CSRF (T230, D4): el backend deja el token en la cookie XSRF-TOKEN y el cliente lo
  // devuelve en la cabecera X-XSRF-TOKEN en las peticiones que cambian datos.
  describe('token CSRF', () => {
    afterEach(() => {
      document.cookie = 'XSRF-TOKEN=; expires=Thu, 01 Jan 1970 00:00:00 GMT; path=/'
    })

    const capturarCabeceraCsrf = () => {
      const capturado = {}
      server.use(
        http.post('/api/auth/login', ({ request: req }) => {
          capturado.cabecera = req.headers.get('X-XSRF-TOKEN')
          return HttpResponse.json({ nextStep: 'emailCode' })
        }),
      )
      return capturado
    }

    it('request_postConCookieCsrf_enviaElTokenEnLaCabecera', async () => {
      document.cookie = 'XSRF-TOKEN=token-de-prueba; path=/'
      const capturado = capturarCabeceraCsrf()

      await request('/api/auth/login', { method: 'POST', body: { email: 'a@b.es' } })

      expect(capturado.cabecera).toBe('token-de-prueba')
    })

    it('request_postSinCookieCsrf_noEnviaLaCabecera', async () => {
      const capturado = capturarCabeceraCsrf()

      await request('/api/auth/login', { method: 'POST', body: { email: 'a@b.es' } })

      expect(capturado.cabecera).toBeNull()
    })
  })

  it('request_conError_esInstanciaDeApiError', async () => {
    server.use(http.get('/api/x', () => new HttpResponse(null, { status: 403 })))

    await expect(request('/api/x')).rejects.toBeInstanceOf(ApiError)
  })
})
