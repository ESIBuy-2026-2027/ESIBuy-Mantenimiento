import { describe, it, expect } from 'vitest'
import { REQUISITOS_PASSWORD, esEmailValido, validarPasswords } from './accountRules'

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
      password: 'El password no cumple los requisitos',
    })
  })

  it('validarPasswords_conPasswordsDistintos_devuelveErrorEnLaRepeticion', () => {
    expect(validarPasswords({ password: 'Password123!', repeticionPassword: 'Otro123!' })).toEqual({
      repeticionPassword: 'Los passwords no coinciden',
    })
  })

  it('validarPasswords_conCamposVacios_noDevuelveErrores', () => {
    expect(validarPasswords({ password: '', repeticionPassword: '' })).toEqual({})
    expect(validarPasswords({ password: 'Password123!', repeticionPassword: '' })).toEqual({})
  })
})
