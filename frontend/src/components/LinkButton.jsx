import { Button } from '@fluentui/react-components'
import { useHref, useLinkClickHandler } from 'react-router-dom'

export default function LinkButton({ to, ...buttonProps }) {
  const href = useHref(to)
  const handleClick = useLinkClickHandler(to)

  return <Button as="a" href={href} onClick={handleClick} {...buttonProps} />
}
