# HU6 · Gestión de métodos de pago (Frontend)

Documento para entender la vista «Métodos de pago»: quién la ve, qué muestra, qué contrato usa del backend y cómo probarla.

- **Fecha:** 28 de octubre de 2026
- **HU:** HU6 · Gestión de métodos de pago
- **Tasks:** AB#269 · Frontend: vista de métodos de pago, AB#113 · Frontend: gestión de métodos de pago y AB#114 · Frontend: tests de métodos de pago
- **Rama:** `feature/US6-T269-T113-T114-frontend-metodos-pago`
- **Metodología:** TDD, un test cada vez, con un commit por ciclo rojo y verde
- **Depende de:** el backend de la HU6 (AB#271, AB#110 y AB#270, PR #148) y `docs/mocks-msw.md` (mocks)

---

## 1. Resumen

El cliente gestiona sus métodos de pago (tarjeta, PayPal o Bizum) en `/payment-methods`. Llega desde el enlace **Métodos de pago** de la cabecera.

| Criterio de aceptación | Cómo se cumple |
|---|---|
| Los datos de pago no se almacenan en claro | El frontend solo recibe y muestra la máscara (`•••• 1111`, `paypal.me`, `•••• 678`). Los datos se envían una vez al dar de alta y no se guardan en ningún sitio del navegador: se borran al cancelar. El CVV viaja para que lo valide el backend, que no lo guarda. |
| Permite añadir, eliminar y marcar un método como predeterminado | Panel de alta, botón «Eliminar» con confirmación y botón «Marcar como predeterminado» en cada método. |
| Solo el cliente | La ruta solo admite el rol `CLIENTE` y el enlace solo le aparece a él. El backend responde 403 a los demás. |

---

## 2. Qué muestra

- **Cabecera:** título «Métodos de pago», subtítulo y el botón **Añadir método de pago**. Con 5 métodos, el máximo, el botón se desactiva y un texto lo explica.
- **Lista:** una tarjeta por método con el icono de su tipo, el nombre del tipo, la máscara y:
  - la etiqueta **Predeterminado** en el que lo es;
  - **Marcar como predeterminado** en los demás;
  - **Eliminar** en todos.
- **Estado vacío:** «Aún no tienes métodos de pago», con el botón para añadir el primero.
- **Panel de alta** (inspirado en la tarjeta «Payment Details», adaptado a Fluent UI y a `DESIGN.md`):
  - superficie blanca con la sombra del panel protagonista, sin efecto cristal;
  - tres botones para elegir el tipo (**Tarjeta**, **PayPal** y **Bizum**);
  - campos con icono: número de tarjeta, caducidad y CVV en dos columnas, y titular; o la URL de PayPal; o el teléfono de Bizum;
  - **Guardar método de pago** a lo ancho, **Cancelar** y el pie «Tus datos de pago se guardan cifrados. El CVV no se guarda»;
  - entra con una animación corta que no se reproduce con movimiento reducido.
- **Diálogo de eliminar:** pide confirmación. Si el método es el predeterminado y quedan otros, hay que elegir el sustituto en **Nuevo método predeterminado** antes de poder eliminar.
- **Avisos:** «Método de pago añadido.», «Método de pago eliminado.», el método que ya no existe y los errores de carga, con **Reintentar**.

### Validación en el navegador

Solo formatos básicos, con los mismos límites que el backend, para avisar antes de enviar. Lo demás (Luhn, que la tarjeta no esté caducada) lo decide el backend.

| Campo | Regla |
|---|---|
| Número de tarjeta | De 13 a 19 dígitos. Se puede escribir con espacios y se envía sin ellos. |
| Caducidad | `MM/AA`, con el mes entre 01 y 12 |
| CVV | 3 o 4 dígitos |
| Titular | Hasta 26 letras, espacios, guiones y apóstrofos |
| URL de PayPal | Una URL con esquema y servidor |
| Teléfono de Bizum | 9 dígitos |

Los mensajes de error nunca repiten los datos enviados ni el texto del backend.

---

## 3. Componentes y archivos

| Archivo | Cambio |
|---|---|
| `src/pages/PaymentMethodsPage.jsx` | Página nueva: lista, predeterminado, límite, estado vacío, avisos y conexión con el panel y el diálogo. |
| `src/features/paymentMethods/AddPaymentMethodPanel.jsx` | Panel de alta con el selector de tipo, la validación y el envío. |
| `src/features/paymentMethods/DeletePaymentMethodDialog.jsx` | Confirmación de eliminar, con la elección del sustituto del predeterminado. |
| `src/features/paymentMethods/tiposDeMetodo.js` | Nombre e icono de cada tipo, compartidos por la página y el diálogo. |
| `src/api/paymentMethods.js` | `getPaymentMethods`, `addPaymentMethod`, `setDefaultPaymentMethod` y `deletePaymentMethod`. |
| `src/routes/paths.js` y `AppRoutes.jsx` | Ruta `/payment-methods` (`PATHS.PAYMENT_METHODS`), protegida para `CLIENTE`. |
| `src/components/AppHeader.jsx` | Enlace **Métodos de pago** para el cliente. |
| `src/mocks/paymentMethodsHandlers.js`, `handlers.js` y `test/setup.js` | Mock de los cuatro endpoints, que se reinicia tras cada test. |

---

## 4. Contrato con el backend (PR #148, ya implementado)

Todos sobre `/api/customer/payment-methods`, solo para `CLIENTE` y con el cliente sacado de la sesión. Sin sesión, `401`; con otro rol, `403`.

| Petición | Respuesta |
|---|---|
| `GET` | `200` con `[{ id, tipo, mascara, predeterminado }]`. Nunca llegan los datos de pago. |
| `POST` con `{ tipo, ... }` | `201` con el método creado. Tarjeta: `numeroTarjeta`, `titular`, `caducidad` y `cvv`; PayPal: `urlPayPal`; Bizum: `telefonoBizum`. |
| `PUT /{id}/default` | `204` |
| `DELETE /{id}?nuevoPredeterminado={id}` | `204`. El parámetro solo hace falta si se elimina el predeterminado y quedan otros. |

| Código de error | HTTP | Mensaje en la vista |
|---|---|---|
| `VALIDATION_ERROR` | 400 | «No se ha podido guardar el método de pago. Revisa los datos: puede que la tarjeta esté caducada.» |
| `PAYMENT_METHOD_LIMIT_REACHED` | 409 | «Ya tienes 5 métodos de pago. Elimina uno para añadir otro.» |
| `PAYMENT_METHOD_NOT_FOUND` | 404 | «Ese método de pago ya no existe. Hemos actualizado la lista.» |
| `DEFAULT_PAYMENT_METHOD_REQUIRED` | 409 | Se evita en la vista: no deja eliminar el predeterminado sin elegir sustituto. |

---

## 5. Mocks

`paymentMethodsHandlers.js` responde como el backend: máximo 5 métodos, el primero queda predeterminado, la misma máscara por tipo, 404 si el método no es del cliente y 409 si se elimina el predeterminado sin un sustituto válido.

| Usuario | Métodos de pago |
|---|---|
| `cliente@esibuy.test` (Carlos) | Tarjeta `•••• 1111` (predeterminada) y Bizum `•••• 678` |
| El resto de clientes | Ninguno |

---

## 6. Tests

Los casos siguen la numeración del backend (HU6-CP01 a CP24). Son 32 tests.

| Caso | Qué se prueba | Archivo |
|---|---|---|
| HU6-CP25 | El cliente de la API: listar, añadir, marcar, eliminar con sustituto opcional y errores como `ApiError` (5 tests) | `api/paymentMethods.test.js` |
| HU6-CP26 | La lista con tipo y máscara, la etiqueta del predeterminado, el estado vacío y el error de carga con reintento (4 tests) | `PaymentMethodsPage.test.jsx` |
| HU6-CP27 | El enlace de la cabecera para el cliente; el vendedor y el administrador no acceden (3 tests) | `PaymentMethodsPage.test.jsx` |
| HU6-CP28 | Marcar otro como predeterminado; el método que ya no existe se avisa y se recarga (2 tests) | `PaymentMethodsPage.test.jsx` |
| HU6-CP29 | Con 5 métodos no se puede añadir otro; con menos, sí (2 tests) | `PaymentMethodsPage.test.jsx` |
| HU6-CP30 | El panel empieza con Tarjeta; PayPal y Bizum muestran solo su campo (2 tests) | `AddPaymentMethodPanel.test.jsx` |
| HU6-CP31 | Validación de formatos de tarjeta, PayPal y Bizum (2 tests) | `AddPaymentMethodPanel.test.jsx` |
| HU6-CP32 | Se envían solo los datos del tipo elegido; al cancelar se borran; desde la página, al guardar o cancelar se cierra el panel (4 tests) | `AddPaymentMethodPanel.test.jsx` y `PaymentMethodsPage.test.jsx` |
| HU6-CP33 | El rechazo del backend se avisa sin repetir los datos; el límite tiene su propio mensaje (2 tests) | `AddPaymentMethodPanel.test.jsx` |
| HU6-CP34 | Eliminar tras confirmar; cancelar no elimina (2 tests) | `DeletePaymentMethodDialog.test.jsx` |
| HU6-CP35 | El predeterminado exige elegir sustituto; el único se elimina sin elegir; desde la página, el sustituto pasa a predeterminado (3 tests) | `DeletePaymentMethodDialog.test.jsx` y `PaymentMethodsPage.test.jsx` |
| HU6-CP36 | Si la eliminación falla, se avisa sin cerrar el diálogo | `DeletePaymentMethodDialog.test.jsx` |

---

## 7. Pendiente

- **Errores de servidor al marcar el predeterminado:** hoy cualquier error muestra el mensaje de «ya no existe». Merece un mensaje propio, con su test.
- **Estado vacío y panel:** si no hay métodos y se abre el panel, el estado vacío sigue visible debajo.
- **Códigos de error:** confirmar con el equipo los códigos y los mensajes de la tabla del apartado 4.
- **Elegir el método al pagar:** es de la HU10, en `/checkout`, junto con el recibo.
