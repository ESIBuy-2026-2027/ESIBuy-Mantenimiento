package es.uclm.esibuy.backend.valoraciones.application.port.out;

import java.util.List;

public interface ProductosDelVendedorPort {

    List<String> idsDe(String vendedorId);
}
