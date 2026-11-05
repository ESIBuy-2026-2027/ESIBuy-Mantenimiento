import { request } from './client'

export function getCurrentUser() {
  return request('/api/auth/me')
}

export function login({ email, password }) {
  return request('/api/auth/login', { method: 'POST', body: { email, password } })
}

export function verifyEmailCode(code) {
  return request('/api/auth/verify-email-code', { method: 'POST', body: { code } })
}

// Alta de Microsoft Authenticator (T20): devuelve { qr } con la imagen del QR para escanear
export function enrollAuthenticator() {
  return request('/api/auth/enroll-authenticator', { method: 'POST' })
}

export function verifyAuthenticatorCode(code) {
  return request('/api/auth/verify-authenticator-code', { method: 'POST', body: { code } })
}

export function logout() {
  return request('/api/auth/logout', { method: 'POST' })
}

// Recuperación de contraseña (HU3, backend en T399). Pedir el código responde siempre 204, exista o no la cuenta.
export function requestPasswordRecovery(email) {
  return request('/api/auth/password-recovery', { method: 'POST', body: { email } })
}

// 204 si se cambia; 401 si el código no sirve; 400 con el code del motivo si la contraseña no cumple la política
export function resetPassword({ email, code, password }) {
  return request('/api/auth/password-recovery/confirm', { method: 'POST', body: { email, code, password } })
}

export function registrarCliente(datos) {
  return request('/api/auth/register/cliente', { method: 'POST', body: datos })
}

export function registrarVendedor(datos) {
  return request('/api/auth/register/vendedor', { method: 'POST', body: datos })
}