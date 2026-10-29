package es.uclm.esibuy.backend.pedidos.infrastructure.adapter.out.carrito;

import java.util.List;
import java.util.Optional;

import es.uclm.esibuy.backend.carrito.application.port.in.GestionCarritoUseCase;
import es.uclm.esibuy.backend.pedidos.application.port.out.CarritoPort;
import es.uclm.esibuy.backend.pedidos.application.port.out.CompraProveedor;
import es.uclm.esibuy.backend.pedidos.domain.model.LineaPedido;

/**
 * Adaptador de salida que pregunta al carrito (HU9) por su puerto de entrada. El carrito ya viene limpio y con los
 * precios del cliente calculados por el servidor: el pedido nunca se fía de precios enviados por el navegador.
 */
public class CarritoAdapter implements CarritoPort {

    private final GestionCarritoUseCase gestionCarrito;

    public CarritoAdapter(GestionCarritoUseCase gestionCarrito) {
        this.gestionCarrito = gestionCarrito;
    }

    @Override
    public Optional<CompraProveedor> compraDelProveedor(String emailCliente, String vendedorId) {
        return gestionCarrito.consultar(emailCliente).grupos().stream()
                .filter(grupo -> grupo.vendedorId().equals(vendedorId))
                .findFirst()
                .map(grupo -> new CompraProveedor(grupo.vendedorId(), grupo.nombreComercial(), grupo.lineas().stream()
                        .map(linea -> new LineaPedido(linea.productoId(), linea.nombre(), linea.cantidad(),
                                linea.precioUnitario()))
                        .toList()));
    }

    @Override
    public void quitar(String emailCliente, List<String> productoIds) {
        productoIds.forEach(productoId -> gestionCarrito.quitar(emailCliente, productoId));
    }
}
