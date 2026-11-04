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
import { CATEGORY_STATUS_LABELS } from './categoryStatus'

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

// Formulario de alta de una categoría (HU40): nombre, descripción y estado, que por defecto es activa.
export default function CategoryFormDialog({ onClose }) {
  const styles = useStyles()
  const [datos, setDatos] = useState({ nombre: '', descripcion: '', estado: 'ACTIVA' })
  const cambiar = (campo) => (_event, data) => setDatos((actuales) => ({ ...actuales, [campo]: data.value }))

  return (
    <Dialog open modalType="modal" onOpenChange={(_event, data) => !data.open && onClose()}>
      <DialogSurface className={styles.surface}>
        <DialogBody>
          <DialogTitle>Nueva categoría</DialogTitle>
          <DialogContent className={styles.content}>
            <Field label="Nombre" required>
              <Input value={datos.nombre} onChange={cambiar('nombre')} />
            </Field>
            <Field label="Descripción">
              <Textarea resize="vertical" value={datos.descripcion} onChange={cambiar('descripcion')} />
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
            <Button onClick={onClose}>Cancelar</Button>
            <Button appearance="primary">Crear</Button>
          </DialogActions>
        </DialogBody>
      </DialogSurface>
    </Dialog>
  )
}

CategoryFormDialog.propTypes = {
  onClose: PropTypes.func.isRequired,
}
