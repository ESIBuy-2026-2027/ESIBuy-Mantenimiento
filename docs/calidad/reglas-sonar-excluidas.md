# Reglas de SonarQube retiradas del perfil y su justificación

Documento para saber qué reglas de SonarQube no se aplican en ESIBuy, por qué se retiraron y qué las compensa. Una regla retirada sin justificar es una excusa para ocultar defectos; una regla retirada y justificada es una decisión de calidad.

- **Reglas cubiertas:** los comentarios `TODO` y `FIXME` (`S1135` y `S1134`), `S6437` y `S2068`
- **Proyectos afectados:** `2026-g02-backend` y `2026-g02-frontend` en SonarQube (`http://seralu4.esi.uclm.es:1521/`)
- **Decisión:** equipo de desarrollo
- **Relacionado:** `frontend/docs/sonar-incidencias.md`, `frontend/docs/sonar-cobertura-implementacion.md` y la sección 8 de `frontend/docs/Contexto_frontend.md`

> **Qué no cambia.** Ninguna de estas reglas afecta a las exigencias de la práctica: **bugs, defectos y vulnerabilidades bloqueantes o críticos: 0**, cobertura ≥ 60 %, duplicación < 10 %. Cualquier otra regla del perfil sigue activa.

---

## 1. Resumen

| Regla | Qué detecta | Por qué se retira | Qué lo compensa |
|---|---|---|---|
| `S1135` · `S1134` (TODO y FIXME) | Comentarios con las marcas `TODO` o `FIXME` | Falsos positivos con el castellano y el trabajo pendiente se gestiona en Azure Boards | Azure Boards, `CLAUDE.md` y la regla de «no dejar código sin utilizar» |
| `S2068` | Una asignación a algo llamado `password` o similar, por si fuera una credencial escrita en el código | Marca nombres y mensajes, no credenciales; el dominio maneja contraseñas por diseño | Variables de entorno, revisión de código y las reglas de secretos que siguen activas |
| `S6437` | Credenciales escritas en el código o en la configuración | Mismo motivo que `S2068`: los datos de prueba y los nombres de campo no son secretos | Ídem |

---

## 2. `S1135` y `S1134` · TODO y FIXME

**Qué hacen.** Avisan de cada comentario que contiene `TODO` o `FIXME`, para que el trabajo pendiente no se quede olvidado en el código.

**Por qué se retiran.**

1. **Falsos positivos en castellano.** Sonar compara la palabra sin distinguir idioma, y «todo» es una palabra corriente en español («si todo es válido»). En el frontend ya dio una incidencia por un comentario así, que hubo que reformular solo para contentar a la regla (`sonar-incidencias.md`, apartado S1940 y S1135). Escribir los comentarios en el idioma del equipo no debería obligar a evitar palabras normales.
2. **El trabajo pendiente no se apunta en el código.** El proyecto sigue SCRUM con Azure Boards: lo pendiente es una *task* con responsable y estado, no un comentario que nadie ve en el tablero. Un `TODO` en el código no tiene dueño ni fecha.
3. **Ya hay una regla más estricta.** El `CLAUDE.md` exige **no dejar código sin utilizar** y que todo cambio lleve su test. Un `TODO` que escondiera código a medias incumpliría esas reglas aunque Sonar no lo marcara.

**Qué lo compensa.**

- Todo lo pendiente se registra como *task* en Azure Boards, enlazada a su historia de usuario.
- La revisión del PR (y `SonarLint` en el IDE antes de cada PR) detecta el código muerto y los métodos sin usar.
- Cuando hay una decisión abierta que no cabe en una task, se anota en la documentación con la etiqueta «PENDIENTE DE REVISAR CON EL EQUIPO» (como en la matriz de trazabilidad), que sí se ve en las reuniones.

**Riesgo residual.** Un `TODO` olvidado en el código no saltaría en el análisis. Se acepta porque la revisión humana del PR y el tablero cubren ese hueco; si el equipo vuelve a necesitar la regla, se reactiva en el perfil sin más.

---

## 3. `S2068` y `S6437` · credenciales escritas en el código

**Qué hacen.**

- `S2068` es un *security hotspot*: avisa cuando a una variable, campo o propiedad llamada `password`, `pwd`, `secret`… se le asigna un texto literal, porque podría ser una contraseña escrita en el código.
- `S6437` es la regla equivalente para credenciales en el código y en los archivos de configuración.

**Por qué se retiran.**

1. **Detectan nombres, no secretos.** ESIBuy es una aplicación con registro e inicio de sesión: `password`, `passwordHash`, `contraseña` aparecen en el dominio, los DTO, los formularios, los mensajes de error y los tests. Las reglas saltan por el nombre aunque el valor no sea una credencial.
2. **Ya se comprobó que eran falsos positivos.** Los dos hotspots `S2068` que quedaron abiertos en el frontend eran mensajes de error de validación que se muestran al usuario (`errores.password = 'El password no cumple los requisitos'` y `errors.password = 'Introduce tu contraseña.'`), no credenciales (`sonar-cobertura-implementacion.md`, sección 6). Además, el grupo no tiene permiso en SonarQube para marcarlos como *Safe*, así que quedaban siempre abiertos y ensuciaban la métrica «Hotspots Reviewed».
3. **Los tests usan contraseñas de ejemplo.** Los casos de la HU1 (política de contraseñas, hash, datos personales) necesitan literales como `Clave-1234` para probar el comportamiento. Son datos de prueba por diseño, no secretos reales.

**Qué lo compensa.** La seguridad real de los secretos no depende de estas reglas:

- **Los secretos reales no están en el repositorio.** La URI de MongoDB Atlas va en la variable de entorno `MONGODB_URI` y la clave de cifrado de los datos de pago en `ESIBUY_PAGOS_CLAVE_CIFRADO`; sin ellas la aplicación no arranca. Está escrito en el `CLAUDE.md` («nunca en el repositorio»).
- **Los tests no necesitan secretos reales.** `MongoTestcontainersConfig` usa MongoDB en Docker y registra una clave aleatoria, así que el CI tampoco los tiene.
- **Las contraseñas de usuario nunca se guardan en claro**: se almacenan con hash y la política de contraseñas se valida en el dominio (HU1, HU2).
- **El analizador de secretos de Sonar sigue activo.** Es el que detectó el token de SonarQube escrito en `sonar-project.properties` (`secrets:S6702`). Ese riesgo está documentado y aceptado: el servidor solo es accesible desde la red de la UCLM por VPN, y en un entorno real no se haría así.
- **Revisión de código**: ningún PR se integra sin que el equipo lo revise.

**Riesgo residual.** Una credencial real pegada por error en una variable llamada `password` ya no saltaría por estas dos reglas. Se acepta porque el analizador de secretos y la revisión del PR siguen cubriendo el caso, y porque con las reglas activas el ruido (hotspots que nadie puede cerrar) quitaba valor a las alertas reales.

---

## 4. Cómo reactivar una regla

1. Pedir al administrador del servidor SonarQube (el profesor) que active la regla en el perfil de calidad del proyecto, o hacerlo el equipo si se concede el permiso.
2. Lanzar el análisis desde `develop` (`./mvnw verify` y `sonar-scanner` en el backend; `npm run test:coverage` y `npx @sonar/scan` en el frontend).
3. Corregir o marcar las incidencias nuevas y actualizar este documento.

No se cambia la configuración de Sonar sin consultarlo con el equipo.
