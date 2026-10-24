package es.uclm.esibuy.backend.usuarios.application.service;

import es.uclm.esibuy.backend.usuarios.application.port.in.AutenticacionUseCase;
import es.uclm.esibuy.backend.usuarios.application.port.in.IniciarSesionCommand;
import es.uclm.esibuy.backend.usuarios.application.port.in.PasoLogin;
import es.uclm.esibuy.backend.usuarios.application.port.in.ResultadoInicioSesion;
import es.uclm.esibuy.backend.usuarios.application.port.in.VerificarCodigoEmailCommand;
import es.uclm.esibuy.backend.usuarios.application.port.out.CodigoVerificacionRepositoryPort;
import es.uclm.esibuy.backend.usuarios.application.port.out.EnvioEmailPort;
import es.uclm.esibuy.backend.usuarios.application.port.out.FactorAutenticacionRepositoryPort;
import es.uclm.esibuy.backend.usuarios.application.port.out.GeneradorCodigoPort;
import es.uclm.esibuy.backend.usuarios.application.port.out.HashContrasenaPort;
import es.uclm.esibuy.backend.usuarios.application.port.out.UsuarioRepositoryPort;
import es.uclm.esibuy.backend.usuarios.domain.exception.CodigoInvalidoException;
import es.uclm.esibuy.backend.usuarios.domain.exception.CredencialesInvalidasException;
import es.uclm.esibuy.backend.usuarios.domain.exception.CuentaBloqueadaException;
import es.uclm.esibuy.backend.usuarios.domain.exception.CuentaDesactivadaException;
import es.uclm.esibuy.backend.usuarios.domain.model.CodigoVerificacion;
import es.uclm.esibuy.backend.usuarios.domain.model.DatosCliente;
import es.uclm.esibuy.backend.usuarios.domain.model.DatosPersonales;
import es.uclm.esibuy.backend.usuarios.domain.model.Dni;
import java.time.LocalDate;
import es.uclm.esibuy.backend.usuarios.domain.model.Email;
import es.uclm.esibuy.backend.usuarios.domain.model.FactorAutenticacion;
import es.uclm.esibuy.backend.usuarios.domain.model.Rol;
import es.uclm.esibuy.backend.usuarios.domain.model.TipoCliente;
import es.uclm.esibuy.backend.usuarios.domain.model.Usuario;
import es.uclm.esibuy.backend.usuarios.domain.exception.CuentaPendienteException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

// Login, paso 1: credenciales, bloqueo por intentos y cuentas desactivadas (HU2, T19/T22).
// Login, paso 2: código de verificación por email (HU2, T21/T22).
// El contrato que debe cumplir sale del frontend (frontend/src/mocks/authHandlers.js).
@ExtendWith(MockitoExtension.class)
class AutenticacionServiceTest {

    private static final DatosPersonales DATOS_PERSONALES = new DatosPersonales("Juan", "Pérez García", null);
    private static final DatosCliente DATOS_CLIENTE = new DatosCliente(
            TipoCliente.ESTANDAR, new Dni("12345678Z"), LocalDate.of(1996, 5, 20), null);

    // Hora fija para los tests: el usuario recibe la hora en vez de consultarla él mismo.
    private static final Instant AHORA = Instant.parse("2026-10-08T10:00:00Z");

    // Código que "genera" el generador en los tests y su hash, para poder comprobar qué se envía y qué se guarda.
    private static final String CODIGO = "654321";
    private static final String HASH_CODIGO = "hash-del-codigo";

    @Mock
    private UsuarioRepositoryPort usuarioRepository;

    @Mock
    private HashContrasenaPort hashContrasena;

    @Mock
    private CodigoVerificacionRepositoryPort codigoRepository;

    @Mock
    private GeneradorCodigoPort generadorCodigo;

    @Mock
    private EnvioEmailPort envioEmail;

    @Mock
    private FactorAutenticacionRepositoryPort factorRepository;

    private AutenticacionUseCase autenticacion; // Tipado como el puerto de entrada, igual que en el registro

