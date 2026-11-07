# HU49 · Supervisión de incidencias (Frontend)

Documento para entender cómo el administrador ve las incidencias de todos los clientes y vendedores: cómo llega a la vista, qué muestra, de dónde salen los datos y cómo probarla.

- **Fecha:** 7 de noviembre de 2026
- **HU:** HU49 · Supervisión de incidencias (AB#294)
- **Task:** T304 (AB#304, vista de supervisión de incidencias)
- **Rama:** `feature/US49-T304-vista-supervision-incidencias`
- **Metodología:** TDD, en dos ciclos. Cada uno tiene su commit de rojo (`test(front-HU49)`) antes del de verde (`feat(front-HU49)`).
- **Depende de:**
  - el backend de la HU49 (T307 en el PR #229 y T306 en el PR #232) y su contrato, `docs/contratos/hu49-supervision-incidencias.md`;
  - la lista de incidencias de la HU44 (`docs/hu44-incidencias.md`).

---

## 1. Resumen

> Como administrador quiero poder ver las incidencias para solucionarlas.

El administrador entra desde el **Panel de control** con la tarjeta **Incidencias** y llega a una lista de las incidencias de todos los clientes y vendedores, por páginas. Cada incidencia muestra:
- lo mismo que en «Mis incidencias» (HU44): asunto, fecha, estado, descripción y respuesta;
- **quién la reportó:** su correo y un badge con el rol (**Cliente** o **Vendedor**), o **«Cuenta eliminada»** si su cuenta ya no existe.

La vista solo permite consultar. Abrir y cerrar incidencias es la HU50.

## 2. Rutas

| Ruta | Página | Quién |
|---|---|---|
| `/admin/incidents` (`PATHS.ADMIN_INCIDENTS`) | `AdminIncidentsPage` | Solo el administrador |

Va dentro de las rutas protegidas del administrador (`ProtectedRoute` con `ROLES.ADMIN`):
- un cliente vuelve a `/products`;
- un vendedor vuelve a `/seller/products`;
- sin sesión, va al login.

## 3. Flujo

1. En `/admin`, la tarjeta **Incidencias** lleva a `/admin/incidents`.
2. `AdminIncidentsPage` pide `GET /api/admin/incidencias?page=1&size=10` con `useApiQuery`. Mientras tanto muestra «Cargando incidencias…».
3. Según la respuesta:
   - **con incidencias:** `IncidentsList` con `showAuthor`, y debajo la paginación (`Pagination`);
   - **sin incidencias:** un `EmptyState`, «No hay incidencias»;
   - **error:** un aviso con «Reintentar».
4. La paginación cambia la página pedida. Mientras llega la nueva, la lista se queda atenuada (`aria-busy`).

## 4. Archivos

| Archivo | Qué |
|---|---|
| `api/adminIncidents.js` (nuevo) | `getAdminIncidents({ page, size })` → `GET /api/admin/incidencias` |
| `pages/AdminIncidentsPage.jsx` (nuevo) | La vista: título, carga, error, lista vacía, lista y paginación |
| `features/incidents/IncidentsList.jsx` | Dos props nuevos y opcionales: `showAuthor` (muestra el autor en cada tarjeta) y `label` (el nombre accesible de la lista, por defecto «Tus incidencias») |
| `pages/AdminPage.jsx` | La tarjeta **Incidencias** del panel |
| `routes/paths.js` y `routes/AppRoutes.jsx` | `ADMIN_INCIDENTS` y su ruta protegida |
| `mocks/incidentsHandlers.js` | `GET /api/admin/incidencias` y una incidencia de una cuenta eliminada |

## 5. Contrato usado

`GET /api/admin/incidencias?page=1&size=10` (contrato completo en `docs/contratos/hu49-supervision-incidencias.md`):

```json
{
  "items": [
    { "id": "…", "asunto": "…", "descripcion": "…", "estado": "PENDIENTE", "respuesta": "",
      "fechaCreacion": "2026-11-06T10:15:30Z", "autor": { "email": "cliente@esibuy.test", "rol": "CLIENTE" } },
    { "id": "…", "…": "…", "autor": null }
  ],
  "page": 1, "size": 10, "totalItems": 2, "totalPages": 1
}
```

- **Respuestas de error:** 401 sin sesión y 403 `ACCESS_DENIED` para el cliente y el vendedor. La vista no llega a pedirla en esos casos, porque la ruta ya los saca.
- **Cualquier fallo** se muestra igual: el aviso con «Reintentar».

## 6. Decisiones

- **Se reutiliza `IncidentsList` de la HU44** con un prop, `showAuthor`, en lugar de un componente propio (decisión del equipo).
  - Así la tarjeta es la misma en las dos vistas y no se duplica código (Sonar pide menos del 10 %).
  - «Mis incidencias» no cambia: sin el prop, el autor no se muestra.
- **Abre en la primera página**, la de las incidencias más antiguas (decisión del equipo). El administrador atiende primero las que más tiempo llevan esperando.
  - «Mis incidencias» abre en la última, porque al usuario le interesa lo que acaba de reportar.
  - El orden lo da el backend (de las más antiguas a las más recientes) y la vista lo respeta.
- **El autor:**
  - se muestra el correo, que es lo que devuelve el contrato (no hay nombre);
  - el rol va en un badge de contorno, para que no se confunda con el del estado;
  - si `autor` es `null`, se muestra «Cuenta eliminada»: con la HU25 y la HU47 la cuenta se borra, pero sus incidencias se quedan.
- **La descripción de la tarjeta del panel no nombra a los clientes ni a los vendedores.** Los tests de la HU18 buscan las tarjetas «Clientes» y «Vendedores» por su nombre accesible, y la descripción forma parte de ese nombre.
- **Sin botones de acción.** La HU49 solo pide ver las incidencias; abrir, cerrar o responder llegará con la HU50.

## 7. Tests

| Archivo | Casos |
|---|---|
| `api/adminIncidents.test.js` (nuevo) | HU49-CP15: la llamada pide la página y el tamaño y devuelve la página; un 403 llega como error |
| `features/incidents/IncidentsList.test.jsx` | HU49-CP18 y CP19: con `showAuthor`, el correo y el rol, o «Cuenta eliminada»; el `label`; sin el prop, ningún autor |
| `pages/AdminPage.test.jsx` | HU49-CP16: la tarjeta Incidencias, su descripción y adónde lleva |
| `pages/AdminIncidentsPage.test.jsx` (nuevo) | HU49-CP17 a CP22: primera página, autor y rol, cuenta eliminada, paginación, carga, lista vacía, error con reintentar y acceso por rol |

**Sin rojo real:** «sin `showAuthor` no se muestra el autor» (CP18) pasó al escribirse, porque el componente todavía no mostraba ningún autor.

**Datos del mock:** hay 15 incidencias, es decir, dos páginas de 10.
- **Página 1:** las 10 más antiguas, todas de Carlos (`cliente@esibuy.test`).
- **Página 2:** dos de Carlos, dos de Vera (`vendedor@esibuy.test`) y la de una cuenta eliminada, que es la más reciente.

«Mis incidencias» no cambia: cada usuario sigue viendo solo las suyas.

## 8. Pendiente

- **Abrir y cerrar incidencias:** HU50. Sus botones irán en cada tarjeta de esta vista.
- **Responder a una incidencia:** el contrato de la HU44 dice que la respuesta la escribe un administrador, pero ni la HU49 ni la HU50 tienen una task para ello. Hay que confirmarlo con el equipo.
- **Mocks y backend real:** los mocks de `incidentsHandlers.js` conviven con el backend de los PRs #229 y #232; no hay que quitarlos.
