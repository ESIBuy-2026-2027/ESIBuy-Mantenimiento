import { describe, it, expect } from 'vitest'
import { screen, waitFor, within } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { http, HttpResponse } from 'msw'
import { server } from '../mocks/server'
import { abrirSesionPorApi, renderApp } from '../test/renderApp'

// Vista «Direcciones de envío» del cliente (HU5, AB#108 y AB#109), con los mocks de MSW del backend.
// Del mock de direcciones: Carlos (cliente@esibuy.test) tiene su principal en Calle Mayor 1 (Ciudad Real) y otra en
// Calle Toledo 5 (Madrid).

const URL_DIRECCIONES = '/api/customer/addresses'

async function abrirDirecciones(email = 'cliente@esibuy.test') {
  await abrirSesionPorApi(email)
  renderApp('/addresses')
  await screen.findByRole('heading', { level: 1, name: /direcciones de envío/i })
}

const tarjetasDeDireccion = () => within(screen.getByRole('list', { name: /tus direcciones/i })).getAllByRole('listitem')

const direccion = (id, calle, principal) => ({
  id,
  destinatario: 'Carlos Cliente',
  calle,
  detalles: null,
  codigoPostal: '13071',
  ciudad: 'Ciudad Real',
  provincia: 'Ciudad Real',
  telefono: '612345678',
  principal,
})

describe('AddressesPage (HU5): listado', () => {
  it('HU5-CP18: muestra cada dirección con su destinatario, la dirección completa y el teléfono', async () => {
    await abrirDirecciones()

    const direcciones = await screen.findAllByRole('listitem')
    expect(direcciones).toHaveLength(2)
    expect(direcciones[0]).toHaveTextContent('Carlos Cliente')
    expect(direcciones[0]).toHaveTextContent('Calle Mayor 1 · 13071 Ciudad Real (Ciudad Real)')
    expect(direcciones[0]).toHaveTextContent('612345678')
    expect(direcciones[1]).toHaveTextContent('Calle Toledo 5, 2º B · 28005 Madrid (Madrid)')
  })

  it('HU5-CP18: indica cuál es la dirección principal', async () => {
    await abrirDirecciones()

    const [mayor, toledo] = await screen.findAllByRole('listitem')
    expect(within(mayor).getByText('Principal')).toBeInTheDocument()
    expect(within(toledo).queryByText('Principal')).not.toBeInTheDocument()
  })

  it('HU5-CP18: sin direcciones muestra un estado vacío con el botón para añadir la primera', async () => {
    server.use(http.get(URL_DIRECCIONES, () => HttpResponse.json([])))

    await abrirDirecciones()

    expect(await screen.findByRole('heading', { level: 2, name: /aún no tienes direcciones/i })).toBeInTheDocument()
    expect(screen.getByRole('button', { name: /añadir dirección/i })).toBeInTheDocument()
    expect(screen.queryByRole('list', { name: /tus direcciones/i })).not.toBeInTheDocument()
  })

  it('HU5-CP18: si falla la carga avisa y deja reintentar', async () => {
    // Solo la primera petición falla: al reintentar responde el mock normal
    server.use(http.get(URL_DIRECCIONES, () => new HttpResponse(null, { status: 500 }), { once: true }))
    const usuario = userEvent.setup()

    await abrirDirecciones()

    expect(await screen.findByText(/no se han podido cargar tus direcciones/i)).toBeInTheDocument()
    await usuario.click(screen.getByRole('button', { name: /reintentar/i }))
    expect(await screen.findAllByRole('listitem')).toHaveLength(2)
  })
})