    @BeforeEach
    void crearServicio() {
        // lenient: solo los tests que terminan con un login correcto llegan a generar el código (paso 2, T21);
        // sin él, Mockito daría por error que los demás tests no usen estas respuestas.
        lenient().when(generadorCodigo.generar()).thenReturn(CODIGO);
        lenient().when(hashContrasena.hashear(CODIGO)).thenReturn(HASH_CODIGO);
        autenticacion = servicioConReloj(Clock.fixed(AHORA, ZoneOffset.UTC));
    }

    private AutenticacionService servicioConReloj(Clock reloj) {
        return new AutenticacionService(usuarioRepository, hashContrasena, codigoRepository, generadorCodigo,
                envioEmail, factorRepository, reloj);
    }

    // Factor de Authenticator con el alta ya confirmada (T20): el usuario pasa a verificar su código
    private static FactorAutenticacion factorConfirmado() {
        return FactorAutenticacion.reconstruir("f-1", "u-1", "secreto-cifrado", true, AHORA, null);
    }

    // Cliente con id: al iniciar sesión se guarda un código de verificación asociado a su id (T21).
    private static Usuario clienteConId() {
        return activo(new Usuario("u-1", DATOS_PERSONALES, new Email("juan@uclm.es"), "hash-guardado", Rol.CLIENTE,
                DATOS_CLIENTE, null));
    }

    // Cliente sin id, como el que sale del registro (T229): datos personales y de cliente válidos.
    private static Usuario clienteNuevo() {
        return new Usuario(null, DATOS_PERSONALES, new Email("juan@uclm.es"), "hash-guardado", Rol.CLIENTE,
                DATOS_CLIENTE, null);
    }

    // Una cuenta nueva nace PENDIENTE (T233) y el login la rechaza: los tests de login usan cuentas verificadas.
    private static Usuario activo(Usuario usuario) {
        usuario.activar();
        return usuario;
    }

    @Test
    @DisplayName("HU2-CP01: inicia sesión con credenciales correctas y pasa al código por email")
    void iniciaSesionConCredencialesCorrectas() {
        Usuario usuario = clienteConId();
        when(usuarioRepository.buscarPorCorreo("juan@uclm.es")).thenReturn(Optional.of(usuario));
        when(hashContrasena.coincide("clave1234", "hash-guardado")).thenReturn(true);

        ResultadoInicioSesion resultado = autenticacion.iniciarSesion(new IniciarSesionCommand("juan@uclm.es", "clave1234"));

        assertEquals("juan@uclm.es", resultado.email());
        assertEquals(PasoLogin.CODIGO_EMAIL, resultado.siguientePaso());
    }

    @Test
    @DisplayName("HU2-CP02: rechaza un correo inexistente")
    void rechazaCorreoInexistente() {
        when(usuarioRepository.buscarPorCorreo("nadie@uclm.es")).thenReturn(Optional.empty());
        IniciarSesionCommand comando = new IniciarSesionCommand("nadie@uclm.es", "clave1234");

        assertThrows(CredencialesInvalidasException.class, () -> autenticacion.iniciarSesion(comando));
    }

    @Test
    @DisplayName("HU2-CP02: rechaza una contraseña incorrecta con la misma respuesta que un correo inexistente")
    void rechazaContrasenaIncorrecta() {
        Usuario usuario = activo(clienteNuevo());
        when(usuarioRepository.buscarPorCorreo("juan@uclm.es")).thenReturn(Optional.of(usuario));
        when(hashContrasena.coincide("otraClave", "hash-guardado")).thenReturn(false);
        when(usuarioRepository.buscarPorCorreo("nadie@uclm.es")).thenReturn(Optional.empty());

        CredencialesInvalidasException contrasenaIncorrecta = assertThrows(CredencialesInvalidasException.class,
                () -> autenticacion.iniciarSesion(new IniciarSesionCommand("juan@uclm.es", "otraClave")));
        CredencialesInvalidasException correoInexistente = assertThrows(CredencialesInvalidasException.class,
                () -> autenticacion.iniciarSesion(new IniciarSesionCommand("nadie@uclm.es", "otraClave")));

        // Mismo mensaje en los dos casos: desde fuera no se distingue si el correo existe (enumeración de usuarios).
        assertEquals(correoInexistente.getMessage(), contrasenaIncorrecta.getMessage());
    }

