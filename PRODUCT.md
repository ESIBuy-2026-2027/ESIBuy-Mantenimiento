# Product

<!-- impeccable:product-schema 1 -->

## Platform

web

## Users

Tres roles con el mismo peso en las decisiones de diseño:

- **Cliente** (Normal o Premium): busca, filtra y compra productos; gestiona carrito, pedidos, direcciones, métodos de pago, favoritos y valoraciones (solo de productos que ha comprado).
- **Vendedor**: gestiona su propio catálogo (alta, edición, activar/desactivar, precios, stock, ofertas) y los pedidos de sus productos; consulta estadísticas básicas.
- **Administrador**: gestiona usuarios (listar, buscar, consultar, crear, modificar, activar/desactivar), crea otros administradores y supervisa a los vendedores en modo solo lectura.

Los visitantes sin sesión ven la home y pueden registrarse (solo como cliente o vendedor) o iniciar sesión.

## Product Purpose

ESIBuy es un marketplace web que permite a usuarios registrados buscar, consultar, comprar y valorar productos en un entorno seguro, escalable y accesible desde cualquier navegador. Es la práctica académica integrada de un equipo de estudiantes (Gestión de Proyectos, Seguridad, Procesos de Ingeniería y Calidad del Software), desarrollada por sprints con revisiones periódicas de los profesores.

## Operating Context

- Trabajo en SCRUM con Azure Boards; cada pantalla nace de una historia de usuario con criterios de aceptación (ver `frontend/docs/Contexto_frontend.md`).
- El frontend habla con un backend Spring Boot por HTTP; mientras el backend no está listo, las pantallas se desarrollan contra mocks de MSW (`frontend/src/mocks`).
- Las zonas de administrador y vendedor son herramientas de trabajo: listados con buscador, filtros y paginación, formularios de alta y edición.

## Capabilities and Constraints

- Stack del frontend: React 19 + Vite, JavaScript sin TypeScript, React Router, Vitest + Testing Library, TDD.
- RBAC: cada rol solo accede a lo suyo. La barrera real es el backend; el frontend solo oculta la interfaz por experiencia de usuario.
- Login multipaso con 2FA/3FA. Los mensajes de error de acceso son genéricos y nunca revelan si falla el usuario, la contraseña o si la cuenta existe (excepción acordada: cuenta desactivada).
- Estados de usuario: pendiente, activo, bloqueado, desactivado (pendiente de confirmar con backend).
- Datos de pago: nunca se muestra, pide ni envía el número completo de tarjeta; solo tokens y últimos 4 dígitos.
- Responsive: adaptable a móvil y tablet, sin desbordamientos horizontales.
- RGPD: aceptación de la política de privacidad en el registro.
- Calidad (SonarQube): cero código muerto, duplicación por debajo del 10 %, complejidad controlada, cobertura mínima del 60 %.
- La interfaz actual está escrita en español; no se ha fijado como compromiso obligatorio.

## Brand Commitments

- **Nombre**: ESIBuy, escrito exactamente así.
- **Librería de interfaz**: toda la interfaz se construye con Fluent UI v9 (componentes y tokens), sin otra librería de componentes.
- El tema naranja actual (`frontend/src/theme/theme.js`) existe pero no es un compromiso de marca obligatorio.

## Evidence on Hand

- No hay logotipo, testimonios, clientes reales, cifras ni contenido de marketing. No deben inventarse.
- Los datos de las pantallas en desarrollo son ficticios y viven en los mocks de MSW.

## Product Principles

1. **Seguridad antes que comodidad**: ningún atajo de interfaz puede saltarse un factor de autenticación ni filtrar información sensible.
2. **Cada rol, su herramienta**: cliente, vendedor y administrador tienen el mismo peso; cada zona se diseña para el trabajo concreto de su rol.
3. **Trazable y evaluable**: cada pantalla responde a una historia de usuario con criterios de aceptación y casos de prueba trazados.
4. **Sin sobreingeniería**: se reutilizan los componentes existentes del equipo antes de crear otros nuevos.

## Accessibility & Inclusion

Sin requisito formal de los profesores ni del equipo; se siguen buenas prácticas generales (etiquetas accesibles, navegación por teclado). Queda como decisión abierta.
