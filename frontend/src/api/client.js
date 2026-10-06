const BASE_URL = import.meta.env.VITE_API_URL ?? ''

export class ApiError extends Error {
  constructor(status, message) {
    super(message)
    this.name = 'ApiError'
    this.status = status
  }
}

export async function request(path, { method = 'GET', body } = {}) {
  const url = new URL(`${BASE_URL}${path}`, window.location.origin)
  const response = await fetch(url, {
    method,
    credentials: 'include',
    headers: body ? { 'Content-Type': 'application/json' } : undefined,
    body: body ? JSON.stringify(body) : undefined,
  })

  if (!response.ok) {
    throw new ApiError(response.status, `Error ${response.status} en ${method} ${path}`)
  }

  return response.status === 204 ? null : response.json()
}
