import PropTypes from 'prop-types'
import {
  Button,
  Dialog,
  DialogBody,
  DialogContent,
  DialogSurface,
  DialogTitle,
  Field,
  MessageBar,
  MessageBarBody,
  MessageBarTitle,
  Textarea,
  makeStyles,
  tokens,
} from '@fluentui/react-components'
import { Dismiss24Regular } from '@fluentui/react-icons'
import ReadOnlyFields from './ReadOnlyFields'
import { getCategoryStatus } from './categoryStatus'

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

// Ficha de una categoría (HU39): sus datos en solo lectura, con la descripción entera, que en la tabla se corta. No
// pide nada al backend: la fila ya trae todos los datos. No ofrece editar ni eliminar.
export default function CategoryDetailDialog({ category, onClose }) {
  const styles = useStyles()

  return (
    <Dialog open modalType="modal" onOpenChange={(_event, data) => !data.open && onClose()}>
      <DialogSurface className={styles.surface}>
        <DialogBody>
          <DialogTitle
            action={
              <Button appearance="subtle" aria-label="Cerrar" icon={<Dismiss24Regular />} onClick={onClose} />
            }
          >
            Información de la categoría
          </DialogTitle>
          <DialogContent className={styles.content}>
            <MessageBar intent="info">
              <MessageBarBody>
                <MessageBarTitle>Modo solo lectura</MessageBarTitle>
                Puedes consultar esta información.
              </MessageBarBody>
            </MessageBar>

            <ReadOnlyFields
              fields={[
                ['Nombre', category.nombre],
                ['Estado', getCategoryStatus(category.estado).label],
                ['Productos', String(category.numeroProductos)],
              ]}
            />
            <Field label="Descripción">
              <Textarea readOnly appearance="filled-darker" resize="vertical" value={category.descripcion || '—'} />
            </Field>
          </DialogContent>
        </DialogBody>
      </DialogSurface>
    </Dialog>
  )
}

CategoryDetailDialog.propTypes = {
  category: PropTypes.shape({
    nombre: PropTypes.string.isRequired,
    descripcion: PropTypes.string.isRequired,
    estado: PropTypes.string.isRequired,
    numeroProductos: PropTypes.number.isRequired,
  }).isRequired,
  onClose: PropTypes.func.isRequired,
}
