import { Field, Input, makeStyles, tokens } from '@fluentui/react-components'
import { formatDate } from '../../utils/format'
import { getSellerStatus } from './sellerStatus'

const useStyles = makeStyles({
  grid: {
    display: 'grid',
    gridTemplateColumns: 'repeat(auto-fit, minmax(240px, 1fr))',
    gap: tokens.spacingVerticalM,
    columnGap: tokens.spacingHorizontalL,
  },
})

export default function SellerDataForm({ seller }) {
  const styles = useStyles()
  const fields = [
    ['Nombre', seller.nombre],
    ['Apellidos', seller.apellidos],
    ['Correo electrónico', seller.email],
    ['Nombre comercial', seller.nombreComercial],
    ['NIF/DNI', seller.nif],
    ['Categoría principal', seller.categoriaPrincipal],
    ['Estado', getSellerStatus(seller.estado).label],
    ['Fecha de alta', formatDate(seller.fechaAlta)],
  ]

  return (
    <div className={styles.grid}>
      {fields.map(([label, value]) => (
        <Field key={label} label={label}>
          <Input readOnly appearance="filled-darker" value={value} />
        </Field>
      ))}
    </div>
  )
}
