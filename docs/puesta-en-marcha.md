# Puesta en marcha en local

Guía para dejar ESIBuy funcionando en tu ordenador: qué instalar, qué configurar y cómo arrancar el backend y el frontend.

## 1. Programas

| Programa | Versión | Para qué | Comprobación |
|----------|---------|----------|--------------|
| Java JDK | **25** (p. ej. Eclipse Temurin 25) | Compilar y ejecutar el backend. El `pom.xml` exige el 25 y no arranca con otro. | `java -version` |
| Node.js | **22 LTS** (incluye npm) | Frontend. Es la versión del CI y de Render. | `node -v` y `npm -v` |
| Docker Desktop | Reciente | Los tests del backend levantan MongoDB en un contenedor (Testcontainers). Sin Docker abierto, `./mvnw test` falla. Para **arrancar** la aplicación no hace falta. | `docker --version`, `docker compose version` y `docker info` |
| Git | Reciente | Control de versiones. | `git --version` |
| Maven | **No hace falta** | El repositorio trae el Maven Wrapper (`mvnw` / `mvnw.cmd`), que descarga Maven 3.9 la primera vez. Si ya lo tienes instalado no molesta. | `mvn -version` (opcional) |

En Windows, `JAVA_HOME` debe apuntar al JDK 25 (Panel de control → Variables de entorno) y `java -version` debe mostrar `25`. Con Docker Desktop, `docker info` tiene que responder sin error: si dice que no puede conectar, Docker Desktop no está abierto o el motor no ha terminado de arrancar.

Comprobación completa en PowerShell:

```powershell
java -version
node -v
npm -v
git --version
docker --version
docker compose version
docker info
```

### Editor

VS Code o Antigravity con estas extensiones recomendadas, no imprescindibles:

- Extension Pack for Java
- Spring Boot Extension Pack
- ES7+ React/Redux/React-Native snippets
- ESLint

## 2. Servicios externos

No se instala nada de esto en local:

| Servicio | Qué necesitas | Variable |
|----------|---------------|----------|
| MongoDB Atlas | Acceso al clúster compartido y su URI de conexión (y tu IP permitida en *Network Access* de Atlas) | `MONGODB_URI` |
| Brevo | La SMTP key del proyecto (envío de códigos de verificación y recuperación) | `BREVO_SMTP_KEY` |
| VirusTotal | Una API key (cuenta gratuita en virustotal.com, puede ser la tuya) | `VIRUSTOTAL_API_KEY` |

## 3. Clonar e instalar dependencias

```powershell
git clone https://github.com/ESIBuy-2026-2027/ESIBuy-Mantenimiento.git
cd ESIBuy-Mantenimiento
git checkout develop

cd frontend
npm ci
cd ..
```

Las dependencias del backend las descarga el wrapper de Maven la primera vez que se compila.

## 4. Variables de entorno del backend

1. Crea el archivo `backend/.env` (ya está en `backend/.gitignore`, así que no se sube) con una línea `VARIABLE=valor` por cada variable de la tabla de abajo, sin comillas ni espacios alrededor del `=`. Por ejemplo, la estructura es:

   ```
   MONGODB_URI=<uri de Atlas>
   ESIBUY_TOTP_CLAVE_CIFRADO=<clave>
   ESIBUY_PAGOS_CLAVE_CIFRADO=<clave>
   ESIBUY_PASSWORD_PEPPER=<pepper>
   BREVO_SMTP_KEY=<smtp key>
   VIRUSTOTAL_API_KEY=<api key>
   ```

2. Los valores te los pasa el equipo **por un canal privado** (nunca por GitHub ni por el chat del grupo). Antes de cada commit, comprueba con `git status` que `.env` no aparece.

3. Al arrancar, Spring Boot lee `backend/.env` automáticamente (`spring.config.import` en `application.properties`). Si una variable también está definida en el entorno del sistema, gana la del sistema.

| Variable | Obligatoria | Qué es |
|----------|-------------|--------|
| `MONGODB_URI` | Sí | URI de MongoDB Atlas. La base `EsiBuy` ya la fija `application.properties`. |
| `ESIBUY_TOTP_CLAVE_CIFRADO` | Sí | Clave AES-256 en Base64 (32 bytes) del secreto TOTP. |
| `ESIBUY_PAGOS_CLAVE_CIFRADO` | Sí | Clave AES-256 en Base64 (32 bytes) de los datos de pago. Distinta de la anterior. |
| `ESIBUY_PASSWORD_PEPPER` | Sí | Pepper del hash de contraseñas, Base64 de 32 bytes. |
| `BREVO_SMTP_KEY` | Sí | SMTP key de Brevo. |
| `VIRUSTOTAL_API_KEY` | Sí | API key de VirusTotal. |
| `ESIBUY_MAIL_PUERTO` | No (587) | Puerto SMTP de Brevo. |
| `ESIBUY_SESION_INACTIVIDAD` | No (15m) | Caducidad de la sesión por inactividad. |
| `PORT` | No (8080) | Puerto del backend. |
| `SONAR_TOKEN` | No | Token personal de SonarQube, solo para los scripts `backend/scripts/sonar.ps1` y `frontend/scripts/sonar.ps1`. |