    @Test
    @DisplayName("HU2-CP03: tras 4 intentos fallidos la cuenta no se bloquea")
    void permiteCuartoIntentoFallidoSinBloquear() {
        // Frontera: 4 fallos → aún se puede entrar.
        Usuario usuario = clienteConId();
        for (int i = 0; i < 4; i++) {
            usuario.registrarIntentoFallido(AHORA);
        }
        when(usuarioRepository.buscarPorCorreo("juan@uclm.es")).thenReturn(Optional.of(usuario));
        when(hashContrasena.coincide("clave1234", "hash-guardado")).thenReturn(true);

        ResultadoInicioSesion resultado = autenticacion.iniciarSesion(new IniciarSesionCommand("juan@uclm.es", "clave1234"));

        assertEquals(PasoLogin.CODIGO_EMAIL, resultado.siguientePaso());
    }

    @Test
    @DisplayName("HU2-CP03: bloquea la cuenta tras 5 intentos fallidos")
    void bloqueaLaCuentaTrasCincoIntentosFallidos() {
        // Frontera: 5 fallos → bloqueada, incluso con la contraseña correcta.
        Usuario usuario = activo(clienteNuevo());
        when(usuarioRepository.buscarPorCorreo("juan@uclm.es")).thenReturn(Optional.of(usuario));
        when(hashContrasena.coincide("otraClave", "hash-guardado")).thenReturn(false);
        IniciarSesionCommand contrasenaIncorrecta = new IniciarSesionCommand("juan@uclm.es", "otraClave");
        for (int i = 0; i < 5; i++) {
            assertThrows(CredencialesInvalidasException.class, () -> autenticacion.iniciarSesion(contrasenaIncorrecta));
        }

        // La contraseña correcta no se llega a comprobar: si se respondiera distinto al acertar,
        // un atacante sabría cuándo ha dado con ella.
        IniciarSesionCommand contrasenaCorrecta = new IniciarSesionCommand("juan@uclm.es", "clave1234");
        assertThrows(CuentaBloqueadaException.class, () -> autenticacion.iniciarSesion(contrasenaCorrecta));

        // Cada fallo se guarda; si no, el contador se perdería entre un login y otro.
        verify(usuarioRepository, times(5)).guardar(usuario);
    }

    @Test
    @DisplayName("HU2-CP03: desbloquea la cuenta pasados 15 minutos")
    void desbloqueaLaCuentaPasados15Minutos() {
        // Frontera: justo a los 15 minutos del bloqueo ya se puede entrar.
        Usuario usuario = clienteConId();
        for (int i = 0; i < 5; i++) {
            usuario.registrarIntentoFallido(AHORA);
        }
        when(usuarioRepository.buscarPorCorreo("juan@uclm.es")).thenReturn(Optional.of(usuario));
        when(hashContrasena.coincide("clave1234", "hash-guardado")).thenReturn(true);

        // El mismo servicio, pero con el reloj 15 minutos después: no hace falta esperar.
        Clock relojQuinceMinutosDespues = Clock.fixed(AHORA.plus(Duration.ofMinutes(15)), ZoneOffset.UTC);
        AutenticacionUseCase autenticacionMasTarde = servicioConReloj(relojQuinceMinutosDespues);

        ResultadoInicioSesion resultado = autenticacionMasTarde.iniciarSesion(new IniciarSesionCommand("juan@uclm.es", "clave1234"));

        assertEquals(PasoLogin.CODIGO_EMAIL, resultado.siguientePaso());
    }

    @Test
    @DisplayName("HU2-CP04: rechaza una cuenta desactivada")
    void rechazaCuentaDesactivada() {
        // «Desactivada» = desactivada por un administrador (desactivar() de Usuario, T214).
        // Se comprueba después de la contraseña: quien no la conoce no puede averiguar el estado de la cuenta.
        Usuario usuario = activo(clienteNuevo());
        usuario.desactivar();
        when(usuarioRepository.buscarPorCorreo("juan@uclm.es")).thenReturn(Optional.of(usuario));
        when(hashContrasena.coincide("clave1234", "hash-guardado")).thenReturn(true);
        IniciarSesionCommand comando = new IniciarSesionCommand("juan@uclm.es", "clave1234");

        assertThrows(CuentaDesactivadaException.class, () -> autenticacion.iniciarSesion(comando));
    }

