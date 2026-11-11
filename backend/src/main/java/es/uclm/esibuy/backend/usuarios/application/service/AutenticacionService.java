package es.uclm.esibuy.backend.usuarios.application.service;

import java.time.Clock;
import java.time.Instant;

import org.springframework.security.access.prepost.PreAuthorize;

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
import es.uclm.esibuy.backend.usuarios.domain.exception.BloqueoPorIntentosException;
import es.uclm.esibuy.backend.usuarios.domain.exception.CuentaBloqueadaException;
import es.uclm.esibuy.backend.usuarios.domain.exception.EmailInvalidoException;
import es.uclm.esibuy.backend.usuarios.domain.model.CodigoVerificacion;
import es.uclm.esibuy.backend.usuarios.domain.model.DatosCliente;
import es.uclm.esibuy.backend.usuarios.domain.model.Email;
import es.uclm.esibuy.backend.usuarios.domain.model.TipoCliente;
import es.uclm.esibuy.backend.usuarios.domain.model.Usuario;
import es.uclm.esibuy.backend.usuarios.domain.exception.CuentaPendienteException;
import es.uclm.esibuy.backend.shared.seguridad.Permisos;

public class AutenticacionService implements AutenticacionUseCase {

    private static final String MENSAJE_CREDENCIALES_INVALIDAS = "Credenciales inválidas";
    private static final String CODIGO_INVALIDO = "Código inválido";

    private final UsuarioRepositoryPort usuarioRepository;
    private final HashContrasenaPort hashContrasena;
    private final CodigoVerificacionRepositoryPort codigoVerificacionRepository;
    private final FactorAutenticacionRepositoryPort factorAutenticacionRepository;
    private final GeneradorCodigoPort generadorCodigo;
    private final EnvioEmailPort envioEmail;
    private final Clock reloj;

    public AutenticacionService(UsuarioRepositoryPort usuarioRepository, HashContrasenaPort hashContrasena,
            CodigoVerificacionRepositoryPort codigoVerificacionRepository, GeneradorCodigoPort generadorCodigo,
            EnvioEmailPort envioEmail, FactorAutenticacionRepositoryPort factorAutenticacionRepository, Clock reloj) {
        this.usuarioRepository = usuarioRepository;
        this.hashContrasena = hashContrasena;
        this.codigoVerificacionRepository = codigoVerificacionRepository;
        this.generadorCodigo = generadorCodigo;
        this.envioEmail = envioEmail;
        this.factorAutenticacionRepository = factorAutenticacionRepository;
        this.reloj = reloj;
    }

    @PreAuthorize(Permisos.PUBLICO)
    @Override
    public ResultadoInicioSesion iniciarSesion(IniciarSesionCommand comando) {

        // El email se busca normalizado (minúsculas, sin espacios), como se guardó en
        // el registro (T240, fallos de la demo)
        String email = emailNormalizado(comando.email());
        Usuario usuario = usuarioRepository.buscarPorCorreo(email)
                .orElseThrow(() -> new CredencialesInvalidasException(MENSAJE_CREDENCIALES_INVALIDAS)); // <- Este
                                                                                                        // orElseThrow()
                                                                                                        // viene de
                                                                                                        // buscarPorCorreo()
                                                                                                        // que devuelve
                                                                                                        // Optional<>

        Instant ahora = reloj.instant();
        if (usuario.tieneBloqueoPorIntentos(ahora)) {
            throw new BloqueoPorIntentosException("Demasiados intentos. Inténtalo más tarde");
        }

        if (!hashContrasena.coincide(comando.password(), usuario.getPasswordHash())) {
            usuario.registrarIntentoFallido(ahora);
            usuarioRepository.guardar(usuario);
            throw new CredencialesInvalidasException(MENSAJE_CREDENCIALES_INVALIDAS);
        }

        if (usuario.estaPendiente()) { // comprobamos que la cuenta haya sido activada por un administrador.
            throw new CuentaPendienteException("La cuenta está pendiente de verificación.");
        }

        if (!usuario.estaActivo()) {
            throw new CuentaBloqueadaException("Esta cuenta está bloqueada"); // <- No es demasiado verboso porque para
                                                                              // llegar a este login necesitas haber
                                                                              // pasado la contraseña
        }

        boolean emailOtpEnabled = usuario.isEmailOtpEnabled();
        boolean authenticatorEnabled = usuario.isAuthenticatorEnabled();

        // Si no hay factores adicionales activos, el login termina tras validar
        // las credenciales y el estado de la cuenta.
        if (!emailOtpEnabled && !authenticatorEnabled) {
            usuario.reiniciarIntentos();
            usuarioRepository.guardar(usuario);

            return new ResultadoInicioSesion(
                    usuario.getEmail().valor(),
                    PasoLogin.ACCESO_DIRECTO,
                    usuario);
        }

        // Si el OTP por correo está activo, se solicita primero.
        if (emailOtpEnabled) {
            String codigo = generadorCodigo.generar();
            String codigoHash = hashContrasena.hashear(codigo);

            codigoVerificacionRepository.guardar(
                    CodigoVerificacion.nuevo(usuario.getId(), codigoHash, ahora));

            envioEmail.enviarCodigoVerificacion(usuario.getEmail().valor(), codigo);

            return new ResultadoInicioSesion(
                    usuario.getEmail().valor(),
                    PasoLogin.CODIGO_EMAIL);
        }

        // Si solo está activo Authenticator, se solicita directamente.
        return new ResultadoInicioSesion(
                usuario.getEmail().valor(),
                PasoLogin.AUTHENTICATOR);
    }

