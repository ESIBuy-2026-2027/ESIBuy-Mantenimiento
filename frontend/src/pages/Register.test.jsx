import { screen, within } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import { registrarCliente, registrarVendedor } from '../api/auth'
import { getCategories } from '../api/products'
import { renderWithProviders } from '../test/utils'
import Register from './Register'

vi.mock('../api/auth', () => ({
  registrarCliente: vi.fn(),
  registrarVendedor: vi.fn(),
}))

vi.mock('../api/products', () => ({
  getCategories: vi.fn(),
}))

async function renderYElegirCliente() {
  renderWithProviders(<Register />)
  await userEvent.click(screen.getByRole('button', { name: /cliente/i }))
}

async function renderYElegirVendedor() {
  renderWithProviders(<Register />)
  await userEvent.click(screen.getByRole('button', { name: /vendedor/i }))
}

async function rellenarFormularioVendedorValido() {
  await userEvent.type(screen.getByLabelText(/^nombre(?! comercial)/i), 'Luis')
  await userEvent.type(screen.getByLabelText(/apellidos/i), 'Pérez')
  await userEvent.type(screen.getByLabelText(/nombre comercial/i), 'Tienda Luis')
  await userEvent.click(await screen.findByRole('checkbox', { name: /tecnología/i }))
  await userEvent.type(screen.getByLabelText(/dni/i), '12345678Z')
  await userEvent.type(screen.getByLabelText(/email/i), 'luis@correo.com')
  await userEvent.type(screen.getByLabelText(/^password/i), 'Password123!')
  await userEvent.type(screen.getByLabelText(/^repetir password/i), 'Password123!')
  await userEvent.click(screen.getByRole('checkbox', { name: /política de privacidad/i }))
}

async function rellenarFormularioValido() {
  await userEvent.type(screen.getByLabelText(/nombre/i), 'Ana')
  await userEvent.type(screen.getByLabelText(/apellidos/i), 'García')
  await userEvent.type(screen.getByLabelText(/fecha de nacimiento/i), '1990-05-20')
  await userEvent.type(screen.getByLabelText(/dni/i), '12345678Z')
  await userEvent.type(screen.getByLabelText(/email/i), 'ana@correo.com')
  await userEvent.type(screen.getByLabelText(/^password/i), 'Password123!')
  await userEvent.type(screen.getByLabelText(/^repetir password/i), 'Password123!')
  await userEvent.click(screen.getByRole('checkbox', { name: /política de privacidad/i }))
}

