import { describe, it, expect } from 'vitest'
import {
  REQUISITOS_PASSWORD,
  esNombreValido,
  filtrarNombre,
  esEmailValido,
  esMayorDeEdad,
  esTelefonoValido,
  validarPasswords,
} from './accountRules'

const requisito = (texto) => REQUISITOS_PASSWORD.find((candidato) => candidato.texto === texto)

describe('REQUISITOS_PASSWORD', () => {
  it('requisitosPassword_sonCincoReglas', () => {
    expect(REQUISITOS_PASSWORD.map(({ texto }) => texto)).toEqual([
      'Al menos 8 caracteres',
      'Una mayúscula',
      'Una minúscula',
      'Un número',
      'Un símbolo',
    ])
  })

  it('requisitosPassword_longitud_exigeAlMenos8Caracteres', () => {
    expect(requisito('Al menos 8 caracteres').cumple('1234567')).toBe(false)
    expect(requisito('Al menos 8 caracteres').cumple('12345678')).toBe(true)
  })

  it('requisitosPassword_mayusculaYMinuscula_admitenLetrasConAcentoYEñe', () => {
    expect(requisito('Una mayúscula').cumple('ñandú')).toBe(false)
    expect(requisito('Una mayúscula').cumple('Ñandú')).toBe(true)
    expect(requisito('Una minúscula').cumple('ÁRBOL')).toBe(false)
    expect(requisito('Una minúscula').cumple('ÁRBOl')).toBe(true)
  })

  it('requisitosPassword_numero_detectaCualquierDigito', () => {
    expect(requisito('Un número').cumple('Password')).toBe(false)
    expect(requisito('Un número').cumple('Password1')).toBe(true)
  })

  it('requisitosPassword_simbolo_noCuentaLasLetrasConAcentoComoSimbolo', () => {
    expect(requisito('Un símbolo').cumple('Contraseña1')).toBe(false)
    expect(requisito('Un símbolo').cumple('Password1!')).toBe(true)
  })
})

describe('esEmailValido', () => {
  it('esEmailValido_conFormatoCorrecto_devuelveTrue', () => {
    expect(esEmailValido('ana@correo.com')).toBe(true)
    expect(esEmailValido('nombre.apellido@alu.uclm.es')).toBe(true)
  })

  it('esEmailValido_conFormatoIncorrecto_devuelveFalse', () => {
    expect(esEmailValido('esto-no-es-un-email')).toBe(false)
    expect(esEmailValido('ana@correo')).toBe(false)
    expect(esEmailValido('ana @correo.com')).toBe(false)
  })
})

describe('validarPasswords', () => {
  it('validarPasswords_conPasswordValidoYRepetido_noDevuelveErrores', () => {
    expect(validarPasswords({ password: 'Password123!', repeticionPassword: 'Password123!' })).toEqual({})
  })

  it('validarPasswords_conPasswordQueNoCumpleLosRequisitos_devuelveErrorEnElPassword', () => {
    expect(validarPasswords({ password: 'password', repeticionPassword: 'password' })).toEqual({
      password: 'La contraseña no cumple los requisitos',
    })
  })

  it('validarPasswords_conPasswordsDistintos_devuelveErrorEnLaRepeticion', () => {
    expect(validarPasswords({ password: 'Password123!', repeticionPassword: 'Otro123!' })).toEqual({
      repeticionPassword: 'Las contraseñas no coinciden',
    })
  })

  it('validarPasswords_conCamposVacios_noDevuelveErrores', () => {
    expect(validarPasswords({ password: '', repeticionPassword: '' })).toEqual({})
    expect(validarPasswords({ password: 'Password123!', repeticionPassword: '' })).toEqual({})
  })
})

const haceAnios = (anios, dias = 0) => {
  const fecha = new Date()
  fecha.setFullYear(fecha.getFullYear() - anios)
  fecha.setDate(fecha.getDate() + dias)
  const mes = String(fecha.getMonth() + 1).padStart(2, '0')
  const dia = String(fecha.getDate()).padStart(2, '0')
  return `${fecha.getFullYear()}-${mes}-${dia}`
}

describe('filtrarNombre', () => {
  it('filtrarNombre_descartaNumerosYSimbolosNoAdmitidosPeroConservaLetrasYSeparadores', () => {
    expect(filtrarNombre('Ana2')).toBe('Ana')
    expect(filtrarNombre('Juan123')).toBe('Juan')
    expect(filtrarNombre('María-José O\'Connor')).toBe('María-José O\'Connor')
    expect(filtrarNombre('Peña_3')).toBe('Peña')
  })
})

describe('esNombreValido', () => {
  it('esNombreValido_admiteNombresConTildesEspaciosGuionesYApostrofos', () => {
    expect(esNombreValido('Ana')).toBe(true)
    expect(esNombreValido('María José')).toBe(true)
    expect(esNombreValido('Jean-Luc')).toBe(true)
    expect(esNombreValido("O'Connor")).toBe(true)
    expect(esNombreValido('Iñaki')).toBe(true)
  })

  it('esNombreValido_rechazaNumerosSimbolosYLongitudesFueraDeRango', () => {
    expect(esNombreValido('Ana2')).toBe(false)
    expect(esNombreValido('Ana_')).toBe(false)
    expect(esNombreValido('A')).toBe(false)
    expect(esNombreValido('A'.repeat(31))).toBe(false)
    expect(esNombreValido('')).toBe(false)
  })
})

describe('esTelefonoValido', () => {
  it('esTelefonoValido_conNueveNumeros_devuelveTrue', () => {
    expect(esTelefonoValido('612345678')).toBe(true)
  })

  it('esTelefonoValido_conLetrasOOtraLongitud_devuelveFalse', () => {
    expect(esTelefonoValido('61234567')).toBe(false)
    expect(esTelefonoValido('6123456789')).toBe(false)
    expect(esTelefonoValido('61234567a')).toBe(false)
  })
})

describe('esMayorDeEdad', () => {
  it('esMayorDeEdad_conMasDe18Anios_devuelveTrue', () => {
    expect(esMayorDeEdad(haceAnios(30))).toBe(true)
  })

  it('esMayorDeEdad_elDiaQueCumple18_devuelveTrue', () => {
    expect(esMayorDeEdad(haceAnios(18))).toBe(true)
  })

  it('esMayorDeEdad_aUnDiaDeCumplir18_devuelveFalse', () => {
    expect(esMayorDeEdad(haceAnios(18, 1))).toBe(false)
  })

  it('esMayorDeEdad_conUnMenor_devuelveFalse', () => {
    expect(esMayorDeEdad(haceAnios(10))).toBe(false)
  })

  it('esMayorDeEdad_conUnaFechaNoValida_devuelveFalse', () => {
    expect(esMayorDeEdad('')).toBe(false)
    expect(esMayorDeEdad('no es una fecha')).toBe(false)
  })
})
