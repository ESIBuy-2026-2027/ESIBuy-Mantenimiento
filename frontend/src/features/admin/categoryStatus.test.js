import { describe, it, expect } from 'vitest'
import { CATEGORY_STATUS_LABELS, getCategoryStatus } from './categoryStatus'

describe('categoryStatus', () => {
  it('HU39-CP07: una categoría ACTIVA se muestra como Activa, con color de éxito', () => {
    expect(getCategoryStatus('ACTIVA')).toEqual({ label: 'Activa', color: 'success' })
  })

  it('HU39-CP07: una categoría INACTIVA se muestra como Inactiva, con color neutro', () => {
    expect(getCategoryStatus('INACTIVA')).toEqual({ label: 'Inactiva', color: 'subtle' })
  })

  it('HU39-CP07: un estado desconocido se muestra como Desconocido', () => {
    expect(getCategoryStatus('BORRADA')).toEqual({ label: 'Desconocido', color: 'subtle' })
  })

  it('HU39-CP07: las etiquetas del filtro son las de los dos estados', () => {
    expect(CATEGORY_STATUS_LABELS).toEqual({ ACTIVA: 'Activa', INACTIVA: 'Inactiva' })
  })
})
