package es.uclm.esibuy.backend.productos.application.service;

import es.uclm.esibuy.backend.productos.application.port.in.BuscarCatalogoQuery;
import es.uclm.esibuy.backend.productos.application.port.in.CatalogoProductosUseCase;
import es.uclm.esibuy.backend.productos.application.port.in.DetalleProductoCatalogo;
import es.uclm.esibuy.backend.productos.application.port.in.PaginaCatalogo;
import es.uclm.esibuy.backend.productos.application.port.in.ProductoComercial;
import es.uclm.esibuy.backend.productos.application.port.in.ResumenProducto;
import es.uclm.esibuy.backend.productos.application.port.out.ProductoRepositoryPort;
import es.uclm.esibuy.backend.productos.application.port.out.UsuarioEnSesionPort;
import es.uclm.esibuy.backend.productos.application.port.out.VendedorPort;
import es.uclm.esibuy.backend.productos.domain.exception.ProductoNoEncontradoException;
import es.uclm.esibuy.backend.productos.domain.model.Descuento;
import es.uclm.esibuy.backend.productos.domain.model.Producto;
import es.uclm.esibuy.backend.productos.domain.model.ProductoPage;
import java.util.Optional;

import org.springframework.security.access.prepost.PreAuthorize;
import es.uclm.esibuy.backend.shared.seguridad.Permisos;

/**
 * Catálogo de productos del cliente (HU7): delega la búsqueda en el repositorio. Da el detalle de un producto con el
 * nombre comercial de su vendedor y el precio para el cliente (HU8), y a otros módulos lo comercial de un producto
 * que se puede comprar (HU9).
 */
public class CatalogoProductosService implements CatalogoProductosUseCase {

    private final ProductoRepositoryPort productoRepository;
    private final VendedorPort vendedorPort;
    private final UsuarioEnSesionPort usuarioEnSesion;

    public CatalogoProductosService(ProductoRepositoryPort productoRepository, VendedorPort vendedorPort,
            UsuarioEnSesionPort usuarioEnSesion) {
        this.productoRepository = productoRepository;
        this.vendedorPort = vendedorPort;
        this.usuarioEnSesion = usuarioEnSesion;
    }

    @PreAuthorize(Permisos.SOLO_CLIENTE_PROPIO)
    @Override
    public PaginaCatalogo buscarProductos(BuscarCatalogoQuery query, String emailCliente) {
        return new PaginaCatalogo(productoRepository.buscarEnCatalogo(query),
                usuarioEnSesion.esClientePremium(emailCliente));
    }

    @PreAuthorize(Permisos.SOLO_CLIENTE)
    @Override
    public Optional<ProductoComercial> consultarComprable(String productoId, boolean clientePremium) {
        return productoRepository.buscarPorId(productoId)
                .filter(producto -> producto.isDisponible())
                .map(producto -> new ProductoComercial(producto.getId(), producto.getVendedorId(),
                        producto.getNombre(), producto.getImagen(), producto.precioFinal(clientePremium),
                        producto.getStock()));
    }

    @PreAuthorize(Permisos.SOLO_CLIENTE)
    @Override
    public Optional<ResumenProducto> consultarResumen(String productoId, boolean clientePremium) {
        return productoRepository.buscarPorId(productoId)
                .map(producto -> new ResumenProducto(producto.getId(), producto.getNombre(), producto.getImagen(),
                        producto.precioFinal(clientePremium), producto.getStock(), producto.isDisponible()));
    }

    @PreAuthorize(Permisos.SOLO_CLIENTE_PROPIO)
    @Override
    public DetalleProductoCatalogo consultarDetalle(String productoId, String emailCliente) {
        boolean premium = usuarioEnSesion.esClientePremium(emailCliente);
        Producto producto = productoRepository.buscarPorId(productoId)
                .filter(Producto::isDisponible)
                .orElseThrow(ProductoNoEncontradoException::new);
        Descuento descuento = producto.descuentoAplicado(premium);
        return new DetalleProductoCatalogo(producto.getId(), producto.getNombre(), producto.getDescripcion(),
                producto.getImagen(), producto.getCategoriaId(), producto.getVendedorId(),
                vendedorPort.nombreComercial(producto.getVendedorId()), producto.getPrecio(),
                descuento.porcentaje(), producto.precioFinal(premium), producto.getStock(), producto.isDisponible(),
                producto.getValoracionMedia(), producto.getNumeroValoraciones());
    }
}
