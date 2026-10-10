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
| HU1-CP01 | Registra un cliente con rol CLIENTE y estado PENDIENTE | Unitario + integración | 7 | ✅ Pasa |
| HU1-CP02 | Guarda la contraseña hasheada, nunca en claro | Unitario + integración | 7 | ✅ Pasa |
| HU1-CP03 | Rechaza un correo ya registrado y no guarda nada | Unitario + integración | 5 | ✅ Pasa |
| HU1-CP04 | Rechaza un username ya registrado | — | 0 | ⏸ Fuera de alcance (no está decidido si existe username) |
| HU1-CP05 | Rechaza contraseñas de menos de 8 caracteres o de más de 64 | Unitario + integración | 10 | ✅ Pasa |
| HU1-CP06 | Rechaza el registro si no se acepta la política de privacidad y guarda el consentimiento | Unitario | 4 | ✅ Pasa |
| HU1-CP07 | Rechaza un email con formato inválido | Unitario + integración (capa web) | 8 | ✅ Pasa |
| HU1-CP08 | Rechaza el registro si falta un campo obligatorio o supera su longitud máxima | Unitario + integración (capa web) | 11 | ✅ Pasa |
| HU1-CP09 | Rechaza contraseñas de la lista de contraseñas comunes | Unitario | 3 | ✅ Pasa |
| HU1-CP10 | Rechaza contraseñas que contienen datos personales del usuario | Unitario | 4 | ✅ Pasa |
| HU1-CP11 | El usuario cumple sus invariantes de dominio (datos personales y de cliente obligatorios y coherentes con el rol, desactivación, fecha de alta) | Unitario | 17 | ✅ Pasa |
| HU1-CP12 | Toda cuenta nueva nace en estado PENDIENTE hasta que un administrador la verifica | Unitario | 4 | ✅ Pasa |
| HU1-CP13 | Rechaza a un cliente menor de edad | Unitario | 4 | ✅ Pasa |
| HU1-CP14 | Rechaza el registro si las contraseñas no coinciden | Unitario | 1 | ✅ Pasa |
| HU1-CP15 | Rechaza un DNI o un teléfono con formato inválido | Unitario + integración (capa web) | 7 | ✅ Pasa |
| HU1-CP16 | Rechaza un DNI ya registrado | Unitario + integración | 4 | ✅ Pasa |

