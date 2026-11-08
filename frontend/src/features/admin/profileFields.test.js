import { describe, it, expect } from 'vitest'
import { ROLES } from '../../auth/roles'
import {
  initialValues,
  profileFieldsFor,
  profilePayload,
  readFields,
  validateProfile,
} from './profileFields'

const CLIENTE = {
  id: 'usr-3',
  nombre: 'Carlos',
  apellidos: 'Moreno Gil',
  email: 'carlos.moreno@esibuy.test',
  estado: 'ACTIVO',
  fechaAlta: '2026-02-03T11:30:00',
  dni: '10004119A',
  telefono: '620023757',
  fechaNacimiento: '1978-04-13',
  tipoCliente: 'PREMIUM',
  avatar: 'avatar1',
}

const ADMINISTRADOR = {
  id: 'usr-11',
  nombre: 'Diego',
  apellidos: 'Delgado Iglesias',
  email: 'diego.delgado@esibuy.test',
  estado: 'DESACTIVADO',
  fechaAlta: '2026-01-20T13:25:00',
  sede: 'Cuenca',
  fechaIncorporacion: '2026-01-20',
  avatar: 'avatar3',
}

const VENDEDOR = {
  id: 'u-2',
  nombre: 'Vera',
  apellidos: 'Vendedora',
  email: 'vendedor@esibuy.test',
  nombreComercial: 'TecnoVera',
  dni: '12345601V',
  telefono: '612345601',
  avatar: 'avatar2',
  categoriaPrincipal: 'Tecnología',
  categoriaPrincipalId: 'cat-1',
  estado: 'ACTIVO',
  fechaAlta: '2026-09-14',
}

const nombresEditables = (rol) =>
  profileFieldsFor(rol)
    .filter((campo) => campo.edit)
    .map((campo) => campo.name)

const hacerAnios = (anios, dias = 0) => {
  const fecha = new Date()
  fecha.setFullYear(fecha.getFullYear() - anios)
  fecha.setDate(fecha.getDate() + dias)
  const mes = String(fecha.getMonth() + 1).padStart(2, '0')
  const dia = String(fecha.getDate()).padStart(2, '0')
  return `${fecha.getFullYear()}-${mes}-${dia}`
}

describe('campos editables por tipo de usuario (HU22)', () => {
  it('profileFields_deUnCliente_permiteModificarTodoMenosElEmail', () => {
    expect(nombresEditables(ROLES.CLIENTE)).toEqual([
      'nombre',
      'apellidos',
      'dni',
      'telefono',
      'fechaNacimiento',
      'tipoCliente',
      'avatar',
    ])
    expect(nombresEditables(ROLES.CLIENTE)).not.toContain('email')
  })

  it('profileFields_deUnAdministrador_noPermiteModificarEmailNiFechaDeIncorporacion', () => {
    expect(nombresEditables(ROLES.ADMIN)).toEqual(['nombre', 'apellidos', 'sede', 'avatar'])
  })

  // El backend guarda el perfil completo: lo que no se envía se borra, así que el teléfono y la imagen también
  // se editan aquí. El DNI se llama igual que en el registro del vendedor (T241).
  it('profileFields_deUnVendedor_permiteModificarTodosSusDatosMenosElEmail', () => {
    expect(nombresEditables(ROLES.VENDEDOR)).toEqual([
      'nombre',
      'apellidos',
      'nombreComercial',
      'dni',
      'telefono',
      'categoriaPrincipalId',
      'avatar',
    ])
  })

  it.each([ROLES.CLIENTE, ROLES.ADMIN, ROLES.VENDEDOR])('profileFields_de_%s_nuncaPermiteModificarLaContrasena', (rol) => {
    const nombres = profileFieldsFor(rol).map((campo) => campo.name.toLowerCase())

    expect(nombres.some((nombre) => nombre.includes('password') || nombre.includes('contrasena'))).toBe(false)
  })

  const soloLectura = (rol) =>
    profileFieldsFor(rol)
      .filter((campo) => !campo.edit)
      .map((campo) => campo.name)

  it.each([ROLES.CLIENTE, ROLES.ADMIN, ROLES.VENDEDOR])('profileFields_de_%s_elEstadoEsDeSoloLectura', (rol) => {
    expect(soloLectura(rol)).toContain('estado')
  })

  // El rol de una cuenta no se puede cambiar ("el que nace cliente muere cliente", decisión del equipo).
  it.each([ROLES.CLIENTE, ROLES.ADMIN, ROLES.VENDEDOR])('profileFields_de_%s_elRolEsDeSoloLectura', (rol) => {
    expect(soloLectura(rol)).toContain('rol')
  })

  it.each([ROLES.CLIENTE, ROLES.ADMIN, ROLES.VENDEDOR])('profileFields_de_%s_elEmailEsDeSoloLectura', (rol) => {
    expect(soloLectura(rol)).toContain('email')
  })

  it('profileFields_soloElAdministradorTieneFechaDeAlta', () => {
    expect(soloLectura(ROLES.ADMIN)).toContain('fechaAlta')
    expect(profileFieldsFor(ROLES.CLIENTE).map((campo) => campo.name)).not.toContain('fechaAlta')
    expect(profileFieldsFor(ROLES.VENDEDOR).map((campo) => campo.name)).not.toContain('fechaAlta')
  })

  it('profileFields_conUnRolDesconocido_muestraSoloLosDatosComunes', () => {
    expect(profileFieldsFor('OTRO').map((campo) => campo.name)).toEqual([
      'nombre',
      'apellidos',
      'email',
      'estado',
    ])
  })
})

