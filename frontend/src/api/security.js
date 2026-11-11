import { request } from './client'

export function getSecurityFactors() {
    return request('/api/seguridad/factores')
}

export function updateSecurityFactors({ emailOtpEnabled, authenticatorEnabled }) {
    return request('/api/seguridad/factores', {
        method: 'PATCH',
        body: { emailOtpEnabled, authenticatorEnabled },
    })
}

// El alta desde el perfil usa rutas autenticadas; no depende del flujo de login.
export function enrollSecurityAuthenticator() {
    return request('/api/seguridad/factores/authenticator/enroll', { method: 'POST' })
}

export function verifySecurityAuthenticator(code) {
    return request('/api/seguridad/factores/authenticator/verify', {
        method: 'POST',
        body: { code },
    })
}
