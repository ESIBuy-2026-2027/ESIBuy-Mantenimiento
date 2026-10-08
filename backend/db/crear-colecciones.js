// Crea las colecciones de ESIBuy y sus índices en MongoDB (T210, HU36).
//
// Modelo: «Planteamiento inicial BBDD» (resumen general: 10 colecciones).
// Las clases Java de cada colección (documento, repositorio, mapper y adaptador) se crean con su HU.
//
// Se puede ejecutar varias veces: si una colección o un índice ya existen, no hace nada.
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

// 1. usuarios: identidad, rol y estado.
// El índice se llama "email", igual que el que crea Spring Data con @Indexed en UsuarioDocument;
// con otro nombre, MongoDB rechazaría un segundo índice sobre la misma clave.
// PENDIENTE: «nombreComercial → único, si aplica» cuando se definan los vendedores.
crearColeccion("usuarios");
crearIndice("usuarios", { email: 1 }, { unique: true, name: "email" });

// 2. sesiones: revocables y caducables. TTL: MongoDB borra la sesión al llegar a expiresAt.
crearColeccion("sesiones");
crearIndice("sesiones", { expiresAt: 1 }, { expireAfterSeconds: 0 });
crearIndice("sesiones", { usuarioId: 1 });

// 3. factores_autenticacion: configuración 2FA/3FA, una por usuario.
crearColeccion("factores_autenticacion");
crearIndice("factores_autenticacion", { usuarioId: 1 }, { unique: true });

// 4. tokens_seguridad: códigos temporales (login, verificación de email) y recuperación de contraseña,
// distinguidos por "purpose" (decisión del equipo: una colección en vez de codigos_verificacion
// y recuperaciones_password por separado).
// PENDIENTE: índice del hash del código/token cuando se fije el nombre del campo.
crearColeccion("tokens_seguridad");
crearIndice("tokens_seguridad", { expiresAt: 1 }, { expireAfterSeconds: 0 });
crearIndice("tokens_seguridad", { usuarioId: 1, purpose: 1 });

// 5. productos: catálogo y stock.
crearColeccion("productos");
crearIndice("productos", { vendedorId: 1 });
crearIndice("productos", { categoriaIds: 1 });
crearIndice("productos", { nombre: 1 });
crearIndice("productos", { estado: 1 });
crearIndice("productos", { visible: 1 });

// 6. pedidos: compras y precios históricos.
crearColeccion("pedidos");
crearIndice("pedidos", { clienteId: 1 });
crearIndice("pedidos", { estado: 1 });
crearIndice("pedidos", { createdAt: 1 });
crearIndice("pedidos", { "lineas.productoId": 1 });
crearIndice("pedidos", { "lineas.vendedorId": 1 });

// 7. carritos: un carrito activo por cliente.
crearColeccion("carritos");
crearIndice("carritos", { clienteId: 1 }, { unique: true });

// 8. valoraciones: una por usuario y producto.
crearColeccion("valoraciones");
crearIndice("valoraciones", { usuarioId: 1, productoId: 1 }, { unique: true });

// 9. categorias: clasificación de productos.
crearColeccion("categorias");
crearIndice("categorias", { nombre: 1 }, { unique: true });

// 10. listas_deseos: una lista principal por cliente (decisión del equipo).
crearColeccion("listas_deseos");
crearIndice("listas_deseos", { usuarioId: 1 }, { unique: true });

print("Colecciones de EsiBuy listas.");
