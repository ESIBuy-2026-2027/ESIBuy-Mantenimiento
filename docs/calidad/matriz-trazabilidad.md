# Matriz de trazabilidad · Casos de Prueba ↔ Historias de Usuario

Relaciona cada historia de usuario (HU) con sus casos de prueba (CP) y cada caso con los tests que lo validan.

- **Tabla A** (HU ↔ CP): ¿está probada cada historia?
- **Tabla B** (CP ↔ test): ¿qué test valida cada caso y de dónde salió?

**Mantenimiento:** todo PR que añada, modifique o elimine tests actualiza esta matriz en el mismo PR (ver el CLAUDE.md). Cada test lleva un `@DisplayName` que empieza por el ID de su caso (`HU<n>-CP<nn>: ...`), que es el vínculo entre el código y la matriz.

**Estados:** ✅ Pasa · 🔍 En revisión (PR abierto) · ⏳ Pendiente · ⏸ Fuera de alcance

---

## HU1 · Registrar usuario

### Tabla A · HU1 ↔ casos de prueba

| Caso | Descripción | Tipo | Tests | Estado |
|---|---|---|---|---|
| HU1-CP01 | Registra un cliente con rol CLIENTE | Unitario + integración | 3 | ✅ Pasa |
| HU1-CP02 | Guarda la contraseña hasheada, nunca en claro | Unitario + integración | 6 | ✅ Pasa |
| HU1-CP03 | Rechaza un correo ya registrado y no guarda nada | Unitario + integración | 4 | ✅ Pasa |
| HU1-CP04 | Rechaza un username ya registrado | — | 0 | ⏸ Fuera de alcance (no está decidido si existe username) |
| HU1-CP05 | Rechaza contraseñas de menos de 8 caracteres | Unitario + integración | 8 | ✅ Pasa |
| HU1-CP06 | Rechaza el registro si no se acepta la política de privacidad | — | 0 | ⏳ Pendiente |
| HU1-CP07 | Rechaza un email con formato inválido | Unitario + integración (capa web) | 6 | ✅ Pasa |
| HU1-CP08 | Rechaza el registro si falta un campo obligatorio | Integración (capa web) | 2 | ✅ Pasa |
| HU1-CP09 | Rechaza contraseñas de la lista de contraseñas comunes | Unitario | 2 | ✅ Pasa |
| HU1-CP10 | Rechaza contraseñas que contienen datos personales del usuario | Unitario | 3 | ✅ Pasa |
| HU1-CP11 | El usuario cumple sus invariantes de dominio (datos obligatorios, rol y tipo de cliente coherentes, desactivación) | Unitario | 12 | ✅ Pasa |

