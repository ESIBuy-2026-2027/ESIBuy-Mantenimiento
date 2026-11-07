# HU50 · Apertura y cierre de incidencias

> Como administrador quiero poder abrir y cerrar las incidencias para dejar constancia de su estado una vez solucionadas.
>
> Criterios de aceptación: la vista de incidencias debe poder abrir la incidencia; la vista de incidencias debe poder cerrar la incidencia; una vez cerrada la incidencia, esta se elimina.

Se apoya en el módulo `incidencias` de la HU44 (crear, contrato en `hu44-incidencias.md`) y en la vista del administrador de la HU49 (`GET /api/admin/incidencias`, contrato en `hu49-supervision-incidencias.md`). La HU50 añade dos acciones sobre cada incidencia de esa vista.

| Task | Qué | Estado |
|---|---|---|
| T302 (AB#302) | Backend: lógica de apertura de incidencias | Hecha |
| T303 (AB#303) | Backend: lógica de cierre y eliminación de incidencias | Hecha |
| T300 (AB#300) | Backend: endpoint `PATCH /api/admin/incidencias/{id}/abrir` | Hecha |
| T301 (AB#301) | Backend: endpoint `DELETE /api/admin/incidencias/{id}` | Hecha |
| T305 (AB#305) | Frontend: botón de apertura y cierre de incidencias (`frontend/docs/hu50-apertura-cierre-incidencias.md`) | Hecha |

## Ciclo de vida de una incidencia

```
PENDIENTE ──abrir (con respuesta)──▶ ABIERTA ──cerrar──▶ (eliminada)
    │                                                       ▲
    └──────────────────────cerrar───────────────────────────┘
```

- Nace `PENDIENTE` (HU44).
- **Abrir** solo se puede desde `PENDIENTE`, y exige la respuesta del administrador. Es lo único que el autor llega a ver, porque al cerrarla la incidencia desaparece.
- **Cerrar** se puede desde cualquier estado y **elimina** la incidencia: no hay estado «cerrada». Cerrar una pendiente sirve para descartar spam o duplicados sin escribir una respuesta.

## Lógica (T302 y T303)

`ResolucionIncidenciasUseCase`, con `abrir(id, respuesta)` y `cerrar(id)`. Es un puerto distinto de `SupervisionIncidenciasUseCase` (HU49): el actor es el mismo, pero aquel solo consulta y este cambia las incidencias.

- **Quién:** solo el administrador (`@PreAuthorize(Permisos.SOLO_ADMINISTRADOR)` en `ResolucionIncidenciasService`).
- **Abrir:** busca la incidencia por id, la abre en el dominio (`Incidencia.abrir`) y la guarda. La respuesta es obligatoria, hasta **1000** caracteres, sin contar los espacios de los extremos, que no se guardan.
- **Cerrar:** comprueba que existe y la elimina.

| Capa | Clase |
|---|---|
| Dominio | `Incidencia.abrir`, `IncidenciaYaAbiertaException`, `IncidenciaNoEncontradaException` |
| Aplicación | `ResolucionIncidenciasUseCase`, `ResolucionIncidenciasService`, `IncidenciaRepositoryPort.buscarPorId` y `eliminar` (nuevos) |
| Infraestructura | `IncidenciaMongoAdapter.buscarPorId` y `eliminar`, bean en `IncidenciasConfig`, `AbrirIncidenciaRequest` y los dos endpoints en `AdministracionIncidenciasController` |
| Shared | `ManejadorGlobalErrores`: 404 `INCIDENT_NOT_FOUND` y 409 `INCIDENT_ALREADY_OPEN` |

## Endpoints (T300 y T301)

Solo para el **administrador**. Los limitan `/api/admin/**` en `SecurityConfig` y el `@PreAuthorize` del servicio. Los sirve `AdministracionIncidenciasController`. Como cualquier petición que modifica datos, llevan el token CSRF en la cabecera `X-XSRF-TOKEN` (el cliente HTTP del frontend ya lo copia de la cookie).

### `PATCH /api/admin/incidencias/{id}/abrir`

Cuerpo:

```json
{ "respuesta": "Lo estamos revisando, te escribimos en 24 horas" }
```

| Campo | Obligatorio | Reglas |
|---|---|---|
| `respuesta` | Sí | No vacía ni solo espacios; hasta **1000** caracteres (sin contar los espacios de los extremos). |

**200 OK.** La incidencia abierta, con el mismo formato que una incidencia de `GET /api/perfil/incidencias` (HU44), sin autor:

```json
{
  "id": "6720f0a1c2b3d4e5f6a7b8c9",
  "asunto": "No me llega el pedido",
  "descripcion": "Lo pedí hace una semana y sigue sin llegar",
  "estado": "ABIERTA",
  "respuesta": "Lo estamos revisando, te escribimos en 24 horas",
  "fechaCreacion": "2026-11-06T10:15:30Z"
}
```

### `DELETE /api/admin/incidencias/{id}`

Sin cuerpo. **204 No Content**, sin cuerpo: la incidencia se ha eliminado. Para refrescar la vista, el frontend puede quitarla de la lista o volver a pedir la página.

### Errores

Cuerpo `{mensaje, code}`, sin detalles internos:

| Estado | `code` | `mensaje` | Cuándo |
|---|---|---|---|
| 400 | `VALIDATION_ERROR` | `La incidencia es inválida` | Abrir sin respuesta, vacía o de más de 1000 caracteres (mensaje genérico: el frontend valida antes con los límites de arriba) |
| 400 | — | — | Abrir con un cuerpo que no es JSON. Lo rechaza Spring antes de llegar al caso de uso |
| 401 | — | — | Sin sesión |
| 403 | `ACCESS_DENIED` | — | La sesión es de un cliente o de un vendedor, o falta el token CSRF |
| 404 | `INCIDENT_NOT_FOUND` | `La incidencia no existe` | Abrir o cerrar una incidencia que no existe (por ejemplo, porque otro administrador ya la cerró) |
| 409 | `INCIDENT_ALREADY_OPEN` | `La incidencia ya está abierta` | Abrir una incidencia que ya está abierta (por ejemplo, porque otro administrador la abrió antes) |

Ante un 404 o un 409, lo razonable en la vista es avisar y volver a pedir la página, porque otro administrador ha cambiado la incidencia.

## Tests

Casos HU50-CP01 a CP17 (35 tests), trazados en `docs/calidad/matriz-trazabilidad.md`, más un test técnico en `IncidenciasConfigTest`. Son casos propuestos, pendientes de confirmar con el equipo.

## Fuera de alcance

- **Editar la respuesta** de una incidencia ya abierta: ninguna HU lo pide, y evita que dos administradores se la sobrescriban sin darse cuenta.
- **Filtrar la vista por estado:** el contrato de la HU49 lo apunta como posible mejora para cuando haya incidencias abiertas; no está en los criterios de la HU50.
