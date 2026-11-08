import { screen, within } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import { Route, Routes } from 'react-router-dom'
import { registrarCliente, registrarVendedor } from '../api/auth'
import { getCategories } from '../api/products'
import RegistroPremiumFlow from '../features/account/RegistroPremiumFlow'
import LocationProbe from '../test/LocationProbe'
import { elegirCategoria, renderWithProviders } from '../test/utils'
import Register from './Register'

vi.mock('../api/auth', () => ({
  registrarCliente: vi.fn(),
  registrarVendedor: vi.fn(),
}))

vi.mock('../api/products', () => ({
  getCategories: vi.fn(),
}))

// El registro vive dentro del flujo que guarda en memoria los datos mientras el cliente Premium pasa por la
// pasarela (HU45). Aquí la pasarela es un marcador: lo que se comprueba es que se llega a ella.
function renderRegister() {
  return renderWithProviders(
    <>
      <Routes>
        <Route element={<RegistroPremiumFlow />}>
          <Route path="/register" element={<Register />} />
          <Route path="/register/pago" element={<p>Pasarela de prueba</p>} />
        </Route>
      </Routes>
      <LocationProbe />
    </>,
    { route: '/register' },
  )
}

async function renderYElegirCliente() {
  renderRegister()
  await userEvent.click(screen.getByRole('button', { name: /cliente/i }))
}

async function renderYElegirVendedor() {
  renderRegister()
  await userEvent.click(screen.getByRole('button', { name: /vendedor/i }))
}

