package es.uclm.esibuy.backend.usuarios.application.port.out;

import java.util.Optional;

import es.uclm.esibuy.backend.usuarios.domain.model.CodigoVerificacion;

public interface CodigoVerificacionRepositoryPort {
    void guardar(CodigoVerificacion codigo);   // sustituye al código anterior del mismo usuario
    Optional<CodigoVerificacion> buscarPorUsuario(String usuarioId);
}
