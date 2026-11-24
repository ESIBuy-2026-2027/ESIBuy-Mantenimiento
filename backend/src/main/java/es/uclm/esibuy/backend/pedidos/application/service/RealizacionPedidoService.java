package es.uclm.esibuy.backend.pedidos.application.service;

import java.time.Clock;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;

import es.uclm.esibuy.backend.pedidos.application.port.in.RealizacionPedidoUseCase;
import es.uclm.esibuy.backend.pedidos.application.port.in.RealizarPedidoCommand;
import es.uclm.esibuy.backend.pedidos.application.port.out.CarritoPort;
import es.uclm.esibuy.backend.pedidos.application.port.out.ClienteEnSesionPort;
import es.uclm.esibuy.backend.pedidos.application.port.out.CompraProveedor;
import es.uclm.esibuy.backend.pedidos.application.port.out.ContadorPedidosPort;
import es.uclm.esibuy.backend.pedidos.application.port.out.DireccionesPort;
import es.uclm.esibuy.backend.pedidos.application.port.out.ExistenciasPort;
import es.uclm.esibuy.backend.pedidos.application.port.out.MetodoPagoPedido;
import es.uclm.esibuy.backend.pedidos.application.port.out.MetodosPagoPort;
import es.uclm.esibuy.backend.pedidos.application.port.out.PagoPort;
import es.uclm.esibuy.backend.pedidos.application.port.out.PedidoRepositoryPort;
import es.uclm.esibuy.backend.pedidos.domain.exception.CarritoSinProductosDelProveedorException;
import es.uclm.esibuy.backend.pedidos.domain.exception.DireccionNoValidaException;
import es.uclm.esibuy.backend.pedidos.domain.exception.MetodoPagoNoValidoException;
import es.uclm.esibuy.backend.pedidos.domain.exception.PagoRechazadoException;
import es.uclm.esibuy.backend.pedidos.domain.exception.StockInsuficientePedidoException;
import es.uclm.esibuy.backend.pedidos.domain.exception.TotalNoCoincideException;
import es.uclm.esibuy.backend.pedidos.domain.model.DatosPedido;
import es.uclm.esibuy.backend.pedidos.domain.model.DireccionEnvioPedido;
import es.uclm.esibuy.backend.pedidos.domain.model.EstadoPedido;
import es.uclm.esibuy.backend.pedidos.domain.model.LineaPedido;
import es.uclm.esibuy.backend.pedidos.domain.model.NumeroPedido;
import es.uclm.esibuy.backend.pedidos.domain.model.Pedido;
import es.uclm.esibuy.backend.shared.seguridad.Permisos;

/**
 * Realiza el pedido de un vendedor (HU10). Solo habla con el exterior a través de los puertos de salida. El orden es:
 * comprobar lo que no cambia nada (método de pago, dirección, carrito y total que vio el cliente), descontar el stock,
 * cobrar, numerar y guardar, y por último quitar los productos del carrito. Sin transacciones (decisión del equipo):
 * si algo falla después de descontar el stock, se repone lo descontado (compensación). El reloj se inyecta para fijar
 * en los tests la fecha y el año del número de pedido.
 */
public class RealizacionPedidoService implements RealizacionPedidoUseCase {

    private final PedidoRepositoryPort pedidoRepository;
    private final ClienteEnSesionPort clienteEnSesion;
    private final CarritoPort carrito;
    private final MetodosPagoPort metodosPago;
    private final DireccionesPort direcciones;
    private final ExistenciasPort existencias;
    private final PagoPort pago;
    private final ContadorPedidosPort contador;
    private final Clock reloj;

    public RealizacionPedidoService(DependenciasRealizacionPedido dependencias) {
        this.pedidoRepository = dependencias.pedidoRepository();
        this.clienteEnSesion = dependencias.clienteEnSesion();
        this.carrito = dependencias.carrito();
        this.metodosPago = dependencias.metodosPago();
        this.direcciones = dependencias.direcciones();
        this.existencias = dependencias.existencias();
        this.pago = dependencias.pago();
        this.contador = dependencias.contador();
        this.reloj = dependencias.reloj();
    }

