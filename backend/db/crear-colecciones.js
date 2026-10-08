// Crea las colecciones de ESIBuy, sus índices y su validación de esquema en MongoDB (T210, HU36).
//
// Modelo: «Planteamiento inicial BBDD» (resumen general: 10 colecciones).
// Las clases Java de cada colección (documento, repositorio, mapper y adaptador) se crean con su HU.
//
// Se puede ejecutar varias veces: las colecciones e índices que ya existen no se tocan y la
// validación se vuelve a aplicar igual.
//
// Validación (permisiva): exige los campos imprescindibles y comprueba el tipo y las reglas del PDF
// de los campos que existan; admite campos nuevos para no bloquear al equipo mientras el modelo es
// preliminar. Nivel "moderate": no revalida documentos antiguos que ya no cumplían las reglas.
//
// Permisos: hace falta un usuario de base de datos con permiso de administración de colecciones
// (collMod), por ejemplo atlasAdmin o dbAdmin sobre EsiBuy. El usuario de la aplicación (MONGODB_URI)
// solo lee y escribe datos, a propósito (mínimo privilegio), y recibirá "not allowed to do action [collMod]".
//
// Cómo ejecutarlo contra Atlas (nunca escribas la contraseña en este archivo):
//   - Compass → conexión al clúster → «Open MongoDB shell» → pegar el contenido de este archivo.
//   - O con mongosh desde la raíz del repositorio:  mongosh "$env:MONGODB_URI" --file backend/db/crear-colecciones.js

const base = db.getSiblingDB("EsiBuy");

function crearColeccion(nombre) {
  if (base.getCollectionNames().includes(nombre)) {
    print(`= ${nombre}: ya existía`);
  } else {
    base.createCollection(nombre);
    print(`+ ${nombre}: creada`);
  }
}

function crearIndice(coleccion, claves, opciones = {}) {
  const nombre = base.getCollection(coleccion).createIndex(claves, opciones);
  print(`    índice ${nombre}`);
}

function validar(coleccion, obligatorios, propiedades) {
  base.runCommand({
    collMod: coleccion,
    validator: { $jsonSchema: { bsonType: "object", required: obligatorios, properties: propiedades } },
    validationLevel: "moderate",
    validationAction: "error",
  });
  print(`    validación: obligatorios ${obligatorios.join(", ")}`);
}

// Tipos comunes. Los identificadores se aceptan como texto u ObjectId: Spring Data guarda el _id como
// ObjectId y las referencias (usuarioId, productoId...) como texto.
const ID = { bsonType: ["string", "objectId"] };
const TEXTO = { bsonType: "string" };
const TEXTO_NO_VACIO = { bsonType: "string", minLength: 1 };
const FECHA = { bsonType: "date" };
const BOOLEANO = { bsonType: "bool" };
const ENTERO_NO_NEGATIVO = { bsonType: ["int", "long"], minimum: 0 };
const ENTERO_POSITIVO = { bsonType: ["int", "long"], minimum: 1 };
// Importes como número (PDF: "precio": 29.99). Si en Java se usa BigDecimal, guardarlo como Decimal128
// (@Field(targetType = FieldType.DECIMAL128)): por defecto Spring Data lo guardaría como texto.
const IMPORTE_NO_NEGATIVO = { bsonType: ["int", "long", "double", "decimal"], minimum: 0 };
const FECHAS_COMUNES = { createdAt: FECHA, updatedAt: FECHA };

