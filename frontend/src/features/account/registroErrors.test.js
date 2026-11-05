import { describe, it, expect } from 'vitest'
import { ApiError } from '../../api/client'
import { erroresDelServidor } from './registroErrors'

// Fallos de la demo del Sprint 1: el backend indica con un code por qué rechaza los datos (PR #104)
describe('erroresDelServidor', () => {
  it('HU1-CP23: traduce cada code a su campo y su mensaje', () => {
    expect(erroresDelServidor(new ApiError(400, 'Datos de registro no válidos', 'PASSWORD_CONTAINS_PERSONAL_DATA')))
      .toEqual({ campos: { password: 'La contraseña no puede contener tu nombre, tus apellidos ni tu email' } })
    expect(erroresDelServidor(new ApiError(400, 'Datos de registro no válidos', 'PASSWORDS_DO_NOT_MATCH')))
      .toEqual({ campos: { repeticionPassword: 'Las contraseñas no coinciden' } })
    expect(erroresDelServidor(new ApiError(409, 'No se ha podido completar el registro', 'DNI_ALREADY_REGISTERED')))
      .toEqual({ campos: { dni: 'Este DNI ya está registrado' } })
    expect(erroresDelServidor(new ApiError(409, 'No se ha podido completar el registro',
      'EMAIL_AND_COMMERCIAL_NAME_ALREADY_REGISTERED')))
      .toEqual({
        campos: {
          email: 'Este email ya está registrado',
          nombreComercial: 'Este nombre comercial ya está registrado',
        },
      })
  })

  it('HU1-CP23: un code sin campo o desconocido da el mensaje general', () => {
    expect(erroresDelServidor(new ApiError(400, 'Datos de registro no válidos', 'VALIDATION_ERROR')))
      .toEqual({ general: 'Revisa los datos del formulario' })
    expect(erroresDelServidor(new ApiError(400, 'Datos de registro no válidos', 'OTRO_CODE')))
      .toEqual({ general: 'Revisa los datos del formulario' })
  })

  it('HU1-CP23: un error que no es de los datos da un mensaje de error del servidor', () => {
    expect(erroresDelServidor(new ApiError(500, 'Error interno')))
      .toEqual({ general: 'No se ha podido completar la operación. Inténtalo de nuevo más tarde' })
  })
})