    @PreAuthorize(Permisos.SOLO_CLIENTE_PROPIO_EN_COMANDO)
    @Override
    public Pedido realizar(RealizarPedidoCommand comando) {
        String email = comando.emailCliente();
        // Primero lo que no cambia nada: si falla, no hay que deshacer
        MetodoPagoPedido metodo = metodosPago.consultar(email, comando.paymentMethodId())
                .orElseThrow(() -> new MetodoPagoNoValidoException("El método de pago no es del cliente"));
        DireccionEnvioPedido direccion = direcciones.consultar(email, comando.direccionId())
                .orElseThrow(() -> new DireccionNoValidaException("La dirección de envío no es del cliente"));
        CompraProveedor compra = carrito.compraDelProveedor(email, comando.vendedorId())
                .orElseThrow(() -> new CarritoSinProductosDelProveedorException(
                        "El carrito no tiene productos de ese proveedor"));
        exigirTotalVisto(compra, comando);

        Pedido guardado = confirmar(comando, compra, direccion, metodo);
        carrito.quitar(email, compra.lineas().stream().map(LineaPedido::productoId).toList());
        return guardado;
    }

    // El cliente confirma lo que vio: si un precio, un descuento o una cantidad han cambiado, tiene que volver a verlo
    private static void exigirTotalVisto(CompraProveedor compra, RealizarPedidoCommand comando) {
        if (comando.totalEsperado() == null || compra.total().compareTo(comando.totalEsperado()) != 0) {
            throw new TotalNoCoincideException("El total del pedido ha cambiado");
        }
    }

    // Descuenta el stock, cobra y guarda. Si algo falla, el stock vuelve a como estaba y no se confirma nada
    private Pedido confirmar(RealizarPedidoCommand comando, CompraProveedor compra, DireccionEnvioPedido direccion,
            MetodoPagoPedido metodo) {
        List<LineaPedido> descontadas = descontarStock(compra.lineas());
        try {
            return cobrarYGuardar(comando, compra, direccion, metodo);
        } catch (RuntimeException e) {
            reponer(descontadas, e);
            throw e;
        }
    }

    // El número se pide después del cobro: un pago rechazado no gasta número
    private Pedido cobrarYGuardar(RealizarPedidoCommand comando, CompraProveedor compra,
            DireccionEnvioPedido direccion, MetodoPagoPedido metodo) {
        if (!pago.cobrar(metodo, compra.total())) {
            throw new PagoRechazadoException("El pago ha sido rechazado");
        }
        Instant ahora = Instant.now(reloj);
        int anio = ZonedDateTime.ofInstant(ahora, reloj.getZone()).getYear();
        NumeroPedido numero = NumeroPedido.de(anio, contador.siguiente(anio));
        DatosPedido datosPedido = new DatosPedido(
                null,
                numero,
                clienteEnSesion.idPorCorreo(comando.emailCliente()),
                compra.vendedorId(),
                compra.nombreComercial(),
                EstadoPedido.PAGADO,
                compra.lineas(),
                direccion,
                comando.paymentMethodId(),
                ahora);
        return pedidoRepository.guardar(Pedido.nuevo(datosPedido));
    }

    // Descuenta línea a línea; si a una le falta stock o algo falla, repone las anteriores y no se crea el pedido
    private List<LineaPedido> descontarStock(List<LineaPedido> lineas) {
        List<LineaPedido> descontadas = new ArrayList<>();
        try {
            for (LineaPedido linea : lineas) {
                if (!existencias.descontar(linea.productoId(), linea.cantidad())) {
                    throw new StockInsuficientePedidoException("No se puede comprar un producto del pedido",
                            linea.productoId(), linea.nombreProducto());
                }
                descontadas.add(linea);
            }
        } catch (RuntimeException e) {
            reponer(descontadas, e);
            throw e;
        }
        return descontadas;
    }

    // Repone todo lo que se pueda: si una reposición falla, se sigue con las demás y el fallo queda anotado en el
    // error original, que no se pierde
    private void reponer(List<LineaPedido> descontadas, RuntimeException causa) {
        for (LineaPedido linea : descontadas) {
            try {
                existencias.reponer(linea.productoId(), linea.cantidad());
            } catch (RuntimeException e) {
                causa.addSuppressed(e);
            }
        }
    }
}
