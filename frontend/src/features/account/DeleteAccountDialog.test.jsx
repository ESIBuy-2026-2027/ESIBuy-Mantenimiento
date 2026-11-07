import { describe, it, expect, vi } from 'vitest'
import { fireEvent, screen, waitFor, within } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { delay, http, HttpResponse } from 'msw'
import { server } from '../../mocks/server'
import { renderWithProviders } from '../../test/utils'
import DeleteAccountDialog from './DeleteAccountDialog'

// Diálogo para que el vendedor (HU47) y el cliente (HU25) eliminen su propia cuenta: explica las consecuencias de su
// rol y pide la contraseña actual.

const CONTRASENA = 'Contraseña-de-prueba-1!'

// Guarda los cuerpos que llegan al backend y responde con el estado indicado
function registrarEliminaciones(respuesta = () => new HttpResponse(null, { status: 204 })) {
  const cuerpos = []
  server.use(
    http.delete('/api/perfil', async ({ request }) => {
      cuerpos.push(await request.json())
      return respuesta()
    }),
  )
  return cuerpos
}

function abrirDialogo(rol = 'VENDEDOR') {
  const onClose = vi.fn()
  const onDeleted = vi.fn()
  renderWithProviders(<DeleteAccountDialog rol={rol} onClose={onClose} onDeleted={onDeleted} />)
  return { onClose, onDeleted }
}

describe('DeleteAccountDialog (HU47): confirmación', () => {
  it('HU47-CP09: explica qué se borra y qué se conserva, y sin la contraseña no deja eliminar', async () => {
    const usuario = userEvent.setup()
    abrirDialogo()

    const dialogo = screen.getByRole('alertdialog', { name: /eliminar mi cuenta/i })
    expect(dialogo).toHaveTextContent(/no se puede deshacer/i)
    expect(dialogo).toHaveTextContent(/productos que no tienen pedidos se eliminan/i)
    expect(dialogo).toHaveTextContent(/pedidos y las valoraciones se conservan/i)
    expect(dialogo).toHaveTextContent(/se cerrará la sesión/i)
    const eliminar = screen.getByRole('button', { name: /^eliminar mi cuenta$/i })
    expect(eliminar).toBeDisabled()

    await usuario.type(screen.getByLabelText(/^contraseña actual/i), CONTRASENA)

    expect(eliminar).toBeEnabled()
  })
})

describe('DeleteAccountDialog (HU47): eliminar', () => {
  it('HU47-CP10: con la contraseña escrita elimina la cuenta enviando solo la contraseña', async () => {
    const cuerpos = registrarEliminaciones()
    const usuario = userEvent.setup()
    const { onDeleted } = abrirDialogo()

    await usuario.type(screen.getByLabelText(/^contraseña actual/i), CONTRASENA)
    await usuario.click(screen.getByRole('button', { name: /^eliminar mi cuenta$/i }))

    await waitFor(() => expect(onDeleted).toHaveBeenCalled())
    expect(cuerpos).toEqual([{ password: CONTRASENA }])
  })
})

describe('DeleteAccountDialog (HU47): cancelar', () => {
  it('HU47-CP12: al cancelar no se elimina la cuenta', async () => {
    const cuerpos = registrarEliminaciones()
    const usuario = userEvent.setup()
    const { onClose, onDeleted } = abrirDialogo()

    await usuario.type(screen.getByLabelText(/^contraseña actual/i), CONTRASENA)
    await usuario.click(screen.getByRole('button', { name: /cancelar/i }))

    expect(onClose).toHaveBeenCalled()
    expect(onDeleted).not.toHaveBeenCalled()
    expect(cuerpos).toEqual([])
  })
})

describe('DeleteAccountDialog (HU47): errores', () => {
  it('HU47-CP11: con la contraseña incorrecta lo avisa, no elimina y deja corregirla', async () => {
    registrarEliminaciones(() => HttpResponse.json({ code: 'WRONG_PASSWORD' }, { status: 400 }))
    const usuario = userEvent.setup()
    const { onClose, onDeleted } = abrirDialogo()

    await usuario.type(screen.getByLabelText(/^contraseña actual/i), CONTRASENA)
    await usuario.click(screen.getByRole('button', { name: /^eliminar mi cuenta$/i }))

    expect(await screen.findByText('La contraseña actual no es correcta.')).toBeInTheDocument()
    expect(screen.getByRole('alertdialog')).not.toHaveTextContent(CONTRASENA)
    expect(screen.getByRole('button', { name: /^eliminar mi cuenta$/i })).toBeEnabled()
    expect(onDeleted).not.toHaveBeenCalled()
    expect(onClose).not.toHaveBeenCalled()
  })

  it('HU47-CP11: si falla por otro motivo da un aviso genérico y no elimina', async () => {
    registrarEliminaciones(() => new HttpResponse(null, { status: 500 }))
    const usuario = userEvent.setup()
    const { onDeleted } = abrirDialogo()

    await usuario.type(screen.getByLabelText(/^contraseña actual/i), CONTRASENA)
    await usuario.click(screen.getByRole('button', { name: /^eliminar mi cuenta$/i }))

    expect(await screen.findByText('No se ha podido eliminar la cuenta. Inténtalo de nuevo.')).toBeInTheDocument()
    expect(onDeleted).not.toHaveBeenCalled()
  })
})

