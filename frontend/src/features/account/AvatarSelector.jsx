import PropTypes from 'prop-types'
import { Avatar, Field, Radio, RadioGroup } from '@fluentui/react-components'
import { AnimalCat24Regular, AnimalDog24Regular, AnimalRabbit24Regular } from '@fluentui/react-icons'

const AVATARES = [
  { valor: 'avatar1', etiqueta: 'Avatar 1', icono: <AnimalCat24Regular />, color: 'marigold' },
  { valor: 'avatar2', etiqueta: 'Avatar 2', icono: <AnimalDog24Regular />, color: 'seafoam' },
  { valor: 'avatar3', etiqueta: 'Avatar 3', icono: <AnimalRabbit24Regular />, color: 'lavender' },
]

// Avatar elegido por el usuario (HU4). Sin avatar, o con uno que no existe, muestra las iniciales de su nombre.
export function UserAvatar({ value, name, size = 72 }) {
  const avatar = AVATARES.find(({ valor }) => valor === value)
  if (!avatar) return <Avatar name={name} size={size} aria-label="Sin imagen de perfil" />
  return <Avatar icon={avatar.icono} color={avatar.color} size={size} aria-label={avatar.etiqueta} />
}

// Selector de la imagen de perfil entre los avatares predefinidos.
export default function AvatarSelector({ value, onChange }) {
  return (
    <Field label="Imagen de perfil">
      <RadioGroup layout="horizontal" value={value} onChange={onChange}>
        {AVATARES.map(({ valor, etiqueta, icono, color }) => (
          <Radio
            key={valor}
            value={valor}
            label={<Avatar icon={icono} color={color} size={48} aria-label={etiqueta} />}
          />
        ))}
      </RadioGroup>
    </Field>
  )
}

UserAvatar.propTypes = {
  value: PropTypes.string,
  name: PropTypes.string.isRequired,
  size: PropTypes.number,
}

AvatarSelector.propTypes = {
  value: PropTypes.string.isRequired,
  onChange: PropTypes.func.isRequired,
}
