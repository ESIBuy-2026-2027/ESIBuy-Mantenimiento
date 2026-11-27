package es.uclm.esibuy.backend.estadisticas.infrastructure.adapter.out.productos;

import java.util.List;

import es.uclm.esibuy.backend.estadisticas.application.port.out.ProductosPlataformaPort;
import es.uclm.esibuy.backend.estadisticas.domain.model.ProductoValorado;
import es.uclm.esibuy.backend.productos.application.port.in.ProductosPlataformaUseCase;

/** Adaptador de salida hacia productos para consultar las valoraciones de toda la plataforma. */
public class ProductosPlataformaAdapter implements ProductosPlataformaPort {

    private final ProductosPlataformaUseCase productosPlataforma;

    public ProductosPlataformaAdapter(ProductosPlataformaUseCase productosPlataforma) {
        this.productosPlataforma = productosPlataforma;
    }

    @Override
    public List<ProductoValorado> consultar() {
        return productosPlataforma.listar().stream()
                .map(producto -> new ProductoValorado(producto.productoId(), producto.vendedorId(), producto.nombre(),
                        producto.valoracionMedia(), producto.numeroValoraciones()))
                .toList();
    }
}
