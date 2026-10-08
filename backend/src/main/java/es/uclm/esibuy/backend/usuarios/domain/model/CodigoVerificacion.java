package es.uclm.esibuy.backend.usuarios.domain.model;

import java.time.Duration;
import java.time.Instant;

import es.uclm.esibuy.backend.usuarios.domain.exception.CodigoVerificacionInvalidoException;

/**
 * Código de verificación del segundo factor del login (HU2, T21): el código de 6 dígitos que el
 * backend envía por correo tras validar la contraseña y que está pendiente de que el usuario lo
 * introduzca.
 *
 * <p>Recuerda entre el paso 1 ({@code iniciarSesion}) y el paso 2 ({@code verificarCodigoEmail})
 * de quién es el código, su hash, hasta cuándo vale, si ya se ha usado y cuántas veces se ha
 * fallado. Solo guarda el hash, nunca el código en claro: si alguien leyera la base de datos no
 * podría usar los códigos pendientes.
 *
 * <p>Reglas (plan de la T21, D3): vale 10 minutos, es de un solo uso y deja de valer tras 5
 * intentos fallidos, incluso con el valor correcto. Viven aquí, en el dominio, y no en
 * {@code AutenticacionService}, igual que el bloqueo por intentos vive en {@link Usuario}: el
 * servicio solo pregunta {@code puedeUsarse(ahora)} y no puede saltarse ninguna regla.
 *
 * <p>Recibe la hora como parámetro en vez de consultar {@code Instant.now()}, para que los tests
 * puedan fijarla y probar las fronteras (a los 9:59 vale; a los 10:00, no).
 */
public class CodigoVerificacion {

    private static final Duration VALIDEZ = Duration.ofMinutes(10);
    private static final int MAX_INTENTOS_FALLIDOS = 5;

    private final String usuarioId;
    private final String codigoHash;
    private final Instant expiraEn;
    private boolean usado;
    private int intentosFallidos;

    private CodigoVerificacion(String usuarioId, String codigoHash, Instant expiraEn) {
        this.usuarioId = exigirNoNulo(usuarioId, "El id del usuario es obligatorio");
        this.codigoHash = exigirNoNulo(codigoHash, "El hash del código es obligatorio");
        this.expiraEn = expiraEn;
    }

    public static CodigoVerificacion nuevo(String usuarioId, String codigoHash, Instant ahora) {
        return new CodigoVerificacion(usuarioId, codigoHash, ahora.plus(VALIDEZ));
    }

    private static String exigirNoNulo(String valor, String mensaje) {
        if (valor == null) {
            throw new CodigoVerificacionInvalidoException(mensaje);
        }
        return valor;
    }

    public boolean puedeUsarse(Instant ahora) {
        return !usado && intentosFallidos < MAX_INTENTOS_FALLIDOS && ahora.isBefore(expiraEn);
    }

    public void marcarUsado() {
        usado = true;
    }

    public void registrarIntentoFallido() {
        intentosFallidos++;
    }

    public String getCodigoHash() {
        return codigoHash;
    }

    public int getIntentosFallidos() {
        return intentosFallidos;
    }

    public String getUsuarioId() {
        return usuarioId;
    }
}
