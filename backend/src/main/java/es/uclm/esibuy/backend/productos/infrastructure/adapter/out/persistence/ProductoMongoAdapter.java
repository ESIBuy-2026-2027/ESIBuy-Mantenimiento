package es.uclm.esibuy.backend.productos.infrastructure.adapter.out.persistence;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
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
        List<Criteria> filtros = new ArrayList<>();
        filtros.add(Criteria.where("disponible").is(true));
        if (query.busqueda() != null && !query.busqueda().isBlank()) {
            filtros.add(Criteria.where("nombre").regex(patronSinTildes(query.busqueda()), "i"));
        }
        if (query.categoriaId() != null && !query.categoriaId().isBlank()) {
            filtros.add(Criteria.where("categoriaId").is(query.categoriaId()));
        }
        Criteria criterios = new Criteria().andOperator(filtros);
        long total = mongoTemplate.count(new Query(criterios), ProductoDocument.class);
        Sort.Direction direccion = query.descendente() ? Sort.Direction.DESC : Sort.Direction.ASC;
        List<Producto> productos = mongoTemplate.find(new Query(criterios).with(PageRequest.of(
                        query.pagina(), query.tamano(), Sort.by(direccion, "nombre"))),
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

    private static String patronSinTildes(String texto) {
        StringBuilder patron = new StringBuilder();
        for (char caracter : texto.trim().toLowerCase(Locale.ROOT).toCharArray()) {
            String base = Normalizer.normalize(String.valueOf(caracter), Normalizer.Form.NFD).replaceAll("\\p{M}", "");
            patron.append(switch (base) {
                case "a" -> "[aáàäâ]";
                case "e" -> "[eéèëê]";
                case "i" -> "[iíìïî]";
                case "o" -> "[oóòöô]";
                case "u" -> "[uúùüû]";
                default -> Pattern.quote(String.valueOf(caracter));
            });
        }
        return patron.toString();
    }

    @Override
    public void eliminar(String id) {
        repository.deleteById(id);
    }
}
