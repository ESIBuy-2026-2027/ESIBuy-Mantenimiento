# HU9 · Gestión del carrito de compra (Frontend)

Documento para entender cómo se añade un producto al carrito y cómo se ve el carrito: qué muestra, qué hace cada componente, qué contrato espera del backend y cómo probarlo.

- **Fecha:** 27 de octubre de 2026
- **HU:** HU9 · Gestión del carrito de compra (AB#71)
- **Tasks:** AB#383 (frontend: vista añadir al carrito) y AB#384 (frontend: vista de carrito)
- **Rama:** `feature/US9-T383-T384-carrito-de-compra` (el botón «Proceder al pago» y la pasarela de pago van en la rama de la HU10, `feature/US10-T401-pasarela-de-pago`; ver `docs/hu10-pasarela-de-pago.md`)
- **Metodología:** TDD. Cada ciclo tiene su commit de rojo (`test(front)`) y su commit de verde (`feat(front)`)
- **Depende de:** `docs/hu7-busqueda-y-filtrado-de-productos.md` (las tarjetas), `docs/hu8-detalle-producto.md` (el detalle) y `docs/mocks-msw.md` (mocks)
- **Backend:** las tasks AB#385 a AB#390 (entidad del carrito, endpoints de añadir y eliminar, lógica de añadir y eliminar productos y limpieza de carritos) están sin hacer. El contrato de la sección 5 lo **propone el frontend**.

---

## 1. Resumen

El cliente ve el **precio** de cada producto en el listado, puede **añadirlo al carrito** desde el listado y desde el detalle, y tiene su carrito siempre a la vista, **abajo a la izquierda**, con los productos **separados por proveedor** y el total de cada proveedor.

| Qué se pidió | Cómo se cumple |
|---|---|
| El precio a la vista desde fuera | Las tarjetas del listado, de la banda animada y de la lista muestran el precio final (con el original tachado y el porcentaje si hay descuento). Lo calcula el servidor para ese cliente (`PriceBlock`) |
| Botón para añadir al carrito, desde la lista y desde dentro | `AddToCartButton` en cada tarjeta (añade una unidad) y en el detalle (con selector de cantidad). Muestra «Añadiendo…», «Añadido» y avisa si no hay existencias |
| El carrito, abajo a la izquierda | `CartWidget`: un botón redondo flotante con el número de productos, que abre un panel |
| Productos separados por proveedor | El panel agrupa por proveedor (`role="group"`), cada grupo con su cabecera |
| Carrito con muchos productos | El panel tiene un scroll global y la lista de cada proveedor, su propio scroll (alto máximo de 236 px). Los proveedores no se comprimen, así que ningún producto queda oculto: con el cursor sobre una lista se mueve esa lista y, fuera de ellas, todo el panel |
| Cabecera por proveedor: «ProveedorX: 35,43 €» | `TecnoVera: 35,43 €`. El total de cada grupo lo calcula el servidor |
| Cálculos del precio por proveedor | Cada proveedor tiene su total; no hay un total único del carrito |
| Botón «Proceder al pago» | En el pie del panel, un enlace a la pasarela de pago (`/checkout`, HU10) |
| El carrito vive en el backend | Se pide con `GET /api/cart` al entrar y cada operación responde con el carrito completo. Al recargar la página, sigue ahí |

> Solo los clientes tienen carrito: el botón y el panel no aparecen para vendedores ni administradores. El backend debe responder 401 o 403 si quien consulta no es un cliente.

---

## 2. Flujo

```mermaid
flowchart TD
    A[Cliente inicia sesión] --> B[CartProvider pide GET /api/cart]
    B --> C[Botón flotante con el número de productos]
    D[Pulsa Añadir al carrito en una tarjeta o en el detalle] --> E[POST /api/cart/items]
    E -- 200 --> F[Carrito actualizado y botón: Añadido]
    E -- 409 INSUFFICIENT_STOCK --> G[No hay existencias suficientes]
    E -- Otro error --> H[No se ha podido añadir al carrito]
    C --> I[Abre el panel]
    I --> J[Productos agrupados por proveedor con su total]
    J -- Cambia la cantidad --> K[PUT /api/cart/items/id]
    J -- Quita un producto --> L[DELETE /api/cart/items/id]
    J -- Vacía el carrito --> M[DELETE /api/cart]
    J -- Proceder al pago --> N[/checkout]
```

---

## 3. Componentes y archivos

### Carrito (`features/cart/`)

| Archivo | Qué hace |
|---|---|
| `CartProvider.jsx` y `CartContext.js` | Guardan la última copia del carrito que ha enviado el servidor, ligada al cliente en sesión (si cambia la sesión, no se enseña el del anterior). Exponen `add`, `setQuantity`, `remove`, `clear` y `reload`. Cada operación aplica la respuesta del servidor. Está dentro de `AppLayout` |
| `useCart.js` | El hook para usarlo; falla si no hay `CartProvider` |
| `AddToCartButton.jsx` | El botón de añadir. Estados: «Añadir al carrito», «Añadiendo…» (desactivado), «Añadido» (2 s) y error (`role="alert"`). Un producto agotado o no disponible no se puede añadir |
| `QuantityStepper.jsx` | Selector de cantidad con menos y más, entre 1 y las existencias. Lo usan el detalle y el panel |
| `CartWidget.jsx` | El botón flotante y el panel: lista por proveedor, cantidades, quitar, vaciar, estados de carga y error y «Proceder al pago». El panel se cierra con Cerrar, con Escape y al cambiar de página. Al abrirlo, el foco pasa a él |

### Cambios en el catálogo

| Archivo | Qué cambia |
|---|---|
| `features/catalog/ProductCard.jsx` | La tarjeta ya no es un único enlace: es un contenedor con el enlace al detalle (imagen, categoría, nombre y valoración) y, debajo, el precio y el botón de añadir (un botón no puede ir dentro de un enlace) |
| `features/catalog/PriceBlock.jsx` | Versión `compact` para las tarjetas |
| `features/catalog/ProductDetail.jsx` | Selector de cantidad y botón de añadir en el panel de precio |
| `components/AppLayout.jsx` | Envuelve las páginas con `CartProvider` y pinta `CartWidget` |
| `utils/propTypes.js` | `catalogProductPropType` ahora lleva el proveedor, el precio y las existencias |

### API y mocks

| Archivo | Qué hace |
|---|---|
| `api/cart.js` | `getCart`, `addCartItem`, `setCartItemQuantity`, `removeCartItem` y `clearCart` |
| `mocks/cartHandlers.js` | El carrito simulado de cada cliente: agrupa por proveedor, calcula los totales, valida cantidades y existencias y no se pierde al recargar la página |
| `mocks/productsHandlers.js` | `datosComerciales`: lo comercial del producto (proveedor, precio, descuento y existencias), compartido por el listado, el detalle y el carrito |
| `test/cart.jsx` | `conCarrito`: un carrito de mentira para probar componentes sueltos |

---

## 4. Estados

| Estado | Cuándo | Qué se ve |
|---|---|---|
| Sin carrito | El usuario no es cliente | No hay botón |
| Cargando | Mientras llega el carrito | «Cargando carrito…» en el panel |
| Error | Falla `GET /api/cart` | «No se ha podido cargar el carrito» con **Reintentar** |
| Vacío | Sin productos | «Tu carrito está vacío»; sin «Vaciar» ni «Proceder al pago» |
| Con productos | Al menos uno | Un grupo por proveedor, con su total |
| Operación en curso | Cambiar cantidad, quitar o vaciar | Los controles se desactivan hasta que responde el servidor |
| Error al modificar | El servidor rechaza la operación | Aviso en el panel (sin existencias suficientes, u otro) |

---

## 5. Contrato esperado del backend

La ruta y el formato de respuesta los ha definido el frontend; **hay que acordarlos con quien desarrolle el backend** (AB#385 a AB#390).

### Catálogo (ampliación del contrato de la HU7)

`GET /api/products` devuelve ahora, en cada producto del listado, lo necesario para el precio y para añadirlo: `vendedor` (`{ id, nombreComercial }`), `precio`, `descuento` (`{ porcentaje }` o `null`), `precioFinal`, `stock` y `disponible`. El precio y el descuento son los del cliente que consulta.

### Carrito

El carrito es del cliente en sesión. **Todas las operaciones responden con el carrito completo**, ya agrupado por proveedor y con los totales calculados por el servidor:

```json
{
  "grupos": [
    {
      "vendedor": { "id": "u-2", "nombreComercial": "TecnoVera" },
      "total": 35.43,
      "lineas": [
        { "productoId": "p-1", "nombre": "Auriculares inalámbricos", "imagen": "https://tienda.test/a.png", "cantidad": 2, "precioUnitario": 12.5, "subtotal": 25.0, "stock": 34 }
      ]
    }
  ],
  "totalProductos": 3,
  "total": 45.43
}
```

| Operación | Ruta | Cuerpo | Notas |
|---|---|---|---|
| Ver | `GET /api/cart` | | Carrito vacío (`grupos: []`) si no hay o se ha limpiado |
| Añadir | `POST /api/cart/items` | `{ "productoId", "cantidad" }` | **Suma** `cantidad` a lo que hubiera. 404 si el producto no existe o no está disponible; 409 con `code: INSUFFICIENT_STOCK` si no hay existencias; 400 si la cantidad no es un entero de 1 o más |
| Cambiar la cantidad | `PUT /api/cart/items/{productoId}` | `{ "cantidad" }` | **Fija** la cantidad. Mismos errores. **No está en las tasks del backend**: sin ella no se puede bajar la cantidad; confirmar si se añade |
| Quitar un producto | `DELETE /api/cart/items/{productoId}` | | |
| Vaciar | `DELETE /api/cart` | | |

- Los precios (`precioUnitario`, `subtotal`, `total`) los calcula el servidor con el descuento del cliente (Estándar o Premium); el frontend solo suma lo que llega.
- El servidor **vuelve a comprobar** precios y existencias cuando se crea el pedido (HU10), así que un precio antiguo en el carrito no se cobra.
- Los carritos antiguos se limpian en el servidor (AB#390): el frontend no hace nada especial, solo recibe un carrito vacío.
- La respuesta con 401 o 403 hace que el panel muestre el error de carga.

---

## 6. Datos de prueba y cómo verlo en el navegador

Con `VITE_USE_MOCKS=true` (`frontend/.env.development`), `npm run dev` y `cliente@esibuy.test` (contraseña `Mock-Clave-1!`, códigos `654321` y `123456`):

1. En `/products`, cada tarjeta enseña su precio (`Auriculares inalámbricos`, con descuento, 53,91 € y 59,90 € tachado).
2. Pulsar **Añadir al carrito** en varias tarjetas, incluida alguna de otro proveedor (por ejemplo `Smartwatch deportivo`, de «Otero Hogar y Tecnología»): el botón redondo de abajo a la izquierda cuenta los productos.
3. Abrir el carrito: aparecen separados por proveedor, con «TecnoVera: …» y «Otero Hogar y Tecnología: …».
4. Cambiar cantidades, quitar un producto y vaciar el carrito.
5. En el detalle de un producto, elegir la cantidad y añadir; un producto agotado (`/products/p-3`) no se puede añadir.
6. Recargar la página: el carrito sigue.
7. Con `vendedor@esibuy.test` no hay botón de carrito.

### Tests automáticos

```bash
npx vitest run
npm run lint
npm run build
```

| Archivo | Tests | Qué cubre |
|---|---|---|
| `features/cart/AddToCartButton.test.jsx` (nuevo) | 8 | Añade una unidad o la cantidad indicada; «Añadiendo…» y «Añadido»; sin existencias; otro fallo; el aviso se quita al reintentar; agotado y no disponible |
| `features/cart/CartWidget.test.jsx` (nuevo) | 25 | Botón con el número de productos (singular, vacío); solo clientes; panel agrupado por proveedor con «Proveedor: total»; cantidades, subtotales y enlaces; cerrar con Cerrar y Escape; carrito vacío; «Proceder al pago»; cantidad, existencias, quitar y vaciar; carrito con muchos productos (scroll global y por proveedor); errores; carga y reintento; con el carrito del servidor (añadir desde el listado y recargar la página) |
| `features/catalog/ProductCard.test.jsx` | 4 nuevos | Precio, descuento, botón de añadir y que el botón no está dentro del enlace |
| `pages/ProductsPage.test.jsx` | 3 nuevos | Precio con descuento en las tarjetas, añadir desde una tarjeta y aviso de existencias |
| `pages/ProductDetailPage.test.jsx` | 5 nuevos | Añadir desde el detalle, elegir la cantidad, mínimo y máximo, y agotado |

---

## 7. Decisiones y librerías

- **El carrito vive en el backend** (decisión del equipo), no en el navegador. El frontend solo guarda la última copia que envía el servidor.
- **Totales por proveedor, sin total único** (decisión del equipo): cada proveedor es un pedido distinto. Solo la pasarela muestra el importe total a pagar.
- **El servidor calcula los precios.** El frontend no multiplica ni aplica descuentos.
- **Un botón dentro de un enlace no es válido**, así que la tarjeta se divide en el enlace al detalle y, aparte, el precio y el botón.
- **Cambiar la cantidad exige `PUT`**, que no está en las tasks del backend; se propone y se marca para confirmar.
- **Sin librerías nuevas:** todo es Fluent UI y los iconos que ya usa el proyecto.

---

## 8. Limitaciones y trabajo pendiente

- **Backend:** falta todo el carrito (AB#385 a AB#390) y el cálculo de precios por tipo de cliente.
- **`PUT` para fijar la cantidad:** no está en las tasks; hay que confirmarlo.
- **Avisos de cambios de precio o de existencias:** si el precio o el stock cambian entre que se añade un producto y se paga, el frontend no avisa; el servidor lo comprueba al crear el pedido.
- **Verificación visual parcial:** el panel del carrito, las tarjetas con precio y la pasarela se vieron en el navegador con datos fijos, pero no la aplicación completa con los mocks (MSW no se registra en el navegador integrado de Claude Code). Conviene que el equipo compruebe el conjunto.
- **Móvil:** el botón y el panel se adaptan al ancho, pero no se han probado en un teléfono real.

---

## 9. Uso de IA generativa

- **Herramienta:** Claude Code (Sonnet 5.5).
- **Qué hizo la IA:** escribió con TDD los dos ciclos, el estado del carrito, el botón de añadir, el selector de cantidad, el panel agrupado por proveedor, el cambio de las tarjetas, el contrato propuesto, los mocks, la documentación y la matriz de trazabilidad.
- **Qué hizo el equipo:** definió el comportamiento (precio visible, botón en el listado y en el detalle, carrito abajo a la izquierda separado por proveedor con la cabecera «Proveedor: total») y decidió que el carrito viva en el backend. El código se revisa, ejecuta y valida por el equipo antes de integrarlo.
