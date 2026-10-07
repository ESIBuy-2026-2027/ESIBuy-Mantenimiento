package es.uclm.esibuy.backend.usuarios.domain.model;

public class Usuario {
    // Recomendado empezar con objetos inmutables y solo quitar final cuando los test lo requieran
    private final String correo;
    private final String contrasenaHash; // REVISAR: Deberia ser final? no podria cambiarse?
    private final Rol rol;

    public Usuario(String correo, String contrasenaHash, Rol rol) {
        this.correo = correo;
        this.contrasenaHash = contrasenaHash;
        this.rol = rol;
    }

    // Mismo nombre que en el Usuario de la T214 (Alberto), que lo devolverá como Email
    public String getEmail() {
        return correo;
    }

    public Rol getRol() {
        return rol;
    }

    public String getContrasenaHash() {
        return contrasenaHash;
    }

}
