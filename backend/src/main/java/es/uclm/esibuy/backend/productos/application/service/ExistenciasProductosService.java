package es.uclm.esibuy.backend.productos.application.service;

import es.uclm.esibuy.backend.productos.application.port.in.ExistenciasProductosUseCase;
import es.uclm.esibuy.backend.productos.application.port.out.ProductoRepositoryPort;
import es.uclm.esibuy.backend.productos.domain.exception.ProductoInvalidoException;
import org.springframework.security.access.prepost.PreAuthorize;
import es.uclm.esibuy.backend.shared.seguridad.Permisos;

/**
 * Descuenta y repone el stock de los productos que se compran (HU10). Que el stock no quede negativo lo garantiza
 * el repositorio con una operación atómica, aunque dos clientes compren a la vez.
 */
public class ExistenciasProductosService implements ExistenciasProductosUseCase {

    private final ProductoRepositoryPort productoRepository;

    public ExistenciasProductosService(ProductoRepositoryPort productoRepository) {
        this.productoRepository = productoRepository;
    }

    @PreAuthorize(Permisos.SOLO_CLIENTE)
    @Override
    public boolean descontar(String productoId, int cantidad) {
        exigirCantidad(cantidad);
        return productoRepository.descontarStock(productoId, cantidad);
    }

    @PreAuthorize(Permisos.SOLO_CLIENTE)
    @Override
    public void reponer(String productoId, int cantidad) {
        exigirCantidad(cantidad);
        productoRepository.reponerStock(productoId, cantidad);
    }

    private static void exigirCantidad(int cantidad) {
        if (cantidad < 1) {
            throw new ProductoInvalidoException("La cantidad debe ser al menos 1");
        }
    }
}