describe('readFields', () => {
  it('readFields_deUnCliente_muestraSusDatosEnTexto', () => {
    const campos = Object.fromEntries(readFields(ROLES.CLIENTE, CLIENTE))

    expect(campos).toMatchObject({
      Nombre: 'Carlos',
      Email: 'carlos.moreno@esibuy.test',
      DNI: '10004119A',
      'Fecha de nacimiento': '13/04/1978',
      'Tipo de cliente': 'Premium',
      'Imagen de perfil': 'Avatar 1',
      Estado: 'Activo',
    })
  })

  it('readFields_soloLaFichaDelAdministradorIncluyeLaFechaDeAlta', () => {
    const etiquetas = (rol, usuario) => readFields(rol, usuario).map(([etiqueta]) => etiqueta)

    expect(etiquetas(ROLES.ADMIN, ADMINISTRADOR)).toContain('Fecha de alta')
    expect(etiquetas(ROLES.CLIENTE, CLIENTE)).not.toContain('Fecha de alta')
    expect(etiquetas(ROLES.VENDEDOR, VENDEDOR)).not.toContain('Fecha de alta')
  })

  it('readFields_conDatosQueFaltan_muestraUnGuion', () => {
    const campos = Object.fromEntries(readFields(ROLES.CLIENTE, { ...CLIENTE, telefono: '', avatar: undefined }))

    expect(campos.Teléfono).toBe('—')
    expect(campos['Imagen de perfil']).toBe('Sin elegir')
  })

  it('readFields_incluyeElRolDeLaCuentaEnTexto', () => {
    expect(Object.fromEntries(readFields(ROLES.CLIENTE, CLIENTE)).Rol).toBe('Cliente')
    expect(Object.fromEntries(readFields(ROLES.ADMIN, ADMINISTRADOR)).Rol).toBe('Administrador')
    expect(Object.fromEntries(readFields(ROLES.VENDEDOR, VENDEDOR)).Rol).toBe('Vendedor')
  })
})

describe('initialValues', () => {
  it('initialValues_deUnCliente_devuelveSusDatosComoTexto', () => {
    expect(initialValues(ROLES.CLIENTE, CLIENTE)).toEqual({
      nombre: 'Carlos',
      apellidos: 'Moreno Gil',
      dni: '10004119A',
      telefono: '620023757',
      fechaNacimiento: '1978-04-13',
      tipoCliente: 'PREMIUM',
      avatar: 'avatar1',
    })
  })

  it('initialValues_soloIncluyeLosCamposModificablesDeSuRol', () => {
    expect(Object.keys(initialValues(ROLES.ADMIN, ADMINISTRADOR))).toEqual(['nombre', 'apellidos', 'sede', 'avatar'])
  })

  it('initialValues_deUnClienteSinTipoDeCliente_proponeEstandar', () => {
    expect(initialValues(ROLES.CLIENTE, { ...CLIENTE, tipoCliente: undefined }).tipoCliente).toBe('ESTANDAR')
  })

  it('initialValues_conDatosQueFaltan_losDejaVacios', () => {
    expect(initialValues(ROLES.ADMIN, { ...ADMINISTRADOR, sede: undefined, avatar: undefined })).toMatchObject({
      sede: '',
      avatar: '',
    })
  })

  it('initialValues_deUnVendedor_usaElIdentificadorDeLaCategoria', () => {
    expect(initialValues(ROLES.VENDEDOR, VENDEDOR).categoriaPrincipalId).toBe('cat-1')
  })
})

