# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Estructura del proyecto

ESIBuy separa backend y frontend en carpetas independientes, sin tooling unificado (no es un monorepo, no hay Makefile en la raíz):

- `backend/` — Spring Boot (Maven)
- `frontend/` — React + Vite
- `docs/` — documentación y requisitos del proyecto (política de uso de IA, requisitos académicos)

Se pueden añadir archivos `CLAUDE.md` dentro de `backend/` o `frontend/` para instrucciones específicas de cada módulo si el proyecto crece.

## Backend (`backend/`)

- Spring Boot 4.1.1, Java 25, Maven — usar siempre el wrapper: `./mvnw`
- Dependencias clave: Spring Data MongoDB, Spring Security, Validation, Web MVC, Lombok
- Comandos: `./mvnw spring-boot:run` (arrancar), `./mvnw test` (tests), `./mvnw clean package` (build)
- **MongoDB**: se usará MongoDB Atlas (cloud), pero la URI/credenciales todavía no están configuradas en `application.properties`. No asumas ni inventes una variable de entorno o convención de conexión — pregunta antes de añadir configuración de Mongo.
- No hay Checkstyle ni Spotless configurados; el análisis de estilo/calidad se hará vía Sonar (aún sin configurar en el repo), no añadir linters locales duplicados.

## Frontend (`frontend/`)

- React 19 + Vite 8, JavaScript puro (los `@types/react*` en devDependencies son solo para el editor; no hay TypeScript real, no hay `tsconfig.json`)
- Comandos: `npm run dev`, `npm run build`, `npm run lint`
- No hay test runner configurado (sin Vitest/Jest) — no asumas que existen tests de frontend
- ESLint (flat config) es el único linter configurado; no hay Prettier

## Convenciones de Git

- Flujo git-flow: las ramas de trabajo parten de `develop`; `develop` se integra en `main`. Los PRs deben apuntar a `develop`, no a `main`, salvo indicación contraria.
- Nombres de rama: `tipo/descripcion` (ej. `chore/configuracion-ia`), sin segmento de área.
- Mensajes de commit: `tipo(area): descripción AB#<id>`, donde `<id>` es el ID real del work item de Azure Boards. Nunca dejes el placeholder literal `AB#ID` — si no conoces el ID, pregúntalo antes de commitear.
- CI: el equipo usará Azure Pipelines (aún no configurado en el repo). No hay `.github/workflows/`.

## Hooks pendientes

Hay un hook de lint de frontend ya preparado pero sin activar (a la espera de
ver si Azure Pipelines cubre esa necesidad) — ver `.claude/hooks-pendientes.md`.
