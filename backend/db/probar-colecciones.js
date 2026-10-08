// Comprueba que las colecciones creadas por crear-colecciones.js funcionan: validación de esquema,
// índices únicos e índices TTL (T210, HU36).
//
// SOLO para un MongoDB desechable (Docker): inserta documentos de prueba. Se niega a ejecutarse
// contra Atlas. Cómo ejecutarlo desde la raíz del repositorio (Docker Desktop abierto):
//
//   docker run -d --rm --name prueba-mongo mongo:8.0
//   docker cp backend/db/. prueba-mongo:/tmp/db/
//   docker exec prueba-mongo sh -c "mongosh --quiet --file /tmp/db/crear-colecciones.js && mongosh --quiet --file /tmp/db/probar-colecciones.js"
//   docker stop prueba-mongo
//
// Resultado esperado: una línea OK por caso y «TODOS LOS CASOS OK». La prueba del TTL tarda hasta
// 75 segundos, porque MongoDB borra los documentos caducados cada 60 segundos.

// throw (y no quit) para que se detenga en cualquier shell: la de Compass no admite quit().
if (JSON.stringify(db.hello()).includes("mongodb.net")) {
  throw new Error("Este script inserta datos de prueba: ejecútalo contra un MongoDB de Docker, nunca contra Atlas.");
}

const b = db.getSiblingDB("EsiBuy");
const ahora = new Date();
let fallos = 0;

function caso(descripcion, coleccion, documento, debeAceptarse) {
  let aceptado = true;
  let motivo = "";
  try {
    b.getCollection(coleccion).insertOne(documento);
  } catch (e) {
    aceptado = false;
    motivo = e.code === 121 ? "validación" : (e.code === 11000 ? "duplicado" : String(e.code || e));
  }
  const ok = aceptado === debeAceptarse;
  if (!ok) fallos++;
  print(`${ok ? "OK " : "MAL"} ${coleccion}: ${descripcion} -> ${aceptado ? "aceptado" : "rechazado (" + motivo + ")"}`);
}

print("== Validación de esquema y campos obligatorios");
// Usuario tal y como lo guarda la aplicación (UsuarioDocument): _id ObjectId, enteros y fechas.
caso("usuario como lo guarda la aplicación", "usuarios", { _id: ObjectId(), email: "app@uclm.es",
  passwordHash: "$2a$10$hash", rol: "CLIENTE", tipoCliente: "ESTANDAR", estado: "ACTIVO",
  failedLoginAttempts: NumberInt(0), createdAt: ahora, updatedAt: ahora }, true);
caso("administrador sin tipoCliente ni nombre", "usuarios", { email: "admin@uclm.es", passwordHash: "h", rol: "ADMINISTRADOR" }, true);
caso("sin bloqueo con lockedUntil null (como el ejemplo del PDF)", "usuarios", { email: "nulo@uclm.es",
  passwordHash: "h", rol: "CLIENTE", lockedUntil: null, lastLoginAt: null }, true);
caso("campo nuevo no previsto (validación permisiva)", "usuarios", { email: "dni@uclm.es", passwordHash: "h", rol: "CLIENTE", dni: "12345678Z" }, true);
caso("sin passwordHash", "usuarios", { email: "x@uclm.es", rol: "CLIENTE" }, false);
caso("rol inexistente", "usuarios", { email: "y@uclm.es", passwordHash: "h", rol: "SUPERUSUARIO" }, false);
caso("tipoCliente inexistente", "usuarios", { email: "z@uclm.es", passwordHash: "h", rol: "CLIENTE", tipoCliente: "VIP" }, false);
caso("intentos fallidos negativos", "usuarios", { email: "w@uclm.es", passwordHash: "h", rol: "CLIENTE", failedLoginAttempts: NumberInt(-1) }, false);

caso("sesión válida", "sesiones", { usuarioId: "u1", tokenHash: "t", estado: "ACTIVA", expiresAt: new Date(Date.now() + 3600000) }, true);
caso("sesión con estado inventado", "sesiones", { usuarioId: "u1", tokenHash: "t", estado: "ABIERTA", expiresAt: ahora }, false);
caso("sesión sin caducidad", "sesiones", { usuarioId: "u1", tokenHash: "t", estado: "ACTIVA" }, false);

caso("2FA válido", "factores_autenticacion", { usuarioId: "u1", totpEnabled: true }, true);
caso("2FA con totpEnabled como texto", "factores_autenticacion", { usuarioId: "u2", totpEnabled: "sí" }, false);

caso("token con purpose inventado", "tokens_seguridad", { usuarioId: "u1", purpose: "OTRO", expiresAt: ahora }, false);

