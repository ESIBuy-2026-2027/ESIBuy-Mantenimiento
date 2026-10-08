import PropTypes from 'prop-types'
import { Avatar, Field, Radio, RadioGroup } from '@fluentui/react-components'
import { AnimalCat24Regular, AnimalDog24Regular, AnimalRabbit24Regular } from '@fluentui/react-icons'

const AVATARES = [
  { valor: 'avatar1', etiqueta: 'Avatar 1', icono: <AnimalCat24Regular />, color: 'marigold' },
  { valor: 'avatar2', etiqueta: 'Avatar 2', icono: <AnimalDog24Regular />, color: 'seafoam' },
  { valor: 'avatar3', etiqueta: 'Avatar 3', icono: <AnimalRabbit24Regular />, color: 'lavender' },
]

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

AvatarSelector.propTypes = {
  value: PropTypes.string.isRequired,
  onChange: PropTypes.func.isRequired,
}
