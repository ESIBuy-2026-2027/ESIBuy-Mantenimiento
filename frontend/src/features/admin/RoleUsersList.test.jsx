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
const filasDeDatos = () => within(screen.getByRole('table')).getAllByRole('row').slice(1)
const primerNombre = () => within(filasDeDatos()[0]).getAllByRole('cell')[0].textContent

// La confirmación se busca por texto: mientras se abre o se cierra, Fluent marca partes de la página
// con aria-hidden y las búsquedas por rol pueden fallar por tiempos.
async function activarDesdeLaFicha(user, nombre) {
  await user.click(screen.getByRole('button', { name: `Ver información de ${nombre}` }))
  const dialogo = await screen.findByRole('dialog', { name: /información del usuario/i })
  await user.click(await within(dialogo).findByText('Activar', { selector: 'button' }))
  const pregunta = await screen.findByText(new RegExp(`seguro que quieres activar a ${nombre}`, 'i'))
  await user.click(within(pregunta.closest('[role="alertdialog"]')).getByText('Activar', { selector: 'button' }))
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


  it('roleUsersList_cadaFilaTieneUnaLupaParaVerLaFicha', async () => {
    await renderClientes()

    expect(within(filaDe('carlos.moreno@esibuy.test')).getByRole('button', { name: 'Ver información de Carlos Moreno Gil' })).toBeInTheDocument()
    expect(screen.getAllByRole('button', { name: /^ver información de/i })).toHaveLength(10)
    expect(screen.queryByText('Activar', { selector: 'button' })).not.toBeInTheDocument()
    expect(screen.queryByText('Desactivar', { selector: 'button' })).not.toBeInTheDocument()
  })

  it('roleUsersList_alPulsarLaLupa_abreLaFichaDeEseUsuario', async () => {
    const user = userEvent.setup()
    await renderClientes()

    await user.click(screen.getByRole('button', { name: 'Ver información de Lucía Torres Vega' }))

    const dialogo = await screen.findByRole('dialog', { name: /información del usuario/i })
    expect(await within(dialogo).findByLabelText('Email')).toHaveValue('lucia.torres@esibuy.test')
  })

  it('roleUsersList_laFichaRecibeElUsuarioEnSesion_yNoOfreceDesactivarse', async () => {
    const user = userEvent.setup()
    await renderClientes('usr-3')

    await user.click(screen.getByRole('button', { name: 'Ver información de Carlos Moreno Gil' }))

    const dialogo = await screen.findByRole('dialog', { name: /información del usuario/i })
    await within(dialogo).findByLabelText('Nombre')
    expect(within(dialogo).queryByText('Desactivar', { selector: 'button' })).not.toBeInTheDocument()
  })

  it('roleUsersList_alCambiarElEstadoEnLaFicha_actualizaElListado', async () => {
    const user = userEvent.setup()
    await renderClientes()

    await activarDesdeLaFicha(user, 'Lucía Torres Vega')

    await waitFor(() => expect(within(filaDe('lucia.torres@esibuy.test')).getByText('Activo')).toBeInTheDocument(), ESPERA)
  })
})
