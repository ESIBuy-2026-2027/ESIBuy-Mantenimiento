import { describe, it, expect } from 'vitest'
import { ROLES } from '../../auth/roles'
import {
  editFieldsFor,
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
  nif: 'B12345601',
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
      'rol',
      'dni',
      'telefono',
      'fechaNacimiento',
      'tipoCliente',
      'avatar',
    ])
    expect(nombresEditables(ROLES.CLIENTE)).not.toContain('email')
  })

  it('profileFields_deUnAdministrador_noPermiteModificarEmailNiFechaDeIncorporacion', () => {
    expect(nombresEditables(ROLES.ADMIN)).toEqual(['nombre', 'apellidos', 'rol', 'sede', 'avatar'])
  })

  it('profileFields_deUnVendedor_permiteModificarTodosSusDatos', () => {
    expect(nombresEditables(ROLES.VENDEDOR)).toEqual([
      'nombre',
      'apellidos',
      'email',
      'rol',
      'nombreComercial',
      'nif',
      'categoriaPrincipalId',
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
})

describe('initialValues', () => {
  it('initialValues_deUnCliente_devuelveSusDatosComoTextoYSuRol', () => {
    expect(initialValues(ROLES.CLIENTE, CLIENTE)).toMatchObject({
      rol: 'CLIENTE',
      nombre: 'Carlos',
      apellidos: 'Moreno Gil',
      dni: '10004119A',
      telefono: '620023757',
      fechaNacimiento: '1978-04-13',
      tipoCliente: 'PREMIUM',
      avatar: 'avatar1',
    })
  })

  it('initialValues_incluyeLosCamposDeTodosLosRolesParaPoderCambiarDeRol', () => {
    expect(Object.keys(initialValues(ROLES.CLIENTE, CLIENTE))).toEqual(
      expect.arrayContaining(['sede', 'nombreComercial', 'nif', 'categoriaPrincipalId', 'dni', 'rol']),
    )
  })

  it('initialValues_deUnaCuentaSinTipoDeCliente_proponeEstandar', () => {
    expect(initialValues(ROLES.ADMIN, ADMINISTRADOR).tipoCliente).toBe('ESTANDAR')
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

  it('validateProfile_deUnVendedor_exigeNombreComercialEmailNifYCategoria', () => {
    const errores = validateProfile(ROLES.VENDEDOR, {
      ...initialValues(ROLES.VENDEDOR, VENDEDOR),
      nombreComercial: '',
      email: '',
      nif: '',
      categoriaPrincipalId: '',
    })

    expect(Object.keys(errores).sort((a, b) => a.localeCompare(b))).toEqual([
      'categoriaPrincipalId',
      'email',
      'nif',
      'nombreComercial',
    ])
  })

  it('validateProfile_deUnVendedorConEmailOnifIncorrectos_devuelveError', () => {
    const errores = validateProfile(ROLES.VENDEDOR, {
      ...initialValues(ROLES.VENDEDOR, VENDEDOR),
      email: 'esto-no-es-un-email',
      nif: 'ABC',
    })

    expect(errores.email).toMatch(/email válido/i)
    expect(errores.nif).toMatch(/nif\/dni válido/i)
  })
})

describe('profilePayload', () => {
  it('profilePayload_deUnCliente_enviaSoloLosCamposModificablesYSinEmail', () => {
    const payload = profilePayload(ROLES.CLIENTE, initialValues(ROLES.CLIENTE, CLIENTE))

    expect(Object.keys(payload)).toEqual([
      'nombre',
      'apellidos',
      'rol',
      'dni',
      'telefono',
      'fechaNacimiento',
      'tipoCliente',
      'avatar',
    ])
  })

  it('profilePayload_deUnAdministrador_noEnviaEmailNiFechaDeIncorporacion', () => {
    const claves = Object.keys(profilePayload(ROLES.ADMIN, initialValues(ROLES.ADMIN, ADMINISTRADOR)))

    expect(claves).toEqual(['nombre', 'apellidos', 'rol', 'sede', 'avatar'])
  })

  it('profilePayload_recortaLosEspaciosDeLosTextos', () => {
    const payload = profilePayload(ROLES.ADMIN, {
      rol: ROLES.ADMIN,
      nombre: '  Diego ',
      apellidos: ' D ',
      sede: ' Cuenca ',
      avatar: 'avatar3',
    })

    expect(payload).toEqual({ rol: 'ADMINISTRADOR', nombre: 'Diego', apellidos: 'D', sede: 'Cuenca', avatar: 'avatar3' })
  })

  it('profilePayload_deUnVendedor_enviaLaCategoriaPorIdentificador', () => {
    expect(profilePayload(ROLES.VENDEDOR, initialValues(ROLES.VENDEDOR, VENDEDOR))).toEqual({
      rol: 'VENDEDOR',
      nombre: 'Vera',
      apellidos: 'Vendedora',
      email: 'vendedor@esibuy.test',
      nombreComercial: 'TecnoVera',
      nif: 'B12345601',
      categoriaPrincipalId: 'cat-1',
    })
  })
})

describe('cambio de rol (HU22)', () => {
  const nombresDe = (campos) => campos.filter((campo) => campo.edit).map((campo) => campo.name)

  it('editFieldsFor_alPasarAAdministrador_ofreceLosCamposDeUnAdministrador', () => {
    expect(nombresDe(editFieldsFor(ROLES.ADMIN, ROLES.CLIENTE))).toEqual(['nombre', 'apellidos', 'rol', 'sede', 'avatar'])
  })

  it('editFieldsFor_alPasarAVendedor_ofreceLosCamposDeUnVendedor', () => {
    expect(nombresDe(editFieldsFor(ROLES.VENDEDOR, ROLES.ADMIN))).toEqual(
      expect.arrayContaining(['nombreComercial', 'nif', 'categoriaPrincipalId']),
    )
  })

  it('editFieldsFor_elEmailSoloEsEditableSiLaCuentaYaEraDeVendedorYLoSigueSiendo', () => {
    expect(nombresDe(editFieldsFor(ROLES.VENDEDOR, ROLES.VENDEDOR))).toContain('email')
    expect(nombresDe(editFieldsFor(ROLES.VENDEDOR, ROLES.CLIENTE))).not.toContain('email')
    expect(nombresDe(editFieldsFor(ROLES.CLIENTE, ROLES.VENDEDOR))).not.toContain('email')
    expect(nombresDe(editFieldsFor(ROLES.ADMIN, ROLES.ADMIN))).not.toContain('email')
  })

  it('editFieldsFor_alConvertirEnVendedorElEmailSeMuestraEnSoloLectura', () => {
    const email = editFieldsFor(ROLES.VENDEDOR, ROLES.CLIENTE).find((campo) => campo.name === 'email')

    expect(email).toBeDefined()
    expect(email.edit).toBeUndefined()
  })

  it('profilePayload_alCambiarDeClienteAAdministrador_enviaElRolNuevoYSoloSusCampos', () => {
    const valores = { ...initialValues(ROLES.CLIENTE, CLIENTE), rol: ROLES.ADMIN, sede: 'Toledo' }

    expect(profilePayload(ROLES.CLIENTE, valores)).toEqual({
      rol: 'ADMINISTRADOR',
      nombre: 'Carlos',
      apellidos: 'Moreno Gil',
      sede: 'Toledo',
      avatar: 'avatar1',
    })
  })

  it('profilePayload_alCambiarDeAdministradorAVendedor_nuncaEnviaElEmail', () => {
    const valores = {
      ...initialValues(ROLES.ADMIN, ADMINISTRADOR),
      rol: ROLES.VENDEDOR,
      nombreComercial: 'Tienda Diego',
      nif: 'B12345678',
      categoriaPrincipalId: 'cat-2',
    }

    const payload = profilePayload(ROLES.ADMIN, valores)

    expect(payload).toEqual({
      rol: 'VENDEDOR',
      nombre: 'Diego',
      apellidos: 'Delgado Iglesias',
      nombreComercial: 'Tienda Diego',
      nif: 'B12345678',
      categoriaPrincipalId: 'cat-2',
    })
    expect(payload).not.toHaveProperty('email')
  })

  it('validateProfile_alCambiarAVendedor_exigeLosDatosDelRolNuevo', () => {
    const errores = validateProfile(ROLES.CLIENTE, { ...initialValues(ROLES.CLIENTE, CLIENTE), rol: ROLES.VENDEDOR })

    expect(Object.keys(errores).sort((a, b) => a.localeCompare(b))).toEqual([
      'categoriaPrincipalId',
      'nif',
      'nombreComercial',
    ])
  })

  it('validateProfile_alCambiarAAdministrador_noExigeLosDatosDelRolAnterior', () => {
    const valores = { ...initialValues(ROLES.CLIENTE, { ...CLIENTE, dni: '', fechaNacimiento: '' }), rol: ROLES.ADMIN }

    expect(validateProfile(ROLES.CLIENTE, valores)).toEqual({})
  })

  it('validateProfile_conUnRolQueNoExiste_devuelveError', () => {
    const errores = validateProfile(ROLES.CLIENTE, { ...initialValues(ROLES.CLIENTE, CLIENTE), rol: 'SUPERUSUARIO' })

    expect(errores.rol).toMatch(/rol válido/i)
  })

  it('readFields_incluyeElRolDeLaCuentaEnTexto', () => {
    expect(Object.fromEntries(readFields(ROLES.CLIENTE, CLIENTE)).Rol).toBe('Cliente')
    expect(Object.fromEntries(readFields(ROLES.ADMIN, ADMINISTRADOR)).Rol).toBe('Administrador')
    expect(Object.fromEntries(readFields(ROLES.VENDEDOR, VENDEDOR)).Rol).toBe('Vendedor')
  })
})
