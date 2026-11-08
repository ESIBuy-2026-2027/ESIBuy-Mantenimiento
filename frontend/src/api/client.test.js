import { afterEach, describe, it, expect, vi } from 'vitest'
import { http, HttpResponse } from 'msw'
import { server } from '../mocks/server'
import { ApiError, onUnauthorized, request } from './client'

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

  // Fallos de la demo del Sprint 1: el backend responde al registro con un 201 sin cuerpo
  it('HU1-CP26: una respuesta 201 sin cuerpo devuelve null', async () => {
    server.use(http.post('/api/auth/register/cliente', () => new HttpResponse(null, { status: 201 })))

    const resultado = await request('/api/auth/register/cliente', { method: 'POST', body: { email: 'a@b.es' } })

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

  // Sesión caducada (HU43): el servidor responde 401 y quien escucha (AuthProvider) cierra la sesión del navegador.
  describe('aviso de 401', () => {
    it('HU43-CP07: avisa a quien escucha cuando una petición recibe 401', async () => {
      const aviso = vi.fn()
      const dejarDeEscuchar = onUnauthorized(aviso)

      await expect(request('/api/auth/me')).rejects.toMatchObject({ status: 401 })

      expect(aviso).toHaveBeenCalledTimes(1)
      dejarDeEscuchar()
    })

    it('HU43-CP07: no avisa con otros errores ni después de dejar de escuchar', async () => {
      const aviso = vi.fn()
      const dejarDeEscuchar = onUnauthorized(aviso)
      server.use(http.get('/api/categorias', () => new HttpResponse(null, { status: 500 })))

      await expect(request('/api/categorias')).rejects.toMatchObject({ status: 500 })
      dejarDeEscuchar()
      await expect(request('/api/auth/me')).rejects.toMatchObject({ status: 401 })

      expect(aviso).not.toHaveBeenCalled()
    })
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

    // Primer login tras abrir la página con el backend dormido (Render): la petición inicial a /api/auth/me no llegó a
    // dejar la cookie, el POST salía sin token, el backend respondía 403 y el login decía que los datos estaban mal.
    describe('sin cookie CSRF', () => {
      const servirTokenEnMe = () => {
        const llamadas = { me: 0 }
        server.use(
          http.get('/api/auth/me', () => {
            llamadas.me += 1
            document.cookie = 'XSRF-TOKEN=token-nuevo; path=/'
            return new HttpResponse(null, { status: 401 })
          }),
        )
        return llamadas
      }

      it('HU2-CP32: pide el token antes del POST y lo envía en la cabecera', async () => {
        const llamadas = servirTokenEnMe()
        const capturado = capturarCabeceraCsrf()

        await request('/api/auth/login', { method: 'POST', body: { email: 'a@b.es' } })

        expect(llamadas.me).toBe(1)
        expect(capturado.cabecera).toBe('token-nuevo')
      })

      it('HU2-CP32: el 401 de la petición del token no avisa de sesión caducada', async () => {
        servirTokenEnMe()
        capturarCabeceraCsrf()
        const aviso = vi.fn()
        const dejarDeEscuchar = onUnauthorized(aviso)

        await request('/api/auth/login', { method: 'POST', body: { email: 'a@b.es' } })

        expect(aviso).not.toHaveBeenCalled()
        dejarDeEscuchar()
      })

      it('HU2-CP32: con la cookie ya puesta, no vuelve a pedir el token', async () => {
        document.cookie = 'XSRF-TOKEN=token-de-prueba; path=/'
        const llamadas = servirTokenEnMe()
        capturarCabeceraCsrf()

        await request('/api/auth/login', { method: 'POST', body: { email: 'a@b.es' } })

        expect(llamadas.me).toBe(0)
      })
    })
  })

  it('request_conError_esInstanciaDeApiError', async () => {
    server.use(http.get('/api/x', () => new HttpResponse(null, { status: 403 })))

    await expect(request('/api/x')).rejects.toBeInstanceOf(ApiError)
  })
})
