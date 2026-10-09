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
| HU1-CP01 | Registra un cliente con rol CLIENTE | Unitario + integración | 5 | ✅ Pasa |
| HU1-CP02 | Guarda la contraseña hasheada, nunca en claro | Unitario + integración | 6 | ✅ Pasa |
| HU1-CP03 | Rechaza un correo ya registrado y no guarda nada | Unitario + integración | 5 | ✅ Pasa |
| HU1-CP04 | Rechaza un username ya registrado | — | 0 | ⏸ Fuera de alcance (no está decidido si existe username) |
| HU1-CP05 | Rechaza contraseñas de menos de 8 caracteres | Unitario + integración | 8 | ✅ Pasa |
| HU1-CP06 | Rechaza el registro si no se acepta la política de privacidad | — | 0 | ⏳ Pendiente |
| HU1-CP07 | Rechaza un email con formato inválido | Unitario + integración (capa web) | 8 | ✅ Pasa |
| HU1-CP08 | Rechaza el registro si falta un campo obligatorio | Integración (capa web) | 2 | ✅ Pasa |
| HU1-CP09 | Rechaza contraseñas de la lista de contraseñas comunes | Unitario | 2 | ✅ Pasa |
| HU1-CP10 | Rechaza contraseñas que contienen datos personales del usuario | Unitario | 3 | ✅ Pasa |
| HU1-CP11 | El usuario cumple sus invariantes de dominio (datos obligatorios, rol y tipo de cliente coherentes, desactivación) | Unitario | 12 | ✅ Pasa |

