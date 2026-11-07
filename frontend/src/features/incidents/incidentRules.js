// Límites de una incidencia (HU44), los mismos que el backend: se cuentan sin los espacios de los extremos.
export const SUBJECT_MAX = 100
export const DESCRIPTION_MAX = 1000

function validateText(value, { required, tooLong }, max) {
  const length = value.trim().length
  if (length === 0) return required
  if (length > max) return tooLong
  return null
}

// Los errores por campo; si no hay ninguno, un objeto vacío.
export function validateIncident({ asunto, descripcion }) {
  const subject = validateText(
    asunto,
    { required: 'El asunto es obligatorio.', tooLong: `El asunto no puede superar los ${SUBJECT_MAX} caracteres.` },
    SUBJECT_MAX,
  )
  const description = validateText(
    descripcion,
    {
      required: 'La descripción es obligatoria.',
      tooLong: `La descripción no puede superar los ${DESCRIPTION_MAX} caracteres.`,
    },
    DESCRIPTION_MAX,
  )
  return {
    ...(subject && { asunto: subject }),
    ...(description && { descripcion: description }),
  }
}
