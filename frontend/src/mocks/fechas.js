// Comprobación de mayoría de edad de los mocks, igual que la del frontend (el backend real también la hará).
export function esMayorDeEdadMock(fecha) {
  const nacimiento = new Date(fecha)
  if (Number.isNaN(nacimiento.getTime())) return false

  const limite = new Date()
  limite.setFullYear(limite.getFullYear() - 18)
  return nacimiento <= limite
}
