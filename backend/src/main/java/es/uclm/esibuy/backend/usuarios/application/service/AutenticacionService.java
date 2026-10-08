package es.uclm.esibuy.backend.usuarios.application.service;

import java.time.Clock;
import java.time.Instant;

import es.uclm.esibuy.backend.usuarios.application.port.in.AutenticacionUseCase;
import es.uclm.esibuy.backend.usuarios.application.port.in.IniciarSesionCommand;
import es.uclm.esibuy.backend.usuarios.application.port.in.PasoLogin;
import es.uclm.esibuy.backend.usuarios.application.port.in.ResultadoInicioSesion;
import es.uclm.esibuy.backend.usuarios.application.port.out.HashContrasenaPort;
import es.uclm.esibuy.backend.usuarios.application.port.out.UsuarioRepositoryPort;
import es.uclm.esibuy.backend.usuarios.domain.exception.CredencialesInvalidasException;
import es.uclm.esibuy.backend.usuarios.domain.exception.CuentaBloqueadaException;
import es.uclm.esibuy.backend.usuarios.domain.exception.CuentaDesactivadaException;
import es.uclm.esibuy.backend.usuarios.domain.model.Usuario;

public class AutenticacionService implements AutenticacionUseCase {

    private static final String MENSAJE_CREDENCIALES_INVALIDAS = "Credenciales inválidas";

    private final UsuarioRepositoryPort usuarioRepository;
    private final HashContrasenaPort hashContrasena;
    private final Clock reloj;

    public AutenticacionService(UsuarioRepositoryPort usuarioRepository, HashContrasenaPort hashContrasena, Clock reloj) {
        this.usuarioRepository = usuarioRepository;
        this.hashContrasena = hashContrasena;
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

        if (usuario.estaDesactivado()) {
            throw new CuentaDesactivadaException("Esta cuenta está desactivada"); // <- No es demasiado verboso porque para llegar a este login necesitas haber pasado la contraseña
        }

        return new ResultadoInicioSesion(usuario.getEmail().valor(), PasoLogin.CODIGO_EMAIL);
    }

    

}
