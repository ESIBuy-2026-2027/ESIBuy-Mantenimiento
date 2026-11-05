# HU13 · Valoración de productos comprados (Frontend)

Documento para entender cómo ve y valora el cliente los productos en el frontend y qué contrato sigue con el backend.

- **Fecha:** 3 de noviembre de 2026
- **HU:** HU13 · Valoración de productos comprados (AB#75)
- **Tasks:** AB#368 (consulta de las valoraciones de un producto) y AB#367 (vista para valorar)
- **Rama:** `feature/US13-T367-T368-valoraciones-front`
- **Metodología:** TDD; el commit `test(front-HU13): ciclo rojo` va antes que el `feat(front-HU13): ciclo verde`
- **Depende de:** el backend de las valoraciones (T369, T370 y T371, PR #179)

---

## 1. Resumen

Criterios de aceptación de la HU13: *«solo se permite valorar productos con compra previa confirmada»* y *«no se permite valorar el mismo producto/pedido dos veces»*.

Todo vive en el **detalle del producto** (`/products/:id`, HU8), en la sección **«Valoraciones de los clientes»**:

| Parte | Qué muestra |
|---|---|
| **Media** | La nota media en grande, sus estrellas y «Basada en N valoraciones». La misma media sale también debajo del nombre del producto. |
| **«Valora este producto»** (T367) | Formulario con la puntuación (de 1 a 5 estrellas, obligatoria) y un comentario opcional de hasta 500 caracteres, con contador. Al enviarlo da las gracias, desaparece y la lista se recarga. |
| **Lista** (T368) | Cada valoración con sus estrellas y, si lo tiene, su comentario. Sin valoraciones: «Todavía no hay valoraciones de este producto». Si falla la carga: aviso con «Reintentar», sin perder el producto. |

Decisiones:

- **Se valora desde el detalle del producto**, no desde el detalle del pedido (HU11): un solo sitio para ver y valorar. El frontend no puede saber antes de enviar si el cliente lo ha comprado o si ya lo valoró: lo dice el backend al enviar y se muestra el aviso.
- **La media y el número de valoraciones salen de la lista** cuando tiene valoraciones; si está vacía, se usan `valoracion` y `numValoraciones` del detalle del producto. Hoy el backend devuelve en el detalle `valoracion: null` y `numValoraciones: 0` (`PENDIENTE DE DECIDIR (equipo)` en `DetalleProductoCatalogoResponse`), así que sin esto se vería «Sin valoraciones» junto a la lista. Cuando el backend rellene esos campos con las mismas valoraciones, coincidirán. La regla está en `features/reviews/reviewSummary.js`.
- **Las valoraciones no traen autor ni fecha** y llegan sin orden garantizado (decisión del backend). Una valoración nueva aparece donde la coloque el backend; «las más recientes primero» necesitaría la fecha en el backend.
- **Errores del envío:**

| Respuesta | Mensaje |
|---|---|
| 403 `PRODUCT_NOT_PURCHASED` | «Solo puedes valorar productos que has comprado.» |
| 409 `REVIEW_ALREADY_EXISTS` | «Ya has valorado este producto.» |
| 400 (`VALIDATION_ERROR`) | «Revisa la puntuación y el comentario.» |
| Cualquier otro | «No se ha podido enviar tu valoración. Inténtalo de nuevo.», conservando lo escrito |

Sin puntuación no se envía nada: el campo marca «Elige una puntuación de 1 a 5 estrellas».

---

## 2. Contrato con el backend (PR #179)

```
GET /api/products/{id}/valoraciones
→ 200 [ { id, productoId, puntuacion, comentario }, ... ]   ([] si no hay; sin cliente ni fecha)

POST /api/products/{id}/valoraciones   { puntuacion: 1..5, comentario: "" .. 500 caracteres }
→ 201 { id, productoId, puntuacion, comentario }
→ 400 { code: "VALIDATION_ERROR" } | 403 { code: "PRODUCT_NOT_PURCHASED" } | 409 { code: "REVIEW_ALREADY_EXISTS" }
```

Las dos rutas son solo del cliente (401 sin sesión, 403 con otro rol). El `POST` lleva el token CSRF, como el resto de peticiones que cambian datos (`api/client.js`). Para el backend, «comprado» es tener un pedido `PAGADO` con ese producto.

---

## 3. Mocks (`src/mocks/reviewsHandlers.js`)

- **Cada producto tiene 10 valoraciones de ejemplo** (salvo uno de cada siete, que no tiene ninguna), siempre las mismas, con comentarios acordes a la puntuación.
- **El catálogo calcula su media y su número con esas mismas valoraciones** (`resumenDeValoraciones`, que usa `productsHandlers.js`), como hará el backend: el catálogo, el detalle y la lista dicen lo mismo. Las medias de ejemplo no cambian respecto a las de antes; el número pasa a ser 10.
- **Valorar:** Carlos (`cliente@esibuy.test`) ha comprado los auriculares (`p-1`), el teclado (`p-2`) y el smartwatch (`p-24`), los de sus pedidos de ejemplo no cancelados (`ordersHandlers.js`). Otro producto responde 403; valorar dos veces el mismo, 409; unos datos no válidos, 400. Lo enviado se suma a la lista y a la media durante la sesión, y `resetReviewsMock()` lo limpia entre tests.

Para probarlo con `npm run dev`: entra como `cliente@esibuy.test` (contraseña `Mock-Clave-1!`, códigos `654321` y `123456`), abre el teclado desde el catálogo y baja hasta «Valoraciones de los clientes». La sesión de los mocks vive en memoria: al recargar la página hay que volver a entrar.

---

## 4. Archivos

| Archivo | Cambio |
|---|---|
| `src/features/reviews/ReviewList.jsx` | Nuevo: la lista, el estado vacío y el error con «Reintentar». |
| `src/features/reviews/ReviewForm.jsx` | Nuevo: el formulario para valorar (`Rating` y `Textarea` de Fluent UI) y sus mensajes. |
| `src/features/reviews/reviewSummary.js` | Nuevo: la media y el número de valoraciones del detalle. |
| `src/features/catalog/ProductDetail.jsx` | Sección «Valoraciones de los clientes» (antes «Valoración de los clientes», solo con media) con la media, el formulario y la lista. |
| `src/pages/ProductDetailPage.jsx` | Pide las valoraciones aparte del producto. |
| `src/api/products.js` | `getProductReviews(id)` y `createProductReview(id, valoracion)`. |
| `src/utils/propTypes.js` | `reviewPropType`. |
| `src/mocks/reviewsHandlers.js` | Nuevo: el GET y el POST de las valoraciones y las de ejemplo. |
| `src/mocks/productsHandlers.js` | La media y el número del catálogo salen de las valoraciones de ejemplo. |
| `src/mocks/handlers.js` y `src/test/setup.js` | Registro de los handlers nuevos y su reinicio entre tests. |

---

## 5. Tests

Los casos siguen la numeración del backend (HU13-CP01 a CP17). Todos están en `src/pages/ProductDetailPage.test.jsx`.

| Caso | Qué se prueba | Task |
|---|---|---|
| HU13-CP18 | Cada valoración con sus estrellas y su comentario; sin comentario, solo las estrellas (2 tests) | AB#368 |
| HU13-CP19 | La media y el número salen de la lista; con la lista vacía, los del detalle (2 tests) | AB#368 |
| HU13-CP20 | Sin valoraciones lo indica; si fallan, aviso con «Reintentar» sin perder el producto (2 tests) | AB#368 |
| HU13-CP21 | Valorar con estrellas y comentario y verla en la lista; el comentario es opcional (2 tests) | AB#367 |
| HU13-CP22 | Sin puntuación no se envía; el comentario admite como mucho 500 caracteres (2 tests) | AB#367 |
| HU13-CP23 | Avisos de no comprado (403), ya valorado (409) y error general conservando lo escrito (3 tests) | AB#367 |

Los tests de la HU8 que usan `responderCon` responden también con la lista de valoraciones vacía: así siguen comprobando la media y el número del detalle del producto.

---

## 6. Pendiente

- **Backend:** rellenar `valoracion` y `numValoraciones` del catálogo (HU7) y del detalle (HU8) con las valoraciones reales. Hasta entonces, en el **listado del catálogo** contra el backend real las tarjetas dicen «Sin valoraciones» (allí no se pide la lista de cada producto), y ordenar o filtrar por valoración no tiene efecto.
- **Orden de las valoraciones:** sin fecha en el backend no se pueden mostrar las más recientes primero.
- **Casos HU13-CP18 a CP23:** propuestos; pendientes de confirmar con el equipo y de añadir a la HU13 en Azure Boards.
