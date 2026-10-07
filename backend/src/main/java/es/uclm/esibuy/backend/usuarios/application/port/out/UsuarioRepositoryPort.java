package es.uclm.esibuy.backend.usuarios.application.port.out;

import es.uclm.esibuy.backend.usuarios.domain.model.Usuario;

public interface UsuarioRepositoryPort {

    void guardar(Usuario usuario);

    boolean existePorCorreo(String correo);

}
