package es.uclm.esibuy.backend.productos.application.service;

import java.util.Map;

import es.uclm.esibuy.backend.productos.application.port.in.ConteoProductosUseCase;
import es.uclm.esibuy.backend.productos.application.port.out.ProductoRepositoryPort;
import org.springframework.security.access.prepost.PreAuthorize;
import es.uclm.esibuy.backend.shared.seguridad.Permisos;

/** Implementa el recuento de productos por categoría. Solo habla con el exterior a través del repositorio. */
public class ConteoProductosService implements ConteoProductosUseCase {

    private final ProductoRepositoryPort productoRepository;

    public ConteoProductosService(ProductoRepositoryPort productoRepository) {
        this.productoRepository = productoRepository;
    }

    @PreAuthorize(Permisos.SOLO_ADMINISTRADOR)
    @Override
    public Map<String, Long> contarPorCategoria() {
        return productoRepository.contarPorCategoria();
    }
}
