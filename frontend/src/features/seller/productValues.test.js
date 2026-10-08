import { describe, it, expect } from 'vitest'
import { emptyProductValues, toProductPayload, validateProduct, valuesFromProduct } from './productValues'

const valoresValidos = () => ({
  ...emptyProductValues(),
  nombre: 'Teclado mecánico',
  categoriaIds: ['cat-1'],
  precio: '89.5',
})

describe('validateProduct', () => {
  it('validateProduct_conFormularioVacio_devuelveTodosLosObligatoriosAlaVez', () => {
    const errores = validateProduct(emptyProductValues())

    expect(Object.keys(errores).sort((a, b) => a.localeCompare(b))).toEqual(['categoriaIds', 'nombre', 'precio'])
  })

  it('validateProduct_conDatosValidos_noDevuelveErrores', () => {
    expect(validateProduct(valoresValidos())).toEqual({})
  })

  it('validateProduct_conNombreSoloDeEspacios_loTrataComoVacio', () => {
    expect(validateProduct({ ...valoresValidos(), nombre: '   ' }).nombre).toBeDefined()
  })

  it('validateProduct_conNombreDeMasDe100Caracteres_devuelveError', () => {
    expect(validateProduct({ ...valoresValidos(), nombre: 'a'.repeat(101) }).nombre).toBeDefined()
    expect(validateProduct({ ...valoresValidos(), nombre: 'a'.repeat(100) }).nombre).toBeUndefined()
  })

  it.each(['0', '-5', 'abc', '0.00'])('validateProduct_conPrecio_%s_devuelveError', (precio) => {
    expect(validateProduct({ ...valoresValidos(), precio }).precio).toMatch(/mayor que 0/i)
  })

  it('validateProduct_conPrecioMinimoPositivo_loAcepta', () => {
    expect(validateProduct({ ...valoresValidos(), precio: '0.01' }).precio).toBeUndefined()
  })

  it.each(['', '0', '10'])('validateProduct_conStock_%s_loAcepta', (stock) => {
    expect(validateProduct({ ...valoresValidos(), stock }).stock).toBeUndefined()
  })

  it.each(['-1', '1.5', 'abc'])('validateProduct_conStock_%s_devuelveError', (stock) => {
    expect(validateProduct({ ...valoresValidos(), stock }).stock).toMatch(/mayor o igual que 0/i)
  })

  it('validateProduct_sinCategorias_devuelveError', () => {
    expect(validateProduct({ ...valoresValidos(), categoriaIds: [] }).categoriaIds).toBeDefined()
  })

  it('validateProduct_conDescripcionDeMasDe1000Caracteres_devuelveError', () => {
    expect(validateProduct({ ...valoresValidos(), descripcion: 'a'.repeat(1001) }).descripcion).toBeDefined()
  })

  it.each([
    ['ftp://servidor/imagen.png', true],
    ['no es una url', true],
    ['https://esibuy.test/imagen.png', false],
    ['', false],
  ])('validateProduct_conImagen_%s_hayError_%s', (imagen, hayError) => {
    expect(Boolean(validateProduct({ ...valoresValidos(), imagen }).imagen)).toBe(hayError)
  })

  it.each(['', '0', '-3', '101', 'abc'])('validateProduct_conDescuentoActivoYPorcentaje_%s_devuelveError', (porcentaje) => {
    const errores = validateProduct({ ...valoresValidos(), descuentoActivo: true, descuentoPorcentaje: porcentaje })

    expect(errores.descuentoPorcentaje).toBeDefined()
  })

  it.each(['1', '10', '100'])('validateProduct_conDescuentoActivoYPorcentaje_%s_loAcepta', (porcentaje) => {
    const errores = validateProduct({ ...valoresValidos(), descuentoActivo: true, descuentoPorcentaje: porcentaje })

    expect(errores.descuentoPorcentaje).toBeUndefined()
  })

  it('validateProduct_conDescuentoPremiumActivoSinPorcentaje_devuelveError', () => {
    const errores = validateProduct({ ...valoresValidos(), descuentoPremiumActivo: true })

    expect(errores.descuentoPremiumPorcentaje).toBeDefined()
  })

  it('validateProduct_conDescuentoDesactivado_ignoraSuPorcentaje', () => {
    const errores = validateProduct({ ...valoresValidos(), descuentoActivo: false, descuentoPorcentaje: 'basura' })

    expect(errores.descuentoPorcentaje).toBeUndefined()
  })
})

