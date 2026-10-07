package es.uclm.esibuy.backend.usuarios.domain.model;

public class Usuario {
    // Recomendado empezar con objetos inmutables y solo quitar final cuando los test lo requieran
    private final String email;
    private final String passwordHash; // REVISAR: Deberia ser final? no podria cambiarse?
    private final Rol rol;

    public Usuario(String email, String passwordHash, Rol rol) {
        this.email = email;
        this.passwordHash = passwordHash;
        this.rol = rol;
    }

    public Rol getRol() {
        return rol;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

}
