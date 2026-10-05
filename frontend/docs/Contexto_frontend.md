# Contexto de proyecto — ESIBuy (Frontend) · v2

Documento de contexto **solo para el frontend** de ESIBuy. Úsalo como base de conocimiento antes de generar o modificar código. Sustituye a la versión anterior e incorpora: HU31 y HU32, y las decisiones funcionales de formularios (registro, alta de admin, productos, perfil).

---

## 1. Qué es ESIBuy

Marketplace (tienda online) desarrollado como Práctica Integrada de la asignatura ISO. Tres roles:

- **Administrador**: gestión global de usuarios y supervisión de vendedores (solo lectura sobre sus datos).
- **Vendedor**: gestión de su propio catálogo (alta, edición, activar/desactivar, estadísticas).
- **Cliente**: búsqueda y compra, carrito, pedidos, reseñas, lista de deseos, direcciones y métodos de pago. Puede ser **Normal** o **Premium**.

## 2. Stack tecnológico

- **Backend**: Java + Spring Boot (API REST), lo desarrolla otro miembro del equipo.
- **Base de datos**: MongoDB Atlas.
- **Frontend**: React (Vite, plantilla `react`, JavaScript sin TypeScript) + **Fluent UI**.
- **Testing**: Vitest + React Testing Library. Metodología **TDD** (al menos el 50 % de las funcionalidades).
- **Calidad**: SonarQube con perfil propio (sección 8).
- **Control de versiones**: Git + GitHub, Git Flow simplificado (sección 7).
- **Gestión ágil**: Scrum, tareas en Azure DevOps Boards.
- **IA generativa**: se permite GitHub Copilot; hay que documentar su uso y el equipo revisa y valida todo el código generado.

## 3. Requisitos no funcionales (afectan al frontend)

- **Seguridad**: login con 2FA (TOTP) + 3FA (código por email). RBAC: cada rol solo accede a lo suyo. **La barrera real es el backend**; el frontend solo oculta UI por experiencia de usuario.
- **Responsive**: adaptable a móvil y tablet (HU29), sin desbordamientos.
- **RGPD**: casilla de aceptación de la política de privacidad en el registro.
- **Datos de pago**: nunca se muestra, pide ni envía el número completo de tarjeta; solo tokens y últimos 4 dígitos.
- **Cero código muerto / duplicación ≤ 10 % / complejidad controlada** (SonarQube).

## 4. Modelo de dominio (relevante para el frontend)

- **Usuario** (abstracto) → Administrador / Vendedor / Cliente. Campos comunes: nombre, apellidos, email, rol, estado (pendiente / activo / bloqueado / desactivado), imagen de perfil.
- **Cliente**: tipo (Normal/Premium), fecha de nacimiento, DNI, teléfono, direcciones (varias, una principal), métodos de pago (tokenizados).
- **Vendedor**: nombre comercial (único), NIF/DNI, categoría principal de productos.
- **Administrador**: sede, fecha de incorporación.
- **Producto**: nombre, descripción, precio, stock, descuentos, visible/disponible, imagen, **categorías (varias)**, valoración media, vendedor.
- **Categoría**: nombre, descripción (las define el sistema).
- **Carrito**, **Pedido** (precio congelado en la compra), **Reseña**, **ListaDeseos**, **AlertaProducto**, **Notificación**, **Incidencia**.

## 5. Backlog y sprint

### Sprint actual (aprobado en Azure Boards): HU1, HU2, HU15, HU18, HU19, HU20, HU21, HU22, HU23, HU24, HU26, HU30

