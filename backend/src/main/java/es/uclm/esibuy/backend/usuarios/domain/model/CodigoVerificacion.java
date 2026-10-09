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

    // Id del documento en MongoDB; null en un código nuevo, que todavía no se ha guardado (T232).
    private final String id;
    private final String usuarioId;
    private final String codigoHash;
    private final Instant expiraEn;

    // Hora en que se aceptó el código; null = sin usar. Se guarda la hora y no solo un «usado»
    // para saber cuándo se usó cada código (trazabilidad de la autenticación, T232). En MongoDB, usedAt.
    private Instant usadoEn;
    private int intentosFallidos;

    private CodigoVerificacion(String id, String usuarioId, String codigoHash, Instant expiraEn) {
        this.id = id;
        this.usuarioId = exigirNoNulo(usuarioId, "El id del usuario es obligatorio");
        this.codigoHash = exigirNoNulo(codigoHash, "El hash del código es obligatorio");
        this.expiraEn = expiraEn;
    }

    public static CodigoVerificacion nuevo(String usuarioId, String codigoHash, Instant ahora) {
        return new CodigoVerificacion(null, usuarioId, codigoHash, ahora.plus(VALIDEZ));
    }

    /**
     * Reconstruye un código ya guardado, con su estado (uso e intentos fallidos). Lo usa la
     * persistencia al leer de la base de datos (T232); un código nuevo se crea con {@link #nuevo}.
     * Pasa por el mismo constructor, así que se aplican las mismas validaciones.
     */
    public static CodigoVerificacion reconstruir(String id, String usuarioId, String codigoHash, Instant expiraEn,
            Instant usadoEn, int intentosFallidos) {
        if (intentosFallidos < 0) {
            throw new CodigoVerificacionInvalidoException("Los intentos fallidos no pueden ser negativos");
        }
        CodigoVerificacion codigo = new CodigoVerificacion(id, usuarioId, codigoHash, expiraEn);
        codigo.usadoEn = usadoEn;
        codigo.intentosFallidos = intentosFallidos;
        return codigo;
    }

    private static String exigirNoNulo(String valor, String mensaje) {
        if (valor == null) {
            throw new CodigoVerificacionInvalidoException(mensaje);
        }
        return valor;
    }

    public boolean puedeUsarse(Instant ahora) {
        return usadoEn == null && intentosFallidos < MAX_INTENTOS_FALLIDOS && ahora.isBefore(expiraEn);
    }

    public void marcarUsado(Instant instante) {
        usadoEn = instante;
    }

    public void registrarIntentoFallido() {
        intentosFallidos++;
    }

    public String getId() {
        return id;
    }

    public String getCodigoHash() {
        return codigoHash;
    }

    public Instant getExpiraEn() {
        return expiraEn;
    }

    public int getIntentosFallidos() {
        return intentosFallidos;
    }

    public String getUsuarioId() {
        return usuarioId;
    }

    public Instant getUsadoEn() {
        return usadoEn;
    }

    
}
