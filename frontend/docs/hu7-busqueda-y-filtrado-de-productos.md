# HU7 · Búsqueda y filtrado de productos (Frontend)

Documento para entender la vista de productos del cliente: qué muestra, cómo se filtra y se ordena, qué hace cada componente, qué contrato espera del backend y cómo probarlo.

- **Fecha:** 26 de octubre de 2026
- **HU:** HU7 · Búsqueda y filtrado de productos (AB#69)
- **Tasks:** AB#350 (frontend: vista de productos) y AB#351 (frontend: vista de filtrado y búsqueda)
- **Rama:** `feature/US7-T350-T351-vista-de-productos-del-cliente`
- **Metodología:** TDD. Cada task tiene su commit de rojo (`test(front)`) y su commit de verde (`feat(front)`)
- **Depende de:** `docs/hu2-login.md` (sesión y rutas protegidas), `docs/hu32-listado-productos.md` (filtros y paginación que se reutilizan) y `docs/mocks-msw.md` (mocks)
- **Backend:** las tasks AB#352 (lógica de filtrado) y AB#353 (endpoint para obtener productos) están sin hacer. El contrato de la sección 5 lo **propone el frontend**.

---

## 1. Resumen

La vista de productos (`/products`) es lo primero que ve el cliente al iniciar sesión. Muestra los productos como **tarjetas, de dos en dos**, y permite buscar, filtrar y ordenar.

| Qué se pidió | Cómo se cumple |
|---|---|
| Lo primero que ve el cliente al iniciar sesión | `/products` es el destino del rol `CLIENTE` (`getHomePath`). Ahora es una ruta **solo para clientes**: sin sesión lleva a `/login` y un vendedor o administrador va a su propia página |
| Tarjetas de dos en dos | Una cuadrícula de dos columnas (`repeat(2, minmax(0, 1fr))`) en **todas** las pantallas, también en móvil |
| Desde fuera se ve nombre, imagen, categoría y valoración | `ProductCard`. El precio, el stock y la descripción no se muestran (son del detalle del producto, otra historia) |
| Ordenar por nombre, categoría y valoración | `Ordenar por` + botón de sentido ascendente / descendente. Por defecto, nombre de la A a la Z |
| Filtrar por nombre, categoría y valoración | Buscador por nombre, desplegable de categoría y **valoración mínima** (4, 3 o 2 estrellas o más) |
| Dinámica y bonita | Estrellas (con medias estrellas), tarjetas que se elevan al pasar el ratón, marcador cuando no hay imagen, y la lista se atenúa mientras se recarga |

> Ocultar o mostrar pantallas es **experiencia de usuario**. El backend debe responder 401 o 403 si quien consulta no es un cliente.

---

## 2. Flujo

```mermaid
flowchart TD
    A[Cliente inicia sesión] --> B[/products]
    B --> C[Pide la primera página al backend: 12 productos, ordenados por nombre]
    C --> D{¿Respuesta?}
    D -- Error --> E[Mensaje de error + Reintentar]
    E -- Reintentar --> C
    D -- Sin productos y sin filtros --> F[Todavía no hay productos]
    D -- Sin productos con filtros --> G[No hay productos que coincidan + Limpiar filtros]
    D -- Con productos --> H[Tarjetas de dos en dos + paginación]
    H -- Cambia búsqueda, categoría, valoración o el orden --> I[Vuelve a la página 1 y pide de nuevo]
    I --> D
    H -- Cambia de página --> C
```

La búsqueda por nombre espera 300 ms desde la última tecla antes de pedir (`useDebouncedValue`). Cualquier cambio de filtro o de orden vuelve a la página 1. La paginación, el filtrado y el orden los hace el **servidor**.

---

## 3. Componentes y archivos

### Página y ruta

| Archivo | Qué hace |
|---|---|
| `pages/ProductsPage.jsx` | Orquesta la pantalla: título, filtros, estados (cargando, error, catálogo vacío, sin coincidencias, tarjetas) y paginación. Traduce el identificador de categoría de cada producto a su nombre con `GET /api/categorias` |
| `routes/AppRoutes.jsx` | `/products` va dentro de `ProtectedRoute` con el rol `CLIENTE` |

### Vista de productos (`features/catalog/`)

| Archivo | Qué hace |
|---|---|
| `ProductCard.jsx` | La tarjeta: imagen, nombre (máximo dos líneas), categoría y valoración. Si el producto no tiene imagen, o la dirección está rota, muestra un marcador con el aviso «Sin imagen». Sin valoraciones muestra «Sin valoraciones» |
| `RatingStars.jsx` | Cinco estrellas: llena por cada punto, media si sobra al menos medio punto y vacía en el resto. Para los lectores de pantalla es **una sola imagen** con su valor («Valoración: 4,5 de 5») |
| `CatalogFilters.jsx` | Buscador y categoría (`ListFilters`, el mismo del listado del vendedor) más la valoración mínima y el orden (`SortControls`, el mismo de los listados del administrador) |
| `useCatalogFilters.js` | Estado de los filtros y del orden. Reutiliza `useListFilters` (búsqueda, categoría y página) y añade `minRating`, `sort` y `order` |
| `useCatalogProducts.js` | Pide la página al backend con `useApiQuery` |

### API, mocks y utilidades

| Archivo | Qué hace |
|---|---|
| `api/products.js` | `getCatalogProducts({ search, category, minRating, sort, order, page, size })`. Omite los parámetros vacíos |
| `mocks/productsHandlers.js` | `GET /api/products` simulado: solo productos disponibles, de todos los vendedores, con búsqueda, categoría, valoración mínima y orden. Las imágenes son dibujos con la inicial del producto (no dependen de internet), cada séptimo producto no tiene valoraciones y cada noveno no tiene imagen |
| `utils/format.js` | `formatRating`: un decimal y coma (4,5) |
| `utils/propTypes.js` | `catalogProductPropType` |

---

## 4. Estados de la pantalla

| Estado | Cuándo | Qué se ve |
|---|---|---|
| Cargando | Primera carga | Spinner «Cargando productos…» |
| Error | La petición falla (401, 403, 5xx) | Aviso de error con **Reintentar** |
| Catálogo vacío | Sin productos y **sin filtros** | «Todavía no hay productos». No se muestran los filtros |
| Sin coincidencias | Sin productos **con** algún filtro | «No hay productos que coincidan con los filtros» y **Limpiar filtros** (no cambia el orden) |
| Con productos | Al menos uno | Tarjetas de dos en dos y paginación de 12 en 12 |
| Recargando | Cambio de filtro, orden o página | Las tarjetas anteriores siguen visibles, atenuadas (`aria-busy`) |

---

## 5. Contrato esperado del backend

La ruta y el formato de respuesta los ha definido el frontend; **hay que acordarlos con quien desarrolle el backend** (AB#352 y AB#353).

`GET /api/products?page=1&size=12&search=teclado&category=cat-2&minRating=4&sort=valoracion&order=desc`

| Parámetro | Obligatorio | Significado |
|---|---|---|
| `page` | Sí | Página, empezando en 1 |
| `size` | Sí | Elementos por página (el frontend envía 12) |
| `search` | No | Texto contenido en el nombre, sin distinguir mayúsculas ni acentos |
| `category` | No | Identificador de la categoría |
| `minRating` | No | Valoración media mínima (el frontend envía 2, 3 o 4). Los productos sin valoraciones no la alcanzan |
| `sort` | Sí | `nombre`, `categoria` (por el **nombre** de la categoría) o `valoracion` |
| `order` | Sí | `asc` o `desc` |

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
  "size": 12,
  "totalItems": 23,
  "totalPages": 2
}
```

| Estado | Significado para el frontend |
|---|---|
| 401 / 403 / 5xx | Mensaje de error con **Reintentar** |

- Solo deben llegar productos **disponibles**, de todos los vendedores.
- `valoracion` es la media de 0 a 5 y es `null` si nadie ha valorado el producto (entonces `numValoraciones` es 0). El backend **todavía no guarda valoraciones**: depende de la historia de valoraciones.
- `imagen` puede llegar vacía: el producto la tiene como opcional (HU31).
- Al ordenar por valoración, el frontend espera que los productos **sin valoraciones cuenten como 0** y que los empates se desempaten por nombre de la A a la Z.
- La categoría llega como **identificador**, igual que en el listado del vendedor (HU32); el frontend la traduce al nombre con `GET /api/categorias`.
- `totalPages` es 0 cuando no hay resultados.

---

## 6. Datos de prueba y cómo verlo en el navegador

Con `VITE_USE_MOCKS=true` (`frontend/.env.development`), `npm run dev` y la cuenta de cliente `cliente@esibuy.test` (contraseña `Mock-Clave-1!`, códigos `654321` y `123456`, ver `docs/hu2-login.md`):

1. Tras iniciar sesión se abre `/products` con 12 tarjetas, y «Mostrando 1–12 de 23 productos» abajo.
2. Escribir «lampara»: queda la lámpara de escritorio (no distingue tildes ni mayúsculas).
3. Elegir la categoría **Hogar** y subir la valoración mínima a **4 estrellas o más**.
4. Ordenar por **Valoración** y pulsar el botón de sentido: los mejor valorados salen primero.
5. Buscar «zzzz»: aparece «No hay productos que coincidan» y **Limpiar filtros**.
6. Los productos sin valoraciones (por ejemplo, el séptimo) enseñan «Sin valoraciones»; los sin imagen (por ejemplo, el noveno) enseñan el marcador.
7. Con `vendedor@esibuy.test`, abrir `/products` lleva a `/seller/products`; sin sesión, a `/login`.

### Tests automáticos

```bash
npx vitest run     # 674 tests (28 son de esta HU)
npm run lint
npm run build
```

| Archivo | Tests | Qué cubre |
|---|---|---|
| `features/catalog/ProductCard.test.jsx` (nuevo) | 5 | Nombre, imagen y categoría; valoración con estrellas y número de valoraciones; sin valoraciones; sin imagen; categoría desconocida |
| `pages/ProductsPage.test.jsx` (nuevo) | 23 | Destino tras iniciar sesión y acceso por rol; tarjetas; doce por página y dos columnas; paginación; error con reintento; catálogo vacío; búsqueda sin distinguir tildes; categoría; valoración mínima; orden y sentido; limpiar filtros |
| `features/auth/LoginFlow.test.jsx` | 1 cambiado | `/products` ya no es pública: sin sesión lleva a `/login` |

Los tests de filtros y orden comprueban **la petición que envía el frontend** (con un handler que captura los parámetros) y, con el mock por defecto, que el resultado se ve ordenado.

---

## 7. Decisiones y librerías

- **Contrato propuesto por el frontend**, con filtrado, orden y paginación en el servidor, igual que en el listado del vendedor (HU32). El backend (AB#352 y AB#353) todavía no tiene el endpoint ni las valoraciones.
- **`/products` solo para clientes** (decisión del equipo). Antes era una ruta pública y vacía; el test que lo afirmaba se ha cambiado.
- **Dos columnas siempre** (decisión del equipo), también en móvil, dentro de una columna centrada de 860 px como máximo.
- **Valoración mínima en lugar de «ordenar por valoración» solamente:** se puede filtrar por 4, 3 o 2 estrellas o más y, además, ordenar. Categoría solo se **filtra** y se **ordena** por su nombre.
- **Se reutiliza** lo que ya había: `ListFilters`, `SortControls`, `Pagination`, `EmptyState`, `useListFilters`, `useApiQuery`, `useCategories` y las tarjetas de Fluent UI. No se ha añadido ninguna librería.
- **Las tarjetas no son un enlace todavía:** el detalle de un producto es otra historia.

---

## 8. Limitaciones y trabajo pendiente

- **Backend:** falta `GET /api/products` y guardar valoraciones (AB#352 y AB#353). Hasta entonces la vista solo funciona con los mocks.
- **Desplegable de categorías:** usa el `Select` de `ListFilters`. Si se integra el desplegable con búsqueda (`CategoryCombobox`, HU31), este filtro lo heredará, y los tests de categoría de esta vista habrá que adaptarlos.
- **Sin detalle de producto, precio ni carrito:** la tarjeta solo enseña lo que se ve desde fuera.
- **Maquetación sin verificar en pantalla:** los mocks (MSW) no se pueden registrar en el navegador integrado de Claude Code, así que solo se ha probado con tests. Conviene que el equipo compruebe cómo se ven las dos columnas en pantallas estrechas, los nombres largos y las estrellas.
- **Valoración mínima fija:** 4, 3 y 2 estrellas. Si se quiere otro tramo, es la constante `RATING_OPTIONS` de `CatalogFilters.jsx`.

---

## 9. Uso de IA generativa

- **Herramienta:** Claude Code (Sonnet 5.5).
- **Qué hizo la IA:** escribió con TDD las dos tasks (los tests en rojo y después el código), la vista de productos, la tarjeta, las estrellas, los filtros, el contrato propuesto, los mocks, la documentación y la matriz de trazabilidad.
- **Qué hizo el equipo:** definió lo que se debe ver en la tarjeta y el diseño (dos tarjetas por fila), eligió que la vista fuese solo para clientes, que la valoración se filtrase con un mínimo, el contrato propuesto y las dos columnas en todas las pantallas. El código se revisa, ejecuta y valida por el equipo antes de integrarlo.
