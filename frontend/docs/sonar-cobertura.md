# Cobertura del frontend en SonarQube (propuesta)

Documento para explicar al equipo por qué SonarQube muestra el frontend con un 0 % de cobertura, qué habría que cambiar para que la mida y qué supone ese cambio. Es la **propuesta inicial**; la implementación y su resultado están en `docs/sonar-cobertura-implementacion.md`.

- **Fecha:** 8 de octubre de 2026
- **Análisis de partida:** proyecto `2026-g02-frontend`, código de `develop` en el commit `8677342`
- **Resultado de partida:** quality gate en **OK** (Passed), 0 bugs, 0 vulnerabilidades, 0 code smells, 1,2 % de duplicación, 0 % de hotspots revisados y **0,0 % de cobertura**

---

## 1. El problema

SonarQube muestra el frontend con un **0 % de cobertura** aunque tenemos 286 tests en Vitest y pasan todos.

No es que falten tests. **SonarQube no ejecuta los tests**: solo lee un informe de cobertura que tiene que existir antes de lanzar el análisis. En el backend ese informe lo genera JaCoCo con `./mvnw verify`. En el frontend ahora mismo no se genera ninguno, y además la configuración no le dice a Sonar dónde buscarlo.

---

## 2. Por qué pasa

1. **`frontend/sonar-project.properties` es una copia del del backend.** Tiene `sonar.java.binaries` y `sonar.coverage.jacoco.xmlReportPaths`, que son propiedades **de Java** y en el frontend no sirven para nada. Le falta la propiedad que usa Sonar para la cobertura de JavaScript: `sonar.javascript.lcov.reportPaths`.
2. **Vitest no genera cobertura.** `vite.config.js` no tiene sección `coverage` y no está instalado el proveedor de cobertura `@vitest/coverage-v8`.
3. **Sonar no sabe qué archivos son tests.** Como no está definida `sonar.tests`, los adivina por el nombre (el propio scanner lo avisa en el log). Funciona, pero es mejor declararlo.

---

## 3. Qué se cambiaría

Son **3 archivos, todos de configuración**. No se toca código de la aplicación.

| Archivo | Cambio |
|---|---|
| `package.json` | Añadir `@vitest/coverage-v8` como **dependencia de desarrollo** (no llega a producción) y un script `test:coverage` que ejecute `vitest run --coverage`. |
| `vite.config.js` | Añadir en `test` una sección `coverage` que genere el informe **LCOV** en `coverage/lcov.info` y excluya los mocks y la configuración de test. |
| `sonar-project.properties` | Quitar las dos propiedades de Java y añadir `sonar.sources=src`, `sonar.tests=src`, `sonar.test.inclusions=**/*.test.js,**/*.test.jsx` y `sonar.javascript.lcov.reportPaths=coverage/lcov.info`. |

La carpeta `coverage/` que genera Vitest no debe subirse al repositorio: habría que comprobar que está en el `.gitignore`.

---

## 4. Qué supone para el equipo

- **Un paso más antes de analizar:** primero `npm run test:coverage` y después el scanner. Es el equivalente a `./mvnw verify` en el backend.
- **Sonar mostrará la cobertura real del frontend.** Los archivos de test dejarán de contarse como código, así que también pueden cambiar las líneas de código, las incidencias y el % de duplicación.
- **Ojo: la cobertura puede salir por debajo del 60 %** que exige el enunciado, y entonces harían falta más tests. Es mejor descubrirlo ahora que al final, y los resultados del Sprint 1 no deben empeorar en sprints posteriores.
- **No cambia nada del funcionamiento de la aplicación**, ni del lint ni del build.
- **Más adelante (opcional):** añadir la cobertura del frontend al CI.

---

## 5. Otros puntos del análisis del 8 de octubre

**Token de SonarQube.** En `sonar-incidencias.md` quedó como incidencia bloqueante pendiente (`secrets:S6702`), porque el token está escrito en los dos `sonar-project.properties`. En el análisis del 8 de octubre aparece **marcada en SonarQube como "falso positivo"**, probablemente por el profesor. Se respeta esa decisión y no se cambia nada. Por eso salen 0 vulnerabilidades y la seguridad en A.

**Security hotspots.** El proyecto tiene **11 security hotspots** sin revisar (en la web sale "Hotspots Reviewed 0 %"). No cuentan como vulnerabilidades ni se arreglan cambiando la configuración: alguien del equipo tiene que revisarlos en la web de SonarQube y marcar cada uno como seguro o como a corregir.

---

## 6. Qué tiene que decidir el equipo

- ¿Hacemos el cambio de la cobertura (sección 3)?
- ¿Quién revisa los 11 security hotspots?

---

## 7. Cómo lanzar el análisis desde el frontend

No hace falta instalar el scanner: se puede usar el paquete npm oficial de SonarSource, que lee el mismo `sonar-project.properties`.

```bash
cd frontend
npm install
npx @sonar/scan
```

Cada análisis sustituye en el servidor al anterior (el servidor de la universidad no distingue ramas), así que conviene lanzarlo desde `develop`.

---

## 8. Uso de IA generativa

Este documento se hizo con ayuda de un asistente de IA generativa, como exige el enunciado.

- **Herramienta:** Claude (modelo de Anthropic), mediante Claude Code.
- **Qué generó la IA:** el diagnóstico de la configuración de Sonar del frontend, la propuesta de cambios y este documento. El análisis del 8 de octubre se lanzó con `npx @sonar/scan`, y los resultados se leyeron de la API de SonarQube con el token del archivo de configuración (solo en memoria, sin copiarlo a ningún otro sitio).
- **Qué NO se ha hecho:** ningún cambio en `package.json`, `vite.config.js` ni `sonar-project.properties`, ni en SonarQube aparte de subir el análisis. Es solo una propuesta.
- **Revisión humana:** pendiente de revisión por el equipo. Revisado por: _pendiente_.