Notas:
- **HU1-CP11:** en el PR #36, el bloqueo manual de la T214 se renombra a desactivar/activar (`desactivar()`, `activar()`, `estaDesactivado()`) para no confundirlo con el bloqueo temporal por intentos del login (HU2-CP03). En la T225 (AB#225) la consulta pasa a nombrarse en positivo: `estaActivo()`.
- **HU1-CP01:** el estado `PENDIENTE` no aplica a clientes, solo al registro de vendedores (verificación manual por un administrador). Decisión del equipo del 07/10/2026.
- **AB#216:** los tests de la capa de infraestructura (persistencia en MongoDB) se asignan a CP01, CP02 y CP03 como propuesta, pendiente de confirmar con el equipo.
- **HU1-CP07 y HU1-CP08:** casos propuestos en la T14 (AB#14) para la validación de entrada del endpoint de registro. Pendientes de confirmar con el equipo.
- **AB#227:** la validación web (`@Email`) acepta un email sin punto en el dominio, como `ana@localhost`, pero el value object `Email` lo rechaza. Antes de la T227 esa `EmailInvalidoException` no se manejaba y el registro respondía con un 500. Ahora `ManejadorGlobalErrores` la traduce a un 400 con el mismo mensaje genérico que una contraseña inválida.
- **HU1-CP09, HU1-CP10 y HU1-CP11:** casos propuestos en la T214 (AB#214, PR #34) para la capa de dominio. Pendientes de confirmar con el equipo y de dar de alta en Azure Boards. CP09 y CP10 salen de los requisitos de Seguridad (lista negra y «evitar datos personales»).
- **AB#214:** los tests de `PoliticaContrasenasTest` (CP05, CP09 y CP10) prueban la política de contraseñas en el dominio, pero el registro todavía no la usa: la validación de longitud sigue en `RegistroUsuarioService`. Conectarla será una task posterior.
- **AB#210:** el test de email duplicado (CP03) comprueba el índice único de `email` a nivel de base de datos, sin pasar por `existePorCorreo`. `conservaLosDatosDePerfil` (CP01) comprueba contra MongoDB lo que `conservaNombreApellidosYTipoDeCliente` (T225) prueba en el mapper. Al integrar `develop`, el PR #43 deja la persistencia del estado de acceso de la T225 (sin `Usuario.reconstruir` ni sus tests) y guarda sus campos con los nombres del modelo de BBDD (`estado`, `failedLoginAttempts`, `lockedUntil`); decisión del equipo del 08/10/2026.

### Tabla B · casos de prueba ↔ tests

| Caso | Clase de test | Método | Capa | Task | Origen | Estado |
|---|---|---|---|---|---|---|
| HU1-CP01 | `RegistroUsuarioServiceTest` | `registraClienteConRolCliente` | Aplicación | AB#16 | PR #14 | ✅ |
| HU1-CP01 | `UsuarioMongoAdapterTest` | `guardarPersisteElUsuarioEnLaColeccionUsuarios` | Infraestructura | AB#216 | PR #28 | ✅ |
| HU1-CP01 | `UsuarioControllerTest` | `registroValidoDevuelve201` | Infraestructura | AB#14 | PR #30 | ✅ |
| HU1-CP01 | `UsuarioDocumentMapperTest` | `conservaNombreApellidosYTipoDeCliente` | Infraestructura | AB#225 | PR #46 | ✅ |
| HU1-CP01 | `UsuarioMongoAdapterTest` | `conservaLosDatosDePerfil` | Infraestructura | AB#210 | PR #43 | ✅ |
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
| HU1-CP03 | `UsuarioMongoAdapterTest` | `rechazaEmailDuplicadoEnLaBaseDeDatos` | Infraestructura | AB#210 | PR #43 | ✅ |
| HU1-CP05 | `RegistroUsuarioServiceTest` | `permiteContrasenaDeExactamente8Caracteres` | Aplicación | AB#16 | PR #14 | ✅ |
| HU1-CP05 | `RegistroUsuarioServiceTest` | `rechazaContrasenaDeMenosDe8Caracteres` | Aplicación | AB#16 | PR #14 | ✅ |
| HU1-CP05 | `RegistroUsuarioServiceTest` | `rechazaContrasenaNula` | Aplicación | AB#223 | PR #26 | ✅ |
| HU1-CP05 | `UsuarioControllerTest` | `contrasenaInvalidaDevuelve400` | Infraestructura | AB#14 | PR #30 | ✅ |
| HU1-CP05 | `PoliticaContrasenasTest` | `aceptaContrasenaSegura` | Dominio | AB#214 | PR #34 | ✅ |
| HU1-CP05 | `PoliticaContrasenasTest` | `rechazaContrasenaNula` | Dominio | AB#214 | PR #34 | ✅ |
| HU1-CP05 | `PoliticaContrasenasTest` | `rechazaContrasenaDeSieteCaracteres` | Dominio | AB#214 | PR #34 | ✅ |
| HU1-CP05 | `PoliticaContrasenasTest` | `aceptaContrasenaDeOchoCaracteres` | Dominio | AB#214 | PR #34 | ✅ |
| HU1-CP07 | `UsuarioControllerTest` | `rechazaEmailConFormatoInvalido` | Infraestructura | AB#14 | PR #30 | ✅ |
| HU1-CP07 | `UsuarioControllerTest` | `emailSinDominioDevuelve400ConMensajeGenerico` | Infraestructura | AB#227 | PR #47 | ✅ |
| HU1-CP07 | `EmailTest` | `creaEmailConFormatoValido` | Dominio | AB#214 | PR #34 | ✅ |
| HU1-CP07 | `EmailTest` | `rechazaEmailNulo` | Dominio | AB#214 | PR #34 | ✅ |
| HU1-CP07 | `EmailTest` | `rechazaEmailEnBlanco` | Dominio | AB#214 | PR #34 | ✅ |
| HU1-CP07 | `EmailTest` | `rechazaEmailSinArroba` | Dominio | AB#214 | PR #34 | ✅ |
| HU1-CP07 | `EmailTest` | `rechazaEmailSinDominio` | Dominio | AB#214 | PR #34 | ✅ |
| HU1-CP07 | `EmailTest` | `rechazaEmailSinPuntoEnElDominio` | Dominio | AB#227 | PR #47 | ✅ |
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

---

## HU19 · Listar y buscar usuarios

### Tabla A · HU19 ↔ casos de prueba

| Caso | Descripción | Tipo | Tests | Estado |
|---|---|---|---|---|
| HU19-CP01 | El servicio delega la consulta intacta al repositorio | Unitario | 1 | 🔍 En revisión |
| HU19-CP02 | El servicio devuelve la página proporcionada por el repositorio | Unitario | 1 | 🔍 En revisión |
| HU19-CP03 | El adaptador busca por los campos visibles del usuario | Integración MongoDB | 1 | 🔍 En revisión |
| HU19-CP04 | El adaptador aplica el filtro por rol | Integración MongoDB | 1 | 🔍 En revisión |
| HU19-CP05 | El adaptador aplica el filtro por estado de bloqueo | Integración MongoDB | 1 | 🔍 En revisión |
| HU19-CP06 | El adaptador devuelve la página solicitada y el total completo | Integración MongoDB | 1 | 🔍 En revisión |
| HU19-CP07 | La búsqueda trata los caracteres especiales como texto literal | Integración MongoDB | 1 | 🔍 En revisión |
| HU19-CP08 | Un administrador accede al listado HTTP sin exponer datos sensibles | Integración web | 1 | 🔍 En revisión |
| HU19-CP09 | Conserva el estado PENDIENTE al mapear el usuario | Unitario | 1 | 🔍 En revisión |
| HU19-CP10 | El adaptador filtra usuarios BLOQUEADO mediante lockedUntil | Integración MongoDB | 1 | 🔍 En revisión |

### Tabla B · casos de prueba ↔ tests

| Caso | Clase de test | Método | Capa | Task | Origen | Estado |
|---|---|---|---|---|---|---|
| HU19-CP01 | `ListadoUsuariosServiceTest` | `listar_delegaLaConsultaIntactaAlRepositorio` | Aplicación | AB#62 | PR por abrir | 🔍 |
| HU19-CP02 | `ListadoUsuariosServiceTest` | `listar_devuelveLaPaginaProporcionadaPorElRepositorio` | Aplicación | AB#62 | PR por abrir | 🔍 |
| HU19-CP03 | `UsuarioMongoAdapterTest` | `listarBuscaEnEmailNombreYApellidos` | Infraestructura | AB#61 | PR por abrir | 🔍 |
| HU19-CP04 | `UsuarioMongoAdapterTest` | `listarFiltraPorRol` | Infraestructura | AB#61 | PR por abrir | 🔍 |
| HU19-CP05 | `UsuarioMongoAdapterTest` | `listarFiltraPorBloqueo` | Infraestructura | AB#61 | PR por abrir | 🔍 |
| HU19-CP06 | `UsuarioMongoAdapterTest` | `listarAplicaPaginacionYConservaElTotal` | Infraestructura | AB#61 | PR por abrir | 🔍 |
| HU19-CP07 | `UsuarioMongoAdapterTest` | `listarEscapaCaracteresEspecialesDeLaBusqueda` | Infraestructura | AB#61 | PR por abrir | 🔍 |
| HU19-CP08 | `UsuarioControllerTest` | `administradorRecibeElListado` | Infraestructura | AB#61/AB#62 | PR por abrir | 🔍 |
| HU19-CP09 | `UsuarioDocumentMapperTest` | `conservaEstadoPendienteEnElMapeo` | Infraestructura | AB#61 | PR por abrir | 🔍 |
| HU19-CP10 | `UsuarioMongoAdapterTest` | `listarFiltraPorBloqueoTemporal` | Infraestructura | AB#61 | PR por abrir | 🔍 |

Rutas de las clases de test (bajo `backend/src/test/java/es/uclm/esibuy/backend/`):
- `RegistroUsuarioServiceTest`: `usuarios/application/service/`
- `ListadoUsuariosServiceTest`: `usuarios/application/service/`
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
| HU2-CP01 | Inicia sesión con credenciales correctas y pasa al paso del código por email | Unitario + integración | 4 | ✅ Pasa |
| HU2-CP02 | Rechaza credenciales incorrectas sin revelar si el correo existe | Unitario + integración | 4 | ✅ Pasa |
| HU2-CP03 | Bloquea la cuenta durante 15 minutos tras 5 intentos fallidos | Unitario + integración | 5 | ✅ Pasa |
| HU2-CP04 | Rechaza el acceso a una cuenta desactivada | Unitario + integración | 3 | ✅ Pasa |
| HU2-CP05 | Al validar las credenciales, envía al correo del usuario un código de verificación | Unitario | 3 | ✅ Pasa |
| HU2-CP06 | Un código correcto pasa al paso de Microsoft Authenticator | Unitario | 2 | ✅ Pasa (1) · 🔍 En revisión (1, AB#232) |
| HU2-CP07 | Rechaza un código incorrecto | Unitario + integración | 4 | ✅ Pasa (2) · 🔍 En revisión (2, AB#232) |
| HU2-CP08 | Rechaza un código caducado (más de 10 minutos) | Unitario + integración | 3 | ✅ Pasa (2) · 🔍 En revisión (1, AB#232) |
| HU2-CP09 | Un código ya usado no se puede volver a usar | Unitario + integración | 6 | ✅ Pasa (2) · 🔍 En revisión (4, AB#232) |
| HU2-CP10 | Tras 5 intentos fallidos el código deja de valer, incluso el correcto | Unitario | 3 | ✅ Pasa (2) · 🔍 En revisión (1, AB#232) |
| HU2-CP11 | Un login nuevo sustituye al código anterior | Integración | 1 | 🔍 En revisión (AB#232) |
| HU2-CP12 | El código se guarda hasheado y asociado al usuario, nunca en claro | Unitario + integración | 5 | ✅ Pasa (3) · 🔍 En revisión (2, AB#232) |
| HU2-CP13 | El código de verificación tiene 6 dígitos aleatorios | Unitario | 3 | ✅ Pasa |

Notas:
- **HU2-CP01 a CP04:** casos definidos en la T19/T22 a partir del contrato del frontend (`frontend/src/mocks/authHandlers.js`). Pendientes de añadir a la descripción de la T22 en Azure Boards.
- **HU2-CP04:** «desactivada» es la cuenta desactivada por un administrador (`desactivar()` de `Usuario`), distinta del bloqueo temporal de CP03.
- **Persistencia (T225, AB#225):** `UsuarioDocument` guarda los intentos fallidos, el bloqueo y la desactivación, así que CP03 y CP04 se cumplen también en la aplicación real. Los tests de integración de `UsuarioMongoAdapterTest` lo comprueban contra MongoDB.
- **AB#210:** el PR #43 guarda esos campos con los nombres del modelo de BBDD (`estado` ACTIVO/DESACTIVADO, `failedLoginAttempts`, `lockedUntil`) y amplía `conservaLaDesactivacion` y `conservaLosIntentosFallidosYElBloqueo` para comprobar esos nombres en el documento guardado.
- **HU2-CP05 a CP12:** casos definidos en la T21 (AB#21, tests con AB#22) a partir del contrato del frontend (`frontend/docs/hu2-login.md`). Pendientes de confirmar con el equipo y de añadir a la descripción de la T22 en Azure Boards. Reglas del código: 6 dígitos, válido 10 minutos, de un solo uso y se agota tras 5 intentos fallidos.
- **HU2-CP11:** que un login nuevo sustituya al código anterior es responsabilidad del adaptador de persistencia de `CodigoVerificacionRepositoryPort` (`tokens_seguridad`). Lo prueba `CodigoVerificacionMongoAdapterTest` (T232).
- **T21:** la T21 solo implementa el caso de uso con mocks. El endpoint, el envío real del correo y la persistencia de los códigos van en tasks nuevas. Los tres tests de la T19 que terminan con un login correcto (`iniciaSesionConCredencialesCorrectas`, `permiteCuartoIntentoFallidoSinBloquear` y `desbloqueaLaCuentaPasados15Minutos`) crean ahora el usuario con id, porque el código se asocia al id del usuario; lo que comprueban no cambia.
- **Persistencia de los códigos (T232, AB#232):** `CodigoVerificacionMongoAdapter` guarda los códigos en `tokens_seguridad` (`purpose` LOGIN_2FA, `tokenHash`, `expiresAt`, `attempts`, `usedAt`). Sus tests de integración comprueban contra MongoDB CP07, CP08, CP09, CP11 y CP12. En el dominio, `CodigoVerificacion` guarda la hora de uso (`usadoEn`, ampliado `codigoCorrectoPasaAlAuthenticator`) y se puede reconstruir desde la base de datos. El adaptador todavía no se registra como bean: lo hará la task de endpoints del login.
- **T231 (AB#231):** envío real del código por correo (Brevo por SMTP, `SmtpEnvioEmailAdapter`) y generador del código con `SecureRandom` (`SecureRandomGeneradorCodigoAdapter`). CP05 gana dos tests de infraestructura (el mensaje que se envía y el fallo del envío sin revelar el código).
- **HU2-CP13:** caso nuevo de la T231 para el formato del código (6 dígitos, con ceros a la izquierda, de 000000 a 999999), que es una regla propia (D3 de la T21) y no la cubría ningún caso. Pendiente de añadir a la descripción de la HU2 en Azure Boards.
- Los casos de Microsoft Authenticator (T20) se añadirán como CP14 en adelante (CP13 lo ocupa la T231).

### Tabla B · casos de prueba ↔ tests

| Caso | Clase de test | Método | Capa | Task | Origen | Estado |
|---|---|---|---|---|---|---|
| HU2-CP01 | `AutenticacionServiceTest` | `iniciaSesionConCredencialesCorrectas` | Aplicación | AB#22 | PR #36 | ✅ |
| HU2-CP01 | `UsuarioDocumentMapperTest` | `convierteDocumentoEnUsuario` | Infraestructura | AB#224 | PR #36 | ✅ |
| HU2-CP01 | `UsuarioMongoAdapterTest` | `buscarPorCorreoDevuelveElUsuarioGuardado` | Infraestructura | AB#224 | PR #36 | ✅ |
| HU2-CP01 | `UsuarioDocumentMapperTest` | `unDocumentoAntiguoSinCamposNuevosTomaLosValoresPorDefecto` | Infraestructura | AB#225 | PR #46 | ✅ |
| HU2-CP02 | `AutenticacionServiceTest` | `rechazaCorreoInexistente` | Aplicación | AB#22 | PR #36 | ✅ |
| HU2-CP02 | `AutenticacionServiceTest` | `rechazaContrasenaIncorrecta` | Aplicación | AB#22 | PR #36 | ✅ |
| HU2-CP02 | `BCryptHashContrasenaAdapterTest` | `coincideConElHashDeLaMismaContrasena` | Infraestructura | AB#22 | PR #36 | ✅ |
| HU2-CP02 | `UsuarioMongoAdapterTest` | `buscarPorCorreoDevuelveVacioSiNoEstaGuardado` | Infraestructura | AB#224 | PR #36 | ✅ |
| HU2-CP03 | `AutenticacionServiceTest` | `permiteCuartoIntentoFallidoSinBloquear` | Aplicación | AB#22 | PR #36 | ✅ |
| HU2-CP03 | `AutenticacionServiceTest` | `bloqueaLaCuentaTrasCincoIntentosFallidos` | Aplicación | AB#22 | PR #36 | ✅ |
| HU2-CP03 | `AutenticacionServiceTest` | `desbloqueaLaCuentaPasados15Minutos` | Aplicación | AB#22 | PR #36 | ✅ |
| HU2-CP03 | `UsuarioMongoAdapterTest` | `guardarUnUsuarioLeidoLoActualizaSinDuplicarlo` | Infraestructura | AB#225 | PR #46 | ✅ |
| HU2-CP03 | `UsuarioMongoAdapterTest` | `conservaLosIntentosFallidosYElBloqueo` | Infraestructura | AB#225 · AB#210 | PR #46 (ampliado en PR #43) | ✅ |
| HU2-CP04 | `AutenticacionServiceTest` | `rechazaCuentaDesactivada` | Aplicación | AB#22 | PR #36 | ✅ |
| HU2-CP04 | `UsuarioMongoAdapterTest` | `conservaLaDesactivacion` | Infraestructura | AB#225 · AB#210 | PR #46 (ampliado en PR #43) | ✅ |
| HU2-CP04 | `UsuarioMongoAdapterTest` | `conservaUnUsuarioActivo` | Infraestructura | AB#225 | PR #46 | ✅ |
| HU2-CP05 | `AutenticacionServiceTest` | `enviaElCodigoAlCorreoTrasValidarLasCredenciales` | Aplicación | AB#22 | PR #52 | ✅ |
| HU2-CP05 | `SmtpEnvioEmailAdapterTest` | `enviaElCodigoAlCorreoDelUsuario` | Infraestructura | AB#231 | PR #54 | ✅ |
| HU2-CP05 | `SmtpEnvioEmailAdapterTest` | `fallaSinRevelarElCodigo` | Infraestructura | AB#231 | PR #54 | ✅ |
| HU2-CP06 | `CodigoVerificacionTest` | `codigoNuevoSePuedeUsar` | Dominio | AB#22 | PR #52 | ✅ |
| HU2-CP06 | `AutenticacionServiceTest` | `codigoCorrectoPasaAlAuthenticator` | Aplicación | AB#22 · AB#232 | PR #52 (ampliado en PR #56) | 🔍 |
| HU2-CP07 | `AutenticacionServiceTest` | `rechazaCodigoIncorrecto` | Aplicación | AB#22 | PR #52 | ✅ |
| HU2-CP07 | `AutenticacionServiceTest` | `rechazaSiNoHayCodigoPendiente` | Aplicación | AB#22 | PR #52 | ✅ |
| HU2-CP08 | `CodigoVerificacionTest` | `caducaALosDiezMinutos` | Dominio | AB#22 | PR #52 | ✅ |
| HU2-CP08 | `AutenticacionServiceTest` | `rechazaCodigoCaducado` | Aplicación | AB#22 | PR #52 | ✅ |
| HU2-CP09 | `CodigoVerificacionTest` | `noSePuedeUsarDosVeces` | Dominio | AB#22 | PR #52 | ✅ |
| HU2-CP09 | `AutenticacionServiceTest` | `rechazaCodigoYaUsado` | Aplicación | AB#22 | PR #52 | ✅ |
| HU2-CP10 | `CodigoVerificacionTest` | `seAgotaTrasCincoIntentosFallidos` | Dominio | AB#22 | PR #52 | ✅ |
| HU2-CP10 | `AutenticacionServiceTest` | `rechazaCodigoAgotadoAunqueSeaCorrecto` | Aplicación | AB#22 | PR #52 | ✅ |
| HU2-CP12 | `CodigoVerificacionTest` | `rechazaCodigoSinUsuarioOSinHash` | Dominio | AB#22 | PR #52 | ✅ |
| HU2-CP12 | `AutenticacionServiceTest` | `guardaElCodigoHasheado` | Aplicación | AB#22 | PR #52 | ✅ |
| HU2-CP12 | `VerificarCodigoEmailCommandTest` | `toStringNoMuestraElCodigo` | Aplicación | AB#22 | PR #52 | ✅ |
| HU2-CP09 | `CodigoVerificacionTest` | `marcarUsadoGuardaLaHoraDeUso` | Dominio | AB#232 | PR #56 | 🔍 |
| HU2-CP09 | `CodigoVerificacionTest` | `reconstruirConservaElEstado` | Dominio | AB#232 | PR #56 | 🔍 |
| HU2-CP10 | `CodigoVerificacionTest` | `rechazaReconstruirConIntentosNegativos` | Dominio | AB#232 | PR #56 | 🔍 |
| HU2-CP12 | `CodigoVerificacionDocumentMapperTest` | `convierteCodigoEnDocumento` | Infraestructura | AB#232 | PR #56 | 🔍 |
| HU2-CP09 | `CodigoVerificacionDocumentMapperTest` | `convierteDocumentoEnCodigo` | Infraestructura | AB#232 | PR #56 | 🔍 |
| HU2-CP12 | `CodigoVerificacionMongoAdapterTest` | `guardaElCodigoEnTokensSeguridad` | Infraestructura | AB#232 | PR #56 | 🔍 |
| HU2-CP08 | `CodigoVerificacionMongoAdapterTest` | `buscarPorUsuarioDevuelveElCodigoGuardado` | Infraestructura | AB#232 | PR #56 | 🔍 |
| HU2-CP07 | `CodigoVerificacionMongoAdapterTest` | `buscarPorUsuarioDevuelveVacioSinCodigo` | Infraestructura | AB#232 | PR #56 | 🔍 |
| HU2-CP11 | `CodigoVerificacionMongoAdapterTest` | `unCodigoNuevoSustituyeAlAnterior` | Infraestructura | AB#232 | PR #56 | 🔍 |
| HU2-CP09 | `CodigoVerificacionMongoAdapterTest` | `guardarUnCodigoLeidoLoActualiza` | Infraestructura | AB#232 | PR #56 | 🔍 |
| HU2-CP07 | `CodigoVerificacionMongoAdapterTest` | `noDevuelveTokensDeOtroProposito` | Infraestructura | AB#232 | PR #56 | 🔍 |
| HU2-CP13 | `SecureRandomGeneradorCodigoAdapterTest` | `generaUnCodigoDeSeisDigitos` | Infraestructura | AB#231 | PR #54 | ✅ |
| HU2-CP13 | `SecureRandomGeneradorCodigoAdapterTest` | `rellenaConCerosALaIzquierda` | Infraestructura | AB#231 | PR #54 | ✅ |
| HU2-CP13 | `SecureRandomGeneradorCodigoAdapterTest` | `usaTodoElRangoDeSeisDigitos` | Infraestructura | AB#231 | PR #54 | ✅ |

Rutas de las clases de test (bajo `backend/src/test/java/es/uclm/esibuy/backend/`):
- `AutenticacionServiceTest`: `usuarios/application/service/` (Mockito, con `Clock` fijo)
- `CodigoVerificacionTest`: `usuarios/domain/model/` (JUnit puro, sin Spring)
- `VerificarCodigoEmailCommandTest`: `usuarios/application/port/in/`
- `BCryptHashContrasenaAdapterTest` y `SecureRandomGeneradorCodigoAdapterTest`: `usuarios/infrastructure/adapter/out/seguridad/`
- `SmtpEnvioEmailAdapterTest`: `usuarios/infrastructure/adapter/out/email/` (Mockito, con un mock de `JavaMailSender`; no envía correos reales)
- `UsuarioDocumentMapperTest`, `UsuarioMongoAdapterTest`, `CodigoVerificacionDocumentMapperTest` y `CodigoVerificacionMongoAdapterTest`: `usuarios/infrastructure/adapter/out/persistence/` (los adaptadores, con Testcontainers)

---

## HU18 · Acceso restringido al panel de administración

### Tabla A · HU18 ↔ casos de prueba

| Caso | Descripción | Tipo | Tests | Estado |
|---|---|---|---|---|
| HU18-CP01 | Sin sesión no se accede a la zona de administración | Integración (capa web) | 1 | 🔍 En revisión |
| HU18-CP02 | Un cliente o un vendedor no accede a la zona de administración | Integración (capa web) | 2 | 🔍 En revisión |
| HU18-CP03 | Un administrador accede a la zona de administración | Integración (capa web) | 1 | 🔍 En revisión |

Notas:
- **HU18-CP01 a CP03:** casos propuestos en la T58 (AB#58) a partir de los criterios de aceptación de la HU18 (`frontend/docs/hu18-panel-admin.md`). Pendientes de confirmar con el equipo y de añadir a la HU18 en Azure Boards.
- **Backend (T58):** `SecurityConfig` solo deja usar `/api/admin/**` al rol `ADMINISTRADOR`. Todavía no hay login ni endpoints de administración: los tests simulan los usuarios con `@WithMockUser` y usan un endpoint de prueba que solo existe en el test. El login (task 230) tendrá que dar a cada usuario la autoridad `ROLE_<rol>`.
- **HU18-CP01:** sin sesión responde 403 y no 401 porque todavía no hay mecanismo de login configurado; la task 230 podrá cambiarlo.
- **Frontend:** la ruta protegida y el menú del panel (HU18) no tienen todavía filas en la matriz.

### Tabla B · casos de prueba ↔ tests

| Caso | Clase de test | Método | Capa | Task | Origen | Estado |
|---|---|---|---|---|---|---|
| HU18-CP01 | `SecurityConfigTest` | `sinSesionNoSeAccedeAAdministracion` | Infraestructura | AB#58 | PR por abrir | 🔍 |
| HU18-CP02 | `SecurityConfigTest` | `clienteNoAccedeAAdministracion` | Infraestructura | AB#58 | PR por abrir | 🔍 |
| HU18-CP02 | `SecurityConfigTest` | `vendedorNoAccedeAAdministracion` | Infraestructura | AB#58 | PR por abrir | 🔍 |
| HU18-CP03 | `SecurityConfigTest` | `administradorAccedeAAdministracion` | Infraestructura | AB#58 | PR por abrir | 🔍 |

Rutas de las clases de test (bajo `backend/src/test/java/es/uclm/esibuy/backend/`):
- `SecurityConfigTest`: `shared/config/` (con `@WebMvcTest` y usuarios simulados con `@WithMockUser`)

---

## HU19 · Listar y buscar usuarios

### Tabla A · HU19 ↔ casos de prueba

| Caso | Descripción | Tipo | Tests | Estado |
|---|---|---|---|---|
| HU19-CP01 | Muestra nombre, email, rol, estado y fecha de alta de cada usuario | Unitario + integración | 4 | 🔍 En revisión |
| HU19-CP02 | Busca por nombre o email | Unitario + integración | 5 | 🔍 En revisión |
| HU19-CP03 | Filtra por rol | Unitario + integración | 4 | 🔍 En revisión |
| HU19-CP04 | Filtra por estado | Unitario + integración | 4 | 🔍 En revisión |
| HU19-CP05 | El listado está paginado de 10 en 10 | Unitario + integración | 5 | 🔍 En revisión |
| HU19-CP06 | Al cambiar la búsqueda o un filtro vuelve a la primera página | Integración | 2 | ✅ Pasa |
| HU19-CP07 | Sin coincidencias muestra que no se han encontrado usuarios | Integración | 2 | ✅ Pasa |
| HU19-CP08 | Si la API falla muestra un mensaje de error con reintento | Unitario + integración | 3 | ✅ Pasa |

Notas:
- **HU19-CP01 a CP08:** casos definidos en la T63/T64 a partir de los criterios de aceptación de la HU19 más los estados vacío y de error. Pendientes de añadir a la descripción de las tasks en Azure Boards.
- **Frontend:** son los primeros tests de frontend de la matriz (Vitest + React Testing Library + MSW). En lugar de `@DisplayName`, el ID del caso va al principio del nombre del test: `it('HU19-CPnn: ...')`. Los tests de integración recorren la página completa contra el mock de MSW (`mocks/usersHandlers.js`).
- **Backend:** el contrato que usan estos tests (`GET /api/admin/usuarios`) está pendiente de confirmar con el backend de la HU19 (AB#61/62). Ver `frontend/docs/hu19-listado-usuarios.md`.
- **AB#63 / AB#64:** los tests de `users.test.js` se escribieron en la T63 (ciclos 1 a 5, antes de separar las tasks); el resto, en la T64.

### Tabla B · casos de prueba ↔ tests

| Caso | Archivo de test | Test | Capa | Task | Origen | Estado |
|---|---|---|---|---|---|---|
| HU19-CP01 | `UsersTable.test.jsx` | `muestra las cabeceras de las cinco columnas` | Frontend · componente | AB#64 | PR #41 | ✅ |
| HU19-CP01 | `UsersTable.test.jsx` | `muestra cada usuario con rol y estado legibles y la fecha en dd/mm/aaaa` | Frontend · componente | AB#64 | PR #41 | ✅ |
| HU19-CP01 | `AdminUsersPage.test.jsx` | `con sesión de admin muestra el título y los 10 primeros usuarios` | Frontend · página | AB#64 | PR #41 | ✅ |
| HU19-CP02 | `users.test.js` | `envía la búsqueda solo cuando tiene texto` | Frontend · API | AB#63 | PR #41 | ✅ |
| HU19-CP02 | `UsersFilters.test.jsx` | `avisa del texto escrito en el buscador por nombre o email` | Frontend · componente | AB#64 | PR #41 | ✅ |
| HU19-CP02 | `AdminUsersPage.test.jsx` | `al buscar "ana" muestra solo los usuarios que coinciden` | Frontend · página | AB#64 | PR #41 | ✅ |
| HU19-CP03 | `users.test.js` | `envía el rol cuando se filtra por rol` | Frontend · API | AB#63 | PR #41 | ✅ |
| HU19-CP03 | `UsersFilters.test.jsx` | `avisa del rol elegido en el filtro de rol` | Frontend · componente | AB#64 | PR #41 | ✅ |
| HU19-CP03 | `AdminUsersPage.test.jsx` | `al filtrar por Vendedor muestra solo vendedores` | Frontend · página | AB#64 | PR #41 | ✅ |
| HU19-CP04 | `users.test.js` | `envía el estado cuando se filtra por estado` | Frontend · API | AB#63 | PR #41 | ✅ |
| HU19-CP04 | `UsersFilters.test.jsx` | `avisa del estado elegido en el filtro de estado` | Frontend · componente | AB#64 | PR #41 | ✅ |
| HU19-CP04 | `AdminUsersPage.test.jsx` | `al filtrar por Pendiente muestra solo cuentas pendientes` | Frontend · página | AB#64 | PR #41 | ✅ |
| HU19-CP05 | `users.test.js` | `pide la página y el tamaño indicados` | Frontend · API | AB#63 | PR #41 | ✅ |
| HU19-CP05 | `AdminUsersPage.test.jsx` | `al pulsar "Siguiente" muestra la página 2 de 3` | Frontend · página | AB#64 | PR #41 | ✅ |
| HU19-CP05 | `AdminUsersPage.test.jsx` | `mientras llega la página siguiente la tabla se marca como ocupada` | Frontend · página | AB#64 | PR #41 | ✅ |
| HU19-CP06 | `AdminUsersPage.test.jsx` | `estando en la página 2, al buscar vuelve a la página 1` | Frontend · página | AB#64 | PR #41 | ✅ |
| HU19-CP06 | `AdminUsersPage.test.jsx` | `estando en la página 2, al filtrar vuelve a la página 1` | Frontend · página | AB#64 | PR #41 | ✅ |
| HU19-CP07 | `AdminUsersPage.test.jsx` | `sin coincidencias muestra que no se han encontrado usuarios` | Frontend · página | AB#64 | PR #41 | ✅ |
| HU19-CP07 | `AdminUsersPage.test.jsx` | `sin coincidencias, "Limpiar filtros" vuelve a mostrar todos los usuarios` | Frontend · página | AB#64 | PR #41 | ✅ |
| HU19-CP08 | `users.test.js` | `propaga un ApiError si la respuesta no es correcta` | Frontend · API | AB#63 | PR #41 | ✅ |
| HU19-CP08 | `AdminUsersPage.test.jsx` | `si la API responde 500 muestra un mensaje de error` | Frontend · página | AB#64 | PR #41 | ✅ |
| HU19-CP08 | `AdminUsersPage.test.jsx` | `tras un error, "Reintentar" vuelve a cargar el listado` | Frontend · página | AB#64 | PR #41 | ✅ |
| HU19-CP01 | `ListadoUsuariosServiceTest` | `listarDelegaLaConsultaIntactaAlRepositorio` | Aplicación | AB#62 | PR por abrir | 🔍 |
| HU19-CP02 | `UsuarioMongoAdapterTest` | `listarBuscaEnLosCamposVisiblesDelUsuario` | Infraestructura | AB#61 | PR por abrir | 🔍 |
| HU19-CP02 | `UsuarioMongoAdapterTest` | `listarEscapaCaracteresEspecialesDeLaBusqueda` | Infraestructura | AB#61 | PR por abrir | 🔍 |
| HU19-CP03 | `UsuarioMongoAdapterTest` | `listarFiltraPorRol` | Infraestructura | AB#61 | PR por abrir | 🔍 |
| HU19-CP04 | `UsuarioMongoAdapterTest` | `listarFiltraPorEstadoActivo` | Infraestructura | AB#61 | PR por abrir | 🔍 |
| HU19-CP05 | `ListadoUsuariosServiceTest` | `listarDevuelveLaPaginaProporcionadaPorElRepositorio` | Aplicación | AB#62 | PR por abrir | 🔍 |
| HU19-CP05 | `UsuarioMongoAdapterTest` | `listarAplicaPaginacionYConservaElTotal` | Infraestructura | AB#61 | PR por abrir | 🔍 |

El test web de acceso sin sesión está en `UsuarioControllerTest`, pero se considera técnico de seguridad y no un caso funcional de HU19.

Rutas de los archivos de test (bajo `frontend/src/`):
- `users.test.js`: `api/`
- `UsersTable.test.jsx` y `UsersFilters.test.jsx`: `features/admin/`
- `AdminUsersPage.test.jsx`: `pages/` (integración: rutas protegidas, sesión simulada y mock de MSW)

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
| `UsuarioMongoAdapterTest` | `elDocumentoNoIncluyeElCampoClass` | Que los documentos guardados no incluyen el campo `_class` de Spring Data (`MongoConfig`; AB#210, PR #43) |
| `UsuarioMongoAdapterTest` | `guardaFechasDeCreacionYModificacion` | Que se guardan `createdAt` y `updatedAt` (auditoría de Spring Data) y que `createdAt` no cambia al actualizar (AB#210, PR #43) |
| `InicializadorColeccionesTest` | `creaLasDiezColecciones` | Que al arrancar se crean las 10 colecciones del modelo de BBDD (AB#210, PR #43) |
| `InicializadorColeccionesTest` | `sePuedeEjecutarOtraVez` | Que el inicializador se puede ejecutar en cada arranque sin fallar ni duplicar índices (AB#210, PR #43) |
| `InicializadorColeccionesTest` | `actualizaLaValidacionDeUnaColeccionExistente` | Que aplica la validación a una colección que ya existía sin ella (AB#210, PR #43) |
| `InicializadorColeccionesTest` | `sinPermisoParaCambiarLaValidacionNoFalla` | Que sin permiso `collMod` (usuario de la aplicación en Atlas) avisa y la aplicación arranca igual (AB#210, PR #43) |
| `InicializadorColeccionesTest` | `aceptaUsuariosValidos` | Que la validación de `usuarios` acepta los documentos de la aplicación, los campos de vendedor y administrador, `lockedUntil` nulo y campos nuevos (AB#210, PR #43) |
| `InicializadorColeccionesTest` | `rechazaUsuariosInvalidos` | Que la validación de `usuarios` rechaza campos obligatorios ausentes y valores fuera del modelo (AB#210, PR #43) |
| `InicializadorColeccionesTest` | `usuariosConDniYNombreComercialUnicos` | Índices de `usuarios`: DNI único por rol (opcional) y nombre comercial único (AB#210, PR #43) |
| `InicializadorColeccionesTest` | `validaSesionesFactoresYTokens` | Validación de `sesiones`, `factores_autenticacion` y `tokens_seguridad`, con `tokenHash` obligatorio (AB#210, PR #43; ampliado en AB#232, PR #56) |
| `InicializadorColeccionesTest` | `validaProductos` | Validación de `productos`: precio mayor que 0, stock no negativo, nombre obligatorio (AB#210, PR #43) |
| `InicializadorColeccionesTest` | `validaDescuentosDeProductos` | Descuentos de `productos` con el formato del formulario de producto (HU31): `{activo, porcentaje}` (AB#210, PR #43) |
| `InicializadorColeccionesTest` | `validaElRestoDeColecciones` | Validación de `pedidos`, `carritos`, `valoraciones`, `categorias` y `listas_deseos` (AB#210, PR #43) |
| `InicializadorColeccionesTest` | `rechazaDuplicadosEnLosIndicesUnicos` | Índices únicos de `carritos`, `valoraciones`, `categorias`, `listas_deseos` y `factores_autenticacion` (AB#210, PR #43) |
| `InicializadorColeccionesTest` | `creaLosIndicesDeLasAnotaciones` | Que también crea los índices de las anotaciones de los `@Document` (email único en `usuarios`) (AB#210, PR #43) |
| `InicializadorColeccionesTest` | `sesionesYTokensCaducan` | Índices TTL de `expiresAt` en `sesiones` y `tokens_seguridad` (AB#210, PR #43) |

`InicializadorColeccionesTest` está en `shared/infrastructure/config/` y usa Testcontainers. HU36 (crear la base de datos) no tiene casos de prueba en Azure Boards, por eso sus tests están en esta sección.
