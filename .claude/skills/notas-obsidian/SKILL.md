---
name: notas-obsidian
description: Documenta en Obsidian (vía Obsidian CLI) decisiones importantes y explicaciones para el aprendizaje personal del desarrollador. Úsalo SOLO si el CLAUDE.local.md del usuario configura esta skill (vault y carpeta), o si el usuario la invoca explícitamente con /notas-obsidian. Si no hay configuración, no la propongas.
---

Documenta en el vault de Obsidian del usuario las decisiones importantes y las explicaciones que pida, para su entendimiento propio. Las notas son personales: no forman parte de la documentación oficial del proyecto (`docs/`).

Argumentos recibidos (opcional): $ARGUMENTS — tema o contenido a anotar, en formato libre.

## Configuración

La configuración es personal y vive en el `CLAUDE.local.md` de cada desarrollador (no versionado). Debe indicar:

- **Vault**: nombre del vault de Obsidian (el que muestra `obsidian vaults`).
- **Carpeta base** dentro del vault, con las subcarpetas `Decisiones/` y `Conceptos/`.
- **Nota de registro** (opcional): nota donde se enlaza cada nota creada o ampliada, agrupada por fecha.
- **Disparo**: en qué situaciones proponer una nota de forma automática.

Si no hay configuración en el contexto y el usuario invoca la skill a mano, pregunta el vault y la carpeta antes de nada. No inventes rutas ni nombres de vault.

## Cuándo proponer una nota (si el CLAUDE.local.md activa el disparo automático)

- **Decisión importante**: arquitectura, diseño, elección de librerías o patrones, trade-offs, convenciones acordadas con el equipo, cambios de alcance.
- **Explicación**: el usuario pide que le expliques o documentes algo (un concepto del stack, por qué funciona algo, cómo encaja una pieza).

No propongas notas para cambios triviales ni para cosas que ya están en el repo (código, historial de git, CLAUDE.md).

## Flujo: proponer y confirmar

1. **Busca si ya existe** una nota del mismo tema para ampliarla en lugar de duplicarla:
   `obsidian vault="<vault>" search query="<tema>" path="<carpeta base>"`
2. **Muestra el borrador** al usuario en el chat: título, carpeta destino (`Decisiones/` o `Conceptos/`), si es nota nueva o ampliación, y el contenido completo.
3. **Espera un OK explícito**. Si pide cambios, ajusta y vuelve a mostrarlo. Nunca escribas en el vault sin confirmación.
4. **Escribe la nota**:
   - Nueva: `obsidian vault="<vault>" create path="<carpeta base>/<Decisiones|Conceptos>/<Título>.md" content="..."`
   - Ampliación: `obsidian vault="<vault>" append path="<ruta exacta>" content="..."`
   - Nunca uses `overwrite`.
5. **Enlázala en la nota de registro**, si está configurada:
   - Léela con `read`. Si no existe, créala con `create` y un encabezado `# <nombre de la nota>`.
   - Si no hay sección del día, añádela: `obsidian vault="<vault>" append path="<nota de registro>" content="\n## <YYYY-MM-DD>"`
   - Añade la entrada: `obsidian vault="<vault>" append path="<nota de registro>" content="- [[<Título>]] — <resumen de una línea>"`
   - No uses la nota diaria de Obsidian (`daily:*`): su ruta y formato dependen de la configuración de cada vault.
6. **Verifica y reporta** al usuario qué se ha escrito y dónde:
   - Una escritura es correcta si el CLI responde `Created:` (en `create`) o termina sin error (en `append`).
   - Obsidian tarda unos segundos en registrar un archivo recién creado: un `read` inmediato puede responder "not found" aunque el archivo exista. No lo trates como error ni repitas el `create`; espera unos segundos y vuelve a hacer `obsidian vault="<vault>" read path="<ruta exacta>"`.
   - Esto también aplica al paso 5: si acabas de crear la nota de registro, no la leas en ese momento para comprobar la sección del día.

Notas sobre el CLI: los valores con espacios van entre comillas; en `content` los saltos de línea se escriben como `\n`. Ignora en la salida los avisos de "Loading updated app package" y "installer is out of date". Obsidian debe estar abierto para que el CLI funcione.

## Plantillas

Títulos en español, descriptivos y sin prefijos de fecha. Enlaza con `[[...]]` los conceptos relacionados que ya existan en el vault (compruébalo con `search`).

### Decisión (`Decisiones/`)

```markdown
---
tipo: decision
fecha: <YYYY-MM-DD>
rama: <rama de git actual>
hu: <USx-Tx si se conoce, si no omitir>
tags: [esibuy, decision]
---

# <Título de la decisión>

## Contexto
<Qué problema había y por qué hubo que decidir.>

## Decisión
<Qué se decidió, en una o dos frases.>

## Alternativas consideradas
- **<Alternativa>**: <por qué no se eligió>

## Trade-offs
<Qué se gana y qué se pierde con la decisión.>

## Relacionado
- [[<Concepto o decisión relacionada>]]
```

### Concepto (`Conceptos/`)

```markdown
---
tipo: concepto
fecha: <YYYY-MM-DD>
tags: [esibuy, concepto]
---

# <Nombre del concepto>

## Qué es
<Explicación breve y clara, sin asumir conocimiento previo del stack.>

## Cómo se aplica en ESIBuy
<Dónde aparece en el proyecto, con rutas de archivo si ayudan.>

## Ejemplo
<Fragmento de código o caso concreto.>

## Relacionado
- [[<Otro concepto>]]
```

Si los datos (rama, HU, fecha) no se pueden obtener, omite el campo en lugar de inventarlo.
