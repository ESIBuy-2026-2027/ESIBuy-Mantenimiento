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

export function verifyCaptcha(token) {
  return request('/api/auth/verify-captcha', { method: 'POST', body: { token } })
}

export function logout() {
  return request('/api/auth/logout', { method: 'POST' })
}

export function registrarCliente(datos) {
  return request('/api/auth/register/cliente', { method: 'POST', body: datos })
}