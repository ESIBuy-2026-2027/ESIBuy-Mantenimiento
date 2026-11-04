# HU10 · Realización de pedido: pasarela de pago (Frontend)

Documento para entender la pasarela de pago: qué muestra, cómo se crean los pedidos, qué contrato espera del backend y cómo probarla.

- **Fecha:** 27 de octubre de 2026
- **HU:** HU10 · Realización de pedido (AB#72)
- **Task:** AB#401 (frontend: vista pasarela de pago)
- **Ampliación (28 de octubre de 2026):** task AB#133 (flujo de checkout: pago y confirmación), rama `feature/US10-T133-metodo-pago-y-ticket`. Añade la elección del método de pago (HU6) y el recibo con forma de ticket.
- **Ampliación (3 de noviembre de 2026):** task AB#133 (dirección de envío y endpoint real). La pasarela deja elegir la dirección de envío (HU5) y crea los pedidos con `POST /api/customer/orders` del backend (PR #166).
- **Rama:** `feature/US10-T401-pasarela-de-pago` (parte de la rama del carrito de la HU9, `feature/US9-T383-T384-carrito-de-compra`; ver `docs/hu9-carrito-de-compra.md`)
- **Metodología:** TDD (commit de rojo `test(front)` y de verde `feat(front)`)
- **Depende de:** `docs/hu9-carrito-de-compra.md` (el carrito del que sale la compra)
- **Backend:** módulo `pedidos` de las tasks AB#380 a AB#382 (PR #166). La sección 5 recoge su contrato.

> La task AB#379 (vista de carrito) de la HU10 es la misma pantalla que la AB#384 de la HU9 y está en `CartWidget`.

---

## 1. Resumen

Al pulsar «Proceder al pago» en el carrito, el cliente llega a `/checkout`: la pasarela resume la compra **separada por proveedor** y, al pulsar **Simular pago**, se hace **un pedido por cada proveedor**. Cada pedido tiene un solo proveedor.

| Qué se pidió | Cómo se cumple |
|---|---|
| «Proceder al pago» lleva a una pasarela | Enlace del pie del carrito a `/checkout` (solo clientes) |
| Un pedido por proveedor | `CheckoutPage` crea los pedidos **uno detrás de otro**, cada uno con `POST /api/customer/orders` y solo el identificador de un proveedor |
| Cada pedido solo puede tener un proveedor | El cuerpo de cada petición lleva un solo `vendedorId`; el servidor toma de su carrito solo lo de ese proveedor |
| Botón para simular el pago | **Simular pago**: muestra «Procesando pago…» un instante (700 ms) y luego crea los pedidos. No hay ningún cobro real |
| Cabecera por proveedor | `TecnoVera: 197,32 €`, igual que en el carrito |
| Elegir el método de pago (AB#133) | «Método de pago» en el resumen: los métodos guardados del cliente (HU6) con su máscara; sale marcado el predeterminado. Sin ninguno no se puede pagar y se lleva a `/payment-methods` (HU6) |
| Elegir la dirección de envío (AB#133) | «Dirección de envío» en el resumen: las direcciones del cliente (HU5); sale marcada la principal. Sin ninguna no se puede pagar y se lleva a `/addresses` (HU5) |
| Recibo con forma de ticket (AB#133) | `PaymentTicket`: total pagado, fecha, una línea por pedido, el método usado con su máscara, código de barras decorativo y confeti breve |

---

## 2. Flujo

```mermaid
flowchart TD
    A[Proceder al pago] --> B[/checkout]
    B --> C{¿Sesión de cliente?}
    C -- Sin sesión --> L[Redirige a /login]
    C -- Otro rol --> R[Redirige al destino de su rol]
    C -- Cliente --> D{¿Carrito vacío?}
    F -.-> P[GET /api/customer/payment-methods: se marca el predeterminado]
    F -.-> Q[GET /api/customer/addresses: se marca la principal]
    D -- Sí --> E[Tu carrito está vacío + Ver los productos]
    D -- No --> F[Un pedido por proveedor y resumen con el total]
    F -- Simular pago --> G[Procesando pago…]
    G --> H[GET /api/customer/cart: los proveedores que hay ahora]
    H --> I[POST /api/customer/orders por cada proveedor, uno detrás de otro]
    I --> J{¿Alguno ha fallado?}
    J -- No --> K[Pago realizado: recibo con un pedido por proveedor]
    J -- Sí --> M[Pedidos creados + aviso de cuáles han fallado + Reintentar el pago]
    M -- Reintentar --> G
```

---

## 3. Componentes y archivos

| Archivo | Qué hace |
|---|---|
| `pages/CheckoutPage.jsx` | La pasarela. Fase de revisión (un pedido por proveedor y resumen), pago en curso y resultado. Pide el carrito al servidor al empezar a pagar, para no depender de una copia vieja |
| `features/checkout/PaymentTicket.jsx` | El recibo con forma de ticket (AB#133) |
| `api/paymentMethods.js` y `features/paymentMethods/tiposDeMetodo.js` | De la vista de métodos de pago de la HU6 (PR #151): `getPaymentMethods()` y el nombre e icono de cada tipo |
| `mocks/paymentMethodsHandlers.js` | De la HU6. La pasarela añade `metodosDePagoDe`, con el que el mock de pedidos comprueba que el método es del cliente |
| `api/addresses.js` | De la vista de direcciones de la HU5: `getAddresses()` |
| `mocks/addressesHandlers.js` | De la HU5. La pasarela añade `direccionesDe`, con el que el mock de pedidos comprueba que la dirección es del cliente (AB#133) |
| `api/orders.js` | `createOrder(vendorId, direccionId, paymentMethodId)` |
| `mocks/ordersHandlers.js` | `POST /api/customer/orders` simulado: comprueba que la dirección y el método de pago son del cliente (si no, 400), toma del carrito del cliente lo de ese proveedor, lo quita del carrito, copia la dirección en el pedido y responde con el pedido (201). 409 `CART_EMPTY` si no hay nada de ese proveedor |
| `mocks/cartHandlers.js` | `retirarGrupoDelCarrito`: lo que usa el mock de pedidos |
| `routes/AppRoutes.jsx` y `routes/paths.js` | Ruta `CHECKOUT` (`/checkout`) solo para clientes |

---

## 4. Estados de la pantalla

| Estado | Cuándo | Qué se ve |
|---|---|---|
| Cargando / error | Mientras llega el carrito o si falla | Indicador o error con **Reintentar** |
| Carrito vacío | Sin productos | «Tu carrito está vacío» y **Ver los productos**; no hay botón de pago |
| Revisión | Hay productos | Un pedido por proveedor y el resumen del pago con el total, la **dirección de envío** y el **método de pago** |
| Sin direcciones de envío | El cliente no tiene ninguna guardada | Aviso con **Añadir una dirección** (lleva a `/addresses`); **Simular pago** desactivado |
| Error de direcciones | Falla `GET /api/customer/addresses` | Error con **Reintentar**; **Simular pago** desactivado |
| Sin métodos de pago | El cliente no tiene ninguno guardado | Aviso con **Añadir un método de pago** (lleva a `/payment-methods`); **Simular pago** desactivado |
| Error de métodos de pago | Falla `GET /api/customer/payment-methods` | Error con **Reintentar**; **Simular pago** desactivado |
| Procesando | Tras pulsar **Simular pago** | El botón pasa a «Procesando pago…» y se desactiva |
| Pago realizado | Se ha creado algún pedido | El recibo con forma de ticket (total de los pedidos creados, fecha, pedidos y método de pago) y **Seguir comprando** |
| Fallo parcial | Algún pedido ha fallado | Los pedidos creados, un aviso con el proveedor de cada uno que ha fallado, **Reintentar el pago** (solo repite lo que falta) y **Seguir comprando** |
| Pago no completado | Han fallado todos | Aviso y **Reintentar el pago** |

---

## 5. Contrato del backend

Lo implementa el módulo `pedidos` del backend (AB#380 a AB#382, PR #166).

`POST /api/customer/orders` con `{ "vendedorId": "u-2", "direccionId": "d-1", "paymentMethodId": "m-1" }` (los tres obligatorios)

`direccionId` es una de las direcciones de envío del cliente (HU5): el servidor la copia en el `direccionEnvio` del pedido, así que el pedido no cambia aunque luego se edite o se borre la dirección. `paymentMethodId` es uno de los métodos de pago del cliente (HU6).

Crea **el pedido de ese proveedor** con los productos suyos que hay en el carrito del cliente en sesión y los quita del carrito. Responde 201:

```json
{
  "id": "o-1",
  "numeroPedido": "ESB-2026-000001",
  "vendedor": { "id": "u-2", "nombreComercial": "TecnoVera" },
  "total": 197.32,
  "estado": "PAGADO",
  "paymentMethodId": "m-1",
  "direccionEnvio": { "destinatario": "Carlos Cliente", "calle": "Calle Mayor 1", "detalles": null, "codigoPostal": "13071", "ciudad": "Ciudad Real", "provincia": "Ciudad Real", "telefono": "612345678" },
  "lineas": [
    { "productoId": "p-1", "nombre": "Auriculares inalámbricos", "cantidad": 2, "precioUnitario": 53.91, "subtotal": 107.82 }
  ],
  "createdAt": "2026-11-03T10:00:00Z"
}
```

| Estado | Significado |
|---|---|
| 400 `VALIDATION_ERROR` | Falta algún campo, o la dirección o el método de pago no son del cliente (no se dice cuál, para no revelar si existe) |
| 401 / 403 | No es un cliente |
| 409 `CART_EMPTY` | No hay productos de ese proveedor en el carrito |
| 409 `INSUFFICIENT_STOCK` | Ya no hay existencias de alguno de los productos |

- **Cada pedido solo tiene un proveedor.** El frontend manda una petición por proveedor y nunca mezcla.
- **El servidor vuelve a comprobar** precios y existencias al crear el pedido, y recalcula los totales.
- **Cobro simulado:** `estado` llega como `PAGADO`; no se cobra nada. Cuando exista un cobro real, esta pantalla cambiará.
- Si un pedido falla, **no** debe quitar nada de ese proveedor del carrito, para que se pueda reintentar.

---

## 6. Datos de prueba y cómo verlo en el navegador

Con `VITE_USE_MOCKS=true` y `cliente@esibuy.test`:

1. Añadir al carrito productos de dos proveedores (por ejemplo, `Auriculares inalámbricos` y `Smartwatch deportivo`).
2. Abrir el carrito (botón de abajo a la izquierda) y pulsar **Proceder al pago**.
3. En la pasarela: un pedido por proveedor y el total; pulsar **Simular pago**.
4. Tras un instante: «Pago realizado» con un pedido por proveedor; el carrito queda vacío.
5. Abrir `/checkout` con el carrito vacío: «Tu carrito está vacío».
6. Antes de pagar, elegir **Calle Toledo 5** en «Dirección de envío»: en «Mis pedidos» los pedidos nuevos llevan esa dirección.
7. Antes de pagar, elegir **Bizum** en «Método de pago»: el recibo dice «Pagado con Bizum» y su máscara.

### Tests automáticos

| Archivo | Tests | Qué cubre |
|---|---|---|
| `pages/CheckoutPage.test.jsx` | 28 | Acceso por rol y desde el carrito; un pedido por proveedor con su total y sus productos; aviso de un pedido por proveedor; importe total; carrito vacío; «Procesando pago…»; pedidos confirmados y carrito vacío tras pagar; un solo proveedor por petición, en orden; fallo parcial (confirma lo creado, avisa y conserva la compra) y reintento sin repetir lo pagado; métodos de pago con el predeterminado marcado, elegir otro, sin métodos, error al cargarlos y `paymentMethodId` en cada pedido (AB#133); recibo tras pagar y con fallo parcial (AB#133); direcciones con la principal marcada, elegir otra, sin direcciones, error al cargarlas y `direccionId` en cada pedido (AB#133) |
| `features/checkout/PaymentTicket.test.jsx` (nuevo, AB#133) | 7 | Total, fecha, pedidos y método de pago del recibo; código de barras y confeti ocultos a los lectores de pantalla; sin confeti con movimiento reducido |

---

## 7. Decisiones y librerías

- **Se paga y luego se crean los pedidos** (decisión del equipo): «Simular pago» simula el cobro y después crea un pedido por proveedor. No hay un estado «pendiente de pago».
- **Un pedido por proveedor, creados uno detrás de otro,** para saber cuál ha fallado y no perder los que sí se han creado.
- **Fallo parcial:** se confirman los pedidos creados y se avisa de los que no; la compra de esos sigue en el carrito.
- **El pago es una simulación:** no se guarda nada de pago, ni tarjeta, ni importe cobrado.
- **Ticket adaptado, no instalado (AB#133):** la referencia `AnimatedTicket` (shadcn, Tailwind, TypeScript y framer-motion) se reproduce con Fluent UI v9 (`makeStyles`, `tokens`, `@fluentui/react-icons`) en JavaScript y siguiendo `DESIGN.md`: superficie blanca con `shadow4`, troqueles y líneas discontinuas, y el naranja como único acento.
- **Confeti breve (AB#133):** unos 1,2 s, solo con CSS y con tonos de la rampa naranja. Es más largo que los 150–200 ms de `DESIGN.md` porque se pidió así para celebrar el pago. No aparece con `prefers-reduced-motion`, lo que se comprueba con `matchMedia` y también en CSS.
- **Un recibo por pago**, aunque se creen varios pedidos (decisión del equipo). Al reintentar se paga con el mismo método y se envía a la misma dirección.
- **Sin librerías nuevas.**

---

## 8. Limitaciones y trabajo pendiente

- **Pasarela real:** no hay cobro; el pedido solo guarda qué método de pago se eligió.
- **El recibo no muestra la dirección de envío:** se ve en el detalle del pedido (HU11).
- **Pedidos no atómicos:** si el navegador se cierra a mitad de los pedidos, algunos pueden haberse creado y otros no; el carrito conserva lo que falta.
- **Verificación visual parcial:** con los mocks se han visto en el navegador la revisión con el selector de método y el recibo tras pagar; no el fallo parcial.

---

## 9. Uso de IA generativa

- **Herramienta:** Claude Code (Sonnet 5.5).
- **Qué hizo la IA:** escribió con TDD el ciclo, la pasarela, el contrato propuesto, los mocks, la documentación y la matriz.
- **AB#133 (Claude Code, Opus 5.5):** propuso el diseño y el mapa de tests (HU10-CP07 a CP11), que el equipo aprobó, y escribió con TDD el selector de método, el ticket, el contrato propuesto, los mocks, la documentación y la matriz. El equipo eligió la task, el enlace a la vista de métodos de pago (al principio al perfil, mientras no existía) y un recibo por pago, y dio el componente de referencia del ticket.
- **AB#133 (Claude Code, Opus 5.5):** detectó que la pasarela llamaba a una ruta que el backend no tiene y sin `direccionId`, propuso el diseño y los casos HU10-CP21 a CP23, que el equipo aprobó, y escribió con TDD el selector de dirección, el cambio de `createOrder`, el mock, la documentación y la matriz.
- **Qué hizo el equipo:** definió que «Proceder al pago» lleve a una pasarela que da la orden de realización del pedido por proveedor, que cada pedido tenga un solo proveedor y que haya un botón para simular el pago. El código se revisa, ejecuta y valida por el equipo antes de integrarlo.
