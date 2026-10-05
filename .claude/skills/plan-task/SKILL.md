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
   - Respeta el CLAUDE.md: arquitectura hexagonal por módulos, convenciones de nombres, un archivo de test por clase, TDD y nomenclatura de ramas `feature/USx-Tx-descripcion`.

2. **Preguntar antes de decidir.** Recoge en una sola tanda de preguntas (con opciones y una recomendada) todo lo ambiguo: números de la rama (US = número de la HU, T = ID de la task), qué casos entran en esta fase, nombres de clases, paquete, dobles de prueba, etc.
   - Lo que el usuario diga que se comenta con el equipo **no se resuelve**: se anota en el plan como `PENDIENTE DE DECIDIR` o `PENDIENTE DE REVISAR CON EL EQUIPO`.
   - No inventes IDs, URIs, variables de entorno, endpoints ni dependencias.

3. **Escribir el plan** en `docs/planes/task-<id>.md`, una carpeta ignorada por Git. Si el archivo ya existe, pregunta antes de sobrescribirlo. Estructura:
   - **Contexto**: qué task es (ID, HU, AB#<id>), por qué se hace y qué alcance tiene esta fase.
   - **Paso 0 · Rama**: nombre `feature/USx-Tx-descripcion` y los comandos para crearla desde `develop`, **solo como texto**.
   - **Decisiones pendientes con el equipo**: una tabla con la decisión y el lugar donde queda reflejada.
   - **Pasos del trabajo**: qué archivos se crean o modifican y en qué orden.
     - Si es una task de tests con TDD, sigue el formato del ejercicio de clase (Red → Green → Refactor). Primero, una tabla de tests básicos (orden de ciclo, nombre, caso CP, tipo: normal, frontera o excepción). Después, el archivo en **FASE ROJO**: cada test con `// TODO` y `fail("Funcionalidad no implementada")`, sin referenciar clases que todavía no existen, para que compile y falle.
     - No metas todas las casuísticas: solo las básicas que se deducen de la task, salvo que se pida otra cosa.
   - **Anexo de conceptos nuevos** (opcional): si el plan usa herramientas o técnicas que el usuario no conoce (p. ej. Mockito), añade una explicación breve con ejemplos.
   - **Verificación**: el comando para comprobar el resultado (p. ej. `./mvnw test -Dtest=<Clase>Test`) y el resultado que se espera.

4. **Parar.** Di la ruta del plan, resume en pocas líneas las decisiones pendientes y espera instrucciones. No ejecutes ningún paso del plan aunque el usuario esté de acuerdo con él: la ejecución se pide aparte.
