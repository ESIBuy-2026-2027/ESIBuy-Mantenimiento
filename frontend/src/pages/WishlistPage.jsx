import { Text, Title1 } from '@fluentui/react-components'
import { usePageStyles } from '../components/pageStyles'

// «Mis favoritos» del cliente (HU14): su lista de deseos.
export default function WishlistPage() {
  const pageStyles = usePageStyles()

  return (
    <main className={pageStyles.page}>
      <div className={pageStyles.heading}>
        <div className={pageStyles.titleBlock}>
          <Title1 as="h1" className={pageStyles.title}>
            Mis favoritos
          </Title1>
          <Text className={pageStyles.subtitle}>Los productos que has guardado para más adelante.</Text>
        </div>
      </div>
    </main>
  )
}
