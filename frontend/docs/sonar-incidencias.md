# Corrección de incidencias de SonarQube (Frontend)

Documento para entender qué marcaba SonarQube en el frontend, cómo se ha corregido y qué convención de código sale de ello (en particular, la validación de props con `prop-types`).

- **Fecha:** 8 de octubre de 2026
- **Rama:** `bugfix/correccion-incidencias-sonar`
- **Análisis de partida:** proyecto `2026-g02-frontend`, código de `develop` en el commit `b40d9b1`
- **Resultado de partida:** 113 incidencias abiertas y quality gate en **ERROR** (fiabilidad D, seguridad E, 1 bloqueante y 2 críticas)

---

## 1. Resumen

| Regla | Qué detecta | Incidencias | Estado |
|---|---|---|---|
| `javascript:S6774` | Props de componentes sin validar | 105 | Corregida |
| `javascript:S2871` | `sort()` sin función de comparación (**bug crítico**) | 2 | Corregida |
| `javascript:S6478` | Componente definido dentro de otro componente | 1 | Corregida |
| `javascript:S6819` | `role="img"` en lugar de un elemento nativo | 2 | Corregida |
| `javascript:S1940` | Negación de una comparación en lugar del operador contrario | 1 | Corregida |
| `javascript:S1135` | Comentario con la palabra "todo" | 1 | Corregida |
| `secrets:S6702` | **Token de SonarQube escrito en `sonar-project.properties` (bloqueante)** | 1 | **Pendiente** (sección 4) |

Las dos incidencias críticas y la bloqueante son las que ponen el quality gate en rojo; las críticas están corregidas, la bloqueante requiere una decisión del equipo.

---

## 2. Qué se ha cambiado

Todas las rutas son relativas a `frontend/src/`.

### S6774 · Validación de props con `prop-types`

Todos los componentes que reciben props las declaran ahora con `Componente.propTypes = { ... }`.

| Archivo | Qué hace |
|---|---|
| `utils/propTypes.js` | Tipos de props compartidos: `categoryPropType`, `productPropType` y `productValuesPropType`. Evita repetir la misma forma de objeto en cada componente. |
| Componentes de `auth/`, `components/`, `features/admin/`, `features/auth/` y `features/seller/` | Cada uno declara sus `propTypes` justo al final del archivo. |

Criterios que se han seguido:

- Lo que el componente necesita para funcionar es `isRequired`; lo opcional (por ejemplo `children` de `ListFilters`, o `refreshing` de las tablas) no lo es.
- Los objetos y las listas se describen con `shape` y `arrayOf`, no con `object` o `array`, para que se validen también sus campos. Esto elimina las incidencias de tipo `seller.nombre is missing in props validation`.
- No se usa `defaultProps` (está en desuso en componentes de función con React 19); los valores por defecto van en los parámetros de la función.
- `prop-types` solo avisa **en desarrollo y en los tests** (en la consola). No cambia el comportamiento en producción.

### S2871 · `sort()` sin función de comparación

`sort()` sin argumento ordena como texto según el código de cada carácter, lo que da orden incorrecto con acentos o mayúsculas. Se ha añadido `(a, b) => a.localeCompare(b)` en `features/seller/productValues.test.js` y en `pages/AdminUsersPage.test.jsx`.

### S6478 · Componente definido dentro de otro

La función que pintaba los botones de editar y eliminar de cada fila estaba escrita dentro de `SellerProductsPage`, de modo que React la trataba como un componente nuevo en cada render (se vuelve a montar y pierde el estado).

- Nuevo `features/seller/ProductRowActions.jsx`: componente propio, definido fuera, con sus `propTypes`.
- `features/seller/ProductsTable.jsx`: la propiedad `actions` se llama ahora **`renderActions`**, el nombre que usa React para las funciones que devuelven JSX y que no son componentes.

### S6819 · `role="img"` en los iconos del registro