Notas:
- **HU1-CP11:** en el PR #36, el bloqueo manual de la T214 se renombra a desactivar/activar (`desactivar()`, `activar()`, `estaDesactivado()`) para no confundirlo con el bloqueo temporal por intentos del login (HU2-CP03).
- **HU1-CP01:** el estado `PENDIENTE` no aplica a clientes, solo al registro de vendedores (verificación manual por un administrador). Decisión del equipo del 07/10/2026.
- **AB#216:** los tests de la capa de infraestructura (persistencia en MongoDB) se asignan a CP01, CP02 y CP03 como propuesta, pendiente de confirmar con el equipo.
- **HU1-CP07 y HU1-CP08:** casos propuestos en la T14 (AB#14) para la validación de entrada del endpoint de registro. Pendientes de confirmar con el equipo.
- **HU1-CP09, HU1-CP10 y HU1-CP11:** casos propuestos en la T214 (AB#214, PR #34) para la capa de dominio. Pendientes de confirmar con el equipo y de dar de alta en Azure Boards. CP09 y CP10 salen de los requisitos de Seguridad (lista negra y «evitar datos personales»).
- **AB#214:** los tests de `PoliticaContrasenasTest` (CP05, CP09 y CP10) prueban la política de contraseñas en el dominio, pero el registro todavía no la usa: la validación de longitud sigue en `RegistroUsuarioService`. Conectarla será una task posterior.

### Tabla B · casos de prueba ↔ tests

| Caso | Clase de test | Método | Capa | Task | Origen | Estado |
|---|---|---|---|---|---|---|
| HU1-CP01 | `RegistroUsuarioServiceTest` | `registraClienteConRolCliente` | Aplicación | AB#16 | PR #14 | ✅ |
| HU1-CP01 | `UsuarioMongoAdapterTest` | `guardarPersisteElUsuarioEnLaColeccionUsuarios` | Infraestructura | AB#216 | PR #28 | ✅ |
| HU1-CP01 | `UsuarioControllerTest` | `registroValidoDevuelve201` | Infraestructura | AB#14 | PR #30 | ✅ |
| HU1-CP02 | `RegistroUsuarioServiceTest` | `guardaContrasenaHasheada` | Aplicación | AB#16 | PR #14 | ✅ |
| HU1-CP02 | `BCryptHashContrasenaAdapterTest` | `hashNoEsLaContrasenaEnClaro` | Infraestructura | AB#15 | PR #20 | ✅ |
| HU1-CP02 | `BCryptHashContrasenaAdapterTest` | `hashEsVerificableConBCrypt` | Infraestructura | AB#15 | PR #20 | ✅ |
| HU1-CP02 | `BCryptHashContrasenaAdapterTest` | `hashUsaElCosteAcordado` | Infraestructura | AB#15 | PR #20 | ✅ |
| HU1-CP02 | `RegistrarUsuarioCommandTest` | `toStringNoMuestraLaContrasena` | Aplicación | AB#223 | PR #26 | ✅ |
| HU1-CP02 | `UsuarioDocumentMapperTest` | `convierteUsuarioEnDocumento` | Infraestructura | AB#216 | PR #28 | ✅ |
| HU1-CP03 | `RegistroUsuarioServiceTest` | `rechazaEmailYaRegistrado` | Aplicación | AB#16 | PR #14 | ✅ |
| HU1-CP03 | `UsuarioMongoAdapterTest` | `existePorCorreoDevuelveTrueSiEstaGuardado` | Infraestructura | AB#216 | PR #28 | ✅ |
| HU1-CP03 | `UsuarioMongoAdapterTest` | `existePorCorreoDevuelveFalseSiNoEstaGuardado` | Infraestructura | AB#216 | PR #28 | ✅ |
| HU1-CP03 | `UsuarioControllerTest` | `correoYaRegistradoDevuelve409` | Infraestructura | AB#14 | PR #30 | ✅ |
| HU1-CP05 | `RegistroUsuarioServiceTest` | `permiteContrasenaDeExactamente8Caracteres` | Aplicación | AB#16 | PR #14 | ✅ |
| HU1-CP05 | `RegistroUsuarioServiceTest` | `rechazaContrasenaDeMenosDe8Caracteres` | Aplicación | AB#16 | PR #14 | ✅ |
| HU1-CP05 | `RegistroUsuarioServiceTest` | `rechazaContrasenaNula` | Aplicación | AB#223 | PR #26 | ✅ |
| HU1-CP05 | `UsuarioControllerTest` | `contrasenaInvalidaDevuelve400` | Infraestructura | AB#14 | PR #30 | ✅ |
| HU1-CP05 | `PoliticaContrasenasTest` | `aceptaContrasenaSegura` | Dominio | AB#214 | PR #34 | ✅ |
| HU1-CP05 | `PoliticaContrasenasTest` | `rechazaContrasenaNula` | Dominio | AB#214 | PR #34 | ✅ |
| HU1-CP05 | `PoliticaContrasenasTest` | `rechazaContrasenaDeSieteCaracteres` | Dominio | AB#214 | PR #34 | ✅ |
| HU1-CP05 | `PoliticaContrasenasTest` | `aceptaContrasenaDeOchoCaracteres` | Dominio | AB#214 | PR #34 | ✅ |
| HU1-CP07 | `UsuarioControllerTest` | `rechazaEmailConFormatoInvalido` | Infraestructura | AB#14 | PR #30 | ✅ |
| HU1-CP07 | `EmailTest` | `creaEmailConFormatoValido` | Dominio | AB#214 | PR #34 | ✅ |
| HU1-CP07 | `EmailTest` | `rechazaEmailNulo` | Dominio | AB#214 | PR #34 | ✅ |
| HU1-CP07 | `EmailTest` | `rechazaEmailEnBlanco` | Dominio | AB#214 | PR #34 | ✅ |
| HU1-CP07 | `EmailTest` | `rechazaEmailSinArroba` | Dominio | AB#214 | PR #34 | ✅ |
| HU1-CP07 | `EmailTest` | `rechazaEmailSinDominio` | Dominio | AB#214 | PR #34 | ✅ |
| HU1-CP08 | `UsuarioControllerTest` | `rechazaEmailVacio` | Infraestructura | AB#14 | PR #30 | ✅ |
| HU1-CP08 | `UsuarioControllerTest` | `rechazaPasswordVacia` | Infraestructura | AB#14 | PR #30 | ✅ |
| HU1-CP09 | `PoliticaContrasenasTest` | `rechazaContrasenaDeListaNegra` | Dominio | AB#214 | PR #34 | ✅ |
| HU1-CP09 | `PoliticaContrasenasTest` | `rechazaContrasenaDeListaNegraConMayusculas` | Dominio | AB#214 | PR #34 | ✅ |
| HU1-CP10 | `PoliticaContrasenasTest` | `rechazaContrasenaQueContieneElNombre` | Dominio | AB#214 | PR #34 | ✅ |
| HU1-CP10 | `PoliticaContrasenasTest` | `rechazaContrasenaQueContieneLosApellidos` | Dominio | AB#214 | PR #34 | ✅ |
| HU1-CP10 | `PoliticaContrasenasTest` | `rechazaContrasenaQueContieneLaParteLocalDelEmail` | Dominio | AB#214 | PR #34 | ✅ |
| HU1-CP11 | `UsuarioTest` | `creaUsuarioConDatosCorrectos` | Dominio | AB#214 | PR #34 | ✅ |
| HU1-CP11 | `UsuarioTest` | `permiteUsuarioSinIdAntesDePersistir` | Dominio | AB#214 | PR #34 | ✅ |
| HU1-CP11 | `UsuarioTest` | `rechazaNombreEnBlanco` | Dominio | AB#214 | PR #34 | ✅ |
| HU1-CP11 | `UsuarioTest` | `rechazaApellidosEnBlanco` | Dominio | AB#214 | PR #34 | ✅ |
| HU1-CP11 | `UsuarioTest` | `permiteUsuarioSinNombreNiApellidos` | Dominio | AB#214 | PR #34 | ✅ |
| HU1-CP11 | `UsuarioTest` | `rechazaEmailNulo` | Dominio | AB#214 | PR #34 | ✅ |
| HU1-CP11 | `UsuarioTest` | `rechazaContrasenaHashEnBlanco` | Dominio | AB#214 | PR #34 | ✅ |
| HU1-CP11 | `UsuarioTest` | `rechazaRolNulo` | Dominio | AB#214 | PR #34 | ✅ |
| HU1-CP11 | `UsuarioTest` | `rechazaClienteSinTipoDeCliente` | Dominio | AB#214 | PR #34 | ✅ |
| HU1-CP11 | `UsuarioTest` | `rechazaTipoDeClienteSiNoEsCliente` | Dominio | AB#214 | PR #34 | ✅ |
| HU1-CP11 | `UsuarioTest` | `desactivaUsuario` | Dominio | AB#214 | PR #34 (renombrado en PR #36) | ✅ |
| HU1-CP11 | `UsuarioTest` | `activaUsuarioDesactivado` | Dominio | AB#214 | PR #34 (renombrado en PR #36) | ✅ |

Rutas de las clases de test (bajo `backend/src/test/java/es/uclm/esibuy/backend/`):
- `RegistroUsuarioServiceTest`: `usuarios/application/service/`
- `RegistrarUsuarioCommandTest`: `usuarios/application/port/in/`
- `BCryptHashContrasenaAdapterTest`: `usuarios/infrastructure/adapter/out/seguridad/`
- `UsuarioDocumentMapperTest` y `UsuarioMongoAdapterTest`: `usuarios/infrastructure/adapter/out/persistence/`
- `UsuariosConfigTest`: `usuarios/infrastructure/config/`
- `EmailTest`, `PoliticaContrasenasTest` y `UsuarioTest`: `usuarios/domain/model/` (JUnit puro, sin Spring)
- `UsuarioControllerTest`: `usuarios/infrastructure/adapter/in/web/` (con `@WebMvcTest`; prueba también `SecurityConfig` y `ManejadorGlobalErrores` de `shared`)

Los tests de integración de MongoDB (`UsuarioMongoAdapterTest`, `UsuariosConfigTest`) usan Testcontainers (`MongoTestcontainersConfig`) y requieren Docker.

---

## HU2 · Inicio de sesión seguro

### Tabla A · HU2 ↔ casos de prueba

| Caso | Descripción | Tipo | Tests | Estado |
|---|---|---|---|---|
| HU2-CP01 | Inicia sesión con credenciales correctas y pasa al paso del código por email | Unitario + integración | 3 | ✅ Pasa |
| HU2-CP02 | Rechaza credenciales incorrectas sin revelar si el correo existe | Unitario + integración | 4 | ✅ Pasa |
| HU2-CP03 | Bloquea la cuenta durante 15 minutos tras 5 intentos fallidos | Unitario | 3 | ✅ Pasa |
| HU2-CP04 | Rechaza el acceso a una cuenta desactivada | Unitario | 1 | ✅ Pasa |

Notas:
- **HU2-CP01 a CP04:** casos definidos en la T19/T22 a partir del contrato del frontend (`frontend/src/mocks/authHandlers.js`). Pendientes de añadir a la descripción de la T22 en Azure Boards.
- **HU2-CP04:** «desactivada» es la cuenta desactivada por un administrador (`desactivar()` de `Usuario`), distinta del bloqueo temporal de CP03.
- **Persistencia:** los tests del login usan mocks. Mientras `UsuarioDocument` no guarde los intentos fallidos ni la desactivación, CP03 y CP04 no se cumplen en la aplicación real (task pendiente).
- Los casos del código por email (T21) y de Microsoft Authenticator (T20) se añadirán como CP05 en adelante.

### Tabla B · casos de prueba ↔ tests

| Caso | Clase de test | Método | Capa | Task | Origen | Estado |
|---|---|---|---|---|---|---|
| HU2-CP01 | `AutenticacionServiceTest` | `iniciaSesionConCredencialesCorrectas` | Aplicación | AB#22 | PR #36 | ✅ |
| HU2-CP01 | `UsuarioDocumentMapperTest` | `convierteDocumentoEnUsuario` | Infraestructura | AB#224 | PR #36 | ✅ |
| HU2-CP01 | `UsuarioMongoAdapterTest` | `buscarPorCorreoDevuelveElUsuarioGuardado` | Infraestructura | AB#224 | PR #36 | ✅ |
| HU2-CP02 | `AutenticacionServiceTest` | `rechazaCorreoInexistente` | Aplicación | AB#22 | PR #36 | ✅ |
| HU2-CP02 | `AutenticacionServiceTest` | `rechazaContrasenaIncorrecta` | Aplicación | AB#22 | PR #36 | ✅ |
| HU2-CP02 | `BCryptHashContrasenaAdapterTest` | `coincideConElHashDeLaMismaContrasena` | Infraestructura | AB#22 | PR #36 | ✅ |
| HU2-CP02 | `UsuarioMongoAdapterTest` | `buscarPorCorreoDevuelveVacioSiNoEstaGuardado` | Infraestructura | AB#224 | PR #36 | ✅ |
| HU2-CP03 | `AutenticacionServiceTest` | `permiteCuartoIntentoFallidoSinBloquear` | Aplicación | AB#22 | PR #36 | ✅ |
| HU2-CP03 | `AutenticacionServiceTest` | `bloqueaLaCuentaTrasCincoIntentosFallidos` | Aplicación | AB#22 | PR #36 | ✅ |
| HU2-CP03 | `AutenticacionServiceTest` | `desbloqueaLaCuentaPasados15Minutos` | Aplicación | AB#22 | PR #36 | ✅ |
| HU2-CP04 | `AutenticacionServiceTest` | `rechazaCuentaDesactivada` | Aplicación | AB#22 | PR #36 | ✅ |

Rutas de las clases de test (bajo `backend/src/test/java/es/uclm/esibuy/backend/`):
- `AutenticacionServiceTest`: `usuarios/application/service/` (Mockito, con `Clock` fijo)
- `BCryptHashContrasenaAdapterTest`: `usuarios/infrastructure/adapter/out/seguridad/`
- `UsuarioDocumentMapperTest` y `UsuarioMongoAdapterTest`: `usuarios/infrastructure/adapter/out/persistence/` (el adaptador, con Testcontainers)

---

## Tests técnicos sin caso de prueba

Tests que no validan una HU concreta.

| Clase de test | Método | Qué comprueba |
|---|---|---|
| `BackendApplicationTests` | `contextLoads` | Que el contexto de Spring arranca, con el MongoDB de Testcontainers y sin Atlas (`MONGODB_URI` no existe en el CI; AB#212) |
| `UsuariosConfigTest` | `elContextoConectaLasPiezasDelRegistro` | Que Spring conecta servicio de registro, hash BCrypt y adaptador de MongoDB (AB#216, PR #28) |
| `UsuarioControllerTest` | `otraRutaSinSesionEstaProtegida` | Que una ruta distinta del registro exige sesión (mínimo privilegio en `SecurityConfig`; AB#14, PR #30) |
| `EmailTest` | `dosEmailsConElMismoValorSonIguales` | Que `Email` es un value object con igualdad por valor (AB#214, PR #34) |
| `UsuarioTest` | `creaClienteNuevoEstandarSinNombreNiId` | Que la factoría `Usuario.nuevoCliente` crea un cliente `ESTANDAR` sin id ni datos de perfil, como lo usa el registro (AB#214, PR #34) |
