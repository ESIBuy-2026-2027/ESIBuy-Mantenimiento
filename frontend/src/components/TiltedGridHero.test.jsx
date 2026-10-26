import { act, render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'
import { FluentProvider, webLightTheme } from '@fluentui/react-components'
import TiltedGridHero from './TiltedGridHero'

const IMAGENES = [
  { src: 'https://tienda.test/a.png', alt: 'Producto A' },
  { src: 'https://tienda.test/b.png', alt: 'Producto B' },
]

function renderConTema(ui) {
  return render(<FluentProvider theme={webLightTheme}>{ui}</FluentProvider>)
}

// Las imágenes de la banda hacen de botón para el ratón; la banda es decorativa (aria-hidden) porque el teclado
// y los lectores de pantalla usan la lista de debajo.
const imagenesDeLaBanda = () => screen.getAllByRole('button', { hidden: true })

describe('TiltedGridHero', () => {
  it('HU7-CP07: muestra su contenido por encima de la banda de imágenes', () => {
    renderConTema(
      <TiltedGridHero images={IMAGENES}>
        <h2>Los mejor valorados</h2>
      </TiltedGridHero>,
    )

    expect(screen.getByRole('heading', { name: /mejor valorados/i })).toBeInTheDocument()
  })

  it('HU7-CP07: la banda de imágenes es decorativa y reparte las imágenes recibidas', () => {
    const { container } = renderConTema(<TiltedGridHero images={IMAGENES} />)

    const banda = container.querySelector('[aria-hidden="true"]')
    expect(banda).not.toBeNull()
    const direcciones = new Set([...banda.querySelectorAll('img')].map((imagen) => imagen.getAttribute('src')))
    expect(direcciones.has(IMAGENES[0].src) || direcciones.has(IMAGENES[1].src)).toBe(true)
    expect([...direcciones].every((direccion) => IMAGENES.some((imagen) => imagen.src === direccion))).toBe(true)
  })

  it('HU7-CP07: sin imágenes no falla y sigue mostrando su contenido', () => {
    renderConTema(
      <TiltedGridHero images={[]}>
        <p>Sin imágenes</p>
      </TiltedGridHero>,
    )

    expect(screen.getByText('Sin imágenes')).toBeInTheDocument()
  })

  it('HU7-CP07: cada imagen puede llevar un pie (nombre, valoración…) que se dibuja sobre ella', () => {
    renderConTema(
      <TiltedGridHero images={[{ src: 'https://tienda.test/a.png', alt: 'Producto A', caption: <span>Pie de A</span> }]} />,
    )

    expect(screen.getAllByText('Pie de A').length).toBeGreaterThan(0)
  })

  it('HU7-CP08: al pulsar una imagen de la banda avisa con esa imagen', async () => {
    const onSelect = vi.fn()
    renderConTema(<TiltedGridHero images={IMAGENES} onSelect={onSelect} />)

    await userEvent.click(imagenesDeLaBanda()[0])

    expect(onSelect).toHaveBeenCalledTimes(1)
    expect(IMAGENES).toContain(onSelect.mock.calls[0][0])
  })

  it('HU7-CP08: sin onSelect las imágenes de la banda no son botones', () => {
    renderConTema(<TiltedGridHero images={IMAGENES} />)

    expect(screen.queryAllByRole('button', { hidden: true })).toHaveLength(0)
  })
})

// ResizeObserver que los tests disparan a mano, con el tamaño que quieran.
describe('TiltedGridHero: cambio de tamaño de la página', () => {
  let observadores = []
  const original = globalThis.ResizeObserver

  class ResizeObserverControlable {
    constructor(callback) {
      this.callback = callback
      observadores.push(this)
    }

    observe() {}

    unobserve() {}

    disconnect() {}
  }

  function redimensionar(width, height) {
    act(() => {
      for (const observador of observadores) observador.callback([{ contentRect: { width, height } }])
    })
  }

  const esperar = (ms) => act(() => vi.advanceTimersByTime(ms))

  beforeEach(() => {
    observadores = []
    globalThis.ResizeObserver = ResizeObserverControlable
    vi.useFakeTimers()
  })

  afterEach(() => {
    vi.useRealTimers()
    globalThis.ResizeObserver = original
  })

  it('HU7-CP09: al cambiar el tamaño de la página la banda se reinicia, para que las imágenes no se solapen', () => {
    renderConTema(<TiltedGridHero images={IMAGENES} onSelect={vi.fn()} />)
    redimensionar(1200, 500)
    const antes = imagenesDeLaBanda()[0]

    redimensionar(700, 500)
    esperar(300)

    expect(antes.isConnected).toBe(false)
    expect(imagenesDeLaBanda()[0].isConnected).toBe(true)
  })

  it('HU7-CP09: espera a que termine de cambiar el tamaño: varios cambios seguidos reinician una sola vez', () => {
    renderConTema(<TiltedGridHero images={IMAGENES} onSelect={vi.fn()} />)
    redimensionar(1200, 500)
    const antes = imagenesDeLaBanda()[0]

    redimensionar(1100, 500)
    esperar(50)
    redimensionar(1000, 500)
    esperar(50)

    expect(antes.isConnected).toBe(true)
    esperar(300)
    expect(antes.isConnected).toBe(false)
  })

  it('HU7-CP09: si el tamaño no cambia, la banda sigue su curso sin reiniciarse', () => {
    renderConTema(<TiltedGridHero images={IMAGENES} onSelect={vi.fn()} />)
    redimensionar(1200, 500)
    const antes = imagenesDeLaBanda()[0]

    redimensionar(1200, 500)
    esperar(300)

    expect(antes.isConnected).toBe(true)
  })
})
