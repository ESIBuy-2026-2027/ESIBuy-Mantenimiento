package es.uclm.esibuy.backend.productos.infrastructure.adapter.out.persistence;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import es.uclm.esibuy.backend.productos.application.port.in.BuscarCatalogoQuery;
import es.uclm.esibuy.backend.productos.application.port.in.ListarProductosQuery;
import es.uclm.esibuy.backend.productos.application.port.out.ProductoRepositoryPort;
import es.uclm.esibuy.backend.productos.domain.exception.ProductoDuplicadoException;
import es.uclm.esibuy.backend.productos.domain.model.Producto;
import es.uclm.esibuy.backend.productos.domain.model.ProductoPage;

/** Adaptador de salida que implementa el repositorio de productos con Spring Data MongoDB. */
public class ProductoMongoAdapter implements ProductoRepositoryPort {

    private final ProductoMongoRepository repository;
    private final ProductoDocumentMapper mapper;
    private final MongoTemplate mongoTemplate;

    public ProductoMongoAdapter(ProductoMongoRepository repository, ProductoDocumentMapper mapper,
            MongoTemplate mongoTemplate) {
        this.repository = repository;
        this.mapper = mapper;
        this.mongoTemplate = mongoTemplate;
    }

    @Override
    public Producto guardar(Producto producto) {
        ProductoDocument documento = mapper.aDocumento(producto);
        if (producto.getId() != null) {
            // Actualización: se conserva la fecha de creación del documento guardado
            repository.findById(producto.getId())
                    .ifPresent(guardado -> documento.setCreatedAt(guardado.getCreatedAt()));
        }
        try {
            return mapper.aDominio(repository.save(documento));
        } catch (DuplicateKeyException duplicateKeyException) {
            // El índice único (vendedorId, nombre) lo ha rechazado: dos altas simultáneas con el mismo nombre.
            throw new ProductoDuplicadoException(duplicateKeyException);
        }
    }

    @Override
    public Optional<Producto> buscarPorId(String id) {
        return repository.findById(id).map(mapper::aDominio);
    }

    @Override
    public ProductoPage listarPorVendedor(String vendedorId, ListarProductosQuery query) {
        Criteria criterios = construirCriterios(vendedorId, query);
        long total = mongoTemplate.count(new Query(criterios), ProductoDocument.class);
        List<Producto> productos = mongoTemplate.find(new Query(criterios).with(PageRequest.of(
                        query.pagina(), query.tamano(), Sort.by(Sort.Direction.DESC, "createdAt"))),
                        ProductoDocument.class).stream()
                .map(mapper::aDominio)
                .toList();
        return new ProductoPage(productos, query.pagina(), query.tamano(), total);
    }
    

    @Override
    public ProductoPage buscarEnCatalogo(BuscarCatalogoQuery query) {
        Criteria criterios = Criteria.where("disponible").is(true);
        long total = mongoTemplate.count(new Query(criterios), ProductoDocument.class);
       List<Producto> productos = mongoTemplate.find(new Query(criterios).with(PageRequest.of(
                        query.pagina(), query.tamano())),
                        ProductoDocument.class).stream()
                .map(mapper::aDominio)
                .toList();

        return new ProductoPage(productos, query.pagina(), query.tamano(), total);
    }

    // Mismo vendedor y mismo nombre completo, sin distinguir mayúsculas y como texto literal (Pattern.quote).
    @Override
    public boolean existePorVendedorYNombre(String vendedorId, String nombre) {
        Criteria criterios = new Criteria().andOperator(
                Criteria.where("vendedorId").is(vendedorId),
                Criteria.where("nombre").regex("^" + Pattern.quote(nombre.trim()) + "$", "i"));
        return mongoTemplate.exists(new Query(criterios), ProductoDocument.class);
    }

    // Siempre del vendedor; el nombre se busca como texto literal (Pattern.quote), sin distinguir mayúsculas.
    private static Criteria construirCriterios(String vendedorId, ListarProductosQuery query) {
        List<Criteria> criterios = new ArrayList<>();
        criterios.add(Criteria.where("vendedorId").is(vendedorId));
        if (query.busqueda() != null && !query.busqueda().isBlank()) {
            criterios.add(Criteria.where("nombre").regex(".*" + Pattern.quote(query.busqueda().trim()) + ".*", "i"));
        }
        if (query.categoriaId() != null && !query.categoriaId().isBlank()) {
            criterios.add(Criteria.where("categoriaId").is(query.categoriaId()));
        }
        return new Criteria().andOperator(criterios);
    }

    @Override
    public void eliminar(String id) {
        repository.deleteById(id);
    }
}
