package es.uclm.esibuy.backend.usuarios.application.port.out;

import java.util.Optional;

import es.uclm.esibuy.backend.usuarios.domain.model.FactorAutenticacion;

public interface FactorAutenticacionRepositoryPort {
    void guardar(FactorAutenticacion factor);

    Optional<FactorAutenticacion> buscarPorUsuario(String usuarioId);

    void eliminarPorUsuario(String usuarioId);
}
