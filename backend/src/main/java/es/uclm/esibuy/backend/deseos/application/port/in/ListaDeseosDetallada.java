package es.uclm.esibuy.backend.deseos.application.port.in;

import java.util.List;

/** La lista de deseos de un cliente con los datos actuales de sus productos, en el orden en que se añadieron. */
public record ListaDeseosDetallada(List<DeseoDetallado> items) {
}
