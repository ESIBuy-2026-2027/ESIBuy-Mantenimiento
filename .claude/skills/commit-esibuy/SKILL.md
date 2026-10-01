---
name: commit-esibuy
description: Crea un commit siguiendo la convención de ESIBuy (tipo(area): descripción AB#<id-de-Azure-Boards>). Úsalo en lugar de un `git commit` genérico en este repo.
disable-model-invocation: true
---

Crea un commit de git para el repositorio ESIBuy siguiendo su convención de mensajes: `tipo(area): descripción AB#<id>`.

Argumentos recibidos (opcional): $ARGUMENTS — puede incluir tipo, área, descripción y/o el ID de Azure Boards en cualquier orden o formato libre.

Pasos:

1. Ejecuta `git status` y `git diff --staged` (y `git diff` si no hay nada staged) para entender qué cambios se van a commitear.
2. Determina `tipo` (ej. `chore`, `feat`, `fix`) y `area` (ej. `front`, `back`, `all`) a partir de $ARGUMENTS o, si no están claros, de los archivos modificados (`backend/` → `back`, `frontend/` → `front`, ambos → `all`).
3. Determina el `<id>` de Azure Boards:
   - Si $ARGUMENTS incluye un número o ID, úsalo.
   - Si no, pregunta al usuario el ID del work item de Azure Boards antes de continuar. Nunca dejes el placeholder literal `AB#ID` ni inventes un número.
4. Redacta la descripción en minúsculas, en español, concisa, describiendo el "por qué" del cambio.
5. Construye el mensaje final: `tipo(area): descripción AB#<id>`.
6. Muestra el mensaje propuesto al usuario y confirma antes de ejecutar `git commit`.
7. Haz `git add` solo de los archivos relevantes (nunca `git add -A` a ciegas) y crea el commit con el mensaje confirmado, añadiendo la línea de atribución de Claude Code si aplica.
