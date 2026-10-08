# Cobertura del frontend en SonarQube (implementación)

Documento de cierre del trabajo para que SonarQube mida la cobertura del frontend. Explica qué se ha cambiado, qué problemas aparecieron por el camino, cómo se resolvieron y cómo lanzar el análisis a partir de ahora. La propuesta inicial, con el diagnóstico y el porqué, está en `docs/sonar-cobertura.md`.

- **Fecha:** 9 de octubre de 2026
- **Rama:** `chore/sonar-cobertura-frontend` (sale de `develop` en el commit `8677342`)
- **Task:** ninguna de Azure Boards; es un trabajo de configuración de calidad, no una task del backlog
- **Depende de:** `docs/sonar-cobertura.md` (propuesta) y `docs/sonar-incidencias.md` (corrección anterior de incidencias)

---

## 1. Resumen

| Métrica (SonarQube, `2026-g02-frontend`) | Antes | Después |
|---|---|---|
| Quality gate | Passed | **Passed** |
| Cobertura | 0,0 % | **96,4 %** (líneas 97,5 %) |
| Líneas de código | 7 257 | 4 145 (los tests ya no cuentan como código) |
| Duplicación | 1,2 % | 0,0 % |
| Bugs / vulnerabilidades / code smells | 0 / 0 / 0 | 0 / 0 / 0 |
| Hotspots revisados | 0 % | 0 % (requiere revisión manual, ver sección 6) |

Cobertura según Vitest (70 archivos): líneas 97,11 %, sentencias 96,72 %, ramas 94,51 % y funciones 94,80 %. Muy por encima del 60 % que exige el enunciado.

---

## 2. Qué se ha cambiado

### Configuración (todas las rutas relativas a `frontend/`)

| Archivo | Cambio |
|---|---|
| `package.json` / `package-lock.json` | `@vitest/coverage-v8@5.0.3` como dependencia de desarrollo (misma versión que Vitest) y el script `test:coverage` (`vitest run --coverage`). |
| `vite.config.js` | Sección `test.coverage`: proveedor `v8`, informes `text-summary` (consola) y `lcov` (`coverage/lcov.info`, el que lee Sonar), `include` de `src/**/*.{js,jsx}` y `exclude` de `src/mocks/**`, `src/test/**` y los propios tests. Además `reportOnFailure: true` (sección 3.1). |
| `sonar-project.properties` | Quitadas `sonar.java.binaries` y `sonar.coverage.jacoco.xmlReportPaths` (eran de Java). Añadidas `sonar.sources=src`, `sonar.tests=src`, `sonar.test.inclusions=**/*.test.js,**/*.test.jsx,src/test/**` y `sonar.javascript.lcov.reportPaths=coverage/lcov.info`. El token y las exclusiones existentes no se han tocado. |
| `.gitignore` | Ignora la carpeta `coverage/`, que se genera en cada ejecución. |
| `eslint.config.js` | Ignora `coverage/`: el informe HTML de la cobertura incluye archivos JavaScript que ESLint revisaría y daría errores. |

### Tests (sección 3.2)

| Archivo | Tests cambiados |
|---|---|
| `src/features/auth/LoginFlow.test.jsx` | 7 |
| `src/pages/SellerProductFormPage.test.jsx` | 1 |
| `src/pages/AdminUsersPage.test.jsx` | 2 (HU19-CP06) |

---

## 3. Problemas encontrados y cómo se resolvieron

### 3.1 Tests inestables que impedían generar el informe

