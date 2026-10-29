package es.uclm.esibuy.backend.direcciones.application.port.out;

import es.uclm.esibuy.backend.direcciones.domain.model.DireccionEnvio;
import java.util.List;

public interface DireccionRepositoryPort {

    List<DireccionEnvio> buscarPorCliente(String clienteId);

    // Devuelve la dirección ya guardada, con su id si era nueva
    DireccionEnvio guardar(DireccionEnvio direccion);

    void eliminar(String direccionId);
}
