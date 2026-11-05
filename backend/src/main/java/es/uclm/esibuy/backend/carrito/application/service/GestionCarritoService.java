package es.uclm.esibuy.backend.carrito.application.service;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.security.access.prepost.PreAuthorize;

import es.uclm.esibuy.backend.carrito.application.port.in.CarritoDetallado;
import es.uclm.esibuy.backend.carrito.application.port.in.GestionCarritoUseCase;
import es.uclm.esibuy.backend.carrito.application.port.in.GrupoCarrito;
import es.uclm.esibuy.backend.carrito.application.port.in.LineaCarritoDetallada;
import es.uclm.esibuy.backend.carrito.application.port.out.CarritoRepositoryPort;
import es.uclm.esibuy.backend.carrito.application.port.out.ClienteEnSesionPort;
import es.uclm.esibuy.backend.carrito.application.port.out.ProductoCarrito;
import es.uclm.esibuy.backend.carrito.application.port.out.ProductosPort;
import es.uclm.esibuy.backend.carrito.application.port.out.VendedoresPort;
import es.uclm.esibuy.backend.carrito.domain.exception.ProductoNoComprableException;
import es.uclm.esibuy.backend.carrito.domain.model.Carrito;
import es.uclm.esibuy.backend.carrito.domain.model.LineaCarrito;
import es.uclm.esibuy.backend.shared.seguridad.Permisos;

/**
 * Implementa la gestión del carrito del cliente (HU9). Solo habla con el exterior a través de los puertos de
 * salida. El precio y el stock se consultan siempre al módulo de productos: el carrito solo guarda qué
 * productos y cuántas unidades. El reloj se inyecta para fijar en los tests las fechas de las líneas.
 */
public class GestionCarritoService implements GestionCarritoUseCase {

    private final CarritoRepositoryPort carritoRepository;
    private final ClienteEnSesionPort clienteEnSesion;
    private final ProductosPort productos;
    private final VendedoresPort vendedores;
    private final Clock reloj;

    public GestionCarritoService(CarritoRepositoryPort carritoRepository, ClienteEnSesionPort clienteEnSesion,
            ProductosPort productos, VendedoresPort vendedores, Clock reloj) {
        this.carritoRepository = carritoRepository;
        this.clienteEnSesion = clienteEnSesion;
        this.productos = productos;
        this.vendedores = vendedores;
        this.reloj = reloj;
    }

    @PreAuthorize(Permisos.SOLO_CLIENTE_PROPIO)
    @Override
    public CarritoDetallado consultar(String emailCliente) {
        return detallar(carritoDe(emailCliente), clienteEnSesion.esPremium(emailCliente));
    }

    @PreAuthorize(Permisos.SOLO_CLIENTE_PROPIO)
    @Override
    public CarritoDetallado anadir(String emailCliente, String productoId, int cantidad) {
        boolean premium = clienteEnSesion.esPremium(emailCliente);
        ProductoCarrito producto = comprable(productoId, premium);
        Carrito carrito = carritoDe(emailCliente);
        carrito.anadir(productoId, cantidad, producto.stock(), Instant.now(reloj));
        return detallar(carritoRepository.guardar(carrito), premium);
    }

    @PreAuthorize(Permisos.SOLO_CLIENTE_PROPIO)
    @Override
    public CarritoDetallado fijarCantidad(String emailCliente, String productoId, int cantidad) {
        boolean premium = clienteEnSesion.esPremium(emailCliente);
        ProductoCarrito producto = comprable(productoId, premium);
        Carrito carrito = carritoDe(emailCliente);
        carrito.fijarCantidad(productoId, cantidad, producto.stock(), Instant.now(reloj));
        return detallar(carritoRepository.guardar(carrito), premium);
    }

    @PreAuthorize(Permisos.SOLO_CLIENTE_PROPIO)
    @Override
    public CarritoDetallado quitar(String emailCliente, String productoId) {
        Carrito carrito = carritoDe(emailCliente);
        carrito.quitar(productoId);
        return detallar(carritoRepository.guardar(carrito), clienteEnSesion.esPremium(emailCliente));
    }

    @PreAuthorize(Permisos.SOLO_CLIENTE_PROPIO)
    @Override
    public CarritoDetallado vaciar(String emailCliente) {
        Carrito carrito = carritoDe(emailCliente);
        carrito.vaciar();
        return detallar(carritoRepository.guardar(carrito), clienteEnSesion.esPremium(emailCliente));
    }

    // El carrito del cliente en sesión, o uno vacío sin guardar si todavía no tiene
    private Carrito carritoDe(String emailCliente) {
        String clienteId = clienteEnSesion.idPorCorreo(emailCliente);
        return carritoRepository.buscarPorCliente(clienteId).orElseGet(() -> Carrito.vacio(clienteId));
    }

    private ProductoCarrito comprable(String productoId, boolean premium) {
        return productos.consultarComprable(productoId, premium)
                .orElseThrow(() -> new ProductoNoComprableException("El producto no se puede comprar"));
    }

    // Limpieza (AB#390): antes de enseñarlo, quita lo que ya no se puede comprar y ajusta las cantidades al stock
    private CarritoDetallado detallar(Carrito carrito, boolean premium) {
        Map<String, ProductoCarrito> actuales = new HashMap<>();
        for (LineaCarrito linea : carrito.getLineas()) {
            productos.consultarComprable(linea.productoId(), premium)
                    .ifPresent(producto -> actuales.put(producto.id(), producto));
        }
        Map<String, Integer> stocks = new HashMap<>();
        actuales.values().forEach(producto -> stocks.put(producto.id(), producto.stock()));
        Carrito vigente = carrito;
        if (carrito.limpiar(stocks, Instant.now(reloj))) {
            vigente = carritoRepository.guardar(carrito);
        }
        return agrupar(vigente, actuales);
    }

    // Una cabecera por proveedor, en el orden en que se añadió su primer producto
    private CarritoDetallado agrupar(Carrito carrito, Map<String, ProductoCarrito> actuales) {
        Map<String, List<LineaCarritoDetallada>> porVendedor = new LinkedHashMap<>();
        int totalProductos = 0;
        for (LineaCarrito linea : carrito.getLineas()) {
            ProductoCarrito producto = actuales.get(linea.productoId());
            BigDecimal subtotal = producto.precioFinal().multiply(BigDecimal.valueOf(linea.cantidad()));
            porVendedor.computeIfAbsent(producto.vendedorId(), id -> new ArrayList<>())
                    .add(new LineaCarritoDetallada(producto.id(), producto.nombre(), producto.imagen(),
                            linea.cantidad(), producto.precioFinal(), subtotal, producto.stock()));
            totalProductos += linea.cantidad();
        }
        List<GrupoCarrito> grupos = porVendedor.entrySet().stream()
                .map(grupo -> new GrupoCarrito(grupo.getKey(), vendedores.nombreComercial(grupo.getKey()),
                        sumar(grupo.getValue()), grupo.getValue()))
                .toList();
        BigDecimal total = grupos.stream().map(GrupoCarrito::total).reduce(BigDecimal.ZERO, BigDecimal::add);
        return new CarritoDetallado(grupos, totalProductos, total);
    }

    private static BigDecimal sumar(List<LineaCarritoDetallada> lineas) {
        return lineas.stream().map(LineaCarritoDetallada::subtotal).reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
