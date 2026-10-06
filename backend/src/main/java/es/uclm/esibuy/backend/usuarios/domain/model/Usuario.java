package es.uclm.esibuy.backend.usuarios.domain.model;

public class Usuario {
    // Recomendado empezar con objetos inmutables y solo quitar final cuando los test lo requieran
    private final String correo;
    private final String contrasena; // REVISAR: Deberia ser final? no podria cambiarse?
    private final Rol rol;

    public Usuario(String correo, String contrasena, Rol rol) {
        this.correo = correo;
        this.contrasena = contrasena;
        this.rol = rol;
    }

    public Rol getRol() {
        return rol;
    }

}
