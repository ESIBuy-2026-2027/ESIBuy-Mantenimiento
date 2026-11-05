import { describe, it, expect, vi } from 'vitest'
import { screen, waitFor } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { http, HttpResponse } from 'msw'
import { server } from '../../mocks/server'
import { renderWithProviders } from '../../test/utils'
import DeleteAddressDialog from './DeleteAddressDialog'

// Diálogo para eliminar una dirección de envío (HU5): si es la principal y quedan otras, se elige la sustituta.

const URL_DIRECCIONES = '/api/customer/addresses'
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
const MAYOR = direccion('d-1', 'Calle Mayor 1', true)
const TOLEDO = direccion('d-2', 'Calle Toledo 5', false)
const ANCHA = direccion('d-3', 'Calle Ancha 3', false)

// Guarda las eliminaciones que llegan al backend: el id y la sustituta, si la hay
function registrarEliminaciones() {
  const eliminaciones = []
  server.use(
    http.delete(`${URL_DIRECCIONES}/:id`, ({ request, params }) => {
      eliminaciones.push({ id: params.id, sustituta: new URL(request.url).searchParams.get('nuevaPrincipal') })
      return new HttpResponse(null, { status: 204 })
    }),
  )
  return eliminaciones
}

function abrirDialogo(aEliminar, direcciones) {
  const onClose = vi.fn()
  const onDeleted = vi.fn()
  renderWithProviders(
    <DeleteAddressDialog direccion={aEliminar} direcciones={direcciones} onClose={onClose} onDeleted={onDeleted} />,
  )
  return { onClose, onDeleted }
}

describe('DeleteAddressDialog (HU5): eliminar', () => {
  it('HU5-CP26: elimina una dirección que no es la principal tras confirmarlo', async () => {
    const eliminaciones = registrarEliminaciones()
    const usuario = userEvent.setup()
    const { onDeleted } = abrirDialogo(TOLEDO, [MAYOR, TOLEDO])

    expect(screen.getByRole('alertdialog', { name: /eliminar dirección/i })).toHaveTextContent('Calle Toledo 5')
    await usuario.click(screen.getByRole('button', { name: /^eliminar$/i }))

    await waitFor(() => expect(onDeleted).toHaveBeenCalled())
    expect(eliminaciones).toEqual([{ id: 'd-2', sustituta: null }])
  })

  it('HU5-CP26: al cancelar no elimina nada', async () => {
    const eliminaciones = registrarEliminaciones()
    const usuario = userEvent.setup()
    const { onClose, onDeleted } = abrirDialogo(TOLEDO, [MAYOR, TOLEDO])

    await usuario.click(screen.getByRole('button', { name: /cancelar/i }))

    expect(onClose).toHaveBeenCalled()
    expect(onDeleted).not.toHaveBeenCalled()
    expect(eliminaciones).toEqual([])
  })
})

describe('DeleteAddressDialog (HU5): sustituta', () => {
  it('HU5-CP27: para eliminar la principal hay que elegir otra dirección como principal', async () => {
    const eliminaciones = registrarEliminaciones()
    const usuario = userEvent.setup()
    const { onDeleted } = abrirDialogo(MAYOR, [MAYOR, TOLEDO, ANCHA])

    // Hasta elegir la sustituta no se puede eliminar; la que se elimina no está entre las opciones
    expect(screen.getByRole('button', { name: /^eliminar$/i })).toBeDisabled()
    const sustituta = screen.getByRole('combobox', { name: /nueva dirección principal/i })
    expect(screen.queryByRole('option', { name: /calle mayor 1/i })).not.toBeInTheDocument()

    await usuario.selectOptions(sustituta, screen.getByRole('option', { name: /calle ancha 3/i }))
    await usuario.click(screen.getByRole('button', { name: /^eliminar$/i }))

    await waitFor(() => expect(onDeleted).toHaveBeenCalled())
    expect(eliminaciones).toEqual([{ id: 'd-1', sustituta: 'd-3' }])
  })

  it('HU5-CP27: la única dirección se elimina sin elegir sustituta', async () => {
    const eliminaciones = registrarEliminaciones()
    const usuario = userEvent.setup()
    const { onDeleted } = abrirDialogo(MAYOR, [MAYOR])

    expect(screen.queryByRole('combobox', { name: /nueva dirección principal/i })).not.toBeInTheDocument()
    await usuario.click(screen.getByRole('button', { name: /^eliminar$/i }))

    await waitFor(() => expect(onDeleted).toHaveBeenCalled())
    expect(eliminaciones).toEqual([{ id: 'd-1', sustituta: null }])
  })
})

describe('DeleteAddressDialog (HU5): errores', () => {
  it('HU5-CP28: si la eliminación falla avisa y no cierra el diálogo', async () => {
    server.use(http.delete(`${URL_DIRECCIONES}/:id`, () => new HttpResponse(null, { status: 500 })))
    const usuario = userEvent.setup()
    const { onDeleted } = abrirDialogo(TOLEDO, [MAYOR, TOLEDO])

    await usuario.click(screen.getByRole('button', { name: /^eliminar$/i }))

    expect(await screen.findByText('No se ha podido eliminar la dirección. Inténtalo de nuevo.')).toBeInTheDocument()
    expect(onDeleted).not.toHaveBeenCalled()
    // Se puede volver a intentar
    expect(screen.getByRole('button', { name: /^eliminar$/i })).toBeEnabled()
  })
})
