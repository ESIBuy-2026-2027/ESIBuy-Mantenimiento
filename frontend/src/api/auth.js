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

export function verifyAuthenticatorCode(code) {
  return request('/api/auth/verify-authenticator-code', { method: 'POST', body: { code } })
}

export function logout() {
  return request('/api/auth/logout', { method: 'POST' })
}

export function registrarCliente(datos) {
  return request('/api/auth/register/cliente', { method: 'POST', body: datos })
}

export function registrarVendedor(datos) {
  return request('/api/auth/register/vendedor', { method: 'POST', body: datos })
}