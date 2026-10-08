package es.uclm.esibuy.backend.usuarios.infrastructure.adapter.out.persistence;

import java.time.Instant;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Forma en que se guarda un usuario en MongoDB (colección {@code usuarios}).
 *
 * <p>Es distinta de {@code Usuario} del dominio, que no conoce MongoDB. Los nombres de los
 * campos siguen el «Planteamiento inicial BBDD». El rol se guarda como texto para que el
 * documento no dependa del enum del dominio.
 *
 * <p>Cuando entre el Usuario de la T214 se añadirán nombre, apellidos, tipoCliente y desactivado.
 */
@Document(collection = "usuarios")
public class UsuarioDocument {

    @Id
    private String id;
    @Indexed(unique = true) // «Planteamiento inicial BBDD»: email → único
    private final String email;
    private final String passwordHash;
    private final String rol;
    private String estado;
    private int failedLoginAttempts;
    private Instant lockedUntil;

    public UsuarioDocument(String email, String passwordHash, String rol) {
        this.email = email;
        this.passwordHash = passwordHash;
        this.rol = rol;
    }

    public String getId() {
        return id;
    }

    // Con el id, guardar un usuario ya existente lo actualiza en vez de insertar otro.
    public void setId(String id) {
        this.id = id;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public int getFailedLoginAttempts() {
        return failedLoginAttempts;
    }

    public void setFailedLoginAttempts(int failedLoginAttempts) {
        this.failedLoginAttempts = failedLoginAttempts;
    }

    public Instant getLockedUntil() {
        return lockedUntil;
    }

    public void setLockedUntil(Instant lockedUntil) {
        this.lockedUntil = lockedUntil;
    }

    public String getEmail() {
        return email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public String getRol() {
        return rol;
    }
}
