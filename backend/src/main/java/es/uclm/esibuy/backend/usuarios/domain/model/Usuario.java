package es.uclm.esibuy.backend.usuarios.domain.model;

import java.time.Instant;

public class Usuario {
    // Recomendado empezar con objetos inmutables y solo quitar final cuando los test lo requieran
    private final String email;
    private final String passwordHash; // REVISAR: Deberia ser final? no podria cambiarse?
    private final Rol rol;
    private int intentosFallidos;

    public Usuario(String email, String passwordHash, Rol rol) {
        this.email = email;
        this.passwordHash = passwordHash;
        this.rol = rol;
        this.intentosFallidos = 0;
    }

    public Rol getRol() {
        return rol;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    // Mismo nombre que en el Usuario de la T214 (Alberto)
    public String getEmail() {
        return email;
    }

    public void registrarIntentoFallido(Instant ahora) {
        intentosFallidos++;
    }

}
