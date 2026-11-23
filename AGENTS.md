# ESIBuy

Plataforma web de comercio electrónico que permite a usuarios registrados buscar, consultar, comprar y valorar productos en un entorno seguro, escalable y accesible desde cualquier navegador.

## Stack

- Backend: Java 25 + Spring Boot 4.1.1 (Maven con wrapper `mvnw`), Spring Data MongoDB, Spring Security, Validation, Web MVC, Lombok
- Base de datos: MongoDB Atlas (cloud); los tests usan MongoDB en Docker (Testcontainers)
- Tests: JUnit (enfoque TDD) en backend; Vitest + React Testing Library + MSW en frontend
- Frontend: React 19 + Vite 8, JavaScript puro (sin TypeScript), con mocks de la API (MSW) activables con `VITE_USE_MOCKS`
- Calidad: SonarQube y SonarLint; CI en GitHub Actions; despliegue en Render
- Gestión de proyecto: SCRUM, con planificación de tareas en Azure DevOps
- Control de versiones: Git

## Puesta en marcha

Programas, variables de entorno, mocks del frontend y arranque: ver `docs/puesta-en-marcha.md`. Nunca se commitea `.env` ni se copian secretos a archivos versionados, issues, PR o chats.

Variables del backend (en `backend/.env`, que Spring Boot importa al arrancar con `spring.config.import`; las del sistema tienen prioridad):

- Obligatorias: `MONGODB_URI` (Atlas, base `EsiBuy`), `ESIBUY_TOTP_CLAVE_CIFRADO` y `ESIBUY_PAGOS_CLAVE_CIFRADO` (AES-256, Base64 de 32 bytes, distintas entre sí), `ESIBUY_PASSWORD_PEPPER` (Base64 de 32 bytes), `BREVO_SMTP_KEY`, `VIRUSTOTAL_API_KEY`.
- Opcionales: `ESIBUY_MAIL_PUERTO` (587; 2525 en Render), `ESIBUY_SESION_INACTIVIDAD` (15m), `PORT` (8080), `SONAR_TOKEN` (solo scripts de Sonar).
- Los tests no usan ninguna: `MongoTestcontainersConfig` registra valores de prueba y MongoDB en Docker.

## Comandos

Backend (`backend/`, siempre con el wrapper de Maven; `mvnw.cmd` en Windows):

- `./mvnw spring-boot:run` — arranca el servidor en local (lee `backend/.env`)
- `./mvnw test` — ejecuta los tests JUnit (deben pasar antes de cada commit; necesitan Docker en marcha)
- `./mvnw verify` — tests + cobertura JaCoCo (el build falla por debajo del 60 %)
- Sonar / SonarLint — revisa la calidad y el estilo del código (antes de cada PR)
- `./mvnw clean package` — compila para producción

Frontend (`frontend/`, React + Vite, tests con Vitest):

- `npm run lint` — ESLint
- `npx vitest run` — ejecuta los tests una vez. Algunos fallan al azar al ejecutar toda la batería (p. ej. `SellerDetailDialog.test.jsx`); si falla uno, ejecútalo solo antes de darlo por roto
- `npm run test:coverage` — tests + informe de cobertura LCOV en `coverage/lcov.info`
- Sonar del frontend: **primero** `npm run test:coverage` y **después** `npx @sonar/scan` desde `frontend/`. Sin el paso de cobertura, Sonar muestra un 0 %. Cada análisis sustituye al anterior en el servidor, así que se lanza desde `develop`. Detalle en `frontend/docs/sonar-cobertura-implementacion.md`

## Estructura del proyecto

- `backend/` — Spring Boot
  - `src/main/java/es/uclm/esibuy/backend/` — código fuente, organizado en módulos funcionales con arquitectura hexagonal (ver sección "Arquitectura del backend"): `carrito`, `categorias`, `deseos`, `direcciones`, `estadisticas`, `incidencias`, `metodospago`, `pedidos`, `preferencias`, `productos`, `usuarios`, `valoraciones` y `shared`
  - `src/main/resources/` — configuración de la aplicación (`application.properties`)
  - `src/test/java/es/uclm/esibuy/backend/` — pruebas unitarias y de integración con JUnit
- `frontend/` — React + Vite
- `docs/` — documentación (puesta en marcha, requisitos, calidad, seguridad, contratos de API, despliegue)

El proyecto es **heredado de otro equipo** y ya está desarrollado: estamos en fase de mantenimiento y evolución.

## Arquitectura del backend

