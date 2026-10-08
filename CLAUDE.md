# CLAUDE.md

## Descripción del proyecto

**ESIBuy** es una plataforma web de comercio electrónico que permite a usuarios registrados buscar, consultar, comprar y valorar productos en un entorno seguro, escalable y accesible desde cualquier navegador.

Es una práctica académica integrada de cuatro áreas: Gestión de Proyectos Software, Seguridad de Sistemas Software, Procesos de Ingeniería del Software y Calidad de Sistemas Software. Se desarrolla a lo largo del cuatrimestre con reuniones periódicas con los profesores.

Funcionalidad básica: administración de productos, gestión de pedidos, carrito de compra, productos favoritos / listas de deseos, valoraciones y seguimiento de envíos.

## Principio rector: no asumir, consultar y avanzar guiados

**No hagas ninguna asunción sobre nada.** Todo se consulta con el equipo de desarrollo **antes de hacer nada**, y el equipo guiará cada paso del trabajo.

- Antes de actuar, confirma con el equipo el alcance, el enfoque y las decisiones relevantes: no decidas por tu cuenta.
- Esto aplica a todo: requisitos, diseño y arquitectura, nombres, dependencias, configuración, estructura de paquetes, librerías, estilo de la interfaz, estrategia de pruebas, cambios de Git y documentación.
- Si algo es ambiguo, falta información o hay varias formas razonables de hacerlo, **pregunta y espera respuesta**; no rellenes huecos con suposiciones ni con valores "típicos".
- No inventes datos, IDs, URIs, variables de entorno, endpoints, versiones ni convenciones.
- No amplíes el alcance por iniciativa propia: no hagas cambios, refactors ni "mejoras" que no se hayan pedido ni acordado.
- Avanza **paso a paso**: propón, espera el visto bueno del equipo, ejecuta solo ese paso y reporta antes de continuar con el siguiente.
- Ante la duda, consultar siempre tiene prioridad sobre avanzar.

## Stack

- **Backend**: Java 25 + Spring Boot 4.1.1 (Maven), Spring Data MongoDB, Spring Security, Validation, Web MVC, Lombok
- **Arquitectura**: Cliente-Servidor + MVC + Hexagonal por módulos funcionales, con patrón Repository (ver sección Arquitectura)
- **Base de datos**: MongoDB (MongoDB Atlas, cloud)
- **Frontend**: React 19 + Vite 8, JavaScript puro (sin TypeScript)
- **Tests backend**: JUnit (enfoque TDD)
- **Calidad**: SonarQube y SonarLint
- **Gestión de proyecto**: SCRUM con Azure DevOps (Azure Boards)
- **Control de versiones**: Git
- **CI**: GitHub Actions (`.github/workflows/ci.yml`)
- **IA de apoyo**: Claude Code (herramienta de IA agéntica del equipo)

## Estructura del proyecto

ESIBuy separa backend y frontend en carpetas independientes, sin tooling unificado (no es un monorepo, no hay Makefile en la raíz):

- `backend/` — Spring Boot (Maven, con wrapper `mvnw`/`mvnw.cmd` y `.mvn/wrapper`)
  - `src/main/java/es/uclm/esibuy/backend/` — código fuente (paquete base `es.uclm.esibuy.backend`)
  - `src/main/resources/` — configuración de la aplicación
  - `src/test/java/es/uclm/esibuy/backend/` — pruebas unitarias y de integración con JUnit
- `.claude/` — configuración de Claude Code (incluye `hooks-pendientes.md`)

**Estado actual**: el proyecto está **sin desarrollar**. El backend solo contiene la clase generada `BackendApplication.java`; aún no hay controladores, servicios, repositorios, modelos ni configuración de dominio. La organización de paquetes ya está acordada: hexagonal por módulos funcionales (ver sección "Arquitectura").
- `frontend/` — React + Vite
- `docs/` — documentación y requisitos (política de uso de IA, requisitos académicos, AGR, matriz de trazabilidad)

Se pueden añadir archivos `CLAUDE.md` dentro de `backend/` o `frontend/` para instrucciones específicas de cada módulo si el proyecto crece.

## Arquitectura

Combinamos tres estilos arquitectónicos **complementarios**, cada uno a un nivel distinto, más el patrón Repository:

