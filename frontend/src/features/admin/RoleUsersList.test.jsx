import { describe, it, expect } from 'vitest'
import { screen, waitFor, within } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { http, HttpResponse } from 'msw'
import { server } from '../../mocks/server'
import { abrirSesionPorApi } from '../../test/renderApp'
import { renderWithProviders } from '../../test/utils'
import RoleUsersList from './RoleUsersList'

// El mock tiene 15 clientes (dos páginas): 8 activos y 7 desactivados. Por defecto se ordenan por
// nombre ascendente: la primera página va de Álvaro Campos Serrano a Pablo Gómez Martín.
const ESPERA = { timeout: 3000 }

async function renderClientes(currentUserId) {
  await abrirSesionPorApi('admin@esibuy.test')
  renderWithProviders(
    <RoleUsersList
      role="CLIENTE"
      itemLabel="clientes"
      errorMessage="No se ha podido cargar el listado de clientes."
      loadingLabel="Cargando clientes…"
      currentUserId={currentUserId}
    />,
  )
  await screen.findByText('carlos.moreno@esibuy.test')
}

// Las filas se buscan por texto: no dependen de que Fluent marque la página con aria-hidden
// mientras se abre o se cierra la confirmación.
const filaDe = (email) => screen.getByText(email).closest('tr')
const botonActivarDe = (email) => within(filaDe(email)).queryByText('Activar', { selector: 'button' })
const botonDesactivarDe = (email) => within(filaDe(email)).queryByText('Desactivar', { selector: 'button' })
const filasDeDatos = () => within(screen.getByRole('table')).getAllByRole('row').slice(1)
const primerNombre = () => within(filasDeDatos()[0]).getAllByRole('cell')[0].textContent

async function confirmarAccion(user, accion, nombre) {
  await user.click(screen.getByRole('button', { name: `${accion} a ${nombre}` }))
  const pregunta = await screen.findByText(new RegExp(`seguro que quieres ${accion} a ${nombre}`, 'i'))
  const confirmacion = pregunta.closest('[role="alertdialog"]')
  await user.click(within(confirmacion).getByText(accion, { selector: 'button' }))
}

const confirmarActivacion = (user, nombre) => confirmarAccion(user, 'Activar', nombre)
const confirmarDesactivacion = (user, nombre) => confirmarAccion(user, 'Desactivar', nombre)

