import { describe, it, expect } from 'vitest'
import { ROLES } from '../../auth/roles'
import { USER_STATUS, statusLabelsForRole } from './usersConfig'

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
