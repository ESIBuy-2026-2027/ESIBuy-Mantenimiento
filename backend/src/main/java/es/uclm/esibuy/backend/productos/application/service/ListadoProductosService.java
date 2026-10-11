package es.uclm.esibuy.backend.productos.application.service;

import es.uclm.esibuy.backend.productos.application.port.in.ListadoProductosUseCase;
import es.uclm.esibuy.backend.productos.application.port.in.ListarProductosQuery;
import es.uclm.esibuy.backend.productos.application.port.out.ProductoRepositoryPort;
import es.uclm.esibuy.backend.productos.application.port.out.UsuarioEnSesionPort;
import es.uclm.esibuy.backend.productos.domain.model.ProductoPage;

/**
 * Listado de los productos propios del vendedor (HU32). El vendedor sale siempre de la sesión,
 * nunca de un parámetro: así nadie puede ver los productos de otro.
 */
public class ListadoProductosService implements ListadoProductosUseCase {

    private final ProductoRepositoryPort productoRepository;
    private final UsuarioEnSesionPort usuarioEnSesion;

    public ListadoProductosService(ProductoRepositoryPort productoRepository, UsuarioEnSesionPort usuarioEnSesion) {
        this.productoRepository = productoRepository;
        this.usuarioEnSesion = usuarioEnSesion;
    }

    @Override
    public ProductoPage listarMisProductos(String emailVendedor, ListarProductosQuery query) {
        return productoRepository.listarPorVendedor(usuarioEnSesion.idPorCorreo(emailVendedor), query);
    }
}
