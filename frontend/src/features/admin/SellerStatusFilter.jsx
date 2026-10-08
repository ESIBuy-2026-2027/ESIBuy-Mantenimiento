import { Field, Select, makeStyles } from '@fluentui/react-components'
import { getSellerStatusOptions } from './sellerStatus'

const useStyles = makeStyles({
  field: {
    flexGrow: 1,
    flexBasis: '160px',
  },
})

export default function SellerStatusFilter({ status, onChange }) {
  const styles = useStyles()

  return (
    <Field label="Estado" className={styles.field}>
      <Select value={status} onChange={(_event, data) => onChange(data.value)}>
        <option value="">Todos los estados</option>
        {getSellerStatusOptions().map(({ value, label }) => (
          <option key={value} value={value}>
            {label}
          </option>
        ))}
      </Select>
    </Field>
  )
}
