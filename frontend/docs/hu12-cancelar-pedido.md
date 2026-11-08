# HU12 · Cancelación de pedido (Frontend)

Documento para entender cómo cancela el cliente un pedido desde el frontend y qué contrato sigue con el backend.

- **Fecha:** 5 de noviembre de 2026
- **HU:** HU12 · Cancelación de pedido (AB#74)
- **Tasks:** AB#372 (botón de cancelar pedido) y AB#373 (confirmación de la cancelación)
- **Rama:** `feature/US12-T372-T373-cancelar-pedido-front`
- **Metodología:** TDD, un ciclo por task; los commits `test(front-HU12): ciclo rojo` van antes que los `feat(front-HU12): ciclo verde`
- **Depende de:** la lógica del backend (T375, AB#375, PR #200), su endpoint (T374, AB#374, PR #207) y el detalle del pedido de la HU11 (`hu11-historial-pedidos.md`)

---

## 1. Resumen

Criterios de aceptación de la HU12: *«solo permite cancelar si el pedido no ha sido enviado»* y *«el stock se repone al cancelar»*. Reponer el stock lo hace el backend.

| Paso | Qué pasa |
|---|---|
| El cliente abre el detalle de un pedido `PAGADO` o `PREPARANDO` | Junto al estado aparece el botón **«Cancelar pedido»**. En los pedidos enviados, entregados o ya cancelados no aparece. |
| Pulsa «Cancelar pedido» | Se abre una confirmación: «¿Seguro que quieres cancelar el pedido ESB-…? No se puede deshacer.» Todavía no se cancela nada. |
| Pulsa **«Volver»** | Se cierra sin cancelar. |
| Pulsa **«Sí, cancelar el pedido»** | Se cancela. El detalle muestra el pedido que devuelve el backend: estado «Cancelado», el aviso de pedido cancelado en lugar de la línea de estados y el mensaje **«Pedido cancelado.»**. El botón desaparece. |
| El backend responde 409 (el pedido se acaba de enviar) | Se cierra la confirmación, se avisa **«Este pedido ya no se puede cancelar: ya se ha enviado.»** y se recarga el pedido para mostrar su estado real. |
| Falla por otro motivo | El aviso «No se ha podido cancelar el pedido. Inténtalo de nuevo.» aparece dentro de la confirmación, que sigue abierta para reintentar. |

Decisiones:

- **El botón solo está en el detalle**, no en la lista de Mis pedidos.
- **La regla de qué se puede cancelar** (`esCancelable` en `features/orders/orderStatus.js`) es la misma que la del backend. El backend la vuelve a comprobar siempre: el botón solo evita ofrecer algo que no se puede hacer.
- **Tras cancelar no se vuelve a pedir el pedido:** se usa el que devuelve el backend.

---

## 2. Contrato con el backend (T374, PR #207)

```
POST /api/customer/orders/{id}/cancel   (sin cuerpo, con el token CSRF)
→ 200 pedido (con estado CANCELADO)
→ 409 { "code": "ORDER_NOT_CANCELLABLE" }   si ya se ha enviado, entregado o cancelado
→ 400 { "code": "ORDER_NOT_FOUND" }         si no existe o es de otro cliente
```

En los mocks (`ordersHandlers.js`), el endpoint sigue la misma regla. El pedido `o-201` de Nora (`nuevo@esibuy.test`) está `PAGADO` y se puede cancelar. Los de Carlos están enviado, entregado y cancelado, así que no.

---

## 3. Archivos

| Archivo | Cambio |
|---|---|
| `src/features/orders/CancelOrderDialog.jsx` | Componente nuevo: la confirmación. |
| `src/pages/OrderDetailPage.jsx` | Botón «Cancelar pedido», la confirmación y los avisos del resultado. |
| `src/features/orders/orderStatus.js` | `esCancelable(estado)`. |
| `src/api/orders.js` | `cancelOrder(id)`. |
| `src/mocks/ordersHandlers.js` | El endpoint de cancelación. |

---

## 4. Tests

Los casos siguen la numeración del backend (HU12-CP01 a CP12).

| Caso | Task | Qué se prueba | Archivo |
|---|---|---|---|
| HU12-CP13 | AB#372 | El botón aparece en un pedido pagado y en uno en preparación, y no en los enviados, entregados o cancelados (3 tests) | `OrderDetailPage.test.jsx` |
| HU12-CP14 | AB#372 | Al pulsarlo pide confirmación y todavía no cancela | `OrderDetailPage.test.jsx` |
| HU12-CP15 | AB#373 | La confirmación nombra el pedido y avisa de que no se puede deshacer; «Volver» cierra sin cancelar (2 tests) | `CancelOrderDialog.test.jsx` |
| HU12-CP16 | AB#373 | Al confirmar se cancela y se entrega el pedido cancelado; el detalle lo muestra con su aviso (2 tests) | `CancelOrderDialog.test.jsx` y `OrderDetailPage.test.jsx` |
| HU12-CP17 | AB#373 | Si ya no se puede cancelar (409), no se da por cancelado y el detalle lo avisa (2 tests) | `CancelOrderDialog.test.jsx` y `OrderDetailPage.test.jsx` |
| HU12-CP18 | AB#373 | Otro error se avisa dentro de la confirmación y se puede reintentar | `CancelOrderDialog.test.jsx` |

Sin rojo real:
- en el ciclo 1, «un pedido enviado, entregado o ya cancelado no tiene el botón», porque el botón aún no existía;
- en el ciclo 2, los dos de HU12-CP15, porque el texto y «Volver» de la confirmación quedaron hechos en el ciclo 1.
