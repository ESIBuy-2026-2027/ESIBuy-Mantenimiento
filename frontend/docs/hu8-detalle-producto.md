# HU8 · Consulta de detalle de producto (Frontend)

Documento para entender la vista de detalle de un producto para el cliente: qué muestra, qué hace cada componente, qué contrato espera del backend y cómo probarla.

- **Fecha:** 27 de octubre de 2026
- **HU:** HU8 · Consulta de detalle de producto (AB#70)
- **Task:** AB#354 (frontend: vista de detalle de producto)
- **Rama:** `feature/US8-T354-vista-detalle-producto`
- **Metodología:** TDD. Cada ciclo tiene su commit de rojo (`test(front)`) y su commit de verde (`feat(front)`)
- **Depende de:** `docs/hu7-busqueda-y-filtrado-de-productos.md` (el catálogo desde el que se llega), `docs/hu2-login.md` (sesión y rutas protegidas) y `docs/mocks-msw.md` (mocks)
- **Backend:** no existe el endpoint de detalle para clientes. El contrato de la sección 5 lo **propone el frontend**.

---

## 1. Resumen

Al pulsar un producto del catálogo, el cliente llega a `/products/:id`: una página con la imagen en grande, el nombre, el proveedor, la valoración, el precio, la disponibilidad, la descripción y una ficha con los datos del producto.

| Qué se pidió | Cómo se cumple |
|---|---|
| Vista dinámica y profesional | Dos columnas en pantallas anchas (imagen fija a la izquierda mientras se lee) y una sola en móvil. La información entra con una animación escalonada, la imagen se acerca al pasar el ratón y mientras carga hay un esqueleto con el mismo reparto, para que no salte. Se respeta `prefers-reduced-motion` |
| Imagen | `ProductDetail`: imagen a 4:3 con esquinas redondeadas y sombra. Sin imagen, o si la dirección está rota, un marcador «Sin imagen» |
| Nombre | Es el título de la página (`h1`) |
| Proveedor | «Vendido por **TecnoVera**» bajo el título y en la ficha. Es el **nombre comercial** del vendedor |
| Descripción | Sección propia. Respeta los saltos de línea. Sin descripción: «Este producto no tiene descripción.» |
| Precio | `PriceBlock`: el precio final, destacado. Con descuento, en rojo, con el original tachado, el porcentaje (`-10 %`) y «Ahorras 5,99 €» |
| Valoración | Estrellas con medias estrellas, la nota y «29 valoraciones» (en singular si es una), y un resumen de «Valoración de los clientes». Sin valoraciones: «Sin valoraciones» |
| Información relevante | Disponibilidad con un punto de color (en stock, pocas unidades, agotado o no disponible), la categoría y una ficha **Información del producto** con categoría, proveedor, disponibilidad y referencia |
| Llegar y volver | Las tarjetas del catálogo (listado, banda animada y lista de los mejores) llevan al detalle. **Volver a los productos** lleva al listado |

> Ocultar o mostrar pantallas es **experiencia de usuario**. El backend debe responder 401 o 403 si quien consulta no es un cliente, y 404 si el producto no existe o no está disponible.

---

## 2. Flujo

```mermaid
flowchart TD
    A[Cliente pulsa un producto del catálogo] --> B[/products/:id]
    B --> C{¿Sesión de cliente?}
    C -- Sin sesión --> L[Redirige a /login]
    C -- Otro rol --> R[Redirige al destino de su rol]
    C -- Cliente --> D[Esqueleto de carga y pide el producto al backend]
    D --> E{¿Respuesta?}
    E -- 404 --> F[No se ha encontrado el producto + Volver a los productos]
    E -- Otro error --> G[Mensaje de error + Reintentar]
    G -- Reintentar --> D
    E -- 200 --> H[Detalle: imagen, nombre, proveedor, valoración, precio, disponibilidad, descripción y ficha]
    H -- Volver a los productos --> P[/products]
```

---

## 3. Componentes y archivos

### Página y ruta

| Archivo | Qué hace |
|---|---|
| `pages/ProductDetailPage.jsx` | Pide el producto por su identificador (`useParams`), decide el estado (cargando con esqueleto, error con **Reintentar**, no encontrado o detalle) y traduce el identificador de categoría a su nombre con `GET /api/categorias` |
| `routes/AppRoutes.jsx` y `routes/paths.js` | Ruta `PRODUCT_DETAIL` (`/products/:id`) dentro de `ProtectedRoute` con el rol `CLIENTE`, y `getProductDetailPath(id)`, que codifica el identificador en la dirección |

### Vista de detalle (`features/catalog/`)

| Archivo | Qué hace |
|---|---|
| `ProductDetail.jsx` | La vista, que solo presenta (recibe el producto): imagen, categoría, título, proveedor, valoración, panel de precio y disponibilidad, descripción, ficha y resumen de valoración |
| `PriceBlock.jsx` | El precio. Muestra el que **llega calculado por el servidor** (`precioFinal`); si hay `descuento`, el original tachado, el porcentaje y el ahorro (`precio - precioFinal`). No recalcula nada |
| `stockStatus.js` | La disponibilidad a partir de `stock` y `disponible`: **No disponible** (el vendedor lo ha retirado), **Agotado** (0 unidades), **Solo quedan N unidades** (de 1 a 5; «Solo queda 1 unidad» en singular) y **En stock** |
| `RatingStars.jsx` | Las estrellas (compartidas con las tarjetas del catálogo). Admite un tamaño (`size`) para usarlas más grandes aquí |

### API, mocks y utilidades

| Archivo | Qué hace |
|---|---|
| `api/products.js` | `getCatalogProduct(id)` |
| `mocks/productsHandlers.js` | `GET /api/products/:id` simulado: solo productos disponibles (el resto responde 404, como el backend), con el proveedor, la valoración y el precio con el descuento de un cliente Estándar. Los nombres comerciales se repiten aquí (no se importan del listado de vendedores del administrador porque ese archivo ya importa este) |
| `utils/propTypes.js` | `productDetailPropType` |
| `test/renderApp.jsx` | Se reutiliza tal cual |

### Cambios en el catálogo de la HU7

- **`ProductCard`** deja de ser un botón y pasa a ser un **enlace** (`Link`) al detalle: se puede abrir en otra pestaña y es lo correcto para quien navega con el teclado o con un lector de pantalla.
- **`FeaturedShowcase`**: las tarjetas de la banda animada (solo ratón) navegan al detalle y los elementos de la lista numerada para teclado (HU7) son enlaces.
- **Se elimina `ProductDialog`**, la ventana de ficha de la HU7: la página de detalle la sustituye.

---

## 4. Estados de la pantalla

| Estado | Cuándo | Qué se ve |
|---|---|---|
| Cargando | Mientras llega el producto | Esqueleto con el reparto de la página y, para lectores de pantalla, «Cargando producto…» |
| Error | La petición falla (401, 403, 5xx) | «No se ha podido cargar el producto» con **Reintentar** |
| No encontrado | 404: el producto no existe o no está disponible | «No se ha encontrado el producto» y el botón **Volver a los productos** |
| Detalle | 200 | La vista completa |

---

## 5. Contrato esperado del backend

La ruta y el formato de respuesta los ha definido el frontend; **hay que acordarlos con quien desarrolle el backend**.

`GET /api/products/{id}`

Respuesta 200:

```json
{
  "id": "p-1",
  "nombre": "Auriculares inalámbricos",
  "descripcion": "Auriculares con cancelación de ruido y 30 horas de batería.",
  "imagen": "https://tienda.test/auriculares.png",
  "categoriaId": "cat-1",
  "vendedor": { "id": "u-2", "nombreComercial": "TecnoVera" },
  "precio": 59.9,
  "descuento": { "porcentaje": 10 },
  "precioFinal": 53.91,
  "stock": 34,
  "disponible": true,
  "valoracion": 3.6,
  "numValoraciones": 16
}
```

| Estado | Significado para el frontend |
|---|---|
| 404 | El producto no existe **o no está disponible**: «No se ha encontrado el producto» |
| 401 / 403 / 5xx | Mensaje de error con **Reintentar** |

- **El precio lo calcula el servidor** para quien consulta: `precio` es el original, `descuento` el que le corresponde a **ese** cliente (Estándar o Premium; `null` si no tiene) y `precioFinal` el que paga. El frontend solo lo muestra y nunca recalcula el precio. La regla de cuál es el descuento de cada tipo de cliente es del backend (el mock supone: un cliente Premium tiene el descuento premium si está activo y, si no, el normal; uno Estándar, el normal).
- **El proveedor** es el nombre comercial del vendedor (`vendedor.nombreComercial`). El producto solo guarda el identificador del vendedor, así que el módulo de productos tiene que pedirlo al de usuarios por su puerto de entrada.
- `valoracion` es la media de 0 a 5 y es `null` si nadie lo ha valorado (entonces `numValoraciones` es 0). El backend **todavía no guarda valoraciones**.
- `imagen` y `descripcion` pueden llegar vacías.
- `stock` se muestra al cliente solo como aviso de pocas unidades (de 1 a 5). Exponer la cantidad exacta es una decisión de producto que conviene confirmar.
- La categoría llega como **identificador**, como en el resto del catálogo; el frontend la traduce con `GET /api/categorias`.
- No debe devolver datos que el cliente no necesita (otros descuentos, datos del vendedor más allá del nombre comercial…).

---

## 6. Datos de prueba y cómo verlo en el navegador

Con `VITE_USE_MOCKS=true` (`frontend/.env.development`), `npm run dev` y la cuenta de cliente `cliente@esibuy.test` (contraseña `Mock-Clave-1!`, códigos `654321` y `123456`, ver `docs/hu2-login.md`):

1. En `/products`, pulsar una tarjeta del listado, una de la banda animada o un elemento de la lista de los mejores (con el tabulador aparece la lista del 1 al 10).
2. En el detalle: `Auriculares inalámbricos` (`/products/p-1`) tiene descuento (-10 %); `Aspirador de mano` (`/products/p-11`), solo 3 unidades; `Ratón ergonómico` (`/products/p-3`), agotado. Cada séptimo producto no tiene valoraciones y cada noveno, imagen.
3. Abrir `/products/p-6` (la webcam, no disponible) o `/products/inexistente`: «No se ha encontrado el producto».
4. **Volver a los productos** lleva al listado.
5. Con `vendedor@esibuy.test`, abrir `/products/p-1` lleva a `/seller/products`; sin sesión, a `/login`.

### Tests automáticos

```bash
npx vitest run
npm run lint
npm run build
```

| Archivo | Tests | Qué cubre |
|---|---|---|
| `pages/ProductDetailPage.test.jsx` (nuevo) | 24 | Acceso por rol; imagen, categoría, proveedor, descripción y valoración; ficha de información; precio sin y con descuento (se muestra el del servidor); disponibilidad (en stock, pocas unidades, singular, agotado, no disponible); sin valoraciones, sin imagen y sin descripción; carga, producto no encontrado, error con reintento y volver |
| `features/catalog/ProductCard.test.jsx` | 2 nuevos (sustituyen a 2) | La tarjeta es un enlace al detalle y el identificador se codifica en la dirección |
| `pages/ProductsPage.test.jsx` | 4 nuevos (sustituyen a 4) | Desde el listado, desde la animación y con el teclado desde la lista de los mejores se llega al detalle, y **Volver a los productos** regresa al listado |

---

## 7. Decisiones y librerías

- **Contrato propuesto por el frontend**; el backend aún no tiene el endpoint de detalle ni las valoraciones.
- **El servidor calcula el precio** (decisión del equipo): el cliente puede ser Estándar o Premium y el navegador no decide cuánto se paga.
- **Comprar desde el detalle (HU9):** el detalle tiene un selector de cantidad (entre 1 y las existencias) y el botón «Añadir al carrito» (`AddToCartButton`). Un producto agotado o no disponible no se puede añadir. Ver `docs/hu9-carrito-de-compra.md`.
- **Una página, no una ventana:** el detalle tiene dirección propia (`/products/:id`), así que se puede compartir, abrir en otra pestaña y volver con el botón del navegador. Sustituye a la ventana de ficha de la HU7.
- **Tarjetas como enlaces:** las tarjetas del catálogo son enlaces reales (`Link`), no botones con un manejador.
- **No se ha inventado información:** no hay envío, devoluciones, garantía, opiniones ni productos relacionados porque no hay datos ni historias para ellos.
- **Reutiliza** `EmptyState`, `LinkButton`, `useApiQuery`, `useCategories`, `RatingStars` y los componentes de Fluent UI (`Skeleton`, `Badge`, `MessageBar`). No se ha añadido ninguna librería.

---

## 8. Limitaciones y trabajo pendiente

- **Backend:** falta `GET /api/products/{id}`, el nombre comercial del vendedor en el módulo de productos, el cálculo del descuento por tipo de cliente y guardar valoraciones.
- **Pagar:** el carrito existe desde la HU9 (`docs/hu9-carrito-de-compra.md`); desde el detalle solo se añade al carrito. La pasarela de pago es la HU10.
- **Opiniones:** solo se muestra la nota media y el número de valoraciones; la lista de opiniones y su reparto por estrellas llegarán con la historia de valoraciones.
- **Una sola imagen:** el producto tiene una imagen; no hay galería.
- **Volver al listado:** **Volver a los productos** (y el botón Atrás del navegador) vuelve a cargar el listado desde la primera página, así que se pierde la posición del scroll infinito.
- **Verificación visual parcial:** la vista se vio en el navegador con datos fijos (escritorio y móvil; con descuento y pocas unidades, y agotado sin imagen ni valoraciones), pero no la pantalla completa con los mocks (MSW no se registra en el navegador integrado de Claude Code). Conviene que el equipo compruebe el conjunto.
- **Descripción larga:** se muestra entera; no hay «ver más».

---

## 9. Uso de IA generativa

- **Herramienta:** Claude Code (Sonnet 5.5).
- **Qué hizo la IA:** escribió con TDD los dos ciclos, la vista de detalle, el bloque de precio, la disponibilidad, la página con sus estados, el contrato propuesto, los mocks, el cambio de las tarjetas a enlaces, la documentación y la matriz de trazabilidad.
- **Qué hizo el equipo:** pidió una vista dinámica y profesional con imagen, nombre, proveedor, descripción, precio, valoración y la información relevante para el cliente; decidió que el servidor calcule el precio y que no haya botón de compra hasta que exista el carrito. El código se revisa, ejecuta y valida por el equipo antes de integrarlo.
