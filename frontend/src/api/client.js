const BASE_URL = import.meta.env.VITE_API_URL ?? ''

export class ApiError extends Error {
  constructor(status, message, code = null) {
    super(message)
    this.name = 'ApiError'
    this.status = status
    this.code = code
  }
}

async function readErrorCode(response) {
  try {
    const data = await response.json()
    return typeof data?.code === 'string' ? data.code : null
  } catch {
    return null
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
    throw new ApiError(
      response.status,
      `Error ${response.status} en ${method} ${path}`,
      await readErrorCode(response),
    )
  }

  return response.status === 204 ? null : response.json()
}
