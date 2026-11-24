package es.uclm.esibuy.backend.pedidos.infrastructure.adapter.out.persistence;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import es.uclm.esibuy.backend.pedidos.domain.exception.PedidoInvalidoException;
import es.uclm.esibuy.backend.pedidos.domain.model.CambioEstado;
import es.uclm.esibuy.backend.pedidos.domain.model.DatosPedido;
import es.uclm.esibuy.backend.pedidos.domain.model.DireccionEnvioPedido;
import es.uclm.esibuy.backend.pedidos.domain.model.EstadoPedido;
import es.uclm.esibuy.backend.pedidos.domain.model.LineaPedido;
import es.uclm.esibuy.backend.pedidos.domain.model.NumeroPedido;
import es.uclm.esibuy.backend.pedidos.domain.model.Pedido;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

// Sin Spring: conversión entre Pedido y su documento de MongoDB
class PedidoDocumentMapperTest {

    private static final Instant AHORA = Instant.parse("2026-10-29T10:00:00Z");

    private final PedidoDocumentMapper mapper = new PedidoDocumentMapper();

    static Pedido pedido(String id, String numero) {
        DatosPedido datosPedido = new DatosPedido(
                id,
                new NumeroPedido(numero),
                "c-1",
                "u-2",
                "TecnoVera",
                EstadoPedido.PAGADO,
                List.of(
                        new LineaPedido("p-1", "u-2", "Auriculares", 2, new BigDecimal("53.91")),
                        new LineaPedido("p-2", "u-2", "Teclado", 1, new BigDecimal("89.50"))),
                new DireccionEnvioPedido(
                        "Carlos Cliente",
                        "Calle Mayor 1",
                        "2º B",
                        "13001",
                        "Ciudad Real",
                        "Ciudad Real",
                        "611222333"),
                "mp-1",
                AHORA);

        return new Pedido(datosPedido);
    }

    @Test
    @DisplayName("HU10-CP19: un pedido pasa a documento y vuelve sin perder ningún dato")
    void convierteADocumentoYVueltaSinPerderDatos() {
        Pedido pedido = pedido("o-1", "ESB-2026-000001");

        PedidoDocument documento = mapper.aDocumento(pedido);
        Pedido leido = mapper.aDominio(documento);

        assertEquals("PAGADO", documento.getEstado());
        assertEquals(new BigDecimal("197.32"), documento.getTotal());
        assertEquals("u-2", documento.getLineas().get(0).getVendedorId());
        assertEquals(new BigDecimal("107.82"), documento.getLineas().get(0).getSubtotal());
        assertEquals("o-1", leido.getId());
        assertEquals(pedido.getNumeroPedido(), leido.getNumeroPedido());
        assertEquals(pedido.getLineas(), leido.getLineas());
        assertEquals(pedido.getDireccionEnvio(), leido.getDireccionEnvio());
        assertEquals("TecnoVera", leido.getNombreComercial());
        assertEquals("mp-1", leido.getPaymentMethodId());
        assertEquals(AHORA, leido.getCreatedAt());
    }

    @Test
    @DisplayName("HU10-CP19: rechaza un documento que no cumple las reglas del dominio")
    void rechazaUnDocumentoQueNoCumpleElDominio() {
        PedidoDocument documento = mapper.aDocumento(pedido("o-1", "ESB-2026-000001"));
        documento.setNumeroPedido("2026-1");

        assertThrows(PedidoInvalidoException.class, () -> mapper.aDominio(documento));
    }

    @Test
    @DisplayName("HU11-CP05: cada estado de envío del pedido se guarda en el documento y se vuelve a leer igual")
    void cadaEstadoDeEnvioSeGuardaYSeLee() {
        for (EstadoPedido estado : EstadoPedido.values()) {
            Pedido base = pedido("o-1", "ESB-2026-000001");

            DatosPedido datosPedido = new DatosPedido(
                    base.getId(),
                    base.getNumeroPedido(),
                    base.getClienteId(),
                    base.getVendedorId(),
                    base.getNombreComercial(),
                    estado,
                    base.getLineas(),
                    base.getDireccionEnvio(),
                    base.getPaymentMethodId(),
                    base.getCreatedAt());

            Pedido pedido = new Pedido(datosPedido);

            PedidoDocument documento = mapper.aDocumento(pedido);

            assertEquals(estado.name(), documento.getEstado());
            assertEquals(estado, mapper.aDominio(documento).getEstado());
        }
    }

    @Test
    @DisplayName("HU11-CP32: el histórico y las fechas de entrega pasan a documento y vuelven sin perder ningún dato")
    void conservaElHistoricoYLasFechasDeEntrega() {
        Instant enviado = Instant.parse("2026-10-30T09:00:00Z");
        Instant estimada = Instant.parse("2026-11-04T00:00:00Z");
        Instant entrega = Instant.parse("2026-11-05T12:30:00Z");
        Pedido base = pedido("o-1", "ESB-2026-000001");
        Pedido pedido = new Pedido(new DatosPedido("o-1", base.getNumeroPedido(), "c-1", "u-2", "TecnoVera",
                EstadoPedido.ENTREGADO, base.getLineas(), base.getDireccionEnvio(), "mp-1", AHORA,
                List.of(new CambioEstado(EstadoPedido.PAGADO, AHORA), new CambioEstado(EstadoPedido.ENVIADO, enviado),
                        new CambioEstado(EstadoPedido.ENTREGADO, entrega)),
                estimada, entrega));

        PedidoDocument documento = mapper.aDocumento(pedido);
        Pedido leido = mapper.aDominio(documento);

        assertEquals("ENVIADO", documento.getHistorial().get(1).getEstado());
        assertEquals(enviado, documento.getHistorial().get(1).getFecha());
        assertEquals(pedido.getHistorial(), leido.getHistorial());
        assertEquals(estimada, leido.getFechaEstimadaEntrega());
        assertEquals(entrega, leido.getFechaEntrega());
    }

    @Test
    @DisplayName("HU11-CP32: un pedido guardado antes del seguimiento, sin histórico ni fechas, se lee con el histórico vacío")
    void unDocumentoSinHistoricoSeLeeVacio() {
        PedidoDocument documento = mapper.aDocumento(pedido("o-1", "ESB-2026-000001"));
        documento.setHistorial(null);

        Pedido leido = mapper.aDominio(documento);

        assertEquals(List.of(), leido.getHistorial());
        assertNull(leido.getFechaEstimadaEntrega());
        assertNull(leido.getFechaEntrega());
    }

    @Test
    @DisplayName("HU11-CP32: un estado desconocido en el histórico guardado es un documento inválido")
    void unEstadoDesconocidoEnElHistoricoEsInvalido() {
        PedidoDocument documento = mapper.aDocumento(pedido("o-1", "ESB-2026-000001"));
        CambioEstadoDocument cambio = new CambioEstadoDocument();
        cambio.setEstado("EN_REPARTO");
        cambio.setFecha(AHORA);
        documento.setHistorial(List.of(cambio));

        assertThrows(PedidoInvalidoException.class, () -> mapper.aDominio(documento));
    }
}
