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
- **Arquitectura**: Cliente-Servidor + MVC + Hexagonal, con patrón Repository (ver sección Arquitectura)
- **Base de datos**: MongoDB (MongoDB Atlas, cloud)
- **Frontend**: React 19 + Vite 8, JavaScript puro (sin TypeScript)
- **Tests backend**: JUnit (enfoque TDD)
- **Calidad**: SonarQube y SonarLint
- **Gestión de proyecto**: SCRUM con Azure DevOps (Azure Boards)
- **Control de versiones**: Git
- **CI**: Azure Pipelines (aún no configurado)
- **IA de apoyo**: Claude Code (herramienta de IA agéntica del equipo)

## Estructura del proyecto

ESIBuy separa backend y frontend en carpetas independientes, sin tooling unificado (no es un monorepo, no hay Makefile en la raíz):

- `backend/` — Spring Boot (Maven, con wrapper `mvnw`/`mvnw.cmd` y `.mvn/wrapper`)
  - `src/main/java/es/uclm/esibuy/backend/` — código fuente (paquete base `es.uclm.esibuy.backend`)
  - `src/main/resources/` — configuración de la aplicación
  - `src/test/java/es/uclm/esibuy/backend/` — pruebas unitarias y de integración con JUnit
- `.claude/` — configuración de Claude Code (incluye `hooks-pendientes.md`)

**Estado actual**: el proyecto está **sin desarrollar**. El backend solo contiene la clase generada `BackendApplication.java`; aún no hay controladores, servicios, repositorios, modelos ni configuración de dominio. No existe todavía ninguna organización de paquetes que respetar: la estructura de la sección "Arquitectura" se acordará con el equipo antes de crear nada.
- `frontend/` — React + Vite
- `docs/` — documentación y requisitos (política de uso de IA, requisitos académicos, AGR, matriz de trazabilidad)

Se pueden añadir archivos `CLAUDE.md` dentro de `backend/` o `frontend/` para instrucciones específicas de cada módulo si el proyecto crece.

## Arquitectura

Combinamos tres estilos arquitectónicos **complementarios**, cada uno a un nivel distinto, más el patrón Repository:

1. **Cliente-Servidor** (nivel sistema): el navegador (frontend React) se comunica con el backend Spring Boot mediante peticiones **HTTP/HTTPS**. El frontend no accede nunca directamente a la base de datos.
2. **MVC** (capa de entrada de Spring): el **Controller** recibe la petición, valida la entrada, **delega** en la capa de aplicación y decide qué vista o JSON devolver. El controller no contiene lógica de negocio.
3. **Hexagonal / Puertos y Adaptadores** (interior del backend, por debajo del controller): el núcleo (dominio + casos de uso) no depende de frameworks ni de la infraestructura.
   - **Dominio**: entidades y reglas de negocio puras (pedido, producto, valoración, etc.), sin anotaciones de Spring ni de MongoDB.
   - **Aplicación**: casos de uso/servicios que orquestan el dominio. Definen **puertos de entrada** (lo que ofrece el núcleo) y **puertos de salida** (lo que necesita del exterior, como la persistencia).
   - **Adaptadores de entrada**: controllers REST (la parte "C" de MVC) que invocan los puertos de entrada.
   - **Adaptadores de salida**: implementaciones de los puertos de salida (persistencia en MongoDB, y en el futuro envío de notificaciones, pasarela de pago, etc.).
4. **Patrón Repository**: el acceso a datos se expone al núcleo mediante interfaces de repositorio (puertos de salida), de modo que dominio y casos de uso son **independientes de MongoDB y de Spring Data**. La implementación con Spring Data MongoDB vive solo en el adaptador de salida.

### Reglas de dependencia

- Las dependencias apuntan **siempre hacia dentro**: adaptadores → aplicación → dominio. El dominio no importa nada de `org.springframework.*` ni de Spring Data/MongoDB.
- Los controllers no llaman a repositorios directamente: siempre pasan por un caso de uso.
- Los casos de uso dependen de **interfaces** de repositorio, nunca de su implementación concreta (inyección de dependencias).
- No exponer entidades de dominio ni documentos de MongoDB directamente en la API: usar DTOs en el borde y mapear en los adaptadores.
- La seguridad (RBAC, 2FA/3FA, sesión) se aplica en el borde (adaptadores de entrada/configuración de Spring Security); las reglas de negocio que dependan del rol (p. ej. valorar solo si se ha comprado) viven en el dominio/aplicación.
- Consecuencia para TDD: dominio y casos de uso se prueban con JUnit **sin levantar Spring ni MongoDB**, sustituyendo los repositorios por dobles de prueba.

### Organización orientativa de paquetes (backend)

Esquema de referencia, **no acordado todavía**: no crees paquetes ni clases siguiéndolo sin confirmarlo antes con el equipo. Si más adelante existe otra organización en el repo, respétala y consulta antes de reestructurar.

