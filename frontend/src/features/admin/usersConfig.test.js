import { describe, it, expect } from 'vitest'
import { ROLES } from '../../auth/roles'
import { USER_STATUS, effectiveStatus, statusLabelsForRole } from './usersConfig'

describe('effectiveStatus', () => {
  it('effectiveStatus_deUnAdministradorPendiente_devuelveActivo', () => {
    expect(effectiveStatus(USER_STATUS.PENDIENTE, ROLES.ADMIN)).toBe(USER_STATUS.ACTIVO)
  })

  it.each([ROLES.CLIENTE, ROLES.VENDEDOR])('effectiveStatus_de_%s_pendiente_sigueSiendoPendiente', (rol) => {
    expect(effectiveStatus(USER_STATUS.PENDIENTE, rol)).toBe(USER_STATUS.PENDIENTE)
  })

  it('effectiveStatus_deUnAdministradorDesactivado_noCambia', () => {
    expect(effectiveStatus(USER_STATUS.DESACTIVADO, ROLES.ADMIN)).toBe(USER_STATUS.DESACTIVADO)
  })
})

describe('statusLabelsForRole', () => {
  it('statusLabelsForRole_deAdministrador_noIncluyePendiente', () => {
    expect(Object.keys(statusLabelsForRole(ROLES.ADMIN))).toEqual([USER_STATUS.ACTIVO, USER_STATUS.DESACTIVADO])
  })

  it('statusLabelsForRole_deCliente_incluyePendiente', () => {
    expect(Object.keys(statusLabelsForRole(ROLES.CLIENTE))).toContain(USER_STATUS.PENDIENTE)
  })

  it('statusLabelsForRole_sinRol_incluyeTodosLosEstados', () => {
    expect(Object.keys(statusLabelsForRole(''))).toEqual(Object.values(USER_STATUS))
  })
})
