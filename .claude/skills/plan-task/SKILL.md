---
name: plan-task
description: Genera el plan de trabajo de una task de Azure Boards en docs/planes/task-<id>.md, sin ejecutar nada. Úsalo cuando se pida un plan para una nueva task.
disable-model-invocation: true
---

Genera el plan para una task de ESIBuy y **déjalo solo como documento**. Esta skill no crea ramas, no escribe código ni tests, no ejecuta comandos que modifiquen nada y no commitea. El plan lo revisa el equipo, y la ejecución se pide aparte y de forma explícita.

Argumentos recibidos (opcional): $ARGUMENTS. Puede traer el ID de la task, una captura o el texto de la task, material de apoyo (diapos, apuntes, PDFs) e indicaciones libres (p. ej. «solo la fase rojo de TDD»).

## Por qué no se usa el modo plan de Claude Code

El modo plan está pensado para planificar y luego ejecutar: al aprobar el plan, Claude empieza a implementarlo. Aquí solo queremos el documento, así que esta skill escribe el plan directamente en un archivo y se para.

## Pasos

1. **Reunir la información (solo lectura).**
   - Lee la task: ID, título, historia de usuario padre y casos de prueba o criterios.
   - Lee el material de apoyo que se adjunte. Si es un PDF y no se puede leer directamente, extrae el texto en macOS con PDFKit, que es de solo lectura:
     `osascript -l JavaScript -e 'ObjC.import("PDFKit"); var d=$.PDFDocument.alloc.initWithURL($.NSURL.fileURLWithPath("<ruta>")); var o=""; for(var i=0;i<d.pageCount;i++){o+="\n=== PÁGINA "+(i+1)+" ===\n"+d.pageAtIndex(i).string.js;} o'`
   - Revisa el código y las ramas relacionadas (`git branch -a`, `git log --all --grep`) para saber qué existe ya. Si se indica que el trabajo es desde cero, no reutilices ramas antiguas: como mucho, menciónalas.
   - **Relación con otras tasks:** comprueba si otra task o PR ya cubre parte del trabajo (p. ej. una task de tests que ya creó los puertos que otra task tiene que implementar). Indica en el plan qué está hecho, qué falta y cómo reflejarlo en Azure Boards (enlazar el PR o los commits desde la task).
   - Respeta el CLAUDE.md: arquitectura hexagonal por módulos, convenciones de nombres, un archivo de test por clase, TDD y nomenclatura de ramas `feature/USx-Tx-descripcion`.

2. **Preguntar antes de decidir.** Recoge en una sola tanda de preguntas (con opciones y una recomendada) todo lo ambiguo: números de la rama (US = número de la HU, T = ID de la task), qué casos entran en esta fase, paquete, dobles de prueba, etc.
   - **Nombres que fija el test:** en TDD, el test decide los nombres de clases, métodos, getters y excepciones que todavía no existen. Antes de planificar cada ciclo, lista los nombres nuevos que aparecerán y pregunta los que no estén ya fijados por el código o el CLAUDE.md. Mantén el mismo término en todo el módulo (p. ej. si el campo es `correo`, el método del puerto es `existePorCorreo`, no `existePorEmail`).
   - Lo que el usuario diga que se comenta con el equipo **no se resuelve**: se anota en el plan como `PENDIENTE DE DECIDIR` o `PENDIENTE DE REVISAR CON EL EQUIPO`.
   - No inventes IDs, URIs, variables de entorno, endpoints ni dependencias.

3. **Escribir el plan** en `docs/planes/task-<id>.md`, una carpeta ignorada por Git. Si el archivo ya existe, pregunta antes de sobrescribirlo. Estructura:
   - **Contexto**: qué task es (ID, HU, AB#<id>), por qué se hace, qué alcance tiene esta fase y su relación con otras tasks.
   - **Paso 0 · Rama**: nombre `feature/USx-Tx-descripcion` y los comandos para crearla, **solo como texto**. Usa `--no-track` para que la rama nueva no quede vinculada a `develop`:
     ```bash
     git fetch origin
     git switch -c feature/USx-Tx-descripcion --no-track origin/develop
     ```
     La primera subida se hace con `git push -u origin <rama>` (o el alias `gpsup`).
   - **Decisiones pendientes con el equipo**: una tabla con la decisión y el lugar donde queda reflejada.
   - **Pasos del trabajo**: qué archivos se crean o modifican y en qué orden.
     - Si es una task con TDD, sigue el formato del ejercicio de clase (Red → Green → Refactor):
       1. **Tabla de ciclos**: orden, nombre del test, caso CP, tipo (normal, frontera o excepción) y si se espera rojo real. Un caso frontera puede pasar sin cambiar producción (como el ciclo 4 de las diapos); márcalo así, porque sigue siendo valioso: protege el límite que fijará el ciclo siguiente.
       2. **Archivo en FASE ROJO**: cada test con `// TODO` y `fail("Funcionalidad no implementada")`, sin referenciar clases que todavía no existen, para que compile y falle.
       3. **Por cada ciclo**, una tabla «qué pide el test para el verde»: qué clase, método o excepción hace falta y en qué capa y paquete va. El código de producción lo escribe el equipo, no Claude. En tests unitarios de servicios solo se crean **puertos** (interfaces); los **adaptadores** (MongoDB, hash, correo...) no se crean salvo que la task trate de ellos, y entonces se prueban con tests de integración.
       4. **Commits por ciclo**, con el formato `tipo(area): descripción AB#<id>`:
          - rojo: `test(back): ciclo N rojo - <comportamiento> AB#<id>`
          - verde: `feat(back): ciclo N verde - <comportamiento> AB#<id>`
          - refactor (si lo hay): `refactor(back): ciclo N - <mejora> AB#<id>`
          - ciclo sin rojo real (frontera): un único `test(back): ciclo N - <comportamiento> AB#<id>`
       5. No metas todas las casuísticas: solo las básicas que se deducen de la task, salvo que se pida otra cosa.
   - **Anexo de conceptos nuevos** (opcional): si el plan usa herramientas o técnicas que el usuario no conoce (p. ej. Mockito, `assertThrows`, records), añade una explicación breve con ejemplos.
   - **Verificación**:
     - El comando para comprobar el resultado, p. ej. `./mvnw clean test -Dtest=<Clase>Test`, y el resultado esperado en cada fase. Usa `clean`: sin él, Maven a veces no detecta los cambios y da una compilación por buena cuando no lo es.
     - Las clases de producción se crean desde `backend/src/main/java` (clic derecho → New Java File, con el nombre completo del paquete), no con el Quick Fix del IDE, que las crea en `src/test/java` y en el paquete del test.
     - Los commits de rojo dejan `./mvnw test` en rojo a propósito. La regla del CLAUDE.md de tener los tests en verde se exige al abrir el PR: indícalo en el plan para que se entienda en la revisión.

4. **Parar.** Di la ruta del plan, resume en pocas líneas las decisiones pendientes y espera instrucciones. No ejecutes ningún paso del plan aunque el usuario esté de acuerdo con él: la ejecución se pide aparte.