describe('RoleUsersList (HU24)', () => {
  it('roleUsersList_muestraSoloLosUsuariosDelRolPedido', async () => {
    await renderClientes()

    expect(filasDeDatos()).toHaveLength(10)
    filasDeDatos().forEach((fila) => expect(within(fila).getByText('Cliente')).toBeInTheDocument())
    expect(screen.getByText(/mostrando 1–10 de 15 clientes/i)).toBeInTheDocument()
  })

  it('roleUsersList_elFiltroDeEstadoNoOfrecePendienteNiFiltroDeRol', async () => {
    await renderClientes()

    const filtro = screen.getByRole('combobox', { name: 'Estado' })
    const opciones = within(filtro).getAllByRole('option').map((opcion) => opcion.textContent)

    expect(opciones).toEqual(['Todos los estados', 'Activo', 'Desactivado'])
    expect(screen.queryByRole('combobox', { name: 'Rol' })).not.toBeInTheDocument()
  })

  it('roleUsersList_alFiltrarPorDesactivado_muestraSoloEsasCuentas', async () => {
    const user = userEvent.setup()
    await renderClientes()

    await user.selectOptions(screen.getByRole('combobox', { name: 'Estado' }), 'Desactivado')

    await waitFor(() => expect(filasDeDatos()).toHaveLength(7))
    filasDeDatos().forEach((fila) => expect(within(fila).getByText('Desactivado')).toBeInTheDocument())
  })

  it('roleUsersList_ofreceActivarSoloEnLasCuentasDesactivadas', async () => {
    await renderClientes()

    expect(botonActivarDe('lucia.torres@esibuy.test')).toBeInTheDocument()
    expect(botonActivarDe('oscar.leon@esibuy.test')).toBeInTheDocument()
    expect(botonActivarDe('carlos.moreno@esibuy.test')).not.toBeInTheDocument()
  })

  it('roleUsersList_porDefecto_ordenaPorNombreAscendente', async () => {
    await renderClientes()

    expect(screen.getByRole('combobox', { name: 'Ordenar por' })).toHaveValue('name')
    expect(primerNombre()).toBe('Álvaro Campos Serrano')
  })

  it('roleUsersList_alCambiarElSentido_ordenaPorNombreDescendente', async () => {
    const user = userEvent.setup()
    await renderClientes()

    await user.click(screen.getByRole('button', { name: 'Orden ascendente' }))

    await waitFor(() => expect(primerNombre()).toBe('Sofía Cabrera Soler'))
  })

  it('roleUsersList_alOrdenarPorEstado_muestraPrimeroLosActivos', async () => {
    const user = userEvent.setup()
    await renderClientes()

    await user.selectOptions(screen.getByRole('combobox', { name: 'Ordenar por' }), 'Estado')

    await waitFor(() => expect(primerNombre()).toBe('Carlos Moreno Gil'))
    filasDeDatos()
      .slice(0, 8)
      .forEach((fila) => expect(within(fila).getByText('Activo')).toBeInTheDocument())
  })

  it('roleUsersList_buscaSoloPorNombreYNoPorEmail', async () => {
    const user = userEvent.setup()
    await renderClientes()
    const buscador = screen.getByRole('searchbox', { name: 'Buscar por nombre' })

    await user.type(buscador, 'torres')
    await waitFor(() => expect(filasDeDatos()).toHaveLength(1))
    expect(screen.getByText('lucia.torres@esibuy.test')).toBeInTheDocument()

    await user.clear(buscador)
    await user.type(buscador, 'esibuy')
    expect(await screen.findByText(/no se han encontrado clientes/i)).toBeInTheDocument()
  })

  it('roleUsersList_alActivarUnCliente_loMuestraActivoYAvisa', async () => {
    const user = userEvent.setup()
    await renderClientes()

    await confirmarActivacion(user, 'Lucía Torres Vega')

    expect(await screen.findByText(/lucía torres vega ya está activo/i, {}, ESPERA)).toBeInTheDocument()
    await waitFor(() => expect(within(filaDe('lucia.torres@esibuy.test')).getByText('Activo')).toBeInTheDocument(), ESPERA)
    expect(botonActivarDe('lucia.torres@esibuy.test')).not.toBeInTheDocument()
  })

  it('roleUsersList_siFallaLaActivacion_muestraErrorYNoCambiaElEstado', async () => {
    server.use(http.patch('/api/admin/usuarios/:id/status', () => new HttpResponse(null, { status: 500 })))
    const user = userEvent.setup()
    await renderClientes()

    await confirmarActivacion(user, 'Lucía Torres Vega')

    expect(await screen.findByText(/no se ha podido activar a lucía torres vega/i, {}, ESPERA)).toBeInTheDocument()
    expect(within(filaDe('lucia.torres@esibuy.test')).getByText('Desactivado')).toBeInTheDocument()
  })

  it('roleUsersList_conBusquedaSinCoincidencias_muestraEstadoVacioYPermiteLimpiar', async () => {
    const user = userEvent.setup()
    await renderClientes()

    await user.type(screen.getByRole('searchbox', { name: 'Buscar por nombre' }), 'zzz')

    expect(await screen.findByText(/no se han encontrado clientes/i)).toBeInTheDocument()
    await user.click(screen.getByRole('button', { name: /limpiar filtros/i }))
    expect(await screen.findByText('carlos.moreno@esibuy.test')).toBeInTheDocument()
  })

  it('roleUsersList_conErrorDeServidor_muestraMensajeYPermiteReintentar', async () => {
    server.use(http.get('/api/admin/usuarios', () => new HttpResponse(null, { status: 500 }), { once: true }))
    const user = userEvent.setup()
    await abrirSesionPorApi('admin@esibuy.test')
    renderWithProviders(
      <RoleUsersList
        role="CLIENTE"
        itemLabel="clientes"
        errorMessage="No se ha podido cargar el listado de clientes."
        loadingLabel="Cargando clientes…"
      />,
    )

    expect(await screen.findByText('No se ha podido cargar el listado de clientes.')).toBeInTheDocument()
    await user.click(screen.getByRole('button', { name: /reintentar/i }))

    expect(await screen.findByText('carlos.moreno@esibuy.test')).toBeInTheDocument()
  })

  it('roleUsersList_ofreceDesactivarSoloEnLasCuentasActivas', async () => {
    await renderClientes()

    expect(botonDesactivarDe('carlos.moreno@esibuy.test')).toBeInTheDocument()
    expect(botonDesactivarDe('lucia.torres@esibuy.test')).not.toBeInTheDocument()
  })

  it('roleUsersList_enLaFilaDelUsuarioEnSesion_noOfreceDesactivar', async () => {
    await renderClientes('usr-3')

    expect(botonDesactivarDe('carlos.moreno@esibuy.test')).not.toBeInTheDocument()
    expect(botonDesactivarDe('claudia.pena@esibuy.test')).toBeInTheDocument()
  })

  it('roleUsersList_alDesactivarUnCliente_loMuestraDesactivadoYAvisa', async () => {
    const user = userEvent.setup()
    await renderClientes()

    await confirmarDesactivacion(user, 'Carlos Moreno Gil')

    expect(await screen.findByText(/carlos moreno gil está desactivado/i, {}, ESPERA)).toBeInTheDocument()
    await waitFor(() => expect(within(filaDe('carlos.moreno@esibuy.test')).getByText('Desactivado')).toBeInTheDocument(), ESPERA)
    expect(botonActivarDe('carlos.moreno@esibuy.test')).toBeInTheDocument()
  })

  it('roleUsersList_siElBackendNoDejaDesactivarseASiMismo_muestraElMotivo', async () => {
    server.use(
      http.patch('/api/admin/usuarios/:id/status', () =>
        HttpResponse.json({ code: 'CANNOT_DEACTIVATE_SELF' }, { status: 409 }),
      ),
    )
    const user = userEvent.setup()
    await renderClientes()

    await confirmarDesactivacion(user, 'Carlos Moreno Gil')

    expect(await screen.findByText('No puedes desactivar tu propia cuenta.', {}, ESPERA)).toBeInTheDocument()
  })

  it('roleUsersList_siFallaLaDesactivacion_muestraUnErrorGenerico', async () => {
    server.use(http.patch('/api/admin/usuarios/:id/status', () => new HttpResponse(null, { status: 500 })))
    const user = userEvent.setup()
    await renderClientes()

    await confirmarDesactivacion(user, 'Carlos Moreno Gil')

    expect(await screen.findByText(/no se ha podido desactivar a carlos moreno gil/i, {}, ESPERA)).toBeInTheDocument()
    expect(within(filaDe('carlos.moreno@esibuy.test')).getByText('Activo')).toBeInTheDocument()
  })
})
