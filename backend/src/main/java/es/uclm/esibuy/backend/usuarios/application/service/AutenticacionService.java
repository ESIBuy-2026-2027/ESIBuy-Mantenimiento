package es.uclm.esibuy.backend.usuarios.application.service;

import java.time.Clock;
import java.time.Instant;

import es.uclm.esibuy.backend.usuarios.application.port.in.AutenticacionUseCase;
import es.uclm.esibuy.backend.usuarios.application.port.in.IniciarSesionCommand;
import es.uclm.esibuy.backend.usuarios.application.port.in.PasoLogin;
import es.uclm.esibuy.backend.usuarios.application.port.in.ResultadoInicioSesion;
import es.uclm.esibuy.backend.usuarios.application.port.in.VerificarCodigoEmailCommand;
import es.uclm.esibuy.backend.usuarios.application.port.out.CodigoVerificacionRepositoryPort;
import es.uclm.esibuy.backend.usuarios.application.port.out.EnvioEmailPort;
import es.uclm.esibuy.backend.usuarios.application.port.out.GeneradorCodigoPort;
import es.uclm.esibuy.backend.usuarios.application.port.out.HashContrasenaPort;
import es.uclm.esibuy.backend.usuarios.application.port.out.UsuarioRepositoryPort;
import es.uclm.esibuy.backend.usuarios.domain.exception.CodigoInvalidoException;
import es.uclm.esibuy.backend.usuarios.domain.exception.CredencialesInvalidasException;
import es.uclm.esibuy.backend.usuarios.domain.exception.CuentaBloqueadaException;
import es.uclm.esibuy.backend.usuarios.domain.exception.CuentaDesactivadaException;
import es.uclm.esibuy.backend.usuarios.domain.model.CodigoVerificacion;
import es.uclm.esibuy.backend.usuarios.domain.model.Usuario;

public class AutenticacionService implements AutenticacionUseCase {

    private static final String MENSAJE_CREDENCIALES_INVALIDAS = "Credenciales inválidas";
    private static final String CODIGO_INVALIDO = "Código inválido";

    private final UsuarioRepositoryPort usuarioRepository;
    private final HashContrasenaPort hashContrasena;
    private final CodigoVerificacionRepositoryPort codigoVerificacionRepository;
    private final GeneradorCodigoPort generadorCodigo;
    private final EnvioEmailPort envioEmail;
    private final Clock reloj;

    public AutenticacionService(UsuarioRepositoryPort usuarioRepository, HashContrasenaPort hashContrasena,
            CodigoVerificacionRepositoryPort codigoVerificacionRepository, GeneradorCodigoPort generadorCodigo,
            EnvioEmailPort envioEmail, Clock reloj) {
        this.usuarioRepository = usuarioRepository;
        this.hashContrasena = hashContrasena;
        this.codigoVerificacionRepository = codigoVerificacionRepository;
        this.generadorCodigo = generadorCodigo;
        this.envioEmail = envioEmail;
        this.reloj = reloj;
    }

    @Override
    public ResultadoInicioSesion iniciarSesion(IniciarSesionCommand comando) {
        
        Usuario usuario = usuarioRepository.buscarPorCorreo(comando.email()).orElseThrow(() -> new CredencialesInvalidasException(MENSAJE_CREDENCIALES_INVALIDAS)); // <- Este orElseThrow() viene de buscarPorCorreo() que devuelve Optional<>

        Instant ahora = reloj.instant();
        if (usuario.tieneBloqueoPorIntentos(ahora)) {
            throw new CuentaBloqueadaException("Demasiados intentos. Inténtalo más tarde");
        }

        if (!hashContrasena.coincide(comando.password(), usuario.getPasswordHash())) {
            usuario.registrarIntentoFallido(ahora);
            usuarioRepository.guardar(usuario);
            throw new CredencialesInvalidasException(MENSAJE_CREDENCIALES_INVALIDAS);
        }

        if (!usuario.estaActivo()) {
            throw new CuentaDesactivadaException("Esta cuenta está desactivada"); // <- No es demasiado verboso porque para llegar a este login necesitas haber pasado la contraseña
        }

        String codigo = generadorCodigo.generar();
        String codigoHash = hashContrasena.hashear(codigo);
        codigoVerificacionRepository.guardar(CodigoVerificacion.nuevo(usuario.getId(), codigoHash, ahora));
        envioEmail.enviarCodigoVerificacion(usuario.getEmail().valor(), codigo);

        return new ResultadoInicioSesion(usuario.getEmail().valor(), PasoLogin.CODIGO_EMAIL);
    }

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
        return new ResultadoInicioSesion(usuario.getEmail().valor(), PasoLogin.AUTHENTICATOR);
    }
    

}
