import { describe, it, expect } from 'vitest'
import { screen, waitFor, within } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { http, HttpResponse } from 'msw'
import { server } from '../mocks/server'
import { abrirSesionPorApi, renderApp } from '../test/renderApp'

// Del mock del perfil: Carlos (cliente@esibuy.test) es cliente Premium con el avatar 1; Vera (vendedor@esibuy.test)
// es vendedora de Tecnología con el avatar 2; Vicente (vendedor-vacio@esibuy.test) no tiene teléfono ni avatar.
async function abrirPerfil(email) {
  await abrirSesionPorApi(email)
  renderApp('/profile')
  await screen.findByRole('heading', { level: 1, name: /mi perfil/i })
  await screen.findByLabelText('Nombre')
}

const valorDe = (etiqueta) => screen.getByLabelText(etiqueta)

describe('ProfilePage (HU4): datos del perfil', () => {
  it('HU4-CP05: el cliente ve sus datos personales y los de cliente', async () => {
    await abrirPerfil('cliente@esibuy.test')

    expect(valorDe('Nombre')).toHaveValue('Carlos')
    expect(valorDe('Apellidos')).toHaveValue('Cliente')
    expect(valorDe('Email')).toHaveValue('cliente@esibuy.test')
    expect(valorDe('Rol')).toHaveValue('Cliente')
    expect(valorDe('DNI')).toHaveValue('87654321X')
    expect(valorDe('Teléfono')).toHaveValue('611222333')
    expect(valorDe('Fecha de nacimiento')).toHaveValue('14/05/1998')
    expect(valorDe('Tipo de cliente')).toHaveValue('Premium')
    expect(valorDe('Fecha de alta')).toHaveValue('10/04/2026')
    expect(screen.queryByLabelText('Nombre comercial')).not.toBeInTheDocument()
  })

  it('HU4-CP05: los datos del perfil son de solo lectura', async () => {
    await abrirPerfil('cliente@esibuy.test')

    screen.getAllByRole('textbox').forEach((campo) => expect(campo).toHaveAttribute('readonly'))
    expect(screen.queryByRole('button', { name: /guardar|editar/i })).not.toBeInTheDocument()
  })

  it('HU4-CP06: el vendedor ve sus datos y los de vendedor, con el nombre de su categoría', async () => {
    await abrirPerfil('vendedor@esibuy.test')

    expect(valorDe('Nombre')).toHaveValue('Vera')
    expect(valorDe('Rol')).toHaveValue('Vendedor')
    expect(valorDe('Nombre comercial')).toHaveValue('Tienda de Vera')
    expect(await screen.findByDisplayValue('Tecnología')).toBe(valorDe('Categoría principal'))
    expect(valorDe('DNI')).toHaveValue('12345678Z')
    expect(screen.queryByLabelText('Fecha de nacimiento')).not.toBeInTheDocument()
    expect(screen.queryByLabelText('Tipo de cliente')).not.toBeInTheDocument()
  })

  it('HU4-CP06: los datos opcionales que no tiene se muestran con un guion', async () => {
    await abrirPerfil('vendedor-vacio@esibuy.test')

    expect(valorDe('Teléfono')).toHaveValue('—')
  })

  it('HU4-CP07: muestra como imagen el avatar que eligió el usuario', async () => {
    await abrirPerfil('vendedor@esibuy.test')

    expect(screen.getByRole('img', { name: 'Avatar 2' })).toBeInTheDocument()
    expect(screen.getByRole('heading', { level: 2, name: 'Vera Vendedora' })).toBeInTheDocument()
  })

  it('HU4-CP07: sin avatar elegido muestra sus iniciales', async () => {
    await abrirPerfil('vendedor-vacio@esibuy.test')

    const avatar = screen.getByRole('img', { name: /sin imagen de perfil/i })
    expect(avatar).toHaveTextContent('VV')
  })

  it('HU4-CP08: no muestra la contraseña ni el estado de la cuenta', async () => {
    await abrirPerfil('cliente@esibuy.test')

    expect(screen.queryByLabelText(/password|contraseña/i)).not.toBeInTheDocument()
    expect(screen.queryByText(/password|contraseña/i)).not.toBeInTheDocument()
    expect(screen.queryByLabelText('Estado')).not.toBeInTheDocument()
  })

  it('HU4-CP09: si el perfil no carga, avisa sin detalles internos y permite reintentar', async () => {
    server.use(
      http.get('/api/perfil', () => HttpResponse.json({ message: 'MongoTimeoutException' }, { status: 500 }), {
        once: true,
      }),
    )
    const user = userEvent.setup()
    await abrirSesionPorApi('cliente@esibuy.test')
    renderApp('/profile')

    const aviso = await screen.findByText(/no se ha podido cargar tu perfil/i)
    expect(screen.queryByText(/mongotimeout/i)).not.toBeInTheDocument()
    expect(aviso).toBeInTheDocument()
    await user.click(screen.getByRole('button', { name: /reintentar/i }))

    expect(await screen.findByLabelText('Nombre')).toHaveValue('Carlos')
  })
})

// Preferencias del cliente (HU46, T286): las categorías que le interesan. Del mock de categorías: Tecnología (cat-1) y
// Hogar (cat-2), las dos activas.
describe('ProfilePage (HU46): preferencias', () => {
  const seccion = () => screen.findByRole('group', { name: 'Mis preferencias' })

  it('HU46-CP20: el cliente ve sus preferencias, con una casilla por cada categoría activa', async () => {
    await abrirPerfil('cliente@esibuy.test')

    const preferencias = await seccion()
    expect(within(preferencias).getByRole('checkbox', { name: 'Tecnología' })).not.toBeChecked()
    expect(within(preferencias).getByRole('checkbox', { name: 'Hogar' })).not.toBeChecked()
  })

  it('HU46-CP20: las categorías que ya eligió aparecen marcadas', async () => {
    server.use(http.get('/api/customer/preferences', () => HttpResponse.json({ categorias: ['cat-2'] })))
    await abrirPerfil('cliente@esibuy.test')

    const preferencias = await seccion()
    await waitFor(() => expect(within(preferencias).getByRole('checkbox', { name: 'Hogar' })).toBeChecked())
    expect(within(preferencias).getByRole('checkbox', { name: 'Tecnología' })).not.toBeChecked()
  })

  it('HU46-CP20: el vendedor no tiene preferencias', async () => {
    await abrirPerfil('vendedor@esibuy.test')

    expect(screen.queryByRole('group', { name: 'Mis preferencias' })).not.toBeInTheDocument()
  })
})

describe('ProfilePage (HU4): acceso', () => {
  it('HU4-CP10: sin sesión, redirige al login', async () => {
    renderApp('/profile')

    await waitFor(() => expect(screen.getByTestId('ruta-actual')).toHaveTextContent(/^\/login$/))
  })

  it('HU4-CP10: el administrador no tiene perfil que consultar', async () => {
    await abrirSesionPorApi('admin@esibuy.test')
    renderApp('/profile')

    await waitFor(() => expect(screen.getByTestId('ruta-actual')).toHaveTextContent(/^\/admin$/))
    expect(screen.queryByRole('heading', { name: /mi perfil/i })).not.toBeInTheDocument()
  })
})
