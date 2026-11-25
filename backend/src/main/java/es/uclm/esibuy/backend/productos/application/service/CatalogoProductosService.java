package es.uclm.esibuy.backend.productos.application.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.springframework.security.access.prepost.PreAuthorize;

import es.uclm.esibuy.backend.productos.application.port.in.BuscarCatalogoQuery;
import es.uclm.esibuy.backend.productos.application.port.in.CatalogoProductosUseCase;
import es.uclm.esibuy.backend.productos.application.port.in.CriterioOrdenCatalogo;
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
import es.uclm.esibuy.backend.shared.seguridad.Permisos;

/**
 * Catálogo de productos del cliente (HU7): delega la búsqueda en el repositorio.
 * También proporciona los destacados públicos de la portada, el detalle de un
 * producto (HU8) y los datos comerciales necesarios para comprarlo (HU9).
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

    @Override
    public List<Producto> buscarDestacados(int limite) {
        BuscarCatalogoQuery query = new BuscarCatalogoQuery(
                null, null, 1, false, false, false, null, null, CriterioOrdenCatalogo.VALORACION, true, 0, limite);
        return productoRepository.buscarEnCatalogo(query).productos().stream()
                .filter(producto -> producto.getValoracionMedia() != null)
                .toList();
    }

    @PreAuthorize(Permisos.SOLO_CLIENTE_PROPIO)
    @Override
    public PaginaCatalogo buscarProductos(BuscarCatalogoQuery query, String emailCliente) {
        boolean premium = usuarioEnSesion.esClientePremium(emailCliente);
        BuscarCatalogoQuery consulta = new BuscarCatalogoQuery(
                query.busqueda(), query.categoriaId(), query.valoracionMinima(), query.soloStock(),
                query.soloOfertas(), premium, query.precioMinimo(), query.precioMaximo(), query.criterio(), query.descendente(), query.pagina(), query.tamano());
        var pagina = productoRepository.buscarEnCatalogo(consulta);
        var precioMaximo = productoRepository.precioMaximoEnCatalogo(consulta);
        return new PaginaCatalogo(pagina, premium, precioMaximo);
    }

    @PreAuthorize(Permisos.SOLO_CLIENTE)
    @Override
    public Optional<ProductoComercial> consultarComprable(String productoId, boolean clientePremium) {
        return productoRepository.buscarPorId(productoId)
                .filter(Producto::isDisponible)
                .map(producto -> new ProductoComercial(producto.getId(), producto.getVendedorId(),
                        producto.getNombre(), producto.getImagen(), producto.precioFinal(clientePremium),
                        producto.getStock(), producto.getPrecio(), porcentajeAplicado(producto, clientePremium)));
    }

    private static BigDecimal porcentajeAplicado(Producto producto, boolean clientePremium) {
        Descuento descuento = producto.descuentoAplicado(clientePremium);
        return descuento.activo() ? descuento.porcentaje() : null;
    }

    @PreAuthorize(Permisos.SOLO_CLIENTE)
    @Override
    public Optional<ResumenProducto> consultarResumen(String productoId, boolean clientePremium) {
        return productoRepository.buscarPorId(productoId)
                .map(producto -> new ResumenProducto(producto.getId(), producto.getNombre(),
                        producto.getImagen(), producto.precioFinal(clientePremium), producto.getStock(),
                        producto.isDisponible()));
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
                descuento.porcentaje(), producto.precioFinal(premium), producto.getStock(),
                producto.isDisponible(), producto.getValoracionMedia(), producto.getNumeroValoraciones());
    }
}