1. **Cliente-Servidor** (nivel sistema): el navegador (frontend React) se comunica con el backend Spring Boot mediante peticiones **HTTP/HTTPS**. El frontend no accede nunca directamente a la base de datos.
2. **MVC** (capa de entrada de Spring): el **Controller** recibe la petición, valida la entrada, **delega** en un puerto de entrada y decide qué JSON devolver. El controller no contiene lógica de negocio.
3. **Hexagonal / Puertos y Adaptadores organizada por módulos funcionales** (interior del backend): cada funcionalidad del sistema es un módulo bajo el paquete raíz `es.uclm.esibuy.backend`, el de `BackendApplication`, para que el escaneo de componentes de Spring lo detecte sin configuración extra (p. ej. `usuarios`, `catalogo`), y **cada módulo contiene su propio hexágono** con tres capas: dominio, aplicación e infraestructura.
4. **Patrón Repository**: el acceso a datos se expone al núcleo mediante puertos de salida (p. ej. `UsuarioRepositoryPort`), de modo que dominio y aplicación son **independientes de MongoDB y de Spring Data**. La implementación con Spring Data MongoDB vive solo en el adaptador de salida.

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
- `application` depende únicamente de `domain`. Define los puertos y orquesta los casos de uso. La **única anotación de Spring permitida es `@Service`**.
- `infrastructure` depende de `application` y de `domain`. Es la **única capa** que conoce Spring Web, Spring Security, MongoDB y cualquier servicio externo.
- Ninguna clase de `domain` o `application` puede importar nada de `infrastructure`.
- No exponer entidades de dominio ni documentos de MongoDB directamente en la API: usar DTOs en el borde y mapear en los adaptadores.
- La seguridad (RBAC, 2FA/3FA, sesión) se aplica en el borde (configuración de Spring Security en `shared` / adaptadores de entrada); las reglas de negocio que dependan del rol (p. ej. valorar solo si se ha comprado) viven en el dominio/aplicación.

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

**Idioma de los nombres** (acordado en clase): los campos, parámetros y componentes de records para el correo y la contraseña van **en inglés**: `email`, `password` y derivados (`passwordHash`, `getEmail()`, `getPasswordHash()`). Las clases, métodos, puertos y excepciones siguen **en español** (`buscarPorCorreo`, `existePorCorreo`, `HashContrasenaPort`, `ContrasenaInvalidaException`). Los getters siguen la convención de Java: `get` + nombre del campo con la primera letra en mayúscula (`getPasswordHash()`, no `getpasswordHash()`).

### Comunicación entre módulos

- Un módulo solo puede usar de otro sus **puertos de entrada** (`port.in`).
- Un módulo **nunca** importa clases de `domain` ni de `infrastructure` de otro.
- Las referencias entre entidades de módulos distintos se hacen **por identificador**, no por referencia al objeto.
- Lo realmente transversal (configuración de Spring Security y RBAC, manejo global de errores, utilidades comunes) vive en `shared`.

### Pruebas por capa

- Dominio y servicios de aplicación se prueban con JUnit **sin levantar el contexto de Spring**, sustituyendo los puertos de salida por dobles de prueba.
- Los adaptadores se prueban con **tests de integración**.

### Reglas al generar o modificar código

1. Ubicar cada clase nueva en el módulo y la capa que le corresponde según esta sección.
2. No añadir anotaciones de framework al dominio.
3. No poner lógica de negocio en controladores ni en adaptadores.
4. Antes de crear un puerto de entrada nuevo, comprobar si el caso de uso encaja en uno existente del mismo actor y propósito.
5. Toda dependencia externa nueva (base de datos, correo, servicios de terceros) se introduce mediante un puerto de salida y su adaptador.
6. El código existente organizado por capas tradicionales (`controller`, `service`, `repository`, `model`) debe **reubicarse** en esta estructura, no ampliarse.

## Comandos

### Backend (`backend/`)

Usar siempre el wrapper de Maven:

- `./mvnw spring-boot:run` — arrancar el servidor en local
- `./mvnw test` — ejecutar tests JUnit (deben pasar antes de cada commit)
- `./mvnw verify` — tests + informe de cobertura de JaCoCo (`target/site/jacoco/index.html`) + comprobación del umbral: el build falla si la cobertura de líneas baja del 60 %
- `./mvnw clean package` — compilar para producción
- Sonar y SonarLint: revisión de calidad antes de cada PR. El análisis se lanza con `sonar-scanner` desde `backend/` (configurado en `backend/sonar-project.properties`), después de `./mvnw verify` para que incluya la cobertura. No cambies la configuración de Sonar sin preguntar.

### Frontend (`frontend/`)

- `npm run dev` — servidor de desarrollo
- `npm run build` — build de producción
- `npm run lint` — ESLint (flat config), único linter configurado; no hay Prettier
- `npm test` — tests de Vitest (React Testing Library + MSW) en modo watch; `npx vitest run` para una sola ejecución
- `npm run test:coverage` — tests + informe de cobertura LCOV en `coverage/lcov.info` (lo genera aunque falle algún test inestable, por `reportOnFailure`)
- Sonar del frontend: **primero** `npm run test:coverage` y **después** `npx @sonar/scan` desde `frontend/` (lee `frontend/sonar-project.properties`; no hace falta instalar `sonar-scanner`). Sin el paso de cobertura, Sonar muestra un 0 %. Cada análisis sustituye al anterior en el servidor (no distingue ramas), así que lánzalo desde `develop` o desde una rama con el mismo código. Detalle en `frontend/docs/sonar-cobertura-implementacion.md`.

