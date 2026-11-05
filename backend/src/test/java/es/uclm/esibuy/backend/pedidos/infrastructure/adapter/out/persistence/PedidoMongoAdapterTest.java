package es.uclm.esibuy.backend.pedidos.infrastructure.adapter.out.persistence;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import es.uclm.esibuy.backend.MongoTestcontainersConfig;
import es.uclm.esibuy.backend.pedidos.domain.model.DireccionEnvioPedido;
import es.uclm.esibuy.backend.pedidos.domain.model.EstadoPedido;
import es.uclm.esibuy.backend.pedidos.domain.model.LineaPedido;
import es.uclm.esibuy.backend.pedidos.domain.model.NumeroPedido;
import es.uclm.esibuy.backend.pedidos.domain.model.Pedido;
import es.uclm.esibuy.backend.shared.config.MongoConfig;
import es.uclm.esibuy.backend.shared.infrastructure.config.InicializadorColecciones;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import org.bson.Document;
import org.bson.types.Decimal128;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.mongodb.test.autoconfigure.DataMongoTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.mongodb.core.MongoTemplate;

// Test de integración del adaptador de MongoDB de pedidos (HU10). Usa un MongoDB real en Docker (Testcontainers):
// Docker Desktop debe estar abierto. InicializadorColecciones crea las colecciones con su validación e índices.
// También la consulta de compras de HU13 (T370). PENDIENTE DE CONFIRMAR CON EL EQUIPO: caso HU13-CP08, propuesto.
// Y el historial y el detalle de los pedidos del cliente (HU11, T378): caso HU11-CP04, propuesto.
// Y guardar un pedido cancelado (HU12, T375): caso HU12-CP08, propuesto.
// Y los pedidos de un vendedor para sus estadísticas (HU17, T280): caso HU17-CP07, propuesto.
@DataMongoTest
@Import({MongoTestcontainersConfig.class, MongoConfig.class, InicializadorColecciones.class})
class PedidoMongoAdapterTest {

    private static final Instant AHORA = Instant.parse("2026-10-29T10:00:00Z");
    private static final DireccionEnvioPedido DIRECCION = new DireccionEnvioPedido("Carlos Cliente",
            "Calle Mayor 1", null, "13001", "Ciudad Real", "Ciudad Real", "611222333");

    @Autowired
    private PedidoMongoRepository pedidoMongoRepository;

    @Autowired
    private MongoTemplate mongoTemplate;

    private PedidoMongoAdapter adapter;

    @BeforeEach
    void preparar() {
        pedidoMongoRepository.deleteAll();
        adapter = new PedidoMongoAdapter(pedidoMongoRepository, new PedidoDocumentMapper());
    }

    @Test
    @DisplayName("HU10-CP19: guardar un pedido le asigna un id y lo deja en la colección pedidos")
    void guardarUnPedidoLeAsignaUnId() {
        Pedido guardado = adapter.guardar(PedidoDocumentMapperTest.pedido(null, "ESB-2026-000001"));

        assertNotNull(guardado.getId());
        Document enMongo = mongoTemplate.getCollection("pedidos").find().first();
        assertNotNull(enMongo);
        assertEquals("ESB-2026-000001", enMongo.get("numeroPedido"));
        assertEquals("c-1", enMongo.get("clienteId"));
        assertEquals("PAGADO", enMongo.get("estado"));
        assertEquals("mp-1", enMongo.get("paymentMethodId"));
        // Los importes van como Decimal128: el validador de la colección exige un número
        assertEquals(new Decimal128(new BigDecimal("197.32")), enMongo.get("total"));
        assertEquals("Calle Mayor 1", enMongo.get("direccionEnvio", Document.class).get("calle"));
    }

