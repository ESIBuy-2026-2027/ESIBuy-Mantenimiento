package es.uclm.esibuy.backend.usuarios.domain.model;

import java.time.Duration;
import java.time.Instant;

import es.uclm.esibuy.backend.usuarios.domain.exception.UsuarioInvalidoException;

public class Usuario {

    private static final int MAX_INTENTOS = 5;
    private static final Duration DURACION_BLOQUEO = Duration.ofMinutes(15);

    private final String id;
    private final DatosPersonales datosPersonales;
    private final Email email;
    private final String passwordHash;
    private final DatosRol datosRol;     // el rol sale de aquí: cliente, vendedor o administrador
    private boolean desactivado;         // desactivada por un administrador (HU23/HU24): no puede entrar
    private int intentosFallidos;        // intentos de login fallidos seguidos
    private Instant bloqueadoHasta;      // bloqueo temporal por intentos; null = sin bloqueo

    public Usuario(String id, DatosPersonales datosPersonales, Email email, String passwordHash,
            DatosRol datosRol) {
        this.id = id;
        this.datosPersonales = exigirNoNulo(datosPersonales, "Los datos personales son obligatorios");
        this.email = exigirNoNulo(email, "El email es obligatorio");
        this.passwordHash = exigirTexto(passwordHash, "La contraseña es obligatoria");
        this.datosRol = exigirNoNulo(datosRol, "El rol es obligatorio");
        this.desactivado = false;
        this.intentosFallidos = 0;
    }

    public static Usuario nuevoCliente(Email email, String passwordHash) {
        return new Usuario(null, DatosPersonales.sinDatos(), email, passwordHash,
                new DatosCliente(TipoCliente.ESTANDAR, null));
    }

    /**
     * Reconstruye un usuario ya guardado, con su estado de acceso (desactivación, intentos fallidos
     * y bloqueo temporal). Lo usa la persistencia al leer de la base de datos; un usuario nuevo se
     * crea con el constructor o con {@link #nuevoCliente}, que empiezan sin bloqueo.
     */
    public static Usuario reconstruir(Usuario datos, boolean desactivado, int intentosFallidos,
            Instant bloqueadoHasta) {
        if (intentosFallidos < 0) {
            throw new UsuarioInvalidoException("Los intentos fallidos no pueden ser negativos");
        }
        Usuario usuario = new Usuario(datos.id, datos.datosPersonales, datos.email, datos.passwordHash,
                datos.datosRol);
        usuario.desactivado = desactivado;
        usuario.intentosFallidos = intentosFallidos;
        usuario.bloqueadoHasta = bloqueadoHasta;
        return usuario;
    }

    private static <T> T exigirNoNulo(T valor, String mensaje) {
        if (valor == null) {
            throw new UsuarioInvalidoException(mensaje);
        }
        return valor;
    }

    private static String exigirTexto(String valor, String mensaje) {
        if (valor == null || valor.isBlank()) {
            throw new UsuarioInvalidoException(mensaje);
        }
        return valor;
    }

    public String getId() {
        return id;
    }

    public DatosPersonales getDatosPersonales() {
        return datosPersonales;
    }

    public String getNombre() {
        return datosPersonales.nombre();
    }

    public String getApellidos() {
        return datosPersonales.apellidos();
    }

    public Email getEmail() {
        return email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public DatosRol getDatosRol() {
        return datosRol;
    }

    public Rol getRol() {
        return datosRol.rol();
    }

    // Solo los clientes tienen tipo de cliente; para los demás roles, null.
    public TipoCliente getTipoCliente() {
        return datosRol instanceof DatosCliente cliente ? cliente.tipoCliente() : null;
    }

    public boolean estaDesactivado() {
        return desactivado;
    }

    public int getIntentosFallidos() {
        return intentosFallidos;
    }

    public Instant getBloqueadoHasta() {
        return bloqueadoHasta;
    }

    public void desactivar() {
        desactivado = true;
    }

    public void activar() {
        desactivado = false;
    }

    public void registrarIntentoFallido(Instant ahora) {
        intentosFallidos++;
        if (intentosFallidos >= MAX_INTENTOS) {
            bloqueadoHasta = ahora.plus(DURACION_BLOQUEO);
        }
    }

    public boolean tieneBloqueoPorIntentos(Instant ahora) {
        return bloqueadoHasta != null && ahora.isBefore(bloqueadoHasta);
    }
}
