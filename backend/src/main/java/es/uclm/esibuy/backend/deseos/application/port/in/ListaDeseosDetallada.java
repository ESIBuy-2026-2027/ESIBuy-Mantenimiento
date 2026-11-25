package es.uclm.esibuy.backend.deseos.application.port.in;

import java.util.List;

import es.uclm.esibuy.backend.deseos.domain.model.ListaDeseos;

/** La lista de deseos de un cliente con los datos actuales de sus productos, en el orden en que se añadieron. */
public record ListaDeseosDetallada(String id, String nombre, List<DeseoDetallado> items) {

    public ListaDeseosDetallada(List<DeseoDetallado> items) {
        this(null, ListaDeseos.NOMBRE_FAVORITOS, items);
    }
}
