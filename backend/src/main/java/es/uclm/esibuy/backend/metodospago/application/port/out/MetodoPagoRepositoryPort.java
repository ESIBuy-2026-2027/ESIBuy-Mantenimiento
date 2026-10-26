package es.uclm.esibuy.backend.metodospago.application.port.out;

import es.uclm.esibuy.backend.metodospago.domain.model.MetodoPago;
import java.util.List;

public interface MetodoPagoRepositoryPort {

    List<MetodoPago> buscarPorCliente(String clienteId);

    // Devuelve el método ya guardado, con su id si era nuevo
    MetodoPago guardar(MetodoPago metodo);

    void eliminar(String metodoId);
}