describe('toProductPayload', () => {
  it('toProductPayload_conStockVacio_loInicializaA0', () => {
    expect(toProductPayload(valoresValidos()).stock).toBe(0)
  })

  it('toProductPayload_convierteLosNumerosYRecortaLosTextos', () => {
    const payload = toProductPayload({
      ...valoresValidos(),
      nombre: '  Teclado  ',
      descripcion: ' Con luz ',
      imagen: ' https://esibuy.test/a.png ',
      precio: '89.5',
      stock: '12',
    })

    expect(payload).toMatchObject({
      nombre: 'Teclado',
      descripcion: 'Con luz',
      imagen: 'https://esibuy.test/a.png',
      precio: 89.5,
      stock: 12,
    })
  })

  it('toProductPayload_conDescuentosInactivos_envia_porcentajeNulo', () => {
    const payload = toProductPayload({ ...valoresValidos(), descuentoPorcentaje: '20' })

    expect(payload.descuento).toEqual({ activo: false, porcentaje: null })
    expect(payload.descuentoPremium).toEqual({ activo: false, porcentaje: null })
  })

  it('toProductPayload_conDescuentosActivos_envia_losPorcentajes', () => {
    const payload = toProductPayload({
      ...valoresValidos(),
      descuentoActivo: true,
      descuentoPorcentaje: '10',
      descuentoPremiumActivo: true,
      descuentoPremiumPorcentaje: '15',
    })

    expect(payload.descuento).toEqual({ activo: true, porcentaje: 10 })
    expect(payload.descuentoPremium).toEqual({ activo: true, porcentaje: 15 })
  })

  it('toProductPayload_noIncluyeAlVendedor', () => {
    expect(Object.keys(toProductPayload(valoresValidos()))).not.toContain('vendedorId')
  })
})

describe('valuesFromProduct', () => {
  it('valuesFromProduct_convierteElProductoDelBackendEnValoresDelFormulario', () => {
    const valores = valuesFromProduct({
      id: 'p-1',
      nombre: 'Auriculares',
      descripcion: 'Con cable',
      precio: 59.9,
      stock: 34,
      disponible: false,
      categoriaIds: ['cat-1', 'cat-2'],
      imagen: '',
      descuento: { activo: true, porcentaje: 10 },
      descuentoPremium: { activo: false, porcentaje: null },
    })

    expect(valores).toEqual({
      nombre: 'Auriculares',
      descripcion: 'Con cable',
      precio: '59.9',
      stock: '34',
      categoriaIds: ['cat-1', 'cat-2'],
      imagen: '',
      disponible: false,
      descuentoActivo: true,
      descuentoPorcentaje: '10',
      descuentoPremiumActivo: false,
      descuentoPremiumPorcentaje: '',
    })
  })

  it('valuesFromProduct_yDespuesToProductPayload_recuperaElProductoOriginal', () => {
    const original = {
      nombre: 'Auriculares',
      descripcion: 'Con cable',
      precio: 59.9,
      stock: 34,
      categoriaIds: ['cat-1'],
      imagen: 'https://esibuy.test/a.png',
      disponible: true,
      descuento: { activo: true, porcentaje: 10 },
      descuentoPremium: { activo: false, porcentaje: null },
    }

    expect(toProductPayload(valuesFromProduct({ id: 'p-1', ...original }))).toEqual(original)
  })
})

describe('emptyProductValues', () => {
  it('emptyProductValues_empiezaDisponibleYSinDescuentos', () => {
    expect(emptyProductValues()).toMatchObject({
      disponible: true,
      descuentoActivo: false,
      descuentoPremiumActivo: false,
      categoriaIds: [],
    })
  })
})
