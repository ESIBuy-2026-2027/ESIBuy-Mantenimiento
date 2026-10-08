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

    public UsuarioDocument aDocumento(Usuario usuario) {
        UsuarioDocument documento = new UsuarioDocument(usuario.getEmail().valor(), usuario.getPasswordHash(), usuario.getRol().name());
        documento.setId(usuario.getId());
        documento.setActivo(usuario.estaActivo());
        return documento;
    }

    // PENDIENTE (persistencia de los campos nuevos): el documento todavía no guarda nombre, apellidos,
    // tipoCliente ni los intentos fallidos. Mientras tanto, un CLIENTE se reconstruye como
    // ESTANDAR (igual que Usuario.nuevoCliente) y el resto de roles sin tipo de cliente.
    public Usuario aDominio(UsuarioDocument documento) {
        Rol rol = Rol.valueOf(documento.getRol());
        TipoCliente tipoCliente = rol == Rol.CLIENTE ? TipoCliente.ESTANDAR : null;
        Usuario usuario = new Usuario(documento.getId(), null, null, new Email(documento.getEmail()),
                documento.getPasswordHash(), rol, tipoCliente);

        // Solo se desactiva si el dato es explícitamente false; null (documento antiguo) = activo.
        if (Boolean.FALSE.equals(documento.getActivo())) {
            usuario.desactivar();
        }
        return usuario;
    }
}