```
src/main/java/es/uclm/esibuy/backend/
├── domain/          # entidades, value objects, reglas de negocio, excepciones de dominio
├── application/     # casos de uso, puertos de entrada (in) y de salida (out, repositorios)
└── infrastructure/
    ├── in/web/      # controllers REST, DTOs, validación de entrada
    ├── out/mongo/   # adaptadores de persistencia: documentos y repositorios Spring Data
    └── config/      # configuración de Spring (seguridad, beans, etc.)
```

## Comandos

### Backend (`backend/`)

Usar siempre el wrapper de Maven:

- `./mvnw spring-boot:run` — arrancar el servidor en local
- `./mvnw test` — ejecutar tests JUnit (deben pasar antes de cada commit)
- `./mvnw clean package` — compilar para producción
- Sonar (`./mvnw sonar:sonar`) y SonarLint: revisión de calidad antes de cada PR. Sonar **aún no está configurado en el repo**; no lo configures sin preguntar.

### Frontend (`frontend/`)

- `npm run dev` — servidor de desarrollo
- `npm run build` — build de producción
- `npm run lint` — ESLint (flat config), único linter configurado; no hay Prettier

## Notas de herramientas y configuración

- **MongoDB**: se usará MongoDB Atlas, pero la URI/credenciales todavía **no están configuradas** en `application.properties`. No asumas ni inventes una variable de entorno o convención de conexión: pregunta antes de añadir configuración de Mongo. Nunca subas credenciales al repositorio.
- No hay Checkstyle ni Spotless; el análisis de estilo/calidad se hará vía Sonar. No añadas linters locales duplicados.
- Frontend sin test runner (sin Vitest/Jest): no asumas que existen tests de frontend ni los añadas sin consultarlo.
- `@types/react*` en devDependencies son solo para el editor; no hay `tsconfig.json`.

## Roles y requisitos funcionales

Autenticación y autorización con el patrón **RBAC**. Roles:

- **Administrador**: gestiona usuarios, vendedores, categorías, incidencias y configuración general. Acceso en modo "solo lectura" a la información gestionada por vendedores. Módulo de gestión de usuarios exclusivo suyo: crear otros administradores, modificar, consultar, bloquear/desbloquear y eliminar usuarios.
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
- Cada caso de prueba debe quedar trazado en la **matriz de trazabilidad** Casos de Prueba ↔ Historias de Usuario
- Se generará un archivo de tests por cada clase existente y dentro de este archivo los tests que afecten directamente a dicha clase. En caso de considerar la necesidad de crear una clase de tests nueva pregutnar y no implementar directamente.

## Uso de IA generativa

- El uso de IA debe estar **documentado y justificado**: especificaciones, prompts, instrucciones o skills que guíen su uso (generación de componentes de interfaz, mejora de usabilidad y aspecto visual, generación de pruebas unitarias/de integración).
- Claude Code es la herramienta de IA agéntica que usa el equipo de desarrollo, en backend y frontend, incluida la propuesta o revisión de casos de prueba.
- Todo código o caso de prueba generado con IA debe ser **revisado, entendido, adaptado y probado** por el equipo antes de integrarse. La responsabilidad final es siempre humana; no la delegues en la IA.

## Convenciones de Git

- Flujo git-flow: las ramas de trabajo parten de `develop`; `develop` se integra en `main`. Los PRs apuntan a `develop`, no a `main`, salvo indicación contraria.
- Nombres de rama: `tipo/descripcion` (ej. `chore/configuracion-ia`), sin segmento de área.
- Mensajes de commit: `tipo(area): descripción AB#<id>`, donde `<id>` es el ID real del work item de Azure Boards. **Nunca** dejes el placeholder literal `AB#ID`; si no conoces el ID, pregúntalo antes de commitear.
- Antes de cada commit: `./mvnw test` en verde. Antes de cada PR: revisión de calidad (Sonar/SonarLint) y `npm run lint` si se toca el frontend.
- CI: Azure Pipelines (aún no configurado). No hay `.github/workflows/`.

## Flujo de trabajo con Claude

- Sin asunciones: consulta con el equipo antes de hacer cualquier cosa (ver "Principio rector").
- Antes de cualquier tarea, propón un plan y espera el OK del equipo; no ejecutes nada sin él.
- Una tarea a la vez; al terminar, indica qué cambiaste para que se revise y espera instrucciones antes del siguiente paso.
- Ante cualquier duda, pregunta. No inventes (URIs, IDs de work items, variables de entorno, dependencias…).
- Sigue SCRUM: el trabajo se organiza por sprints y se refleja en el panel de Azure DevOps.
- Respeta la arquitectura descrita arriba (Cliente-Servidor, MVC, Hexagonal y patrón Repository). Aplica otros patrones de ingeniería del software (además de RBAC) solo cuando aporten valor, y evita la sobreingeniería.

## Documentación del proyecto

- AGR (ISO/IEC 27001:2022).
- Principios de seguridad CCN-CERT BP/28 y guías de clase.
- Matriz de trazabilidad Casos de Prueba ↔ Historias de Usuario.
- Especificaciones, prompts, instrucciones o skills de uso de IA.

## Hooks pendientes

Hay un hook de lint de frontend preparado pero sin activar (a la espera de ver si Azure Pipelines cubre esa necesidad). Ver `.claude/hooks-pendientes.md`.