En `pages/Register.jsx`, los iconos de "Cumplido" y "Pendiente" llevaban `role="img"` y `aria-label`. Los iconos de Fluent UI con `aria-label` ya se exponen como imagen, así que se ha quitado el `role` redundante. Los tests del registro, que buscan esos iconos por rol e imagen, siguen pasando.

### S1940 y S1135 · Validación del precio y un comentario

- `features/seller/productValues.js`: `!(Number(price) > 0)` pasa a `Number.isNaN(Number(price)) || Number(price) <= 0`. Se añade el control de `NaN` explícito porque `Number('abc') <= 0` es falso, y sin él un precio con letras dejaría de dar error.
- Un comentario del mismo archivo contenía la palabra "todo" (en "si todo es válido") y Sonar lo tomaba por una marca `TODO`; se ha reformulado.

---

## 3. Librería añadida

| Librería | Versión | Para qué | Dónde |
|---|---|---|---|
| `prop-types` | 15.8.x | Declarar y comprobar el tipo de las props de cada componente | `dependencies` de `frontend/package.json` |

Es la librería oficial de React para esto, no añade dependencias propias relevantes y no afecta al tamaño del código del producto de forma apreciable. No se ha añadido ninguna regla de ESLint nueva.

> **Convención para el código nuevo:** todo componente que reciba props debe declarar sus `propTypes`. Si no, Sonar vuelve a marcar `S6774`.

---

## 4. Pendiente: token de SonarQube en el repositorio (bloqueante)

Sonar marca como **vulnerabilidad bloqueante** (`secrets:S6702`) que `frontend/sonar-project.properties` contiene `sonar.token=squ_…` escrito en claro. El archivo del backend tiene el mismo problema. Esta incidencia es la que mantiene en rojo la puntuación de seguridad.

**No se ha corregido en esta rama.** `CLAUDE.md` indica que no se cambie la configuración de Sonar sin consultarlo, y la corrección afecta a cómo lanza el análisis todo el equipo. Lo que haría falta:

1. **Revocar** el token actual en SonarQube y generar uno nuevo. Eliminar el valor del archivo no basta: el token sigue en el historial de Git.
2. **Quitar** la línea `sonar.token` de `sonar-project.properties` (frontend y backend).
3. **Pasar el token** al lanzar el análisis, por ejemplo con la variable de entorno `SONAR_TOKEN` o con `sonar-scanner -Dsonar.token=<token>`, y documentarlo para el equipo.

---

## 5. Cómo comprobarlo

```bash
npx vitest run     # 286 tests, todos en verde
npm run lint
npm run build
```

Para ver el efecto en Sonar hay que volver a lanzar `sonar-scanner` desde `frontend/` una vez integrada la rama en `develop`. Resultado esperado: desaparecen las 112 incidencias corregidas y queda solo la del token (sección 4).

---

## 6. Uso de IA generativa

Este documento y el trabajo técnico que describe se hicieron con ayuda de un asistente de IA generativa, como exige el enunciado.

- **Herramienta:** Claude (modelo de Anthropic), mediante Claude Code.
- **Qué generó la IA:** las correcciones, el componente `ProductRowActions`, los tipos de props compartidos y este documento. La lista de incidencias se leyó de la API de SonarQube con el token que facilitó el equipo (solo en memoria, sin escribirlo en ningún archivo). Las decisiones de producto las tomó el equipo: corregir las incidencias de Sonar y hacerlo en una rama aparte.
- **Cómo se comprobó:** se ejecutaron los **286 tests (todos en verde)**, ESLint sin errores y un build de producción correcto. No se volvió a lanzar el análisis de Sonar tras los cambios.
- **Qué NO se comprobó:** que Sonar dé por resueltas todas las incidencias (depende de un nuevo análisis).
- **Revisión humana:** el código fue revisado, ejecutado y validado por el equipo antes de su integración. Revisado por: Ricardo.
