# HU11 · Historial y seguimiento de pedidos (Frontend)

Documento para entender cómo consulta el cliente sus pedidos en el frontend y qué contrato sigue con el backend.

- **Fecha:** 3 de noviembre de 2026
- **HU:** HU11 · Historial y seguimiento de pedidos (AB#73)
- **Task:** AB#376 (vista de pedidos)
- **Rama:** `feature/US11-T376-vista-pedidos`
- **Metodología:** TDD; el commit `test(front-HU11): ciclo rojo` va antes que el `feat(front-HU11): ciclo verde`
- **Depende de:** la lógica del backend (T378, AB#378, PR #182) y sus endpoints (T377, AB#377, PR #186)

---

## 1. Resumen

Criterio de aceptación de la HU11: *«el sistema muestra el detalle de los productos, desglose de precios y la máquina de estados de envío cuando el cliente selecciona un pedido concreto en su sección de pedidos»*.

| Pantalla | Qué muestra |
|---|---|
| **Mis pedidos** (`/orders`), con su enlace en la cabecera del cliente | Un pedido por fila, del más reciente al más antiguo: número, fecha, vendedor, total y estado. Pulsar el número abre el detalle. Si no hay pedidos, lo indica y ofrece ir a los productos. |
| **Detalle** (`/orders/:id`) | Número, fecha, vendedor y estado; la **línea de estados del envío**; los productos con cantidad, precio unitario y subtotal, y el total; la dirección de envío. |
| **Pantalla final del pago** (HU10) | Nuevo enlace **«Ver mis pedidos»** junto a «Seguir comprando». |

La línea de estados del envío sigue el orden del backend (`EstadoPedido`): **Pagado → Preparando → Enviado → Entregado**. Los pasos ya hechos se marcan con un tick, el actual se resalta (`aria-current="step"`) y los que faltan se ven atenuados.

Decisiones:

- **El detalle va en una página aparte:** tiene su propia dirección, el botón «atrás» funciona y es lo que pide el criterio («al seleccionar un pedido»).
- **Un pedido `CANCELADO`** muestra el aviso «Este pedido está cancelado: no se enviará» en lugar de la línea de estados, porque la cancelación no es un paso del envío. Cancelar (el botón y la confirmación) es de la HU12.
- **Un pedido que no existe o es de otro cliente** se ve igual: «Pedido no encontrado», con el enlace para volver a Mis pedidos.
- **Los textos y el orden de los estados** están en `features/orders/orderStatus.js`, que comparten las dos páginas. Si el backend añade un estado, basta con añadirlo ahí.

---

## 2. Contrato con el backend (T377, PR #186)

```
GET /api/customer/orders
→ 200 [ pedido, ... ]   (del más reciente al más antiguo; [] si no hay)

GET /api/customer/orders/{id}
→ 200 pedido | 400 { "code": "ORDER_NOT_FOUND" }

pedido = { id, numeroPedido, vendedor: { id, nombreComercial }, total, estado, paymentMethodId,
           direccionEnvio: { destinatario, calle, detalles, codigoPostal, ciudad, provincia, telefono },
           lineas: [{ productoId, nombre, cantidad, precioUnitario, subtotal }], createdAt }
```

Las dos rutas son solo del cliente (401 sin sesión, 403 con otro rol).

En los mocks (`ordersHandlers.js`), Carlos (`cliente@esibuy.test`) tiene tres pedidos, `ENVIADO`, `ENTREGADO` y `CANCELADO`, y Nora tiene uno que Carlos no puede ver. Los pedidos que se crean desde la pasarela de pago aparecen también en el historial.

---

## 3. Archivos

| Archivo | Cambio |
|---|---|
| `src/pages/OrdersPage.jsx` | Página nueva: el historial. |
| `src/pages/OrderDetailPage.jsx` | Página nueva: el detalle con la línea de estados del envío. |
| `src/features/orders/orderStatus.js` | Pasos del envío, textos y colores de cada estado. |
| `src/api/orders.js` | `getOrders()` y `getOrder(id)`. |
| `src/mocks/ordersHandlers.js` | Los dos GET, los pedidos de ejemplo y los creados en el pago. |
| `src/components/AppHeader.jsx` | Enlace «Mis pedidos» para el cliente. |
| `src/pages/CheckoutPage.jsx` | Enlace «Ver mis pedidos» tras pagar. |
| `src/routes/paths.js` y `AppRoutes.jsx` | Rutas `/orders` y `/orders/:id`, solo para el cliente. |

---

## 4. Tests

Los casos siguen la numeración del backend (HU11-CP01 a CP10).

| Caso | Qué se prueba | Archivo |
|---|---|---|
| HU11-CP11 | El historial, ordenado y con sus datos; sin pedidos; error con «Reintentar» (3 tests) | `OrdersPage.test.jsx` |
| HU11-CP12 | Pulsar un pedido abre su detalle | `OrdersPage.test.jsx` |
| HU11-CP13 | Sin sesión lleva al login; un vendedor va a su página (2 tests) | `OrdersPage.test.jsx` |
| HU11-CP14 | Número, fecha, vendedor y dirección; productos con cantidad, precio unitario, subtotal y total (2 tests) | `OrderDetailPage.test.jsx` |
| HU11-CP15 | Línea de estados de un pedido enviado y de uno entregado (2 tests) | `OrderDetailPage.test.jsx` |
| HU11-CP16 | Un pedido cancelado lo indica en lugar de la línea de estados | `OrderDetailPage.test.jsx` |
| HU11-CP17 | No encontrado (inexistente o ajeno); otro error con «Reintentar»; volver a Mis pedidos (3 tests) | `OrderDetailPage.test.jsx` |
| HU11-CP18 | El cliente tiene el enlace «Mis pedidos»; el vendedor y el administrador no (2 tests) | `AppHeader.test.jsx` |
| HU11-CP19 | Tras pagar se puede ir a Mis pedidos | `CheckoutPage.test.jsx` |

El test «el vendedor y el administrador no tienen el enlace a Mis pedidos» (HU11-CP18) pasó sin rojo real, porque el enlace aún no existía.

---

## 5. Pendiente

- ~~La pasarela de pago (HU10) aún no crea los pedidos contra el backend real~~: resuelto en la task AB#133. La pasarela usa `POST /api/customer/orders` con la `direccionId` elegida, y los pedidos nuevos llegan al historial con esa dirección.
