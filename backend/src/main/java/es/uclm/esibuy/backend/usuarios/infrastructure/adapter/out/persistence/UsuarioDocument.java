package es.uclm.esibuy.backend.usuarios.infrastructure.adapter.out.persistence;

import java.time.Instant;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Forma en que se guarda un usuario en MongoDB (colección {@code usuarios}).
 *
 * <p>Es distinta de {@code Usuario} del dominio, que no conoce MongoDB. Los nombres de los
 * campos siguen el «Planteamiento inicial BBDD». El rol se guarda como texto para que el
 * documento no dependa del enum del dominio.
 *
 * <p>Cuando entre el Usuario de la T214 se añadirán nombre, apellidos, tipoCliente, intentos fallidos y bloqueo.
 */
@Document(collection = "usuarios")
public class UsuarioDocument {

    @Id
    private String id;
    private final String email;
    private final String passwordHash;
    private final String rol;

    private String nombre;
    private String apellidos;
    private String tipoCliente;
    // Boolean y no boolean: un documento antiguo, sin este campo, se lee como null, que el mapper trata
    // como activo. Con el primitivo se leería false y todos los usuarios antiguos quedarían desactivados.
    private Boolean activo;
    private int intentosFallidos;
    private Instant bloqueadoHasta;

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

    public void setId(String id) {
        this.id = id;
    }

    public Boolean getActivo() {
        return activo;
    }

    public void setActivo(Boolean activo) {
        this.activo = activo;
    }

    public int getIntentosFallidos() {
        return intentosFallidos;
    }

    public void setIntentosFallidos(int intentosFallidos) {
        this.intentosFallidos = intentosFallidos;
    }

    public Instant getBloqueadoHasta() {
        return bloqueadoHasta;
    }

    public void setBloqueadoHasta(Instant bloqueadoHasta) {
        this.bloqueadoHasta = bloqueadoHasta;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getApellidos() {
        return apellidos;
    }

    public void setApellidos(String apellidos) {
        this.apellidos = apellidos;
    }

    public String getTipoCliente() {
        return tipoCliente;
    }

    public void setTipoCliente(String tipoCliente) {
        this.tipoCliente = tipoCliente;
    }
}