## Notas de herramientas y configuración

- **MongoDB**: MongoDB Atlas (clúster `cluster0`, base `EsiBuy`). La conexión se configura en `application.properties` con `spring.mongodb.uri=${MONGODB_URI}` y `spring.mongodb.database=EsiBuy` (en Spring Boot 4 la propiedad es `spring.mongodb.uri`, no `spring.data.mongodb.uri`; y la base se llama `EsiBuy`, con esas mayúsculas, porque MongoDB no admite dos bases que solo difieran en mayúsculas). La URI con credenciales solo existe en la variable de entorno `MONGODB_URI` de cada desarrollador: **nunca** en el repositorio. Sin esa variable la aplicación no arranca. Los tests no usan Atlas: los que arrancan Spring importan `MongoTestcontainersConfig` (MongoDB en Docker) y el CI no tiene `MONGODB_URI`. Cualquier otro cambio en la configuración de Mongo se consulta antes.
- No hay Checkstyle ni Spotless; el análisis de estilo/calidad se hará vía Sonar. No añadas linters locales duplicados.
- Frontend con Vitest (React Testing Library y MSW para simular la API; tests junto al código, `*.test.js(x)`). Algunos tests fallan al azar al ejecutar toda la batería a la vez (p. ej. `SellerDetailDialog.test.jsx`); si falla uno, ejecútalo solo antes de darlo por roto.
- `@types/react*` en devDependencies son solo para el editor; no hay `tsconfig.json`.

## Roles y requisitos funcionales

Autenticación y autorización con el patrón **RBAC**. Roles:

- **Administrador**: gestiona usuarios, vendedores, categorías, incidencias y configuración general. Acceso en modo "solo lectura" a la información gestionada por vendedores. Módulo de gestión de usuarios exclusivo suyo: crear otros administradores, modificar, consultar, desactivar/activar y eliminar usuarios.
- **Vendedor**: crea, modifica, consulta, activa/desactiva y elimina productos; gestiona precios, stock, disponibilidad, ofertas y pedidos de sus productos; consulta estadísticas básicas de ventas, valoraciones y stock.
- **Cliente** (tipos **Premium** y **Estándar**): busca y filtra productos (categoría, precio, valoración, disponibilidad), consulta detalles, gestiona carrito, realiza pedidos, consulta historial, cancela pedidos cuando sea posible, sigue el estado del envío, gestiona direcciones y métodos de pago, guarda favoritos / listas de deseos y **valora productos solo si los ha comprado**.

Comunes a todos los usuarios: registro, inicio de sesión, gestión de perfil, direcciones, métodos de pago y preferencias.

### Mantenimiento (alcance posterior)

- Sistema de recomendaciones según preferencias, historial de compras y búsquedas.
- Alertas de bajada de precio o reposición de stock.
- Disponibilidad en dispositivos móviles.

## Seguridad

- RBAC en todos los endpoints; aplicar mínimo privilegio.
- Política de contraseñas segura; registro de usuario seguro; recuperación de contraseñas.
- Contraseñas con hash robusto; **nunca** en texto plano.
- Control y seguridad de sesión.
- **Segundo y tercer factor de autenticación (2FA/3FA)**: no omitirlos en los flujos de acceso.
- **HTTPS obligatorio**: no desplegar ni exponer la aplicación sin él.
- Información sensible protegida en BBDD; datos de pago **nunca en claro**, siguiendo buenas prácticas de seguridad y cifrado.
- Medidas de seguridad a nivel de BBDD que eviten el robo de información aunque fallen las capas superiores (p. ej. cifrado de campos sensibles, mínimo privilegio de usuarios de BBDD).
- Validar toda entrada de usuario antes de usarla, especialmente registro, direcciones y métodos de pago.
- Bugs, defectos y vulnerabilidades de severidad **bloqueante o crítica: 0**.
- Cumplir los principios de CCN-CERT BP/28 y las guías de buenas prácticas vistas en clase.
- Análisis y Gestión de Riesgos (AGR) conforme a ISO/IEC 27001:2022.

## Calidad y pruebas

