import { describe, it, expect, vi } from 'vitest'
import { screen, within } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { server } from '../../mocks/server'
import { renderWithProviders } from '../../test/utils'
import IncidentsTable from './IncidentsTable'

const LARGA = 'Lo pedí hace una semana y sigue sin llegar. '.repeat(10).trim()

const INCIDENCIAS = [
  {
    id: 'inc-1',
    asunto: 'No me llega el pedido',
    descripcion: LARGA,
    estado: 'PENDIENTE',
    respuesta: '',
    fechaCreacion: '2026-10-01T10:15:30Z',
  },
  {
    id: 'inc-2',
    asunto: 'Cobro duplicado',
    descripcion: 'Me han cobrado dos veces el pedido.',
    estado: 'ABIERTA',
    respuesta: 'Estamos revisando el cobro con el banco.',
    fechaCreacion: '2026-10-02T08:00:00Z',
  },
]

const dos = (n) => String(n).padStart(2, '0')
// La fecha en hora local y en español, como la muestra la tabla: DD/MM/AAAA, HH:MM
const enHoraLocal = (iso) => {
  const d = new Date(iso)
  return `${dos(d.getDate())}/${dos(d.getMonth() + 1)}/${d.getFullYear()}, ${dos(d.getHours())}:${dos(d.getMinutes())}`
}

const fila = (asunto) => screen.getByRole('row', { name: new RegExp(asunto) })

describe('IncidentsTable', () => {
  it('HU44-CP26: la tabla tiene los encabezados asunto, descripción, estado, respuesta, fecha y acciones', () => {
    renderWithProviders(<IncidentsTable incidents={INCIDENCIAS} />)

    const tabla = screen.getByRole('table', { name: 'Mis incidencias' })
    const encabezados = within(tabla)
      .getAllByRole('columnheader')
      .map((celda) => celda.textContent)
    expect(encabezados).toEqual(['Asunto', 'Descripción', 'Estado', 'Respuesta', 'Fecha de creación', 'Acciones'])
  })

  it('HU44-CP26: cada incidencia ocupa una fila con su asunto, descripción, respuesta y fecha', () => {
    renderWithProviders(<IncidentsTable incidents={INCIDENCIAS} />)

    expect(screen.getAllByRole('row')).toHaveLength(3)
    const segunda = within(fila('Cobro duplicado'))
    expect(segunda.getByText('Cobro duplicado')).toBeInTheDocument()
    expect(segunda.getByText('Me han cobrado dos veces el pedido.')).toBeInTheDocument()
    expect(segunda.getByText('Estamos revisando el cobro con el banco.')).toBeInTheDocument()
    expect(segunda.getByText(enHoraLocal('2026-10-02T08:00:00Z'))).toBeInTheDocument()
  })

  it('HU44-CP26: la fecha se muestra en hora local y su atributo datetime conserva la fecha del servidor', () => {
    renderWithProviders(<IncidentsTable incidents={INCIDENCIAS} />)

    const fecha = within(fila('No me llega el pedido')).getByText(enHoraLocal('2026-10-01T10:15:30Z'))
    expect(fecha.tagName).toBe('TIME')
    expect(fecha).toHaveAttribute('datetime', '2026-10-01T10:15:30Z')
  })

  it('HU44-CP26: la descripción larga se recorta en pantalla y su texto completo sigue accesible', () => {
    renderWithProviders(<IncidentsTable incidents={INCIDENCIAS} />)

    const descripcion = within(fila('No me llega el pedido')).getByText(LARGA)
    expect(descripcion).toHaveAttribute('title', LARGA)
  })

  it('HU44-CP26: sin respuesta, la fila dice Sin respuesta todavía', () => {
    renderWithProviders(<IncidentsTable incidents={INCIDENCIAS} />)

    expect(within(fila('No me llega el pedido')).getByText('Sin respuesta todavía')).toBeInTheDocument()
    expect(within(fila('Cobro duplicado')).queryByText('Sin respuesta todavía')).not.toBeInTheDocument()
  })

  it('HU44-CP25: el estado de cada incidencia se muestra en un badge: Pendiente o Abierta', () => {
    renderWithProviders(<IncidentsTable incidents={INCIDENCIAS} />)

    expect(within(fila('No me llega el pedido')).getByText('Pendiente')).toBeInTheDocument()
    expect(within(fila('Cobro duplicado')).getByText('Abierta')).toBeInTheDocument()
  })

  it('HU44-CP26: mientras se recarga, la tabla avisa con aria-busy', () => {
    const { container } = renderWithProviders(<IncidentsTable incidents={INCIDENCIAS} refreshing />)

    expect(container.querySelector('[aria-busy="true"]')).not.toBeNull()
  })

  it('HU44-CP34: cada fila tiene un botón Cerrar incidencia, con el asunto en su nombre, que todavía no está disponible', () => {
    renderWithProviders(<IncidentsTable incidents={INCIDENCIAS} />)

    const boton = screen.getByRole('button', { name: 'Cerrar incidencia: Cobro duplicado' })
    expect(screen.getAllByRole('button', { name: /^Cerrar incidencia: / })).toHaveLength(2)
    expect(boton).toHaveAttribute('aria-disabled', 'true')
  })

  it('HU44-CP34: el botón de cerrar se puede enfocar con el teclado y explica que estará disponible pronto', async () => {
    const user = userEvent.setup()
    renderWithProviders(<IncidentsTable incidents={INCIDENCIAS} />)

    await user.tab()
    const boton = screen.getByRole('button', { name: 'Cerrar incidencia: No me llega el pedido' })
    expect(boton).toHaveFocus()
    expect(await screen.findByRole('tooltip')).toHaveTextContent('Disponible pronto')
  })

  it('HU44-CP34: pulsar el botón de cerrar no llama a ningún endpoint', async () => {
    const user = userEvent.setup()
    const peticion = vi.fn()
    server.events.on('request:start', peticion)
    renderWithProviders(<IncidentsTable incidents={INCIDENCIAS} />)

    await user.click(screen.getByRole('button', { name: 'Cerrar incidencia: Cobro duplicado' }))

    expect(peticion).not.toHaveBeenCalled()
    server.events.removeAllListeners()
  })
})