async function rellenarFormularioVendedorValido() {
  await userEvent.type(screen.getByLabelText(/^nombre(?! comercial)/i), 'Luis')
  await userEvent.type(screen.getByLabelText(/apellidos/i), 'Pérez')
  await userEvent.type(screen.getByLabelText(/nombre comercial/i), 'Tienda Luis')
  await elegirCategoria(userEvent, /tecnología/i)
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
    renderRegister()

    expect(screen.getByRole('heading', { level: 1 })).toBeInTheDocument()
    expect(screen.getByText(/cliente/i)).toBeInTheDocument()
    expect(screen.getByText(/vendedor/i)).toBeInTheDocument()
  })

  it('register_alElegirCliente_muestraFormularioDeCliente', async () => {
    await renderYElegirCliente()

    expect(screen.getByRole('heading', { name: /registro de cliente/i })).toBeInTheDocument()
  })

  it('register_alElegirVendedor_muestraFormularioDeVendedor', async () => {
    renderRegister()

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

  it('register_alElegirVendedor_muestraUnDesplegableConLasCategoriasYSoloPermiteElegirUna', async () => {
    await renderYElegirVendedor()

    await userEvent.click(screen.getByRole('combobox', { name: /categoría/i }))
    expect((await screen.findAllByRole('option')).map((opcion) => opcion.textContent)).toEqual(['Tecnología', 'Hogar'])
    await userEvent.click(screen.getByRole('option', { name: /tecnología/i }))
    await elegirCategoria(userEvent, /hogar/i)

    expect(screen.getByRole('combobox', { name: /categoría/i })).toHaveValue('Hogar')
  })

  it('register_alElegirVendedor_permiteElegirUnAvatar', async () => {
    await renderYElegirVendedor()

    expect(screen.getAllByRole('radio', { name: /avatar/i })).toHaveLength(3)

    await userEvent.click(screen.getByRole('radio', { name: /avatar 2/i }))

    expect(screen.getByRole('radio', { name: /avatar 2/i })).toBeChecked()
  })

  it('register_alElegirVendedor_marcaLaCategoriaComoObligatoria', async () => {
    await renderYElegirVendedor()

    expect(screen.getByRole('combobox', { name: /categoría/i })).toBeRequired()
  })

  it('register_alEnviarFormularioVendedorVacio_muestraErroresDeCamposObligatoriosYDeCategoria', async () => {
    await renderYElegirVendedor()
    await userEvent.click(screen.getByRole('button', { name: /registrarse/i }))

    expect(screen.getAllByText(/obligatorio/i)).toHaveLength(7)
    expect(screen.getByText(/elige una categoría/i)).toBeInTheDocument()
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
        categoriaPrincipalId: 'cat-1',
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

  it('register_siElRegistroDeVendedorEsCorrecto_avisaDeQueLaCuentaQuedaPendienteDeVerificacion', async () => {
    registrarVendedor.mockResolvedValue({})
    await renderYElegirVendedor()
    await rellenarFormularioVendedorValido()
    await userEvent.click(screen.getByRole('button', { name: /registrarse/i }))

    expect(
      await screen.findByText(/pendiente de que un administrador la verifique/i)
    ).toBeInTheDocument()
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

  it('register_alElegirCliente_muestraTipoEstandarPreseleccionadoConLosValoresDelBackend', async () => {
    await renderYElegirCliente()

    expect(screen.getByLabelText(/tipo/i)).toHaveValue('ESTANDAR')
    expect(screen.getByRole('option', { name: /estándar/i })).toHaveValue('ESTANDAR')
    expect(screen.getByRole('option', { name: /premium/i })).toHaveValue('PREMIUM')
  })

  it('register_alEnviarDatosValidosDeCliente_enviaElTipoEstandarPorDefecto', async () => {
    registrarCliente.mockResolvedValue({})
    await renderYElegirCliente()
    await rellenarFormularioValido()
    await userEvent.click(screen.getByRole('button', { name: /registrarse/i }))

    expect(registrarCliente).toHaveBeenCalledWith(expect.objectContaining({ tipoCliente: 'ESTANDAR' }))
  })

  // HU45 (T420): elegir Premium no registra al momento; antes hay que pasar por la pasarela de pago simulada
  it('HU45-CP21: al elegir Premium, Registrarse no envía el registro y lleva a la pasarela de pago', async () => {
    await renderYElegirCliente()
    await rellenarFormularioValido()
    await userEvent.selectOptions(screen.getByLabelText(/tipo/i), 'PREMIUM')
    await userEvent.click(screen.getByRole('button', { name: /registrarse/i }))

    expect(await screen.findByText('Pasarela de prueba')).toBeInTheDocument()
    expect(screen.getByTestId('ruta-actual')).toHaveTextContent(/^\/register\/pago$/)
    expect(registrarCliente).not.toHaveBeenCalled()
  })

  it('HU45-CP21: con datos no válidos, elegir Premium marca los errores y no lleva a la pasarela', async () => {
    await renderYElegirCliente()
    await userEvent.selectOptions(screen.getByLabelText(/tipo/i), 'PREMIUM')
    await userEvent.click(screen.getByRole('button', { name: /registrarse/i }))

    expect(screen.getAllByText(/obligatorio/i).length).toBeGreaterThan(0)
    expect(screen.getByTestId('ruta-actual')).toHaveTextContent(/^\/register$/)
    expect(screen.queryByText('Pasarela de prueba')).not.toBeInTheDocument()
    expect(registrarCliente).not.toHaveBeenCalled()
  })

  it('register_siElClienteNoIndicaTelefono_noEnviaElTelefono', async () => {
    registrarCliente.mockResolvedValue({})
    await renderYElegirCliente()
    await rellenarFormularioValido()
    await userEvent.click(screen.getByRole('button', { name: /registrarse/i }))

    expect(registrarCliente).toHaveBeenCalledTimes(1)
    expect(registrarCliente.mock.calls[0][0]).not.toHaveProperty('telefono')
  })

  it('register_siElVendedorNoIndicaTelefono_noEnviaElTelefono', async () => {
    registrarVendedor.mockResolvedValue({})
    await renderYElegirVendedor()
    await rellenarFormularioVendedorValido()
    await userEvent.click(screen.getByRole('button', { name: /registrarse/i }))

    expect(registrarVendedor).toHaveBeenCalledTimes(1)
    expect(registrarVendedor.mock.calls[0][0]).not.toHaveProperty('telefono')
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
    // El backend acompaña cada 409 de su code (PR #104)
    registrarCliente.mockRejectedValue({ status: 409, code: 'EMAIL_ALREADY_REGISTERED' })
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

  it('register_siElRegistroEsCorrecto_avisaDeQueLaCuentaQuedaPendienteDeVerificacion', async () => {
    registrarCliente.mockResolvedValue({})
    await renderYElegirCliente()
    await rellenarFormularioValido()
    await userEvent.click(screen.getByRole('button', { name: /registrarse/i }))

    expect(
      await screen.findByText(/pendiente de que un administrador la verifique/i)
    ).toBeInTheDocument()
    expect(screen.getByText(/podrás iniciar sesión cuando esté activada/i)).toBeInTheDocument()
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

  // Fallos de la demo del Sprint 1: el formulario marca en su campo el motivo que da el backend (PR #104)

  it('HU1-CP23: una contraseña con datos personales se marca en el campo de la contraseña', async () => {
    registrarCliente.mockRejectedValue({ status: 400, code: 'PASSWORD_CONTAINS_PERSONAL_DATA' })
    await renderYElegirCliente()
    await rellenarFormularioValido()
    await userEvent.click(screen.getByRole('button', { name: /registrarse/i }))

    expect(await screen.findByText('La contraseña no puede contener tu nombre, tus apellidos ni tu email'))
      .toBeInTheDocument()
    expect(screen.getByLabelText(/^password/i)).toHaveAccessibleDescription(
      'La contraseña no puede contener tu nombre, tus apellidos ni tu email'
    )
  })

  it('HU1-CP23: un DNI ya registrado se marca en el DNI y no en el email', async () => {
    registrarCliente.mockRejectedValue({ status: 409, code: 'DNI_ALREADY_REGISTERED' })
    await renderYElegirCliente()
    await rellenarFormularioValido()
    await userEvent.click(screen.getByRole('button', { name: /registrarse/i }))

    expect(await screen.findByText('Este DNI ya está registrado')).toBeInTheDocument()
    expect(screen.getByLabelText(/dni/i)).toHaveAccessibleDescription('Este DNI ya está registrado')
    expect(screen.queryByText(/email ya está registrado/i)).not.toBeInTheDocument()
  })

  it('HU1-CP23: en el vendedor, un DNI ya registrado se marca en el DNI', async () => {
    registrarVendedor.mockRejectedValue({ status: 409, code: 'DNI_ALREADY_REGISTERED' })
    await renderYElegirVendedor()
    await rellenarFormularioVendedorValido()
    await userEvent.click(screen.getByRole('button', { name: /registrarse/i }))

    expect(await screen.findByText('Este DNI ya está registrado')).toBeInTheDocument()
    expect(screen.getByLabelText(/dni/i)).toHaveAccessibleDescription('Este DNI ya está registrado')
  })

  it('HU1-CP23: un error sin campo muestra un mensaje general', async () => {
    registrarCliente.mockRejectedValue({ status: 400, code: 'VALIDATION_ERROR' })
    await renderYElegirCliente()
    await rellenarFormularioValido()
    await userEvent.click(screen.getByRole('button', { name: /registrarse/i }))

    expect(await screen.findByText('Revisa los datos del formulario')).toBeInTheDocument()
  })

  it('HU1-CP24: tras registrarse ofrece ir a iniciar sesión', async () => {
    registrarCliente.mockResolvedValue({})
    await renderYElegirCliente()
    await rellenarFormularioValido()
    await userEvent.click(screen.getByRole('button', { name: /registrarse/i }))

    const enlace = await screen.findByRole('link', { name: /ir a iniciar sesión/i })
    expect(enlace).toHaveAttribute('href', '/login')
  })

  it('HU1-CP27: en el registro de cliente no se puede pegar el password ni su repetición', async () => {
    await renderYElegirCliente()

    for (const campo of [screen.getByLabelText(/^password/i), screen.getByLabelText(/^repetir password/i)]) {
      await userEvent.click(campo)
      await userEvent.paste('Password123!')
      expect(campo).toHaveValue('')
    }
  })

  it('HU1-CP27: en el registro de vendedor no se puede pegar el password ni su repetición', async () => {
    await renderYElegirVendedor()

    for (const campo of [screen.getByLabelText(/^password/i), screen.getByLabelText(/^repetir password/i)]) {
      await userEvent.click(campo)
      await userEvent.paste('Password123!')
      expect(campo).toHaveValue('')
    }
  })

  it('HU1-CP27: el password escrito a mano en los dos campos sigue permitiendo registrarse', async () => {
    registrarCliente.mockResolvedValue({})
    await renderYElegirCliente()
    await rellenarFormularioValido()
    await userEvent.click(screen.getByRole('button', { name: /registrarse/i }))

    expect(registrarCliente).toHaveBeenCalledTimes(1)
  })
})
