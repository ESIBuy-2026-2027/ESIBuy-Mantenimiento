package es.uclm.esibuy.backend.usuarios.infrastructure.adapter.out.persistence;

import java.time.Instant;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Forma en que se guarda un usuario en MongoDB (colección {@code usuarios}).
 *
 * <p>Es distinta de {@code Usuario} del dominio, que no conoce MongoDB. Los nombres de los
 * campos siguen el «Planteamiento inicial BBDD». El rol se guarda como texto para que el
 * documento no dependa del enum del dominio.
 *
 * <p>Guarda todos los datos del {@code Usuario}: los de perfil y el estado de acceso
 * ({@code estado} ACTIVO/DESACTIVADO, {@code failedLoginAttempts} y {@code lockedUntil}).
 */
@Document(collection = "usuarios")
public class UsuarioDocument {

    @Id
    private String id;
    @Indexed(unique = true) // «Planteamiento inicial BBDD»: email → único
    private final String email;
    private final String passwordHash;
    private final String rol;
    private String nombre;
    private String apellidos;
    private String dni;              // clientes y vendedores
    private String telefono;
    private DatosClienteDocument cliente;              // solo uno de los tres, el del rol
    private DatosVendedorDocument vendedor;
    private DatosAdministradorDocument administrador;
    private String estado;
    private int failedLoginAttempts;
    private Instant lockedUntil;
    @CreatedDate // «Convenciones comunes» del documento de BBDD; lo rellena Spring (MongoConfig)
    private Instant createdAt;
    @LastModifiedDate
    private Instant updatedAt;

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

    public String getDni() {
        return dni;
    }

    public void setDni(String dni) {
        this.dni = dni;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public DatosClienteDocument getCliente() {
        return cliente;
    }

    public void setCliente(DatosClienteDocument cliente) {
        this.cliente = cliente;
    }

    public DatosVendedorDocument getVendedor() {
        return vendedor;
    }

    public void setVendedor(DatosVendedorDocument vendedor) {
        this.vendedor = vendedor;
    }

    public DatosAdministradorDocument getAdministrador() {
        return administrador;
    }

    public void setAdministrador(DatosAdministradorDocument administrador) {
        this.administrador = administrador;
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

    public Instant getCreatedAt() {
        return createdAt;
    }

    // Al actualizar un usuario hay que conservar su fecha de creación: el documento se rehace desde
    // el dominio, que no la conoce, y Spring solo rellena createdAt en los documentos nuevos.
    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
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