// 1. usuarios: identidad, rol y estado.
// El índice se llama "email", igual que el que crea Spring Data con @Indexed en UsuarioDocument;
// con otro nombre, MongoDB rechazaría un segundo índice sobre la misma clave.
// PENDIENTE: «nombreComercial → único, si aplica» cuando se definan los vendedores.
crearColeccion("usuarios");
crearIndice("usuarios", { email: 1 }, { unique: true, name: "email" });
validar("usuarios", ["email", "passwordHash", "rol"], {
  email: TEXTO_NO_VACIO,
  passwordHash: TEXTO_NO_VACIO,
  rol: { enum: ["ADMINISTRADOR", "VENDEDOR", "CLIENTE"] },
  tipoCliente: { enum: ["PREMIUM", "ESTANDAR"] },
  // PENDIENTE solo para vendedores a la espera de verificación (decisión del equipo del 07/10/2026).
  estado: { enum: ["ACTIVO", "DESACTIVADO", "PENDIENTE"] },
  nombre: TEXTO_NO_VACIO,
  apellidos: TEXTO_NO_VACIO,
  failedLoginAttempts: ENTERO_NO_NEGATIVO,
  lockedUntil: FECHA,
  ...FECHAS_COMUNES,
});

// 2. sesiones: revocables y caducables. TTL: MongoDB borra la sesión al llegar a expiresAt.
crearColeccion("sesiones");
crearIndice("sesiones", { expiresAt: 1 }, { expireAfterSeconds: 0 });
crearIndice("sesiones", { usuarioId: 1 });
validar("sesiones", ["usuarioId", "tokenHash", "estado", "expiresAt"], {
  usuarioId: ID,
  tokenHash: TEXTO_NO_VACIO,
  estado: { enum: ["ACTIVA", "REVOCADA", "EXPIRADA"] },
  expiresAt: FECHA,
  revokedAt: { bsonType: ["date", "null"] },
  lastUsedAt: FECHA,
  ipHash: TEXTO,
  userAgent: TEXTO,
  deviceIdentifier: TEXTO,
  ...FECHAS_COMUNES,
});

// 3. factores_autenticacion: configuración 2FA/3FA, una por usuario.
// El secreto TOTP se guarda cifrado; la clave no va ni en MongoDB ni en el repositorio.
crearColeccion("factores_autenticacion");
crearIndice("factores_autenticacion", { usuarioId: 1 }, { unique: true });
validar("factores_autenticacion", ["usuarioId"], {
  usuarioId: ID,
  totpEnabled: BOOLEANO,
  totpSecretEncrypted: TEXTO_NO_VACIO,
  emailFactorEnabled: BOOLEANO,
  verifiedAt: FECHA,
  lastUsedAt: FECHA,
  ...FECHAS_COMUNES,
});

// 4. tokens_seguridad: códigos temporales (login, verificación de email) y recuperación de contraseña,
// distinguidos por "purpose" (decisión del equipo: una colección en vez de codigos_verificacion
// y recuperaciones_password por separado).
// PENDIENTE: índice del hash del código/token cuando se fije el nombre del campo.
crearColeccion("tokens_seguridad");
crearIndice("tokens_seguridad", { expiresAt: 1 }, { expireAfterSeconds: 0 });
crearIndice("tokens_seguridad", { usuarioId: 1, purpose: 1 });
validar("tokens_seguridad", ["usuarioId", "purpose", "expiresAt"], {
  usuarioId: ID,
  purpose: { enum: ["LOGIN_2FA", "EMAIL_VERIFICATION", "PASSWORD_RECOVERY"] },
  status: TEXTO,
  attempts: ENTERO_NO_NEGATIVO,
  maxAttempts: ENTERO_POSITIVO,
  expiresAt: FECHA,
  usedAt: { bsonType: ["date", "null"] },
  ...FECHAS_COMUNES,
});

// 5. productos: catálogo y stock. Validaciones del PDF: precio > 0, stock >= 0, nombre y vendedorId obligatorios.
crearColeccion("productos");
crearIndice("productos", { vendedorId: 1 });
crearIndice("productos", { categoriaIds: 1 });
crearIndice("productos", { nombre: 1 });
crearIndice("productos", { estado: 1 });
crearIndice("productos", { visible: 1 });
validar("productos", ["vendedorId", "nombre", "precio", "stock"], {
  vendedorId: ID,
  nombre: TEXTO_NO_VACIO,
  descripcion: TEXTO,
  precio: { bsonType: ["int", "long", "double", "decimal"], minimum: 0, exclusiveMinimum: true },
  stock: ENTERO_NO_NEGATIVO,
  descuento: IMPORTE_NO_NEGATIVO,
  precioFinal: IMPORTE_NO_NEGATIVO,
  categoriaIds: { bsonType: "array", items: ID },
  imagen: TEXTO,
  estado: TEXTO,
  visible: BOOLEANO,
  disponible: BOOLEANO,
  valoracionMedia: { bsonType: ["int", "long", "double", "decimal"], minimum: 0, maximum: 5 },
  numeroValoraciones: ENTERO_NO_NEGATIVO,
  ...FECHAS_COMUNES,
});