**Claves compartidas.** Si trabajáis contra el mismo clúster de Atlas, las dos claves AES y el pepper tienen que ser **las mismas para todo el equipo**: con otras, no se pueden comprobar las contraseñas de los usuarios ya creados ni descifrar los secretos TOTP y los métodos de pago guardados. Solo si usas una base de datos propia y vacía puedes generar las tuyas:

```bash
openssl rand -base64 32
```

(En Windows, `openssl` viene con Git Bash.)

## 5. Configuración local del frontend (mocks)

Por defecto el frontend en desarrollo usa **mocks** (MSW): `frontend/.env.development` tiene `VITE_USE_MOCKS=true` y las peticiones a la API no salen del navegador, así que se puede trabajar en la interfaz sin backend.

Para trabajar contra el **backend real**, crea `frontend/.env.development.local` (lo ignora Git por el patrón `*.local`; no modifiques `.env.development`):

```
VITE_USE_MOCKS=false
```

Reinicia `npm run dev` después de cambiarlo. El proxy de Vite envía `/api` a `http://localhost:8080`, así que no hay que tocar nada más.

## 6. Arrancar

En dos terminales:

```powershell
# Terminal 1: backend (http://localhost:8080)
cd backend
.\mvnw.cmd spring-boot:run      # en macOS/Linux: ./mvnw spring-boot:run
```

```powershell
# Terminal 2: frontend (http://localhost:5173)
cd frontend
npm run dev
```

El backend está listo cuando el log muestra `Started BackendApplication`. Comprobación: <http://localhost:8080/actuator/health> responde `{"status":"UP"}`.

### Desde VS Code / Antigravity (opcional)

Cada uno puede crearse su propio `.vscode/launch.json` local para arrancar backend, frontend o los análisis de Sonar desde el panel *Ejecutar y depurar*. La carpeta `.vscode/` está en `.gitignore`: no se sube al repositorio. Si lo usas, el backend debe leer `backend/.env` (`"envFile"`) y los scripts de Sonar son `backend/scripts/sonar.ps1` y `frontend/scripts/sonar.ps1`, que necesitan `SONAR_TOKEN` en `backend/.env` y la VPN de la UCLM.

## 7. Tests y comprobaciones

```powershell
# Backend (con Docker Desktop abierto)
cd backend
.\mvnw.cmd test        # tests
.\mvnw.cmd verify      # tests + cobertura JaCoCo (falla por debajo del 60 %)

# Frontend
cd frontend
npm run lint
npx vitest run
npm run build
```

Los tests del backend no usan Atlas ni el `.env`: levantan MongoDB en Docker y usan claves de prueba.

## 8. Problemas frecuentes

| Síntoma | Causa | Solución |
|---------|-------|----------|
| `Could not resolve placeholder 'MONGODB_URI'` (u otra variable) | Falta `backend/.env` o esa línea | Crea `backend/.env` con todas las variables obligatorias (sección 4). Arranca desde `backend/` o desde la raíz. |
| `Cannot find matching toolchain definitions ... {version=[25,26)}` | Maven se ejecuta con otro JDK | Instala el JDK 25, apunta `JAVA_HOME` a él y abre una terminal nueva. No hace falta `toolchains.xml`. |
| Los tests fallan con `Could not find a valid Docker environment` | Docker Desktop cerrado | Ábrelo y espera a que el motor esté en marcha. |
| Timeout al conectar con Atlas | Tu IP no está permitida | Pide que la añadan en *Network Access* de Atlas. |
| `El Pepper debe contener exactamente 32 bytes` o error con las claves AES | Valor mal copiado | Debe ser Base64 de 32 bytes, sin comillas ni espacios. |
| El frontend muestra datos de ejemplo aunque el backend esté arrancado | Mocks activos | Crea `frontend/.env.development.local` con `VITE_USE_MOCKS=false` y reinicia Vite. |
| No llega el correo de verificación | `BREVO_SMTP_KEY` incorrecta o puerto bloqueado | Revisa la key; si tu red bloquea el 587, prueba `ESIBUY_MAIL_PUERTO=2525`. |
