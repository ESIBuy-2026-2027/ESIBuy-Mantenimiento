import { ROLES } from '../../auth/roles'
import { formatDate } from '../../utils/format'
import { esDniValido, esEmailValido, esMayorDeEdad, esTelefonoValido } from '../account/accountRules'
import { STATUS_LABELS } from './usersConfig'

// Campos de la ficha de cada tipo de usuario (HU20, HU22 y HU26). Una sola definición sirve para los dos modos:
// - modo lectura: cada campo muestra el texto de `read`;
// - modo edición: los campos con `edit` se pueden modificar y el resto se queda en solo lectura.
// El nombre de un campo editable es la clave con la que viaja al backend.

export const TIPOS_CLIENTE = { ESTANDAR: 'Estándar', PREMIUM: 'Premium' }

const SIN_DATO = '—'
const AVATAR = 'avatar'
const DNI_NIF_LENGTH = 9

const textoOSinDato = (valor) => valor || SIN_DATO
const fechaOSinDato = (valor) => (valor ? formatDate(valor) : SIN_DATO)
const imagenDePerfil = (avatar) => (avatar ? `Avatar ${avatar.replace('avatar', '')}` : 'Sin elegir')
const esNifValido = (nif) => new RegExp(`^[A-Za-z0-9]{${DNI_NIF_LENGTH}}$`).test(nif)

const nombre = { name: 'nombre', label: 'Nombre', read: (u) => u.nombre, edit: { required: true, maxLength: 30 } }
const apellidos = {
  name: 'apellidos',
  label: 'Apellidos',
  read: (u) => u.apellidos,
  edit: { required: true, maxLength: 30 },
}
const email = { name: 'email', label: 'Email', read: (u) => u.email }
const imagen = {
  name: AVATAR,
  label: 'Imagen de perfil',
  read: (u) => imagenDePerfil(u.avatar),
  edit: { type: AVATAR },
}
const estado = { name: 'estado', label: 'Estado', read: (u) => STATUS_LABELS[u.estado] ?? u.estado }
const fechaAlta = { name: 'fechaAlta', label: 'Fecha de alta', read: (u) => formatDate(u.fechaAlta) }

const PROFILE_FIELDS = {
  // Todo menos el email (y la contraseña, que solo se cambia con "He olvidado mi contraseña").
  [ROLES.CLIENTE]: [
    nombre,
    apellidos,
    email,
    {
      name: 'dni',
      label: 'DNI',
      read: (u) => u.dni ?? SIN_DATO,
      edit: {
        required: true,
        maxLength: DNI_NIF_LENGTH,
        validate: (valor) => (esDniValido(valor) ? undefined : 'Introduce un DNI válido (8 números y una letra)'),
      },
    },
    {
      name: 'telefono',
      label: 'Teléfono',
      read: (u) => textoOSinDato(u.telefono),
      edit: {
        type: 'tel',
        maxLength: 9,
        validate: (valor) => (!valor || esTelefonoValido(valor) ? undefined : 'Introduce un teléfono válido (9 números)'),
      },
    },
    {
      name: 'fechaNacimiento',
      label: 'Fecha de nacimiento',
      read: (u) => fechaOSinDato(u.fechaNacimiento),
      edit: {
        type: 'date',
        required: true,
        validate: (valor) => (esMayorDeEdad(valor) ? undefined : 'Debe ser mayor de edad'),
      },
    },
    {
      name: 'tipoCliente',
      label: 'Tipo de cliente',
      read: (u) => TIPOS_CLIENTE[u.tipoCliente] ?? SIN_DATO,
      edit: { type: 'select', required: true },
    },
    imagen,
    estado,
  ],
  // No se pueden cambiar el email ni la fecha de incorporación (ni la contraseña). Es el único tipo
  // de usuario con fecha de alta: la de clientes y vendedores no se guarda.
  // No se pueden cambiar el email ni la fecha de incorporación (ni la contraseña).
  [ROLES.ADMIN]: [
    nombre,
    apellidos,
    email,
    { name: 'sede', label: 'Sede', read: (u) => textoOSinDato(u.sede), edit: { maxLength: 30 } },
    {
      name: 'fechaIncorporacion',
      label: 'Fecha de incorporación',
      read: (u) => fechaOSinDato(u.fechaIncorporacion),
    },
    imagen,
    estado,
    fechaAlta,
  ],
  // Se puede modificar todo, y el nombre comercial tiene que seguir siendo único (lo comprueba el backend).
  // Sus productos, precios y pedidos no se modifican desde aquí (HU26).
  [ROLES.VENDEDOR]: [
    nombre,
    apellidos,
    {
      name: 'email',
      label: 'Correo electrónico',
      read: (u) => u.email,
      edit: {
        type: 'email',
        required: true,
        maxLength: 100,
        validate: (valor) => (esEmailValido(valor) ? undefined : 'Introduce un email válido'),
      },
    },
    {
      name: 'nombreComercial',
      label: 'Nombre comercial',
      read: (u) => u.nombreComercial,
      edit: { required: true, maxLength: 30 },
    },
    {
      name: 'nif',
      label: 'NIF/DNI',
      read: (u) => u.nif,
      edit: {
        required: true,
        maxLength: DNI_NIF_LENGTH,
        validate: (valor) => (esNifValido(valor) ? undefined : `Introduce un NIF/DNI válido (${DNI_NIF_LENGTH} caracteres)`),
      },
    },
    {
      name: 'categoriaPrincipalId',
      label: 'Categoría principal',
      read: (u) => u.categoriaPrincipal,
      edit: { type: 'select', required: true },
    },
    estado,
  ],
}

const FALLBACK_FIELDS = [nombre, apellidos, email, estado]

export const profileFieldsFor = (rol) => PROFILE_FIELDS[rol] ?? FALLBACK_FIELDS

const isEditable = (field) => Boolean(field.edit)

// Pares [etiqueta, texto] para el modo lectura. Nunca incluye la contraseña.
export const readFields = (rol, usuario) => profileFieldsFor(rol).map((field) => [field.label, field.read(usuario)])

// Valores iniciales del formulario: solo los campos que se pueden modificar, siempre como texto.
export function initialValues(rol, usuario) {
  return Object.fromEntries(
    profileFieldsFor(rol)
      .filter(isEditable)
      .map((field) => [field.name, String(usuario[field.name] ?? '').slice(0, field.edit.type === 'date' ? 10 : undefined)]),
  )
}

// Un mensaje por cada campo incorrecto (vacío si todo es correcto), para mostrarlos todos a la vez.
export function validateProfile(rol, values) {
  const errors = {}
  for (const field of profileFieldsFor(rol).filter(isEditable)) {
    const valor = (values[field.name] ?? '').trim()
    if (field.edit.required && !valor) errors[field.name] = 'Este campo es obligatorio'
    else if (valor && field.edit.validate) {
      const mensaje = field.edit.validate(valor)
      if (mensaje) errors[field.name] = mensaje
    }
  }
  return errors
}

// Cuerpo de la petición: solo los campos modificables. El email de clientes y administradores, la fecha de
// incorporación y la contraseña no viajan nunca.
export function profilePayload(rol, values) {
  return Object.fromEntries(
    profileFieldsFor(rol)
      .filter(isEditable)
      .map((field) => [field.name, (values[field.name] ?? '').trim()]),
  )
}
