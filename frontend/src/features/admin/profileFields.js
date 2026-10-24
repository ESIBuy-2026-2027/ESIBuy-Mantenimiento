import { ROLES } from '../../auth/roles'
import { formatDate } from '../../utils/format'
import { esDniValido, esMayorDeEdad, esTelefonoValido } from '../account/accountRules'
import { ROLE_LABELS, STATUS_LABELS } from './usersConfig'

// Campos de la ficha de cada tipo de usuario (HU20, HU22 y HU26). Una sola definición sirve para los dos modos:
// - modo lectura: cada campo muestra el texto de `read`;
// - modo edición: los campos con `edit` se pueden modificar y el resto se queda en solo lectura.
// El nombre de un campo editable es la clave con la que viaja al backend.

export const TIPOS_CLIENTE = { ESTANDAR: 'Estándar', PREMIUM: 'Premium' }

const SIN_DATO = '—'
const AVATAR = 'avatar'
const DNI_LENGTH = 9
const TIPO_CLIENTE_POR_DEFECTO = 'ESTANDAR'

const textoOSinDato = (valor) => valor || SIN_DATO
const fechaOSinDato = (valor) => (valor ? formatDate(valor) : SIN_DATO)
const imagenDePerfil = (avatar) => (avatar ? `Avatar ${avatar.replace('avatar', '')}` : 'Sin elegir')

const nombre = { name: 'nombre', label: 'Nombre', read: (u) => u.nombre, edit: { required: true, maxLength: 30 } }
const apellidos = {
  name: 'apellidos',
  label: 'Apellidos',
  read: (u) => u.apellidos,
  edit: { required: true, maxLength: 30 },
}
const email = { name: 'email', label: 'Email', read: (u) => u.email }
// El rol de una cuenta no se puede cambiar ("el que nace cliente muere cliente", decisión del equipo): siempre
// es de solo lectura.
const rol = { name: 'rol', label: 'Rol', read: (_u, rolActual) => ROLE_LABELS[rolActual] ?? rolActual }
const imagen = {
  name: AVATAR,
  label: 'Imagen de perfil',
  read: (u) => imagenDePerfil(u.avatar),
  edit: { type: AVATAR },
}
// El DNI y el teléfono (opcional) son iguales en el cliente y en el vendedor, como en el backend.
const dni = {
  name: 'dni',
  label: 'DNI',
  read: (u) => textoOSinDato(u.dni),
  edit: {
    required: true,
    maxLength: DNI_LENGTH,
    validate: (valor) => (esDniValido(valor) ? undefined : 'Introduce un DNI válido (8 números y una letra)'),
  },
}
const telefono = {
  name: 'telefono',
  label: 'Teléfono',
  read: (u) => textoOSinDato(u.telefono),
  edit: {
    type: 'tel',
    maxLength: 9,
    validate: (valor) => (!valor || esTelefonoValido(valor) ? undefined : 'Introduce un teléfono válido (9 números)'),
  },
}
const estado = { name: 'estado', label: 'Estado', read: (u) => STATUS_LABELS[u.estado] ?? u.estado }
const fechaAlta = { name: 'fechaAlta', label: 'Fecha de alta', read: (u) => fechaOSinDato(u.fechaAlta) }

const PROFILE_FIELDS = {
  // Todo menos el email (y la contraseña, que solo se cambia con "He olvidado mi contraseña").
  [ROLES.CLIENTE]: [
    nombre,
    apellidos,
    email,
    rol,
    dni,
    telefono,
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
  // de usuario con fecha de alta: la de clientes y vendedores no se guarda. Un administrador nunca está
  // pendiente: lo da de alta otro administrador.
  [ROLES.ADMIN]: [
    nombre,
    apellidos,
    email,
    rol,
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
  // Se puede modificar todo menos el email (por diseño del backend, de momento no se puede cambiar el correo
  // de un vendedor), y el nombre comercial tiene que seguir siendo único (lo comprueba el backend). Un vendedor
  // tiene una sola categoría. El backend guarda el perfil entero, así que el teléfono y la imagen también viajan:
  // si no, se borrarían. Sus productos, precios y pedidos no se modifican desde aquí (HU26).
  [ROLES.VENDEDOR]: [
    nombre,
    apellidos,
    { ...email, label: 'Correo electrónico' },
    rol,
    {
      name: 'nombreComercial',
      label: 'Nombre comercial',
      read: (u) => textoOSinDato(u.nombreComercial),
      edit: { required: true, maxLength: 30 },
    },
    dni,
    telefono,
    {
      name: 'categoriaPrincipalId',
      label: 'Categoría principal',
      read: (u) => textoOSinDato(u.categoriaPrincipal),
      edit: { type: 'select', required: true },
    },
    imagen,
    estado,
  ],
}

const FALLBACK_FIELDS = [nombre, apellidos, email, estado]

export const profileFieldsFor = (rolDeLaCuenta) => PROFILE_FIELDS[rolDeLaCuenta] ?? FALLBACK_FIELDS

const isEditable = (field) => Boolean(field.edit)
const editableFieldsFor = (rolDeLaCuenta) => profileFieldsFor(rolDeLaCuenta).filter(isEditable)

// Pares [etiqueta, texto] para el modo lectura. Nunca incluye la contraseña.
export const readFields = (rolDeLaCuenta, usuario) =>
  profileFieldsFor(rolDeLaCuenta).map((field) => [field.label, field.read(usuario, rolDeLaCuenta)])

// Valores iniciales del formulario: los campos modificables del rol de la cuenta, siempre como texto.
export function initialValues(rolDeLaCuenta, usuario) {
  const valores = Object.fromEntries(
    editableFieldsFor(rolDeLaCuenta).map((field) => {
      const texto = String(usuario[field.name] ?? '')
      return [field.name, field.edit.type === 'date' ? texto.slice(0, 10) : texto]
    }),
  )
  if ('tipoCliente' in valores) valores.tipoCliente ||= TIPO_CLIENTE_POR_DEFECTO
  return valores
}

// Un mensaje por cada campo incorrecto (vacío si todo es correcto), para mostrarlos todos a la vez. Se valida
// con los campos del rol de la cuenta.
export function validateProfile(rolDeLaCuenta, values) {
  const errors = {}
  for (const field of editableFieldsFor(rolDeLaCuenta)) {
    const valor = (values[field.name] ?? '').trim()
    if (field.edit.required && !valor) errors[field.name] = 'Este campo es obligatorio'
    else if (valor && field.edit.validate) {
      const mensaje = field.edit.validate(valor)
      if (mensaje) errors[field.name] = mensaje
    }
  }
  return errors
}

// Cuerpo de la petición: solo los campos modificables del rol de la cuenta. El email, el rol, la fecha de
// incorporación y la contraseña no viajan nunca.
export function profilePayload(rolDeLaCuenta, values) {
  return Object.fromEntries(
    editableFieldsFor(rolDeLaCuenta).map((field) => [field.name, (values[field.name] ?? '').trim()]),
  )
}