El backend (Java + Spring, MongoDB) sigue una arquitectura **hexagonal (puertos y adaptadores) organizada por módulos funcionales**. Cada funcionalidad del sistema es un módulo bajo el paquete raíz `es.uclm.esibuy.backend` (p. ej. `usuarios` o `catalogo`), y cada módulo contiene su propio hexágono con tres capas: dominio, aplicación e infraestructura.

### Estructura de paquetes (backend)

```
es.uclm.esibuy.backend
├── usuarios
│   ├── domain
│   │   ├── model            Entidades, objetos de valor y enumerados
│   │   └── exception        Excepciones de negocio
│   ├── application
│   │   ├── port
│   │   │   ├── in           Casos de uso que el módulo ofrece (interfaces)
│   │   │   └── out          Lo que el módulo necesita del exterior (interfaces)
│   │   └── service          Implementación de los puertos de entrada
│   └── infrastructure
│       ├── adapter
│       │   ├── in.web       Controladores REST, DTO de petición y respuesta
│       │   └── out          Adaptadores de salida (persistence, email...)
│       └── config           Configuración de Spring propia del módulo
├── catalogo                 Misma estructura
└── shared                   Elementos transversales (seguridad, errores, config)
```

Los módulos concretos que existan más allá de los ejemplos (`usuarios`, `catalogo`) se acuerdan con el equipo antes de crearlos.

### Reglas de dependencia

Las dependencias apuntan **siempre hacia el interior del hexágono**:

- `domain` no depende de ninguna otra capa ni de ningún framework: **Java puro**, sin anotaciones de Spring, Spring Data ni MongoDB.
- `application` depende únicamente de `domain`. Define los puertos y orquesta los casos de uso. Las **únicas anotaciones de Spring permitidas son `@Service` y `@PreAuthorize`** (seguridad en la capa de negocio, con las constantes de `shared.seguridad`).
- `infrastructure` depende de `application` y de `domain`. Es la **única capa** que conoce Spring Web, Spring Security, MongoDB y cualquier servicio externo.
- Ninguna clase de `domain` o `application` puede importar nada de `infrastructure`.
- No exponer entidades de dominio ni documentos de MongoDB directamente en la API: usar DTOs en el borde y mapear en los adaptadores.
- La seguridad se aplica en dos niveles: en el borde (configuración de Spring Security en `shared`: rutas, sesión, CSRF, 2FA/3FA) y en la capa de negocio (`@PreAuthorize` en cada método de los servicios de aplicación, denegar por defecto; sin permiso, 403 con `ACCESS_DENIED`). Detalle en `CLAUDE.md` y `docs/seguridad/seguridad-capa-negocio.md`. Las reglas de negocio que dependan del rol (p. ej. valorar solo si se ha comprado) viven en el dominio/aplicación.

### Responsabilidad de cada capa

**Dominio.** Modelo y reglas de negocio: entidades, objetos de valor, enumerados y excepciones de negocio. Las invariantes se validan aquí (p. ej. precio mayor que cero o stock no negativo).

**Aplicación.** Puertos y servicios.
- **Puertos de entrada** (`port.in`): interfaces que agrupan los casos de uso de **un mismo actor con un mismo propósito**, p. ej. `AdministracionUsuariosUseCase` (crear, consultar, modificar, bloquear y eliminar usuarios). No se crea una interfaz por operación ni una única interfaz por módulo. Si un puerto mezcla actores o intenciones distintas, se divide.
- **Puertos de salida** (`port.out`): interfaces que expresan lo que el módulo necesita del exterior, agrupadas por recurso o servicio externo, p. ej. `UsuarioRepositoryPort`, `HashContrasenaPort` o `EnvioEmailPort`.
- **Servicios** (`service`): implementan los puertos de entrada, **uno por puerto**, y solo se comunican con el exterior a través de los puertos de salida.

**Infraestructura.** Adaptadores.
- **Adaptadores de entrada** (`adapter.in.web`): controladores REST. Reciben DTO, los traducen y llaman a un puerto de entrada. No contienen lógica de negocio ni acceden a repositorios.
- **Adaptadores de salida** (`adapter.out`): implementan los puertos de salida (persistencia en MongoDB, envío de correo, hash de contraseñas, etc.).
- La persistencia usa **clases propias anotadas con `@Document`**, distintas de las entidades de dominio, y un **mapper** que convierte entre ambas.

### Convenciones de nombres

