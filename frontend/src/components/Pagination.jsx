import { Button, Text } from '@fluentui/react-components'

export default function Pagination({ page, totalPages }) {
  return (
    <div>
      <Button disabled={page === 0}>Anterior</Button>
      <Text>{`Página ${page + 1} de ${totalPages}`}</Text>
      <Button disabled={page >= totalPages - 1}>Siguiente</Button>
    </div>
  )
}
