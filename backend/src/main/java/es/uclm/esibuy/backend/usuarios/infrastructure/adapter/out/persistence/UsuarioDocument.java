package es.uclm.esibuy.backend.usuarios.infrastructure.adapter.out.persistence;

import org.springframework.data.annotation.Id;
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
    private final String email;
    private final String passwordHash;
    private final String rol;

    public UsuarioDocument(String email, String passwordHash, String rol) {
        this.email = email;
        this.passwordHash = passwordHash;
        this.rol = rol;
    }

    public String getId() {
        return id;
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
