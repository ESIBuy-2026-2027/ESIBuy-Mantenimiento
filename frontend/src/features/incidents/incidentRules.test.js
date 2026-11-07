import { describe, it, expect } from 'vitest'
import { DESCRIPTION_MAX, SUBJECT_MAX, validateIncident } from './incidentRules'

const valida = { asunto: 'No me llega el pedido', descripcion: 'Lo pedí hace una semana' }

describe('validateIncident', () => {
  it('HU44-CP24: una incidencia con asunto y descripción dentro de los límites no tiene errores', () => {
    expect(validateIncident(valida)).toEqual({})
  })

  it.each([
    ['vacío', ''],
    ['con solo espacios', '   '],
  ])('HU44-CP24: un asunto %s es obligatorio', (_nombre, asunto) => {
    expect(validateIncident({ ...valida, asunto })).toEqual({ asunto: 'El asunto es obligatorio.' })
  })

  it.each([
    ['vacía', ''],
    ['con solo espacios', ' \t '],
  ])('HU44-CP24: una descripción %s es obligatoria', (_nombre, descripcion) => {
    expect(validateIncident({ ...valida, descripcion })).toEqual({ descripcion: 'La descripción es obligatoria.' })
  })

  it('HU44-CP24: los límites son 100 caracteres para el asunto y 1000 para la descripción', () => {
    expect(SUBJECT_MAX).toBe(100)
    expect(DESCRIPTION_MAX).toBe(1000)
  })

  it('HU44-CP24: el asunto admite 100 caracteres y rechaza 101', () => {
    expect(validateIncident({ ...valida, asunto: 'a'.repeat(100) })).toEqual({})
    expect(validateIncident({ ...valida, asunto: 'a'.repeat(101) })).toEqual({
      asunto: 'El asunto no puede superar los 100 caracteres.',
    })
  })

  it('HU44-CP24: la descripción admite 1000 caracteres y rechaza 1001', () => {
    expect(validateIncident({ ...valida, descripcion: 'b'.repeat(1000) })).toEqual({})
    expect(validateIncident({ ...valida, descripcion: 'b'.repeat(1001) })).toEqual({
      descripcion: 'La descripción no puede superar los 1000 caracteres.',
    })
  })

  it('HU44-CP24: los espacios de los extremos no cuentan para el máximo', () => {
    const asunto = `  ${'a'.repeat(100)}  `
    const descripcion = `\n${'b'.repeat(1000)}\n`

    expect(validateIncident({ asunto, descripcion })).toEqual({})
  })

  it('HU44-CP24: informa del asunto y de la descripción a la vez', () => {
    expect(validateIncident({ asunto: '', descripcion: '' })).toEqual({
      asunto: 'El asunto es obligatorio.',
      descripcion: 'La descripción es obligatoria.',
    })
  })
})
