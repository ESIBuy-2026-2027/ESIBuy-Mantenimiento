import { describe, it, expect, vi } from 'vitest'
import { screen, waitFor } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { http, HttpResponse } from 'msw'
import { server } from '../../mocks/server'
import { renderWithProviders } from '../../test/utils'
import DeletePaymentMethodDialog from './DeletePaymentMethodDialog'

// Diálogo para eliminar un método de pago (HU6): si es el predeterminado y quedan otros, se elige el sustituto.

const URL_METODOS = '/api/customer/payment-methods'
const TARJETA = { id: 'm-1', tipo: 'TARJETA', mascara: '•••• 1111', predeterminado: true }
const BIZUM = { id: 'm-2', tipo: 'BIZUM', mascara: '•••• 678', predeterminado: false }
const PAYPAL = { id: 'm-3', tipo: 'PAYPAL', mascara: 'paypal.me', predeterminado: false }

// Guarda las eliminaciones que llegan al backend: el id y el sustituto, si lo hay
function registrarEliminaciones() {
  const eliminaciones = []
  server.use(
    http.delete(`${URL_METODOS}/:id`, ({ request, params }) => {
      eliminaciones.push({ id: params.id, sustituto: new URL(request.url).searchParams.get('nuevoPredeterminado') })
      return new HttpResponse(null, { status: 204 })
    }),
  )
  return eliminaciones
}

function abrirDialogo(metodo, metodos) {
  const onClose = vi.fn()
  const onDeleted = vi.fn()
  renderWithProviders(
    <DeletePaymentMethodDialog metodo={metodo} metodos={metodos} onClose={onClose} onDeleted={onDeleted} />,
  )
  return { onClose, onDeleted }
}

describe('DeletePaymentMethodDialog (HU6): eliminar', () => {
  it('HU6-CP34: elimina un método que no es el predeterminado tras confirmarlo', async () => {
    const eliminaciones = registrarEliminaciones()
    const usuario = userEvent.setup()
    const { onDeleted } = abrirDialogo(BIZUM, [TARJETA, BIZUM])

    expect(screen.getByRole('alertdialog', { name: /eliminar método de pago/i })).toHaveTextContent('Bizum •••• 678')
    await usuario.click(screen.getByRole('button', { name: /^eliminar$/i }))

    await waitFor(() => expect(onDeleted).toHaveBeenCalled())
    expect(eliminaciones).toEqual([{ id: 'm-2', sustituto: null }])
  })

  it('HU6-CP34: al cancelar no elimina nada', () => {
    expect.fail('Funcionalidad no implementada')
  })
})

describe('DeletePaymentMethodDialog (HU6): sustituto', () => {
  it('HU6-CP35: para eliminar el predeterminado hay que elegir otro método como predeterminado', () => {
    expect.fail('Funcionalidad no implementada')
  })

  it('HU6-CP35: el único método se elimina sin elegir sustituto', () => {
    expect.fail('Funcionalidad no implementada')
  })
})

describe('DeletePaymentMethodDialog (HU6): errores', () => {
  it('HU6-CP36: si la eliminación falla avisa y no cierra el diálogo', () => {
    expect.fail('Funcionalidad no implementada')
  })
})
