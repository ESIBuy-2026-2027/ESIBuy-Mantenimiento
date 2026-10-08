package es.uclm.esibuy.backend.usuarios.infrastructure.adapter.out.persistence;

import es.uclm.esibuy.backend.usuarios.domain.model.Email;
import es.uclm.esibuy.backend.usuarios.domain.model.Rol;
import es.uclm.esibuy.backend.usuarios.domain.model.TipoCliente;
import es.uclm.esibuy.backend.usuarios.domain.model.Usuario;

/**
 * Convierte el {@code Usuario} del dominio en el documento que se guarda en MongoDB.
 *
 * <p>Convierte en los dos sentidos: {@code aDocumento} para guardar y {@code aDominio} para leer.
 */
public class UsuarioDocumentMapper {

    // Valores del campo "estado" del «Planteamiento inicial BBDD»; en el dominio, Usuario.estaDesactivado().
    private static final String ESTADO_ACTIVO = "ACTIVO";
    private static final String ESTADO_DESACTIVADO = "DESACTIVADO";

    public UsuarioDocument aDocumento(Usuario usuario) {
        UsuarioDocument documento = new UsuarioDocument(usuario.getEmail().valor(), usuario.getPasswordHash(),
                usuario.getRol().name());
        documento.setId(usuario.getId());
        documento.setNombre(usuario.getNombre());
        documento.setApellidos(usuario.getApellidos());
        documento.setTipoCliente(usuario.getTipoCliente() == null ? null : usuario.getTipoCliente().name());
        documento.setEstado(usuario.estaDesactivado() ? ESTADO_DESACTIVADO : ESTADO_ACTIVO);
        documento.setFailedLoginAttempts(usuario.getIntentosFallidos());
        documento.setLockedUntil(usuario.getBloqueadoHasta());
        return documento;
    }

    public Usuario aDominio(UsuarioDocument documento) {
        Rol rol = Rol.valueOf(documento.getRol());
        Usuario datos = new Usuario(documento.getId(), documento.getNombre(), documento.getApellidos(),
                new Email(documento.getEmail()), documento.getPasswordHash(), rol, tipoCliente(documento, rol));
        boolean desactivado = ESTADO_DESACTIVADO.equals(documento.getEstado());
        return Usuario.reconstruir(datos, desactivado, documento.getFailedLoginAttempts(), documento.getLockedUntil());
    }

    // Los clientes guardados antes de la T210 no tienen tipoCliente: se leen como ESTANDAR,
    // igual que Usuario.nuevoCliente. Los demás roles no tienen tipo de cliente.
    private static TipoCliente tipoCliente(UsuarioDocument documento, Rol rol) {
        if (documento.getTipoCliente() != null) {
            return TipoCliente.valueOf(documento.getTipoCliente());
        }
        return rol == Rol.CLIENTE ? TipoCliente.ESTANDAR : null;
    }
}
