package es.uclm.esibuy.backend.usuarios.infrastructure.adapter.out.persistence;

import es.uclm.esibuy.backend.usuarios.domain.model.Rol;
import es.uclm.esibuy.backend.usuarios.domain.model.Usuario;

/**
 * Convierte el {@code Usuario} del dominio en el documento que se guarda en MongoDB.
 *
 * <p>Convierte en los dos sentidos: {@code aDocumento} para guardar y {@code aDominio} para leer.
 */
public class UsuarioDocumentMapper {

    public UsuarioDocument aDocumento(Usuario usuario) {
        return new UsuarioDocument(usuario.getEmail(), usuario.getPasswordHash(), usuario.getRol().name());
    }

    public Usuario aDominio(UsuarioDocument documento) {
        return new Usuario(documento.getEmail(), documento.getPasswordHash(), Rol.valueOf(documento.getRol()));
    }
}
