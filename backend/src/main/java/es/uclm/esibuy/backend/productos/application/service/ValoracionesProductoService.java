package es.uclm.esibuy.backend.productos.application.service;

import org.springframework.security.access.prepost.PreAuthorize;

import es.uclm.esibuy.backend.productos.application.port.in.ValoracionesProductoUseCase;
import es.uclm.esibuy.backend.productos.application.port.out.ProductoRepositoryPort;
import es.uclm.esibuy.backend.shared.seguridad.Permisos;

/** Guarda en el producto la media y el número de valoraciones que calcula el módulo de valoraciones (HU13). */
public class ValoracionesProductoService implements ValoracionesProductoUseCase {

    private final ProductoRepositoryPort productoRepository;

    public ValoracionesProductoService(ProductoRepositoryPort productoRepository) {
        this.productoRepository = productoRepository;
    }

    // Se actualiza mientras el cliente valora un producto que ha comprado
    @PreAuthorize(Permisos.SOLO_CLIENTE)
    @Override
    public void actualizarValoracion(String productoId, double media, int numero) {
        productoRepository.actualizarValoracion(productoId, media, numero);
    }
}