describe('DeleteAccountDialog (HU25): consecuencias del cliente', () => {
  it('HU25-CP07: explica qué se elimina y qué se conserva de la cuenta de un cliente', async () => {
    abrirDialogo('CLIENTE')

    const dialogo = screen.getByRole('alertdialog', { name: /eliminar mi cuenta/i })
    expect(dialogo).toHaveTextContent(/no se puede deshacer/i)
    expect(dialogo).toHaveTextContent(/direcciones/i)
    expect(dialogo).toHaveTextContent(/métodos de pago/i)
    expect(dialogo).toHaveTextContent(/carrito/i)
    expect(dialogo).toHaveTextContent(/lista de deseos/i)
    expect(dialogo).toHaveTextContent(/doble factor/i)
    expect(dialogo).toHaveTextContent(/pedidos y las valoraciones se conservan/i)
    expect(dialogo).toHaveTextContent(/se cerrará la sesión/i)
  })

  it('HU25-CP07: el cliente no ve las consecuencias del vendedor', () => {
    abrirDialogo('CLIENTE')

    expect(screen.getByRole('alertdialog')).not.toHaveTextContent(/productos/i)
  })

  it('HU25-CP07: las consecuencias del vendedor no cambian y no hablan de las del cliente', () => {
    abrirDialogo('VENDEDOR')

    const dialogo = screen.getByRole('alertdialog', { name: /eliminar mi cuenta/i })
    expect(dialogo).toHaveTextContent(/productos que no tienen pedidos se eliminan/i)
    expect(dialogo).toHaveTextContent(/productos con pedidos se desactivan y se conservan/i)
    expect(dialogo).not.toHaveTextContent(/carrito|direcciones|lista de deseos/i)
  })

  it('HU25-CP07: las consecuencias salen de una lista, un elemento por consecuencia', () => {
    abrirDialogo('CLIENTE')

    const lista = within(screen.getByRole('alertdialog')).getByRole('list')
    expect(within(lista).getAllByRole('listitem').length).toBeGreaterThanOrEqual(3)
  })
})

describe('DeleteAccountDialog (HU25): contraseña', () => {
  it.each([
    ['vacía', ''],
    ['con solo espacios', '   '],
  ])('HU25-CP08: con la contraseña %s no se puede confirmar ni se llama al servidor', async (_nombre, texto) => {
    const cuerpos = registrarEliminaciones()
    const usuario = userEvent.setup()
    abrirDialogo('CLIENTE')

    if (texto) await usuario.type(screen.getByLabelText(/^contraseña actual/i), texto)
    await usuario.click(screen.getByRole('button', { name: /^eliminar mi cuenta$/i }))

    expect(screen.getByRole('button', { name: /^eliminar mi cuenta$/i })).toBeDisabled()
    expect(cuerpos).toEqual([])
  })

  it('HU25-CP08: al abrir el diálogo el foco está en el campo de la contraseña actual', async () => {
    abrirDialogo('CLIENTE')

    await waitFor(() => expect(screen.getByLabelText(/^contraseña actual/i)).toHaveFocus())
  })

  it('HU25-CP08: la contraseña actual se pide como un campo obligatorio de contraseña, oculta por defecto', () => {
    abrirDialogo('CLIENTE')

    const campo = screen.getByLabelText(/^contraseña actual/i)
    expect(campo).toBeRequired()
    expect(campo).toHaveAttribute('type', 'password')
  })
})

