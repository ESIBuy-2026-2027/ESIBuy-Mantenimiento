---
name: matriz-trazabilidad
description: Actualiza docs/calidad/matriz-trazabilidad.md cuando se añaden, modifican o eliminan tests. Úsalo siempre antes de commitear cambios en tests que se vayan a llevar a un PR, al preparar o abrir un PR que toque tests (backend/src/test o tests del frontend), y cuando cambie el estado de un PR que aparece en la matriz.
---

Mantén al día la matriz de trazabilidad de ESIBuy (`docs/calidad/matriz-trazabilidad.md`) para que refleje los tests reales del código. Es un requisito del CLAUDE.md: cada caso de prueba debe quedar trazado contra su historia de usuario.

Argumentos recibidos (opcional): $ARGUMENTS. Puede traer el número de PR, la rama o la HU afectada.

## Cuándo se usa

- Antes de abrir un PR (o de hacer el commit que lo prepara) que añada, modifique o elimine tests.
- Cuando se mergea o se cierra un PR que aparece en la matriz (para pasar sus tests de 🔍 a ✅, o quitarlos).
- Cuando el usuario lo pida directamente.

## Pasos

1. **Detectar los tests afectados (solo lectura).**
   - Compara la rama con `origin/develop` (`git fetch origin` y `git diff --name-status origin/develop...HEAD`) y quédate con los archivos de test.
   - En cada archivo, localiza los métodos `@Test` y su `@DisplayName`. El `@DisplayName` empieza por el ID del caso: `HU<n>-CP<nn>: <descripción>`.
   - Si un test **no tiene `@DisplayName`** o no lleva ID de caso, **no lo deduzcas**: pregunta al usuario a qué caso corresponde o si es un test técnico sin caso. Avisa de que, según el CLAUDE.md, le falta el `@DisplayName`.

2. **Actualizar la Tabla B** (CP ↔ test) de la HU correspondiente:
   - Test nuevo: añade una fila con caso, clase, método, capa (dominio, aplicación o infraestructura según el paquete), task (`AB#<id>`, de los commits o preguntando), origen (`PR #<n>`) y estado.
   - Test renombrado o movido: actualiza la fila, sin duplicarla.
   - Test eliminado: quita la fila y, si el motivo es una decisión del equipo, añade una nota en la Tabla A.
   - Tests técnicos sin caso (p. ej. `contextLoads`): van en la sección «Tests técnicos sin caso de prueba».

3. **Actualizar la Tabla A** (HU ↔ CP):
   - Recalcula la columna «Tests» contando las filas de la Tabla B de cada caso.
   - Actualiza el estado: ✅ si todos sus tests están en `develop` y pasan; 🔍 si alguno está en un PR abierto; ⏳ si no tiene tests; ⏸ si está fuera de alcance por decisión del equipo.
   - Si aparece una HU nueva, crea su sección con las dos tablas. Las descripciones de los casos salen de Azure Boards: pídelas al usuario, no las inventes.

4. **Comprobar el estado de los PR** que aparecen en la matriz (`gh pr view <n> --json state`): los mergeados pasan a ✅ y los cerrados sin merge se revisan con el usuario.

5. **Mostrar el diff de la matriz** al usuario y proponer que vaya en el mismo PR que los tests, con un commit como `docs(calidad): actualizar matriz de trazabilidad AB#<id>`. No commitees sin su OK.

## Reglas

- No inventes casos, descripciones, IDs de Azure ni números de PR.
- No cambies los tests: si les falta el `@DisplayName`, avisa y deja que el usuario decida cómo añadirlo.
- La matriz refleja el código: si la matriz y el código no coinciden, manda el código y se pregunta por la diferencia.
