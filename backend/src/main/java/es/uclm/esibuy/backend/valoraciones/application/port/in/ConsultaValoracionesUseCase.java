package es.uclm.esibuy.backend.valoraciones.application.port.in;

import java.util.List;

import es.uclm.esibuy.backend.valoraciones.domain.model.Valoracion;

/** Consulta de valoraciones y del estado de compra/valoración de un cliente (CLI-03/CLI-04). */
public interface ConsultaValoracionesUseCase {

    List<Valoracion> consultarPorProducto(String productoId);

    EstadoValoracion consultarEstado(String emailCliente, String productoId);
}
