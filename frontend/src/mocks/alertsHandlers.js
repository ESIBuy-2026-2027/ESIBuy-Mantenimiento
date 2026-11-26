import { http, HttpResponse } from 'msw'
import { getUsuarioEnSesion } from './authHandlers'

// Imagen de ejemplo: un dibujo con la inicial del producto, como en el mock de productos, para no depender de internet
const imagenDeEjemplo = (nombre, color) => {
  const svg =
    `<svg xmlns="http://www.w3.org/2000/svg" width="400" height="400" viewBox="0 0 400 400">` +
    `<rect width="400" height="400" fill="${color}"/>` +
    `<text x="200" y="260" font-family="Segoe UI, Arial" font-size="200" font-weight="600" fill="#ffffff" ` +
    `text-anchor="middle">${nombre.charAt(0)}</text></svg>`
  return `data:image/svg+xml,${encodeURIComponent(svg)}`
}

// Alertas de cada cliente de ejemplo (AB#529): identificador del cliente → sus alertas, de la más reciente a la más
// antigua. Como el backend real, viven en el servidor: se pierden al reiniciar los mocks. El cliente de ejemplo
// (u-3) empieza con una bajada de precio sin leer, una vuelta a estar disponible ya leída y otra bajada sin imagen.
const alertasIniciales = () =>
  new Map([
    [
      'u-3',
      [
        {
          id: 'a-3',
          tipo: 'BAJADA_PRECIO',
          fecha: '2026-11-26T10:00:00Z',
          leida: false,
          producto: {
            id: 'p-1',
            nombre: 'Teclado mecánico',
            imagen: imagenDeEjemplo('Teclado mecánico', '#0f6cbd'),
            precioAnterior: 89.5,
            precioNuevo: 74.9,
          },
        },
        {
          id: 'a-2',
          tipo: 'DISPONIBILIDAD',
          fecha: '2026-11-25T18:30:00Z',
          leida: true,
          producto: {
            id: 'p-2',
            nombre: 'Ratón inalámbrico',
            imagen: imagenDeEjemplo('Ratón inalámbrico', '#0e700e'),
            precioAnterior: null,
            precioNuevo: null,
          },
        },
        {
          id: 'a-1',
          tipo: 'BAJADA_PRECIO',
          fecha: '2026-11-24T09:15:00Z',
          leida: true,
          producto: { id: 'p-3', nombre: 'Monitor 27"', imagen: '', precioAnterior: 249, precioNuevo: 199 },
        },
      ],
    ],
  ])

let alertas = alertasIniciales()

export function resetAlertsMock() {
  alertas = alertasIniciales()
}

export const alertsHandlers = [
  http.get('/api/customer/alerts', () => {
    const usuario = getUsuarioEnSesion()
    if (!usuario) return new HttpResponse(null, { status: 401 })
    if (usuario.rol !== 'CLIENTE') return HttpResponse.json({ code: 'ACCESS_DENIED' }, { status: 403 })

    const suyas = alertas.get(usuario.id) ?? []
    return HttpResponse.json({
      mensaje: suyas.length ? 'Alertas consultadas correctamente' : 'No tienes alertas',
      alertas: suyas,
    })
  }),

  // Marcar como leída (AB#531): una alerta de otro cliente se responde igual que una que no existe
  http.patch('/api/customer/alerts/:id/read', ({ params }) => {
    const usuario = getUsuarioEnSesion()
    if (!usuario) return new HttpResponse(null, { status: 401 })
    if (usuario.rol !== 'CLIENTE') return HttpResponse.json({ code: 'ACCESS_DENIED' }, { status: 403 })

    const suyas = alertas.get(usuario.id) ?? []
    const alerta = suyas.find((candidata) => candidata.id === params.id)
    if (!alerta) return HttpResponse.json({ mensaje: 'La alerta no existe', code: 'ALERT_NOT_FOUND' }, { status: 404 })

    const leida = { ...alerta, leida: true }
    alertas.set(
      usuario.id,
      suyas.map((candidata) => (candidata.id === leida.id ? leida : candidata)),
    )
    return HttpResponse.json({ mensaje: 'Alerta marcada como leída', alerta: leida })
  }),
]
