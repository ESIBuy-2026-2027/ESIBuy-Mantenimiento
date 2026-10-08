---
name: pr-esibuy
description: Prepara y abre un PR hacia develop con la plantilla de ESIBuy (.github/pull_request_template.md), rellenada a partir de los commits, el plan y las decisiones de la task. Úsalo siempre que se pida abrir, crear o preparar un PR en este repo.
---

Prepara un PR de ESIBuy rellenando la plantilla del repositorio (`.github/pull_request_template.md`) con la información real de la rama. El PR apunta a `develop` salvo que el usuario diga otra cosa; nunca a `main` sin indicación expresa.

Argumentos recibidos (opcional): $ARGUMENTS. Puede traer el ID de la task, la rama o indicaciones libres.

## 1. Comprobaciones previas (solo lectura)

- **La rama ya tiene un PR**: `gh pr list --head <rama> --state all`.
  - Si hay uno **abierto**, los commits nuevos ya entran en él: no abras otro. Como mucho, actualiza su descripción.
  - Si hay uno **mergeado o cerrado**, los commits nuevos no están en ningún PR: hay que abrir uno nuevo. **No edites la descripción de un PR ya mergeado** para describir trabajo que no incluye.
- **Estado frente a `develop`**: `git fetch origin` y `git log --oneline HEAD..origin/develop`. Si `develop` ha avanzado, avisa y propón integrarlo antes, sobre todo si toca los mismos archivos.
- **Qué entra en el PR**: `git log --oneline --no-merges origin/develop..HEAD` y `git diff --name-status origin/develop...HEAD`. Comprueba que solo hay cambios de esta task; si se cuelan otros, avísalo.
- **Tests**: si el PR toca `backend/`, ejecuta `./mvnw verify` (tests y umbral de cobertura del 60 %). Los tests de integración con Testcontainers necesitan Docker Desktop abierto. Si toca `frontend/`, `npm run lint`. **No abras el PR con tests en rojo** (incluidos `fail(...)` de fases rojo sin terminar): avisa al usuario.
- **Matriz de trazabilidad**: si el PR añade, modifica o elimina tests, usa antes la skill `matriz-trazabilidad` para actualizarla en el mismo PR.

## 2. Reunir la información

- **Task y HU**: el `AB#<id>` de los commits y el nombre de la rama (`tipo/USx-Tx-descripcion`). Si no está claro, pregunta; no inventes IDs.
- **Plan de la task**: si existe `docs/planes/task-<id>.md`, úsalo para las decisiones, los ciclos y lo pendiente.
- **Decisiones**: las tomadas durante la task (alternativas descartadas, cambios de alcance, convenciones nuevas).

## 3. Redactar título y descripción

- **Título**: `tipo(area): descripción AB#<id>` (convención de commits del CLAUDE.md). Si el PR cubre varias tasks, incluye todos los `AB#`.
- **Descripción**: rellena las secciones de la plantilla, en este orden:
  1. **Resumen** (párrafo inicial): qué hace el PR, la HU y la task.
  2. **Cambios**: qué clases o archivos cambian y para qué, agrupados por capa si ayuda.
  3. **Decisiones** (si las hay): qué se decidió y por qué. Si hubo un cambio de alcance, explícalo aquí.
  4. **Historial TDD** (si hubo TDD): cómo se organizan los commits de rojo y verde, y qué ciclos pasaron sin rojo real y por qué.
  5. **Pruebas**: el comando y el resultado real (`N tests, 0 fallos`), no uno supuesto.
  6. **Pendiente / Para otras tasks**: lo que queda fuera y lo que afecta a otros compañeros (clases compartidas, tasks nuevas que crear en Azure).
  7. **Uso de IA**: qué hizo la IA (planificar, escribir tests, explicar...) y qué hizo el equipo (código de producción, revisión, pruebas). Debe ser fiel a lo que pasó.
- Borra las secciones opcionales que no apliquen, y los comentarios `<!-- -->` de la plantilla.
- Termina la descripción con la línea de atribución de Claude Code.

## 4. Confirmar y abrir

1. Muestra al usuario el **título y la descripción completos** y espera su OK.
2. Sube la rama si hace falta (`git push -u origin <rama>`).
3. Abre el PR: `gh pr create --base develop --head <rama> --title "..." --body-file -`.
4. Da el enlace del PR y recuerda lo que queda fuera de GitHub: tareas de Azure Boards, avisos a otros compañeros, pasar la task a *Done* cuando se mergee.
