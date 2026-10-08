package es.uclm.esibuy.backend.usuarios.infrastructure.adapter.out.persistence;

import es.uclm.esibuy.backend.usuarios.domain.model.Usuario;

/**
 * Convierte el {@code Usuario} del dominio en el documento que se guarda en MongoDB.
 *
 * <p>Solo va en un sentido porque el puerto todavía no lee usuarios de la base de datos.
 */
public class UsuarioDocumentMapper {

    public UsuarioDocument aDocumento(Usuario usuario) {
        return new UsuarioDocument(usuario.getEmail().valor(), usuario.getContrasenaHash(), usuario.getRol().name());
    }
}