    @Test
    @DisplayName("HU2-CP28: una cuenta bloqueada por un administrador se rechaza antes de enviar el código del correo")
    void cuentaBloqueadaPorAdministradorNoRecibeCodigo() {
        // Bloqueada por un administrador = estado DESACTIVADO (desactivar() de Usuario) hasta que se renombre (T246).
        Usuario usuario = activo(clienteNuevo());
        usuario.desactivar();
        when(usuarioRepository.buscarPorCorreo("juan@uclm.es")).thenReturn(Optional.of(usuario));
        when(hashContrasena.coincide("clave1234", "hash-guardado")).thenReturn(true);
        IniciarSesionCommand comando = new IniciarSesionCommand("juan@uclm.es", "clave1234");

        assertThrows(CuentaDesactivadaException.class, () -> autenticacion.iniciarSesion(comando));
        verify(generadorCodigo, never()).generar();
        verify(codigoRepository, never()).guardar(any());
        verify(envioEmail, never()).enviarCodigoVerificacion(any(), any());
    }

    // --- Paso 2: código de verificación por email (T21) ---

    @Test
    @DisplayName("HU2-CP05: al validar las credenciales envía un código al correo del usuario")
    void enviaElCodigoAlCorreoTrasValidarLasCredenciales() {
        when(usuarioRepository.buscarPorCorreo("juan@uclm.es")).thenReturn(Optional.of(clienteConId()));
        when(hashContrasena.coincide("clave1234", "hash-guardado")).thenReturn(true);

        autenticacion.iniciarSesion(new IniciarSesionCommand("juan@uclm.es", "clave1234"));

        // Se envía el código en claro (el usuario tiene que leerlo); lo que se guarda es su hash (ciclo 7).
        verify(envioEmail).enviarCodigoVerificacion("juan@uclm.es", CODIGO);
    }

    @Test
    @DisplayName("HU2-CP12: el código se guarda hasheado y antes de enviarlo")
    void guardaElCodigoHasheado() {
        when(usuarioRepository.buscarPorCorreo("juan@uclm.es")).thenReturn(Optional.of(clienteConId()));
        when(hashContrasena.coincide("clave1234", "hash-guardado")).thenReturn(true);

        autenticacion.iniciarSesion(new IniciarSesionCommand("juan@uclm.es", "clave1234"));

        // Se guarda antes de enviar: si el envío falla basta con repetir el login (plan de la T21, P6).
        InOrder orden = inOrder(codigoRepository, envioEmail);
        ArgumentCaptor<CodigoVerificacion> guardado = ArgumentCaptor.forClass(CodigoVerificacion.class);
        orden.verify(codigoRepository).guardar(guardado.capture());
        orden.verify(envioEmail).enviarCodigoVerificacion("juan@uclm.es", CODIGO);

        // En la base de datos solo queda el hash: quien la lea no puede usar el código.
        assertEquals(HASH_CODIGO, guardado.getValue().getCodigoHash());
        // Y queda asociado al usuario que ha iniciado sesión: el paso 2 lo busca por su id.
        assertEquals("u-1", guardado.getValue().getUsuarioId());
    }

    @Test
    @DisplayName("HU2-CP06: un código correcto pasa al paso de Microsoft Authenticator")
    void codigoCorrectoPasaAlAuthenticator() {
        when(usuarioRepository.buscarPorCorreo("juan@uclm.es")).thenReturn(Optional.of(clienteConId()));
        when(factorRepository.buscarPorUsuario("u-1")).thenReturn(Optional.of(factorConfirmado())); // T20
        when(codigoRepository.buscarPorUsuario("u-1"))
                .thenReturn(Optional.of(CodigoVerificacion.nuevo("u-1", HASH_CODIGO, AHORA)));
        when(hashContrasena.coincide(CODIGO, HASH_CODIGO)).thenReturn(true);

        ResultadoInicioSesion resultado = autenticacion.verificarCodigoEmail(
                new VerificarCodigoEmailCommand("juan@uclm.es", CODIGO));

        assertEquals("juan@uclm.es", resultado.email());
        assertEquals(PasoLogin.AUTHENTICATOR, resultado.siguientePaso());

        // Se guarda como usado: el mismo código no sirve para un segundo intento (CP09).
        ArgumentCaptor<CodigoVerificacion> guardado = ArgumentCaptor.forClass(CodigoVerificacion.class);
        verify(codigoRepository).guardar(guardado.capture());
        assertFalse(guardado.getValue().puedeUsarse(AHORA));
        assertEquals(AHORA, guardado.getValue().getUsadoEn());   // con la hora del servicio (su Clock)
    }

