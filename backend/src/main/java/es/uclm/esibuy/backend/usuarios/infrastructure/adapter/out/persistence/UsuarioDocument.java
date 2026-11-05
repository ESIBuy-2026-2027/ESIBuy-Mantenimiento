package es.uclm.esibuy.backend.usuarios.infrastructure.adapter.out.persistence;

import java.time.Instant;
import java.time.LocalDate;

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
 * ({@code estado} ACTIVO/BLOQUEADO/PENDIENTE, {@code failedLoginAttempts} y {@code lockedUntil}),
 * los datos del cliente (DNI, fecha de nacimiento, teléfono, imagen) y el consentimiento de privacidad.
 * Un documento antiguo no tiene estos campos: el mapper les da los valores por defecto al leerlo.
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
    private String tipoCliente;
    // Datos del cliente y consentimiento (T229), con los nombres del esquema de ColeccionesEsiBuy.
    private String dni;
    private LocalDate fechaNacimiento;
    private String telefono;
    private String imagenPerfil;
    // Vendedor (T241): nombres del esquema de la T210; el índice único de nombreComercial lo declara ColeccionesEsiBuy.
    private String nombreComercial;
    private String categoriaPrincipalId;
    private boolean privacyPolicyAccepted;
    private Instant privacyPolicyAcceptedAt;
    private String privacyPolicyVersion;
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

    public String getTipoCliente() {
        return tipoCliente;
    }

    public void setTipoCliente(String tipoCliente) {
        this.tipoCliente = tipoCliente;
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

    public String getDni() {
        return dni;
    }

    public void setDni(String dni) {
        this.dni = dni;
    }

    public LocalDate getFechaNacimiento() {
        return fechaNacimiento;
    }

    public void setFechaNacimiento(LocalDate fechaNacimiento) {
        this.fechaNacimiento = fechaNacimiento;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public String getImagenPerfil() {
        return imagenPerfil;
    }

    public void setImagenPerfil(String imagenPerfil) {
        this.imagenPerfil = imagenPerfil;
    }

    public String getNombreComercial() {
        return nombreComercial;
    }

    public void setNombreComercial(String nombreComercial) {
        this.nombreComercial = nombreComercial;
    }

    public String getCategoriaPrincipalId() {
        return categoriaPrincipalId;
    }

    public void setCategoriaPrincipalId(String categoriaPrincipalId) {
        this.categoriaPrincipalId = categoriaPrincipalId;
    }

    public boolean isPrivacyPolicyAccepted() {
        return privacyPolicyAccepted;
    }

    public void setPrivacyPolicyAccepted(boolean privacyPolicyAccepted) {
        this.privacyPolicyAccepted = privacyPolicyAccepted;
    }

    public Instant getPrivacyPolicyAcceptedAt() {
        return privacyPolicyAcceptedAt;
    }

    public void setPrivacyPolicyAcceptedAt(Instant privacyPolicyAcceptedAt) {
        this.privacyPolicyAcceptedAt = privacyPolicyAcceptedAt;
    }

    public String getPrivacyPolicyVersion() {
        return privacyPolicyVersion;
    }

    public void setPrivacyPolicyVersion(String privacyPolicyVersion) {
        this.privacyPolicyVersion = privacyPolicyVersion;
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