// 6. pedidos: compras y precios históricos. Los estados posibles están pendientes de acordar: solo se exige texto.
crearColeccion("pedidos");
crearIndice("pedidos", { clienteId: 1 });
crearIndice("pedidos", { estado: 1 });
crearIndice("pedidos", { createdAt: 1 });
crearIndice("pedidos", { "lineas.productoId": 1 });
crearIndice("pedidos", { "lineas.vendedorId": 1 });
validar("pedidos", ["clienteId", "estado", "lineas"], {
  clienteId: ID,
  estado: TEXTO_NO_VACIO,
  lineas: {
    bsonType: "array",
    minItems: 1,
    items: {
      bsonType: "object",
      required: ["productoId", "cantidad", "precioUnitario"],
      properties: {
        productoId: ID,
        nombreProducto: TEXTO,
        vendedorId: ID,
        cantidad: ENTERO_POSITIVO,
        precioUnitario: IMPORTE_NO_NEGATIVO,
        descuento: IMPORTE_NO_NEGATIVO,
        subtotal: IMPORTE_NO_NEGATIVO,
      },
    },
  },
  subtotal: IMPORTE_NO_NEGATIVO,
  gastosEnvio: IMPORTE_NO_NEGATIVO,
  descuentoTotal: IMPORTE_NO_NEGATIVO,
  total: IMPORTE_NO_NEGATIVO,
  direccionEnvio: { bsonType: "object" },
  paymentMethodId: ID,
  ...FECHAS_COMUNES,
});

// 7. carritos: un carrito activo por cliente.
crearColeccion("carritos");
crearIndice("carritos", { clienteId: 1 }, { unique: true });
validar("carritos", ["clienteId"], {
  clienteId: ID,
  items: {
    bsonType: "array",
    items: {
      bsonType: "object",
      required: ["productoId", "cantidad"],
      properties: { productoId: ID, cantidad: ENTERO_POSITIVO, addedAt: FECHA, updatedAt: FECHA },
    },
  },
  ...FECHAS_COMUNES,
});

// 8. valoraciones: una por usuario y producto. Regla del PDF: 1 <= puntuacion <= 5.
crearColeccion("valoraciones");
crearIndice("valoraciones", { usuarioId: 1, productoId: 1 }, { unique: true });
validar("valoraciones", ["usuarioId", "productoId", "puntuacion"], {
  usuarioId: ID,
  productoId: ID,
  puntuacion: { bsonType: ["int", "long"], minimum: 1, maximum: 5 },
  comentario: TEXTO,
  estado: TEXTO,
  ...FECHAS_COMUNES,
});

// 9. categorias: clasificación de productos.
crearColeccion("categorias");
crearIndice("categorias", { nombre: 1 }, { unique: true });
validar("categorias", ["nombre"], {
  nombre: TEXTO_NO_VACIO,
  descripcion: TEXTO,
  estado: TEXTO,
  ...FECHAS_COMUNES,
});

// 10. listas_deseos: una lista principal por cliente (decisión del equipo).
crearColeccion("listas_deseos");
crearIndice("listas_deseos", { usuarioId: 1 }, { unique: true });
validar("listas_deseos", ["usuarioId"], {
  usuarioId: ID,
  nombre: TEXTO,
  items: {
    bsonType: "array",
    items: { bsonType: "object", required: ["productoId"], properties: { productoId: ID, addedAt: FECHA } },
  },
  ...FECHAS_COMUNES,
});

print("Colecciones de EsiBuy listas.");
