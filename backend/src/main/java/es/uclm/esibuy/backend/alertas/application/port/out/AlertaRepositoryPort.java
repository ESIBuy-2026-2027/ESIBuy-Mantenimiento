package es.uclm.esibuy.backend.alertas.application.port.out;

import java.util.List;
import java.util.Optional;

import es.uclm.esibuy.backend.alertas.domain.model.Alerta;

/** Persistencia de las alertas de los clientes (AB#529), reutilizable por AB#530, AB#531 y AB#532. */
public interface AlertaRepositoryPort {

    /** Guarda varias alertas nuevas a la vez (una por cliente interesado). */
    void guardarTodas(List<Alerta> alertas);

    /** Las alertas de un cliente, de la más reciente a la más antigua. */
    List<Alerta> buscarPorCliente(String clienteId);

    /** Una alerta del cliente (AB#531). Vacío si no existe o es de otro cliente. */
    Optional<Alerta> buscarPorIdYCliente(String alertaId, String clienteId);

    /** Guarda los cambios de una alerta ya existente (AB#531) y la devuelve como queda guardada. */
    Alerta guardar(Alerta alerta);
}