    @PreAuthorize(Permisos.PUBLICO)
    @Override
    public ResultadoInicioSesion verificarCodigoEmail(VerificarCodigoEmailCommand comando) {
        Usuario usuario = usuarioRepository.buscarPorCorreo(comando.email())
                .orElseThrow(() -> new CodigoInvalidoException(CODIGO_INVALIDO));
        CodigoVerificacion codigo = codigoVerificacionRepository.buscarPorUsuario(usuario.getId())
                .orElseThrow(() -> new CodigoInvalidoException(CODIGO_INVALIDO));

        Instant ahora = reloj.instant();
        if (!codigo.puedeUsarse(ahora)) {
            throw new CodigoInvalidoException(CODIGO_INVALIDO);
        }

        if (!hashContrasena.coincide(comando.codigo(), codigo.getCodigoHash())) {
            codigo.registrarIntentoFallido();
            codigoVerificacionRepository.guardar(codigo);
            throw new CodigoInvalidoException(CODIGO_INVALIDO);
        }

        codigo.marcarUsado(ahora);
        codigoVerificacionRepository.guardar(codigo);
        PasoLogin siguientePaso = tieneAuthenticatorConfirmado(usuario)
                ? PasoLogin.AUTHENTICATOR
                : PasoLogin.ALTA_AUTHENTICATOR;
        return new ResultadoInicioSesion(usuario.getEmail().valor(), siguientePaso);
    }

    @PreAuthorize(Permisos.AUTENTICADO_PROPIO)
    @Override
    public Usuario consultarUsuarioEnSesion(String email) {
        return usuarioRepository.buscarPorCorreo(email)
                .orElseThrow(() -> new CredencialesInvalidasException(MENSAJE_CREDENCIALES_INVALIDAS));
    }

    @PreAuthorize(Permisos.CLIENTE_O_VENDEDOR_PROPIO)
    @Override
    public String consultarIdUsuarioEnSesion(String email) {
        return consultarUsuarioEnSesion(email).getId();
    }

    @PreAuthorize(Permisos.SOLO_CLIENTE_PROPIO_POR_EMAIL)
    @Override
    public boolean esClientePremium(String email) {
        DatosCliente datos = consultarUsuarioEnSesion(email).getDatosCliente();
        return datos != null && datos.tipoCliente() == TipoCliente.PREMIUM;
    }

    private boolean tieneAuthenticatorConfirmado(Usuario usuario) {
        return factorAutenticacionRepository.buscarPorUsuario(usuario.getId())
                .map(factor -> factor.estaConfirmado()).orElse(false);
    }

    // Un email mal formado responde igual que uno inexistente: 401 y nada más
    // (fallos de la demo del Sprint 1)
    private static String emailNormalizado(String email) {
        try {
            return new Email(email).valor();
        } catch (EmailInvalidoException e) {
            throw new CredencialesInvalidasException(MENSAJE_CREDENCIALES_INVALIDAS);
        }
    }
}