Al ejecutar todos los tests a la vez, algunos de `src/features/admin/SellerDetailDialog.test.jsx` (HU21, llegados con el PR #49) **fallan al azar**: no encuentran el diálogo a tiempo. Ejecutados solos pasan siempre (26 de 26). Medir la cobertura añade carga y hace que fallen más a menudo, incluso alguno de `Login` o `Register` por tiempo agotado.

Por defecto, Vitest **no genera el informe de cobertura si falla algún test**, así que no llegaba nada a Sonar.

**Decisión:** activar `reportOnFailure: true` en `vite.config.js`. El informe se genera aunque falle algún test inestable. No se ha tocado ningún test de la HU21. Consecuencia: cuando un test falla, las líneas que habría cubierto no cuentan, y la cobertura puede variar unas décimas entre ejecuciones.

**Pendiente:** estabilizar `SellerDetailDialog.test.jsx` (lo tiene que ver quien hizo la HU21).

### 3.2 Diez tests "sin comprobaciones" que ponían el quality gate en rojo

Al declarar los tests como tests (`sonar.tests`), Sonar empezó a aplicarles sus reglas de tests. Una de ellas, `javascript:S2699` ("Add at least one assertion to this test case"), es **bloqueante**, y el quality gate no admite ninguna incidencia bloqueante. El primer análisis con cobertura dio **Failed** por 10 incidencias de este tipo.

Los tests sí comprobaban lo que debían, pero el `expect` estaba dentro de funciones auxiliares (`esperarRuta` y `esperarFilas`), y Sonar solo mira el cuerpo del `it(...)`.

**Decisión:** en esos 10 tests, la última línea pasa a llevar el `expect` en el propio cuerpo del test. Comprueban **exactamente lo mismo**: solo cambia dónde está escrito.

```js
// antes
await esperarFilas(2)
// después
await waitFor(() => expect(filasDeDatos()).toHaveLength(2))
```

Las funciones auxiliares se mantienen, porque las usan muchos otros tests. Los nombres de los tests no cambian y la matriz de trazabilidad no necesita cambios: los dos de HU19-CP06 siguen en su fila, y los otros 8 no estaban en la matriz.

---

## 4. Cómo lanzar el análisis a partir de ahora

Desde `frontend/` y con el código de `develop`, porque cada análisis sustituye al anterior en el servidor:

```bash
npm install
npm run test:coverage   # genera coverage/lcov.info
npx @sonar/scan         # sube el análisis, lee sonar-project.properties
```

`npx @sonar/scan` es el scanner oficial de SonarSource distribuido por npm: no hace falta instalar `sonar-scanner` aparte. Es el equivalente a `./mvnw verify` seguido de `sonar-scanner` en el backend. Si no se ejecuta `test:coverage` antes, Sonar volverá a mostrar un 0 % de cobertura.

---

## 5. Cómo comprobarlo

```bash
npm run test:coverage   # 344 tests; los únicos que pueden fallar son los inestables de la sección 3.1
npm run lint            # sin errores
```

En SonarQube (`http://seralu4.esi.uclm.es:1521/dashboard?id=2026-g02-frontend`): quality gate **Passed**, cobertura alrededor del 96 % y 0 incidencias.

---

## 6. Limitaciones y trabajo pendiente

- **Tests inestables de la HU21** (`SellerDetailDialog.test.jsx`): ver sección 3.1.
- **11 security hotspots sin revisar** ("Hotspots Reviewed 0 %"). No se arreglan con configuración: alguien del equipo tiene que revisarlos en la web de SonarQube.
- **Token de SonarQube** en `sonar-project.properties`: la incidencia está marcada como falso positivo en SonarQube, probablemente por el profesor, y se respeta esa decisión.
- **Archivos sin cubrir:** `main.jsx`, `App.jsx` y `theme/theme.js` tienen un 0 %. Son el arranque y la configuración de la app y ningún test los ejecuta, lo cual es normal.
- **CI:** la cobertura del frontend no se genera en GitHub Actions. Se podría añadir más adelante.

---

## 7. Uso de IA generativa

Este trabajo y este documento se hicieron con ayuda de un asistente de IA generativa, como exige el enunciado.

- **Herramienta:** Claude (modelo de Anthropic), mediante Claude Code, con la skill `matriz-trazabilidad` para comprobar la matriz.
- **Qué generó la IA:** el diagnóstico de la configuración, los cambios de configuración, el cambio de los 10 tests y este documento. Los análisis se lanzaron con `npx @sonar/scan` y los resultados se leyeron de la API de SonarQube con el token del archivo de configuración (solo en memoria).
- **Qué decidió el desarrollador:** hacer el cambio, usar `npx @sonar/scan`, activar `reportOnFailure` en lugar de tocar los tests inestables, hacer explícito el `expect` en los 10 tests sin cambiar lo que comprueban, respetar el falso positivo del token y no asociar el trabajo a una task de Azure Boards.
- **Cómo se comprobó:** los 3 archivos de test modificados pasan (64 de 64), ESLint sin errores, la cobertura se genera (97,11 % de líneas) y el análisis de SonarQube da quality gate **Passed** con 96,4 % de cobertura y 0 incidencias.
- **Qué NO se comprobó:** que los tests inestables de la HU21 pasen siempre (fallan al azar al ejecutar todos los tests a la vez, también sin cobertura).
- **Revisión humana:** pendiente de revisión por el equipo. Documentado por: Daniel.
