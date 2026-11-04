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
  MessageBar,
  MessageBarBody,
  Radio,
  RadioGroup,
  Textarea,
  makeStyles,
  tokens,
} from '@fluentui/react-components'
import { createAdminCategory, updateAdminCategory } from '../../api/adminCategories'
import { CATEGORY_STATUS_LABELS } from './categoryStatus'

// Los mismos límites que el dominio del backend (Categoria): con maxLength no se puede escribir más.
const LONGITUD_MAXIMA = { nombre: 50, descripcion: 200 }
const OBLIGATORIO = 'Este campo es obligatorio'
const REPETIDO = 'Ya existe una categoría con ese nombre.'

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

// Formulario de una categoría: de alta (HU40) o, si recibe `category`, de edición de su nombre y su descripción
// (HU41), que no permite cambiar el estado. Al crear se elige el estado, que por defecto es activa. El nombre es
// obligatorio: si está vacío o solo tiene espacios, se avisa en el campo y no se envía nada. Si el backend dice que el
// nombre ya existe (sin distinguir mayúsculas ni tildes), también se avisa en el campo; cualquier otro error, con un
// mensaje genérico, y el formulario sigue abierto con lo escrito. `onSaved` recibe la categoría creada o editada;
// `onNotFound`, que la categoría que se editaba ya no existe (404); `onClose`, que se cierra sin más.
export default function CategoryFormDialog({ category = null, onClose, onSaved, onNotFound }) {
  const styles = useStyles()
  const editando = category !== null
  const [datos, setDatos] = useState({
    nombre: category?.nombre ?? '',
    descripcion: category?.descripcion ?? '',
    estado: category?.estado ?? 'ACTIVA',
  })
  const [errorNombre, setErrorNombre] = useState(null)
  const [enviando, setEnviando] = useState(false)
  const [errorServidor, setErrorServidor] = useState(false)
  const cambiar = (campo) => (_event, data) => setDatos((actuales) => ({ ...actuales, [campo]: data.value }))
  const cambiarNombre = (event, data) => {
    setErrorNombre(null)
    cambiar('nombre')(event, data)
  }

  const enviar = async () => {
    if (!datos.nombre.trim()) {
      setErrorNombre(OBLIGATORIO)
      return
    }
    setEnviando(true)
    setErrorServidor(false)
    try {
      onSaved(await (editando ? updateAdminCategory(category.id, datos) : createAdminCategory(datos)))
    } catch (error) {
      if (error?.code === 'CATEGORY_NAME_ALREADY_EXISTS') setErrorNombre(REPETIDO)
      else if (editando && error?.code === 'CATEGORY_NOT_FOUND') onNotFound()
      else setErrorServidor(true)
    } finally {
      setEnviando(false)
    }
  }

  return (
    <Dialog open modalType="modal" onOpenChange={(_event, data) => !data.open && onClose()}>
      <DialogSurface className={styles.surface}>
        <DialogBody>
          <DialogTitle>{editando ? 'Editar categoría' : 'Nueva categoría'}</DialogTitle>
          <DialogContent className={styles.content}>
            {errorServidor && (
              <MessageBar intent="error">
                <MessageBarBody>
                  {editando
                    ? 'No se han podido guardar los cambios. Inténtalo de nuevo.'
                    : 'No se ha podido crear la categoría. Inténtalo de nuevo.'}
                </MessageBarBody>
              </MessageBar>
            )}
            <Field label="Nombre" required validationMessage={errorNombre}>
              <Input maxLength={LONGITUD_MAXIMA.nombre} value={datos.nombre} onChange={cambiarNombre} />
            </Field>
            <Field label="Descripción">
              <Textarea
                resize="vertical"
                maxLength={LONGITUD_MAXIMA.descripcion}
                value={datos.descripcion}
                onChange={cambiar('descripcion')}
              />
            </Field>
            {!editando && (
              <Field label="Estado">
                <RadioGroup layout="horizontal" value={datos.estado} onChange={cambiar('estado')}>
                  {Object.entries(CATEGORY_STATUS_LABELS).map(([estado, etiqueta]) => (
                    <Radio key={estado} value={estado} label={etiqueta} />
                  ))}
                </RadioGroup>
              </Field>
            )}
          </DialogContent>
          <DialogActions>
            <Button onClick={onClose} disabled={enviando}>
              Cancelar
            </Button>
            <Button appearance="primary" onClick={enviar} disabled={enviando}>
              {editando ? 'Guardar' : 'Crear'}
            </Button>
          </DialogActions>
        </DialogBody>
      </DialogSurface>
    </Dialog>
  )
}

CategoryFormDialog.propTypes = {
  category: PropTypes.shape({
    id: PropTypes.string.isRequired,
    nombre: PropTypes.string.isRequired,
    descripcion: PropTypes.string.isRequired,
    estado: PropTypes.string.isRequired,
  }),
  onClose: PropTypes.func.isRequired,
  onSaved: PropTypes.func.isRequired,
  onNotFound: PropTypes.func,
}