describe('DeleteAccountDialog (HU25): errores del cliente', () => {
  const campo = () => screen.getByLabelText(/^contraseña actual/i)
  const confirmar = () => screen.getByRole('button', { name: /^eliminar mi cuenta$/i })

  it('HU25-CP09: con la contraseña incorrecta lo avisa en el propio campo, no cierra la sesión y deja corregirla', async () => {
    registrarEliminaciones(() => HttpResponse.json({ code: 'WRONG_PASSWORD' }, { status: 400 }))
    const usuario = userEvent.setup()
    const { onClose, onDeleted } = abrirDialogo('CLIENTE')

    await usuario.type(campo(), CONTRASENA)
    await usuario.click(confirmar())

    await waitFor(() => expect(campo()).toHaveAccessibleDescription('La contraseña actual no es correcta.'))
    expect(campo()).toBeInvalid()
    expect(campo()).toHaveValue(CONTRASENA)
    expect(screen.getAllByText('La contraseña actual no es correcta.')).toHaveLength(1)
    expect(confirmar()).toBeEnabled()
    expect(onDeleted).not.toHaveBeenCalled()
    expect(onClose).not.toHaveBeenCalled()
  })

  it('HU25-CP09: el aviso de la contraseña desaparece al corregirla y se puede volver a confirmar', async () => {
    const cuerpos = registrarEliminaciones(() => HttpResponse.json({ code: 'WRONG_PASSWORD' }, { status: 400 }))
    const usuario = userEvent.setup()
    abrirDialogo('CLIENTE')
    await usuario.type(campo(), CONTRASENA)
    await usuario.click(confirmar())
    await waitFor(() => expect(campo()).toHaveAccessibleDescription('La contraseña actual no es correcta.'))

    await usuario.type(campo(), 'x')

    expect(campo()).not.toHaveAccessibleDescription('La contraseña actual no es correcta.')
    await usuario.click(confirmar())
    await waitFor(() => expect(cuerpos).toHaveLength(2))
    expect(cuerpos[1]).toEqual({ password: `${CONTRASENA}x` })
  })

  it.each([
    ['un error del servidor (500)', () => new HttpResponse(null, { status: 500 })],
    ['una validación del backend (400)', () => HttpResponse.json({ code: 'VALIDATION_ERROR' }, { status: 400 })],
    ['un error de red', () => HttpResponse.error()],
  ])('HU25-CP09: %s da un aviso genérico, sin marcar el campo, y no elimina', async (_nombre, respuesta) => {
    registrarEliminaciones(respuesta)
    const usuario = userEvent.setup()
    const { onDeleted } = abrirDialogo('CLIENTE')

    await usuario.type(campo(), CONTRASENA)
    await usuario.click(confirmar())

    expect(await screen.findByText('No se ha podido eliminar la cuenta. Inténtalo de nuevo.')).toBeInTheDocument()
    expect(campo()).not.toHaveAccessibleDescription('La contraseña actual no es correcta.')
    expect(campo()).toBeValid()
    expect(onDeleted).not.toHaveBeenCalled()
    expect(screen.getByRole('alertdialog')).not.toHaveTextContent(CONTRASENA)
  })

  it('HU25-CP09: mientras se envía, los botones quedan deshabilitados y no se envía dos veces', async () => {
    const cuerpos = registrarEliminaciones(async () => {
      await delay(150)
      return new HttpResponse(null, { status: 204 })
    })
    const usuario = userEvent.setup()
    const { onDeleted } = abrirDialogo('CLIENTE')
    await usuario.type(campo(), CONTRASENA)

    await usuario.click(confirmar())
    await usuario.click(confirmar())

    expect(confirmar()).toBeDisabled()
    expect(screen.getByRole('button', { name: /cancelar/i })).toBeDisabled()
    await waitFor(() => expect(onDeleted).toHaveBeenCalledTimes(1))
    expect(cuerpos).toHaveLength(1)
  })

  it('HU25-CP09: con la contraseña correcta elimina la cuenta del cliente enviando solo la contraseña', async () => {
    const cuerpos = registrarEliminaciones()
    const usuario = userEvent.setup()
    const { onDeleted } = abrirDialogo('CLIENTE')

    await usuario.type(campo(), CONTRASENA)
    await usuario.click(confirmar())

    await waitFor(() => expect(onDeleted).toHaveBeenCalled())
    expect(cuerpos).toEqual([{ password: CONTRASENA }])
  })
})

describe('DeleteAccountDialog (HU25): la contraseña actual no se copia (R9)', () => {
  it.each([['CLIENTE'], ['VENDEDOR']])('HU25-CP10: en el diálogo del %s no se puede copiar ni cortar, pero sí pegar', (rol) => {
    abrirDialogo(rol)
    const campo = screen.getByLabelText(/^contraseña actual/i)

    expect(fireEvent.copy(campo)).toBe(false)
    expect(fireEvent.cut(campo)).toBe(false)
    expect(fireEvent.paste(campo)).toBe(true)
  })
})
