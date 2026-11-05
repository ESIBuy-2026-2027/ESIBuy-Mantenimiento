import { describe, it, expect, vi } from 'vitest'
import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { FluentProvider, webLightTheme } from '@fluentui/react-components'
import AvatarSelector, { UserAvatar } from './AvatarSelector'

function renderSelector(props) {
  return render(
    <FluentProvider theme={webLightTheme}>
      <AvatarSelector value="" onChange={vi.fn()} {...props} />
    </FluentProvider>,
  )
}

describe('AvatarSelector', () => {
  it('avatarSelector_muestraTresAvataresConSuNombreAccesible', () => {
    renderSelector()

    expect(screen.getByRole('radiogroup')).toBeInTheDocument()
    expect(screen.getAllByRole('radio')).toHaveLength(3)
    expect(screen.getByRole('radio', { name: /avatar 1/i })).toBeInTheDocument()
    expect(screen.getByRole('radio', { name: /avatar 2/i })).toBeInTheDocument()
    expect(screen.getByRole('radio', { name: /avatar 3/i })).toBeInTheDocument()
  })

  it('avatarSelector_marcaElAvatarDelValorRecibido', () => {
    renderSelector({ value: 'avatar3' })

    expect(screen.getByRole('radio', { name: /avatar 3/i })).toBeChecked()
    expect(screen.getByRole('radio', { name: /avatar 1/i })).not.toBeChecked()
  })

  it('avatarSelector_sinValor_noMarcaNinguno', () => {
    renderSelector()

    screen.getAllByRole('radio').forEach((radio) => expect(radio).not.toBeChecked())
  })

  it('avatarSelector_alElegirUnAvatar_avisaConSuValor', async () => {
    const user = userEvent.setup()
    const onChange = vi.fn()
    renderSelector({ onChange })

    await user.click(screen.getByRole('radio', { name: /avatar 2/i }))

    expect(onChange).toHaveBeenCalledWith(expect.anything(), expect.objectContaining({ value: 'avatar2' }))
  })
})

describe('UserAvatar', () => {
  const renderAvatar = (props) =>
    render(
      <FluentProvider theme={webLightTheme}>
        <UserAvatar name="Vera Vendedora" {...props} />
      </FluentProvider>,
    )

  it('HU4-CP07: muestra el avatar elegido con su nombre accesible', () => {
    renderAvatar({ value: 'avatar3' })

    expect(screen.getByRole('img', { name: 'Avatar 3' })).toBeInTheDocument()
  })

  it('HU4-CP07: sin avatar, o con uno que no existe, muestra las iniciales', () => {
    const { unmount } = renderAvatar({ value: '' })
    expect(screen.getByRole('img', { name: /sin imagen de perfil/i })).toHaveTextContent('VV')
    unmount()

    renderAvatar({ value: 'avatar9' })
    expect(screen.getByRole('img', { name: /sin imagen de perfil/i })).toHaveTextContent('VV')
  })
})
