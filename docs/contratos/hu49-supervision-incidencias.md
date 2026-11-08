# HU49 · Supervisión de incidencias

> Como administrador quiero poder ver las incidencias para solucionarlas.
>
> Criterio de aceptación: el administrador debe disponer de una vista para las incidencias.

Se apoya en el módulo `incidencias` de la HU44 (PR #224, contrato en `hu44-incidencias.md`). La HU44 deja que cada cliente o vendedor vea **sus** incidencias. La HU49 deja que el administrador vea **las de todos**, cada una con quién la reportó.

| Task | Qué | Estado |
|---|---|---|
| T307 (AB#307) | Backend: lógica de obtención de incidencias | Hecha (PR #229) |
| T306 (AB#306) | Backend: endpoint `GET /api/admin/incidencias` | Hecha (contrato abajo) |
| T304 (AB#304) | Frontend: vista de supervisión de incidencias | Pendiente |

## Lógica (T307)

### Caso de uso

`SupervisionIncidenciasUseCase.consultarTodas(pagina, tamano)` devuelve una `IncidenciaSupervisadaPage`. Es un puerto distinto de `ConsultaIncidenciasUseCase` (HU44) porque el actor es otro: el administrador, no el autor.

- **Quién:** solo el administrador (`@PreAuthorize(Permisos.SOLO_ADMINISTRADOR)`). Un cliente o un vendedor reciben `AccessDeniedException` (403 `ACCESS_DENIED`), y sin sesión se rechaza (401).
- **Qué:** las incidencias de todos los autores, de las más antiguas a las más recientes, con orden estable (por `fechaCreacion` y después por `id`). El orden y la paginación son los mismos que en la HU44.
- **Paginación:** la primera página es la 0 y el tamaño va de 1 a 50. Fuera de rango lanza `IncidenciaInvalidaException` (400 `VALIDATION_ERROR`) sin consultar nada.

### Lo que devuelve

```
IncidenciaSupervisadaPage(incidencias, pagina, tamano, totalElementos)
  └─ IncidenciaSupervisada(incidencia, autor)
       ├─ incidencia: la Incidencia de la HU44 (id, autorId, asunto, descripcion, estado, respuesta, fechaCreacion)
       └─ autor: AutorIncidencia(email, rol), o null si la cuenta se ha eliminado
```

`rol` es texto: `"CLIENTE"` o `"VENDEDOR"`, los dos roles que pueden crear incidencias.

### Cómo se obtiene el autor

La incidencia solo guarda el id de su autor (`autorId`, `usuarioId` en MongoDB). Incidencias no puede usar el dominio de usuarios, así que pregunta por el puerto de entrada de ese módulo:

```
SupervisionIncidenciasService ─▶ AutoresPort ─▶ AutoresAdapter ─▶ AdministracionUsuariosUseCase.consultarContacto(id)
                                 (incidencias)   (infraestructura)   (usuarios: devuelve ContactoUsuario(email, rol))
```

- `consultarContacto` es nuevo en `AdministracionUsuariosUseCase` y solo lo puede usar el administrador. Devuelve el correo y el rol como texto, para que el otro módulo no dependa de `Usuario` ni de `Rol`.
- **Cada autor se busca una sola vez por página**, aunque tenga varias incidencias en ella. El servicio guarda los autores ya buscados en un mapa local a la llamada, que no se conserva entre consultas.

### Cuenta eliminada

Con la HU25 y la HU47 la cuenta del usuario se borra del todo, pero sus incidencias se quedan. En ese caso `consultarContacto` devuelve vacío y la incidencia llega con `autor = null`: **el listado no falla**. La vista del administrador mostrará «Cuenta eliminada» (T304).

### Piezas nuevas

| Capa | Clase |
|---|---|
| Dominio | `AutorIncidencia`, `IncidenciaSupervisada`, `IncidenciaSupervisadaPage` |
| Aplicación | `SupervisionIncidenciasUseCase` (puerto de entrada), `AutoresPort` (puerto de salida), `SupervisionIncidenciasService`, `IncidenciaRepositoryPort.buscarTodas` (método nuevo) |
| Infraestructura | `IncidenciaMongoAdapter.buscarTodas`, `AutoresAdapter` y dos beans en `IncidenciasConfig` |
| Usuarios | `ContactoUsuario` y `AdministracionUsuariosUseCase.consultarContacto` |

### Tests

Casos HU49-CP01 a CP10 (22 tests), trazados en `docs/calidad/matriz-trazabilidad.md`, más un test técnico en `IncidenciasConfigTest`. Son casos propuestos, pendientes de confirmar con el equipo.

## Endpoint (T306)

### `GET /api/admin/incidencias?page=1&size=10`

Solo para el **administrador**. Lo limitan `/api/admin/**` en `SecurityConfig` y el `@PreAuthorize` del servicio. Lo sirve `AdministracionIncidenciasController`.

| Parámetro | Por defecto | Valores |
|---|---|---|
| `page` | `1` | La primera es la 1 |
| `size` | `10` | De 1 a 50 |

**200 OK.** Una página con el mismo formato que `GET /api/perfil/incidencias` (HU44), y además el `autor` de cada incidencia:

```json
{
  "items": [
    {
      "id": "6720f0a1c2b3d4e5f6a7b8c9",
      "asunto": "No me llega el pedido",
      "descripcion": "Lo pedí hace una semana y sigue sin llegar",
      "estado": "PENDIENTE",
      "respuesta": "",
      "fechaCreacion": "2026-11-06T10:15:30Z",
      "autor": { "email": "cliente@esibuy.test", "rol": "CLIENTE" }
    },
    {
      "id": "6720f0a1c2b3d4e5f6a7b8ca",
      "asunto": "Un producto aparece sin stock",
      "descripcion": "...",
      "estado": "PENDIENTE",
      "respuesta": "",
      "fechaCreacion": "2026-11-06T11:00:00Z",
      "autor": null
    }
  ],
  "page": 1,
  "size": 10,
  "totalItems": 2,
  "totalPages": 1
}
```

- Las incidencias van **de la más antigua a la más reciente**: la última página tiene las más nuevas.
- `estado`: `PENDIENTE` o `ABIERTA` (HU50).
- `respuesta`: `""` mientras nadie la haya escrito.
- `autor`: `{email, rol}`, con `rol` igual a `CLIENTE` o `VENDEDOR`. Es **`null` si la cuenta del autor se ha eliminado**: la clave siempre está presente.
- No se expone el id interno del autor.
- Sin incidencias: `items` vacío, `totalItems` 0 y `totalPages` 0.

**Errores** (cuerpo `{mensaje, code}`, sin detalles internos):

| Estado | `code` | Cuándo |
|---|---|---|
| 400 | `VALIDATION_ERROR` | `page` menor que 1 o `size` fuera de 1 a 50 |
| 400 | — | `page` o `size` no son un número. Lo rechaza Spring antes de llegar al caso de uso, y el cuerpo no lleva necesariamente el `code` |
| 401 | — | Sin sesión |
| 403 | `ACCESS_DENIED` | La sesión es de un cliente o de un vendedor |

Tests: casos HU49-CP11 a CP14 en `AdministracionIncidenciasControllerTest` (8 tests, 11 ejecuciones con los parametrizados).

## Fuera de alcance

- **Responder a una incidencia** o cambiarla a `ABIERTA`: el criterio de la HU49 solo pide verlas. Abrir y cerrar es la HU50.
- **Filtrar por estado:** encaja mejor en la HU50, cuando haya incidencias abiertas.
