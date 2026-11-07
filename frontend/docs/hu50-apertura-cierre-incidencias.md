# HU50 · Apertura y cierre de incidencias (Frontend)

Documento para entender cómo el administrador abre y cierra las incidencias desde la vista de supervisión: qué botones hay, qué hace cada diálogo, de dónde salen los datos y cómo probarlo.

- **Fecha:** 7 de noviembre de 2026
- **HU:** HU50 · Apertura y cierre de incidencias
- **Task:** T305 (AB#305, botón de apertura y cierre de incidencias)
- **Rama:** `feature/US50-T305-botones-apertura-cierre-incidencias`
- **Metodología:** TDD, en tres ciclos. Cada uno tiene su commit de rojo (`test(front-HU50)`) antes del de verde (`feat(front-HU50)`).
- **Depende de:**
  - el backend de la HU50 (T300 a T303, PR #233) y su contrato, `docs/contratos/hu50-apertura-cierre-incidencias.md`;
  - la vista de supervisión de la HU49 (`docs/hu49-supervision-incidencias.md`).

---

## 1. Resumen

> Como administrador quiero poder abrir y cerrar las incidencias para dejar constancia de su estado una vez solucionadas.

En `/admin/incidents` (HU49), cada tarjeta tiene ahora sus acciones:

| Estado de la incidencia | Botones |
|---|---|
| `PENDIENTE` | **Abrir** y **Cerrar** |
| `ABIERTA` | **Cerrar** |

- **Abrir** pide la respuesta del administrador, que es lo que verá el autor en «Mis incidencias» (HU44). La incidencia pasa a «Abierta» con esa respuesta.
- **Cerrar** pide confirmación y **elimina** la incidencia: no hay estado «cerrada».

## 2. Flujo

1. El administrador pulsa **Abrir** o **Cerrar** en una tarjeta. Se borra el aviso anterior, si lo había, y se abre el diálogo.
2. **Abrir** (`OpenIncidentDialog`):
   - muestra el asunto y un campo «Respuesta», obligatorio, con contador `n/1000`;
   - como en el formulario de la HU44, «obligatoria» sale al intentar enviar y «demasiado larga», en cuanto se pasa;
   - envía `PATCH /api/admin/incidencias/{id}/abrir` con la respuesta sin espacios en los extremos.
3. **Cerrar** (`CloseIncidentDialog`): «¿Seguro que quieres cerrar «asunto»? Se eliminará y no se puede deshacer.», con **Volver** y **Sí, cerrar la incidencia**. Envía `DELETE /api/admin/incidencias/{id}`.
4. Según la respuesta:
   - **bien:** se cierra el diálogo, la página avisa («Incidencia abierta.» o «Incidencia cerrada.») y vuelve a pedir la página actual;
   - **otro administrador ya la ha cambiado** (409 `INCIDENT_ALREADY_OPEN` o 404 `INCIDENT_NOT_FOUND`): se cierra el diálogo, la página avisa («Otro administrador ya ha cambiado esa incidencia. Hemos actualizado la lista.») y la vuelve a pedir;
   - **cualquier otro fallo:** el diálogo sigue abierto con un aviso de error, y se puede volver a intentar.
5. Si se cierra la **única incidencia de la última página**, esa página deja de existir y la vista pasa a la anterior.

## 3. Archivos

| Archivo | Qué |
|---|---|
| `api/adminIncidents.js` | `openIncident(id, respuesta)` → `PATCH …/{id}/abrir` y `closeIncident(id)` → `DELETE …/{id}` |
| `features/incidents/incidentRules.js` | `RESPONSE_MAX` (1000) y `validateResponse`, con el mismo `validateText` que el asunto y la descripción |
| `features/incidents/OpenIncidentDialog.jsx` (nuevo) | El diálogo de apertura |
| `features/incidents/CloseIncidentDialog.jsx` (nuevo) | La confirmación de cierre, como `CancelOrderDialog` (HU12) |
| `features/incidents/IncidentsList.jsx` | Prop nuevo y opcional `renderActions(incident)`: lo que devuelve va al final de cada tarjeta. Sin él, nada cambia |
| `pages/AdminIncidentsPage.jsx` | Los botones de cada tarjeta, los dos diálogos y los avisos |
| `mocks/incidentsHandlers.js` | `PATCH /api/admin/incidencias/:id/abrir` y `DELETE /api/admin/incidencias/:id`, con las reglas del backend |

## 4. Contrato usado

Contrato completo en `docs/contratos/hu50-apertura-cierre-incidencias.md`.

| Petición | Bien | Errores que trata la vista |
|---|---|---|
| `PATCH /api/admin/incidencias/{id}/abrir` con `{ "respuesta": "…" }` | 200 con la incidencia abierta | 409 `INCIDENT_ALREADY_OPEN` y 404 `INCIDENT_NOT_FOUND`: aviso y recarga |
| `DELETE /api/admin/incidencias/{id}` | 204 sin cuerpo | 404 `INCIDENT_NOT_FOUND`: aviso y recarga |

- El **400 `VALIDATION_ERROR`** no debería llegar, porque el diálogo valida antes con los mismos límites. Si llega, se muestra como cualquier otro fallo.
- El **CSRF** lo añade `api/client.js` en las peticiones que cambian datos.

## 5. Decisiones

- **Cerrar pide confirmación** (decisión del equipo): elimina la incidencia y no se puede deshacer.
- **La respuesta se escribe al abrir** y es obligatoria, como en el backend: es lo único que llega a ver el autor.
- **Un prop genérico en `IncidentsList`** (`renderActions`), en lugar de meter en la lista los botones del administrador:
  - la lista sigue sin saber de roles ni de llamadas a la API;
  - la página decide qué botones van en cada incidencia;
  - «Mis incidencias» (HU44) no cambia.
- **Tras cada acción se vuelve a pedir la página** en vez de cambiar la lista en memoria. Así los totales y la paginación salen siempre del backend, también cuando otro administrador ha cambiado algo.
- **409 y 404 no se tratan como un error del diálogo**, sino como un cambio de otro administrador: se avisa y se recarga, como pide el contrato.
- **Nombre accesible de los botones:** `Abrir «asunto»` y `Cerrar «asunto»`, porque en la lista hay muchos botones con el mismo texto visible.
- **Iconos:** `Open20Regular` para abrir y `CheckmarkCircle20Regular` para cerrar, porque la HU habla de cerrar las incidencias «una vez solucionadas».

## 6. Tests

| Archivo | Casos |
|---|---|
| `api/adminIncidents.test.js` | HU50-CP18: las dos llamadas, y un 409 y un 404 llegan como error con su código |
| `features/incidents/incidentRules.test.js` | HU50-CP19: respuesta obligatoria y hasta 1000 caracteres sin contar los extremos |
| `features/incidents/OpenIncidentDialog.test.jsx` (nuevo) | HU50-CP20: asunto y contador, envío recortado, obligatoria, demasiado larga, cancelar, fallo con reintento, 409 y 404 |
| `features/incidents/CloseIncidentDialog.test.jsx` (nuevo) | HU50-CP21: confirmación, cerrar, volver, fallo con reintento y 404 |
| `features/incidents/IncidentsList.test.jsx` | HU50-CP22: con `renderActions` y sin él |
| `pages/AdminIncidentsPage.test.jsx` | HU50-CP23 a CP26: botones según el estado, abrir, cerrar (también la última de la página y cancelar) y cambios de otro administrador |

**Sin rojo real:** «sin `renderActions` las tarjetas no tienen acciones» (CP22) pasó al escribirse, porque la lista todavía no tenía acciones. Se queda para proteger «Mis incidencias».

**Datos del mock:** los de la HU49 (15 incidencias, dos páginas de 10). En la página 1, «No me llega el pedido» (la primera) está pendiente y «Cobro duplicado» (la segunda), abierta. Cerrar y abrir cambian el mock hasta que se reinicia, entre test y test.

## 7. Pendiente

- **Editar la respuesta** de una incidencia ya abierta: fuera de alcance, como dice el contrato.
- **Filtrar la vista por estado:** posible mejora para cuando haya muchas incidencias abiertas; no está en los criterios de la HU50.
