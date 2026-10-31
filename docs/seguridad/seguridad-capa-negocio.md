# Seguridad en la capa de negocio

Documento para entender cómo se aplican las reglas de acceso dentro de los casos de uso del backend, por qué se hizo así y cómo mantenerlo.

- **Origen:** comentario R8 de la revisión del Sprint 1
- **Rama:** `fix/seguridad-capa-negocio` (fix transversal: no pertenece a ninguna HU ni task, por eso sus commits no llevan `AB#`)
- **Decisión:** opción B, `@PreAuthorize` en los servicios de aplicación (acordada con el equipo)
- **Reglas de proyecto:** sección «Seguridad en la capa de negocio» del `CLAUDE.md`

---

## 1. Qué problema resuelve

Antes, el rol solo se comprobaba por ruta, en `SecurityConfig` (`/api/admin/**`, `/api/seller/**`, `/api/customer/**`). Si un caso de uso se invocaba sin pasar por esa ruta, ninguna regla de acceso se aplicaba.

Ahora cada caso de uso se protege a sí mismo, de modo que las reglas se cumplen aunque se llame directamente. Hay dos tipos de regla:

| Regla | Qué comprueba |
|---|---|
| **Rol** | El rol mínimo que necesita el caso de uso. Se deniega por defecto: ningún método de servicio queda sin anotar. |
| **Propiedad** | Si el caso de uso recibe el correo del actor, que coincida con el de la sesión. Un vendedor solo opera con sus productos y pedidos; un cliente, con su carrito, pedidos, direcciones, métodos de pago y perfil. |

El administrador **solo lee** los productos y pedidos de los vendedores (HU26), pero sí gestiona los datos de su cuenta (editar, activar, desactivar, eliminar).

## 2. Cómo funciona

- `SecurityConfig` lleva `@EnableMethodSecurity`.
- Cada método público de un servicio de aplicación lleva `@PreAuthorize` con una constante de `shared.seguridad.Permisos`. No se escribe `hasRole('...')` a mano en los servicios.
- Los servicios son POJOs creados con `@Bean` en las clases `*Config`. Spring envuelve esos beans en un proxy que evalúa la anotación antes de ejecutar el método.
- Las reglas por ruta de `SecurityConfig` se mantienen como primera barrera. Los controladores no llevan `@PreAuthorize`: el rol se declara en el servicio.
- La capa `application` solo puede usar de Spring `@Service` y `@PreAuthorize` (y de este último, solo `spring-security-core` y las constantes de `shared.seguridad`).

### Constantes de `Permisos`

| Constante | Significado |
|---|---|
| `PUBLICO` | `permitAll()`: se puede llamar sin sesión (registro y pasos del login, listado público de categorías) |
| `SOLO_ADMINISTRADOR`, `SOLO_VENDEDOR`, `SOLO_CLIENTE`, `CLIENTE_O_VENDEDOR` | Solo el rol indicado |
| `SOLO_VENDEDOR_PROPIO` | Vendedor y `#emailVendedor == authentication.name` |
| `SOLO_VENDEDOR_PROPIO_EN_COMANDO` | Vendedor y `#comando.emailVendedor() == authentication.name` |
| `SOLO_CLIENTE_PROPIO` | Cliente y `#emailCliente == authentication.name` |
| `SOLO_CLIENTE_PROPIO_EN_COMANDO` | Cliente y `#comando.emailCliente() == authentication.name` |
| `SOLO_CLIENTE_PROPIO_POR_EMAIL` | Cliente y `#email == authentication.name` |
| `SOLO_ADMINISTRADOR_PROPIO` | Administrador y `#emailAdministrador == authentication.name` |
| `AUTENTICADO_PROPIO` | Cualquier usuario autenticado y `#email == authentication.name` |
| `CLIENTE_O_VENDEDOR_PROPIO` | Cliente o vendedor y `#email == authentication.name` |

Las expresiones con `#nombre` dependen del **nombre del parámetro** del método (Spring lo lee porque el proyecto se compila con `-parameters`). Si renombras un parámetro anotado, la expresión deja de funcionar.

### Qué responde cuando falla

| Situación | Respuesta |
|---|---|
| Sin sesión | `401` |
| Con sesión, sin permiso | `403` con `{"mensaje": "Acceso denegado", "code": "ACCESS_DENIED"}`, igual en todos los módulos y sin detalles internos |
| Recurso ajeno | `404` con el código de «no encontrado» del recurso, que ya existía (en pedidos, `400`, para no revelar que existe) |

El `403` lo producen dos piezas de `shared.error`: `ManejadorGlobalErrores` (excepciones lanzadas desde un servicio) y `ManejadorAccesoDenegado` (el filtro de `SecurityConfig`, para las reglas por ruta).

## 3. Reglas por módulo