describe('AddressesPage (HU5): acceso', () => {
  it('HU5-CP19: el cliente tiene el enlace «Direcciones» en la cabecera y llega a la vista', async () => {
    const usuario = userEvent.setup()
    await abrirSesionPorApi('cliente@esibuy.test')
    renderApp('/')

    const cuenta = await screen.findByRole('navigation', { name: 'Cuenta' })
    await usuario.click(within(cuenta).getByRole('link', { name: /direcciones/i }))

    expect(await screen.findByRole('heading', { level: 1, name: /direcciones de envío/i })).toBeInTheDocument()
  })

  it.each(['vendedor@esibuy.test', 'admin@esibuy.test'])(
    'HU5-CP19: un vendedor o un administrador no acceden a la vista (%s)',
    async (email) => {
      await abrirSesionPorApi(email)
      renderApp('/addresses')

      // Se le lleva a la página de inicio de su rol, y en su cabecera no hay enlace a las direcciones
      await waitFor(() => expect(screen.getByTestId('ruta-actual')).not.toHaveTextContent('/addresses'))
      expect(screen.queryByRole('heading', { level: 1, name: /direcciones de envío/i })).not.toBeInTheDocument()
      expect(screen.queryByRole('link', { name: /direcciones/i })).not.toBeInTheDocument()
    },
  )
})

describe('AddressesPage (HU5): principal', () => {
  it('HU5-CP20: marcar otra dirección como principal la cambia en la lista', async () => {
    const usuario = userEvent.setup()
    await abrirDirecciones()
    const [, toledo] = await screen.findAllByRole('listitem')
    // La principal no ofrece marcarse otra vez
    expect(within(tarjetasDeDireccion()[0]).queryByRole('button', { name: /marcar como principal/i })).toBeNull()

    await usuario.click(within(toledo).getByRole('button', { name: /marcar como principal/i }))

    await waitFor(() => expect(within(tarjetasDeDireccion()[1]).getByText('Principal')).toBeInTheDocument())
    expect(within(tarjetasDeDireccion()[0]).queryByText('Principal')).not.toBeInTheDocument()
  })

  it('HU5-CP20: si la dirección ya no existe avisa y recarga la lista', async () => {
    // Se ha eliminado desde otra pestaña: el PUT da 404 y la lista que se vuelve a pedir ya no la trae
    const mayor = direccion('d-1', 'Calle Mayor 1', true)
    const toledo = direccion('d-2', 'Calle Toledo 5', false)
    let peticionesDeLista = 0
    server.use(
      http.get(URL_DIRECCIONES, () => {
        peticionesDeLista += 1
        return HttpResponse.json(peticionesDeLista === 1 ? [mayor, toledo] : [mayor])
      }),
      http.put(`${URL_DIRECCIONES}/:id/principal`, () =>
        HttpResponse.json({ code: 'ADDRESS_NOT_FOUND' }, { status: 404 }),
      ),
    )
    const usuario = userEvent.setup()
    await abrirDirecciones()
    const [, eliminada] = await screen.findAllByRole('listitem')

    await usuario.click(within(eliminada).getByRole('button', { name: /marcar como principal/i }))

    expect(await screen.findByText(/esa dirección ya no existe/i)).toBeInTheDocument()
    await waitFor(() => expect(tarjetasDeDireccion()).toHaveLength(1))
  })
})

describe('AddressesPage (HU5): límite', () => {
  it('HU5-CP21: con 5 direcciones no deja añadir otra y explica por qué', async () => {
    const cinco = Array.from({ length: 5 }, (_, i) => direccion(`d-${i + 1}`, `Calle Mayor ${i + 1}`, i === 0))
    server.use(http.get(URL_DIRECCIONES, () => HttpResponse.json(cinco)))

    await abrirDirecciones()
    await screen.findAllByRole('listitem')

    expect(screen.getByRole('button', { name: /añadir dirección/i })).toBeDisabled()
    expect(screen.getByText(/has alcanzado el máximo de 5 direcciones/i)).toBeInTheDocument()
  })

  it('HU5-CP21: con menos de 5 direcciones se puede añadir otra', async () => {
    await abrirDirecciones()
    await screen.findAllByRole('listitem')

    expect(screen.getByRole('button', { name: /añadir dirección/i })).toBeEnabled()
    expect(screen.queryByText(/has alcanzado el máximo/i)).not.toBeInTheDocument()
  })
})