| Elemento                   | Ejemplo                         |
|----------------------------|---------------------------------|
| Puerto de entrada          | `AdministracionUsuariosUseCase` |
| Puerto de salida           | `UsuarioRepositoryPort`         |
| Servicio de aplicación     | `AdministracionUsuariosService` |
| Controlador REST           | `UsuarioController`             |
| Adaptador de persistencia  | `UsuarioMongoAdapter`           |
| Documento de MongoDB       | `UsuarioDocument`               |
| Repositorio de Spring Data | `UsuarioMongoRepository`        |
| Mapper dominio-documento   | `UsuarioDocumentMapper`         |

### Comunicación entre módulos

- Un módulo solo puede usar de otro sus **puertos de entrada** (`port.in`).
- Un módulo **nunca** importa clases de `domain` ni de `infrastructure` de otro.
- Las referencias entre entidades de módulos distintos se hacen **por identificador**, no por referencia al objeto.
- Lo realmente transversal (configuración de Spring Security y RBAC, manejo global de errores, utilidades comunes) vive en `shared`.

### Pruebas por capa

- Dominio y servicios de aplicación se prueban con JUnit **sin levantar el contexto de Spring**, sustituyendo los puertos de salida por dobles de prueba.
- Las anotaciones `@PreAuthorize` se prueban con un contexto mínimo (sin MongoDB), `@EnableMethodSecurity` y `@WithMockUser`: rol correcto pasa, los demás reciben 403 y sin sesión se rechaza.
- Los adaptadores se prueban con **tests de integración**.
- Cada test lleva un `@DisplayName` que empieza por el ID de su caso de prueba (`HU<n>-CP<nn>: ...`) y se traza en `docs/calidad/matriz-trazabilidad.md` en el mismo PR. Un archivo de tests por clase.

### Reglas al generar o modificar código

1. Ubicar cada clase nueva en el módulo y la capa que le corresponde según esta sección.
2. No añadir anotaciones de framework al dominio.
3. No poner lógica de negocio en controladores ni en adaptadores.
4. Antes de crear un puerto de entrada nuevo, comprobar si el caso de uso encaja en uno existente del mismo actor y propósito.
5. Toda dependencia externa nueva (base de datos, correo, servicios de terceros) se introduce mediante un puerto de salida y su adaptador.
6. El código existente organizado por capas tradicionales (`controller`, `service`, `repository`, `model`) debe **reubicarse** en esta estructura, no ampliarse.

## Convenciones

- Aplicar el patrón RBAC (Role-Based Access Control) para la autenticación y autorización, con tres roles: Administrador, Vendedor y Cliente (Premium y Estándar).
- Desarrollo dirigido por pruebas (TDD): al menos el 50% de las funcionalidades implementadas deben desarrollarse siguiendo TDD.
- Toda entrada de usuario debe validarse antes de usarla, especialmente en formularios de registro, direcciones y métodos de pago.
- La información sensible (incluidos los datos de pago) no debe almacenarse en claro; debe gestionarse siguiendo buenas prácticas de seguridad y cifrado.
- Evitar código duplicado (debe mantenerse por debajo del 10%) y métodos con complejidad ciclomática excesiva; si existen, deben justificarse.
- No debe quedar código sin utilizar en la aplicación.
- Cada caso de prueba debe quedar trazado en la matriz de trazabilidad entre Casos de Prueba e Historias de Usuario.
- Todo código o caso de prueba generado con apoyo de IA (GitHub Copilot) debe ser revisado, entendido, adaptado y probado por el equipo antes de integrarse.

## No hagas

- No bajar la cobertura de pruebas por debajo del 60% del código total.
- No introducir ni dejar sin resolver defectos, bugs o vulnerabilidades de severidad bloqueante o crítica (deben ser 0).
- No empeorar en sprints posteriores los resultados de calidad alcanzados en el Sprint 1.
- No almacenar contraseñas ni datos de pago sin cifrar o en texto plano.
- No omitir el segundo y tercer factor de autenticación (2FA/3FA) en los flujos de acceso.
- No desplegar ni exponer la aplicación sin conexión HTTPS.
- No delegar en la IA (Copilot) la responsabilidad final sobre el código o las pruebas generadas: siempre debe haber revisión humana.

## Nomenclatura de ramas

- **Obligatorio**: `tipo/descripcion` (`feature/`, `chore/`…; ej. `chore/configuracion-ia`), sin segmento de área.
- **Recomendado (buena práctica, no obligatorio)**: tras el tipo, indicar la historia de usuario y la task de Azure Boards: `tipo/USx-Tx-descripcion`. Ej.: `feature/US12-T45-alta-producto-service`.
- **Recomendado (pendiente de acordar, no obligatorio)**: cada rama debería corresponder a una **task**, no a una historia de usuario completa.

