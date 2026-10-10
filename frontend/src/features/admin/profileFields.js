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
const DNI_NIF_LENGTH = 9
const TIPO_CLIENTE_POR_DEFECTO = 'ESTANDAR'

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
// Un administrador puede cambiar el rol de cualquier cuenta menos el suyo (lo impone el backend; el formulario
// además lo bloquea).
const rol = {
  name: 'rol',
  label: 'Rol',
  read: (_u, rolActual) => ROLE_LABELS[rolActual] ?? rolActual,
  edit: {
    type: 'select',
    required: true,
    validate: (valor) => (ROLE_LABELS[valor] ? undefined : 'Elige un rol válido'),
  },
}
const imagen = {
  name: AVATAR,
  label: 'Imagen de perfil',
  read: (u) => imagenDePerfil(u.avatar),
  edit: { type: AVATAR },
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
  // tiene una sola categoría. Sus productos, precios y pedidos no se modifican desde aquí (HU26).
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
    {
      name: 'nif',
      label: 'NIF/DNI',
      read: (u) => textoOSinDato(u.nif),
      edit: {
        required: true,
        maxLength: DNI_NIF_LENGTH,
        validate: (valor) => (esNifValido(valor) ? undefined : `Introduce un NIF/DNI válido (${DNI_NIF_LENGTH} caracteres)`),
      },
    },
    {
      name: 'categoriaPrincipalId',
      label: 'Categoría principal',
      read: (u) => textoOSinDato(u.categoriaPrincipal),
      edit: { type: 'select', required: true },
    },
    estado,
  ],
}

const FALLBACK_FIELDS = [nombre, apellidos, email, estado]

export const profileFieldsFor = (rolDeLaCuenta) => PROFILE_FIELDS[rolDeLaCuenta] ?? FALLBACK_FIELDS

const isEditable = (field) => Boolean(field.edit)

// Campos del formulario de edición cuando la cuenta se queda con el rol `rolDestino`. Si el administrador
// cambia el rol, el formulario pasa a los campos del rol nuevo. El email no se puede cambiar en ningún rol.
export const editFieldsFor = (rolDestino) => profileFieldsFor(rolDestino)

// Pares [etiqueta, texto] para el modo lectura. Nunca incluye la contraseña.
export const readFields = (rolDeLaCuenta, usuario) =>
  profileFieldsFor(rolDeLaCuenta).map((field) => [field.label, field.read(usuario, rolDeLaCuenta)])

// Todos los campos que se pueden modificar en algún rol (sin repetir), para poder cambiar de rol sin perder lo
// escrito ni pedir de nuevo lo común.
const ALL_EDITABLE_FIELDS = new Map(
  Object.values(PROFILE_FIELDS)
    .flat()
    .filter(isEditable)
    .map((field) => [field.name, field]),
)

// Valores iniciales del formulario: todos los campos modificables de cualquier rol, siempre como texto, y el
// rol actual de la cuenta.
export function initialValues(rolDeLaCuenta, usuario) {
  const valores = Object.fromEntries(
    [...ALL_EDITABLE_FIELDS.values()].map((field) => {
      const texto = String(usuario[field.name] ?? '')
      return [field.name, field.edit.type === 'date' ? texto.slice(0, 10) : texto]
    }),
  )

  return {
    ...valores,
    rol: rolDeLaCuenta,
    tipoCliente: valores.tipoCliente || TIPO_CLIENTE_POR_DEFECTO,
  }
}

// Un mensaje por cada campo incorrecto (vacío si todo es correcto), para mostrarlos todos a la vez. Se valida
// con los campos del rol que queda (`values.rol`).
export function validateProfile(rolDeLaCuenta, values) {
  const errors = {}
  const rolDestino = values.rol ?? rolDeLaCuenta
  if (!ROLE_LABELS[rolDestino]) errors.rol = 'Elige un rol válido'

  for (const field of editFieldsFor(rolDestino).filter(isEditable)) {
    const valor = (values[field.name] ?? '').trim()
    if (field.edit.required && !valor) errors[field.name] = 'Este campo es obligatorio'
    else if (valor && field.edit.validate) {
      const mensaje = field.edit.validate(valor)
      if (mensaje) errors[field.name] = mensaje
    }
  }
  return errors
}

// Cuerpo de la petición: el rol que queda y solo los campos modificables de ese rol. El email, la fecha de
// incorporación y la contraseña no viajan nunca.
export function profilePayload(rolDeLaCuenta, values) {
  const rolDestino = values.rol ?? rolDeLaCuenta

  return Object.fromEntries(
    editFieldsFor(rolDestino)
      .filter(isEditable)
      .map((field) => [field.name, (values[field.name] ?? '').trim()]),
  )
}
