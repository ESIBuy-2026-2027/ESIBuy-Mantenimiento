# Contrato de la API · HU44 Creación de incidencias

Contrato de los endpoints del backend que usa el frontend de la HU44 (T289 y T290). Tasks del backend: T291 (AB#291), T293 (AB#293), T292 (AB#292) y T419 (AB#419).

Los dos endpoints son para el **cliente** y el **vendedor** autenticados. El autor de la incidencia es siempre el usuario de la sesión: el cuerpo de la petición no lleva autor.

## Convenciones comunes

- Ruta base: `/api/perfil/incidencias`.
- Se necesita sesión (cookie `JSESSIONID`). Sin sesión: **401**.
- El **administrador** no usa estos endpoints: recibe **403** con `code: "ACCESS_DENIED"`.
- Los `POST` necesitan el token CSRF: el cliente HTTP copia la cookie `XSRF-TOKEN` en la cabecera `X-XSRF-TOKEN`. Sin él: **403**.
- Las fechas son texto ISO-8601 en UTC, p. ej. `"2026-11-06T10:15:30Z"`.
- Los errores tienen la forma `{ "mensaje": "...", "code": "..." }`.

## Estados de una incidencia

| Estado | Significado |
|---|---|
| `PENDIENTE` | Ningún administrador la ha revisado todavía. Es el estado con el que nace. |
| `ABIERTA` | Un administrador la ha abierto (HU50). |

No existe un estado «cerrada»: una incidencia cerrada se elimina (HU50), por lo que deja de aparecer en la lista. La `respuesta` está vacía (`""`) hasta que un administrador la escriba (HU49 y HU50, fuera de este alcance).

## Crear una incidencia (T292)

`POST /api/perfil/incidencias`

Cuerpo:

```json
{
  "asunto": "No me llega el pedido",
  "descripcion": "Lo pedí hace una semana y sigue sin llegar"
}
```

| Campo | Obligatorio | Reglas |
|---|---|---|
| `asunto` | Sí | No vacío ni solo espacios; hasta **100** caracteres (sin contar los espacios de los extremos). |
| `descripcion` | Sí | No vacía ni solo espacios; hasta **1000** caracteres (sin contar los espacios de los extremos). |

Ambos textos se guardan sin espacios en los extremos. Cualquier otro campo del cuerpo (p. ej. un `autorId`) se ignora.

Respuesta **201**:

```json
{
  "id": "6720c1f0a1b2c3d4e5f60718",
  "asunto": "No me llega el pedido",
  "descripcion": "Lo pedí hace una semana y sigue sin llegar",
  "estado": "PENDIENTE",
  "respuesta": "",
  "fechaCreacion": "2026-11-06T10:15:30Z"
}
```

Errores:

| Estado | Cuándo | Cuerpo |
|---|---|---|
| 400 | Falta el asunto o la descripción, o se supera un máximo, o el cuerpo no se puede leer (JSON mal formado) | `{ "mensaje": "La incidencia es inválida", "code": "VALIDATION_ERROR" }` (mensaje genérico: no dice qué campo falla; el frontend valida antes con los límites de arriba) |
| 401 | Sin sesión | — |
| 403 | Administrador (`code: "ACCESS_DENIED"`) o falta el token CSRF | — |

## Consultar mis incidencias (T419)

`GET /api/perfil/incidencias?page=1&size=10`

Devuelve las incidencias del usuario en sesión, **de las más antiguas a las más recientes**, por páginas.

| Parámetro | Por defecto | Reglas |
|---|---|---|
| `page` | `1` | La primera página es la **1**. Un número menor que 1 se rechaza. |
| `size` | `10` | De **1 a 50**. |

Respuesta **200**:

```json
{
  "items": [
    {
      "id": "6720c1f0a1b2c3d4e5f60718",
      "asunto": "No me llega el pedido",
      "descripcion": "Lo pedí hace una semana y sigue sin llegar",
      "estado": "PENDIENTE",
      "respuesta": "",
      "fechaCreacion": "2026-11-06T10:15:30Z"
    }
  ],
  "page": 1,
  "size": 10,
  "totalItems": 1,
  "totalPages": 1
}
```

Sin incidencias: `items: []`, `totalItems: 0` y `totalPages: 0`. Si se pide una página más allá de la última, `items` viene vacío pero con los totales. Los elementos de `items` tienen el mismo formato que la respuesta de crear (sin el autor). La paginación sigue la misma convención que `/api/admin/usuarios`.

Errores:

| Estado | Cuándo | Cuerpo |
|---|---|---|
| 400 | `page` o `size` fuera de rango (`page` menor que 1, `size` menor que 1 o mayor que 50) | `{ "mensaje": "La incidencia es inválida", "code": "VALIDATION_ERROR" }` |
| 400 | `page` o `size` no es un número | Error por defecto de Spring (sin el formato `RespuestaError`) |
| 401 | Sin sesión | — |
| 403 | Administrador | `code: "ACCESS_DENIED"` |

## Notas para el frontend

- Tras crear, la nueva incidencia es la **más reciente**: con el orden de más antiguas a más recientes, aparece en la **última** página.
- La lista no incluye a quién pertenece cada incidencia: siempre son las del usuario en sesión.
- Pendiente de confirmar con el equipo (casos HU44-CP01 a CP22 propuestos).
