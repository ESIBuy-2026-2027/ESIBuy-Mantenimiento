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

1. Se mergea un cambio en `main` (los PR siguen yendo a `develop`; `develop` se integra en `main`).
2. El CI de GitHub Actions (`.github/workflows/ci.yml`) se ejecuta sobre ese commit.
3. Con `autoDeployTrigger: checksPass`, Render espera a que pasen **todos** los checks de GitHub del commit (cuentan como pasados los que terminan en *success*, *neutral* o *skipped*) y entonces despliega. Si alguno falla, o no detecta ningún check, no despliega.
4. Cada servicio se despliega solo si cambian sus rutas (`buildFilter`): `backend/**` el backend y `frontend/**` el frontend. Un cambio en `render.yaml` los despliega a los dos.

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
5. **Revisar la configuración de auto-deploy.** En cada servicio, *Settings → Auto-Deploy* debe mostrar *After CI Checks Pass* y la rama `main`.

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
- **Checks de otras herramientas.** Render espera a todos los checks de GitHub del commit. Si Azure Pipelines (o Sonar) informa a GitHub y su check falla o se queda pendiente, el despliegue se bloquea.
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
- Monorepos y `buildFilter`: https://render.com/docs/monorepo-support
- Auto-deploy: https://render.com/docs/deploys
- IP de salida: https://render.com/docs/outbound-ip-addresses
