package es.uclm.esibuy.backend.usuarios.application.service;

import java.time.Clock;
import java.util.Optional;

import org.springframework.security.access.prepost.PreAuthorize;

import es.uclm.esibuy.backend.shared.seguridad.Permisos;
import es.uclm.esibuy.backend.usuarios.application.port.in.RecuperacionContrasenaUseCase;
import es.uclm.esibuy.backend.usuarios.application.port.in.RestablecerContrasenaCommand;
import es.uclm.esibuy.backend.usuarios.application.port.out.CodigoRecuperacionRepositoryPort;
import es.uclm.esibuy.backend.usuarios.application.port.out.EnvioEmailPort;
import es.uclm.esibuy.backend.usuarios.application.port.out.GeneradorCodigoPort;
import es.uclm.esibuy.backend.usuarios.application.port.out.HashContrasenaPort;
import es.uclm.esibuy.backend.usuarios.application.port.out.UsuarioRepositoryPort;
import es.uclm.esibuy.backend.usuarios.application.port.out.exception.EnvioEmailException;
import es.uclm.esibuy.backend.usuarios.domain.exception.CodigoInvalidoException;
import es.uclm.esibuy.backend.usuarios.domain.exception.EmailInvalidoException;
import es.uclm.esibuy.backend.usuarios.domain.model.CodigoVerificacion;
import es.uclm.esibuy.backend.usuarios.domain.model.Email;
import es.uclm.esibuy.backend.usuarios.domain.model.PoliticaContrasenas;
import es.uclm.esibuy.backend.usuarios.domain.model.Usuario;

public class RecuperacionContrasenaService implements RecuperacionContrasenaUseCase {

    private static final String CODIGO_INVALIDO = "Código inválido";

    private final UsuarioRepositoryPort usuarioRepository;
    private final HashContrasenaPort hashContrasena;
    private final CodigoRecuperacionRepositoryPort codigoRepository;
    private final GeneradorCodigoPort generadorCodigo;
    private final EnvioEmailPort envioEmail;
    private final Clock reloj;

    public RecuperacionContrasenaService(UsuarioRepositoryPort usuarioRepository, HashContrasenaPort hashContrasena,
            CodigoRecuperacionRepositoryPort codigoRepository, GeneradorCodigoPort generadorCodigo,
            EnvioEmailPort envioEmail, Clock reloj) {
        this.usuarioRepository = usuarioRepository;
        this.hashContrasena = hashContrasena;
        this.codigoRepository = codigoRepository;
        this.generadorCodigo = generadorCodigo;
        this.envioEmail = envioEmail;
        this.reloj = reloj;
    }

    // HU3-CP02 y CP03: la respuesta es siempre la misma, exista o no la cuenta, para no revelar qué emails hay
    @Override
    @PreAuthorize(Permisos.PUBLICO)
    public void solicitarRecuperacion(String email) {
        Optional<String> normalizado = normalizar(email);
        if (normalizado.isEmpty()) {
            return;
        }
        Optional<Usuario> cuenta = usuarioRepository.buscarPorCorreo(normalizado.get());
        if (cuenta.isEmpty() || !cuenta.get().estaActivo()) {
            return;
        }
        Usuario usuario = cuenta.get();

        String codigo = generadorCodigo.generar();
        String hash = hashContrasena.hashear(codigo);
        codigoRepository.guardar(CodigoVerificacion.nuevo(usuario.getId(), hash, reloj.instant()));
        try {
            envioEmail.enviarCodigoRecuperacion(normalizado.get(), codigo);
        } catch (EnvioEmailException e) {
            // HU3-CP11: se ignora a propósito. Con un email desconocido no se intenta enviar nada y la respuesta
            // es correcta; si el fallo del envío llegara al cliente, solo pasaría con cuentas que existen y lo
            // revelaría. El usuario puede volver a pedir el código.
        }
    }

    // HU3-CP04 a CP07. El orden importa: la política se comprueba después del código y antes de guardar nada.
    @Override
    @PreAuthorize(Permisos.PUBLICO)
    public void restablecerContrasena(RestablecerContrasenaCommand comando) {
        String email = normalizar(comando.email()).orElseThrow(RecuperacionContrasenaService::codigoInvalido);
        // Email desconocido: misma respuesta que un código incorrecto, para no revelar qué emails existen
        Usuario usuario = usuarioRepository.buscarPorCorreo(email)
                .orElseThrow(RecuperacionContrasenaService::codigoInvalido);
        CodigoVerificacion codigo = codigoRepository.buscarPorUsuario(usuario.getId())
                .orElseThrow(RecuperacionContrasenaService::codigoInvalido);

        // Caducado, ya usado o con 5 fallos
        if (!codigo.puedeUsarse(reloj.instant())) {
            throw codigoInvalido();
        }
        if (!hashContrasena.coincide(comando.codigo(), codigo.getCodigoHash())) {
            codigo.registrarIntentoFallido();
            codigoRepository.guardar(codigo);
            throw codigoInvalido();
        }

        // Si no cumple la política lanza ContrasenaInvalidaException y el código no se gasta (HU3-CP07)
        PoliticaContrasenas.validar(comando.password(), usuario.getDatosPersonales(), usuario.getEmail());

        usuario.cambiarContrasena(hashContrasena.hashear(comando.password()));
        codigo.marcarUsado(reloj.instant());
        usuarioRepository.guardar(usuario);
        codigoRepository.guardar(codigo);
    }

    // Como emailNormalizado de AutenticacionService, pero sin lanzar: cada caso de uso decide qué hacer
    private static Optional<String> normalizar(String email) {
        try {
            return Optional.of(new Email(email).valor());
        } catch (EmailInvalidoException e) {
            return Optional.empty();
        }
    }

    private static CodigoInvalidoException codigoInvalido() {
        return new CodigoInvalidoException(CODIGO_INVALIDO);
    }
}
