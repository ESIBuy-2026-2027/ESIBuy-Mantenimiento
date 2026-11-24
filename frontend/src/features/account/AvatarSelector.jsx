import PropTypes from 'prop-types'
import { Avatar, Field, makeStyles, tokens } from '@fluentui/react-components'
import { AnimalCat24Regular, AnimalDog24Regular, AnimalRabbit24Regular } from '@fluentui/react-icons'

const AVATARES = [
  { valor: 'avatar1', etiqueta: 'Avatar 1', icono: <AnimalCat24Regular />, color: 'marigold' },
  { valor: 'avatar2', etiqueta: 'Avatar 2', icono: <AnimalDog24Regular />, color: 'seafoam' },
  { valor: 'avatar3', etiqueta: 'Avatar 3', icono: <AnimalRabbit24Regular />, color: 'lavender' },
]

const useStyles = makeStyles({
  opciones: { display: 'flex', flexWrap: 'wrap', gap: tokens.spacingHorizontalM },
  opcion: {
    width: '64px', height: '64px', minWidth: '64px', minHeight: '64px',
    display: 'grid', placeItems: 'center', boxSizing: 'border-box',
    border: '2px solid transparent', borderRadius: '14px',
    backgroundColor: 'transparent', cursor: 'pointer',
    ':hover': { backgroundColor: '#fff7f0', borderColor: '#f6c6a7' },
    ':focus-visible': { outline: '2px solid #f05a0a', outlineOffset: '2px' },
  },
  seleccionada: { border: '2px solid #f05a0a', boxShadow: 'inset 0 0 0 1px #f05a0a', backgroundColor: '#fff5ed' },
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
          <button
            key={valor}
            type="button"
            className={value === valor ? styles.opcion + ' ' + styles.seleccionada : styles.opcion}
            aria-label={etiqueta}
            aria-pressed={value === valor}
            onClick={() => onChange(null, { value: valor })}
          >
            <Avatar icon={icono} color={color} size={44} />
          </button>
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
