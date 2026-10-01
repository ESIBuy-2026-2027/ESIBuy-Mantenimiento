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

## Estructura del proyecto

- `src/main/java/` — código fuente del backend (controladores, servicios, repositorios, modelos)
- `src/main/resources/` — configuración de la aplicación, vistas y recursos estáticos (HTML/CSS)
- `src/test/java/` — pruebas unitarias e de integración con JUnit

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
