import { useLocation } from 'react-router-dom'

export default function LocationProbe() {
  const location = useLocation()
  return <output data-testid="ruta-actual">{location.pathname}</output>
}
