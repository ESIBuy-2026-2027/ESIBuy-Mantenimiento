# HU7 · Búsqueda y filtrado de productos (Frontend)

Documento para entender la vista de productos del cliente: qué muestra, cómo se filtra y se ordena, qué hace cada componente, qué contrato espera del backend y cómo probarlo.

- **Fecha:** 26 de octubre de 2026
- **HU:** HU7 · Búsqueda y filtrado de productos (AB#69)
- **Tasks:** AB#350 (frontend: vista de productos) y AB#351 (frontend: vista de filtrado y búsqueda)
- **Rama:** `feature/US7-T350-T351-vista-de-productos-del-cliente`
- **Metodología:** TDD. Cada ciclo tiene su commit de rojo (`test(front)`) y su commit de verde (`feat(front)`)
- **Depende de:** `docs/hu2-login.md` (sesión y rutas protegidas), `docs/hu32-listado-productos.md` (filtros que se reutilizan) y `docs/mocks-msw.md` (mocks)
- **Backend:** las tasks AB#352 (lógica de filtrado) y AB#353 (endpoint para obtener productos) están sin hacer. El contrato de la sección 5 lo **propone el frontend**.

---

## 1. Resumen

La vista de productos (`/products`) es lo primero que ve el cliente al iniciar sesión. Arriba enseña una **selección de los mejor valorados**; debajo, los productos como **tarjetas de dos en dos**, de **diez en diez**, que se cargan solas al llegar al final de la página. Permite buscar, filtrar y ordenar.

| Qué se pidió | Cómo se cumple |
|---|---|
| Lo primero que ve el cliente al iniciar sesión | `/products` es el destino del rol `CLIENTE` (`getHomePath`). Ahora es una ruta **solo para clientes**: sin sesión lleva a `/login` y un vendedor o administrador va a su propia página |
| Tarjetas de dos en dos | Una cuadrícula de dos columnas (`repeat(2, minmax(0, 1fr))`) en **todas** las pantallas, también en móvil |
| Desde fuera se ve nombre, imagen, categoría y valoración | `ProductCard`. El precio, el stock y la descripción no se muestran (son del detalle del producto, otra historia) |
| De diez en diez, cargando más al llegar al final | Scroll infinito: una marca al final de la lista (`IntersectionObserver`) pide la página siguiente 400 px antes de llegar. El botón **Ver más productos** hace lo mismo para quien no use el scroll |
| Ordenar por nombre y valoración | `Ordenar por` + botón de sentido ascendente / descendente. Por defecto, nombre de la A a la Z. **No se ordena por categoría** (decisión del equipo: no tiene sentido) |
| Filtrar por nombre, categoría y valoración | Buscador por nombre, desplegable de categoría y **valoración mínima** (4, 3 o 2 estrellas o más) |
| Selección de los mejores productos arriba | `FeaturedProducts`: los productos de 4 estrellas o más, de mejor a peor valorados, con sus imágenes en una banda curva (`TiltedGridHero`) |
| Dinámica y bonita | Banda de imágenes curva que se desplaza sola; tarjetas que entran subiendo y apareciendo una tras otra; tarjetas que se elevan y cuya imagen se acerca al pasar el ratón; estrellas con medias estrellas; marcador cuando no hay imagen; la lista se atenúa mientras se recarga |

> Ocultar o mostrar pantallas es **experiencia de usuario**. El backend debe responder 401 o 403 si quien consulta no es un cliente.

---

## 2. Flujo

```mermaid
flowchart TD
    A[Cliente inicia sesión] --> B[/products]
    B --> S[Pide los mejor valorados: 8, 4 estrellas o más]
    B --> C[Pide la primera página: 10 productos, ordenados por nombre]
    S --> S2{¿3 con imagen o más?}
    S2 -- Sí --> S3[Banda curva con la selección]
    S2 -- No o error --> S4[No se muestra la selección]
    C --> D{¿Respuesta?}
    D -- Error --> E[Mensaje de error + Reintentar]
    E -- Reintentar --> C
    D -- Sin productos y sin filtros --> F[Todavía no hay productos]
    D -- Sin productos con filtros --> G[No hay productos que coincidan + Limpiar filtros]
    D -- Con productos --> H[Tarjetas de dos en dos]
    H -- Llega al final de la página o pulsa Ver más productos --> M[Pide la página siguiente y la añade debajo]
    M --> H
    M -- Error --> M2[Aviso + Reintentar, sin perder lo cargado]
    H -- Cambia búsqueda, categoría, valoración o el orden --> I[Empieza otra vez desde la página 1]
    I --> D
    H -- No hay más páginas --> J[Has visto todos los productos]
```

La búsqueda por nombre espera 300 ms desde la última tecla antes de pedir (`useDebouncedValue`). Cualquier cambio de filtro o de orden empieza de nuevo desde la primera página: lo ya cargado se sustituye. Con algún filtro activo (búsqueda, categoría o valoración mínima) la selección de los mejores se **oculta** para centrarse en los resultados. El filtrado, el orden y la paginación los hace el **servidor**.

---

## 3. Componentes y archivos

### Página y ruta

| Archivo | Qué hace |
|---|---|
| `pages/ProductsPage.jsx` | Orquesta la pantalla: título, selección de los mejores, filtros, estados (cargando, error, catálogo vacío, sin coincidencias, tarjetas) y el final de la lista (cargando más, error al cargar más, **Ver más productos**, «Has visto todos los productos»). Traduce el identificador de categoría de cada producto a su nombre con `GET /api/categorias` |
| `routes/AppRoutes.jsx` | `/products` va dentro de `ProtectedRoute` con el rol `CLIENTE` |

### Vista de productos (`features/catalog/`)

| Archivo | Qué hace |
|---|---|
| `ProductCard.jsx` | La tarjeta: imagen (se acerca al pasar el ratón), nombre (máximo dos líneas), categoría y valoración. Si el producto no tiene imagen, o la dirección está rota, muestra un marcador con el aviso «Sin imagen». Sin valoraciones muestra «Sin valoraciones» |
| `RatingStars.jsx` | Cinco estrellas: llena por cada punto, media si sobra al menos medio punto y vacía en el resto. Para los lectores de pantalla es **una sola imagen** con su valor («Valoración: 4,5 de 5») |
| `FeaturedProducts.jsx` | La selección de los mejor valorados: pide los productos y los pinta sobre `TiltedGridHero`, con el título y los tres primeros con su nota. Solo se muestra con 3 o más productos **con imagen**; si no hay o la petición falla, no aparece y el listado sigue funcionando |
| `CatalogFilters.jsx` | Buscador y categoría (`ListFilters`, el mismo del listado del vendedor) más la valoración mínima y el orden (`SortControls`, el mismo de los listados del administrador) |
| `useCatalogFilters.js` | Estado de los filtros y del orden. Reutiliza `useListFilters` (búsqueda y categoría) y añade `minRating`, `sort` y `order` |
| `useCatalogFeed.js` | El scroll infinito: pide la primera página, acumula las siguientes con `loadMore` y empieza de cero cuando cambia un filtro. Distingue el fallo de la primera página (no hay nada que enseñar) del de las siguientes (se conserva lo cargado) |

### Componentes y hooks compartidos

| Archivo | Qué hace |
|---|---|
| `components/TiltedGridHero.jsx` | La banda de imágenes curva. **Adaptada** de «Tilted Grid Hero» (shadcn, TypeScript y Tailwind) a JavaScript y a los estilos de Fluent UI, con su lógica y su geometría originales (ver abajo) |
| `hooks/useOnReachEnd.js` | Devuelve un `ref` para poner al final de una lista y avisa cuando está a la vista o a 400 px de estarlo. Se vuelve a observar tras cada carga, así que sigue cargando si la lista no llena la pantalla |

**`TiltedGridHero`:** una fila de imágenes que avanza de derecha a izquierda por el interior de un cilindro: el centro mira de frente y los extremos se curvan hacia quien mira y se difuminan. Como una caja CSS no se puede curvar, cada imagen se corta en 16 tiras con su parte de la imagen, giradas alrededor del eje; solo usa transformaciones (va en el compositor). La banda es **decorativa** (`aria-hidden`), no recibe clics y con `prefers-reduced-motion` se queda quieta. Al montarse pide todas las imágenes de antemano. Lo que se adaptó respecto al original: sin Tailwind (estilos de Fluent con `makeStyles`), sin `cn` (`mergeClasses`), `PropTypes` en lugar de tipos de TypeScript, y sin `"use client"`. No hizo falta añadir shadcn, Tailwind ni TypeScript, ni ninguna librería.

### API, mocks y utilidades

| Archivo | Qué hace |
|---|---|
| `api/products.js` | `getCatalogProducts({ search, category, minRating, sort, order, page, size })`. Omite los parámetros vacíos |
| `mocks/productsHandlers.js` | `GET /api/products` simulado: solo productos disponibles, de todos los vendedores, con búsqueda, categoría, valoración mínima y orden. Las imágenes son dibujos con la inicial del producto (no dependen de internet), cada séptimo producto no tiene valoraciones y cada noveno, imagen |
| `utils/format.js` | `formatRating`: un decimal y coma (4,5) |
| `utils/propTypes.js` | `catalogProductPropType` |
| `test/setup.js` | `IntersectionObserver` de mentira (jsdom no lo trae): no avisa nunca; los tests del scroll infinito ponen uno propio que dispara a mano |

---

## 4. Estados de la pantalla

| Estado | Cuándo | Qué se ve |
|---|---|---|
| Cargando | Primera carga | Spinner «Cargando productos…» |
| Error | Falla la primera página (401, 403, 5xx) | Aviso de error con **Reintentar** |
| Catálogo vacío | Sin productos y **sin filtros** | «Todavía no hay productos». No se muestran los filtros |
| Sin coincidencias | Sin productos **con** algún filtro | «No hay productos que coincidan con los filtros» y **Limpiar filtros** (no cambia el orden) |
| Con productos | Al menos uno | Tarjetas de dos en dos; al llegar al final, se cargan otros diez |
| Cargando más | Pidiendo la página siguiente | Spinner «Cargando más productos…» debajo de las tarjetas |
| Error al cargar más | Falla una página que no es la primera | «No se han podido cargar más productos» con **Reintentar**; las tarjetas ya cargadas se conservan |
| Hay más | Quedan páginas | Botón **Ver más productos** (alternativa al scroll) |
| Final | No quedan páginas | «Has visto todos los productos» |
| Recargando | Cambio de filtro u orden | Las tarjetas anteriores siguen visibles, atenuadas (`aria-busy`), hasta que llegan las nuevas |

---

## 5. Contrato esperado del backend

La ruta y el formato de respuesta los ha definido el frontend; **hay que acordarlos con quien desarrolle el backend** (AB#352 y AB#353). Se usa el **mismo endpoint** para el listado y para la selección de los mejores.

`GET /api/products?page=1&size=10&search=teclado&category=cat-2&minRating=4&sort=valoracion&order=desc`

| Parámetro | Obligatorio | Significado |
|---|---|---|
| `page` | Sí | Página, empezando en 1 |
| `size` | Sí | Elementos por página: el frontend envía 10 para el listado y 8 para la selección de los mejores |
| `search` | No | Texto contenido en el nombre, sin distinguir mayúsculas ni acentos |
| `category` | No | Identificador de la categoría |
| `minRating` | No | Valoración media mínima (el frontend envía 2, 3 o 4). Los productos sin valoraciones no la alcanzan |
| `sort` | Sí | `nombre` o `valoracion` |
| `order` | Sí | `asc` o `desc` |

La selección de los mejores es `page=1&size=8&sort=valoracion&order=desc&minRating=4`. El listado envía, por defecto, `sort=nombre&order=asc`.

Respuesta 200:

```json
{
  "items": [
    {
      "id": "p-1",
      "nombre": "Teclado mecánico",
      "imagen": "https://tienda.test/teclado.png",
      "categoriaId": "cat-1",
      "valoracion": 4.5,
      "numValoraciones": 12
    }
  ],
  "page": 1,
  "size": 10,
  "totalItems": 23,
  "totalPages": 3
}
```

| Estado | Significado para el frontend |
|---|---|
| 401 / 403 / 5xx | Listado: mensaje de error con **Reintentar**. Selección de los mejores: no se muestra |

- Solo deben llegar productos **disponibles**, de todos los vendedores.
- `totalPages` indica si quedan más: el frontend sigue pidiendo mientras `page < totalPages`. Es 0 cuando no hay resultados.
- `valoracion` es la media de 0 a 5 y es `null` si nadie ha valorado el producto (entonces `numValoraciones` es 0). El backend **todavía no guarda valoraciones**: depende de la historia de valoraciones.
- `imagen` puede llegar vacía: el producto la tiene como opcional (HU31). La selección de los mejores solo usa los que tienen imagen.
- Al ordenar por valoración, el frontend espera que los productos **sin valoraciones cuenten como 0** y que los empates se desempaten por nombre de la A a la Z.
- La categoría llega como **identificador**, igual que en el listado del vendedor (HU32); el frontend la traduce al nombre con `GET /api/categorias`.
- Con scroll infinito, si entre dos peticiones se añade o se quita algún producto, puede repetirse o saltarse uno entre páginas. El frontend no lo trata; una paginación por cursor lo evitaría.

---

## 6. Datos de prueba y cómo verlo en el navegador

Con `VITE_USE_MOCKS=true` (`frontend/.env.development`), `npm run dev` y la cuenta de cliente `cliente@esibuy.test` (contraseña `Mock-Clave-1!`, códigos `654321` y `123456`, ver `docs/hu2-login.md`):

1. Tras iniciar sesión se abre `/products`: arriba, la banda curva con los mejor valorados y sus tres primeros (Teclado mecánico 4,7, Memoria USB 128 GB 4,5 y Hub USB-C 7 en 1 4,4); debajo, 10 tarjetas.
2. Bajar hasta el final: se cargan otras 10, y luego las 3 últimas con «Has visto todos los productos». Hay 23 productos disponibles.
3. Escribir «lampara»: queda la lámpara de escritorio (no distingue tildes ni mayúsculas) y la banda desaparece.
4. Elegir la categoría **Hogar** y subir la valoración mínima a **4 estrellas o más**.
5. Ordenar por **Valoración** y pulsar el botón de sentido: los mejor valorados salen primero.
6. Buscar «zzzz»: aparece «No hay productos que coincidan» y **Limpiar filtros**.
7. Los productos sin valoraciones (por ejemplo, el séptimo) enseñan «Sin valoraciones»; los sin imagen (por ejemplo, el noveno) enseñan el marcador.
8. Con `vendedor@esibuy.test`, abrir `/products` lleva a `/seller/products`; sin sesión, a `/login`.

### Tests automáticos

```bash
npx vitest run     # 684 tests (38 son de esta HU)
npm run lint
npm run build
```

| Archivo | Tests | Qué cubre |
|---|---|---|
| `features/catalog/ProductCard.test.jsx` (nuevo) | 5 | Nombre, imagen y categoría; valoración con estrellas y número de valoraciones; sin valoraciones; sin imagen; categoría desconocida |
| `components/TiltedGridHero.test.jsx` (nuevo) | 3 | Muestra su contenido; la banda es decorativa y reparte las imágenes recibidas; sin imágenes no falla |
| `pages/ProductsPage.test.jsx` (nuevo) | 30 | Destino tras iniciar sesión y acceso por rol; tarjetas; diez por página y dos columnas; scroll infinito (más al llegar al final, última página, botón **Ver más productos**, error al cargar más con reintento); error y catálogo vacío; búsqueda sin distinguir tildes; categoría; valoración mínima; orden (sin categoría) y sentido; limpiar filtros; selección de los mejores (se muestra, qué pide, se oculta con filtros, con pocas imágenes y si falla) |
| `features/auth/LoginFlow.test.jsx` | 1 cambiado | `/products` ya no es pública: sin sesión lleva a `/login` |

Los tests de filtros, orden y selección comprueban **la petición que envía el frontend** (con un handler que captura los parámetros) y, con el mock por defecto, que el resultado se ve ordenado. El final de la página se simula con un `IntersectionObserver` de mentira que el test dispara.

---

## 7. Decisiones y librerías

- **Contrato propuesto por el frontend**, con filtrado, orden y paginación en el servidor, igual que en el listado del vendedor (HU32). El backend (AB#352 y AB#353) todavía no tiene el endpoint ni las valoraciones.
- **`/products` solo para clientes** (decisión del equipo). Antes era una ruta pública y vacía; el test que lo afirmaba se ha cambiado.
- **Dos columnas siempre** (decisión del equipo), también en móvil, dentro de una columna centrada de 860 px como máximo.
- **Scroll infinito de diez en diez** (decisión del equipo) en lugar de paginación numérica. Se añade el botón **Ver más productos** porque el scroll solo no lo puede usar quien navega con teclado o lector de pantalla.
- **Sin ordenación por categoría** (decisión del equipo: no tiene sentido). La categoría solo se filtra.
- **Selección de los mejores con el mismo endpoint** y no uno nuevo: es el listado ordenado por valoración descendente con un mínimo de 4 estrellas. Se oculta con filtros activos.
- **Componente curvo de terceros adaptado, no instalado:** el proyecto es JavaScript con Fluent UI, sin Tailwind, shadcn ni TypeScript, y añadirlos para un solo componente habría cambiado todo el proyecto. Se portó a JSX conservando su lógica.
- **Valoración mínima en lugar de «ordenar por valoración» solamente:** se puede filtrar por 4, 3 o 2 estrellas o más y, además, ordenar.
- **Se reutiliza** lo que ya había: `ListFilters`, `SortControls`, `EmptyState`, `useListFilters`, `useApiQuery`, `useCategories` y las tarjetas de Fluent UI. Se retira `Pagination` de esta vista y el hook `useCatalogProducts`, que ya no se usaban.
- **Las tarjetas no son un enlace todavía:** el detalle de un producto es otra historia.

---

## 8. Limitaciones y trabajo pendiente

- **Backend:** falta `GET /api/products` y guardar valoraciones (AB#352 y AB#353). Hasta entonces la vista solo funciona con los mocks.
- **Desplegable de categorías:** usa el `Select` de `ListFilters`. Si se integra el desplegable con búsqueda (`CategoryCombobox`, HU31), este filtro lo heredará, y los tests de categoría de esta vista habrá que adaptarlos.
- **Sin detalle de producto, precio ni carrito:** la tarjeta solo enseña lo que se ve desde fuera.
- **Maquetación sin verificar en pantalla:** los mocks (MSW) no se pueden registrar en el navegador integrado de Claude Code, así que solo se ha probado con tests (que en jsdom no ejecutan el IntersectionObserver real ni dibujan la banda 3D). Conviene que el equipo compruebe: la banda curva (tamaño, velocidad y que no tape el título y los nombres), las dos columnas en pantallas estrechas, los nombres largos, las animaciones de entrada y que el scroll infinito carga bien en el navegador.
- **Rendimiento de la banda:** son hasta 60 imágenes por 16 tiras; está pensada para el compositor, pero conviene vigilarla en móviles antiguos. Con `prefers-reduced-motion` la banda se queda quieta.
- **Páginas repetidas o saltadas:** ver la última nota de la sección 5.
- **Parámetros de la banda:** altura (380 px), tamaño de imagen, curvatura y velocidad están en `FeaturedProducts.jsx` (`tileHeight`, `aspectRatio`, `axis`, `curve`, `speed`).
- **Valoración mínima fija:** 4, 3 y 2 estrellas. Si se quiere otro tramo, es la constante `RATING_OPTIONS` de `CatalogFilters.jsx`.

---

## 9. Uso de IA generativa

- **Herramienta:** Claude Code (Sonnet 5.5).
- **Qué hizo la IA:** escribió con TDD las dos tasks (los tests en rojo y después el código), la vista de productos, la tarjeta, las estrellas, los filtros, el scroll infinito, la selección de los mejores, la adaptación a JavaScript y a Fluent del componente curvo, el contrato propuesto, los mocks, la documentación y la matriz de trazabilidad.
- **Qué hizo el equipo:** definió lo que se debe ver en la tarjeta y el diseño (dos tarjetas por fila, de diez en diez, con más al llegar al final), eligió que la vista fuese solo para clientes, que la valoración se filtrase con un mínimo, que no se ordenase por categoría, que hubiese una selección de los mejores arriba, aportó el componente de la banda curva y decidió el contrato propuesto. El código se revisa, ejecuta y valida por el equipo antes de integrarlo.
