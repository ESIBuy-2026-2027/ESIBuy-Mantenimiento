package es.uclm.esibuy.backend.usuarios.domain.model;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;

import es.uclm.esibuy.backend.usuarios.domain.exception.MotivoRechazo;
import es.uclm.esibuy.backend.usuarios.domain.exception.UsuarioInvalidoException;

public class Usuario {

    private static final int MAX_INTENTOS = 5;
    private static final Duration DURACION_BLOQUEO = Duration.ofMinutes(15);
    private static final int MAYORIA_EDAD = 18;

    private final String id;
    private DatosPersonales datosPersonales;
    private PerfilUsuario perfil;        // datos propios del rol: DatosCliente, DatosVendedor o null
    private final ConsentimientoPrivacidad consentimiento;
    private final Email email;
    private final String passwordHash;
    private final Rol rol;
    private Instant fechaAlta;
    private String estado;
    private int intentosFallidos;        // intentos de login fallidos seguidos
    private Instant bloqueadoHasta;      // bloqueo temporal por intentos; null = sin bloqueo

    public Usuario(String id, DatosPersonales datosPersonales, Email email,
            String passwordHash, Rol rol, PerfilUsuario perfil, ConsentimientoPrivacidad consentimiento) {
        this.id = id;
        this.datosPersonales = exigirNoNulo(datosPersonales, "Los datos personales son obligatorios");
        this.email = exigirNoNulo(email, "El email es obligatorio");
        this.passwordHash = exigirTexto(passwordHash, "La contraseña es obligatoria");
        this.rol = exigirNoNulo(rol, "El rol es obligatorio");
        this.perfil = exigirPerfilCoherente(this.rol, perfil);
        this.consentimiento = consentimiento;
        this.estado = "PENDIENTE";
        this.intentosFallidos = 0;
    }

    public void restaurarEstado(String estado) {
        if (estado != null) {
            this.estado = estado;
        }
    }

    // Edición de los datos por un administrador (HU22). El email, el rol y la contraseña no cambian por aquí.
    public void modificarDatos(DatosPersonales datosPersonales, PerfilUsuario nuevoPerfil, LocalDate hoy) {
        // Se comprueba todo antes de asignar: si algo falla, el usuario se queda como estaba.
        DatosPersonales personales = exigirNoNulo(datosPersonales, "Los datos personales son obligatorios");
        PerfilUsuario perfilCoherente = exigirPerfilCoherente(rol, nuevoPerfil);
        // Al editar, un vendedor siempre lleva sus datos: si no, se borrarían los que tiene (T241).
        if (rol == Rol.VENDEDOR && !(perfilCoherente instanceof DatosVendedor)) {
            throw new UsuarioInvalidoException("Un vendedor debe tener datos de vendedor");
        }
        if (perfilCoherente instanceof DatosCliente datosCliente) {
            exigirMayoriaDeEdad(datosCliente, hoy);
        }
        this.datosPersonales = personales;
        this.perfil = perfilCoherente;
    }

    public static Usuario nuevoCliente(DatosPersonales datosPersonales, Email email, String passwordHash,
        DatosCliente datosCliente, ConsentimientoPrivacidad consentimiento, LocalDate hoy) {
        exigirMayoriaDeEdad(datosCliente, hoy);
        return new Usuario(null, datosPersonales, email, passwordHash, Rol.CLIENTE, datosCliente, consentimiento);
    }

    // El vendedor no tiene fecha de nacimiento: no hay mayoría de edad que comprobar (T241).
    public static Usuario nuevoVendedor(DatosPersonales datosPersonales, Email email, String passwordHash,
            DatosVendedor datosVendedor, ConsentimientoPrivacidad consentimiento) {
        exigirNoNulo(datosVendedor, "Un vendedor debe tener datos de vendedor");
        return new Usuario(null, datosPersonales, email, passwordHash, Rol.VENDEDOR, datosVendedor, consentimiento);
    }

    private static void exigirMayoriaDeEdad(DatosCliente datosCliente, LocalDate hoy) {
        if (datosCliente.fechaNacimiento().plusYears(MAYORIA_EDAD).isAfter(hoy)) {
            throw new UsuarioInvalidoException(MotivoRechazo.MENOR_DE_EDAD, "El cliente debe ser mayor de edad");
        }
    }

    public static Usuario nuevoAdministrador(DatosPersonales datosPersonales, Email email, String passwordHash) {
        // A un administrador no le exigimos DatosCliente ni ConsentimientoPrivacidad
        Usuario admin = new Usuario(null, datosPersonales, email, passwordHash, Rol.ADMINISTRADOR, null, null);
        admin.restaurarEstado("ACTIVO"); // Nace activo directamente
        return admin;
    }

    // Un cliente lleva siempre DatosCliente; un vendedor o un administrador pueden no tener perfil
    // (cuentas anteriores a la T241 o creadas por un administrador).
    private static PerfilUsuario exigirPerfilCoherente(Rol rol, PerfilUsuario perfil) {
        boolean esCliente = rol == Rol.CLIENTE;
        if (esCliente && !(perfil instanceof DatosCliente)) {
            throw new UsuarioInvalidoException("Un cliente debe tener datos de cliente");
        }
        if (!esCliente && perfil instanceof DatosCliente) {
            throw new UsuarioInvalidoException("Solo los clientes tienen datos de cliente");
        }

        if (rol != Rol.VENDEDOR && perfil instanceof DatosVendedor) {
            throw new UsuarioInvalidoException("Solo los vendedores tienen datos de vendedor");
        }
        return perfil;
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
        return perfil instanceof DatosCliente cliente ? cliente : null;
    }

    public DatosVendedor getDatosVendedor() {
        return perfil instanceof DatosVendedor vendedor ? vendedor : null;
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
        // Un bloqueo ya caducado es una serie cerrada: el fallo nuevo empieza a contar desde cero
        // (fallos de la demo del Sprint 1: antes cada fallo tras el bloqueo volvía a bloquear 15 minutos).
        if (bloqueadoHasta != null && !ahora.isBefore(bloqueadoHasta)) {
            reiniciarIntentos();
        }
        intentosFallidos++;
        if (intentosFallidos >= MAX_INTENTOS) {
            bloqueadoHasta = ahora.plus(DURACION_BLOQUEO);
        }
    }

    // Al completar el login (paso 3) y al caducar un bloqueo: la cuenta vuelve a tener sus 5 intentos
    public void reiniciarIntentos() {
        intentosFallidos = 0;
        bloqueadoHasta = null;
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
