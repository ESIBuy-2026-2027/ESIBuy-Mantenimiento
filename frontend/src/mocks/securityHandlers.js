
import { http, HttpResponse } from 'msw'
import { getUsuarioEnSesion } from './authHandlers'

// Preferencias de seguridad independientes para cada usuario de ejemplo.
const configuraciones = new Map()

export function resetSecurityMock() {
    configuraciones.clear()
}

const configuracionInicial = () => ({
    emailOtpEnabled: false,
    authenticatorEnabled: false,
    authenticatorConfigured: false,
    authenticatorVerifiedAt: null,
})

function rechazarSiNoHaySesion() {
    const usuario = getUsuarioEnSesion()
    if (!usuario) return new HttpResponse(null, { status: 401 })
    return null
}

export const securityHandlers = [
    http.get('/api/seguridad/factores', () => {
        const rechazo = rechazarSiNoHaySesion()
        if (rechazo) return rechazo

        const usuario = getUsuarioEnSesion()
        return HttpResponse.json(configuraciones.get(usuario.id) ?? configuracionInicial())
    }),

    http.patch('/api/seguridad/factores', async ({ request }) => {
        const rechazo = rechazarSiNoHaySesion()
        if (rechazo) return rechazo

        const usuario = getUsuarioEnSesion()
        const cuerpo = await request.json()
        const actual = configuraciones.get(usuario.id) ?? configuracionInicial()

        if (
            typeof cuerpo?.emailOtpEnabled !== 'boolean' ||
            typeof cuerpo?.authenticatorEnabled !== 'boolean'
        ) {
            return HttpResponse.json({ code: 'VALIDATION_ERROR' }, { status: 400 })
        }

        if (cuerpo.authenticatorEnabled && !actual.authenticatorConfigured) {
            return HttpResponse.json({ code: 'VALIDATION_ERROR' }, { status: 400 })
        }

        const actualizada = {
            ...actual,
            emailOtpEnabled: cuerpo.emailOtpEnabled,
            authenticatorEnabled: cuerpo.authenticatorEnabled,
        }

        configuraciones.set(usuario.id, actualizada)
        return HttpResponse.json(actualizada)
    }),
]
