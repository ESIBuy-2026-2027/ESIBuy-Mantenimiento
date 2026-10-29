package es.uclm.esibuy.backend.carrito.infrastructure.adapter.out.persistence;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import es.uclm.esibuy.backend.carrito.domain.exception.CantidadInvalidaException;
import es.uclm.esibuy.backend.carrito.domain.model.Carrito;
import es.uclm.esibuy.backend.carrito.domain.model.LineaCarrito;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

// Sin Spring: conversión entre Carrito y su documento de MongoDB
class CarritoDocumentMapperTest {

    private static final Instant AYER = Instant.parse("2026-10-28T10:00:00Z");
    private static final Instant HOY = Instant.parse("2026-10-29T10:00:00Z");

    private final CarritoDocumentMapper mapper = new CarritoDocumentMapper();

    @Test
    @DisplayName("HU9-CP17: un carrito pasa a documento y vuelve sin perder ningún dato")
    void convierteADocumentoYVueltaSinPerderDatos() {
        Carrito carrito = new Carrito("car-1", "cliente-1",
                List.of(new LineaCarrito("p-1", 2, AYER, HOY), new LineaCarrito("p-2", 1, HOY, HOY)));

        CarritoDocument documento = mapper.aDocumento(carrito);
        Carrito leido = mapper.aDominio(documento);

        assertEquals("p-1", documento.getItems().get(0).getProductoId());
        assertEquals("car-1", leido.getId());
        assertEquals("cliente-1", leido.getClienteId());
        assertEquals(carrito.getLineas(), leido.getLineas());
    }

    @Test
    @DisplayName("HU9-CP17: rechaza un documento con una cantidad que no cumple las reglas del dominio")
    void rechazaUnDocumentoQueNoCumpleElDominio() {
        CarritoDocument documento = mapper.aDocumento(
                new Carrito("car-1", "cliente-1", List.of(new LineaCarrito("p-1", 2, AYER, HOY))));
        documento.getItems().get(0).setCantidad(0);

        assertThrows(CantidadInvalidaException.class, () -> mapper.aDominio(documento));
    }
}
