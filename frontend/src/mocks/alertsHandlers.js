import { http, HttpResponse } from 'msw'
import { getUsuarioEnSesion } from './authHandlers'

// Alertas de cada cliente de ejemplo (AB#529): identificador del cliente → sus alertas, de la más reciente a la más
// antigua. Como el backend real, viven en el servidor: se pierden al reiniciar los mocks. El cliente de ejemplo
// (u-3) empieza con una bajada de precio sin leer y una vuelta a estar disponible ya leída.
const alertasIniciales = () =>
  new Map([
    [
      'u-3',
      [
        {
          id: 'a-2',
          tipo: 'BAJADA_PRECIO',
          fecha: '2026-11-26T10:00:00Z',
          leida: false,
          producto: { id: 'p-1', nombre: 'Teclado mecánico', precioAnterior: 89.5, precioNuevo: 74.9 },
        },
        {
          id: 'a-1',
          tipo: 'DISPONIBILIDAD',
          fecha: '2026-11-25T18:30:00Z',
          leida: true,
          producto: { id: 'p-2', nombre: 'Ratón inalámbrico', precioAnterior: null, precioNuevo: null },
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
]