| HU | Título | Rol | Pts |
|---|---|---|---|
| HU1 | Registrar usuario | Visitante | 5 |
| HU2 | Inicio de sesión seguro (2FA/3FA) | Usuario registrado | 6 |
| HU15 | Acceso a la zona de vendedor | Vendedor | 1 |
| HU18 | Acceso restringido al panel de administración | Administrador | 1 |
| HU19 | Listar y buscar usuarios | Administrador | 2 |
| HU20 | Consultar el detalle de un usuario | Administrador | 1 |
| HU21 | Crear usuario (incluido administrador) | Administrador | 2 |
| HU22 | Modificar datos y rol de un usuario | Administrador | 2 |
| HU23 | Desactivar usuario | Administrador | 2 |
| HU24 | Activar usuario | Administrador | 2 |
| HU26 | Supervisión de vendedores (solo lectura) | Administrador | 3 |
| HU30 | Desarrollo de una home | Todos | 1 |

### Historias nuevas (HU31 y HU32, CRUD de productos del vendedor)

| HU | Título | Rol | Pts |
|---|---|---|---|
| HU31 | Dar de alta un producto | Vendedor | 3 |
| HU32 | Listar mis productos | Vendedor | 2 |

(El segundo número que aparece en el fichero original, 90 y 85, parece prioridad/valor; confirmar su significado. Aún no están asignadas a un sprint.)

### Criterios de aceptación

- **HU1**: acceso denegado si falla cualquier factor de seguridad; permisos por rol (RBAC); bloqueo tras intentos fallidos. La cuenta queda **pendiente** hasta que un admin la active (HU24).
- **HU2**: login multipaso (credenciales → TOTP → código por email).
- **HU15**: solo un Vendedor activo ve la opción en el menú; `/vendedor/productos` sin rol → 403; sin sesión → 401 y redirección a login.
- **HU18**: solo el Administrador ve la opción en el menú; sin sesión → redirección a login.
- **HU19**: listado con nombre, email, rol, estado, fecha de alta; búsqueda por nombre/email; filtro por rol y estado; paginado.
- **HU20**: ficha completa sin contraseña; accesos a editar, bloquear/desbloquear, eliminar.
- **HU21**: validación de campos y email único; asignación de rol; cuenta creada ya activa; contraseña cifrada o temporal con cambio en primer acceso.
- **HU22**: mismas validaciones que HU21; un admin no puede quitarse su rol ni degradar al último administrador.
- **HU23**: el usuario desactivado no puede iniciar sesión (mensaje específico); su sesión se invalida; requiere confirmación; un admin no puede desactivarse a sí mismo ni al último admin activo.
- **HU24**: activa cuentas pendientes o desactivadas; el listado (HU19) filtra por "pendiente".
- **HU26**: el admin ve productos/precios/pedidos de un vendedor con **todas las acciones de edición/borrado deshabilitadas**.
- **HU30**: la home es la primera pantalla y da una visión general de la app.
- **HU31**: vista dedicada con los campos del producto; se validan campos obligatorios, **precio > 0** y **stock ≥ 0**; el producto queda asociado automáticamente al vendedor autenticado; tras guardar aparece en el catálogo visible para clientes.
- **HU32**: solo productos del vendedor autenticado; búsqueda por nombre y filtro por categoría; paginado; si no hay productos, **estado vacío** con acceso directo a "Añadir producto".

El resto del backlog (HU3–HU14, HU16, HU17, HU25, HU27–HU29, HU33–HU35) cubre recuperación de contraseña, perfil, direcciones, pagos, catálogo, carrito, pedidos, reseñas, favoritos, edición de productos, estadísticas, recomendaciones y alertas. No están en el sprint, pero no hay que cerrar decisiones de arquitectura que las bloqueen.

## 6. Decisiones funcionales de pantallas y formularios

Leyenda: **\*** = obligatorio · **(único)** = no repetible (validación real en backend).

### 6.1 Home (HU30)
- Home cuidada visualmente; si no da tiempo en el Sprint 1, el pulido pasa al Sprint 2 (mínimo: la home funcional).
- Incluye botón de **Iniciar sesión** y botón de **Registrarse**.

### 6.2 Login (HU2)
- Pantalla estándar: usuario/email + contraseña, enlace "¿Has olvidado tu contraseña?" y enlace al registro.
- **Mensajes de error asépticos y genéricos**: nunca revelar si falla el usuario o la contraseña, ni si la cuenta existe.
- Excepción acordada en HU23: cuenta desactivada → mensaje específico.

