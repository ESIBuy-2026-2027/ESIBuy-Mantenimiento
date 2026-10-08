import { describe, it, expect } from 'vitest'
import { screen, waitFor, within } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { http, HttpResponse } from 'msw'
import { server } from '../../mocks/server'
import { abrirSesionPorApi } from '../../test/renderApp'
import { renderWithProviders } from '../../test/utils'
import RoleUsersList from './RoleUsersList'

// El mock tiene 15 clientes (dos páginas): 8 activos, 5 desactivados y 2 bloqueados.
const ESPERA = { timeout: 3000 }

async function renderClientes() {
  await abrirSesionPorApi('admin@esibuy.test')
  renderWithProviders(
    <RoleUsersList
      role="CLIENTE"
      itemLabel="clientes"
      errorMessage="No se ha podido cargar el listado de clientes."
      loadingLabel="Cargando clientes…"
    />,
  )
  await screen.findByText('ruben.cortes@esibuy.test')
}

// Las filas se buscan por texto: no dependen de que Fluent marque la página con aria-hidden
// mientras se abre o se cierra la confirmación.
const filaDe = (email) => screen.getByText(email).closest('tr')
const botonActivarDe = (email) => within(filaDe(email)).queryByText('Activar', { selector: 'button' })
const filasDeDatos = () => within(screen.getByRole('table')).getAllByRole('row').slice(1)

async function confirmarActivacion(user, nombre) {
  await user.click(screen.getByRole('button', { name: `Activar a ${nombre}` }))
  const pregunta = await screen.findByText(new RegExp(`seguro que quieres activar a ${nombre}`, 'i'))
  const confirmacion = pregunta.closest('[role="alertdialog"]')
  await user.click(within(confirmacion).getByText('Activar', { selector: 'button' }))
}

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

    expect(opciones).toEqual(['Todos los estados', 'Activo', 'Bloqueado', 'Desactivado'])
    expect(screen.queryByRole('combobox', { name: 'Rol' })).not.toBeInTheDocument()
  })

  it('roleUsersList_alFiltrarPorDesactivado_muestraSoloEsasCuentas', async () => {
    const user = userEvent.setup()
    await renderClientes()

    await user.selectOptions(screen.getByRole('combobox', { name: 'Estado' }), 'Desactivado')

    await waitFor(() => expect(filasDeDatos()).toHaveLength(5))
    filasDeDatos().forEach((fila) => expect(within(fila).getByText('Desactivado')).toBeInTheDocument())
  })

  it('roleUsersList_ofreceActivarSoloEnLasCuentasDesactivadasOBloqueadas', async () => {
    await renderClientes()

    expect(botonActivarDe('lucia.torres@esibuy.test')).toBeInTheDocument()
    expect(botonActivarDe('oscar.leon@esibuy.test')).toBeInTheDocument()
    expect(botonActivarDe('ruben.cortes@esibuy.test')).not.toBeInTheDocument()
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

    await user.type(screen.getByRole('searchbox', { name: 'Buscar por nombre o email' }), 'zzz')

    expect(await screen.findByText(/no se han encontrado clientes/i)).toBeInTheDocument()
    await user.click(screen.getByRole('button', { name: /limpiar filtros/i }))
    expect(await screen.findByText('ruben.cortes@esibuy.test')).toBeInTheDocument()
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

    expect(await screen.findByText('ruben.cortes@esibuy.test')).toBeInTheDocument()
  })
})