describe('validateProfile', () => {
  const valoresDeCliente = () => initialValues(ROLES.CLIENTE, CLIENTE)

  it('validateProfile_conLosDatosActuales_noDevuelveErrores', () => {
    expect(validateProfile(ROLES.CLIENTE, valoresDeCliente())).toEqual({})
    expect(validateProfile(ROLES.ADMIN, initialValues(ROLES.ADMIN, ADMINISTRADOR))).toEqual({})
    expect(validateProfile(ROLES.VENDEDOR, initialValues(ROLES.VENDEDOR, VENDEDOR))).toEqual({})
  })

  it('validateProfile_conCamposObligatoriosVacios_devuelveTodosLosErroresALaVez', () => {
    const errores = validateProfile(ROLES.CLIENTE, {
      ...valoresDeCliente(),
      nombre: ' ',
      apellidos: '',
      dni: '',
      fechaNacimiento: '',
    })

    expect(Object.keys(errores).sort((a, b) => a.localeCompare(b))).toEqual([
      'apellidos',
      'dni',
      'fechaNacimiento',
      'nombre',
    ])
  })

  it.each(['1234567', '12345678', 'ABCDEFGHI', '123456789A'])('validateProfile_conDni_%s_devuelveError', (dni) => {
    expect(validateProfile(ROLES.CLIENTE, { ...valoresDeCliente(), dni }).dni).toMatch(/dni válido/i)
  })

  it.each(['', '12345', '12345678a', '1234567890'])('validateProfile_conTelefono_%s', (telefono) => {
    const error = validateProfile(ROLES.CLIENTE, { ...valoresDeCliente(), telefono }).telefono

    if (telefono === '') expect(error).toBeUndefined()
    else expect(error).toMatch(/teléfono válido/i)
  })

  it('validateProfile_conFechaDeNacimientoDeUnMenor_devuelveError', () => {
    const errores = validateProfile(ROLES.CLIENTE, { ...valoresDeCliente(), fechaNacimiento: hacerAnios(17) })

    expect(errores.fechaNacimiento).toMatch(/mayor de edad/i)
  })

  it('validateProfile_conFechaDeNacimientoDeUnMayor_laAcepta', () => {
    expect(validateProfile(ROLES.CLIENTE, { ...valoresDeCliente(), fechaNacimiento: hacerAnios(30) })).toEqual({})
  })

  it('validateProfile_conExactamente18Anios_laAcepta', () => {
    expect(validateProfile(ROLES.CLIENTE, { ...valoresDeCliente(), fechaNacimiento: hacerAnios(18) })).toEqual({})
  })

  it('validateProfile_aUnDiaDeCumplir18_devuelveError', () => {
    const errores = validateProfile(ROLES.CLIENTE, { ...valoresDeCliente(), fechaNacimiento: hacerAnios(18, 1) })

    expect(errores.fechaNacimiento).toBeDefined()
  })

  it('validateProfile_deUnAdministrador_laSedeEsOpcional', () => {
    expect(validateProfile(ROLES.ADMIN, { ...initialValues(ROLES.ADMIN, ADMINISTRADOR), sede: '' })).toEqual({})
  })

  it('validateProfile_deUnVendedor_exigeNombreComercialDniYCategoriaPeroNoTelefono', () => {
    const errores = validateProfile(ROLES.VENDEDOR, {
      ...initialValues(ROLES.VENDEDOR, VENDEDOR),
      nombreComercial: '',
      dni: '',
      telefono: '',
      categoriaPrincipalId: '',
    })

    expect(Object.keys(errores).sort((a, b) => a.localeCompare(b))).toEqual([
      'categoriaPrincipalId',
      'dni',
      'nombreComercial',
    ])
  })

  it('validateProfile_deUnVendedorConDniIncorrecto_devuelveElMismoErrorQueUnCliente', () => {
    const errores = validateProfile(ROLES.VENDEDOR, { ...initialValues(ROLES.VENDEDOR, VENDEDOR), dni: 'B12345601' })

    expect(errores.dni).toMatch(/dni válido \(8 números y una letra\)/i)
  })
})

