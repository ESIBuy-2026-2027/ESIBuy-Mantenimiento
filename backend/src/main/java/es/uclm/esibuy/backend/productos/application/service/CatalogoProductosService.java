package es.uclm.esibuy.backend.productos.application.service;

import es.uclm.esibuy.backend.productos.application.port.in.BuscarCatalogoQuery;
import es.uclm.esibuy.backend.productos.application.port.in.CatalogoProductosUseCase;
import es.uclm.esibuy.backend.productos.application.port.in.DetalleProductoCatalogo;
import es.uclm.esibuy.backend.productos.application.port.in.ProductoComercial;
import es.uclm.esibuy.backend.productos.application.port.out.ProductoRepositoryPort;
import es.uclm.esibuy.backend.productos.application.port.out.UsuarioEnSesionPort;
import es.uclm.esibuy.backend.productos.application.port.out.VendedorPort;
import es.uclm.esibuy.backend.productos.domain.model.Descuento;
import es.uclm.esibuy.backend.productos.domain.model.Producto;
import es.uclm.esibuy.backend.productos.domain.model.ProductoPage;
import java.util.Optional;

/**
 * Catálogo de productos del cliente (HU7): delega la búsqueda en el repositorio. También da a otros módulos lo
 * comercial de un producto que se puede comprar (HU9).
 */
public class CatalogoProductosService implements CatalogoProductosUseCase {

    private final ProductoRepositoryPort productoRepository;
    private final VendedorPort vendedorPort;
    private final UsuarioEnSesionPort usuarioEnSesion;

    public CatalogoProductosService(ProductoRepositoryPort productoRepository, VendedorPort vendedorPort, UsuarioEnSesionPort usuarioEnSesion) {
        this.productoRepository = productoRepository;
        this.vendedorPort = vendedorPort;
        this.usuarioEnSesion = usuarioEnSesion;
    }

    @Override
    public ProductoPage buscarProductos(BuscarCatalogoQuery query) {
        return productoRepository.buscarEnCatalogo(query);
    }

    @Override
    public Optional<ProductoComercial> consultarComprable(String productoId, boolean clientePremium) {
        return productoRepository.buscarPorId(productoId)
                .filter(producto -> producto.isDisponible())
                .map(producto -> new ProductoComercial(producto.getId(), producto.getVendedorId(),
                        producto.getNombre(), producto.getImagen(), producto.precioFinal(clientePremium),
                        producto.getStock()));
    }

    @Override
    public DetalleProductoCatalogo consultarDetalle(String productoId, String emailCliente) {
        boolean premium = usuarioEnSesion.esClientePremium(emailCliente);
        Producto producto = productoRepository.buscarPorId(productoId).orElseThrow();
        Descuento descuento = producto.descuentoAplicado(premium);
        return new DetalleProductoCatalogo(producto.getId(), producto.getNombre(), producto.getDescripcion(),
                producto.getImagen(), producto.getCategoriaId(), producto.getVendedorId(),
                vendedorPort.nombreComercial(producto.getVendedorId()), producto.getPrecio(),
                descuento.porcentaje(), producto.precioFinal(premium), producto.getStock(), producto.isDisponible());
    }

    
}
