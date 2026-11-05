# Matriz de trazabilidad · Casos de Prueba ↔ Historias de Usuario

Relaciona cada historia de usuario (HU) con sus casos de prueba (CP) y cada caso con los tests que lo validan.

- **Tabla A** (HU ↔ CP): ¿está probada cada historia?
- **Tabla B** (CP ↔ test): ¿qué test valida cada caso y de dónde salió?

**Mantenimiento:** todo PR que añada, modifique o elimine tests actualiza esta matriz en el mismo PR (ver el CLAUDE.md). Cada test lleva un `@DisplayName` que empieza por el ID de su caso (`HU<n>-CP<nn>: ...`), que es el vínculo entre el código y la matriz.

**Estados:** ✅ Pasa · 🔍 En revisión (PR abierto) · ⏳ Pendiente · ⏸ Fuera de alcance · ❌ Falla

---

## HU1 · Registrar usuario

### Tabla A · HU1 ↔ casos de prueba

| Caso | Descripción | Tipo | Tests | Estado |
|---|---|---|---|---|
| HU1-CP01 | Registra un cliente con rol CLIENTE y estado PENDIENTE | Unitario + integración | 8 | ✅ Pasa |
| HU1-CP02 | Guarda la contraseña hasheada, nunca en claro | Unitario + integración | 8 | ✅ Pasa |
| HU1-CP03 | Rechaza un correo ya registrado y no guarda nada | Unitario + integración | 5 | ✅ Pasa |
| HU1-CP04 | Rechaza un username ya registrado | — | 0 | ⏸ Fuera de alcance (no está decidido si existe username) |
| HU1-CP05 | Rechaza contraseñas de menos de 8 caracteres o de más de 64 | Unitario + integración | 10 | ✅ Pasa |
| HU1-CP06 | Rechaza el registro si no se acepta la política de privacidad y guarda el consentimiento | Unitario | 5 | ✅ Pasa |
| HU1-CP07 | Rechaza un email con formato inválido | Unitario + integración (capa web) | 8 | ✅ Pasa |
| HU1-CP08 | Rechaza el registro si falta un campo obligatorio o supera su longitud máxima | Unitario + integración (capa web) | 11 | ✅ Pasa |
| HU1-CP09 | Rechaza contraseñas de la lista de contraseñas comunes | Unitario | 4 | ✅ Pasa |
| HU1-CP10 | Rechaza contraseñas que contienen datos personales del usuario | Unitario | 4 | ✅ Pasa |
| HU1-CP11 | El usuario cumple sus invariantes de dominio (datos personales y de cliente obligatorios y coherentes con el rol, desactivación, fecha de alta) | Unitario | 17 | ✅ Pasa |
| HU1-CP12 | Toda cuenta nueva nace en estado PENDIENTE hasta que un administrador la verifica | Unitario | 4 | ✅ Pasa |
| HU1-CP13 | Rechaza a un cliente menor de edad | Unitario | 4 | ✅ Pasa |
| HU1-CP14 | Rechaza el registro si las contraseñas no coinciden | Unitario | 1 | ✅ Pasa |
| HU1-CP15 | Rechaza un DNI o un teléfono con formato inválido | Unitario + integración (capa web) | 7 | ✅ Pasa |
| HU1-CP16 | Rechaza un DNI ya registrado en otra cuenta del mismo rol | Unitario + integración | 5 | ✅ Pasa |
| HU1-CP17 | Rechaza contraseñas sin mayúscula, minúscula, número o símbolo (misma composición que el frontend) | Unitario | 7 | ✅ Pasa |
| HU1-CP18 | El email no distingue mayúsculas ni espacios en los extremos: se guarda normalizado y no se duplica | Unitario | 5 | ✅ Pasa |
| HU1-CP19 | Registra un vendedor en estado PENDIENTE con su nombre comercial y su categoría principal | Unitario + integración | 9 | ✅ Pasa |
| HU1-CP20 | Rechaza un nombre comercial ya registrado, también cuando se repite junto con el correo | Unitario + integración | 6 | ✅ Pasa |
| HU1-CP21 | Rechaza datos de vendedor inválidos o un perfil que no corresponde al rol | Unitario + integración (capa web) | 8 | ✅ Pasa |
| HU1-CP22 | Los errores 400 del registro indican el motivo con un `code` (contraseña, DNI, teléfono, edad, privacidad, email o campo) | Unitario + integración (capa web) | 14 | ✅ Pasa |
| HU1-CP23 | El formulario marca en su campo el motivo que da el backend (o un mensaje general si no tiene campo), y un DNI repetido se marca en el DNI | Frontend (unitario y página) | 7 | ✅ Pasa |
| HU1-CP24 | Tras registrarse se ofrece ir a iniciar sesión | Frontend (página) | 1 | ✅ Pasa (1, AB#234, PR #105) |
| HU1-CP25 | Con la sesión iniciada no se ofrece crear una cuenta y /register lleva a la página del rol | Frontend (página y rutas) | 3 | ✅ Pasa (3, AB#17 · AB#18, PR #105) |
| HU1-CP26 | Un registro correcto no se trata como un error aunque el backend responda 201 sin cuerpo | Frontend (API) | 1 | ✅ Pasa (1, AB#17 · AB#18, PR #105) |
| HU1-CP27 | En el registro, el password y su repetición solo se pueden escribir a mano: no admiten copiar, cortar, pegar ni soltar texto, y el login sigue admitiendo pegar (aunque no copiar, HU2-CP31) | Frontend (componente y página) | 7 | ✅ Pasa (7, AB#242, PR #125) |

Notas:
- **HU1-CP11:** en el PR #36, el bloqueo manual de la T214 se renombra a desactivar/activar (`desactivar()`, `activar()`, `estaDesactivado()`) para no confundirlo con el bloqueo temporal por intentos del login (HU2-CP03). En la T225 (AB#225) la consulta pasa a nombrarse en positivo: `estaActivo()`.
- **HU1-CP12 (AB#233):** toda cuenta nueva nace en estado `PENDIENTE` hasta que un administrador la verifica, y `estaActivo()` se calcula desde `estado` (el booleano `activo` se eliminó). Decisión de la daily del 09/10/2026; sustituye a la versión del PR #66, que dejaba el constructor en `ACTIVO`. Decidido por el equipo (10/10/2026): un administrador creado por otro administrador nace `ACTIVO`, porque lo verifica quien lo crea; la regla se aplicará con el alta de administradores (HU21), que aún no existe.
- **AB#216:** los tests de la capa de infraestructura (persistencia en MongoDB) se asignan a CP01, CP02 y CP03 como propuesta, pendiente de confirmar con el equipo.
- **HU1-CP07 y HU1-CP08:** casos propuestos en la T14 (AB#14) para la validación de entrada del endpoint de registro. Pendientes de confirmar con el equipo.
- **AB#227:** la validación web (`@Email`) acepta un email sin punto en el dominio, como `ana@localhost`, pero el value object `Email` lo rechaza. Antes de la T227 esa `EmailInvalidoException` no se manejaba y el registro respondía con un 500. Ahora `ManejadorGlobalErrores` la traduce a un 400 con el mismo mensaje genérico que una contraseña inválida.
- **HU1-CP09, HU1-CP10 y HU1-CP11:** casos propuestos en la T214 (AB#214, PR #34) para la capa de dominio. Pendientes de confirmar con el equipo. CP09 y CP10 salen de los requisitos de Seguridad (lista negra y «evitar datos personales»).
- **AB#226:** `RegistroUsuarioService` aplica `PoliticaContrasenas` (que ahora recibe `DatosPersonales` y limita a 64 caracteres) y deja de comprobar la longitud por su cuenta. La frontera de 8 caracteres usa una contraseña que no está en la lista negra.
- **AB#229 (registro del cliente con todos los campos):** el dominio agrupa los datos en value objects (`DatosPersonales`, `DatosCliente`, `Dni`, `Telefono`, `ConsentimientoPrivacidad`); nombre y apellidos pasan a ser obligatorios, por lo que se eliminan `permiteUsuarioSinNombreNiApellidos` y `creaClienteNuevoEstandarSinNombreNiId` (T214) y las comprobaciones de nombre/apellidos en blanco pasan de `UsuarioTest` a `DatosPersonalesTest`. Casos propuestos, pendientes de confirmar con el equipo: CP13 (mayoría de edad, `Contexto_frontend.md` 6.3), CP14 (repetir contraseña), CP15 (formato de DNI y teléfono, MVP del Sprint 1) y CP16 (DNI único por rol, índice `rol_dni` de la T210). El registro responde en `POST /api/auth/register/cliente` y el 409 lleva `code` (`EMAIL_ALREADY_REGISTERED` / `DNI_ALREADY_REGISTERED`), contrato acordado con el frontend.
- **AB#228:** `UsuarioInvalidoException` se traduce a 400 en el registro (datos del cliente). Un documento de MongoDB que no cumple las reglas del dominio se envuelve en `UsuarioPersistidoInvalidoException` (infraestructura) y acaba en 500. PENDIENTE DE REVISAR CON EL EQUIPO: las cuentas de prueba de Atlas sin nombre, apellidos, DNI o fecha de nacimiento ya no se pueden leer.
- **AB#239 (HU1-CP17):** la contraseña exige al menos una mayúscula, una minúscula, un número y un símbolo, con las mismas clases Unicode que el frontend (`accountRules.js`): las tildes y la ñ cuentan como letras y el espacio como símbolo. Con esta regla, las entradas antiguas de la lista negra (`password`, `12345678`…) ya no podían llegar a compararse, así que la lista se sustituye por contraseñas comunes que sí cumplen la composición (`P@ssw0rd`, `Password1!`…) y los tres tests de CP09 cambian sus contraseñas para seguir probando la lista negra y no la composición. Las contraseñas de los tests que esperan un registro correcto pasan de `clave1234` a `Clave-1234`.
- **AB#240 (HU1-CP18):** el value object `Email` normaliza en su constructor (`strip()` y minúsculas con `Locale.ROOT`) antes de validar el formato, y `RegistroUsuarioService` comprueba el duplicado con ese valor. Los puertos siguen recibiendo `String`; cambiarlos a `Email` queda como refactor posible. El paso 1 del login (`AutenticacionService.iniciarSesion`) sigue buscando con el email tal como llega: se propone a la HU2 como task propia (buscar con el email normalizado y responder credenciales inválidas ante un email mal formado). PENDIENTE DE REVISAR CON EL EQUIPO: las cuentas de Atlas con mayúsculas en el email dejarán de encontrarse; migrarlas a minúsculas o borrarlas.
- **AB#241 (HU1-CP19 a CP21, registro del vendedor):** `POST /api/auth/register/vendedor` con el contrato del frontend (una única categoría en `categoriaPrincipalId`, PR #81). El perfil del usuario pasa a la interfaz sellada `PerfilUsuario` (`DatosCliente` o `DatosVendedor`) como sexto parámetro de `Usuario`, sin cambiar las llamadas existentes; la factoría `nuevoVendedor` exige `DatosVendedor`, pero el constructor admite un VENDEDOR sin perfil para no romper las cuentas anteriores de Atlas ni los tests de otras HU (endurecer cuando se limpie Atlas). Los identificadores de categoría se guardan sin comprobarlos contra el catálogo (decisión del equipo). Un nombre comercial repetido devuelve 409 `COMMERCIAL_NAME_ALREADY_REGISTERED`, y si además se repite el correo, `EMAIL_AND_COMMERCIAL_NAME_ALREADY_REGISTERED`; un DNI repetido devuelve `DNI_ALREADY_REGISTERED`, que el formulario del vendedor no trata (comunicado al frontend). `registrar` pasa a `registrarCliente` y `RegistrarUsuarioCommand` a `RegistrarClienteCommand`.
- **Fallos de la demo del Sprint 1 (HU1-CP22, rama `fix/sprint1-demo-backend`):** el dominio indica el motivo del rechazo con el enum `MotivoRechazo` dentro de `ContrasenaInvalidaException` y `UsuarioInvalidoException`, y `ManejadorGlobalErrores` lo traduce a un `code` en inglés (`PASSWORD_TOO_SHORT`, `PASSWORD_TOO_LONG`, `PASSWORD_WEAK`, `PASSWORD_TOO_COMMON`, `PASSWORD_CONTAINS_PERSONAL_DATA`, `PASSWORDS_DO_NOT_MATCH`, `INVALID_EMAIL`, `INVALID_DNI`, `INVALID_PHONE`, `UNDERAGE`, `PRIVACY_NOT_ACCEPTED`; el motivo genérico `DATO_INVALIDO`, `VALIDATION_ERROR`). Los errores de `@Valid` de cualquier endpoint responden con el mismo formato y `VALIDATION_ERROR` (antes `INVALID_FIELD`; cambiado en la rama `fix/errores-validacion-api`, ver la nota de HU16-CP12). El registro recibe la imagen como `avatar`, igual que el resto de la API. Sin tasks nuevas en Azure: los commits llevan el `AB#` de la task original de cada parte.
- **HU1-CP23 a CP26 (fallos de la demo del Sprint 1, frontend):** `features/account/registroErrors.js` traduce cada `code` del backend a su campo y su mensaje; lo usan el registro, el alta de administrador (HU21-CP19) y la edición (HU22-CP16). Tras registrarse, un botón lleva al login, sin redirección automática. `GuestOnlyRoute` lleva a la página del rol a quien abre `/register` con sesión. El backend responde al registro con un 201 sin cuerpo, y `api/client.js` intentaba leerlo como JSON: el usuario se creaba pero la pantalla mostraba un error (CP26). El mock del registro responde ahora igual que el backend. Como en la HU19, el ID del caso va al principio del nombre del test (`it('HU1-CP23: …')`). Los tests anteriores de estas pantallas no tienen todavía filas en la matriz.
- **HU1-CP16 (AB#43):** la comprobación del DNI repetido pasa de `existePorDni` a `existePorDniYRol`, en el registro del cliente y en el del vendedor, para que coincida con el índice `rol_dni` (una persona puede tener una cuenta de cliente y otra de vendedor con el mismo DNI). Antes, un vendedor con el DNI de un cliente recibía un 409 aunque MongoDB lo habría aceptado. Lo pidió el equipo en la edición del vendedor (HU22); cambia código de la T241.
- **Frontend, fix del registro (sin task):** el formulario enviaba `tipoCliente` como `normal`/`premium` y el backend espera el enum `ESTANDAR`/`PREMIUM` (400 sin llegar al registro), y enviaba `telefono: ""` cuando se dejaba vacío, que el dominio rechaza como teléfono inválido. En `Register.test.jsx` se adapta `register_alElegirCliente_muestraTipoConNormalPreseleccionado` (ahora `register_alElegirCliente_muestraTipoEstandarPreseleccionadoConLosValoresDelBackend`) y se añaden `register_alEnviarDatosValidosDeCliente_enviaElTipoEstandarPorDefecto`, `register_alElegirPremium_enviaElTipoPremium`, `register_siElClienteNoIndicaTelefono_noEnviaElTelefono` y `register_siElVendedorNoIndicaTelefono_noEnviaElTelefono`. Los tests de `Register.test.jsx` no tienen todavía filas en la Tabla B ni un caso propio para el contrato del cuerpo de la petición: pendiente de decidir con el equipo a qué casos de la HU1 se asignan.
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
| HU1-CP01 | `UsuarioControllerTest` | `elRegistroDelClienteGuardaElAvatar` | Infraestructura | AB#229 | PR #104 | ✅ |
| HU1-CP02 | `RegistroUsuarioServiceTest` | `guardaContrasenaHasheada` | Aplicación | AB#16 | PR #14 | ✅ |
| HU1-CP02 | `BCryptHashContrasenaAdapterTest` | `hashNoEsLaContrasenaEnClaro` | Infraestructura | AB#15 | PR #20 | ✅ |
| HU1-CP02 | `BCryptHashContrasenaAdapterTest` | `hashEsVerificableConBCrypt` | Infraestructura | AB#15 | PR #20 | ✅ |
| HU1-CP02 | `BCryptHashContrasenaAdapterTest` | `hashUsaElCosteAcordado` | Infraestructura | AB#15 | PR #20 | ✅ |
| HU1-CP02 | `RegistrarClienteCommandTest` (antes `RegistrarUsuarioCommandTest`) | `toStringNoMuestraLaContrasena` | Aplicación | AB#223 · AB#241 | PR #26 (renombrado en PR #98) | ✅ |
| HU1-CP02 | `RegistrarVendedorCommandTest` | `toStringNoMuestraLaContrasena` | Aplicación | AB#241 | PR #98 | ✅ |
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
| HU1-CP06 | `RegistroUsuarioServiceTest` | `rechazaVendedorSiNoAceptaLaPrivacidad` | Aplicación | AB#241 | PR #98 | ✅ |
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
| HU1-CP09 | `PoliticaContrasenasTest` | `rechazaContrasenaDeListaNegra` | Dominio | AB#214 · AB#239 | PR #34 (modificado en PR #87) | ✅ |
| HU1-CP09 | `PoliticaContrasenasTest` | `rechazaContrasenaDeListaNegraConMayusculas` | Dominio | AB#214 · AB#239 | PR #34 (modificado en PR #87) | ✅ |
| HU1-CP09 | `RegistroUsuarioServiceTest` | `rechazaContrasenaComun` | Aplicación | AB#226 · AB#239 | PR #72 (modificado en PR #87) | ✅ |
| HU1-CP09 | `RegistroUsuarioServiceTest` | `elVendedorPasaPorLaPoliticaDeContrasenas` | Aplicación | AB#241 | PR #98 | ✅ |
| HU1-CP10 | `PoliticaContrasenasTest` | `rechazaContrasenaQueContieneElNombre` | Dominio | AB#214 | PR #34 | ✅ |
| HU1-CP10 | `PoliticaContrasenasTest` | `rechazaContrasenaQueContieneLosApellidos` | Dominio | AB#214 | PR #34 | ✅ |
| HU1-CP10 | `PoliticaContrasenasTest` | `rechazaContrasenaQueContieneLaParteLocalDelEmail` | Dominio | AB#214 | PR #34 | ✅ |
| HU1-CP10 | `RegistroUsuarioServiceTest` | `rechazaContrasenaConElNombre` | Aplicación | AB#226 | PR #72 | ✅ |
| HU1-CP11 | `UsuarioTest` | `creaUsuarioConDatosCorrectos` | Dominio | AB#214 | PR #34 | ✅ |
| HU1-CP11 | `UsuarioTest` | `permiteUsuarioSinIdAntesDePersistir` | Dominio | AB#214 | PR #34 | ✅ |
| HU1-CP11 | `UsuarioTest` | `rechazaEmailNulo` | Dominio | AB#214 | PR #34 | ✅ |
| HU1-CP11 | `UsuarioTest` | `rechazaContrasenaHashEnBlanco` | Dominio | AB#214 | PR #34 | ✅ |
| HU1-CP11 | `UsuarioTest` | `rechazaRolNulo` | Dominio | AB#214 | PR #34 | ✅ |
| HU1-CP11 | `UsuarioTest` | `bloqueaUsuario` | Dominio | AB#214 · AB#243 · AB#252 | PR #34 (renombrado en PR #36) (modificado en PR #180) (renombrado en PR #185) | ✅ |
| HU1-CP11 | `UsuarioTest` | `activaUsuarioBloqueado` | Dominio | AB#214 · AB#252 | PR #34 (renombrado en PR #36) (renombrado en PR #185) | ✅ |
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
| HU1-CP16 | `RegistroUsuarioServiceTest` | `rechazaDniYaRegistrado` | Aplicación | AB#229, AB#43 | PR #72, PR #100 | ✅ |
| HU1-CP16 | `RegistroUsuarioServiceTest` | `rechazaElDniDeOtroVendedor` | Aplicación | AB#43 | PR #100 | ✅ |
| HU1-CP16 | `UsuarioMongoAdapterTest` | `rechazaDniDuplicadoEnLaBaseDeDatos` | Infraestructura | AB#229 | PR #72 | ✅ |
| HU1-CP16 | `UsuarioMongoAdapterTest` | `existePorDniYRolDistingueLosDniGuardadosPorRol` (antes `existePorDniDistingueLosDniGuardados`) | Infraestructura | AB#229, AB#43 | PR #72, PR #100 | ✅ |
| HU1-CP16 | `UsuarioControllerTest` | `dniYaRegistradoDevuelve409ConCodigo` | Infraestructura | AB#229 | PR #72 | ✅ |
| HU1-CP17 | `PoliticaContrasenasTest` | `rechazaContrasenaSinMayuscula` | Dominio | AB#239 | PR #87 | ✅ |
| HU1-CP17 | `PoliticaContrasenasTest` | `rechazaContrasenaSinMinuscula` | Dominio | AB#239 | PR #87 | ✅ |
| HU1-CP17 | `PoliticaContrasenasTest` | `rechazaContrasenaSinNumero` | Dominio | AB#239 | PR #87 | ✅ |
| HU1-CP17 | `PoliticaContrasenasTest` | `rechazaContrasenaSinSimbolo` | Dominio | AB#239 | PR #87 | ✅ |
| HU1-CP17 | `PoliticaContrasenasTest` | `aceptaLetrasConTildeYEnie` | Dominio | AB#239 | PR #87 | ✅ |
| HU1-CP17 | `PoliticaContrasenasTest` | `aceptaElEspacioComoSimbolo` | Dominio | AB#239 | PR #87 | ✅ |
| HU1-CP17 | `RegistroUsuarioServiceTest` | `rechazaContrasenaSinSimboloYNoGuardaNada` | Aplicación | AB#239 | PR #87 | ✅ |
| HU1-CP18 | `EmailTest` | `guardaElEmailEnMinusculas` | Dominio | AB#240 | PR #89 | ✅ |
| HU1-CP18 | `EmailTest` | `quitaLosEspaciosDeLosExtremos` | Dominio | AB#240 | PR #89 | ✅ |
| HU1-CP18 | `EmailTest` | `dosEmailsQueSoloDifierenEnMayusculasSonIguales` | Dominio | AB#240 | PR #89 | ✅ |
| HU1-CP18 | `RegistroUsuarioServiceTest` | `rechazaUnCorreoYaRegistradoAunqueCambienLasMayusculas` | Aplicación | AB#240 | PR #89 | ✅ |
| HU1-CP18 | `RegistroUsuarioServiceTest` | `guardaElEmailNormalizado` | Aplicación | AB#240 | PR #89 | ✅ |
| HU1-CP19 | `DatosVendedorTest` | `creaDatosVendedorValidos` | Dominio | AB#241 | PR #98 | ✅ |
| HU1-CP19 | `DatosVendedorTest` | `permiteVendedorSinTelefono` | Dominio | AB#241 | PR #98 | ✅ |
| HU1-CP19 | `UsuarioTest` | `nuevoVendedorNaceEnEstadoPendienteConSusDatosComerciales` | Dominio | AB#241 | PR #98 | ✅ |
| HU1-CP19 | `RegistroUsuarioServiceTest` | `registraVendedorConTodosSusDatos` | Aplicación | AB#241 | PR #98 | ✅ |
| HU1-CP19 | `UsuarioDocumentMapperTest` | `convierteLosDatosDelVendedorEnDocumento` | Infraestructura | AB#241 | PR #98 | ✅ |
| HU1-CP19 | `UsuarioDocumentMapperTest` | `conservaLosDatosDelVendedorEnElMapeo` | Infraestructura | AB#241 | PR #98 | ✅ |
| HU1-CP19 | `UsuarioDocumentMapperTest` | `unVendedorSinNombreComercialSeLeeSinPerfil` | Infraestructura | AB#241 | PR #98 | ✅ |
| HU1-CP19 | `UsuarioControllerTest` | `registroDeVendedorValidoDevuelve201` | Infraestructura | AB#241 | PR #98 | ✅ |
| HU1-CP19 | `UsuarioControllerTest` | `elRegistroDelVendedorGuardaElAvatar` | Infraestructura | AB#229 · AB#241 | PR #104 | ✅ |
| HU1-CP20 | `RegistroUsuarioServiceTest` | `rechazaNombreComercialYaRegistradoYNoGuardaNada` | Aplicación | AB#241 | PR #98 | ✅ |
| HU1-CP20 | `RegistroUsuarioServiceTest` | `rechazaCorreoYNombreComercialYaRegistradosYNoGuardaNada` | Aplicación | AB#241 | PR #98 | ✅ |
| HU1-CP20 | `UsuarioMongoAdapterTest` | `existePorNombreComercialDistingueLosGuardados` | Infraestructura | AB#241 | PR #98 | ✅ |
| HU1-CP20 | `UsuarioMongoAdapterTest` | `rechazaNombreComercialDuplicadoEnLaBaseDeDatos` | Infraestructura | AB#241 | PR #98 | ✅ |
| HU1-CP20 | `UsuarioControllerTest` | `nombreComercialYaRegistradoDevuelve409ConCodigo` | Infraestructura | AB#241 | PR #98 | ✅ |
| HU1-CP20 | `UsuarioControllerTest` | `correoYNombreComercialYaRegistradosDevuelve409ConCodigo` | Infraestructura | AB#241 | PR #98 | ✅ |
| HU1-CP21 | `DatosVendedorTest` | `rechazaSinNombreComercial` | Dominio | AB#241 | PR #98 | ✅ |
| HU1-CP21 | `DatosVendedorTest` | `rechazaNombreComercialDe31Caracteres` | Dominio | AB#241 | PR #98 | ✅ |
| HU1-CP21 | `DatosVendedorTest` | `aceptaNombreComercialDe30Caracteres` | Dominio | AB#241 | PR #98 | ✅ |
| HU1-CP21 | `DatosVendedorTest` | `rechazaSinCategoriaPrincipal` | Dominio | AB#241 | PR #98 | ✅ |
| HU1-CP21 | `DatosVendedorTest` | `rechazaSinDni` | Dominio | AB#241 | PR #98 | ✅ |
| HU1-CP21 | `UsuarioTest` | `rechazaDatosDeVendedorSiNoEsVendedor` | Dominio | AB#241 | PR #98 | ✅ |
| HU1-CP21 | `UsuarioTest` | `nuevoVendedorExigeDatosDeVendedor` | Dominio | AB#241 | PR #98 | ✅ |
| HU1-CP21 | `UsuarioControllerTest` | `vendedorSinCategoriaDevuelve400` | Infraestructura | AB#241 | PR #98 | ✅ |
| HU1-CP22 | `PoliticaContrasenasTest` | `rechazoConDatosPersonalesIndicaSuMotivo` | Dominio | AB#229 | PR #104 | ✅ |
| HU1-CP22 | `PoliticaContrasenasTest` | `rechazoPorLongitudIndicaSuMotivo` | Dominio | AB#229 | PR #104 | ✅ |
| HU1-CP22 | `PoliticaContrasenasTest` | `rechazoPorComposicionIndicaSuMotivo` | Dominio | AB#229 | PR #104 | ✅ |
| HU1-CP22 | `PoliticaContrasenasTest` | `rechazoPorContrasenaComunIndicaSuMotivo` | Dominio | AB#229 | PR #104 | ✅ |
| HU1-CP22 | `DniTest` | `dniInvalidoIndicaSuMotivo` | Dominio | AB#229 | PR #104 | ✅ |
| HU1-CP22 | `TelefonoTest` | `telefonoInvalidoIndicaSuMotivo` | Dominio | AB#229 | PR #104 | ✅ |
| HU1-CP22 | `UsuarioTest` | `menorDeEdadIndicaSuMotivo` | Dominio | AB#229 | PR #104 | ✅ |
| HU1-CP22 | `RegistroUsuarioServiceTest` | `contrasenasDistintasIndicanSuMotivo` | Aplicación | AB#229 | PR #104 | ✅ |
| HU1-CP22 | `RegistroUsuarioServiceTest` | `privacidadNoAceptadaIndicaSuMotivo` | Aplicación | AB#229 | PR #104 | ✅ |
| HU1-CP22 | `UsuarioControllerTest` | `contrasenaConDatosPersonalesDevuelve400ConSuCodigo` | Infraestructura | AB#229 | PR #104 | ✅ |
| HU1-CP22 | `UsuarioControllerTest` | `dniInvalidoDevuelve400ConSuCodigo` | Infraestructura | AB#229 | PR #104 | ✅ |
| HU1-CP22 | `UsuarioControllerTest` | `emailInvalidoDevuelve400ConSuCodigo` | Infraestructura | AB#229 | PR #104 | ✅ |
| HU1-CP22 | `UsuarioControllerTest` | `datoInvalidoGenericoDevuelve400ConValidationError` | Infraestructura | AB#229 | PR #104 (renombrado y modificado en PR #183) | ✅ |
| HU1-CP22 | `UsuarioControllerTest` | `errorDeValidacionDelFormularioDevuelve400ConValidationError` | Infraestructura | AB#229 | PR #104 (renombrado y modificado en PR #183) | ✅ |
| HU1-CP23 | `registroErrors.test.js` | `HU1-CP23: traduce cada code a su campo y su mensaje` | Frontend · unitario | AB#17 · AB#18 | PR #105 | ✅ |
| HU1-CP23 | `registroErrors.test.js` | `HU1-CP23: un code sin campo o desconocido da el mensaje general` | Frontend · unitario | AB#17 · AB#18 | PR #105 (modificado en PR #183) | ✅ |
| HU1-CP23 | `registroErrors.test.js` | `HU1-CP23: un error que no es de los datos da un mensaje de error del servidor` | Frontend · unitario | AB#17 · AB#18 | PR #105 | ✅ |
| HU1-CP23 | `Register.test.jsx` | `HU1-CP23: una contraseña con datos personales se marca en el campo de la contraseña` | Frontend · página | AB#17 · AB#18 | PR #105 | ✅ |
| HU1-CP23 | `Register.test.jsx` | `HU1-CP23: un DNI ya registrado se marca en el DNI y no en el email` | Frontend · página | AB#17 · AB#18 | PR #105 | ✅ |
| HU1-CP23 | `Register.test.jsx` | `HU1-CP23: en el vendedor, un DNI ya registrado se marca en el DNI` | Frontend · página | AB#17 · AB#18 | PR #105 | ✅ |
| HU1-CP23 | `Register.test.jsx` | `HU1-CP23: un error sin campo muestra un mensaje general` | Frontend · página | AB#17 · AB#18 | PR #105 (modificado en PR #183) | ✅ |
| HU1-CP24 | `Register.test.jsx` | `HU1-CP24: tras registrarse ofrece ir a iniciar sesión` | Frontend · página | AB#234 | PR #105 | ✅ |
| HU1-CP25 | `Home.test.jsx` | `HU1-CP25: con la sesión iniciada no ofrece crear una cuenta` | Frontend · página | AB#17 · AB#18 | PR #105 | ✅ |
| HU1-CP25 | `GuestOnlyRoute.test.jsx` | `HU1-CP25: con la sesión iniciada /register lleva a la página de inicio del rol` | Frontend · rutas | AB#17 · AB#18 | PR #105 | ✅ |
| HU1-CP25 | `GuestOnlyRoute.test.jsx` | `HU1-CP25: sin sesión /register muestra el formulario de registro` | Frontend · rutas | AB#17 · AB#18 | PR #105 | ✅ |
| HU1-CP26 | `client.test.js` | `HU1-CP26: una respuesta 201 sin cuerpo devuelve null` | Frontend · API | AB#17 · AB#18 | PR #105 | ✅ |
| HU1-CP27 | `PasswordField.test.jsx` | `HU1-CP27: con bloquearPegado no deja pegar texto en el campo` | Frontend · componente | AB#242 | PR #125 | ✅ |
| HU1-CP27 | `PasswordField.test.jsx` | `HU1-CP27: con bloquearPegado tampoco deja copiar, cortar ni soltar texto arrastrado en el campo` | Frontend · componente | AB#242 | PR #125 | ✅ |
| HU1-CP27 | `PasswordField.test.jsx` | `HU1-CP27: con el password visible sigue sin dejar copiarlo` | Frontend · componente | AB#242 | PR #125 | ✅ |
| HU1-CP27 | `PasswordField.test.jsx` | `HU1-CP27: sin bloquearPegado ni bloquearCopia (el campo sin restricciones) se puede pegar` | Frontend · componente | AB#242 | PR #125 | ✅ |
| HU1-CP27 | `Register.test.jsx` | `HU1-CP27: en el registro de cliente no se puede pegar el password ni su repetición` | Frontend · página | AB#242 | PR #125 | ✅ |
| HU1-CP27 | `Register.test.jsx` | `HU1-CP27: en el registro de vendedor no se puede pegar el password ni su repetición` | Frontend · página | AB#242 | PR #125 | ✅ |
| HU1-CP27 | `Register.test.jsx` | `HU1-CP27: el password escrito a mano en los dos campos sigue permitiendo registrarse` | Frontend · página | AB#242 | PR #125 | ✅ |

---

## HU19 · Listar y buscar usuarios

### Tabla A · HU19 ↔ casos de prueba

| Caso | Descripción | Tipo | Tests | Estado |
|---|---|---|---|---|
| HU19-CP01 | El servicio delega la consulta intacta al repositorio | Unitario | 1 | ✅ Pasa |
| HU19-CP02 | El servicio devuelve la página proporcionada por el repositorio | Unitario | 1 | ✅ Pasa |
| HU19-CP03 | El adaptador busca por los campos visibles del usuario | Integración MongoDB | 1 | ✅ Pasa |
| HU19-CP04 | El adaptador aplica el filtro por rol | Integración MongoDB | 1 | ✅ Pasa |
| HU19-CP05 | El adaptador aplica el filtro por estado de bloqueo | Integración MongoDB | 1 | ✅ Pasa |
| HU19-CP06 | El adaptador devuelve la página solicitada y el total completo | Integración MongoDB | 1 | ✅ Pasa |
| HU19-CP07 | La búsqueda trata los caracteres especiales como texto literal | Integración MongoDB | 1 | ✅ Pasa |
| HU19-CP08 | Un administrador accede al listado HTTP sin exponer datos sensibles | Integración web | 1 | ✅ Pasa |
| HU19-CP09 | Conserva el estado PENDIENTE al mapear el usuario | Unitario | 1 | ✅ Pasa |
| HU19-CP10 | El adaptador filtra BLOQUEADO por el estado guardado, no por el bloqueo temporal por intentos | Integración MongoDB | 1 | ✅ Pasa |
| HU19-CP11 | El listado se ordena por nombre o por estado, en los dos sentidos | Integración (capa web y MongoDB) | 2 | ✅ Pasa |
| HU19-CP12 | El orden por nombre no distingue mayúsculas ni tildes | Integración MongoDB | 1 | ✅ Pasa |
| HU19-CP13 | El filtro de estado ofrece Pendiente, Activo y Bloqueado, sin Desactivado (los administradores no tienen Pendiente) | Frontend (componente y página) | 9 | ✅ Pasa (9, AB#247, PR #116) |
| HU19-CP14 | Cada fila de la tabla permite bloquear o desbloquear la cuenta con confirmación (una pendiente se acepta desbloqueándola) y la ficha ya no cambia el estado | Frontend (componente y página) | 32 | ✅ Pasa (32, AB#248, PR #116) |
| HU19-CP15 | Cada fila de la tabla permite editar y eliminar la cuenta (con confirmación), y la ficha es de solo lectura | Frontend (componente y página) | 44 | ✅ Pasa (44, AB#249, PR #116) |
| HU19-CP16 | El administrador en sesión y el último administrador activo no se pueden bloquear ni eliminar: los botones quedan deshabilitados y explican el motivo | Frontend (componente y página) | 20 | ✅ Pasa (20, AB#250, PR #116) |
| HU19-CP17 | En la tabla de vendedores, el filtro, la etiqueta y la ordenación del estado usan Bloqueado en lugar de Desactivado | Frontend (componente y página) | 4 | ✅ Pasa (4, AB#247, PR #116) |
| HU19-CP18 | Cada fila de la tabla de vendedores permite bloquear o desbloquear la cuenta con confirmación (nombre comercial en los botones y avisos), con los mismos endpoints que el resto de usuarios | Frontend (componente y página) | 8 | ✅ Pasa (8, AB#248, PR #116) |
| HU19-CP19 | Cada fila de la tabla de vendedores permite editar y eliminar la cuenta, y su ficha (datos, productos y pedidos) es de solo lectura | Frontend (componente y página) | 25 | ✅ Pasa (25, AB#249, PR #116) |

Notas:
- **HU19-CP13 a CP19 (Sprint 2, frontend):** CP17 a CP19 son los mismos comportamientos en la tabla de vendedores (HU26). Las cuentas ya no se desactivan, se bloquean (decisión del equipo), y sus acciones salen de la ficha a la fila de la tabla (ver, editar, bloquear o desbloquear y eliminar). El frontend espera el estado `BLOQUEADO` (ya no `DESACTIVADO`), `DELETE /api/admin/usuarios/{id}` y los `409` `CANNOT_BLOCK_SELF`, `CANNOT_DELETE_SELF` y `LAST_ACTIVE_ADMIN`: es un **contrato propuesto por el frontend, pendiente de implementar y validar en el backend**. Los tests de `frontend/src` de estas tareas no llevan el ID del caso en el nombre (estilo de esos archivos); se trazan aquí por su nombre.
- **Último administrador (HU19-CP16):** el frontend lo calcula con una consulta al listado (`role=ADMINISTRADOR&status=ACTIVO&size=1`); el backend debe imponerlo igualmente con un `409`.

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
| HU19-CP10 | `UsuarioMongoAdapterTest` | `listarFiltraBloqueadoPorEstadoGuardado` | Infraestructura | AB#61 · AB#243 | PR #59 (sustituido en PR #180) | ✅ |
| HU19-CP11 | `UsuarioControllerTest` | `elListadoDeUsuariosAceptaOrdenYSentido` | Infraestructura | AB#61 | PR #104 | ✅ |
| HU19-CP11 | `UsuarioMongoAdapterTest` | `ordenaClientesPorEstadoDescendente` | Infraestructura | AB#61 · AB#243 | PR #104 (modificado en PR #180) | ✅ |
| HU19-CP12 | `UsuarioMongoAdapterTest` | `ordenaPorNombreSinDistinguirMayusculasNiTildes` | Infraestructura | AB#29 | PR #104 | ✅ |
| HU19-CP13 | `usersConfig.test.js` | `statusLabelsForRole_deAdministrador_noIncluyePendiente` | Frontend | AB#247 | PR #116 | ✅ |
| HU19-CP13 | `usersConfig.test.js` | `statusLabelsForRole_ningunRol_ofreceDesactivado` | Frontend | AB#247 | PR #116 | ✅ |
| HU19-CP13 | `usersConfig.test.js` | `statusLabelsForRole_deCliente_ofrecePendienteActivoYBloqueado` | Frontend | AB#247 | PR #116 | ✅ |
| HU19-CP13 | `UsersFilters.test.jsx` | `HU19-CP04: avisa del estado elegido en el filtro de estado` | Frontend | AB#247 | PR #116 | ✅ |
| HU19-CP13 | `UsersFilters.test.jsx` | `usersFilters_conStatusLabels_soloOfreceEsosEstados` | Frontend | AB#247 | PR #116 | ✅ |
| HU19-CP13 | `RoleUsersList.test.jsx` | `roleUsersList_elFiltroDeEstadoOfrecePendienteYNoHayFiltroDeRol` | Frontend | AB#247 | PR #116 | ✅ |
| HU19-CP13 | `RoleUsersList.test.jsx` | `roleUsersList_alFiltrarPorBloqueado_muestraSoloEsasCuentas` | Frontend | AB#247 | PR #116 | ✅ |
| HU19-CP13 | `RoleUsersList.test.jsx` | `roleUsersList_deAdministradores_elFiltroDeEstadoNoOfrecePendiente` | Frontend | AB#247 | PR #116 | ✅ |
| HU19-CP13 | `AdminUsersPage.test.jsx` | `adminUsersPage_alFiltrarPorAdministrador_elFiltroDeEstadoNoOfrecePendiente` | Frontend | AB#247 | PR #116 | ✅ |
| HU19-CP14 | `UserStatusButton.test.jsx` | `userStatusButton_desbloquear_muestraUnBotonConElNombreDelUsuario` | Frontend | AB#248 | PR #116 | ✅ |
| HU19-CP14 | `UserStatusButton.test.jsx` | `userStatusButton_desbloquear_pideConfirmacionSinDesbloquearTodavia` | Frontend | AB#248 | PR #116 | ✅ |
| HU19-CP14 | `UserStatusButton.test.jsx` | `userStatusButton_desbloquear_alCancelar_noCambiaNada` | Frontend | AB#248 | PR #116 | ✅ |
| HU19-CP14 | `UserStatusButton.test.jsx` | `userStatusButton_desbloquear_alConfirmar_enviaActivoYAvisaConElNombre` | Frontend | AB#248 | PR #116 | ✅ |
| HU19-CP14 | `UserStatusButton.test.jsx` | `userStatusButton_desbloquear_siFalla_avisaDelError` | Frontend | AB#248 | PR #116 | ✅ |
| HU19-CP14 | `UserStatusButton.test.jsx` | `userStatusButton_bloquear_muestraUnBotonConElNombreDelUsuario` | Frontend | AB#248 | PR #116 | ✅ |
| HU19-CP14 | `UserStatusButton.test.jsx` | `userStatusButton_bloquear_pideConfirmacionAvisandoDeQueNoPodraIniciarSesion` | Frontend | AB#248 | PR #116 | ✅ |
| HU19-CP14 | `UserStatusButton.test.jsx` | `userStatusButton_bloquear_alConfirmar_enviaBloqueadoYAvisaConElNombre` | Frontend | AB#248 | PR #116 | ✅ |
| HU19-CP14 | `UserStatusButton.test.jsx` | `userStatusButton_bloquear_conConflicto_pasaElErrorConSuCodigo` | Frontend | AB#248 | PR #116 | ✅ |
| HU19-CP14 | `UserRowActions.test.jsx` | `userRowActions_deUnaCuentaActiva_ofreceVerYBloquear` | Frontend | AB#248 | PR #116 | ✅ |
| HU19-CP14 | `UserRowActions.test.jsx` | `userRowActions_deUnaCuentaBloqueada_ofreceDesbloquearYNoBloquear` | Frontend | AB#248 | PR #116 | ✅ |
| HU19-CP14 | `UserRowActions.test.jsx` | `userRowActions_deUnaCuentaPendiente_ofreceDesbloquearParaAceptarla` | Frontend | AB#248 | PR #116 | ✅ |
| HU19-CP14 | `UserRowActions.test.jsx` | `userRowActions_nuncaOfreceDesactivarNiActivar` | Frontend | AB#248 | PR #116 | ✅ |
| HU19-CP14 | `UserRowActions.test.jsx` | `userRowActions_alBloquear_avisaConElMensajeYActualizaElListado` | Frontend | AB#248 | PR #116 | ✅ |
| HU19-CP14 | `UserRowActions.test.jsx` | `userRowActions_alDesbloquear_avisaConElMensajeYActualizaElListado` | Frontend | AB#248 | PR #116 | ✅ |
| HU19-CP14 | `UserRowActions.test.jsx` | `userRowActions_alDesbloquearUnaCuentaPendiente_laAcepta` | Frontend | AB#248 | PR #116 | ✅ |
| HU19-CP14 | `UserRowActions.test.jsx` | `userRowActions_siEsElUltimoAdministradorActivo_muestraElMotivoYNoActualiza` | Frontend | AB#248 | PR #116 | ✅ |
| HU19-CP14 | `UserRowActions.test.jsx` | `userRowActions_siElBackendDiceQueEsLaPropiaCuenta_muestraElMotivo` | Frontend | AB#248 | PR #116 | ✅ |
| HU19-CP14 | `UserRowActions.test.jsx` | `userRowActions_siFallaElCambio_muestraUnErrorGenericoYNoActualiza` | Frontend | AB#248 | PR #116 | ✅ |
| HU19-CP14 | `UserDetailDialog.test.jsx` | `userDetailDialog_deUnaCuentaActiva_noOfreceBloquear` | Frontend | AB#248 | PR #116 | ✅ |
| HU19-CP14 | `UserDetailDialog.test.jsx` | `userDetailDialog_deUnaCuentaBloqueada_noOfreceDesbloquearNiActivar` | Frontend | AB#248 | PR #116 | ✅ |
| HU19-CP14 | `UserDetailDialog.test.jsx` | `userDetailDialog_deUnaCuentaPendiente_noOfreceActivar` | Frontend | AB#248 | PR #116 | ✅ |
| HU19-CP14 | `UsersTable.test.jsx` | `usersTable_conRenderActions_anadeLaColumnaAcciones` | Frontend | AB#248 | PR #116 | ✅ |
| HU19-CP14 | `users.test.js` | `updateUserStatus_paraBloquear_enviaBloqueado` | Frontend | AB#248 | PR #116 | ✅ |
| HU19-CP14 | `RoleUsersList.test.jsx` | `roleUsersList_cadaFilaTieneLupaYBotonDeBloqueo` | Frontend | AB#248 | PR #116 | ✅ |
| HU19-CP14 | `RoleUsersList.test.jsx` | `roleUsersList_alDesbloquearDesdeLaFila_actualizaElListadoYAvisa` | Frontend | AB#248 | PR #116 | ✅ |
| HU19-CP14 | `RoleUsersList.test.jsx` | `roleUsersList_alBloquearDesdeLaFila_actualizaElListadoYAvisa` | Frontend | AB#248 | PR #116 | ✅ |
| HU19-CP14 | `RoleUsersList.test.jsx` | `roleUsersList_siFallaElBloqueo_muestraElErrorYNoCambiaLaFila` | Frontend | AB#248 | PR #116 | ✅ |
| HU19-CP14 | `AdminUsersPage.test.jsx` | `adminUsersPage_laTablaTieneUnaColumnaAcciones_conLupaYBloqueoEnCadaFila` | Frontend | AB#248 | PR #116 | ✅ |
| HU19-CP14 | `AdminUsersPage.test.jsx` | `adminUsersPage_alBloquearDesdeLaTabla_laFilaPasaABloqueadoYAvisa` | Frontend | AB#248 | PR #116 | ✅ |
| HU19-CP14 | `AdminUsersPage.test.jsx` | `adminUsersPage_alDesbloquearUnaCuentaPendiente_laAcepta` | Frontend | AB#248 | PR #116 | ✅ |
| HU19-CP14 | `AdminAdminsPage.test.jsx` | `adminAdminsPage_alDesbloquearUnAdministradorDesdeLaTabla_elListadoLoMuestraActivo` | Frontend | AB#248 | PR #116 | ✅ |
| HU19-CP15 | `UserEditDialog.test.jsx` | `userEditDialog_alAbrirse_muestraElModoEdicionConGuardarYCancelar` | Frontend | AB#249 | PR #116 | ✅ |
| HU19-CP15 | `UserEditDialog.test.jsx` | `userEditDialog_deUnCliente_soloDejaModificarLoQueSePuede` | Frontend | AB#249 | PR #116 | ✅ |
| HU19-CP15 | `UserEditDialog.test.jsx` | `userEditDialog_deUnAdministrador_noDejaModificarEmailNiFechaDeIncorporacion` | Frontend | AB#249 | PR #116 | ✅ |
| HU19-CP15 | `UserEditDialog.test.jsx` | `userEditDialog_nuncaMuestraLaContrasena` | Frontend | AB#249 | PR #116 | ✅ |
| HU19-CP15 | `UserEditDialog.test.jsx` | `userEditDialog_alPulsarCerrar_avisaDelCierre` | Frontend | AB#249 | PR #116 | ✅ |
| HU19-CP15 | `UserEditDialog.test.jsx` | `userEditDialog_conErrorAlCargar_muestraMensajeYPermiteReintentar` | Frontend | AB#249 | PR #116 | ✅ |
| HU19-CP15 | `UserEditDialog.test.jsx` | `userEditDialog_alGuardarUnCliente_enviaSusDatosSinElEmailNiElEstado` | Frontend | AB#249 | PR #116 | ✅ |
| HU19-CP15 | `UserEditDialog.test.jsx` | `userEditDialog_tras_guardar_avisaUnaSolaVez` | Frontend | AB#249 | PR #116 | ✅ |
| HU19-CP15 | `UserEditDialog.test.jsx` | `userEditDialog_alCambiarElTipoDeCliente_loEnvia` | Frontend | AB#249 | PR #116 | ✅ |
| HU19-CP15 | `UserEditDialog.test.jsx` | `userEditDialog_alCambiarLaFechaDeNacimientoAOtraDeUnMayor_laGuarda` | Frontend | AB#249 | PR #116 | ✅ |
| HU19-CP15 | `UserEditDialog.test.jsx` | `userEditDialog_conUnaFechaDeNacimientoDeUnMenor_muestraElErrorYNoGuarda` | Frontend | AB#249 | PR #116 | ✅ |
| HU19-CP15 | `UserEditDialog.test.jsx` | `userEditDialog_conCamposObligatoriosVacios_losMuestraTodosALaVezYNoGuarda` | Frontend | AB#249 | PR #116 | ✅ |
| HU19-CP15 | `UserEditDialog.test.jsx` | `userEditDialog_alPulsarCancelar_cierraSinGuardarNada` | Frontend | AB#249 | PR #116 | ✅ |
| HU19-CP15 | `UserEditDialog.test.jsx` | `userEditDialog_conErrorDelServidor_muestraElMensajeYSigueEnEdicion` | Frontend | AB#249 | PR #116 | ✅ |
| HU19-CP15 | `UserEditDialog.test.jsx` | `userEditDialog_alEditarLaSedeDeUnAdministrador_laEnvia` | Frontend | AB#249 | PR #116 | ✅ |
| HU19-CP15 | `UserEditDialog.test.jsx` | `userEditDialog_laSedeDeUnAdministradorPuedeQuedarVacia` | Frontend | AB#249 | PR #116 | ✅ |
| HU19-CP15 | `UserEditDialog.test.jsx` | `userEditDialog_unAdministradorPuedeEditarSusPropiosDatos` | Frontend | AB#249 | PR #116 | ✅ |
| HU19-CP15 | `UserEditDialog.test.jsx` | `userEditDialog_elRolEsDeSoloLectura_enLaEdicionDe %s` | Frontend | AB#249 | PR #116 | ✅ |
| HU19-CP15 | `UserEditDialog.test.jsx` | `userEditDialog_alGuardar_noEnviaElRolNiElEmail` | Frontend | AB#249 | PR #116 | ✅ |
| HU19-CP15 | `UserRowActions.test.jsx` | `userRowActions_alPulsarLaLupa_abreLaFichaDeEseUsuario` | Frontend | AB#249 | PR #116 | ✅ |
| HU19-CP15 | `UserRowActions.test.jsx` | `userRowActions_alCerrarLaFicha_vuelveALaFila` | Frontend | AB#249 | PR #116 | ✅ |
| HU19-CP15 | `UserRowActions.test.jsx` | `userRowActions_ofreceEditarYEliminarAunqueLaCuentaEsteBloqueada` | Frontend | AB#249 | PR #116 | ✅ |
| HU19-CP15 | `UserRowActions.test.jsx` | `userRowActions_alPulsarEditar_abreElFormularioDeEdicionConSusDatos` | Frontend | AB#249 | PR #116 | ✅ |
| HU19-CP15 | `UserRowActions.test.jsx` | `userRowActions_alGuardarLaEdicion_avisaActualizaElListadoYCierraElFormulario` | Frontend | AB#249 | PR #116 | ✅ |
| HU19-CP15 | `UserRowActions.test.jsx` | `userRowActions_alCancelarLaEdicion_cierraElFormularioSinAvisar` | Frontend | AB#249 | PR #116 | ✅ |
| HU19-CP15 | `UserRowActions.test.jsx` | `userRowActions_alPulsarEliminar_pideConfirmacionAvisandoDeQueNoSePuedeDeshacer` | Frontend | AB#249 | PR #116 | ✅ |
| HU19-CP15 | `UserRowActions.test.jsx` | `userRowActions_alCancelarLaEliminacion_noBorraNada` | Frontend | AB#249 | PR #116 | ✅ |
| HU19-CP15 | `UserRowActions.test.jsx` | `userRowActions_alConfirmarLaEliminacion_borraAvisaYActualizaElListado` | Frontend | AB#249 | PR #116 | ✅ |
| HU19-CP15 | `UserRowActions.test.jsx` | `userRowActions_siEsElUltimoAdministradorActivo_muestraElMotivoDeLaEliminacion` | Frontend | AB#249 | PR #116 | ✅ |
| HU19-CP15 | `UserRowActions.test.jsx` | `userRowActions_siElBackendDiceQueEsLaPropiaCuenta_muestraElMotivoDeLaEliminacion` | Frontend | AB#249 | PR #116 | ✅ |
| HU19-CP15 | `UserRowActions.test.jsx` | `userRowActions_siFallaLaEliminacion_muestraUnErrorGenericoYNoActualiza` | Frontend | AB#249 | PR #116 | ✅ |
| HU19-CP15 | `UserDetailDialog.test.jsx` | `userDetailDialog_noOfreceEditar_laEdicionSeHaceDesdeLaTabla` | Frontend | AB#249 | PR #116 | ✅ |
| HU19-CP15 | `UserDetailDialog.test.jsx` | `userDetailDialog_soloOfreceCerrar` | Frontend | AB#249 | PR #116 | ✅ |
| HU19-CP15 | `UserDetailDialog.test.jsx` | `userDetailDialog_muestraElRolDeLaCuenta` | Frontend | AB#249 | PR #116 | ✅ |
| HU19-CP15 | `UserDetailDialog.test.jsx` | `userDetailDialog_deUnAdministrador_muestraSuSedeYSuRol` | Frontend | AB#249 | PR #116 | ✅ |
| HU19-CP15 | `users.test.js` | `deleteUser_envia_DELETE_ALaRutaDelUsuario_yDevuelveNullSinCuerpo` | Frontend | AB#249 | PR #116 | ✅ |
| HU19-CP15 | `users.test.js` | `deleteUser_codificaElIdEnLaRuta` | Frontend | AB#249 | PR #116 | ✅ |
| HU19-CP15 | `users.test.js` | `deleteUser_conConflicto_lanzaApiErrorConSuCodigo` | Frontend | AB#249 | PR #116 | ✅ |
| HU19-CP15 | `RoleUsersList.test.jsx` | `roleUsersList_alEliminarDesdeLaFila_laFilaDesapareceYAvisa` | Frontend | AB#249 | PR #116 | ✅ |
| HU19-CP15 | `AdminUsersPage.test.jsx` | `adminUsersPage_alEditarDesdeLaTabla_laFilaMuestraElNombreNuevo` | Frontend | AB#249 | PR #116 | ✅ |
| HU19-CP15 | `AdminUsersPage.test.jsx` | `adminUsersPage_alEliminarDesdeLaTabla_laFilaDesapareceYAvisa` | Frontend | AB#249 | PR #116 | ✅ |
| HU19-CP15 | `AdminUsersPage.test.jsx` | `adminUsersPage_alPulsarLaLupa_abreLaFichaSinAccionesDeEstado` | Frontend | AB#249 | PR #116 | ✅ |
| HU19-CP15 | `AdminClientsPage.test.jsx` | `adminClientsPage_alEditarUnClienteDesdeLaTabla_elListadoMuestraElNombreNuevo` | Frontend | AB#249 | PR #116 | ✅ |
| HU19-CP15 | `AdminClientsPage.test.jsx` | `adminClientsPage_laFichaNoTieneEditar` | Frontend | AB#249 | PR #116 | ✅ |
| HU19-CP16 | `UserStatusButton.test.jsx` | `userStatusButton_bloquear_deshabilitado_noSePuedePulsarYExplicaElMotivo` | Frontend | AB#250 | PR #116 | ✅ |
| HU19-CP16 | `UserRowActions.test.jsx` | `userRowActions_deLaPropiaCuenta_dejaElBloqueoDeshabilitadoYExplicaElMotivo` | Frontend | AB#250 | PR #116 | ✅ |
| HU19-CP16 | `UserRowActions.test.jsx` | `userRowActions_deLaPropiaCuenta_dejaEliminarDeshabilitadoYExplicaElMotivo` | Frontend | AB#250 | PR #116 | ✅ |
| HU19-CP16 | `UserRowActions.test.jsx` | `userRowActions_deLaPropiaCuenta_dejaVerYEditarDisponibles` | Frontend | AB#250 | PR #116 | ✅ |
| HU19-CP16 | `UserRowActions.test.jsx` | `userRowActions_delUltimoAdministradorActivo_dejaBloquearYEliminarDeshabilitadosConSuMotivo` | Frontend | AB#250 | PR #116 | ✅ |
| HU19-CP16 | `UserRowActions.test.jsx` | `userRowActions_delUltimoAdministradorActivo_dejaVerYEditarDisponibles` | Frontend | AB#250 | PR #116 | ✅ |
| HU19-CP16 | `UserRowActions.test.jsx` | `userRowActions_siElUltimoAdministradorEsOtro_dejaBloquearYEliminarDisponibles` | Frontend | AB#250 | PR #116 | ✅ |
| HU19-CP16 | `UserRowActions.test.jsx` | `userRowActions_sinDatosDelUltimoAdministrador_dejaBloquearYEliminarDisponibles` | Frontend | AB#250 | PR #116 | ✅ |
| HU19-CP16 | `UserRowActions.test.jsx` | `userRowActions_deUnaCuentaDeshabilitada_noAbreLaConfirmacionAlPulsar` | Frontend | AB#250 | PR #116 | ✅ |
| HU19-CP16 | `useLastActiveAdmin.test.jsx` | `useLastActiveAdmin_pideSoloLosAdministradoresActivos` | Frontend | AB#250 | PR #116 | ✅ |
| HU19-CP16 | `useLastActiveAdmin.test.jsx` | `useLastActiveAdmin_conVariosAdministradoresActivos_noHayUltimo` | Frontend | AB#250 | PR #116 | ✅ |
| HU19-CP16 | `useLastActiveAdmin.test.jsx` | `useLastActiveAdmin_conUnSoloAdministradorActivo_devuelveSuId` | Frontend | AB#250 | PR #116 | ✅ |
| HU19-CP16 | `useLastActiveAdmin.test.jsx` | `useLastActiveAdmin_deshabilitado_noPideNadaYNoHayUltimo` | Frontend | AB#250 | PR #116 | ✅ |
| HU19-CP16 | `useLastActiveAdmin.test.jsx` | `useLastActiveAdmin_alRefrescar_vuelveAPedirlo` | Frontend | AB#250 | PR #116 | ✅ |
| HU19-CP16 | `RoleUsersList.test.jsx` | `roleUsersList_conElUsuarioEnSesion_suFilaNoPermiteBloquearse` | Frontend | AB#250 | PR #116 | ✅ |
| HU19-CP16 | `RoleUsersList.test.jsx` | `roleUsersList_deClientes_noPideCuantosAdministradoresActivosQuedan` | Frontend | AB#250 | PR #116 | ✅ |
| HU19-CP16 | `AdminAdminsPage.test.jsx` | `adminAdminsPage_laFilaDelAdministradorEnSesion_noPermiteBloquearse` | Frontend | AB#250 | PR #116 | ✅ |
| HU19-CP16 | `AdminAdminsPage.test.jsx` | `adminAdminsPage_siSoloQuedaUnAdministradorActivo_noSePuedeBloquearNiEliminar` | Frontend | AB#250 | PR #116 | ✅ |
| HU19-CP16 | `AdminUsersPage.test.jsx` | `adminUsersPage_laFilaDelAdministradorEnSesion_noPermiteBloquearse` | Frontend | AB#250 | PR #116 | ✅ |
| HU19-CP16 | `AdminUsersPage.test.jsx` | `adminUsersPage_siSoloQuedaUnAdministradorActivo_nadieMasPuedeBloquearloNiEliminarlo` | Frontend | AB#250 | PR #116 | ✅ |
| HU19-CP17 | `AdminSellersPage.test.jsx` | `adminSellersPage_distingueLosEstadosPendienteYBloqueado` | Frontend | AB#247 | PR #116 | ✅ |
| HU19-CP17 | `AdminSellersPage.test.jsx` | `adminSellersPage_elFiltroDeEstado_ofreceTodosLosEstados` | Frontend | AB#247 | PR #116 | ✅ |
| HU19-CP17 | `AdminSellersPage.test.jsx` | `adminSellersPage_alInvertirElOrdenDeEstado_losPendientesPasanAlFinal` | Frontend | AB#247 | PR #116 | ✅ |
| HU19-CP17 | `AdminSellersPage.test.jsx` | `adminSellersPage_sinCoincidencias_ofreceLimpiarLosFiltros` | Frontend | AB#247 | PR #116 | ✅ |
| HU19-CP18 | `AdminSellersPage.test.jsx` | `adminSellersPage_laTablaTieneUnaColumnaAcciones` | Frontend | AB#248 | PR #116 | ✅ |
| HU19-CP18 | `AdminSellersPage.test.jsx` | `adminSellersPage_cadaFilaTieneVerEditarBloquearOEliminar` | Frontend | AB#248 | PR #116 | ✅ |
| HU19-CP18 | `AdminSellersPage.test.jsx` | `adminSellersPage_alBloquearUnVendedor_laFilaPasaABloqueadoYAvisa` | Frontend | AB#248 | PR #116 | ✅ |
| HU19-CP18 | `AdminSellersPage.test.jsx` | `adminSellersPage_alDesbloquearUnVendedorPendiente_loAcepta` | Frontend | AB#248 | PR #116 | ✅ |
| HU19-CP18 | `AdminSellersPage.test.jsx` | `adminSellersPage_siFallaElBloqueo_muestraElErrorYNoCambiaLaFila` | Frontend | AB#248 | PR #116 | ✅ |
| HU19-CP18 | `UserRowActions.test.jsx` | `userRowActions_conUnNombreIndicado_losBotonesYLosAvisosLoUsan` | Frontend | AB#248 | PR #116 | ✅ |
| HU19-CP18 | `UserRowActions.test.jsx` | `userRowActions_alBloquearUnVendedor_usaElMismoEndpointDeEstadoQueElRestoDeUsuarios` | Frontend | AB#248 | PR #116 | ✅ |
| HU19-CP18 | `UserRowActions.test.jsx` | `userRowActions_alDesbloquearUnVendedorPendiente_loAcepta` | Frontend | AB#248 | PR #116 | ✅ |
| HU19-CP19 | `AdminSellersPage.test.jsx` | `adminSellersPage_alEliminarUnVendedor_laFilaDesapareceYAvisa` | Frontend | AB#249 | PR #116 | ✅ |
| HU19-CP19 | `AdminSellersPage.test.jsx` | `adminSellersPage_alEditarUnVendedorDesdeLaTabla_elListadoMuestraSuNombreComercialNuevo` | Frontend | AB#249 | PR #116 | ✅ |
| HU19-CP19 | `AdminSellersPage.test.jsx` | `adminSellersPage_laFichaDelVendedorNoTieneEditarNiBloquear` | Frontend | AB#249 | PR #116 | ✅ |
| HU19-CP19 | `UserRowActions.test.jsx` | `userRowActions_conRenderDetail_laLupaAbreEseDialogoYSePuedeCerrar` | Frontend | AB#249 | PR #116 | ✅ |
| HU19-CP19 | `UserRowActions.test.jsx` | `userRowActions_alEliminarUnVendedor_usaElMismoEndpointQueElRestoDeUsuarios` | Frontend | AB#249 | PR #116 | ✅ |
| HU19-CP19 | `UserRowActions.test.jsx` | `userRowActions_alEditarUnVendedor_abreElFormularioConSusDatos` | Frontend | AB#249 | PR #116 | ✅ |
| HU19-CP19 | `UserEditDialog.test.jsx` | `userEditDialog_deUnVendedor_muestraSusDatosParaEditar` | Frontend | AB#249 | PR #116 | ✅ |
| HU19-CP19 | `UserEditDialog.test.jsx` | `userEditDialog_deUnVendedor_dejaModificarTodoMenosElEmailYElEstado` | Frontend | AB#249 | PR #116 | ✅ |
| HU19-CP19 | `UserEditDialog.test.jsx` | `userEditDialog_deUnVendedor_alGuardarEnviaSoloLosDatosDelVendedor` | Frontend | AB#249 | PR #116 | ✅ |
| HU19-CP19 | `UserEditDialog.test.jsx` | `userEditDialog_deUnVendedor_tras_guardar_avisaUnaSolaVez` | Frontend | AB#249 | PR #116 | ✅ |
| HU19-CP19 | `UserEditDialog.test.jsx` | `userEditDialog_deUnVendedor_alCambiarLaCategoriaPrincipal_laEnvia` | Frontend | AB#249 | PR #116 | ✅ |
| HU19-CP19 | `UserEditDialog.test.jsx` | `userEditDialog_conElNombreComercialDeOtroVendedor_muestraElErrorEnElCampoYSigueEnEdicion` | Frontend | AB#249 | PR #116 | ✅ |
| HU19-CP19 | `UserEditDialog.test.jsx` | `userEditDialog_alGuardarSinCambiarElNombreComercial_noLoTomaPorRepetido` | Frontend | AB#249 | PR #116 | ✅ |
| HU19-CP19 | `UserEditDialog.test.jsx` | `userEditDialog_deUnVendedor_conCamposVaciosOIncorrectos_losMuestraTodosALaVezYNoGuarda` | Frontend | AB#249 | PR #116 | ✅ |
| HU19-CP19 | `UserEditDialog.test.jsx` | `userEditDialog_deUnVendedor_alPulsarCancelar_cierraSinGuardarNada` | Frontend | AB#249 | PR #116 | ✅ |
| HU19-CP19 | `UserEditDialog.test.jsx` | `userEditDialog_deUnVendedor_conErrorDelServidor_muestraElMensajeYSigueEnEdicion` | Frontend | AB#249 | PR #116 | ✅ |
| HU19-CP19 | `UserEditDialog.test.jsx` | `userEditDialog_deUnVendedor_siNoCarganLasCategorias_avisaYPermiteReintentar` | Frontend | AB#249 | PR #116 | ✅ |
| HU19-CP19 | `UserEditDialog.test.jsx` | `userEditDialog_deUnVendedorPendiente_tambienSePuedeEditar` | Frontend | AB#249 | PR #116 | ✅ |
| HU19-CP19 | `UserEditDialog.test.jsx` | `userEditDialog_deUnVendedor_elRolEsDeSoloLecturaYNoSeEnvia` | Frontend | AB#249 | PR #116 | ✅ |
| HU19-CP19 | `SellerDetailDialog.test.jsx` | `sellerDetailDialog_enLaPestanaDatos_noOfreceEditarNiCambiarElEstado` | Frontend | AB#249 | PR #116 | ✅ |
| HU19-CP19 | `SellerDetailDialog.test.jsx` | `sellerDetailDialog_conVendedorBloqueado_tampocoOfreceDesbloquear` | Frontend | AB#249 | PR #116 | ✅ |
| HU19-CP19 | `SellerDetailDialog.test.jsx` | `sellerDetailDialog_conVendedorPendiente_tampocoOfreceActivar` | Frontend | AB#249 | PR #116 | ✅ |
| HU19-CP19 | `SellerDetailDialog.test.jsx` | `sellerDetailDialog_elAvisoRemiteALosBotonesDeLaTabla` | Frontend | AB#249 | PR #116 | ✅ |
| HU19-CP19 | `SellerDetailDialog.test.jsx` | `sellerDetailDialog_enLasPestanasDeProductosYPedidos_noOfreceEditar` | Frontend | AB#249 | PR #116 | ✅ |
| HU19-CP19 | `SellerDetailDialog.test.jsx` | `sellerDetailDialog_muestraElRolVendedor` | Frontend | AB#249 | PR #116 | ✅ |

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
| HU2-CP04 | Rechaza el acceso a una cuenta desactivada | Unitario + integración | 3 | ✅ Pasa |
| HU2-CP05 | Al validar las credenciales, envía al correo del usuario un código de verificación | Unitario + integración | 4 | ✅ Pasa |
| HU2-CP06 | Un código correcto pasa al paso de Microsoft Authenticator | Unitario + integración | 3 | ✅ Pasa |
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
| HU2-CP17 | Sin haber superado el paso 2 no se puede usar el paso 3 | Unitario + integración | 3 | ✅ Pasa |
| HU2-CP18 | Un código de Authenticator correcto abre la sesión con el rol del usuario | Unitario + integración | 4 | ✅ Pasa |
| HU2-CP19 | Rechaza un código de Authenticator incorrecto y cuenta el fallo para el bloqueo | Unitario + integración | 4 | ✅ Pasa |
| HU2-CP20 | Un código de Authenticator ya usado no se puede volver a usar | Unitario + integración | 4 | ✅ Pasa |
| HU2-CP21 | Acepta el código del periodo anterior o siguiente (±30 s) | Unitario | 1 | ✅ Pasa |
| HU2-CP22 | Un usuario sin Authenticator pasa al alta y recibe un QR | Unitario + integración | 13 | ✅ Pasa |
| HU2-CP23 | El primer código correcto confirma el alta | Unitario + integración | 7 | ✅ Pasa |
| HU2-CP24 | El secreto se guarda cifrado, nunca en claro | Unitario + integración | 7 | ✅ Pasa |
| HU2-CP25 | El login no distingue mayúsculas ni espacios en el email, y un email mal formado responde como unas credenciales inválidas | Unitario | 2 | ✅ Pasa |
| HU2-CP26 | El contador de intentos fallidos se reinicia al completar el login y al caducar el bloqueo, no al acertar solo la contraseña | Unitario | 4 | ✅ Pasa |
| HU2-CP27 | El campo de la contraseña del login tiene un botón para mostrarla y ocultarla | Frontend (página) | 1 | ✅ Pasa (1, AB#19, PR #105) |
| HU2-CP28 | Una cuenta bloqueada por un administrador muestra su mensaje en el paso 1, antes del código del correo, y no avanza al 2FA | Frontend (página) + unitario + integración | 4 | ✅ Pasa (2, AB#246, PR #114) |
| HU2-CP29 | El registro y los pasos del login se pueden usar sin sesión; consultar el usuario en sesión exige estar autenticado y que el correo sea el de la sesión; el id del usuario y el perfil son del cliente y del vendedor, y saber si es premium, solo del cliente; sin sesión se rechaza. Seguridad en la capa de negocio (R8) | Unitario con Spring Security (sin MongoDB) | 7 | 🔍 En revisión (7, rama `fix/seguridad-capa-negocio`) |
| HU2-CP30 | `PasswordField` con `bloquearCopia` no deja copiar ni cortar la contraseña (tampoco con el ojo activado) y sí pegar, soltar y escribir; sin ninguna propiedad se puede copiar | Frontend (componente) | 5 | 🔍 En revisión (5, AB#242, PR pendiente) |
| HU2-CP31 | En el login la contraseña no se puede copiar ni cortar, tampoco con el ojo activado, pero sí pegar, y el login sigue funcionando pegando o escribiendo a mano | Frontend (página) | 4 | 🔍 En revisión (4, AB#242, PR pendiente) |

Notas:
- **HU2-CP25 y CP26 (fallos de la demo del Sprint 1):** el paso 1 busca el email normalizado (`Email`, T240) y responde «Credenciales inválidas» si está mal formado. El contador de intentos se reinicia al caducar el bloqueo (`Usuario.registrarIntentoFallido`) y al completar el tercer paso (`AuthenticatorService.verificarCodigo`), no al acertar la contraseña: así quien solo tenga la contraseña no puede probar códigos sin límite.
- **HU2-CP01 a CP04:** casos definidos en la T19/T22 a partir del contrato del frontend (`frontend/src/mocks/authHandlers.js`). Pendientes de añadir a la descripción de la T22 en Azure Boards.
- **HU2-CP04:** «desactivada» es la cuenta desactivada por un administrador (`desactivar()` de `Usuario`), distinta del bloqueo temporal de CP03.
- **Persistencia (T225, AB#225):** `UsuarioDocument` guarda los intentos fallidos, el bloqueo y la desactivación, así que CP03 y CP04 se cumplen también en la aplicación real. Los tests de integración de `UsuarioMongoAdapterTest` lo comprueban contra MongoDB.
- **AB#210:** el PR #43 guarda esos campos con los nombres del modelo de BBDD (`estado` ACTIVO/DESACTIVADO, `failedLoginAttempts`, `lockedUntil`) y amplía `conservaElBloqueo` y `conservaLosIntentosFallidosYElBloqueo` para comprobar esos nombres en el documento guardado.
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
- **HU2-CP28 (AB#245, Sprint 2):** las cuentas ya no se desactivan, se bloquean (decisión del equipo). El frontend deja de esperar `ACCOUNT_DISABLED` y espera `403` con `ACCOUNT_BLOCKED` (estado `BLOQUEADO`); solo cambia el login. El backend lo completa en la T246 (AB#246): el `403` de `CuentaDesactivadaException` responde `ACCOUNT_BLOCKED` y un test fija que la cuenta se rechaza sin generar ni enviar el código del correo. El test del controlador que esperaba `ACCOUNT_DISABLED` (antes en CP04) pasa a CP28. El estado `DESACTIVADO` y la excepción no se renombran todavía, así que CP04 sigue cubriendo el rechazo y la persistencia de ese estado.
- **Frontend (T20):** los tests de `LoginFlow.test.jsx` no llevan el ID del caso en el nombre (estilo del archivo, anterior a la convención de la HU19); se trazan aquí por su nombre.
- **HU2-CP29 (R8, seguridad en la capa de negocio):** caso propuesto en la rama `fix/seguridad-capa-negocio`, sin task ni `AB#` por ser un fix transversal. Pendiente de confirmar con el equipo. Cubre con `@PreAuthorize` los servicios `AutenticacionService`, `AuthenticatorService`, `RegistroUsuarioService` y `GestionPerfilService`, que sirven también a las HU1 y HU4; se traza aquí por ser `AutenticacionService` el centro del ciclo. Los pasos del login y el registro son `permitAll()` explícito: el correo del login a medias vive en la sesión HTTP y no en `authentication.name`, así que la propiedad no se puede comprobar con `@PreAuthorize` sin rediseñar el login. Clase de test nueva `usuarios/application/service/SeguridadSesionUsuarioServiceTest` (contexto mínimo de Spring con `@EnableMethodSecurity`, sin MongoDB).
- **HU2-CP30 y CP31 (R9 de la revisión del Sprint 1, "no se puede copiar la contraseña", AB#242):** casos propuestos. `PasswordField` gana la propiedad `bloquearCopia` (cancela copiar y cortar, también con el ojo activado) y el login la usa: la contraseña no se copia, pero sí se pega, para no romper los gestores de contraseñas. `bloquearPegado` (registro, recuperación y alta de administrador) no cambia. Pendientes de confirmar con el equipo y de añadir a la HU2 en Azure Boards. Rama `fix/US1-T242-bloquear-copia-contrasena-login`.

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
| HU2-CP04 | `AutenticacionServiceTest` | `rechazaCuentaBloqueada` | Aplicación | AB#22 · AB#252 | PR #36 (renombrado en PR #185) | ✅ |
| HU2-CP04 | `UsuarioMongoAdapterTest` | `conservaElBloqueo` | Infraestructura | AB#225 · AB#210 · AB#243 · AB#252 | PR #46 (ampliado en PR #43) (modificado en PR #180) (renombrado en PR #185) | ✅ |
| HU2-CP04 | `UsuarioMongoAdapterTest` | `conservaUnUsuarioActivo` | Infraestructura | AB#225 | PR #46 | ✅ |
| HU2-CP05 | `AutenticacionServiceTest` | `enviaElCodigoAlCorreoTrasValidarLasCredenciales` | Aplicación | AB#22 | PR #52 | ✅ |
| HU2-CP05 | `SmtpEnvioEmailAdapterTest` | `enviaElCodigoAlCorreoDelUsuario` | Infraestructura | AB#231 | PR #54 | ✅ |
| HU2-CP05 | `SmtpEnvioEmailAdapterTest` | `fallaSinRevelarElCodigo` | Infraestructura | AB#231 | PR #54 | ✅ |
| HU2-CP06 | `CodigoVerificacionTest` | `codigoNuevoSePuedeUsar` | Dominio | AB#22 | PR #52 | ✅ |
| HU2-CP06 | `AutenticacionServiceTest` | `codigoCorrectoPasaAlAuthenticator` | Aplicación | AB#22 · AB#232 · AB#20 | PR #52 (ampliado en PR #56 y PR #75) | ✅ |
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
| HU2-CP28 | `AutenticacionControllerTest` | `cuentaBloqueadaPorAdministradorDevuelve403ConCodigo` | Infraestructura | AB#230 · AB#246 | PR #67 (cambiado en PR #114) | ✅ |
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
| HU2-CP17 | `AuthenticatorServiceTest` | `sinFactorSeRechaza` | Aplicación | AB#20 | PR #75 | ✅ |
| HU2-CP17 | `AutenticacionControllerTest` | `codigoAuthenticatorSinPasoDosDevuelve401` | Infraestructura | AB#20 | PR #75 | ✅ |
| HU2-CP17 | `AutenticacionControllerTest` | `altaSinPasoDosDevuelve401` | Infraestructura | AB#20 | PR #75 | ✅ |
| HU2-CP18 | `JavaTotpAdapterTest` | `aceptaElCodigoDelRfcEnSuPeriodo` | Infraestructura | AB#20 | PR #75 | ✅ |
| HU2-CP18 | `AuthenticatorServiceTest` | `codigoCorrectoDevuelveElUsuarioYRegistraElUso` | Aplicación | AB#20 | PR #75 | ✅ |
| HU2-CP18 | `AutenticacionControllerTest` | `codigoAuthenticatorCorrectoAbreLaSesion` | Infraestructura | AB#20 | PR #75 | ✅ |
| HU2-CP18 | `AutenticacionControllerTest` | `trasElPasoTresMeDevuelveElUsuario` | Infraestructura | AB#20 | PR #75 | ✅ |
| HU2-CP19 | `JavaTotpAdapterTest` | `rechazaUnCodigoIncorrecto` | Infraestructura | AB#20 | PR #75 | ✅ |
| HU2-CP19 | `AuthenticatorServiceTest` | `codigoIncorrectoCuentaUnFalloParaElBloqueo` | Aplicación | AB#20 | PR #75 | ✅ |
| HU2-CP19 | `AuthenticatorServiceTest` | `cuentaBloqueadaSeRechazaSinComprobarElCodigo` | Aplicación | AB#20 | PR #75 | ✅ |
| HU2-CP19 | `LoginFlow.test.jsx` | `login_sinAuthenticator_conUnPrimerCodigoIncorrecto_muestraErrorYSigueEnElAlta` | Frontend | AB#20 | PR #75 | ✅ |
| HU2-CP20 | `FactorAutenticacionTest` | `noSePuedeReutilizarUnPeriodo` | Dominio | AB#20 | PR #75 | ✅ |
| HU2-CP20 | `FactorAutenticacionTest` | `unFactorSinUsosAceptaCualquierPeriodo` | Dominio | AB#20 | PR #75 | ✅ |
| HU2-CP20 | `FactorAutenticacionMongoAdapterTest` | `guardarUnFactorLeidoLoActualiza` | Infraestructura | AB#20 | PR #75 | ✅ |
| HU2-CP20 | `AuthenticatorServiceTest` | `codigoReutilizadoSeRechaza` | Aplicación | AB#20 | PR #75 | ✅ |
| HU2-CP21 | `JavaTotpAdapterTest` | `aceptaUnPeriodoDeDesfaseYNoMas` | Infraestructura | AB#20 | PR #75 | ✅ |
| HU2-CP22 | `JavaTotpAdapterTest` | `generaUnSecretoAleatorioEnBase32` | Infraestructura | AB#20 | PR #75 | ✅ |
| HU2-CP22 | `JavaTotpAdapterTest` | `generaElQrComoImagen` | Infraestructura | AB#20 | PR #75 | ✅ |
| HU2-CP22 | `FactorAutenticacionMongoAdapterTest` | `buscarPorUsuarioDevuelveVacioSinFactor` | Infraestructura | AB#20 | PR #75 | ✅ |
| HU2-CP22 | `FactorAutenticacionMongoAdapterTest` | `unAltaNuevaSustituyeAlFactorAnterior` | Infraestructura | AB#20 | PR #75 | ✅ |
| HU2-CP22 | `AuthenticatorServiceTest` | `prepararAltaGuardaElSecretoCifradoYDevuelveElQr` | Aplicación | AB#20 | PR #75 | ✅ |
| HU2-CP22 | `AuthenticatorServiceTest` | `prepararAltaDeUnCorreoInexistenteSeRechaza` | Aplicación | AB#20 | PR #75 | ✅ |
| HU2-CP22 | `AuthenticatorServiceTest` | `prepararAltaConAuthenticatorConfirmadoSeRechaza` | Aplicación | AB#20 | PR #75 | ✅ |
| HU2-CP22 | `AutenticacionServiceTest` | `sinAuthenticatorPasaAlAlta` | Aplicación | AB#20 | PR #75 | ✅ |
| HU2-CP22 | `AutenticacionServiceTest` | `conAltaSinConfirmarVuelveAlAlta` | Aplicación | AB#20 | PR #75 | ✅ |
| HU2-CP22 | `AutenticacionControllerTest` | `altaTrasPasoDosDevuelveElQr` | Infraestructura | AB#20 | PR #75 | ✅ |
| HU2-CP22 | `AutenticacionControllerTest` | `altaDeQuienYaTieneAuthenticatorDevuelve403` | Infraestructura | AB#20 | PR #75 | ✅ |
| HU2-CP22 | `LoginFlow.test.jsx` | `login_sinAuthenticator_trasElCodigoDeCorreo_muestraElQrDelAlta` | Frontend | AB#20 | PR #75 | ✅ |
| HU2-CP22 | `LoginFlow.test.jsx` | `login_conAuthenticatorYaDadoDeAlta_noMuestraElQr` | Frontend | AB#20 | PR #75 | ✅ |
| HU2-CP23 | `FactorAutenticacionTest` | `unFactorNuevoNoEstaConfirmado` | Dominio | AB#20 | PR #75 | ✅ |
| HU2-CP23 | `FactorAutenticacionTest` | `elPrimerUsoConfirmaElAlta` | Dominio | AB#20 | PR #75 | ✅ |
| HU2-CP23 | `FactorAutenticacionTest` | `losUsosSiguientesNoCambianLaFechaDelAlta` | Dominio | AB#20 | PR #75 | ✅ |
| HU2-CP23 | `FactorAutenticacionDocumentMapperTest` | `convierteDocumentoEnFactor` | Infraestructura | AB#20 | PR #75 | ✅ |
| HU2-CP23 | `FactorAutenticacionMongoAdapterTest` | `buscarPorUsuarioDevuelveElFactorGuardado` | Infraestructura | AB#20 | PR #75 | ✅ |
| HU2-CP23 | `AuthenticatorServiceTest` | `primerCodigoCorrectoConfirmaElAlta` | Aplicación | AB#20 | PR #75 | ✅ |
| HU2-CP23 | `LoginFlow.test.jsx` | `login_sinAuthenticator_conElPrimerCodigo_confirmaElAltaYEntra` | Frontend | AB#20 | PR #75 | ✅ |
| HU2-CP24 | `AesGcmCifradoSecretoAdapterTest` | `descifrarDevuelveElSecretoOriginal` | Infraestructura | AB#20 | PR #75 | ✅ |
| HU2-CP24 | `AesGcmCifradoSecretoAdapterTest` | `elCifradoNoContieneElSecreto` | Infraestructura | AB#20 | PR #75 | ✅ |
| HU2-CP24 | `AesGcmCifradoSecretoAdapterTest` | `cadaCifradoEsDistinto` | Infraestructura | AB#20 | PR #75 | ✅ |
| HU2-CP24 | `AesGcmCifradoSecretoAdapterTest` | `unCifradoManipuladoSeRechaza` | Infraestructura | AB#20 | PR #75 | ✅ |
| HU2-CP24 | `FactorAutenticacionDocumentMapperTest` | `convierteFactorEnDocumento` | Infraestructura | AB#20 | PR #75 | ✅ |
| HU2-CP24 | `FactorAutenticacionMongoAdapterTest` | `guardaElFactorEnFactoresAutenticacion` | Infraestructura | AB#20 | PR #75 | ✅ |
| HU2-CP24 | `AuthenticatorServiceTest` | `prepararAltaNoGuardaElSecretoEnClaro` | Aplicación | AB#20 | PR #75 | ✅ |
| HU2-CP25 | `AutenticacionServiceTest` | `iniciaSesionAunqueElEmailLleveMayusculas` | Aplicación | AB#240 | PR #104 | ✅ |
| HU2-CP25 | `AutenticacionServiceTest` | `unEmailMalFormadoEnElLoginNoRevelaNada` | Aplicación | AB#240 | PR #104 | ✅ |
| HU2-CP26 | `UsuarioTest` | `trasCaducarElBloqueoElContadorEmpiezaDeCero` | Dominio | AB#19 | PR #104 | ✅ |
| HU2-CP26 | `UsuarioTest` | `reiniciarIntentosQuitaElContadorYElBloqueo` | Dominio | AB#19 | PR #104 | ✅ |
| HU2-CP26 | `AuthenticatorServiceTest` | `completarElTercerPasoReiniciaElContador` | Aplicación | AB#19 | PR #104 | ✅ |
| HU2-CP26 | `AutenticacionServiceTest` | `acertarLaContrasenaNoReiniciaElContador` | Aplicación | AB#19 | PR #104 | ✅ |
| HU2-CP27 | `LoginFlow.test.jsx` | `HU2-CP27: el campo de la contraseña tiene un botón para mostrarla y ocultarla` | Frontend · página | AB#19 | PR #105 | ✅ |
| HU2-CP28 | `LoginFlow.test.jsx` | `HU2-CP28: una cuenta bloqueada muestra su mensaje antes del código del correo y no avanza al 2FA` | Frontend · página | AB#245 | PR #108 | ✅ |
| HU2-CP28 | `LoginFlow.test.jsx` | `HU2-CP28: el mensaje de cuenta bloqueada no es el de demasiados intentos ni el genérico` | Frontend · página | AB#245 | PR #108 | ✅ |
| HU2-CP28 | `AutenticacionServiceTest` | `cuentaBloqueadaPorAdministradorNoRecibeCodigo` | Aplicación | AB#246 | PR #114 | ✅ |
| HU2-CP29 | `SeguridadSesionUsuarioServiceTest` | `elRegistroYElLoginSonPublicos` | Aplicación | — | Rama `fix/seguridad-capa-negocio` | 🔍 |
| HU2-CP29 | `SeguridadSesionUsuarioServiceTest` | `nadieConsultaConElCorreoDeOtro` | Aplicación | — | Rama `fix/seguridad-capa-negocio` | 🔍 |
| HU2-CP29 | `SeguridadSesionUsuarioServiceTest` | `unAdministradorSoloConsultaSuUsuarioEnSesion` | Aplicación | — | Rama `fix/seguridad-capa-negocio` | 🔍 |
| HU2-CP29 | `SeguridadSesionUsuarioServiceTest` | `unVendedorNoConsultaSiEsPremium` | Aplicación | — | Rama `fix/seguridad-capa-negocio` | 🔍 |
| HU2-CP29 | `SeguridadSesionUsuarioServiceTest` | `unClienteConsultaLoSuyo` | Aplicación | — | Rama `fix/seguridad-capa-negocio` | 🔍 |
| HU2-CP29 | `SeguridadSesionUsuarioServiceTest` | `unAnonimoNoConsultaNada` | Aplicación | — | Rama `fix/seguridad-capa-negocio` | 🔍 |
| HU2-CP29 | `SeguridadSesionUsuarioServiceTest` | `sinSesionSeRechazanLasConsultas` | Aplicación | — | Rama `fix/seguridad-capa-negocio` | 🔍 |
| HU2-CP30 | `PasswordField.test.jsx` | `HU2-CP30: con bloquearCopia no deja copiar ni cortar la contraseña` | Frontend · componente | AB#242 | PR pendiente | 🔍 |
| HU2-CP30 | `PasswordField.test.jsx` | `HU2-CP30: con bloquearCopia sigue sin dejar copiarla ni cortarla con la contraseña visible` | Frontend · componente | AB#242 | PR pendiente | 🔍 |
| HU2-CP30 | `PasswordField.test.jsx` | `HU2-CP30: con bloquearCopia sí deja pegar (gestores de contraseñas) y soltar texto` | Frontend · componente | AB#242 | PR pendiente | 🔍 |
| HU2-CP30 | `PasswordField.test.jsx` | `HU2-CP30: con bloquearCopia se puede escribir a mano` | Frontend · componente | AB#242 | PR pendiente | 🔍 |
| HU2-CP30 | `PasswordField.test.jsx` | `HU2-CP30: sin ninguna de las dos propiedades se puede copiar y cortar` | Frontend · componente | AB#242 | PR pendiente | 🔍 |
| HU2-CP31 | `LoginFlow.test.jsx` | `HU2-CP31: la contraseña del login no se puede copiar ni cortar` | Frontend · página | AB#242 | PR pendiente | 🔍 |
| HU2-CP31 | `LoginFlow.test.jsx` | `HU2-CP31: tampoco se puede copiar ni cortar con la contraseña visible` | Frontend · página | AB#242 | PR pendiente | 🔍 |
| HU2-CP31 | `LoginFlow.test.jsx` | `HU2-CP31: sí se puede pegar la contraseña y el login sigue funcionando` | Frontend · página | AB#242 | PR pendiente | 🔍 |
| HU2-CP31 | `LoginFlow.test.jsx` | `HU2-CP31: escribiendo la contraseña a mano el login sigue funcionando` | Frontend · página | AB#242 | PR pendiente | 🔍 |

Rutas de las clases de test (bajo `backend/src/test/java/es/uclm/esibuy/backend/`):
- `AutenticacionServiceTest`: `usuarios/application/service/` (Mockito, con `Clock` fijo)
- `CodigoVerificacionTest`: `usuarios/domain/model/` (JUnit puro, sin Spring)
- `VerificarCodigoEmailCommandTest`: `usuarios/application/port/in/`
- `AutenticacionControllerTest`, `LoginRequestTest` y `VerificarCodigoRequestTest`: `usuarios/infrastructure/adapter/in/web/` (el controlador con `@WebMvcTest` y el caso de uso simulado)
- `BCryptHashContrasenaAdapterTest` y `SecureRandomGeneradorCodigoAdapterTest`: `usuarios/infrastructure/adapter/out/seguridad/`
- `SmtpEnvioEmailAdapterTest`: `usuarios/infrastructure/adapter/out/email/` (Mockito, con un mock de `JavaMailSender`; no envía correos reales)
- `UsuarioDocumentMapperTest`, `UsuarioMongoAdapterTest`, `CodigoVerificacionDocumentMapperTest` y `CodigoVerificacionMongoAdapterTest`: `usuarios/infrastructure/adapter/out/persistence/` (los adaptadores, con Testcontainers)

---

## HU3 · Recuperación de contraseña

### Tabla A · HU3 ↔ casos de prueba

| Caso | Descripción | Tipo | Tests | Estado |
|---|---|---|---|---|
| HU3-CP01 | Cambiar la contraseña de un usuario guarda el hash de la nueva, rechaza uno vacío y quita el bloqueo por intentos fallidos de login | Unitario (dominio) | 3 | ✅ Pasa |
| HU3-CP02 | Solicitar la recuperación de una cuenta activa envía un código de 6 dígitos por correo y guarda solo su hash, válido 10 minutos; el email se busca normalizado | Unitario (aplicación) | 2 | ✅ Pasa |
| HU3-CP03 | Con un email no registrado o mal formado, o una cuenta pendiente o desactivada, no se envía nada y la respuesta es la misma (no se revela qué emails existen) | Unitario (aplicación) | 3 | ✅ Pasa |
| HU3-CP04 | Con el código correcto se guarda el hash de la contraseña nueva y el código queda usado (un solo uso) | Unitario (aplicación) | 1 | ✅ Pasa |
| HU3-CP05 | Con un código incorrecto se registra el fallo y la contraseña no cambia | Unitario (aplicación) | 1 | ✅ Pasa |
| HU3-CP06 | Un código caducado, ya usado o con 5 fallos no sirve; sin código o con un email desconocido se responde como con un código incorrecto | Unitario (aplicación) | 2 | ✅ Pasa |
| HU3-CP07 | Si la contraseña nueva no cumple la política de seguridad no cambia nada y el código sigue sirviendo | Unitario (aplicación) | 1 | ✅ Pasa |
| HU3-CP08 | El comando de restablecer no muestra la contraseña ni el código en su `toString` (logs) | Unitario (aplicación) | 1 | ✅ Pasa |
| HU3-CP09 | El correo de recuperación lleva el código y su validez; si falla el envío, la excepción no revela el código | Unitario (infraestructura) | 2 | ✅ Pasa |
| HU3-CP10 | Los códigos de recuperación se guardan en `tokens_seguridad` con `purpose` PASSWORD_RECOVERY, solo con su hash; uno nuevo sustituye al anterior y no se pisan con el código del login | Unitario + integración (Testcontainers) | 6 | ✅ Pasa |
| HU3-CP11 | Si falla el envío del correo de recuperación, la solicitud termina igual: el fallo no revela que la cuenta existe | Unitario (aplicación) | 1 | ✅ Pasa |
| HU3-CP12 | `POST /api/auth/password-recovery` responde 204 sin sesión y pasa el email al caso de uso | Integración web (`@WebMvcTest`) | 1 | ✅ Pasa |
| HU3-CP13 | `POST /api/auth/password-recovery/confirm` con el código correcto responde 204 y pasa email, código y contraseña al caso de uso | Integración web (`@WebMvcTest`) | 1 | ✅ Pasa |
| HU3-CP14 | Restablecer con un código que no sirve responde 401 «Código incorrecto» | Integración web (`@WebMvcTest`) | 1 | ✅ Pasa |
| HU3-CP15 | Restablecer con una contraseña que no cumple la política responde 400 con el código del motivo (p. ej. `PASSWORD_WEAK`) | Integración web (`@WebMvcTest`) | 1 | ✅ Pasa |
| HU3-CP16 | Los dos endpoints rechazan un cuerpo incompleto (400) y una petición sin token anti-CSRF (403) sin llamar al caso de uso | Integración web (`@WebMvcTest`) | 4 | ✅ Pasa |
| HU3-CP17 | La petición de restablecer no muestra la contraseña ni el código en su `toString` (logs) | Unitario (infraestructura) | 1 | ✅ Pasa |
| HU3-CP18 | El inicio de sesión tiene el enlace «¿Has olvidado tu contraseña?», que lleva a la recuperación | Frontend (página) | 1 | ✅ Pasa |
| HU3-CP19 | Sin email, o con uno mal formado, se avisa en el campo y no se pide ningún código | Frontend (página) | 1 | ✅ Pasa |
| HU3-CP20 | Se pide el código para el email escrito y se pasa al segundo paso con el mismo aviso exista o no la cuenta (caduca en 10 minutos); si el servidor falla, se avisa y no se avanza | Frontend (página) | 3 | ✅ Pasa |
| HU3-CP21 | Antes de enviar se comprueba que el código tiene 6 dígitos y que la contraseña nueva cumple los requisitos (que se muestran) y coincide con su repetición | Frontend (página) | 2 | ✅ Pasa |
| HU3-CP22 | Con el código correcto se envían email, código y contraseña, y se vuelve al login con «Contraseña cambiada. Ya puedes iniciar sesión.» | Frontend (página) | 1 | ✅ Pasa |
| HU3-CP23 | Con un código incorrecto o caducado se avisa y se puede volver a intentar o pedir otro código | Frontend (página) | 2 | ✅ Pasa |
| HU3-CP24 | Si el servidor rechaza la contraseña por la política, el motivo se muestra en el campo; con otro error, un aviso genérico | Frontend (página) | 2 | ✅ Pasa |
| HU3-CP25 | Desde la recuperación se vuelve al login; con la sesión abierta, la ruta lleva al inicio del usuario | Frontend (página) | 2 | ✅ Pasa |

Notas:
- **HU3-CP01 a CP10 (Sprint 2, backend, TDD):** casos propuestos en la T400 (AB#400, lógica de recuperación de contraseña) a partir de los criterios de aceptación de la HU3 (AB#65): «envío de enlace/código temporal y de un solo uso», «expiración del enlace tras un tiempo determinado» y «la contraseña nueva debe cumplir la política de seguridad». Pendientes de confirmar con el equipo y de añadir a la HU3 en Azure Boards.
- **HU3-CP11 a CP17 (Sprint 2, backend, TDD):** casos propuestos en la T399 (AB#399, endpoint de recuperación de contraseña): `POST /api/auth/password-recovery` (`{email}`, siempre 204) y `POST /api/auth/password-recovery/confirm` (`{email, code, password}`: 204, 401 o 400). Son públicos, pero piden el token anti-CSRF como el login. Pendientes de confirmar con el equipo y de añadir a la HU3 en Azure Boards.
- **HU3-CP18 a CP25 (Sprint 2, frontend, TDD):** casos propuestos en la T398 (AB#398, formulario de recuperación de contraseña): enlace en el login y página `/forgot-password` en dos pasos, con el contrato de la T399 (`frontend/docs/hu3-recuperar-contrasena.md`). Pendientes de confirmar con el equipo y de añadir a la HU3 en Azure Boards.
- **Decisiones del equipo:** código de 6 dígitos por correo (no enlace), que reutiliza las reglas del código del login (`CodigoVerificacion`: 10 minutos, un solo uso, se anula tras 5 fallos, solo se guarda su hash). Solo se envía a cuentas activas. Solicitar la recuperación responde siempre igual; al restablecer, un email desconocido responde como un código incorrecto. Si falla el envío del correo, la solicitud también responde igual (HU3-CP11): el envío solo se intenta con cuentas activas, así que un error revelaría que el email existe.
- **Pendiente:** comprobar el envío real por Brevo de punta a punta, con el formulario de la T398. **No hay límite de solicitudes de código** por email o por IP (no lo pide la HU3); si hace falta, irá en una task aparte.

### Tabla B · casos de prueba ↔ tests

| Caso | Clase de test | Método | Capa | Task | Origen | Estado |
|---|---|---|---|---|---|---|
| HU3-CP01 | `UsuarioTest` | `cambiarContrasenaSustituyeElHash` | Dominio | AB#400 | PR #172 | ✅ |
| HU3-CP01 | `UsuarioTest` | `cambiarContrasenaRechazaUnHashVacio` | Dominio | AB#400 | PR #172 | ✅ |
| HU3-CP01 | `UsuarioTest` | `cambiarContrasenaQuitaElBloqueoPorIntentos` | Dominio | AB#400 | PR #172 | ✅ |
| HU3-CP02 | `RecuperacionContrasenaServiceTest` | `solicitarConCuentaActivaEnviaElCodigoYGuardaSoloSuHash` | Aplicación | AB#400 | PR #172 | ✅ |
| HU3-CP02 | `RecuperacionContrasenaServiceTest` | `solicitarBuscaLaCuentaConElEmailNormalizado` | Aplicación | AB#400 | PR #172 | ✅ |
| HU3-CP03 | `RecuperacionContrasenaServiceTest` | `solicitarConEmailDesconocidoNoEnviaNadaNiLoRevela` | Aplicación | AB#400 | PR #172 | ✅ |
| HU3-CP03 | `RecuperacionContrasenaServiceTest` | `solicitarConEmailMalFormadoNoEnviaNadaNiLoRevela` | Aplicación | AB#400 | PR #172 | ✅ |
| HU3-CP03 | `RecuperacionContrasenaServiceTest` | `solicitarConCuentaNoActivaNoEnviaNada` | Aplicación | AB#400 | PR #172 | ✅ |
| HU3-CP04 | `RecuperacionContrasenaServiceTest` | `restablecerConCodigoCorrectoCambiaLaContrasenaYGastaElCodigo` | Aplicación | AB#400 | PR #172 | ✅ |
| HU3-CP05 | `RecuperacionContrasenaServiceTest` | `restablecerConCodigoIncorrectoRegistraElFalloYNoCambiaNada` | Aplicación | AB#400 | PR #172 | ✅ |
| HU3-CP06 | `RecuperacionContrasenaServiceTest` | `restablecerConCodigoQueYaNoSirveNoCambiaNada` | Aplicación | AB#400 | PR #172 | ✅ |
| HU3-CP06 | `RecuperacionContrasenaServiceTest` | `restablecerSinCodigoOConEmailDesconocidoRespondeComoCodigoIncorrecto` | Aplicación | AB#400 | PR #172 | ✅ |
| HU3-CP07 | `RecuperacionContrasenaServiceTest` | `restablecerConContrasenaQueNoCumpleLaPoliticaNoGastaElCodigo` | Aplicación | AB#400 | PR #172 | ✅ |
| HU3-CP08 | `RestablecerContrasenaCommandTest` | `toStringNoMuestraLaContrasenaNiElCodigo` | Aplicación | AB#400 | PR #172 | ✅ |
| HU3-CP09 | `SmtpEnvioEmailAdapterTest` | `enviaElCodigoDeRecuperacionAlCorreoDelUsuario` | Infraestructura | AB#400 | PR #172 | ✅ |
| HU3-CP09 | `SmtpEnvioEmailAdapterTest` | `fallaElEnvioDeRecuperacionSinRevelarElCodigo` | Infraestructura | AB#400 | PR #172 | ✅ |
| HU3-CP10 | `CodigoVerificacionDocumentMapperTest` | `convierteCodigoDeRecuperacionEnDocumento` | Infraestructura | AB#400 | PR #172 | ✅ |
| HU3-CP10 | `CodigoRecuperacionMongoAdapterTest` | `guardaElCodigoComoDeRecuperacion` | Infraestructura | AB#400 | PR #172 | ✅ |
| HU3-CP10 | `CodigoRecuperacionMongoAdapterTest` | `buscarPorUsuarioDevuelveElCodigoGuardado` | Infraestructura | AB#400 | PR #172 | ✅ |
| HU3-CP10 | `CodigoRecuperacionMongoAdapterTest` | `unCodigoNuevoSustituyeAlAnterior` | Infraestructura | AB#400 | PR #172 | ✅ |
| HU3-CP10 | `CodigoRecuperacionMongoAdapterTest` | `guardarUnCodigoLeidoLoActualiza` | Infraestructura | AB#400 | PR #172 | ✅ |
| HU3-CP10 | `CodigoRecuperacionMongoAdapterTest` | `elCodigoDeRecuperacionYElDelLoginNoSePisan` | Infraestructura | AB#400 | PR #172 | ✅ |
| HU3-CP11 | `RecuperacionContrasenaServiceTest` | `solicitarConFalloDelCorreoNoLoRevela` | Aplicación | AB#399 | PR #176 | ✅ |
| HU3-CP12 | `RecuperacionContrasenaControllerTest` | `solicitarRespondeSinContenidoYPasaElEmail` | Infraestructura (web) | AB#399 | PR #176 | ✅ |
| HU3-CP13 | `RecuperacionContrasenaControllerTest` | `restablecerRespondeSinContenidoYPasaElComando` | Infraestructura (web) | AB#399 | PR #176 | ✅ |
| HU3-CP14 | `RecuperacionContrasenaControllerTest` | `restablecerConCodigoInvalidoRespondeUnauthorized` | Infraestructura (web) | AB#399 | PR #176 | ✅ |
| HU3-CP15 | `RecuperacionContrasenaControllerTest` | `restablecerConContrasenaDebilRespondeBadRequestConSuCodigo` | Infraestructura (web) | AB#399 | PR #176 | ✅ |
| HU3-CP16 | `RecuperacionContrasenaControllerTest` | `solicitarSinEmailRespondeBadRequest` | Infraestructura (web) | AB#399 | PR #176 (modificado en PR #183) | ✅ |
| HU3-CP16 | `RecuperacionContrasenaControllerTest` | `solicitarSinCsrfRespondeForbidden` | Infraestructura (web) | AB#399 | PR #176 | ✅ |
| HU3-CP16 | `RecuperacionContrasenaControllerTest` | `restablecerConCamposVaciosRespondeBadRequest` | Infraestructura (web) | AB#399 | PR #176 (modificado en PR #183) | ✅ |
| HU3-CP16 | `RecuperacionContrasenaControllerTest` | `restablecerSinCsrfRespondeForbidden` | Infraestructura (web) | AB#399 | PR #176 | ✅ |
| HU3-CP17 | `RestablecerContrasenaRequestTest` | `toStringNoMuestraLaContrasenaNiElCodigo` | Infraestructura (web) | AB#399 | PR #176 | ✅ |
| HU3-CP18 | `LoginFlow.test.jsx` | `HU3-CP18: el inicio de sesión tiene un enlace a la recuperación de contraseña` | Frontend · página | AB#398 | PR #178 | ✅ |
| HU3-CP19 | `ForgotPasswordPage.test.jsx` | `HU3-CP19: sin email, o con uno mal formado, avisa en el campo y no pide ningún código` | Frontend · página | AB#398 | PR #178 | ✅ |
| HU3-CP20 | `ForgotPasswordPage.test.jsx` | `HU3-CP20: pide el código para el email escrito y pasa al segundo paso con el aviso de que se ha enviado` | Frontend · página | AB#398 | PR #178 | ✅ |
| HU3-CP20 | `ForgotPasswordPage.test.jsx` | `HU3-CP20: con un email que no está registrado muestra el mismo aviso (no revela qué emails existen)` | Frontend · página | AB#398 | PR #178 | ✅ |
| HU3-CP20 | `ForgotPasswordPage.test.jsx` | `HU3-CP20: si el servidor falla al pedir el código, lo avisa y se queda en el primer paso` | Frontend · página | AB#398 | PR #178 | ✅ |
| HU3-CP21 | `ForgotPasswordPage.test.jsx` | `HU3-CP21: comprueba el código y la contraseña nueva antes de enviarlos` | Frontend · página | AB#398 | PR #178 | ✅ |
| HU3-CP21 | `ForgotPasswordPage.test.jsx` | `HU3-CP21: muestra los requisitos de la contraseña nueva y marca los que ya cumple` | Frontend · página | AB#398 | PR #178 | ✅ |
| HU3-CP22 | `ForgotPasswordPage.test.jsx` | `HU3-CP22: con el código correcto cambia la contraseña y vuelve al login avisando del cambio` | Frontend · página | AB#398 | PR #178 | ✅ |
| HU3-CP23 | `ForgotPasswordPage.test.jsx` | `HU3-CP23: con un código incorrecto o caducado lo avisa y se queda en el paso para volver a intentarlo` | Frontend · página | AB#398 | PR #178 | ✅ |
| HU3-CP23 | `ForgotPasswordPage.test.jsx` | `HU3-CP23: se puede pedir otro código para el mismo email` | Frontend · página | AB#398 | PR #178 | ✅ |
| HU3-CP24 | `ForgotPasswordPage.test.jsx` | `HU3-CP24: si el servidor rechaza la contraseña por la política, lo muestra en el campo` | Frontend · página | AB#398 | PR #178 | ✅ |
| HU3-CP24 | `ForgotPasswordPage.test.jsx` | `HU3-CP24: si el servidor falla por otro motivo, muestra un aviso genérico sin detalles internos` | Frontend · página | AB#398 | PR #178 | ✅ |
| HU3-CP25 | `ForgotPasswordPage.test.jsx` | `HU3-CP25: desde la recuperación se puede volver al inicio de sesión` | Frontend · página | AB#398 | PR #178 | ✅ |
| HU3-CP25 | `ForgotPasswordPage.test.jsx` | `HU3-CP25: con la sesión abierta no se muestra la recuperación y lleva a su inicio` | Frontend · página | AB#398 | PR #178 | ✅ |

Rutas de las clases de test (bajo `backend/src/test/java/es/uclm/esibuy/backend/`):
- `UsuarioTest`: `usuarios/domain/model/` (JUnit, sin Spring)
- `RecuperacionContrasenaServiceTest`: `usuarios/application/service/` (JUnit y Mockito, sin Spring)
- `RestablecerContrasenaCommandTest`: `usuarios/application/port/in/` (JUnit)
- `SmtpEnvioEmailAdapterTest`: `usuarios/infrastructure/adapter/out/email/` (Mockito, sin conectarse a Brevo)
- `CodigoVerificacionDocumentMapperTest`: `usuarios/infrastructure/adapter/out/persistence/` (JUnit)
- `CodigoRecuperacionMongoAdapterTest`: `usuarios/infrastructure/adapter/out/persistence/` (`@DataMongoTest` y Testcontainers)
- `RecuperacionContrasenaControllerTest`: `usuarios/infrastructure/adapter/in/web/` (`@WebMvcTest` con `SecurityConfig`, sin MongoDB)
- `RestablecerContrasenaRequestTest`: `usuarios/infrastructure/adapter/in/web/` (JUnit)

Rutas de los tests del frontend (bajo `frontend/src/`, Vitest con React Testing Library y MSW):
- `LoginFlow.test.jsx`: `features/auth/`
- `ForgotPasswordPage.test.jsx`: `pages/`

---

## HU4 · Visión de perfil

### Tabla A · HU4 ↔ casos de prueba

| Caso | Descripción | Tipo | Tests | Estado |
|---|---|---|---|---|
| HU4-CP01 | Un cliente consulta su perfil (`GET /api/perfil`) y recibe sus datos personales y los de cliente, sin los de vendedor | Unitario + controlador | 2 | ✅ Pasa |
| HU4-CP02 | Un vendedor consulta su perfil y recibe sus datos personales y los de vendedor, sin los de cliente | Controlador | 1 | ✅ Pasa |
| HU4-CP03 | El perfil no expone la contraseña ni datos internos de la cuenta (estado, intentos, bloqueo) | Controlador | 1 | ✅ Pasa |
| HU4-CP04 | Solo el cliente y el vendedor consultan su perfil: administrador 403, sin sesión 401, y una sesión de un usuario que ya no existe no es válida | Unitario + controlador | 3 | ✅ Pasa |
| HU4-CP05 | En «Mi perfil» el cliente ve sus datos personales y los de cliente, en solo lectura | Frontend (página) | 2 | ✅ Pasa |
| HU4-CP06 | El vendedor ve sus datos y los de vendedor, con el nombre de su categoría; un dato opcional que falta se ve con un guion | Frontend (página) | 2 | ✅ Pasa |
| HU4-CP07 | La imagen de perfil se ve como el avatar elegido; sin avatar, se ven las iniciales | Frontend (componente y página) | 4 | ✅ Pasa |
| HU4-CP08 | La vista no muestra la contraseña ni el estado de la cuenta | Frontend (página) | 1 | ✅ Pasa |
| HU4-CP09 | Si el perfil no carga, se avisa sin detalles internos y se puede reintentar | Frontend (página) | 1 | ✅ Pasa |
| HU4-CP10 | Sin sesión, `/profile` lleva al login; el administrador no entra en el perfil | Frontend (página) | 2 | ✅ Pasa |
| HU4-CP11 | El cliente y el vendedor tienen el enlace «Mi perfil» en la cabecera, que abre su perfil; el administrador no lo tiene | Frontend (página) | 3 | ✅ Pasa |

Notas:
- **HU4-CP01 a CP04:** casos propuestos en la T297 (AB#297), a partir del criterio de aceptación «cuando el usuario accede a su panel de perfil puede visualizar sus datos personales». Pendientes de confirmar con el equipo y de añadir a la HU4 en Azure Boards.
- **Endpoint (T297):** `GET /api/perfil` → `{id, nombre, apellidos, email, rol, fechaAlta, dni, telefono, avatar}` y, según el rol, `fechaNacimiento` y `tipoCliente` (cliente) o `nombreComercial` y `categoriaPrincipalId` (vendedor). Los campos vacíos no aparecen. El usuario sale de la sesión (su email), nunca de la petición. Solo para `CLIENTE` y `VENDEDOR` (`SecurityConfig`). Es el contrato de la vista del frontend (T296).
- **HU4-CP05 a CP11 (Sprint 2, frontend):** casos propuestos en la task AB#296 (vista de perfil) a partir del mismo criterio de aceptación y del contrato de la T297 (`frontend/docs/hu4-vista-perfil.md`). Siguen la numeración del backend. Pendientes de confirmar con el equipo y de añadir a la HU4 en Azure Boards.
- **Archivo de tests nuevo** (con el OK del equipo): `ProfilePage.test.jsx`. `AppHeader.test.jsx` y `AvatarSelector.test.jsx` ganan casos HU4.
- **Pendiente:** el criterio «el usuario debe dar al botón de guardar para modificar sus datos personales» es de la HU45 (edición de perfil). Aclarar con el equipo qué incluyen sus tasks 101 (endpoint consultar/editar perfil) y 298 (lógica para la obtención segura de datos), que se solapan con la T297.

### Tabla B · casos de prueba ↔ tests

| Caso | Clase de test | Método | Capa | Task | Origen | Estado |
|---|---|---|---|---|---|---|
| HU4-CP01 | `GestionPerfilServiceTest` | `consultarPerfilDevuelveElUsuarioDelCorreo` | Aplicación | AB#297 | PR #140 | ✅ |
| HU4-CP01 | `PerfilControllerTest` | `unClienteRecibeSusDatos` | Infraestructura | AB#297 | PR #140 | ✅ |
| HU4-CP02 | `PerfilControllerTest` | `unVendedorRecibeSusDatos` | Infraestructura | AB#297 | PR #140 | ✅ |
| HU4-CP03 | `PerfilControllerTest` | `elPerfilNoExponeDatosSensibles` | Infraestructura | AB#297 | PR #140 | ✅ |
| HU4-CP04 | `GestionPerfilServiceTest` | `consultarPerfilDeUnCorreoQueNoExisteLanzaExcepcion` | Aplicación | AB#297 | PR #140 | ✅ |
| HU4-CP04 | `PerfilControllerTest` | `unAdministradorRecibe403` | Infraestructura | AB#297 | PR #140 | ✅ |
| HU4-CP04 | `PerfilControllerTest` | `sinSesionRecibe401` | Infraestructura | AB#297 | PR #140 | ✅ |
| HU4-CP05 | `ProfilePage.test.jsx` | `HU4-CP05: el cliente ve sus datos personales y los de cliente` | Frontend · página | AB#296 | PR #150 | ✅ |
| HU4-CP05 | `ProfilePage.test.jsx` | `HU4-CP05: los datos del perfil son de solo lectura` | Frontend · página | AB#296 | PR #150 | ✅ |
| HU4-CP06 | `ProfilePage.test.jsx` | `HU4-CP06: el vendedor ve sus datos y los de vendedor, con el nombre de su categoría` | Frontend · página | AB#296 | PR #150 | ✅ |
| HU4-CP06 | `ProfilePage.test.jsx` | `HU4-CP06: los datos opcionales que no tiene se muestran con un guion` | Frontend · página | AB#296 | PR #150 | ✅ |
| HU4-CP07 | `ProfilePage.test.jsx` | `HU4-CP07: muestra como imagen el avatar que eligió el usuario` | Frontend · página | AB#296 | PR #150 | ✅ |
| HU4-CP07 | `ProfilePage.test.jsx` | `HU4-CP07: sin avatar elegido muestra sus iniciales` | Frontend · página | AB#296 | PR #150 | ✅ |
| HU4-CP07 | `AvatarSelector.test.jsx` | `HU4-CP07: muestra el avatar elegido con su nombre accesible` | Frontend · componente | AB#296 | PR #150 | ✅ |
| HU4-CP07 | `AvatarSelector.test.jsx` | `HU4-CP07: sin avatar, o con uno que no existe, muestra las iniciales` | Frontend · componente | AB#296 | PR #150 | ✅ |
| HU4-CP08 | `ProfilePage.test.jsx` | `HU4-CP08: no muestra la contraseña ni el estado de la cuenta` | Frontend · página | AB#296 | PR #150 | ✅ |
| HU4-CP09 | `ProfilePage.test.jsx` | `HU4-CP09: si el perfil no carga, avisa sin detalles internos y permite reintentar` | Frontend · página | AB#296 | PR #150 | ✅ |
| HU4-CP10 | `ProfilePage.test.jsx` | `HU4-CP10: sin sesión, redirige al login` | Frontend · página | AB#296 | PR #150 | ✅ |
| HU4-CP10 | `ProfilePage.test.jsx` | `HU4-CP10: el administrador no tiene perfil que consultar` | Frontend · página | AB#296 | PR #150 | ✅ |
| HU4-CP11 | `AppHeader.test.jsx` | `HU4-CP11: el cliente y el vendedor tienen el enlace a su perfil` | Frontend · página | AB#296 | PR #150 | ✅ |
| HU4-CP11 | `AppHeader.test.jsx` | `HU4-CP11: el administrador no tiene el enlace al perfil` | Frontend · página | AB#296 | PR #150 | ✅ |
| HU4-CP11 | `AppHeader.test.jsx` | `HU4-CP11: al pulsar Mi perfil se abre el perfil del usuario` | Frontend · página | AB#296 | PR #150 | ✅ |

Rutas de las clases de test (bajo `backend/src/test/java/es/uclm/esibuy/backend/`):
- `GestionPerfilServiceTest`: `usuarios/application/service/` (JUnit y Mockito, sin Spring)
- `PerfilControllerTest`: `usuarios/infrastructure/adapter/in/web/` (con `@WebMvcTest` y la seguridad real)
- `ProfilePage.test.jsx`: `frontend/src/pages/` (Vitest, RTL y MSW)
- `AppHeader.test.jsx`: `frontend/src/components/` (Vitest, RTL y MSW)
- `AvatarSelector.test.jsx`: `frontend/src/features/account/` (Vitest y RTL)

---

## HU5 · Gestión de direcciones de envío

### Tabla A · HU5 ↔ casos de prueba

| Caso | Descripción | Tipo | Tests | Estado |
|---|---|---|---|---|
| HU5-CP01 | El código postal tiene 5 dígitos; rechaza uno de 4 o 6, con letras o espacios, nulo o vacío | Unitario | 4 | ✅ Pasa |
| HU5-CP02 | El teléfono de contacto tiene 9 dígitos, sin prefijo; rechaza uno de 8 o 10, con letras, nulo o vacío | Unitario | 4 | ✅ Pasa |
| HU5-CP03 | Una dirección exige cliente, destinatario, calle, código postal, ciudad, provincia y teléfono, con su longitud máxima (100; la calle, 150); los detalles son opcionales; se reconstruye conservando si es la principal | Unitario | 8 | ✅ Pasa |
| HU5-CP04 | Un cliente tiene como máximo 5 direcciones; la sexta se rechaza y no se guarda | Unitario | 3 | ✅ Pasa |
| HU5-CP05 | Solo hay una dirección principal: la primera lo es, marcar otra desmarca la anterior y no se puede marcar una de otro cliente | Unitario | 8 | ✅ Pasa |
| HU5-CP06 | Al eliminar la principal el cliente elige la sustituta (no la misma ni una ajena); la única se elimina sin elegir; no se elimina una de otro cliente | Unitario | 11 | ✅ Pasa |
| HU5-CP07 | Editar cambia los datos y conserva el id y la principal; no se edita una dirección de otro cliente ni se guardan datos inválidos | Unitario | 7 | ✅ Pasa |
| HU5-CP08 | El alta guarda la dirección con el cliente en sesión y rechaza datos inválidos; el listado devuelve solo las del cliente en sesión | Unitario | 3 | ✅ Pasa |
| HU5-CP09 | La dirección se convierte a su documento de MongoDB y vuelve sin perder datos; un documento inválido se rechaza | Unitario | 2 | ✅ Pasa |
| HU5-CP10 | Persistencia en MongoDB: guardar, buscar por cliente, actualizar conservando `createdAt` y eliminar; la colección rechaza un documento sin un campo obligatorio o con un código postal o un teléfono inválidos | Integración | 6 | ✅ Pasa |
| HU5-CP11 | El id del cliente se pide al módulo de usuarios por su puerto de entrada | Unitario | 1 | ✅ Pasa |
| HU5-CP12 | `GET /api/customer/addresses` devuelve las direcciones del cliente en sesión con la marca `principal` | Controlador | 1 | ✅ Pasa |
| HU5-CP13 | `POST`: 201 con la dirección creada y el cliente de la sesión; dato inválido 400 `VALIDATION_ERROR`; límite 409 `ADDRESS_LIMIT_REACHED` | Controlador | 3 | ✅ Pasa |
| HU5-CP14 | `PUT /{id}`: 200 con la dirección editada; dato inválido 400; una ajena o inexistente 404 `ADDRESS_NOT_FOUND` | Controlador | 3 | ✅ Pasa |
| HU5-CP15 | `PUT /{id}/principal` (204) y `DELETE /{id}?nuevaPrincipal=` (204); sin sustituta, 409 `PRINCIPAL_ADDRESS_REQUIRED`; una ajena o inexistente da 404 | Controlador | 5 | ✅ Pasa |
| HU5-CP16 | Solo un cliente con sesión accede, y las peticiones que modifican datos exigen CSRF | Controlador | 3 | ✅ Pasa |
| HU5-CP17 | El cliente de la API de direcciones: listar, añadir, editar, marcar la principal, eliminar con sustituta opcional y errores como `ApiError` con su código | Frontend · API | 6 | ✅ Pasa |
| HU5-CP18 | La vista lista cada dirección con su destinatario, la dirección completa y el teléfono, marca la principal, muestra un estado vacío y, si falla la carga, deja reintentar | Frontend (página) | 4 | ✅ Pasa |
| HU5-CP19 | El cliente llega desde el enlace «Direcciones» de la cabecera; el vendedor y el administrador no acceden | Frontend (página) | 3 | ✅ Pasa |
| HU5-CP20 | Marcar otra dirección como principal la cambia en la lista; si la dirección ya no existe, se avisa y se recarga | Frontend (página) | 2 | ✅ Pasa |
| HU5-CP21 | Con 5 direcciones no se puede añadir otra y se explica por qué; con menos, sí | Frontend (página) | 2 | ✅ Pasa |
| HU5-CP22 | El formulario no envía una dirección sin los campos obligatorios ni con un código postal, un teléfono o una longitud incorrectos | Frontend (componente) | 2 | ✅ Pasa |
| HU5-CP23 | Alta: el panel empieza vacío y envía la dirección sin espacios sobrantes; al cancelar no envía nada; desde la página, guardar cierra el panel, avisa y añade la dirección a la lista | Frontend (componente y página) | 4 | ✅ Pasa |
| HU5-CP24 | Edición: el panel empieza con los datos de la dirección y guarda los cambios con un `PUT` sin tocar la principal; si ya no existe, se avisa; desde la página, la lista muestra los cambios | Frontend (componente y página) | 3 | ✅ Pasa |
| HU5-CP25 | Una dirección rechazada por el backend se avisa sin repetir los datos enviados; el límite tiene su propio mensaje | Frontend (componente) | 2 | ✅ Pasa |
| HU5-CP26 | Eliminar una dirección tras confirmarlo; cancelar no elimina nada | Frontend (componente) | 2 | ✅ Pasa |
| HU5-CP27 | Para eliminar la principal hay que elegir la sustituta, salvo si es la única; desde la página, la elegida pasa a ser la principal | Frontend (componente y página) | 3 | ✅ Pasa |
| HU5-CP28 | Si la eliminación falla, se avisa sin cerrar el diálogo | Frontend (componente) | 1 | ✅ Pasa |
| HU5-CP29 | La gestión de direcciones exige el rol cliente y que el correo recibido sea el de la sesión; sin sesión se rechaza. Seguridad en la capa de negocio (R8) | Unitario con Spring Security (sin MongoDB) | 5 | ✅ Pasa |

Notas:
- **HU5-CP01 a CP16 (backend):** casos propuestos y aprobados por el equipo junto con el diseño de la HU5 (tasks AB#105, AB#106 y AB#107). Los casos de prueba no se registran en Azure Boards: viven en esta matriz y en el `@DisplayName` de cada test. Los del frontend, HU5-CP17 a CP28, se aprobaron con el mismo diseño (tasks AB#108 y AB#109).
- **Decisiones del equipo:** mismas reglas que los métodos de pago (HU6): máximo 5 direcciones por cliente, la primera queda como principal y al eliminar la principal el cliente elige otra. Solo España (código postal de 5 dígitos, teléfono de 9, sin país). La dirección no se cifra en esta HU: la protegen el RBAC y la validación de la colección (pendiente de valorar en el AGR).
- **Endpoints (AB#105):** `GET`, `POST`, `PUT /{id}`, `PUT /{id}/principal` y `DELETE /{id}?nuevaPrincipal=` sobre `/api/customer/addresses`, solo para `CLIENTE` (`SecurityConfig`). El cliente sale de la sesión. La respuesta es `{id, destinatario, calle, detalles, codigoPostal, ciudad, provincia, telefono, principal}`; sin `id` ni `principal`, es el `direccionEnvio` que copiará el pedido (HU10).
- **Colección nueva:** `direcciones_envio` (`ColeccionesEsiBuy`), acordada con el equipo.
- **Vista del frontend (AB#108):** `/addresses`, solo para clientes. Detalle en `frontend/docs/hu5-direcciones-de-envio.md`.
- **Archivos de tests nuevos**, acordados con el equipo: `addresses.test.js`, `AddressesPage.test.jsx`, `AddressFormPanel.test.jsx` y `DeleteAddressDialog.test.jsx`.
- **HU5-CP29 (R8, seguridad en la capa de negocio):** caso propuesto en la rama `fix/seguridad-capa-negocio` (PR #168, mergeado), sin task ni `AB#` por ser un fix transversal. Pendiente de confirmar con el equipo. Comprueba con `@PreAuthorize` que `GestionDireccionesUseCase` exige el rol cliente y que el correo recibido es el de la sesión. Clase de test nueva `direcciones/application/service/SeguridadDireccionesServiceTest` (contexto mínimo de Spring con `@EnableMethodSecurity`, sin MongoDB).

### Tabla B · casos de prueba ↔ tests

| Caso | Clase de test | Método | Capa | Task | Origen | Estado |
|---|---|---|---|---|---|---|
| HU5-CP01 | `CodigoPostalTest` | `creaCodigoPostalDeCincoDigitos` | Dominio | AB#105 | PR #161 | ✅ |
| HU5-CP01 | `CodigoPostalTest` | `rechazaCodigoPostalDeCuatroOSeisDigitos` | Dominio | AB#105 | PR #161 | ✅ |
| HU5-CP01 | `CodigoPostalTest` | `rechazaCodigoPostalConLetrasOEspacios` | Dominio | AB#105 | PR #161 | ✅ |
| HU5-CP01 | `CodigoPostalTest` | `rechazaCodigoPostalNuloOVacio` | Dominio | AB#105 | PR #161 | ✅ |
| HU5-CP02 | `TelefonoContactoTest` | `creaTelefonoDeNueveDigitos` | Dominio | AB#105 | PR #161 | ✅ |
| HU5-CP02 | `TelefonoContactoTest` | `rechazaTelefonoDeOchoODiezDigitos` | Dominio | AB#105 | PR #161 | ✅ |
| HU5-CP02 | `TelefonoContactoTest` | `rechazaTelefonoConLetrasOPrefijo` | Dominio | AB#105 | PR #161 | ✅ |
| HU5-CP02 | `TelefonoContactoTest` | `rechazaTelefonoNuloOVacio` | Dominio | AB#105 | PR #161 | ✅ |
| HU5-CP03 | `DatosDireccionTest` | `creaDireccionCompleta` | Dominio | AB#105 | PR #161 | ✅ |
| HU5-CP03 | `DatosDireccionTest` | `losDetallesSonOpcionales` | Dominio | AB#105 | PR #161 | ✅ |
| HU5-CP03 | `DatosDireccionTest` | `rechazaDireccionSinCamposObligatorios` | Dominio | AB#105 | PR #161 | ✅ |
| HU5-CP03 | `DatosDireccionTest` | `rechazaDireccionSinCodigoPostalOTelefono` | Dominio | AB#105 | PR #161 | ✅ |
| HU5-CP03 | `DatosDireccionTest` | `respetaLaLongitudMaximaDeCadaCampo` | Dominio | AB#105 | PR #161 | ✅ |
| HU5-CP03 | `DireccionEnvioTest` | `creaDireccionAsociadaAUnCliente` | Dominio | AB#105 | PR #161 | ✅ |
| HU5-CP03 | `DireccionEnvioTest` | `reconstruyeDireccionGuardadaConservandoLaPrincipal` | Dominio | AB#105 | PR #161 | ✅ |
| HU5-CP03 | `DireccionEnvioTest` | `rechazaDireccionSinClienteOSinDatos` | Dominio | AB#105 | PR #161 | ✅ |
| HU5-CP04 | `DireccionesClienteTest` | `anadeDireccionesHastaElLimite` | Dominio | AB#105 | PR #161 | ✅ |
| HU5-CP04 | `DireccionesClienteTest` | `rechazaSextaDireccion` | Dominio | AB#105 | PR #161 | ✅ |
| HU5-CP04 | `GestionDireccionesServiceTest` | `rechazaSextaDireccionYNoLaGuarda` | Aplicación | AB#105 | PR #161 | ✅ |
| HU5-CP05 | `DireccionEnvioTest` | `marcaYDesmarcaLaPrincipal` | Dominio | AB#106 | PR #161 | ✅ |
| HU5-CP05 | `DireccionesClienteTest` | `primeraDireccionQuedaPrincipal` | Dominio | AB#106 | PR #161 | ✅ |
| HU5-CP05 | `DireccionesClienteTest` | `direccionPosteriorNoCambiaLaPrincipal` | Dominio | AB#106 | PR #161 | ✅ |
| HU5-CP05 | `DireccionesClienteTest` | `marcarOtraDejaSoloUnaPrincipal` | Dominio | AB#106 | PR #161 | ✅ |
| HU5-CP05 | `DireccionesClienteTest` | `rechazaMarcarDireccionInexistente` | Dominio | AB#106 | PR #161 | ✅ |
| HU5-CP05 | `GestionDireccionesServiceTest` | `primeraDireccionAnadidaSeGuardaPrincipal` | Aplicación | AB#106 | PR #161 | ✅ |
| HU5-CP05 | `GestionDireccionesServiceTest` | `marcarPrincipalGuardaElCambio` | Aplicación | AB#106 | PR #161 | ✅ |
| HU5-CP05 | `GestionDireccionesServiceTest` | `rechazaMarcarDireccionDeOtroCliente` | Aplicación | AB#106 | PR #161 | ✅ |
| HU5-CP06 | `DireccionesClienteTest` | `eliminaDireccionNoPrincipal` | Dominio | AB#106 | PR #161 | ✅ |
| HU5-CP06 | `DireccionesClienteTest` | `eliminarPrincipalExigeElegirOtra` | Dominio | AB#106 | PR #161 | ✅ |
| HU5-CP06 | `DireccionesClienteTest` | `eliminarPrincipalYElegirOtra` | Dominio | AB#106 | PR #161 | ✅ |
| HU5-CP06 | `DireccionesClienteTest` | `rechazaElegirComoSustitutaLaDireccionEliminada` | Dominio | AB#106 | PR #161 | ✅ |
| HU5-CP06 | `DireccionesClienteTest` | `rechazaSustitutaDeOtroCliente` | Dominio | AB#106 | PR #161 | ✅ |
| HU5-CP06 | `DireccionesClienteTest` | `eliminarLaUnicaDireccionDejaListaVacia` | Dominio | AB#106 | PR #161 | ✅ |
| HU5-CP06 | `DireccionesClienteTest` | `rechazaEliminarDireccionInexistente` | Dominio | AB#106 | PR #161 | ✅ |
| HU5-CP06 | `GestionDireccionesServiceTest` | `eliminarDireccionLaBorraDelRepositorio` | Aplicación | AB#106 | PR #161 | ✅ |
| HU5-CP06 | `GestionDireccionesServiceTest` | `eliminarPrincipalGuardaLaNuevaPrincipal` | Aplicación | AB#106 | PR #161 | ✅ |
| HU5-CP06 | `GestionDireccionesServiceTest` | `eliminarPrincipalSinElegirNoBorraNada` | Aplicación | AB#106 | PR #161 | ✅ |
| HU5-CP06 | `GestionDireccionesServiceTest` | `rechazaEliminarDireccionDeOtroCliente` | Aplicación | AB#106 | PR #161 | ✅ |
| HU5-CP07 | `DireccionEnvioTest` | `editarCambiaLosDatosYConservaLoDemas` | Dominio | AB#105 | PR #161 | ✅ |
| HU5-CP07 | `DireccionEnvioTest` | `rechazaEditarSinDatos` | Dominio | AB#105 | PR #161 | ✅ |
| HU5-CP07 | `DireccionesClienteTest` | `editarDireccionDelCliente` | Dominio | AB#105 | PR #161 | ✅ |
| HU5-CP07 | `DireccionesClienteTest` | `rechazaEditarDireccionInexistente` | Dominio | AB#105 | PR #161 | ✅ |
| HU5-CP07 | `GestionDireccionesServiceTest` | `editarGuardaLosDatosNuevos` | Aplicación | AB#105 | PR #161 | ✅ |
| HU5-CP07 | `GestionDireccionesServiceTest` | `rechazaEditarConDatosInvalidos` | Aplicación | AB#105 | PR #161 | ✅ |
| HU5-CP07 | `GestionDireccionesServiceTest` | `rechazaEditarDireccionDeOtroCliente` | Aplicación | AB#105 | PR #161 | ✅ |
| HU5-CP08 | `GestionDireccionesServiceTest` | `anadirGuardaLaDireccionDelClienteEnSesion` | Aplicación | AB#105 | PR #161 | ✅ |
| HU5-CP08 | `GestionDireccionesServiceTest` | `rechazaAnadirDireccionInvalida` | Aplicación | AB#105 | PR #161 | ✅ |
| HU5-CP08 | `GestionDireccionesServiceTest` | `listaSoloLasDireccionesDelClienteEnSesion` | Aplicación | AB#105 | PR #161 | ✅ |
| HU5-CP09 | `DireccionDocumentMapperTest` | `convierteADocumentoYVueltaSinPerderDatos` | Infraestructura | AB#105 | PR #161 | ✅ |
| HU5-CP09 | `DireccionDocumentMapperTest` | `rechazaDocumentoQueNoCumpleElDominio` | Infraestructura | AB#105 | PR #161 | ✅ |
| HU5-CP10 | `DireccionMongoAdapterTest` | `guardarDireccionNuevaLeAsignaId` | Infraestructura | AB#105 | PR #161 | ✅ |
| HU5-CP10 | `DireccionMongoAdapterTest` | `buscarPorClienteDevuelveSoloLasSuyas` | Infraestructura | AB#105 | PR #161 | ✅ |
| HU5-CP10 | `DireccionMongoAdapterTest` | `actualizarConservaLaFechaDeCreacion` | Infraestructura | AB#105 | PR #161 | ✅ |
| HU5-CP10 | `DireccionMongoAdapterTest` | `eliminarBorraLaDireccion` | Infraestructura | AB#105 | PR #161 | ✅ |
| HU5-CP10 | `DireccionMongoAdapterTest` | `laColeccionRechazaDocumentoIncompleto` | Infraestructura | AB#105 | PR #161 | ✅ |
| HU5-CP10 | `DireccionMongoAdapterTest` | `laColeccionRechazaFormatosInvalidos` | Infraestructura | AB#105 | PR #161 | ✅ |
| HU5-CP11 | `ClienteEnSesionAdapterTest` | `idPorCorreoPreguntaAlModuloDeUsuarios` | Infraestructura | AB#105 | PR #161 | ✅ |
| HU5-CP12 | `DireccionControllerTest` | `listarDevuelveLasDireccionesConLaPrincipal` | Infraestructura | AB#105 | PR #161 | ✅ |
| HU5-CP13 | `DireccionControllerTest` | `anadirDevuelve201` | Infraestructura | AB#105 | PR #161 | ✅ |
| HU5-CP13 | `DireccionControllerTest` | `direccionInvalidaDevuelve400SinRepetirLosDatos` | Infraestructura | AB#105 | PR #161 | ✅ |
| HU5-CP13 | `DireccionControllerTest` | `superarElLimiteDevuelve409` | Infraestructura | AB#105 | PR #161 | ✅ |
| HU5-CP14 | `DireccionControllerTest` | `editarDevuelve200` | Infraestructura | AB#105 | PR #161 | ✅ |
| HU5-CP14 | `DireccionControllerTest` | `editarConDatosInvalidosDevuelve400` | Infraestructura | AB#105 | PR #161 | ✅ |
| HU5-CP14 | `DireccionControllerTest` | `editarDireccionAjenaDevuelve404` | Infraestructura | AB#105 | PR #161 | ✅ |
| HU5-CP15 | `DireccionControllerTest` | `marcarPrincipalDevuelve204` | Infraestructura | AB#105 | PR #161 | ✅ |
| HU5-CP15 | `DireccionControllerTest` | `marcarDireccionAjenaDevuelve404` | Infraestructura | AB#105 | PR #161 | ✅ |
| HU5-CP15 | `DireccionControllerTest` | `eliminarDevuelve204ConLaSustituta` | Infraestructura | AB#105 | PR #161 | ✅ |
| HU5-CP15 | `DireccionControllerTest` | `eliminarPrincipalSinSustitutaDevuelve409` | Infraestructura | AB#105 | PR #161 | ✅ |
| HU5-CP15 | `DireccionControllerTest` | `eliminarDireccionAjenaDevuelve404` | Infraestructura | AB#105 | PR #161 | ✅ |
| HU5-CP16 | `DireccionControllerTest` | `sinSesionNoSeAccede` | Infraestructura | AB#105 | PR #161 | ✅ |
| HU5-CP16 | `DireccionControllerTest` | `unUsuarioQueNoEsClienteNoAccede` | Infraestructura | AB#105 | PR #161 | ✅ |
| HU5-CP16 | `DireccionControllerTest` | `sinTokenCsrfSeRechaza` | Infraestructura | AB#105 | PR #161 | ✅ |
| HU5-CP17 | `addresses.test.js` | `HU5-CP17: getAddresses pide las direcciones del cliente en sesión con la marca de la principal` | Frontend · API | AB#109 | PR #163 | ✅ |
| HU5-CP17 | `addresses.test.js` | `HU5-CP17: addAddress envía los datos de la dirección con un POST` | Frontend · API | AB#109 | PR #163 | ✅ |
| HU5-CP17 | `addresses.test.js` | `HU5-CP17: updateAddress envía los datos nuevos con un PUT a /{id}` | Frontend · API | AB#109 | PR #163 | ✅ |
| HU5-CP17 | `addresses.test.js` | `HU5-CP17: setPrincipalAddress marca la dirección con un PUT a /{id}/principal` | Frontend · API | AB#109 | PR #163 | ✅ |
| HU5-CP17 | `addresses.test.js` | `HU5-CP17: deleteAddress elimina la dirección y envía la sustituta solo si se indica` | Frontend · API | AB#109 | PR #163 | ✅ |
| HU5-CP17 | `addresses.test.js` | `HU5-CP17: un error del backend llega como ApiError con su código` | Frontend · API | AB#109 | PR #163 | ✅ |
| HU5-CP18 | `AddressesPage.test.jsx` | `HU5-CP18: muestra cada dirección con su destinatario, la dirección completa y el teléfono` | Frontend · página | AB#109 | PR #163 | ✅ |
| HU5-CP18 | `AddressesPage.test.jsx` | `HU5-CP18: indica cuál es la dirección principal` | Frontend · página | AB#109 | PR #163 | ✅ |
| HU5-CP18 | `AddressesPage.test.jsx` | `HU5-CP18: sin direcciones muestra un estado vacío con el botón para añadir la primera` | Frontend · página | AB#109 | PR #163 | ✅ |
| HU5-CP18 | `AddressesPage.test.jsx` | `HU5-CP18: si falla la carga avisa y deja reintentar` | Frontend · página | AB#109 | PR #163 | ✅ |
| HU5-CP19 | `AddressesPage.test.jsx` | `HU5-CP19: el cliente tiene el enlace «Direcciones» en la cabecera y llega a la vista` | Frontend · página | AB#109 | PR #163 | ✅ |
| HU5-CP19 | `AddressesPage.test.jsx` | `HU5-CP19: un vendedor o un administrador no acceden a la vista (vendedor@esibuy.test)` | Frontend · página | AB#109 | PR #163 | ✅ |
| HU5-CP19 | `AddressesPage.test.jsx` | `HU5-CP19: un vendedor o un administrador no acceden a la vista (admin@esibuy.test)` | Frontend · página | AB#109 | PR #163 | ✅ |
| HU5-CP20 | `AddressesPage.test.jsx` | `HU5-CP20: marcar otra dirección como principal la cambia en la lista` | Frontend · página | AB#109 | PR #163 | ✅ |
| HU5-CP20 | `AddressesPage.test.jsx` | `HU5-CP20: si la dirección ya no existe avisa y recarga la lista` | Frontend · página | AB#109 | PR #163 | ✅ |
| HU5-CP21 | `AddressesPage.test.jsx` | `HU5-CP21: con 5 direcciones no deja añadir otra y explica por qué` | Frontend · página | AB#109 | PR #163 | ✅ |
| HU5-CP21 | `AddressesPage.test.jsx` | `HU5-CP21: con menos de 5 direcciones se puede añadir otra` | Frontend · página | AB#109 | PR #163 | ✅ |
| HU5-CP22 | `AddressFormPanel.test.jsx` | `HU5-CP22: no envía una dirección sin los campos obligatorios y marca cada uno` | Frontend · componente | AB#109 | PR #163 | ✅ |
| HU5-CP22 | `AddressFormPanel.test.jsx` | `HU5-CP22: no envía un código postal, un teléfono o un texto con formato o longitud incorrectos` | Frontend · componente | AB#109 | PR #163 | ✅ |
| HU5-CP23 | `AddressesPage.test.jsx` | `HU5-CP23: al guardar una dirección nueva se cierra el panel, se avisa y aparece en la lista` | Frontend · página | AB#109 | PR #163 | ✅ |
| HU5-CP23 | `AddressesPage.test.jsx` | `HU5-CP23: al cancelar se cierra el panel sin añadir nada` | Frontend · página | AB#109 | PR #163 | ✅ |
| HU5-CP23 | `AddressFormPanel.test.jsx` | `HU5-CP23: empieza vacío y guarda la dirección nueva con un POST, sin espacios sobrantes` | Frontend · componente | AB#109 | PR #163 | ✅ |
| HU5-CP23 | `AddressFormPanel.test.jsx` | `HU5-CP23: al cancelar no se envía nada` | Frontend · componente | AB#109 | PR #163 | ✅ |
| HU5-CP24 | `AddressesPage.test.jsx` | `HU5-CP24: al editar una dirección se guardan los cambios y la lista los muestra` | Frontend · página | AB#109 | PR #163 | ✅ |
| HU5-CP24 | `AddressFormPanel.test.jsx` | `HU5-CP24: al editar empieza con los datos de la dirección y guarda los cambios con un PUT` | Frontend · componente | AB#109 | PR #163 | ✅ |
| HU5-CP24 | `AddressFormPanel.test.jsx` | `HU5-CP24: si la dirección que se edita ya no existe, lo avisa` | Frontend · componente | AB#109 | PR #163 | ✅ |
| HU5-CP25 | `AddressFormPanel.test.jsx` | `HU5-CP25: una dirección rechazada por el backend muestra un aviso sin repetir los datos enviados` | Frontend · componente | AB#109 | PR #163 | ✅ |
| HU5-CP25 | `AddressFormPanel.test.jsx` | `HU5-CP25: al superar el límite de direcciones avisa de que ya hay 5` | Frontend · componente | AB#109 | PR #163 | ✅ |
| HU5-CP26 | `DeleteAddressDialog.test.jsx` | `HU5-CP26: elimina una dirección que no es la principal tras confirmarlo` | Frontend · componente | AB#109 | PR #163 | ✅ |
| HU5-CP26 | `DeleteAddressDialog.test.jsx` | `HU5-CP26: al cancelar no elimina nada` | Frontend · componente | AB#109 | PR #163 | ✅ |
| HU5-CP27 | `AddressesPage.test.jsx` | `HU5-CP27: al eliminar la principal eligiendo otra, desaparece de la lista y la elegida pasa a serlo` | Frontend · página | AB#109 | PR #163 | ✅ |
| HU5-CP27 | `DeleteAddressDialog.test.jsx` | `HU5-CP27: para eliminar la principal hay que elegir otra dirección como principal` | Frontend · componente | AB#109 | PR #163 | ✅ |
| HU5-CP27 | `DeleteAddressDialog.test.jsx` | `HU5-CP27: la única dirección se elimina sin elegir sustituta` | Frontend · componente | AB#109 | PR #163 | ✅ |
| HU5-CP28 | `DeleteAddressDialog.test.jsx` | `HU5-CP28: si la eliminación falla avisa y no cierra el diálogo` | Frontend · componente | AB#109 | PR #163 | ✅ |
| HU5-CP29 | `SeguridadDireccionesServiceTest` | `unVendedorNoUsaLasDirecciones` | Aplicación | — | PR #168 | ✅ |
| HU5-CP29 | `SeguridadDireccionesServiceTest` | `unAdministradorNoUsaLasDirecciones` | Aplicación | — | PR #168 | ✅ |
| HU5-CP29 | `SeguridadDireccionesServiceTest` | `unClienteNoOperaConElCorreoDeOtro` | Aplicación | — | PR #168 | ✅ |
| HU5-CP29 | `SeguridadDireccionesServiceTest` | `unClienteOperaConSuPropioCorreo` | Aplicación | — | PR #168 | ✅ |
| HU5-CP29 | `SeguridadDireccionesServiceTest` | `sinSesionSeRechazaTodo` | Aplicación | — | PR #168 | ✅ |

---

## HU6 · Gestión de métodos de pago

### Tabla A · HU6 ↔ casos de prueba

| Caso | Descripción | Tipo | Tests | Estado |
|---|---|---|---|---|
| HU6-CP01 | El número de tarjeta solo admite dígitos, de 13 a 19, y tiene que superar el algoritmo de Luhn; rechaza uno nulo o vacío | Unitario | 6 | ✅ Pasa |
| HU6-CP02 | La caducidad tiene formato `MM/AA` con el mes entre 01 y 12, y la tarjeta es válida durante todo su mes de caducidad | Unitario | 6 | ✅ Pasa |
| HU6-CP03 | El titular es obligatorio, tiene como máximo 26 caracteres y solo admite letras, espacios, guion y apóstrofo | Unitario | 4 | ✅ Pasa |
| HU6-CP04 | La URL de PayPal tiene que ser una URL con esquema y servidor | Unitario | 3 | ✅ Pasa |
| HU6-CP05 | El teléfono de Bizum tiene 9 dígitos, sin prefijo | Unitario | 4 | ✅ Pasa |
| HU6-CP06 | Cada método se muestra solo con su máscara: últimos 4 dígitos de la tarjeta, dominio de la URL de PayPal y últimos 3 dígitos del teléfono de Bizum | Unitario | 3 | ✅ Pasa |
| HU6-CP07 | Un método de pago exige cliente, tipo, máscara y bloque cifrado, y se reconstruye conservando si es predeterminado | Unitario | 3 | ✅ Pasa |
| HU6-CP08 | Un cliente tiene como máximo 5 métodos de pago; el sexto se rechaza y no se guarda | Unitario | 3 | ✅ Pasa |
| HU6-CP09 | Solo hay un método predeterminado: el primero lo es, marcar otro desmarca el anterior y no se puede marcar uno de otro cliente | Unitario | 10 | ✅ Pasa |
| HU6-CP10 | Al eliminar el predeterminado el cliente elige el sustituto (no el mismo); el único método se elimina sin elegir; no se elimina uno de otro cliente | Unitario | 10 | ✅ Pasa |
| HU6-CP11 | Al añadir una tarjeta se guarda su máscara y un solo bloque cifrado con número, titular y caducidad; nada queda en claro | Unitario | 3 | ✅ Pasa |
| HU6-CP12 | Se rechaza una tarjeta caducada respecto al mes actual; la que caduca este mes se acepta | Unitario | 2 | ✅ Pasa |
| HU6-CP13 | Se añaden métodos PayPal y Bizum con su máscara y su dato cifrado | Unitario | 2 | ✅ Pasa |
| HU6-CP14 | Se rechaza el alta si falta un dato del tipo elegido o falta el tipo | Unitario | 4 | ✅ Pasa |
| HU6-CP15 | El listado devuelve solo los métodos del cliente en sesión | Unitario | 1 | ✅ Pasa |
| HU6-CP16 | Los datos de pago se cifran con AES-256-GCM: IV aleatorio, solo la misma clave los descifra, se rechaza un cifrado manipulado y una clave nula, que no esté en Base64 o que no mida 32 bytes | Unitario | 9 | ✅ Pasa |
| HU6-CP17 | El método de pago se convierte a su documento de MongoDB y vuelve sin perder datos; un documento inválido se rechaza | Unitario | 2 | ✅ Pasa |
| HU6-CP18 | Persistencia en MongoDB: guardar, buscar por cliente, actualizar conservando `createdAt` y eliminar; no queda ningún dato de la tarjeta en claro y la colección rechaza un documento sin bloque cifrado | Integración | 6 | ✅ Pasa |
| HU6-CP19 | El id del cliente se pide al módulo de usuarios por su puerto de entrada | Unitario | 1 | ✅ Pasa |
| HU6-CP20 | `GET /api/customer/payment-methods` devuelve los métodos del cliente en sesión sin datos cifrados | Controlador | 1 | ✅ Pasa |
| HU6-CP21 | `POST`: 201 con el método creado; el CVV es obligatorio en tarjeta (3 o 4 dígitos) y no llega al caso de uso; dato inválido o tarjeta caducada 400 `VALIDATION_ERROR`; límite 409 `PAYMENT_METHOD_LIMIT_REACHED` | Controlador | 6 | ✅ Pasa |
| HU6-CP22 | `PUT /{id}/default` marca el predeterminado (204); uno ajeno o inexistente da 404 `PAYMENT_METHOD_NOT_FOUND` | Controlador | 2 | ✅ Pasa |
| HU6-CP23 | `DELETE /{id}` elimina (204) con el sustituto elegido; sin él, 409 `DEFAULT_PAYMENT_METHOD_REQUIRED`; uno ajeno o inexistente da 404 | Controlador | 3 | ✅ Pasa |
| HU6-CP24 | Solo un cliente con sesión accede, y las peticiones que modifican datos exigen CSRF | Controlador | 3 | ✅ Pasa |
| HU6-CP25 | El cliente de la API de métodos de pago: listar, añadir, marcar el predeterminado, eliminar con sustituto opcional y errores como `ApiError` con su código | Frontend · API | 5 | ✅ Pasa |
| HU6-CP26 | La vista lista cada método con su tipo y su máscara, marca el predeterminado, muestra un estado vacío y, si falla la carga, deja reintentar | Frontend (página) | 4 | ✅ Pasa |
| HU6-CP27 | El cliente llega desde el enlace «Métodos de pago» de la cabecera; el vendedor y el administrador no acceden | Frontend (página) | 3 | ✅ Pasa |
| HU6-CP28 | Marcar otro método como predeterminado lo cambia en la lista; si el método ya no existe, se avisa y se recarga | Frontend (página) | 2 | ✅ Pasa |
| HU6-CP29 | Con 5 métodos no se puede añadir otro y se explica por qué; con menos, sí | Frontend (página) | 2 | ✅ Pasa |
| HU6-CP30 | El panel de alta empieza con Tarjeta y sus campos; PayPal y Bizum muestran solo el suyo | Frontend (componente) | 2 | ✅ Pasa |
| HU6-CP31 | El panel no envía datos con formato incorrecto (número, caducidad, CVV, titular, URL de PayPal y teléfono de Bizum) | Frontend (componente) | 2 | ✅ Pasa |
| HU6-CP32 | Se envían solo los datos del tipo elegido; al cancelar se borran; desde la página, guardar cierra el panel, avisa y añade el método a la lista | Frontend (componente y página) | 4 | ✅ Pasa |
| HU6-CP33 | Un método rechazado por el backend se avisa sin repetir los datos enviados; el límite tiene su propio mensaje | Frontend (componente) | 2 | ✅ Pasa |
| HU6-CP34 | Eliminar un método tras confirmarlo; cancelar no elimina nada | Frontend (componente) | 2 | ✅ Pasa |
| HU6-CP35 | Para eliminar el predeterminado hay que elegir el sustituto, salvo si es el único; desde la página, el elegido pasa a ser el predeterminado | Frontend (componente y página) | 3 | ✅ Pasa |
| HU6-CP36 | Si la eliminación falla, se avisa sin cerrar el diálogo | Frontend (componente) | 1 | ✅ Pasa |
| HU6-CP37 | La gestión de métodos de pago exige el rol cliente y que el correo recibido sea el de la sesión; sin sesión se rechaza. Seguridad en la capa de negocio (R8) | Unitario con Spring Security (sin MongoDB) | 5 | 🔍 En revisión (5, rama `fix/seguridad-capa-negocio`) |

Notas:
- **HU6-CP01 a CP36:** casos propuestos y aprobados por el equipo durante el desarrollo de la HU6: el backend (CP01 a CP24, tasks 271, 110, 270 y 112) y la vista del frontend (CP25 a CP36, tasks 269, 113 y 114). Los casos de prueba no se registran en Azure Boards: viven en esta matriz y en el `@DisplayName` de cada test.
- **Decisiones del Product Owner y del equipo:** los datos de pago **sí se persisten**, cifrados con AES-256-GCM (sustituye al «solo tipo y máscara» de `docs/requisitos/sprint2-decisiones.md`); el **CVV nunca se guarda**, solo se valida en la petición; máximo 5 métodos por cliente; al eliminar el predeterminado, el cliente elige otro.
- **Endpoints (T110):** `GET`, `POST`, `PUT /{id}/default` y `DELETE /{id}?nuevoPredeterminado=` sobre `/api/customer/payment-methods`, solo para `CLIENTE` (`SecurityConfig`). El cliente sale de la sesión. La respuesta es `{id, tipo, mascara, predeterminado}`, nunca el bloque cifrado.
- **Vista del frontend (T269 y T113):** `/payment-methods`, solo para clientes. Detalle en `frontend/docs/hu6-metodos-de-pago.md`.
- **Pendiente:** confirmar con el equipo los códigos de error y sus mensajes en la vista. La clave de cifrado va en la variable de entorno `ESIBUY_PAGOS_CLAVE_CIFRADO`.
- **HU6-CP37 (R8, seguridad en la capa de negocio):** caso propuesto en la rama `fix/seguridad-capa-negocio`, sin task ni `AB#` por ser un fix transversal. Pendiente de confirmar con el equipo. Comprueba con `@PreAuthorize` que `GestionMetodosPagoUseCase` exige el rol cliente y que el correo recibido es el de la sesión. Clase de test nueva `metodospago/application/service/SeguridadMetodosPagoServiceTest` (contexto mínimo de Spring con `@EnableMethodSecurity`, sin MongoDB).

### Tabla B · casos de prueba ↔ tests

| Caso | Clase de test | Método | Capa | Task | Origen | Estado |
|---|---|---|---|---|---|---|
| HU6-CP01 | `NumeroTarjetaTest` | `creaNumeroDeTarjetaValido` | Dominio | AB#271 | PR #148 | ✅ |
| HU6-CP01 | `NumeroTarjetaTest` | `rechazaNumeroConLuhnIncorrecto` | Dominio | AB#271 | PR #148 | ✅ |
| HU6-CP01 | `NumeroTarjetaTest` | `rechazaNumeroConCaracteresNoNumericos` | Dominio | AB#271 | PR #148 | ✅ |
| HU6-CP01 | `NumeroTarjetaTest` | `rechazaNumeroDeDoceDigitos` | Dominio | AB#271 | PR #148 | ✅ |
| HU6-CP01 | `NumeroTarjetaTest` | `rechazaNumeroDeVeinteDigitos` | Dominio | AB#271 | PR #148 | ✅ |
| HU6-CP01 | `NumeroTarjetaTest` | `rechazaNumeroNuloOVacio` | Dominio | AB#271 | PR #148 | ✅ |
| HU6-CP02 | `CaducidadTarjetaTest` | `caducidadFuturaEstaVigente` | Dominio | AB#271 | PR #148 | ✅ |
| HU6-CP02 | `CaducidadTarjetaTest` | `caducidadDelMesActualEstaVigente` | Dominio | AB#271 | PR #148 | ✅ |
| HU6-CP02 | `CaducidadTarjetaTest` | `caducidadDelMesAnteriorNoEstaVigente` | Dominio | AB#271 | PR #148 | ✅ |
| HU6-CP02 | `CaducidadTarjetaTest` | `rechazaMesFueraDeRango` | Dominio | AB#271 | PR #148 | ✅ |
| HU6-CP02 | `CaducidadTarjetaTest` | `rechazaFormatoIncorrecto` | Dominio | AB#271 | PR #148 | ✅ |
| HU6-CP02 | `CaducidadTarjetaTest` | `rechazaCaducidadNulaOVacia` | Dominio | AB#271 | PR #148 | ✅ |
| HU6-CP03 | `TitularTarjetaTest` | `creaTitularValido` | Dominio | AB#271 | PR #148 | ✅ |
| HU6-CP03 | `TitularTarjetaTest` | `rechazaTitularVacioONulo` | Dominio | AB#271 | PR #148 | ✅ |
| HU6-CP03 | `TitularTarjetaTest` | `rechazaTitularDemasiadoLargo` | Dominio | AB#271 | PR #148 | ✅ |
| HU6-CP03 | `TitularTarjetaTest` | `rechazaTitularConDigitosOSimbolos` | Dominio | AB#271 | PR #148 | ✅ |
| HU6-CP04 | `UrlPayPalTest` | `creaUrlValida` | Dominio | AB#271 | PR #148 | ✅ |
| HU6-CP04 | `UrlPayPalTest` | `rechazaTextoQueNoEsUnaUrl` | Dominio | AB#271 | PR #148 | ✅ |
| HU6-CP04 | `UrlPayPalTest` | `rechazaUrlNulaOVacia` | Dominio | AB#271 | PR #148 | ✅ |
| HU6-CP05 | `TelefonoBizumTest` | `creaTelefonoDeNueveDigitos` | Dominio | AB#271 | PR #148 | ✅ |
| HU6-CP05 | `TelefonoBizumTest` | `rechazaTelefonoConLetras` | Dominio | AB#271 | PR #148 | ✅ |
| HU6-CP05 | `TelefonoBizumTest` | `rechazaTelefonoDeOchoDigitos` | Dominio | AB#271 | PR #148 | ✅ |
| HU6-CP05 | `TelefonoBizumTest` | `rechazaTelefonoNuloOVacio` | Dominio | AB#271 | PR #148 | ✅ |
| HU6-CP06 | `NumeroTarjetaTest` | `enmascaraTodoMenosLosUltimosCuatroDigitos` | Dominio | AB#271 | PR #148 | ✅ |
| HU6-CP06 | `TelefonoBizumTest` | `enmascaraTodoMenosLosUltimosTresDigitos` | Dominio | AB#271 | PR #148 | ✅ |
| HU6-CP06 | `UrlPayPalTest` | `enmascaraMostrandoSoloElDominio` | Dominio | AB#271 | PR #148 | ✅ |
| HU6-CP07 | `MetodoPagoTest` | `creaMetodoAsociadoAUnCliente` | Dominio | AB#271 | PR #148 | ✅ |
| HU6-CP07 | `MetodoPagoTest` | `reconstruyeMetodoGuardadoConservandoElPredeterminado` | Dominio | AB#271 | PR #148 | ✅ |
| HU6-CP07 | `MetodoPagoTest` | `rechazaMetodoConDatosObligatoriosAusentes` | Dominio | AB#271 | PR #148 | ✅ |
| HU6-CP08 | `GestionMetodosPagoServiceTest` | `rechazaSextoMetodoYNoLoGuarda` | Aplicación | AB#271 | PR #148 | ✅ |
| HU6-CP08 | `MetodosPagoClienteTest` | `anadeMetodosHastaElLimite` | Dominio | AB#271 | PR #148 | ✅ |
| HU6-CP08 | `MetodosPagoClienteTest` | `rechazaSextoMetodo` | Dominio | AB#271 | PR #148 | ✅ |
| HU6-CP09 | `GestionMetodosPagoServiceTest` | `primerMetodoAnadidoSeGuardaPredeterminado` | Aplicación | AB#271 | PR #148 | ✅ |
| HU6-CP09 | `GestionMetodosPagoServiceTest` | `marcarPredeterminadoGuardaElCambio` | Aplicación | AB#271 | PR #148 | ✅ |
| HU6-CP09 | `GestionMetodosPagoServiceTest` | `rechazaMarcarMetodoDeOtroCliente` | Aplicación | AB#271 | PR #148 | ✅ |
| HU6-CP09 | `MetodoPagoTest` | `nuevoMetodoNoEsPredeterminado` | Dominio | AB#271 | PR #148 | ✅ |
| HU6-CP09 | `MetodoPagoTest` | `marcaMetodoComoPredeterminado` | Dominio | AB#271 | PR #148 | ✅ |
| HU6-CP09 | `MetodoPagoTest` | `desmarcaMetodoPredeterminado` | Dominio | AB#271 | PR #148 | ✅ |
| HU6-CP09 | `MetodosPagoClienteTest` | `primerMetodoQuedaPredeterminado` | Dominio | AB#271 | PR #148 | ✅ |
| HU6-CP09 | `MetodosPagoClienteTest` | `metodoPosteriorNoCambiaElPredeterminado` | Dominio | AB#271 | PR #148 | ✅ |
| HU6-CP09 | `MetodosPagoClienteTest` | `marcarOtroLeDejaSoloUnPredeterminado` | Dominio | AB#271 | PR #148 | ✅ |
| HU6-CP09 | `MetodosPagoClienteTest` | `rechazaMarcarMetodoInexistente` | Dominio | AB#271 | PR #148 | ✅ |
| HU6-CP10 | `GestionMetodosPagoServiceTest` | `eliminarMetodoLoBorraDelRepositorio` | Aplicación | AB#271 | PR #148 | ✅ |
| HU6-CP10 | `GestionMetodosPagoServiceTest` | `eliminarPredeterminadoGuardaElNuevoPredeterminado` | Aplicación | AB#271 | PR #148 | ✅ |
| HU6-CP10 | `GestionMetodosPagoServiceTest` | `eliminarPredeterminadoSinElegirNoBorraNada` | Aplicación | AB#271 | PR #148 | ✅ |
| HU6-CP10 | `GestionMetodosPagoServiceTest` | `rechazaEliminarMetodoDeOtroCliente` | Aplicación | AB#271 | PR #148 | ✅ |
| HU6-CP10 | `MetodosPagoClienteTest` | `eliminaMetodoNoPredeterminado` | Dominio | AB#271 | PR #148 | ✅ |
| HU6-CP10 | `MetodosPagoClienteTest` | `eliminarPredeterminadoExigeElegirOtro` | Dominio | AB#271 | PR #148 | ✅ |
| HU6-CP10 | `MetodosPagoClienteTest` | `eliminarPredeterminadoYElegirOtro` | Dominio | AB#271 | PR #148 | ✅ |
| HU6-CP10 | `MetodosPagoClienteTest` | `rechazaElegirComoSustitutoElMetodoEliminado` | Dominio | AB#271 | PR #148 | ✅ |
| HU6-CP10 | `MetodosPagoClienteTest` | `eliminarElUnicoMetodoDejaListaVacia` | Dominio | AB#271 | PR #148 | ✅ |
| HU6-CP10 | `MetodosPagoClienteTest` | `rechazaEliminarMetodoInexistente` | Dominio | AB#271 | PR #148 | ✅ |
| HU6-CP11 | `GestionMetodosPagoServiceTest` | `anadirTarjetaGuardaMascaraYDatosCifrados` | Aplicación | AB#271 | PR #148 | ✅ |
| HU6-CP11 | `GestionMetodosPagoServiceTest` | `anadirTarjetaCifraNumeroTitularYCaducidadEnUnBloque` | Aplicación | AB#271 | PR #148 | ✅ |
| HU6-CP11 | `GestionMetodosPagoServiceTest` | `anadirTarjetaNoGuardaNumeroNiTitularEnClaro` | Aplicación | AB#271 | PR #148 | ✅ |
| HU6-CP12 | `GestionMetodosPagoServiceTest` | `rechazaTarjetaCaducada` | Aplicación | AB#271 | PR #148 | ✅ |
| HU6-CP12 | `GestionMetodosPagoServiceTest` | `aceptaTarjetaQueCaducaEsteMes` | Aplicación | AB#271 | PR #148 | ✅ |
| HU6-CP13 | `GestionMetodosPagoServiceTest` | `anadePayPalConSuMascara` | Aplicación | AB#271 | PR #148 | ✅ |
| HU6-CP13 | `GestionMetodosPagoServiceTest` | `anadeBizumConSuMascara` | Aplicación | AB#271 | PR #148 | ✅ |
| HU6-CP14 | `GestionMetodosPagoServiceTest` | `rechazaTarjetaConDatosIncompletos` | Aplicación | AB#271 | PR #148 | ✅ |
| HU6-CP14 | `GestionMetodosPagoServiceTest` | `rechazaPayPalSinUrl` | Aplicación | AB#271 | PR #148 | ✅ |
| HU6-CP14 | `GestionMetodosPagoServiceTest` | `rechazaBizumSinTelefono` | Aplicación | AB#271 | PR #148 | ✅ |
| HU6-CP14 | `GestionMetodosPagoServiceTest` | `rechazaMetodoSinTipo` | Aplicación | AB#271 | PR #148 | ✅ |
| HU6-CP15 | `GestionMetodosPagoServiceTest` | `listaSoloLosMetodosDelClienteEnSesion` | Aplicación | AB#271 | PR #148 | ✅ |
| HU6-CP16 | `AesGcmCifradoDatosPagoAdapterTest` | `cifraConLaClaveConfigurada` | Infraestructura | AB#271 | PR #148 | ✅ |
| HU6-CP16 | `AesGcmCifradoDatosPagoAdapterTest` | `rechazaClaveNulaOVacia` | Infraestructura | AB#271 | PR #148 | ✅ |
| HU6-CP16 | `AesGcmCifradoDatosPagoAdapterTest` | `rechazaClaveQueNoEsBase64` | Infraestructura | AB#271 | PR #148 | ✅ |
| HU6-CP16 | `AesGcmCifradoDatosPagoAdapterTest` | `rechazaClaveQueNoMide32Bytes` | Infraestructura | AB#271 | PR #148 | ✅ |
| HU6-CP16 | `CifradorAesGcmTest` | `conLaMismaClaveSeRecuperanLosDatos` | Infraestructura | AB#271 | PR #148 | ✅ |
| HU6-CP16 | `CifradorAesGcmTest` | `elCifradoNoContieneLosDatos` | Infraestructura | AB#271 | PR #148 | ✅ |
| HU6-CP16 | `CifradorAesGcmTest` | `cadaCifradoEsDistinto` | Infraestructura | AB#271 | PR #148 | ✅ |
| HU6-CP16 | `CifradorAesGcmTest` | `conOtraClaveNoSeDescifran` | Infraestructura | AB#271 | PR #148 | ✅ |
| HU6-CP16 | `CifradorAesGcmTest` | `unCifradoManipuladoSeRechaza` | Infraestructura | AB#271 | PR #148 | ✅ |
| HU6-CP17 | `MetodoPagoDocumentMapperTest` | `convierteAdocumentoYVueltaSinPerderDatos` | Infraestructura | AB#271 | PR #148 | ✅ |
| HU6-CP17 | `MetodoPagoDocumentMapperTest` | `rechazaDocumentoQueNoCumpleElDominio` | Infraestructura | AB#271 | PR #148 | ✅ |
| HU6-CP18 | `MetodoPagoMongoAdapterTest` | `guardarMetodoNuevoLeAsignaId` | Infraestructura | AB#271 | PR #148 | ✅ |
| HU6-CP18 | `MetodoPagoMongoAdapterTest` | `buscarPorClienteDevuelveSoloLosSuyos` | Infraestructura | AB#271 | PR #148 | ✅ |
| HU6-CP18 | `MetodoPagoMongoAdapterTest` | `actualizarConservaLaFechaDeCreacion` | Infraestructura | AB#271 | PR #148 | ✅ |
| HU6-CP18 | `MetodoPagoMongoAdapterTest` | `eliminarBorraElMetodo` | Infraestructura | AB#271 | PR #148 | ✅ |
| HU6-CP18 | `MetodoPagoMongoAdapterTest` | `enMongoNoQuedaNingunDatoEnClaro` | Infraestructura | AB#271 | PR #148 | ✅ |
| HU6-CP18 | `MetodoPagoMongoAdapterTest` | `laColeccionRechazaDocumentoSinDatosCifrados` | Infraestructura | AB#271 | PR #148 | ✅ |
| HU6-CP19 | `ClienteEnSesionAdapterTest` | `idPorCorreoPreguntaAlModuloDeUsuarios` | Infraestructura | AB#271 | PR #148 | ✅ |
| HU6-CP20 | `MetodoPagoControllerTest` | `listarDevuelveLosMetodosSinDatosCifrados` | Infraestructura | AB#110 | PR #148 | ✅ |
| HU6-CP21 | `MetodoPagoControllerTest` | `anadirTarjetaDevuelve201` | Infraestructura | AB#110 | PR #148 | ✅ |
| HU6-CP21 | `MetodoPagoControllerTest` | `elCvvNoLlegaAlCasoDeUso` | Infraestructura | AB#110 | PR #148 | ✅ |
| HU6-CP21 | `MetodoPagoControllerTest` | `tarjetaConCvvAusenteOInvalidoDevuelve400` | Infraestructura | AB#110 | PR #148 | ✅ |
| HU6-CP21 | `MetodoPagoControllerTest` | `metodoSinTipoDevuelve400` | Infraestructura | AB#110 | PR #148 | ✅ |
| HU6-CP21 | `MetodoPagoControllerTest` | `datoInvalidoDevuelve400SinRepetirLosDatos` | Infraestructura | AB#110 | PR #148 | ✅ |
| HU6-CP21 | `MetodoPagoControllerTest` | `superarElLimiteDevuelve409` | Infraestructura | AB#110 | PR #148 | ✅ |
| HU6-CP22 | `MetodoPagoControllerTest` | `marcarPredeterminadoDevuelve204` | Infraestructura | AB#110 | PR #148 | ✅ |
| HU6-CP22 | `MetodoPagoControllerTest` | `marcarMetodoAjenoDevuelve404` | Infraestructura | AB#110 | PR #148 | ✅ |
| HU6-CP23 | `MetodoPagoControllerTest` | `eliminarDevuelve204ConElSustituto` | Infraestructura | AB#110 | PR #148 | ✅ |
| HU6-CP23 | `MetodoPagoControllerTest` | `eliminarPredeterminadoSinSustitutoDevuelve409` | Infraestructura | AB#110 | PR #148 | ✅ |
| HU6-CP23 | `MetodoPagoControllerTest` | `eliminarMetodoAjenoDevuelve404` | Infraestructura | AB#110 | PR #148 | ✅ |
| HU6-CP24 | `MetodoPagoControllerTest` | `sinSesionNoSeAccede` | Infraestructura | AB#110 | PR #148 | ✅ |
| HU6-CP24 | `MetodoPagoControllerTest` | `unUsuarioQueNoEsClienteNoAccede` | Infraestructura | AB#110 | PR #148 | ✅ |
| HU6-CP24 | `MetodoPagoControllerTest` | `sinTokenCsrfSeRechaza` | Infraestructura | AB#110 | PR #148 | ✅ |
| HU6-CP25 | `paymentMethods.test.js` | `HU6-CP25: getPaymentMethods pide la lista de métodos del cliente en sesión` | Frontend · API | AB#269 | PR #151 | ✅ |
| HU6-CP25 | `paymentMethods.test.js` | `HU6-CP25: addPaymentMethod envía los datos del método con un POST` | Frontend · API | AB#269 | PR #151 | ✅ |
| HU6-CP25 | `paymentMethods.test.js` | `HU6-CP25: setDefaultPaymentMethod marca el método con un PUT a /{id}/default` | Frontend · API | AB#269 | PR #151 | ✅ |
| HU6-CP25 | `paymentMethods.test.js` | `HU6-CP25: deletePaymentMethod elimina el método y envía el sustituto solo si se indica` | Frontend · API | AB#269 | PR #151 | ✅ |
| HU6-CP25 | `paymentMethods.test.js` | `HU6-CP25: un error del backend llega como ApiError con su código` | Frontend · API | AB#269 | PR #151 | ✅ |
| HU6-CP26 | `PaymentMethodsPage.test.jsx` | `HU6-CP26: muestra cada método con su tipo y su máscara, sin otros datos de pago` | Frontend · página | AB#269 | PR #151 | ✅ |
| HU6-CP26 | `PaymentMethodsPage.test.jsx` | `HU6-CP26: indica cuál es el método predeterminado` | Frontend · página | AB#269 | PR #151 | ✅ |
| HU6-CP26 | `PaymentMethodsPage.test.jsx` | `HU6-CP26: sin métodos muestra un estado vacío con el botón para añadir el primero` | Frontend · página | AB#269 | PR #151 | ✅ |
| HU6-CP26 | `PaymentMethodsPage.test.jsx` | `HU6-CP26: si falla la carga avisa y deja reintentar` | Frontend · página | AB#269 | PR #151 | ✅ |
| HU6-CP27 | `PaymentMethodsPage.test.jsx` | `HU6-CP27: el cliente tiene el enlace «Métodos de pago» en la cabecera y llega a la vista` | Frontend · página | AB#269 | PR #151 | ✅ |
| HU6-CP27 | `PaymentMethodsPage.test.jsx` | `HU6-CP27: un vendedor o un administrador no acceden a la vista (vendedor@esibuy.test)` | Frontend · página | AB#269 | PR #151 | ✅ |
| HU6-CP27 | `PaymentMethodsPage.test.jsx` | `HU6-CP27: un vendedor o un administrador no acceden a la vista (admin@esibuy.test)` | Frontend · página | AB#269 | PR #151 | ✅ |
| HU6-CP28 | `PaymentMethodsPage.test.jsx` | `HU6-CP28: marcar otro método como predeterminado lo cambia en la lista` | Frontend · página | AB#269 | PR #151 | ✅ |
| HU6-CP28 | `PaymentMethodsPage.test.jsx` | `HU6-CP28: si el método ya no existe avisa y recarga la lista` | Frontend · página | AB#269 | PR #151 | ✅ |
| HU6-CP29 | `PaymentMethodsPage.test.jsx` | `HU6-CP29: con 5 métodos no deja añadir otro y explica por qué` | Frontend · página | AB#269 | PR #151 | ✅ |
| HU6-CP29 | `PaymentMethodsPage.test.jsx` | `HU6-CP29: con menos de 5 métodos se puede añadir otro` | Frontend · página | AB#269 | PR #151 | ✅ |
| HU6-CP30 | `AddPaymentMethodPanel.test.jsx` | `HU6-CP30: empieza con Tarjeta elegida y sus campos: número, caducidad, CVV y titular` | Frontend · componente | AB#269 | PR #151 | ✅ |
| HU6-CP30 | `AddPaymentMethodPanel.test.jsx` | `HU6-CP30: al elegir PayPal o Bizum muestra solo su campo` | Frontend · componente | AB#269 | PR #151 | ✅ |
| HU6-CP31 | `AddPaymentMethodPanel.test.jsx` | `HU6-CP31: no envía una tarjeta con el número, la caducidad, el CVV o el titular con formato incorrecto` | Frontend · componente | AB#269 | PR #151 | ✅ |
| HU6-CP31 | `AddPaymentMethodPanel.test.jsx` | `HU6-CP31: no envía una URL de PayPal o un teléfono de Bizum con formato incorrecto` | Frontend · componente | AB#269 | PR #151 | ✅ |
| HU6-CP32 | `PaymentMethodsPage.test.jsx` | `HU6-CP32: al guardar un método se cierra el panel, se avisa y aparece en la lista` | Frontend · página | AB#269 | PR #151 | ✅ |
| HU6-CP32 | `PaymentMethodsPage.test.jsx` | `HU6-CP32: al cancelar se cierra el panel sin añadir nada` | Frontend · página | AB#269 | PR #151 | ✅ |
| HU6-CP32 | `AddPaymentMethodPanel.test.jsx` | `HU6-CP32: guarda solo los datos del tipo elegido y avisa de que se ha añadido` | Frontend · componente | AB#269 | PR #151 | ✅ |
| HU6-CP32 | `AddPaymentMethodPanel.test.jsx` | `HU6-CP32: al cancelar o cerrar el panel se borran los datos escritos` | Frontend · componente | AB#269 | PR #151 | ✅ |
| HU6-CP33 | `AddPaymentMethodPanel.test.jsx` | `HU6-CP33: un método rechazado por el backend muestra un aviso sin repetir los datos enviados` | Frontend · componente | AB#269 | PR #151 | ✅ |
| HU6-CP33 | `AddPaymentMethodPanel.test.jsx` | `HU6-CP33: al superar el límite de métodos avisa de que ya hay 5` | Frontend · componente | AB#269 | PR #151 | ✅ |
| HU6-CP34 | `DeletePaymentMethodDialog.test.jsx` | `HU6-CP34: elimina un método que no es el predeterminado tras confirmarlo` | Frontend · componente | AB#269 | PR #151 | ✅ |
| HU6-CP34 | `DeletePaymentMethodDialog.test.jsx` | `HU6-CP34: al cancelar no elimina nada` | Frontend · componente | AB#269 | PR #151 | ✅ |
| HU6-CP35 | `PaymentMethodsPage.test.jsx` | `HU6-CP35: al eliminar el predeterminado eligiendo otro, desaparece de la lista y el elegido pasa a serlo` | Frontend · página | AB#269 | PR #151 | ✅ |
| HU6-CP35 | `DeletePaymentMethodDialog.test.jsx` | `HU6-CP35: para eliminar el predeterminado hay que elegir otro método como predeterminado` | Frontend · componente | AB#269 | PR #151 | ✅ |
| HU6-CP35 | `DeletePaymentMethodDialog.test.jsx` | `HU6-CP35: el único método se elimina sin elegir sustituto` | Frontend · componente | AB#269 | PR #151 | ✅ |
| HU6-CP36 | `DeletePaymentMethodDialog.test.jsx` | `HU6-CP36: si la eliminación falla avisa y no cierra el diálogo` | Frontend · componente | AB#269 | PR #151 | ✅ |
| HU6-CP37 | `SeguridadMetodosPagoServiceTest` | `unVendedorNoUsaLosMetodosDePago` | Aplicación | — | Rama `fix/seguridad-capa-negocio` | 🔍 |
| HU6-CP37 | `SeguridadMetodosPagoServiceTest` | `unAdministradorNoUsaLosMetodosDePago` | Aplicación | — | Rama `fix/seguridad-capa-negocio` | 🔍 |
| HU6-CP37 | `SeguridadMetodosPagoServiceTest` | `unClienteNoOperaConElCorreoDeOtro` | Aplicación | — | Rama `fix/seguridad-capa-negocio` | 🔍 |
| HU6-CP37 | `SeguridadMetodosPagoServiceTest` | `unClienteOperaConSuPropioCorreo` | Aplicación | — | Rama `fix/seguridad-capa-negocio` | 🔍 |
| HU6-CP37 | `SeguridadMetodosPagoServiceTest` | `sinSesionSeRechazaTodo` | Aplicación | — | Rama `fix/seguridad-capa-negocio` | 🔍 |

Rutas de las clases de test (bajo `backend/src/test/java/es/uclm/esibuy/backend/`):
- `NumeroTarjetaTest`, `CaducidadTarjetaTest`, `TitularTarjetaTest`, `UrlPayPalTest`, `TelefonoBizumTest`, `MetodoPagoTest` y `MetodosPagoClienteTest`: `metodospago/domain/model/` (solo JUnit)
- `GestionMetodosPagoServiceTest`: `metodospago/application/service/` (dobles de prueba escritos a mano, sin Spring)
- `CifradorAesGcmTest`: `shared/infrastructure/seguridad/` (lo usan también el secreto TOTP de la HU2)
- `AesGcmCifradoDatosPagoAdapterTest`: `metodospago/infrastructure/adapter/out/seguridad/`
- `MetodoPagoDocumentMapperTest` y `MetodoPagoMongoAdapterTest`: `metodospago/infrastructure/adapter/out/persistence/` (el adaptador, con Testcontainers)
- `ClienteEnSesionAdapterTest`: `metodospago/infrastructure/adapter/out/usuarios/` (Mockito)
- `MetodoPagoControllerTest`: `metodospago/infrastructure/adapter/in/web/` (con `@WebMvcTest` y la seguridad real)

Tests del frontend (bajo `frontend/src/`, con Vitest, React Testing Library y MSW):
- `paymentMethods.test.js`: `api/`
- `PaymentMethodsPage.test.jsx`: `pages/` (la aplicación entera con la sesión del mock)
- `AddPaymentMethodPanel.test.jsx` y `DeletePaymentMethodDialog.test.jsx`: `features/paymentMethods/`

---

## HU7 · Búsqueda y filtrado de productos

### Tabla A · HU7 ↔ casos de prueba

| Caso | Descripción | Tipo | Tests | Estado |
|---|---|---|---|---|
| HU7-CP01 | La vista de productos es lo primero que ve el cliente al iniciar sesión, y solo la ve un cliente (sin sesión lleva al login; otro rol, a su página) | Frontend (página) | 3 | ✅ Pasa (3, AB#350, PR #131) |
| HU7-CP02 | Cada producto se muestra en una tarjeta con su nombre, imagen, categoría y valoración (estrellas y número de valoraciones; con avisos si no tiene imagen o valoraciones) | Frontend (componente y página) | 6 | ✅ Pasa (6, AB#350, PR #131) |
| HU7-CP03 | Las tarjetas se colocan de dos en dos y de diez en diez: al llegar al final de la página (o con «Ver más productos») se cargan otras diez, hasta avisar de que no hay más; con estados de carga, error con reintento y catálogo vacío | Frontend (página) | 8 | ✅ Pasa (8, AB#350, PR #131) |
| HU7-CP04 | Se puede buscar por nombre (sin distinguir mayúsculas ni tildes) y filtrar por categoría; cualquier cambio empieza de nuevo desde la primera página, y si nada coincide se ofrece limpiar los filtros | Frontend (página) | 5 | ✅ Pasa (5, AB#351, PR #131; 2 adaptados en el PR #143, ver nota) |
| HU7-CP05 | Se puede filtrar por valoración mínima (4, 3 o 2 estrellas o más) y volver a «Cualquiera» | Frontend (página) | 3 | ✅ Pasa (3, AB#351, PR #131) |
| HU7-CP06 | Se puede ordenar por nombre y por valoración (no por categoría), ascendente y descendente; por defecto, nombre de la A a la Z | Frontend (página) | 5 | ✅ Pasa (5, AB#351, PR #131) |
| HU7-CP07 | Arriba se muestran los diez productos mejor valorados como tarjetas (puesto, nombre y valoración) dentro de una banda curva sin marco; se ocultan con filtros activos, con pocos productos valorados o si falla | Frontend (componente y página) | 9 | ✅ Pasa (9, AB#350, PR #131) |
| HU7-CP08 | Las tarjetas de la banda animada de los mejores se pueden pulsar con el ratón y avisan con su producto; sin esa función no son botones (el detalle al que llevan es la HU8-CP06) | Frontend (componente) | 2 | ✅ Pasa (2, AB#350, PR #131) |
| HU7-CP09 | Al cambiar el tamaño de la página la banda de los mejores se reinicia (una sola vez, cuando termina el cambio) para que las tarjetas no se solapen; si el tamaño no cambia, no se reinicia | Frontend (componente) | 3 | ✅ Pasa (3, AB#350, PR #131) |
| HU7-CP10 | El caso de uso busca en el catálogo con los criterios recibidos y devuelve la página del repositorio | Unitario | 1 | ✅ Pasa (1, AB#352, PR #153) |
| HU7-CP11 | El catálogo solo devuelve los productos disponibles, de todos los vendedores | Integración MongoDB | 1 | ✅ Pasa (1, AB#352, PR #153) |
| HU7-CP12 | La búsqueda por nombre no distingue mayúsculas ni tildes y trata el texto como literal, no como expresión regular | Integración MongoDB | 2 | ✅ Pasa (2, AB#352, PR #153) |
| HU7-CP13 | El filtro por categoría se puede combinar con la búsqueda | Integración MongoDB | 1 | ✅ Pasa (1, AB#352, PR #153) |
| HU7-CP14 | El catálogo se ordena por nombre en los dos sentidos, paginado y con el total completo | Integración MongoDB | 1 | ✅ Pasa (1, AB#352, PR #153) |
| HU7-CP15 | Una valoración mínima solo deja los productos con esa media o más, y ordenar por valoración ordena por la media (HU13), desempata por nombre de la A a la Z y trata los productos sin valoraciones como los de menor media | Integración MongoDB | 2 | 🔍 En revisión (2, AB#352 · AB#415, PR #195) |
| HU7-CP16 | El cliente recibe el catálogo con el formato del contrato (sin precio, stock ni vendedor; `valoracion` null y `numValoraciones` 0) | Integración (capa web) | 1 | ✅ Pasa (1, AB#353, PR #153) |
| HU7-CP17 | Los parámetros llegan al caso de uso traducidos: página desde 0, criterio de orden y sentido | Integración (capa web) | 1 | ✅ Pasa (1, AB#353, PR #153) |
| HU7-CP18 | Un parámetro no válido (página o tamaño menor que 1, valoración fuera de 1 a 5, orden o sentido desconocidos) devuelve 400 y no llega al caso de uso | Integración (capa web) | 1 | ✅ Pasa (1, AB#353, PR #153) |
| HU7-CP19 | Sin sesión (401) o sin ser cliente (403) no se ve el catálogo | Integración (capa web) | 2 | ✅ Pasa (2, AB#353, PR #153) |

Notas:
- **HU7-CP01 a CP09 (Sprint 2, frontend):** casos propuestos en las tasks AB#350 (vista de productos) y AB#351 (vista de filtrado y búsqueda), a partir de lo pedido en ellas y de las decisiones del equipo (`frontend/docs/hu7-busqueda-y-filtrado-de-productos.md`). Pendientes de confirmar con el equipo y de añadir a la HU7 (AB#69) en Azure Boards.
- **HU7-CP10 a CP19 (Sprint 2, backend):** casos propuestos en las tasks AB#352 (lógica de filtrado de productos) y AB#353 (endpoint para obtener productos), con el contrato de `GET /api/products` que ya usaba el frontend. Desarrollados con TDD. Pendientes de confirmar con el equipo y de añadir a la HU7 (AB#69) en Azure Boards.
- **Valoraciones (HU13, T415):** `valoracion` y `numValoraciones` salen de la media y el número de valoraciones que el módulo de valoraciones guarda en el producto (`valoracionMedia` y `numeroValoraciones`). La valoración mínima filtra por esa media y ordenar por valoración ordena por ella. Hasta la T415, una valoración mínima no devolvía nada y ordenar por valoración equivalía a ordenar por nombre; HU7-CP15 se modificó en la T415 para comprobar el comportamiento real. Un producto sin valoraciones sigue dando `null` y 0 (HU7-CP16).
- **Decisiones de HU7-CP18:** `sort` y `order` se validan con `@Pattern`, como `page`, `size` y `minRating` con `@Min` y `@Max`. Que `minRating=6` sea inválido es una propuesta (la valoración va de 1 a 5).
- **Pendiente de decidir:** el orden por nombre no usa la collation en español (`ORDEN_ALFABETICO_ES` de `UsuarioMongoAdapter`): las mayúsculas salen antes y las vocales con tilde, tras la Z. HU7-CP14 usa nombres sin tilde y en mayúscula para no depender de ello.
- **Decisión del equipo:** no se ordena por categoría (no tiene sentido); el listado es un scroll infinito de diez en diez y no una paginación numérica; la selección son los diez mejor valorados, como tarjetas dentro de la animación y a todo el ancho de la página.
- **HU7-CP08 (cambio en la HU8):** en la primera versión las tarjetas abrían una ventana de ficha (HU7-CP08, 8 tests). La HU8 la sustituye por la página de detalle: los tests del clic en el listado, la animación y la lista pasan a ser HU8-CP06 y la ventana (`ProductDialog`) se elimina. Quedan en HU7-CP08 los de la banda animada.
- **HU7-CP09:** el reinicio al cambiar el tamaño se prueba con un `ResizeObserver` que dispara el test y con temporizadores simulados.
- **Cambio de comportamiento:** `/products` ya no es una ruta pública: sin sesión lleva a `/login`. Se ha cambiado en consecuencia el test técnico `productos_sinSesion_llevaAIniciarSesion` de `LoginFlow.test.jsx` (antes `productos_sinSesion_esUnaRutaPublica`).
- **Infraestructura de tests:** `test/setup.js` añade un `IntersectionObserver` que no avisa nunca (jsdom no lo trae); los tests del scroll infinito lo sustituyen por uno que dispara a mano.
- **Fallo resuelto (no era de esta HU):** `HU7-CP04: al elegir una categoría pide solo los productos de esa categoría` y `HU7-CP04: al filtrar vuelve a empezar desde la primera página, sin conservar lo cargado` fallaban en `develop` desde que el desplegable de categorías de la HU31 sustituyó al selector nativo que usaban. El PR #143 los adapta al desplegable con el helper `elegirCategoria` (abre el desplegable y pulsa la opción).
- **Test inestable (HU7-CP03):** `HU7-CP03: al llegar otra vez al final carga las últimas y avisa de que no hay más` fallaba a veces con la máquina cargada: el test simulaba la llegada al final antes de que la página conectara el `IntersectionObserver` (lo hace en un `useEffect`, justo después de pintar las tarjetas). En el mismo PR #143, `llegarAlFinal` espera a que haya un observador conectado antes de avisar.

### Tabla B · casos de prueba ↔ tests

| Caso | Clase de test | Método | Capa | Task | Origen | Estado |
|---|---|---|---|---|---|---|
| HU7-CP01 | `ProductsPage.test.jsx` | `HU7-CP01: tras iniciar sesión, el cliente aterriza en la vista de productos` | Frontend · página | AB#350 | PR #131 | ✅ |
| HU7-CP01 | `ProductsPage.test.jsx` | `HU7-CP01: sin sesión, la vista de productos lleva a iniciar sesión` | Frontend · página | AB#350 | PR #131 | ✅ |
| HU7-CP01 | `ProductsPage.test.jsx` | `HU7-CP01: un vendedor no ve la vista de productos del cliente: va a la suya` | Frontend · página | AB#350 | PR #131 | ✅ |
| HU7-CP02 | `ProductCard.test.jsx` | `HU7-CP02: muestra el nombre, la imagen y la categoría del producto` | Frontend · componente | AB#350 | PR #131 | ✅ |
| HU7-CP02 | `ProductCard.test.jsx` | `HU7-CP02: muestra la valoración en estrellas y cuántas valoraciones tiene` | Frontend · componente | AB#350 | PR #131 | ✅ |
| HU7-CP02 | `ProductCard.test.jsx` | `HU7-CP02: un producto sin valoraciones lo indica en lugar de mostrar estrellas` | Frontend · componente | AB#350 | PR #131 | ✅ |
| HU7-CP02 | `ProductCard.test.jsx` | `HU7-CP02: un producto sin imagen muestra un marcador en su lugar` | Frontend · componente | AB#350 | PR #131 | ✅ |
| HU7-CP02 | `ProductCard.test.jsx` | `HU7-CP02: si no se conoce la categoría muestra un guion` | Frontend · componente | AB#350 | PR #131 | ✅ |
| HU7-CP02 | `ProductsPage.test.jsx` | `HU7-CP02: cada tarjeta muestra el nombre, la categoría y la valoración del producto` | Frontend · página | AB#350 | PR #131 | ✅ |
| HU7-CP03 | `ProductsPage.test.jsx` | `HU7-CP03: muestra las diez primeras tarjetas y solo los productos disponibles` | Frontend · página | AB#350 | PR #131 | ✅ |
| HU7-CP03 | `ProductsPage.test.jsx` | `HU7-CP03: las tarjetas se colocan de dos en dos` | Frontend · página | AB#350 | PR #131 | ✅ |
| HU7-CP03 | `ProductsPage.test.jsx` | `HU7-CP03: al llegar al final de la página carga otras diez debajo de las anteriores` | Frontend · página | AB#350 | PR #131 | ✅ |
| HU7-CP03 | `ProductsPage.test.jsx` | `HU7-CP03: al llegar otra vez al final carga las últimas y avisa de que no hay más` | Frontend · página | AB#350 | PR #131 | ✅ |
| HU7-CP03 | `ProductsPage.test.jsx` | `HU7-CP03: el botón «Ver más productos» también carga más, para quien no use el scroll` | Frontend · página | AB#350 | PR #131 | ✅ |
| HU7-CP03 | `ProductsPage.test.jsx` | `HU7-CP03: si falla al cargar más, avisa sin perder las tarjetas y permite reintentar` | Frontend · página | AB#350 | PR #131 | ✅ |
| HU7-CP03 | `ProductsPage.test.jsx` | `HU7-CP03: si falla la carga avisa y permite reintentar` | Frontend · página | AB#350 | PR #131 | ✅ |
| HU7-CP03 | `ProductsPage.test.jsx` | `HU7-CP03: si todavía no hay productos lo indica` | Frontend · página | AB#350 | PR #131 | ✅ |
| HU7-CP04 | `ProductsPage.test.jsx` | `HU7-CP04: al escribir en el buscador pide los productos con ese nombre` | Frontend · página | AB#351 | PR #131 | ✅ |
| HU7-CP04 | `ProductsPage.test.jsx` | `HU7-CP04: la búsqueda no distingue mayúsculas ni tildes` | Frontend · página | AB#351 | PR #131 | ✅ |
| HU7-CP04 | `ProductsPage.test.jsx` | `HU7-CP04: al elegir una categoría pide solo los productos de esa categoría` | Frontend · página | AB#351 | PR #131 | ✅ |
| HU7-CP04 | `ProductsPage.test.jsx` | `HU7-CP04: al filtrar vuelve a empezar desde la primera página, sin conservar lo cargado` | Frontend · página | AB#351 | PR #131 | ✅ |
| HU7-CP04 | `ProductsPage.test.jsx` | `HU7-CP04: si ningún producto coincide lo indica y permite limpiar los filtros` | Frontend · página | AB#351 | PR #131 | ✅ |
| HU7-CP05 | `ProductsPage.test.jsx` | `HU7-CP05: al elegir una valoración mínima pide solo los productos con esa valoración o más` | Frontend · página | AB#351 | PR #131 | ✅ |
| HU7-CP05 | `ProductsPage.test.jsx` | `HU7-CP05: con la valoración mínima, todos los productos mostrados la alcanzan` | Frontend · página | AB#351 | PR #131 | ✅ |
| HU7-CP05 | `ProductsPage.test.jsx` | `HU7-CP05: al volver a «Cualquiera» deja de filtrar por valoración` | Frontend · página | AB#351 | PR #131 | ✅ |
| HU7-CP06 | `ProductsPage.test.jsx` | `HU7-CP06: por defecto pide diez productos ordenados por nombre de la A a la Z y sin filtros` | Frontend · página | AB#351 | PR #131 | ✅ |
| HU7-CP06 | `ProductsPage.test.jsx` | `HU7-CP06: permite ordenar por nombre y por valoración, y no por categoría` | Frontend · página | AB#351 | PR #131 | ✅ |
| HU7-CP06 | `ProductsPage.test.jsx` | `HU7-CP06: el botón de sentido alterna entre ascendente y descendente` | Frontend · página | AB#351 | PR #131 | ✅ |
| HU7-CP06 | `ProductsPage.test.jsx` | `HU7-CP06: ordenados por nombre, los productos salen de la A a la Z` | Frontend · página | AB#351 | PR #131 | ✅ |
| HU7-CP06 | `ProductsPage.test.jsx` | `HU7-CP06: ordenados por valoración descendente, el mejor valorado sale primero` | Frontend · página | AB#351 | PR #131 | ✅ |
| HU7-CP07 | `TiltedGridHero.test.jsx` | `HU7-CP07: muestra su contenido por encima de la banda de imágenes` | Frontend · componente | AB#350 | PR #131 | ✅ |
| HU7-CP07 | `TiltedGridHero.test.jsx` | `HU7-CP07: la banda de imágenes es decorativa y reparte las imágenes recibidas` | Frontend · componente | AB#350 | PR #131 | ✅ |
| HU7-CP07 | `TiltedGridHero.test.jsx` | `HU7-CP07: sin imágenes no falla y sigue mostrando su contenido` | Frontend · componente | AB#350 | PR #131 | ✅ |
| HU7-CP07 | `TiltedGridHero.test.jsx` | `HU7-CP07: cada imagen puede llevar un pie (nombre, valoración…) que se dibuja sobre ella` | Frontend · componente | AB#350 | PR #131 | ✅ |
| HU7-CP07 | `ProductsPage.test.jsx` | `HU7-CP07: arriba muestra los diez productos mejor valorados, numerados y con su valoración` | Frontend · página | AB#350 | PR #131 | ✅ |
| HU7-CP07 | `ProductsPage.test.jsx` | `HU7-CP07: la selección pide los diez mejor valorados: valoración descendente y sin mínimo de estrellas` | Frontend · página | AB#350 | PR #131 | ✅ |
| HU7-CP07 | `ProductsPage.test.jsx` | `HU7-CP07: con algún filtro activo la selección se oculta para centrarse en los resultados` | Frontend · página | AB#350 | PR #131 | ✅ |
| HU7-CP07 | `ProductsPage.test.jsx` | `HU7-CP07: si hay menos de tres productos valorados no se muestra la selección` | Frontend · página | AB#350 | PR #131 | ✅ |
| HU7-CP07 | `ProductsPage.test.jsx` | `HU7-CP07: si la selección no carga, el listado sigue funcionando sin ella` | Frontend · página | AB#350 | PR #131 | ✅ |
| HU7-CP08 | `TiltedGridHero.test.jsx` | `HU7-CP08: al pulsar una imagen de la banda avisa con esa imagen` | Frontend · componente | AB#350 | PR #131 | ✅ |
| HU7-CP08 | `TiltedGridHero.test.jsx` | `HU7-CP08: sin onSelect las imágenes de la banda no son botones` | Frontend · componente | AB#350 | PR #131 | ✅ |
| HU7-CP09 | `TiltedGridHero.test.jsx` | `HU7-CP09: al cambiar el tamaño de la página la banda se reinicia, para que las imágenes no se solapen` | Frontend · componente | AB#350 | PR #131 | ✅ |
| HU7-CP09 | `TiltedGridHero.test.jsx` | `HU7-CP09: espera a que termine de cambiar el tamaño: varios cambios seguidos reinician una sola vez` | Frontend · componente | AB#350 | PR #131 | ✅ |
| HU7-CP09 | `TiltedGridHero.test.jsx` | `HU7-CP09: si el tamaño no cambia, la banda sigue su curso sin reiniciarse` | Frontend · componente | AB#350 | PR #131 | ✅ |
| HU7-CP10 | `CatalogoProductosServiceTest` | `buscaEnElCatalogoConLosCriteriosRecibidos` | Aplicación | AB#352 | PR #153 | ✅ |
| HU7-CP11 | `ProductoMongoAdapterTest` | `buscarEnCatalogoSoloDevuelveDisponiblesDeTodosLosVendedores` | Infraestructura | AB#352 | PR #153 | ✅ |
| HU7-CP12 | `ProductoMongoAdapterTest` | `buscarEnCatalogoBuscaSinDistinguirMayusculasNiTildes` | Infraestructura | AB#352 | PR #153 | ✅ |
| HU7-CP12 | `ProductoMongoAdapterTest` | `buscarEnCatalogoTrataLaBusquedaComoTextoLiteral` | Infraestructura | AB#352 | PR #153 | ✅ |
| HU7-CP13 | `ProductoMongoAdapterTest` | `buscarEnCatalogoFiltraPorCategoriaYBusqueda` | Infraestructura | AB#352 | PR #153 | ✅ |
| HU7-CP14 | `ProductoMongoAdapterTest` | `buscarEnCatalogoOrdenaPorNombreYPagina` | Infraestructura | AB#352 | PR #153 | ✅ |
| HU7-CP15 | `ProductoMongoAdapterTest` | `buscarEnCatalogoConValoracionMinimaFiltraPorLaMedia` | Infraestructura | AB#352 · AB#415 | PR #153 (sustituido en el PR #195) | 🔍 |
| HU7-CP15 | `ProductoMongoAdapterTest` | `buscarEnCatalogoPorValoracionOrdenaPorLaMedia` | Infraestructura | AB#352 · AB#415 | PR #153 (sustituido en el PR #195) | 🔍 |
| HU7-CP16 | `CatalogoControllerTest` | `clienteRecibeElCatalogoConElFormatoDelContrato` | Infraestructura | AB#353 | PR #153 | ✅ |
| HU7-CP17 | `CatalogoControllerTest` | `traduceLosParametrosALaConsulta` | Infraestructura | AB#353 | PR #153 | ✅ |
| HU7-CP18 | `CatalogoControllerTest` | `parametroNoValidoDevuelve400` | Infraestructura | AB#353 | PR #153 | ✅ |
| HU7-CP19 | `CatalogoControllerTest` | `sinSesionDevuelve401` | Infraestructura | AB#353 | PR #153 | ✅ |
| HU7-CP19 | `CatalogoControllerTest` | `otroRolDevuelve403` | Infraestructura | AB#353 | PR #153 | ✅ |

Rutas de las clases de test (bajo `frontend/src/`):
- `ProductCard.test.jsx`: `features/catalog/` (Vitest y React Testing Library)
- `TiltedGridHero.test.jsx`: `components/` (Vitest y React Testing Library)
- `ProductsPage.test.jsx`: `pages/` (Vitest, React Testing Library y MSW)

Rutas de las clases de test (bajo `backend/src/test/java/es/uclm/esibuy/backend/`):
- `CatalogoProductosServiceTest`: `productos/application/service/` (JUnit y Mockito, sin Spring)
- `ProductoMongoAdapterTest`: `productos/infrastructure/adapter/out/persistence/` (`@DataMongoTest` con Testcontainers)
- `CatalogoControllerTest`: `productos/infrastructure/adapter/in/web/` (`@WebMvcTest` con la seguridad real)

---

## HU8 · Consulta de detalle de producto

### Tabla A · HU8 ↔ casos de prueba

| Caso | Descripción | Tipo | Tests | Estado |
|---|---|---|---|---|
| HU8-CP01 | El detalle de un producto solo lo ve un cliente (sin sesión lleva al login; otro rol, a su página) | Frontend (página) | 3 | ✅ Pasa (3, AB#354, PR #136) |
| HU8-CP02 | El detalle muestra la imagen, el nombre, la categoría, el proveedor (nombre comercial), la descripción, la valoración (estrellas, nota y número de valoraciones) y una ficha con la información del producto | Frontend (página) | 5 | ✅ Pasa (5, AB#354, PR #136) |
| HU8-CP03 | El precio es el que calcula el servidor para ese cliente: sin descuento, solo el precio; con descuento, el final, el original tachado, el porcentaje y lo que se ahorra | Frontend (página) | 3 | ✅ Pasa (3, AB#354, PR #136) |
| HU8-CP04 | La disponibilidad se indica según las existencias: en stock, pocas unidades (con cuántas quedan, en singular o plural), agotado o no disponible | Frontend (página) | 5 | ✅ Pasa (5, AB#354, PR #136) |
| HU8-CP05 | La página tiene estado de carga, de producto no encontrado (404, también para un producto no disponible) con enlace para volver, y de error con reintento | Frontend (página) | 4 | ✅ Pasa (4, AB#354, PR #136) |
| HU8-CP06 | Las tarjetas del listado, de la animación y de la lista de los mejores llevan al detalle del producto (con el teclado también); desde el detalle se vuelve a los productos; la tarjeta es un enlace con el identificador codificado | Frontend (componente y página) | 7 | ✅ Pasa (7, AB#354, PR #136) |
| HU8-CP07 | Un producto sin valoraciones, sin imagen o sin descripción se muestra con un aviso en lugar de un hueco | Frontend (página) | 3 | ✅ Pasa (3, AB#354, PR #136) |
| HU8-CP08 | El descuento que se aplica al cliente: ninguno si no hay descuentos activos; al Estándar, el general; al Premium, el premium si está activo y, si no, el general | Unitario (dominio) | 3 | ✅ Pasa (3, AB#356, PR #173) |
| HU8-CP09 | El detalle de un producto disponible lleva sus datos, el nombre comercial de su vendedor y, para un cliente Estándar, el precio con el descuento general | Unitario | 1 | ✅ Pasa (1, AB#356, PR #173) |
| HU8-CP10 | Al cliente Premium se le muestra el precio con su descuento premium | Unitario | 1 | ✅ Pasa (1, AB#356, PR #173) |
| HU8-CP11 | El detalle de un producto que no existe o no está disponible no se encuentra (ProductoNoEncontradoException) | Unitario | 1 | ✅ Pasa (1, AB#356, PR #173) |
| HU8-CP12 | Si el cliente es Premium y el nombre comercial del vendedor se piden al módulo de usuarios por su puerto de entrada | Unitario | 2 | ✅ Pasa (2, AB#356, PR #173) |
| HU8-CP13 | El cliente recibe el detalle con el formato del contrato (`vendedor` con id y nombre comercial; `descuento` con su porcentaje, o null sin descuento; `valoracion` null y `numValoraciones` 0) | Integración (capa web) | 2 | ✅ Pasa (2, AB#355, PR #173) |
| HU8-CP14 | Un producto que no existe o no está disponible devuelve 404 con el código PRODUCT_NOT_FOUND | Integración (capa web) | 1 | ✅ Pasa (1, AB#355, PR #173) |
| HU8-CP15 | Solo el cliente en sesión consulta el detalle: sin sesión, 401; otro rol, 403; y el caso de uso rechaza otro rol, el correo de otro cliente y la falta de sesión | Integración (capa web) + unitario con Spring Security (sin MongoDB) | 7 | ✅ Pasa (7, AB#355, PR #173) |

Notas:
- **HU8-CP01 a CP07 (Sprint 2, frontend):** casos propuestos en la task AB#354 (vista de detalle de producto) a partir de lo pedido en ella y de las decisiones del equipo (`frontend/docs/hu8-detalle-producto.md`). Pendientes de confirmar con el equipo y de añadir a la HU8 (AB#70) en Azure Boards.
- **HU8-CP08 a CP15 (Sprint 2, backend):** casos propuestos en las tasks AB#356 (lógica del detalle de producto) y AB#355 (endpoint `GET /api/products/{id}`), con el contrato que ya usaba el frontend en sus mocks (MSW). Desarrollados con TDD y mergeados en `develop` (PR #173). Pendientes de confirmar con el equipo y de añadir a la HU8 (AB#70) en Azure Boards.
- **Producto no disponible:** el detalle lo trata como si no existiera (404 `PRODUCT_NOT_FOUND`), igual que el frontend (HU8-CP05).
- **Seguridad (HU8-CP15):** la regla de `SecurityConfig` pasa a `GET /api/products/**`, solo para CLIENTE, y cubre el catálogo (HU7) y el detalle. `consultarDetalle` lleva `@PreAuthorize(Permisos.SOLO_CLIENTE_PROPIO)`: además del rol, el correo tiene que ser el de la sesión, para que un cliente Estándar no vea el precio de un Premium.
- **Valoraciones (HU13, sin hacer):** como en el catálogo, `valoracion` es `null` y `numValoraciones` es 0 (`PENDIENTE DE DECIDIR (equipo)` en el código).
- **Decisión del equipo:** el servidor calcula el precio para el cliente (Estándar o Premium) y no hay botón de compra hasta que exista el carrito.
- **HU8-CP06:** los tests del clic en el listado, la animación y la lista de los mejores eran HU7-CP08 cuando abrían una ventana de ficha; ahora navegan a la página de detalle.
- **Test inestable conocido (no es de esta HU):** `HU7-CP03: al llegar otra vez al final carga las últimas y avisa de que no hay más` falla a veces al ejecutar toda la batería y pasa solo.

### Tabla B · casos de prueba ↔ tests

| Caso | Clase de test | Método | Capa | Task | Origen | Estado |
|---|---|---|---|---|---|---|
| HU8-CP01 | `ProductDetailPage.test.jsx` | `HU8-CP01: sin sesión, el detalle de un producto lleva a iniciar sesión` | Frontend · página | AB#354 | PR #136 | ✅ |
| HU8-CP01 | `ProductDetailPage.test.jsx` | `HU8-CP01: un vendedor no ve el detalle del cliente: va a su página` | Frontend · página | AB#354 | PR #136 | ✅ |
| HU8-CP01 | `ProductDetailPage.test.jsx` | `HU8-CP01: un cliente ve el detalle del producto con su nombre como título` | Frontend · página | AB#354 | PR #136 | ✅ |
| HU8-CP02 | `ProductDetailPage.test.jsx` | `HU8-CP02: muestra la imagen, la categoría, el proveedor y la descripción` | Frontend · página | AB#354 | PR #136 | ✅ |
| HU8-CP02 | `ProductDetailPage.test.jsx` | `HU8-CP02: muestra la valoración en estrellas, su nota y cuántas valoraciones tiene` | Frontend · página | AB#354 | PR #136 | ✅ |
| HU8-CP02 | `ProductDetailPage.test.jsx` | `HU8-CP02: con una sola valoración lo dice en singular` | Frontend · página | AB#354 | PR #136 | ✅ |
| HU8-CP02 | `ProductDetailPage.test.jsx` | `HU8-CP02: la ficha recoge la categoría, el proveedor, la disponibilidad y la referencia` | Frontend · página | AB#354 | PR #136 | ✅ |
| HU8-CP02 | `ProductDetailPage.test.jsx` | `HU8-CP02: con los datos de ejemplo muestra un producto completo` | Frontend · página | AB#354 | PR #136 | ✅ |
| HU8-CP03 | `ProductDetailPage.test.jsx` | `HU8-CP03: sin descuento muestra solo el precio` | Frontend · página | AB#354 | PR #136 | ✅ |
| HU8-CP03 | `ProductDetailPage.test.jsx` | `HU8-CP03: con descuento muestra el precio final, el original tachado, el porcentaje y lo que se ahorra` | Frontend · página | AB#354 | PR #136 | ✅ |
| HU8-CP03 | `ProductDetailPage.test.jsx` | `HU8-CP03: el precio lo calcula el servidor: se muestra el que llega, no uno recalculado` | Frontend · página | AB#354 | PR #136 | ✅ |
| HU8-CP04 | `ProductDetailPage.test.jsx` | `HU8-CP04: con existencias muestra que está en stock` | Frontend · página | AB#354 | PR #136 | ✅ |
| HU8-CP04 | `ProductDetailPage.test.jsx` | `HU8-CP04: con pocas unidades avisa de cuántas quedan` | Frontend · página | AB#354 | PR #136 | ✅ |
| HU8-CP04 | `ProductDetailPage.test.jsx` | `HU8-CP04: con una sola unidad lo dice en singular` | Frontend · página | AB#354 | PR #136 | ✅ |
| HU8-CP04 | `ProductDetailPage.test.jsx` | `HU8-CP04: sin existencias muestra que está agotado` | Frontend · página | AB#354 | PR #136 | ✅ |
| HU8-CP04 | `ProductDetailPage.test.jsx` | `HU8-CP04: un producto no disponible lo indica aunque tenga existencias` | Frontend · página | AB#354 | PR #136 | ✅ |
| HU8-CP05 | `ProductDetailPage.test.jsx` | `HU8-CP05: mientras carga muestra un indicador de carga` | Frontend · página | AB#354 | PR #136 | ✅ |
| HU8-CP05 | `ProductDetailPage.test.jsx` | `HU8-CP05: si el producto no existe lo indica y ofrece volver a los productos` | Frontend · página | AB#354 | PR #136 | ✅ |
| HU8-CP05 | `ProductDetailPage.test.jsx` | `HU8-CP05: si falla la carga avisa y permite reintentar` | Frontend · página | AB#354 | PR #136 | ✅ |
| HU8-CP05 | `ProductDetailPage.test.jsx` | `HU8-CP05: un producto que el catálogo no ofrece (no disponible) se trata como no encontrado` | Frontend · página | AB#354 | PR #136 | ✅ |
| HU8-CP06 | `ProductCard.test.jsx` | `HU8-CP06: la tarjeta es un enlace al detalle del producto` | Frontend · componente | AB#354 | PR #136 | ✅ |
| HU8-CP06 | `ProductCard.test.jsx` | `HU8-CP06: el identificador del producto se codifica en la dirección` | Frontend · componente | AB#354 | PR #136 | ✅ |
| HU8-CP06 | `ProductsPage.test.jsx` | `HU8-CP06: al pulsar una tarjeta del listado se abre el detalle de ese producto` | Frontend · página | AB#354 | PR #136 | ✅ |
| HU8-CP06 | `ProductsPage.test.jsx` | `HU8-CP06: al pulsar una tarjeta de la animación de los mejores se abre el detalle de su producto` | Frontend · página | AB#354 | PR #136 | ✅ |
| HU8-CP06 | `ProductsPage.test.jsx` | `HU8-CP06: con el teclado se llega al detalle desde la lista de los mejores` | Frontend · página | AB#354 | PR #136 | ✅ |
| HU8-CP06 | `ProductsPage.test.jsx` | `HU8-CP06: desde el detalle, «Volver a los productos» lleva otra vez al listado` | Frontend · página | AB#354 | PR #136 | ✅ |
| HU8-CP06 | `ProductDetailPage.test.jsx` | `HU8-CP06: desde el detalle se puede volver a los productos` | Frontend · página | AB#354 | PR #136 | ✅ |
| HU8-CP07 | `ProductDetailPage.test.jsx` | `HU8-CP07: un producto sin valoraciones lo indica en lugar de mostrar estrellas` | Frontend · página | AB#354 | PR #136 | ✅ |
| HU8-CP07 | `ProductDetailPage.test.jsx` | `HU8-CP07: un producto sin imagen muestra un marcador en su lugar` | Frontend · página | AB#354 | PR #136 | ✅ |
| HU8-CP07 | `ProductDetailPage.test.jsx` | `HU8-CP07: un producto sin descripción lo indica` | Frontend · página | AB#354 | PR #136 | ✅ |
| HU8-CP08 | `ProductoTest` | `sinDescuentosNoSeAplicaNinguno` | Dominio | AB#356 | PR #173 | ✅ |
| HU8-CP08 | `ProductoTest` | `alClienteEstandarSeLeAplicaElGeneral` | Dominio | AB#356 | PR #173 | ✅ |
| HU8-CP08 | `ProductoTest` | `alClientePremiumSeLeAplicaElPremiumOElGeneral` | Dominio | AB#356 | PR #173 | ✅ |
| HU8-CP09 | `CatalogoProductosServiceTest` | `elDetalleLlevaSusDatosYElNombreComercialDelVendedor` | Aplicación | AB#356 | PR #173 | ✅ |
| HU8-CP10 | `CatalogoProductosServiceTest` | `alClientePremiumSeLeMuestraElPrecioConSuDescuento` | Aplicación | AB#356 | PR #173 | ✅ |
| HU8-CP11 | `CatalogoProductosServiceTest` | `elDetalleDeUnProductoQueNoExisteONoEstaDisponibleNoSeEncuentra` | Aplicación | AB#356 | PR #173 | ✅ |
| HU8-CP12 | `UsuarioEnSesionAdapterTest` | `esClientePremiumPreguntaAlModuloDeUsuarios` | Infraestructura | AB#356 | PR #173 | ✅ |
| HU8-CP12 | `VendedorAdapterTest` | `nombreComercialPreguntaAlModuloDeUsuarios` | Infraestructura | AB#356 | PR #173 | ✅ |
| HU8-CP13 | `CatalogoControllerTest` | `clienteRecibeElDetalleConElFormatoDelContrato` | Infraestructura | AB#355 | PR #173 | ✅ |
| HU8-CP13 | `CatalogoControllerTest` | `sinDescuentoElDescuentoDelDetalleEsNull` | Infraestructura | AB#355 | PR #173 | ✅ |
| HU8-CP14 | `CatalogoControllerTest` | `productoNoEncontradoDevuelve404` | Infraestructura | AB#355 | PR #173 | ✅ |
| HU8-CP15 | `CatalogoControllerTest` | `sinSesionElDetalleDevuelve401` | Infraestructura | AB#355 | PR #173 | ✅ |
| HU8-CP15 | `CatalogoControllerTest` | `otroRolElDetalleDevuelve403` | Infraestructura | AB#355 | PR #173 | ✅ |
| HU8-CP15 | `SeguridadProductosServiceTest` | `unVendedorNoConsultaElDetalle` | Aplicación | AB#355 | PR #173 | ✅ |
| HU8-CP15 | `SeguridadProductosServiceTest` | `unAdministradorNoConsultaElDetalle` | Aplicación | AB#355 | PR #173 | ✅ |
| HU8-CP15 | `SeguridadProductosServiceTest` | `unClienteNoConsultaElDetalleConElCorreoDeOtro` | Aplicación | AB#355 | PR #173 | ✅ |
| HU8-CP15 | `SeguridadProductosServiceTest` | `unClienteConsultaElDetalleConSuPropioCorreo` | Aplicación | AB#355 | PR #173 | ✅ |
| HU8-CP15 | `SeguridadProductosServiceTest` | `sinSesionNoSeConsultaElDetalle` | Aplicación | AB#355 | PR #173 | ✅ |

Rutas de las clases de test (bajo `frontend/src/`):
- `ProductDetailPage.test.jsx` y `ProductsPage.test.jsx`: `pages/` (Vitest, React Testing Library y MSW)
- `ProductCard.test.jsx`: `features/catalog/` (Vitest y React Testing Library)

Rutas de las clases de test (bajo `backend/src/test/java/es/uclm/esibuy/backend/`):
- `ProductoTest`: `productos/domain/model/` (JUnit, sin Spring)
- `CatalogoProductosServiceTest`: `productos/application/service/` (JUnit y Mockito, sin Spring)
- `SeguridadProductosServiceTest`: `productos/application/service/` (contexto mínimo de Spring con la seguridad de métodos, sin MongoDB)
- `UsuarioEnSesionAdapterTest` y `VendedorAdapterTest`: `productos/infrastructure/adapter/out/usuarios/` (JUnit y Mockito, sin Spring)
- `CatalogoControllerTest`: `productos/infrastructure/adapter/in/web/` (`@WebMvcTest` con la seguridad real)

---

## HU9 · Gestión del carrito de compra

### Tabla A · HU9 ↔ casos de prueba

| Caso | Descripción | Tipo | Tests | Estado |
|---|---|---|---|---|
| HU9-CP01 | El precio de cada producto se ve desde fuera, en las tarjetas del catálogo, con el descuento (precio final, original tachado y porcentaje) | Frontend (componente y página) | 3 | 🔍 En revisión (3, AB#383, PR sin abrir) |
| HU9-CP02 | Desde el listado se puede añadir una unidad de un producto al carrito: el botón muestra «Añadiendo…» y «Añadido», avisa si no hay existencias o falla, no abre el detalle y no deja añadir un producto agotado o no disponible | Frontend (componente y página) | 12 | 🔍 En revisión (12, AB#383, PR sin abrir) |
| HU9-CP03 | Desde el detalle se puede elegir la cantidad (entre 1 y las existencias) y añadirla al carrito; un producto agotado no se puede añadir | Frontend (página) | 5 | 🔍 En revisión (5, AB#383, PR sin abrir) |
| HU9-CP04 | Solo los clientes tienen carrito: el botón flotante no aparece para otros roles | Frontend (componente y página) | 2 | 🔍 En revisión (2, AB#384, PR sin abrir) |
| HU9-CP05 | El carrito está siempre a la vista abajo a la izquierda, con el número de productos, y su panel separa los productos por proveedor con la cabecera «Proveedor: total», la cantidad y el subtotal de cada producto | Frontend (componente y página) | 11 | 🔍 En revisión (11, AB#384, PR sin abrir) |
| HU9-CP06 | Desde el panel se puede cambiar la cantidad de un producto (sin pasar de las existencias), quitarlo y vaciar el carrito, con aviso si el servidor lo rechaza | Frontend (componente) | 6 | 🔍 En revisión (6, AB#384, PR sin abrir) |
| HU9-CP07 | El carrito tiene estado de carga y de error con reintento, y vive en el servidor: al recargar la página sigue ahí | Frontend (componente y página) | 3 | 🔍 En revisión (3, AB#384, PR sin abrir) |
| HU9-CP08 | «Proceder al pago» lleva a la pasarela de pago | Frontend (componente) | 1 | 🔍 En revisión (1, AB#384, PR sin abrir) |
| HU9-CP09 | Con muchos productos el carrito se puede recorrer entero: el panel tiene un scroll global y la lista de cada proveedor su propio scroll, sin que ningún proveedor se comprima | Frontend (componente) | 3 | 🔍 En revisión (3, AB#384, PR sin abrir) |
| HU9-CP10 | Añadir un producto crea su línea o suma unidades a las que ya hay, y el carrito del cliente se guarda | Unitario + controlador | 4 | 🔍 En revisión (4, AB#385 a AB#390, PR #162) |
| HU9-CP11 | No se puede superar el stock al añadir ni al fijar la cantidad (409 `INSUFFICIENT_STOCK`) | Unitario + controlador | 4 | 🔍 En revisión (4, AB#385 a AB#390, PR #162) |
| HU9-CP12 | Un producto que no existe o no está disponible no se añade ni cambia de cantidad (404 `PRODUCT_NOT_FOUND`) | Unitario + controlador | 4 | 🔍 En revisión (4, AB#385 a AB#390, PR #162) |
| HU9-CP13 | Fijar la cantidad la sustituye; una cantidad menor que 1 o una petición incompleta responde 400, y un producto que no está en el carrito, 404 | Unitario + controlador | 7 | 🔍 En revisión (7, AB#385 a AB#390, PR #162) |
| HU9-CP14 | Quitar un producto y vaciar el carrito se guardan y devuelven el carrito | Unitario + controlador | 4 | 🔍 En revisión (4, AB#385 a AB#390, PR #162) |
| HU9-CP15 | El carrito sale agrupado por proveedor, con su nombre comercial, subtotales y totales calculados por el servidor; sin carrito, uno vacío | Unitario + controlador | 6 | 🔍 En revisión (6, AB#385 a AB#390, PR #162) |
| HU9-CP16 | Solo el cliente usa su carrito: sale de la sesión, sin sesión responde 401, otros roles 403 y sin CSRF se rechaza | Unitario + controlador | 3 | 🔍 En revisión (3, AB#385 a AB#390, PR #162) |
| HU9-CP17 | Persistencia en `carritos`: el mapper no pierde datos, se guarda y recupera por cliente conservando `createdAt`, un solo carrito por cliente y la colección rechaza una cantidad menor que 1 | Unitario + integración | 6 | 🔍 En revisión (6, AB#385 a AB#390, PR #162) |
| HU9-CP18 | Limpieza del carrito (AB#390): al consultarlo se quitan los productos que ya no se pueden comprar o sin stock y se ajusta la cantidad al stock | Unitario | 4 | 🔍 En revisión (4, AB#385 a AB#390, PR #162) |
| HU9-CP19 | El precio de cada línea lleva el descuento del cliente: el premium si es PREMIUM y está activo; si no, el general, redondeado a céntimos | Unitario | 9 | 🔍 En revisión (9, AB#385 a AB#390, PR #162) |
| HU9-CP20 | El carrito exige el rol cliente y que el correo recibido sea el de la sesión; sin sesión se rechaza. Seguridad en la capa de negocio (R8) | Unitario con Spring Security (sin MongoDB) | 5 | 🔍 En revisión (5, rama `fix/seguridad-capa-negocio`) |

Notas:
- **HU9-CP01 a CP09 (Sprint 2, frontend):** casos propuestos en las tasks AB#383 (vista añadir al carrito) y AB#384 (vista de carrito) a partir de lo pedido en ellas y de las decisiones del equipo (`frontend/docs/hu9-carrito-de-compra.md`). Pendientes de confirmar con el equipo y de añadir a la HU9 (AB#71) en Azure Boards.
- **Backend (HU9-CP10 a CP19):** módulo `carrito` (AB#385 a AB#390, AB#125 a AB#127), con el contrato que ya usaba el frontend bajo `/api/customer/cart`. Casos propuestos y aprobados por el equipo junto con el diseño; pendientes de añadir a la HU9 (AB#71) en Azure Boards. Decisiones del equipo: la ruta va bajo `/api/customer/**`, el precio lleva el descuento del cliente (premium si lo es) y la limpieza (AB#390) quita al consultar lo que ya no se puede comprar.
- **Decisión del equipo:** los productos del carrito se separan por proveedor, con una cabecera «Proveedor: total» y los totales calculados por el servidor; no hay un total único del carrito.
- **Cambiar la cantidad:** el backend la implementa como `PUT /api/customer/cart/items/{productoId}`.
- **Archivos de tests nuevos** creados sin consulta previa: `AddToCartButton.test.jsx` y `CartWidget.test.jsx`. `ProductCard.test.jsx`, `ProductsPage.test.jsx` y `ProductDetailPage.test.jsx` ganan casos HU9.
- **Infraestructura de tests:** `test/cart.jsx` (`conCarrito`) pone un carrito de mentira alrededor de un componente y `test/setup.js` reinicia el carrito y los pedidos simulados tras cada test.
- **Tests en clases que ya existían** (un archivo de tests por clase): `ProductoTest` y `CatalogoProductosServiceTest` (productos), y `AutenticacionServiceTest` y `AdministracionUsuariosServiceTest` (usuarios) ganan casos HU9, por los métodos nuevos que usa el carrito.
- **HU9-CP20 (R8, seguridad en la capa de negocio):** caso propuesto en la rama `fix/seguridad-capa-negocio`, sin task ni `AB#` por ser un fix transversal. Pendiente de confirmar con el equipo. Comprueba con `@PreAuthorize` que `GestionCarritoUseCase` exige el rol cliente y que el correo recibido es el de la sesión. Clase de test nueva `carrito/application/service/SeguridadCarritoServiceTest` (contexto mínimo de Spring con `@EnableMethodSecurity`, sin MongoDB).

### Tabla B · casos de prueba ↔ tests

| Caso | Clase de test | Método | Capa | Task | Origen | Estado |
|---|---|---|---|---|---|---|
| HU9-CP01 | `ProductCard.test.jsx` | `HU9-CP01: la tarjeta muestra el precio del producto` | Frontend · componente | AB#383 | Rama `feature/US9-T383-T384-carrito-de-compra` (PR sin abrir) | 🔍 |
| HU9-CP01 | `ProductCard.test.jsx` | `HU9-CP01: con descuento muestra el precio final, el original tachado y el porcentaje` | Frontend · componente | AB#383 | Rama `feature/US9-T383-T384-carrito-de-compra` (PR sin abrir) | 🔍 |
| HU9-CP01 | `ProductsPage.test.jsx` | `HU9-CP01: las tarjetas del catálogo muestran el precio de cada producto, con su descuento` | Frontend · página | AB#383 | Rama `feature/US9-T383-T384-carrito-de-compra` (PR sin abrir) | 🔍 |
| HU9-CP02 | `AddToCartButton.test.jsx` | `HU9-CP02: al pulsarlo añade una unidad del producto y confirma con «Añadido»` | Frontend · componente | AB#383 | Rama `feature/US9-T383-T384-carrito-de-compra` (PR sin abrir) | 🔍 |
| HU9-CP02 | `AddToCartButton.test.jsx` | `HU9-CP02: añade la cantidad que se le indica` | Frontend · componente | AB#383 | Rama `feature/US9-T383-T384-carrito-de-compra` (PR sin abrir) | 🔍 |
| HU9-CP02 | `AddToCartButton.test.jsx` | `HU9-CP02: mientras se añade queda desactivado, para no enviar el mismo producto dos veces` | Frontend · componente | AB#383 | Rama `feature/US9-T383-T384-carrito-de-compra` (PR sin abrir) | 🔍 |
| HU9-CP02 | `AddToCartButton.test.jsx` | `HU9-CP02: si no hay existencias suficientes lo avisa y se puede volver a intentar` | Frontend · componente | AB#383 | Rama `feature/US9-T383-T384-carrito-de-compra` (PR sin abrir) | 🔍 |
| HU9-CP02 | `AddToCartButton.test.jsx` | `HU9-CP02: si falla por otro motivo avisa de que no se ha podido añadir` | Frontend · componente | AB#383 | Rama `feature/US9-T383-T384-carrito-de-compra` (PR sin abrir) | 🔍 |
| HU9-CP02 | `AddToCartButton.test.jsx` | `HU9-CP02: el aviso desaparece al volver a intentarlo` | Frontend · componente | AB#383 | Rama `feature/US9-T383-T384-carrito-de-compra` (PR sin abrir) | 🔍 |
| HU9-CP02 | `AddToCartButton.test.jsx` | `HU9-CP02: un producto agotado no se puede añadir` | Frontend · componente | AB#383 | Rama `feature/US9-T383-T384-carrito-de-compra` (PR sin abrir) | 🔍 |
| HU9-CP02 | `AddToCartButton.test.jsx` | `HU9-CP02: un producto no disponible no se puede añadir` | Frontend · componente | AB#383 | Rama `feature/US9-T383-T384-carrito-de-compra` (PR sin abrir) | 🔍 |
| HU9-CP02 | `ProductCard.test.jsx` | `HU9-CP02: la tarjeta tiene un botón para añadir una unidad al carrito` | Frontend · componente | AB#383 | Rama `feature/US9-T383-T384-carrito-de-compra` (PR sin abrir) | 🔍 |
| HU9-CP02 | `ProductCard.test.jsx` | `HU9-CP02: el botón no forma parte del enlace, así que añadir no abre el detalle` | Frontend · componente | AB#383 | Rama `feature/US9-T383-T384-carrito-de-compra` (PR sin abrir) | 🔍 |
| HU9-CP02 | `ProductsPage.test.jsx` | `HU9-CP02: al pulsar «Añadir al carrito» en una tarjeta se envía ese producto al carrito` | Frontend · página | AB#383 | Rama `feature/US9-T383-T384-carrito-de-compra` (PR sin abrir) | 🔍 |
| HU9-CP02 | `ProductsPage.test.jsx` | `HU9-CP02: si el servidor dice que no hay existencias, la tarjeta lo avisa` | Frontend · página | AB#383 | Rama `feature/US9-T383-T384-carrito-de-compra` (PR sin abrir) | 🔍 |
| HU9-CP03 | `ProductDetailPage.test.jsx` | `HU9-CP03: el detalle tiene un botón para añadir el producto al carrito` | Frontend · página | AB#383 | Rama `feature/US9-T383-T384-carrito-de-compra` (PR sin abrir) | 🔍 |
| HU9-CP03 | `ProductDetailPage.test.jsx` | `HU9-CP03: se puede elegir la cantidad antes de añadir` | Frontend · página | AB#383 | Rama `feature/US9-T383-T384-carrito-de-compra` (PR sin abrir) | 🔍 |
| HU9-CP03 | `ProductDetailPage.test.jsx` | `HU9-CP03: la cantidad no baja de una unidad` | Frontend · página | AB#383 | Rama `feature/US9-T383-T384-carrito-de-compra` (PR sin abrir) | 🔍 |
| HU9-CP03 | `ProductDetailPage.test.jsx` | `HU9-CP03: la cantidad no puede superar las existencias` | Frontend · página | AB#383 | Rama `feature/US9-T383-T384-carrito-de-compra` (PR sin abrir) | 🔍 |
| HU9-CP03 | `ProductDetailPage.test.jsx` | `HU9-CP03: un producto agotado no se puede añadir ni se elige cantidad` | Frontend · página | AB#383 | Rama `feature/US9-T383-T384-carrito-de-compra` (PR sin abrir) | 🔍 |
| HU9-CP04 | `CartWidget.test.jsx` | `HU9-CP04: quien no tiene carrito (no es cliente) no ve el botón` | Frontend · componente | AB#384 | Rama `feature/US9-T383-T384-carrito-de-compra` (PR sin abrir) | 🔍 |
| HU9-CP04 | `CartWidget.test.jsx` | `HU9-CP04: un cliente ve el carrito y un vendedor no` | Frontend · componente | AB#384 | Rama `feature/US9-T383-T384-carrito-de-compra` (PR sin abrir) | 🔍 |
| HU9-CP05 | `CartWidget.test.jsx` | `HU9-CP05: indica cuántos productos hay en el carrito` | Frontend · componente | AB#384 | Rama `feature/US9-T383-T384-carrito-de-compra` (PR sin abrir) | 🔍 |
| HU9-CP05 | `CartWidget.test.jsx` | `HU9-CP05: con un solo producto lo dice en singular` | Frontend · componente | AB#384 | Rama `feature/US9-T383-T384-carrito-de-compra` (PR sin abrir) | 🔍 |
| HU9-CP05 | `CartWidget.test.jsx` | `HU9-CP05: con el carrito vacío lo indica` | Frontend · componente | AB#384 | Rama `feature/US9-T383-T384-carrito-de-compra` (PR sin abrir) | 🔍 |
| HU9-CP05 | `CartWidget.test.jsx` | `HU9-CP05: el panel está cerrado hasta que se pulsa el botón` | Frontend · componente | AB#384 | Rama `feature/US9-T383-T384-carrito-de-compra` (PR sin abrir) | 🔍 |
| HU9-CP05 | `CartWidget.test.jsx` | `HU9-CP05: separa los productos por proveedor, con una cabecera «Proveedor: total» por cada uno` | Frontend · componente | AB#384 | Rama `feature/US9-T383-T384-carrito-de-compra` (PR sin abrir) | 🔍 |
| HU9-CP05 | `CartWidget.test.jsx` | `HU9-CP05: cada proveedor agrupa sus propios productos` | Frontend · componente | AB#384 | Rama `feature/US9-T383-T384-carrito-de-compra` (PR sin abrir) | 🔍 |
| HU9-CP05 | `CartWidget.test.jsx` | `HU9-CP05: cada producto muestra su cantidad y lo que cuesta ese producto` | Frontend · componente | AB#384 | Rama `feature/US9-T383-T384-carrito-de-compra` (PR sin abrir) | 🔍 |
| HU9-CP05 | `CartWidget.test.jsx` | `HU9-CP05: cada producto es un enlace a su detalle` | Frontend · componente | AB#384 | Rama `feature/US9-T383-T384-carrito-de-compra` (PR sin abrir) | 🔍 |
| HU9-CP05 | `CartWidget.test.jsx` | `HU9-CP05: el panel se cierra con el botón Cerrar y con Escape` | Frontend · componente | AB#384 | Rama `feature/US9-T383-T384-carrito-de-compra` (PR sin abrir) | 🔍 |
| HU9-CP05 | `CartWidget.test.jsx` | `HU9-CP05: con el carrito vacío lo indica y no ofrece pagar` | Frontend · componente | AB#384 | Rama `feature/US9-T383-T384-carrito-de-compra` (PR sin abrir) | 🔍 |
| HU9-CP05 | `CartWidget.test.jsx` | `HU9-CP05: un producto añadido desde el listado aparece en el carrito, bajo su proveedor` | Frontend · componente | AB#384 | Rama `feature/US9-T383-T384-carrito-de-compra` (PR sin abrir) | 🔍 |
| HU9-CP06 | `CartWidget.test.jsx` | `HU9-CP06: se puede subir y bajar la cantidad de un producto` | Frontend · componente | AB#384 | Rama `feature/US9-T383-T384-carrito-de-compra` (PR sin abrir) | 🔍 |
| HU9-CP06 | `CartWidget.test.jsx` | `HU9-CP06: no se puede pedir más de lo que hay en existencias` | Frontend · componente | AB#384 | Rama `feature/US9-T383-T384-carrito-de-compra` (PR sin abrir) | 🔍 |
| HU9-CP06 | `CartWidget.test.jsx` | `HU9-CP06: se puede quitar un producto del carrito` | Frontend · componente | AB#384 | Rama `feature/US9-T383-T384-carrito-de-compra` (PR sin abrir) | 🔍 |
| HU9-CP06 | `CartWidget.test.jsx` | `HU9-CP06: se puede vaciar el carrito entero` | Frontend · componente | AB#384 | Rama `feature/US9-T383-T384-carrito-de-compra` (PR sin abrir) | 🔍 |
| HU9-CP06 | `CartWidget.test.jsx` | `HU9-CP06: si el servidor rechaza la cantidad por existencias, lo avisa` | Frontend · componente | AB#384 | Rama `feature/US9-T383-T384-carrito-de-compra` (PR sin abrir) | 🔍 |
| HU9-CP06 | `CartWidget.test.jsx` | `HU9-CP06: si falla al quitar un producto, avisa de que no se ha podido` | Frontend · componente | AB#384 | Rama `feature/US9-T383-T384-carrito-de-compra` (PR sin abrir) | 🔍 |
| HU9-CP07 | `CartWidget.test.jsx` | `HU9-CP07: mientras carga el carrito lo indica` | Frontend · componente | AB#384 | Rama `feature/US9-T383-T384-carrito-de-compra` (PR sin abrir) | 🔍 |
| HU9-CP07 | `CartWidget.test.jsx` | `HU9-CP07: si no carga, avisa y permite reintentar` | Frontend · componente | AB#384 | Rama `feature/US9-T383-T384-carrito-de-compra` (PR sin abrir) | 🔍 |
| HU9-CP07 | `CartWidget.test.jsx` | `HU9-CP07: el carrito vive en el servidor: al recargar la página sigue ahí` | Frontend · componente | AB#384 | Rama `feature/US9-T383-T384-carrito-de-compra` (PR sin abrir) | 🔍 |
| HU9-CP08 | `CartWidget.test.jsx` | `HU9-CP08: «Proceder al pago» lleva a la pasarela de pago` | Frontend · componente | AB#384 | Rama `feature/US10-T401-pasarela-de-pago` (PR sin abrir) | 🔍 |
| HU9-CP09 | `CartWidget.test.jsx` | `HU9-CP09: el panel tiene un scroll global y cada proveedor su propio scroll, sin comprimir a ninguno` | Frontend · componente | AB#384 | Rama `fix/US9-carrito-scroll-global-y-por-proveedor` (PR sin abrir) | 🔍 |
| HU9-CP09 | `CartWidget.test.jsx` | `HU9-CP09: con muchos productos siguen estando todos en el carrito, también los últimos de cada proveedor` | Frontend · componente | AB#384 | Rama `fix/US9-carrito-scroll-global-y-por-proveedor` (PR sin abrir) | 🔍 |
| HU9-CP09 | `CartWidget.test.jsx` | `HU9-CP09: con tres proveedores ninguno se comprime, tampoco el que tiene un solo producto` | Frontend · componente | AB#384 | Rama `fix/US9-carrito-scroll-global-y-por-proveedor` (PR sin abrir) | 🔍 |
| HU9-CP10 | `GestionCarritoServiceTest` | `anadirGuardaElCarritoDelCliente` | Aplicación | AB#388 | PR #162 | 🔍 |
| HU9-CP10 | `CarritoTest` | `anadirUnProductoNuevoCreaSuLinea` | Dominio | AB#385 | PR #162 | 🔍 |
| HU9-CP10 | `CarritoTest` | `anadirUnProductoQueYaEstaSumaLasUnidades` | Dominio | AB#385 | PR #162 | 🔍 |
| HU9-CP10 | `CarritoControllerTest` | `anadirRespondeConElCarrito` | Infraestructura | AB#386 | PR #162 | 🔍 |
| HU9-CP11 | `GestionCarritoServiceTest` | `noSeAnadenMasUnidadesQueElStock` | Aplicación | AB#388 | PR #162 | 🔍 |
| HU9-CP11 | `CarritoTest` | `noDejaAnadirMasDelStock` | Dominio | AB#385 | PR #162 | 🔍 |
| HU9-CP11 | `CarritoTest` | `noDejaFijarMasDelStock` | Dominio | AB#385 | PR #162 | 🔍 |
| HU9-CP11 | `CarritoControllerTest` | `superarElStockResponde409` | Infraestructura | AB#386 | PR #162 | 🔍 |
| HU9-CP12 | `GestionCarritoServiceTest` | `unProductoQueNoSePuedeComprarNoSeAnade` | Aplicación | AB#388 | PR #162 | 🔍 |
| HU9-CP12 | `CarritoControllerTest` | `unProductoQueNoSePuedeComprarResponde404` | Infraestructura | AB#386 | PR #162 | 🔍 |
| HU9-CP12 | `ProductosAdapterTest` | `unProductoNoComprableSigueSinSerlo` | Infraestructura | AB#388 | PR #162 | 🔍 |
| HU9-CP12 | `CatalogoProductosServiceTest` | `unProductoQueNoExisteONoEstaDisponibleNoSePuedeComprar` | Aplicación | AB#388 | PR #162 | 🔍 |
| HU9-CP13 | `GestionCarritoServiceTest` | `fijarLaCantidadLaSustituye` | Aplicación | AB#388 | PR #162 | 🔍 |
| HU9-CP13 | `CarritoTest` | `fijarLaCantidadLaSustituye` | Dominio | AB#385 | PR #162 | 🔍 |
| HU9-CP13 | `CarritoTest` | `unaCantidadMenorQueUnoNoSeAcepta` | Dominio | AB#385 | PR #162 | 🔍 |
| HU9-CP13 | `CarritoTest` | `noSePuedeFijarLaCantidadDeUnProductoQueNoEsta` | Dominio | AB#385 | PR #162 | 🔍 |
| HU9-CP13 | `CarritoControllerTest` | `fijarLaCantidadRespondeConElCarrito` | Infraestructura | AB#386 | PR #162 | 🔍 |
| HU9-CP13 | `CarritoControllerTest` | `fijarUnProductoQueNoEstaResponde404` | Infraestructura | AB#386 | PR #162 | 🔍 |
| HU9-CP13 | `CarritoControllerTest` | `unaPeticionIncompletaResponde400` | Infraestructura | AB#386 | PR #162 | 🔍 |
| HU9-CP14 | `GestionCarritoServiceTest` | `quitarYVaciarSeGuardan` | Aplicación | AB#388 | PR #162 | 🔍 |
| HU9-CP14 | `CarritoTest` | `quitarUnProductoDejaLosDemas` | Dominio | AB#385 | PR #162 | 🔍 |
| HU9-CP14 | `CarritoTest` | `vaciarQuitaTodasLasLineas` | Dominio | AB#385 | PR #162 | 🔍 |
| HU9-CP14 | `CarritoControllerTest` | `quitarYVaciarRespondenConElCarrito` | Infraestructura | AB#386 | PR #162 | 🔍 |
| HU9-CP15 | `GestionCarritoServiceTest` | `elCarritoSaleAgrupadoPorProveedor` | Aplicación | AB#388 | PR #162 | 🔍 |
| HU9-CP15 | `GestionCarritoServiceTest` | `unClienteSinCarritoVeUnCarritoVacio` | Aplicación | AB#388 | PR #162 | 🔍 |
| HU9-CP15 | `CarritoControllerTest` | `consultarDevuelveElCarritoAgrupadoPorProveedor` | Infraestructura | AB#386 | PR #162 | 🔍 |
| HU9-CP15 | `VendedoresAdapterTest` | `nombreComercialPreguntaAlModuloDeUsuarios` | Infraestructura | AB#388 | PR #162 | 🔍 |
| HU9-CP15 | `AdministracionUsuariosServiceTest` | `consultarNombreComercialDevuelveElDelVendedor` | Aplicación | AB#388 | PR #162 | 🔍 |
| HU9-CP15 | `AdministracionUsuariosServiceTest` | `consultarNombreComercialDeOtroRolFalla` | Aplicación | AB#388 | PR #162 | 🔍 |
| HU9-CP16 | `CarritoControllerTest` | `soloElClienteUsaElCarrito` | Infraestructura | AB#386 | PR #162 | 🔍 |
| HU9-CP16 | `CarritoControllerTest` | `cambiarElCarritoSinCsrfSeRechaza` | Infraestructura | AB#386 | PR #162 | 🔍 |
| HU9-CP16 | `ClienteEnSesionAdapterTest` | `idPorCorreoPreguntaAlModuloDeUsuarios` | Infraestructura | AB#388 | PR #162 | 🔍 |
| HU9-CP17 | `CarritoDocumentMapperTest` | `convierteADocumentoYVueltaSinPerderDatos` | Infraestructura | AB#385 | PR #162 | 🔍 |
| HU9-CP17 | `CarritoDocumentMapperTest` | `rechazaUnDocumentoQueNoCumpleElDominio` | Infraestructura | AB#385 | PR #162 | 🔍 |
| HU9-CP17 | `CarritoMongoAdapterTest` | `guardarUnCarritoNuevoYRecuperarloPorCliente` | Infraestructura | AB#385 | PR #162 | 🔍 |
| HU9-CP17 | `CarritoMongoAdapterTest` | `guardarOtraVezActualizaElCarrito` | Infraestructura | AB#385 | PR #162 | 🔍 |
| HU9-CP17 | `CarritoMongoAdapterTest` | `laColeccionSoloAdmiteUnCarritoPorCliente` | Infraestructura | AB#385 | PR #162 | 🔍 |
| HU9-CP17 | `CarritoMongoAdapterTest` | `laColeccionRechazaUnaCantidadMenorQueUno` | Infraestructura | AB#385 | PR #162 | 🔍 |
| HU9-CP18 | `GestionCarritoServiceTest` | `alConsultarSeLimpiaElCarrito` | Aplicación | AB#388 | PR #162 | 🔍 |
| HU9-CP18 | `CarritoTest` | `laLimpiezaQuitaLosProductosQueNoSePuedenComprar` | Dominio | AB#385 | PR #162 | 🔍 |
| HU9-CP18 | `CarritoTest` | `laLimpiezaAjustaLaCantidadAlStock` | Dominio | AB#385 | PR #162 | 🔍 |
| HU9-CP18 | `CarritoTest` | `siTodoSePuedeComprarLaLimpiezaNoCambiaNada` | Dominio | AB#385 | PR #162 | 🔍 |
| HU9-CP19 | `GestionCarritoServiceTest` | `elPrecioEsElDelCliente` | Aplicación | AB#388 | PR #162 | 🔍 |
| HU9-CP19 | `ProductosAdapterTest` | `consultarComprablePreguntaAlModuloDeProductos` | Infraestructura | AB#388 | PR #162 | 🔍 |
| HU9-CP19 | `ClienteEnSesionAdapterTest` | `esPremiumPreguntaAlModuloDeUsuarios` | Infraestructura | AB#388 | PR #162 | 🔍 |
| HU9-CP19 | `CatalogoProductosServiceTest` | `consultarUnProductoComprableDevuelveSusDatosYSuPrecioFinal` | Aplicación | AB#388 | PR #162 | 🔍 |
| HU9-CP19 | `ProductoTest` | `sinDescuentosElPrecioFinalEsElPrecio` | Dominio | AB#388 | PR #162 | 🔍 |
| HU9-CP19 | `ProductoTest` | `elClienteEstandarPagaConElDescuentoGeneral` | Dominio | AB#388 | PR #162 | 🔍 |
| HU9-CP19 | `ProductoTest` | `elClientePremiumPagaConElDescuentoPremium` | Dominio | AB#388 | PR #162 | 🔍 |
| HU9-CP19 | `ProductoTest` | `sinDescuentoPremiumElPremiumPagaConElGeneral` | Dominio | AB#388 | PR #162 | 🔍 |
| HU9-CP19 | `AutenticacionServiceTest` | `esClientePremiumDiceSiElClienteEsPremium` | Aplicación | AB#388 | PR #162 | 🔍 |
| HU9-CP20 | `SeguridadCarritoServiceTest` | `unVendedorNoUsaElCarrito` | Aplicación | — | Rama `fix/seguridad-capa-negocio` | 🔍 |
| HU9-CP20 | `SeguridadCarritoServiceTest` | `unAdministradorNoUsaElCarrito` | Aplicación | — | Rama `fix/seguridad-capa-negocio` | 🔍 |
| HU9-CP20 | `SeguridadCarritoServiceTest` | `unClienteNoOperaConElCorreoDeOtro` | Aplicación | — | Rama `fix/seguridad-capa-negocio` | 🔍 |
| HU9-CP20 | `SeguridadCarritoServiceTest` | `unClienteConsultaSuPropioCarrito` | Aplicación | — | Rama `fix/seguridad-capa-negocio` | 🔍 |
| HU9-CP20 | `SeguridadCarritoServiceTest` | `sinSesionSeRechazaTodo` | Aplicación | — | Rama `fix/seguridad-capa-negocio` | 🔍 |

Rutas de las clases de test (bajo `frontend/src/`):
- `AddToCartButton.test.jsx` y `CartWidget.test.jsx`: `features/cart/` (Vitest, React Testing Library y MSW)
- `ProductCard.test.jsx`: `features/catalog/`
- `ProductsPage.test.jsx` y `ProductDetailPage.test.jsx`: `pages/`

Rutas de las clases de test del backend (bajo `backend/src/test/java/es/uclm/esibuy/backend/`):
- `CarritoTest`: `carrito/domain/model/`
- `GestionCarritoServiceTest`: `carrito/application/service/`
- `CarritoDocumentMapperTest` y `CarritoMongoAdapterTest`: `carrito/infrastructure/adapter/out/persistence/` (el segundo, con Testcontainers)
- `ClienteEnSesionAdapterTest` y `VendedoresAdapterTest`: `carrito/infrastructure/adapter/out/usuarios/`
- `ProductosAdapterTest`: `carrito/infrastructure/adapter/out/productos/`
- `CarritoControllerTest`: `carrito/infrastructure/adapter/in/web/`
- `ProductoTest` y `CatalogoProductosServiceTest`: `productos/domain/model/` y `productos/application/service/`
- `AutenticacionServiceTest` y `AdministracionUsuariosServiceTest`: `usuarios/application/service/`

---

## HU10 · Realización de pedido

### Tabla A · HU10 ↔ casos de prueba

| Caso | Descripción | Tipo | Tests | Estado |
|---|---|---|---|---|
| HU10-CP01 | La pasarela de pago solo la ve un cliente (sin sesión lleva al login; otro rol, a su página) y se llega a ella desde «Proceder al pago» | Frontend (página) | 3 | ✅ Pasa |
| HU10-CP02 | La pasarela resume la compra con un pedido por proveedor («Proveedor: total»), con sus productos, el aviso de un pedido por proveedor y el importe total a pagar | Frontend (página) | 4 | ✅ Pasa |
| HU10-CP03 | Con el carrito vacío la pasarela lo indica, no ofrece pagar y permite volver a los productos | Frontend (página) | 1 | ✅ Pasa |
| HU10-CP04 | Al pulsar «Simular pago» se indica que se procesa, se crea un pedido por cada proveedor, se confirma cada uno y el carrito queda vacío | Frontend (página) | 3 | ✅ Pasa |
| HU10-CP05 | Si falla el pedido de un proveedor se confirman los demás, se avisa de cuál ha fallado, su compra se conserva en el carrito y se puede reintentar sin repetir lo ya pagado | Frontend (página) | 3 | ✅ Pasa |
| HU10-CP06 | Cada pedido se pide con un solo proveedor, uno detrás de otro | Frontend (página) | 1 | ✅ Pasa |
| HU10-CP07 | La pasarela lista los métodos de pago guardados del cliente (HU6), con su máscara, marca el predeterminado y deja elegir otro antes de pagar | Frontend (página) | 2 | ✅ Pasa |
| HU10-CP08 | Sin métodos de pago guardados no se puede pagar y se lleva a la vista de métodos de pago (HU6) para añadir uno; si no se pueden cargar, se indica y se puede reintentar | Frontend (página) | 2 | ✅ Pasa |
| HU10-CP09 | Cada pedido se crea con el método de pago elegido (`paymentMethodId`) | Frontend (página) | 1 | 🔍 En revisión (1, AB#133, PR #194) |
| HU10-CP10 | Tras pagar se muestra un recibo con forma de ticket: total pagado (solo de los pedidos creados), fecha, cada pedido con su número, proveedor e importe, y el método de pago usado con su máscara | Frontend (página + componente) | 6 | ✅ Pasa |
| HU10-CP11 | La decoración del recibo no estorba: el código de barras y el confeti quedan ocultos a los lectores de pantalla y no hay confeti si el usuario prefiere reducir el movimiento | Frontend (componente) | 3 | ✅ Pasa |
| HU10-CP12 | El pedido nace PAGADO, con un solo proveedor, al menos una línea, sin productos repetidos, cliente, dirección y método de pago, y su total es la suma de las líneas | Unitario | 5 | 🔍 En revisión (5, AB#380 a AB#382 y AB#131, PR #166) |
| HU10-CP13 | Número de pedido `ESB-AAAA-NNNNNN`: año y correlativo de seis cifras, un contador por año en `contadores`, atómico aunque haya pedidos a la vez | Unitario + integración | 8 | 🔍 En revisión (8, AB#380 a AB#382 y AB#131, PR #166) |
| HU10-CP14 | El pedido toma del carrito solo los productos de su proveedor, con los precios del servidor, los quita del carrito y responde 201 con el contrato del frontend | Unitario + controlador | 5 | 🔍 En revisión (5, AB#380 a AB#382 y AB#131, PR #166) |
| HU10-CP15 | Se descuenta el stock de forma atómica; si falta de algún producto o no se puede guardar, no se crea el pedido, se repone lo descontado y responde 409 `INSUFFICIENT_STOCK` | Unitario + integración + controlador | 11 | 🔍 En revisión (11, AB#380 a AB#382 y AB#131, PR #166) |
| HU10-CP16 | El método de pago y la dirección tienen que ser del cliente (si no, 400) y la dirección se copia en el pedido; una petición incompleta responde 400 | Unitario + controlador | 10 | ✅ Pasa |
| HU10-CP17 | Sin productos de ese proveedor en el carrito no se crea el pedido (409 `CART_EMPTY`) | Unitario + controlador | 3 | 🔍 En revisión (3, AB#380 a AB#382 y AB#131, PR #166) |
| HU10-CP18 | Solo el cliente en sesión realiza pedidos: sin sesión 401, otro rol 403 y sin CSRF se rechaza | Unitario + controlador | 2 | 🔍 En revisión (2, AB#380 a AB#382 y AB#131, PR #166) |
| HU10-CP19 | Persistencia en `pedidos`: el mapper no pierde datos, importes como Decimal128, número de pedido único y con formato validado por la colección | Unitario + integración | 5 | 🔍 En revisión (5, AB#380 a AB#382 y AB#131, PR #166) |
| HU10-CP20 | Realizar un pedido exige el rol cliente y que el correo del comando sea el de la sesión; sin sesión se rechaza. Seguridad en la capa de negocio (R8) | Unitario con Spring Security (sin MongoDB) | 5 | 🔍 En revisión (5, rama `fix/seguridad-capa-negocio`) |
| HU10-CP21 | La pasarela lista las direcciones de envío del cliente (HU5), marca la principal y deja elegir otra antes de pagar | Frontend (página) | 2 | 🔍 En revisión (2, AB#133, PR #194) |
| HU10-CP22 | Sin direcciones de envío no se puede pagar y se lleva a la vista de direcciones (HU5) para añadir una; si no se pueden cargar, se indica y se puede reintentar | Frontend (página) | 2 | 🔍 En revisión (2, AB#133, PR #194) |
| HU10-CP23 | Cada pedido se crea con la dirección de envío elegida (`direccionId`) | Frontend (página) | 1 | 🔍 En revisión (1, AB#133, PR #194) |

Notas:
- **HU10-CP01 a CP06 (Sprint 2, frontend):** casos propuestos en la task AB#401 (vista de la pasarela de pago) a partir de lo pedido (`frontend/docs/hu10-pasarela-de-pago.md`). Pendientes de confirmar con el equipo y de añadir a la HU10 (AB#72) en Azure Boards. La task AB#379 (vista de carrito) es la misma pantalla que la AB#384 (HU9).
- **Backend:** la entidad pedido y su endpoint (AB#380 a AB#382) no existen todavía; los tests usan el contrato propuesto por el frontend y los mocks (MSW). Superado: el backend existe desde el PR #166 y la pasarela lo usa desde PR #194.
- **Decisión del equipo:** cada pedido tiene un solo proveedor, la pasarela da la orden de realización del pedido por proveedor y hay un botón para simular el pago (no se cobra nada).
- **Archivo de tests nuevo** creado sin consulta previa: `CheckoutPage.test.jsx`.
- **HU10-CP07 a CP11 (Sprint 2, frontend):** casos propuestos en la task AB#133 (flujo de checkout: pago y confirmación) y aprobados por el equipo junto con el diseño. Pendientes de añadir a la HU10 (AB#72) en Azure Boards. El ticket se adapta del componente de referencia `AnimatedTicket` (shadcn) a Fluent UI v9, sin instalar nada.
- **Contrato propuesto:** `POST /api/orders` pasa a enviar `{ vendedorId, paymentMethodId }` (campo `paymentMethodId` de la colección `pedidos`), pendiente de acordar con el backend de pedidos (AB#380 a AB#382). Por eso HU10-CP06 compara ahora solo el proveedor de cada pedido. Superado en AB#133 (PR #194): la pasarela usa el contrato real del backend, `POST /api/customer/orders` con `{ vendedorId, direccionId, paymentMethodId }`.
- **Archivo de tests nuevo**, acordado con el equipo: `PaymentTicket.test.jsx`.

- **Backend (HU10-CP12 a CP19):** módulo `pedidos` (AB#380 entidad, AB#382 lógica, AB#381 y AB#130 endpoint, AB#131 número y stock, AB#132 tests), con `POST /api/customer/orders` y `{ vendedorId, direccionId, paymentMethodId }`. Casos propuestos y aprobados por el equipo junto con el diseño; pendientes de añadir a la HU10 (AB#72) en Azure Boards. Decisiones del equipo: la ruta va bajo `/api/customer/**`, el número `ESB-AAAA-NNNNNN` vuelve a empezar cada año, el pedido nace PAGADO y, sin transacciones, lo que falla a mitad se compensa reponiendo el stock.
- **Tests en clases que ya existían** (un archivo de tests por clase): `ProductoMongoAdapterTest` (productos), `GestionMetodosPagoServiceTest` (métodos de pago), `GestionDireccionesServiceTest` (direcciones) e `InicializadorColeccionesTest` (colecciones) ganan casos HU10, por los métodos y la colección nuevos que usa el pedido.
- **HU10-CP20 (R8, seguridad en la capa de negocio):** caso propuesto en la rama `fix/seguridad-capa-negocio`, sin task ni `AB#` por ser un fix transversal. Pendiente de confirmar con el equipo. Comprueba con `@PreAuthorize` que `RealizacionPedidoUseCase` exige el rol cliente y que el correo del comando es el de la sesión. Clase de test nueva `pedidos/application/service/SeguridadPedidosServiceTest` (contexto mínimo de Spring con `@EnableMethodSecurity`, sin MongoDB).
- **HU10-CP21 a CP23 (frontend):** casos propuestos en la task AB#133 (pasarela con dirección de envío y endpoint real) y aprobados por el equipo junto con el diseño. Pendientes de añadir a la HU10 (AB#72) en Azure Boards. HU10-CP09 cambia porque cada pedido envía también la `direccionId`; los tests de CP05, CP06 y CP10 que espían la petición solo cambian la ruta a `/api/customer/orders`. Sin archivos de tests nuevos.

### Tabla B · casos de prueba ↔ tests

| Caso | Clase de test | Método | Capa | Task | Origen | Estado |
|---|---|---|---|---|---|---|
| HU10-CP01 | `CheckoutPage.test.jsx` | `HU10-CP01: sin sesión, la pasarela de pago lleva a iniciar sesión` | Frontend · página | AB#401 | PR #146 | ✅ |
| HU10-CP01 | `CheckoutPage.test.jsx` | `HU10-CP01: un vendedor no ve la pasarela del cliente: va a su página` | Frontend · página | AB#401 | PR #146 | ✅ |
| HU10-CP01 | `CheckoutPage.test.jsx` | `HU10-CP01: desde el carrito, «Proceder al pago» lleva a la pasarela` | Frontend · página | AB#401 | PR #146 | ✅ |
| HU10-CP02 | `CheckoutPage.test.jsx` | `HU10-CP02: muestra un pedido por proveedor, con la cabecera «Proveedor: total»` | Frontend · página | AB#401 | PR #146 | ✅ |
| HU10-CP02 | `CheckoutPage.test.jsx` | `HU10-CP02: cada pedido lista solo los productos de su proveedor, con cantidad y precio` | Frontend · página | AB#401 | PR #146 | ✅ |
| HU10-CP02 | `CheckoutPage.test.jsx` | `HU10-CP02: avisa de que se hace un pedido independiente por cada proveedor` | Frontend · página | AB#401 | PR #146 | ✅ |
| HU10-CP02 | `CheckoutPage.test.jsx` | `HU10-CP02: muestra el importe total a pagar` | Frontend · página | AB#401 | PR #146 | ✅ |
| HU10-CP03 | `CheckoutPage.test.jsx` | `HU10-CP03: con el carrito vacío lo indica, no ofrece pagar y permite volver a los productos` | Frontend · página | AB#401 | PR #146 | ✅ |
| HU10-CP04 | `CheckoutPage.test.jsx` | `HU10-CP04: mientras se procesa el pago lo indica y no deja pulsar otra vez` | Frontend · página | AB#401 | PR #146 | ✅ |
| HU10-CP04 | `CheckoutPage.test.jsx` | `HU10-CP04: tras pagar se confirma un pedido por cada proveedor` | Frontend · página | AB#401 | PR #146 | ✅ |
| HU10-CP04 | `CheckoutPage.test.jsx` | `HU10-CP04: tras pagar el carrito queda vacío` | Frontend · página | AB#401 | PR #146 | ✅ |
| HU10-CP05 | `CheckoutPage.test.jsx` | `HU10-CP05: confirma los pedidos que sí se han creado e indica cuál ha fallado` | Frontend · página | AB#401 | PR #146 | ✅ |
| HU10-CP05 | `CheckoutPage.test.jsx` | `HU10-CP05: la compra del pedido que ha fallado se conserva en el carrito` | Frontend · página | AB#401 | PR #146 | ✅ |
| HU10-CP05 | `CheckoutPage.test.jsx` | `HU10-CP05: se puede reintentar el pago de lo que falló, sin repetir lo ya pagado` | Frontend · página | AB#401 | PR #146 | ✅ |
| HU10-CP06 | `CheckoutPage.test.jsx` | `HU10-CP06: cada pedido se pide con un solo proveedor, uno detrás de otro` | Frontend · página | AB#401 | PR #146; modificado en PR #157 | ✅ |
| HU10-CP07 | `CheckoutPage.test.jsx` | `HU10-CP07: lista los métodos de pago guardados del cliente y marca el predeterminado` | Frontend · página | AB#133 | PR #157 |✅ |
| HU10-CP07 | `CheckoutPage.test.jsx` | `HU10-CP07: se puede elegir otro método de pago antes de pagar` | Frontend · página | AB#133 | PR #157 |✅ |
| HU10-CP08 | `CheckoutPage.test.jsx` | `HU10-CP08: sin métodos de pago guardados no deja pagar y lleva a sus métodos de pago para añadir uno` | Frontend · página | AB#133 | PR #157 |✅ |
| HU10-CP08 | `CheckoutPage.test.jsx` | `HU10-CP08: si no se pueden cargar los métodos de pago lo indica y deja reintentar` | Frontend · página | AB#133 | PR #157 |✅ |
| HU10-CP09 | `CheckoutPage.test.jsx` | `HU10-CP09: cada pedido se crea con el método de pago elegido` | Frontend · página | AB#133 | PR #157; modificado en PR #194 | 🔍 |
| HU10-CP10 | `CheckoutPage.test.jsx` | `HU10-CP10: tras pagar muestra un recibo con el total pagado, cada pedido y el método de pago usado` | Frontend · página | AB#133 | PR #157 |✅ |
| HU10-CP10 | `CheckoutPage.test.jsx` | `HU10-CP10: si falla un pedido, el recibo solo cobra los pedidos creados` | Frontend · página | AB#133 | PR #157 |✅ |
| HU10-CP10 | `PaymentTicket.test.jsx` | `HU10-CP10: muestra el total pagado, la suma de los pedidos` | Frontend · componente | AB#133 | PR #157 |✅ |
| HU10-CP10 | `PaymentTicket.test.jsx` | `HU10-CP10: muestra la fecha del pago` | Frontend · componente | AB#133 | PR #157 |✅ |
| HU10-CP10 | `PaymentTicket.test.jsx` | `HU10-CP10: lista cada pedido con su número, su proveedor y su importe` | Frontend · componente | AB#133 | PR #157 |✅ |
| HU10-CP10 | `PaymentTicket.test.jsx` | `HU10-CP10: muestra el método de pago usado solo con su máscara` | Frontend · componente | AB#133 | PR #157 |✅ |
| HU10-CP11 | `PaymentTicket.test.jsx` | `HU10-CP11: el código de barras es decorativo y queda oculto a los lectores de pantalla` | Frontend · componente | AB#133 | PR #157 |✅ |
| HU10-CP11 | `PaymentTicket.test.jsx` | `HU10-CP11: celebra el pago con confeti decorativo` | Frontend · componente | AB#133 | PR #157 |✅ |
| HU10-CP11 | `PaymentTicket.test.jsx` | `HU10-CP11: sin confeti si el usuario prefiere reducir el movimiento` | Frontend · componente | AB#133 | PR #157 |✅ |
| HU10-CP12 | `PedidoTest` | `unPedidoNuevoNacePagadoConSuTotal` | Dominio | AB#380 | PR #166 | 🔍 |
| HU10-CP12 | `PedidoTest` | `unPedidoSinLineasNoEsValido` | Dominio | AB#380 | PR #166 | 🔍 |
| HU10-CP12 | `PedidoTest` | `unPedidoSinDatosObligatoriosNoEsValido` | Dominio | AB#380 | PR #166 | 🔍 |
| HU10-CP12 | `PedidoTest` | `unaLineaNecesitaCantidadYPrecioValidos` | Dominio | AB#380 | PR #166 | 🔍 |
| HU10-CP12 | `PedidoTest` | `unProductoSoloApareceEnUnaLinea` | Dominio | AB#380 | PR #166 | 🔍 |
| HU10-CP13 | `RealizacionPedidoServiceTest` | `cadaPedidoRecibeElSiguienteNumeroDelAno` | Aplicación | AB#382 | PR #166 | 🔍 |
| HU10-CP13 | `NumeroPedidoTest` | `elNumeroLlevaElAnoYElCorrelativo` | Dominio | AB#380 | PR #166 | 🔍 |
| HU10-CP13 | `NumeroPedidoTest` | `elCorrelativoEstaEntreUnoYSeisCifras` | Dominio | AB#380 | PR #166 | 🔍 |
| HU10-CP13 | `NumeroPedidoTest` | `unNumeroConOtroFormatoNoEsValido` | Dominio | AB#380 | PR #166 | 🔍 |
| HU10-CP13 | `ContadorPedidosMongoAdapterTest` | `elContadorEmpiezaEnUnoYSube` | Infraestructura | AB#131 | PR #166 | 🔍 |
| HU10-CP13 | `ContadorPedidosMongoAdapterTest` | `cadaAnoTieneSuPropioContador` | Infraestructura | AB#131 | PR #166 | 🔍 |
| HU10-CP13 | `ContadorPedidosMongoAdapterTest` | `dosPedidosALaVezNoRecibenElMismoNumero` | Infraestructura | AB#131 | PR #166 | 🔍 |
| HU10-CP13 | `InicializadorColeccionesTest` | `elContadorDePedidosEsUnNumeroDeAlMenosUno` | Infraestructura | AB#131 | PR #166 | 🔍 |
| HU10-CP14 | `RealizacionPedidoServiceTest` | `elPedidoLlevaSoloLosProductosDeSuProveedor` | Aplicación | AB#382 | PR #166 | 🔍 |
| HU10-CP14 | `RealizacionPedidoServiceTest` | `trasCrearElPedidoSusProductosSalenDelCarrito` | Aplicación | AB#382 | PR #166 | 🔍 |
| HU10-CP14 | `PedidoControllerTest` | `realizarUnPedidoResponde201ConElPedido` | Infraestructura | AB#381 | PR #166 | 🔍 |
| HU10-CP14 | `CarritoAdapterTest` | `laCompraDeUnProveedorSaleDeSuGrupo` | Infraestructura | AB#382 | PR #166 | 🔍 |
| HU10-CP14 | `CarritoAdapterTest` | `quitarLosProductosDelPedidoLosQuitaDelCarrito` | Infraestructura | AB#382 | PR #166 | 🔍 |
| HU10-CP15 | `RealizacionPedidoServiceTest` | `crearElPedidoDescuentaElStock` | Aplicación | AB#382 | PR #166 | 🔍 |
| HU10-CP15 | `RealizacionPedidoServiceTest` | `siFaltaStockNoSeCreaElPedidoYSeRepone` | Aplicación | AB#382 | PR #166 | 🔍 |
| HU10-CP15 | `RealizacionPedidoServiceTest` | `siNoSePuedeGuardarSeReponeElStock` | Aplicación | AB#382 | PR #166 | 🔍 |
| HU10-CP15 | `PedidoControllerTest` | `siFaltaStockResponde409` | Infraestructura | AB#381 | PR #166 | 🔍 |
| HU10-CP15 | `ExistenciasAdapterTest` | `descontarYReponerSePidenAlModuloDeProductos` | Infraestructura | AB#131 | PR #166 | 🔍 |
| HU10-CP15 | `ExistenciasProductosServiceTest` | `descontarDiceSiHabiaStockSuficiente` | Aplicación | AB#131 | PR #166 | 🔍 |
| HU10-CP15 | `ExistenciasProductosServiceTest` | `reponerDevuelveLasUnidades` | Aplicación | AB#131 | PR #166 | 🔍 |
| HU10-CP15 | `ExistenciasProductosServiceTest` | `noSeUsanCantidadesMenoresQueUno` | Aplicación | AB#131 | PR #166 | 🔍 |
| HU10-CP15 | `ProductoMongoAdapterTest` | `descontarStockLoRestaSiHaySuficiente` | Infraestructura | AB#131 | PR #166 | 🔍 |
| HU10-CP15 | `ProductoMongoAdapterTest` | `dosDescuentosALaVezNoDejanElStockNegativo` | Infraestructura | AB#131 | PR #166 | 🔍 |
| HU10-CP15 | `ProductoMongoAdapterTest` | `reponerStockDevuelveLasUnidades` | Infraestructura | AB#131 | PR #166 | 🔍 |
| HU10-CP16 | `GestionDireccionesServiceTest` | `consultarParaPedidoDevuelveLosDatosDeUnaDireccionPropia` | Aplicación | AB#382 | PR #166 | ✅ |
| HU10-CP16 | `GestionDireccionesServiceTest` | `consultarParaPedidoNoDevuelveUnaDireccionAjena` | Aplicación | AB#382 | PR #166 | ✅ |
| HU10-CP16 | `GestionMetodosPagoServiceTest` | `esDelClienteSoloAceptaUnMetodoPropio` | Aplicación | AB#382 | PR #166 | ✅ |
| HU10-CP16 | `RealizacionPedidoServiceTest` | `elMetodoDePagoTieneQueSerDelCliente` | Aplicación | AB#382 | PR #166 | ✅ |
| HU10-CP16 | `RealizacionPedidoServiceTest` | `laDireccionTieneQueSerDelClienteYSeCopia` | Aplicación | AB#382 | PR #166 | ✅ |
| HU10-CP16 | `PedidoControllerTest` | `unMetodoODireccionAjenosResponden400` | Infraestructura | AB#381 | PR #166 | ✅ |
| HU10-CP16 | `PedidoControllerTest` | `unaPeticionIncompletaResponde400` | Infraestructura | AB#381 | PR #166 | ✅ |
| HU10-CP16 | `DireccionesAdapterTest` | `losDatosDeLaDireccionSeCopian` | Infraestructura | AB#382 | PR #166 | ✅ |
| HU10-CP16 | `DireccionesAdapterTest` | `unaDireccionAjenaNoSeDevuelve` | Infraestructura | AB#382 | PR #166 | ✅ |
| HU10-CP16 | `MetodosPagoAdapterTest` | `esDelClientePreguntaAlModuloDeMetodosDePago` | Infraestructura | AB#382 | PR #166 | ✅ |
| HU10-CP17 | `RealizacionPedidoServiceTest` | `sinProductosDelProveedorNoSeCreaElPedido` | Aplicación | AB#382 | PR #166 | 🔍 |
| HU10-CP17 | `PedidoControllerTest` | `sinProductosDelProveedorResponde409` | Infraestructura | AB#381 | PR #166 | 🔍 |
| HU10-CP17 | `CarritoAdapterTest` | `sinProductosDelProveedorNoHayCompra` | Infraestructura | AB#382 | PR #166 | 🔍 |
| HU10-CP18 | `PedidoControllerTest` | `soloElClienteRealizaPedidos` | Infraestructura | AB#381 | PR #166 | 🔍 |
| HU10-CP18 | `ClienteEnSesionAdapterTest` | `idPorCorreoPreguntaAlModuloDeUsuarios` | Infraestructura | AB#382 | PR #166 | 🔍 |
| HU10-CP19 | `PedidoDocumentMapperTest` | `convierteADocumentoYVueltaSinPerderDatos` | Infraestructura | AB#380 | PR #166 | 🔍 |
| HU10-CP19 | `PedidoDocumentMapperTest` | `rechazaUnDocumentoQueNoCumpleElDominio` | Infraestructura | AB#380 | PR #166 | 🔍 |
| HU10-CP19 | `PedidoMongoAdapterTest` | `guardarUnPedidoLeAsignaUnId` | Infraestructura | AB#380 | PR #166 | 🔍 |
| HU10-CP19 | `PedidoMongoAdapterTest` | `dosPedidosNoPuedenTenerElMismoNumero` | Infraestructura | AB#380 | PR #166 | 🔍 |
| HU10-CP19 | `InicializadorColeccionesTest` | `elNumeroDePedidoTieneFormatoYNoSeRepite` | Infraestructura | AB#380 | PR #166 | 🔍 |
| HU10-CP20 | `SeguridadPedidosServiceTest` | `unVendedorNoRealizaPedidos` | Aplicación | — | Rama `fix/seguridad-capa-negocio` | 🔍 |
| HU10-CP20 | `SeguridadPedidosServiceTest` | `unAdministradorNoRealizaPedidos` | Aplicación | — | Rama `fix/seguridad-capa-negocio` | 🔍 |
| HU10-CP20 | `SeguridadPedidosServiceTest` | `unClienteNoPideConElCorreoDeOtro` | Aplicación | — | Rama `fix/seguridad-capa-negocio` | 🔍 |
| HU10-CP20 | `SeguridadPedidosServiceTest` | `unClientePideConSuPropioCorreo` | Aplicación | — | Rama `fix/seguridad-capa-negocio` | 🔍 |
| HU10-CP20 | `SeguridadPedidosServiceTest` | `sinSesionSeRechazaElPedido` | Aplicación | — | Rama `fix/seguridad-capa-negocio` | 🔍 |
| HU10-CP21 | `CheckoutPage.test.jsx` | `HU10-CP21: lista las direcciones de envío del cliente y marca la principal` | Frontend · página | AB#133 | PR #194 | 🔍 |
| HU10-CP21 | `CheckoutPage.test.jsx` | `HU10-CP21: se puede elegir otra dirección de envío antes de pagar` | Frontend · página | AB#133 | PR #194 | 🔍 |
| HU10-CP22 | `CheckoutPage.test.jsx` | `HU10-CP22: sin direcciones de envío no deja pagar y lleva a sus direcciones para añadir una` | Frontend · página | AB#133 | PR #194 | 🔍 |
| HU10-CP22 | `CheckoutPage.test.jsx` | `HU10-CP22: si no se pueden cargar las direcciones de envío lo indica y deja reintentar` | Frontend · página | AB#133 | PR #194 | 🔍 |
| HU10-CP23 | `CheckoutPage.test.jsx` | `HU10-CP23: cada pedido se crea con la dirección de envío elegida` | Frontend · página | AB#133 | PR #194 | 🔍 |

Ruta de la clase de test (bajo `frontend/src/`):
- `CheckoutPage.test.jsx`: `pages/` (Vitest, React Testing Library y MSW)
- `PaymentTicket.test.jsx`: `features/checkout/`

Rutas de las clases de test del backend (bajo `backend/src/test/java/es/uclm/esibuy/backend/`):
- `PedidoTest` y `NumeroPedidoTest`: `pedidos/domain/model/`
- `RealizacionPedidoServiceTest`: `pedidos/application/service/`
- `PedidoDocumentMapperTest`, `PedidoMongoAdapterTest` y `ContadorPedidosMongoAdapterTest`: `pedidos/infrastructure/adapter/out/persistence/` (los dos últimos, con Testcontainers)
- `CarritoAdapterTest`, `MetodosPagoAdapterTest`, `DireccionesAdapterTest`, `ExistenciasAdapterTest` y `ClienteEnSesionAdapterTest`: `pedidos/infrastructure/adapter/out/` (`carrito/`, `metodospago/`, `direcciones/`, `productos/` y `usuarios/`)
- `PedidoControllerTest`: `pedidos/infrastructure/adapter/in/web/`
- `ExistenciasProductosServiceTest` y `ProductoMongoAdapterTest`: `productos/application/service/` y `productos/infrastructure/adapter/out/persistence/`
- `GestionMetodosPagoServiceTest` y `GestionDireccionesServiceTest`: `metodospago/application/service/` y `direcciones/application/service/`
- `InicializadorColeccionesTest`: `shared/infrastructure/config/`

---

## HU11 · Historial y seguimiento de pedidos

### Tabla A · HU11 ↔ casos de prueba

| Caso | Descripción | Tipo | Tests | Estado |
|---|---|---|---|---|
| HU11-CP01 | El historial devuelve los pedidos del cliente de la sesión, del más reciente al más antiguo (vacío si no tiene); se consulta con su id, no con su correo | Unitario (aplicación) | 2 | ✅ Pasa |
| HU11-CP02 | El detalle devuelve el pedido del cliente con sus productos, precios y estado | Unitario (aplicación) | 1 | ✅ Pasa |
| HU11-CP03 | El pedido de otro cliente se trata como si no existiera, igual que uno inexistente (`PedidoNoEncontradoException`) | Unitario (aplicación) | 1 | ✅ Pasa |
| HU11-CP04 | En MongoDB se buscan los pedidos de un cliente, del más reciente al más antiguo, y un pedido por su id con sus líneas | Integración (Testcontainers) | 2 | ✅ Pasa |
| HU11-CP05 | Los estados de envío van en el orden en que avanza un pedido (`PAGADO`, `PREPARANDO`, `ENVIADO`, `ENTREGADO`) con `CANCELADO` al final, y cada uno se guarda y se lee igual | Unitario (dominio e infraestructura) | 2 | ✅ Pasa |
| HU11-CP06 | Solo el cliente consulta su historial y el detalle de sus pedidos: vendedor, administrador, otro cliente y sin sesión, rechazados | Seguridad (`@PreAuthorize`, contexto mínimo) | 5 | ✅ Pasa |
| HU11-CP07 | `GET /api/customer/orders` responde 200 con los pedidos completos del cliente de la sesión (`[]` si no tiene); no expone el cliente | Integración web (`@WebMvcTest`) | 2 | ✅ Pasa |
| HU11-CP08 | `GET /api/customer/orders/{id}` responde 200 con el pedido: productos, desglose de precios, total, estado y dirección de envío | Integración web (`@WebMvcTest`) | 1 | ✅ Pasa |
| HU11-CP09 | Un pedido que no existe o es de otro cliente responde 400 con el code `ORDER_NOT_FOUND` («El pedido no existe») | Integración web (`@WebMvcTest`) | 1 | ✅ Pasa |
| HU11-CP10 | Sin sesión el historial y el detalle responden 401, y con otro rol (vendedor, administrador) 403 | Integración web (`@WebMvcTest`) | 1 | ✅ Pasa |
| HU11-CP11 | Mis pedidos muestra los pedidos del cliente, del más reciente al más antiguo, con número, fecha, vendedor, total y estado; sin pedidos lo indica, y si falla permite reintentar | Frontend (página) | 3 | ✅ Pasa |
| HU11-CP12 | Al seleccionar un pedido se abre su detalle | Frontend (página) | 1 | ✅ Pasa |
| HU11-CP13 | Mis pedidos es solo del cliente: sin sesión lleva al login y un vendedor va a su página | Frontend (página) | 2 | ✅ Pasa |
| HU11-CP14 | El detalle muestra número, fecha, vendedor y dirección de envío, y desglosa cada producto (cantidad, precio unitario, subtotal) y el total | Frontend (página) | 2 | ✅ Pasa |
| HU11-CP15 | La línea de estados del envío (Pagado → Preparando → Enviado → Entregado) marca los pasos hechos, el actual y los pendientes | Frontend (página) | 2 | ✅ Pasa |
| HU11-CP16 | Un pedido cancelado lo indica en lugar de la línea de estados del envío | Frontend (página) | 1 | ✅ Pasa |
| HU11-CP17 | Un pedido inexistente o de otro cliente se ve como no encontrado; otro error permite reintentar; desde el detalle se vuelve a Mis pedidos | Frontend (página) | 3 | ✅ Pasa |
| HU11-CP18 | La cabecera del cliente tiene el enlace «Mis pedidos»; la del vendedor y la del administrador, no | Frontend (componente) | 2 | ✅ Pasa |
| HU11-CP19 | Tras pagar (HU10) se puede ir a Mis pedidos | Frontend (página) | 1 | ✅ Pasa |

Notas:
- **HU11-CP01 a CP06 (Sprint 2, backend, TDD):** casos propuestos en la T378 (AB#378, lógica de obtención de pedidos) a partir del criterio de aceptación de la HU11 (AB#73): «el sistema muestra el detalle de los productos, desglose de precios y la máquina de estados de envío cuando el cliente selecciona un pedido concreto». Pendientes de confirmar con el equipo y de añadir a la HU11 en Azure Boards.
- **Decisiones del equipo:** el historial y el detalle se añaden a `ConsultaComprasUseCase` (mismo actor y propósito que `haComprado` de la HU13). Estados de envío: `PAGADO → PREPARANDO → ENVIADO → ENTREGADO`, más `CANCELADO` para la HU12; los hará avanzar el vendedor en otra HU. Un pedido ajeno se trata como inexistente.
- **HU11-CP07 a CP10 (Sprint 2, backend, TDD):** casos propuestos en la T377 (AB#377, endpoint de obtención de pedidos): `GET /api/customer/orders` (historial, cada pedido completo con el formato de `PedidoResponse`) y `GET /api/customer/orders/{id}` (detalle). Un pedido ajeno o inexistente responde 400 `ORDER_NOT_FOUND`, como pide el CLAUDE.md para los pedidos. HU11-CP10 pasó sin rojo real: `SecurityConfig` ya protege `/api/customer/**`. Pendientes de confirmar con el equipo y de añadir a la HU11 en Azure Boards.
- **HU11-CP11 a CP19 (Sprint 2, frontend, TDD):** casos propuestos en la T376 (AB#376, vista de pedidos): «Mis pedidos» (`/orders`) y el detalle (`/orders/:id`) con la línea de estados del envío, el enlace en la cabecera y «Ver mis pedidos» tras pagar (`frontend/docs/hu11-historial-pedidos.md`). El test «el vendedor y el administrador no tienen el enlace» (CP18) pasó sin rojo real. El PR #187 se mergeó antes de añadirle estas filas, así que entran en la rama de la T375 (HU12). Pendientes de confirmar con el equipo y de añadir a la HU11 en Azure Boards.
- **Pendiente:** `haComprado` (HU13) solo cuenta los pedidos `PAGADO`: cuando los estados avancen, habrá que decidir si `ENTREGADO` cuenta como compra (comentario «PENDIENTE DE DECIDIR» en `ConsultaComprasService`).

### Tabla B · casos de prueba ↔ tests

| Caso | Clase de test | Método | Capa | Task | Origen | Estado |
|---|---|---|---|---|---|---|
| HU11-CP01 | `ConsultaComprasServiceTest` | `listarPedidosDevuelveLosDelClienteEnElOrdenDelRepositorio` | Aplicación | AB#378 | PR #182 | ✅ |
| HU11-CP01 | `ConsultaComprasServiceTest` | `listarPedidosSinPedidosDevuelveUnaListaVacia` | Aplicación | AB#378 | PR #182 | ✅ |
| HU11-CP02 | `ConsultaComprasServiceTest` | `consultarPedidoDevuelveElPedidoDelCliente` | Aplicación | AB#378 | PR #182 | ✅ |
| HU11-CP03 | `ConsultaComprasServiceTest` | `consultarPedidoAjenoOInexistenteNoLoRevela` | Aplicación | AB#378 | PR #182 | ✅ |
| HU11-CP04 | `PedidoMongoAdapterTest` | `buscarPorClienteDevuelveSusPedidosDelMasRecienteAlMasAntiguo` | Infraestructura | AB#378 | PR #182 | ✅ |
| HU11-CP04 | `PedidoMongoAdapterTest` | `buscarPorIdDevuelveElPedidoConSusLineas` | Infraestructura | AB#378 | PR #182 | ✅ |
| HU11-CP05 | `EstadoPedidoTest` | `losEstadosVanEnElOrdenDelEnvio` | Dominio | AB#378 | PR #182 | ✅ |
| HU11-CP05 | `PedidoDocumentMapperTest` | `cadaEstadoDeEnvioSeGuardaYSeLee` | Infraestructura | AB#378 | PR #182 | ✅ |
| HU11-CP06 | `SeguridadPedidosServiceTest` | `unVendedorNoConsultaElHistorial` | Aplicación (seguridad) | AB#378 | PR #182 | ✅ |
| HU11-CP06 | `SeguridadPedidosServiceTest` | `unAdministradorNoConsultaElHistorial` | Aplicación (seguridad) | AB#378 | PR #182 | ✅ |
| HU11-CP06 | `SeguridadPedidosServiceTest` | `unClienteNoConsultaLosPedidosDeOtro` | Aplicación (seguridad) | AB#378 | PR #182 | ✅ |
| HU11-CP06 | `SeguridadPedidosServiceTest` | `unClienteConsultaSuPropioHistorial` | Aplicación (seguridad) | AB#378 | PR #182 | ✅ |
| HU11-CP06 | `SeguridadPedidosServiceTest` | `sinSesionSeRechazaConsultarPedidos` | Aplicación (seguridad) | AB#378 | PR #182 | ✅ |
| HU11-CP07 | `PedidoControllerTest` | `elHistorialResponde200ConLosPedidosDelCliente` | Infraestructura (web) | AB#377 | PR #186 | ✅ |
| HU11-CP07 | `PedidoControllerTest` | `unClienteSinPedidosRecibeUnaListaVacia` | Infraestructura (web) | AB#377 | PR #186 | ✅ |
| HU11-CP08 | `PedidoControllerTest` | `elDetalleResponde200ConElPedido` | Infraestructura (web) | AB#377 | PR #186 | ✅ |
| HU11-CP09 | `PedidoControllerTest` | `unPedidoInexistenteOAjenoResponde400` | Infraestructura (web) | AB#377 | PR #186 | ✅ |
| HU11-CP10 | `PedidoControllerTest` | `soloElClienteConsultaSusPedidos` | Infraestructura (web) | AB#377 | PR #186 | ✅ |
| HU11-CP11 | `OrdersPage.test.jsx` | `HU11-CP11: muestra los pedidos del cliente, del más reciente al más antiguo, con número, fecha, vendedor, total y estado` | Frontend · página | AB#376 | PR #187 | ✅ |
| HU11-CP11 | `OrdersPage.test.jsx` | `HU11-CP11: sin pedidos lo indica y ofrece ir a los productos` | Frontend · página | AB#376 | PR #187 | ✅ |
| HU11-CP11 | `OrdersPage.test.jsx` | `HU11-CP11: si no se pueden cargar los pedidos lo avisa y permite reintentar` | Frontend · página | AB#376 | PR #187 | ✅ |
| HU11-CP12 | `OrdersPage.test.jsx` | `HU11-CP12: al pulsar un pedido se abre su detalle` | Frontend · página | AB#376 | PR #187 | ✅ |
| HU11-CP13 | `OrdersPage.test.jsx` | `HU11-CP13: sin sesión, Mis pedidos lleva a iniciar sesión` | Frontend · página | AB#376 | PR #187 | ✅ |
| HU11-CP13 | `OrdersPage.test.jsx` | `HU11-CP13: un vendedor no ve los pedidos de los clientes: va a su página` | Frontend · página | AB#376 | PR #187 | ✅ |
| HU11-CP14 | `OrderDetailPage.test.jsx` | `HU11-CP14: muestra el número, la fecha, el vendedor y la dirección de envío del pedido` | Frontend · página | AB#376 | PR #187 | ✅ |
| HU11-CP14 | `OrderDetailPage.test.jsx` | `HU11-CP14: desglosa cada producto con su cantidad, precio unitario y subtotal, y el total` | Frontend · página | AB#376 | PR #187 | ✅ |
| HU11-CP15 | `OrderDetailPage.test.jsx` | `HU11-CP15: la línea de estados del envío marca los pasos hechos, el actual y los pendientes` | Frontend · página | AB#376 | PR #187 | ✅ |
| HU11-CP15 | `OrderDetailPage.test.jsx` | `HU11-CP15: un pedido entregado tiene todos los pasos hechos y el último como actual` | Frontend · página | AB#376 | PR #187 | ✅ |
| HU11-CP16 | `OrderDetailPage.test.jsx` | `HU11-CP16: un pedido cancelado lo indica en lugar de la línea de estados del envío` | Frontend · página | AB#376 | PR #187 | ✅ |
| HU11-CP17 | `OrderDetailPage.test.jsx` | `HU11-CP17: un pedido que no existe o es de otro cliente se muestra como no encontrado` | Frontend · página | AB#376 | PR #187 | ✅ |
| HU11-CP17 | `OrderDetailPage.test.jsx` | `HU11-CP17: si falla por otro motivo lo avisa y permite reintentar` | Frontend · página | AB#376 | PR #187 | ✅ |
| HU11-CP17 | `OrderDetailPage.test.jsx` | `HU11-CP17: desde el detalle se vuelve a Mis pedidos` | Frontend · página | AB#376 | PR #187 | ✅ |
| HU11-CP18 | `AppHeader.test.jsx` | `HU11-CP18: el cliente tiene el enlace a sus pedidos y lleva a Mis pedidos` | Frontend · componente | AB#376 | PR #187 | ✅ |
| HU11-CP18 | `AppHeader.test.jsx` | `HU11-CP18: el vendedor y el administrador no tienen el enlace a Mis pedidos` | Frontend · componente | AB#376 | PR #187 | ✅ |
| HU11-CP19 | `CheckoutPage.test.jsx` | `HU11-CP19: tras pagar se puede ir a Mis pedidos` | Frontend · página | AB#376 | PR #187 | ✅ |

Rutas de las clases de test (bajo `backend/src/test/java/es/uclm/esibuy/backend/pedidos/`):
- `ConsultaComprasServiceTest` y `SeguridadPedidosServiceTest`: `application/service/` (el primero sin Spring; el segundo con un contexto mínimo y `@WithMockUser`)
- `EstadoPedidoTest`: `domain/model/` (JUnit)
- `PedidoMongoAdapterTest` y `PedidoDocumentMapperTest`: `infrastructure/adapter/out/persistence/` (el primero con Testcontainers)
- `PedidoControllerTest`: `infrastructure/adapter/in/web/` (`@WebMvcTest` con `SecurityConfig`, sin MongoDB)

Rutas de los tests del frontend (bajo `frontend/src/`, Vitest con React Testing Library y MSW):
- `OrdersPage.test.jsx`, `OrderDetailPage.test.jsx` y `CheckoutPage.test.jsx`: `pages/`
- `AppHeader.test.jsx`: `components/`

---

## HU12 · Cancelación de pedido

### Tabla A · HU12 ↔ casos de prueba

| Caso | Descripción | Tipo | Tests | Estado |
|---|---|---|---|---|
| HU12-CP01 | Un pedido que aún no se ha enviado (`PAGADO` o `PREPARANDO`) se puede cancelar y pasa a `CANCELADO` | Unitario (dominio) | 1 | ✅ Pasa |
| HU12-CP02 | Un pedido `ENVIADO`, `ENTREGADO` o ya `CANCELADO` no se puede cancelar (`PedidoNoCancelableException`) y no cambia | Unitario (dominio) | 1 | ✅ Pasa |
| HU12-CP03 | Cancelar un pedido del cliente lo guarda cancelado y lo devuelve | Unitario (aplicación) | 1 | ✅ Pasa |
| HU12-CP04 | Al cancelar se repone el stock de cada producto del pedido, después de guardar la cancelación | Unitario (aplicación) | 1 | ✅ Pasa |
| HU12-CP05 | Un pedido de otro cliente o que no existe no se cancela y se trata como inexistente (`PedidoNoEncontradoException`) | Unitario (aplicación) | 1 | ✅ Pasa |
| HU12-CP06 | Un pedido enviado o ya cancelado no se cancela: ni se guarda ni se repone stock (cancelar dos veces repondría el stock dos veces) | Unitario (aplicación) | 1 | ✅ Pasa |
| HU12-CP07 | Solo el cliente cancela sus pedidos: vendedor, administrador, otro cliente y sin sesión, rechazados | Seguridad (`@PreAuthorize`, contexto mínimo) | 5 | ✅ Pasa |
| HU12-CP08 | En MongoDB, guardar un pedido leído y cancelado actualiza el mismo documento, sin crear otro | Integración (Testcontainers) | 1 | ✅ Pasa |
| HU12-CP09 | `POST /api/customer/orders/{id}/cancel` responde 200 con el pedido ya cancelado (`CANCELADO`), sin exponer el cliente | Integración web (`@WebMvcTest`) | 1 | ✅ Pasa |
| HU12-CP10 | Un pedido que ya no se puede cancelar (enviado, entregado o ya cancelado) responde 409 con el code `ORDER_NOT_CANCELLABLE` | Integración web (`@WebMvcTest`) | 1 | ✅ Pasa |
| HU12-CP11 | Cancelar un pedido que no existe o es de otro cliente responde 400 con el code `ORDER_NOT_FOUND` | Integración web (`@WebMvcTest`) | 1 | ✅ Pasa |
| HU12-CP12 | Sin sesión cancelar responde 401, con otro rol 403 y sin el token CSRF se rechaza (403) | Integración web (`@WebMvcTest`) | 1 | ✅ Pasa |
| HU12-CP13 | El detalle de un pedido pagado o en preparación tiene el botón «Cancelar pedido»; uno enviado, entregado o cancelado, no | Frontend (página) | 3 | 🔍 En revisión (3, AB#372, PR sin abrir) |
| HU12-CP14 | Al pulsar «Cancelar pedido» se pide confirmación y todavía no se cancela | Frontend (página) | 1 | 🔍 En revisión (1, AB#372, PR sin abrir) |
| HU12-CP15 | La confirmación nombra el pedido y avisa de que no se puede deshacer; «Volver» cierra sin cancelar | Frontend (componente) | 2 | 🔍 En revisión (2, AB#373, PR sin abrir) |
| HU12-CP16 | Al confirmar se cancela el pedido y el detalle lo muestra cancelado, con el aviso «Pedido cancelado.» | Frontend (componente y página) | 2 | 🔍 En revisión (2, AB#373, PR sin abrir) |
| HU12-CP17 | Si el pedido ya no se puede cancelar (409), no se da por cancelado: se cierra la confirmación y el detalle lo avisa | Frontend (componente y página) | 2 | 🔍 En revisión (2, AB#373, PR sin abrir) |
| HU12-CP18 | Si la cancelación falla por otro motivo, se avisa dentro de la confirmación y se puede reintentar | Frontend (componente) | 1 | 🔍 En revisión (1, AB#373, PR sin abrir) |

Notas:
- **HU12-CP01 a CP08 (Sprint 2, backend, TDD):** casos propuestos en la T375 (AB#375, lógica de cancelación de pedido) a partir de los criterios de aceptación de la HU12 (AB#74): «solo permite cancelar si el pedido no ha sido enviado» y «el stock se repone al cancelar». Pendientes de confirmar con el equipo y de añadir a la HU12 en Azure Boards.
- **Decisiones del equipo:** se cancelan los pedidos `PAGADO` y `PREPARANDO`, como dice el criterio (no los `ENVIADO`). Cancelar un pedido ya cancelado es un error, para no reponer el stock dos veces. Primero se guarda la cancelación y después se repone el stock. Caso de uso nuevo `CancelacionPedidoUseCase` (cancelar no es lo mismo que comprar). Un pedido ajeno se trata como inexistente.
- **HU12-CP09 a CP12 (Sprint 2, backend, TDD):** casos propuestos en la T374 (AB#374, endpoint de cancelación): `POST /api/customer/orders/{id}/cancel`, sin cuerpo y con CSRF, responde 200 con el pedido cancelado; 409 `ORDER_NOT_CANCELLABLE` si ya no se puede cancelar; 400 `ORDER_NOT_FOUND` si no existe o es de otro cliente. HU12-CP12 pasó sin rojo real: `SecurityConfig` ya protege `/api/customer/**`. Pendientes de confirmar con el equipo y de añadir a la HU12 en Azure Boards.
- **HU12-CP13 a CP18 (Sprint 2, frontend, TDD, un ciclo por task):** casos propuestos en la T372 (botón de cancelar pedido) y la T373 (confirmación), con el contrato de la T374 (`frontend/docs/hu12-cancelar-pedido.md`). Sin rojo real: «un pedido enviado, entregado o ya cancelado no tiene el botón» (CP13, ciclo 1) y los dos de CP15 (ciclo 2), que quedaron hechos en el ciclo 1. Pendientes de confirmar con el equipo y de añadir a la HU12 en Azure Boards.

### Tabla B · casos de prueba ↔ tests

| Caso | Clase de test | Método | Capa | Task | Origen | Estado |
|---|---|---|---|---|---|---|
| HU12-CP01 | `PedidoTest` | `unPedidoNoEnviadoSeCancela` | Dominio | AB#375 | PR #200 | ✅ |
| HU12-CP02 | `PedidoTest` | `unPedidoEnviadoEntregadoOCanceladoNoSeCancela` | Dominio | AB#375 | PR #200 | ✅ |
| HU12-CP03 | `CancelacionPedidoServiceTest` | `cancelarGuardaElPedidoCanceladoYLoDevuelve` | Aplicación | AB#375 | PR #200 | ✅ |
| HU12-CP04 | `CancelacionPedidoServiceTest` | `cancelarReponeElStockDespuesDeGuardar` | Aplicación | AB#375 | PR #200 | ✅ |
| HU12-CP05 | `CancelacionPedidoServiceTest` | `unPedidoAjenoOInexistenteNoSeCancela` | Aplicación | AB#375 | PR #200 | ✅ |
| HU12-CP06 | `CancelacionPedidoServiceTest` | `unPedidoNoCancelableNoCambiaNada` | Aplicación | AB#375 | PR #200 | ✅ |
| HU12-CP07 | `SeguridadPedidosServiceTest` | `unVendedorNoCancelaPedidos` | Aplicación (seguridad) | AB#375 | PR #200 | ✅ |
| HU12-CP07 | `SeguridadPedidosServiceTest` | `unAdministradorNoCancelaPedidos` | Aplicación (seguridad) | AB#375 | PR #200 | ✅ |
| HU12-CP07 | `SeguridadPedidosServiceTest` | `unClienteNoCancelaConElCorreoDeOtro` | Aplicación (seguridad) | AB#375 | PR #200 | ✅ |
| HU12-CP07 | `SeguridadPedidosServiceTest` | `unClienteCancelaConSuPropioCorreo` | Aplicación (seguridad) | AB#375 | PR #200 | ✅ |
| HU12-CP07 | `SeguridadPedidosServiceTest` | `sinSesionSeRechazaCancelar` | Aplicación (seguridad) | AB#375 | PR #200 | ✅ |
| HU12-CP08 | `PedidoMongoAdapterTest` | `guardarUnPedidoCanceladoActualizaElMismoDocumento` | Infraestructura | AB#375 | PR #200 | ✅ |
| HU12-CP09 | `PedidoControllerTest` | `cancelarUnPedidoResponde200ConElPedidoCancelado` | Infraestructura (web) | AB#374 | PR #207 | ✅ |
| HU12-CP10 | `PedidoControllerTest` | `unPedidoNoCancelableResponde409` | Infraestructura (web) | AB#374 | PR #207 | ✅ |
| HU12-CP11 | `PedidoControllerTest` | `cancelarUnPedidoInexistenteOAjenoResponde400` | Infraestructura (web) | AB#374 | PR #207 | ✅ |
| HU12-CP12 | `PedidoControllerTest` | `soloElClienteCancelaSusPedidos` | Infraestructura (web) | AB#374 | PR #207 | ✅ |
| HU12-CP13 | `OrderDetailPage.test.jsx` | `HU12-CP13: un pedido pagado o en preparación, que aún no se ha enviado, tiene el botón «Cancelar pedido»` | Frontend · página | AB#372 | Rama `feature/US12-T372-T373-cancelar-pedido-front` (PR sin abrir) | 🔍 |
| HU12-CP13 | `OrderDetailPage.test.jsx` | `HU12-CP13: un pedido en preparación también se puede cancelar` | Frontend · página | AB#372 | Rama `feature/US12-T372-T373-cancelar-pedido-front` (PR sin abrir) | 🔍 |
| HU12-CP13 | `OrderDetailPage.test.jsx` | `HU12-CP13: un pedido enviado, entregado o ya cancelado no tiene el botón` | Frontend · página | AB#372 | Rama `feature/US12-T372-T373-cancelar-pedido-front` (PR sin abrir) | 🔍 |
| HU12-CP14 | `OrderDetailPage.test.jsx` | `HU12-CP14: al pulsar «Cancelar pedido» pide confirmación y todavía no cancela nada` | Frontend · página | AB#372 | Rama `feature/US12-T372-T373-cancelar-pedido-front` (PR sin abrir) | 🔍 |
| HU12-CP15 | `CancelOrderDialog.test.jsx` | `HU12-CP15: pregunta por el pedido que se va a cancelar y avisa de que no se puede deshacer` | Frontend · componente | AB#373 | Rama `feature/US12-T372-T373-cancelar-pedido-front` (PR sin abrir) | 🔍 |
| HU12-CP15 | `CancelOrderDialog.test.jsx` | `HU12-CP15: «Volver» cierra la confirmación sin cancelar el pedido` | Frontend · componente | AB#373 | Rama `feature/US12-T372-T373-cancelar-pedido-front` (PR sin abrir) | 🔍 |
| HU12-CP16 | `CancelOrderDialog.test.jsx` | `HU12-CP16: al confirmar cancela el pedido y entrega el pedido ya cancelado` | Frontend · componente | AB#373 | Rama `feature/US12-T372-T373-cancelar-pedido-front` (PR sin abrir) | 🔍 |
| HU12-CP16 | `OrderDetailPage.test.jsx` | `HU12-CP16: al confirmar, el detalle muestra el pedido cancelado y avisa de que se ha cancelado` | Frontend · página | AB#373 | Rama `feature/US12-T372-T373-cancelar-pedido-front` (PR sin abrir) | 🔍 |
| HU12-CP17 | `CancelOrderDialog.test.jsx` | `HU12-CP17: si el pedido ya no se puede cancelar (409), lo comunica sin darlo por cancelado` | Frontend · componente | AB#373 | Rama `feature/US12-T372-T373-cancelar-pedido-front` (PR sin abrir) | 🔍 |
| HU12-CP17 | `OrderDetailPage.test.jsx` | `HU12-CP17: si el pedido ya no se puede cancelar, cierra la confirmación y lo avisa en el detalle` | Frontend · página | AB#373 | Rama `feature/US12-T372-T373-cancelar-pedido-front` (PR sin abrir) | 🔍 |
| HU12-CP18 | `CancelOrderDialog.test.jsx` | `HU12-CP18: si falla por otro motivo avisa dentro de la confirmación y se puede reintentar` | Frontend · componente | AB#373 | Rama `feature/US12-T372-T373-cancelar-pedido-front` (PR sin abrir) | 🔍 |

Rutas de las clases de test (bajo `backend/src/test/java/es/uclm/esibuy/backend/pedidos/`):
- `PedidoTest`: `domain/model/` (JUnit)
- `CancelacionPedidoServiceTest` y `SeguridadPedidosServiceTest`: `application/service/` (el primero con Mockito, sin Spring; el segundo con un contexto mínimo y `@WithMockUser`)
- `PedidoMongoAdapterTest`: `infrastructure/adapter/out/persistence/` (Testcontainers)
- `PedidoControllerTest`: `infrastructure/adapter/in/web/` (`@WebMvcTest` con `SecurityConfig`, sin MongoDB)

Rutas de los tests del frontend (bajo `frontend/src/`, Vitest con React Testing Library y MSW):
- `OrderDetailPage.test.jsx`: `pages/`
- `CancelOrderDialog.test.jsx`: `features/orders/`

---

## HU13 · Valorar productos comprados

### Tabla A · HU13 ↔ casos de prueba

| Caso | Descripción | Tipo | Tests | Estado |
|---|---|---|---|---|
| HU13-CP01 | Una valoración se crea con su producto, su cliente, su puntuación y un comentario; nace sin id, y el comentario es opcional (vacío si no hay) y se guarda sin espacios en los extremos | Unitario (dominio) | 3 | ✅ Pasa (3, AB#369, PR #179) |
| HU13-CP02 | La puntuación es un entero de 1 a 5: se admiten los extremos y se rechaza lo de fuera | Unitario (dominio) | 2 | ✅ Pasa (2, AB#369, PR #179) |
| HU13-CP03 | El comentario admite hasta 500 caracteres, sin contar los espacios de los extremos | Unitario (dominio) | 2 | ✅ Pasa (2, AB#369, PR #179) |
| HU13-CP04 | El producto y el cliente son obligatorios (ni nulos ni en blanco) | Unitario (dominio) | 2 | ✅ Pasa (2, AB#369, PR #179) |
| HU13-CP05 | Persistencia en `valoraciones`: se asigna el id, el cliente se guarda como `usuarioId`, se busca por producto, se sabe si un cliente ya valoró un producto y el índice único (usuarioId, productoId) rechaza la repetida como valoración duplicada | Integración MongoDB | 5 | ✅ Pasa (5, AB#369, PR #179) |
| HU13-CP06 | Pedidos indica si un cliente ha comprado un producto: un pedido PAGADO suyo que lo contenga; la consulta se hace con el id del cliente, no con su correo | Unitario | 3 | ✅ Pasa (3, AB#370, PR #179) |
| HU13-CP07 | Consultar si se ha comprado un producto exige el rol cliente y que el correo sea el de la sesión; sin sesión se rechaza. Seguridad en la capa de negocio (R8) | Unitario con Spring Security (sin MongoDB) | 5 | ✅ Pasa (5, AB#370, PR #179) |
| HU13-CP08 | La consulta de compras en MongoDB encuentra el producto en cualquier línea de un pedido PAGADO del cliente, no cuenta los pedidos de otro cliente y solo cuenta el estado PAGADO | Integración MongoDB | 3 | ✅ Pasa (3, AB#370, PR #179) |
| HU13-CP09 | El cliente que ha comprado un producto lo valora: la valoración se guarda con el id del cliente y se devuelve con su id | Unitario | 2 | ✅ Pasa (2, AB#370, PR #179) |
| HU13-CP10 | No se puede valorar un producto que no se ha comprado, y no se guarda nada | Unitario | 1 | ✅ Pasa (1, AB#370, PR #179) |
| HU13-CP11 | No se puede valorar dos veces el mismo producto, y se conserva la primera valoración | Unitario | 1 | ✅ Pasa (1, AB#370, PR #179) |
| HU13-CP12 | Valorar exige el rol cliente y que el correo del comando sea el de la sesión; sin sesión se rechaza. Seguridad en la capa de negocio (R8) | Unitario con Spring Security (sin MongoDB) | 5 | ✅ Pasa (5, AB#370, PR #179) |
| HU13-CP13 | Los adaptadores preguntan por su puerto de entrada al módulo de usuarios (id del cliente) y al de pedidos (si ha comprado) | Unitario | 3 | ✅ Pasa (3, AB#370, PR #179) |
| HU13-CP14 | `POST /api/products/{id}/valoraciones`: 201 con la valoración y sin el cliente; 400 `VALIDATION_ERROR`; 403 `PRODUCT_NOT_PURCHASED`; 409 `REVIEW_ALREADY_EXISTS`; sin sesión 401, otro rol 403 y sin CSRF se rechaza | Integración (capa web) | 5 | ✅ Pasa (5, AB#370, PR #179) |
| HU13-CP15 | Consultar un producto devuelve todas sus valoraciones y solo las suyas; sin valoraciones, una lista vacía | Unitario | 2 | ✅ Pasa (2, AB#371, PR #179) |
| HU13-CP16 | Consultar las valoraciones de un producto exige el rol cliente; sin sesión se rechaza. Seguridad en la capa de negocio (R8) | Unitario con Spring Security (sin MongoDB) | 4 | ✅ Pasa (4, AB#371, PR #179) |
| HU13-CP17 | `GET /api/products/{id}/valoraciones`: 200 con la lista sin el cliente, o vacía; sin sesión 401 y otro rol 403 | Integración (capa web) | 3 | ✅ Pasa (3, AB#371, PR #179) |
| HU13-CP18 | En el detalle del producto, cada valoración se muestra con sus estrellas y, si lo tiene, su comentario | Frontend (página) | 2 | 🔍 En revisión (2, AB#368, PR #191) |
| HU13-CP19 | La media y el número de valoraciones del detalle salen de la lista; si la lista está vacía, se usan los del detalle del producto | Frontend (página) | 2 | 🔍 En revisión (2, AB#368, PR #191) |
| HU13-CP20 | Sin valoraciones se indica en su sección; si fallan, se avisa con «Reintentar» sin perder el producto | Frontend (página) | 2 | 🔍 En revisión (2, AB#368, PR #191) |
| HU13-CP21 | El cliente valora desde el detalle con estrellas y un comentario opcional; tras enviarla se le dan las gracias y su valoración aparece en la lista | Frontend (página) | 2 | 🔍 En revisión (2, AB#367, PR #191) |
| HU13-CP22 | Sin puntuación no se envía y se pide elegir una; el comentario admite como mucho 500 caracteres | Frontend (página) | 2 | 🔍 En revisión (2, AB#367, PR #191) |
| HU13-CP23 | Avisos al valorar: no comprado (403 `PRODUCT_NOT_PURCHASED`), ya valorado (409 `REVIEW_ALREADY_EXISTS`) y error general conservando lo escrito | Frontend (página) | 3 | 🔍 En revisión (3, AB#367, PR #191) |
| HU13-CP24 | El producto guarda en MongoDB la media y el número de valoraciones que recibe, sin tocar el resto de sus datos | Integración MongoDB | 2 | 🔍 En revisión (2, AB#415, PR #195) |
| HU13-CP25 | El producto lee su media y su número de valoraciones (sin valoraciones: `null` y 0) y los conserva al modificarlo, activarlo o desactivarlo y al quitarle la imagen | Unitario (dominio) + integración MongoDB | 4 | 🔍 En revisión (4, AB#415, PR #195) |
| HU13-CP26 | El catálogo (HU7) y el detalle (HU8) devuelven la media y el número de valoraciones del producto | Unitario + integración (capa web) | 3 | 🔍 En revisión (3, AB#415, PR #195) |
| HU13-CP27 | Actualizar la valoración de un producto la guarda y solo lo puede hacer el cliente; un vendedor o un administrador reciben 403 y sin sesión se rechaza. Seguridad en la capa de negocio (R8) | Unitario + unitario con Spring Security (sin MongoDB) | 5 | 🔍 En revisión (5, AB#415, PR #195) |
| HU13-CP28 | Al valorar, la media y el número se calculan con todas las valoraciones del producto y se le pasan al módulo de productos; si la valoración no se guarda, el producto no cambia | Unitario | 3 | 🔍 En revisión (3, AB#415, PR #195) |

Notas:
- **HU13-CP01 a CP17 (Sprint 2, backend):** casos propuestos en las tasks AB#369 (entidad valoración), AB#370 (endpoint para crear una valoración) y AB#371 (endpoint para obtener las valoraciones), y desarrollados con TDD. PENDIENTE DE REVISAR CON EL EQUIPO: confirmarlos y añadirlos a la HU13 (AB#75) en Azure Boards.
- **Decisiones del equipo:** módulo nuevo `valoraciones`; puntuación entera de 1 a 5 y comentario opcional de hasta 500 caracteres; una valoración por cliente y producto aunque lo compre varias veces; compra confirmada = pedido PAGADO (`ConsultaComprasUseCase`, puerto de entrada nuevo de `pedidos`); sin compra responde 403 `PRODUCT_NOT_PURCHASED`; el GET devuelve las valoraciones sin fecha ni orden garantizado.
- **Valoración en el catálogo:** `valoracion` y `numValoraciones` del catálogo (HU7) y del detalle (HU8) se rellenan en la T415 (AB#415, HU13-CP24 a CP28), que estaba fuera del alcance de las T369 a T371.
- **Valoraciones simultáneas (HU13-CP05):** si dos peticiones del mismo cliente pasan a la vez la comprobación de duplicado, el índice único rechaza la segunda y el adaptador la traduce a `ValoracionDuplicadaException` (409, no 500).
- **Seguridad:** `POST /api/products/*/valoraciones` tiene su propia regla en `SecurityConfig` (solo CLIENTE, con CSRF); el GET queda cubierto por `GET /api/products/**` (HU8). Los servicios llevan `@PreAuthorize`: `SOLO_CLIENTE_PROPIO_EN_COMANDO` para valorar, `SOLO_CLIENTE_PROPIO` para consultar compras y `SOLO_CLIENTE` para consultar valoraciones.
- **Ciclos sin rojo real:** HU13-CP12 (el `@PreAuthorize` se puso con el servicio, regla 7 del CLAUDE.md) y la parte de seguridad de HU13-CP17 (la cubría ya la regla del catálogo).
- **Clases de test nuevas**, acordadas con el equipo: `ValoracionTest`, `ValoracionMongoAdapterTest`, `ConsultaComprasServiceTest`, `ValoracionProductosServiceTest`, `SeguridadValoracionesServiceTest`, `ClienteEnSesionAdapterTest` (de valoraciones), `ComprasAdapterTest`, `ValoracionesConfigTest`, `ValoracionControllerTest` y `ConsultaValoracionesServiceTest`. **Tests en clases que ya existían** (módulo `pedidos`): `SeguridadPedidosServiceTest`, `PedidoMongoAdapterTest` y `PedidosConfigTest`.
- **HU13-CP18 a CP23 (Sprint 2, frontend):** casos propuestos en las tasks AB#368 (consulta de las valoraciones) y AB#367 (vista para valorar), desarrollados con TDD; pendientes de confirmar con el equipo y de añadir a la HU13 en Azure Boards. Se valora desde el detalle del producto; la media y el número del detalle salen de la lista porque el backend aún no rellena `valoracion` ni `numValoraciones` (decisión del equipo). Los tests de la HU8 que usan `responderCon` (`ProductDetailPage.test.jsx`) responden también con la lista vacía, sin cambiar lo que comprueban. Detalle en `frontend/docs/hu13-valoraciones.md`.
- **HU13-CP24 a CP28 (T415, AB#415):** casos propuestos, pendientes de confirmar con el equipo y de añadir a la HU13 en Azure Boards. Al guardar una valoración, el módulo de valoraciones calcula la media con todas las valoraciones del producto y se la pasa a productos por su puerto de entrada `ValoracionesProductoUseCase`, que la guarda en `valoracionMedia` y `numeroValoraciones` (decisión del equipo: recalcular desde todas las valoraciones es más simple que actualizar la media de forma incremental y se corrige sola; si dos clientes valoran a la vez el mismo producto, el último en escribir puede no contar la del otro hasta la siguiente valoración, riesgo asumido). El catálogo filtra y ordena por la media (HU7-CP15, modificado). Al modificar un producto se conservan las valoraciones guardadas. Se ajustaron sin cambiar lo que comprueban los tests que construyen `DetalleProductoCatalogo` (HU8-CP09, HU8-CP13 a CP15) y los comentarios de HU7-CP16 y HU8-CP13. Clases de test nuevas, acordadas con el equipo: `ValoracionesProductoServiceTest` y `ProductosAdapterTest` (de valoraciones).

### Tabla B · casos de prueba ↔ tests

| Caso | Clase de test | Método | Capa | Task | Origen | Estado |
|---|---|---|---|---|---|---|
| HU13-CP01 | `ValoracionTest` | `creaUnaValoracionConSusDatos` | Dominio | AB#369 | PR #179 | ✅ |
| HU13-CP01 | `ValoracionTest` | `unaValoracionNuevaNaceSinId` | Dominio | AB#369 | PR #179 | ✅ |
| HU13-CP01 | `ValoracionTest` | `elComentarioEsOpcionalYSeRecorta` | Dominio | AB#369 | PR #179 | ✅ |
| HU13-CP02 | `ValoracionTest` | `admiteLasPuntuacionesDeLosExtremos` (1 y 5) | Dominio | AB#369 | PR #179 | ✅ |
| HU13-CP02 | `ValoracionTest` | `rechazaUnaPuntuacionFueraDeRango` (0, 6 y -1) | Dominio | AB#369 | PR #179 | ✅ |
| HU13-CP03 | `ValoracionTest` | `elComentarioAdmiteHasta500Caracteres` | Dominio | AB#369 | PR #179 | ✅ |
| HU13-CP03 | `ValoracionTest` | `rechazaUnComentarioDeMasDe500Caracteres` | Dominio | AB#369 | PR #179 | ✅ |
| HU13-CP04 | `ValoracionTest` | `rechazaUnaValoracionSinProducto` (null, vacío y en blanco) | Dominio | AB#369 | PR #179 | ✅ |
| HU13-CP04 | `ValoracionTest` | `rechazaUnaValoracionSinCliente` (null, vacío y en blanco) | Dominio | AB#369 | PR #179 | ✅ |
| HU13-CP05 | `ValoracionMongoAdapterTest` | `guardarUnaValoracionNuevaYRecuperarlaPorProducto` | Infraestructura | AB#369 | PR #179 | ✅ |
| HU13-CP05 | `ValoracionMongoAdapterTest` | `elClienteSeGuardaComoUsuarioId` | Infraestructura | AB#369 | PR #179 | ✅ |
| HU13-CP05 | `ValoracionMongoAdapterTest` | `buscarPorProductoDevuelveSoloLasDeEseProducto` | Infraestructura | AB#369 | PR #179 | ✅ |
| HU13-CP05 | `ValoracionMongoAdapterTest` | `sabeSiUnClienteYaHaValoradoUnProducto` | Infraestructura | AB#369 | PR #179 | ✅ |
| HU13-CP05 | `ValoracionMongoAdapterTest` | `laColeccionSoloAdmiteUnaValoracionPorClienteYProducto` | Infraestructura | AB#369 | PR #179 | ✅ |
| HU13-CP06 | `ConsultaComprasServiceTest` | `haCompradoSiTieneUnPedidoPagadoConElProducto` | Aplicación | AB#370 | PR #179 | ✅ |
| HU13-CP06 | `ConsultaComprasServiceTest` | `noHaCompradoSiNoTienePedidoPagadoConElProducto` | Aplicación | AB#370 | PR #179 | ✅ |
| HU13-CP06 | `ConsultaComprasServiceTest` | `laConsultaSeHaceConElIdDelCliente` | Aplicación | AB#370 | PR #179 | ✅ |
| HU13-CP07 | `SeguridadPedidosServiceTest` | `unVendedorNoConsultaCompras` | Aplicación | AB#370 | PR #179 | ✅ |
| HU13-CP07 | `SeguridadPedidosServiceTest` | `unAdministradorNoConsultaCompras` | Aplicación | AB#370 | PR #179 | ✅ |
| HU13-CP07 | `SeguridadPedidosServiceTest` | `unClienteNoConsultaLasComprasDeOtro` | Aplicación | AB#370 | PR #179 | ✅ |
| HU13-CP07 | `SeguridadPedidosServiceTest` | `unClienteConsultaSusPropiasCompras` | Aplicación | AB#370 | PR #179 | ✅ |
| HU13-CP07 | `SeguridadPedidosServiceTest` | `sinSesionSeRechazaConsultarCompras` | Aplicación | AB#370 | PR #179 | ✅ |
| HU13-CP08 | `PedidoMongoAdapterTest` | `unPedidoPagadoConElProductoEsUnaCompra` | Infraestructura | AB#370 | PR #179 | ✅ |
| HU13-CP08 | `PedidoMongoAdapterTest` | `noEsCompraSinElProductoNiConElPedidoDeOtroCliente` | Infraestructura | AB#370 | PR #179 | ✅ |
| HU13-CP08 | `PedidoMongoAdapterTest` | `soloCuentaUnPedidoPagado` | Infraestructura | AB#370 | PR #179 | ✅ |
| HU13-CP09 | `ValoracionProductosServiceTest` | `elClienteQueHaCompradoValoraElProducto` | Aplicación | AB#370 | PR #179 | ✅ |
| HU13-CP09 | `ValoracionProductosServiceTest` | `valorarDevuelveLaValoracionGuardada` | Aplicación | AB#370 | PR #179 | ✅ |
| HU13-CP10 | `ValoracionProductosServiceTest` | `noPuedeValorarUnProductoQueNoHaComprado` | Aplicación | AB#370 | PR #179 | ✅ |
| HU13-CP11 | `ValoracionProductosServiceTest` | `noPuedeValorarDosVecesElMismoProducto` | Aplicación | AB#370 | PR #179 | ✅ |
| HU13-CP12 | `SeguridadValoracionesServiceTest` | `unVendedorNoValora` | Aplicación | AB#370 | PR #179 | ✅ |
| HU13-CP12 | `SeguridadValoracionesServiceTest` | `unAdministradorNoValora` | Aplicación | AB#370 | PR #179 | ✅ |
| HU13-CP12 | `SeguridadValoracionesServiceTest` | `unClienteNoValoraConElCorreoDeOtro` | Aplicación | AB#370 | PR #179 | ✅ |
| HU13-CP12 | `SeguridadValoracionesServiceTest` | `unClienteValoraConSuPropioCorreo` | Aplicación | AB#370 | PR #179 | ✅ |
| HU13-CP12 | `SeguridadValoracionesServiceTest` | `sinSesionSeRechazaValorar` | Aplicación | AB#370 | PR #179 | ✅ |
| HU13-CP13 | `ClienteEnSesionAdapterTest` | `idPorCorreoPreguntaAlModuloDeUsuarios` | Infraestructura | AB#370 | PR #179 | ✅ |
| HU13-CP13 | `ComprasAdapterTest` | `haCompradoPreguntaAlModuloDePedidos` | Infraestructura | AB#370 | PR #179 | ✅ |
| HU13-CP13 | `ComprasAdapterTest` | `siPedidosRespondeQueNoElAdaptadorTambien` | Infraestructura | AB#370 | PR #179 | ✅ |
| HU13-CP14 | `ValoracionControllerTest` | `valorarUnProductoResponde201` | Infraestructura | AB#370 | PR #179 | ✅ |
| HU13-CP14 | `ValoracionControllerTest` | `unaValoracionInvalidaResponde400` | Infraestructura | AB#370 | PR #179 | ✅ |
| HU13-CP14 | `ValoracionControllerTest` | `valorarSinHaberCompradoResponde403` | Infraestructura | AB#370 | PR #179 | ✅ |
| HU13-CP14 | `ValoracionControllerTest` | `valorarDosVecesResponde409` | Infraestructura | AB#370 | PR #179 | ✅ |
| HU13-CP14 | `ValoracionControllerTest` | `soloElClienteValora` | Infraestructura | AB#370 | PR #179 | ✅ |
| HU13-CP15 | `ConsultaValoracionesServiceTest` | `devuelveLasValoracionesDelProducto` | Aplicación | AB#371 | PR #179 | ✅ |
| HU13-CP15 | `ConsultaValoracionesServiceTest` | `unProductoSinValoracionesDevuelveUnaListaVacia` | Aplicación | AB#371 | PR #179 | ✅ |
| HU13-CP16 | `SeguridadValoracionesServiceTest` | `unVendedorNoConsultaValoraciones` | Aplicación | AB#371 | PR #179 | ✅ |
| HU13-CP16 | `SeguridadValoracionesServiceTest` | `unAdministradorNoConsultaValoraciones` | Aplicación | AB#371 | PR #179 | ✅ |
| HU13-CP16 | `SeguridadValoracionesServiceTest` | `unClienteConsultaValoraciones` | Aplicación | AB#371 | PR #179 | ✅ |
| HU13-CP16 | `SeguridadValoracionesServiceTest` | `sinSesionSeRechazaConsultarValoraciones` | Aplicación | AB#371 | PR #179 | ✅ |
| HU13-CP17 | `ValoracionControllerTest` | `consultarLasValoracionesResponde200ConLaLista` | Infraestructura | AB#371 | PR #179 | ✅ |
| HU13-CP17 | `ValoracionControllerTest` | `unProductoSinValoracionesResponde200ConUnaListaVacia` | Infraestructura | AB#371 | PR #179 | ✅ |
| HU13-CP17 | `ValoracionControllerTest` | `soloElClienteConsultaLasValoraciones` | Infraestructura | AB#371 | PR #179 | ✅ |
| HU13-CP18 | `ProductDetailPage.test.jsx` | `HU13-CP18: muestra cada valoración del producto con sus estrellas y su comentario` | Frontend · página | AB#368 | PR #191 | 🔍 |
| HU13-CP18 | `ProductDetailPage.test.jsx` | `HU13-CP18: una valoración sin comentario muestra solo sus estrellas` | Frontend · página | AB#368 | PR #191 | 🔍 |
| HU13-CP19 | `ProductDetailPage.test.jsx` | `HU13-CP19: la media y el número de valoraciones salen de la lista aunque el catálogo no las tenga` | Frontend · página | AB#368 | PR #191 | 🔍 |
| HU13-CP19 | `ProductDetailPage.test.jsx` | `HU13-CP19: si la lista está vacía se mantienen la media y el número del catálogo` | Frontend · página | AB#368 | PR #191 | 🔍 |
| HU13-CP20 | `ProductDetailPage.test.jsx` | `HU13-CP20: un producto sin valoraciones lo indica en su sección` | Frontend · página | AB#368 | PR #191 | 🔍 |
| HU13-CP20 | `ProductDetailPage.test.jsx` | `HU13-CP20: si fallan las valoraciones avisa y permite reintentar, sin perder el producto` | Frontend · página | AB#368 | PR #191 | 🔍 |
| HU13-CP21 | `ProductDetailPage.test.jsx` | `HU13-CP21: el cliente valora con estrellas y un comentario, y su valoración aparece en la lista` | Frontend · página | AB#367 | PR #191 | 🔍 |
| HU13-CP21 | `ProductDetailPage.test.jsx` | `HU13-CP21: el comentario es opcional: sin él se envía vacío` | Frontend · página | AB#367 | PR #191 | 🔍 |
| HU13-CP22 | `ProductDetailPage.test.jsx` | `HU13-CP22: sin puntuación no se envía y se pide elegir una` | Frontend · página | AB#367 | PR #191 | 🔍 |
| HU13-CP22 | `ProductDetailPage.test.jsx` | `HU13-CP22: el comentario admite como mucho 500 caracteres` | Frontend · página | AB#367 | PR #191 | 🔍 |
| HU13-CP23 | `ProductDetailPage.test.jsx` | `HU13-CP23: si no ha comprado el producto se le avisa y el formulario sigue ahí` | Frontend · página | AB#367 | PR #191 | 🔍 |
| HU13-CP23 | `ProductDetailPage.test.jsx` | `HU13-CP23: si ya lo había valorado se le avisa` | Frontend · página | AB#367 | PR #191 | 🔍 |
| HU13-CP23 | `ProductDetailPage.test.jsx` | `HU13-CP23: si falla el envío avisa y conserva lo escrito para reintentar` | Frontend · página | AB#367 | PR #191 | 🔍 |
| HU13-CP24 | `ProductoMongoAdapterTest` | `guardaLaMediaYElNumeroDeValoraciones` | Infraestructura | AB#415 | PR #195 | 🔍 |
| HU13-CP24 | `ProductoMongoAdapterTest` | `unaActualizacionNuevaSustituyeLaMediaYElNumero` | Infraestructura | AB#415 | PR #195 | 🔍 |
| HU13-CP25 | `ProductoTest` | `unProductoSinValoracionesNoTieneMedia` | Dominio | AB#415 | PR #195 | 🔍 |
| HU13-CP25 | `ProductoTest` | `lasCopiasConservanLaMediaYElNumero` | Dominio | AB#415 | PR #195 | 🔍 |
| HU13-CP25 | `ProductoMongoAdapterTest` | `alLeerElProductoTraeSuMediaYSuNumero` | Infraestructura | AB#415 | PR #195 | 🔍 |
| HU13-CP25 | `ProductoMongoAdapterTest` | `modificarElProductoConservaSuMediaYSuNumero` | Infraestructura | AB#415 | PR #195 | 🔍 |
| HU13-CP26 | `CatalogoProductosServiceTest` | `elDetalleLlevaSuMediaYSuNumeroDeValoraciones` | Aplicación | AB#415 | PR #195 | 🔍 |
| HU13-CP26 | `CatalogoControllerTest` | `elCatalogoDevuelveLaMediaYElNumeroDeValoraciones` | Infraestructura | AB#415 | PR #195 | 🔍 |
| HU13-CP26 | `CatalogoControllerTest` | `elDetalleDevuelveLaMediaYElNumeroDeValoraciones` | Infraestructura | AB#415 | PR #195 | 🔍 |
| HU13-CP27 | `ValoracionesProductoServiceTest` | `actualizarLaValoracionGuardaLaMediaYElNumero` | Aplicación | AB#415 | PR #195 | 🔍 |
| HU13-CP27 | `SeguridadProductosServiceTest` | `unClienteActualizaLaValoracionDelProducto` | Aplicación | AB#415 | PR #195 | 🔍 |
| HU13-CP27 | `SeguridadProductosServiceTest` | `unVendedorNoCambiaLaValoracion` | Aplicación | AB#415 | PR #195 | 🔍 |
| HU13-CP27 | `SeguridadProductosServiceTest` | `unAdministradorNoCambiaLaValoracion` | Aplicación | AB#415 | PR #195 | 🔍 |
| HU13-CP27 | `SeguridadProductosServiceTest` | `sinSesionNoSeCambiaLaValoracion` | Aplicación | AB#415 | PR #195 | 🔍 |
| HU13-CP28 | `ValoracionProductosServiceTest` | `alValorarElProductoRecibeLaMediaYElNumero` | Aplicación | AB#415 | PR #195 | 🔍 |
| HU13-CP28 | `ValoracionProductosServiceTest` | `siLaValoracionNoSeGuardaElProductoNoCambia` | Aplicación | AB#415 | PR #195 | 🔍 |
| HU13-CP28 | `ProductosAdapterTest` | `laMediaYElNumeroSeLePasanAlModuloDeProductos` | Infraestructura | AB#415 | PR #195 | 🔍 |

Rutas de las clases de test (bajo `backend/src/test/java/es/uclm/esibuy/backend/`):
- `ValoracionTest`: `valoraciones/domain/model/` (JUnit, sin Spring)
- `ValoracionProductosServiceTest` y `ConsultaValoracionesServiceTest`: `valoraciones/application/service/` (JUnit con dobles de prueba, sin Spring)
- `SeguridadValoracionesServiceTest`: `valoraciones/application/service/` (contexto mínimo de Spring con la seguridad de métodos, sin MongoDB)
- `ValoracionMongoAdapterTest`: `valoraciones/infrastructure/adapter/out/persistence/` (`@DataMongoTest` con Testcontainers)
- `ClienteEnSesionAdapterTest`: `valoraciones/infrastructure/adapter/out/usuarios/`; `ComprasAdapterTest`: `valoraciones/infrastructure/adapter/out/pedidos/` (JUnit y Mockito, sin Spring)
- `ValoracionControllerTest`: `valoraciones/infrastructure/adapter/in/web/` (`@WebMvcTest` con la seguridad real)
- `ConsultaComprasServiceTest` y `SeguridadPedidosServiceTest`: `pedidos/application/service/`; `PedidoMongoAdapterTest`: `pedidos/infrastructure/adapter/out/persistence/` (Testcontainers)

---

## HU14 · Gestión de Favoritos / Lista de Deseos

### Tabla A · HU14 ↔ casos de prueba

| Caso | Descripción | Tipo | Tests | Estado |
|---|---|---|---|---|
| HU14-CP01 | El primer producto crea la lista del cliente en sesión y guarda la fecha; el id del cliente se pide al módulo de usuarios | Unitario | 3 | ✅ Pasa (3, AB#360, PR #196) |
| HU14-CP02 | Añadir otro producto conserva los anteriores | Unitario | 2 | ✅ Pasa (2, AB#360, PR #196) |
| HU14-CP03 | Un producto repetido no se duplica ni cambia su fecha, y no es un error | Unitario | 2 | ✅ Pasa (2, AB#360, PR #196) |
| HU14-CP04 | Un producto inexistente o no disponible se rechaza y no se guarda nada; la disponibilidad se pregunta al módulo de productos | Unitario | 4 | ✅ Pasa (4, AB#360, PR #196) |
| HU14-CP05 | Solo el cliente con su propio correo puede añadir: vendedor, administrador u otro correo reciben 403, y sin sesión se rechaza. Seguridad en la capa de negocio (R8) | Unitario con Spring Security (sin MongoDB) | 5 | ✅ Pasa (5, AB#360, PR #196) |
| HU14-CP06 | Persistencia en `listas_deseos`: guardar y recuperar por cliente; volver a guardar actualiza sin crear otra lista; la colección admite una sola lista por cliente | Unitario + integración MongoDB | 4 | ✅ Pasa (4, AB#360, PR #196) |
| HU14-CP07 | `POST /api/customer/wishlist/items` devuelve 200 con la lista; el cliente sale de la sesión y un `clienteId` en el cuerpo se ignora | Integración (capa web) | 1 | ✅ Pasa (1, AB#358, PR #198) |
| HU14-CP08 | Un cuerpo sin `productoId`, con `productoId` vacío o ilegible devuelve 400 `VALIDATION_ERROR` y no llega al caso de uso | Integración (capa web) | 1 | ✅ Pasa (1, AB#358, PR #198) |
| HU14-CP09 | Un producto no disponible devuelve 404 con el código `PRODUCT_NOT_FOUND` | Integración (capa web) | 1 | ✅ Pasa (1, AB#358, PR #198) |
| HU14-CP10 | Sin sesión (401), sin ser cliente (403) o sin token CSRF (403) no se añade a la lista | Integración (capa web) | 3 | ✅ Pasa (3, AB#358, PR #198) |
| HU14-CP11 | Consultar devuelve cada producto con sus datos actuales y su fecha, en orden de añadido; sin lista, una vacía que no se guarda | Unitario | 2 | ✅ Pasa (2, AB#362, PR #205) |
| HU14-CP12 | Un producto desactivado sigue en la lista, marcado como no disponible | Unitario | 1 | ✅ Pasa (1, AB#362, PR #205) |
| HU14-CP13 | Un producto eliminado desaparece y la lista se guarda sin él; si no hay ninguno eliminado, no se guarda nada | Unitario | 4 | ✅ Pasa (4, AB#362, PR #205) |
| HU14-CP14 | El precio de cada producto es el que le toca al cliente, estándar o premium | Unitario | 1 | ✅ Pasa (1, AB#362, PR #205) |
| HU14-CP15 | Solo el cliente con su propio correo consulta su lista: otro correo, vendedor o administrador reciben 403, y sin sesión se rechaza. Seguridad en la capa de negocio (R8) | Unitario con Spring Security (sin MongoDB) | 5 | ✅ Pasa (5, AB#362, PR #205) |
| HU14-CP16 | El resumen de `productos` devuelve los productos disponibles y los desactivados, vacío si no existe, y exige el rol cliente | Unitario y unitario con Spring Security (sin MongoDB) | 7 | ✅ Pasa (7, AB#362, PR #205) |
| HU14-CP17 | Los adaptadores preguntan a `usuarios` si el cliente es premium y a `productos` por los datos del producto | Unitario | 3 | ✅ Pasa (3, AB#362, PR #205) |
| HU14-CP18 | `GET /api/customer/wishlist` devuelve 200 con los datos de cada producto, también de uno desactivado, o con `items` vacío; el cliente sale de la sesión | Integración (capa web) | 2 | ✅ Pasa (2, AB#361, PR #206) |
| HU14-CP19 | Sin sesión (401) o sin ser cliente (403) no se consulta la lista | Integración (capa web) | 2 | ✅ Pasa (2, AB#361, PR #206) |
| HU14-CP20 | La llamada del cliente pide `POST /api/customer/wishlist/items` con el producto y devuelve la lista; un 404 llega como error con su estado y su código | Frontend (API) | 2 | ✅ Pasa (2, AB#357, PR #202) |
| HU14-CP21 | Cada tarjeta del catálogo tiene un corazón para añadir su producto a favoritos, fuera del enlace al detalle; también los agotados, pero no los no disponibles | Frontend (componente y página) | 5 | ✅ Pasa (5, AB#357, PR #202) |
| HU14-CP22 | Al pulsar el corazón se añade el producto; mientras se añade queda deshabilitado y después queda relleno, sin salir del catálogo | Frontend (componente y página) | 4 | 🔍 En revisión (4, AB#357 en PR #202; 2 modificados en AB#364, PR #213) |
| HU14-CP23 | La lista que devuelve el backend al añadir rellena también los corazones de los productos que ya estaban en favoritos | Frontend (página) | 1 | 🔍 En revisión (1, AB#357 en PR #202; modificado en AB#364, PR #213) |
| HU14-CP24 | Si no se puede añadir, la tarjeta lo avisa (producto que ya no está disponible o mensaje genérico) y el corazón se puede volver a pulsar; al pulsarlo desaparece el aviso | Frontend (componente y página) | 4 | ✅ Pasa (4, AB#357, PR #202) |
| HU14-CP25 | Quitar un producto lo saca de la lista, conserva los demás, la guarda y devuelve la lista actualizada | Unitario | 2 | ✅ Pasa (2, AB#366, PR #208) |
| HU14-CP26 | Quitar un producto que no está no es un error ni guarda nada; si el cliente no tiene lista, tampoco la crea | Unitario | 3 | ✅ Pasa (3, AB#366, PR #208) |
| HU14-CP27 | Un producto desactivado también se puede quitar | Unitario | 1 | ✅ Pasa (1, AB#366, PR #208) |
| HU14-CP28 | Solo el cliente con su propio correo puede quitar: otro correo, vendedor o administrador reciben 403, y sin sesión se rechaza. Seguridad en la capa de negocio (R8) | Unitario con Spring Security (sin MongoDB) | 5 | ✅ Pasa (5, AB#366, PR #208) |
| HU14-CP29 | `DELETE /api/customer/wishlist/items/{productoId}` devuelve 200 con la lista actualizada; el cliente sale de la sesión | Integración (capa web) | 1 | ✅ Pasa (1, AB#365, PR #209) |
| HU14-CP30 | Sin sesión (401), sin ser cliente (403) o sin token CSRF (403) no se quita de la lista | Integración (capa web) | 3 | ✅ Pasa (3, AB#365, PR #209) |
| HU14-CP31 | Las llamadas del cliente piden `GET /api/customer/wishlist` y `DELETE /api/customer/wishlist/items/{productoId}` (con el id codificado) y devuelven la lista; un 403 llega como error con su estado y su código | Frontend (API) | 3 | 🔍 En revisión (3, AB#363 y AB#364, PR #213) |
| HU14-CP32 | Mis favoritos está en `/wishlist` y el cliente llega desde el enlace Favoritos de la cabecera; sin sesión va al login, y el vendedor y el administrador no tienen ni el enlace ni la vista | Frontend (página y rutas) | 5 | 🔍 En revisión (5, AB#363, PR #213) |
| HU14-CP33 | Mis favoritos muestra cada producto en el orden en que se añadió, con su imagen (o un marcador), nombre, precio y fecha; uno agotado lo indica y uno no disponible lo indica y no enlaza al detalle | Frontend (página) | 5 | 🔍 En revisión (5, AB#363, PR #213) |
| HU14-CP34 | Mis favoritos muestra un indicador mientras carga, un estado vacío que lleva al catálogo y, si falla, un error con «Reintentar» | Frontend (página) | 3 | 🔍 En revisión (3, AB#363, PR #213) |
| HU14-CP35 | Desde Mis favoritos se quita cada producto (también uno no disponible): se avisa y la lista se actualiza; mientras se quita, su botón queda deshabilitado; al quitar el último sale el estado vacío, y si falla se avisa y el producto sigue | Frontend (página) | 5 | 🔍 En revisión (5, AB#364, PR #213) |
| HU14-CP36 | Al entrar en el catálogo se cargan los favoritos y sus corazones salen rellenos; si no se pueden cargar, el catálogo funciona con los corazones vacíos | Frontend (página) | 2 | 🔍 En revisión (2, AB#363, PR #213) |
| HU14-CP37 | El corazón relleno del catálogo quita el producto de favoritos: queda deshabilitado mientras se quita, después vacío, y si falla la tarjeta lo avisa | Frontend (componente y página) | 4 | 🔍 En revisión (4, AB#364, PR #213) |

- **HU14-CP01 a CP06 (Sprint 2, backend):** casos propuestos en la task AB#360 (lógica de guardado de deseo), revisados y aceptados por el desarrollador. Azure no los trae: habría que añadirlos a la HU14.
- **Decisiones del equipo:** módulo nuevo `deseos`; una sola lista por cliente, como dicen el criterio de aceptación y la colección `listas_deseos`, que ya tenía su índice único. El campo de la colección sigue siendo `usuarioId`, y guarda el id del cliente. Añadir un producto repetido no es un error, y un producto inexistente o no disponible se rechaza.
- **T359 (edición de la entidad cliente):** pasada a *Removed*. La lista va en su propia colección y se relaciona con el cliente por id, así que `Usuario` no cambia.
- **HU14-CP11 a CP17 (Sprint 2, backend):** casos propuestos en la task AB#362 (lógica de obtención de deseos), revisados y aceptados por el desarrollador.
- **Consultar la lista (T362):** devuelve de cada producto `nombre`, `imagen`, `precioFinal` (el del cliente), `stock`, `disponible` y `addedAt`. Un producto desactivado sigue en la lista, con su precio, y uno eliminado desaparece. Como en el carrito (AB#390), la lista se limpia al consultarla: un borrado físico en `productos` no avisa a `deseos`. Para distinguir los dos casos, `productos` añade `CatalogoProductosUseCase.consultarResumen`, porque `consultarComprable` devuelve vacío en ambos.
- **Tests que pasaban en el rojo (T362):** "sin lista" y "sin eliminados no se guarda" (HU14-CP11 y CP13) y "sin eliminados la lista no cambia" (HU14-CP13), porque el esqueleto no hacía nada. Se comprobaron con versiones que siempre guardaban o que siempre decían que la lista había cambiado, y entonces fallaron.
- **HU14-CP18 y CP19 (Sprint 2, backend):** casos propuestos en la task AB#361 (endpoint obtener deseos), revisados y aceptados por el desarrollador. Los casos de frontend empiezan en CP20, así que los siguientes de backend irán desde CP25.
- **Endpoint (T361):** `GET /api/customer/wishlist` → 200 `{ "items": [{ "productoId", "nombre", "imagen", "precioFinal", "stock", "disponible", "addedAt" }] }`, o `items` vacío; 401 sin sesión y 403 con otro rol. El `POST` sigue devolviendo solo `productoId` y `addedAt`, porque el botón de favoritos no necesita más.
- **HU14-CP19 sin rojo real:** la regla de `/api/customer/**` de `SecurityConfig` ya lo cubría. Se comprobó abriendo la ruta de forma temporal, y entonces fallaron los tests de sesión y de rol.
- **HU14-CP25 a CP28 (Sprint 2, backend):** casos propuestos en la task AB#366 (lógica de eliminación de deseos), revisados y aceptados por el desarrollador.
- **Quitar un producto (T366):** devuelve la lista actualizada con los mismos datos que al consultarla, para que la vista se refresque sin otra llamada. Como en el carrito, quitar un producto que no está no es un error, y no se comprueba si está disponible para que se pueda quitar uno desactivado. Se comprobó con una versión que siempre guardaba: fallaron los dos tests que protegen "no se guarda nada" y "no se crea la lista", que pasaban en el rojo.
- **HU14-CP29 y CP30 (Sprint 2, backend):** casos propuestos en la task AB#365 (endpoint de eliminación de deseos), revisados y aceptados por el desarrollador.
- **Endpoint (T365):** `DELETE /api/customer/wishlist/items/{productoId}` → 200 con la lista actualizada, en el mismo formato que el `GET`, también si el producto no estaba; 401 sin sesión y 403 con otro rol o sin CSRF. Con esta task queda terminado el backend de la HU14.
- **HU14-CP30 sin rojo real:** la regla de `/api/customer/**` de `SecurityConfig` ya lo cubría. Se comprobó con dos cambios temporales: con la ruta abierta fallaron los tests de sesión y rol, y sin CSRF falló el del token.
- **HU14-CP07 a CP10 (Sprint 2, backend):** casos propuestos en la task AB#358 (endpoint añadir deseo), revisados y aceptados por el desarrollador.
- **Endpoint (T358):** `POST /api/customer/wishlist/items` con `{ "productoId": "p-1" }` → 200 `{ "items": [{ "productoId", "addedAt" }] }`, también si el producto ya estaba. Errores: 400 `VALIDATION_ERROR`, 404 `PRODUCT_NOT_FOUND`, 401 sin sesión y 403 con otro rol o sin CSRF. Los datos del producto (nombre, imagen, aviso de "no disponible") llegan con la consulta de la T361 y la T362. `ProductoNoDisponibleException` comparte el handler de `ProductoNoComprableException` en `ManejadorGlobalErrores`, porque responden igual.
- **HU14-CP10 sin rojo real:** la regla de `/api/customer/**` de `SecurityConfig` ya lo cubría. Se comprobó con dos cambios temporales: con la ruta abierta fallaron los tres tests de sesión y rol, y sin CSRF falló el del token.
- **Ciclo con rojo parcial:** HU14-CP03 pasaba en el rojo del dominio porque `anadir` no hacía nada. Se comprobó con una versión sin la regla de repetidos, y entonces falló.
- **HU14-CP20 a CP24 (Sprint 2, frontend):** casos propuestos en la task AB#357 (botón de favoritos en la lista de productos), a partir del criterio «el sistema guarda el producto seleccionado por el cliente en la lista de deseos vinculada a su cuenta», y desarrollados con TDD. Empiezan en CP20 para dejar hueco a los casos del backend que faltan (T361, T362, T365 y T366). Pendientes de confirmar con el equipo y de añadir a la HU14 en Azure Boards. Rama `feature/US14-T357-boton-favoritos-lista-productos`.
- **Vista (T357):** el corazón va en la tarjeta del catálogo (`ProductCard`), encima de la esquina de la imagen y fuera del enlace al detalle. La página (`ProductsPage`) rellena los corazones con la lista completa que devuelve el `POST`. Un producto no disponible no tiene corazón (el backend respondería 404); uno agotado sí.
- **Tests que nacen en verde:** en HU14-CP21, «un producto no disponible no tiene el corazón» y «sin onAddFavorite la tarjeta no ofrece favoritos», porque antes del ciclo la tarjeta no tenía corazón. Se dejan para que no cambie sin darnos cuenta.
- **HU14-CP31 a CP37 (Sprint 2, frontend):** casos propuestos en las tasks AB#363 (vista de deseos) y AB#364 (botón de eliminación de deseos), y desarrollados con TDD. Pendientes de confirmar con el equipo y de añadir a la HU14 en Azure Boards. Rama `feature/US14-T363-T364-vista-y-eliminar-deseos`.
- **Vista (T363 y T364):** «Mis favoritos» en `/wishlist`, solo del cliente, con el enlace Favoritos en la cabecera. Lee `GET /api/customer/wishlist` y cada producto se quita sin confirmación (se puede volver a añadir) con `DELETE /api/customer/wishlist/items/{productoId}`, cuya respuesta sustituye a la lista. Un producto desactivado se ve atenuado, con la etiqueta «No disponible» y sin enlace al detalle; uno agotado lleva «Agotado» y sí enlaza.
- **Catálogo (T363 y T364):** al entrar se cargan los favoritos, así que los corazones salen rellenos desde el principio; si la lista no se puede leer, el catálogo funciona igual con los corazones vacíos. El corazón relleno deja de estar deshabilitado y quita el producto de favoritos. Por eso cambian tres tests de la T357: HU14-CP22 de la tarjeta («…relleno y deshabilitado» pasa a «…relleno para quitarlo») y los de página de HU14-CP22 y CP23, que buscan el corazón por su nuevo nombre, «Quitar X de favoritos».
- **Tests que nacen en verde (T363 y T364):** en HU14-CP32, «el vendedor y el administrador no tienen el enlace a favoritos», y en HU14-CP36, «si no se pueden cargar los favoritos, el catálogo funciona con los corazones vacíos», porque antes del ciclo no existían ni el enlace ni la lectura. Se dejan para que no cambie sin darnos cuenta.
- **Pendiente:** el corazón en el detalle del producto y en los productos destacados del catálogo, que no son de estas tasks.

### Tabla B · casos de prueba ↔ tests

| Caso | Clase de test | Método | Capa | Task | Origen | Estado |
|---|---|---|---|---|---|---|
| HU14-CP01 | `ListaDeseosTest` | `anadirElPrimerProductoLoGuardaConSuFecha` | Dominio | AB#360 | PR #196 | ✅ |
| HU14-CP01 | `GestionListaDeseosServiceTest` | `elPrimerProductoCreaLaListaDelCliente` | Aplicación | AB#360 | PR #196 | ✅ |
| HU14-CP01 | `ClienteEnSesionAdapterTest` | `idPorCorreoPreguntaAlModuloDeUsuarios` | Infraestructura | AB#360 | PR #196 | ✅ |
| HU14-CP02 | `ListaDeseosTest` | `anadirOtroProductoConservaLosAnteriores` | Dominio | AB#360 | PR #196 | ✅ |
| HU14-CP02 | `GestionListaDeseosServiceTest` | `anadirOtroProductoUsaLaListaDelCliente` | Aplicación | AB#360 | PR #196 | ✅ |
| HU14-CP03 | `ListaDeseosTest` | `anadirUnProductoQueYaEstaNoLoDuplica` | Dominio | AB#360 | PR #196 | ✅ |
| HU14-CP03 | `GestionListaDeseosServiceTest` | `anadirUnProductoQueYaEstaNoEsUnError` | Aplicación | AB#360 | PR #196 | ✅ |
| HU14-CP04 | `GestionListaDeseosServiceTest` | `unProductoNoDisponibleNoCreaLaLista` | Aplicación | AB#360 | PR #196 | ✅ |
| HU14-CP04 | `GestionListaDeseosServiceTest` | `unProductoNoDisponibleNoCambiaLaLista` | Aplicación | AB#360 | PR #196 | ✅ |
| HU14-CP04 | `ProductosAdapterTest` | `unProductoQueDevuelveProductosEstaDisponible` | Infraestructura | AB#360 | PR #196 | ✅ |
| HU14-CP04 | `ProductosAdapterTest` | `unProductoQueNoDevuelveProductosNoEstaDisponible` | Infraestructura | AB#360 | PR #196 | ✅ |
| HU14-CP05 | `SeguridadDeseosServiceTest` | `elClienteAnadeConSuPropioCorreo` | Aplicación | AB#360 | PR #196 | ✅ |
| HU14-CP05 | `SeguridadDeseosServiceTest` | `unClienteNoAnadeConElCorreoDeOtro` | Aplicación | AB#360 | PR #196 | ✅ |
| HU14-CP05 | `SeguridadDeseosServiceTest` | `unVendedorNoAnade` | Aplicación | AB#360 | PR #196 | ✅ |
| HU14-CP05 | `SeguridadDeseosServiceTest` | `unAdministradorNoAnade` | Aplicación | AB#360 | PR #196 | ✅ |
| HU14-CP05 | `SeguridadDeseosServiceTest` | `sinSesionSeRechaza` | Aplicación | AB#360 | PR #196 | ✅ |
| HU14-CP06 | `ListaDeseosDocumentMapperTest` | `convierteADocumentoYVueltaSinPerderDatos` | Infraestructura | AB#360 | PR #196 | ✅ |
| HU14-CP06 | `ListaDeseosMongoAdapterTest` | `guardarUnaListaNuevaYRecuperarlaPorCliente` | Infraestructura | AB#360 | PR #196 | ✅ |
| HU14-CP06 | `ListaDeseosMongoAdapterTest` | `guardarOtraVezActualizaLaLista` | Infraestructura | AB#360 | PR #196 | ✅ |
| HU14-CP06 | `ListaDeseosMongoAdapterTest` | `laColeccionSoloAdmiteUnaListaPorCliente` | Infraestructura | AB#360 | PR #196 | ✅ |
| HU14-CP07 | `ListaDeseosControllerTest` | `anadirRespondeConLaListaDelClienteEnSesion` | Infraestructura | AB#358 | PR #198 | ✅ |
| HU14-CP08 | `ListaDeseosControllerTest` | `cuerpoInvalidoNoLlegaAlCasoDeUso` (sin `productoId`, nulo, vacío, en blanco y JSON mal formado) | Infraestructura | AB#358 | PR #198 | ✅ |
| HU14-CP09 | `ListaDeseosControllerTest` | `unProductoNoDisponibleResponde404` | Infraestructura | AB#358 | PR #198 | ✅ |
| HU14-CP10 | `ListaDeseosControllerTest` | `sinSesionNoSeAnade` | Infraestructura | AB#358 | PR #198 | ✅ |
| HU14-CP10 | `ListaDeseosControllerTest` | `noClienteNoAnade` (vendedor y administrador) | Infraestructura | AB#358 | PR #198 | ✅ |
| HU14-CP10 | `ListaDeseosControllerTest` | `sinTokenCsrfNoSeAnade` | Infraestructura | AB#358 | PR #198 | ✅ |
| HU14-CP11 | `GestionListaDeseosServiceTest` | `consultarDevuelveCadaProductoConSusDatosActuales` | Aplicación | AB#362 | PR #205 | ✅ |
| HU14-CP11 | `GestionListaDeseosServiceTest` | `sinListaDevuelveUnaVaciaSinGuardarla` | Aplicación | AB#362 | PR #205 | ✅ |
| HU14-CP12 | `GestionListaDeseosServiceTest` | `unProductoDesactivadoSigueEnLaLista` | Aplicación | AB#362 | PR #205 | ✅ |
| HU14-CP13 | `ListaDeseosTest` | `unProductoEliminadoDesapareceDeLaLista` | Dominio | AB#362 | PR #205 | ✅ |
| HU14-CP13 | `ListaDeseosTest` | `sinProductosEliminadosLaListaNoCambia` | Dominio | AB#362 | PR #205 | ✅ |
| HU14-CP13 | `GestionListaDeseosServiceTest` | `unProductoEliminadoDesapareceYLaListaSeGuarda` | Aplicación | AB#362 | PR #205 | ✅ |
| HU14-CP13 | `GestionListaDeseosServiceTest` | `sinProductosEliminadosNoSeGuardaLaLista` | Aplicación | AB#362 | PR #205 | ✅ |
| HU14-CP14 | `GestionListaDeseosServiceTest` | `elPrecioEsElQueLeTocaAlCliente` | Aplicación | AB#362 | PR #205 | ✅ |
| HU14-CP15 | `SeguridadDeseosServiceTest` | `elClienteConsultaConSuPropioCorreo` | Aplicación | AB#362 | PR #205 | ✅ |
| HU14-CP15 | `SeguridadDeseosServiceTest` | `unClienteNoConsultaConElCorreoDeOtro` | Aplicación | AB#362 | PR #205 | ✅ |
| HU14-CP15 | `SeguridadDeseosServiceTest` | `unVendedorNoConsulta` | Aplicación | AB#362 | PR #205 | ✅ |
| HU14-CP15 | `SeguridadDeseosServiceTest` | `unAdministradorNoConsulta` | Aplicación | AB#362 | PR #205 | ✅ |
| HU14-CP15 | `SeguridadDeseosServiceTest` | `sinSesionNoSeConsulta` | Aplicación | AB#362 | PR #205 | ✅ |
| HU14-CP16 | `CatalogoProductosServiceTest` | `elResumenDeUnProductoDisponibleLlevaSusDatos` | Aplicación | AB#362 | PR #205 | ✅ |
| HU14-CP16 | `CatalogoProductosServiceTest` | `unProductoDesactivadoTambienTieneResumen` | Aplicación | AB#362 | PR #205 | ✅ |
| HU14-CP16 | `CatalogoProductosServiceTest` | `unProductoQueNoExisteNoTieneResumen` | Aplicación | AB#362 | PR #205 | ✅ |
| HU14-CP16 | `SeguridadProductosServiceTest` | `unClienteConsultaElResumen` | Aplicación | AB#362 | PR #205 | ✅ |
| HU14-CP16 | `SeguridadProductosServiceTest` | `unVendedorNoConsultaElResumen` | Aplicación | AB#362 | PR #205 | ✅ |
| HU14-CP16 | `SeguridadProductosServiceTest` | `unAdministradorNoConsultaElResumen` | Aplicación | AB#362 | PR #205 | ✅ |
| HU14-CP16 | `SeguridadProductosServiceTest` | `sinSesionNoSeConsultaElResumen` | Aplicación | AB#362 | PR #205 | ✅ |
| HU14-CP17 | `ClienteEnSesionAdapterTest` | `esPremiumPreguntaAlModuloDeUsuarios` | Infraestructura | AB#362 | PR #205 | ✅ |
| HU14-CP17 | `ProductosAdapterTest` | `consultarResumenPreguntaAlModuloDeProductos` | Infraestructura | AB#362 | PR #205 | ✅ |
| HU14-CP17 | `ProductosAdapterTest` | `unProductoQueNoExisteNoTieneDatos` | Infraestructura | AB#362 | PR #205 | ✅ |
| HU14-CP18 | `ListaDeseosControllerTest` | `consultarRespondeConLosDatosDeCadaProducto` | Infraestructura | AB#361 | PR #206 | ✅ |
| HU14-CP18 | `ListaDeseosControllerTest` | `consultarUnaListaVaciaRespondeConItemsVacio` | Infraestructura | AB#361 | PR #206 | ✅ |
| HU14-CP19 | `ListaDeseosControllerTest` | `sinSesionNoSeConsultaLaLista` | Infraestructura | AB#361 | PR #206 | ✅ |
| HU14-CP19 | `ListaDeseosControllerTest` | `noClienteNoConsultaLaLista` (vendedor y administrador) | Infraestructura | AB#361 | PR #206 | ✅ |
| HU14-CP20 | `wishlist.test.js` | `HU14-CP20: pide POST /api/customer/wishlist/items con el producto y devuelve la lista del cliente` | Frontend · API | AB#357 | PR #202 | ✅ |
| HU14-CP20 | `wishlist.test.js` | `HU14-CP20: un 404 llega como error con su estado y su código` | Frontend · API | AB#357 | PR #202 | ✅ |
| HU14-CP21 | `ProductCard.test.jsx` | `HU14-CP21: la tarjeta tiene un corazón para añadir el producto a favoritos, fuera del enlace al detalle` | Frontend · componente | AB#357 | PR #202 | ✅ |
| HU14-CP21 | `ProductCard.test.jsx` | `HU14-CP21: un producto agotado también se puede añadir a favoritos` | Frontend · componente | AB#357 | PR #202 | ✅ |
| HU14-CP21 | `ProductCard.test.jsx` | `HU14-CP21: un producto no disponible no tiene el corazón` | Frontend · componente | AB#357 | PR #202 | ✅ |
| HU14-CP21 | `ProductCard.test.jsx` | `HU14-CP21: sin onAddFavorite la tarjeta no ofrece favoritos` | Frontend · componente | AB#357 | PR #202 | ✅ |
| HU14-CP21 | `ProductsPage.test.jsx` | `HU14-CP21: cada tarjeta del catálogo tiene el corazón para añadir su producto a favoritos` | Frontend · página | AB#357 | PR #202 | ✅ |
| HU14-CP22 | `ProductCard.test.jsx` | `HU14-CP22: al pulsar el corazón pide añadir ese producto a favoritos` | Frontend · componente | AB#357 | PR #202 | ✅ |
| HU14-CP22 | `ProductCard.test.jsx` | `HU14-CP22: mientras se añade, el corazón queda deshabilitado` | Frontend · componente | AB#357 | PR #202 | ✅ |
| HU14-CP22 | `ProductCard.test.jsx` | `HU14-CP22: un producto que ya está en favoritos tiene el corazón relleno para quitarlo` | Frontend · componente | AB#364 | PR #213 (modificado) | 🔍 |
| HU14-CP22 | `ProductsPage.test.jsx` | `HU14-CP22: al pulsar el corazón se añade el producto a favoritos y el corazón queda relleno` | Frontend · página | AB#364 | PR #213 (modificado) | 🔍 |
| HU14-CP23 | `ProductsPage.test.jsx` | `HU14-CP23: al añadir, se rellenan también los corazones de los productos que ya estaban en la lista del cliente` | Frontend · página | AB#364 | PR #213 (modificado) | 🔍 |
| HU14-CP24 | `ProductCard.test.jsx` | `HU14-CP24: si %s, la tarjeta lo avisa y el corazón se puede volver a pulsar` (2 variantes: el producto ya no está disponible y falla por otro motivo) | Frontend · componente | AB#357 | PR #202 | ✅ |
| HU14-CP24 | `ProductCard.test.jsx` | `HU14-CP24: al volver a pulsar el corazón desaparece el aviso anterior` | Frontend · componente | AB#357 | PR #202 | ✅ |
| HU14-CP24 | `ProductsPage.test.jsx` | `HU14-CP24: si el producto ya no está disponible, su tarjeta lo avisa y el corazón sigue vacío` | Frontend · página | AB#357 | PR #202 | ✅ |
| HU14-CP25 | `ListaDeseosTest` | `quitarUnProductoLoSacaDeLaLista` | Dominio | AB#366 | PR #208 | ✅ |
| HU14-CP25 | `GestionListaDeseosServiceTest` | `quitarUnProductoDevuelveLaListaActualizada` | Aplicación | AB#366 | PR #208 | ✅ |
| HU14-CP26 | `ListaDeseosTest` | `quitarUnProductoQueNoEstaNoCambiaLaLista` | Dominio | AB#366 | PR #208 | ✅ |
| HU14-CP26 | `GestionListaDeseosServiceTest` | `quitarUnProductoQueNoEstaNoGuardaNada` | Aplicación | AB#366 | PR #208 | ✅ |
| HU14-CP26 | `GestionListaDeseosServiceTest` | `quitarSinListaNoLaCrea` | Aplicación | AB#366 | PR #208 | ✅ |
| HU14-CP27 | `GestionListaDeseosServiceTest` | `unProductoDesactivadoTambienSeQuita` | Aplicación | AB#366 | PR #208 | ✅ |
| HU14-CP28 | `SeguridadDeseosServiceTest` | `elClienteQuitaConSuPropioCorreo` | Aplicación | AB#366 | PR #208 | ✅ |
| HU14-CP28 | `SeguridadDeseosServiceTest` | `unClienteNoQuitaConElCorreoDeOtro` | Aplicación | AB#366 | PR #208 | ✅ |
| HU14-CP28 | `SeguridadDeseosServiceTest` | `unVendedorNoQuita` | Aplicación | AB#366 | PR #208 | ✅ |
| HU14-CP28 | `SeguridadDeseosServiceTest` | `unAdministradorNoQuita` | Aplicación | AB#366 | PR #208 | ✅ |
| HU14-CP28 | `SeguridadDeseosServiceTest` | `sinSesionNoSeQuita` | Aplicación | AB#366 | PR #208 | ✅ |
| HU14-CP29 | `ListaDeseosControllerTest` | `quitarRespondeConLaListaActualizada` | Infraestructura | AB#365 | PR #209 | ✅ |
| HU14-CP30 | `ListaDeseosControllerTest` | `sinSesionNoSeQuita` | Infraestructura | AB#365 | PR #209 | ✅ |
| HU14-CP30 | `ListaDeseosControllerTest` | `noClienteNoQuita` (vendedor y administrador) | Infraestructura | AB#365 | PR #209 | ✅ |
| HU14-CP30 | `ListaDeseosControllerTest` | `sinTokenCsrfNoSeQuita` | Infraestructura | AB#365 | PR #209 | ✅ |
| HU14-CP31 | `wishlist.test.js` | `HU14-CP31: pide GET /api/customer/wishlist y devuelve la lista con los datos de cada producto` | Frontend · API | AB#363 | PR #213 | 🔍 |
| HU14-CP31 | `wishlist.test.js` | `HU14-CP31: un 403 llega como error con su estado y su código` | Frontend · API | AB#363 | PR #213 | 🔍 |
| HU14-CP31 | `wishlist.test.js` | `HU14-CP31: pide DELETE /api/customer/wishlist/items/{productoId} y devuelve la lista actualizada` | Frontend · API | AB#364 | PR #213 | 🔍 |
| HU14-CP32 | `WishlistPage.test.jsx` | `HU14-CP32: el cliente abre Mis favoritos en /wishlist` | Frontend · página | AB#363 | PR #213 | 🔍 |
| HU14-CP32 | `WishlistPage.test.jsx` | `HU14-CP32: sin sesión, Mis favoritos lleva a iniciar sesión` | Frontend · página | AB#363 | PR #213 | 🔍 |
| HU14-CP32 | `WishlistPage.test.jsx` | `HU14-CP32: un vendedor no tiene lista de deseos: va a su página` | Frontend · página | AB#363 | PR #213 | 🔍 |
| HU14-CP32 | `AppHeader.test.jsx` | `HU14-CP32: el cliente tiene el enlace a sus favoritos y lleva a Mis favoritos` | Frontend · componente | AB#363 | PR #213 | 🔍 |
| HU14-CP32 | `AppHeader.test.jsx` | `HU14-CP32: el vendedor y el administrador no tienen el enlace a favoritos` | Frontend · componente | AB#363 | PR #213 | 🔍 |
| HU14-CP33 | `WishlistPage.test.jsx` | `HU14-CP33: muestra los favoritos en el orden en que se añadieron, con su nombre, precio y fecha` | Frontend · página | AB#363 | PR #213 | 🔍 |
| HU14-CP33 | `WishlistPage.test.jsx` | `HU14-CP33: cada favorito muestra su imagen o, si no tiene, un marcador` | Frontend · página | AB#363 | PR #213 | 🔍 |
| HU14-CP33 | `WishlistPage.test.jsx` | `HU14-CP33: el nombre de un producto disponible lleva a su detalle` | Frontend · página | AB#363 | PR #213 | 🔍 |
| HU14-CP33 | `WishlistPage.test.jsx` | `HU14-CP33: un producto agotado lo indica y sigue llevando a su detalle` | Frontend · página | AB#363 | PR #213 | 🔍 |
| HU14-CP33 | `WishlistPage.test.jsx` | `HU14-CP33: un producto que ya no está disponible lo indica y no lleva a su detalle` | Frontend · página | AB#363 | PR #213 | 🔍 |
| HU14-CP34 | `WishlistPage.test.jsx` | `HU14-CP34: mientras carga muestra un indicador` | Frontend · página | AB#363 | PR #213 | 🔍 |
| HU14-CP34 | `WishlistPage.test.jsx` | `HU14-CP34: sin favoritos lo indica y ofrece ir a los productos` | Frontend · página | AB#363 | PR #213 | 🔍 |
| HU14-CP34 | `WishlistPage.test.jsx` | `HU14-CP34: si no se pueden cargar los favoritos lo avisa y permite reintentar` | Frontend · página | AB#363 | PR #213 | 🔍 |
| HU14-CP35 | `WishlistPage.test.jsx` | `HU14-CP35: cada favorito, también uno que ya no está disponible, tiene su botón para quitarlo` | Frontend · página | AB#364 | PR #213 | 🔍 |
| HU14-CP35 | `WishlistPage.test.jsx` | `HU14-CP35: al quitar un favorito pide quitar ese producto, avisa y desaparece de la lista` | Frontend · página | AB#364 | PR #213 | 🔍 |
| HU14-CP35 | `WishlistPage.test.jsx` | `HU14-CP35: mientras se quita, su botón queda deshabilitado` | Frontend · página | AB#364 | PR #213 | 🔍 |
| HU14-CP35 | `WishlistPage.test.jsx` | `HU14-CP35: al quitar el último favorito se muestra el estado vacío` | Frontend · página | AB#364 | PR #213 | 🔍 |
| HU14-CP35 | `WishlistPage.test.jsx` | `HU14-CP35: si no se puede quitar, lo avisa y el producto sigue en la lista` | Frontend · página | AB#364 | PR #213 | 🔍 |
| HU14-CP36 | `ProductsPage.test.jsx` | `HU14-CP36: al entrar en el catálogo, los productos que ya están en favoritos tienen el corazón relleno` | Frontend · página | AB#363 | PR #213 | 🔍 |
| HU14-CP36 | `ProductsPage.test.jsx` | `HU14-CP36: si no se pueden cargar los favoritos, el catálogo funciona con los corazones vacíos` | Frontend · página | AB#363 | PR #213 | 🔍 |
| HU14-CP37 | `ProductCard.test.jsx` | `HU14-CP37: al pulsar el corazón relleno pide quitar ese producto de favoritos` | Frontend · componente | AB#364 | PR #213 | 🔍 |
| HU14-CP37 | `ProductCard.test.jsx` | `HU14-CP37: mientras se quita, el corazón queda deshabilitado` | Frontend · componente | AB#364 | PR #213 | 🔍 |
| HU14-CP37 | `ProductCard.test.jsx` | `HU14-CP37: si no se puede quitar, la tarjeta lo avisa y el corazón se puede volver a pulsar` | Frontend · componente | AB#364 | PR #213 | 🔍 |
| HU14-CP37 | `ProductsPage.test.jsx` | `HU14-CP37: al pulsar el corazón relleno se quita el producto de favoritos y el corazón queda vacío` | Frontend · página | AB#364 | PR #213 | 🔍 |

---

## HU15 · Acceso a la zona de vendedor

### Tabla A · HU15 ↔ casos de prueba

| Caso | Descripción | Tipo | Tests | Estado |
|---|---|---|---|---|
| HU15-CP01 | Sin sesión no se accede a la zona de vendedor (401) | Integración (capa web) | 1 | ✅ Pasa |
| HU15-CP02 | Un cliente o un administrador no accede a la zona de vendedor (403) | Integración (capa web) | 1 | ✅ Pasa |
| HU15-CP03 | Un vendedor accede a la zona de vendedor | Integración (capa web) | 1 | ✅ Pasa |
| HU15-CP04 | El vendedor ve en la cabecera el enlace a sus productos (y el cliente no) | Frontend (página) | 2 | ✅ Pasa (2, AB#57, PR #105) |

Notas:
- **HU15-CP01 a CP03:** casos propuestos en la T57 (AB#57) a partir de los criterios de la HU15 (`frontend/docs/Contexto_frontend.md`). Pendientes de confirmar con el equipo y de añadir a la HU15 en Azure Boards.
- **Backend (T57):** `SecurityConfig` solo deja usar `/api/seller/**` al rol `VENDEDOR`. Aún no hay endpoints de vendedor (llegarán con los productos, HU31 y HU32): los tests usan un endpoint que solo existe en el test. La task, aunque es de tests, incluye la regla (avisado al equipo).
- **El administrador** ve la información de los vendedores en solo lectura por su propia zona (`/api/admin/sellers/...`, HU26), no por `/api/seller/**`.
- **«Vendedor activo»:** el estado de la cuenta lo comprueba el login (las cuentas `PENDIENTE` o `BLOQUEADO` no entran), no esta regla.
- **Frontend:** los tests de la HU15 (AB#213) no tienen todavía filas en la matriz.

### Tabla B · casos de prueba ↔ tests

| Caso | Clase de test | Método | Capa | Task | Origen | Estado |
|---|---|---|---|---|---|---|
| HU15-CP01 | `SecurityConfigTest` | `sinSesionNoSeAccedeALaZonaDeVendedor` | Infraestructura | AB#57 | PR #74 | ✅ |
| HU15-CP02 | `SecurityConfigTest` | `otroRolNoAccedeALaZonaDeVendedor` (cliente y administrador) | Infraestructura | AB#57 | PR #74 | ✅ |
| HU15-CP03 | `SecurityConfigTest` | `vendedorAccedeALaZonaDeVendedor` | Infraestructura | AB#57 | PR #74 | ✅ |
| HU15-CP04 | `AppHeader.test.jsx` | `HU15-CP04: con sesión de vendedor muestra el enlace a sus productos` | Frontend · página | AB#57 | PR #105 | ✅ |
| HU15-CP04 | `AppHeader.test.jsx` | `HU15-CP04: con sesión de cliente no muestra el enlace a los productos del vendedor` | Frontend · página | AB#57 | PR #105 | ✅ |

Rutas de las clases de test (bajo `backend/src/test/java/es/uclm/esibuy/backend/`):
- `SecurityConfigTest`: `shared/config/` (con `@WebMvcTest` y usuarios simulados)

---

## HU16 · Activar y desactivar productos

### Tabla A · HU16 ↔ casos de prueba

| Caso | Descripción | Tipo | Tests | Estado |
|---|---|---|---|---|
| HU16-CP01 | Cada fila de «Mis productos» ofrece Desactivar si el producto está disponible y Activar si no lo está | Frontend (página) | 1 | ✅ Pasa |
| HU16-CP02 | Al pulsar Desactivar, sin confirmación, el producto pasa a «No disponible» y se avisa «Producto desactivado.» | Frontend (página) | 1 | ✅ Pasa |
| HU16-CP03 | Al pulsar Activar, el producto pasa a «Disponible» y se avisa «Producto activado.» | Frontend (página) | 1 | ✅ Pasa |
| HU16-CP04 | Si falla el cambio, se avisa sin detalles internos y el producto no cambia | Frontend (página) | 1 | ✅ Pasa |
| HU16-CP05 | La ficha del producto ofrece Activar o Desactivar según su estado y avisa a la página con el producto | Frontend (componente) | 2 | ✅ Pasa |
| HU16-CP06 | Desde la ficha, Desactivar cierra la ficha, cambia el producto y avisa | Frontend (página) | 1 | ✅ Pasa |
| HU16-CP07 | El vendedor desactiva un producto suyo y el resto de sus datos no cambia | Unitario | 1 | ✅ Pasa |
| HU16-CP08 | El vendedor activa un producto suyo que no estaba disponible | Unitario | 1 | ✅ Pasa |
| HU16-CP09 | Un producto ajeno o inexistente no se puede activar ni desactivar (`ProductoNoEncontradoException`) y no se guarda nada | Unitario | 2 | ✅ Pasa |
| HU16-CP10 | Solo el vendedor dueño, con su correo, activa o desactiva su producto: cliente, administrador u otro vendedor reciben 403 y sin sesión se rechaza | Unitario (seguridad, contexto mínimo de Spring) | 5 | ✅ Pasa |
| HU16-CP11 | `PATCH /api/seller/products/{id}/availability` devuelve 200 con el producto actualizado; el vendedor sale de la sesión | Integración (capa web) | 1 | ✅ Pasa |
| HU16-CP12 | Un cuerpo sin `disponible` o con un valor que no es booleano devuelve 400 y no llega al caso de uso | Integración (capa web) | 1 | ✅ Pasa |
| HU16-CP13 | Cambiar la disponibilidad de un producto inexistente o de otro vendedor devuelve 404 con el código `PRODUCT_NOT_FOUND` | Integración (capa web) | 1 | ✅ Pasa |
| HU16-CP14 | Sin sesión (401), sin ser vendedor (403) o sin token CSRF (403) no se cambia la disponibilidad | Integración (capa web) | 3 | ✅ Pasa |

Notas:
- **HU16-CP01 a CP06 (Sprint 2, frontend):** casos propuestos en la task AB#281 (botón activar/desactivar productos) a partir de lo pedido en ella y de las decisiones del equipo (`frontend/docs/hu16-activar-desactivar-productos.md`). Pendientes de confirmar con el equipo y de añadir a la HU16 en Azure Boards. Los casos del backend siguen la numeración desde el CP07.
- **HU16-CP07 a CP10 (Sprint 2, backend):** casos propuestos en las tasks AB#284 y AB#285 (lógica de activación y desactivación, PR #171), que se hacen juntas en un único caso de uso, `cambiarDisponibilidadMiProducto`, porque el frontend usa un único endpoint con un booleano. Pendientes de añadir a la HU16 en Azure Boards. Pedir el estado que ya tiene no es un error: el producto se guarda igual (decisión del desarrollador). Solo CP07 tiene ciclo rojo: CP08 a CP10 ya los cubría el código del ciclo 1 (el booleano se pasa tal cual, `consultarMiProducto` filtra por vendedor y el `@PreAuthorize` se puso desde el primer test). Se comprobó que los tests de CP10 fallan si se quita el `@PreAuthorize`.
- **HU16-CP11 a CP14 (Sprint 2, backend):** casos propuestos en las tasks AB#282 y AB#283 (endpoint de activación y desactivación, PR #177), que van juntas porque son un único endpoint, con el contrato de `frontend/docs/hu16-activar-desactivar-productos.md`. Pendientes de añadir a la HU16 en Azure Boards. Solo CP11 tiene ciclo rojo: CP12 a CP14 ya los cubrían el `@NotNull` de `DisponibilidadRequest`, `ManejadorGlobalErrores` y `SecurityConfig`. Se comprobó que CP12 falla sin el `@NotNull` y que CP14 falla si `/api/seller/**` solo exige sesión.
- **HU16-CP12 y errores de validación de la API (rama `fix/errores-validacion-api`, sin task en Azure por ser transversal):** decisión del equipo en la daily del 03/11/2026. Un cuerpo que no pasa `@Valid` y uno que no se puede leer (`HttpMessageNotReadableException`: JSON mal formado o tipo incompatible) responden 400 + `VALIDATION_ERROR` + "Los datos enviados no son válidos" en todos los endpoints; antes daban `INVALID_FIELD` con el mensaje del registro y el 400 por defecto de Spring sin `code`. Jackson ya no convierte `"true"` ni `1` en booleano (`JacksonConfig`). Un `boolean` primitivo ausente o nulo ya daba 400 porque Jackson 3 activa por defecto `FAIL_ON_NULL_FOR_PRIMITIVES` (HU31-CP20 lo deja probado, sin rojo real). Los tests de HU1-CP22, HU1-CP23, HU3-CP16, HU31-CP20, HU34-CP07, HU40-CP07 y HU41-CP12 se adaptan al código común; el número de tests no cambia.
- **Versión inicial (decisión del equipo):** sin confirmación al activar o desactivar; puede cambiar por decisión del PO o del equipo.
- **Backend:** la lógica (CP07 a CP10, PR #171) y el endpoint `PATCH /api/seller/products/{id}/availability` (CP11 a CP14, PR #177) siguen el contrato que propuso el frontend. Los tests del frontend siguen usando los mocks (MSW).
- **Sin archivos de tests nuevos:** los casos van en `SellerProductsPage.test.jsx`, `ProductDetailDialog.test.jsx`, `GestionProductosServiceTest`, `SeguridadProductosServiceTest` y `ProductoControllerTest`, que ya existían.
- **Infraestructura de tests:** `test/setup.js` reinicia tras cada test la disponibilidad cambiada en el mock de productos (`resetProductsMock`).

### Tabla B · casos de prueba ↔ tests

| Caso | Clase de test | Método | Capa | Task | Origen | Estado |
|---|---|---|---|---|---|---|
| HU16-CP01 | `SellerProductsPage.test.jsx` | `HU16-CP01: un producto disponible se puede desactivar y uno no disponible, activar` | Frontend · página | AB#281 | PR #147 | ✅ |
| HU16-CP02 | `SellerProductsPage.test.jsx` | `HU16-CP02: al pulsar Desactivar, sin pedir confirmación, el producto pasa a no disponible y se avisa` | Frontend · página | AB#281 | PR #147 | ✅ |
| HU16-CP03 | `SellerProductsPage.test.jsx` | `HU16-CP03: al pulsar Activar, el producto pasa a disponible y se avisa` | Frontend · página | AB#281 | PR #147 | ✅ |
| HU16-CP04 | `SellerProductsPage.test.jsx` | `HU16-CP04: si falla el cambio, avisa sin detalles internos y el producto no cambia` | Frontend · página | AB#281 | PR #147 | ✅ |
| HU16-CP05 | `ProductDetailDialog.test.jsx` | `HU16-CP05: un producto disponible ofrece Desactivar, que avisa con el producto` | Frontend · componente | AB#281 | PR #147 | ✅ |
| HU16-CP05 | `ProductDetailDialog.test.jsx` | `HU16-CP05: un producto no disponible ofrece Activar, que avisa con el producto` | Frontend · componente | AB#281 | PR #147 | ✅ |
| HU16-CP06 | `SellerProductsPage.test.jsx` | `HU16-CP06: desde la ficha, Desactivar cierra la ficha, cambia el producto y avisa` | Frontend · página | AB#281 | PR #147 | ✅ |
| HU16-CP07 | `GestionProductosServiceTest` | `elVendedorDesactivaSuProducto` | Aplicación | AB#284 · AB#285 | PR #171 | ✅ |
| HU16-CP08 | `GestionProductosServiceTest` | `elVendedorActivaSuProducto` | Aplicación | AB#284 · AB#285 | PR #171 | ✅ |
| HU16-CP09 | `GestionProductosServiceTest` | `unProductoQueNoExisteNoCambiaDeDisponibilidad` | Aplicación | AB#284 · AB#285 | PR #171 | ✅ |
| HU16-CP09 | `GestionProductosServiceTest` | `unProductoDeOtroVendedorNoCambiaDeDisponibilidad` | Aplicación | AB#284 · AB#285 | PR #171 | ✅ |
| HU16-CP10 | `SeguridadProductosServiceTest` | `unClienteNoCambiaLaDisponibilidad` | Aplicación | AB#284 · AB#285 | PR #171 | ✅ |
| HU16-CP10 | `SeguridadProductosServiceTest` | `unAdministradorNoCambiaLaDisponibilidad` | Aplicación | AB#284 · AB#285 | PR #171 | ✅ |
| HU16-CP10 | `SeguridadProductosServiceTest` | `unVendedorNoCambiaLaDisponibilidadConElCorreoDeOtro` | Aplicación | AB#284 · AB#285 | PR #171 | ✅ |
| HU16-CP10 | `SeguridadProductosServiceTest` | `unVendedorCambiaLaDisponibilidadConSuPropioCorreo` | Aplicación | AB#284 · AB#285 | PR #171 | ✅ |
| HU16-CP10 | `SeguridadProductosServiceTest` | `sinSesionNoSeCambiaLaDisponibilidad` | Aplicación | AB#284 · AB#285 | PR #171 | ✅ |
| HU16-CP11 | `ProductoControllerTest` | `vendedorCambiaLaDisponibilidadDeSuProducto` | Infraestructura | AB#282 · AB#283 | PR #177 | ✅ |
| HU16-CP12 | `ProductoControllerTest` | `cuerpoDeDisponibilidadInvalidoNoCambiaElProducto` (sin `disponible`, nulo, no booleano, `"true"` como texto, `1` y JSON mal formado; comprueba el `code` `VALIDATION_ERROR`) | Infraestructura | AB#282 · AB#283 | PR #177 (modificado en PR #183) | ✅ |
| HU16-CP13 | `ProductoControllerTest` | `cambiarLaDisponibilidadDeUnProductoNoEncontradoDevuelve404` | Infraestructura | AB#282 · AB#283 | PR #177 | ✅ |
| HU16-CP14 | `ProductoControllerTest` | `sinSesionNoSeCambiaLaDisponibilidad` | Infraestructura | AB#282 · AB#283 | PR #177 | ✅ |
| HU16-CP14 | `ProductoControllerTest` | `noVendedorNoCambiaLaDisponibilidad` (cliente y administrador) | Infraestructura | AB#282 · AB#283 | PR #177 | ✅ |
| HU16-CP14 | `ProductoControllerTest` | `sinTokenCsrfNoSeCambiaLaDisponibilidad` | Infraestructura | AB#282 · AB#283 | PR #177 | ✅ |

Rutas de las clases de test:
- `SellerProductsPage.test.jsx`: `frontend/src/pages/` (Vitest, RTL y MSW)
- `ProductDetailDialog.test.jsx`: `frontend/src/features/seller/` (Vitest, RTL y MSW)
- `GestionProductosServiceTest` y `SeguridadProductosServiceTest`: `backend/src/test/java/es/uclm/esibuy/backend/productos/application/service/`
- `ProductoControllerTest`: `backend/src/test/java/es/uclm/esibuy/backend/productos/infrastructure/adapter/in/web/`

---

## HU17 · Consulta de estadísticas de ventas

### Tabla A · HU17 ↔ casos de prueba

| Caso | Descripción | Tipo | Tests | Estado |
|---|---|---|---|---|
| HU17-CP01 | Las ventas cuentan los pedidos distintos, las unidades y los ingresos; sin ventas, todo a cero | Unitario (dominio) | 2 | ✅ Pasa |
| HU17-CP02 | Los 5 productos más vendidos, sumando las unidades de cada uno y de más a menos; a igualdad de unidades, primero el que más ha ingresado | Unitario (dominio) | 3 | ✅ Pasa |
| HU17-CP03 | La valoración media global pondera la media de cada producto por su número de valoraciones; sin valoraciones no hay media y el total es 0 | Unitario (dominio) | 2 | ✅ Pasa |
| HU17-CP04 | La valoración por producto solo incluye los valorados, del mejor al peor | Unitario (dominio) | 1 | ✅ Pasa |
| HU17-CP05 | El stock cuenta los productos, los que no tienen stock, los de stock bajo (de 1 a 5 unidades) y los no disponibles | Unitario (dominio) | 2 | ✅ Pasa |
| HU17-CP06 | Las ventas del vendedor son las líneas de sus pedidos con su importe; los pedidos `CANCELADO` no cuentan | Unitario (aplicación, pedidos) | 3 | ✅ Pasa |
| HU17-CP07 | En MongoDB, `buscarPorVendedor` devuelve todos los pedidos del vendedor, también los cancelados | Integración (Testcontainers) | 1 | ✅ Pasa |
| HU17-CP08 | El inventario del vendedor da el stock, la disponibilidad y las valoraciones de cada producto | Unitario (aplicación, productos) | 2 | ✅ Pasa |
| HU17-CP09 | En MongoDB, `listarTodosPorVendedor` devuelve todos sus productos, también los no disponibles, sin paginar | Integración (Testcontainers) | 1 | ✅ Pasa |
| HU17-CP10 | Las estadísticas se calculan con las ventas y los productos del vendedor de la sesión, nunca de otro | Unitario (aplicación) | 1 | ✅ Pasa |
| HU17-CP11 | `GET /api/seller/statistics` devuelve las estadísticas agrupadas en ventas, valoraciones y stock; sin valoraciones, la media llega vacía | Web (`@WebMvcTest`) | 2 | ✅ Pasa |
| HU17-CP12 | Sin sesión, 401; un cliente o un administrador, 403 | Web (`@WebMvcTest`, seguridad real) | 3 | ✅ Pasa |
| HU17-CP13 | Los adaptadores piden el vendedor en sesión a usuarios, las ventas a pedidos y los productos a productos, sin perder datos | Unitario (infraestructura, Mockito) | 3 | ✅ Pasa |
| HU17-CP14 | En «Mi perfil» el vendedor ve sus ventas: pedidos, unidades, ingresos y los 5 más vendidos en orden | Frontend (página) | 1 | 🔍 En revisión (1, AB#278, PR #214) |
| HU17-CP15 | Ve la valoración media global con un decimal, el total de valoraciones y la media de cada producto valorado, del mejor al peor | Frontend (página) | 1 | 🔍 En revisión (1, AB#278, PR #214) |
| HU17-CP16 | Ve sus productos, los que no tienen stock, los de stock bajo (de 1 a 5 unidades) y los no disponibles | Frontend (página) | 1 | 🔍 En revisión (1, AB#278, PR #214) |
| HU17-CP17 | Sin ventas ni valoraciones, se avisa con un estado vacío en vez de tablas vacías y la media se ve como «Sin valoraciones» | Frontend (página) | 1 | 🔍 En revisión (1, AB#278, PR #214) |
| HU17-CP18 | El cliente no ve la sección de estadísticas y no se piden al backend | Frontend (página) | 1 | 🔍 En revisión (1, AB#278, PR #214) |
| HU17-CP19 | Si las estadísticas no cargan, se avisa sin detalles internos, se puede reintentar y el perfil sigue visible | Frontend (página) | 1 | 🔍 En revisión (1, AB#278, PR #214) |
| HU17-CP20 | Mientras cargan las estadísticas, la sección muestra que se están cargando | Frontend (página) | 1 | 🔍 En revisión (1, AB#278, PR #214) |

Notas:
- **HU17-CP01 a CP13 (Sprint 2, backend, TDD):** casos propuestos en la T280 (AB#280, cálculo y devolución de estadísticas) y la T279 (AB#279, endpoint) a partir del criterio de aceptación de la HU17 (AB#78): «se muestran en una vista estadísticas básicas de ventas, valoraciones y stock». Pendientes de confirmar con el equipo.
- **Decisiones del equipo:** set básico de métricas, desde siempre y sin filtro de fechas. Ventas: pedidos, unidades e ingresos sin contar los `CANCELADO`, y los 5 más vendidos. Valoraciones: media global ponderada y media por producto. Stock: productos, sin stock, stock bajo (de 1 a 5 unidades) y no disponibles. Módulo nuevo `estadisticas`, que pide los datos a `pedidos` (`VentasVendedorUseCase`) y a `productos` (`InventarioVendedorUseCase`) por sus puertos de entrada.
- **Reutilización:** `buscarPorVendedor` (pedidos) y `listarTodosPorVendedor` (productos) los añadió la HU47 (AB#312 y AB#313, PR #203); la HU17 los reutiliza. HU17-CP07 y CP09 prueban lo que la HU17 necesita de ellos y los tests de la HU47 no cubren: que incluyen los pedidos cancelados y los productos no disponibles, sin paginar.
- **HU17-CP14 a CP20 (Sprint 2, frontend, TDD):** casos propuestos en la T278 (AB#278, vista de estadísticas) a partir del mismo criterio de aceptación y del contrato de la T279. Siguen la numeración del backend. La vista es una sección «Mis estadísticas» en «Mi perfil», solo para el rol `VENDEDOR`. El endpoint se simula con MSW (`frontend/src/mocks/statisticsHandlers.js`). HU17-CP18 no tuvo ciclo rojo: la condición por rol entró con el CP14, y se comprobó que el test falla si se quita. Pendientes de confirmar con el equipo.

### Tabla B · casos de prueba ↔ tests

| Caso | Clase de test | Método | Capa | Task | Origen | Estado |
|---|---|---|---|---|---|---|
| HU17-CP01 | `EstadisticasVendedorTest` | `lasVentasCuentanPedidosUnidadesEIngresos` | Dominio | AB#280 | PR #212 | ✅ |
| HU17-CP01 | `EstadisticasVendedorTest` | `sinVentasLasVentasEstanACero` | Dominio | AB#280 | PR #212 | ✅ |
| HU17-CP02 | `EstadisticasVendedorTest` | `losMasVendidosAgrupanPorProductoYOrdenanPorUnidades` | Dominio | AB#280 | PR #212 | ✅ |
| HU17-CP02 | `EstadisticasVendedorTest` | `losMasVendidosDesempatanPorIngresos` | Dominio | AB#280 | PR #212 | ✅ |
| HU17-CP02 | `EstadisticasVendedorTest` | `soloSeMuestranLosCincoMasVendidos` | Dominio | AB#280 | PR #212 | ✅ |
| HU17-CP03 | `EstadisticasVendedorTest` | `laMediaGlobalEstaPonderadaPorElNumeroDeValoraciones` | Dominio | AB#280 | PR #212 | ✅ |
| HU17-CP03 | `EstadisticasVendedorTest` | `sinValoracionesNoHayMediaGlobal` | Dominio | AB#280 | PR #212 | ✅ |
| HU17-CP04 | `EstadisticasVendedorTest` | `laValoracionPorProductoSoloIncluyeLosValoradosDelMejorAlPeor` | Dominio | AB#280 | PR #212 | ✅ |
| HU17-CP05 | `EstadisticasVendedorTest` | `elStockCuentaProductosSinStockYNoDisponibles` | Dominio | AB#280 | PR #212 | ✅ |
| HU17-CP05 | `EstadisticasVendedorTest` | `stockBajoEsEntreUnaYCincoUnidades` | Dominio | AB#280 | PR #212 | ✅ |
| HU17-CP06 | `VentasVendedorServiceTest` | `lasVentasSonLasLineasDeSusPedidos` | Aplicación | AB#280 | PR #212 | ✅ |
| HU17-CP06 | `VentasVendedorServiceTest` | `losPedidosCanceladosNoCuentan` | Aplicación | AB#280 | PR #212 | ✅ |
| HU17-CP06 | `VentasVendedorServiceTest` | `sinPedidosNoHayVentas` | Aplicación | AB#280 | PR #212 | ✅ |
| HU17-CP07 | `PedidoMongoAdapterTest` | `buscarPorVendedorIncluyeLosPedidosCancelados` | Infraestructura | AB#280 | PR #212 | ✅ |
| HU17-CP08 | `InventarioVendedorServiceTest` | `elInventarioDaStockDisponibilidadYValoraciones` | Aplicación | AB#280 | PR #212 | ✅ |
| HU17-CP08 | `InventarioVendedorServiceTest` | `sinProductosElInventarioEstaVacio` | Aplicación | AB#280 | PR #212 | ✅ |
| HU17-CP09 | `ProductoMongoAdapterTest` | `listarTodosPorVendedorIncluyeLosNoDisponiblesSinPaginar` | Infraestructura | AB#280 | PR #212 | ✅ |
| HU17-CP10 | `EstadisticasVendedorServiceTest` | `calculaConLasVentasYLosProductosDelVendedorDeLaSesion` | Aplicación | AB#280 | PR #212 | ✅ |
| HU17-CP11 | `EstadisticasVendedorControllerTest` | `vendedorRecibeSusEstadisticas` | Infraestructura | AB#279 | PR #212 | ✅ |
| HU17-CP11 | `EstadisticasVendedorControllerTest` | `sinValoracionesLaMediaLlegaVacia` | Infraestructura | AB#279 | PR #212 | ✅ |
| HU17-CP12 | `EstadisticasVendedorControllerTest` | `sinSesionDevuelve401` | Infraestructura | AB#279 | PR #212 | ✅ |
| HU17-CP12 | `EstadisticasVendedorControllerTest` | `otroRolDevuelve403 (CLIENTE y ADMINISTRADOR)` | Infraestructura | AB#279 | PR #212 | ✅ |
| HU17-CP13 | `VendedorEnSesionAdapterTest` | `elIdDelVendedorSePideAlModuloDeUsuarios` | Infraestructura | AB#279 | PR #212 | ✅ |
| HU17-CP13 | `VentasAdapterTest` | `lasVentasSePidenAlModuloDePedidos` | Infraestructura | AB#279 | PR #212 | ✅ |
| HU17-CP13 | `InventarioAdapterTest` | `losProductosSePidenAlModuloDeProductos` | Infraestructura | AB#279 | PR #212 | ✅ |
| HU17-CP14 | `SellerStatistics.test.jsx` | `HU17-CP14: el vendedor ve sus pedidos, unidades e ingresos y los más vendidos en orden` | Frontend · página | AB#278 | PR #214 | 🔍 |
| HU17-CP15 | `SellerStatistics.test.jsx` | `HU17-CP15: el vendedor ve la media global, el total y la valoración de cada producto, del mejor al peor` | Frontend · página | AB#278 | PR #214 | 🔍 |
| HU17-CP16 | `SellerStatistics.test.jsx` | `HU17-CP16: el vendedor ve cuántos productos tiene, sin stock, con stock bajo y no disponibles` | Frontend · página | AB#278 | PR #214 | 🔍 |
| HU17-CP17 | `SellerStatistics.test.jsx` | `HU17-CP17: sin ventas ni valoraciones, avisa de que aún no hay en vez de mostrar tablas vacías` | Frontend · página | AB#278 | PR #214 | 🔍 |
| HU17-CP18 | `SellerStatistics.test.jsx` | `HU17-CP18: el cliente no ve las estadísticas y no se le piden al backend` | Frontend · página | AB#278 | PR #214 | 🔍 |
| HU17-CP19 | `SellerStatistics.test.jsx` | `HU17-CP19: si las estadísticas no cargan, avisa sin detalles internos, permite reintentar y el perfil sigue visible` | Frontend · página | AB#278 | PR #214 | 🔍 |
| HU17-CP20 | `SellerStatistics.test.jsx` | `HU17-CP20: mientras cargan las estadísticas, la sección muestra que se están cargando` | Frontend · página | AB#278 | PR #214 | 🔍 |

Rutas de las clases de test (bajo `backend/src/test/java/es/uclm/esibuy/backend/`):
- `EstadisticasVendedorTest`: `estadisticas/domain/model/` (JUnit)
- `EstadisticasVendedorServiceTest`: `estadisticas/application/service/` (JUnit, con dobles de prueba propios, sin Spring)
- `EstadisticasVendedorControllerTest`: `estadisticas/infrastructure/adapter/in/web/` (`@WebMvcTest` con la seguridad real)
- `VendedorEnSesionAdapterTest`, `VentasAdapterTest` e `InventarioAdapterTest`: `estadisticas/infrastructure/adapter/out/usuarios/`, `.../pedidos/` y `.../productos/` (Mockito)
- `VentasVendedorServiceTest`: `pedidos/application/service/` (JUnit, con dobles de prueba propios)
- `PedidoMongoAdapterTest`: `pedidos/infrastructure/adapter/out/persistence/` (Testcontainers)
- `InventarioVendedorServiceTest`: `productos/application/service/` (Mockito)
- `ProductoMongoAdapterTest`: `productos/infrastructure/adapter/out/persistence/` (Testcontainers)

Ruta del test del frontend:
- `SellerStatistics.test.jsx`: `frontend/src/features/seller/` (Vitest, RTL y MSW; renderiza la app en `/profile`)

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
| HU19-CP01 | Muestra nombre, email, rol, estado y fecha de alta de cada usuario | Unitario + integración | 4 | ✅ Pasa |
| HU19-CP02 | Busca por nombre o email | Unitario + integración | 5 | ✅ Pasa |
| HU19-CP03 | Filtra por rol | Unitario + integración | 4 | ✅ Pasa |
| HU19-CP04 | Filtra por estado | Unitario + integración | 4 | ✅ Pasa |
| HU19-CP05 | El listado está paginado de 10 en 10 | Unitario + integración | 5 | ✅ Pasa |
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
## HU21 · Alta de administradores

### Tabla A · HU21 ↔ casos de prueba

| Caso | Descripción | Tipo | Tests | Estado |
|---|---|---|---|---|
| HU21-CP01 | Crea el administrador en estado ACTIVO y con la contraseña cifrada | Unitario | 1 | ✅ Pasa |
| HU21-CP02 | Rechaza la creación si el correo ya está registrado y no guarda nada | Unitario | 1 | ✅ Pasa |
| HU21-CP03 | Delega a la política de contraseñas y rechaza una muy corta | Unitario | 1 | ✅ Pasa |
| HU21-CP04 | Rechaza una contraseña que contiene el nombre del administrador | Unitario | 1 | ✅ Pasa |
| HU21-CP05 | El factory method crea un administrador activo y sin datos de cliente | Unitario | 1 | ✅ Pasa |
| HU21-CP06 | Crear administrador con datos correctos devuelve 201 | Integración (capa web) | 1 | ✅ Pasa |
| HU21-CP07 | Crear administrador rechaza campos obligatorios vacíos devolviendo 400 | Integración (capa web) | 1 | ✅ Pasa |
| HU21-CP08 | Usuarios sin rol ADMINISTRADOR no pueden crear administradores | Integración (capa web) | 1 | ✅ Pasa |
| HU21-CP09 | Un administrador se lee sin datos de cliente ni consentimiento | Unitario | 1 | ✅ Pasa |
| HU21-CP10 | Activar un vendedor pendiente lo deja activo y lo guarda | Unitario | 1 | ✅ Pasa |
| HU21-CP11 | Desactivar un vendedor activo lo deja desactivado y lo guarda | Unitario | 1 | ✅ Pasa |
| HU21-CP12 | Activar o bloquear a un usuario que no es vendedor falla como si no existiera y no guarda nada | Unitario | 2 | ✅ Pasa |
| HU21-CP13 | Un administrador activa un vendedor y recibe sus datos con estado ACTIVO | Integración (capa web) | 1 | ✅ Pasa |
| HU21-CP14 | Un administrador bloquea un vendedor y recibe sus datos con estado BLOQUEADO | Integración (capa web) | 1 | ✅ Pasa |
| HU21-CP15 | Cambiar un vendedor a un estado que no es ACTIVO ni BLOQUEADO devuelve 400 y no cambia nada | Integración (capa web) | 1 | ✅ Pasa |
| HU21-CP16 | Cambiar el estado de un usuario que no existe o no es vendedor devuelve 404 | Integración (capa web) | 1 | ✅ Pasa |
| HU21-CP17 | Sin sesión o sin rol ADMINISTRADOR no se puede cambiar el estado de un vendedor | Integración (capa web) | 2 | ✅ Pasa |
| HU21-CP18 | Guarda el avatar elegido como imagen de perfil del administrador | Unitario + integración (capa web) | 2 | ✅ Pasa |
| HU21-CP19 | Al crear un administrador, un dato rechazado por el backend se marca en su campo | Frontend (página) | 1 | ✅ Pasa (1, AB#50, PR #105) |
| HU21-CP20 | En el alta de administrador, ni el password ni su repetición se pueden copiar, cortar ni pegar, y escribiéndolos a mano la cuenta se sigue creando | Frontend (página) | 3 | 🔍 En revisión (3, AB#242, PR pendiente) |

Notas:
- **Alta de administradores (AB#49):** Siguiendo el acuerdo con el frontend, la creación la realiza un administrador autenticado que establece manualmente la contraseña (opción "a" del documento `Contexto_frontend.md`).
- **HU21-CP05:** Las cuentas con rol `ADMINISTRADOR` nacen directamente en estado `ACTIVO` porque la creación la realiza otro administrador ya verificado.
- **HU21-CP06 y CP07:** El endpoint `POST /api/admin/admins` acepta los campos `sede` y `avatar` en el JSON para ser 100% compatible con la llamada de la API del frontend. Desde la T47 (HU21-CP18) el `avatar` se guarda como imagen de perfil; la `sede` sigue sin guardarse (pendiente por decisión del equipo).
- **HU21-CP10 a CP17 (AB#47):** activar y desactivar vendedores desde su supervisión con `PATCH /api/admin/sellers/{id}/status`, el contrato de `frontend/docs/hu21-alta-admin-y-vendedores.md`, que hasta ahora solo respondía el mock. Solo cambia el estado de un vendedor: otro usuario devuelve 404, como la ficha de la HU26. Casos propuestos, pendientes de confirmar con el equipo y de añadir a Azure Boards.
- **HU21-CP18 (AB#47):** caso propuesto, pendiente de confirmar con el equipo y de añadir a Azure Boards.

### Tabla B · casos de prueba ↔ tests

| Caso | Clase de test | Método | Capa | Task | Origen | Estado |
|---|---|---|---|---|---|---|
| HU21-CP01 | `CrearAdministradorServiceTest` | `creaAdministradorActivoYConContrasenaCifrada` | Aplicación | AB#49 | PR actual | ✅ |
| HU21-CP02 | `CrearAdministradorServiceTest` | `rechazaEmailYaRegistrado` | Aplicación | AB#49 | PR actual | ✅ |
| HU21-CP03 | `CrearAdministradorServiceTest` | `rechazaContrasenaMuyCorta` | Aplicación | AB#49 | PR actual | ✅ |
| HU21-CP04 | `CrearAdministradorServiceTest` | `rechazaContrasenaConElNombre` | Aplicación | AB#49 | PR actual | ✅ |
| HU21-CP05 | `UsuarioTest` | `creaAdministradorNuevoActivoYSinDatosCliente` | Dominio | AB#49 | PR actual | ✅ |
| HU21-CP06 | `UsuarioControllerTest` | `crearAdministradorValidoDevuelve201` | Infraestructura | AB#49 | PR actual | ✅ |
| HU21-CP07 | `UsuarioControllerTest` | `crearAdministradorRechazaCamposVacios` | Infraestructura | AB#49 | PR actual | ✅ |
| HU21-CP08 | `UsuarioControllerTest` | `crearAdministradorRechazaNoAdministradores` | Infraestructura | AB#49 | PR actual | ✅ |
| HU21-CP09 | `UsuarioDocumentMapperTest` | `unAdministradorSeLeeSinDatosDeClienteYConsentimiento` | Infraestructura | AB#49 | PR actual | ✅ |
| HU21-CP10 | `AdministracionUsuariosServiceTest` | `activarVendedorPendienteLoDejaActivoYLoGuarda` | Aplicación | AB#47 | PR #102 | ✅ |
| HU21-CP11 | `AdministracionUsuariosServiceTest` | `bloquearVendedorActivoLoDejaBloqueadoYLoGuarda` | Aplicación | AB#47 · AB#243 · AB#252 | PR #102 (modificado en PR #180) (renombrado en PR #185) | ✅ |
| HU21-CP12 | `AdministracionUsuariosServiceTest` | `activarVendedorDeOtroRolFalla` | Aplicación | AB#47 | PR #102 | ✅ |
| HU21-CP12 | `AdministracionUsuariosServiceTest` | `bloquearVendedorDeOtroRolFalla` | Aplicación | AB#47 · AB#252 | PR #102 (renombrado en PR #185) | ✅ |
| HU21-CP13 | `UsuarioControllerTest` | `administradorActivaUnVendedorYRecibeSusDatos` | Infraestructura | AB#47 | PR #102 | ✅ |
| HU21-CP14 | `UsuarioControllerTest` | `administradorBloqueaUnVendedorYRecibeSusDatos` | Infraestructura | AB#47 · AB#243 · AB#252 | PR #102 (modificado en PR #180) (renombrado en PR #185) | ✅ |
| HU21-CP15 | `UsuarioControllerTest` | `cambiarVendedorAUnEstadoNoAdmitidoDevuelve400` | Infraestructura | AB#47 | PR #102 | ✅ |
| HU21-CP16 | `UsuarioControllerTest` | `cambiarEstadoDeUnNoVendedorDevuelve404` | Infraestructura | AB#47 | PR #102 | ✅ |
| HU21-CP17 | `UsuarioControllerTest` | `cambiarEstadoDeVendedorSinSesionDevuelve401` | Infraestructura | AB#47 | PR #102 | ✅ |
| HU21-CP17 | `UsuarioControllerTest` | `noAdministradorNoCambiaElEstadoDeUnVendedor` | Infraestructura | AB#47 | PR #102 | ✅ |
| HU21-CP18 | `CrearAdministradorServiceTest` | `guardaElAvatarComoImagenDePerfil` | Aplicación | AB#47 | PR #102 | ✅ |
| HU21-CP18 | `UsuarioControllerTest` | `crearAdministradorPasaElAvatar` | Infraestructura | AB#47 | PR #102 | ✅ |
| HU21-CP19 | `AdminNewAdminPage.test.jsx` | `HU21-CP19: una contraseña rechazada por el backend se marca en su campo` | Frontend · página | AB#50 | PR #105 | ✅ |
| HU21-CP20 | `AdminNewAdminPage.test.jsx` | `HU21-CP20: ni el password ni su repetición se pueden pegar` | Frontend · página | AB#242 | PR pendiente | 🔍 |
| HU21-CP20 | `AdminNewAdminPage.test.jsx` | `HU21-CP20: ni el password ni su repetición se pueden copiar o cortar, tampoco con el password visible` | Frontend · página | AB#242 | PR pendiente | 🔍 |
| HU21-CP20 | `AdminNewAdminPage.test.jsx` | `HU21-CP20: escribiendo el password a mano en los dos campos el administrador se sigue creando` | Frontend · página | AB#242 | PR pendiente | 🔍 |

---

## HU20 · Consultar el detalle de un usuario

### Tabla A · HU20 ↔ casos de prueba

| Caso | Descripción | Tipo | Tests | Estado |
|---|---|---|---|---|
| HU20-CP01 | La ficha de un cliente trae sus datos personales y de cliente, sin datos sensibles | Unitario + integración (capa web) | 2 | ✅ Pasa |
| HU20-CP02 | La ficha de un administrador no trae datos de cliente | Integración (capa web) | 1 | ✅ Pasa |
| HU20-CP03 | Consultar un usuario que no existe devuelve 404 | Unitario + integración (capa web) | 2 | ✅ Pasa |
| HU20-CP04 | Sin sesión (401) o sin ser administrador (403) no se ve la ficha | Integración (capa web) | 2 | ✅ Pasa |
| HU20-CP05 | La ficha de un administrador trae su fecha de incorporación, que es su fecha de alta; la de un cliente no la trae | Integración (capa web) | 2 | ✅ Pasa |
| HU20-CP06 | La ficha de un vendedor trae sus datos de vendedor (nombre comercial, categoría principal, DNI y teléfono) y no los de cliente | Integración (capa web) | 1 | ✅ Pasa |
| HU20-CP07 | La administración de usuarios (consultar, editar, activar, desactivar, eliminar administradores, listar y crear administradores) exige el rol administrador y, al desactivar o eliminar, que el correo del administrador sea el de la sesión; el nombre comercial de un vendedor es del cliente (lo usa el carrito); sin sesión se rechaza. Seguridad en la capa de negocio (R8) | Unitario con Spring Security (sin MongoDB) | 7 | 🔍 En revisión (7, rama `fix/seguridad-capa-negocio`) |

Notas:
- **HU20-CP01 a CP04:** casos propuestos en la T54 (AB#54) a partir del contrato del frontend (`frontend/docs/hu20-ficha-usuario.md`). Pendientes de confirmar con el equipo y de añadir a la HU20 en Azure Boards.
- **Backend (T52, AB#52, PR #76):** `GET /api/admin/usuarios/{id}` con `UsuarioDetalleResponse`. La imagen sale como `avatar` (en el dominio, `imagenPerfil`) y cada ficha trae solo los campos de su rol.
- **HU20-CP05 (AB#43):** caso propuesto en la T43. El contrato del frontend dice que la fecha de incorporación del administrador es su fecha de alta, automática y no editable (`Contexto_frontend.md`). La HU21 (PR #94) no añadió ningún campo nuevo, así que la ficha devuelve `fechaIncorporacion` con el mismo instante que `fechaAlta` (`createdAt` en MongoDB). Como el rol de una cuenta no cambia nunca (decisión del equipo, 11/10/2026), la fecha de alta de un administrador es siempre la de su incorporación. Pendiente de confirmar con el equipo y de añadir a Azure Boards. La `sede` del administrador sigue sin existir en el modelo: el equipo decide dejarla pendiente en este sprint.
- **HU20-CP06 (AB#43):** caso propuesto en la edición del vendedor. Con el registro de vendedores (T241) el vendedor tiene `DatosVendedor`, y la ficha los devuelve (el DNI y el teléfono salen del perfil del cliente o del vendedor). Pendiente de confirmar con el equipo y de añadir a Azure Boards.
- **Frontend:** los tests de la HU20 (AB#211) no tienen todavía filas en la matriz.
- **HU20-CP07 (R8, seguridad en la capa de negocio):** caso propuesto en la rama `fix/seguridad-capa-negocio`, sin task ni `AB#` por ser un fix transversal. Pendiente de confirmar con el equipo. Cubre con `@PreAuthorize` los servicios `AdministracionUsuariosService`, `ListadoUsuariosService` y `CrearAdministradorService`, que sirven también a las HU19, HU21, HU22, HU23, HU24, HU26 y HU37; se traza aquí por ser la ficha de usuario (HU20) la operación base del puerto. Clase de test nueva `usuarios/application/service/SeguridadAdministracionUsuarioServiceTest` (contexto mínimo de Spring con `@EnableMethodSecurity`, sin MongoDB).

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
| HU20-CP05 | `UsuarioControllerTest` | `fichaDeUnAdministradorConSuFechaDeIncorporacion` | Infraestructura | AB#43 | PR #96 | ✅ |
| HU20-CP05 | `UsuarioControllerTest` | `fichaDeUnClienteSinFechaDeIncorporacion` | Infraestructura | AB#43 | PR #96 | ✅ |
| HU20-CP06 | `UsuarioControllerTest` | `fichaDeUnVendedorConSusDatosDeVendedor` | Infraestructura | AB#43 | PR #100 | ✅ |
| HU20-CP07 | `SeguridadAdministracionUsuarioServiceTest` | `unClienteNoAdministraUsuarios` | Aplicación | — | Rama `fix/seguridad-capa-negocio` | 🔍 |
| HU20-CP07 | `SeguridadAdministracionUsuarioServiceTest` | `unVendedorNoAdministraUsuarios` | Aplicación | — | Rama `fix/seguridad-capa-negocio` | 🔍 |
| HU20-CP07 | `SeguridadAdministracionUsuarioServiceTest` | `unAdministradorNoOperaConElCorreoDeOtro` | Aplicación | — | Rama `fix/seguridad-capa-negocio` | 🔍 |
| HU20-CP07 | `SeguridadAdministracionUsuarioServiceTest` | `unAdministradorUsaLasOperaciones` | Aplicación | — | Rama `fix/seguridad-capa-negocio` | 🔍 |
| HU20-CP07 | `SeguridadAdministracionUsuarioServiceTest` | `unClienteConsultaElNombreComercial` | Aplicación | — | Rama `fix/seguridad-capa-negocio` | 🔍 |
| HU20-CP07 | `SeguridadAdministracionUsuarioServiceTest` | `unAdministradorNoConsultaElNombreComercial` | Aplicación | — | Rama `fix/seguridad-capa-negocio` | 🔍 |
| HU20-CP07 | `SeguridadAdministracionUsuarioServiceTest` | `sinSesionSeRechazaTodo` | Aplicación | — | Rama `fix/seguridad-capa-negocio` | 🔍 |

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
| HU22-CP03 | El administrador edita los datos de un vendedor (nombre, apellidos, imagen, nombre comercial, DNI, teléfono y categoría principal) y recibe su ficha | Unitario + integración (capa web) | 3 | ✅ Pasa |
| HU22-CP04 | El email, la contraseña, el estado y el rol no cambian aunque lleguen en el cuerpo (sin rol, con otro rol o con uno inexistente se edita igual) | Unitario + integración (capa web) | 3 | ✅ Pasa |
| HU22-CP05 | Rechaza datos no válidos (400): nombre o apellidos no válidos, DNI mal formado, cliente menor de edad o vendedor sin sus datos de vendedor; no guarda nada | Unitario + integración (capa web) | 8 | ✅ Pasa |
| HU22-CP06 | Rechaza un DNI que ya es de otra cuenta del mismo rol (409 `DNI_ALREADY_REGISTERED`) o el nombre comercial de otro vendedor (409 `COMMERCIAL_NAME_ALREADY_REGISTERED`); conservar los propios no cuenta | Unitario + integración | 8 | ✅ Pasa |
| HU22-CP07 | Editar un usuario que no existe devuelve 404 | Unitario + integración (capa web) | 2 | ✅ Pasa |
| HU22-CP08 | Solo un administrador con sesión y token CSRF puede editar usuarios (401 / 403) | Integración (capa web) | 3 | ✅ Pasa |
| HU22-CP15 | `GET /api/categorias` devuelve las categorías (id y nombre) ordenadas por nombre, también sin sesión | Unitario + integración | 5 | ✅ Pasa |
| HU22-CP16 | Al editar, un DNI ya registrado (y cualquier dato rechazado con campo) se marca en su campo | Frontend (componente) | 1 | ✅ Pasa (1, AB#45, PR #105) |

Notas:
- **HU22-CP01 a CP08:** casos propuestos en la T42/T44 (AB#42, AB#44) a partir del contrato del frontend (`frontend/docs/hu22-editar-usuario.md`). Pendientes de confirmar con el equipo y de añadir a la HU22 en Azure Boards. Se desarrollaron con TDD (ciclos rojo, verde y refactor); los de CP03, CP04, CP06 (persistencia) y CP08 son tests de protección sobre código que ya existía y pasaron a la primera.
- **Backend (T42):** `PUT /api/admin/usuarios/{id}` con `EditarUsuarioRequest`, en `AdministracionUsuariosUseCase.editar`. Devuelve la ficha de la HU20 (`UsuarioDetalleResponse`).
- **Decisiones del equipo (10/10/2026):** el email no se edita en ningún rol (también en el vendedor, PR #81), así que el 409 `EMAIL_ALREADY_REGISTERED` ya no forma parte de la HU22. El teléfono es opcional: en blanco (`""`) deja al cliente sin teléfono.
- **El rol no se puede cambiar (decisión del equipo, 11/10/2026: "el que nace cliente muere cliente"):** el PR #92 (T43) añadió el cambio de rol con sus reglas (propio rol, último administrador, cuenta pendiente que pasa a administrador, cierre de la sesión) y sus casos HU22-CP09 a CP14. Se mergeó, y después el equipo decidió que el rol no cambia nunca. La T43 (rama `fix/US22-T43-rol-no-editable`) **revierte el PR #92** (con él salen CP09 a CP14 y sus tests) y hace que la edición **ignore el `rol` del cuerpo**, como el email: `EditarUsuarioRequest` y `EditarUsuarioCommand` ya no lo llevan y los datos se validan según el rol de la cuenta. Por eso CP04 gana `editarIgnoraElRolDelCuerpo` y CP05 pierde `rechazaEditarConUnRolDistintoDelActual`, `rechazaEditarSinRol` y las variantes "sin rol" y "rol inexistente" de `rechazaUnCuerpoDeEdicionNoValido` (antes eran un 400).
- **Frontend (T43):** el rol es de solo lectura en el formulario, no viaja al guardar y las categorías solo se cargan al editar un vendedor (ver `frontend/docs/hu22-editar-usuario.md`).
- **Edición del vendedor (AB#43, PR #100):** al mergear el registro de vendedores (T241, PR #98), editar un vendedor **borraba sus datos de vendedor**: la edición solo construía datos de cliente y `Usuario.modificarDatos` dejaba el perfil del vendedor a `null`. Ahora la edición recibe `nombreComercial` y `categoriaPrincipalId` (el DNI y el teléfono son los mismos campos que en el cliente) y `modificarDatos` exige `DatosVendedor` a un vendedor (CP05). Un vendedor anterior a la T241, sin datos de vendedor, tiene que rellenarlos la primera vez que se edita. El DNI se comprueba dentro de su rol (`existePorDniYRol`, como el índice `rol_dni`) y el nombre comercial solo si cambia (CP06). Por eso CP03 pasa a usar un vendedor con datos de vendedor (también `editarNoCambiaEmailContrasenaEstadoNiRol` de CP04) y los dos tests de servicio de CP06 del cliente cambian de puerto.
- **HU22-CP15 (AB#43):** caso propuesto. El formulario del vendedor elige su categoría principal entre las de `GET /api/categorias`, que no existía en el backend. Se crea el módulo `categorias` (decisión del equipo, 11/10/2026), solo con la consulta. El endpoint es público porque el registro del vendedor lo usa sin sesión. Pendiente de confirmar con el equipo y de añadir a Azure Boards.
- **Pendiente:** la sede del administrador (el equipo la deja pendiente en este sprint; la HU21, PR #94, la acepta en el alta pero no la guarda).
- **Frontend:** los tests de la HU22 (AB#46, y los que cambia la T43) no tienen todavía filas en la matriz.
- **Qué no se puede usar todavía contra el backend real** (el backend de la HU22 funciona; fallan piezas de otras historias):
  - **Editar un vendedor:** el backend ya da las categorías, pero hay que comprobar que la colección `categorias` de Atlas tiene datos; si está vacía, el formulario no tiene categorías que elegir.
  - **Iniciar sesión:** Brevo rechaza la clave SMTP de `application.properties`, así que el código por correo no se envía. El equipo tiene que generar una clave nueva y pasarla por la variable de entorno `SPRING_MAIL_PASSWORD`.

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
| HU22-CP03 | `UsuarioTest` | `modificarDatosCambiaLosDatosDeUnVendedor` | Dominio | AB#43 | PR #100 | ✅ |
| HU22-CP03 | `AdministracionUsuariosServiceTest` | `editaUnVendedorYLoGuarda` | Aplicación | AB#44, AB#43 | PR #82, PR #100 | ✅ |
| HU22-CP03 | `UsuarioControllerTest` | `administradorEditaUnVendedorYRecibeSuFicha` | Infraestructura | AB#43 | PR #100 | ✅ |
| HU22-CP04 | `AdministracionUsuariosServiceTest` | `editarNoCambiaEmailContrasenaEstadoNiRol` | Aplicación | AB#44, AB#43 | PR #82, PR #100 | ✅ |
| HU22-CP04 | `UsuarioControllerTest` | `editarIgnoraEmailContrasenaYEstadoDelCuerpo` | Infraestructura | AB#44 | PR #82 | ✅ |
| HU22-CP04 | `UsuarioControllerTest` | `editarIgnoraElRolDelCuerpo` (sin rol, con otro rol y con uno inexistente) | Infraestructura | AB#43 | PR #96 | ✅ |
| HU22-CP05 | `UsuarioTest` | `modificarDatosRechazaUnClienteMenorDeEdad` | Dominio | AB#44 | PR #82 | ✅ |
| HU22-CP05 | `UsuarioTest` | `modificarDatosAceptaUnClienteQueCumple18Hoy` | Dominio | AB#44 | PR #82 | ✅ |
| HU22-CP05 | `UsuarioTest` | `modificarDatosExigeDatosDeClienteCoherentesConElRol` | Dominio | AB#44 | PR #82 | ✅ |
| HU22-CP05 | `UsuarioTest` | `modificarDatosNoQuitaLosDatosDeVendedorAUnVendedor` | Dominio | AB#43 | PR #100 | ✅ |
| HU22-CP05 | `AdministracionUsuariosServiceTest` | `rechazaEditarUnClienteConDatosNoValidos` | Aplicación | AB#44 | PR #82 | ✅ |
| HU22-CP05 | `AdministracionUsuariosServiceTest` | `rechazaEditarUnVendedorSinSusDatosDeVendedor` | Aplicación | AB#43 | PR #100 | ✅ |
| HU22-CP05 | `UsuarioControllerTest` | `rechazaUnCuerpoDeEdicionNoValido` (3 variantes: nombre vacío, nombre de 31 caracteres y apellidos vacíos; T43 quita "sin rol" y "rol inexistente") | Infraestructura | AB#44, AB#43 | PR #82, PR #96 | ✅ |
| HU22-CP05 | `UsuarioControllerTest` | `editarConDatosQueElDominioRechazaDevuelve400` | Infraestructura | AB#44 | PR #82 | ✅ |
| HU22-CP06 | `AdministracionUsuariosServiceTest` | `rechazaEditarUnClienteConElDniDeOtraCuenta` | Aplicación | AB#44, AB#43 | PR #82, PR #100 | ✅ |
| HU22-CP06 | `AdministracionUsuariosServiceTest` | `editarUnClienteQueConservaSuDniNoLoComprueba` | Aplicación | AB#44, AB#43 | PR #82, PR #100 | ✅ |
| HU22-CP06 | `AdministracionUsuariosServiceTest` | `rechazaEditarUnVendedorConElDniDeOtroVendedor` | Aplicación | AB#43 | PR #100 | ✅ |
| HU22-CP06 | `AdministracionUsuariosServiceTest` | `rechazaEditarUnVendedorConElNombreComercialDeOtro` | Aplicación | AB#43 | PR #100 | ✅ |
| HU22-CP06 | `AdministracionUsuariosServiceTest` | `editarUnVendedorQueConservaSuDniYSuNombreComercialNoLosComprueba` | Aplicación | AB#43 | PR #100 | ✅ |
| HU22-CP06 | `UsuarioControllerTest` | `editarConElDniDeOtraCuentaDevuelve409` | Infraestructura | AB#44 | PR #82 | ✅ |
| HU22-CP06 | `UsuarioControllerTest` | `editarConElNombreComercialDeOtroDevuelve409` | Infraestructura | AB#43 | PR #100 | ✅ |
| HU22-CP06 | `UsuarioMongoAdapterTest` | `rechazaEditarUnClienteConElDniDeOtroEnLaBaseDeDatos` | Infraestructura | AB#44 | PR #82 | ✅ |
| HU22-CP07 | `AdministracionUsuariosServiceTest` | `rechazaEditarUnUsuarioQueNoExiste` | Aplicación | AB#44 | PR #82 | ✅ |
| HU22-CP07 | `UsuarioControllerTest` | `editarUnUsuarioInexistenteDevuelve404` | Infraestructura | AB#44 | PR #82 | ✅ |
| HU22-CP08 | `UsuarioControllerTest` | `unNoAdministradorNoPuedeEditarUsuarios` (cliente y vendedor) | Infraestructura | AB#44 | PR #82 | ✅ |
| HU22-CP08 | `UsuarioControllerTest` | `editarSinSesionEstaProtegido` | Infraestructura | AB#44 | PR #82 | ✅ |
| HU22-CP08 | `UsuarioControllerTest` | `editarSinTokenCsrfSeRechaza` | Infraestructura | AB#44 | PR #82 | ✅ |
| HU22-CP15 | `ConsultaCategoriasServiceTest` | `listarDevuelveLasCategoriasDelRepositorio` | Aplicación | AB#43 | PR #100 | ✅ |
| HU22-CP15 | `CategoriaMongoAdapterTest` | `listarDevuelveLasCategoriasOrdenadasPorNombre` | Infraestructura | AB#43 | PR #100 | ✅ |
| HU22-CP15 | `CategoriaMongoAdapterTest` | `sinCategoriasDevuelveUnaListaVacia` | Infraestructura | AB#43 | PR #100 | ✅ |
| HU22-CP15 | `CategoriaControllerTest` | `sinSesionSeRecibenLasCategorias` | Infraestructura | AB#43 | PR #100 | ✅ |
| HU22-CP15 | `CategoriaControllerTest` | `unAdministradorRecibeLasCategorias` | Infraestructura | AB#43 | PR #100 | ✅ |
| HU22-CP16 | `ProfileEditor.test.jsx` | `HU22-CP16: un DNI ya registrado se marca en el DNI` | Frontend · componente | AB#45 | PR #105 | ✅ |

Rutas de las clases de test (bajo `backend/src/test/java/es/uclm/esibuy/backend/`):
- `UsuarioTest`: `usuarios/domain/model/` (JUnit, sin Spring)
- `AdministracionUsuariosServiceTest`: `usuarios/application/service/` (Mockito, sin Spring)
- `UsuarioControllerTest`: `usuarios/infrastructure/adapter/in/web/` (con `@WebMvcTest` y usuarios simulados)
- `UsuarioMongoAdapterTest`: `usuarios/infrastructure/adapter/out/persistence/` (con Testcontainers)
- `ConsultaCategoriasServiceTest`: `categorias/application/service/` (Mockito, sin Spring)
- `CategoriaMongoAdapterTest`: `categorias/infrastructure/adapter/out/persistence/` (con Testcontainers)
- `CategoriaControllerTest`: `categorias/infrastructure/adapter/in/web/` (con `@WebMvcTest`)

---

## HU23 · Bloquear usuario

### Tabla A · HU23 ↔ casos de prueba

| Caso | Descripción | Tipo | Tests | Estado |
|---|---|---|---|---|
| HU23-CP01 | Un administrador bloquea una cuenta activa y recibe sus datos con estado BLOQUEADO | Unitario + integración (capa web) | 2 | ✅ Pasa |
| HU23-CP02 | Bloquear un usuario que no existe devuelve 404 y no guarda nada | Unitario + integración (capa web) | 2 | ✅ Pasa |
| HU23-CP03 | Un administrador no puede bloquear su propia cuenta (409 `CANNOT_BLOCK_SELF`) | Unitario + integración (capa web) | 2 | ✅ Pasa |
| HU23-CP04 | No se puede bloquear al último administrador activo (409 `LAST_ACTIVE_ADMIN`) | Unitario + integración | 4 | ✅ Pasa |
| HU23-CP05 | Se puede desactivar a un administrador activo si queda otro administrador activo | Unitario | 1 | ✅ Pasa |
| HU23-CP06 | Sin sesión (401) o sin ser administrador (403) no se puede bloquear una cuenta | Integración (capa web) | 2 | ✅ Pasa |
| HU23-CP07 | Una petición sin usuario en sesión pasa sin consultar la cuenta | Unitario | 1 | ✅ Pasa |
| HU23-CP08 | Una petición de un usuario con la cuenta activa pasa y conserva su sesión | Unitario | 1 | ✅ Pasa |
| HU23-CP09 | Si la cuenta del usuario en sesión está bloqueada, se invalida su sesión y responde 401 | Unitario | 1 | ✅ Pasa |
| HU23-CP10 | Si el usuario en sesión ya no existe, se invalida su sesión y responde 401 | Unitario | 1 | ✅ Pasa |
| HU23-CP11 | Una cuenta con bloqueo temporal por intentos sale con su estado guardado en el listado, la ficha y los vendedores | Integración (capa web) | 3 | ✅ Pasa |
| HU23-CP12 | DESACTIVADO ya no es un estado válido para filtrar el listado | Unitario | 1 | ✅ Pasa |

Notas:
- **HU23-CP01 a CP06:** casos propuestos en la T39 (AB#39) a partir del contrato del frontend (`frontend/docs/hu23-desactivar-usuario.md`). Pendientes de confirmar con el equipo y de añadir a la HU23 en Azure Boards.
- **Backend (T37/T38, AB#37 y AB#38):** el mismo `PATCH /api/admin/usuarios/{id}/status` de la HU24, ahora con `{ "estado": "DESACTIVADO" }`, en `AdministracionUsuariosUseCase.desactivar`. El controlador pasa el email del usuario en sesión (`Authentication.getName()`, como `/api/auth/me`) para la regla de no desactivarse a sí mismo. El conteo de administradores activos es `UsuarioRepositoryPort.contarAdministradoresActivos()`. Los 409 llevan los códigos que espera el frontend.
- **BLOQUEADO sustituye a DESACTIVADO (T243, AB#243):** decisión del equipo. Los estados guardados son `PENDIENTE`, `ACTIVO` y `BLOQUEADO`, y `PATCH /status` (usuarios y vendedores) recibe `BLOQUEADO`. El bloqueo temporal por intentos fallidos deja de mostrarse como `BLOQUEADO`: las respuestas devuelven el estado guardado y el filtro `status=BLOQUEADO` solo devuelve cuentas bloqueadas por un administrador (HU19-CP10); en el login sigue siendo 429. Los nombres `desactivar`, sus excepciones y el 409 `CANNOT_DEACTIVATE_SELF` los cambia la T252. **PENDIENTE DE CONFIRMAR CON EL EQUIPO:** casos HU23-CP11 y CP12, propuestos.
- **Bloquear en vez de desactivar (T252, AB#252):** completa la T243 con los nombres. `Usuario.bloquear()`, `bloquear` y `bloquearVendedor` del caso de uso; la excepción del bloqueo de un administrador pasa a `CuentaBloqueadaException` (403 `ACCOUNT_BLOCKED`), la del bloqueo temporal por intentos a `BloqueoPorIntentosException` (429) y la de bloquearse a sí mismo a `AutobloqueoNoPermitidoException`. El 409 pasa a `CANNOT_BLOCK_SELF`, el código que ya espera el frontend, y los mensajes dicen «bloquear». Los tests de la tabla con «renombrado en este PR» solo cambian de nombre y de `@DisplayName`, salvo HU23-CP03, que espera el código nuevo.
- **Invalidar la sesión (T206, AB#206):** `CuentaActivaFilter` comprueba en cada petición con un usuario en sesión que su cuenta sigue activa; si no (o si ya no existe), invalida la sesión y responde 401, como espera el frontend. Se registra como bean en `UsuariosConfig` y Spring Boot lo añade a los filtros de cada petición, sin tocar `SecurityConfig`. El login (HU2) guarda al usuario en sesión con su email como nombre, que es lo que usan el filtro y la regla de HU23-CP03. Casos HU23-CP07 a CP10 propuestos en la T206, pendientes de confirmar con el equipo.
- **Frontend:** los tests de la HU23 (AB#41) no tienen todavía filas en la matriz.

### Tabla B · casos de prueba ↔ tests

| Caso | Clase de test | Método | Capa | Task | Origen | Estado |
|---|---|---|---|---|---|---|
| HU23-CP01 | `AdministracionUsuariosServiceTest` | `bloqueaUnaCuentaActivaYLaGuarda` | Aplicación | AB#39 · AB#243 · AB#252 | PR #79 (modificado en PR #180) (renombrado en PR #185) | ✅ |
| HU23-CP01 | `UsuarioControllerTest` | `administradorBloqueaUnaCuentaYRecibeSusDatos` | Infraestructura | AB#39 · AB#243 · AB#252 | PR #79 (modificado en PR #180) (renombrado en PR #185) | ✅ |
| HU23-CP02 | `AdministracionUsuariosServiceTest` | `rechazaBloquearUnUsuarioQueNoExiste` | Aplicación | AB#39 · AB#252 | PR #79 (renombrado en PR #185) | ✅ |
| HU23-CP02 | `UsuarioControllerTest` | `bloquearUnUsuarioInexistenteDevuelve404` | Infraestructura | AB#39 · AB#252 | PR #79 (renombrado en PR #185) | ✅ |
| HU23-CP03 | `AdministracionUsuariosServiceTest` | `rechazaQueUnAdministradorSeBloqueeASiMismo` | Aplicación | AB#39 · AB#252 | PR #79 (renombrado en PR #185) | ✅ |
| HU23-CP03 | `UsuarioControllerTest` | `bloquearseASiMismoDevuelve409` | Infraestructura | AB#39 · AB#252 | PR #79 (renombrado en PR #185) | ✅ |
| HU23-CP04 | `AdministracionUsuariosServiceTest` | `rechazaBloquearAlUltimoAdministradorActivo` | Aplicación | AB#39 · AB#252 | PR #79 (renombrado en PR #185) | ✅ |
| HU23-CP04 | `UsuarioControllerTest` | `bloquearAlUltimoAdministradorActivoDevuelve409` | Infraestructura | AB#39 · AB#252 | PR #79 (renombrado en PR #185) | ✅ |
| HU23-CP04 | `UsuarioMongoAdapterTest` | `contarAdministradoresActivosSoloCuentaLosAdministradoresActivos` | Infraestructura | AB#39 | PR #79 | ✅ |
| HU23-CP04 | `UsuarioMongoAdapterTest` | `contarAdministradoresActivosDevuelveCeroSinAdministradoresActivos` | Infraestructura | AB#39 | PR #79 | ✅ |
| HU23-CP05 | `AdministracionUsuariosServiceTest` | `bloqueaAUnAdministradorSiQuedaOtroActivo` | Aplicación | AB#39 · AB#243 · AB#252 | PR #79 (modificado en PR #180) (renombrado en PR #185) | ✅ |
| HU23-CP06 | `UsuarioControllerTest` | `unNoAdministradorNoPuedeBloquearCuentas` (cliente y vendedor) | Infraestructura | AB#39 · AB#252 | PR #79 (renombrado en PR #185) | ✅ |
| HU23-CP06 | `UsuarioControllerTest` | `bloquearSinSesionEstaProtegido` | Infraestructura | AB#39 · AB#252 | PR #79 (renombrado en PR #185) | ✅ |
| HU23-CP07 | `CuentaActivaFilterTest` | `sinUsuarioEnSesionDejaPasar` | Infraestructura | AB#39 | PR #85 | ✅ |
| HU23-CP08 | `CuentaActivaFilterTest` | `cuentaActivaDejaPasar` | Infraestructura | AB#39 | PR #85 | ✅ |
| HU23-CP09 | `CuentaActivaFilterTest` | `cuentaBloqueadaInvalidaLaSesionYDevuelve401` | Infraestructura | AB#39 · AB#252 | PR #85 (renombrado en PR #185) | ✅ |
| HU23-CP10 | `CuentaActivaFilterTest` | `usuarioInexistenteInvalidaLaSesionYDevuelve401` | Infraestructura | AB#39 | PR #85 | ✅ |
| HU23-CP11 | `UsuarioControllerTest` | `elListadoNoMuestraElBloqueoPorIntentosComoBloqueado` | Infraestructura | AB#243 | PR #180 | ✅ |
| HU23-CP11 | `UsuarioControllerTest` | `laFichaNoMuestraElBloqueoPorIntentosComoBloqueado` | Infraestructura | AB#243 | PR #180 | ✅ |
| HU23-CP11 | `UsuarioControllerTest` | `laFichaDeVendedorNoMuestraElBloqueoPorIntentosComoBloqueado` | Infraestructura | AB#243 | PR #180 | ✅ |
| HU23-CP12 | `ListadoUsuariosServiceTest` | `laConsultaRechazaElEstadoDesactivado` | Aplicación | AB#243 | PR #180 | ✅ |

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
| HU24-CP01 | Un administrador activa una cuenta bloqueada y recibe sus datos con estado ACTIVO | Unitario + integración | 3 | ✅ Pasa |
| HU24-CP02 | Rechaza un estado que no es ACTIVO ni BLOQUEADO (400) y no cambia nada | Integración (capa web) | 1 | ✅ Pasa |
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
| HU24-CP01 | `AdministracionUsuariosServiceTest` | `activaUnaCuentaBloqueadaYLaGuarda` | Aplicación | AB#34 · AB#252 | PR #63 (renombrado en PR #185) | ✅ |
| HU24-CP01 | `UsuarioControllerTest` | `administradorActivaUnaCuentaYRecibeSusDatos` | Infraestructura | AB#34 | PR #63 | ✅ |
| HU24-CP01 | `UsuarioMongoAdapterTest` | `buscarPorIdDevuelveElUsuarioGuardado` | Infraestructura | AB#34 | PR #63 | ✅ |
| HU24-CP02 | `UsuarioControllerTest` | `rechazaUnEstadoNoAdmitido` | Infraestructura | AB#34 · AB#39 · AB#243 | PR #63 (modificado en PR #79) (modificado en PR #180) | ✅ |
| HU24-CP03 | `AdministracionUsuariosServiceTest` | `rechazaActivarUnUsuarioQueNoExiste` | Aplicación | AB#34 | PR #63 | ✅ |
| HU24-CP03 | `UsuarioControllerTest` | `activarUnUsuarioInexistenteDevuelve404` | Infraestructura | AB#34 | PR #63 | ✅ |
| HU24-CP03 | `UsuarioMongoAdapterTest` | `buscarPorIdDevuelveVacioSiNoExiste` | Infraestructura | AB#34 | PR #63 | ✅ |
| HU24-CP04 | `UsuarioControllerTest` | `unNoAdministradorNoPuedeActivarCuentas` (cliente y vendedor) | Infraestructura | AB#34 · AB#60 | PR #63 (ampliado en PR #64) | ✅ |

Rutas de las clases de test (bajo `backend/src/test/java/es/uclm/esibuy/backend/`):
- `AdministracionUsuariosServiceTest`: `usuarios/application/service/` (Mockito, sin Spring)
- `UsuarioControllerTest`: `usuarios/infrastructure/adapter/in/web/` (con `@WebMvcTest` y `@WithMockUser`)
- `UsuarioMongoAdapterTest`: `usuarios/infrastructure/adapter/out/persistence/` (con Testcontainers)

---

## HU25 · Eliminar mi cuenta de cliente

### Tabla A · HU25 ↔ casos de prueba

| Caso | Descripción | Tipo | Tests | Estado |
|---|---|---|---|---|
| HU25-CP01 | Con la contraseña actual correcta se elimina la cuenta del cliente de la sesión | Unitario | 1 | 🔍 En revisión |
| HU25-CP02 | Con una contraseña incorrecta no se elimina la cuenta | Unitario | 1 | 🔍 En revisión |
| HU25-CP03 | Al eliminar la cuenta se responde 204, se invalida la sesión y se borra la cookie `JSESSIONID` | Integración (capa web) | 0 | ⏳ Pendiente (AB#274) |
| HU25-CP04 | Solo el cliente elimina su propia cuenta: el correo de otro, un vendedor o un administrador dan 403 y sin sesión se rechaza. Seguridad en la capa de negocio (R8) | Unitario con Spring Security (sin MongoDB) | 5 | 🔍 En revisión |
| HU25-CP05 | Sin contraseña (vacía o ausente) se responde 400 y no se llama al caso de uso | Integración (capa web) | 0 | ⏳ Pendiente (AB#274) |

Notas:
- **HU25-CP01 a CP05:** casos propuestos en las T274/T275 (AB#274, AB#275) a partir de los criterios de aceptación de la HU25. Pendientes de confirmar con el equipo y de añadir a la HU25 en Azure Boards.
- **Lógica (T275, AB#275):** `GestionPerfilUseCase.eliminarCuenta(email, password)` con `@PreAuthorize` de `Permisos.SOLO_CLIENTE_PROPIO_POR_EMAIL`: comprueba la contraseña actual (si no coincide, `ContrasenaActualIncorrectaException`, sin borrar nada) y borra al usuario. El borrado es **físico**, como el de un administrador (HU37): solo se borra el documento del usuario. Tras borrarlo, el login con esa cuenta da 401 (correo inexistente) y `CuentaActivaFilter` invalida cualquier sesión abierta en su siguiente petición.
- **Endpoint (T274, AB#274):** `DELETE /api/perfil` va en su propia rama y PR, con los tests del controlador de CP02 a CP05 (400 `WRONG_PASSWORD`, cierre de sesión, 401, CSRF y validación).
- **Pendiente con el equipo:** el carrito, las direcciones, los métodos de pago (cifrados), los pedidos y las valoraciones del cliente quedan con un `clienteId` huérfano. Se decide junto con la HU38 (borrado lógico o anonimización).
- **Tests existentes:** `GestionPerfilServiceTest` saca el usuario de prueba a un método (`lucia()`) que comparten HU4-CP01 y los casos nuevos, y `SeguridadSesionUsuarioServiceTest` crea el servicio con el doble del hash. No cambia lo que comprueban HU4-CP01 ni HU2-CP29.
- **Frontend:** las tasks AB#272 y AB#273 están sin empezar.

### Tabla B · casos de prueba ↔ tests

| Caso | Clase de test | Método | Capa | Task | Origen | Estado |
|---|---|---|---|---|---|---|
| HU25-CP01 | `GestionPerfilServiceTest` | `eliminarCuentaConLaContrasenaCorrectaBorraAlUsuario` | Aplicación | AB#275 | este PR | 🔍 |
| HU25-CP02 | `GestionPerfilServiceTest` | `eliminarCuentaConLaContrasenaIncorrectaNoBorraNada` | Aplicación | AB#275 | este PR | 🔍 |
| HU25-CP04 | `SeguridadSesionUsuarioServiceTest` | `unClienteEliminaSuCuenta` | Aplicación | AB#275 | este PR | 🔍 |
| HU25-CP04 | `SeguridadSesionUsuarioServiceTest` | `unClienteNoEliminaLaCuentaDeOtro` | Aplicación | AB#275 | este PR | 🔍 |
| HU25-CP04 | `SeguridadSesionUsuarioServiceTest` | `unVendedorNoEliminaSuCuenta` | Aplicación | AB#275 | este PR | 🔍 |
| HU25-CP04 | `SeguridadSesionUsuarioServiceTest` | `unAdministradorNoEliminaSuCuenta` | Aplicación | AB#275 | este PR | 🔍 |
| HU25-CP04 | `SeguridadSesionUsuarioServiceTest` | `sinSesionNoSeEliminaNingunaCuenta` | Aplicación | AB#275 | este PR | 🔍 |

Rutas de las clases de test (bajo `backend/src/test/java/es/uclm/esibuy/backend/`):
- `GestionPerfilServiceTest` y `SeguridadSesionUsuarioServiceTest`: `usuarios/application/service/` (Mockito sin Spring, y contexto mínimo con `@EnableMethodSecurity` y `@WithMockUser`)

---

## HU26 · Supervisión de vendedores (solo lectura)

### Tabla A · HU26 ↔ casos de prueba

| Caso | Descripción | Tipo | Tests | Estado |
|---|---|---|---|---|
| HU26-CP01 | El listado solo trae vendedores, con sus datos (también el nombre comercial y la categoría principal) y sin datos sensibles | Integración (capa web) | 1 | ✅ Pasa |
| HU26-CP02 | El listado se ordena por nombre o por estado (pendientes, activos, bloqueados), en sentido ascendente o descendente | Integración (capa web y MongoDB) | 4 | ✅ Pasa |
| HU26-CP03 | La ficha de un vendedor trae sus datos (también el nombre comercial, la categoría principal con su nombre, el DNI y el teléfono), sin datos sensibles | Unitario + integración (capa web) | 2 | ✅ Pasa |
| HU26-CP04 | La ficha de un usuario que no existe o que no es vendedor devuelve 404 | Unitario + integración (capa web) | 3 | ✅ Pasa |
| HU26-CP05 | Sin sesión (401) o sin ser administrador (403) no se ve ni el listado ni la ficha | Integración (capa web) | 2 | ✅ Pasa |
| HU26-CP06 | Filtra los vendedores por su categoría principal | Integración (capa web y MongoDB) | 2 | ✅ Pasa |
| HU26-CP07 | Ordena los vendedores por el nombre de su categoría, en los dos sentidos | Integración (capa web y MongoDB) | 1 | ✅ Pasa |

Notas:
- **HU26-CP01 a CP05:** casos propuestos en la T30 (AB#30) a partir de los criterios de aceptación de la HU26 y del contrato del frontend (`frontend/docs/hu26-supervision-vendedores.md`). Pendientes de confirmar con el equipo y de añadir a la HU26 en Azure Boards.
- **Backend (T29, AB#29, PR #78):** `GET /api/admin/sellers` (el listado de la HU19 con el rol `VENDEDOR` fijo y ordenación por nombre o estado) y `GET /api/admin/sellers/{id}` (`consultarVendedor`).
- **Datos del vendedor (AB#43):** con el registro de vendedores (T241), el listado y la ficha devuelven `nombreComercial`, `categoriaPrincipalId`, `categoriaPrincipal` (el nombre, que da el módulo `categorias` por su puerto de entrada), `dni` y `telefono`. El contrato del frontend decía `nif`; el equipo decidió usar `dni`, como el registro (11/10/2026).
- **Sin tests todavía, porque no existe el código:** los pedidos del vendedor (`/sellers/{id}/orders`).
- **HU26-CP06 y CP07 (fallos de la demo del Sprint 1):** `category` filtra por `categoriaPrincipalId` y `sort=category` ordena por el **nombre** de la categoría: el adaptador lo obtiene del módulo `categorias` por su puerto de entrada (`ConsultaCategoriasUseCase`) y ordena con un `$switch`, como el rango del estado. Todos los listados ordenan con la *collation* `es` (HU19-CP12). `ListarUsuariosQuery` agrupa campo y sentido en `Ordenacion` para no pasar de 7 componentes.
- **Frontend:** los tests de la HU26 no tienen todavía filas en la matriz.

### Tabla B · casos de prueba ↔ tests

| Caso | Clase de test | Método | Capa | Task | Origen | Estado |
|---|---|---|---|---|---|---|
| HU26-CP01 | `UsuarioControllerTest` | `administradorRecibeElListadoDeVendedores` | Infraestructura | AB#30, AB#43 | PR #80, PR #100 | ✅ |
| HU26-CP02 | `UsuarioControllerTest` | `listadoDeVendedoresTraduceLaOrdenacion` (nombre, estado, categoría y sin orden) | Infraestructura | AB#30 · AB#29 | PR #80 (modificado en PR #104: `category` pasa a `CATEGORIA`) | ✅ |
| HU26-CP02 | `UsuarioMongoAdapterTest` | `listarOrdenaPorEstadoAscendente` | Infraestructura | AB#30 · AB#243 | PR #80 (modificado en PR #180) | ✅ |
| HU26-CP02 | `UsuarioMongoAdapterTest` | `listarOrdenaPorEstadoDescendente` | Infraestructura | AB#30 · AB#243 | PR #80 (modificado en PR #180) | ✅ |
| HU26-CP02 | `UsuarioMongoAdapterTest` | `listarOrdenaPorNombre` | Infraestructura | AB#30 | PR #80 | ✅ |
| HU26-CP03 | `AdministracionUsuariosServiceTest` | `consultarVendedorDevuelveElVendedor` | Aplicación | AB#30 | PR #80 | ✅ |
| HU26-CP03 | `UsuarioControllerTest` | `fichaDeUnVendedor` | Infraestructura | AB#30, AB#43 | PR #80, PR #100 | ✅ |
| HU26-CP04 | `AdministracionUsuariosServiceTest` | `consultarVendedorDeOtroRolFalla` | Aplicación | AB#30 | PR #80 | ✅ |
| HU26-CP04 | `AdministracionUsuariosServiceTest` | `consultarVendedorQueNoExisteFalla` | Aplicación | AB#30 | PR #80 | ✅ |
| HU26-CP04 | `UsuarioControllerTest` | `fichaDeUnNoVendedorDevuelve404` | Infraestructura | AB#30 | PR #80 | ✅ |
| HU26-CP05 | `UsuarioControllerTest` | `vendedoresSinSesionDevuelve401` (listado y ficha) | Infraestructura | AB#30 | PR #80 | ✅ |
| HU26-CP05 | `UsuarioControllerTest` | `noAdministradorNoVeLosVendedores` (cliente y vendedor, en el listado y la ficha) | Infraestructura | AB#30 | PR #80 | ✅ |
| HU26-CP06 | `UsuarioControllerTest` | `elListadoDeVendedoresFiltraPorCategoria` | Infraestructura | AB#29 | PR #104 | ✅ |
| HU26-CP06 | `UsuarioMongoAdapterTest` | `filtraVendedoresPorCategoria` | Infraestructura | AB#29 | PR #104 | ✅ |
| HU26-CP07 | `UsuarioMongoAdapterTest` | `ordenaVendedoresPorNombreDeCategoria` | Infraestructura | AB#29 | PR #104 | ✅ |

Rutas de las clases de test (bajo `backend/src/test/java/es/uclm/esibuy/backend/`):
- `AdministracionUsuariosServiceTest`: `usuarios/application/service/` (Mockito, sin Spring)
- `UsuarioControllerTest`: `usuarios/infrastructure/adapter/in/web/` (con `@WebMvcTest` y `@WithMockUser`)
- `UsuarioMongoAdapterTest`: `usuarios/infrastructure/adapter/out/persistence/` (con Testcontainers)

---

## HU30 · Desarrollo de una home

### Tabla A · HU30 ↔ casos de prueba

| Caso | Descripción | Tipo | Tests | Estado |
|---|---|---|---|---|
| HU30-CP01 | El logotipo de la cabecera lleva al inicio de cada usuario: con sesión, al de su rol (cliente a los productos, vendedor a sus productos y administrador a su panel); sin sesión, a la home | Frontend (página) | 5 | 🔍 En revisión (5, AB#27, PR #193) |

Notas:
- **HU30-CP01 (fix, AB#27):** caso propuesto al corregir que el logotipo llevaba siempre a la home de invitado aunque hubiera sesión. Usa `getHomePath(rol)` (`auth/roles.js`), el mismo inicio por rol que el login. Pendiente de confirmar con el equipo y de añadir a la HU30 en Azure Boards. Los tests de la home (`Home.test.jsx`) no llevan ID de caso y no están en esta matriz.

### Tabla B · casos de prueba ↔ tests

| Caso | Clase de test | Método | Capa | Task | Origen | Estado |
|---|---|---|---|---|---|---|
| HU30-CP01 | `AppHeader.test.jsx` | `HU30-CP01: con la sesión de cliente@esibuy.test, el logotipo lleva a /products` | Frontend · página | AB#27 | PR #193 | 🔍 |
| HU30-CP01 | `AppHeader.test.jsx` | `HU30-CP01: con la sesión de vendedor@esibuy.test, el logotipo lleva a /seller/products` | Frontend · página | AB#27 | PR #193 | 🔍 |
| HU30-CP01 | `AppHeader.test.jsx` | `HU30-CP01: con la sesión de admin@esibuy.test, el logotipo lleva a /admin` | Frontend · página | AB#27 | PR #193 | 🔍 |
| HU30-CP01 | `AppHeader.test.jsx` | `HU30-CP01: sin sesión, el logotipo lleva a la home` | Frontend · página | AB#27 | PR #193 | 🔍 |
| HU30-CP01 | `AppHeader.test.jsx` | `HU30-CP01: un cliente que pulsa el logotipo llega a los productos, no a la home de invitado` | Frontend · página | AB#27 | PR #193 | 🔍 |

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
| HU31-CP11 | Un producto se guarda en la colección `productos` con todos sus datos y se recupera igual | Unitario + integración MongoDB | 4 | ✅ Pasa |
| HU31-CP12 | Buscar un producto que no existe devuelve vacío | Integración MongoDB | 1 | ✅ Pasa |
| HU31-CP13 | El servicio crea el producto con el vendedor de la sesión y lo guarda | Unitario (servicio) | 1 | ✅ Pasa |
| HU31-CP14 | El servicio devuelve el producto guardado, con el id asignado por la base de datos | Unitario (servicio) | 1 | ✅ Pasa |
| HU31-CP15 | Unos datos que incumplen las reglas del producto se rechazan y no se guarda nada | Unitario (servicio) | 1 | ✅ Pasa |
| HU31-CP16 | Un vendedor no puede tener dos productos con el mismo nombre (sin distinguir mayúsculas) y no se guarda el segundo | Unitario (servicio) | 1 | ✅ Pasa |
| HU31-CP17 | `POST /api/seller/products` crea el producto y responde 201 con el producto creado, sin `vendedorId` | Integración (capa web) | 1 | ✅ Pasa |
| HU31-CP18 | El vendedor sale de la sesión: un `vendedorId` en el cuerpo se ignora | Integración (capa web) | 1 | ✅ Pasa |
| HU31-CP19 | Sin sesión (401), sin ser vendedor o sin token CSRF (403) no se crea el producto | Integración (capa web) | 3 | ✅ Pasa |
| HU31-CP20 | Un cuerpo con datos no válidos o que no es JSON devuelve 400 y no llega al caso de uso | Integración (capa web) | 2 | ✅ Pasa |
| HU31-CP21 | Una regla del dominio incumplida devuelve 400 con `code: VALIDATION_ERROR` y un mensaje genérico | Integración (capa web) | 1 | ✅ Pasa |
| HU31-CP22 | Un nombre repetido del mismo vendedor devuelve 409 con `code: PRODUCT_NAME_ALREADY_EXISTS` | Integración (capa web) | 1 | ✅ Pasa |
| HU31-CP23 | La comprobación de nombre repetido no distingue mayúsculas, compara el nombre completo como texto literal y solo cuenta los productos del mismo vendedor | Integración MongoDB | 2 | ✅ Pasa |
| HU31-CP24 | La base de datos rechaza un nombre repetido del mismo vendedor aunque se salte la comprobación previa (condición de carrera) | Integración MongoDB | 1 | ✅ Pasa |
| HU31-CP25 | El nombre se guarda sin espacios en los extremos, así que con espacios cuenta como repetido también para el índice único | Unitario + integración MongoDB | 3 | ✅ Pasa |
| HU31-CP26 | La categoría se elige en un desplegable con búsqueda con las categorías actuales: se puede escribir para filtrar sin distinguir mayúsculas ni tildes, y en los filtros la primera opción es «Todas las categorías» | Frontend (componente y página) | 11 | ✅ Pasa (11, AB#256, PR #123) |
| HU31-CP27 | Un nombre que el vendedor ya usa (409 `PRODUCT_NAME_ALREADY_EXISTS`) se marca en el campo Nombre con un mensaje propio al dar de alta, y el aviso desaparece al cambiar el nombre | Frontend (página) | 2 | ✅ Pasa (2, AB#257, PR #123) |
| HU31-CP28 | Los casos de uso de productos exigen su rol (vendedor, administrador o cliente según el caso de uso) y, los del vendedor, que el correo recibido sea el de la sesión; sin sesión se rechazan. Seguridad en la capa de negocio (R8) | Unitario con Spring Security (sin MongoDB) | 9 | 🔍 En revisión (9, rama `fix/seguridad-capa-negocio`) |
| HU31-CP29 | El antivirus (VirusTotal) da por maliciosa una URL ya analizada si algún motor la marca como `malicious`; los sospechosos no cuentan | Unitario (adaptador, servidor HTTP simulado) | 2 | 🔍 En revisión (2, AB#397, PR #189) |
| HU31-CP30 | Una URL que VirusTotal no conoce (404) se envía a analizar y se consulta el análisis como mucho 2 veces; si no termina a tiempo, no hay resultado | Unitario (adaptador, servidor HTTP simulado) | 3 | 🔍 En revisión (3, AB#397, PR #189) |
| HU31-CP31 | Un error de VirusTotal (401, 429, 500, 503), también al enviar la URL a analizar, o no poder conectar no da resultado en lugar de un error | Unitario (adaptador, servidor HTTP simulado) | 3 | 🔍 En revisión (3, AB#397, PR #189) |
| HU31-CP32 | Un producto con una imagen que el antivirus marca como maliciosa se rechaza al crearlo y no se guarda | Unitario | 1 | 🔍 En revisión (1, AB#397, PR #189) |
| HU31-CP33 | Si el antivirus no da resultado, el producto se guarda sin imagen; si la da por segura, con ella; un producto sin imagen no se analiza | Unitario | 3 | 🔍 En revisión (3, AB#397, PR #189) |
| HU31-CP34 | Una imagen que no supera el antivirus responde 400 con el código `IMAGE_NOT_SAFE` | Integración (capa web) | 1 | 🔍 En revisión (1, AB#397, PR #189) |

Notas:
- **HU31-CP01 a CP10:** casos propuestos en la T201 (AB#201) a partir del contrato del formulario de producto del frontend (`frontend/docs/hu31-formulario-producto.md`). Pendientes de confirmar con el equipo y de añadir a la HU31 en Azure Boards.
- **Backend (T201):** solo la entidad `Producto` y el objeto de valor `Descuento` (`productos/domain/model`), en Java puro. Aún no hay repositorio, servicio ni endpoint: llegarán con las tasks AB#202, AB#204 y AB#205. Los casos de capa web y de persistencia (400/409, guardado en MongoDB) se añadirán con ellas.
- **HU31-CP11 y CP12:** casos propuestos en la T202 (AB#202) para la persistencia. Pendientes de confirmar con el equipo y de añadir a la HU31 en Azure Boards.
- **HU31-CP25:** caso propuesto en la T258 (AB#258), de la revisión del Sprint 1. El índice único `vendedor_nombre` ya no distinguía mayúsculas (PR #99, AB#179); la T258 guarda el nombre sin espacios en los extremos para que el índice coincida con la comprobación previa. Pendiente de confirmar con el equipo. Las tildes siguen contando (`Ratón` ≠ `Raton`).
- **Backend (T202):** repositorio de productos: puerto `ProductoRepositoryPort` (`guardar` y `buscarPorId`) y su adaptador de MongoDB, con documento y mapper propios. El listado por vendedor llegará con la AB#183.
- **HU31-CP11 y CP12:** casos propuestos en la T202 (AB#202) para la persistencia. Pendientes de confirmar con el equipo y de añadir a la HU31 en Azure Boards.
- **Backend (T202):** repositorio de productos: puerto `ProductoRepositoryPort` (`guardar` y `buscarPorId`) y su adaptador de MongoDB, con documento y mapper propios. El listado por vendedor llegará con la AB#183.
- **Una sola categoría por producto:** decisión del equipo; el producto guarda `categoriaId`, no una lista. Desde la T202 el validador de MongoDB también lo exige (antes era una lista `categoriaIds`).; desde la T202 el validador de MongoDB también exige `categoriaId` (antes era una lista `categoriaIds`).
- **Estado:** los tests de la T201 (PR #83) y de la T202 (PR #86) están en `develop`.
- **HU31-CP13 a CP24:** casos propuestos en las tasks AB#179, AB#180, AB#204 y AB#205 (alta de producto, TDD). Pendientes de confirmar con el equipo y de añadir a la HU31 en Azure Boards. El orden de los casos sigue el orden de las capas (servicio, capa web, MongoDB); CP11 y CP12 ya eran de la persistencia (T202).
- **Backend (AB#204 y AB#205):** `GestionProductosUseCase.crear` y su servicio, y `POST /api/seller/products` (201; 400 y 409 con `code`). El vendedor sale de la sesión. Al guardar, la comprobación `existePorVendedorYNombre` se complementa con un índice único `(vendedorId, nombre)` con collation de nivel 2 (sin distinguir mayúsculas, sí tildes) y el adaptador traduce `DuplicateKeyException` a `ProductoDuplicadoException`.
- **Decisión del equipo:** «teclado» y «Teclado» cuentan como el mismo nombre de producto de un vendedor.
- **Pendiente de acordar con el frontend:** el `code` del 409 (`PRODUCT_NAME_ALREADY_EXISTS`); el contrato (`frontend/docs/hu31-formulario-producto.md`) solo define el 400. Los 400 de validación de campos (Bean Validation) devuelven el cuerpo por defecto de Spring, sin `code`; solo el 400 de una regla del dominio lleva `VALIDATION_ERROR` (CP21).
- **Sin comprobar:** que la categoría exista (aún no hay módulo de categorías). La condición de carrera de CP24 se prueba creando el segundo producto directamente en el adaptador, sin hilos concurrentes.
- **Estado:** los tests de AB#179, AB#180, AB#204 y AB#205 están en `develop` (PR #99).
- **Frontend:** los tests de la HU31 (AB#182) no tienen todavía filas en la matriz.
- **HU31-CP26 y CP27 (revisión del Sprint 1, R5 y R6):** casos propuestos en las T256 (AB#256) y T257 (AB#257). Pendientes de confirmar con el equipo y de añadir a la HU31 en Azure Boards. El desplegable es el componente compartido `CategoryCombobox` y, por decisión del equipo, se usa en todas las vistas que eligen una categoría: formulario de producto, registro de vendedor, edición de un vendedor por el administrador y filtros de los listados. Los tests de esas otras pantallas (`Register.test.jsx`, `ProfileEditor.test.jsx`, `EditableProfile.test.jsx`, `UserEditDialog.test.jsx`, `SellerProductsPage.test.jsx` y `AdminSellersPage.test.jsx`) solo se adaptan para elegir la categoría en el desplegable; no llevan ID de caso y no tienen filas en la Tabla B. `ProfileEditor.test.jsx` añade además `profileEditor_deUnVendedor_laCategoriaPrincipalSeEligeEnUnDesplegableConLasCategorias`, que por decisión del equipo tampoco lleva ID de caso. El 409 ya lo devolvía el backend (CP22); el mock del frontend lo devuelve ahora igual. Al editar, el mismo mensaje en el campo Nombre lo comprueba HU34-CP18 (decisión del equipo al resolver el conflicto con el PR #122: un único mensaje, junto al campo, en alta y en edición).
- **HU31-CP28 (R8, seguridad en la capa de negocio):** caso propuesto en la rama `fix/seguridad-capa-negocio`, sin task ni `AB#` por ser un fix transversal. Pendiente de confirmar con el equipo. Comprueba con `@PreAuthorize` que cada caso de uso de productos exige su rol (gestión y listado: vendedor con su propio correo; supervisión y conteo por categoría: administrador; catálogo y existencias: cliente). Clase de test nueva `productos/application/service/SeguridadProductosServiceTest` (contexto mínimo de Spring con `@EnableMethodSecurity`, sin MongoDB).
- **HU31-CP29 a CP34 (T397, AB#397):** casos propuestos, pendientes de confirmar con el equipo y de añadir a la HU31 en Azure Boards. Antivirus de la URL de la imagen con la API pública v3 de VirusTotal (no hay subida de ficheros). Decisiones del equipo: maliciosa si `malicious > 0` → 400 `IMAGE_NOT_SAFE`; si VirusTotal no responde, se queda sin cuota o falla la API key, el producto se guarda sin imagen; una URL nueva se espera como mucho 2 consultas cada 3 s (4 peticiones, el límite por minuto); la API no avisa del descarte (PENDIENTE para el frontend). Los tests del adaptador no llaman a VirusTotal (`MockRestServiceServer`) y la API key de los tests es falsa (`MongoTestcontainersConfig`).

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
| HU31-CP11 | `ProductoDocumentMapperTest` | `convierteProductoEnDocumento` | Infraestructura | AB#202 | PR #86 | ✅ |
| HU31-CP11 | `ProductoDocumentMapperTest` | `convierteDocumentoEnProducto` | Infraestructura | AB#202 | PR #86 | ✅ |
| HU31-CP11 | `ProductoMongoAdapterTest` | `guardarAsignaIdYLoGuardaEnProductos` | Infraestructura | AB#202 | PR #86 | ✅ |
| HU31-CP11 | `ProductoMongoAdapterTest` | `buscarPorIdDevuelveElProductoGuardado` | Infraestructura | AB#202 | PR #86 | ✅ |
| HU31-CP12 | `ProductoMongoAdapterTest` | `buscarPorIdDevuelveVacioSiNoExiste` | Infraestructura | AB#202 | PR #86 | ✅ |
| HU31-CP13 | `GestionProductosServiceTest` | `creaElProductoConElVendedorDeLaSesion` | Aplicación | AB#204 | PR #99 | ✅ |
| HU31-CP14 | `GestionProductosServiceTest` | `devuelveElProductoGuardadoConSuId` | Aplicación | AB#204 | PR #99 | ✅ |
| HU31-CP15 | `GestionProductosServiceTest` | `rechazaDatosInvalidosYNoGuardaNada (precio 0, stock negativo y categoría en blanco)` | Aplicación | AB#204 | PR #99 | ✅ |
| HU31-CP16 | `GestionProductosServiceTest` | `rechazaUnNombreRepetidoDelMismoVendedor` | Aplicación | AB#204 | PR #99 | ✅ |
| HU31-CP17 | `ProductoControllerTest` | `vendedorCreaUnProducto` | Infraestructura | AB#205 | PR #99 | ✅ |
| HU31-CP18 | `ProductoControllerTest` | `elVendedorSaleDeLaSesionYNoDelCuerpo` | Infraestructura | AB#205 | PR #99 | ✅ |
| HU31-CP19 | `ProductoControllerTest` | `sinSesionNoSeCreaUnProducto` | Infraestructura | AB#205 | PR #99 | ✅ |
| HU31-CP19 | `ProductoControllerTest` | `noVendedorNoCreaUnProducto (cliente y administrador)` | Infraestructura | AB#205 | PR #99 | ✅ |
| HU31-CP19 | `ProductoControllerTest` | `sinTokenCsrfNoSeCreaUnProducto` | Infraestructura | AB#205 | PR #99 | ✅ |
| HU31-CP20 | `ProductoControllerTest` | `cuerpoInvalidoDevuelve400 (nombre vacío o de más de 100 caracteres, precio 0, negativo o ausente, stock negativo, categoría vacía, imagen `javascript:`, `disponible` ausente o nulo, descuento activo sin porcentaje y porcentaje 101)` | Infraestructura | AB#179 | PR #99 (modificado en PR #183) | ✅ |
| HU31-CP20 | `ProductoControllerTest` | `cuerpoQueNoEsJsonDevuelve400` | Infraestructura | AB#179 | PR #99 (modificado en PR #183) | ✅ |
| HU31-CP21 | `ProductoControllerTest` | `reglaDeDominioIncumplidaDevuelve400Generico` | Infraestructura | AB#179 | PR #99 | ✅ |
| HU31-CP22 | `ProductoControllerTest` | `nombreRepetidoDevuelve409` | Infraestructura | AB#179 | PR #99 | ✅ |
| HU31-CP23 | `ProductoMongoAdapterTest` | `existePorVendedorYNombreNoDistingueMayusculas` | Infraestructura | AB#204 | PR #99 | ✅ |
| HU31-CP23 | `ProductoMongoAdapterTest` | `existePorVendedorYNombreComparaElNombreCompletoYLiteral` | Infraestructura | AB#204 | PR #99 | ✅ |
| HU31-CP24 | `ProductoMongoAdapterTest` | `guardarRechazaUnNombreRepetidoDelMismoVendedor` | Infraestructura | AB#179 | PR #99 | ✅ |
| HU31-CP25 | `ProductoTest` | `quitaLosEspaciosDeLosExtremosDelNombre` | Dominio | AB#258 | PR #113 | ✅ |
| HU31-CP25 | `ProductoTest` | `elLimiteDelNombreSeCuentaSinLosEspacios` | Dominio | AB#258 | PR #113 | ✅ |
| HU31-CP25 | `ProductoMongoAdapterTest` | `guardarRechazaUnNombreRepetidoConEspacios` | Infraestructura | AB#258 | PR #113 | ✅ |
| HU31-CP26 | `CategoryCombobox.test.jsx` | `HU31-CP26: al abrirlo muestra todas las categorías en el orden en que llegan` | Frontend · componente | AB#256 | PR #123 | ✅ |
| HU31-CP26 | `CategoryCombobox.test.jsx` | `HU31-CP26: al escribir filtra las categorías sin distinguir mayúsculas ni tildes` | Frontend · componente | AB#256 | PR #123 | ✅ |
| HU31-CP26 | `CategoryCombobox.test.jsx` | `HU31-CP26: si ninguna categoría coincide lo indica` | Frontend · componente | AB#256 | PR #123 | ✅ |
| HU31-CP26 | `CategoryCombobox.test.jsx` | `HU31-CP26: al elegir una categoría avisa con su identificador` | Frontend · componente | AB#256 | PR #123 | ✅ |
| HU31-CP26 | `CategoryCombobox.test.jsx` | `HU31-CP26: muestra el nombre de la categoría elegida` | Frontend · componente | AB#256 | PR #123 | ✅ |
| HU31-CP26 | `CategoryCombobox.test.jsx` | `HU31-CP26: con una categoría elegida, al abrirlo sigue ofreciendo todas` | Frontend · componente | AB#256 | PR #123 | ✅ |
| HU31-CP26 | `CategoryCombobox.test.jsx` | `HU31-CP26: si se escribe y se sale sin elegir, vuelve a mostrar la categoría elegida` | Frontend · componente | AB#256 | PR #123 | ✅ |
| HU31-CP26 | `CategoryCombobox.test.jsx` | `HU31-CP26: con una opción para todas, la ofrece la primera y elegirla avisa con un valor vacío` | Frontend · componente | AB#256 | PR #123 | ✅ |
| HU31-CP26 | `CategoryCombobox.test.jsx` | `HU31-CP26: con una opción para todas y sin categoría elegida, muestra esa opción` | Frontend · componente | AB#256 | PR #123 | ✅ |
| HU31-CP26 | `SellerProductFormPage.test.jsx` | `HU31-CP26: la categoría se elige en un desplegable con las categorías actuales` | Frontend · página | AB#256 | PR #123 | ✅ |
| HU31-CP26 | `SellerProductFormPage.test.jsx` | `HU31-CP26: se puede escribir para buscar la categoría y se envía su identificador` | Frontend · página | AB#256 | PR #123 | ✅ |
| HU31-CP27 | `SellerProductFormPage.test.jsx` | `HU31-CP27: un nombre que el vendedor ya usa se marca en el campo Nombre con un mensaje propio` | Frontend · página | AB#257 | PR #123 | ✅ |
| HU31-CP27 | `SellerProductFormPage.test.jsx` | `HU31-CP27: al cambiar el nombre desaparece el aviso de nombre repetido` | Frontend · página | AB#257 | PR #123 | ✅ |
| HU31-CP28 | `SeguridadProductosServiceTest` | `unClienteNoUsaLosCasosDeUsoDelVendedor` | Aplicación | — | Rama `fix/seguridad-capa-negocio` | 🔍 |
| HU31-CP28 | `SeguridadProductosServiceTest` | `unAdministradorNoUsaLosCasosDeUsoDelVendedor` | Aplicación | — | Rama `fix/seguridad-capa-negocio` | 🔍 |
| HU31-CP28 | `SeguridadProductosServiceTest` | `unVendedorNoOperaConElCorreoDeOtro` | Aplicación | — | Rama `fix/seguridad-capa-negocio` | 🔍 |
| HU31-CP28 | `SeguridadProductosServiceTest` | `unVendedorOperaConSuPropioCorreo` | Aplicación | — | Rama `fix/seguridad-capa-negocio` | 🔍 |
| HU31-CP28 | `SeguridadProductosServiceTest` | `soloElAdministradorSupervisa` | Aplicación | — | Rama `fix/seguridad-capa-negocio` | 🔍 |
| HU31-CP28 | `SeguridadProductosServiceTest` | `elAdministradorCuentaLosProductos` | Aplicación | — | Rama `fix/seguridad-capa-negocio` | 🔍 |
| HU31-CP28 | `SeguridadProductosServiceTest` | `unVendedorNoUsaElCatalogoNiLasExistencias` | Aplicación | — | Rama `fix/seguridad-capa-negocio` | 🔍 |
| HU31-CP28 | `SeguridadProductosServiceTest` | `unClienteUsaElCatalogo` | Aplicación | — | Rama `fix/seguridad-capa-negocio` | 🔍 |
| HU31-CP28 | `SeguridadProductosServiceTest` | `sinSesionSeRechazaTodo` | Aplicación | — | Rama `fix/seguridad-capa-negocio` | 🔍 |
| HU31-CP29 | `VirusTotalAdapterTest` | `unaUrlConInformeMaliciosoEsMaliciosa` | Infraestructura | AB#397 | PR #189 | 🔍 |
| HU31-CP29 | `VirusTotalAdapterTest` | `unaUrlConInformeSinMaliciososEsSegura` | Infraestructura | AB#397 | PR #189 | 🔍 |
| HU31-CP30 | `VirusTotalAdapterTest` | `unaUrlNuevaSeEnviaAAnalizarYSeEsperaAlResultado` | Infraestructura | AB#397 | PR #189 | 🔍 |
| HU31-CP30 | `VirusTotalAdapterTest` | `siElAnalisisTerminaEnLaPrimeraConsultaNoSeVuelveAConsultar` | Infraestructura | AB#397 | PR #189 | 🔍 |
| HU31-CP30 | `VirusTotalAdapterTest` | `siElAnalisisNoTerminaNoHayResultado` | Infraestructura | AB#397 | PR #189 | 🔍 |
| HU31-CP31 | `VirusTotalAdapterTest` | `unErrorAlBuscarElInformeNoDaResultado` (401, 429, 500 y 503) | Infraestructura | AB#397 | PR #189 | 🔍 |
| HU31-CP31 | `VirusTotalAdapterTest` | `sinCuotaAlEnviarAAnalizarNoHayResultado` | Infraestructura | AB#397 | PR #189 | 🔍 |
| HU31-CP31 | `VirusTotalAdapterTest` | `sinConexionNoHayResultado` | Infraestructura | AB#397 | PR #189 | 🔍 |
| HU31-CP32 | `GestionProductosServiceTest` | `rechazaUnaImagenMaliciosaYNoGuardaNada` | Aplicación | AB#397 | PR #189 | 🔍 |
| HU31-CP33 | `GestionProductosServiceTest` | `sinResultadoDelAntivirusSeGuardaSinImagen` | Aplicación | AB#397 | PR #189 | 🔍 |
| HU31-CP33 | `GestionProductosServiceTest` | `unaImagenSeguraSeGuardaConElProducto` | Aplicación | AB#397 | PR #189 | 🔍 |
| HU31-CP33 | `GestionProductosServiceTest` | `unProductoSinImagenNoSeAnaliza` | Aplicación | AB#397 | PR #189 | 🔍 |
| HU31-CP34 | `ProductoControllerTest` | `imagenNoSeguraDevuelve400` | Infraestructura | AB#397 | PR #189 | 🔍 |

Rutas de las clases de test (bajo `backend/src/test/java/es/uclm/esibuy/backend/`):
- `ProductoTest` y `DescuentoTest`: `productos/domain/model/` (JUnit, sin Spring)
- `ProductoDocumentMapperTest`: `productos/infrastructure/adapter/out/persistence/` (JUnit, sin Spring)
- `ProductoMongoAdapterTest`: `productos/infrastructure/adapter/out/persistence/` (con Testcontainers)
- `GestionProductosServiceTest`: `productos/application/service/` (Mockito, sin Spring)
- `ProductoControllerTest`: `productos/infrastructure/adapter/in/web/` (con `@WebMvcTest`; la seguridad es la real de `SecurityConfig`)
- `CategoryCombobox.test.jsx` y `SellerProductFormPage.test.jsx` (frontend): `frontend/src/components/` y `frontend/src/pages/` (Vitest, React Testing Library y MSW)

---

## HU32 · Listar mis productos

### Tabla A · HU32 ↔ casos de prueba

| Caso | Descripción | Tipo | Tests | Estado |
|---|---|---|---|---|
| HU32-CP01 | Solo se listan los productos del vendedor con sesión (el vendedor sale de la sesión, no de un parámetro) | Unitario + integración (MongoDB y capa web) | 5 | ✅ Pasa |
| HU32-CP02 | Búsqueda por nombre (sin distinguir mayúsculas, texto literal) y filtro por categoría, combinables | Integración MongoDB | 2 | ✅ Pasa |
| HU32-CP03 | Listado paginado, del más reciente al más antiguo, con `totalPages` 0 si no hay resultados | Integración (MongoDB y capa web) | 2 | ✅ Pasa |
| HU32-CP04 | Una página o un tamaño menor que 1 devuelve 400 | Integración (capa web) | 1 | ✅ Pasa |
| HU32-CP05 | Sin sesión (401) o sin ser vendedor (403) no se ve el listado | Integración (capa web) | 2 | ✅ Pasa |

Notas:
- **HU32-CP01 a CP05:** casos propuestos en la T184 (AB#184) a partir de los criterios de aceptación de la HU32 (`frontend/docs/hu32-listado-productos.md`). Pendientes de confirmar con el equipo y de añadir a la HU32 en Azure Boards.
- **Backend (T183, AB#183, PR #88):** `GET /api/seller/products`. El vendedor sale de la sesión: productos pide su id al módulo de usuarios por su puerto de entrada (`UsuarioEnSesionPort`).
- **Pendiente:** el contrato del frontend pide `categorias` con el nombre de la categoría; hasta que exista el módulo de categorías se devuelve `categoriaId`. La búsqueda todavía distingue acentos.
- **Frontend:** los tests de la HU32 (AB#187) no tienen todavía filas en la matriz.

### Tabla B · casos de prueba ↔ tests

| Caso | Clase de test | Método | Capa | Task | Origen | Estado |
|---|---|---|---|---|---|---|
| HU32-CP01 | `AutenticacionServiceTest` | `consultaElIdDelUsuarioEnSesion` | Aplicación | AB#184 | PR #90 | ✅ |
| HU32-CP01 | `UsuarioEnSesionAdapterTest` | `idPorCorreoPreguntaAlModuloDeUsuarios` | Infraestructura | AB#184 | PR #90 | ✅ |
| HU32-CP01 | `ListadoProductosServiceTest` | `listaLosProductosDelVendedorDeLaSesion` | Aplicación | AB#184 | PR #90 | ✅ |
| HU32-CP01 | `ProductoMongoAdapterTest` | `listarPorVendedorSoloDevuelveLosSuyos` | Infraestructura | AB#184 | PR #90 | ✅ |
| HU32-CP01 | `ProductoControllerTest` | `vendedorRecibeSuListado` | Infraestructura | AB#184 | PR #90 | ✅ |
| HU32-CP02 | `ProductoMongoAdapterTest` | `listarPorVendedorBuscaPorNombreSinDistinguirMayusculas` | Infraestructura | AB#184 | PR #90 | ✅ |
| HU32-CP02 | `ProductoMongoAdapterTest` | `listarPorVendedorFiltraPorCategoriaYBusqueda` | Infraestructura | AB#184 | PR #90 | ✅ |
| HU32-CP03 | `ProductoMongoAdapterTest` | `listarPorVendedorPaginaDelMasRecienteAlMasAntiguo` | Infraestructura | AB#184 | PR #90 | ✅ |
| HU32-CP03 | `ProductoControllerTest` | `listadoVacioTieneCeroPaginas` | Infraestructura | AB#184 | PR #90 | ✅ |
| HU32-CP04 | `ProductoControllerTest` | `paginaOTamanoMenorQueUnoDevuelve400` (`page` y `size`) | Infraestructura | AB#184 | PR #90 | ✅ |
| HU32-CP05 | `ProductoControllerTest` | `sinSesionDevuelve401` | Infraestructura | AB#184 | PR #90 | ✅ |
| HU32-CP05 | `ProductoControllerTest` | `noVendedorNoVeElListado` (cliente y administrador) | Infraestructura | AB#184 | PR #90 | ✅ |

Rutas de las clases de test (bajo `backend/src/test/java/es/uclm/esibuy/backend/`):
- `AutenticacionServiceTest`: `usuarios/application/service/` (Mockito, sin Spring)
- `UsuarioEnSesionAdapterTest`: `productos/infrastructure/adapter/out/usuarios/` (Mockito, sin Spring)
- `ListadoProductosServiceTest`: `productos/application/service/` (Mockito, sin Spring)
- `ProductoMongoAdapterTest`: `productos/infrastructure/adapter/out/persistence/` (con Testcontainers)
- `ProductoControllerTest`: `productos/infrastructure/adapter/in/web/` (con `@WebMvcTest`)

---

## HU33 · Consultar el detalle de un producto propio

### Tabla A · HU33 ↔ casos de prueba

| Caso | Descripción | Tipo | Tests | Estado |
|---|---|---|---|---|
| HU33-CP01 | El vendedor consulta el detalle de un producto suyo (el vendedor sale de la sesión) | Unitario | 1 | ✅ Pasa |
| HU33-CP02 | Un producto que no existe no se encuentra (`ProductoNoEncontradoException`) | Unitario | 1 | ✅ Pasa |
| HU33-CP03 | Un producto de otro vendedor no se encuentra, con el mismo error que si no existiera, para no revelar que existe | Unitario | 1 | ✅ Pasa |
| HU33-CP04 | `GET /api/seller/products/{id}` devuelve 200 con el producto en el formato del contrato y sin el id del vendedor | Integración (capa web) | 1 | ✅ Pasa |
| HU33-CP05 | Un producto inexistente o de otro vendedor devuelve 404 con el código `PRODUCT_NOT_FOUND` | Integración (capa web) | 1 | ✅ Pasa |
| HU33-CP06 | Sin sesión (401) o sin ser vendedor (403) no se consulta un producto | Integración (capa web) | 2 | ✅ Pasa |
| HU33-CP07 | Desde el listado, Ver abre la ficha del producto con todos sus datos en solo lectura y la categoría con su nombre | Frontend (componente y página) | 4 | ✅ Pasa |
| HU33-CP08 | La ficha muestra las acciones Editar, que lleva al formulario de edición (HU34), y Eliminar, que pide la confirmación del listado (HU35) | Frontend (componente) | 1 | ✅ Pasa |
| HU33-CP09 | Un producto no encontrado (403 o 404, p. ej. de otro vendedor) lo indica y no ofrece acciones | Frontend (componente) | 2 | ✅ Pasa |
| HU33-CP10 | Si la carga falla por otro motivo, la ficha muestra un error genérico y permite reintentar | Frontend (componente) | 1 | ✅ Pasa |

Notas:
- **HU33-CP01 a CP06:** casos propuestos en la T349 (AB#349) y la T348 (AB#348) y acordados con el equipo durante el desarrollo. Pendientes de añadir a la HU33 en Azure Boards.
- **HU33-CP07 a CP10:** casos propuestos en la T347 (AB#347) a partir de los criterios de aceptación de la HU33 y acordados con el equipo. Pendientes de añadir a la HU33 en Azure Boards.
- **Backend (T349, AB#349, PR #117):** `consultarMiProducto` en `GestionProductosUseCase`. Un producto ajeno se trata como inexistente.
- **Backend (T348, AB#348, PR #118):** `GET /api/seller/products/{id}`, con el contrato de `frontend/docs/hu31-formulario-producto.md`. El producto inexistente y el ajeno dan 404, no 403.
- **HU33-CP06:** sin rojo real, porque `SecurityConfig` ya limita `/api/seller/**` al rol VENDEDOR.
- **Frontend (T347, AB#347, PR #119):** `ProductDetailDialog`, que se abre con el botón Ver de cada fila del listado (`ProductRowActions`). Usa `GET /api/seller/products/:id`.
- **Editar y Eliminar** se ven en la ficha. Editar se activa en la HU34 (T341, PR #122) y Eliminar en la HU35 (T344, PR #132); cada una cambia el HU33-CP08.

### Tabla B · casos de prueba ↔ tests

| Caso | Clase de test | Método | Capa | Task | Origen | Estado |
|---|---|---|---|---|---|---|
| HU33-CP01 | `GestionProductosServiceTest` | `devuelveElProductoSiEsDelVendedorDeLaSesion` | Aplicación | AB#349 | PR #117 | ✅ |
| HU33-CP02 | `GestionProductosServiceTest` | `unProductoQueNoExisteNoSeEncuentra` | Aplicación | AB#349 | PR #117 | ✅ |
| HU33-CP03 | `GestionProductosServiceTest` | `unProductoDeOtroVendedorNoSeEncuentra` | Aplicación | AB#349 | PR #117 | ✅ |
| HU33-CP04 | `ProductoControllerTest` | `vendedorConsultaSuProducto` | Infraestructura | AB#348 | PR #118 | ✅ |
| HU33-CP05 | `ProductoControllerTest` | `productoNoEncontradoDevuelve404` | Infraestructura | AB#348 | PR #118 | ✅ |
| HU33-CP06 | `ProductoControllerTest` | `sinSesionNoSeConsultaUnProducto` | Infraestructura | AB#348 | PR #118 | ✅ |
| HU33-CP06 | `ProductoControllerTest` | `noVendedorNoConsultaUnProducto` (cliente y administrador) | Infraestructura | AB#348 | PR #118 | ✅ |
| HU33-CP07 | `ProductDetailDialog.test.jsx` | `HU33-CP07: muestra todos los datos del producto, con el nombre de su categoría` | Frontend · componente | AB#347 | PR #119 | ✅ |
| HU33-CP07 | `ProductDetailDialog.test.jsx` | `HU33-CP07: todos los campos son de solo lectura` | Frontend · componente | AB#347 | PR #119 | ✅ |
| HU33-CP07 | `SellerProductsPage.test.jsx` | `HU33-CP07: cada fila tiene un botón para ver la ficha del producto` | Frontend · página | AB#347 | PR #119 | ✅ |
| HU33-CP07 | `SellerProductsPage.test.jsx` | `HU33-CP07: al pulsar Ver se abre la ficha de ese producto` | Frontend · página | AB#347 | PR #119 | ✅ |
| HU33-CP08 | `ProductDetailDialog.test.jsx` | `HU33-CP08: muestra las acciones Editar, que lleva al formulario de edición, y Eliminar, que avisa con el producto` | Frontend · componente | AB#347 · AB#341 · AB#344 | PR #119 · PR #122 · PR #132 | ✅ |
| HU33-CP09 | `ProductDetailDialog.test.jsx` | `HU33-CP09: un producto de otro vendedor muestra que no se ha encontrado y no ofrece acciones` | Frontend · componente | AB#347 | PR #119 | ✅ |
| HU33-CP09 | `ProductDetailDialog.test.jsx` | `HU33-CP09: una respuesta %i muestra que no se ha encontrado el producto` (403 y 404) | Frontend · componente | AB#347 | PR #119 | ✅ |
| HU33-CP10 | `ProductDetailDialog.test.jsx` | `HU33-CP10: si la carga falla por otro motivo, muestra un error genérico y permite reintentar` | Frontend · componente | AB#347 | PR #119 | ✅ |

Rutas de las clases de test:
- `GestionProductosServiceTest`: `backend/src/test/java/es/uclm/esibuy/backend/productos/application/service/` (Mockito, sin Spring)
- `ProductoControllerTest`: `backend/src/test/java/es/uclm/esibuy/backend/productos/infrastructure/adapter/in/web/` (con `@WebMvcTest`)
- `ProductDetailDialog.test.jsx`: `frontend/src/features/seller/` (Vitest, RTL y MSW)
- `SellerProductsPage.test.jsx`: `frontend/src/pages/` (Vitest, RTL y MSW)

---

## HU34 · Modificar un producto propio

### Tabla A · HU34 ↔ casos de prueba

| Caso | Descripción | Tipo | Tests | Estado |
|---|---|---|---|---|
| HU34-CP01 | El vendedor guarda los cambios de un producto suyo, con el mismo id y el mismo vendedor | Unitario | 1 | ✅ Pasa |
| HU34-CP02 | Se aplican las mismas validaciones que en el alta y unos datos inválidos no se guardan | Unitario | 1 | ✅ Pasa |
| HU34-CP03 | No se puede cambiar el nombre por el de otro producto del vendedor (`ProductoDuplicadoException`) | Unitario | 1 | ✅ Pasa |
| HU34-CP04 | Conservar el nombre del producto, aunque cambien las mayúsculas, no es un nombre repetido | Unitario | 1 | ✅ Pasa |
| HU34-CP05 | Un vendedor no puede modificar un producto de otro vendedor ni uno inexistente (`ProductoNoEncontradoException`) | Unitario | 2 | ✅ Pasa |
| HU34-CP06 | `PUT /api/seller/products/{id}` devuelve 200 con el producto actualizado; el vendedor sale de la sesión, no del cuerpo | Integración (capa web) | 1 | ✅ Pasa |
| HU34-CP07 | Una modificación con datos no válidos devuelve 400 y no llega al caso de uso; una regla del dominio incumplida, 400 genérico | Integración (capa web) | 2 | ✅ Pasa |
| HU34-CP08 | Cambiar el nombre por uno repetido del mismo vendedor devuelve 409 con el código `PRODUCT_NAME_ALREADY_EXISTS` | Integración (capa web) | 1 | ✅ Pasa |
| HU34-CP09 | Modificar un producto inexistente o de otro vendedor devuelve 404 con el código `PRODUCT_NOT_FOUND` | Integración (capa web) | 1 | ✅ Pasa |
| HU34-CP10 | Sin sesión (401), sin ser vendedor (403) o sin token CSRF (403) no se modifica un producto | Integración (capa web) | 3 | ✅ Pasa |
| HU34-CP11 | Los cambios se reflejan inmediatamente en el catálogo que ven los clientes | — | 0 | ⏳ Pendiente (sin verificar hasta que exista el catálogo de clientes) |
| HU34-CP12 | La vista de edición se abre con los datos actuales del producto ya cargados (también los descuentos) | Frontend (página) | 2 | ✅ Pasa |
| HU34-CP13 | Al guardar, se envían los cambios al producto indicado y se vuelve al listado con un aviso | Frontend (página) | 1 | ✅ Pasa |
| HU34-CP14 | La edición aplica las mismas validaciones que el alta | Frontend (página) | 1 | ✅ Pasa |
| HU34-CP15 | Un producto de otro vendedor o inexistente no muestra el formulario | Frontend (página) | 2 | ✅ Pasa |
| HU34-CP16 | Si el producto no carga, la vista muestra un error y permite reintentar | Frontend (página) | 1 | ✅ Pasa |
| HU34-CP17 | Desde la ficha del producto, Editar abre el formulario de edición con sus datos | Frontend (página) | 1 | ✅ Pasa |
| HU34-CP18 | Si el nombre ya lo tiene otro producto del vendedor (409), la edición lo indica en el campo Nombre con el mismo mensaje que el alta (HU31-CP27) y no sale del formulario | Frontend (página) | 1 | ✅ Pasa |
| HU34-CP19 | Al modificar, cambiar la imagen por una maliciosa se rechaza y, si el antivirus no da resultado, se guarda sin imagen; si la imagen no cambia no se vuelve a analizar | Unitario | 3 | 🔍 En revisión (3, AB#397, PR #189) |
| HU34-CP20 | Cambiar la imagen por una que no supera el antivirus responde 400 con el código `IMAGE_NOT_SAFE` | Integración (capa web) | 1 | 🔍 En revisión (1, AB#397, PR #189) |

Notas:
- **HU34-CP01 a CP05:** casos propuestos en la T343 (AB#343) a partir de los criterios de aceptación de la HU34. Pendientes de añadir a la HU34 en Azure Boards.
- **Campos editables (decisión del equipo):** el id y el vendedor no se modifican; el nombre sí, controlando duplicados, y `disponible` también.
- **HU34-CP06 a CP10:** casos propuestos en la T342 (AB#342, PR #121). `PUT /api/seller/products/{id}` sigue el contrato de `frontend/docs/hu31-formulario-producto.md`.
- **HU34-CP02 y CP07 a CP10:** sin rojo real. El CP02, porque el constructor de `Producto` ya aplica las validaciones del alta; los CP07 a CP10, porque la validación de `ProductoRequest`, `ManejadorGlobalErrores` y `SecurityConfig` ya los cubrían.
- **HU34-CP11 · PENDIENTE DE VERIFICAR (decisión del equipo):** el criterio «los cambios se reflejan inmediatamente en el catálogo que ven los clientes» **no se verifica todavía**, porque en el backend aún no existe el catálogo de clientes. La modificación actualiza el mismo documento de la colección `productos` (lo prueba el test técnico «Persistencia: guardar un producto leído lo actualiza sin duplicarlo y conserva createdAt» de `ProductoMongoAdapterTest`), que es la que leerá el catálogo. **Cuando se desarrolle el catálogo de clientes, hay que añadir el test de este caso y pasarlo a ✅.**
- **HU34-CP12 a CP16:** la vista de edición se hizo en la HU31 (formulario compartido con el alta). Por decisión del equipo, sus 7 tests se renombran en la T341 (AB#341, PR #122) con el ID de su caso de la HU34 y se trazan aquí; no tienen ciclo rojo porque no hay código nuevo detrás.
- **HU34-CP17 y CP18:** casos nuevos de la T341. El nombre repetido se indica en el campo Nombre con el mismo mensaje en el alta y en la edición (decisión del equipo al resolver el conflicto del #123 con el #122; ver HU31-CP27).
- **HU34-CP19 y CP20 (T397, AB#397):** casos propuestos, pendientes de confirmar con el equipo. Mismas reglas del antivirus que en el alta (HU31-CP29 a CP34), pero solo si la URL de la imagen cambia (decisión del equipo, para no gastar la cuota de VirusTotal en cada modificación).

### Tabla B · casos de prueba ↔ tests

| Caso | Clase de test | Método | Capa | Task | Origen | Estado |
|---|---|---|---|---|---|---|
| HU34-CP01 | `GestionProductosServiceTest` | `guardaLosCambiosConElMismoIdYVendedor` | Aplicación | AB#343 | PR #120 | ✅ |
| HU34-CP02 | `GestionProductosServiceTest` | `rechazaUnaModificacionConDatosInvalidos` | Aplicación | AB#343 | PR #120 | ✅ |
| HU34-CP03 | `GestionProductosServiceTest` | `rechazaCambiarElNombrePorUnoRepetido` | Aplicación | AB#343 | PR #120 | ✅ |
| HU34-CP04 | `GestionProductosServiceTest` | `conservarElNombreNoEsUnNombreRepetido` (mismo nombre y en mayúsculas) | Aplicación | AB#343 | PR #120 | ✅ |
| HU34-CP05 | `GestionProductosServiceTest` | `unProductoQueNoExisteNoSeModifica` | Aplicación | AB#343 | PR #120 | ✅ |
| HU34-CP05 | `GestionProductosServiceTest` | `unProductoDeOtroVendedorNoSeModifica` | Aplicación | AB#343 | PR #120 | ✅ |
| HU34-CP06 | `ProductoControllerTest` | `vendedorModificaSuProducto` | Infraestructura | AB#342 | PR #121 | ✅ |
| HU34-CP07 | `ProductoControllerTest` | `cuerpoInvalidoNoModificaElProducto` (los mismos cuerpos no válidos que el alta) | Infraestructura | AB#342 | PR #121 (modificado en PR #183) | ✅ |
| HU34-CP07 | `ProductoControllerTest` | `reglaDeDominioIncumplidaAlModificarDevuelve400Generico` | Infraestructura | AB#342 | PR #121 | ✅ |
| HU34-CP08 | `ProductoControllerTest` | `nombreRepetidoAlModificarDevuelve409` | Infraestructura | AB#342 | PR #121 | ✅ |
| HU34-CP09 | `ProductoControllerTest` | `modificarUnProductoNoEncontradoDevuelve404` | Infraestructura | AB#342 | PR #121 | ✅ |
| HU34-CP10 | `ProductoControllerTest` | `sinSesionNoSeModificaUnProducto` | Infraestructura | AB#342 | PR #121 | ✅ |
| HU34-CP10 | `ProductoControllerTest` | `noVendedorNoModificaUnProducto` (cliente y administrador) | Infraestructura | AB#342 | PR #121 | ✅ |
| HU34-CP10 | `ProductoControllerTest` | `sinTokenCsrfNoSeModificaUnProducto` | Infraestructura | AB#342 | PR #121 | ✅ |
| HU34-CP11 | — | — (pendiente: sin catálogo de clientes) | — | — | — | ⏳ |
| HU34-CP12 | `SellerProductFormPage.test.jsx` | `HU34-CP12: la vista de edición se abre con los datos actuales del producto` | Frontend · página | AB#341 | PR #122 | ✅ |
| HU34-CP12 | `SellerProductFormPage.test.jsx` | `HU34-CP12: la vista de edición muestra los descuentos activos del producto` | Frontend · página | AB#341 | PR #122 | ✅ |
| HU34-CP13 | `SellerProductFormPage.test.jsx` | `HU34-CP13: al guardar, envía los cambios al producto indicado y vuelve al listado con un aviso` | Frontend · página | AB#341 | PR #122 | ✅ |
| HU34-CP14 | `SellerProductFormPage.test.jsx` | `HU34-CP14: la edición aplica las mismas validaciones que el alta` | Frontend · página | AB#341 | PR #122 | ✅ |
| HU34-CP15 | `SellerProductFormPage.test.jsx` | `HU34-CP15: un producto de otro vendedor no muestra el formulario` | Frontend · página | AB#341 | PR #122 | ✅ |
| HU34-CP15 | `SellerProductFormPage.test.jsx` | `HU34-CP15: un producto inexistente no muestra el formulario` | Frontend · página | AB#341 | PR #122 | ✅ |
| HU34-CP16 | `SellerProductFormPage.test.jsx` | `HU34-CP16: si el producto no carga, muestra un error y permite reintentar` | Frontend · página | AB#341 | PR #122 | ✅ |
| HU34-CP17 | `SellerProductsPage.test.jsx` | `HU34-CP17: desde la ficha, Editar abre el formulario de edición con los datos del producto` | Frontend · página | AB#341 | PR #122 | ✅ |
| HU34-CP18 | `SellerProductFormPage.test.jsx` | `HU34-CP18: si el nombre ya lo tiene otro producto del vendedor, lo indica y no sale del formulario` | Frontend · página | AB#341 | PR #122 | ✅ |
| HU34-CP19 | `GestionProductosServiceTest` | `cambiarAUnaImagenMaliciosaSeRechaza` | Aplicación | AB#397 | PR #189 | 🔍 |
| HU34-CP19 | `GestionProductosServiceTest` | `cambiarLaImagenSinResultadoSeGuardaSinImagen` | Aplicación | AB#397 | PR #189 | 🔍 |
| HU34-CP19 | `GestionProductosServiceTest` | `siLaImagenNoCambiaNoSeVuelveAAnalizar` | Aplicación | AB#397 | PR #189 | 🔍 |
| HU34-CP20 | `ProductoControllerTest` | `imagenNoSeguraAlModificarDevuelve400` | Infraestructura | AB#397 | PR #189 | 🔍 |

Rutas de las clases de test:
- `GestionProductosServiceTest`: `backend/src/test/java/es/uclm/esibuy/backend/productos/application/service/` (Mockito, sin Spring)
- `ProductoControllerTest`: `backend/src/test/java/es/uclm/esibuy/backend/productos/infrastructure/adapter/in/web/` (con `@WebMvcTest`)
- `SellerProductFormPage.test.jsx` y `SellerProductsPage.test.jsx`: `frontend/src/pages/` (Vitest, RTL y MSW)

---

## HU35 · Eliminar un producto propio

### Tabla A · HU35 ↔ casos de prueba

| Caso | Descripción | Tipo | Tests | Estado |
|---|---|---|---|---|
| HU35-CP01 | El vendedor elimina un producto suyo (el vendedor sale de la sesión) | Unitario | 1 | ✅ Pasa |
| HU35-CP02 | Un vendedor no puede eliminar un producto de otro vendedor ni uno inexistente (`ProductoNoEncontradoException`) y no se borra nada | Unitario | 2 | ✅ Pasa |
| HU35-CP03 | Eliminar un producto lo borra de la colección `productos` (borrado físico) y deja los demás | Integración | 1 | ✅ Pasa |
| HU35-CP04 | No se puede eliminar un producto que está o ha estado en algún pedido | — | 0 | ⏳ Pendiente (sin verificar hasta que exista el módulo de pedidos) |
| HU35-CP05 | `DELETE /api/seller/products/{id}` devuelve 204 sin cuerpo; el vendedor sale de la sesión | Integración (capa web) | 1 | ✅ Pasa |
| HU35-CP06 | Eliminar un producto inexistente o de otro vendedor devuelve 404 con el código `PRODUCT_NOT_FOUND` | Integración (capa web) | 1 | ✅ Pasa |
| HU35-CP07 | Sin sesión (401), sin ser vendedor (403) o sin token CSRF (403) no se elimina un producto | Integración (capa web) | 3 | ✅ Pasa |
| HU35-CP08 | Cada fila del listado tiene un botón para eliminar el producto | Frontend (página) | 1 | ✅ Pasa |
| HU35-CP09 | Al pulsar Eliminar se pide confirmación sin borrar nada | Frontend (página) | 1 | ✅ Pasa |
| HU35-CP10 | Cancelar o pulsar Escape cierra la confirmación sin borrar | Frontend (página) | 2 | ✅ Pasa |
| HU35-CP11 | Al confirmar, se borra el producto indicado y se avisa de que se ha eliminado | Frontend (página) | 1 | ✅ Pasa |
| HU35-CP12 | Tras eliminar, el listado se vuelve a cargar | Frontend (página) | 1 | ✅ Pasa |
| HU35-CP13 | Si falla la eliminación, se avisa sin detalles internos y se mantiene la confirmación | Frontend (página) | 1 | ✅ Pasa |
| HU35-CP14 | Desde la ficha del producto, Eliminar pide la misma confirmación y al confirmar se borra y se avisa | Frontend (página) | 1 | ✅ Pasa |

Notas:
- **HU35-CP01 a CP03:** casos propuestos en la T346 (AB#346, PR #126). Pendientes de contrastar con los criterios de aceptación de la HU35 y de añadir a la HU35 en Azure Boards.
- **Borrado físico (decisión del equipo):** el documento desaparece de la colección. Los `pedidos` guardan `nombreProducto` y `precioUnitario` en cada línea, así que el historial no depende del producto. `carritos`, `listas_deseos` y `valoraciones` aún no tienen código: sus HU tendrán que tratar las referencias a productos borrados.
- **HU35-CP04 · PENDIENTE DE IMPLEMENTAR (regla acordada en la reunión del Sprint 2):** un producto que está o ha estado en algún pedido no se puede eliminar. **No se comprueba todavía**, porque en el backend aún no existe el módulo de pedidos. **Cuando se desarrolle, hay que añadir la comprobación a `eliminarMiProducto` (a través de un puerto de salida hacia pedidos), su respuesta en el endpoint, el test de este caso y pasarlo a ✅.**
- **HU35-CP05 a CP07:** casos propuestos en la T345 (AB#345, PR #129). `DELETE /api/seller/products/{id}` sigue el contrato de `frontend/docs/hu31-formulario-producto.md`. CP06 y CP07 sin rojo real, porque `ManejadorGlobalErrores` y `SecurityConfig` ya los cubrían.
- **HU35-CP08 a CP13:** la eliminación desde el listado se hizo en la HU31. Por decisión del equipo, sus 7 tests se renombran en la T344 (AB#344, PR #132) con el ID de su caso de la HU35 y se trazan aquí; no tienen ciclo rojo porque no hay código nuevo detrás.
- **HU35-CP14:** caso nuevo de la T344. Eliminar en la ficha cierra la ficha y abre la confirmación del listado (decisión del equipo). La confirmación se busca por texto: en jsdom, Fluent la marca con `aria-hidden` a los ~300 ms de abrirse, y la búsqueda por rol fallaba en la batería completa.

### Tabla B · casos de prueba ↔ tests

| Caso | Clase de test | Método | Capa | Task | Origen | Estado |
|---|---|---|---|---|---|---|
| HU35-CP01 | `GestionProductosServiceTest` | `elVendedorEliminaSuProducto` | Aplicación | AB#346 | PR #126 | ✅ |
| HU35-CP02 | `GestionProductosServiceTest` | `unProductoQueNoExisteNoSeElimina` | Aplicación | AB#346 | PR #126 | ✅ |
| HU35-CP02 | `GestionProductosServiceTest` | `unProductoDeOtroVendedorNoSeElimina` | Aplicación | AB#346 | PR #126 | ✅ |
| HU35-CP03 | `ProductoMongoAdapterTest` | `eliminarBorraElDocumentoYDejaLosDemas` | Infraestructura | AB#346 | PR #126 | ✅ |
| HU35-CP04 | — | — (pendiente: sin módulo de pedidos) | — | — | — | ⏳ |
| HU35-CP05 | `ProductoControllerTest` | `vendedorEliminaSuProducto` | Infraestructura | AB#345 | PR #129 | ✅ |
| HU35-CP06 | `ProductoControllerTest` | `eliminarUnProductoNoEncontradoDevuelve404` | Infraestructura | AB#345 | PR #129 | ✅ |
| HU35-CP07 | `ProductoControllerTest` | `sinSesionNoSeEliminaUnProducto` | Infraestructura | AB#345 | PR #129 | ✅ |
| HU35-CP07 | `ProductoControllerTest` | `noVendedorNoEliminaUnProducto` (cliente y administrador) | Infraestructura | AB#345 | PR #129 | ✅ |
| HU35-CP07 | `ProductoControllerTest` | `sinTokenCsrfNoSeEliminaUnProducto` | Infraestructura | AB#345 | PR #129 | ✅ |
| HU35-CP08 | `SellerProductsPage.test.jsx` | `HU35-CP08: cada fila tiene un botón para eliminar el producto` | Frontend · página | AB#344 | PR #132 | ✅ |
| HU35-CP09 | `SellerProductsPage.test.jsx` | `HU35-CP09: al pulsar Eliminar pide confirmación sin borrar nada` | Frontend · página | AB#344 | PR #132 | ✅ |
| HU35-CP10 | `SellerProductsPage.test.jsx` | `HU35-CP10: al cancelar la eliminación, cierra la alerta sin borrar` | Frontend · página | AB#344 | PR #132 | ✅ |
| HU35-CP10 | `SellerProductsPage.test.jsx` | `HU35-CP10: al pulsar Escape en la alerta, la cierra sin borrar` | Frontend · página | AB#344 | PR #132 | ✅ |
| HU35-CP11 | `SellerProductsPage.test.jsx` | `HU35-CP11: al confirmar, pide borrar el producto indicado y avisa de que se ha eliminado` | Frontend · página | AB#344 | PR #132 | ✅ |
| HU35-CP12 | `SellerProductsPage.test.jsx` | `HU35-CP12: tras eliminar, vuelve a cargar el listado` | Frontend · página | AB#344 | PR #132 | ✅ |
| HU35-CP13 | `SellerProductsPage.test.jsx` | `HU35-CP13: si falla la eliminación, avisa sin detalles internos y mantiene la alerta` | Frontend · página | AB#344 | PR #132 | ✅ |
| HU35-CP14 | `SellerProductsPage.test.jsx` | `HU35-CP14: desde la ficha, Eliminar pide confirmación y al confirmar borra el producto y avisa` | Frontend · página | AB#344 | PR #132 | ✅ |

Rutas de las clases de test (bajo `backend/src/test/java/es/uclm/esibuy/backend/`):
- `GestionProductosServiceTest`: `productos/application/service/` (Mockito, sin Spring)
- `ProductoMongoAdapterTest`: `productos/infrastructure/adapter/out/persistence/` (Testcontainers)
- `ProductoControllerTest`: `productos/infrastructure/adapter/in/web/` (con `@WebMvcTest`)
- `SellerProductsPage.test.jsx`: `frontend/src/pages/` (Vitest, RTL y MSW)

---

## HU37 · Eliminar administrador

### Tabla A · HU37 ↔ casos de prueba

| Caso | Descripción | Tipo | Tests | Estado |
|---|---|---|---|---|
| HU37-CP01 | Eliminar un administrador activo lo borra físicamente si queda otro administrador activo | Unitario | 1 | ✅ Pasa |
| HU37-CP02 | Eliminar un usuario que no existe falla (`UsuarioNoEncontradoException`) y no borra nada | Unitario | 1 | ✅ Pasa |
| HU37-CP03 | Eliminar a un usuario que no es administrador falla como si no existiera y no borra nada | Unitario | 1 | ✅ Pasa |
| HU37-CP04 | Un administrador no puede eliminar su propia cuenta (`AutoeliminacionNoPermitidaException`) y no se borra nada | Unitario | 1 | ✅ Pasa |
| HU37-CP05 | No se puede eliminar al último administrador activo (`UltimoAdministradorActivoException`) y no se borra nada | Unitario | 1 | ✅ Pasa |
| HU37-CP06 | Eliminar un administrador desactivado no comprueba el conteo de activos y lo borra | Unitario | 1 | ✅ Pasa |
| HU37-CP07 | `DELETE /api/admin/usuarios/{id}` elimina a otro administrador y responde 204 sin cuerpo | Integración (capa web) | 1 | ✅ Pasa |
| HU37-CP08 | Eliminar un administrador que no existe devuelve 404 | Integración (capa web) | 1 | ✅ Pasa |
| HU37-CP09 | Intentar eliminarse a sí mismo devuelve 409 con el código `CANNOT_DELETE_SELF` | Integración (capa web) | 1 | ✅ Pasa |
| HU37-CP10 | Intentar eliminar al último administrador activo devuelve 409 con el código `LAST_ACTIVE_ADMIN` | Integración (capa web) | 1 | ✅ Pasa |
| HU37-CP11 | Sin sesión (401), sin ser administrador (403) o sin token CSRF (403) no se puede eliminar una cuenta | Integración (capa web) | 3 | ✅ Pasa |

Notas:
- **HU37-CP01 a CP06 (AB#340):** casos propuestos para la lógica de negocio del borrado físico de administradores en `AdministracionUsuariosService.eliminarAdministrador` (PR #139). Pendientes de confirmar con el equipo y de añadir a la HU37 en Azure Boards.
- **HU37-CP07 a CP11:** casos propuestos para el endpoint `DELETE /api/admin/usuarios/{id}` en `UsuarioController` y la traducción de `AutoeliminacionNoPermitidaException` a 409 `CANNOT_DELETE_SELF` en `ManejadorGlobalErrores`, siguiendo el contrato esperado por el frontend (ver notas de `HU19-CP13 a CP19`).
- **Sesión del administrador eliminado:** la invalidación de la sesión al borrar físicamente la cuenta ya la cubre `CuentaActivaFilter` (ver **HU23-CP10**, `usuarioInexistenteInvalidaLaSesionYDevuelve401`, PR #85): en cuanto el documento desaparece de la colección `usuarios`, cualquier petición con su sesión recibe un 401 y la sesión queda invalidada.

### Tabla B · casos de prueba ↔ tests

| Caso | Clase de test | Método | Capa | Task | Origen | Estado |
|---|---|---|---|---|---|---|
| HU37-CP01 | `AdministracionUsuariosServiceTest` | `eliminaAUnAdministradorSiQuedaOtroActivo` | Aplicación | AB#340 | PR #139 | ✅ |
| HU37-CP02 | `AdministracionUsuariosServiceTest` | `rechazaEliminarUnUsuarioQueNoExiste` | Aplicación | AB#340 | PR #139 | ✅ |
| HU37-CP03 | `AdministracionUsuariosServiceTest` | `rechazaEliminarAUsuarioQueNoEsAdministrador` | Aplicación | AB#340 | PR #139 | ✅ |
| HU37-CP04 | `AdministracionUsuariosServiceTest` | `rechazaQueUnAdministradorSeElimineASiMismo` | Aplicación | AB#340 | PR #139 | ✅ |
| HU37-CP05 | `AdministracionUsuariosServiceTest` | `rechazaEliminarAlUltimoAdministradorActivo` | Aplicación | AB#340 | PR #139 | ✅ |
| HU37-CP06 | `AdministracionUsuariosServiceTest` | `eliminaAUnAdministradorDesactivadoSinComprobarActivos` | Aplicación | AB#340 | PR #139 | ✅ |
| HU37-CP07 | `UsuarioControllerTest` | `administradorEliminaAOtroAdministrador` | Infraestructura | AB#340 | PR actual | ✅ |
| HU37-CP08 | `UsuarioControllerTest` | `eliminarAdministradorInexistenteDevuelve404` | Infraestructura | AB#340 | PR actual | ✅ |
| HU37-CP09 | `UsuarioControllerTest` | `eliminarseASiMismoDevuelve409` | Infraestructura | AB#340 | PR actual | ✅ |
| HU37-CP10 | `UsuarioControllerTest` | `eliminarAlUltimoAdministradorActivoDevuelve409` | Infraestructura | AB#340 | PR actual | ✅ |
| HU37-CP11 | `UsuarioControllerTest` | `unNoAdministradorNoPuedeEliminarCuentas` (cliente y vendedor) | Infraestructura | AB#340 | PR actual | ✅ |
| HU37-CP11 | `UsuarioControllerTest` | `eliminarSinSesionEstaProtegido` | Infraestructura | AB#340 | PR actual | ✅ |
| HU37-CP11 | `UsuarioControllerTest` | `eliminarSinTokenCsrfSeRechaza` | Infraestructura | AB#340 | PR actual | ✅ |

Rutas de las clases de test (bajo `backend/src/test/java/es/uclm/esibuy/backend/`):
- `AdministracionUsuariosServiceTest`: `usuarios/application/service/` (Mockito, sin Spring)
- `UsuarioControllerTest`: `usuarios/infrastructure/adapter/in/web/` (con `@WebMvcTest` y `@WithMockUser`)

---

## HU47 · Eliminar mi cuenta de vendedor

### Tabla A · HU47 ↔ casos de prueba

| Caso | Descripción | Tipo | Tests | Estado |
|---|---|---|---|---|
| HU47-CP01 | Con la contraseña actual incorrecta no se elimina la cuenta ni se modifica ningún dato | Unitario (aplicación) | 1 | 🔍 En revisión |
| HU47-CP02 | Al eliminar la cuenta se conservan como no disponibles los productos con pedidos y se eliminan los demás junto con sus valoraciones | Unitario + integración (aplicación y persistencia) | 10 | 🔍 En revisión |
| HU47-CP03 | Un producto no disponible no se puede comprar aunque tenga stock | Integración (persistencia) | 1 | 🔍 En revisión |
| HU47-CP04 | Si una eliminación quedó a medias se puede repetir y completarse sin errores | Unitario (aplicación) | 1 | 🔍 En revisión |
| HU47-CP05 | Al eliminar la cuenta se invalida la sesión y se limpia el contexto de seguridad | Unitario + integración (capa de aplicación y web) | 2 | 🔍 En revisión |
| HU47-CP06 | Un cliente recibe 403 y no se modifica nada al intentar eliminar la cuenta | Unitario + integración (capa de aplicación y web) | 2 | 🔍 En revisión |
| HU47-CP07 | Una petición sin autenticar recibe 401 y no se modifica nada al intentar eliminar la cuenta | Integración (capa web) | 1 | 🔍 En revisión |

Notas:
- `DELETE /api/perfil` se restringe a VENDEDOR en la regla de ruta; `GET /api/perfil` sigue disponible para CLIENTE y VENDEDOR. Las pruebas web usan la cadena real de `SecurityConfig`, con CSRF activo.
- La consulta interna se denomina `buscarPorVendedor`, no `buscarPagadosPorVendedor`, y considera todos los pedidos del vendedor. Se conservan los productos incluidos en cualquier pedido para no romper el historial si el estado del pedido cambia.
- El `false` de descuento de existencias puede deberse a stock insuficiente o a producto no disponible; la respuesta 409 mantiene el código `INSUFFICIENT_STOCK` por compatibilidad y usa un mensaje neutral: «No se puede comprar un producto del pedido».
- La eliminación de la cuenta cruza módulos mediante puertos de salida propios de `usuarios` y adaptadores en `usuarios/infrastructure/adapter/out/`, que delegan en los puertos de entrada de productos, pedidos y valoraciones.

### Tabla B · casos de prueba ↔ tests

| Caso | Clase de test | Método | Capa | Task | Origen | Estado |
|---|---|---|---|---|---|---|
| HU47-CP01 | `GestionCuentaServiceTest` | `contraseñaIncorrectaNoModificaNada` | Aplicación | AB#312 / AB#313 | Rama `feature/US47-T312-T313-eliminar-mi-cuenta-vendedor` (PR pendiente) | 🔍 |
| HU47-CP02 | `GestionCuentaServiceTest` | `conservaProductosConPedidosYEliminaElResto` | Aplicación | AB#312 / AB#313 | Rama `feature/US47-T312-T313-eliminar-mi-cuenta-vendedor` (PR pendiente) | 🔍 |
| HU47-CP02 | `GestionProductosVendedorServiceTest` | `desactivaTodosLosProductosDelVendedor` | Aplicación | AB#312 / AB#313 | Rama `feature/US47-T312-T313-eliminar-mi-cuenta-vendedor` (PR pendiente) | 🔍 |
| HU47-CP02 | `ProductosConPedidosServiceTest` | `devuelveLosProductosDePedidosDelVendedor` | Aplicación | AB#312 / AB#313 | Rama `feature/US47-T312-T313-eliminar-mi-cuenta-vendedor` (PR pendiente) | 🔍 |
| HU47-CP02 | `LimpiezaValoracionesServiceTest` | `eliminaLasValoracionesDelProductoEliminado` | Aplicación | AB#312 / AB#313 | Rama `feature/US47-T312-T313-eliminar-mi-cuenta-vendedor` (PR pendiente) | 🔍 |
| HU47-CP02 | `ProductoMongoAdapterTest` | `listarTodosPorVendedorDevuelveSoloLosDelVendedor` | Infraestructura | AB#312 / AB#313 | Rama `feature/US47-T312-T313-eliminar-mi-cuenta-vendedor` (PR pendiente) | 🔍 |
| HU47-CP02 | `PedidoMongoAdapterTest` | `buscarPorVendedorDevuelveSoloLosDelVendedor` | Infraestructura | AB#312 / AB#313 | Rama `feature/US47-T312-T313-eliminar-mi-cuenta-vendedor` (PR pendiente) | 🔍 |
| HU47-CP02 | `FactorAutenticacionMongoAdapterTest` | `eliminarPorUsuarioBorraElFactor` | Infraestructura | AB#312 / AB#313 | Rama `feature/US47-T312-T313-eliminar-mi-cuenta-vendedor` (PR pendiente) | 🔍 |
| HU47-CP02 | `CodigoVerificacionMongoAdapterTest` | `eliminarPorUsuarioBorraSoloElCodigoDeLogin` | Infraestructura | AB#312 / AB#313 | Rama `feature/US47-T312-T313-eliminar-mi-cuenta-vendedor` (PR pendiente) | 🔍 |
| HU47-CP02 | `CodigoRecuperacionMongoAdapterTest` | `eliminarPorUsuarioBorraSoloElCodigoDeRecuperacion` | Infraestructura | AB#312 / AB#313 | Rama `feature/US47-T312-T313-eliminar-mi-cuenta-vendedor` (PR pendiente) | 🔍 |
| HU47-CP02 | `ValoracionMongoAdapterTest` | `eliminarPorProductoBorraSoloLasValoracionesDelProducto` | Infraestructura | AB#312 / AB#313 | Rama `feature/US47-T312-T313-eliminar-mi-cuenta-vendedor` (PR pendiente) | 🔍 |
| HU47-CP03 | `ProductoMongoAdapterTest` | `productoNoDisponibleNoSePuedeComprarAunqueTengaStock` | Infraestructura | AB#312 / AB#313 | Rama `feature/US47-T312-T313-eliminar-mi-cuenta-vendedor` (PR pendiente) | 🔍 |
| HU47-CP04 | `GestionCuentaServiceTest` | `reintentaTrasUnBorradoParcial` | Aplicación | AB#312 / AB#313 | Rama `feature/US47-T312-T313-eliminar-mi-cuenta-vendedor` (PR pendiente) | 🔍 |
| HU47-CP05 | `SeguridadGestionCuentaServiceTest` | `unVendedorPuedeInvocarSuEliminacion` | Aplicación | AB#312 / AB#313 | Rama `feature/US47-T312-T313-eliminar-mi-cuenta-vendedor` (PR pendiente) | 🔍 |
| HU47-CP05 | `PerfilControllerTest` | `eliminarCuentaInvalidaSesionYLimpiaContexto` | Infraestructura | AB#312 / AB#313 | Rama `feature/US47-T312-T313-eliminar-mi-cuenta-vendedor` (PR pendiente) | 🔍 |
| HU47-CP06 | `SeguridadGestionCuentaServiceTest` | `unClienteNoPuedeInvocarLaEliminacion` | Aplicación | AB#312 / AB#313 | Rama `feature/US47-T312-T313-eliminar-mi-cuenta-vendedor` (PR pendiente) | 🔍 |
| HU47-CP06 | `PerfilControllerTest` | `usuarioNoVendedorNoPuedeEliminarCuenta` | Infraestructura | AB#312 / AB#313 | Rama `feature/US47-T312-T313-eliminar-mi-cuenta-vendedor` (PR pendiente) | 🔍 |
| HU47-CP07 | `PerfilControllerTest` | `sinAutenticarNoPuedeEliminarCuenta` | Infraestructura | AB#312 / AB#313 | Rama `feature/US47-T312-T313-eliminar-mi-cuenta-vendedor` (PR pendiente) | 🔍 |

Rutas de las clases de test (bajo `backend/src/test/java/es/uclm/esibuy/backend/`):
- `GestionCuentaServiceTest`: `usuarios/application/service/`
- `GestionProductosVendedorServiceTest`: `productos/application/service/`
- `ProductosConPedidosServiceTest`: `pedidos/application/service/`
- `LimpiezaValoracionesServiceTest`: `valoraciones/application/service/`
- `ProductoMongoAdapterTest`: `productos/infrastructure/adapter/out/persistence/`
- `PerfilControllerTest`: `usuarios/infrastructure/adapter/in/web/`

---

## HU39 · Consultar categorías

### Tabla A · HU39 ↔ casos de prueba

| Caso | Descripción | Tipo | Tests | Estado |
|---|---|---|---|---|
| HU39-CP01 | El administrador obtiene cada categoría con su nombre, descripción, estado y número de productos (`GET /api/admin/categorias`) | Unitario + controlador | 2 | ✅ Pasa |
| HU39-CP02 | El número de productos se cuenta por categoría en el módulo de productos (una sola agregación); una categoría sin productos tiene 0 | Unitario + integración MongoDB | 5 | ✅ Pasa |
| HU39-CP03 | El administrador también ve las categorías `INACTIVA` | Unitario | 1 | ✅ Pasa |
| HU39-CP04 | Sin rol de administrador el listado devuelve 403 (cliente y vendedor), y 401 sin sesión | Controlador | 2 | ✅ Pasa |
| HU39-CP05 | El listado público de categorías (`GET /api/categorias`) solo devuelve las activas | Unitario | 1 | 🔍 En revisión (1, rama `fix/seguridad-capa-negocio`) |
| HU39-CP06 | La llamada del administrador pide `GET /api/admin/categorias` y devuelve el array de categorías; un 403 llega como error con su estado y su código | Frontend (API) | 2 | ✅ Pasa |
| HU39-CP07 | El estado de cada categoría se muestra como Activa (color de éxito) o Inactiva (neutro); un estado desconocido, como Desconocido | Frontend (unitario y componente) | 5 | ✅ Pasa |
| HU39-CP08 | Filtrar las categorías por nombre (sin distinguir mayúsculas ni tildes) y por estado, y ordenarlas por nombre, productos o estado, en el cliente | Frontend (unitario) | 10 | ✅ Pasa |
| HU39-CP09 | La tabla de categorías muestra de cada una su nombre, descripción, estado y número de productos, con una columna de acciones | Frontend (componente y página) | 6 | ✅ Pasa |
| HU39-CP10 | Una descripción vacía se muestra como un guion y una categoría sin productos muestra 0 | Frontend (componente y página) | 2 | ✅ Pasa |
| HU39-CP11 | La lupa de cada fila abre la ficha de solo lectura con los datos de la categoría y su descripción completa | Frontend (componente y página) | 8 | ✅ Pasa |
| HU39-CP12 | La vista muestra un indicador mientras carga, todas las categorías (también las inactivas) ordenadas por nombre y un estado vacío si no hay ninguna | Frontend (página) | 3 | ✅ Pasa |
| HU39-CP13 | Un 403 o un error del servidor muestran el mismo mensaje genérico con «Reintentar», sin el texto del servidor | Frontend (página) | 2 | ✅ Pasa |
| HU39-CP14 | La vista permite buscar por nombre, filtrar por estado y ordenar; sin coincidencias avisa y «Limpiar filtros» mantiene el orden | Frontend (página) | 4 | ✅ Pasa |
| HU39-CP15 | El panel de control tiene la tarjeta Categorías, que lleva al listado, y «Volver al panel» regresa al panel | Frontend (página y rutas) | 3 | ✅ Pasa |
| HU39-CP16 | El listado de categorías es solo del administrador: sin sesión va al login y un vendedor o un cliente van a su zona | Frontend (rutas) | 3 | ✅ Pasa |

Notas:
- **HU39-CP01 a CP03:** casos propuestos en la T332 (AB#332), a partir del criterio de aceptación «listado con nombre, descripción, estado y número de productos asociados». **HU39-CP04:** propuesto en la T331 (AB#331), a partir del criterio «cualquier llamada a la API sin rol de administrador devuelve 403». Pendientes de confirmar con el equipo y de añadir a la HU39 en Azure Boards.
- **Endpoint (T331):** `GET /api/admin/categorias` → `[{id, nombre, descripcion, estado, numeroProductos}]`, sin paginar y ordenado por nombre, con `estado` como texto (`ACTIVA` / `INACTIVA`). Lo atiende `AdministracionCategoriasController`; el 401 y el 403 los da la regla `/api/admin/**` de `SecurityConfig`. Es el contrato de la vista del frontend (T330).
- **Backend (T332):** `AdministracionCategoriasUseCase.listarConProductos()` devuelve `CategoriaConProductos`. Categorías pide el recuento a productos con `ProductosPorCategoriaPort` → `ProductosPorCategoriaAdapter` → `ConteoProductosUseCase` (puerto de entrada de productos). El recuento incluye todos los productos de la categoría, también los no disponibles. `GET /api/categorias` no cambia.
- **Pendiente:** el nombre único sin distinguir mayúsculas (T323).
- **HU39-CP05 (R8, seguridad en la capa de negocio):** caso propuesto en la rama `fix/seguridad-capa-negocio`, sin task ni `AB#` por ser un fix transversal. Pendiente de confirmar con el equipo. Tapa un hueco de seguridad: el listado público de categorías devolvía también las inactivas, que solo debe ver el administrador (HU39-CP03). Se trazó al revisar la rama, porque el test ya estaba en el código.
- **HU39-CP06 a CP16 (frontend, T330):** casos propuestos en la T330 (AB#330), a partir de los criterios de aceptación de la HU39 («opción de categorías en el panel de administrador», «listado con nombre, descripción, estado y número de productos» y «cualquier llamada a la API sin rol de administrador devuelve 403», que la vista trata como un error genérico), y desarrollados con TDD. Pendientes de confirmar con el equipo y de añadir a la HU39 en Azure Boards. Rama `feature/US39-T330-vista-consulta-categorias`.
- **Vista (T330):** `/admin/categories`, solo del administrador, con la tarjeta Categorías del panel. Lee `GET /api/admin/categorias`, que devuelve todas las categorías sin paginar y ordenadas por nombre, y por eso la búsqueda por nombre, el filtro por estado y la ordenación (nombre, productos y estado) se hacen en el cliente y no hay paginación. La lupa de cada fila abre una ficha de solo lectura con los datos de la propia fila (no hay endpoint de detalle de categoría): era la interpretación del criterio «el botón de consultar debe estar en la tabla». Sin botones de crear, editar ni eliminar, que son de las HU40, HU41 y HU42.
- **Fuera de alcance de la vista:** el criterio «nombre obligatorio y único sin distinguir mayúsculas» lo valida el backend (HU40); aquí solo se muestra el nombre.

### Tabla B · casos de prueba ↔ tests

| Caso | Clase de test | Método | Capa | Task | Origen | Estado |
|---|---|---|---|---|---|---|
| HU39-CP01 | `AdministracionCategoriasServiceTest` | `listarConProductosDevuelveCadaCategoriaConSuNumero` | Aplicación | AB#332 | PR #134 | ✅ |
| HU39-CP01 | `AdministracionCategoriasControllerTest` | `unAdministradorRecibeLasCategoriasConSuNumeroDeProductos` | Infraestructura | AB#331 | PR #135 | ✅ |
| HU39-CP02 | `AdministracionCategoriasServiceTest` | `unaCategoriaSinProductosTieneCero` | Aplicación | AB#332 | PR #134 | ✅ |
| HU39-CP02 | `ConteoProductosServiceTest` | `contarPorCategoriaDelegaEnElRepositorio` | Aplicación | AB#332 | PR #134 | ✅ |
| HU39-CP02 | `ProductosPorCategoriaAdapterTest` | `contarPorCategoriaPreguntaAlModuloDeProductos` | Infraestructura | AB#332 | PR #134 | ✅ |
| HU39-CP02 | `ProductoMongoAdapterTest` | `contarPorCategoriaAgrupaLosProductos` | Infraestructura | AB#332 | PR #134 | ✅ |
| HU39-CP02 | `ProductoMongoAdapterTest` | `contarPorCategoriaSinProductosDevuelveVacio` | Infraestructura | AB#332 | PR #134 | ✅ |
| HU39-CP03 | `AdministracionCategoriasServiceTest` | `incluyeLasCategoriasInactivas` | Aplicación | AB#332 | PR #134 | ✅ |
| HU39-CP04 | `AdministracionCategoriasControllerTest` | `otroRolRecibe403` (cliente y vendedor) | Infraestructura | AB#331 | PR #135 | ✅ |
| HU39-CP04 | `AdministracionCategoriasControllerTest` | `sinSesionRecibe401` | Infraestructura | AB#331 | PR #135 | ✅ |
| HU39-CP05 | `ConsultaCategoriasServiceTest` | `listarSoloDevuelveLasActivas` | Aplicación | — | Rama `fix/seguridad-capa-negocio` | 🔍 |
| HU39-CP06 | `adminCategories.test.js` | `HU39-CP06: pide GET /api/admin/categorias y devuelve el array de categorías` | Frontend · API | AB#330 | PR #197 | ✅ |
| HU39-CP06 | `adminCategories.test.js` | `HU39-CP06: un 403 llega como error con su estado y su código` | Frontend · API | AB#330 | PR #197 | ✅ |
| HU39-CP07 | `categoryStatus.test.js` | `HU39-CP07: una categoría ACTIVA se muestra como Activa, con color de éxito` | Frontend · unitario | AB#330 | PR #197 | ✅ |
| HU39-CP07 | `categoryStatus.test.js` | `HU39-CP07: una categoría INACTIVA se muestra como Inactiva, con color neutro` | Frontend · unitario | AB#330 | PR #197 | ✅ |
| HU39-CP07 | `categoryStatus.test.js` | `HU39-CP07: un estado desconocido se muestra como Desconocido` | Frontend · unitario | AB#330 | PR #197 | ✅ |
| HU39-CP07 | `categoryStatus.test.js` | `HU39-CP07: las etiquetas del filtro son las de los dos estados` | Frontend · unitario | AB#330 | PR #197 | ✅ |
| HU39-CP07 | `CategoriesTable.test.jsx` | `HU39-CP07: el estado de cada categoría se muestra en una insignia` | Frontend · componente | AB#330 | PR #197 | ✅ |
| HU39-CP08 | `categoriesList.test.js` | `HU39-CP08: sin filtros devuelve todas las categorías, también las inactivas, ordenadas por nombre` | Frontend · unitario | AB#330 | PR #197 | ✅ |
| HU39-CP08 | `categoriesList.test.js` | `HU39-CP08: la búsqueda por nombre no distingue mayúsculas ni tildes` | Frontend · unitario | AB#330 | PR #197 | ✅ |
| HU39-CP08 | `categoriesList.test.js` | `HU39-CP08: la búsqueda encuentra el texto en cualquier parte del nombre y solo mira el nombre` | Frontend · unitario | AB#330 | PR #197 | ✅ |
| HU39-CP08 | `categoriesList.test.js` | `HU39-CP08: el filtro por estado deja solo las activas o solo las inactivas` | Frontend · unitario | AB#330 | PR #197 | ✅ |
| HU39-CP08 | `categoriesList.test.js` | `HU39-CP08: la búsqueda y el estado se combinan` | Frontend · unitario | AB#330 | PR #197 | ✅ |
| HU39-CP08 | `categoriesList.test.js` | `HU39-CP08: ordena por nombre en sentido descendente` | Frontend · unitario | AB#330 | PR #197 | ✅ |
| HU39-CP08 | `categoriesList.test.js` | `HU39-CP08: ordena por número de productos, de menos a más y al revés` | Frontend · unitario | AB#330 | PR #197 | ✅ |
| HU39-CP08 | `categoriesList.test.js` | `HU39-CP08: ordena por estado, las activas primero, y a igualdad desempata por nombre` | Frontend · unitario | AB#330 | PR #197 | ✅ |
| HU39-CP08 | `categoriesList.test.js` | `HU39-CP08: no modifica la lista que recibe` | Frontend · unitario | AB#330 | PR #197 | ✅ |
| HU39-CP08 | `categoriesList.test.js` | `HU39-CP08: ofrece ordenar por nombre, productos y estado` | Frontend · unitario | AB#330 | PR #197 | ✅ |
| HU39-CP09 | `CategoriesTable.test.jsx` | `HU39-CP09: muestra las cabeceras de las cuatro columnas` | Frontend · componente | AB#330 | PR #197 | ✅ |
| HU39-CP09 | `CategoriesTable.test.jsx` | `HU39-CP09: muestra de cada categoría su nombre, descripción, estado y número de productos` | Frontend · componente | AB#330 | PR #197 | ✅ |
| HU39-CP09 | `CategoriesTable.test.jsx` | `HU39-CP09: con renderActions añade la columna Acciones con lo que devuelva para cada categoría` | Frontend · componente | AB#330 | PR #197 | ✅ |
| HU39-CP09 | `CategoriesTable.test.jsx` | `HU39-CP09: mientras se refresca, la tabla avisa de que está ocupada` | Frontend · componente | AB#330 | PR #197 | ✅ |
| HU39-CP09 | `AdminCategoriesPage.test.jsx` | `HU39-CP09: la tabla tiene las columnas Nombre, Descripción, Estado, Productos y Acciones` | Frontend · página | AB#330 | PR #197 | ✅ |
| HU39-CP09 | `AdminCategoriesPage.test.jsx` | `HU39-CP09: cada fila muestra el nombre, la descripción, el estado y el número de productos` | Frontend · página | AB#330 | PR #197 | ✅ |
| HU39-CP10 | `CategoriesTable.test.jsx` | `HU39-CP10: una descripción vacía se muestra como un guion` | Frontend · componente | AB#330 | PR #197 | ✅ |
| HU39-CP10 | `AdminCategoriesPage.test.jsx` | `HU39-CP10: una categoría sin descripción muestra un guion y una sin productos muestra 0` | Frontend · página | AB#330 | PR #197 | ✅ |
| HU39-CP11 | `CategoryDetailDialog.test.jsx` | `HU39-CP11: muestra el nombre, el estado, el número de productos y la descripción completa` | Frontend · componente | AB#330 | PR #197 | ✅ |
| HU39-CP11 | `CategoryDetailDialog.test.jsx` | `HU39-CP11: una categoría inactiva sin descripción ni productos muestra Inactiva, 0 y un guion` | Frontend · componente | AB#330 | PR #197 | ✅ |
| HU39-CP11 | `CategoryDetailDialog.test.jsx` | `HU39-CP11: todos los campos son de solo lectura y la ficha avisa de ello` | Frontend · componente | AB#330 | PR #197 | ✅ |
| HU39-CP11 | `CategoryDetailDialog.test.jsx` | `HU39-CP11: la ficha no ofrece editar, crear ni eliminar` | Frontend · componente | AB#330 | PR #197 | ✅ |
| HU39-CP11 | `CategoryDetailDialog.test.jsx` | `HU39-CP11: se cierra con la X` | Frontend · componente | AB#330 | PR #197 | ✅ |
| HU39-CP11 | `CategoryDetailDialog.test.jsx` | `HU39-CP11: se cierra con Escape` | Frontend · componente | AB#330 | PR #197 | ✅ |
| HU39-CP11 | `AdminCategoriesPage.test.jsx` | `HU39-CP11: cada fila tiene un botón con lupa para ver la información de su categoría` | Frontend · página | AB#330 | PR #197 | ✅ |
| HU39-CP11 | `AdminCategoriesPage.test.jsx` | `HU39-CP11: la lupa abre la ficha de esa categoría con su descripción completa y se puede cerrar` | Frontend · página | AB#330 | PR #197 | ✅ |
| HU39-CP12 | `AdminCategoriesPage.test.jsx` | `HU39-CP12: muestra el título de la vista y mientras carga un indicador` | Frontend · página | AB#330 | PR #197 | ✅ |
| HU39-CP12 | `AdminCategoriesPage.test.jsx` | `HU39-CP12: muestra todas las categorías, también las inactivas, ordenadas por nombre` | Frontend · página | AB#330 | PR #197 | ✅ |
| HU39-CP12 | `AdminCategoriesPage.test.jsx` | `HU39-CP12: sin categorías muestra el estado vacío y ningún filtro` | Frontend · página | AB#330 | PR #197 | ✅ |
| HU39-CP13 | `AdminCategoriesPage.test.jsx` | `HU39-CP13: un 403 muestra el error genérico, sin el texto del servidor, y se puede reintentar` | Frontend · página | AB#330 | PR #197 | ✅ |
| HU39-CP13 | `AdminCategoriesPage.test.jsx` | `HU39-CP13: un error del servidor muestra el mismo mensaje genérico con Reintentar` | Frontend · página | AB#330 | PR #197 | ✅ |
| HU39-CP14 | `AdminCategoriesPage.test.jsx` | `HU39-CP14: buscar por nombre no distingue mayúsculas ni tildes` | Frontend · página | AB#330 | PR #197 | ✅ |
| HU39-CP14 | `AdminCategoriesPage.test.jsx` | `HU39-CP14: filtrar por estado deja solo las inactivas o solo las activas` | Frontend · página | AB#330 | PR #197 | ✅ |
| HU39-CP14 | `AdminCategoriesPage.test.jsx` | `HU39-CP14: ordenar por productos y cambiar el sentido` | Frontend · página | AB#330 | PR #197 | ✅ |
| HU39-CP14 | `AdminCategoriesPage.test.jsx` | `HU39-CP14: sin coincidencias avisa y Limpiar filtros vuelve a mostrarlas todas, manteniendo el orden` | Frontend · página | AB#330 | PR #197 | ✅ |
| HU39-CP15 | `AdminCategoriesPage.test.jsx` | `HU39-CP15: Volver al panel regresa al panel de control` | Frontend · página | AB#330 | PR #197 | ✅ |
| HU39-CP15 | `AdminPage.test.jsx` | `HU39-CP15: el panel tiene la tarjeta Categorías, con su descripción` | Frontend · página | AB#330 | PR #197 | ✅ |
| HU39-CP15 | `AdminPage.test.jsx` | `HU39-CP15: la tarjeta Categorías lleva al listado de categorías` | Frontend · página | AB#330 | PR #197 | ✅ |
| HU39-CP16 | `AdminCategoriesPage.test.jsx` | `HU39-CP16: sin sesión redirige al login` | Frontend · página | AB#330 | PR #197 | ✅ |
| HU39-CP16 | `AdminCategoriesPage.test.jsx` | `HU39-CP16: un vendedor no ve el listado y va a su zona` | Frontend · página | AB#330 | PR #197 | ✅ |
| HU39-CP16 | `AdminCategoriesPage.test.jsx` | `HU39-CP16: un cliente no ve el listado y va a su zona` | Frontend · página | AB#330 | PR #197 | ✅ |

Rutas de las clases de test (bajo `backend/src/test/java/es/uclm/esibuy/backend/`):
- `AdministracionCategoriasControllerTest`: `categorias/infrastructure/adapter/in/web/` (con `@WebMvcTest` y la seguridad real)
- `AdministracionCategoriasServiceTest`: `categorias/application/service/` (JUnit y Mockito, sin Spring)
- `ProductosPorCategoriaAdapterTest`: `categorias/infrastructure/adapter/out/productos/` (JUnit y Mockito, sin Spring)
- `ConteoProductosServiceTest`: `productos/application/service/` (JUnit y Mockito, sin Spring)
- `ProductoMongoAdapterTest`: `productos/infrastructure/adapter/out/persistence/` (con Testcontainers)
- `adminCategories.test.js`: `frontend/src/api/` (Vitest y MSW)
- `categoryStatus.test.js`, `categoriesList.test.js`, `CategoriesTable.test.jsx` y `CategoryDetailDialog.test.jsx`: `frontend/src/features/admin/` (Vitest y React Testing Library)
- `AdminCategoriesPage.test.jsx` y `AdminPage.test.jsx`: `frontend/src/pages/` (Vitest, React Testing Library y MSW)

---

## HU40 · Crear categorías

### Tabla A · HU40 ↔ casos de prueba

| Caso | Descripción | Tipo | Tests | Estado |
|---|---|---|---|---|
| HU40-CP01 | Una categoría se crea con nombre, descripción y estado; una nueva nace ACTIVA y sin id | Unitario (dominio) | 2 | ✅ Pasa |
| HU40-CP02 | El nombre es obligatorio, se guarda sin espacios en los extremos y admite hasta 50 caracteres | Unitario (dominio) | 2 | ✅ Pasa |
| HU40-CP03 | La descripción es opcional y admite hasta 200 caracteres | Unitario (dominio) | 1 | ✅ Pasa |
| HU40-CP04 | El estado es obligatorio; la descripción y el estado se guardan en MongoDB, y una categoría guardada sin ellos se lee ACTIVA y sin descripción | Unitario + integración MongoDB | 5 | ✅ Pasa |
| HU40-CP05 | El administrador crea una categoría con el estado que elige (ACTIVA o INACTIVA); se guarda con un id nuevo y `POST /api/admin/categorias` responde 201 con 0 productos | Unitario + integración MongoDB + controlador | 3 | ✅ Pasa |
| HU40-CP06 | No se puede crear una categoría con un nombre que ya existe, sin distinguir mayúsculas ni tildes; el nombre se compara completo y literal, y la base de datos lo impide aunque se salte la comprobación previa (409) | Unitario + integración MongoDB + controlador | 5 | ✅ Pasa |
| HU40-CP07 | Crear sin nombre, sin estado, con un estado desconocido o incumpliendo una regla del dominio devuelve 400 | Controlador | 2 | ✅ Pasa |
| HU40-CP08 | Sin rol de administrador no se pueden crear categorías: 403 (cliente y vendedor) y 401 sin sesión | Controlador | 2 | ✅ Pasa |
| HU40-CP09 | El listado público de categorías se puede usar sin sesión; administrarlas (listar con su número de productos y crear) exige el rol administrador; sin sesión se rechaza. Seguridad en la capa de negocio (R8) | Unitario con Spring Security (sin MongoDB) | 6 | 🔍 En revisión (6, rama `fix/seguridad-capa-negocio`) |
| HU40-CP10 | La llamada del administrador pide `POST /api/admin/categorias` con nombre, descripción y estado y devuelve la categoría creada; un 409 llega como error con su estado y su código | Frontend (API) | 2 | ✅ Pasa (2, AB#320, PR #201) |
| HU40-CP11 | La vista tiene el botón «Nueva categoría» (también sin ninguna categoría), que abre el formulario con nombre, descripción y estado (activa por defecto); cancelar no crea nada | Frontend (componente y página) | 4 | ✅ Pasa (4, AB#320, PR #201) |
| HU40-CP12 | El nombre es obligatorio (vacío o con solo espacios no se envía) y admite como mucho 50 caracteres; la descripción, 200 | Frontend (componente) | 2 | ✅ Pasa (2, AB#320, PR #201) |
| HU40-CP13 | Al crear se envían los datos elegidos, se avisa de que se ha creado y la categoría aparece en la lista con su estado; mientras se crea, los botones quedan deshabilitados | Frontend (componente y página) | 3 | ✅ Pasa (3, AB#320, PR #201) |
| HU40-CP14 | Un nombre repetido (aunque cambien las mayúsculas o las tildes) se marca en el campo y el aviso desaparece al cambiarlo; cualquier otro error muestra un mensaje genérico y el formulario sigue abierto | Frontend (componente y página) | 3 | ✅ Pasa (3, AB#320, PR #201) |

Notas:
- **HU40-CP01 a CP04:** casos propuestos en la T321 (AB#321), sin contrato del frontend todavía. **HU40-CP05 y CP06:** propuestos en la T323 (AB#323), a partir de los criterios «la categoría no puede estar repetida» y «se debe poder elegir si activar o desactivar la categoría en la creación». **HU40-CP07 y CP08:** propuestos en la T322 (AB#322). Pendientes de confirmar con el equipo y de añadir a la HU40 en Azure Boards.
- **Endpoint (T322):** `POST /api/admin/categorias` con `{nombre, descripcion, estado: "ACTIVA"|"INACTIVA"}` → 201 `{id, nombre, descripcion, estado, numeroProductos: 0}`, el mismo formato que una fila de `GET /api/admin/categorias`. Errores: 400 `VALIDATION_ERROR` (falta el nombre o el estado, el estado es desconocido o se incumple una regla del dominio, p. ej. más de 50 caracteres), 409 `CATEGORY_NAME_ALREADY_EXISTS`, 401 sin sesión y 403 con otro rol. Necesita la cabecera CSRF `X-XSRF-TOKEN`. Es el contrato de la vista del frontend (T320).
- **Backend (T323):** `AdministracionCategoriasUseCase.crear(CrearCategoriaCommand)`. El nombre único no distingue mayúsculas ni tildes (decisión del equipo, 27/10/2026): comprobación previa (`existePorNombre`) y un índice único nuevo, `nombre_sin_mayusculas` (collation `es` de nivel 1), que se crea junto al anterior para no chocar con él en Atlas. Errores: `CategoriaInvalidaException` → 400 `VALIDATION_ERROR`; `CategoriaDuplicadaException` → 409 `CATEGORY_NAME_ALREADY_EXISTS`.
- **Backend (T321):** entidad `Categoria` (`id`, `nombre`, `descripcion`, `estado`), `EstadoCategoria` (`ACTIVA`, `INACTIVA`), `CategoriaInvalidaException` y `CategoriaDocumentMapper`. El validador de la colección `categorias` aplica los mismos límites. `GET /api/categorias` no cambia (sigue devolviendo `id` y `nombre`).
- **Pendiente:** si una categoría `INACTIVA` debe dejar de verse para clientes y vendedores en `GET /api/categorias` (criterio «si la categoría se crea activada, debe ser visible para clientes y vendedores») queda pendiente de revisar con el equipo.
- **HU40-CP09 (R8, seguridad en la capa de negocio):** caso propuesto en la rama `fix/seguridad-capa-negocio`, sin task ni `AB#` por ser un fix transversal. Pendiente de confirmar con el equipo. Cubre con `@PreAuthorize` los servicios `AdministracionCategoriasService` y `ConsultaCategoriasService`, que sirven también a las HU39 y HU40. Clase de test nueva `categorias/application/service/SeguridadCategoriasServiceTest` (contexto mínimo de Spring con `@EnableMethodSecurity`, sin MongoDB).
- **HU40-CP10 a CP14 (frontend, T320):** casos propuestos en la T320 (AB#320), a partir de los criterios «el botón de creación debe estar en la vista de la tabla», «la categoría no puede estar repetida», «al crear la categoría debe ser visible en la lista» y «se debe poder elegir si activar o desactivar la categoría en la creación». Pendientes de confirmar con el equipo y de añadir a la HU40 en Azure Boards.
- **Vista (T320):** botón «Nueva categoría» en la cabecera de `/admin/categories`, que abre `CategoryFormDialog` (el mismo formulario que la edición de la HU41). El estado se elige con dos opciones, Activa (por defecto) e Inactiva. Los límites (50 y 200) se aplican con `maxLength`, como en el alta de administrador. Al crear, la página avisa («Jardín se ha creado.») y vuelve a pedir el listado. El mock (`adminCategoriesHandlers`) simula el `POST`: 201, 400 sin nombre y 409 si el nombre se repite sin distinguir mayúsculas ni tildes.
- **HU40-CP11 y CP12:** el test «al cancelar se cierra sin crear nada» y la variante del nombre «con solo espacios» no tuvieron rojo propio: cancelar ya funcionaba desde el ciclo 2 y la comprobación con `trim()` cubre las dos variantes.

### Tabla B · casos de prueba ↔ tests

| Caso | Clase de test | Método | Capa | Task | Origen | Estado |
|---|---|---|---|---|---|---|
| HU40-CP01 | `CategoriaTest` | `creaUnaCategoriaConDatosCorrectos` | Dominio | AB#321 | PR #133 | ✅ |
| HU40-CP01 | `CategoriaTest` | `unaCategoriaNuevaNaceActivaYSinId` | Dominio | AB#321 | PR #133 | ✅ |
| HU40-CP02 | `CategoriaTest` | `rechazaUnaCategoriaSinNombre` (nulo, vacío y en blanco) | Dominio | AB#321 | PR #133 | ✅ |
| HU40-CP02 | `CategoriaTest` | `elNombreSeRecortaYAdmiteHasta50Caracteres` | Dominio | AB#321 | PR #133 | ✅ |
| HU40-CP03 | `CategoriaTest` | `laDescripcionEsOpcionalYAdmiteHasta200Caracteres` | Dominio | AB#321 | PR #133 | ✅ |
| HU40-CP04 | `CategoriaTest` | `rechazaUnaCategoriaSinEstado` | Dominio | AB#321 | PR #133 | ✅ |
| HU40-CP04 | `CategoriaDocumentMapperTest` | `convierteCategoriaEnDocumento` | Infraestructura | AB#321 | PR #133 | ✅ |
| HU40-CP04 | `CategoriaDocumentMapperTest` | `convierteDocumentoEnCategoria` | Infraestructura | AB#321 | PR #133 | ✅ |
| HU40-CP04 | `CategoriaDocumentMapperTest` | `unDocumentoSinEstadoNiDescripcionSeLeeActivo` | Infraestructura | AB#321 | PR #133 | ✅ |
| HU40-CP04 | `CategoriaMongoAdapterTest` | `guardaYLeeLaDescripcionYElEstado` | Infraestructura | AB#321 | PR #133 | ✅ |
| HU40-CP05 | `AdministracionCategoriasServiceTest` | `crearGuardaLaCategoriaConElEstadoElegido` (ACTIVA e INACTIVA) | Aplicación | AB#323 | PR #137 | ✅ |
| HU40-CP05 | `CategoriaMongoAdapterTest` | `guardarAsignaIdYLaGuardaConSuEstado` | Infraestructura | AB#323 | PR #137 | ✅ |
| HU40-CP06 | `AdministracionCategoriasServiceTest` | `crearRechazaUnNombreRepetido` | Aplicación | AB#323 | PR #137 | ✅ |
| HU40-CP06 | `CategoriaMongoAdapterTest` | `existePorNombreNoDistingueMayusculasNiTildes` (4 variantes) | Infraestructura | AB#323 | PR #137 | ✅ |
| HU40-CP06 | `CategoriaMongoAdapterTest` | `existePorNombreComparaElNombreCompletoYLiteral` (3 variantes) | Infraestructura | AB#323 | PR #137 | ✅ |
| HU40-CP06 | `CategoriaMongoAdapterTest` | `guardarRechazaUnNombreRepetidoConOtrasMayusculasOTildes` (mayúsculas y tildes) | Infraestructura | AB#323 | PR #137 | ✅ |
| HU40-CP05 | `AdministracionCategoriasControllerTest` | `unAdministradorCreaUnaCategoria` | Infraestructura | AB#322 | PR #138 | ✅ |
| HU40-CP06 | `AdministracionCategoriasControllerTest` | `crearUnNombreRepetidoDevuelve409` | Infraestructura | AB#322 | PR #138 | ✅ |
| HU40-CP07 | `AdministracionCategoriasControllerTest` | `crearConDatosInvalidosDevuelve400` (sin nombre, sin estado y estado desconocido) | Infraestructura | AB#322 | PR #138 (modificado en PR #183) | ✅ |
| HU40-CP07 | `AdministracionCategoriasControllerTest` | `crearConUnaReglaDelDominioIncumplidaDevuelve400` | Infraestructura | AB#322 | PR #138 | ✅ |
| HU40-CP08 | `AdministracionCategoriasControllerTest` | `crearConOtroRolDevuelve403` (cliente y vendedor) | Infraestructura | AB#322 | PR #138 | ✅ |
| HU40-CP08 | `AdministracionCategoriasControllerTest` | `crearSinSesionDevuelve401` | Infraestructura | AB#322 | PR #138 | ✅ |
| HU40-CP09 | `SeguridadCategoriasServiceTest` | `elListadoPublicoNoNecesitaSesion` | Aplicación | — | Rama `fix/seguridad-capa-negocio` | 🔍 |
| HU40-CP09 | `SeguridadCategoriasServiceTest` | `unClienteNoAdministraCategorias` | Aplicación | — | Rama `fix/seguridad-capa-negocio` | 🔍 |
| HU40-CP09 | `SeguridadCategoriasServiceTest` | `unVendedorNoAdministraCategorias` | Aplicación | — | Rama `fix/seguridad-capa-negocio` | 🔍 |
| HU40-CP09 | `SeguridadCategoriasServiceTest` | `unAnonimoNoAdministraCategorias` | Aplicación | — | Rama `fix/seguridad-capa-negocio` | 🔍 |
| HU40-CP09 | `SeguridadCategoriasServiceTest` | `unAdministradorAdministraCategorias` | Aplicación | — | Rama `fix/seguridad-capa-negocio` | 🔍 |
| HU40-CP09 | `SeguridadCategoriasServiceTest` | `sinSesionSeRechazaLaAdministracion` | Aplicación | — | Rama `fix/seguridad-capa-negocio` | 🔍 |
| HU40-CP10 | `adminCategories.test.js` | `HU40-CP10: pide POST /api/admin/categorias con el nombre, la descripción y el estado, y devuelve la categoría creada` | Frontend · API | AB#320 | PR #201 | ✅ |
| HU40-CP10 | `adminCategories.test.js` | `HU40-CP10: un 409 llega como error con su estado y su código` | Frontend · API | AB#320 | PR #201 | ✅ |
| HU40-CP11 | `CategoryFormDialog.test.jsx` | `HU40-CP11: pide el nombre, la descripción y el estado, con la categoría activa por defecto` | Frontend · componente | AB#320 | PR #201 | ✅ |
| HU40-CP11 | `CategoryFormDialog.test.jsx` | `HU40-CP11: al cancelar se cierra sin crear nada` | Frontend · componente | AB#320 | PR #201 | ✅ |
| HU40-CP11 | `AdminCategoriesPage.test.jsx` | `HU40-CP11: la vista tiene el botón Nueva categoría, que abre el formulario de creación` | Frontend · página | AB#320 | PR #201 | ✅ |
| HU40-CP11 | `AdminCategoriesPage.test.jsx` | `HU40-CP11: sin ninguna categoría también se puede crear la primera` | Frontend · página | AB#320 | PR #201 | ✅ |
| HU40-CP12 | `CategoryFormDialog.test.jsx` | `HU40-CP12: con el nombre %s avisa de que es obligatorio y no envía nada` (2 variantes: vacío y con solo espacios) | Frontend · componente | AB#320 | PR #201 | ✅ |
| HU40-CP12 | `CategoryFormDialog.test.jsx` | `HU40-CP12: el nombre admite como mucho 50 caracteres y la descripción 200, como el backend` | Frontend · componente | AB#320 | PR #201 | ✅ |
| HU40-CP13 | `CategoryFormDialog.test.jsx` | `HU40-CP13: envía el nombre, la descripción y el estado elegidos y avisa con la categoría creada` | Frontend · componente | AB#320 | PR #201 | ✅ |
| HU40-CP13 | `CategoryFormDialog.test.jsx` | `HU40-CP13: mientras se crea, los botones quedan deshabilitados` | Frontend · componente | AB#320 | PR #201 | ✅ |
| HU40-CP13 | `AdminCategoriesPage.test.jsx` | `HU40-CP13: al crearla, avisa y la categoría aparece en la lista con el estado elegido` | Frontend · página | AB#320 | PR #201 | ✅ |
| HU40-CP14 | `CategoryFormDialog.test.jsx` | `HU40-CP14: si el nombre ya existe lo marca en el campo, no cierra y al cambiarlo desaparece el aviso` | Frontend · componente | AB#320 | PR #201 | ✅ |
| HU40-CP14 | `CategoryFormDialog.test.jsx` | `HU40-CP14: cualquier otro error avisa con un mensaje genérico y deja el formulario abierto` | Frontend · componente | AB#320 | PR #201 | ✅ |
| HU40-CP14 | `AdminCategoriesPage.test.jsx` | `HU40-CP14: no se puede crear una categoría repetida, aunque cambien las mayúsculas o las tildes` | Frontend · página | AB#320 | PR #201 | ✅ |

Rutas de las clases de test (bajo `backend/src/test/java/es/uclm/esibuy/backend/`):
- `AdministracionCategoriasControllerTest`: `categorias/infrastructure/adapter/in/web/` (con `@WebMvcTest` y la seguridad real)
- `AdministracionCategoriasServiceTest`: `categorias/application/service/` (JUnit y Mockito, sin Spring)
- `CategoriaTest`: `categorias/domain/model/` (JUnit, sin Spring)
- `CategoriaDocumentMapperTest`: `categorias/infrastructure/adapter/out/persistence/` (JUnit, sin Spring)
- `CategoriaMongoAdapterTest`: `categorias/infrastructure/adapter/out/persistence/` (con Testcontainers)

---

## HU41 · Editar categorías

### Tabla A · HU41 ↔ casos de prueba

| Caso | Descripción | Tipo | Tests | Estado |
|---|---|---|---|---|
| HU41-CP01 | El administrador cambia el nombre y la descripción de una categoría; se conservan su id y su estado y se devuelve con su número de productos | Unitario (aplicación) | 1 | ✅ Pasa |
| HU41-CP02 | El nombre de la propia categoría no cuenta como repetido: se pueden cambiar solo las mayúsculas o las tildes | Unitario (aplicación) | 1 | ✅ Pasa |
| HU41-CP03 | No se puede poner a una categoría el nombre de otra, sin distinguir mayúsculas ni tildes (409) | Unitario (aplicación) | 1 | ✅ Pasa |
| HU41-CP04 | Editar una categoría que no existe falla sin guardar nada (404) | Unitario (aplicación) | 1 | ✅ Pasa |
| HU41-CP05 | Editar con datos que incumplen el dominio falla sin guardar nada (400) | Unitario (aplicación) | 1 | ✅ Pasa |
| HU41-CP06 | Editar categorías exige el rol administrador; cliente, vendedor y sin sesión se rechazan. Seguridad en la capa de negocio (R8) | Unitario con Spring Security (sin MongoDB) | 5 | ✅ Pasa |
| HU41-CP07 | Buscar una categoría por id la devuelve con su descripción y su estado; si no existe, o el id no tiene formato de id, devuelve vacío | Integración MongoDB | 2 | ✅ Pasa |
| HU41-CP08 | Comprobar si otra categoría tiene un nombre: no distingue mayúsculas ni tildes, excluye la propia categoría y compara el nombre completo y literal | Integración MongoDB | 3 | ✅ Pasa |
| HU41-CP09 | Guardar una categoría con id la actualiza sin crear otra, y la base de datos rechaza cambiar su nombre al de otra aunque se salte la comprobación previa | Integración MongoDB | 2 | ✅ Pasa |
| HU41-CP10 | `PUT /api/admin/categorias/{id}` responde 200 con la fila actualizada y su número de productos | Controlador | 1 | ✅ Pasa |
| HU41-CP11 | Editar con el nombre de otra categoría devuelve 409 `CATEGORY_NAME_ALREADY_EXISTS` | Controlador | 1 | ✅ Pasa |
| HU41-CP12 | Editar sin nombre o incumpliendo una regla del dominio devuelve 400 | Controlador | 2 | ✅ Pasa |
| HU41-CP13 | Editar una categoría que no existe devuelve 404 `CATEGORY_NOT_FOUND` | Controlador | 1 | ✅ Pasa |
| HU41-CP14 | Sin rol de administrador no se pueden editar categorías: 403 (cliente y vendedor) y 401 sin sesión | Controlador | 2 | ✅ Pasa |
| HU41-CP15 | La llamada del administrador pide `PUT /api/admin/categorias/{id}` con nombre y descripción y devuelve la categoría editada; un 404 llega como error con su estado y su código | Frontend (API) | 2 | ✅ Pasa (2, AB#327, PR #201) |
| HU41-CP16 | Cada fila de la lista tiene el lápiz, que abre el formulario con el nombre y la descripción de la categoría, sin el estado; cancelar no edita nada | Frontend (componente y página) | 4 | ✅ Pasa (4, AB#327, PR #201) |
| HU41-CP17 | Al guardar se envían el nombre y la descripción nuevos, se avisa de que se ha actualizado y la lista muestra los cambios; el nombre sigue siendo obligatorio | Frontend (componente y página) | 4 | ✅ Pasa (4, AB#327, PR #201) |
| HU41-CP18 | El nombre de otra categoría se marca en el campo; si la categoría ya no existe se cierra el formulario, se avisa y se recarga la lista; cualquier otro error muestra un mensaje genérico | Frontend (componente y página) | 5 | ✅ Pasa (5, AB#327, PR #201) |
| HU41-CP19 | Cambiar el estado guarda la categoría con el estado nuevo (activar o desactivar), conserva su nombre y su descripción y devuelve su número de productos | Unitario (aplicación) | 2 | 🔍 En revisión (2, AB#416, PR #215) |
| HU41-CP20 | Cambiar el estado de una categoría que no existe falla sin guardar nada (404) | Unitario (aplicación) | 1 | 🔍 En revisión (1, AB#416, PR #215) |
| HU41-CP21 | Cambiar el estado sin indicar el estado nuevo falla sin guardar nada (400) | Unitario (aplicación) | 1 | 🔍 En revisión (1, AB#416, PR #215) |
| HU41-CP22 | Activar y desactivar categorías exige el rol administrador; cliente, vendedor y sin sesión se rechazan. Seguridad en la capa de negocio (R8) | Unitario con Spring Security (sin MongoDB) | 5 | 🔍 En revisión (5, AB#416, PR #215) |
| HU41-CP23 | `PATCH /api/admin/categorias/{id}/status` responde 200 con la fila actualizada y su número de productos | Controlador | 1 | 🔍 En revisión (1, AB#416, PR #215) |
| HU41-CP24 | Cambiar el estado sin estado o con un estado desconocido devuelve 400 `VALIDATION_ERROR` y no llega al caso de uso | Controlador | 3 | 🔍 En revisión (3, AB#416, PR #215) |
| HU41-CP25 | Cambiar el estado de una categoría que no existe devuelve 404 `CATEGORY_NOT_FOUND` | Controlador | 1 | 🔍 En revisión (1, AB#416, PR #215) |
| HU41-CP26 | Sin rol de administrador no se puede cambiar el estado de las categorías: 403 (cliente y vendedor) y 401 sin sesión | Controlador | 3 | 🔍 En revisión (3, AB#416, PR #215) |
| HU41-CP27 | La llamada del administrador pide `PATCH /api/admin/categorias/{id}/status` con el estado nuevo y devuelve la fila; un 404 llega como error con su estado y su código | Frontend (API) | 2 | 🔍 En revisión (2, AB#417, PR #215) |
| HU41-CP28 | Cada fila ofrece desactivar una categoría activa y activar una inactiva; el botón pide confirmación explicando si dejará de mostrarse o volverá a mostrarse, y cancelar no cambia nada | Frontend (componente y página) | 6 | 🔍 En revisión (6, AB#417, PR #215) |
| HU41-CP29 | Al confirmar se cambia el estado, se avisa y la lista muestra el estado nuevo; mientras se cambia, los botones de la confirmación quedan deshabilitados | Frontend (componente y página) | 4 | 🔍 En revisión (4, AB#417, PR #215) |
| HU41-CP30 | Si la categoría ya no existe se avisa y se recarga la lista; cualquier otro error muestra un mensaje genérico | Frontend (componente) | 2 | 🔍 En revisión (2, AB#417, PR #215) |

Notas:
- **HU41-CP01 a CP14:** casos propuestos en las T329 (AB#329) y T328 (AB#328), a partir de los criterios de la HU41. Pendientes de confirmar con el equipo y de añadir a la HU41 en Azure Boards.
- **Endpoint (T328):** `PUT /api/admin/categorias/{id}` con `{nombre, descripcion}` → 200 `{id, nombre, descripcion, estado, numeroProductos}`, el mismo formato que una fila de `GET /api/admin/categorias`, para que la vista (T327) sustituya la fila sin recargar. El estado no se edita. Errores: 400 `VALIDATION_ERROR` (falta el nombre o se incumple una regla del dominio), 404 `CATEGORY_NOT_FOUND`, 409 `CATEGORY_NAME_ALREADY_EXISTS`, 401 sin sesión y 403 con otro rol. Necesita la cabecera CSRF `X-XSRF-TOKEN`.
- **Backend (T329):** `AdministracionCategoriasUseCase.editar(EditarCategoriaCommand)`, que devuelve `CategoriaConProductos`. Puerto de salida con `buscarPorId` y `existeOtraConNombre`. El listado público lee del mismo repositorio, así que una edición se ve a la vez para clientes y vendedores.
- **Pendiente:** la edición reemplaza el documento y no copia `createdAt` (el mapper no lo lleva), así que la fecha de creación puede perderse al editar. Es una hipótesis sin comprobar; queda para una task aparte.
- **HU41-CP06 (R8, seguridad en la capa de negocio):** nace en verde, sin rojo real, porque `editar` lleva el `@PreAuthorize` desde el primer ciclo. Cubre el servicio `AdministracionCategoriasService` con la clase de test existente `SeguridadCategoriasServiceTest`.
- **HU41-CP15 a CP18 (frontend, T327):** casos propuestos en la T327 (AB#327), a partir de los criterios «el botón de editar debe estar en la tabla», «el nuevo nombre no puede ser igual a uno existente» y «se debe actualizar en la lista de categorías». Pendientes de confirmar con el equipo y de añadir a la HU41 en Azure Boards.
- **Vista (T327):** el lápiz de cada fila (`CategoryRowActions`) abre `CategoryFormDialog` con la categoría: nombre y descripción cargados, sin el estado (el backend no lo edita) y con el botón «Guardar». Al guardar se avisa («Casa se ha actualizado.») y se vuelve a pedir el listado. Si la categoría ya no existe (404), se cierra el formulario, se avisa con el mismo mensaje que al eliminar y se recarga la lista. El mock simula el `PUT` (404, 400, 409 si el nombre es el de otra y la fila actualizada).
- **HU41-CP16 a CP18:** «al cancelar se cierra sin editar nada», «al editar también se valida que el nombre no quede vacío» y los dos tests del nombre repetido nacen en verde: el formulario es el mismo que el de creación, que ya lo hacía.
- **HU41-CP19 a CP30 (activar y desactivar categorías):** casos propuestos en las tasks AB#416 (backend) y AB#417 (frontend), creadas en el Sprint 2 porque ninguna HU permitía cambiar el estado de una categoría después de crearla. Pendientes de confirmar con el equipo y de añadir a la HU41 en Azure Boards. Rama `feature/US41-T416-T417-activar-desactivar-categorias`.
- **Endpoint (T416):** `PATCH /api/admin/categorias/{id}/status` con `{ "estado": "ACTIVA" | "INACTIVA" }` → 200 con la fila del listado (`{id, nombre, descripcion, estado, numeroProductos}`); 400 `VALIDATION_ERROR` sin estado o con uno desconocido (el enum se lee del JSON y `ManejadorGlobalErrores` trata igual un cuerpo ilegible), 404 `CATEGORY_NOT_FOUND`, 401 sin sesión y 403 con otro rol. `AdministracionCategoriasUseCase.cambiarEstado(id, estado)` lleva `@PreAuthorize` de administrador.
- **Pendiente de decidir con el equipo:** qué pasa con los productos de una categoría desactivada. Ahora se permite desactivarla aunque tenga productos, que no cambian; la categoría solo deja de salir en el listado público.
- **Tests que nacen en verde (T416):** HU41-CP22, porque `cambiarEstado` lleva el `@PreAuthorize` desde el primer ciclo (se comprobó quitándolo de forma temporal: fallan los cuatro tests de rechazo), y HU41-CP26, porque la regla de `/api/admin/**` de `SecurityConfig` protege cualquier ruta.
- **Vista (T417):** en cada fila de `CategoryRowActions`, un ojo tachado («Desactivar X») en las activas y un ojo («Activar X») en las inactivas, con confirmación (`StatusConfirmDialog`). Al confirmar se avisa («X se ha desactivado.» o «X se ha activado.») y se recarga la lista; si la categoría ya no existe se avisa con «X ya no existe.» y también se recarga.

### Tabla B · casos de prueba ↔ tests

| Caso | Clase de test | Método | Capa | Task | Origen | Estado |
|---|---|---|---|---|---|---|
| HU41-CP01 | `AdministracionCategoriasServiceTest` | `editarCambiaElNombreYLaDescripcion` | Aplicación | AB#329 | PR #175 | ✅ |
| HU41-CP02 | `AdministracionCategoriasServiceTest` | `editarPermiteCambiarLasMayusculasDelPropioNombre` | Aplicación | AB#329 | PR #175 | ✅ |
| HU41-CP03 | `AdministracionCategoriasServiceTest` | `editarRechazaElNombreDeOtraCategoria` | Aplicación | AB#329 | PR #175 | ✅ |
| HU41-CP04 | `AdministracionCategoriasServiceTest` | `editarUnaCategoriaInexistenteFalla` | Aplicación | AB#329 | PR #175 | ✅ |
| HU41-CP05 | `AdministracionCategoriasServiceTest` | `editarConDatosInvalidosFalla` | Aplicación | AB#329 | PR #175 | ✅ |
| HU41-CP06 | `SeguridadCategoriasServiceTest` | `unClienteNoEditaCategorias` | Aplicación | AB#329 | PR #175 | ✅ |
| HU41-CP06 | `SeguridadCategoriasServiceTest` | `unVendedorNoEditaCategorias` | Aplicación | AB#329 | PR #175 | ✅ |
| HU41-CP06 | `SeguridadCategoriasServiceTest` | `unAnonimoNoEditaCategorias` | Aplicación | AB#329 | PR #175 | ✅ |
| HU41-CP06 | `SeguridadCategoriasServiceTest` | `unAdministradorEditaCategorias` | Aplicación | AB#329 | PR #175 | ✅ |
| HU41-CP06 | `SeguridadCategoriasServiceTest` | `sinSesionSeRechazaLaEdicion` | Aplicación | AB#329 | PR #175 | ✅ |
| HU41-CP07 | `CategoriaMongoAdapterTest` | `buscarPorIdDevuelveLaCategoria` | Infraestructura | AB#329 | PR #175 | ✅ |
| HU41-CP07 | `CategoriaMongoAdapterTest` | `buscarPorIdDevuelveVacioSiNoExiste` (2 variantes) | Infraestructura | AB#329 | PR #175 | ✅ |
| HU41-CP08 | `CategoriaMongoAdapterTest` | `existeOtraConNombreNoDistingueMayusculasNiTildes` (4 variantes) | Infraestructura | AB#329 | PR #175 | ✅ |
| HU41-CP08 | `CategoriaMongoAdapterTest` | `existeOtraConNombreIgnoraLaPropiaCategoria` (3 variantes) | Infraestructura | AB#329 | PR #175 | ✅ |
| HU41-CP08 | `CategoriaMongoAdapterTest` | `existeOtraConNombreComparaElNombreCompletoYLiteral` (3 variantes) | Infraestructura | AB#329 | PR #175 | ✅ |
| HU41-CP09 | `CategoriaMongoAdapterTest` | `guardarConIdActualizaLaCategoria` | Infraestructura | AB#329 | PR #175 | ✅ |
| HU41-CP09 | `CategoriaMongoAdapterTest` | `guardarRechazaCambiarElNombreAlDeOtraCategoria` | Infraestructura | AB#329 | PR #175 | ✅ |
| HU41-CP10 | `AdministracionCategoriasControllerTest` | `unAdministradorEditaUnaCategoria` | Infraestructura | AB#328 | PR #175 | ✅ |
| HU41-CP11 | `AdministracionCategoriasControllerTest` | `editarUnNombreRepetidoDevuelve409` | Infraestructura | AB#328 | PR #175 | ✅ |
| HU41-CP12 | `AdministracionCategoriasControllerTest` | `editarConDatosInvalidosDevuelve400` (2 variantes) | Infraestructura | AB#328 | PR #175 (modificado en PR #183) | ✅ |
| HU41-CP12 | `AdministracionCategoriasControllerTest` | `editarConUnaReglaDelDominioIncumplidaDevuelve400` | Infraestructura | AB#328 | PR #175 | ✅ |
| HU41-CP13 | `AdministracionCategoriasControllerTest` | `editarUnaCategoriaInexistenteDevuelve404` | Infraestructura | AB#328 | PR #175 | ✅ |
| HU41-CP14 | `AdministracionCategoriasControllerTest` | `editarConOtroRolDevuelve403` (cliente y vendedor) | Infraestructura | AB#328 | PR #175 | ✅ |
| HU41-CP14 | `AdministracionCategoriasControllerTest` | `editarSinSesionDevuelve401` | Infraestructura | AB#328 | PR #175 | ✅ |
| HU41-CP15 | `adminCategories.test.js` | `HU41-CP15: pide PUT /api/admin/categorias/{id} con el nombre y la descripción, y devuelve la categoría editada` | Frontend · API | AB#327 | PR #201 | ✅ |
| HU41-CP15 | `adminCategories.test.js` | `HU41-CP15: un 404 llega como error con su estado y su código` | Frontend · API | AB#327 | PR #201 | ✅ |
| HU41-CP16 | `CategoryFormDialog.test.jsx` | `HU41-CP16: con una categoría, muestra su nombre y su descripción para editarlos, sin el estado` | Frontend · componente | AB#327 | PR #201 | ✅ |
| HU41-CP16 | `CategoryFormDialog.test.jsx` | `HU41-CP16: al cancelar se cierra sin editar nada` | Frontend · componente | AB#327 | PR #201 | ✅ |
| HU41-CP16 | `CategoryRowActions.test.jsx` | `HU41-CP16: ofrece el lápiz para editar la categoría, que abre su formulario con sus datos` | Frontend · componente | AB#327 | PR #201 | ✅ |
| HU41-CP16 | `AdminCategoriesPage.test.jsx` | `HU41-CP16: cada fila de la lista tiene el lápiz para editar su categoría` | Frontend · página | AB#327 | PR #201 | ✅ |
| HU41-CP17 | `CategoryFormDialog.test.jsx` | `HU41-CP17: envía el nombre y la descripción nuevos y avisa con la categoría editada` | Frontend · componente | AB#327 | PR #201 | ✅ |
| HU41-CP17 | `CategoryFormDialog.test.jsx` | `HU41-CP17: al editar también se valida que el nombre no quede vacío` | Frontend · componente | AB#327 | PR #201 | ✅ |
| HU41-CP17 | `CategoryRowActions.test.jsx` | `HU41-CP17: al guardar, avisa de que se ha actualizado y pide actualizar la lista` | Frontend · componente | AB#327 | PR #201 | ✅ |
| HU41-CP17 | `AdminCategoriesPage.test.jsx` | `HU41-CP17: al guardar, avisa y la lista muestra el nombre y la descripción nuevos` | Frontend · página | AB#327 | PR #201 | ✅ |
| HU41-CP18 | `CategoryFormDialog.test.jsx` | `HU41-CP18: si el nombre nuevo es el de otra categoría lo marca en el campo y no cierra` | Frontend · componente | AB#327 | PR #201 | ✅ |
| HU41-CP18 | `CategoryFormDialog.test.jsx` | `HU41-CP18: si la categoría ya no existe avisa a quien abrió el formulario` | Frontend · componente | AB#327 | PR #201 | ✅ |
| HU41-CP18 | `CategoryFormDialog.test.jsx` | `HU41-CP18: cualquier otro error avisa con un mensaje genérico y deja el formulario abierto` | Frontend · componente | AB#327 | PR #201 | ✅ |
| HU41-CP18 | `CategoryRowActions.test.jsx` | `HU41-CP18: al guardar una categoría que ya no existe, cierra el formulario, avisa y pide actualizar la lista` | Frontend · componente | AB#327 | PR #201 | ✅ |
| HU41-CP18 | `AdminCategoriesPage.test.jsx` | `HU41-CP18: no se puede poner el nombre de otra categoría, aunque cambien las mayúsculas o las tildes` | Frontend · página | AB#327 | PR #201 | ✅ |
| HU41-CP19 | `AdministracionCategoriasServiceTest` | `cambiarEstadoGuardaElEstadoNuevo` (activa e inactiva) | Aplicación | AB#416 | PR #215 | 🔍 |
| HU41-CP20 | `AdministracionCategoriasServiceTest` | `cambiarEstadoDeUnaCategoriaInexistenteFalla` | Aplicación | AB#416 | PR #215 | 🔍 |
| HU41-CP21 | `AdministracionCategoriasServiceTest` | `cambiarEstadoSinEstadoFalla` | Aplicación | AB#416 | PR #215 | 🔍 |
| HU41-CP22 | `SeguridadCategoriasServiceTest` | `unClienteNoCambiaElEstadoDeCategorias` | Aplicación | AB#416 | PR #215 | 🔍 |
| HU41-CP22 | `SeguridadCategoriasServiceTest` | `unVendedorNoCambiaElEstadoDeCategorias` | Aplicación | AB#416 | PR #215 | 🔍 |
| HU41-CP22 | `SeguridadCategoriasServiceTest` | `unAnonimoNoCambiaElEstadoDeCategorias` | Aplicación | AB#416 | PR #215 | 🔍 |
| HU41-CP22 | `SeguridadCategoriasServiceTest` | `unAdministradorCambiaElEstadoDeCategorias` | Aplicación | AB#416 | PR #215 | 🔍 |
| HU41-CP22 | `SeguridadCategoriasServiceTest` | `sinSesionSeRechazaElCambioDeEstado` | Aplicación | AB#416 | PR #215 | 🔍 |
| HU41-CP23 | `AdministracionCategoriasControllerTest` | `unAdministradorCambiaElEstadoDeUnaCategoria` | Infraestructura | AB#416 | PR #215 | 🔍 |
| HU41-CP24 | `AdministracionCategoriasControllerTest` | `cambiarEstadoConDatosInvalidosDevuelve400` (3 variantes: sin estado, desconocido y en minúsculas) | Infraestructura | AB#416 | PR #215 | 🔍 |
| HU41-CP25 | `AdministracionCategoriasControllerTest` | `cambiarEstadoDeUnaCategoriaInexistenteDevuelve404` | Infraestructura | AB#416 | PR #215 | 🔍 |
| HU41-CP26 | `AdministracionCategoriasControllerTest` | `cambiarEstadoConOtroRolDevuelve403` (cliente y vendedor) | Infraestructura | AB#416 | PR #215 | 🔍 |
| HU41-CP26 | `AdministracionCategoriasControllerTest` | `cambiarEstadoSinSesionDevuelve401` | Infraestructura | AB#416 | PR #215 | 🔍 |
| HU41-CP27 | `adminCategories.test.js` | `HU41-CP27: pide PATCH /api/admin/categorias/{id}/status con el estado nuevo y devuelve la fila actualizada` | Frontend · API | AB#417 | PR #215 | 🔍 |
| HU41-CP27 | `adminCategories.test.js` | `HU41-CP27: un 404 llega como error con su estado y su código` | Frontend · API | AB#417 | PR #215 | 🔍 |
| HU41-CP28 | `CategoryRowActions.test.jsx` | `HU41-CP28: una categoría activa ofrece desactivarla` | Frontend · componente | AB#417 | PR #215 | 🔍 |
| HU41-CP28 | `CategoryRowActions.test.jsx` | `HU41-CP28: una categoría inactiva ofrece activarla` | Frontend · componente | AB#417 | PR #215 | 🔍 |
| HU41-CP28 | `CategoryRowActions.test.jsx` | `HU41-CP28: al pulsar Desactivar pide confirmación avisando de que dejará de mostrarse` | Frontend · componente | AB#417 | PR #215 | 🔍 |
| HU41-CP28 | `CategoryRowActions.test.jsx` | `HU41-CP28: al pulsar Activar pide confirmación avisando de que volverá a mostrarse` | Frontend · componente | AB#417 | PR #215 | 🔍 |
| HU41-CP28 | `CategoryRowActions.test.jsx` | `HU41-CP28: al cancelar se cierra la confirmación y no se cambia nada` | Frontend · componente | AB#417 | PR #215 | 🔍 |
| HU41-CP28 | `AdminCategoriesPage.test.jsx` | `HU41-CP28: cada fila ofrece activar o desactivar su categoría según su estado` | Frontend · página | AB#417 | PR #215 | 🔍 |
| HU41-CP29 | `CategoryRowActions.test.jsx` | `HU41-CP29: al confirmar desactiva la categoría, avisa y pide actualizar la lista` | Frontend · componente | AB#417 | PR #215 | 🔍 |
| HU41-CP29 | `CategoryRowActions.test.jsx` | `HU41-CP29: al confirmar activa una categoría inactiva y avisa` | Frontend · componente | AB#417 | PR #215 | 🔍 |
| HU41-CP29 | `CategoryRowActions.test.jsx` | `HU41-CP29: mientras se cambia el estado, los botones de la confirmación quedan deshabilitados` | Frontend · componente | AB#417 | PR #215 | 🔍 |
| HU41-CP29 | `AdminCategoriesPage.test.jsx` | `HU41-CP29: al desactivar una categoría, avisa y la lista la muestra inactiva` | Frontend · página | AB#417 | PR #215 | 🔍 |
| HU41-CP30 | `CategoryRowActions.test.jsx` | `HU41-CP30: si la categoría ya no existe, avisa y pide actualizar la lista` | Frontend · componente | AB#417 | PR #215 | 🔍 |
| HU41-CP30 | `CategoryRowActions.test.jsx` | `HU41-CP30: cualquier otro error muestra un mensaje genérico y no recarga la lista` | Frontend · componente | AB#417 | PR #215 | 🔍 |

Ruta de la clase de test de seguridad (bajo `backend/src/test/java/es/uclm/esibuy/backend/`):
- `SeguridadCategoriasServiceTest`: `categorias/application/service/` (contexto mínimo de Spring con `@EnableMethodSecurity`, sin MongoDB)

---

## HU42 · Eliminar categorías

### Tabla A · HU42 ↔ casos de prueba

| Caso | Descripción | Tipo | Tests | Estado |
|---|---|---|---|---|
| HU42-CP01 | Eliminar borra una categoría sin productos ni vendedores | Unitario (aplicación) | 1 | ✅ Pasa |
| HU42-CP02 | Una categoría con productos no se puede eliminar y no se borra nada (409) | Unitario (aplicación) | 1 | ✅ Pasa |
| HU42-CP03 | Una categoría que es la principal de algún vendedor no se puede eliminar y no se borra nada (409) | Unitario (aplicación) | 1 | ✅ Pasa |
| HU42-CP04 | Eliminar una categoría que no existe falla sin consultar ni borrar nada (404) | Unitario (aplicación) | 1 | ✅ Pasa |
| HU42-CP05 | Eliminar categorías exige el rol administrador; cliente, vendedor y sin sesión se rechazan. Seguridad en la capa de negocio (R8) | Unitario con Spring Security (sin MongoDB) | 5 | ✅ Pasa |
| HU42-CP06 | Eliminar borra físicamente solo la categoría indicada; un id que no existe, o que no tiene formato de id, no falla ni borra nada | Integración MongoDB | 2 | ✅ Pasa |
| HU42-CP07 | El módulo de usuarios dice si algún vendedor tiene una categoría como principal | Unitario (aplicación) | 2 | ✅ Pasa |
| HU42-CP08 | Preguntar por los vendedores de una categoría exige el rol administrador; cliente, vendedor y sin sesión se rechazan. Seguridad en la capa de negocio (R8) | Unitario con Spring Security (sin MongoDB) | 4 | ✅ Pasa |
| HU42-CP09 | La consulta en MongoDB detecta vendedores con la categoría como principal y no cuenta a quien no es vendedor | Integración MongoDB | 3 | ✅ Pasa |
| HU42-CP10 | El adaptador de categorías pregunta al módulo de usuarios por su puerto de entrada | Unitario (adaptador) | 2 | ✅ Pasa |
| HU42-CP11 | `DELETE /api/admin/categorias/{id}` responde 204 sin cuerpo | Controlador | 1 | ✅ Pasa |
| HU42-CP12 | Eliminar una categoría con productos devuelve 409 `CATEGORY_HAS_PRODUCTS` | Controlador | 1 | ✅ Pasa |
| HU42-CP13 | Eliminar la categoría principal de un vendedor devuelve 409 `CATEGORY_HAS_SELLERS` | Controlador | 1 | ✅ Pasa |
| HU42-CP14 | Eliminar una categoría que no existe devuelve 404 `CATEGORY_NOT_FOUND` | Controlador | 1 | ✅ Pasa |
| HU42-CP15 | Sin rol de administrador no se pueden eliminar categorías: 403 (cliente y vendedor) y 401 sin sesión | Controlador | 2 | ✅ Pasa |
| HU42-CP16 | La llamada del administrador pide `DELETE /api/admin/categorias/{id}` con el id de la categoría; un 409 llega como error con su estado y su código | Frontend (API) | 2 | ✅ Pasa |
| HU42-CP17 | Cada fila de la lista de categorías tiene la papelera; al pulsarla se pide confirmación avisando de que no se puede deshacer, y cancelar no borra nada | Frontend (componente y página) | 4 | ✅ Pasa |
| HU42-CP18 | Al confirmar se borra la categoría, se avisa de que se ha eliminado y desaparece de la lista; mientras se elimina, los botones de la confirmación quedan deshabilitados | Frontend (componente y página) | 3 | ✅ Pasa |
| HU42-CP19 | Una categoría con productos tiene la papelera deshabilitada con el motivo; una sin productos la tiene habilitada | Frontend (componente y página) | 3 | ✅ Pasa |
| HU42-CP20 | Cada error del backend al eliminar muestra su mensaje (tiene productos, es la principal de un vendedor, ya no existe o genérico); con productos o ya eliminada, se recarga la lista | Frontend (componente y página) | 5 | ✅ Pasa |

Notas:
- **HU42-CP01 a CP15:** casos propuestos en las T326 (AB#326) y T325 (AB#325), a partir de los criterios de la HU42. Pendientes de confirmar con el equipo y de añadir a la HU42 en Azure Boards.
- **Endpoint (T325):** `DELETE /api/admin/categorias/{id}` → 204 sin cuerpo. Errores: 404 `CATEGORY_NOT_FOUND`, 409 `CATEGORY_HAS_PRODUCTS`, 409 `CATEGORY_HAS_SELLERS`, 401 sin sesión y 403 con otro rol. Necesita la cabecera CSRF `X-XSRF-TOKEN`.
- **Backend (T326):** `AdministracionCategoriasUseCase.eliminar(id)`. Orden de comprobaciones: existe (404), productos (409), vendedores (409) y borrado físico. Puertos de salida `CategoriaRepositoryPort.eliminar` y `VendedoresPorCategoriaPort`. El módulo de usuarios ofrece `existenVendedoresConCategoria` (CP07 a CP09) y categorías lo consume solo por su puerto de entrada (CP10).
- **Decisión:** además de los productos, no se puede eliminar la categoría que es la principal de un vendedor. El recuento de productos incluye los no disponibles.
- **Pendiente (riesgo conocido):** puede haber una condición de carrera entre eliminar una categoría y crear un producto o un vendedor con ella, porque no hay transacción entre colecciones. No está resuelto.
- **HU42-CP16 a CP20 (frontend, T324):** casos propuestos en la T324 (AB#324), a partir de los criterios «el botón de eliminación debe estar en la lista de categorías» y «no debe ser posible la eliminación si la categoría tiene productos asociados». Pendientes de confirmar con el equipo y de añadir a la HU42 en Azure Boards.
- **Vista (T324):** la papelera va en la columna Acciones de `/admin/categories`, junto a la lupa, en el componente `CategoryRowActions` (la lupa pasa de la página a este componente). Pide confirmación con `StatusConfirmDialog` y, al eliminar, la página muestra el aviso y vuelve a pedir el listado. Si la fila trae `numeroProductos > 0`, la papelera queda deshabilitada; los dos 409, el 404 y cualquier otro error tienen su mensaje. Con `CATEGORY_HAS_PRODUCTS` y `CATEGORY_NOT_FOUND` se recarga la lista, porque la que se ve está desfasada.
- **Mock:** `adminCategoriesHandlers` simula el `DELETE` (404, 409 `CATEGORY_HAS_PRODUCTS` y 204) con una lista que se reinicia tras cada test (`resetAdminCategoriesMock`). El 409 `CATEGORY_HAS_SELLERS` no se simula: los tests que lo necesitan ponen su handler.
- **HU42-CP19:** el test «una sin productos la tiene habilitada con el título Eliminar» nace en verde: ya se cumplía desde el ciclo 2 y se deja para que no se deshabilite por error.
- **HU39-CP11 eliminado de la página:** el test «la vista no ofrece crear, editar ni eliminar categorías» se modificó en la T324 (HU42, papelera) y se elimina en las T320 y T327 (HU40 y HU41), porque la vista ya ofrece crear, editar y eliminar.
- **HU42-CP05 y CP08 (R8, seguridad en la capa de negocio):** nacen en verde, sin rojo real, porque `@PreAuthorize` va desde el primer ciclo. CP05 usa la clase existente `SeguridadCategoriasServiceTest` y CP08 la de HU20-CP07; los tests de CP08 llegaron en un commit posterior al que solo cambió el comentario de cabecera.
- **HU42-CP14 y CP15:** también sin rojo real, porque el handler del 404 (HU41) y la regla de `/api/admin/**` ya existían.

### Tabla B · casos de prueba ↔ tests

| Caso | Clase de test | Método | Capa | Task | Origen | Estado |
|---|---|---|---|---|---|---|
| HU42-CP01 | `AdministracionCategoriasServiceTest` | `eliminarBorraUnaCategoriaSinProductosNiVendedores` | Aplicación | AB#326 | PR #181 | ✅ |
| HU42-CP02 | `AdministracionCategoriasServiceTest` | `eliminarRechazaUnaCategoriaConProductos` | Aplicación | AB#326 | PR #181 | ✅ |
| HU42-CP03 | `AdministracionCategoriasServiceTest` | `eliminarRechazaUnaCategoriaConVendedores` | Aplicación | AB#326 | PR #181 | ✅ |
| HU42-CP04 | `AdministracionCategoriasServiceTest` | `eliminarUnaCategoriaInexistenteFalla` | Aplicación | AB#326 | PR #181 | ✅ |
| HU42-CP05 | `SeguridadCategoriasServiceTest` | `unClienteNoEliminaCategorias` | Aplicación | AB#326 | PR #181 | ✅ |
| HU42-CP05 | `SeguridadCategoriasServiceTest` | `unVendedorNoEliminaCategorias` | Aplicación | AB#326 | PR #181 | ✅ |
| HU42-CP05 | `SeguridadCategoriasServiceTest` | `unAnonimoNoEliminaCategorias` | Aplicación | AB#326 | PR #181 | ✅ |
| HU42-CP05 | `SeguridadCategoriasServiceTest` | `unAdministradorEliminaCategorias` | Aplicación | AB#326 | PR #181 | ✅ |
| HU42-CP05 | `SeguridadCategoriasServiceTest` | `sinSesionSeRechazaLaEliminacion` | Aplicación | AB#326 | PR #181 | ✅ |
| HU42-CP06 | `CategoriaMongoAdapterTest` | `eliminarBorraSoloLaCategoriaIndicada` | Infraestructura | AB#326 | PR #181 | ✅ |
| HU42-CP06 | `CategoriaMongoAdapterTest` | `eliminarUnIdInexistenteNoBorraNada` (2 variantes) | Infraestructura | AB#326 | PR #181 | ✅ |
| HU42-CP07 | `AdministracionUsuariosServiceTest` | `hayVendedoresConLaCategoria` | Aplicación | AB#326 | PR #181 | ✅ |
| HU42-CP07 | `AdministracionUsuariosServiceTest` | `noHayVendedoresConLaCategoria` | Aplicación | AB#326 | PR #181 | ✅ |
| HU42-CP08 | `SeguridadAdministracionUsuarioServiceTest` | `unClienteNoPreguntaPorLosVendedoresDeUnaCategoria` | Aplicación | AB#326 | PR #181 | ✅ |
| HU42-CP08 | `SeguridadAdministracionUsuarioServiceTest` | `unVendedorNoPreguntaPorLosVendedoresDeUnaCategoria` | Aplicación | AB#326 | PR #181 | ✅ |
| HU42-CP08 | `SeguridadAdministracionUsuarioServiceTest` | `unAdministradorPreguntaPorLosVendedoresDeUnaCategoria` | Aplicación | AB#326 | PR #181 | ✅ |
| HU42-CP08 | `SeguridadAdministracionUsuarioServiceTest` | `sinSesionSeRechazaLaPreguntaPorLosVendedores` | Aplicación | AB#326 | PR #181 | ✅ |
| HU42-CP09 | `UsuarioMongoAdapterTest` | `existeVendedorConLaCategoriaPrincipal` | Infraestructura | AB#326 | PR #181 | ✅ |
| HU42-CP09 | `UsuarioMongoAdapterTest` | `noExisteVendedorConLaCategoriaPrincipal` | Infraestructura | AB#326 | PR #181 | ✅ |
| HU42-CP09 | `UsuarioMongoAdapterTest` | `soloCuentanLosVendedores` | Infraestructura | AB#326 | PR #181 | ✅ |
| HU42-CP10 | `VendedoresPorCategoriaAdapterTest` | `existenVendedoresPreguntaAlModuloDeUsuarios` | Infraestructura | AB#326 | PR #181 | ✅ |
| HU42-CP10 | `VendedoresPorCategoriaAdapterTest` | `noExistenVendedoresSiElModuloDeUsuariosDiceQueNo` | Infraestructura | AB#326 | PR #181 | ✅ |
| HU42-CP11 | `AdministracionCategoriasControllerTest` | `unAdministradorEliminaUnaCategoria` | Infraestructura | AB#325 | PR #181 | ✅ |
| HU42-CP12 | `AdministracionCategoriasControllerTest` | `eliminarUnaCategoriaConProductosDevuelve409` | Infraestructura | AB#325 | PR #181 | ✅ |
| HU42-CP13 | `AdministracionCategoriasControllerTest` | `eliminarUnaCategoriaPrincipalDeUnVendedorDevuelve409` | Infraestructura | AB#325 | PR #181 | ✅ |
| HU42-CP14 | `AdministracionCategoriasControllerTest` | `eliminarUnaCategoriaInexistenteDevuelve404` | Infraestructura | AB#325 | PR #181 | ✅ |
| HU42-CP15 | `AdministracionCategoriasControllerTest` | `eliminarConOtroRolDevuelve403` (cliente y vendedor) | Infraestructura | AB#325 | PR #181 | ✅ |
| HU42-CP15 | `AdministracionCategoriasControllerTest` | `eliminarSinSesionDevuelve401` | Infraestructura | AB#325 | PR #181 | ✅ |
| HU42-CP16 | `adminCategories.test.js` | `HU42-CP16: pide DELETE /api/admin/categorias/{id} con el id de la categoría` | Frontend · API | AB#324 | PR #199 | ✅ |
| HU42-CP16 | `adminCategories.test.js` | `HU42-CP16: un 409 llega como error con su estado y su código` | Frontend · API | AB#324 | PR #199 | ✅ |
| HU42-CP17 | `CategoryRowActions.test.jsx` | `HU42-CP17: ofrece la lupa para ver la ficha y la papelera para eliminar la categoría` | Frontend · componente | AB#324 | PR #199 | ✅ |
| HU42-CP17 | `CategoryRowActions.test.jsx` | `HU42-CP17: al pulsar la papelera pide confirmación avisando de que no se puede deshacer` | Frontend · componente | AB#324 | PR #199 | ✅ |
| HU42-CP17 | `CategoryRowActions.test.jsx` | `HU42-CP17: al cancelar se cierra la confirmación y no se borra nada` | Frontend · componente | AB#324 | PR #199 | ✅ |
| HU42-CP17 | `AdminCategoriesPage.test.jsx` | `HU42-CP17: cada fila de la lista tiene la papelera para eliminar su categoría` | Frontend · página | AB#324 | PR #199 | ✅ |
| HU42-CP18 | `CategoryRowActions.test.jsx` | `HU42-CP18: al confirmar borra la categoría, avisa de que se ha eliminado y pide actualizar la lista` | Frontend · componente | AB#324 | PR #199 | ✅ |
| HU42-CP18 | `CategoryRowActions.test.jsx` | `HU42-CP18: mientras se elimina, los botones de la confirmación quedan deshabilitados` | Frontend · componente | AB#324 | PR #199 | ✅ |
| HU42-CP18 | `AdminCategoriesPage.test.jsx` | `HU42-CP18: al confirmar, avisa de que se ha eliminado y la categoría desaparece de la lista` | Frontend · página | AB#324 | PR #199 | ✅ |
| HU42-CP19 | `CategoryRowActions.test.jsx` | `HU42-CP19: si la categoría tiene productos, la papelera queda deshabilitada y explica por qué` | Frontend · componente | AB#324 | PR #199 | ✅ |
| HU42-CP19 | `CategoryRowActions.test.jsx` | `HU42-CP19: una categoría sin productos tiene la papelera habilitada con el título Eliminar` | Frontend · componente | AB#324 | PR #199 | ✅ |
| HU42-CP19 | `AdminCategoriesPage.test.jsx` | `HU42-CP19: solo se pueden eliminar las categorías sin productos` | Frontend · página | AB#324 | PR #199 | ✅ |
| HU42-CP20 | `CategoryRowActions.test.jsx` | `HU42-CP20: $caso` (4 variantes: tiene productos, principal de un vendedor, ya no existe y error genérico) | Frontend · componente | AB#324 | PR #199 | ✅ |
| HU42-CP20 | `AdminCategoriesPage.test.jsx` | `HU42-CP20: si el backend no deja eliminarla, avisa del motivo y la categoría sigue en la lista` | Frontend · página | AB#324 | PR #199 | ✅ |

Ruta de las clases de test de seguridad (bajo `backend/src/test/java/es/uclm/esibuy/backend/`):
- `SeguridadCategoriasServiceTest`: `categorias/application/service/` (contexto mínimo de Spring con `@EnableMethodSecurity`, sin MongoDB)
- `SeguridadAdministracionUsuarioServiceTest`: `usuarios/application/service/` (contexto mínimo de Spring con `@EnableMethodSecurity`, sin MongoDB)

---

## HU43 · Cerrar sesión

### Tabla A · HU43 ↔ casos de prueba

| Caso | Descripción | Tipo | Tests | Estado |
|---|---|---|---|---|
| HU43-CP01 | Al cerrar sesión (`POST /api/auth/logout`) la sesión se invalida en el servidor y la cookie de sesión se borra del navegador | Controlador | 2 | ✅ Pasa |
| HU43-CP02 | La sesión caduca tras 15 minutos sin actividad si no se configura otro tiempo (`ESIBUY_SESION_INACTIVIDAD`) | Integración (servidor real) | 1 | ✅ Pasa |
| HU43-CP03 | Las cookies de sesión (`JSESSIONID`: Secure, HttpOnly y SameSite=Strict) y de CSRF (`XSRF-TOKEN`: Secure y SameSite=Strict, legible por el frontend) llevan sus atributos de seguridad | Controlador + integración (servidor real) | 2 | ✅ Pasa |
| HU43-CP04 | El cliente cierra sesión desde su zona: se avisa al servidor, vuelve al login con «Has cerrado sesión.» y su zona deja de ser accesible | Frontend (página) | 1 | 🔍 En revisión (1, AB#314, PR sin abrir) |
| HU43-CP05 | El vendedor cierra sesión desde su zona, con el mismo resultado | Frontend (página) | 1 | 🔍 En revisión (1, AB#315, PR sin abrir) |
| HU43-CP06 | El administrador cierra sesión desde su panel, con el mismo resultado | Frontend (página) | 1 | 🔍 En revisión (1, AB#316, PR sin abrir) |
| HU43-CP07 | Si la sesión caduca (401 con la sesión abierta), el frontend la cierra y vuelve al login con «Tu sesión ha caducado»; una contraseña incorrecta no se toma por caducidad | Frontend (cliente HTTP y página) | 4 | 🔍 En revisión (4, AB#317, PR sin abrir) |
| HU43-CP08 | Si el servidor falla al cerrar sesión, la sesión del navegador se cierra igualmente | Frontend (página) | 1 | 🔍 En revisión (1, AB#317, PR sin abrir) |

Notas:
- **HU43-CP01:** caso propuesto en la T318 (AB#318), a partir del criterio de aceptación «al cerrar sesión, el token/sesión se invalida en el servidor». **HU43-CP02 y CP03:** propuestos en la T319 (AB#319), a partir de «la sesión caduca tras un tiempo de inactividad configurable» y «cookies de sesión con atributos Secure, HttpOnly y SameSite». Pendientes de confirmar con el equipo y de añadir a la HU43 en Azure Boards.
- **Backend (T319):** en `application.properties`, `server.servlet.session.timeout=${ESIBUY_SESION_INACTIVIDAD:15m}` y los atributos de la cookie de sesión; en `SecurityConfig`, un `CookieCsrfTokenRepository` con Secure y SameSite=Strict. Al caducar, la siguiente petición recibe 401 y el frontend debe redirigir al login.
- **Backend (T318):** el endpoint ya existía (Spring Security, `SecurityConfig`, T230): invalida la sesión, borra `JSESSIONID` y responde 204. `logoutCierraLaSesion` es el test de la HU2 (HU2-CP15, PR #67) que comprueba la invalidación; se traza también aquí sin cambiar su `@DisplayName`.
- **La cookie de sesión la borra el servidor:** es HttpOnly, así que el frontend (T317, «lógica de borrado de cookies de sesión») no puede borrarla; le basta con llamar a `logout()` y limpiar su estado.
- **HU43-CP04 a CP08 (Sprint 2, frontend, TDD):** casos propuestos en las tasks AB#314 a AB#317 a partir de los criterios de la HU43 y de las notas del backend (`frontend/docs/hu43-cerrar-sesion.md`). Pendientes de confirmar con el equipo y de añadir a la HU43 en Azure Boards.
- **Decisiones del equipo:** cerrar sesión lleva al login con el aviso «Has cerrado sesión.» (el backend responde 204 sin redirección) y no pide confirmación.
- **Sin rojo real:** HU43-CP08 y el test «una contraseña incorrecta (401) no se toma por una sesión caducada» (CP07) protegen comportamientos que ya existían; en el ciclo rojo CP08 fallaba solo por el destino y el aviso.
- **Tests de la HU2 eliminados:** `appHeader_alCerrarSesion_vuelveALaHomeYMuestraBotonesDeInvitado` y `appHeader_tras_cerrarSesion_laRutaProtegidaYaNoEsAccesible` (sin caso en la matriz) esperaban volver a la página de inicio; los sustituyen CP04 a CP06.
- **Pendiente:** en despliegue, con Secure la aplicación tiene que servirse por HTTPS.

### Tabla B · casos de prueba ↔ tests

| Caso | Clase de test | Método | Capa | Task | Origen | Estado |
|---|---|---|---|---|---|---|
| HU43-CP01 | `AutenticacionControllerTest` | `logoutCierraLaSesion` (`@DisplayName` HU2-CP15) | Infraestructura | AB#230 | PR #67 | ✅ |
| HU43-CP01 | `AutenticacionControllerTest` | `logoutBorraLaCookieDeSesion` | Infraestructura | AB#318 | PR #141 | ✅ |
| HU43-CP02 | `ConfiguracionSesionTest` | `laSesionCaducaA15MinutosDeInactividadPorDefecto` | Infraestructura | AB#319 | PR #142 | ✅ |
| HU43-CP03 | `ConfiguracionSesionTest` | `laCookieDeSesionEsSecureHttpOnlyYSameSiteStrict` | Infraestructura | AB#319 | PR #142 | ✅ |
| HU43-CP03 | `SecurityConfigTest` | `laCookieCsrfEsSecureYSameSiteStrict` | Infraestructura | AB#319 | PR #142 | ✅ |
| HU43-CP04 | `AppHeader.test.jsx` | `HU43-CP04: el cliente cierra sesión desde su zona, vuelve al login con un aviso y ya no puede entrar` | Frontend · página | AB#314 | Rama `feature/US43-T314-T317-cerrar-sesion` (PR sin abrir) | 🔍 |
| HU43-CP05 | `AppHeader.test.jsx` | `HU43-CP05: el vendedor cierra sesión desde su zona, vuelve al login con un aviso y ya no puede entrar` | Frontend · página | AB#315 | Rama `feature/US43-T314-T317-cerrar-sesion` (PR sin abrir) | 🔍 |
| HU43-CP06 | `AppHeader.test.jsx` | `HU43-CP06: el administrador cierra sesión desde su panel, vuelve al login con un aviso y ya no puede entrar` | Frontend · página | AB#316 | Rama `feature/US43-T314-T317-cerrar-sesion` (PR sin abrir) | 🔍 |
| HU43-CP07 | `client.test.js` | `HU43-CP07: avisa a quien escucha cuando una petición recibe 401` | Frontend · cliente HTTP | AB#317 | Rama `feature/US43-T314-T317-cerrar-sesion` (PR sin abrir) | 🔍 |
| HU43-CP07 | `client.test.js` | `HU43-CP07: no avisa con otros errores ni después de dejar de escuchar` | Frontend · cliente HTTP | AB#317 | Rama `feature/US43-T314-T317-cerrar-sesion` (PR sin abrir) | 🔍 |
| HU43-CP07 | `LoginFlow.test.jsx` | `HU43-CP07: si la sesión caduca, vuelve al login avisando de que ha caducado` | Frontend · página | AB#317 | Rama `feature/US43-T314-T317-cerrar-sesion` (PR sin abrir) | 🔍 |
| HU43-CP07 | `LoginFlow.test.jsx` | `HU43-CP07: una contraseña incorrecta (401) no se toma por una sesión caducada` | Frontend · página | AB#317 | Rama `feature/US43-T314-T317-cerrar-sesion` (PR sin abrir) | 🔍 |
| HU43-CP08 | `AppHeader.test.jsx` | `HU43-CP08: si el servidor falla al cerrar sesión, la sesión del navegador se cierra igualmente` | Frontend · página | AB#317 | Rama `feature/US43-T314-T317-cerrar-sesion` (PR sin abrir) | 🔍 |

Rutas de las clases de test (bajo `backend/src/test/java/es/uclm/esibuy/backend/`):
- `AutenticacionControllerTest`: `usuarios/infrastructure/adapter/in/web/` (con `@WebMvcTest` y la seguridad real)
- `ConfiguracionSesionTest`: `shared/config/` (`@SpringBootTest` con el servidor real en un puerto aleatorio y Testcontainers)
- `SecurityConfigTest`: `shared/config/` (con `@WebMvcTest`)
- `AppHeader.test.jsx`: `frontend/src/components/` (Vitest, RTL y MSW)
- `client.test.js`: `frontend/src/api/` (Vitest y MSW)
- `LoginFlow.test.jsx`: `frontend/src/features/auth/` (Vitest, RTL y MSW)

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
| `UsuariosConfigTest` | `elContextoConectaLasPiezasDeLaRecuperacionDeContrasena` | Que Spring conecta la recuperación de contraseña (`RecuperacionContrasenaService`) con su adaptador de códigos en MongoDB (HU3; AB#400, PR #172) |
| `MetodosPagoConfigTest` | `elContextoConectaLasPiezasDeLosMetodosDePago` | Que Spring conecta el servicio de métodos de pago con su adaptador de MongoDB, su cifrado AES-GCM y el cliente en sesión (HU6; AB#271, PR #148) |
| `MetodosPagoConfigTest` | `losDatosDePagoSeCifranConLaClaveConfigurada` | Que los datos de pago se cifran con la clave de `esibuy.pagos.clave-cifrado`, que en los tests pone `MongoTestcontainersConfig` (HU6; AB#271, PR #148) |
| `CarritoConfigTest` | `elContextoConectaLasPiezasDelCarrito` | Que Spring conecta el servicio del carrito con su adaptador de MongoDB, los productos y los usuarios (HU9; AB#385, PR #162) |
| `PedidosConfigTest` | `elContextoConectaLasPiezasDeLosPedidos` | Que Spring conecta la realización de pedidos con su repositorio, el contador y los adaptadores hacia usuarios, carrito, métodos de pago, direcciones y productos (HU10; AB#381, PR #166); y la consulta de compras de HU13 (AB#370, PR #179). También la cancelación de pedidos (HU12; AB#375, PR #200) y las ventas del vendedor (HU17; AB#280, PR #212) |
| `ValoracionesConfigTest` | `elContextoConectaLasPiezasDeLasValoraciones` | Que Spring conecta la valoración y la consulta de valoraciones con su adaptador de MongoDB y los adaptadores hacia usuarios y pedidos (HU13; AB#370 y AB#371, PR #179), y hacia productos (AB#415, PR #195) |
| `DeseosConfigTest` | `elContextoConectaLasPiezasDeLaListaDeDeseos` | Que Spring conecta el servicio de la lista de deseos con su adaptador de MongoDB y los adaptadores hacia usuarios y productos (HU14; AB#360, PR #196) |
| `DireccionesConfigTest` | `elContextoConectaLasPiezasDeLasDirecciones` | Que Spring conecta el servicio de direcciones de envío con su adaptador de MongoDB y su propio cliente en sesión, distinto del de métodos de pago (HU5; AB#105, PR #161) |
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
| `InicializadorColeccionesTest` | `creaLasTreceColecciones` | Que al arrancar se crean las 13 colecciones: las 10 del modelo de BBDD, `metodos_pago`, `direcciones_envio` y `contadores` (AB#210, PR #43; ampliado en AB#271, PR #148, y en AB#105, PR #161; ampliado en AB#131, PR #166) |
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
| `ProductosConfigTest` | `elContextoConectaElAnalisisDeImagenesConVirusTotal` | Que Spring conecta el análisis antivirus de las imágenes (`AnalisisImagenPort`) con `VirusTotalAdapter`, con una API key falsa (HU31; AB#397, PR #189) |
| `ProductosConfigTest` | `elContextoConectaLaActualizacionDeLaValoracion` | Que Spring conecta la actualización de la valoración de los productos (`ValoracionesProductoUseCase`) con `ValoracionesProductoService` (HU13; AB#415, PR #195) |
| `ProductosConfigTest` | `elContextoConectaElInventarioDelVendedor` | Que Spring conecta el inventario del vendedor (`InventarioVendedorUseCase`) con `InventarioVendedorService` (HU17; AB#280, PR #212) |
| `EstadisticasConfigTest` | `elContextoConectaLasPiezasDeLasEstadisticas` | Que Spring conecta las estadísticas del vendedor (`EstadisticasVendedorService`) con sus adaptadores hacia usuarios, pedidos y productos (HU17; AB#279, PR #212) |

`InicializadorColeccionesTest` está en `shared/infrastructure/config/` y usa Testcontainers. HU36 (crear la base de datos) no tiene casos de prueba en Azure Boards, por eso sus tests están en esta sección.
