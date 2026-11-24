import PropTypes from 'prop-types'
import { Avatar, Button, Field, makeStyles, tokens } from '@fluentui/react-components'
import { AnimalCat24Regular, AnimalDog24Regular, AnimalRabbit24Regular } from '@fluentui/react-icons'

const AVATARES = [
  { valor: 'avatar1', etiqueta: 'Avatar 1', icono: <AnimalCat24Regular />, color: 'marigold' },
  { valor: 'avatar2', etiqueta: 'Avatar 2', icono: <AnimalDog24Regular />, color: 'seafoam' },
  { valor: 'avatar3', etiqueta: 'Avatar 3', icono: <AnimalRabbit24Regular />, color: 'lavender' },
]

const useStyles = makeStyles({
  opciones: { display: 'flex', flexWrap: 'wrap', gap: tokens.spacingHorizontalM },
  opcion: {
    minWidth: '76px', minHeight: '76px', padding: '8px',
    border: '1px solid transparent', borderRadius: '14px', backgroundColor: 'transparent',
    ':hover': { backgroundColor: '#fff7f0' },
  },
  seleccionada: { border: '2px solid #f05a0a', backgroundColor: '#fff5ed', padding: '7px' },
})

export function UserAvatar({ value, name, size = 72 }) {
  const avatar = AVATARES.find(({ valor }) => valor === value)
  if (!avatar) return <Avatar name={name} size={size} aria-label="Sin imagen de perfil" />
  return <Avatar icon={avatar.icono} color={avatar.color} size={size} aria-label={avatar.etiqueta} />
}

export default function AvatarSelector({ value, onChange }) {
  const styles = useStyles()
  return (
    <Field label="Imagen de perfil">
      <div className={styles.opciones} role="group" aria-label="Selecciona tu avatar">
        {AVATARES.map(({ valor, etiqueta, icono, color }) => (
          <Button
            key={valor}
            type="button"
            appearance="subtle"
            className={value === valor ? styles.seleccionada + ' ' + styles.opcion : styles.opcion}
            aria-label={etiqueta}
            aria-pressed={value === valor}
            onClick={() => onChange(null, { value: valor })}
          >
            <Avatar icon={icono} color={color} size={48} />
          </Button>
        ))}
      </div>
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
