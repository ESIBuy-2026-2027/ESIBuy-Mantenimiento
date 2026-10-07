import { Text } from '@fluentui/react-components'

export default function Pagination({ page, totalPages }) {
  return <Text>{`Página ${page + 1} de ${totalPages}`}</Text>
}