### 6.3 Registro (HU1)
El tipo de usuario se elige en el propio formulario (botones, tarjetas, desplegable o checklist, libre elección de diseño). Solo se registran **Cliente** y **Vendedor**; el Administrador se crea desde la zona de administración (6.4).

**Cliente**
- Nombre\*, apellidos\*, fecha de nacimiento\* (formato exacto; comprobación de **mayor de edad**), DNI\*, email\* (único), teléfono (opcional), imagen de perfil, contraseña\*, repetir contraseña\*.
- **Tipo\***: Normal o Premium, **Normal por defecto**. Si elige Premium, se le redirige al flujo de pago.
- Casilla de aceptación de política de privacidad (RGPD).

**Vendedor**
- Nombre\*, apellidos\*, **categoría principal\*** (elegida de las del sistema; puede vender en otras), **nombre comercial\* (único)**, DNI/NIF\*, email\*, imagen de perfil, contraseña\*.

**Imagen de perfil (todos los roles)**: elegir una de una selección predefinida o subir una propia. Si no elige, se asigna una por defecto.

**Reglas de validación**
- Email: formato correcto; se confirma que existe mediante el enlace/código que llega al correo.
- Teléfono: validar longitud y que no contenga letras; no hace falta prefijo.
- DNI: no se valida el algoritmo (confianza); solo obligatorio y formato razonable.
- Fecha de nacimiento: formato exacto y mayor de edad.
- Campos de texto: limitar o truncar longitud para que no falle con entradas muy largas.
- Se validan en **frontend**; no es crítico duplicarlas en backend para este proyecto (en un sistema real sí). Excepción: unicidad (email, nombre comercial), que solo puede garantizar el backend.

**UX de validaciones**
- Validación **en vivo** (a medida que escribe) o al enviar, pero siempre **mostrando todos los errores a la vez**, no campo a campo.
- **Contraseña**: mostrar las reglas **antes** de escribirla (usabilidad) y marcar cada requisito con un tick verde al cumplirse; al fallar, indicar por qué. Botón de **mostrar/ocultar contraseña** (el "ojito").

### 6.4 Alta de administrador (HU21, desde el panel de admin)
- Nombre\*, apellidos\*, email\* (único), **sede** (campo que debe existir aunque no se use para nada), **fecha de incorporación\*** (= fecha de alta, automática y no editable), imagen de perfil.
- El administrador creado aparece **activo por defecto**.
- Contraseña: dos opciones aceptadas por el equipo; **decisión pendiente de cerrar**:
  1. El admin escribe una contraseña inicial en el formulario (más simple).
  2. Se envía un enlace por correo y el nuevo admin elige su contraseña en esa página, reutilizando el formulario de cambio de contraseña (más correcto).

### 6.5 Perfil y credenciales
- La **contraseña** solo se cambia desde el **formulario de cambio de contraseña**.
- Desde el perfil se puede editar todo lo demás **excepto email y nombre**.
- Vendedor: el nombre comercial se puede cambiar mientras siga siendo único.
- Administrador: fecha de incorporación y email no editables.
- **Recuperación de contraseña**: siempre mediante correo con enlace; la página de destino permite fijar la nueva contraseña (mismo formulario/validaciones de contraseña).

### 6.6 Alta de producto (HU31)
- Nombre\*, **categorías\*** (selección múltiple entre las categorías del sistema; mínimo una), precio\* (> 0), stock (≥ 0; si se deja vacío se inicializa a **0**), descripción, imagen, **descuento** y **descuento premium**, visible/disponible.
- **Descuentos**: cada uno se puede activar o desactivar. Un cliente normal recibe el descuento normal; uno premium recibe el descuento premium. El descuento normal se aplica a todos los clientes; **No se acumulan**.
- **Disponible/visible** se puede cambiar después (activar/desactivar producto).