describe('profilePayload', () => {
  it('profilePayload_deUnCliente_enviaSoloLosCamposModificablesYSinEmail', () => {
    const payload = profilePayload(ROLES.CLIENTE, initialValues(ROLES.CLIENTE, CLIENTE))

    expect(Object.keys(payload)).toEqual([
      'nombre',
      'apellidos',
      'dni',
      'telefono',
      'fechaNacimiento',
      'tipoCliente',
      'avatar',
    ])
  })

  it('profilePayload_deUnAdministrador_noEnviaEmailNiFechaDeIncorporacion', () => {
    const claves = Object.keys(profilePayload(ROLES.ADMIN, initialValues(ROLES.ADMIN, ADMINISTRADOR)))

    expect(claves).toEqual(['nombre', 'apellidos', 'sede', 'avatar'])
  })

  it('profilePayload_recortaLosEspaciosDeLosTextos', () => {
    const payload = profilePayload(ROLES.ADMIN, {
      nombre: '  Diego ',
      apellidos: ' D ',
      sede: ' Cuenca ',
      avatar: 'avatar3',
    })

    expect(payload).toEqual({ nombre: 'Diego', apellidos: 'D', sede: 'Cuenca', avatar: 'avatar3' })
  })

  it('profilePayload_deUnVendedor_enviaUnaSolaCategoriaPorIdentificadorYNuncaElEmail', () => {
    expect(profilePayload(ROLES.VENDEDOR, initialValues(ROLES.VENDEDOR, VENDEDOR))).toEqual({
      nombre: 'Vera',
      apellidos: 'Vendedora',
      nombreComercial: 'TecnoVera',
      dni: '12345601V',
      telefono: '612345601',
      categoriaPrincipalId: 'cat-1',
      avatar: 'avatar2',
    })
  })
})

describe('el rol no se puede cambiar (HU22)', () => {
  it('profilePayload_nuncaEnviaElRolAunqueVengaEnLosValores', () => {
    const valores = { ...initialValues(ROLES.CLIENTE, CLIENTE), rol: ROLES.ADMIN }

    expect(profilePayload(ROLES.CLIENTE, valores)).not.toHaveProperty('rol')
  })

  it('validateProfile_conUnRolEnLosValores_validaConLosCamposDelRolDeLaCuenta', () => {
    const valores = { ...initialValues(ROLES.CLIENTE, CLIENTE), rol: ROLES.VENDEDOR, dni: '' }

    expect(Object.keys(validateProfile(ROLES.CLIENTE, valores))).toEqual(['dni'])
  })
})

