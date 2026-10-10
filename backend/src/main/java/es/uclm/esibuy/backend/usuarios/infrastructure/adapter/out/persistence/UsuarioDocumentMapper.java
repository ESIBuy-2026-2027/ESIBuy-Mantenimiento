package es.uclm.esibuy.backend.usuarios.infrastructure.adapter.out.persistence;

import es.uclm.esibuy.backend.usuarios.domain.exception.EmailInvalidoException;
import es.uclm.esibuy.backend.usuarios.domain.exception.UsuarioInvalidoException;
import es.uclm.esibuy.backend.usuarios.domain.model.ConsentimientoPrivacidad;
import es.uclm.esibuy.backend.usuarios.domain.model.DatosCliente;
import es.uclm.esibuy.backend.usuarios.domain.model.DatosPersonales;
import es.uclm.esibuy.backend.usuarios.domain.model.Dni;
import es.uclm.esibuy.backend.usuarios.domain.model.Email;
import es.uclm.esibuy.backend.usuarios.domain.model.Rol;
import es.uclm.esibuy.backend.usuarios.domain.model.Telefono;
import es.uclm.esibuy.backend.usuarios.domain.model.TipoCliente;
import es.uclm.esibuy.backend.usuarios.domain.model.Usuario;

/**
 * Convierte entre el {@code Usuario} del dominio y el documento que se guarda en MongoDB.
 *
 * <p>Al guardar aplana los value objects en los campos del modelo de BBDD. Al leer los reconstruye
 * y restaura el estado guardado (estado, bloqueo por intentos y fecha de alta), que el constructor
 * de {@code Usuario} no conoce porque describe un usuario nuevo.
 *
 * <p>Un documento sin nombre, apellidos o, en un cliente, sin DNI o fecha de nacimiento, no se
 * puede reconstruir: el dominio los exige desde la T229 y se lanza
 * {@link UsuarioPersistidoInvalidoException} (T228). PENDIENTE DE REVISAR CON EL EQUIPO qué hacer
 * con las cuentas de prueba anteriores guardadas en Atlas.
 */
public class UsuarioDocumentMapper {

    // Un documento anterior a la T225 no tiene estado: es una cuenta creada antes de la verificación por administrador.
    private static final String ESTADO_ACTIVO = "ACTIVO";

    public UsuarioDocument aDocumento(Usuario usuario) {
        UsuarioDocument documento = new UsuarioDocument(usuario.getEmail().valor(), usuario.getPasswordHash(),
                usuario.getRol().name());
        documento.setId(usuario.getId());
        documento.setEstado(usuario.getEstado());
        documento.setFailedLoginAttempts(usuario.getIntentosFallidos());
        documento.setLockedUntil(usuario.getBloqueadoHasta());

        DatosPersonales datosPersonales = usuario.getDatosPersonales();
        documento.setNombre(datosPersonales.nombre());
        documento.setApellidos(datosPersonales.apellidos());
        documento.setImagenPerfil(datosPersonales.imagenPerfil());

        DatosCliente datosCliente = usuario.getDatosCliente();
        if (datosCliente != null) {
            documento.setTipoCliente(datosCliente.tipoCliente().name());
            documento.setDni(datosCliente.dni().valor());
            documento.setFechaNacimiento(datosCliente.fechaNacimiento());
            documento.setTelefono(datosCliente.telefono() == null ? null : datosCliente.telefono().valor());
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
            // Las reglas del dominio han rechazado datos ya guardados: es un fallo del servidor, no del cliente.
            throw new UsuarioPersistidoInvalidoException(documento.getId(), e);
        }
    }

    private static Usuario reconstruir(UsuarioDocument documento) {
        Rol rol = Rol.valueOf(documento.getRol());
        Usuario usuario = new Usuario(documento.getId(),
                new DatosPersonales(documento.getNombre(), documento.getApellidos(), documento.getImagenPerfil()),
                new Email(documento.getEmail()), documento.getPasswordHash(), rol,
                rol == Rol.CLIENTE ? datosCliente(documento) : null,
                consentimiento(documento));

        usuario.restaurarEstado(documento.getEstado() == null ? ESTADO_ACTIVO : documento.getEstado());
        usuario.restaurarBloqueoPorIntentos(documento.getFailedLoginAttempts(), documento.getLockedUntil());
        usuario.restaurarFechaAlta(documento.getCreatedAt());
        return usuario;
    }

    // Tipo de cliente: el guardado si existe; si no (documento anterior a la T225), ESTANDAR.
    private static DatosCliente datosCliente(UsuarioDocument documento) {
        TipoCliente tipoCliente = documento.getTipoCliente() == null
                ? TipoCliente.ESTANDAR
                : TipoCliente.valueOf(documento.getTipoCliente());
        return new DatosCliente(tipoCliente, new Dni(documento.getDni()), documento.getFechaNacimiento(),
                documento.getTelefono() == null ? null : new Telefono(documento.getTelefono()));
    }

    private static ConsentimientoPrivacidad consentimiento(UsuarioDocument documento) {
        if (!documento.isPrivacyPolicyAccepted()) {
            return null;
        }
        return new ConsentimientoPrivacidad(documento.getPrivacyPolicyAcceptedAt(), documento.getPrivacyPolicyVersion());
    }
}
