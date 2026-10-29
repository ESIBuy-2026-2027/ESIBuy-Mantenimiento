package es.uclm.esibuy.backend.pedidos.application.service;

import java.time.Clock;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

import es.uclm.esibuy.backend.pedidos.application.port.in.RealizacionPedidoUseCase;
import es.uclm.esibuy.backend.pedidos.application.port.in.RealizarPedidoCommand;
import es.uclm.esibuy.backend.pedidos.application.port.out.CarritoPort;
import es.uclm.esibuy.backend.pedidos.application.port.out.ClienteEnSesionPort;
import es.uclm.esibuy.backend.pedidos.application.port.out.CompraProveedor;
import es.uclm.esibuy.backend.pedidos.application.port.out.ContadorPedidosPort;
import es.uclm.esibuy.backend.pedidos.application.port.out.DireccionesPort;
import es.uclm.esibuy.backend.pedidos.application.port.out.ExistenciasPort;
import es.uclm.esibuy.backend.pedidos.application.port.out.MetodosPagoPort;
import es.uclm.esibuy.backend.pedidos.application.port.out.PedidoRepositoryPort;
import es.uclm.esibuy.backend.pedidos.domain.exception.CarritoSinProductosDelProveedorException;
import es.uclm.esibuy.backend.pedidos.domain.exception.DireccionNoValidaException;
import es.uclm.esibuy.backend.pedidos.domain.exception.MetodoPagoNoValidoException;
import es.uclm.esibuy.backend.pedidos.domain.exception.StockInsuficienteException;
import es.uclm.esibuy.backend.pedidos.domain.model.DireccionEnvioPedido;
import es.uclm.esibuy.backend.pedidos.domain.model.LineaPedido;
import es.uclm.esibuy.backend.pedidos.domain.model.NumeroPedido;
import es.uclm.esibuy.backend.pedidos.domain.model.Pedido;

/**
 * Realiza el pedido de un proveedor (HU10). Solo habla con el exterior a través de los puertos de salida. Sin
 * transacciones (decisión del equipo): si algo falla después de descontar el stock, se repone lo descontado
 * (compensación). El reloj se inyecta para fijar en los tests la fecha y el año del número de pedido.
 */
public class RealizacionPedidoService implements RealizacionPedidoUseCase {

    private final PedidoRepositoryPort pedidoRepository;
    private final ClienteEnSesionPort clienteEnSesion;
    private final CarritoPort carrito;
    private final MetodosPagoPort metodosPago;
    private final DireccionesPort direcciones;
    private final ExistenciasPort existencias;
    private final ContadorPedidosPort contador;
    private final Clock reloj;

    public RealizacionPedidoService(PedidoRepositoryPort pedidoRepository, ClienteEnSesionPort clienteEnSesion,
            CarritoPort carrito, MetodosPagoPort metodosPago, DireccionesPort direcciones, ExistenciasPort existencias,
            ContadorPedidosPort contador, Clock reloj) {
        this.pedidoRepository = pedidoRepository;
        this.clienteEnSesion = clienteEnSesion;
        this.carrito = carrito;
        this.metodosPago = metodosPago;
        this.direcciones = direcciones;
        this.existencias = existencias;
        this.contador = contador;
        this.reloj = reloj;
    }

    @Override
    public Pedido realizar(RealizarPedidoCommand comando) {
        String email = comando.emailCliente();
        // Primero lo que no cambia nada: si falla, no hay que deshacer
        if (!metodosPago.esDelCliente(email, comando.paymentMethodId())) {
            throw new MetodoPagoNoValidoException("El método de pago no es del cliente");
        }
        DireccionEnvioPedido direccion = direcciones.consultar(email, comando.direccionId())
                .orElseThrow(() -> new DireccionNoValidaException("La dirección de envío no es del cliente"));
        CompraProveedor compra = carrito.compraDelProveedor(email, comando.vendedorId())
                .orElseThrow(() -> new CarritoSinProductosDelProveedorException(
                        "El carrito no tiene productos de ese proveedor"));
        String clienteId = clienteEnSesion.idPorCorreo(email);

        List<LineaPedido> descontadas = descontarStock(compra.lineas());
        Pedido guardado;
        try {
            Instant ahora = Instant.now(reloj);
            int anio = ZonedDateTime.ofInstant(ahora, reloj.getZone()).getYear();
            NumeroPedido numero = NumeroPedido.de(anio, contador.siguiente(anio));
            guardado = pedidoRepository.guardar(Pedido.nuevo(numero, clienteId, compra.vendedorId(),
                    compra.nombreComercial(), compra.lineas(), direccion, comando.paymentMethodId(), ahora));
        } catch (RuntimeException e) {
            reponer(descontadas);
            throw e;
        }
        carrito.quitar(email, compra.lineas().stream().map(LineaPedido::productoId).toList());
        return guardado;
    }

    // Descuenta línea a línea; si a una le falta stock, repone las anteriores y no se crea el pedido
    private List<LineaPedido> descontarStock(List<LineaPedido> lineas) {
        List<LineaPedido> descontadas = new ArrayList<>();
        for (LineaPedido linea : lineas) {
            if (!existencias.descontar(linea.productoId(), linea.cantidad())) {
                reponer(descontadas);
                throw new StockInsuficienteException("No hay stock suficiente de un producto del pedido");
            }
            descontadas.add(linea);
        }
        return descontadas;
    }

    private void reponer(List<LineaPedido> descontadas) {
        descontadas.forEach(linea -> existencias.reponer(linea.productoId(), linea.cantidad()));
    }
}
