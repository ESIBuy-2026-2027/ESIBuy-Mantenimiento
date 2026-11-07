import { describe, it, expect } from 'vitest'
import { screen, within } from '@testing-library/react'
import { renderWithProviders } from '../../test/utils'
import IncidentsList from './IncidentsList'

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
// La fecha en hora local y en español, como la muestra la lista: DD/MM/AAAA, HH:MM
const enHoraLocal = (iso) => {
  const d = new Date(iso)
  return `${dos(d.getDate())}/${dos(d.getMonth() + 1)}/${d.getFullYear()}, ${dos(d.getHours())}:${dos(d.getMinutes())}`
}

const elemento = (asunto) => screen.getByText(asunto).closest('li')

describe('IncidentsList', () => {
  it('HU44-CP26: la lista tiene un elemento por incidencia, en el orden en que llegan', () => {
    renderWithProviders(<IncidentsList incidents={INCIDENCIAS} />)

    const elementos = within(screen.getByRole('list', { name: 'Tus incidencias' })).getAllByRole('listitem')
    expect(elementos).toHaveLength(2)
    expect(elementos[0]).toHaveTextContent('No me llega el pedido')
    expect(elementos[1]).toHaveTextContent('Cobro duplicado')
  })

  it('HU44-CP26: cada incidencia muestra su asunto, su descripción completa, su respuesta y su fecha', () => {
    renderWithProviders(<IncidentsList incidents={INCIDENCIAS} />)

    const segunda = within(elemento('Cobro duplicado'))
    expect(segunda.getByText('Cobro duplicado')).toBeInTheDocument()
    expect(segunda.getByText('Me han cobrado dos veces el pedido.')).toBeInTheDocument()
    expect(segunda.getByText('Estamos revisando el cobro con el banco.')).toBeInTheDocument()
    expect(segunda.getByText(enHoraLocal('2026-10-02T08:00:00Z'))).toBeInTheDocument()
  })

  it('HU44-CP26: la fecha se muestra en hora local y su atributo datetime conserva la fecha del servidor', () => {
    renderWithProviders(<IncidentsList incidents={INCIDENCIAS} />)

    const fecha = within(elemento('No me llega el pedido')).getByText(enHoraLocal('2026-10-01T10:15:30Z'))
    expect(fecha.tagName).toBe('TIME')
    expect(fecha).toHaveAttribute('datetime', '2026-10-01T10:15:30Z')
  })

  it('HU44-CP26: una descripción larga se muestra entera', () => {
    renderWithProviders(<IncidentsList incidents={INCIDENCIAS} />)

    expect(within(elemento('No me llega el pedido')).getByText(LARGA)).toBeInTheDocument()
  })

  it('HU44-CP26: sin respuesta, la incidencia dice Sin respuesta todavía', () => {
    renderWithProviders(<IncidentsList incidents={INCIDENCIAS} />)

    expect(within(elemento('No me llega el pedido')).getByText('Sin respuesta todavía')).toBeInTheDocument()
    expect(within(elemento('Cobro duplicado')).queryByText('Sin respuesta todavía')).not.toBeInTheDocument()
  })

  it('HU44-CP25: el estado de cada incidencia se muestra en un badge: Pendiente o Abierta', () => {
    renderWithProviders(<IncidentsList incidents={INCIDENCIAS} />)

    expect(within(elemento('No me llega el pedido')).getByText('Pendiente')).toBeInTheDocument()
    expect(within(elemento('Cobro duplicado')).getByText('Abierta')).toBeInTheDocument()
  })

  it('HU44-CP26: la lista no tiene ningún botón: el usuario no cierra incidencias', () => {
    renderWithProviders(<IncidentsList incidents={INCIDENCIAS} />)

    expect(screen.queryByRole('button')).not.toBeInTheDocument()
  })

  it('HU44-CP26: mientras se recarga, la lista avisa con aria-busy', () => {
    renderWithProviders(<IncidentsList incidents={INCIDENCIAS} refreshing />)

    expect(screen.getByRole('list', { name: 'Tus incidencias' })).toHaveAttribute('aria-busy', 'true')
  })
})

// La misma lista en la supervisión del administrador (HU49, T304): cada incidencia lleva además su autor
const SUPERVISADAS = [
  { ...INCIDENCIAS[0], autor: { email: 'cliente@esibuy.test', rol: 'CLIENTE' } },
  { ...INCIDENCIAS[1], autor: { email: 'vendedor@esibuy.test', rol: 'VENDEDOR' } },
  {
    id: 'inc-3',
    asunto: 'Producto que no existe',
    descripcion: 'Aparece en la búsqueda pero no se puede abrir.',
    estado: 'PENDIENTE',
    respuesta: '',
    fechaCreacion: '2026-10-03T09:00:00Z',
    autor: null,
  },
]

describe('IncidentsList con el autor (HU49)', () => {
  it('HU49-CP18: con showAuthor, cada incidencia muestra el correo y el rol de quien la reportó', () => {
    renderWithProviders(<IncidentsList incidents={SUPERVISADAS} showAuthor label="Incidencias de los usuarios" />)

    expect(elemento('No me llega el pedido')).toHaveTextContent('cliente@esibuy.test')
    expect(elemento('No me llega el pedido')).toHaveTextContent('Cliente')
    expect(elemento('Cobro duplicado')).toHaveTextContent('vendedor@esibuy.test')
    expect(elemento('Cobro duplicado')).toHaveTextContent('Vendedor')
  })

  it('HU49-CP19: una incidencia cuya cuenta se ha eliminado dice Cuenta eliminada', () => {
    renderWithProviders(<IncidentsList incidents={SUPERVISADAS} showAuthor label="Incidencias de los usuarios" />)

    expect(elemento('Producto que no existe')).toHaveTextContent('Cuenta eliminada')
    expect(elemento('No me llega el pedido')).not.toHaveTextContent('Cuenta eliminada')
  })

  it('HU49-CP18: el nombre accesible de la lista se puede cambiar', () => {
    renderWithProviders(<IncidentsList incidents={SUPERVISADAS} showAuthor label="Incidencias de los usuarios" />)

    expect(screen.getByRole('list', { name: 'Incidencias de los usuarios' })).toBeInTheDocument()
    expect(screen.queryByRole('list', { name: 'Tus incidencias' })).not.toBeInTheDocument()
  })

  it('HU49-CP18: sin showAuthor no se muestra el autor, como en Mis incidencias', () => {
    renderWithProviders(<IncidentsList incidents={SUPERVISADAS} />)

    expect(screen.queryByText('cliente@esibuy.test')).not.toBeInTheDocument()
    expect(screen.queryByText('Cuenta eliminada')).not.toBeInTheDocument()
  })
})

describe('IncidentsList con acciones (HU50)', () => {
  it('HU50-CP22: con renderActions, cada tarjeta muestra las acciones de su incidencia', () => {
    const renderActions = (incident) => <button type="button">{`Acción de ${incident.id}`}</button>

    renderWithProviders(<IncidentsList incidents={SUPERVISADAS} showAuthor renderActions={renderActions} />)

    expect(within(elemento('No me llega el pedido')).getByRole('button')).toHaveTextContent('Acción de inc-1')
    expect(within(elemento('Cobro duplicado')).getByRole('button')).toHaveTextContent('Acción de inc-2')
  })

  it('HU50-CP22: sin renderActions las tarjetas no tienen acciones, como en Mis incidencias', () => {
    renderWithProviders(<IncidentsList incidents={INCIDENCIAS} />)

    expect(screen.queryByRole('button')).not.toBeInTheDocument()
  })
})
