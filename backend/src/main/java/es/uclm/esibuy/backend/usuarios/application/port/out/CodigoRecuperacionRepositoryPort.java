package es.uclm.esibuy.backend.usuarios.application.port.out;

import java.util.Optional;

import es.uclm.esibuy.backend.usuarios.domain.model.CodigoVerificacion;

// Códigos de recuperación de contraseña (HU3). Son del mismo tipo que los del login, pero se guardan aparte:
// pedir uno no anula el código del login, ni al revés.
public interface CodigoRecuperacionRepositoryPort {
    void guardar(CodigoVerificacion codigo);   // sustituye al código de recuperación anterior del mismo usuario
    Optional<CodigoVerificacion> buscarPorUsuario(String usuarioId);
}