    @Test
    @DisplayName("HU2-CP22: un código correcto de un usuario sin Authenticator pasa al alta")
    void sinAuthenticatorPasaAlAlta() {
        prepararCodigoCorrecto();
        when(factorRepository.buscarPorUsuario("u-1")).thenReturn(Optional.empty());

        ResultadoInicioSesion resultado = autenticacion.verificarCodigoEmail(
                new VerificarCodigoEmailCommand("juan@uclm.es", CODIGO));

        assertEquals(PasoLogin.ALTA_AUTHENTICATOR, resultado.siguientePaso());
    }

    @Test
    @DisplayName("HU2-CP22: con un alta de Authenticator sin confirmar vuelve al alta")
    void conAltaSinConfirmarVuelveAlAlta() {
        // Escaneó el QR pero nunca introdujo el primer código: tiene que poder volver a darse de alta
        prepararCodigoCorrecto();
        when(factorRepository.buscarPorUsuario("u-1"))
                .thenReturn(Optional.of(FactorAutenticacion.nuevo("u-1", "secreto-cifrado")));

        ResultadoInicioSesion resultado = autenticacion.verificarCodigoEmail(
                new VerificarCodigoEmailCommand("juan@uclm.es", CODIGO));

        assertEquals(PasoLogin.ALTA_AUTHENTICATOR, resultado.siguientePaso());
    }

    // Usuario con un código del correo pendiente y correcto (paso 2 superado)
    private void prepararCodigoCorrecto() {
        when(usuarioRepository.buscarPorCorreo("juan@uclm.es")).thenReturn(Optional.of(clienteConId()));
        when(codigoRepository.buscarPorUsuario("u-1"))
                .thenReturn(Optional.of(CodigoVerificacion.nuevo("u-1", HASH_CODIGO, AHORA)));
        when(hashContrasena.coincide(CODIGO, HASH_CODIGO)).thenReturn(true);
    }

    @Test
    @DisplayName("HU2-CP07: rechaza un código incorrecto")
    void rechazaCodigoIncorrecto() {
        when(usuarioRepository.buscarPorCorreo("juan@uclm.es")).thenReturn(Optional.of(clienteConId()));
        CodigoVerificacion codigo = CodigoVerificacion.nuevo("u-1", HASH_CODIGO, AHORA);
        when(codigoRepository.buscarPorUsuario("u-1")).thenReturn(Optional.of(codigo));
        when(hashContrasena.coincide("000000", HASH_CODIGO)).thenReturn(false);
        VerificarCodigoEmailCommand comando = new VerificarCodigoEmailCommand("juan@uclm.es", "000000");

        assertThrows(CodigoInvalidoException.class, () -> autenticacion.verificarCodigoEmail(comando));

        // El fallo se cuenta y se guarda: si no, el límite de 5 intentos (CP10) no funcionaría
        // entre una petición y otra.
        assertEquals(1, codigo.getIntentosFallidos());
        verify(codigoRepository).guardar(codigo);
    }

    @Test
    @DisplayName("HU2-CP08: rechaza un código caducado aunque sea correcto")
    void rechazaCodigoCaducado() {
        when(usuarioRepository.buscarPorCorreo("juan@uclm.es")).thenReturn(Optional.of(clienteConId()));
        // Creado hace exactamente 10 minutos: ya ha caducado (frontera del dominio, ciclo 2).
        CodigoVerificacion codigo = CodigoVerificacion.nuevo("u-1", HASH_CODIGO, AHORA.minus(Duration.ofMinutes(10)));
        when(codigoRepository.buscarPorUsuario("u-1")).thenReturn(Optional.of(codigo));
        // El código es correcto: solo se rechaza por caducado. lenient porque, una vez comprobada la
        // caducidad, el servicio ya no llega a comparar el código.
        lenient().when(hashContrasena.coincide(CODIGO, HASH_CODIGO)).thenReturn(true);
        VerificarCodigoEmailCommand comando = new VerificarCodigoEmailCommand("juan@uclm.es", CODIGO);

        assertThrows(CodigoInvalidoException.class, () -> autenticacion.verificarCodigoEmail(comando));

        // Un código caducado se rechaza sin contar el intento ni guardar nada.
        verify(codigoRepository, never()).guardar(any());
    }

