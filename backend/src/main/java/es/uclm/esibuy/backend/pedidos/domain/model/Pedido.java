package es.uclm.esibuy.backend.pedidos.domain.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import es.uclm.esibuy.backend.pedidos.domain.exception.PedidoInvalidoException;
import es.uclm.esibuy.backend.pedidos.domain.exception.PedidoNoCancelableException;

/**
 * Pedido de un cliente a un solo vendedor (HU10; regla del equipo: no se mezclan productos de vendedores distintos).
 * El descuento va en el precio de cada producto; el total se calcula siempre sumando las líneas.
 */
public class Pedido {

    private final String id;
    private final NumeroPedido numeroPedido;
    private final String clienteId;
    private final String vendedorId;
    private final String nombreComercial;
    private EstadoPedido estado;
    private final List<LineaPedido> lineas;
    private final DireccionEnvioPedido direccionEnvio;
    private final String paymentMethodId;
    private final Instant createdAt;

    public Pedido(DatosPedido datos) {
        exigirPresente(datos, "El pedido necesita sus datos");

        exigirTexto(datos.clienteId(), "El pedido necesita el cliente");
        exigirTexto(datos.vendedorId(), "El pedido necesita el proveedor");
        exigirTexto(datos.paymentMethodId(), "El pedido necesita el método de pago");

        exigirPresente(datos.numeroPedido(), "El pedido necesita su número");
        exigirPresente(datos.estado(), "El pedido necesita su estado");
        exigirPresente(datos.direccionEnvio(),
                "El pedido necesita la dirección de envío");

        List<LineaPedido> lineasPedido = datos.lineas();

        if (lineasPedido == null || lineasPedido.isEmpty()) {
            throw new PedidoInvalidoException(
                    "El pedido necesita al menos un producto");
        }

        if (lineasPedido.stream()
                .map(LineaPedido::productoId)
                .distinct()
                .count() != lineasPedido.size()) {
            throw new PedidoInvalidoException(
                    "Un producto solo puede aparecer una vez en el pedido");
        }

        if (lineasPedido.stream().anyMatch(linea -> !linea.vendedorId().equals(datos.vendedorId()))) {
            throw new PedidoInvalidoException(
                    "Un pedido solo puede tener productos de un vendedor");
        }

        this.id = datos.id();
        this.numeroPedido = datos.numeroPedido();
        this.clienteId = datos.clienteId();
        this.vendedorId = datos.vendedorId();
        this.nombreComercial = datos.nombreComercial();
        this.estado = datos.estado();
        this.lineas = List.copyOf(lineasPedido);
        this.direccionEnvio = datos.direccionEnvio();
        this.paymentMethodId = datos.paymentMethodId();
        this.createdAt = datos.createdAt();
    }

    // Pedido aún no guardado. Nace pagado.
    public static Pedido nuevo(DatosPedido datos) {
        exigirPresente(datos, "El pedido necesita sus datos");

        return new Pedido(new DatosPedido(
                null,
                datos.numeroPedido(),
                datos.clienteId(),
                datos.vendedorId(),
                datos.nombreComercial(),
                EstadoPedido.PAGADO,
                datos.lineas(),
                datos.direccionEnvio(),
                datos.paymentMethodId(),
                datos.createdAt()));
    }

    // Cancelación (HU12): solo mientras el pedido no se ha enviado.
    public void cancelar() {
        if (estado != EstadoPedido.PAGADO
                && estado != EstadoPedido.PREPARANDO) {
            throw new PedidoNoCancelableException(
                    "El pedido ya no se puede cancelar");
        }

        estado = EstadoPedido.CANCELADO;
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

    // Suma de las líneas, cada una con el descuento ya aplicado a su precio unitario
    public static BigDecimal totalDe(List<LineaPedido> lineas) {
        return lineas.stream()
                .map(LineaPedido::subtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public BigDecimal getTotal() {
        return totalDe(lineas);
    }

    public String getId() {
        return id;
    }

    public NumeroPedido getNumeroPedido() {
        return numeroPedido;
    }

    public String getClienteId() {
        return clienteId;
    }

    public String getVendedorId() {
        return vendedorId;
    }

    public String getNombreComercial() {
        return nombreComercial;
    }

    public EstadoPedido getEstado() {
        return estado;
    }

    public List<LineaPedido> getLineas() {
        return lineas;
    }

    public DireccionEnvioPedido getDireccionEnvio() {
        return direccionEnvio;
    }

    public String getPaymentMethodId() {
        return paymentMethodId;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}