describe('Register', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    getCategories.mockResolvedValue([
      { id: 'cat-1', nombre: 'Tecnología' },
      { id: 'cat-2', nombre: 'Hogar' },
    ])
  })

  it('register_alRenderizar_muestraOpcionesClienteYVendedor', () => {
    renderWithProviders(<Register />)

    expect(screen.getByRole('heading', { level: 1 })).toBeInTheDocument()
    expect(screen.getByText(/cliente/i)).toBeInTheDocument()
    expect(screen.getByText(/vendedor/i)).toBeInTheDocument()
  })

  it('register_alElegirCliente_muestraFormularioDeCliente', async () => {
    await renderYElegirCliente()

    expect(screen.getByRole('heading', { name: /registro de cliente/i })).toBeInTheDocument()
  })

  it('register_alElegirVendedor_muestraFormularioDeVendedor', async () => {
    renderWithProviders(<Register />)

    await userEvent.click(screen.getByRole('button', { name: /vendedor/i }))

    expect(screen.getByRole('heading', { name: /registro de vendedor/i })).toBeInTheDocument()
  })

  it('register_alElegirVendedor_muestraLosCamposDelFormulario', async () => {
    await renderYElegirVendedor()

    expect(screen.getByLabelText(/^nombre(?! comercial)/i)).toBeInTheDocument()
    expect(screen.getByLabelText(/apellidos/i)).toBeInTheDocument()
    expect(screen.getByLabelText(/nombre comercial/i)).toBeInTheDocument()
    expect(screen.getByLabelText(/dni/i)).toBeInTheDocument()
    expect(screen.getByLabelText(/email/i)).toBeInTheDocument()
    expect(screen.getByLabelText(/teléfono/i)).toBeInTheDocument()
    expect(screen.getByLabelText(/^password/i)).toBeInTheDocument()
    expect(screen.getByLabelText(/^repetir password/i)).toBeInTheDocument()
  })

  it('register_alElegirCliente_muestraLosCamposDelFormulario', async () => {
    await renderYElegirCliente()

    expect(screen.getByLabelText(/nombre/i)).toBeInTheDocument()
    expect(screen.getByLabelText(/apellidos/i)).toBeInTheDocument()
    expect(screen.getByLabelText(/fecha de nacimiento/i)).toBeInTheDocument()
    expect(screen.getByLabelText(/dni/i)).toBeInTheDocument()
    expect(screen.getByLabelText(/email/i)).toBeInTheDocument()
    expect(screen.getByLabelText(/teléfono/i)).toBeInTheDocument()
    expect(screen.getByLabelText(/^password/i)).toBeInTheDocument()
    expect(screen.getByLabelText(/^repetir password/i)).toBeInTheDocument()
  })

  it('register_alElegirVendedor_marcaComoRequeridosLosCamposObligatorios', async () => {
    await renderYElegirVendedor()

    expect(screen.getByLabelText(/^nombre(?! comercial)/i)).toBeRequired()
    expect(screen.getByLabelText(/apellidos/i)).toBeRequired()
    expect(screen.getByLabelText(/nombre comercial/i)).toBeRequired()
    expect(screen.getByLabelText(/dni/i)).toBeRequired()
    expect(screen.getByLabelText(/email/i)).toBeRequired()
    expect(screen.getByLabelText(/^password/i)).toBeRequired()
    expect(screen.getByLabelText(/^repetir password/i)).toBeRequired()
    expect(screen.getByLabelText(/teléfono/i)).not.toBeRequired()
  })

  it('register_alElegirVendedor_muestraUnaCasillaPorCategoriaYPermiteMarcarVarias', async () => {
    await renderYElegirVendedor()

    const tecnologia = await screen.findByRole('checkbox', { name: /tecnología/i })
    const hogar = screen.getByRole('checkbox', { name: /hogar/i })
    await userEvent.click(tecnologia)
    await userEvent.click(hogar)

    expect(tecnologia).toBeChecked()
    expect(hogar).toBeChecked()
  })

  it('register_alElegirVendedor_permiteElegirUnAvatar', async () => {
    await renderYElegirVendedor()

    expect(screen.getAllByRole('radio')).toHaveLength(3)

    await userEvent.click(screen.getByRole('radio', { name: /avatar 2/i }))

    expect(screen.getByRole('radio', { name: /avatar 2/i })).toBeChecked()
  })

  it('register_alElegirVendedor_marcaLasCategoriasComoObligatorias', async () => {
    await renderYElegirVendedor()

    expect(screen.getByRole('group', { name: /categorías/i })).toHaveTextContent('*')
  })

  it('register_alEnviarFormularioVendedorVacio_muestraErroresDeCamposObligatoriosYDeCategoria', async () => {
    await renderYElegirVendedor()
    await userEvent.click(screen.getByRole('button', { name: /registrarse/i }))

    expect(screen.getAllByText(/obligatorio/i)).toHaveLength(7)
    expect(screen.getByText(/al menos una categoría/i)).toBeInTheDocument()
  })

  it('register_alIntroducirDniInvalidoComoVendedor_muestraErrorDeFormato', async () => {
    await renderYElegirVendedor()
    await userEvent.type(screen.getByLabelText(/dni/i), '1234')
    await userEvent.click(screen.getByRole('button', { name: /registrarse/i }))

    expect(screen.getByText(/dni válido/i)).toBeInTheDocument()
  })

  it('register_alIntroducirEmailInvalidoComoVendedor_muestraErrorDeFormato', async () => {
    await renderYElegirVendedor()
    await userEvent.type(screen.getByLabelText(/email/i), 'esto-no-es-un-email')
    await userEvent.click(screen.getByRole('button', { name: /registrarse/i }))

    expect(screen.getByText(/email válido/i)).toBeInTheDocument()
  })

  it('register_siLosPasswordsDelVendedorNoCoinciden_muestraError', async () => {
    await renderYElegirVendedor()
    await rellenarFormularioVendedorValido()
    await userEvent.type(screen.getByLabelText(/^repetir password/i), 'Extra')
    await userEvent.click(screen.getByRole('button', { name: /registrarse/i }))

    expect(screen.getByText(/los passwords no coinciden/i)).toBeInTheDocument()
  })

  it('register_siRepetirPasswordDelVendedorEstaVacio_muestraErrorDeObligatorioYNoDeCoincidencia', async () => {
    await renderYElegirVendedor()
    await rellenarFormularioVendedorValido()
    await userEvent.clear(screen.getByLabelText(/^repetir password/i))
    await userEvent.click(screen.getByRole('button', { name: /registrarse/i }))

    expect(screen.getByText(/obligatorio/i)).toBeInTheDocument()
    expect(screen.queryByText(/no coinciden/i)).not.toBeInTheDocument()
  })

  it('register_siPasswordDelVendedorEstaVacio_muestraErrorDeObligatorioYNoDeCoincidencia', async () => {
    await renderYElegirVendedor()
    await rellenarFormularioVendedorValido()
    await userEvent.clear(screen.getByLabelText(/^password/i))
    await userEvent.click(screen.getByRole('button', { name: /registrarse/i }))

    expect(screen.getByText(/obligatorio/i)).toBeInTheDocument()
    expect(screen.queryByText(/no coinciden/i)).not.toBeInTheDocument()
  })

  it('register_alEnviarDatosValidosDeVendedor_llamaAlServicioConLosDatos', async () => {
    registrarVendedor.mockResolvedValue({})
    await renderYElegirVendedor()
    await rellenarFormularioVendedorValido()
    await userEvent.click(screen.getByRole('radio', { name: /avatar 2/i }))
    await userEvent.click(screen.getByRole('button', { name: /registrarse/i }))

    expect(registrarVendedor).toHaveBeenCalledWith(
      expect.objectContaining({
        nombre: 'Luis',
        nombreComercial: 'Tienda Luis',
        email: 'luis@correo.com',
        categorias: ['cat-1'],
        avatar: 'avatar2',
      })
    )
  })

  it('register_siElRegistroDeVendedorEsCorrecto_muestraMensajeDeConfirmacion', async () => {
    registrarVendedor.mockResolvedValue({})
    await renderYElegirVendedor()
    await rellenarFormularioVendedorValido()
    await userEvent.click(screen.getByRole('button', { name: /registrarse/i }))

    expect(await screen.findByText(/cuenta creada/i)).toBeInTheDocument()
  })

  it('register_siElEmailDelVendedorYaEstaRegistrado_muestraErrorEnElCampoEmail', async () => {
    registrarVendedor.mockRejectedValue({ status: 409, code: 'EMAIL_ALREADY_REGISTERED' })
    await renderYElegirVendedor()
    await rellenarFormularioVendedorValido()
    await userEvent.click(screen.getByRole('button', { name: /registrarse/i }))

    expect(await screen.findByText(/email ya está registrado/i)).toBeInTheDocument()
    expect(screen.queryByText(/nombre comercial ya está registrado/i)).not.toBeInTheDocument()
  })

  it('register_siElNombreComercialYaEstaRegistrado_muestraErrorEnElCampoNombreComercial', async () => {
    registrarVendedor.mockRejectedValue({ status: 409, code: 'COMMERCIAL_NAME_ALREADY_REGISTERED' })
    await renderYElegirVendedor()
    await rellenarFormularioVendedorValido()
    await userEvent.click(screen.getByRole('button', { name: /registrarse/i }))

    expect(await screen.findByText(/nombre comercial ya está registrado/i)).toBeInTheDocument()
    expect(screen.queryByText(/email ya está registrado/i)).not.toBeInTheDocument()
  })

  it('register_siEmailYNombreComercialYaEstanRegistrados_muestraAmbosErrores', async () => {
    registrarVendedor.mockRejectedValue({
      status: 409,
      code: 'EMAIL_AND_COMMERCIAL_NAME_ALREADY_REGISTERED',
    })
    await renderYElegirVendedor()
    await rellenarFormularioVendedorValido()
    await userEvent.click(screen.getByRole('button', { name: /registrarse/i }))

    expect(await screen.findByText(/email ya está registrado/i)).toBeInTheDocument()
    expect(screen.getByText(/nombre comercial ya está registrado/i)).toBeInTheDocument()
  })

  it('register_alPulsarMostrarPassword_alternaEntreVerloYOcultarlo', async () => {
    await renderYElegirCliente()
    const password = screen.getByLabelText(/^password/i)
    const repeticion = screen.getByLabelText(/^repetir password/i)

    expect(password).toHaveAttribute('type', 'password')
    expect(repeticion).toHaveAttribute('type', 'password')

    await userEvent.click(screen.getByRole('button', { name: /^mostrar password$/i }))
    await userEvent.click(screen.getByRole('button', { name: /^mostrar repetir password$/i }))

    expect(password).toHaveAttribute('type', 'text')
    expect(repeticion).toHaveAttribute('type', 'text')

    await userEvent.click(screen.getByRole('button', { name: /^ocultar password$/i }))

    expect(password).toHaveAttribute('type', 'password')
    expect(repeticion).toHaveAttribute('type', 'text')
  })

  it('register_antesDeEscribir_muestraLosRequisitosDelPasswordPendientes', async () => {
    await renderYElegirCliente()

    const requisitos = screen.getByRole('list', { name: /requisitos del password/i })
    const items = within(requisitos).getAllByRole('listitem')

    expect(items).toHaveLength(5)
    expect(requisitos).toHaveTextContent(/al menos 8 caracteres/i)
    expect(requisitos).toHaveTextContent(/una mayúscula/i)
    expect(requisitos).toHaveTextContent(/una minúscula/i)
    expect(requisitos).toHaveTextContent(/un número/i)
    expect(requisitos).toHaveTextContent(/un símbolo/i)
    items.forEach((item) => {
      expect(within(item).getByRole('img', { name: /pendiente/i })).toBeInTheDocument()
    })
  })

  it('register_alEscribirElPassword_marcaComoCumplidosLosRequisitosQueSatisface', async () => {
    await renderYElegirCliente()
    await userEvent.type(screen.getByLabelText(/^password/i), 'abcdefgh')

    const requisito = (texto) => screen.getByText(texto).closest('li')

    expect(within(requisito(/al menos 8 caracteres/i)).getByRole('img', { name: /cumplido/i })).toBeInTheDocument()
    expect(within(requisito(/una minúscula/i)).getByRole('img', { name: /cumplido/i })).toBeInTheDocument()
    expect(within(requisito(/una mayúscula/i)).getByRole('img', { name: /pendiente/i })).toBeInTheDocument()
    expect(within(requisito(/un número/i)).getByRole('img', { name: /pendiente/i })).toBeInTheDocument()
    expect(within(requisito(/un símbolo/i)).getByRole('img', { name: /pendiente/i })).toBeInTheDocument()
  })

  it('register_siElPasswordNoCumpleLosRequisitos_muestraErrorYNoLlamaAlServicio', async () => {
    await renderYElegirCliente()
    await rellenarFormularioValido()
    await userEvent.clear(screen.getByLabelText(/^password/i))
    await userEvent.clear(screen.getByLabelText(/^repetir password/i))
    await userEvent.type(screen.getByLabelText(/^password/i), 'password')
    await userEvent.type(screen.getByLabelText(/^repetir password/i), 'password')
    await userEvent.click(screen.getByRole('button', { name: /registrarse/i }))

    expect(screen.getByText(/no cumple los requisitos/i)).toBeInTheDocument()
    expect(registrarCliente).not.toHaveBeenCalled()
  })

  it('register_siElTelefonoNoTieneNueveDigitos_muestraErrorYNoLlamaAlServicio', async () => {
    await renderYElegirCliente()
    await rellenarFormularioValido()
    await userEvent.type(screen.getByLabelText(/teléfono/i), '61234')
    await userEvent.click(screen.getByRole('button', { name: /registrarse/i }))

    expect(screen.getByText(/teléfono válido/i)).toBeInTheDocument()
    expect(registrarCliente).not.toHaveBeenCalled()
  })

  it('register_siElTelefonoContieneLetras_muestraErrorYNoLlamaAlServicio', async () => {
    await renderYElegirCliente()
    await rellenarFormularioValido()
    await userEvent.type(screen.getByLabelText(/teléfono/i), '61234567a')
    await userEvent.click(screen.getByRole('button', { name: /registrarse/i }))

    expect(screen.getByText(/teléfono válido/i)).toBeInTheDocument()
    expect(registrarCliente).not.toHaveBeenCalled()
  })

  it('register_conTelefonoDeNueveDigitos_llamaAlServicio', async () => {
    registrarCliente.mockResolvedValue({})
    await renderYElegirCliente()
    await rellenarFormularioValido()
    await userEvent.type(screen.getByLabelText(/teléfono/i), '612345678')
    await userEvent.click(screen.getByRole('button', { name: /registrarse/i }))

    expect(registrarCliente).toHaveBeenCalledWith(
      expect.objectContaining({ telefono: '612345678' })
    )
  })

  it('register_alElegirCliente_limitaLaLongitudDeLosCampos', async () => {
    await renderYElegirCliente()

    expect(screen.getByLabelText(/^nombre/i)).toHaveAttribute('maxlength', '30')
    expect(screen.getByLabelText(/apellidos/i)).toHaveAttribute('maxlength', '30')
    expect(screen.getByLabelText(/email/i)).toHaveAttribute('maxlength', '100')
    expect(screen.getByLabelText(/^password/i)).toHaveAttribute('maxlength', '64')
    expect(screen.getByLabelText(/^repetir password/i)).toHaveAttribute('maxlength', '64')
  })

  it('register_alElegirVendedor_limitaLaLongitudDelNombreComercial', async () => {
    await renderYElegirVendedor()

    expect(screen.getByLabelText(/nombre comercial/i)).toHaveAttribute('maxlength', '30')
  })

  it('register_alEscribirMasDelLimite_cortaElTextoEnElMaximo', async () => {
    await renderYElegirCliente()
    const nombre = screen.getByLabelText(/^nombre/i)

    await userEvent.type(nombre, 'a'.repeat(35))

    expect(nombre).toHaveValue('a'.repeat(30))
  })

  it('register_alElegirCliente_muestraLaPoliticaDePrivacidadComoObligatoria', async () => {
    await renderYElegirCliente()

    expect(screen.getByRole('checkbox', { name: /acepto la política de privacidad/i })).toBeRequired()
  })

  it('register_alElegirVendedor_muestraLaPoliticaDePrivacidadComoObligatoria', async () => {
    await renderYElegirVendedor()

    expect(screen.getByRole('checkbox', { name: /acepto la política de privacidad/i })).toBeRequired()
  })

  it('register_siElClienteNoAceptaLaPoliticaDePrivacidad_muestraErrorYNoLlamaAlServicio', async () => {
    await renderYElegirCliente()
    await rellenarFormularioValido()
    await userEvent.click(screen.getByRole('checkbox', { name: /política de privacidad/i }))
    await userEvent.click(screen.getByRole('button', { name: /registrarse/i }))

    expect(screen.getByText(/debes aceptar la política de privacidad/i)).toBeInTheDocument()
    expect(registrarCliente).not.toHaveBeenCalled()
  })

  it('register_siElVendedorNoAceptaLaPoliticaDePrivacidad_muestraErrorYNoLlamaAlServicio', async () => {
    await renderYElegirVendedor()
    await rellenarFormularioVendedorValido()
    await userEvent.click(screen.getByRole('checkbox', { name: /política de privacidad/i }))
    await userEvent.click(screen.getByRole('button', { name: /registrarse/i }))

    expect(screen.getByText(/debes aceptar la política de privacidad/i)).toBeInTheDocument()
    expect(registrarVendedor).not.toHaveBeenCalled()
  })

  it('register_alEnviarFormularioClienteVacio_muestraErroresDeCamposObligatorios', async () => {
    await renderYElegirCliente()
    await userEvent.click(screen.getByRole('button', { name: /registrarse/i }))

    expect(screen.getAllByText(/obligatorio/i)).toHaveLength(7)
  })

  it('register_alIntroducirFechaDeMenorDeEdad_muestraErrorDeEdad', async () => {
    await renderYElegirCliente()
    await userEvent.type(screen.getByLabelText(/fecha de nacimiento/i), '2020-01-01')
    await userEvent.click(screen.getByRole('button', { name: /registrarse/i }))

    expect(screen.getByText(/mayor de edad/i)).toBeInTheDocument()
  })

  it('register_alIntroducirEmailInvalido_muestraErrorDeFormato', async () => {
    await renderYElegirCliente()
    await userEvent.type(screen.getByLabelText(/email/i), 'esto-no-es-un-email')
    await userEvent.click(screen.getByRole('button', { name: /registrarse/i }))

    expect(screen.getByText(/email válido/i)).toBeInTheDocument()
  })

  it('register_alIntroducirDniInvalido_muestraErrorDeFormato', async () => {
    await renderYElegirCliente()
    await userEvent.type(screen.getByLabelText(/dni/i), '1234')
    await userEvent.click(screen.getByRole('button', { name: /registrarse/i }))

    expect(screen.getByText(/dni válido/i)).toBeInTheDocument()
  })

  it('register_alElegirCliente_muestraTipoConNormalPreseleccionado', async () => {
    await renderYElegirCliente()

    expect(screen.getByLabelText(/tipo/i)).toHaveValue('normal')
    expect(screen.getByRole('option', { name: /premium/i })).toBeInTheDocument()
  })

  it('register_alEnviarDatosValidosDeCliente_llamaAlServicioConLosDatos', async () => {
    registrarCliente.mockResolvedValue({})
    await renderYElegirCliente()
    await rellenarFormularioValido()
    await userEvent.click(screen.getByRole('button', { name: /registrarse/i }))

    expect(registrarCliente).toHaveBeenCalledWith(
      expect.objectContaining({ nombre: 'Ana', email: 'ana@correo.com' })
    )
  })

  it('register_siElEmailYaEstaRegistrado_muestraErrorEnElCampoEmail', async () => {
    registrarCliente.mockRejectedValue({ status: 409 })
    await renderYElegirCliente()
    await rellenarFormularioValido()
    await userEvent.click(screen.getByRole('button', { name: /registrarse/i }))

    expect(await screen.findByText(/ya está registrado/i)).toBeInTheDocument()
  })

  it('register_siElRegistroEsCorrecto_muestraMensajeDeConfirmacion', async () => {
    registrarCliente.mockResolvedValue({})
    await renderYElegirCliente()
    await rellenarFormularioValido()
    await userEvent.click(screen.getByRole('button', { name: /registrarse/i }))

    expect(await screen.findByText(/cuenta creada/i)).toBeInTheDocument()
  })

  it('register_alElegirCliente_marcaComoRequeridosLosCamposObligatorios', async () => {
    await renderYElegirCliente()

    expect(screen.getByLabelText(/nombre/i)).toBeRequired()
    expect(screen.getByLabelText(/apellidos/i)).toBeRequired()
    expect(screen.getByLabelText(/fecha de nacimiento/i)).toBeRequired()
    expect(screen.getByLabelText(/dni/i)).toBeRequired()
    expect(screen.getByLabelText(/email/i)).toBeRequired()
    expect(screen.getByLabelText(/^password/i)).toBeRequired()
    expect(screen.getByLabelText(/^repetir password/i)).toBeRequired()
    expect(screen.getByLabelText(/tipo/i)).toBeRequired()
    expect(screen.getByLabelText(/teléfono/i)).not.toBeRequired()
  })

  it('register_alElegirCliente_permiteElegirUnAvatarYLoEnviaConLosDatos', async () => {
    registrarCliente.mockResolvedValue({})
    await renderYElegirCliente()

    expect(screen.getAllByRole('radio')).toHaveLength(3)

    await userEvent.click(screen.getByRole('radio', { name: /avatar 2/i }))
    expect(screen.getByRole('radio', { name: /avatar 2/i })).toBeChecked()

    await rellenarFormularioValido()
    await userEvent.click(screen.getByRole('button', { name: /registrarse/i }))

    expect(registrarCliente).toHaveBeenCalledWith(
      expect.objectContaining({ avatar: 'avatar2' })
    )
  })

  it('register_siLosPasswordsNoCoinciden_muestraErrorYNoLlamaAlServicio', async () => {
    await renderYElegirCliente()
    await rellenarFormularioValido()
    await userEvent.type(screen.getByLabelText(/^repetir password/i), 'Extra')
    await userEvent.click(screen.getByRole('button', { name: /registrarse/i }))

    expect(screen.getByText(/los passwords no coinciden/i)).toBeInTheDocument()
    expect(registrarCliente).not.toHaveBeenCalled()
  })

  it('register_siRepetirPasswordEstaVacio_muestraErrorDeObligatorioYNoDeCoincidencia', async () => {
    await renderYElegirCliente()
    await rellenarFormularioValido()
    await userEvent.clear(screen.getByLabelText(/^repetir password/i))
    await userEvent.click(screen.getByRole('button', { name: /registrarse/i }))

    expect(screen.getByText(/obligatorio/i)).toBeInTheDocument()
    expect(screen.queryByText(/no coinciden/i)).not.toBeInTheDocument()
    expect(registrarCliente).not.toHaveBeenCalled()
  })

  it('register_siPasswordEstaVacio_muestraErrorDeObligatorioYNoDeCoincidencia', async () => {
    await renderYElegirCliente()
    await rellenarFormularioValido()
    await userEvent.clear(screen.getByLabelText(/^password/i))
    await userEvent.click(screen.getByRole('button', { name: /registrarse/i }))

    expect(screen.getByText(/obligatorio/i)).toBeInTheDocument()
    expect(screen.queryByText(/no coinciden/i)).not.toBeInTheDocument()
    expect(registrarCliente).not.toHaveBeenCalled()
  })
})