## Commits, PR e integración continua

- Flujo git-flow: las ramas parten de `develop` y los PR apuntan a `develop`. Integrar `develop` en `main` despliega en Render (vía el pipeline de Sonar de Azure).
- Mensajes de commit: `tipo(area): descripción AB#<id>` con el ID real de Azure Boards (nunca el placeholder `AB#ID`). Las excepciones acordadas que van sin `AB#` están en `CLAUDE.md` (Convenciones de Git).
- Antes de cada commit, `./mvnw test` en verde; antes de cada PR, Sonar/SonarLint y `npm run lint` si se toca el frontend. Plantilla de PR en `.github/pull_request_template.md`.
- CI (`.github/workflows/ci.yml`): en cada push y PR a `develop` o `main`, `mvn -B verify` (tests y cobertura del 60 %) y lint y build del frontend.

## Trampas conocidas (entorno local)

- **JDK 25 obligatorio**: con otro JDK, Maven falla en `maven-toolchains-plugin` (`Cannot find matching toolchain definitions ... [25,26)`). Basta con `JAVA_HOME` apuntando al 25; no hace falta `toolchains.xml`. Maven no hace falta instalarlo: el wrapper lo descarga.
- **Docker solo para los tests**: los tests del backend levantan MongoDB con Testcontainers; sin Docker en marcha fallan. Arrancar la aplicación no lo necesita (usa Atlas).
- **Claves compartidas**: las claves AES (TOTP y pagos) y el pepper deben ser las mismas para todo el que use el clúster compartido; con otras no se pueden comprobar contraseñas ni descifrar TOTP o métodos de pago ya guardados.
- **Mocks del frontend**: `frontend/.env.development` activa MSW (`VITE_USE_MOCKS=true`). Para trabajar contra el backend real, cada uno crea `frontend/.env.development.local` con `VITE_USE_MOCKS=false` (ignorado por `*.local`); no se modifica `.env.development`. El proxy de Vite envía `/api` a `http://localhost:8080`.
- **Tests de Vitest inestables**: con toda la batería (y más si la máquina va cargada) fallan algunos al azar (`SellerDetailDialog`, `SellerProductFormPage`, `productValues`…); se repiten solos antes de darlos por rotos.
- **Sonar**: el servidor (`seralu4.esi.uclm.es`) solo es accesible por la VPN de la UCLM. Los scripts `backend/scripts/sonar.ps1` y `frontend/scripts/sonar.ps1` leen `SONAR_TOKEN` de `backend/.env`; el del backend no ejecuta `./mvnw verify` antes, así que sin ese paso previo el análisis sale sin cobertura.
- **Render**: `render.yaml` no declara `ESIBUY_PASSWORD_PEPPER`; el backend desplegado solo arranca si esa variable está puesta a mano en el panel de Render. Cambiar `render.yaml` se consulta antes.
- **Secretos en el historial**: el commit `ce5d4b4` (rama `feature/gestion_pedidos_vendedor`) subió un `.env` en la raíz con secretos reales; hay que darlos por expuestos y rotarlos. `backend/.gitignore` ignora `backend/.env`, pero un `.env` en la raíz no está ignorado.

## Flujo de trabajo

- Antes de una tarea no trivial, propón un plan y espera mi OK.
- Una tarea a la vez; al terminar, dime qué cambiaste para que lo revise.
- Si no estás seguro al 80%, pregunta. No inventes.
- Usa GitHub Copilot principalmente como apoyo en la parte frontend (componentes de interfaz, usabilidad) y para proponer o revisar pruebas unitarias/integración, documentando siempre su uso.
- Sigue la metodología SCRUM: organiza el trabajo por sprints y refleja las tareas en el panel de Azure DevOps.

## Documentación

- Análisis y Gestión de Riesgos (AGR) del sistema conforme a ISO/IEC 27001:2022.
- Principios de seguridad del documento CCN-CERT BP/28 y guías de buenas prácticas de clase.
- Matriz de trazabilidad entre Casos de Prueba e Historias de Usuario.
- Especificaciones, prompts, instrucciones o skills que documenten el uso de GitHub Copilot durante el desarrollo.
- Enunciado de la práctica: Gestión de Proyectos Software + Seguridad de Sistemas Software + Procesos de Ingeniería del Software + Calidad de Sistemas Software (2026-27).
