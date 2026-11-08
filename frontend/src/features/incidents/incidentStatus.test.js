import { describe, it, expect } from 'vitest'
import { colorDeEstado, etiquetaDeEstado } from './incidentStatus'

describe('incidentStatus', () => {
  it('HU44-CP25: PENDIENTE se muestra como Pendiente y ABIERTA como Abierta', () => {
    expect(etiquetaDeEstado('PENDIENTE')).toBe('Pendiente')
    expect(etiquetaDeEstado('ABIERTA')).toBe('Abierta')
  })

  it('HU44-CP25: un estado que el frontend no conoce se muestra tal cual', () => {
    expect(etiquetaDeEstado('RESUELTA')).toBe('RESUELTA')
  })

  it('HU44-CP25: cada estado tiene su color: pendiente en aviso y abierta en la marca', () => {
    expect(colorDeEstado('PENDIENTE')).toBe('warning')
    expect(colorDeEstado('ABIERTA')).toBe('brand')
  })
})
