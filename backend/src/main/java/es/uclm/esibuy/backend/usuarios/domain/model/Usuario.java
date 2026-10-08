package es.uclm.esibuy.backend.usuarios.domain.model;

import es.uclm.esibuy.backend.usuarios.domain.exception.UsuarioInvalidoException;

public class Usuario {

    private final String id;
    private final String nombre;
    private final String apellidos;
    private final Email email;
    private final String contrasenaHash;
    private final Rol rol;
    private final TipoCliente tipoCliente;
    private boolean bloqueado;

    public Usuario(String id, String nombre, String apellidos, Email email,
            String contrasenaHash, Rol rol, TipoCliente tipoCliente) {
        this.id = id;
        this.nombre = exigirTextoSiExiste(nombre, "El nombre no puede estar en blanco");
        this.apellidos = exigirTextoSiExiste(apellidos, "Los apellidos no pueden estar en blanco");
        this.email = exigirNoNulo(email, "El email es obligatorio");
        this.contrasenaHash = exigirTexto(contrasenaHash, "La contraseña es obligatoria");
        this.rol = exigirNoNulo(rol, "El rol es obligatorio");
        this.tipoCliente = exigirTipoClienteCoherente(this.rol, tipoCliente);
        this.bloqueado = false;
    }

    public static Usuario nuevoCliente(Email email, String contrasenaHash) {
        return new Usuario(null, null, null, email, contrasenaHash, Rol.CLIENTE, TipoCliente.ESTANDAR);
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

    public String getContrasenaHash() {
        return contrasenaHash;
    }

    public Rol getRol() {
        return rol;
    }

    public TipoCliente getTipoCliente() {
        return tipoCliente;
    }

    public boolean estaBloqueado() {
        return bloqueado;
    }

    public void bloquear() {
        bloqueado = true;
    }

    public void desbloquear() {
        bloqueado = false;
    }
}
