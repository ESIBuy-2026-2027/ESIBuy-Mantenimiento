package es.uclm.esibuy.backend.productos.infrastructure.adapter.out.persistence;

import java.util.Optional;

import es.uclm.esibuy.backend.productos.application.port.out.ProductoRepositoryPort;
import es.uclm.esibuy.backend.productos.domain.model.Producto;

/** Adaptador de salida que implementa el repositorio de productos con Spring Data MongoDB. */
public class ProductoMongoAdapter implements ProductoRepositoryPort {

    private final ProductoMongoRepository repository;
    private final ProductoDocumentMapper mapper;

    public ProductoMongoAdapter(ProductoMongoRepository repository, ProductoDocumentMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    public Producto guardar(Producto producto) {
        ProductoDocument documento = mapper.aDocumento(producto);
        if (producto.getId() != null) {
            // Actualización: se conserva la fecha de creación del documento guardado
            repository.findById(producto.getId())
                    .ifPresent(guardado -> documento.setCreatedAt(guardado.getCreatedAt()));
        }
        return mapper.aDominio(repository.save(documento));
    }

    @Override
    public Optional<Producto> buscarPorId(String id) {
        return repository.findById(id).map(mapper::aDominio);
    }
}
