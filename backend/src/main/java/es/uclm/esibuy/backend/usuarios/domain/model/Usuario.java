package es.uclm.esibuy.backend.usuarios.domain.model;

import java.time.Duration;
import java.time.Instant;

public class Usuario {

    private static final int MAX_INTENTOS = 5;
    private static final Duration DURACION_BLOQUEO = Duration.ofMinutes(15);

    // Recomendado empezar con objetos inmutables y solo quitar final cuando los test lo requieran
    private final String email;
    private final String passwordHash; // REVISAR: Deberia ser final? no podria cambiarse?
    private final Rol rol;
    private int intentosFallidos;
    private Instant bloqueadoHasta;   // null = no bloqueado

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
        if (intentosFallidos >= MAX_INTENTOS) {
            bloqueadoHasta = ahora.plus(DURACION_BLOQUEO);
        }
    }

    public boolean estaBloqueado(Instant ahora) {
        return bloqueadoHasta != null && ahora.isBefore(bloqueadoHasta);
    }

}
