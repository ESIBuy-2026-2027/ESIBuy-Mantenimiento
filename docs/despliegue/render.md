# Despliegue continuo en Render

ESIBuy se despliega en [Render](https://render.com) con el plan gratuito. La configuración está en `render.yaml` (un *Blueprint* de Render) en la raíz del repositorio. Este documento explica cómo funciona, qué pasos se hacen a mano en Render y en MongoDB Atlas, y qué riesgos y limitaciones tiene.

## 1. Cómo funciona

| Servicio | Tipo en Render | Qué es | URL |
|---|---|---|---|
| `esibuy` | Static Site | El frontend React ya compilado (`frontend/dist`), servido desde la CDN de Render. No ejecuta código en el servidor: React se ejecuta en el navegador. | `https://esibuy.onrender.com` |
| `esibuy-backend` | Web Service (Docker) | El backend Spring Boot, construido con `backend/Dockerfile`. | `https://esibuy-backend.onrender.com` |

Las URL son las previstas: si el nombre ya está cogido en Render, se le añade un sufijo (ver el paso 3 de la sección 3).

**Un solo origen.** El frontend llama a `/api/...` en relativo y el backend no tiene CORS; además, la cookie de sesión es `SameSite=Strict`. Por eso el usuario solo usa la URL del frontend, y el Static Site reenvía (*rewrite*) `/api/*` al backend. El resto de rutas sirve `index.html`, para que funcionen las rutas de React.

**Cuándo se despliega.**

El despliegue lo decide el pipeline de Sonar (`azure-pipelines-sonar.yml`), no Render: en `render.yaml` los dos servicios tienen `autoDeployTrigger: off`.

1. Se mergea un cambio en `main` (los PR siguen yendo a `develop`; `develop` se integra en `main`).
2. El pipeline de Sonar se lanza sobre ese commit y lo ejecuta un agente propio del equipo (pool `Default`), con la VPN de la UCLM y Docker.
3. **Compilar y testear**: `./mvnw -B verify` en el backend y lint, tests con cobertura y build en el frontend. Si algo falla, se para.
4. **Analizar con SonarQube**: analiza los dos proyectos con `-Dsonar.qualitygate.wait=true`. Si alguno no pasa el Quality Gate, el paso falla y el pipeline se para.
5. **Desplegar**: solo en `main` y solo si el análisis se ha hecho de verdad y ha pasado (resultado *Succeeded*). El job «Desplegar en Render» llama al *Deploy Hook* del backend y al del frontend, y Render construye y despliega los dos servicios desde ese commit.

Sin VPN, el análisis se salta con un aviso (*SucceededWithIssues*) y **no se despliega**. En `develop` el pipeline hace lo mismo menos el despliegue. El CI de GitHub Actions y «Pipeline tests» de Azure siguen validando cada PR y cada push, pero ya no deciden el despliegue.

**Comprobación de salud.** Render consulta `/actuator/health/liveness` (Actuator, PR 228). No manda tráfico a una versión nueva hasta que responde 200, y reinicia el servicio si deja de responder. Esa ruta no consulta MongoDB, y el indicador de correo está desactivado, para que un fallo de Atlas o del SMTP no provoque reinicios en bucle.

## 2. Variables de entorno del backend

| Variable | Valor | Dónde se define |
|---|---|---|
| `MONGODB_URI` | URI de Atlas con usuario y contraseña | Panel de Render (secreto) |
| `ESIBUY_PAGOS_CLAVE_CIFRADO` | Clave AES-256 en Base64 de los datos de pago | Panel de Render (secreto) |
| `ESIBUY_TOTP_CLAVE_CIFRADO` | Clave AES-256 en Base64 del secreto TOTP | Panel de Render (secreto) |
| `BREVO_SMTP_KEY` | SMTP key de Brevo | Panel de Render (secreto) |
| `VIRUSTOTAL_API_KEY` | API key de VirusTotal | Panel de Render (secreto) |
| `ESIBUY_MAIL_PUERTO` | `2525` | `render.yaml` |
| `JAVA_TOOL_OPTIONS` | `-XX:MaxRAMPercentage=70 -XX:+UseSerialGC` | `render.yaml` |
| `PORT` | La pone Render | Render, automático |

Los secretos van en `render.yaml` con `sync: false`: Render pide su valor al crear el Blueprint y **nunca** se escriben en el repositorio. Las claves de pagos y TOTP deben ser las mismas que usa la base de datos de Atlas: con otras, los datos cifrados existentes no se pueden descifrar.

El frontend solo tiene `NODE_VERSION=22`, la misma que el CI. **No** se define `VITE_USE_MOCKS`: los mocks de MSW solo se activan en desarrollo (`frontend/.env.development`).

## 3. Puesta en marcha (una sola vez)

1. **Crear el Blueprint.** En Render: *New → Blueprint*, conectar el repositorio de GitHub y elegir la rama `main`. Render lee `render.yaml` y propone crear los dos servicios.
2. **Cargar los secretos.** Render pide el valor de cada variable con `sync: false` (sección 2).
3. **Comprobar la URL del backend.** Cuando exista el servicio `esibuy-backend`, comprobar que su URL es `https://esibuy-backend.onrender.com`. Si Render le ha añadido un sufijo, cambiar el `destination` del rewrite de `/api/*` en `render.yaml` (en un PR, como cualquier cambio).
4. **Permitir a Render en MongoDB Atlas.** Las IP de salida de Render no son fijas por servicio: son bloques CIDR de la región (Fráncfort), compartidos por los servicios de esa región. Para verlos: en el panel del servicio `esibuy-backend`, *Connect → Outbound*. Añadir cada bloque en Atlas en *Network Access → IP Access List*. **No** usar `0.0.0.0/0`. Si Render cambia los bloques, hay que actualizar la lista.
5. **Revisar la configuración de auto-deploy.** En cada servicio, *Settings → Auto-Deploy* debe estar en *Off* (lo pone `render.yaml` al sincronizar el Blueprint) y la rama debe ser `main`.
6. **Crear los Deploy Hooks.** En cada servicio, *Settings → Deploy Hook* da una URL secreta que, al recibir un `POST`, despliega el servicio. Quien la tenga puede desplegar: no se comparte ni se escribe en el repositorio.
7. **Guardarlos en Azure Pipelines.** En el pipeline de Sonar, *Edit → Variables*, crear `RENDER_DEPLOY_HOOK_BACKEND` y `RENDER_DEPLOY_HOOK_FRONTEND` con la URL de cada servicio y marcar *Keep this value secret* (como `SQ_TOKEN`). En los logs salen como `***`.

## 4. Lista de comprobación del primer despliegue

- [ ] El despliegue del backend termina y `https://esibuy-backend.onrender.com/actuator/health/liveness` responde `{"status":"UP"}`.
- [ ] `https://esibuy.onrender.com` carga la página de inicio.
- [ ] Al recargar una ruta interna de React (no la raíz), se sigue viendo la aplicación y no un 404.
- [ ] **Cookies a través del rewrite** (riesgo abierto, sección 5): el login completo funciona en `https://esibuy.onrender.com`, incluidos el código por correo y el segundo factor; tras iniciar sesión, una página que exige sesión carga; una acción que cambia datos (con CSRF) funciona; el cierre de sesión funciona. En las herramientas del navegador, las cookies `JSESSIONID` y `XSRF-TOKEN` aparecen en el dominio del frontend.
- [ ] Llega el correo del código de verificación (SMTP por el puerto 2525).
- [ ] El análisis antivirus de una imagen de producto funciona (VirusTotal).
- [ ] Las respuestas del frontend llevan las cabeceras `X-Content-Type-Options`, `X-Frame-Options` y `Referrer-Policy`.

## 5. Riesgos y plan B

- **Cookies en el rewrite (riesgo abierto).** Render documenta que el destino de un rewrite puede ser una URL pública y que sirve su contenido sin redirigir al navegador, pero no documenta qué hace con las cabeceras `Set-Cookie`, con la cabecera `Host` ni con las cookies que envía el navegador. Si el login no funciona a través del rewrite, el plan B es **servir el `dist/` del frontend desde el propio Spring Boot** y desplegar un solo servicio. Elimina el problema de raíz, pero acopla los builds y hay que acordarlo con el equipo.
- **Orden de las reglas.** La documentación de Render no dice qué regla gana si varias coinciden. En `render.yaml`, `/api/*` va antes que el fallback `/*`. Además, Render sirve primero un archivo estático si existe en la ruta pedida.
- **El despliegue depende de un agente propio.** El pipeline de Sonar solo corre en un ordenador del equipo con el agente encendido, la VPN de la UCLM y Docker. Si nadie lo enciende, la ejecución espera en cola y no se despliega. Con varias fusiones seguidas se lanza una sola ejecución con el último estado (`batch: true`).
- **Un Quality Gate en ERROR bloquea el despliegue.** Es lo que se busca, pero también lo bloquea una incidencia nueva de Sonar en los tests (p. ej. un `S2699`): hay que arreglarla antes de poder desplegar.
- **Desplegar a mano.** Si hace falta desplegar sin pasar por el pipeline (p. ej. para volver a una versión anterior), se hace desde el panel de Render (*Manual Deploy* o *Rollback*).
- **`mockServiceWorker.js` en producción.** El archivo viene de `frontend/public/` y acaba en `dist/`. Es inerte, porque en producción nadie lo registra, pero se podría excluir del build más adelante.

### Para el AGR (ISO/IEC 27001:2022)

- **Acceso a Atlas por CIDR**: el acceso de red a la base de datos depende de bloques compartidos con otros clientes de Render en Fráncfort; la protección real es la contraseña de la URI. Alternativa de pago: IP dedicadas de Render.
- **Tránsito de datos**: el backend (Render, Fráncfort) y la base de datos (Atlas, AWS París, `eu-west-3`) se comunican por internet con TLS, siempre dentro de la UE. Render no tiene región en París.
- **Secretos en el historial de Git**: la SMTP key de Brevo y la clave del TOTP estuvieron en `application.properties` hasta el PR 225. Se mantienen los valores por ser un entorno controlado; hay que regenerarlos antes de exponer la aplicación públicamente.
- **HTTPS**: Render sirve los dos servicios con HTTPS y certificado gestionado. El backend confía en las cabeceras `X-Forwarded-*` del proxy de Render (`server.forward-headers-strategy=native`).

## 6. Limitaciones del plan gratuito

- El backend **se duerme a los 15 minutos** sin tráfico y tarda alrededor de **un minuto** en despertar. El frontend estático no se duerme. Para una demo, conviene abrir la aplicación un minuto antes.
- **750 horas al mes** de instancia por workspace. Un servicio siempre despierto gasta unas 744, así que no hay margen para un segundo Web Service gratuito.
- Render **puede reiniciar** el servicio en cualquier momento. La sesión de Spring está en memoria, así que un reinicio o un redespliegue cierra la sesión de todos los usuarios.
- El sistema de archivos es efímero. El backend guarda sus datos en Atlas, así que no le afecta.
- Salida bloqueada en los puertos 25, 465 y 587 (por eso el correo va por el 2525).
- Sin acceso por SSH ni consola, sin escalado a varias instancias y sin discos persistentes.

## 7. Referencias

- Plan gratuito: https://render.com/docs/free
- Especificación de `render.yaml`: https://render.com/docs/blueprint-spec (esquema JSON: https://render.com/schema/render.yaml.json)
- Redirects y rewrites: https://render.com/docs/redirects-rewrites
- Deploy Hooks: https://render.com/docs/deploy-hooks
- Auto-deploy: https://render.com/docs/deploys
- IP de salida: https://render.com/docs/outbound-ip-addresses
