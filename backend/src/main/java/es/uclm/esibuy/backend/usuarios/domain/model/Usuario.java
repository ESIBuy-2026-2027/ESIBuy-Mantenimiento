package es.uclm.esibuy.backend.usuarios.domain.model;

import java.time.Duration;
import java.time.Instant;

import es.uclm.esibuy.backend.usuarios.domain.exception.UsuarioInvalidoException;

public class Usuario {

    private static final int MAX_INTENTOS = 5;
    private static final Duration DURACION_BLOQUEO = Duration.ofMinutes(15);

    private final String id;
    private final String nombre;
    private final String apellidos;
    private final Email email;
    private final String passwordHash;
    private final Rol rol;
    private final TipoCliente tipoCliente;
    private boolean desactivado;         // desactivada por un administrador (HU23/HU24): no puede entrar
    private int intentosFallidos;        // intentos de login fallidos seguidos
    private Instant bloqueadoHasta;      // bloqueo temporal por intentos; null = sin bloqueo

    public Usuario(String id, String nombre, String apellidos, Email email,
            String passwordHash, Rol rol, TipoCliente tipoCliente) {
        this.id = id;
        this.nombre = exigirTextoSiExiste(nombre, "El nombre no puede estar en blanco");
        this.apellidos = exigirTextoSiExiste(apellidos, "Los apellidos no pueden estar en blanco");
        this.email = exigirNoNulo(email, "El email es obligatorio");
        this.passwordHash = exigirTexto(passwordHash, "La contraseña es obligatoria");
        this.rol = exigirNoNulo(rol, "El rol es obligatorio");
        this.tipoCliente = exigirTipoClienteCoherente(this.rol, tipoCliente);
        this.desactivado = false;
        this.intentosFallidos = 0;
    }

    public static Usuario nuevoCliente(Email email, String passwordHash) {
        return new Usuario(null, null, null, email, passwordHash, Rol.CLIENTE, TipoCliente.ESTANDAR);
    }

    private static TipoCliente exigirTipoClienteCoherente(Rol rol, TipoCliente tipoCliente) {
        boolean esCliente = rol == Rol.CLIENTE;
        if (esCliente && tipoCliente == null) {
            throw new UsuarioInvalidoException("Un cliente debe tener tipo de cliente");
        }
        if (!esCliente && tipoCliente != null) {
            throw new UsuarioInvalidoException("Solo los clientes tienen tipo de cliente");
        }
        return tipoCliente;
    }

    private static <T> T exigirNoNulo(T valor, String mensaje) {
        if (valor == null) {
            throw new UsuarioInvalidoException(mensaje);
        }
        return valor;
    }

    private static String exigirTextoSiExiste(String valor, String mensaje) {
        return valor == null ? null : exigirTexto(valor, mensaje);
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

    public String getNombre() {
        return nombre;
    }

    public String getApellidos() {
        return apellidos;
    }

    public Email getEmail() {
        return email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public Rol getRol() {
        return rol;
    }

    public TipoCliente getTipoCliente() {
        return tipoCliente;
    }

    public boolean estaDesactivado() {
        return desactivado;
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
