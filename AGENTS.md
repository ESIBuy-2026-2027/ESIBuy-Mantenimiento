# ESIBuy

Plataforma web de comercio electrónico que permite a usuarios registrados buscar, consultar, comprar y valorar productos en un entorno seguro, escalable y accesible desde cualquier navegador.

## Stack

- Lenguaje: Java
- Framework / runtime: Spring Framework (Spring Boot) para el backend
- Base de datos: MongoDB
- Tests: JUnit (enfoque TDD)
- Frontend: HTML y CSS, con apoyo de GitHub Copilot o cualquier otra herramienta de IA generativa para generación de componentes de interfaz y mejora de usabilidad
- Gestión de proyecto: SCRUM, con planificación de tareas en Azure DevOps
- Control de versiones: Git

## Comandos

- `mvn spring-boot:run` — arranca el servidor en local
- `mvn test` — ejecuta los tests JUnit (deben pasar antes de cada commit)
- `mvn sonar:sonar` / SonarLint — revisa la calidad y el estilo del código (antes de cada PR)
- `mvn clean package` — compila para producción

Frontend (`frontend/`, React + Vite, tests con Vitest):

- `npm run lint` — ESLint
- `npx vitest run` — ejecuta los tests una vez. Algunos fallan al azar al ejecutar toda la batería (p. ej. `SellerDetailDialog.test.jsx`); si falla uno, ejecútalo solo antes de darlo por roto
- `npm run test:coverage` — tests + informe de cobertura LCOV en `coverage/lcov.info`
- Sonar del frontend: **primero** `npm run test:coverage` y **después** `npx @sonar/scan` desde `frontend/`. Sin el paso de cobertura, Sonar muestra un 0 %. Cada análisis sustituye al anterior en el servidor, así que se lanza desde `develop`. Detalle en `frontend/docs/sonar-cobertura-implementacion.md`

## Estructura del proyecto

- `src/main/java/` — código fuente del backend, organizado en módulos funcionales con arquitectura hexagonal (ver sección "Arquitectura del backend")
- `src/main/resources/` — configuración de la aplicación, vistas y recursos estáticos (HTML/CSS)
- `src/test/java/` — pruebas unitarias e de integración con JUnit

## Arquitectura del backend

El backend (Java + Spring, MongoDB) sigue una arquitectura **hexagonal (puertos y adaptadores) organizada por módulos funcionales**. Cada funcionalidad del sistema es un módulo bajo el paquete raíz `es.uclm.esibuy` (p. ej. `usuarios` o `catalogo`), y cada módulo contiene su propio hexágono con tres capas: dominio, aplicación e infraestructura.

### Estructura de paquetes (backend)

```
es.uclm.esibuy
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
