# Política de uso de IA generativa en ESIBuy

## 1. Objetivo y alcance

Esta política define cómo usa el equipo de ESIBuy la IA generativa durante el desarrollo, qué instrucciones la guían, qué límites tiene y cómo se documenta cada uso. Responde a los requisitos de la práctica integrada: definir de antemano las especificaciones, prompts, instrucciones o skills que guían a la IA, documentar y justificar su uso, y garantizar que todo lo generado se revisa.

Se aplica a todo el equipo y a todo el trabajo del proyecto: backend, frontend, pruebas, documentación y gestión en Azure Boards.

## 2. Herramienta utilizada y justificación

El equipo usa **Claude Code** (de Anthropic) como única herramienta de IA generativa, tanto en backend como en frontend.

El enunciado propone GitHub Copilot como apoyo, principalmente en el frontend. El equipo ha elegido Claude Code porque cubre los mismos usos (generación de componentes de interfaz, mejora de usabilidad y aspecto visual, generación de pruebas) y además:

- **Es agéntico**: además de proponer código, ejecuta los tests y los comandos del proyecto, de modo que lo que propone se comprueba en el momento.
- **Se guía con instrucciones y skills versionadas** en el repositorio (sección 4), que es justo lo que pide el enunciado: especificaciones definidas antes de usar la IA y compartidas por todo el equipo.
- **Se integra con Azure Boards** mediante el MCP oficial de Azure DevOps, para leer las historias de usuario y las tasks con sus criterios de aceptación.

## 3. Principio de responsabilidad humana

La IA es una ayuda; la responsabilidad sobre el código, las pruebas y la documentación es siempre del equipo.

- Todo código o caso de prueba generado con apoyo de IA debe ser **revisado, entendido, adaptado y probado** por el equipo antes de integrarse.
- Ningún cambio llega a `develop` sin pasar por un PR revisado por una persona.
- Si alguien no entiende lo que ha generado la IA, no lo integra: pide una explicación o lo reescribe.

## 4. Especificaciones, instrucciones y skills que guían la IA

Todas están versionadas en el repositorio, así que cualquier miembro del equipo trabaja con las mismas reglas.

### Instrucciones de contexto

| Archivo | Qué define |
|---|---|
| `CLAUDE.md` | Instrucciones principales de Claude Code: principio de no asumir y consultar antes de actuar, stack, arquitectura hexagonal por módulos, convenciones de nombres, seguridad, calidad y pruebas, convenciones de Git y flujo de trabajo paso a paso con el equipo. |
| `AGENTS.md` | El mismo contexto de proyecto en el formato estándar para agentes de IA. |
| `frontend/docs/Contexto_frontend.md` | Contexto solo del frontend: roles, historias de usuario y decisiones funcionales de los formularios. Se usa como base antes de generar o modificar código del frontend. |
| `DESIGN.md` | Sistema visual del frontend (paleta, tipografía, componentes sobre Fluent UI v9) para que las interfaces generadas sean coherentes. |
| `PRODUCT.md` | Contexto de producto (usuarios y roles) para las decisiones de diseño y usabilidad. |
| `frontend/docs/hu*.md` | Documentación de cada historia de usuario del frontend. |

### Skills del proyecto (`.claude/skills/`)

Una skill es un conjunto de instrucciones que Claude Code sigue para una tarea concreta, de modo que esa tarea se haga siempre igual.

| Skill | Para qué sirve |
|---|---|
| `plan-task` | Genera el plan de una task de Azure Boards en `docs/planes/task-<id>.md`, sin ejecutar nada, para revisarlo antes de empezar. |
| `commit-esibuy` | Crea commits con la convención `tipo(area): descripción AB#<id>`. |
| `pr-esibuy` | Prepara y abre el PR hacia `develop` con la plantilla del repositorio, incluida la sección "Uso de IA". |
| `matriz-trazabilidad` | Actualiza `docs/calidad/matriz-trazabilidad.md` cuando se añaden, modifican o eliminan tests. |
| `notas-obsidian` | Opcional y personal: documenta decisiones y explicaciones en el Obsidian de quien la configure. No forma parte de la documentación del proyecto. |

