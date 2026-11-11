
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
