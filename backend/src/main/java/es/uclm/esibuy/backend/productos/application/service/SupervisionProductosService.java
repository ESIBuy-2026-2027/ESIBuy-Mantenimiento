package es.uclm.esibuy.backend.productos.application.service;

import es.uclm.esibuy.backend.productos.application.port.in.ListarProductosQuery;
import es.uclm.esibuy.backend.productos.application.port.in.SupervisionProductosUseCase;
import es.uclm.esibuy.backend.productos.application.port.out.ProductoRepositoryPort;
import es.uclm.esibuy.backend.productos.application.port.out.VendedorPort;
import es.uclm.esibuy.backend.productos.domain.model.ProductoPage;

/**
 * Productos de un vendedor vistos por el administrador (HU26). Antes de listar comprueba que el id es de un
 * vendedor, para no responder con una página vacía a un id que no existe.
 */
public class SupervisionProductosService implements SupervisionProductosUseCase {

    private final ProductoRepositoryPort productoRepository;
    private final VendedorPort vendedores;

    public SupervisionProductosService(ProductoRepositoryPort productoRepository, VendedorPort vendedores) {
        this.productoRepository = productoRepository;
        this.vendedores = vendedores;
    }

    @Override
    public ProductoPage listarProductosDeVendedor(String vendedorId, int pagina, int tamano) {
        vendedores.comprobarQueEsVendedor(vendedorId);
        return productoRepository.listarPorVendedor(vendedorId, new ListarProductosQuery(null, null, pagina, tamano));
    }
}