describe('AddressesPage (HU5): añadir y editar', () => {
  it('HU5-CP23: al guardar una dirección nueva se cierra el panel, se avisa y aparece en la lista', async () => {
    const usuario = userEvent.setup()
    await abrirDirecciones()
    await screen.findAllByRole('listitem')

    await usuario.click(screen.getByRole('button', { name: /añadir dirección/i }))
    const panel = screen.getByRole('region', { name: /nueva dirección/i })
    await usuario.type(within(panel).getByLabelText(/destinatario/i), 'Ana López')
    await usuario.type(within(panel).getByLabelText(/calle y número/i), 'Calle Ancha 3')
    await usuario.type(within(panel).getByLabelText(/código postal/i), '45001')
    await usuario.type(within(panel).getByLabelText(/ciudad/i), 'Toledo')
    await usuario.type(within(panel).getByLabelText(/provincia/i), 'Toledo')
    await usuario.type(within(panel).getByLabelText(/teléfono/i), '698765432')
    await usuario.click(within(panel).getByRole('button', { name: /guardar dirección/i }))

    expect(await screen.findByText('Dirección añadida.')).toBeInTheDocument()
    expect(screen.queryByRole('region', { name: /nueva dirección/i })).not.toBeInTheDocument()
    await waitFor(() => expect(tarjetasDeDireccion()).toHaveLength(3))
    expect(tarjetasDeDireccion()[2]).toHaveTextContent('Calle Ancha 3 · 45001 Toledo (Toledo)')
  })

  it('HU5-CP23: al cancelar se cierra el panel sin añadir nada', async () => {
    const usuario = userEvent.setup()
    await abrirDirecciones()
    await screen.findAllByRole('listitem')

    await usuario.click(screen.getByRole('button', { name: /añadir dirección/i }))
    await usuario.click(screen.getByRole('button', { name: /cancelar/i }))

    expect(screen.queryByRole('region', { name: /nueva dirección/i })).not.toBeInTheDocument()
    expect(tarjetasDeDireccion()).toHaveLength(2)
  })

  it('HU5-CP24: al editar una dirección se guardan los cambios y la lista los muestra', async () => {
    const usuario = userEvent.setup()
    await abrirDirecciones()
    const [, toledo] = await screen.findAllByRole('listitem')

    await usuario.click(within(toledo).getByRole('button', { name: /editar/i }))
    const panel = screen.getByRole('region', { name: /editar dirección/i })
    const calle = within(panel).getByLabelText(/calle y número/i)
    await usuario.clear(calle)
    await usuario.type(calle, 'Calle Toledo 7')
    await usuario.click(within(panel).getByRole('button', { name: /guardar dirección/i }))

    expect(await screen.findByText('Dirección guardada.')).toBeInTheDocument()
    expect(screen.queryByRole('region', { name: /editar dirección/i })).not.toBeInTheDocument()
    await waitFor(() => expect(tarjetasDeDireccion()[1]).toHaveTextContent('Calle Toledo 7, 2º B'))
    // Editar no cambia la principal
    expect(within(tarjetasDeDireccion()[0]).getByText('Principal')).toBeInTheDocument()
  })
})

describe('AddressesPage (HU5): eliminar', () => {
  it('HU5-CP27: al eliminar la principal eligiendo otra, desaparece de la lista y la elegida pasa a serlo', async () => {
    const usuario = userEvent.setup()
    await abrirDirecciones()
    const [mayor] = await screen.findAllByRole('listitem')

    await usuario.click(within(mayor).getByRole('button', { name: /eliminar/i }))
    const dialogo = screen.getByRole('alertdialog', { name: /eliminar dirección/i })
    await usuario.selectOptions(
      within(dialogo).getByRole('combobox', { name: /nueva dirección principal/i }),
      within(dialogo).getByRole('option', { name: /calle toledo 5/i }),
    )
    await usuario.click(within(dialogo).getByRole('button', { name: /^eliminar$/i }))

    expect(await screen.findByText('Dirección eliminada.')).toBeInTheDocument()
    await waitFor(() => expect(tarjetasDeDireccion()).toHaveLength(1))
    expect(tarjetasDeDireccion()[0]).toHaveTextContent('Calle Toledo 5')
    expect(within(tarjetasDeDireccion()[0]).getByText('Principal')).toBeInTheDocument()
  })
})