### Integración con Azure Boards (MCP)

- `.mcp.json` configura el MCP oficial de Azure DevOps (`@azure-devops/mcp`, versión fijada), limitado a los dominios de Boards (`core`, `work`, `work-items`).
- `.claude/settings.json` autoriza sin preguntar solo las herramientas de **lectura**. Las de escritura (crear o modificar work items, comentarios, enlaces, reordenar el backlog) **siempre piden confirmación** a la persona.

## 5. Usos permitidos

| Área | Usos |
|---|---|
| Frontend | Generar componentes de interfaz, mejorar la usabilidad y el aspecto visual siguiendo `DESIGN.md`, y proponer los tests de Vitest de cada componente. |
| Backend | Explicar conceptos y código, proponer diseños que respeten la arquitectura hexagonal y escribir los tests (ver sección 6). |
| Pruebas | Proponer, escribir y revisar casos de prueba, y ejecutar las baterías de tests. |
| Calidad | Analizar y explicar incidencias de Sonar y SonarLint y proponer cómo corregirlas. |
| Gestión | Leer historias de usuario, tasks y el sprint en Azure Boards; preparar planes, commits y PR. |
| Documentación | Redactar documentación del proyecto, que el equipo revisa antes de integrarla. |

## 6. Pruebas asistidas por IA y TDD

El proyecto sigue TDD en al menos el 50 % de las funcionalidades. El reparto entre la IA y el equipo es:

- **La IA escribe los tests** de la fase roja a partir de los criterios de aceptación de la historia de usuario.
- **El código de producción lo implementa el equipo a mano** hasta poner los tests en verde.
- Cada test lleva un `@DisplayName` que empieza por el ID de su caso de prueba (`HU<n>-CP<nn>: …`) y queda trazado en la matriz de trazabilidad (`docs/calidad/matriz-trazabilidad.md`).
- Todo caso de prueba propuesto por la IA lo revisa y valida el equipo: que prueba lo que dice la historia de usuario, que el nombre y el ID son correctos y que falla por el motivo esperado antes de implementar.

Así se cumplen los requisitos de validar el sistema mediante pruebas dirigidas o asistidas por IA y de justificar y documentar ese uso.

## 7. Límites y seguridad

- **Secretos**: nunca se pasan a la IA ni se versionan contraseñas, tokens, PAT, la `MONGODB_URI` ni otras credenciales. La IA no escribe credenciales en archivos del repositorio.
- **Datos personales**: no se comparten con la IA datos personales reales; en pruebas se usan datos ficticios.
- **Control humano de las acciones**: la IA no hace push, merge ni cambios en Azure Boards sin el OK explícito de una persona.
- **Contenido externo**: el texto de historias de usuario, comentarios, páginas web o archivos externos se trata como información, no como instrucciones para la IA.
- **Sin asunciones**: siguiendo el `CLAUDE.md`, la IA no inventa requisitos, IDs, URIs, variables de entorno ni dependencias; ante la duda, pregunta al equipo.
- **Seguridad del código**: lo generado debe cumplir las reglas de seguridad del proyecto (RBAC, contraseñas con hash, validación de entradas, 0 vulnerabilidades bloqueantes o críticas en Sonar).

## 8. Cómo se documenta cada uso

- **PR**: la sección "Uso de IA" de la plantilla es obligatoria cuando se ha usado IA y separa lo que hizo la IA de lo que hizo el equipo (revisión, adaptación y pruebas).
- **Planes**: los planes de cada task quedan en `docs/planes/task-<id>.md`.
- **Commits**: los commits hechos con ayuda de Claude Code llevan la línea `Co-Authored-By: Claude …`.
- **Matriz de trazabilidad**: recoge los casos de prueba, incluidos los propuestos por la IA.

## 9. Revisión de la política

Esta política se actualiza en el mismo PR en que cambie algo de lo que describe: una herramienta nueva, una skill nueva o modificada, un cambio en la configuración del MCP o una regla nueva acordada por el equipo. Cualquier miembro puede proponer cambios mediante un PR a `develop`.