    @Test
    @DisplayName("HU10-CP19: dos pedidos no pueden tener el mismo número")
    void dosPedidosNoPuedenTenerElMismoNumero() {
        adapter.guardar(PedidoDocumentMapperTest.pedido(null, "ESB-2026-000001"));

        assertThrows(DuplicateKeyException.class,
                () -> adapter.guardar(PedidoDocumentMapperTest.pedido(null, "ESB-2026-000001")));
    }

    private static Pedido pedidoPagado(String numero, String clienteId, String... productos) {
        List<LineaPedido> lineas = Arrays.stream(productos)
                .map(productoId -> new LineaPedido(productoId, "Producto " + productoId, 1, new BigDecimal("10.00")))
                .toList();
        return new Pedido(null, new NumeroPedido(numero), clienteId, "u-2", "TecnoVera", EstadoPedido.PAGADO, lineas,
                DIRECCION, "mp-1", AHORA);
    }

    @Test
    @DisplayName("HU13-CP08: un pedido pagado del cliente con el producto en cualquiera de sus líneas es una compra")
    void unPedidoPagadoConElProductoEsUnaCompra() {
        adapter.guardar(pedidoPagado("ESB-2026-000001", "c-1", "p-1", "p-2"));

        assertTrue(adapter.existePagadoConProducto("c-1", "p-1"));
        assertTrue(adapter.existePagadoConProducto("c-1", "p-2"));
    }

    @Test
    @DisplayName("HU47-CP02: buscar pedidos devuelve solo los del vendedor indicado")
    void buscarPorVendedorDevuelveSoloLosDelVendedor() {
        adapter.guardar(pedidoPagado("ESB-2026-000001", "c-1", "p-1"));
        Pedido deOtroVendedor = new Pedido(null, new NumeroPedido("ESB-2026-000002"), "c-1", "u-3", "Otra tienda",
                EstadoPedido.PAGADO, List.of(new LineaPedido("p-2", "Producto p-2", 1, new BigDecimal("10.00"))),
                DIRECCION, "mp-1", AHORA);
        adapter.guardar(deOtroVendedor);

        List<Pedido> pedidos = adapter.buscarPorVendedor("u-2");

        assertEquals(1, pedidos.size());
        assertEquals("u-2", pedidos.get(0).getVendedorId());
    }

    @Test
    @DisplayName("HU13-CP08: no es compra un producto que no está en sus pedidos ni el pedido de otro cliente")
    void noEsCompraSinElProductoNiConElPedidoDeOtroCliente() {
        adapter.guardar(pedidoPagado("ESB-2026-000001", "c-1", "p-1"));
        adapter.guardar(pedidoPagado("ESB-2026-000002", "c-2", "p-2"));

        assertFalse(adapter.existePagadoConProducto("c-1", "p-2"));
        assertFalse(adapter.existePagadoConProducto("c-2", "p-1"));
        assertFalse(adapter.existePagadoConProducto("c-3", "p-1"));
    }

    @Test
    @DisplayName("HU13-CP08: solo cuenta como compra un pedido en estado PAGADO")
    void soloCuentaUnPedidoPagado() {
        // Un pedido en otro estado, insertado directamente: el dominio aún solo tiene PAGADO (HU11 y HU12)
        mongoTemplate.getCollection("pedidos").insertOne(new Document("clienteId", "c-1")
                .append("estado", "CANCELADO")
                .append("lineas", List.of(new Document("productoId", "p-9").append("cantidad", 1)
                        .append("precioUnitario", new Decimal128(new BigDecimal("10.00"))))));

        assertFalse(adapter.existePagadoConProducto("c-1", "p-9"));
    }

    private static Pedido pedidoDelCliente(String numero, String clienteId, Instant creado) {
        return new Pedido(null, new NumeroPedido(numero), clienteId, "u-2", "TecnoVera", EstadoPedido.PAGADO,
                List.of(new LineaPedido("p-1", "Producto p-1", 1, new BigDecimal("10.00"))), DIRECCION, "mp-1",
                creado);
    }

