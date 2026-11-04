import PropTypes from 'prop-types'
import { useState } from 'react'
import {
  Button,
  Dialog,
  DialogActions,
  DialogBody,
  DialogContent,
  DialogSurface,
  DialogTitle,
  Field,
  Input,
  Radio,
  RadioGroup,
  Textarea,
  makeStyles,
  tokens,
} from '@fluentui/react-components'
import { createAdminCategory } from '../../api/adminCategories'
import { CATEGORY_STATUS_LABELS } from './categoryStatus'

// Los mismos límites que el dominio del backend (Categoria): con maxLength no se puede escribir más.
const LONGITUD_MAXIMA = { nombre: 50, descripcion: 200 }
const OBLIGATORIO = 'Este campo es obligatorio'

const useStyles = makeStyles({
  surface: {
    width: '100%',
    maxWidth: '560px',
  },
  content: {
    display: 'flex',
    flexDirection: 'column',
    gap: tokens.spacingVerticalL,
  },
})

// Formulario de alta de una categoría (HU40): nombre, descripción y estado, que por defecto es activa. El nombre es
// obligatorio: si está vacío o solo tiene espacios, se avisa en el campo y no se envía nada. `onSaved` recibe la
// categoría creada; `onClose`, que se cierra sin más.
export default function CategoryFormDialog({ onClose, onSaved }) {
  const styles = useStyles()
  const [datos, setDatos] = useState({ nombre: '', descripcion: '', estado: 'ACTIVA' })
  const [errorNombre, setErrorNombre] = useState(null)
  const [enviando, setEnviando] = useState(false)
  const cambiar = (campo) => (_event, data) => setDatos((actuales) => ({ ...actuales, [campo]: data.value }))

  const crear = async () => {
    if (!datos.nombre.trim()) {
      setErrorNombre(OBLIGATORIO)
      return
    }
    setEnviando(true)
    try {
      onSaved(await createAdminCategory(datos))
    } finally {
      setEnviando(false)
    }
  }

  return (
    <Dialog open modalType="modal" onOpenChange={(_event, data) => !data.open && onClose()}>
      <DialogSurface className={styles.surface}>
        <DialogBody>
          <DialogTitle>Nueva categoría</DialogTitle>
          <DialogContent className={styles.content}>
            <Field label="Nombre" required validationMessage={errorNombre}>
              <Input maxLength={LONGITUD_MAXIMA.nombre} value={datos.nombre} onChange={cambiar('nombre')} />
            </Field>
            <Field label="Descripción">
              <Textarea
                resize="vertical"
                maxLength={LONGITUD_MAXIMA.descripcion}
                value={datos.descripcion}
                onChange={cambiar('descripcion')}
              />
            </Field>
            <Field label="Estado">
              <RadioGroup layout="horizontal" value={datos.estado} onChange={cambiar('estado')}>
                {Object.entries(CATEGORY_STATUS_LABELS).map(([estado, etiqueta]) => (
                  <Radio key={estado} value={estado} label={etiqueta} />
                ))}
              </RadioGroup>
            </Field>
          </DialogContent>
          <DialogActions>
            <Button onClick={onClose} disabled={enviando}>
              Cancelar
            </Button>
            <Button appearance="primary" onClick={crear} disabled={enviando}>
              Crear
            </Button>
          </DialogActions>
        </DialogBody>
      </DialogSurface>
    </Dialog>
  )
}

CategoryFormDialog.propTypes = {
  onClose: PropTypes.func.isRequired,
  onSaved: PropTypes.func.isRequired,
}
