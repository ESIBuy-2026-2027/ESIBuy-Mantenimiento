package es.uclm.esibuy.backend.usuarios.infrastructure.adapter.out.persistence;

import es.uclm.esibuy.backend.usuarios.domain.exception.EmailInvalidoException;
import es.uclm.esibuy.backend.usuarios.domain.exception.UsuarioInvalidoException;
import es.uclm.esibuy.backend.usuarios.domain.model.ConsentimientoPrivacidad;
import es.uclm.esibuy.backend.usuarios.domain.model.DatosCliente;
import es.uclm.esibuy.backend.usuarios.domain.model.DatosPersonales;
import es.uclm.esibuy.backend.usuarios.domain.model.DatosVendedor;
import es.uclm.esibuy.backend.usuarios.domain.model.Dni;
import es.uclm.esibuy.backend.usuarios.domain.model.Email;
import es.uclm.esibuy.backend.usuarios.domain.model.PerfilUsuario;
import es.uclm.esibuy.backend.usuarios.domain.model.Rol;
import es.uclm.esibuy.backend.usuarios.domain.model.Telefono;
import es.uclm.esibuy.backend.usuarios.domain.model.TipoCliente;
import es.uclm.esibuy.backend.usuarios.domain.model.Usuario;

/**
 * Convierte entre el {@code Usuario} del dominio y el documento que se guarda
 * en MongoDB.
 *
 * <p>
 * Al guardar aplana los value objects en los campos del modelo de BBDD. Al leer
 * los reconstruye
 * y restaura el estado guardado (estado, bloqueo por intentos y fecha de alta),
 * que el constructor
 * de {@code Usuario} no conoce porque describe un usuario nuevo.
 *
 * <p>
 * Un documento sin nombre, apellidos o, en un cliente, sin DNI o fecha de
 * nacimiento, no se
 * puede reconstruir: el dominio los exige desde la T229 y se lanza
 * {@link UsuarioPersistidoInvalidoException} (T228). PENDIENTE DE REVISAR CON
 * EL EQUIPO qué hacer
 * con las cuentas de prueba anteriores guardadas en Atlas.
 */
public class UsuarioDocumentMapper {

    // Un documento anterior a la T225 no tiene estado: es una cuenta creada antes
    // de la verificación por administrador.
    private static final String ESTADO_ACTIVO = "ACTIVO";

    public UsuarioDocument aDocumento(Usuario usuario) {
        UsuarioDocument documento = new UsuarioDocument(usuario.getEmail().valor(), usuario.getPasswordHash(),
                usuario.getRol().name());
        documento.setId(usuario.getId());
        documento.setEstado(usuario.getEstado());
        documento.setFailedLoginAttempts(usuario.getIntentosFallidos());
        documento.setLockedUntil(usuario.getBloqueadoHasta());
        documento.setEmailOtpEnabled(usuario.isEmailOtpEnabled());
        documento.setAuthenticatorEnabled(usuario.isAuthenticatorEnabled());

        DatosPersonales datosPersonales = usuario.getDatosPersonales();
        documento.setNombre(datosPersonales.nombre());
        documento.setApellidos(datosPersonales.apellidos());
        documento.setImagenPerfil(datosPersonales.imagenPerfil());

        DatosCliente datosCliente = usuario.getDatosCliente();
        if (datosCliente != null) {
            documento.setTipoCliente(datosCliente.tipoCliente().name());
            documento.setFechaNacimiento(datosCliente.fechaNacimiento());
            escribirDniYTelefono(documento, datosCliente);
        }
        DatosVendedor datosVendedor = usuario.getDatosVendedor();
        if (datosVendedor != null) {
            documento.setNombreComercial(datosVendedor.nombreComercial());
            documento.setCategoriaPrincipalId(datosVendedor.categoriaPrincipalId());
            escribirDniYTelefono(documento, datosVendedor);
        }

        ConsentimientoPrivacidad consentimiento = usuario.getConsentimiento();
        if (consentimiento != null) {
            documento.setPrivacyPolicyAccepted(true);
            documento.setPrivacyPolicyAcceptedAt(consentimiento.aceptadaEn());
            documento.setPrivacyPolicyVersion(consentimiento.version());
        }
        return documento;
    }

    public Usuario aDominio(UsuarioDocument documento) {
        try {
            return reconstruir(documento);
        } catch (UsuarioInvalidoException | EmailInvalidoException e) {
            // Las reglas del dominio han rechazado datos ya guardados: es un fallo del
            // servidor, no del cliente.
            throw new UsuarioPersistidoInvalidoException(documento.getId(), e);
        }
    }

    private static Usuario reconstruir(UsuarioDocument documento) {
        Rol rol = Rol.valueOf(documento.getRol());
        Usuario usuario = new Usuario(documento.getId(),
                new DatosPersonales(documento.getNombre(), documento.getApellidos(), documento.getImagenPerfil()),
                new Email(documento.getEmail()), documento.getPasswordHash(), rol,
                perfil(rol, documento),
                consentimiento(documento));

        usuario.restaurarPreferenciasSeguridad(
                Boolean.TRUE.equals(documento.getEmailOtpEnabled()),
                Boolean.TRUE.equals(documento.getAuthenticatorEnabled()));

        usuario.restaurarEstado(documento.getEstado() == null ? ESTADO_ACTIVO : documento.getEstado());
        usuario.restaurarBloqueoPorIntentos(documento.getFailedLoginAttempts(), documento.getLockedUntil());
        usuario.restaurarFechaAlta(documento.getCreatedAt());
        return usuario;
    }

    // El DNI y el teléfono van en la raíz del documento sea cual sea el perfil.
    private static void escribirDniYTelefono(UsuarioDocument documento, PerfilUsuario perfil) {
        documento.setDni(perfil.dni().valor());
        documento.setTelefono(perfil.telefono() == null ? null : perfil.telefono().valor());
    }

    // El perfil se elige por rol. Un vendedor sin nombre comercial (anterior a la
    // T241) se lee sin perfil.
    private static PerfilUsuario perfil(Rol rol, UsuarioDocument documento) {
        return switch (rol) {
            case CLIENTE -> datosCliente(documento);
            case VENDEDOR -> documento.getNombreComercial() == null ? null : datosVendedor(documento);
            case ADMINISTRADOR -> null;
        };
    }

    private static DatosVendedor datosVendedor(UsuarioDocument documento) {
        return new DatosVendedor(documento.getNombreComercial(), documento.getCategoriaPrincipalId(),
                new Dni(documento.getDni()), telefono(documento));
    }

    private static Telefono telefono(UsuarioDocument documento) {
        return documento.getTelefono() == null ? null : new Telefono(documento.getTelefono());
    }

    // Tipo de cliente: el guardado si existe; si no (documento anterior a la T225),
    // ESTANDAR.
    private static DatosCliente datosCliente(UsuarioDocument documento) {
        TipoCliente tipoCliente = documento.getTipoCliente() == null
                ? TipoCliente.ESTANDAR
                : TipoCliente.valueOf(documento.getTipoCliente());
        return new DatosCliente(tipoCliente, new Dni(documento.getDni()), documento.getFechaNacimiento(),
                telefono(documento));
    }

    private static ConsentimientoPrivacidad consentimiento(UsuarioDocument documento) {
        if (!documento.isPrivacyPolicyAccepted()) {
            return null;
        }
        return new ConsentimientoPrivacidad(documento.getPrivacyPolicyAcceptedAt(),
                documento.getPrivacyPolicyVersion());
    }
}
