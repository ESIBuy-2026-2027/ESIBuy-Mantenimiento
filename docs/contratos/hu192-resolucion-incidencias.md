# HU192 · Marcar incidencia como resuelta (PBI 192)

> Como administrador, quiero marcar una incidencia como resuelta, para llevar control de qué problemas siguen abiertos.
>
> Criterios de aceptación: el administrador cambia el estado de una incidencia a resuelta; el sistema registra la fecha de resolución; las incidencias resueltas quedan diferenciadas de las abiertas.

Se apoya en el módulo `incidencias` (HU44, HU49 y HU50; contratos `hu44-incidencias.md`, `hu49-supervision-incidencias.md` y `hu50-apertura-cierre-incidencias.md`).

## Ciclo de vida de una incidencia

```
PENDIENTE ──abrir (con respuesta)──▶ ABIERTA ──resolver──▶ RESUELTA
    │                                   │                      │
    └───────────────cerrar──────────────┴────────cerrar────────┘──▶ (eliminada)
```

- **Resolver** solo se puede desde `ABIERTA`. Una pendiente se abre antes (HU50), para que su autor tenga una respuesta.
- La **fecha de resolución la pone el sistema** (reloj del servidor). La petición no lleva cuerpo y, si lo lleva, se ignora.
- Una incidencia resuelta no se vuelve a resolver (la fecha original no se sobrescribe) ni se vuelve a abrir. No hay «reabrir».
- **Cerrar** (HU50) no cambia: sigue eliminando la incidencia, también si está resuelta.

## `PATCH /api/admin/incidencias/{id}/resolver`

Solo para el **administrador**: lo limitan `/api/admin/**` en `SecurityConfig` y el `@PreAuthorize` de `ResolucionIncidenciasService`. Sin cuerpo. Como cualquier petición que modifica datos, lleva el token CSRF en la cabecera `X-XSRF-TOKEN`.

**200 OK.**

```json
{
  "id": "6720f0a1c2b3d4e5f6a7b8c9",
  "asunto": "No me llega el pedido",
  "descripcion": "Lo pedí hace una semana y sigue sin llegar",
  "estado": "RESUELTA",
  "respuesta": "Lo estamos revisando, te escribimos en 24 horas",
  "fechaCreacion": "2026-11-06T10:15:30Z",
  "fechaResolucion": "2026-11-25T12:00:00Z"
}
```

### Errores

Cuerpo `{mensaje, code}`, sin detalles internos:

| Estado | `code` | `mensaje` | Cuándo |
|---|---|---|---|
| 401 | — | — | Sin sesión |
| 403 | `ACCESS_DENIED` | — | La sesión es de un cliente o de un vendedor, o falta el token CSRF |
| 404 | `INCIDENT_NOT_FOUND` | `La incidencia no existe` | La incidencia no existe (por ejemplo, porque otro administrador la cerró) |
| 409 | `INCIDENT_ALREADY_RESOLVED` | `La incidencia ya está resuelta` | Ya está resuelta (por ejemplo, porque otro administrador se ha adelantado). También al intentar abrirla |
| 409 | `INCIDENT_NOT_OPEN` | `Solo se puede resolver una incidencia abierta` | Sigue pendiente |

Ante un 404 o un 409, la vista avisa y vuelve a pedir la página.

## Cambios en las demás respuestas

- Todas las respuestas con una incidencia (`/api/perfil/incidencias` y `/api/admin/incidencias`) añaden `fechaResolucion`: la fecha ISO-8601 si está resuelta y `null` si no.
- `estado` admite el valor `RESUELTA`.
- `GET /api/admin/incidencias?estado=RESUELTA` devuelve solo las resueltas; `estado=ABIERTA` devuelve solo las abiertas sin resolver.

## Persistencia

La colección `incidencias` **no cambia su validación** (`estado` sigue siendo `PENDIENTE` o `ABIERTA`). Una incidencia resuelta se guarda como `estado: "ABIERTA"` con el campo `fechaResolucion`, y el mapper la lee como `RESUELTA`. Quien consulte la base de datos directamente debe mirar `fechaResolucion` para saber si está resuelta. La fecha se registra con una única actualización condicionada (sigue abierta y sin fecha), así que dos administradores a la vez no la sobrescriben.