| Módulo | Servicio | Regla |
|---|---|---|
| productos | `GestionProductosService`, `ListadoProductosService` | Vendedor con su propio correo |
| productos | `SupervisionProductosService`, `ConteoProductosService` | Administrador |
| productos | `CatalogoProductosService`, `ExistenciasProductosService` | Cliente |
| metodospago | `GestionMetodosPagoService` | Cliente con su propio correo |
| direcciones | `GestionDireccionesService` | Cliente con su propio correo |
| carrito | `GestionCarritoService` | Cliente con su propio correo |
| pedidos | `RealizacionPedidoService` | Cliente con el correo del comando igual al de la sesión |
| usuarios | `AdministracionUsuariosService`, `ListadoUsuariosService`, `CrearAdministradorService` | Administrador (al desactivar o eliminar, además con su propio correo); `consultarNombreComercial` es del cliente |
| usuarios | `AutenticacionService`, `GestionPerfilService` | Consultar el usuario en sesión: autenticado y con su correo. Id y perfil: cliente o vendedor. `esClientePremium`: cliente. |
| usuarios | `RegistroUsuarioService`, `AuthenticatorService` y los pasos del login | Público |
| categorias | `ConsultaCategoriasService` | Público |
| categorias | `AdministracionCategoriasService` | Administrador |

Cuando un método llama a otro de la misma clase (por ejemplo `consultarNombreComercial` llama a `consultarVendedor`), la llamada no pasa por el proxy y la regla no se comprueba dos veces. Por eso `consultarNombreComercial` puede ser del cliente aunque `consultarVendedor` sea del administrador.

## 4. Cómo se prueba

Las anotaciones solo se evalúan con Spring, así que cada módulo tiene una clase `Seguridad<Modulo>ServiceTest` con un contexto mínimo (sin MongoDB):

- `@SpringJUnitConfig` con una `@Configuration` anidada y `@EnableMethodSecurity`.
- Los servicios reales, con dobles de prueba de los puertos de salida.
- `@WithMockUser` y `@WithAnonymousUser` para el rol. Cada clase comprueba que el rol correcto pasa, que los demás reciben `AccessDeniedException`, que otro correo es rechazado sin tocar los puertos de salida y que sin sesión se lanza `AuthenticationCredentialsNotFoundException`.
- Para demostrar que la seguridad deja pasar, se espera una excepción de negocio (por ejemplo, «no encontrado») en lugar de la de acceso.
- Los comandos se construyen **antes** de la lambda del `assertThrows`, para que dentro solo haya una invocación (regla S5778 de Sonar).

| Caso | Módulo | Clase de test |
|---|---|---|
| HU31-CP28 | productos | `SeguridadProductosServiceTest` |
| HU6-CP37 | metodospago | `SeguridadMetodosPagoServiceTest` |
| HU5-CP29 | direcciones | `SeguridadDireccionesServiceTest` |
| HU9-CP20 | carrito | `SeguridadCarritoServiceTest` |
| HU10-CP20 | pedidos | `SeguridadPedidosServiceTest` |
| HU20-CP07 | usuarios (administración) | `SeguridadAdministracionUsuarioServiceTest` |
| HU2-CP29 | usuarios (sesión, perfil, registro y login) | `SeguridadSesionUsuarioServiceTest` |
| HU40-CP09 | categorias | `SeguridadCategoriasServiceTest` |

Además, `SecurityConfigTest` comprueba el `403` con `ACCESS_DENIED` tanto desde un servicio como desde una ruta, y el `401` sin sesión.

## 5. Cómo añadir un caso de uso nuevo

1. Escribe el test de seguridad primero (TDD): rol permitido, roles denegados, correo ajeno y sin sesión.
2. Anota **cada** método del servicio con una constante de `Permisos`. Un método sin anotar es un defecto.
3. Si la regla no existe todavía en `Permisos`, añade la constante allí, no en el servicio.
4. Si el caso de uso recibe el correo del actor, usa una constante «propia» y comprueba que el nombre del parámetro coincide con el de la expresión.
5. Actualiza la matriz de trazabilidad en el mismo PR.

## 6. Limitaciones conocidas

- **Pasos del login a medias.** `AuthenticatorService.prepararAlta` y `verificarCodigo`, y los dos primeros pasos del login, son `permitAll()`. El correo del login a medias vive en un atributo de la sesión HTTP, no en `authentication.name`, y aún no hay usuario autenticado, así que la propiedad no se puede comprobar con `@PreAuthorize`. Los protege el controlador (`exigirPasoDosSuperado`). Cerrarlo del todo exigiría rediseñar el login y queda fuera de este fix.
- **CSRF.** El `403` que produce una petición sin token CSRF ahora también devuelve el JSON `ACCESS_DENIED`, porque lo atiende el mismo manejador.
- **Filtro `CuentaActivaFilter`.** Consulta el usuario en sesión con el correo de la propia sesión, así que cumple la regla de propiedad. Si en el futuro consulta con otro correo, será rechazado.
- **Pedidos.** Hoy solo existe el caso de uso de realizar un pedido. Cuando lleguen las consultas de vendedor y de administrador (HU26), nacerán con su `@PreAuthorize`.