caso("producto válido", "productos", { vendedorId: "v1", nombre: "Teclado", precio: 29.99, stock: NumberInt(10) }, true);
caso("producto con precio 0", "productos", { vendedorId: "v1", nombre: "Gratis", precio: 0, stock: NumberInt(1) }, false);
caso("producto con stock negativo", "productos", { vendedorId: "v1", nombre: "Ratón", precio: 5, stock: NumberInt(-1) }, false);
caso("producto sin nombre", "productos", { vendedorId: "v1", precio: 5, stock: NumberInt(1) }, false);
caso("producto con precio como texto", "productos", { vendedorId: "v1", nombre: "Pantalla", precio: "29.99", stock: NumberInt(1) }, false);

caso("pedido válido", "pedidos", { clienteId: "c1", estado: "PENDIENTE",
  lineas: [{ productoId: "p1", cantidad: NumberInt(2), precioUnitario: 19.99 }], total: 39.98 }, true);
caso("pedido sin líneas", "pedidos", { clienteId: "c1", estado: "PENDIENTE", lineas: [] }, false);
caso("pedido con cantidad 0", "pedidos", { clienteId: "c1", estado: "PENDIENTE",
  lineas: [{ productoId: "p1", cantidad: NumberInt(0), precioUnitario: 1 }] }, false);

caso("carrito con cantidad 0", "carritos", { clienteId: "c9", items: [{ productoId: "p1", cantidad: NumberInt(0) }] }, false);
caso("valoración de 6", "valoraciones", { usuarioId: "u9", productoId: "p9", puntuacion: NumberInt(6) }, false);
caso("valoración de 0", "valoraciones", { usuarioId: "u9", productoId: "p9", puntuacion: NumberInt(0) }, false);
caso("categoría con nombre vacío", "categorias", { nombre: "" }, false);
caso("lista de deseos sin usuario", "listas_deseos", { items: [] }, false);

print("== Índices únicos");
caso("primer usuario con un email", "usuarios", { email: "unico@uclm.es", passwordHash: "h", rol: "CLIENTE" }, true);
caso("segundo usuario con el mismo email", "usuarios", { email: "unico@uclm.es", passwordHash: "h", rol: "CLIENTE" }, false);
caso("primer carrito de un cliente", "carritos", { clienteId: "c1", items: [] }, true);
caso("segundo carrito del mismo cliente", "carritos", { clienteId: "c1", items: [] }, false);
caso("valoración de un producto", "valoraciones", { usuarioId: "u1", productoId: "p1", puntuacion: NumberInt(5) }, true);
caso("mismo usuario, otro producto", "valoraciones", { usuarioId: "u1", productoId: "p2", puntuacion: NumberInt(4) }, true);
caso("mismo usuario y mismo producto", "valoraciones", { usuarioId: "u1", productoId: "p1", puntuacion: NumberInt(3) }, false);
caso("primera categoría con un nombre", "categorias", { nombre: "Hogar" }, true);
caso("segunda categoría con el mismo nombre", "categorias", { nombre: "Hogar" }, false);
caso("primera lista de deseos de un usuario", "listas_deseos", { usuarioId: "u1", items: [] }, true);
caso("segunda lista del mismo usuario", "listas_deseos", { usuarioId: "u1", items: [] }, false);
caso("segundo 2FA del mismo usuario", "factores_autenticacion", { usuarioId: "u1", totpEnabled: false }, false);
caso("dos pedidos del mismo cliente (permitido)", "pedidos", { clienteId: "c1", estado: "PAGADO",
  lineas: [{ productoId: "p2", cantidad: NumberInt(1), precioUnitario: 5 }] }, true);

print("== Índices TTL (hasta 75 s)");
b.sesiones.insertOne({ usuarioId: "ttl", tokenHash: "t", estado: "EXPIRADA", expiresAt: new Date(Date.now() - 60000) });
b.tokens_seguridad.insertOne({ usuarioId: "ttl", purpose: "LOGIN_2FA", expiresAt: new Date(Date.now() - 60000) });
let pendientes = -1;
for (let i = 0; i < 15 && pendientes !== 0; i++) {
  sleep(5000);
  pendientes = b.sesiones.countDocuments({ usuarioId: "ttl" }) + b.tokens_seguridad.countDocuments({ usuarioId: "ttl" });
}
if (pendientes !== 0) fallos++;
print(`${pendientes === 0 ? "OK " : "MAL"} sesión y token caducados borrados automáticamente`);
const vigentes = b.sesiones.countDocuments({ usuarioId: "u1" });
if (vigentes !== 1) fallos++;
print(`${vigentes === 1 ? "OK " : "MAL"} la sesión vigente se conserva`);

if (fallos !== 0) {
  throw new Error(`${fallos} CASOS NO ESPERADOS`);
}
print("TODOS LOS CASOS OK");
