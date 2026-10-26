import { render, screen } from '@testing-library/react'
import { describe, expect, it } from 'vitest'
import { FluentProvider, webLightTheme } from '@fluentui/react-components'
import TiltedGridHero from './TiltedGridHero'

const IMAGENES = [
  { src: 'https://tienda.test/a.png', alt: 'Producto A' },
  { src: 'https://tienda.test/b.png', alt: 'Producto B' },
]

function renderConTema(ui) {
  return render(<FluentProvider theme={webLightTheme}>{ui}</FluentProvider>)
}

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
})