### 6.7 Listado de productos del vendedor (HU32)
- Tabla paginada con buscador por nombre y filtro por categoría.
- Estado vacío con botón destacado "Añadir producto" (lleva al formulario de HU31).

## 7. Convenciones de Git (aplican a cualquier código generado)

- **Ramas**: `tipo/parte/ID-descripcion` (ej. `feature/front/123-pantalla-login`). Tipos: `feature`, `bugfix`, `hotfix`, `chore`. Por ahora **sin ID de Azure DevOps** (se añadirá cuando lo enseñen en clase).
- **Commits**: Conventional Commits con ámbito `front`/`back`/`ci`: `feat(front): ...`, `test(front): ...`, `fix(front): ...`, `chore(front): ...`, `refactor(front): ...`, `style(front): ...`.
- **TDD en el historial**: el commit `test(front): ...` va **antes** que el `feat(front): ...` de la misma funcionalidad.
- **PRs**: siempre a `develop`, con ≥ 1 aprobación y CI (build + tests + SonarQube) en verde. Squash merge.

## 8. Reglas SonarQube que afectan al frontend

- Desactivadas: `javascript:S1135` / `S1134` (TODO/FIXME).
- Relajadas solo en tests: `javascript:S100` (nombres tipo `funcion_condicion_resultado`) y `javascript:S1192` (literales duplicados).
- **Activas**: imports/variables sin usar, código comentado, complejidad cognitiva, tests sin asserts. Cero código muerto.

## 9. Estructura de carpetas acordada

```
frontend/src/
├── api/          cliente HTTP y funciones por endpoint
├── auth/         AuthContext, ProtectedRoute
├── components/   piezas reutilizables
├── features/     una carpeta por historia/dominio (auth, usuarios, productos...)
├── pages/        pantallas que juntan componentes
├── routes/       definición de rutas
└── test/         setup de tests y mocks
```

Sugerencia derivada de los formularios: reutilizar entre registro, alta de admin, perfil y cambio de contraseña los mismos componentes (campo de contraseña con ojito y checklist de requisitos, selector de imagen de perfil, validadores compartidos) para cumplir el límite de duplicación de SonarQube.

## 10. Decisión de UI: Fluent UI

- Usa componentes de Fluent UI (`Button`, `Input`, `DataGrid`/`Table`, `Dialog`, `MessageBar`, etc.), no HTML plano sin estilar.
- Theming con `FluentProvider` y tokens de diseño, no CSS ad-hoc.
- Formularios con validación visible mediante los componentes de error de Fluent UI (`Field` con `validationState` y `validationMessage`).

## 11. Seguridad en el frontend

- Token/sesión idealmente en **cookie httpOnly** gestionada por el backend, no en `localStorage` (pendiente de acordar con backend).
- `ProtectedRoute` comprueba rol y estado, pero **nunca sustituye** la validación del backend.
- Nunca pedir, mostrar ni loguear el número completo de tarjeta ni otros datos sensibles de pago.
- Mensajes de error de autenticación genéricos (ver 6.2).

## 12. Puntos abiertos a confirmar

1. Contraseña inicial del admin: formulario directo o enlace por correo (6.4).
2. Significado de los números 90/85 en HU31/HU32 y sprint en que entran.
3. Estado tras registro: HU1 deja la cuenta **pendiente** hasta activación por admin, mientras que el registro prevé confirmación de email; aclarar si son dos pasos distintos.
4. Flujo de pago del cliente Premium en el registro (depende de las HU de pagos, fuera del sprint actual).
5. Validaciones solo en frontend: pendiente de que el responsable de backend lo acepte.

## 13. Estado actual del trabajo

- Proyecto React inicializado con Vite (rama `chore/front/inicializacion-react`, subida).
- ESLint configurado (el de Vite por defecto).
- Pendiente: instalar Fluent UI, router + layout base, `AuthContext` + `ProtectedRoute`, y empezar por HU30 (home) y HU1 (registro).
- Para test es necesario importar lo que se use: no hay globals. Si no se hace asi los tests generados podrían fallar o romper el lint.