describe('campos del perfil propio (HU45)', () => {
  const PROPIO = { own: true }
  const campo = (rol, nombre) => profileFieldsFor(rol, PROPIO).find((candidato) => candidato.name === nombre)
  const nombresDeSoloLectura = (rol) =>
    profileFieldsFor(rol, PROPIO)
      .filter((candidato) => !candidato.edit)
      .map((candidato) => candidato.name)

  it('HU45-CP11: el cliente edita sus datos personales y ve en solo lectura el email, el DNI, el tipo y el rol', () => {
    const editables = profileFieldsFor(ROLES.CLIENTE, PROPIO)
      .filter((candidato) => candidato.edit)
      .map((candidato) => candidato.name)

    expect(editables).toEqual(['nombre', 'apellidos', 'telefono', 'fechaNacimiento', 'avatar'])
    expect(nombresDeSoloLectura(ROLES.CLIENTE)).toEqual(['email', 'rol', 'dni', 'tipoCliente'])
  })

  it('HU45-CP11: el vendedor edita sus datos y su negocio y ve en solo lectura el email, el DNI y el rol', () => {
    const editables = profileFieldsFor(ROLES.VENDEDOR, PROPIO)
      .filter((candidato) => candidato.edit)
      .map((candidato) => candidato.name)

    expect(editables).toEqual([
      'nombre',
      'apellidos',
      'nombreComercial',
      'telefono',
      'categoriaPrincipalId',
      'avatar',
    ])
    expect(nombresDeSoloLectura(ROLES.VENDEDOR)).toEqual(['email', 'rol', 'dni'])
  })

  it('HU45-CP11: el perfil propio no muestra el estado de la cuenta', () => {
    expect(campo(ROLES.CLIENTE, 'estado')).toBeUndefined()
    expect(campo(ROLES.VENDEDOR, 'estado')).toBeUndefined()
  })

  it('HU45-CP11: los valores iniciales son los datos actuales del usuario, sin el DNI ni el tipo de cliente', () => {
    expect(initialValues(ROLES.CLIENTE, CLIENTE, PROPIO)).toEqual({
      nombre: 'Carlos',
      apellidos: 'Moreno Gil',
      telefono: '620023757',
      fechaNacimiento: '1978-04-13',
      avatar: 'avatar1',
    })
  })

  it('HU45-CP12: el cuerpo de la petición lleva todos los datos modificables del cliente y nada más', () => {
    const valores = initialValues(ROLES.CLIENTE, CLIENTE, PROPIO)

    expect(profilePayload(ROLES.CLIENTE, valores, PROPIO)).toEqual({
      nombre: 'Carlos',
      apellidos: 'Moreno Gil',
      telefono: '620023757',
      fechaNacimiento: '1978-04-13',
      avatar: 'avatar1',
    })
  })

  it('HU45-CP12: el cuerpo de la petición lleva todos los datos modificables del vendedor y nada más', () => {
    const valores = initialValues(ROLES.VENDEDOR, VENDEDOR, PROPIO)

    expect(profilePayload(ROLES.VENDEDOR, { ...valores, nombre: '  Vera  ' }, PROPIO)).toEqual({
      nombre: 'Vera',
      apellidos: 'Vendedora',
      nombreComercial: 'TecnoVera',
      telefono: '612345601',
      categoriaPrincipalId: 'cat-1',
      avatar: 'avatar2',
    })
  })

  it('HU45-CP13: con los datos actuales no hay errores, aunque el DNI del usuario no se valide', () => {
    expect(validateProfile(ROLES.CLIENTE, initialValues(ROLES.CLIENTE, CLIENTE, PROPIO), PROPIO)).toEqual({})
    expect(validateProfile(ROLES.VENDEDOR, initialValues(ROLES.VENDEDOR, VENDEDOR, PROPIO), PROPIO)).toEqual({})
  })

  it('HU45-CP13: nombre y apellidos vacíos o con solo espacios dan error en cada campo', () => {
    const valores = { ...initialValues(ROLES.CLIENTE, CLIENTE, PROPIO), nombre: '', apellidos: '   ' }

    expect(Object.keys(validateProfile(ROLES.CLIENTE, valores, PROPIO))).toEqual(['nombre', 'apellidos'])
  })

  it('HU45-CP13: un teléfono que no tiene 9 números da error, y vacío es válido', () => {
    const valores = initialValues(ROLES.CLIENTE, CLIENTE, PROPIO)

    expect(validateProfile(ROLES.CLIENTE, { ...valores, telefono: '12345' }, PROPIO)).toHaveProperty('telefono')
    expect(validateProfile(ROLES.CLIENTE, { ...valores, telefono: '' }, PROPIO)).toEqual({})
  })

  it('HU45-CP13: el cliente menor de edad o sin fecha de nacimiento da error', () => {
    const valores = initialValues(ROLES.CLIENTE, CLIENTE, PROPIO)

    expect(validateProfile(ROLES.CLIENTE, { ...valores, fechaNacimiento: hacerAnios(17) }, PROPIO)).toEqual({
      fechaNacimiento: 'Debes ser mayor de edad',
    })
    expect(Object.keys(validateProfile(ROLES.CLIENTE, { ...valores, fechaNacimiento: '' }, PROPIO))).toEqual([
      'fechaNacimiento',
    ])
  })

  it('HU45-CP13: el vendedor sin nombre comercial o sin categoría da error', () => {
    const valores = { ...initialValues(ROLES.VENDEDOR, VENDEDOR, PROPIO), nombreComercial: '', categoriaPrincipalId: '' }

    expect(Object.keys(validateProfile(ROLES.VENDEDOR, valores, PROPIO))).toEqual([
      'nombreComercial',
      'categoriaPrincipalId',
    ])
  })
})
