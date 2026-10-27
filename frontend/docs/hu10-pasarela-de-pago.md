# HU10 · Realización de pedido: pasarela de pago (Frontend)

Documento para entender la pasarela de pago: qué muestra, cómo se crean los pedidos, qué contrato espera del backend y cómo probarla.

- **Fecha:** 27 de octubre de 2026
- **HU:** HU10 · Realización de pedido (AB#72)
- **Task:** AB#401 (frontend: vista pasarela de pago)
- **Rama:** `feature/US10-T401-pasarela-de-pago` (parte de la rama del carrito de la HU9, `feature/US9-T383-T384-carrito-de-compra`; ver `docs/hu9-carrito-de-compra.md`)
- **Metodología:** TDD (commit de rojo `test(front)` y de verde `feat(front)`)
- **Depende de:** `docs/hu9-carrito-de-compra.md` (el carrito del que sale la compra)
- **Backend:** las tasks AB#380 a AB#382 (entidad pedido, endpoint de crear pedido y lógica de creación) están sin hacer. El contrato de la sección 5 lo **propone el frontend**.

> La task AB#379 (vista de carrito) de la HU10 es la misma pantalla que la AB#384 de la HU9 y está en `CartWidget`.

---

## 1. Resumen

Al pulsar «Proceder al pago» en el carrito, el cliente llega a `/checkout`: la pasarela resume la compra **separada por proveedor** y, al pulsar **Simular pago**, se hace **un pedido por cada proveedor**. Cada pedido tiene un solo proveedor.

| Qué se pidió | Cómo se cumple |
|---|---|
| «Proceder al pago» lleva a una pasarela | Enlace del pie del carrito a `/checkout` (solo clientes) |
| Un pedido por proveedor | `CheckoutPage` crea los pedidos **uno detrás de otro**, cada uno con `POST /api/orders` y solo el identificador de un proveedor |
| Cada pedido solo puede tener un proveedor | El cuerpo de cada petición lleva únicamente `{ vendedorId }`; el servidor toma de su carrito solo lo de ese proveedor |
| Botón para simular el pago | **Simular pago**: muestra «Procesando pago…» un instante (700 ms) y luego crea los pedidos. No hay ningún cobro real |
| Cabecera por proveedor | `TecnoVera: 197,32 €`, igual que en el carrito |

---

## 2. Flujo

```mermaid
flowchart TD
    A[Proceder al pago] --> B[/checkout]
    B --> C{¿Sesión de cliente?}
    C -- Sin sesión --> L[Redirige a /login]
    C -- Otro rol --> R[Redirige al destino de su rol]
    C -- Cliente --> D{¿Carrito vacío?}
    D -- Sí --> E[Tu carrito está vacío + Ver los productos]
    D -- No --> F[Un pedido por proveedor y resumen con el total]
    F -- Simular pago --> G[Procesando pago…]
    G --> H[GET /api/cart: los proveedores que hay ahora]
    H --> I[POST /api/orders por cada proveedor, uno detrás de otro]
    I --> J{¿Alguno ha fallado?}
    J -- No --> K[Pago realizado: un pedido por proveedor]
    J -- Sí --> M[Pedidos creados + aviso de cuáles han fallado + Reintentar el pago]
    M -- Reintentar --> G
```

---

## 3. Componentes y archivos

| Archivo | Qué hace |
|---|---|
| `pages/CheckoutPage.jsx` | La pasarela. Fase de revisión (un pedido por proveedor y resumen), pago en curso y resultado. Pide el carrito al servidor al empezar a pagar, para no depender de una copia vieja |
| `api/orders.js` | `createOrder(vendorId)` |
| `mocks/ordersHandlers.js` | `POST /api/orders` simulado: toma del carrito del cliente lo de ese proveedor, lo quita del carrito y responde con el pedido (201). 409 `CART_EMPTY` si no hay nada de ese proveedor |
| `mocks/cartHandlers.js` | `retirarGrupoDelCarrito`: lo que usa el mock de pedidos |
| `routes/AppRoutes.jsx` y `routes/paths.js` | Ruta `CHECKOUT` (`/checkout`) solo para clientes |

---

## 4. Estados de la pantalla

| Estado | Cuándo | Qué se ve |
|---|---|---|
| Cargando / error | Mientras llega el carrito o si falla | Indicador o error con **Reintentar** |
| Carrito vacío | Sin productos | «Tu carrito está vacío» y **Ver los productos**; no hay botón de pago |
| Revisión | Hay productos | Un pedido por proveedor y el resumen del pago con el total |
| Procesando | Tras pulsar **Simular pago** | El botón pasa a «Procesando pago…» y se desactiva |
| Pago realizado | Se ha creado algún pedido | Lista de pedidos (proveedor, número y total) y **Seguir comprando** |
| Fallo parcial | Algún pedido ha fallado | Los pedidos creados, un aviso con el proveedor de cada uno que ha fallado, **Reintentar el pago** (solo repite lo que falta) y **Seguir comprando** |
| Pago no completado | Han fallado todos | Aviso y **Reintentar el pago** |

---

## 5. Contrato esperado del backend

Hay que acordarlo con quien desarrolle el backend (AB#380 a AB#382).

`POST /api/orders` con `{ "vendedorId": "u-2" }`

Crea **el pedido de ese proveedor** con los productos suyos que hay en el carrito del cliente en sesión y los quita del carrito. Responde 201:

```json
{
  "id": "o-1",
  "vendedor": { "id": "u-2", "nombreComercial": "TecnoVera" },
  "total": 197.32,
  "estado": "PAGADO",
  "lineas": [
    { "productoId": "p-1", "nombre": "Auriculares inalámbricos", "cantidad": 2, "precioUnitario": 53.91, "subtotal": 107.82 }
  ]
}
```

| Estado | Significado |
|---|---|
| 400 | `vendedorId` ausente o inválido |
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

### Tests automáticos

| Archivo | Tests | Qué cubre |
|---|---|---|
| `pages/CheckoutPage.test.jsx` (nuevo) | 15 | Acceso por rol y desde el carrito; un pedido por proveedor con su total y sus productos; aviso de un pedido por proveedor; importe total; carrito vacío; «Procesando pago…»; pedidos confirmados y carrito vacío tras pagar; un solo proveedor por petición, en orden; fallo parcial (confirma lo creado, avisa y conserva la compra) y reintento sin repetir lo pagado |

---

## 7. Decisiones y librerías

- **Se paga y luego se crean los pedidos** (decisión del equipo): «Simular pago» simula el cobro y después crea un pedido por proveedor. No hay un estado «pendiente de pago».
- **Un pedido por proveedor, creados uno detrás de otro,** para saber cuál ha fallado y no perder los que sí se han creado.
- **Fallo parcial:** se confirman los pedidos creados y se avisa de los que no; la compra de esos sigue en el carrito.
- **El pago es una simulación:** no se guarda nada de pago, ni tarjeta, ni importe cobrado.
- **Sin librerías nuevas.**

---

## 8. Limitaciones y trabajo pendiente

- **Backend:** falta la entidad pedido, `POST /api/orders` y su lógica (AB#380 a AB#382).
- **Pasarela real:** no hay datos de pago ni cobro; cuando exista, habrá que añadir el formulario y tratar sus errores.
- **Sin historial de pedidos:** tras pagar solo se ve la confirmación; ver los pedidos es otra historia.
- **Pedidos no atómicos:** si el navegador se cierra a mitad de los pedidos, algunos pueden haberse creado y otros no; el carrito conserva lo que falta.
- **Verificación visual parcial:** la revisión de la pasarela se vio en el navegador con datos fijos; no el resultado ni el fallo parcial.

---

## 9. Uso de IA generativa

- **Herramienta:** Claude Code (Sonnet 5.5).
- **Qué hizo la IA:** escribió con TDD el ciclo, la pasarela, el contrato propuesto, los mocks, la documentación y la matriz.
- **Qué hizo el equipo:** definió que «Proceder al pago» lleve a una pasarela que da la orden de realización del pedido por proveedor, que cada pedido tenga un solo proveedor y que haya un botón para simular el pago. El código se revisa, ejecuta y valida por el equipo antes de integrarlo.