    @Test
    @DisplayName("HU2-CP09: rechaza un código ya usado")
    void rechazaCodigoYaUsado() {
        when(usuarioRepository.buscarPorCorreo("juan@uclm.es")).thenReturn(Optional.of(clienteConId()));
        CodigoVerificacion codigo = CodigoVerificacion.nuevo("u-1", HASH_CODIGO, AHORA);
        codigo.marcarUsado(AHORA);
        when(codigoRepository.buscarPorUsuario("u-1")).thenReturn(Optional.of(codigo));
        // El código es correcto: solo se rechaza por estar usado (lenient: no se llega a comparar).
        lenient().when(hashContrasena.coincide(CODIGO, HASH_CODIGO)).thenReturn(true);
        VerificarCodigoEmailCommand comando = new VerificarCodigoEmailCommand("juan@uclm.es", CODIGO);

        assertThrows(CodigoInvalidoException.class, () -> autenticacion.verificarCodigoEmail(comando));
        verify(codigoRepository, never()).guardar(any());
    }

    @Test
    @DisplayName("HU2-CP10: rechaza un código agotado aunque sea correcto")
    void rechazaCodigoAgotadoAunqueSeaCorrecto() {
        when(usuarioRepository.buscarPorCorreo("juan@uclm.es")).thenReturn(Optional.of(clienteConId()));
        CodigoVerificacion codigo = CodigoVerificacion.nuevo("u-1", HASH_CODIGO, AHORA);
        for (int intento = 0; intento < 5; intento++) {
            codigo.registrarIntentoFallido();
        }
        when(codigoRepository.buscarPorUsuario("u-1")).thenReturn(Optional.of(codigo));
        // El código es correcto: solo se rechaza por estar agotado (lenient: no se llega a comparar).
        lenient().when(hashContrasena.coincide(CODIGO, HASH_CODIGO)).thenReturn(true);
        VerificarCodigoEmailCommand comando = new VerificarCodigoEmailCommand("juan@uclm.es", CODIGO);

        assertThrows(CodigoInvalidoException.class, () -> autenticacion.verificarCodigoEmail(comando));

        // Agotado no suma más intentos ni se guarda: el código ya no vale y hay que repetir el login.
        assertEquals(5, codigo.getIntentosFallidos());
        verify(codigoRepository, never()).guardar(any());
    }

    @Test
    @DisplayName("HU2-CP07: rechaza la verificación si no hay código pendiente")
    void rechazaSiNoHayCodigoPendiente() {
        // El usuario existe pero no tiene código guardado: no ha pasado el paso 1 (el backend impone el orden).
        when(usuarioRepository.buscarPorCorreo("juan@uclm.es")).thenReturn(Optional.of(clienteConId()));
        when(codigoRepository.buscarPorUsuario("u-1")).thenReturn(Optional.empty());
        VerificarCodigoEmailCommand sinCodigo = new VerificarCodigoEmailCommand("juan@uclm.es", CODIGO);
        // Un correo que no existe tampoco tiene código pendiente: misma respuesta, para no revelar qué cuentas existen.
        when(usuarioRepository.buscarPorCorreo("nadie@uclm.es")).thenReturn(Optional.empty());
        VerificarCodigoEmailCommand sinUsuario = new VerificarCodigoEmailCommand("nadie@uclm.es", CODIGO);

        assertThrows(CodigoInvalidoException.class, () -> autenticacion.verificarCodigoEmail(sinCodigo));
        assertThrows(CodigoInvalidoException.class, () -> autenticacion.verificarCodigoEmail(sinUsuario));
    }