- **TDD**: al menos el 50% de las funcionalidades se desarrollan con TDD (JUnit en backend). Escribe el test antes que la implementación cuando se trate de una funcionalidad nueva.
- Estrategia de pruebas con pruebas unitarias, y validación mediante pruebas dirigidas o asistidas por IA.
- **Cobertura ≥ 60%** del código total; no la bajes.
- **Código duplicado < 10%**.
- Evitar métodos con complejidad ciclomática excesiva; si existen, deben justificarse.
- **No dejar código sin utilizar.**
- Seguir las mejores prácticas de Java, HTML y CSS (y JSX en React); evitar defectos graves o críticos.
- Los resultados de calidad del **Sprint 1 no deben empeorar** en sprints posteriores.
- Cada caso de prueba debe quedar trazado en la **matriz de trazabilidad** Casos de Prueba ↔ Historias de Usuario, en `docs/calidad/matriz-trazabilidad.md`. Todo PR que añada, modifique o elimine tests la actualiza en el mismo PR (skill `matriz-trazabilidad`).
- **Cada test lleva un `@DisplayName` que empieza por el ID de su caso de prueba**: `HU<n>-CP<nn>: <descripción del comportamiento>`, p. ej. `@DisplayName("HU1-CP02: el hash no es la contraseña en claro")`. Es el vínculo entre el código y la matriz de trazabilidad, y hace que el caso aparezca en los informes de test y en el IDE (un comentario `// HU1-CP02` no aparece). Los tests técnicos que no validan una HU (p. ej. `contextLoads`) no llevan ID de caso y van en su propia sección de la matriz.
- Se generará un archivo de tests por cada clase existente y dentro de este archivo los tests que afecten directamente a dicha clase. En caso de considerar la necesidad de crear una clase de tests nueva pregutnar y no implementar directamente.

## Uso de IA generativa

- El uso de IA debe estar **documentado y justificado**: especificaciones, prompts, instrucciones o skills que guíen su uso (generación de componentes de interfaz, mejora de usabilidad y aspecto visual, generación de pruebas unitarias/de integración).
- Claude Code es la herramienta de IA agéntica que usa el equipo de desarrollo, en backend y frontend, incluida la propuesta o revisión de casos de prueba.
- Todo código o caso de prueba generado con IA debe ser **revisado, entendido, adaptado y probado** por el equipo antes de integrarse. La responsabilidad final es siempre humana; no la delegues en la IA.

## Convenciones de Git

- Flujo git-flow: las ramas de trabajo parten de `develop`; `develop` se integra en `main`. Los PRs apuntan a `develop`, no a `main`, salvo indicación contraria.
- Nomenclatura de ramas:
  - **Obligatorio**: `tipo/descripcion` (`feature/`, `chore/`…; ej. `chore/configuracion-ia`), sin segmento de área.
  - **Recomendado (buena práctica, no obligatorio)**: tras el tipo, indicar la historia de usuario y la task de Azure Boards: `tipo/USx-Tx-descripcion`. Ej.: `feature/US12-T45-alta-producto-service`.
  - **Recomendado (pendiente de acordar, no obligatorio)**: cada rama debería corresponder a una **task**, no a una historia de usuario completa.
- Mensajes de commit: `tipo(area): descripción AB#<id>`, donde `<id>` es el ID real del work item de Azure Boards. **Nunca** dejes el placeholder literal `AB#ID`; si no conoces el ID, pregúntalo antes de commitear.
- Antes de cada commit: `./mvnw test` en verde. Antes de cada PR: revisión de calidad (Sonar/SonarLint) y `npm run lint` si se toca el frontend.
- CI: GitHub Actions (`.github/workflows/ci.yml`). En cada push y PR a `develop` o `main` ejecuta `mvn -B verify` en el backend (tests y umbral de cobertura del 60 %) y el lint y el build del frontend.

## Flujo de trabajo con Claude

- Sin asunciones: consulta con el equipo antes de hacer cualquier cosa (ver "Principio rector").
- Antes de cualquier tarea, propón un plan y espera el OK del equipo; no ejecutes nada sin él.
- Una tarea a la vez; al terminar, indica qué cambiaste para que se revise y espera instrucciones antes del siguiente paso.
- Ante cualquier duda, pregunta. No inventes (URIs, IDs de work items, variables de entorno, dependencias…).
- Al preparar o abrir un PR que toque tests, usa la skill `matriz-trazabilidad` para actualizar la matriz en el mismo PR.
- Sigue SCRUM: el trabajo se organiza por sprints y se refleja en el panel de Azure DevOps.
- Respeta la arquitectura descrita arriba (Cliente-Servidor, MVC, Hexagonal y patrón Repository). Aplica otros patrones de ingeniería del software (además de RBAC) solo cuando aporten valor, y evita la sobreingeniería.

## Documentación del proyecto

- AGR (ISO/IEC 27001:2022).
- Principios de seguridad CCN-CERT BP/28 y guías de clase.
- Matriz de trazabilidad Casos de Prueba ↔ Historias de Usuario.
- Especificaciones, prompts, instrucciones o skills de uso de IA.

## Hooks pendientes

Hay un hook de lint de frontend preparado pero sin activar (a la espera de ver si Azure Pipelines cubre esa necesidad). Ver `.claude/hooks-pendientes.md`.
