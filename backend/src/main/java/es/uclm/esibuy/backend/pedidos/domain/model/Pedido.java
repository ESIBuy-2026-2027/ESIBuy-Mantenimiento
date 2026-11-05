package es.uclm.esibuy.backend.pedidos.domain.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import es.uclm.esibuy.backend.pedidos.domain.exception.PedidoInvalidoException;

/**
 * Pedido de un cliente a un solo proveedor (HU10; decisión del equipo: un pedido por proveedor). El total se
 * calcula siempre a partir de las líneas, nunca se recibe.
 */
public class Pedido {

    private final String id;
    private final NumeroPedido numeroPedido;
    private final String clienteId;
    private final String vendedorId;
    private final String nombreComercial;
    private final EstadoPedido estado;
    private final List<LineaPedido> lineas;
    private final DireccionEnvioPedido direccionEnvio;
    private final String paymentMethodId;
    private final Instant createdAt;

    public Pedido(String id, NumeroPedido numeroPedido, String clienteId, String vendedorId, String nombreComercial,
            EstadoPedido estado, List<LineaPedido> lineas, DireccionEnvioPedido direccionEnvio,
            String paymentMethodId, Instant createdAt) {
        exigirTexto(clienteId, "El pedido necesita el cliente");
        exigirTexto(vendedorId, "El pedido necesita el proveedor");
        exigirTexto(paymentMethodId, "El pedido necesita el método de pago");
        exigirPresente(numeroPedido, "El pedido necesita su número");
        exigirPresente(estado, "El pedido necesita su estado");
        exigirPresente(direccionEnvio, "El pedido necesita la dirección de envío");
        if (lineas == null || lineas.isEmpty()) {
            throw new PedidoInvalidoException("El pedido necesita al menos un producto");
        }
        if (lineas.stream().map(LineaPedido::productoId).distinct().count() != lineas.size()) {
            throw new PedidoInvalidoException("Un producto solo puede aparecer una vez en el pedido");
        }
        this.id = id;
        this.numeroPedido = numeroPedido;
        this.clienteId = clienteId;
        this.vendedorId = vendedorId;
        this.nombreComercial = nombreComercial;
        this.estado = estado;
        this.lineas = List.copyOf(lineas);
        this.direccionEnvio = direccionEnvio;
        this.paymentMethodId = paymentMethodId;
        this.createdAt = createdAt;
    }

    // Pedido aún no guardado. Nace pagado: el pago se simula antes de crearlo
    public static Pedido nuevo(NumeroPedido numeroPedido, String clienteId, String vendedorId, String nombreComercial,
            List<LineaPedido> lineas, DireccionEnvioPedido direccionEnvio, String paymentMethodId, Instant ahora) {
        return new Pedido(null, numeroPedido, clienteId, vendedorId, nombreComercial, EstadoPedido.PAGADO, lineas,
                direccionEnvio, paymentMethodId, ahora);
    }

    private static void exigirTexto(String valor, String mensaje) {
        if (valor == null || valor.isBlank()) {
            throw new PedidoInvalidoException(mensaje);
        }
    }

    private static void exigirPresente(Object valor, String mensaje) {
        if (valor == null) {
            throw new PedidoInvalidoException(mensaje);
        }
    }

    public BigDecimal getTotal() {
        return lineas.stream().map(LineaPedido::subtotal).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public String getId() { return id; }
    public NumeroPedido getNumeroPedido() { return numeroPedido; }
    public String getClienteId() { return clienteId; }
    public String getVendedorId() { return vendedorId; }
    public String getNombreComercial() { return nombreComercial; }
    public EstadoPedido getEstado() { return estado; }
    public List<LineaPedido> getLineas() { return lineas; }
    public DireccionEnvioPedido getDireccionEnvio() { return direccionEnvio; }
    public String getPaymentMethodId() { return paymentMethodId; }
    public Instant getCreatedAt() { return createdAt; }
}
