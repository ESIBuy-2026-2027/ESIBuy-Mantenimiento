package es.uclm.esibuy.backend.usuarios.domain.model;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;

import es.uclm.esibuy.backend.usuarios.domain.exception.UsuarioInvalidoException;

public class Usuario {

    private static final int MAX_INTENTOS = 5;
    private static final Duration DURACION_BLOQUEO = Duration.ofMinutes(15);
    private static final int MAYORIA_EDAD = 18;

    private final String id;
    private final DatosPersonales datosPersonales;
    private final DatosCliente datosCliente;
    private final ConsentimientoPrivacidad consentimiento;
    private final Email email;
    private final String passwordHash;
    private final Rol rol;
    private Instant fechaAlta;
    private String estado;
    private int intentosFallidos;        // intentos de login fallidos seguidos
    private Instant bloqueadoHasta;      // bloqueo temporal por intentos; null = sin bloqueo

    public Usuario(String id, DatosPersonales datosPersonales, Email email,
            String passwordHash, Rol rol, DatosCliente datosCliente, ConsentimientoPrivacidad consentimiento) {
        this.id = id;
        this.datosPersonales = exigirNoNulo(datosPersonales, "Los datos personales son obligatorios");
        this.email = exigirNoNulo(email, "El email es obligatorio");
        this.passwordHash = exigirTexto(passwordHash, "La contraseña es obligatoria");
        this.rol = exigirNoNulo(rol, "El rol es obligatorio");
        this.datosCliente = exigirDatosClienteCoherente(this.rol, datosCliente);
        this.consentimiento = consentimiento;
        this.estado = "PENDIENTE";
        this.intentosFallidos = 0;
    }

    public void restaurarEstado(String estado) {
        if (estado != null) {
            this.estado = estado;
        }
    }

    public static Usuario nuevoCliente(DatosPersonales datosPersonales, Email email, String passwordHash, 
        DatosCliente datosCliente, ConsentimientoPrivacidad consentimiento, LocalDate hoy) {
        if (datosCliente.fechaNacimiento().plusYears(MAYORIA_EDAD).isAfter(hoy)) {
            throw new UsuarioInvalidoException("El cliente debe ser mayor de edad");
        }
        return new Usuario(null, datosPersonales, email, passwordHash, Rol.CLIENTE, datosCliente, consentimiento);
    }

    public static Usuario nuevoAdministrador(DatosPersonales datosPersonales, Email email, String passwordHash) {
        // A un administrador no le exigimos DatosCliente ni ConsentimientoPrivacidad
        Usuario admin = new Usuario(null, datosPersonales, email, passwordHash, Rol.ADMINISTRADOR, null, null);
        admin.restaurarEstado("ACTIVO"); // Nace activo directamente
        return admin;
    }

    private static DatosCliente exigirDatosClienteCoherente(Rol rol, DatosCliente datosCliente) {
        boolean esCliente = rol == Rol.CLIENTE;
        if (esCliente && datosCliente == null) {
            throw new UsuarioInvalidoException("Un cliente debe tener datos de cliente");
        }
        if (!esCliente && datosCliente != null) {
            throw new UsuarioInvalidoException("Solo los clientes tienen datos de cliente");
        }
        return datosCliente;
    }

    private static <T> T exigirNoNulo(T valor, String mensaje) {
        if (valor == null) {
            throw new UsuarioInvalidoException(mensaje);
        }
        return valor;
    }

    private static String exigirTexto(String valor, String mensaje) {
        if (valor == null || valor.isBlank()) {
            throw new UsuarioInvalidoException(mensaje);
        }
        return valor;
    }

    public String getId() {
        return id;
    }

    public DatosPersonales getDatosPersonales() {
        return datosPersonales;
    }

    public DatosCliente getDatosCliente() {
        return datosCliente;
    }

    public Email getEmail() {
        return email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public Rol getRol() {
        return rol;
    }

    public ConsentimientoPrivacidad getConsentimiento() {
        return consentimiento;
    }

    public Instant getFechaAlta() {
        return fechaAlta;
    }

    public String getEstado() {
        return estado;
    }

    public boolean estaActivo() {
        return "ACTIVO".equals(estado);
    }

    public void desactivar() {
        estado = "DESACTIVADO";
    }

    public void activar() {
        estado = "ACTIVO";
    }

    public void registrarIntentoFallido(Instant ahora) {
        intentosFallidos++;
        if (intentosFallidos >= MAX_INTENTOS) {
            bloqueadoHasta = ahora.plus(DURACION_BLOQUEO);
        }
    }

    public boolean tieneBloqueoPorIntentos(Instant ahora) {
        return bloqueadoHasta != null && ahora.isBefore(bloqueadoHasta);
    }

    public int getIntentosFallidos() {
        return intentosFallidos;
    }

    public Instant getBloqueadoHasta() {
        return bloqueadoHasta;
    }

    // Usamos esta operación para obligar a que se restauren a la vez los dos parámetros, además de ser más descriptivos pese a ser dos setters
    public void restaurarBloqueoPorIntentos(int intentosFallidos, Instant bloqueadoHasta) {
        this.intentosFallidos = intentosFallidos;
        this.bloqueadoHasta = bloqueadoHasta;
    }

    public void restaurarFechaAlta(Instant fechaAlta) {
        this.fechaAlta = fechaAlta;
    }

    public boolean estaPendiente() {
        return "PENDIENTE".equals(this.estado); 
    }
}
