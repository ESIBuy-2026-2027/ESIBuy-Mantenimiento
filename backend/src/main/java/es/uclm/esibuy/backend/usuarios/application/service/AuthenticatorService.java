package es.uclm.esibuy.backend.usuarios.application.service;

import java.time.Clock;
import java.time.Instant;
import java.util.OptionalLong;

import es.uclm.esibuy.backend.usuarios.application.port.in.AltaAuthenticator;
import es.uclm.esibuy.backend.usuarios.application.port.in.AuthenticatorUseCase;
import es.uclm.esibuy.backend.usuarios.application.port.in.VerificarCodigoAuthenticatorCommand;
import es.uclm.esibuy.backend.usuarios.application.port.out.CifradoSecretoPort;
import es.uclm.esibuy.backend.usuarios.application.port.out.FactorAutenticacionRepositoryPort;
import es.uclm.esibuy.backend.usuarios.application.port.out.TotpPort;
import es.uclm.esibuy.backend.usuarios.application.port.out.UsuarioRepositoryPort;
import es.uclm.esibuy.backend.usuarios.domain.exception.AuthenticatorYaDadoDeAltaException;
import es.uclm.esibuy.backend.usuarios.domain.exception.CodigoInvalidoException;
import es.uclm.esibuy.backend.usuarios.domain.exception.CredencialesInvalidasException;
import es.uclm.esibuy.backend.usuarios.domain.exception.CuentaBloqueadaException;
import es.uclm.esibuy.backend.usuarios.domain.model.FactorAutenticacion;
import es.uclm.esibuy.backend.usuarios.domain.model.Usuario;

/**
 * Paso 3 del login con Microsoft Authenticator (HU2, T20): alta del secreto con QR y verificación del código.
 *
 * <p>Va aparte de {@code AutenticacionService} (pasos 1 y 2 y {@code /me}) por dos motivos:
 * <ul>
 *   <li><b>Propósito propio:</b> el CLAUDE.md agrupa los puertos de entrada por actor y propósito, y si un
 *   puerto mezcla intenciones distintas se divide. Gestionar y verificar el Authenticator (su secreto, su
 *   alta y su código) es un propósito distinto de comprobar la contraseña y el código del correo.</li>
 *   <li><b>Tamaño:</b> metido en {@code AutenticacionService}, su constructor pasaría de 7 a 9 dependencias
 *   (TOTP y cifrado), por encima del límite de Sonar ({@code java:S107}). Aquí bastan 5.</li>
 * </ul>
 * El controlador del login usa los dos puertos ({@code AutenticacionUseCase} y {@code AuthenticatorUseCase}).
 */
public class AuthenticatorService implements AuthenticatorUseCase {

    private static final String MENSAJE_CREDENCIALES_INVALIDAS = "Credenciales inválidas";
    private static final String MENSAJE_CODIGO_INVALIDO = "Código inválido";

    private final UsuarioRepositoryPort usuarioRepository;
    private final FactorAutenticacionRepositoryPort factorAutenticacionRepository;
    private final TotpPort totp;
    private final CifradoSecretoPort cifrado;
    private final Clock reloj;

    public AuthenticatorService(UsuarioRepositoryPort usuarioRepository,
            FactorAutenticacionRepositoryPort factorAutenticacionRepository, TotpPort totp,
            CifradoSecretoPort cifrado, Clock reloj) {
        this.usuarioRepository = usuarioRepository;
        this.factorAutenticacionRepository = factorAutenticacionRepository;
        this.totp = totp;
        this.cifrado = cifrado;
        this.reloj = reloj;
    }

    @Override
    public AltaAuthenticator prepararAlta(String email) {
        Usuario usuario = usuarioRepository.buscarPorCorreo(email)
                .orElseThrow(() -> new CredencialesInvalidasException(MENSAJE_CREDENCIALES_INVALIDAS));

        // Solo puede darse de alta quien no tiene un Authenticator confirmado: si no, quien supiera la
        // contraseña y tuviera el correo podría dar de alta su móvil y saltarse el tercer factor
        boolean yaConfirmado = factorAutenticacionRepository.buscarPorUsuario(usuario.getId())
                .map(factor -> factor.estaConfirmado())
                .orElse(false);
        if (yaConfirmado) {
            throw new AuthenticatorYaDadoDeAltaException("El usuario ya tiene Authenticator");
        }

        String secreto = totp.generarSecreto();
        // Se guarda cifrado y sin confirmar: el alta se confirma con el primer código correcto (verificarCodigo)
        factorAutenticacionRepository.guardar(FactorAutenticacion.nuevo(usuario.getId(), cifrado.cifrar(secreto)));
        return new AltaAuthenticator(totp.generarQr(email, secreto));
    }

    @Override
    public Usuario verificarCodigo(VerificarCodigoAuthenticatorCommand comando) {
        Usuario usuario = usuarioRepository.buscarPorCorreo(comando.email())
                .orElseThrow(() -> new CodigoInvalidoException(MENSAJE_CODIGO_INVALIDO));

        // Con la cuenta bloqueada por intentos ni siquiera se calcula el código, como en el paso 1
        Instant ahora = reloj.instant();
        if (usuario.tieneBloqueoPorIntentos(ahora)) {
            throw new CuentaBloqueadaException("Demasiados intentos. Inténtalo más tarde");
        }

        FactorAutenticacion factor = factorAutenticacionRepository.buscarPorUsuario(usuario.getId())
                .orElseThrow(() -> new CodigoInvalidoException(MENSAJE_CODIGO_INVALIDO));

        // El secreto solo existe en claro en memoria, durante esta llamada
        String secreto = cifrado.descifrar(factor.getSecretoCifrado());
        OptionalLong periodo = totp.periodoDelCodigo(secreto, comando.codigo(), ahora);

        // Incorrecto, o correcto pero ya usado: el mismo 401, y cuenta para el bloqueo del usuario (T20, D5 y D7)
        if (periodo.isEmpty() || !factor.puedeUsarPeriodo(periodo.getAsLong())) {
            usuario.registrarIntentoFallido(ahora);
            usuarioRepository.guardar(usuario);
            throw new CodigoInvalidoException(MENSAJE_CODIGO_INVALIDO);
        }

        // Si era el primer código tras el alta, la confirma
        factor.registrarUso(periodo.getAsLong(), ahora);
        factorAutenticacionRepository.guardar(factor);

        // Login completado: la cuenta recupera sus 5 intentos (fallos de la demo del Sprint 1). Se reinicia aquí
        // y no al acertar la contraseña: quien tenga solo la contraseña no debe poder probar códigos sin límite.
        usuario.reiniciarIntentos();
        usuarioRepository.guardar(usuario);
        return usuario;
    }
}