Notas:
- **HU1-CP11:** en el PR #36, el bloqueo manual de la T214 se renombra a desactivar/activar (`desactivar()`, `activar()`, `estaDesactivado()`) para no confundirlo con el bloqueo temporal por intentos del login (HU2-CP03). En la T225 (AB#225) la consulta pasa a nombrarse en positivo: `estaActivo()`.
- **HU1-CP12 (AB#233):** toda cuenta nueva nace en estado `PENDIENTE` hasta que un administrador la verifica, y `estaActivo()` se calcula desde `estado` (el booleano `activo` se eliminó). Decisión de la daily del 09/10/2026; sustituye a la versión del PR #66, que dejaba el constructor en `ACTIVO`. Decidido por el equipo (10/10/2026): un administrador creado por otro administrador nace `ACTIVO`, porque lo verifica quien lo crea; la regla se aplicará con el alta de administradores (HU21), que aún no existe.
- **AB#216:** los tests de la capa de infraestructura (persistencia en MongoDB) se asignan a CP01, CP02 y CP03 como propuesta, pendiente de confirmar con el equipo.
- **HU1-CP07 y HU1-CP08:** casos propuestos en la T14 (AB#14) para la validación de entrada del endpoint de registro. Pendientes de confirmar con el equipo.
- **AB#227:** la validación web (`@Email`) acepta un email sin punto en el dominio, como `ana@localhost`, pero el value object `Email` lo rechaza. Antes de la T227 esa `EmailInvalidoException` no se manejaba y el registro respondía con un 500. Ahora `ManejadorGlobalErrores` la traduce a un 400 con el mismo mensaje genérico que una contraseña inválida.
- **HU1-CP09, HU1-CP10 y HU1-CP11:** casos propuestos en la T214 (AB#214, PR #34) para la capa de dominio. Pendientes de confirmar con el equipo y de dar de alta en Azure Boards. CP09 y CP10 salen de los requisitos de Seguridad (lista negra y «evitar datos personales»).
- **AB#226:** `RegistroUsuarioService` aplica `PoliticaContrasenas` (que ahora recibe `DatosPersonales` y limita a 64 caracteres) y deja de comprobar la longitud por su cuenta. La frontera de 8 caracteres usa una contraseña que no está en la lista negra.
- **AB#229 (registro del cliente con todos los campos):** el dominio agrupa los datos en value objects (`DatosPersonales`, `DatosCliente`, `Dni`, `Telefono`, `ConsentimientoPrivacidad`); nombre y apellidos pasan a ser obligatorios, por lo que se eliminan `permiteUsuarioSinNombreNiApellidos` y `creaClienteNuevoEstandarSinNombreNiId` (T214) y las comprobaciones de nombre/apellidos en blanco pasan de `UsuarioTest` a `DatosPersonalesTest`. Casos propuestos, pendientes de confirmar con el equipo y de dar de alta en Azure Boards: CP13 (mayoría de edad, `Contexto_frontend.md` 6.3), CP14 (repetir contraseña), CP15 (formato de DNI y teléfono, MVP del Sprint 1) y CP16 (DNI único por rol, índice `rol_dni` de la T210). El registro responde en `POST /api/auth/register/cliente` y el 409 lleva `code` (`EMAIL_ALREADY_REGISTERED` / `DNI_ALREADY_REGISTERED`), contrato acordado con el frontend.
- **AB#228:** `UsuarioInvalidoException` se traduce a 400 en el registro (datos del cliente). Un documento de MongoDB que no cumple las reglas del dominio se envuelve en `UsuarioPersistidoInvalidoException` (infraestructura) y acaba en 500. PENDIENTE DE REVISAR CON EL EQUIPO: las cuentas de prueba de Atlas sin nombre, apellidos, DNI o fecha de nacimiento ya no se pueden leer.
- **AB#210:** el test de email duplicado (CP03) comprueba el índice único de `email` a nivel de base de datos, sin pasar por `existePorCorreo`. `conservaLosDatosDePerfil` (CP01) comprueba contra MongoDB lo que `conservaNombreApellidosYTipoDeCliente` (T225) prueba en el mapper. Al integrar `develop`, el PR #43 deja la persistencia del estado de acceso de la T225 (sin `Usuario.reconstruir` ni sus tests) y guarda sus campos con los nombres del modelo de BBDD (`estado`, `failedLoginAttempts`, `lockedUntil`); decisión del equipo del 08/10/2026.

### Tabla B · casos de prueba ↔ tests

| Caso | Clase de test | Método | Capa | Task | Origen | Estado |
|---|---|---|---|---|---|---|
| HU1-CP01 | `RegistroUsuarioServiceTest` | `registraClienteConRolCliente` | Aplicación | AB#16 | PR #14 | ✅ |
| HU1-CP01 | `UsuarioMongoAdapterTest` | `guardarPersisteElUsuarioEnLaColeccionUsuarios` | Infraestructura | AB#216 | PR #28 | ✅ |
| HU1-CP01 | `UsuarioControllerTest` | `registroValidoDevuelve201` | Infraestructura | AB#14 | PR #30 | ✅ |
| HU1-CP01 | `UsuarioDocumentMapperTest` | `conservaLosDatosDelClienteEnElMapeo` | Infraestructura | AB#225 · AB#229 | PR #46 (ampliado en PR #72) | ✅ |
| HU1-CP01 | `UsuarioMongoAdapterTest` | `conservaLosDatosDePerfil` | Infraestructura | AB#210 | PR #43 | ✅ |
| HU1-CP01 | `RegistroUsuarioServiceTest` | `registraClienteConTodosSusDatos` | Aplicación | AB#229 | PR #72 | ✅ |
| HU1-CP01 | `UsuarioDocumentMapperTest` | `convierteLosDatosDelClienteEnDocumento` | Infraestructura | AB#229 | PR #72 | ✅ |
| HU1-CP02 | `RegistroUsuarioServiceTest` | `guardaContrasenaHasheada` | Aplicación | AB#16 | PR #14 | ✅ |
| HU1-CP02 | `BCryptHashContrasenaAdapterTest` | `hashNoEsLaContrasenaEnClaro` | Infraestructura | AB#15 | PR #20 | ✅ |
| HU1-CP02 | `BCryptHashContrasenaAdapterTest` | `hashEsVerificableConBCrypt` | Infraestructura | AB#15 | PR #20 | ✅ |
| HU1-CP02 | `BCryptHashContrasenaAdapterTest` | `hashUsaElCosteAcordado` | Infraestructura | AB#15 | PR #20 | ✅ |
| HU1-CP02 | `RegistrarUsuarioCommandTest` | `toStringNoMuestraLaContrasena` | Aplicación | AB#223 | PR #26 | ✅ |
| HU1-CP02 | `UsuarioDocumentMapperTest` | `convierteUsuarioEnDocumento` | Infraestructura | AB#216 | PR #28 | ✅ |
| HU1-CP02 | `LoginRequestTest` | `toStringNoMuestraLaContrasena` | Infraestructura | AB#230 | PR #67 | ✅ |
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
| HU1-CP05 | `PoliticaContrasenasTest` | `rechazaContrasenaDe65Caracteres` | Dominio | AB#226 | PR #72 | ✅ |
| HU1-CP05 | `PoliticaContrasenasTest` | `aceptaContrasenaDe64Caracteres` | Dominio | AB#226 | PR #72 | ✅ |
| HU1-CP06 | `ConsentimientoPrivacidadTest` | `creaConsentimientoConVersion` | Dominio | AB#229 | PR #72 | ✅ |
| HU1-CP06 | `ConsentimientoPrivacidadTest` | `rechazaConsentimientoIncompleto` | Dominio | AB#229 | PR #72 | ✅ |
| HU1-CP06 | `RegistroUsuarioServiceTest` | `rechazaSiNoAceptaLaPrivacidad` | Aplicación | AB#229 | PR #72 | ✅ |
| HU1-CP06 | `RegistroUsuarioServiceTest` | `guardaElConsentimientoConLaFechaYLaVersion` | Aplicación | AB#229 | PR #72 | ✅ |
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
| HU1-CP08 | `DatosPersonalesTest` | `rechazaNombreEnBlanco` | Dominio | AB#229 | PR #72 | ✅ |
| HU1-CP08 | `DatosPersonalesTest` | `rechazaApellidosEnBlanco` | Dominio | AB#229 | PR #72 | ✅ |
| HU1-CP08 | `DatosPersonalesTest` | `rechazaNombreDe31Caracteres` | Dominio | AB#229 | PR #72 | ✅ |
| HU1-CP08 | `DatosPersonalesTest` | `aceptaNombreDe30Caracteres` | Dominio | AB#229 | PR #72 | ✅ |
| HU1-CP08 | `DatosClienteTest` | `rechazaSinTipoDeCliente` | Dominio | AB#229 | PR #72 | ✅ |
| HU1-CP08 | `DatosClienteTest` | `rechazaSinDni` | Dominio | AB#229 | PR #72 | ✅ |
| HU1-CP08 | `DatosClienteTest` | `rechazaSinFechaDeNacimiento` | Dominio | AB#229 | PR #72 | ✅ |
| HU1-CP08 | `UsuarioControllerTest` | `nombreDe31CaracteresDevuelve400` | Infraestructura | AB#229 | PR #72 | ✅ |
| HU1-CP08 | `UsuarioControllerTest` | `emailDe101CaracteresDevuelve400` | Infraestructura | AB#229 | PR #72 | ✅ |
| HU1-CP09 | `PoliticaContrasenasTest` | `rechazaContrasenaDeListaNegra` | Dominio | AB#214 | PR #34 | ✅ |
| HU1-CP09 | `PoliticaContrasenasTest` | `rechazaContrasenaDeListaNegraConMayusculas` | Dominio | AB#214 | PR #34 | ✅ |
| HU1-CP09 | `RegistroUsuarioServiceTest` | `rechazaContrasenaComun` | Aplicación | AB#226 | PR #72 | ✅ |
| HU1-CP10 | `PoliticaContrasenasTest` | `rechazaContrasenaQueContieneElNombre` | Dominio | AB#214 | PR #34 | ✅ |
| HU1-CP10 | `PoliticaContrasenasTest` | `rechazaContrasenaQueContieneLosApellidos` | Dominio | AB#214 | PR #34 | ✅ |
| HU1-CP10 | `PoliticaContrasenasTest` | `rechazaContrasenaQueContieneLaParteLocalDelEmail` | Dominio | AB#214 | PR #34 | ✅ |
| HU1-CP10 | `RegistroUsuarioServiceTest` | `rechazaContrasenaConElNombre` | Aplicación | AB#226 | PR #72 | ✅ |
| HU1-CP11 | `UsuarioTest` | `creaUsuarioConDatosCorrectos` | Dominio | AB#214 | PR #34 | ✅ |
| HU1-CP11 | `UsuarioTest` | `permiteUsuarioSinIdAntesDePersistir` | Dominio | AB#214 | PR #34 | ✅ |
| HU1-CP11 | `UsuarioTest` | `rechazaEmailNulo` | Dominio | AB#214 | PR #34 | ✅ |
| HU1-CP11 | `UsuarioTest` | `rechazaContrasenaHashEnBlanco` | Dominio | AB#214 | PR #34 | ✅ |
| HU1-CP11 | `UsuarioTest` | `rechazaRolNulo` | Dominio | AB#214 | PR #34 | ✅ |
| HU1-CP11 | `UsuarioTest` | `desactivaUsuario` | Dominio | AB#214 | PR #34 (renombrado en PR #36) | ✅ |
| HU1-CP11 | `UsuarioTest` | `activaUsuarioDesactivado` | Dominio | AB#214 | PR #34 (renombrado en PR #36) | ✅ |
| HU1-CP11 | `UsuarioTest` | `rechazaUsuarioSinDatosPersonales` | Dominio | AB#229 | PR #72 | ✅ |
| HU1-CP11 | `UsuarioTest` | `rechazaClienteSinDatosDeCliente` | Dominio | AB#229 | PR #72 | ✅ |
| HU1-CP11 | `UsuarioTest` | `rechazaDatosDeClienteSiNoEsCliente` | Dominio | AB#229 | PR #72 | ✅ |
| HU1-CP11 | `UsuarioTest` | `permiteVendedorYAdministradorSinDatosDeCliente` | Dominio | AB#229 | PR #72 | ✅ |
| HU1-CP11 | `UsuarioTest` | `unUsuarioNuevoNoTieneFechaDeAlta` | Dominio | AB#229 | PR #72 | ✅ |
| HU1-CP11 | `UsuarioTest` | `restauraLaFechaDeAlta` | Dominio | AB#229 | PR #72 | ✅ |
| HU1-CP11 | `DatosPersonalesTest` | `creaDatosPersonalesValidos` | Dominio | AB#229 | PR #72 | ✅ |
| HU1-CP11 | `DatosClienteTest` | `creaDatosClienteValidos` | Dominio | AB#229 | PR #72 | ✅ |
| HU1-CP11 | `UsuarioDocumentMapperTest` | `unVendedorSeLeeSinDatosDeCliente` | Infraestructura | AB#229 | PR #72 | ✅ |
| HU1-CP11 | `UsuarioDocumentMapperTest` | `documentoInvalidoNoSeConfundeConUnErrorDelCliente` | Infraestructura | AB#228 | PR #72 | ✅ |
| HU1-CP12 | `UsuarioTest` | `unUsuarioNuevoNaceEnEstadoPendiente` | Dominio | AB#233 | PR #72 | ✅ |
| HU1-CP12 | `UsuarioTest` | `unClienteNuevoNaceEnEstadoPendiente` | Dominio | AB#233 | PR #72 | ✅ |
| HU1-CP12 | `UsuarioTest` | `restaurarEstadoPendienteNoCuentaComoActivo` | Dominio | AB#233 | PR #72 | ✅ |
| HU1-CP12 | `UsuarioTest` | `restaurarEstadoActivoCuentaComoActivo` | Dominio | AB#233 | PR #72 | ✅ |
| HU1-CP13 | `UsuarioTest` | `nuevoClienteMayorDeEdadSeRegistra` | Dominio | AB#229 | PR #72 | ✅ |
| HU1-CP13 | `UsuarioTest` | `rechazaClienteMenorDeEdad` | Dominio | AB#229 | PR #72 | ✅ |
| HU1-CP13 | `UsuarioTest` | `aceptaClienteQueCumple18Hoy` | Dominio | AB#229 | PR #72 | ✅ |
| HU1-CP13 | `RegistroUsuarioServiceTest` | `rechazaClienteMenorDeEdad` | Aplicación | AB#229 | PR #72 | ✅ |
| HU1-CP14 | `RegistroUsuarioServiceTest` | `rechazaContrasenasQueNoCoinciden` | Aplicación | AB#229 | PR #72 | ✅ |
| HU1-CP15 | `DniTest` | `creaDniConFormatoValido` | Dominio | AB#229 | PR #72 | ✅ |
| HU1-CP15 | `DniTest` | `rechazaDniSinLetra` | Dominio | AB#229 | PR #72 | ✅ |
| HU1-CP15 | `DniTest` | `rechazaDniConSieteNumeros` | Dominio | AB#229 | PR #72 | ✅ |
| HU1-CP15 | `TelefonoTest` | `creaTelefonoDeNueveDigitos` | Dominio | AB#229 | PR #72 | ✅ |
| HU1-CP15 | `TelefonoTest` | `rechazaTelefonoConLetras` | Dominio | AB#229 | PR #72 | ✅ |
| HU1-CP15 | `TelefonoTest` | `rechazaTelefonoDeOchoDigitos` | Dominio | AB#229 | PR #72 | ✅ |
| HU1-CP15 | `UsuarioControllerTest` | `datosDelClienteInvalidosDevuelven400` | Infraestructura | AB#228 | PR #72 | ✅ |
| HU1-CP16 | `RegistroUsuarioServiceTest` | `rechazaDniYaRegistrado` | Aplicación | AB#229 | PR #72 | ✅ |
| HU1-CP16 | `UsuarioMongoAdapterTest` | `rechazaDniDuplicadoEnLaBaseDeDatos` | Infraestructura | AB#229 | PR #72 | ✅ |
| HU1-CP16 | `UsuarioMongoAdapterTest` | `existePorDniDistingueLosDniGuardados` | Infraestructura | AB#229 | PR #72 | ✅ |
| HU1-CP16 | `UsuarioControllerTest` | `dniYaRegistradoDevuelve409ConCodigo` | Infraestructura | AB#229 | PR #72 | ✅ |

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
| HU19-CP01 | `ListadoUsuariosServiceTest` | `listar_delegaLaConsultaIntactaAlRepositorio` | Aplicación | AB#62 | PR #59 | ✅ |
| HU19-CP02 | `ListadoUsuariosServiceTest` | `listar_devuelveLaPaginaProporcionadaPorElRepositorio` | Aplicación | AB#62 | PR #59 | ✅ |
| HU19-CP03 | `UsuarioMongoAdapterTest` | `listarBuscaEnEmailNombreYApellidos` | Infraestructura | AB#61 | PR #59 | ✅ |
| HU19-CP04 | `UsuarioMongoAdapterTest` | `listarFiltraPorRol` | Infraestructura | AB#61 | PR #59 | ✅ |
| HU19-CP05 | `UsuarioMongoAdapterTest` | `listarFiltraPorBloqueo` | Infraestructura | AB#61 | PR #59 | ✅ |
| HU19-CP06 | `UsuarioMongoAdapterTest` | `listarAplicaPaginacionYConservaElTotal` | Infraestructura | AB#61 | PR #59 | ✅ |
| HU19-CP07 | `UsuarioMongoAdapterTest` | `listarEscapaCaracteresEspecialesDeLaBusqueda` | Infraestructura | AB#61 | PR #59 | ✅ |
| HU19-CP08 | `UsuarioControllerTest` | `administradorRecibeElListado` | Infraestructura | AB#61/AB#62 | PR #59 | ✅ |
| HU19-CP09 | `UsuarioDocumentMapperTest` | `conservaEstadoPendienteEnElMapeo` | Infraestructura | AB#61 | PR #59 | ✅ |
| HU19-CP10 | `UsuarioMongoAdapterTest` | `listarFiltraPorBloqueoTemporal` | Infraestructura | AB#61 | PR #59 | ✅ |

Rutas de las clases de test (bajo `backend/src/test/java/es/uclm/esibuy/backend/`):
- `RegistroUsuarioServiceTest`: `usuarios/application/service/`
- `ListadoUsuariosServiceTest`: `usuarios/application/service/`
- `RegistrarUsuarioCommandTest`: `usuarios/application/port/in/`
- `BCryptHashContrasenaAdapterTest`: `usuarios/infrastructure/adapter/out/seguridad/`
- `UsuarioDocumentMapperTest` y `UsuarioMongoAdapterTest`: `usuarios/infrastructure/adapter/out/persistence/`
- `UsuariosConfigTest`: `usuarios/infrastructure/config/`
- `EmailTest`, `DniTest`, `TelefonoTest`, `DatosPersonalesTest`, `DatosClienteTest`, `ConsentimientoPrivacidadTest`, `PoliticaContrasenasTest` y `UsuarioTest`: `usuarios/domain/model/` (JUnit puro, sin Spring)
- `UsuarioControllerTest`: `usuarios/infrastructure/adapter/in/web/` (con `@WebMvcTest`; prueba también `SecurityConfig` y `ManejadorGlobalErrores` de `shared`)

Los tests de integración de MongoDB (`UsuarioMongoAdapterTest`, `UsuariosConfigTest`) usan Testcontainers (`MongoTestcontainersConfig`) y requieren Docker.

---

## HU2 · Inicio de sesión seguro

### Tabla A · HU2 ↔ casos de prueba

| Caso | Descripción | Tipo | Tests | Estado |
|---|---|---|---|---|
| HU2-CP01 | Inicia sesión con credenciales correctas y pasa al paso del código por email | Unitario + integración | 5 | ✅ Pasa |
| HU2-CP02 | Rechaza credenciales incorrectas sin revelar si el correo existe | Unitario + integración | 5 | ✅ Pasa |
| HU2-CP03 | Bloquea la cuenta durante 15 minutos tras 5 intentos fallidos | Unitario + integración | 6 | ✅ Pasa |
| HU2-CP04 | Rechaza el acceso a una cuenta desactivada | Unitario + integración | 4 | ✅ Pasa |
| HU2-CP05 | Al validar las credenciales, envía al correo del usuario un código de verificación | Unitario + integración | 4 | ✅ Pasa |
| HU2-CP06 | Un código correcto pasa al paso de Microsoft Authenticator | Unitario + integración | 3 | ✅ Pasa (2) · 🔍 En revisión (1, ampliado en AB#20) |
| HU2-CP07 | Rechaza un código incorrecto | Unitario + integración | 6 | ✅ Pasa |
| HU2-CP08 | Rechaza un código caducado (más de 10 minutos) | Unitario + integración | 3 | ✅ Pasa |
| HU2-CP09 | Un código ya usado no se puede volver a usar | Unitario + integración | 6 | ✅ Pasa |
| HU2-CP10 | Tras 5 intentos fallidos el código deja de valer, incluso el correcto | Unitario | 3 | ✅ Pasa |
| HU2-CP11 | Un login nuevo sustituye al código anterior | Integración | 1 | ✅ Pasa |
| HU2-CP12 | El código se guarda hasheado y asociado al usuario, nunca en claro | Unitario + integración | 6 | ✅ Pasa |
| HU2-CP13 | El código de verificación tiene 6 dígitos aleatorios | Unitario | 3 | ✅ Pasa |
| HU2-CP14 | Con sesión iniciada devuelve los datos del usuario; sin sesión, 401 | Unitario + integración | 3 | ✅ Pasa |
| HU2-CP15 | Cerrar sesión invalida la sesión | Integración | 1 | ✅ Pasa |
| HU2-CP16 | Rechaza el acceso a una cuenta pendiente de verificación | Unitario + integración | 2 | ✅ Pasa |
| HU2-CP17 | Sin haber superado el paso 2 no se puede usar el paso 3 | Unitario + integración | 3 | 🔍 En revisión (AB#20) |
| HU2-CP18 | Un código de Authenticator correcto abre la sesión con el rol del usuario | Unitario + integración | 4 | 🔍 En revisión (AB#20) |
| HU2-CP19 | Rechaza un código de Authenticator incorrecto y cuenta el fallo para el bloqueo | Unitario + integración | 4 | 🔍 En revisión (AB#20) |
| HU2-CP20 | Un código de Authenticator ya usado no se puede volver a usar | Unitario + integración | 4 | 🔍 En revisión (AB#20) |
| HU2-CP21 | Acepta el código del periodo anterior o siguiente (±30 s) | Unitario | 1 | 🔍 En revisión (AB#20) |
| HU2-CP22 | Un usuario sin Authenticator pasa al alta y recibe un QR | Unitario + integración | 13 | 🔍 En revisión (AB#20) |
| HU2-CP23 | El primer código correcto confirma el alta | Unitario + integración | 7 | 🔍 En revisión (AB#20) |
| HU2-CP24 | El secreto se guarda cifrado, nunca en claro | Unitario + integración | 7 | 🔍 En revisión (AB#20) |

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
- **T230 (AB#230):** endpoints del login (`AutenticacionController`): `POST /api/auth/login`, `POST /api/auth/verify-email-code`, `GET /api/auth/me` y `POST /api/auth/logout` (este último lo hace Spring Security). Los tests de `AutenticacionControllerTest` (`@WebMvcTest`) prueban CP01 a CP07 a través de HTTP: códigos 200, 401, 429, 403 con `code: ACCOUNT_DISABLED` y 503, y que el paso 2 sin el paso 1 se rechaza (CP07). Prueba manual de los pasos 1 y 2 contra el backend real (MongoDB local, correo por Brevo) el 10/10/2026.
- **HU2-CP14 y CP15:** casos nuevos de la T230 para `/me` y el cierre de sesión. Pendientes de añadir a la descripción de la HU2 en Azure Boards. Hasta la T20 (paso 3), `/me` responde siempre 401 en la aplicación real: la sesión no se abre todavía.
- **HU2-CP16 (T235, AB#235):** la comprobación de cuenta pendiente se realiza intencionadamente después de validar la contraseña para evitar la enumeración de cuentas inactivas por parte de posibles atacantes. El PR #70 lo propuso como CP14, pero CP14 y CP15 ya los usaba la T230 (PR #67): se renumeró a CP16 al integrar develop. Responde **403 con `code: ACCOUNT_PENDING`** (como la cuenta desactivada), que es lo que leerá el frontend en la T236; el test del controlador lo comprueba (sin `@ResponseStatus` respondía 200).
- **T20 (AB#20, paso 3 con Microsoft Authenticator):** TOTP (RFC 6238) con la librería java-totp (`JavaTotpAdapter`), secreto cifrado con AES-GCM (`AesGcmCifradoSecretoAdapter`) en `factores_autenticacion`, alta con QR en el primer login y apertura de la sesión de Spring Security con `ROLE_<rol>`. El paso 3 va en su propio servicio (`AuthenticatorService`). `codigoCorrectoPasaAlAuthenticator` (CP06) se amplía: ahora prepara un usuario con Authenticator confirmado. Prueba manual de punta a punta (login, correo real, alta con QR escaneado en el móvil, sesión abierta, `/me` y logout) el 10/10/2026.
- **HU2-CP17 a CP24:** casos nuevos de la T20 (plan `docs/planes/task-20.md`). Pendientes de añadir a la descripción de la T20 en Azure Boards.
- **Frontend (T20):** los tests de `LoginFlow.test.jsx` no llevan el ID del caso en el nombre (estilo del archivo, anterior a la convención de la HU19); se trazan aquí por su nombre.

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
| HU2-CP06 | `AutenticacionServiceTest` | `codigoCorrectoPasaAlAuthenticator` | Aplicación | AB#22 · AB#232 · AB#20 | PR #52 (ampliado en PR #56 y PR #75) | 🔍 |
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
| HU2-CP09 | `CodigoVerificacionTest` | `marcarUsadoGuardaLaHoraDeUso` | Dominio | AB#232 | PR #56 | ✅ |
| HU2-CP09 | `CodigoVerificacionTest` | `reconstruirConservaElEstado` | Dominio | AB#232 | PR #56 | ✅ |
| HU2-CP10 | `CodigoVerificacionTest` | `rechazaReconstruirConIntentosNegativos` | Dominio | AB#232 | PR #56 | ✅ |
| HU2-CP12 | `CodigoVerificacionDocumentMapperTest` | `convierteCodigoEnDocumento` | Infraestructura | AB#232 | PR #56 | ✅ |
| HU2-CP09 | `CodigoVerificacionDocumentMapperTest` | `convierteDocumentoEnCodigo` | Infraestructura | AB#232 | PR #56 | ✅ |
| HU2-CP12 | `CodigoVerificacionMongoAdapterTest` | `guardaElCodigoEnTokensSeguridad` | Infraestructura | AB#232 | PR #56 | ✅ |
| HU2-CP08 | `CodigoVerificacionMongoAdapterTest` | `buscarPorUsuarioDevuelveElCodigoGuardado` | Infraestructura | AB#232 | PR #56 | ✅ |
| HU2-CP07 | `CodigoVerificacionMongoAdapterTest` | `buscarPorUsuarioDevuelveVacioSinCodigo` | Infraestructura | AB#232 | PR #56 | ✅ |
| HU2-CP11 | `CodigoVerificacionMongoAdapterTest` | `unCodigoNuevoSustituyeAlAnterior` | Infraestructura | AB#232 | PR #56 | ✅ |
| HU2-CP09 | `CodigoVerificacionMongoAdapterTest` | `guardarUnCodigoLeidoLoActualiza` | Infraestructura | AB#232 | PR #56 | ✅ |
| HU2-CP07 | `CodigoVerificacionMongoAdapterTest` | `noDevuelveTokensDeOtroProposito` | Infraestructura | AB#232 | PR #56 | ✅ |
| HU2-CP13 | `SecureRandomGeneradorCodigoAdapterTest` | `generaUnCodigoDeSeisDigitos` | Infraestructura | AB#231 | PR #54 | ✅ |
| HU2-CP13 | `SecureRandomGeneradorCodigoAdapterTest` | `rellenaConCerosALaIzquierda` | Infraestructura | AB#231 | PR #54 | ✅ |
| HU2-CP13 | `SecureRandomGeneradorCodigoAdapterTest` | `usaTodoElRangoDeSeisDigitos` | Infraestructura | AB#231 | PR #54 | ✅ |
| HU2-CP01 | `AutenticacionControllerTest` | `loginCorrectoPasaAlCodigoDelCorreo` | Infraestructura | AB#230 | PR #67 | ✅ |
| HU2-CP02 | `AutenticacionControllerTest` | `loginConCredencialesIncorrectasDevuelve401` | Infraestructura | AB#230 | PR #67 | ✅ |
| HU2-CP03 | `AutenticacionControllerTest` | `cuentaBloqueadaDevuelve429` | Infraestructura | AB#230 | PR #67 | ✅ |
| HU2-CP04 | `AutenticacionControllerTest` | `cuentaDesactivadaDevuelve403ConCodigo` | Infraestructura | AB#230 | PR #67 | ✅ |
| HU2-CP05 | `AutenticacionControllerTest` | `falloDelCorreoDevuelve503` | Infraestructura | AB#230 | PR #67 | ✅ |
| HU2-CP06 | `AutenticacionControllerTest` | `codigoCorrectoPasaAlAuthenticator` | Infraestructura | AB#230 | PR #67 | ✅ |
| HU2-CP07 | `AutenticacionControllerTest` | `codigoSinLoginPrevioDevuelve401` | Infraestructura | AB#230 | PR #67 | ✅ |
| HU2-CP07 | `AutenticacionControllerTest` | `codigoIncorrectoDevuelve401` | Infraestructura | AB#230 | PR #67 | ✅ |
| HU2-CP12 | `VerificarCodigoRequestTest` | `toStringNoMuestraElCodigo` | Infraestructura | AB#230 | PR #67 | ✅ |
| HU2-CP14 | `AutenticacionServiceTest` | `consultaElUsuarioEnSesion` | Aplicación | AB#230 | PR #67 | ✅ |
| HU2-CP14 | `AutenticacionControllerTest` | `meSinSesionDevuelve401` | Infraestructura | AB#230 | PR #67 | ✅ |
| HU2-CP14 | `AutenticacionControllerTest` | `meConSesionDevuelveElUsuario` | Infraestructura | AB#230 | PR #67 | ✅ |
| HU2-CP15 | `AutenticacionControllerTest` | `logoutCierraLaSesion` | Infraestructura | AB#230 | PR #67 | ✅ |
| HU2-CP16 | `AutenticacionServiceTest` | `rechazaCuentaPendiente` | Aplicación | AB#235 | PR #70 | ✅ |
| HU2-CP16 | `AutenticacionControllerTest` | `cuentaPendienteDevuelve403ConCodigo` | Infraestructura | AB#235 | PR #70 | ✅ |
| HU2-CP17 | `AuthenticatorServiceTest` | `sinFactorSeRechaza` | Aplicación | AB#20 | PR #75 | 🔍 |
| HU2-CP17 | `AutenticacionControllerTest` | `codigoAuthenticatorSinPasoDosDevuelve401` | Infraestructura | AB#20 | PR #75 | 🔍 |
| HU2-CP17 | `AutenticacionControllerTest` | `altaSinPasoDosDevuelve401` | Infraestructura | AB#20 | PR #75 | 🔍 |
| HU2-CP18 | `JavaTotpAdapterTest` | `aceptaElCodigoDelRfcEnSuPeriodo` | Infraestructura | AB#20 | PR #75 | 🔍 |
| HU2-CP18 | `AuthenticatorServiceTest` | `codigoCorrectoDevuelveElUsuarioYRegistraElUso` | Aplicación | AB#20 | PR #75 | 🔍 |
| HU2-CP18 | `AutenticacionControllerTest` | `codigoAuthenticatorCorrectoAbreLaSesion` | Infraestructura | AB#20 | PR #75 | 🔍 |
| HU2-CP18 | `AutenticacionControllerTest` | `trasElPasoTresMeDevuelveElUsuario` | Infraestructura | AB#20 | PR #75 | 🔍 |
| HU2-CP19 | `JavaTotpAdapterTest` | `rechazaUnCodigoIncorrecto` | Infraestructura | AB#20 | PR #75 | 🔍 |
| HU2-CP19 | `AuthenticatorServiceTest` | `codigoIncorrectoCuentaUnFalloParaElBloqueo` | Aplicación | AB#20 | PR #75 | 🔍 |
| HU2-CP19 | `AuthenticatorServiceTest` | `cuentaBloqueadaSeRechazaSinComprobarElCodigo` | Aplicación | AB#20 | PR #75 | 🔍 |
| HU2-CP19 | `LoginFlow.test.jsx` | `login_sinAuthenticator_conUnPrimerCodigoIncorrecto_muestraErrorYSigueEnElAlta` | Frontend | AB#20 | PR #75 | 🔍 |
| HU2-CP20 | `FactorAutenticacionTest` | `noSePuedeReutilizarUnPeriodo` | Dominio | AB#20 | PR #75 | 🔍 |
| HU2-CP20 | `FactorAutenticacionTest` | `unFactorSinUsosAceptaCualquierPeriodo` | Dominio | AB#20 | PR #75 | 🔍 |
| HU2-CP20 | `FactorAutenticacionMongoAdapterTest` | `guardarUnFactorLeidoLoActualiza` | Infraestructura | AB#20 | PR #75 | 🔍 |
| HU2-CP20 | `AuthenticatorServiceTest` | `codigoReutilizadoSeRechaza` | Aplicación | AB#20 | PR #75 | 🔍 |
| HU2-CP21 | `JavaTotpAdapterTest` | `aceptaUnPeriodoDeDesfaseYNoMas` | Infraestructura | AB#20 | PR #75 | 🔍 |
| HU2-CP22 | `JavaTotpAdapterTest` | `generaUnSecretoAleatorioEnBase32` | Infraestructura | AB#20 | PR #75 | 🔍 |
| HU2-CP22 | `JavaTotpAdapterTest` | `generaElQrComoImagen` | Infraestructura | AB#20 | PR #75 | 🔍 |
| HU2-CP22 | `FactorAutenticacionMongoAdapterTest` | `buscarPorUsuarioDevuelveVacioSinFactor` | Infraestructura | AB#20 | PR #75 | 🔍 |
| HU2-CP22 | `FactorAutenticacionMongoAdapterTest` | `unAltaNuevaSustituyeAlFactorAnterior` | Infraestructura | AB#20 | PR #75 | 🔍 |
| HU2-CP22 | `AuthenticatorServiceTest` | `prepararAltaGuardaElSecretoCifradoYDevuelveElQr` | Aplicación | AB#20 | PR #75 | 🔍 |
| HU2-CP22 | `AuthenticatorServiceTest` | `prepararAltaDeUnCorreoInexistenteSeRechaza` | Aplicación | AB#20 | PR #75 | 🔍 |
| HU2-CP22 | `AuthenticatorServiceTest` | `prepararAltaConAuthenticatorConfirmadoSeRechaza` | Aplicación | AB#20 | PR #75 | 🔍 |
| HU2-CP22 | `AutenticacionServiceTest` | `sinAuthenticatorPasaAlAlta` | Aplicación | AB#20 | PR #75 | 🔍 |
| HU2-CP22 | `AutenticacionServiceTest` | `conAltaSinConfirmarVuelveAlAlta` | Aplicación | AB#20 | PR #75 | 🔍 |
| HU2-CP22 | `AutenticacionControllerTest` | `altaTrasPasoDosDevuelveElQr` | Infraestructura | AB#20 | PR #75 | 🔍 |
| HU2-CP22 | `AutenticacionControllerTest` | `altaDeQuienYaTieneAuthenticatorDevuelve403` | Infraestructura | AB#20 | PR #75 | 🔍 |
| HU2-CP22 | `LoginFlow.test.jsx` | `login_sinAuthenticator_trasElCodigoDeCorreo_muestraElQrDelAlta` | Frontend | AB#20 | PR #75 | 🔍 |
| HU2-CP22 | `LoginFlow.test.jsx` | `login_conAuthenticatorYaDadoDeAlta_noMuestraElQr` | Frontend | AB#20 | PR #75 | 🔍 |
| HU2-CP23 | `FactorAutenticacionTest` | `unFactorNuevoNoEstaConfirmado` | Dominio | AB#20 | PR #75 | 🔍 |
| HU2-CP23 | `FactorAutenticacionTest` | `elPrimerUsoConfirmaElAlta` | Dominio | AB#20 | PR #75 | 🔍 |
| HU2-CP23 | `FactorAutenticacionTest` | `losUsosSiguientesNoCambianLaFechaDelAlta` | Dominio | AB#20 | PR #75 | 🔍 |
| HU2-CP23 | `FactorAutenticacionDocumentMapperTest` | `convierteDocumentoEnFactor` | Infraestructura | AB#20 | PR #75 | 🔍 |
| HU2-CP23 | `FactorAutenticacionMongoAdapterTest` | `buscarPorUsuarioDevuelveElFactorGuardado` | Infraestructura | AB#20 | PR #75 | 🔍 |
| HU2-CP23 | `AuthenticatorServiceTest` | `primerCodigoCorrectoConfirmaElAlta` | Aplicación | AB#20 | PR #75 | 🔍 |
| HU2-CP23 | `LoginFlow.test.jsx` | `login_sinAuthenticator_conElPrimerCodigo_confirmaElAltaYEntra` | Frontend | AB#20 | PR #75 | 🔍 |
| HU2-CP24 | `AesGcmCifradoSecretoAdapterTest` | `descifrarDevuelveElSecretoOriginal` | Infraestructura | AB#20 | PR #75 | 🔍 |
| HU2-CP24 | `AesGcmCifradoSecretoAdapterTest` | `elCifradoNoContieneElSecreto` | Infraestructura | AB#20 | PR #75 | 🔍 |
| HU2-CP24 | `AesGcmCifradoSecretoAdapterTest` | `cadaCifradoEsDistinto` | Infraestructura | AB#20 | PR #75 | 🔍 |
| HU2-CP24 | `AesGcmCifradoSecretoAdapterTest` | `unCifradoManipuladoSeRechaza` | Infraestructura | AB#20 | PR #75 | 🔍 |
| HU2-CP24 | `FactorAutenticacionDocumentMapperTest` | `convierteFactorEnDocumento` | Infraestructura | AB#20 | PR #75 | 🔍 |
| HU2-CP24 | `FactorAutenticacionMongoAdapterTest` | `guardaElFactorEnFactoresAutenticacion` | Infraestructura | AB#20 | PR #75 | 🔍 |
| HU2-CP24 | `AuthenticatorServiceTest` | `prepararAltaNoGuardaElSecretoEnClaro` | Aplicación | AB#20 | PR #75 | 🔍 |

Rutas de las clases de test (bajo `backend/src/test/java/es/uclm/esibuy/backend/`):
- `AutenticacionServiceTest`: `usuarios/application/service/` (Mockito, con `Clock` fijo)
- `CodigoVerificacionTest`: `usuarios/domain/model/` (JUnit puro, sin Spring)
- `VerificarCodigoEmailCommandTest`: `usuarios/application/port/in/`
- `AutenticacionControllerTest`, `LoginRequestTest` y `VerificarCodigoRequestTest`: `usuarios/infrastructure/adapter/in/web/` (el controlador con `@WebMvcTest` y el caso de uso simulado)
- `BCryptHashContrasenaAdapterTest` y `SecureRandomGeneradorCodigoAdapterTest`: `usuarios/infrastructure/adapter/out/seguridad/`
- `SmtpEnvioEmailAdapterTest`: `usuarios/infrastructure/adapter/out/email/` (Mockito, con un mock de `JavaMailSender`; no envía correos reales)
- `UsuarioDocumentMapperTest`, `UsuarioMongoAdapterTest`, `CodigoVerificacionDocumentMapperTest` y `CodigoVerificacionMongoAdapterTest`: `usuarios/infrastructure/adapter/out/persistence/` (los adaptadores, con Testcontainers)

---

## HU15 · Acceso a la zona de vendedor

### Tabla A · HU15 ↔ casos de prueba

| Caso | Descripción | Tipo | Tests | Estado |
|---|---|---|---|---|
| HU15-CP01 | Sin sesión no se accede a la zona de vendedor (401) | Integración (capa web) | 1 | ✅ Pasa |
| HU15-CP02 | Un cliente o un administrador no accede a la zona de vendedor (403) | Integración (capa web) | 1 | ✅ Pasa |
| HU15-CP03 | Un vendedor accede a la zona de vendedor | Integración (capa web) | 1 | ✅ Pasa |

Notas:
- **HU15-CP01 a CP03:** casos propuestos en la T57 (AB#57) a partir de los criterios de la HU15 (`frontend/docs/Contexto_frontend.md`). Pendientes de confirmar con el equipo y de añadir a la HU15 en Azure Boards.
- **Backend (T57):** `SecurityConfig` solo deja usar `/api/seller/**` al rol `VENDEDOR`. Aún no hay endpoints de vendedor (llegarán con los productos, HU31 y HU32): los tests usan un endpoint que solo existe en el test. La task, aunque es de tests, incluye la regla (avisado al equipo).
- **El administrador** ve la información de los vendedores en solo lectura por su propia zona (`/api/admin/sellers/...`, HU26), no por `/api/seller/**`.
- **«Vendedor activo»:** el estado de la cuenta lo comprueba el login (las cuentas `PENDIENTE` o `DESACTIVADO` no entran), no esta regla.
- **Frontend:** los tests de la HU15 (AB#213) no tienen todavía filas en la matriz.

### Tabla B · casos de prueba ↔ tests

| Caso | Clase de test | Método | Capa | Task | Origen | Estado |
|---|---|---|---|---|---|---|
| HU15-CP01 | `SecurityConfigTest` | `sinSesionNoSeAccedeALaZonaDeVendedor` | Infraestructura | AB#57 | PR #74 | ✅ |
| HU15-CP02 | `SecurityConfigTest` | `otroRolNoAccedeALaZonaDeVendedor` (cliente y administrador) | Infraestructura | AB#57 | PR #74 | ✅ |
| HU15-CP03 | `SecurityConfigTest` | `vendedorAccedeALaZonaDeVendedor` | Infraestructura | AB#57 | PR #74 | ✅ |

Rutas de las clases de test (bajo `backend/src/test/java/es/uclm/esibuy/backend/`):
- `SecurityConfigTest`: `shared/config/` (con `@WebMvcTest` y usuarios simulados)

---

## HU18 · Acceso restringido al panel de administración

### Tabla A · HU18 ↔ casos de prueba

| Caso | Descripción | Tipo | Tests | Estado |
|---|---|---|---|---|
| HU18-CP01 | Sin sesión no se accede a la zona de administración | Integración (capa web) | 2 | ✅ Pasa |
| HU18-CP02 | Un cliente o un vendedor no accede a la zona de administración | Integración (capa web) | 3 | ✅ Pasa |
| HU18-CP03 | Un administrador accede a la zona de administración | Integración (capa web) | 1 | ✅ Pasa |

Notas:
- **HU18-CP01 a CP03:** casos propuestos en la T58 (AB#58) a partir de los criterios de aceptación de la HU18 (`frontend/docs/hu18-panel-admin.md`). Pendientes de confirmar con el equipo y de añadir a la HU18 en Azure Boards.
- **Backend (T58):** `SecurityConfig` solo deja usar `/api/admin/**` al rol `ADMINISTRADOR`. Todavía no hay login ni endpoints de administración: los tests simulan los usuarios con `@WithMockUser` y usan un endpoint de prueba que solo existe en el test. El login (task 230) tendrá que dar a cada usuario la autoridad `ROLE_<rol>`.
- **HU18-CP01:** desde la T230 (AB#230, D10) sin sesión responde **401** («no sé quién eres») en toda la API; con sesión y sin permiso sigue siendo 403 (CP02). Antes respondía 403, el valor por defecto de Spring sin mecanismo de login. Sus dos tests (y el técnico `listadoSinSesionEstaProtegido` de la HU19) pasan a esperar 401.
- **T60 (AB#60):** prueba la regla sobre los endpoints reales de administración (`GET /api/admin/usuarios` de la HU19 y `PATCH /api/admin/usuarios/{id}/status` de la HU24), en `UsuarioControllerTest`. Con los tests que ya había (HU19-CP08, HU24-CP01, HU24-CP04, ampliado al vendedor, y el técnico `listadoSinSesionEstaProtegido`), los dos endpoints quedan cubiertos para todos los roles. Los tests de cliente y vendedor son parametrizados (avisos S4144 y S5976 de SonarLint).
- **Frontend:** la ruta protegida y el menú del panel (HU18) no tienen todavía filas en la matriz.

### Tabla B · casos de prueba ↔ tests

| Caso | Clase de test | Método | Capa | Task | Origen | Estado |
|---|---|---|---|---|---|---|
| HU18-CP01 | `SecurityConfigTest` | `sinSesionNoSeAccedeAAdministracion` | Infraestructura | AB#58 · AB#230 | PR #57 (401 en PR #67) | ✅ |
| HU18-CP02 | `SecurityConfigTest` | `clienteNoAccedeAAdministracion` | Infraestructura | AB#58 | PR #57 | ✅ |
| HU18-CP02 | `SecurityConfigTest` | `vendedorNoAccedeAAdministracion` | Infraestructura | AB#58 | PR #57 | ✅ |
| HU18-CP03 | `SecurityConfigTest` | `administradorAccedeAAdministracion` | Infraestructura | AB#58 | PR #57 | ✅ |
| HU18-CP02 | `UsuarioControllerTest` | `noAdministradorNoAccedeAlListado` (cliente y vendedor) | Infraestructura | AB#60 | PR #64 | ✅ |
| HU18-CP01 | `UsuarioControllerTest` | `activarSinSesionEstaProtegido` | Infraestructura | AB#60 · AB#230 | PR #64 (401 en PR #67) | ✅ |

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
| HU19-CP01 | `ListadoUsuariosServiceTest` | `listarDelegaLaConsultaIntactaAlRepositorio` | Aplicación | AB#62 | PR #59 | ✅ |
| HU19-CP02 | `UsuarioMongoAdapterTest` | `listarBuscaEnLosCamposVisiblesDelUsuario` | Infraestructura | AB#61 | PR #59 | ✅ |
| HU19-CP02 | `UsuarioMongoAdapterTest` | `listarEscapaCaracteresEspecialesDeLaBusqueda` | Infraestructura | AB#61 | PR #59 | ✅ |
| HU19-CP03 | `UsuarioMongoAdapterTest` | `listarFiltraPorRol` | Infraestructura | AB#61 | PR #59 | ✅ |
| HU19-CP04 | `UsuarioMongoAdapterTest` | `listarFiltraPorEstadoActivo` | Infraestructura | AB#61 | PR #59 | ✅ |
| HU19-CP05 | `ListadoUsuariosServiceTest` | `listarDevuelveLaPaginaProporcionadaPorElRepositorio` | Aplicación | AB#62 | PR #59 | ✅ |
| HU19-CP05 | `UsuarioMongoAdapterTest` | `listarAplicaPaginacionYConservaElTotal` | Infraestructura | AB#61 | PR #59 | ✅ |

El test web de acceso sin sesión está en `UsuarioControllerTest`, pero se considera técnico de seguridad y no un caso funcional de HU19.

Rutas de los archivos de test (bajo `frontend/src/`):
- `users.test.js`: `api/`
- `UsersTable.test.jsx` y `UsersFilters.test.jsx`: `features/admin/`
- `AdminUsersPage.test.jsx`: `pages/` (integración: rutas protegidas, sesión simulada y mock de MSW)

---

## HU20 · Consultar el detalle de un usuario

### Tabla A · HU20 ↔ casos de prueba

| Caso | Descripción | Tipo | Tests | Estado |
|---|---|---|---|---|
| HU20-CP01 | La ficha de un cliente trae sus datos personales y de cliente, sin datos sensibles | Unitario + integración (capa web) | 2 | ✅ Pasa |
| HU20-CP02 | La ficha de un administrador no trae datos de cliente | Integración (capa web) | 1 | ✅ Pasa |
| HU20-CP03 | Consultar un usuario que no existe devuelve 404 | Unitario + integración (capa web) | 2 | ✅ Pasa |
| HU20-CP04 | Sin sesión (401) o sin ser administrador (403) no se ve la ficha | Integración (capa web) | 2 | ✅ Pasa |

Notas:
- **HU20-CP01 a CP04:** casos propuestos en la T54 (AB#54) a partir del contrato del frontend (`frontend/docs/hu20-ficha-usuario.md`). Pendientes de confirmar con el equipo y de añadir a la HU20 en Azure Boards.
- **Backend (T52, AB#52, PR #76):** `GET /api/admin/usuarios/{id}` con `UsuarioDetalleResponse`. La imagen sale como `avatar` (en el dominio, `imagenPerfil`) y cada ficha trae solo los campos de su rol. `sede` y `fechaIncorporacion` del administrador llegarán con la HU21.
- **Frontend:** los tests de la HU20 (AB#211) no tienen todavía filas en la matriz.

### Tabla B · casos de prueba ↔ tests

| Caso | Clase de test | Método | Capa | Task | Origen | Estado |
|---|---|---|---|---|---|---|
| HU20-CP01 | `AdministracionUsuariosServiceTest` | `consultarDevuelveElUsuario` | Aplicación | AB#54 | PR #77 | ✅ |
| HU20-CP01 | `UsuarioControllerTest` | `fichaDeUnClienteConSusDatos` | Infraestructura | AB#54 | PR #77 | ✅ |
| HU20-CP02 | `UsuarioControllerTest` | `fichaDeUnAdministradorSinDatosDeCliente` | Infraestructura | AB#54 | PR #77 | ✅ |
| HU20-CP03 | `AdministracionUsuariosServiceTest` | `consultarUnUsuarioQueNoExisteFalla` | Aplicación | AB#54 | PR #77 | ✅ |
| HU20-CP03 | `UsuarioControllerTest` | `fichaDeUnUsuarioInexistenteDevuelve404` | Infraestructura | AB#54 | PR #77 | ✅ |
| HU20-CP04 | `UsuarioControllerTest` | `fichaSinSesionDevuelve401` | Infraestructura | AB#54 | PR #77 | ✅ |
| HU20-CP04 | `UsuarioControllerTest` | `noAdministradorNoVeLaFicha` (cliente y vendedor) | Infraestructura | AB#54 | PR #77 | ✅ |

Rutas de las clases de test (bajo `backend/src/test/java/es/uclm/esibuy/backend/`):
- `AdministracionUsuariosServiceTest`: `usuarios/application/service/` (Mockito, sin Spring)
- `UsuarioControllerTest`: `usuarios/infrastructure/adapter/in/web/` (con `@WebMvcTest` y usuarios simulados)

---

## HU22 · Modificar datos y rol de un usuario

### Tabla A · HU22 ↔ casos de prueba

| Caso | Descripción | Tipo | Tests | Estado |
|---|---|---|---|---|
| HU22-CP01 | El administrador edita los datos de un cliente y recibe su ficha actualizada (el teléfono es opcional) | Unitario + integración | 5 | ✅ Pasa |
| HU22-CP02 | El administrador edita los datos de un administrador (nombre, apellidos e imagen) | Unitario | 2 | ✅ Pasa |
| HU22-CP03 | El administrador edita los datos de un vendedor (nombre, apellidos e imagen) | Unitario | 1 | ✅ Pasa |
| HU22-CP04 | El email, la contraseña, el estado y el rol no cambian aunque lleguen en el cuerpo | Unitario + integración (capa web) | 2 | ✅ Pasa |
| HU22-CP05 | Rechaza datos no válidos (400): sin rol, nombre o apellidos no válidos, DNI mal formado o cliente menor de edad; no guarda nada | Unitario + integración (capa web) | 7 | ✅ Pasa |
| HU22-CP06 | Rechaza un DNI que ya es de otra cuenta (409 `DNI_ALREADY_REGISTERED`), también al pasar a cliente; conservar el propio DNI no cuenta | Unitario + integración | 5 | ✅ Pasa (4) · 🔍 En revisión (1, PR por abrir) |
| HU22-CP07 | Editar un usuario que no existe devuelve 404 | Unitario + integración (capa web) | 2 | ✅ Pasa |
| HU22-CP08 | Solo un administrador con sesión y token CSRF puede editar usuarios (401 / 403) | Integración (capa web) | 3 | ✅ Pasa |
| HU22-CP09 | El administrador cambia el rol de una cuenta entre cliente y administrador con los datos del rol nuevo; al dejar de ser cliente se borran sus datos de cliente, también en la base de datos | Unitario + integración | 7 | 🔍 En revisión (PR por abrir) |
| HU22-CP10 | Una cuenta pendiente que pasa a administrador queda activa (una desactivada sigue desactivada) | Unitario + integración | 3 | 🔍 En revisión (PR por abrir) |
| HU22-CP11 | Un administrador no puede cambiar su propio rol (409 `CANNOT_CHANGE_OWN_ROLE`), pero sí editar sus propios datos | Unitario + integración (capa web) | 3 | 🔍 En revisión (PR por abrir) |
| HU22-CP12 | No se puede degradar al último administrador activo (409 `LAST_ACTIVE_ADMIN`); sí si queda otro activo o si el degradado no está activo | Unitario + integración (capa web) | 4 | 🔍 En revisión (PR por abrir) |
| HU22-CP13 | Pasar una cuenta a vendedor se rechaza (400) mientras no existan los datos del vendedor | Unitario | 1 | 🔍 En revisión (PR por abrir) |
| HU22-CP14 | Si al usuario en sesión le cambian el rol, se invalida su sesión y responde 401 | Unitario | 1 | 🔍 En revisión (PR por abrir) |

Notas:
- **HU22-CP01 a CP08:** casos propuestos en la T42/T44 (AB#42, AB#44) a partir del contrato del frontend (`frontend/docs/hu22-editar-usuario.md`). Pendientes de confirmar con el equipo y de añadir a la HU22 en Azure Boards. Se desarrollaron con TDD (ciclos rojo, verde y refactor); los de CP03, CP04, CP06 (persistencia) y CP08 son tests de protección sobre código que ya existía y pasaron a la primera.
- **Backend (T42):** `PUT /api/admin/usuarios/{id}` con `EditarUsuarioRequest`, en `AdministracionUsuariosUseCase.editar`. Devuelve la ficha de la HU20 (`UsuarioDetalleResponse`).
- **Decisiones del equipo (10/10/2026):** el email no se edita en ningún rol (también en el vendedor, PR #81), así que el 409 `EMAIL_ALREADY_REGISTERED` ya no forma parte de la HU22. El teléfono es opcional: en blanco (`""`) deja al cliente sin teléfono.
- **HU22-CP09 a CP14:** casos propuestos en la T43 (AB#43) a partir del mismo contrato. Pendientes de confirmar con el equipo y de añadir a la HU22 en Azure Boards. Desarrollados con TDD; los de persistencia de CP09 y CP10, el 200 web de CP09, el de un administrador desactivado de CP12 y el 409 web de CP12 son tests de protección que pasaron a la primera.
- **Backend (T43):** el mismo `PUT`, ahora con cambio de rol (`Usuario.cambiarRol`, que sustituye a `modificarDatos`). `AdministracionUsuariosUseCase.editar` recibe el email del administrador en sesión, como `desactivar` (HU23). La regla del último administrador activo es la misma para desactivar y degradar (`exigirQueNoSeaElUltimoAdministradorActivo`) y reutiliza el 409 `LAST_ACTIVE_ADMIN` de la HU23. CP14 amplía `CuentaActivaFilter` (HU23, T206): la sesión también se invalida si su permiso `ROLE_<rol>` ya no es el del usuario.
- **Cambios en tests de la T42 (T43):** `rechazaEditarConUnRolDistintoDelActual` (CP05) se convierte en `rechazaPasarUnaCuentaAVendedor` (CP13), porque el cambio de rol ya está permitido. Los tests de `UsuarioTest` y `UsuarioMongoAdapterTest` que llamaban a `modificarDatos` llaman ahora a `cambiarRol` con el mismo rol, y las llamadas a `editar` llevan el email del administrador en sesión; no cambia lo que comprueban.
- **Decisiones del equipo (T43):** pasar a vendedor queda en 400 hasta que existan sus datos; al dejar de ser cliente se borran los datos de cliente (minimización de datos); el último administrador usa el handler de la HU23 tal cual; la fecha de incorporación, cuando exista, no se podrá editar.
- **Pendiente:** la sede del administrador y el nombre comercial, el NIF y la categoría del vendedor no existen todavía en el modelo (ver `UsuarioDetalleResponse` y `VendedorResponse`); cuando existan se añadirán a la edición, con el 409 `COMMERCIAL_NAME_ALREADY_REGISTERED`, y se podrá pasar a vendedor (CP13). `existePorDni` no distingue rol, aunque el índice `rol_dni` sí: revisar cuando los vendedores tengan DNI.
- **Frontend:** los tests de la HU22 (AB#46) no tienen todavía filas en la matriz.

### Tabla B · casos de prueba ↔ tests

| Caso | Clase de test | Método | Capa | Task | Origen | Estado |
|---|---|---|---|---|---|---|
| HU22-CP01 | `UsuarioTest` | `modificarDatosCambiaLosDatosDeUnCliente` | Dominio | AB#44 | PR #82 | ✅ |
| HU22-CP01 | `AdministracionUsuariosServiceTest` | `editaUnClienteYLoGuarda` | Aplicación | AB#44 | PR #82 | ✅ |
| HU22-CP01 | `AdministracionUsuariosServiceTest` | `editarUnClienteConElTelefonoEnBlancoLoDejaSinTelefono` | Aplicación | AB#44 | PR #82 | ✅ |
| HU22-CP01 | `UsuarioControllerTest` | `administradorEditaUnClienteYRecibeSuFicha` | Infraestructura | AB#44 | PR #82 | ✅ |
| HU22-CP01 | `UsuarioMongoAdapterTest` | `guardarUnUsuarioEditadoActualizaSuDocumento` | Infraestructura | AB#44 | PR #82 | ✅ |
| HU22-CP02 | `UsuarioTest` | `modificarDatosCambiaLosDatosDeUnAdministrador` | Dominio | AB#44 | PR #82 | ✅ |
| HU22-CP02 | `AdministracionUsuariosServiceTest` | `editaUnAdministradorYLoGuarda` | Aplicación | AB#44 | PR #82 | ✅ |
| HU22-CP03 | `AdministracionUsuariosServiceTest` | `editaUnVendedorYLoGuarda` | Aplicación | AB#44 | PR #82 | ✅ |
| HU22-CP04 | `AdministracionUsuariosServiceTest` | `editarNoCambiaEmailContrasenaEstadoNiRol` | Aplicación | AB#44 | PR #82 | ✅ |
| HU22-CP04 | `UsuarioControllerTest` | `editarIgnoraEmailContrasenaYEstadoDelCuerpo` | Infraestructura | AB#44 | PR #82 | ✅ |
| HU22-CP05 | `UsuarioTest` | `modificarDatosRechazaUnClienteMenorDeEdad` | Dominio | AB#44 | PR #82 | ✅ |
| HU22-CP05 | `UsuarioTest` | `modificarDatosAceptaUnClienteQueCumple18Hoy` | Dominio | AB#44 | PR #82 | ✅ |
| HU22-CP05 | `UsuarioTest` | `modificarDatosExigeDatosDeClienteCoherentesConElRol` | Dominio | AB#44 | PR #82 | ✅ |
| HU22-CP05 | `AdministracionUsuariosServiceTest` | `rechazaEditarUnClienteConDatosNoValidos` | Aplicación | AB#44 | PR #82 | ✅ |
| HU22-CP05 | `AdministracionUsuariosServiceTest` | `rechazaEditarSinRol` | Aplicación | AB#44 | PR #82 | ✅ |
| HU22-CP05 | `UsuarioControllerTest` | `rechazaUnCuerpoDeEdicionNoValido` (5 variantes) | Infraestructura | AB#44 | PR #82 | ✅ |
| HU22-CP05 | `UsuarioControllerTest` | `editarConDatosQueElDominioRechazaDevuelve400` | Infraestructura | AB#44 | PR #82 | ✅ |
| HU22-CP06 | `AdministracionUsuariosServiceTest` | `rechazaEditarUnClienteConElDniDeOtraCuenta` | Aplicación | AB#44 | PR #82 | ✅ |
| HU22-CP06 | `AdministracionUsuariosServiceTest` | `editarUnClienteQueConservaSuDniNoLoComprueba` | Aplicación | AB#44 | PR #82 | ✅ |
| HU22-CP06 | `UsuarioControllerTest` | `editarConElDniDeOtraCuentaDevuelve409` | Infraestructura | AB#44 | PR #82 | ✅ |
| HU22-CP06 | `UsuarioMongoAdapterTest` | `rechazaEditarUnClienteConElDniDeOtroEnLaBaseDeDatos` | Infraestructura | AB#44 | PR #82 | ✅ |
| HU22-CP06 | `AdministracionUsuariosServiceTest` | `rechazaPasarAClienteConElDniDeOtraCuenta` | Aplicación | AB#43 | PR por abrir | 🔍 |
| HU22-CP07 | `AdministracionUsuariosServiceTest` | `rechazaEditarUnUsuarioQueNoExiste` | Aplicación | AB#44 | PR #82 | ✅ |
| HU22-CP07 | `UsuarioControllerTest` | `editarUnUsuarioInexistenteDevuelve404` | Infraestructura | AB#44 | PR #82 | ✅ |
| HU22-CP08 | `UsuarioControllerTest` | `unNoAdministradorNoPuedeEditarUsuarios` (cliente y vendedor) | Infraestructura | AB#44 | PR #82 | ✅ |
| HU22-CP08 | `UsuarioControllerTest` | `editarSinSesionEstaProtegido` | Infraestructura | AB#44 | PR #82 | ✅ |
| HU22-CP08 | `UsuarioControllerTest` | `editarSinTokenCsrfSeRechaza` | Infraestructura | AB#44 | PR #82 | ✅ |
| HU22-CP09 | `UsuarioTest` | `cambiarRolDeClienteAAdministradorBorraLosDatosDeCliente` | Dominio | AB#43 | PR por abrir | 🔍 |
| HU22-CP09 | `UsuarioTest` | `cambiarRolDeAdministradorAClienteGuardaLosDatosDeCliente` | Dominio | AB#43 | PR por abrir | 🔍 |
| HU22-CP09 | `UsuarioTest` | `cambiarRolAClienteExigeDatosDeClienteValidos` | Dominio | AB#43 | PR por abrir | 🔍 |
| HU22-CP09 | `AdministracionUsuariosServiceTest` | `editarPasaUnClienteAAdministrador` | Aplicación | AB#43 | PR por abrir | 🔍 |
| HU22-CP09 | `AdministracionUsuariosServiceTest` | `editarPasaUnAdministradorACliente` | Aplicación | AB#43 | PR por abrir | 🔍 |
| HU22-CP09 | `UsuarioControllerTest` | `cambioDeRolDevuelveLaFichaDelRolNuevo` | Infraestructura | AB#43 | PR por abrir | 🔍 |
| HU22-CP09 | `UsuarioMongoAdapterTest` | `unClienteQuePasaAAdministradorPierdeSusDatosDeClienteEnLaBaseDeDatos` | Infraestructura | AB#43 | PR por abrir | 🔍 |
| HU22-CP10 | `UsuarioTest` | `unaCuentaPendienteQuePasaAAdministradorQuedaActiva` | Dominio | AB#43 | PR por abrir | 🔍 |
| HU22-CP10 | `UsuarioTest` | `unaCuentaDesactivadaQuePasaAAdministradorSigueDesactivada` | Dominio | AB#43 | PR por abrir | 🔍 |
| HU22-CP10 | `UsuarioMongoAdapterTest` | `unaCuentaPendienteQuePasaAAdministradorQuedaActivaEnLaBaseDeDatos` | Infraestructura | AB#43 | PR por abrir | 🔍 |
| HU22-CP11 | `AdministracionUsuariosServiceTest` | `rechazaQueUnAdministradorCambieSuPropioRol` | Aplicación | AB#43 | PR por abrir | 🔍 |
| HU22-CP11 | `AdministracionUsuariosServiceTest` | `unAdministradorEditaSusPropiosDatos` | Aplicación | AB#43 | PR por abrir | 🔍 |
| HU22-CP11 | `UsuarioControllerTest` | `cambiarElPropioRolDevuelve409` | Infraestructura | AB#43 | PR por abrir | 🔍 |
| HU22-CP12 | `AdministracionUsuariosServiceTest` | `rechazaDegradarAlUltimoAdministradorActivo` | Aplicación | AB#43 | PR por abrir | 🔍 |
| HU22-CP12 | `AdministracionUsuariosServiceTest` | `degradaAUnAdministradorSiQuedaOtroActivo` | Aplicación | AB#43 | PR por abrir | 🔍 |
| HU22-CP12 | `AdministracionUsuariosServiceTest` | `degradarAUnAdministradorDesactivadoNoConsultaElConteo` | Aplicación | AB#43 | PR por abrir | 🔍 |
| HU22-CP12 | `UsuarioControllerTest` | `degradarAlUltimoAdministradorActivoDevuelve409` | Infraestructura | AB#43 | PR por abrir | 🔍 |
| HU22-CP13 | `AdministracionUsuariosServiceTest` | `rechazaPasarUnaCuentaAVendedor` | Aplicación | AB#43 | PR por abrir (sustituye a `rechazaEditarConUnRolDistintoDelActual`, PR #82) | 🔍 |
| HU22-CP14 | `CuentaActivaFilterTest` | `rolCambiadoInvalidaLaSesionYDevuelve401` | Infraestructura | AB#43 | PR por abrir | 🔍 |

Rutas de las clases de test (bajo `backend/src/test/java/es/uclm/esibuy/backend/`):
- `UsuarioTest`: `usuarios/domain/model/` (JUnit, sin Spring)
- `AdministracionUsuariosServiceTest`: `usuarios/application/service/` (Mockito, sin Spring)
- `UsuarioControllerTest`: `usuarios/infrastructure/adapter/in/web/` (con `@WebMvcTest` y usuarios simulados)
- `CuentaActivaFilterTest`: `usuarios/infrastructure/adapter/in/web/` (Mockito, sin Spring)
- `UsuarioMongoAdapterTest`: `usuarios/infrastructure/adapter/out/persistence/` (con Testcontainers)

---

## HU23 · Desactivar usuario

### Tabla A · HU23 ↔ casos de prueba

| Caso | Descripción | Tipo | Tests | Estado |
|---|---|---|---|---|
| HU23-CP01 | Un administrador desactiva una cuenta activa y recibe sus datos con estado DESACTIVADO | Unitario + integración (capa web) | 2 | ✅ Pasa |
| HU23-CP02 | Desactivar un usuario que no existe devuelve 404 y no guarda nada | Unitario + integración (capa web) | 2 | ✅ Pasa |
| HU23-CP03 | Un administrador no puede desactivar su propia cuenta (409 `CANNOT_DEACTIVATE_SELF`) | Unitario + integración (capa web) | 2 | ✅ Pasa |
| HU23-CP04 | No se puede desactivar al último administrador activo (409 `LAST_ACTIVE_ADMIN`) | Unitario + integración | 4 | ✅ Pasa |
| HU23-CP05 | Se puede desactivar a un administrador activo si queda otro administrador activo | Unitario | 1 | ✅ Pasa |
| HU23-CP06 | Sin sesión (401) o sin ser administrador (403) no se puede desactivar una cuenta | Integración (capa web) | 2 | ✅ Pasa |
| HU23-CP07 | Una petición sin usuario en sesión pasa sin consultar la cuenta | Unitario | 1 | 🔍 En revisión |
| HU23-CP08 | Una petición de un usuario con la cuenta activa pasa y conserva su sesión | Unitario | 1 | 🔍 En revisión |
| HU23-CP09 | Si la cuenta del usuario en sesión está desactivada, se invalida su sesión y responde 401 | Unitario | 1 | 🔍 En revisión |
| HU23-CP10 | Si el usuario en sesión ya no existe, se invalida su sesión y responde 401 | Unitario | 1 | 🔍 En revisión |

Notas:
- **HU23-CP01 a CP06:** casos propuestos en la T39 (AB#39) a partir del contrato del frontend (`frontend/docs/hu23-desactivar-usuario.md`). Pendientes de confirmar con el equipo y de añadir a la HU23 en Azure Boards.
- **Backend (T37/T38, AB#37 y AB#38):** el mismo `PATCH /api/admin/usuarios/{id}/status` de la HU24, ahora con `{ "estado": "DESACTIVADO" }`, en `AdministracionUsuariosUseCase.desactivar`. El controlador pasa el email del usuario en sesión (`Authentication.getName()`, como `/api/auth/me`) para la regla de no desactivarse a sí mismo. El conteo de administradores activos es `UsuarioRepositoryPort.contarAdministradoresActivos()`. Los 409 llevan los códigos que espera el frontend.
- **Invalidar la sesión (T206, AB#206):** `CuentaActivaFilter` comprueba en cada petición con un usuario en sesión que su cuenta sigue activa; si no (o si ya no existe), invalida la sesión y responde 401, como espera el frontend. Se registra como bean en `UsuariosConfig` y Spring Boot lo añade a los filtros de cada petición, sin tocar `SecurityConfig`. El login (HU2) guarda al usuario en sesión con su email como nombre, que es lo que usan el filtro y la regla de HU23-CP03. Casos HU23-CP07 a CP10 propuestos en la T206, pendientes de confirmar con el equipo.
- **Frontend:** los tests de la HU23 (AB#41) no tienen todavía filas en la matriz.

### Tabla B · casos de prueba ↔ tests

| Caso | Clase de test | Método | Capa | Task | Origen | Estado |
|---|---|---|---|---|---|---|
| HU23-CP01 | `AdministracionUsuariosServiceTest` | `desactivaUnaCuentaActivaYLaGuarda` | Aplicación | AB#39 | PR #79 | ✅ |
| HU23-CP01 | `UsuarioControllerTest` | `administradorDesactivaUnaCuentaYRecibeSusDatos` | Infraestructura | AB#39 | PR #79 | ✅ |
| HU23-CP02 | `AdministracionUsuariosServiceTest` | `rechazaDesactivarUnUsuarioQueNoExiste` | Aplicación | AB#39 | PR #79 | ✅ |
| HU23-CP02 | `UsuarioControllerTest` | `desactivarUnUsuarioInexistenteDevuelve404` | Infraestructura | AB#39 | PR #79 | ✅ |
| HU23-CP03 | `AdministracionUsuariosServiceTest` | `rechazaQueUnAdministradorSeDesactiveASiMismo` | Aplicación | AB#39 | PR #79 | ✅ |
| HU23-CP03 | `UsuarioControllerTest` | `desactivarseASiMismoDevuelve409` | Infraestructura | AB#39 | PR #79 | ✅ |
| HU23-CP04 | `AdministracionUsuariosServiceTest` | `rechazaDesactivarAlUltimoAdministradorActivo` | Aplicación | AB#39 | PR #79 | ✅ |
| HU23-CP04 | `UsuarioControllerTest` | `desactivarAlUltimoAdministradorActivoDevuelve409` | Infraestructura | AB#39 | PR #79 | ✅ |
| HU23-CP04 | `UsuarioMongoAdapterTest` | `contarAdministradoresActivosSoloCuentaLosAdministradoresActivos` | Infraestructura | AB#39 | PR #79 | ✅ |
| HU23-CP04 | `UsuarioMongoAdapterTest` | `contarAdministradoresActivosDevuelveCeroSinAdministradoresActivos` | Infraestructura | AB#39 | PR #79 | ✅ |
| HU23-CP05 | `AdministracionUsuariosServiceTest` | `desactivaAUnAdministradorSiQuedaOtroActivo` | Aplicación | AB#39 | PR #79 | ✅ |
| HU23-CP06 | `UsuarioControllerTest` | `unNoAdministradorNoPuedeDesactivarCuentas` (cliente y vendedor) | Infraestructura | AB#39 | PR #79 | ✅ |
| HU23-CP06 | `UsuarioControllerTest` | `desactivarSinSesionEstaProtegido` | Infraestructura | AB#39 | PR #79 | ✅ |
| HU23-CP07 | `CuentaActivaFilterTest` | `sinUsuarioEnSesionDejaPasar` | Infraestructura | AB#39 | PR #85 | 🔍 |
| HU23-CP08 | `CuentaActivaFilterTest` | `cuentaActivaDejaPasar` | Infraestructura | AB#39 | PR #85 | 🔍 |
| HU23-CP09 | `CuentaActivaFilterTest` | `cuentaDesactivadaInvalidaLaSesionYDevuelve401` | Infraestructura | AB#39 | PR #85 | 🔍 |
| HU23-CP10 | `CuentaActivaFilterTest` | `usuarioInexistenteInvalidaLaSesionYDevuelve401` | Infraestructura | AB#39 | PR #85 | 🔍 |

Rutas de las clases de test (bajo `backend/src/test/java/es/uclm/esibuy/backend/`):
- `AdministracionUsuariosServiceTest`: `usuarios/application/service/` (Mockito, sin Spring)
- `UsuarioControllerTest`: `usuarios/infrastructure/adapter/in/web/` (con `@WebMvcTest` y `@WithMockUser`)
- `UsuarioMongoAdapterTest`: `usuarios/infrastructure/adapter/out/persistence/` (con Testcontainers)
- `CuentaActivaFilterTest`: `usuarios/infrastructure/adapter/in/web/` (Mockito y petición simulada, sin Spring)

---

## HU24 · Activar usuario

### Tabla A · HU24 ↔ casos de prueba

| Caso | Descripción | Tipo | Tests | Estado |
|---|---|---|---|---|
| HU24-CP01 | Un administrador activa una cuenta desactivada y recibe sus datos con estado ACTIVO | Unitario + integración | 3 | ✅ Pasa |
| HU24-CP02 | Rechaza un estado que no es ACTIVO ni DESACTIVADO (400) y no cambia nada | Integración (capa web) | 1 | ✅ Pasa |
| HU24-CP03 | Activar un usuario que no existe devuelve 404 | Unitario + integración | 3 | ✅ Pasa |
| HU24-CP04 | Un usuario que no es administrador no puede activar cuentas (403) | Integración (capa web) | 1 | ✅ Pasa |

Notas:
- **HU24-CP01 a CP04:** casos propuestos en la T33/T34 (AB#33, AB#34) a partir del contrato del frontend (`frontend/docs/hu24-activar-usuario.md`). Pendientes de confirmar con el equipo y de añadir a la HU24 en Azure Boards.
- **Backend (T33/T34):** `PATCH /api/admin/usuarios/{id}/status` con `{ "estado": "ACTIVO" }`, en `AdministracionUsuariosUseCase`. El 403 lo da la regla de `SecurityConfig` de la T58.
- **HU23 (T39):** el mismo endpoint admite ahora también `DESACTIVADO` (ver la HU23). Por eso HU24-CP02 envía `BLOQUEADO` en lugar de `DESACTIVADO`, y su método pasó de `rechazaUnEstadoDistintoDeActivo` a `rechazaUnEstadoNoAdmitido`.
- **Pendiente con el equipo:** el `PATCH` real necesitará el token CSRF (se decide con el login, task 230); y en la aplicación arrancada el 400 de CP02 sale como 403 por el fallo de `/error` bloqueado en `SecurityConfig`. Con MockMvc se ve el 400.
- **Frontend:** los tests de la HU24 (AB#36) no tienen todavía filas en la matriz.

### Tabla B · casos de prueba ↔ tests

| Caso | Clase de test | Método | Capa | Task | Origen | Estado |
|---|---|---|---|---|---|---|
| HU24-CP01 | `AdministracionUsuariosServiceTest` | `activaUnaCuentaDesactivadaYLaGuarda` | Aplicación | AB#34 | PR #63 | ✅ |
| HU24-CP01 | `UsuarioControllerTest` | `administradorActivaUnaCuentaYRecibeSusDatos` | Infraestructura | AB#34 | PR #63 | ✅ |
| HU24-CP01 | `UsuarioMongoAdapterTest` | `buscarPorIdDevuelveElUsuarioGuardado` | Infraestructura | AB#34 | PR #63 | ✅ |
| HU24-CP02 | `UsuarioControllerTest` | `rechazaUnEstadoNoAdmitido` | Infraestructura | AB#34 · AB#39 | PR #63 (modificado en PR #79) | ✅ |
| HU24-CP03 | `AdministracionUsuariosServiceTest` | `rechazaActivarUnUsuarioQueNoExiste` | Aplicación | AB#34 | PR #63 | ✅ |
| HU24-CP03 | `UsuarioControllerTest` | `activarUnUsuarioInexistenteDevuelve404` | Infraestructura | AB#34 | PR #63 | ✅ |
| HU24-CP03 | `UsuarioMongoAdapterTest` | `buscarPorIdDevuelveVacioSiNoExiste` | Infraestructura | AB#34 | PR #63 | ✅ |
| HU24-CP04 | `UsuarioControllerTest` | `unNoAdministradorNoPuedeActivarCuentas` (cliente y vendedor) | Infraestructura | AB#34 · AB#60 | PR #63 (ampliado en PR #64) | ✅ |

Rutas de las clases de test (bajo `backend/src/test/java/es/uclm/esibuy/backend/`):
- `AdministracionUsuariosServiceTest`: `usuarios/application/service/` (Mockito, sin Spring)
- `UsuarioControllerTest`: `usuarios/infrastructure/adapter/in/web/` (con `@WebMvcTest` y `@WithMockUser`)
- `UsuarioMongoAdapterTest`: `usuarios/infrastructure/adapter/out/persistence/` (con Testcontainers)

---

## HU26 · Supervisión de vendedores (solo lectura)

### Tabla A · HU26 ↔ casos de prueba

| Caso | Descripción | Tipo | Tests | Estado |
|---|---|---|---|---|
| HU26-CP01 | El listado solo trae vendedores, con sus datos y sin datos sensibles | Integración (capa web) | 1 | ✅ Pasa |
| HU26-CP02 | El listado se ordena por nombre o por estado (pendientes, activos, desactivados), en sentido ascendente o descendente | Integración (capa web y MongoDB) | 4 | ✅ Pasa |
| HU26-CP03 | La ficha de un vendedor trae sus datos, sin datos sensibles | Unitario + integración (capa web) | 2 | ✅ Pasa |
| HU26-CP04 | La ficha de un usuario que no existe o que no es vendedor devuelve 404 | Unitario + integración (capa web) | 3 | ✅ Pasa |
| HU26-CP05 | Sin sesión (401) o sin ser administrador (403) no se ve ni el listado ni la ficha | Integración (capa web) | 2 | ✅ Pasa |

Notas:
- **HU26-CP01 a CP05:** casos propuestos en la T30 (AB#30) a partir de los criterios de aceptación de la HU26 y del contrato del frontend (`frontend/docs/hu26-supervision-vendedores.md`). Pendientes de confirmar con el equipo y de añadir a la HU26 en Azure Boards.
- **Backend (T29, AB#29, PR #78):** `GET /api/admin/sellers` (el listado de la HU19 con el rol `VENDEDOR` fijo y ordenación por nombre o estado) y `GET /api/admin/sellers/{id}` (`consultarVendedor`).
- **Sin tests todavía, porque no existe el código:** los productos y los pedidos del vendedor (`/sellers/{id}/products` y `/orders`), sus datos propios (`nombreComercial`, `categoriaPrincipal`, `nif`) y el filtro y orden por categoría.
- **Frontend:** los tests de la HU26 no tienen todavía filas en la matriz.

### Tabla B · casos de prueba ↔ tests

| Caso | Clase de test | Método | Capa | Task | Origen | Estado |
|---|---|---|---|---|---|---|
| HU26-CP01 | `UsuarioControllerTest` | `administradorRecibeElListadoDeVendedores` | Infraestructura | AB#30 | PR #80 | ✅ |
| HU26-CP02 | `UsuarioControllerTest` | `listadoDeVendedoresTraduceLaOrdenacion` (nombre, estado, categoría y sin orden) | Infraestructura | AB#30 | PR #80 | ✅ |
| HU26-CP02 | `UsuarioMongoAdapterTest` | `listarOrdenaPorEstadoAscendente` | Infraestructura | AB#30 | PR #80 | ✅ |
| HU26-CP02 | `UsuarioMongoAdapterTest` | `listarOrdenaPorEstadoDescendente` | Infraestructura | AB#30 | PR #80 | ✅ |
| HU26-CP02 | `UsuarioMongoAdapterTest` | `listarOrdenaPorNombre` | Infraestructura | AB#30 | PR #80 | ✅ |
| HU26-CP03 | `AdministracionUsuariosServiceTest` | `consultarVendedorDevuelveElVendedor` | Aplicación | AB#30 | PR #80 | ✅ |
| HU26-CP03 | `UsuarioControllerTest` | `fichaDeUnVendedor` | Infraestructura | AB#30 | PR #80 | ✅ |
| HU26-CP04 | `AdministracionUsuariosServiceTest` | `consultarVendedorDeOtroRolFalla` | Aplicación | AB#30 | PR #80 | ✅ |
| HU26-CP04 | `AdministracionUsuariosServiceTest` | `consultarVendedorQueNoExisteFalla` | Aplicación | AB#30 | PR #80 | ✅ |
| HU26-CP04 | `UsuarioControllerTest` | `fichaDeUnNoVendedorDevuelve404` | Infraestructura | AB#30 | PR #80 | ✅ |
| HU26-CP05 | `UsuarioControllerTest` | `vendedoresSinSesionDevuelve401` (listado y ficha) | Infraestructura | AB#30 | PR #80 | ✅ |
| HU26-CP05 | `UsuarioControllerTest` | `noAdministradorNoVeLosVendedores` (cliente y vendedor, en el listado y la ficha) | Infraestructura | AB#30 | PR #80 | ✅ |

Rutas de las clases de test (bajo `backend/src/test/java/es/uclm/esibuy/backend/`):
- `AdministracionUsuariosServiceTest`: `usuarios/application/service/` (Mockito, sin Spring)
- `UsuarioControllerTest`: `usuarios/infrastructure/adapter/in/web/` (con `@WebMvcTest` y `@WithMockUser`)
- `UsuarioMongoAdapterTest`: `usuarios/infrastructure/adapter/out/persistence/` (con Testcontainers)

---

## HU31 · Dar de alta un producto

### Tabla A · HU31 ↔ casos de prueba

| Caso | Descripción | Tipo | Tests | Estado |
|---|---|---|---|---|
| HU31-CP01 | Se crea un producto con datos correctos, con o sin id antes de persistirlo | Unitario (dominio) | 2 | ✅ Pasa |
| HU31-CP02 | Un producto sin vendedor se rechaza | Unitario (dominio) | 1 | ✅ Pasa |
| HU31-CP03 | El nombre es obligatorio y admite hasta 100 caracteres | Unitario (dominio) | 2 | ✅ Pasa |
| HU31-CP04 | El precio tiene que ser mayor que 0 | Unitario (dominio) | 2 | ✅ Pasa |
| HU31-CP05 | El stock no puede ser negativo (0 es válido) | Unitario (dominio) | 1 | ✅ Pasa |
| HU31-CP06 | Un producto sin categoría se rechaza (una sola categoría por producto) | Unitario (dominio) | 1 | ✅ Pasa |
| HU31-CP07 | Los dos descuentos (normal y premium) son obligatorios, y un descuento inactivo no lleva porcentaje | Unitario (dominio) | 5 | ✅ Pasa |
| HU31-CP08 | Un descuento activo lleva un porcentaje mayor que 0 y no superior a 100 | Unitario (dominio) | 2 | ✅ Pasa |
| HU31-CP09 | La imagen es opcional y, si existe, es una dirección http o https | Unitario (dominio) | 2 | ✅ Pasa |
| HU31-CP10 | La descripción es opcional y admite hasta 1000 caracteres | Unitario (dominio) | 1 | ✅ Pasa |
| HU31-CP11 | Un producto se guarda en la colección `productos` con todos sus datos y se recupera igual | Unitario + integración MongoDB | 4 | 🔍 En revisión |
| HU31-CP12 | Buscar un producto que no existe devuelve vacío | Integración MongoDB | 1 | 🔍 En revisión |

Notas:
- **HU31-CP01 a CP10:** casos propuestos en la T201 (AB#201) a partir del contrato del formulario de producto del frontend (`frontend/docs/hu31-formulario-producto.md`). Pendientes de confirmar con el equipo y de añadir a la HU31 en Azure Boards.
- **Backend (T201):** solo la entidad `Producto` y el objeto de valor `Descuento` (`productos/domain/model`), en Java puro. Aún no hay repositorio, servicio ni endpoint: llegarán con las tasks AB#202, AB#204 y AB#205. Los casos de capa web y de persistencia (400/409, guardado en MongoDB) se añadirán con ellas.
- **HU31-CP11 y CP12:** casos propuestos en la T202 (AB#202) para la persistencia. Pendientes de confirmar con el equipo y de añadir a la HU31 en Azure Boards.
- **Backend (T202):** repositorio de productos: puerto `ProductoRepositoryPort` (`guardar` y `buscarPorId`) y su adaptador de MongoDB, con documento y mapper propios. El listado por vendedor llegará con la AB#183.
- **HU31-CP11 y CP12:** casos propuestos en la T202 (AB#202) para la persistencia. Pendientes de confirmar con el equipo y de añadir a la HU31 en Azure Boards.
- **Backend (T202):** repositorio de productos: puerto `ProductoRepositoryPort` (`guardar` y `buscarPorId`) y su adaptador de MongoDB, con documento y mapper propios. El listado por vendedor llegará con la AB#183.
- **Una sola categoría por producto:** decisión del equipo; el producto guarda `categoriaId`, no una lista. Desde la T202 el validador de MongoDB también lo exige (antes era una lista `categoriaIds`).; desde la T202 el validador de MongoDB también exige `categoriaId` (antes era una lista `categoriaIds`).
- **Estado:** los tests de la T201 están en `develop` (PR #83). Los de la T202 están en revisión (PR #86) y pasan a ✅ cuando se mergee.
- **Frontend:** los tests de la HU31 (AB#182) no tienen todavía filas en la matriz.

### Tabla B · casos de prueba ↔ tests

| Caso | Clase de test | Método | Capa | Task | Origen | Estado |
|---|---|---|---|---|---|---|
| HU31-CP01 | `ProductoTest` | `creaUnProductoConDatosCorrectos` | Dominio | AB#201 | PR #83 | ✅ |
| HU31-CP01 | `ProductoTest` | `permiteUnProductoSinIdAntesDePersistir` | Dominio | AB#201 | PR #83 | ✅ |
| HU31-CP02 | `ProductoTest` | `rechazaUnProductoSinVendedor` (nulo, vacío y en blanco) | Dominio | AB#201 | PR #83 | ✅ |
| HU31-CP03 | `ProductoTest` | `rechazaUnProductoSinNombre` (nulo, vacío y en blanco) | Dominio | AB#201 | PR #83 | ✅ |
| HU31-CP03 | `ProductoTest` | `elNombreTieneUnMaximoDe100Caracteres` | Dominio | AB#201 | PR #83 | ✅ |
| HU31-CP04 | `ProductoTest` | `rechazaUnPrecioNoPositivo` (0, 0.00, -1 y -0.01) | Dominio | AB#201 | PR #83 | ✅ |
| HU31-CP04 | `ProductoTest` | `rechazaUnPrecioNuloYAceptaElMinimo` | Dominio | AB#201 | PR #83 | ✅ |
| HU31-CP05 | `ProductoTest` | `rechazaUnStockNegativoYAceptaCero` | Dominio | AB#201 | PR #83 | ✅ |
| HU31-CP06 | `ProductoTest` | `rechazaUnProductoSinCategoria` (nulo, vacío y en blanco) | Dominio | AB#201 | PR #83 | ✅ |
| HU31-CP07 | `DescuentoTest` | `conservaUnDescuentoActivoValido` | Dominio | AB#201 | PR #83 | ✅ |
| HU31-CP07 | `DescuentoTest` | `aceptaLosPorcentajesLimite` (0,01, 50 y 100) | Dominio | AB#201 | PR #83 | ✅ |
| HU31-CP07 | `DescuentoTest` | `sinDescuentoEsInactivoYSinPorcentaje` | Dominio | AB#201 | PR #83 | ✅ |
| HU31-CP07 | `DescuentoTest` | `unDescuentoInactivoIgnoraElPorcentaje` | Dominio | AB#201 | PR #83 | ✅ |
| HU31-CP07 | `ProductoTest` | `rechazaUnProductoSinDescuentos` | Dominio | AB#201 | PR #83 | ✅ |
| HU31-CP08 | `DescuentoTest` | `rechazaPorcentajesFueraDeRango` (0, -5, 100.01 y 150) | Dominio | AB#201 | PR #83 | ✅ |
| HU31-CP08 | `DescuentoTest` | `rechazaUnDescuentoActivoSinPorcentaje` | Dominio | AB#201 | PR #83 | ✅ |
| HU31-CP09 | `ProductoTest` | `aceptaImagenesHttpOVacias` (http, https y vacía) | Dominio | AB#201 | PR #83 | ✅ |
| HU31-CP09 | `ProductoTest` | `rechazaImagenesQueNoSonHttp` (javascript:, ftp:, texto libre y data:) | Dominio | AB#201 | PR #83 | ✅ |
| HU31-CP10 | `ProductoTest` | `laDescripcionTieneUnMaximoDe1000Caracteres` | Dominio | AB#201 | PR #83 | ✅ |
| HU31-CP11 | `ProductoDocumentMapperTest` | `convierteProductoEnDocumento` | Infraestructura | AB#202 | PR #86 | 🔍 |
| HU31-CP11 | `ProductoDocumentMapperTest` | `convierteDocumentoEnProducto` | Infraestructura | AB#202 | PR #86 | 🔍 |
| HU31-CP11 | `ProductoMongoAdapterTest` | `guardarAsignaIdYLoGuardaEnProductos` | Infraestructura | AB#202 | PR #86 | 🔍 |
| HU31-CP11 | `ProductoMongoAdapterTest` | `buscarPorIdDevuelveElProductoGuardado` | Infraestructura | AB#202 | PR #86 | 🔍 |
| HU31-CP12 | `ProductoMongoAdapterTest` | `buscarPorIdDevuelveVacioSiNoExiste` | Infraestructura | AB#202 | PR #86 | 🔍 |

Rutas de las clases de test (bajo `backend/src/test/java/es/uclm/esibuy/backend/`):
- `ProductoTest` y `DescuentoTest`: `productos/domain/model/` (JUnit, sin Spring)
- `ProductoDocumentMapperTest`: `productos/infrastructure/adapter/out/persistence/` (JUnit, sin Spring)
- `ProductoMongoAdapterTest`: `productos/infrastructure/adapter/out/persistence/` (con Testcontainers)

---

## Tests técnicos sin caso de prueba

Tests que no validan una HU concreta.

| Clase de test | Método | Qué comprueba |
|---|---|---|
| `BackendApplicationTests` | `contextLoads` | Que el contexto de Spring arranca, con el MongoDB de Testcontainers y sin Atlas (`MONGODB_URI` no existe en el CI; AB#212) |
| `UsuariosConfigTest` | `elContextoConectaLasPiezasDelRegistro` | Que Spring conecta servicio de registro, hash BCrypt y adaptador de MongoDB (AB#216, PR #28) |
| `UsuariosConfigTest` | `elContextoConectaLasPiezasDelLogin` | Que Spring conecta el login con sus adaptadores de códigos (MongoDB), generador (`SecureRandom`) y correo (SMTP) (AB#230, PR #67) |
| `UsuariosConfigTest` | `elContextoConectaLasPiezasDelAuthenticator` | Que Spring conecta el paso 3 (`AuthenticatorService`) con la librería TOTP, el cifrado AES-GCM y el adaptador de `factores_autenticacion` (AB#20, PR #75) |
| `UsuariosConfigTest` | `elContextoRegistraElFiltroDeCuentaActiva` | Que Spring registra `CuentaActivaFilter`, el filtro que invalida la sesión de las cuentas desactivadas (HU23; AB#39, AB#206, PR #85) |
| `SecurityConfigTest` | `unaPeticionRecibeElTokenCsrfEnUnaCookie` | Que cualquier respuesta, aunque sea un 401, entrega el token CSRF en la cookie `XSRF-TOKEN` sin `HttpOnly` (AB#230, PR #67) |
| `SecurityConfigTest` | `unPostSinTokenCsrfSeRechaza` | Que un `POST` sin token CSRF se rechaza con 403 (AB#230, PR #67) |
| `SecurityConfigTest` | `unPostConElTokenDeLaCookieEnLaCabeceraSeAcepta` | Que un `POST` con el token de la cookie en la cabecera `X-XSRF-TOKEN` pasa el filtro CSRF (AB#230, PR #67) |
| `SecurityConfigTest` | `laPaginaDeErrorConservaElCodigoSinSesion` | Que `/error` es accesible sin sesión, para que un 403 (CSRF) o un 500 no lleguen como 401 (AB#230, PR #67) |
| `client.test.js` (frontend) | `request_postConCookieCsrf_enviaElTokenEnLaCabecera` | Que el cliente HTTP copia la cookie `XSRF-TOKEN` en la cabecera `X-XSRF-TOKEN` (AB#230, PR #67) |
| `client.test.js` (frontend) | `request_postSinCookieCsrf_noEnviaLaCabecera` | Que sin la cookie no envía la cabecera CSRF (AB#230, PR #67) |
| `UsuarioControllerTest` | `otraRutaSinSesionEstaProtegida` | Que una ruta distinta del registro exige sesión (mínimo privilegio en `SecurityConfig`; AB#14, PR #30) |
| `EmailTest` | `dosEmailsConElMismoValorSonIguales` | Que `Email` es un value object con igualdad por valor (AB#214, PR #34) |
| `UsuarioControllerTest` | `listadoSinSesionEstaProtegido` | Que el listado administrativo sin sesión devuelve 401 (`SecurityConfig`) |
| `UsuarioMongoAdapterTest` | `elDocumentoNoIncluyeElCampoClass` | Que los documentos guardados no incluyen el campo `_class` de Spring Data (`MongoConfig`; AB#210, PR #43) |
| `UsuarioMongoAdapterTest` | `guardaFechasDeCreacionYModificacion` | Que se guardan `createdAt` y `updatedAt` (auditoría de Spring Data) y que `createdAt` no cambia al actualizar (AB#210, PR #43) |
| `ProductoMongoAdapterTest` | `guardarUnProductoLeidoLoActualiza` | Que guardar un producto leído lo actualiza sin duplicarlo y conserva `createdAt` (AB#202, PR #86) |
| `ProductoMongoAdapterTest` | `elPrecioSeGuardaComoNumero` | Que el precio y los porcentajes de descuento se guardan como `Decimal128`, no como texto (AB#202, PR #86) |
| `InicializadorColeccionesTest` | `creaLasDiezColecciones` | Que al arrancar se crean las 10 colecciones del modelo de BBDD (AB#210, PR #43) |
| `InicializadorColeccionesTest` | `sePuedeEjecutarOtraVez` | Que el inicializador se puede ejecutar en cada arranque sin fallar ni duplicar índices (AB#210, PR #43) |
| `InicializadorColeccionesTest` | `actualizaLaValidacionDeUnaColeccionExistente` | Que aplica la validación a una colección que ya existía sin ella (AB#210, PR #43) |
| `InicializadorColeccionesTest` | `sinPermisoParaCambiarLaValidacionNoFalla` | Que sin permiso `collMod` (usuario de la aplicación en Atlas) avisa y la aplicación arranca igual (AB#210, PR #43) |
| `InicializadorColeccionesTest` | `aceptaUsuariosValidos` | Que la validación de `usuarios` acepta los documentos de la aplicación, los campos de vendedor y administrador, `lockedUntil` nulo y campos nuevos (AB#210, PR #43) |
| `InicializadorColeccionesTest` | `rechazaUsuariosInvalidos` | Que la validación de `usuarios` rechaza campos obligatorios ausentes y valores fuera del modelo (AB#210, PR #43) |
| `InicializadorColeccionesTest` | `usuariosConDniYNombreComercialUnicos` | Índices de `usuarios`: DNI único por rol (opcional) y nombre comercial único (AB#210, PR #43) |
| `InicializadorColeccionesTest` | `validaSesionesFactoresYTokens` | Validación de `sesiones`, `factores_autenticacion` y `tokens_seguridad`, con `tokenHash` obligatorio (AB#210, PR #43; ampliado en AB#232, PR #56) |
| `InicializadorColeccionesTest` | `validaProductos` | Validación de `productos`: precio mayor que 0, stock no negativo, y nombre y categoría (`categoriaId`) obligatorios (AB#210, PR #43; ampliado en AB#202, PR #86) |
| `InicializadorColeccionesTest` | `validaDescuentosDeProductos` | Descuentos de `productos` con el formato del formulario de producto (HU31): `{activo, porcentaje}` (AB#210, PR #43) |
| `InicializadorColeccionesTest` | `validaElRestoDeColecciones` | Validación de `pedidos`, `carritos`, `valoraciones`, `categorias` y `listas_deseos` (AB#210, PR #43) |
| `InicializadorColeccionesTest` | `rechazaDuplicadosEnLosIndicesUnicos` | Índices únicos de `carritos`, `valoraciones`, `categorias`, `listas_deseos` y `factores_autenticacion` (AB#210, PR #43) |
| `InicializadorColeccionesTest` | `creaLosIndicesDeLasAnotaciones` | Que también crea los índices de las anotaciones de los `@Document` (email único en `usuarios`) (AB#210, PR #43) |
| `InicializadorColeccionesTest` | `sesionesYTokensCaducan` | Índices TTL de `expiresAt` en `sesiones` y `tokens_seguridad` (AB#210, PR #43) |

`InicializadorColeccionesTest` está en `shared/infrastructure/config/` y usa Testcontainers. HU36 (crear la base de datos) no tiene casos de prueba en Azure Boards, por eso sus tests están en esta sección.
