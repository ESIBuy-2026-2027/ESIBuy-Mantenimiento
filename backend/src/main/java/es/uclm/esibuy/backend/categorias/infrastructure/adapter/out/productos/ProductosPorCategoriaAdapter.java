package es.uclm.esibuy.backend.categorias.infrastructure.adapter.out.productos;

import java.util.Map;

import es.uclm.esibuy.backend.categorias.application.port.out.ProductosPorCategoriaPort;
import es.uclm.esibuy.backend.productos.application.port.in.ConteoProductosUseCase;

/**
 * Adaptador de salida que pregunta al módulo de productos por su puerto de entrada, sin usar su
 * dominio (comunicación entre módulos del CLAUDE.md).
 */
public class ProductosPorCategoriaAdapter implements ProductosPorCategoriaPort {

    private final ConteoProductosUseCase conteoProductos;

    public ProductosPorCategoriaAdapter(ConteoProductosUseCase conteoProductos) {
        this.conteoProductos = conteoProductos;
    }

    @Override
    public Map<String, Long> contarPorCategoria() {
        return conteoProductos.contarPorCategoria();
    }
}