    @Test
    @DisplayName("HU11-CP04: buscarPorCliente devuelve solo los pedidos del cliente, del más reciente al más antiguo")
    void buscarPorClienteDevuelveSusPedidosDelMasRecienteAlMasAntiguo() {
        adapter.guardar(pedidoDelCliente("ESB-2026-000001", "c-1", AHORA));
        adapter.guardar(pedidoDelCliente("ESB-2026-000002", "c-2", AHORA.plusSeconds(60)));
        adapter.guardar(pedidoDelCliente("ESB-2026-000003", "c-1", AHORA.plusSeconds(120)));

        List<String> numeros = adapter.buscarPorCliente("c-1").stream()
                .map(pedido -> pedido.getNumeroPedido().valor())
                .toList();

        assertEquals(List.of("ESB-2026-000003", "ESB-2026-000001"), numeros);
        assertTrue(adapter.buscarPorCliente("c-sin-pedidos").isEmpty());
    }

    @Test
    @DisplayName("HU11-CP04: buscarPorId devuelve el pedido con sus líneas, y nada si no existe")
    void buscarPorIdDevuelveElPedidoConSusLineas() {
        Pedido guardado = adapter.guardar(PedidoDocumentMapperTest.pedido(null, "ESB-2026-000001"));

        Pedido leido = adapter.buscarPorId(guardado.getId()).orElseThrow();

        assertEquals("ESB-2026-000001", leido.getNumeroPedido().valor());
        assertEquals(2, leido.getLineas().size());
        assertEquals(new BigDecimal("197.32"), leido.getTotal());
        assertTrue(adapter.buscarPorId("000000000000000000000000").isEmpty());
    }

    @Test
    @DisplayName("HU12-CP08: guardar un pedido leído y cancelado actualiza el mismo documento, sin crear otro")
    void guardarUnPedidoCanceladoActualizaElMismoDocumento() {
        Pedido guardado = adapter.guardar(pedidoDelCliente("ESB-2026-000001", "c-1", AHORA));
        Pedido leido = adapter.buscarPorId(guardado.getId()).orElseThrow();

        leido.cancelar();
        adapter.guardar(leido);

        assertEquals(1, pedidoMongoRepository.count());
        Pedido releido = adapter.buscarPorId(guardado.getId()).orElseThrow();
        assertEquals(EstadoPedido.CANCELADO, releido.getEstado());
        assertEquals("ESB-2026-000001", releido.getNumeroPedido().valor());
        assertEquals(AHORA, releido.getCreatedAt());
    }

    @Test
    @DisplayName("HU17-CP07: buscarPorVendedor devuelve todos los pedidos del vendedor, también los cancelados")
    void buscarPorVendedorIncluyeLosPedidosCancelados() {
        adapter.guardar(pedidoDelVendedor("ESB-2026-000001", "u-2", EstadoPedido.PAGADO));
        adapter.guardar(pedidoDelVendedor("ESB-2026-000002", "u-3", EstadoPedido.PAGADO));
        adapter.guardar(pedidoDelVendedor("ESB-2026-000003", "u-2", EstadoPedido.CANCELADO));

        List<String> numeros = adapter.buscarPorVendedor("u-2").stream()
                .map(pedido -> pedido.getNumeroPedido().valor())
                .sorted()
                .toList();

        assertEquals(List.of("ESB-2026-000001", "ESB-2026-000003"), numeros);
        assertTrue(adapter.buscarPorVendedor("u-sin-pedidos").isEmpty());
    }

    private static Pedido pedidoDelVendedor(String numero, String vendedorId, EstadoPedido estado) {
        return new Pedido(null, new NumeroPedido(numero), "c-1", vendedorId, "TecnoVera", estado,
                List.of(new LineaPedido("p-1", "Producto p-1", 1, new BigDecimal("10.00"))), DIRECCION, "mp-1",
                AHORA);
    }
}
