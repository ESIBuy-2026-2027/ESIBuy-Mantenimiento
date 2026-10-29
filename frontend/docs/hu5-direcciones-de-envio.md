# HU5 · Gestión de direcciones de envío (Frontend)

Este documento explica la vista «Direcciones de envío»: quién la ve, qué muestra, qué contrato usa del backend y cómo se prueba.

- **Fecha:** 29 de octubre de 2026
- **HU:** HU5 · Gestión de direcciones de envío (AB#67)
- **Tasks:** AB#108 · Frontend: gestión de direcciones (listar, añadir, editar, eliminar) y AB#109 · Frontend: tests de gestión de direcciones
- **Rama:** `feature/US5-T108-T109-frontend-direcciones`
- **Metodología:** TDD. En cada pieza (API, panel, diálogo y página) se escribieron primero los tests y después el código.
- **Depende de:** el backend de la HU5 (AB#105, AB#106 y AB#107, PR #161) y `docs/mocks-msw.md` (mocks)

---

## 1. Resumen

El cliente gestiona sus direcciones de envío en `/addresses`. Llega desde el enlace **Direcciones** de la cabecera.

| Criterio de aceptación | Cómo se cumple |
|---|---|
| El sistema guarda los cambios del usuario en la sección de direcciones | Cada alta, edición, eliminación o cambio de principal se envía al backend. Después la lista se vuelve a pedir, así que muestra lo guardado. |
| El sistema establece como principal la dirección que marca el usuario, para el checkout | Hay un botón **Marcar como principal** en cada dirección y la etiqueta **Principal** en la que lo es. El backend desmarca la anterior. El checkout (HU10) elegirá la que llega con `principal: true`. |
| Solo el cliente | La ruta solo admite el rol `CLIENTE` y el enlace solo le aparece a él. El backend responde 403 a los demás roles. |

---

## 2. Qué muestra

- **Cabecera:** el título «Direcciones de envío», un subtítulo y el botón **Añadir dirección**. Con 5 direcciones, que es el máximo, el botón se desactiva y un texto lo explica.
- **Lista:** una tarjeta por dirección, con:
  - el destinatario;
  - la dirección en una línea («Calle Toledo 5, 2º B · 28005 Madrid (Madrid)»);
  - el teléfono;
  - la etiqueta **Principal** o el botón **Marcar como principal**;
  - los botones **Editar** y **Eliminar**.
- **Estado vacío:** «Aún no tienes direcciones de envío», con el botón para añadir la primera. Mientras el panel está abierto no se muestra.
- **Panel de dirección:** el mismo para añadir («Nueva dirección») y para editar («Editar dirección», con los datos ya cargados). Tiene la misma superficie que el panel de alta de métodos de pago (HU6).
  - Campos: destinatario, calle y número, piso y puerta (opcional), código postal, ciudad, provincia y teléfono.
  - Botones: **Guardar dirección** y **Cancelar**.
- **Diálogo de eliminar:** pide confirmación. Si la dirección es la principal y quedan otras, hay que elegir cuál la sustituye en **Nueva dirección principal** antes de poder eliminarla.
- **Avisos:** «Dirección añadida.», «Dirección guardada.», «Dirección eliminada.», la dirección que ya no existe y los errores de carga, con **Reintentar**.

### Validación en el navegador

Las reglas son las mismas que en el backend, para avisar antes de enviar. Antes de validar y enviar se quitan los espacios sobrantes. Unos detalles vacíos se envían como `null`.

| Campo | Regla |
|---|---|
| Destinatario, ciudad y provincia | Obligatorios, hasta 100 caracteres |
| Calle y número | Obligatorio, hasta 150 caracteres |
| Piso, puerta… | Opcional, hasta 100 caracteres |
| Código postal | 5 dígitos |
| Teléfono | 9 dígitos, sin prefijo |

Los mensajes de error nunca repiten los datos enviados ni el texto del backend.

---

## 3. Componentes y archivos

| Archivo | Cambio |
|---|---|
| `src/pages/AddressesPage.jsx` | Página nueva: lista, principal, límite, estado vacío, avisos y conexión con el panel y el diálogo. |
| `src/features/addresses/AddressFormPanel.jsx` | Panel de alta y edición, con la validación y el envío. |
| `src/features/addresses/DeleteAddressDialog.jsx` | Confirmación de eliminar, con la elección de la nueva principal. |
| `src/features/addresses/direccionPropType.js` | Forma de una dirección y su resumen en una línea, que comparten la página, el panel y el diálogo. |
| `src/api/addresses.js` | `getAddresses`, `addAddress`, `updateAddress`, `setPrincipalAddress` y `deleteAddress`. |
| `src/routes/paths.js` y `AppRoutes.jsx` | Ruta `/addresses` (`PATHS.ADDRESSES`), protegida para `CLIENTE`. |
| `src/components/AppHeader.jsx` | Enlace **Direcciones** para el cliente. |
| `src/mocks/addressesHandlers.js`, `handlers.js` y `test/setup.js` | Mock de los cinco endpoints, que se reinicia tras cada test. |

---

## 4. Contrato con el backend (PR #161)

Todos los endpoints van sobre `/api/customer/addresses`. Son solo para `CLIENTE` y el cliente sale de la sesión. Sin sesión responden `401`; con otro rol, `403`.

| Petición | Respuesta |
|---|---|
| `GET` | `200` con `[{ id, destinatario, calle, detalles, codigoPostal, ciudad, provincia, telefono, principal }]` |
| `POST` con `{ destinatario, calle, detalles, codigoPostal, ciudad, provincia, telefono }` | `201` con la dirección creada; la primera queda como principal |
| `PUT /{id}` con los mismos campos | `200` con la dirección editada; no cambia la principal |
| `PUT /{id}/principal` | `204` |
| `DELETE /{id}?nuevaPrincipal={id}` | `204`. El parámetro solo hace falta si se elimina la principal y quedan otras. |

| Código de error | HTTP | Mensaje en la vista |
|---|---|---|
| `VALIDATION_ERROR` | 400 | «No se ha podido guardar la dirección. Revisa los datos e inténtalo de nuevo.» |
| `ADDRESS_LIMIT_REACHED` | 409 | «Ya tienes 5 direcciones. Elimina una para añadir otra.» |
| `ADDRESS_NOT_FOUND` | 404 | Al editar: «Esa dirección ya no existe…». Al marcar la principal: «Esa dirección ya no existe. Hemos actualizado la lista.» |
| `PRINCIPAL_ADDRESS_REQUIRED` | 409 | No llega a darse: la vista no deja eliminar la principal sin elegir sustituta. |

---

## 5. Mocks

`addressesHandlers.js` responde como el backend:

- máximo 5 direcciones, y la primera queda como principal;
- editar no cambia la principal;
- 404 si la dirección no es del cliente;
- 409 si se elimina la principal sin una sustituta válida.

| Usuario | Direcciones |
|---|---|
| `cliente@esibuy.test` (Carlos) | Calle Mayor 1, 13071 Ciudad Real (principal) y Calle Toledo 5, 2º B, 28005 Madrid |
| El resto de clientes | Ninguna |

---

## 6. Tests

Los casos siguen la numeración del backend (HU5-CP01 a CP16). En total son 34 tests.

| Caso | Qué se prueba | Archivo |
|---|---|---|
| HU5-CP17 | El cliente de la API: listar, añadir, editar, marcar la principal, eliminar con sustituta opcional y los errores como `ApiError` (6 tests) | `api/addresses.test.js` |
| HU5-CP18 | La lista con los datos de cada dirección, la etiqueta de la principal, el estado vacío y el error de carga con reintento (4 tests) | `AddressesPage.test.jsx` |
| HU5-CP19 | El enlace de la cabecera para el cliente; el vendedor y el administrador no acceden (3 tests) | `AddressesPage.test.jsx` |
| HU5-CP20 | Marcar otra como principal; la dirección que ya no existe se avisa y la lista se recarga (2 tests) | `AddressesPage.test.jsx` |
| HU5-CP21 | Con 5 direcciones no se puede añadir otra; con menos, sí (2 tests) | `AddressesPage.test.jsx` |
| HU5-CP22 | Validación de los campos obligatorios, los formatos y las longitudes (2 tests) | `AddressFormPanel.test.jsx` |
| HU5-CP23 | Alta: envío sin espacios sobrantes; cancelar no envía nada; desde la página, guardar o cancelar cierra el panel (4 tests) | `AddressFormPanel.test.jsx` y `AddressesPage.test.jsx` |
| HU5-CP24 | Edición: el panel empieza con los datos y guarda con un `PUT`; la dirección que ya no existe se avisa; desde la página, la lista muestra el cambio (3 tests) | `AddressFormPanel.test.jsx` y `AddressesPage.test.jsx` |
| HU5-CP25 | El rechazo del backend se avisa sin repetir los datos; el límite tiene su propio mensaje (2 tests) | `AddressFormPanel.test.jsx` |
| HU5-CP26 | Eliminar tras confirmar; cancelar no elimina (2 tests) | `DeleteAddressDialog.test.jsx` |
| HU5-CP27 | La principal exige elegir sustituta; la única se elimina sin elegir; desde la página, la sustituta pasa a ser la principal (3 tests) | `DeleteAddressDialog.test.jsx` y `AddressesPage.test.jsx` |
| HU5-CP28 | Si la eliminación falla, se avisa sin cerrar el diálogo | `DeleteAddressDialog.test.jsx` |

---

## 7. Pendiente

- **Elegir la dirección al pagar:** es de la HU10, en `/checkout`. Se preselecciona la que llega con `principal: true` y, sin direcciones, se lleva a `/addresses`.
- **Cabecera en móvil:** con el enlace nuevo, la cabecera del cliente se sale aún más del ancho de un móvil (375 px). Ya se salía antes; la adaptación a móviles es de mantenimiento.
- **Errores de servidor al marcar la principal:** igual que en la HU6, hoy cualquier error muestra el mensaje de «ya no existe».