    @Test
    @DisplayName("HU2-CP16: rechaza el acceso a una cuenta en estado PENDIENTE")
    void rechazaCuentaPendiente() {
        // Una cuenta nueva nace en estado PENDIENTE (T233); clienteNuevo() la crea con los datos del registro (T229)
        Usuario usuario = clienteNuevo();
        when(usuarioRepository.buscarPorCorreo("juan@uclm.es")).thenReturn(Optional.of(usuario));
        
        // Se comprueba después de validar la contraseña para no revelar el estado de la cuenta a atacantes
        when(hashContrasena.coincide("clave1234", "hash-guardado")).thenReturn(true);
        IniciarSesionCommand comando = new IniciarSesionCommand("juan@uclm.es", "clave1234");
        assertThrows(CuentaPendienteException.class, () -> autenticacion.iniciarSesion(comando));
    }

    @Test
    @DisplayName("HU2-CP14: con sesión iniciada devuelve el usuario de esa sesión")
    void consultaElUsuarioEnSesion() {
        // GET /api/auth/me (T230): el controlador pasa el correo del usuario autenticado y el servicio lo busca.
        // Devuelve el Usuario del dominio; el controlador elige qué campos salen en el JSON (DTO).
        Usuario usuario = clienteConId();
        when(usuarioRepository.buscarPorCorreo("juan@uclm.es")).thenReturn(Optional.of(usuario));

        Usuario enSesion = autenticacion.consultarUsuarioEnSesion("juan@uclm.es");

        assertSame(usuario, enSesion);
    }

    @Test
    @DisplayName("HU32-CP01: devuelve solo el id del usuario en sesión, para otros módulos")
    void consultaElIdDelUsuarioEnSesion() {
        // Productos (HU32) guarda el id del vendedor, pero la sesión solo tiene su correo.
        when(usuarioRepository.buscarPorCorreo("juan@uclm.es")).thenReturn(Optional.of(clienteConId()));

        assertEquals("u-1", autenticacion.consultarIdUsuarioEnSesion("juan@uclm.es"));
    }

    // Fallos de la demo del Sprint 1: el paso 1 busca la cuenta con el email normalizado (T240).

    @Test
    @DisplayName("HU2-CP25: inicia sesión aunque el email lleve mayúsculas o espacios")
    void iniciaSesionAunqueElEmailLleveMayusculas() {
        Usuario usuario = clienteConId();
        when(usuarioRepository.buscarPorCorreo("juan@uclm.es")).thenReturn(Optional.of(usuario));
        when(hashContrasena.coincide("clave1234", "hash-guardado")).thenReturn(true);

        ResultadoInicioSesion resultado = autenticacion.iniciarSesion(new IniciarSesionCommand(" Juan@UCLM.es ", "clave1234"));

        assertEquals("juan@uclm.es", resultado.email());
        assertEquals(PasoLogin.CODIGO_EMAIL, resultado.siguientePaso());
    }

    @Test
    @DisplayName("HU2-CP25: un email mal formado en el login responde como unas credenciales inválidas")
    void unEmailMalFormadoEnElLoginNoRevelaNada() {
        // Sin stubs: con un email mal formado no se llega a consultar el repositorio
        assertThrows(CredencialesInvalidasException.class,
                () -> autenticacion.iniciarSesion(new IniciarSesionCommand("ana@", "clave1234")));
    }

    @Test
    @DisplayName("HU2-CP26: acertar la contraseña no reinicia el contador de intentos")
    void acertarLaContrasenaNoReiniciaElContador() {
        // Frontera de la decisión: el contador solo se reinicia al completar el tercer paso. Si se reiniciara aquí,
        // quien tuviera la contraseña podría probar códigos sin que el contador llegara nunca a 5.
        Usuario usuario = clienteConId();
        usuario.registrarIntentoFallido(AHORA);
        usuario.registrarIntentoFallido(AHORA);
        usuario.registrarIntentoFallido(AHORA);
        when(usuarioRepository.buscarPorCorreo("juan@uclm.es")).thenReturn(Optional.of(usuario));
        when(hashContrasena.coincide("clave1234", "hash-guardado")).thenReturn(true);

        autenticacion.iniciarSesion(new IniciarSesionCommand("juan@uclm.es", "clave1234"));

        assertEquals(3, usuario.getIntentosFallidos());
    }
}
