# Hooks pendientes de activar

Este archivo guarda configuración de hooks ya preparada pero **no activa** todavía.
`.claude/settings.json` no soporta un flag `enabled: false` por hook — cualquier
entrada bajo `hooks` en ese archivo se dispara de inmediato. Por eso este bloque
se guarda aquí, fuera de `settings.json`, hasta que se decida activarlo.

## Lint de frontend tras cada edición

**Motivo para no activarlo aún:** el equipo va a usar Azure Pipelines para CI y
todavía no está claro si eso ya cubre esta necesidad (lint en cada push/PR) o si
conviene además un aviso inmediato en local vía hook. Pendiente de decidir.

**Qué hace:** tras cada `Write`/`Edit` sobre un archivo `.js`/`.jsx` dentro de
`frontend/`, ejecuta `npm run lint` en esa carpeta para detectar errores de
ESLint al instante.

**Para activarlo:** copia este bloque dentro de `hooks` en
`.claude/settings.json` (créalo si no existe) y recarga con `/hooks`:

```json
{
  "hooks": {
    "PostToolUse": [
      {
        "matcher": "Write|Edit",
        "hooks": [
          {
            "type": "command",
            "command": "jq -r '.tool_response.filePath // .tool_input.file_path' | { read -r f; case \"$f\" in */frontend/*.js|*/frontend/*.jsx) (cd \"$(dirname \"$f\")/../../frontend\" 2>/dev/null || cd frontend) && npm run lint;; esac; } 2>/dev/null || true"
          }
        ]
      }
    ]
  }
}
```

Si ya existe un hook `PostToolUse` con matcher `Write|Edit` en `settings.json`,
añade este comando al array `hooks` de esa misma entrada en lugar de duplicar
el bloque completo.
