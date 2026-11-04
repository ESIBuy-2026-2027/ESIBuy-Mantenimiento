package es.uclm.esibuy.backend.productos.infrastructure.adapter.out.persistence;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.bson.Document;

import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

import es.uclm.esibuy.backend.productos.application.port.in.BuscarCatalogoQuery;
import es.uclm.esibuy.backend.productos.application.port.in.CriterioOrdenCatalogo;
import es.uclm.esibuy.backend.productos.application.port.in.ListarProductosQuery;
import es.uclm.esibuy.backend.productos.application.port.out.ProductoRepositoryPort;
import es.uclm.esibuy.backend.productos.domain.exception.ProductoDuplicadoException;
import es.uclm.esibuy.backend.productos.domain.model.Producto;
import es.uclm.esibuy.backend.productos.domain.model.ProductoPage;

/** Adaptador de salida que implementa el repositorio de productos con Spring Data MongoDB. */
public class ProductoMongoAdapter implements ProductoRepositoryPort {

    private static final String CAMPO_STOCK = "stock";
    private static final String CAMPO_NOMBRE = "nombre";
    private static final String CAMPO_VALORACION_MEDIA = "valoracionMedia";

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
            // Actualización: se conservan la fecha de creación y las valoraciones del documento guardado (HU13: las
            // cambia solo actualizarValoracion, nunca el vendedor al editar el producto)
            repository.findById(producto.getId()).ifPresent(guardado -> {
                documento.setCreatedAt(guardado.getCreatedAt());
                documento.setValoracionMedia(guardado.getValoracionMedia());
                documento.setNumeroValoraciones(guardado.getNumeroValoraciones());
            });
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

    // La condición (stock >= cantidad) y la resta van en la misma operación de MongoDB: dos compras a la vez del
    // último stock no pueden pasar las dos
    @Override
    public boolean descontarStock(String id, int cantidad) {
        Query conStockSuficiente = new Query(Criteria.where("_id").is(id).and(CAMPO_STOCK).gte(cantidad));
        return mongoTemplate.updateFirst(conStockSuficiente, new Update().inc(CAMPO_STOCK, -cantidad),
                ProductoDocument.class).getModifiedCount() == 1;
    }

    // Solo estos dos campos: el resto del producto no se toca
    @Override
    public void actualizarValoracion(String id, double media, int numero) {
        mongoTemplate.updateFirst(new Query(Criteria.where("_id").is(id)),
                new Update().set(CAMPO_VALORACION_MEDIA, media).set("numeroValoraciones", numero),
                ProductoDocument.class);
    }

    @Override
    public void reponerStock(String id, int cantidad) {
        mongoTemplate.updateFirst(new Query(Criteria.where("_id").is(id)), new Update().inc(CAMPO_STOCK, cantidad),
                ProductoDocument.class);
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

    // Catálogo del cliente (HU7): solo productos disponibles, de todos los vendedores; la búsqueda por nombre
    // no distingue mayúsculas ni tildes y trata el texto como literal.
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
        // Media de las valoraciones (HU13): un producto sin valoraciones no tiene el campo y no llega a ningún mínimo
        if (query.valoracionMinima() != null) {
            filtros.add(Criteria.where(CAMPO_VALORACION_MEDIA).gte(query.valoracionMinima()));
        }
        Criteria criterios = new Criteria().andOperator(filtros);
        long total = mongoTemplate.count(new Query(criterios), ProductoDocument.class);
        // Por valoración, los empates van por nombre de la A a la Z en los dos sentidos. Sin valoraciones, MongoDB los
        // trata como el valor más bajo: al principio en ascendente y al final en descendente.
        Sort.Direction direccion = query.descendente() ? Sort.Direction.DESC : Sort.Direction.ASC;
        Sort orden = query.criterio() == CriterioOrdenCatalogo.VALORACION
                ? Sort.by(direccion, CAMPO_VALORACION_MEDIA).and(Sort.by(Sort.Direction.ASC, CAMPO_NOMBRE))
                : Sort.by(direccion, CAMPO_NOMBRE);
        // PENDIENTE DE DECIDIR (equipo): ordenar como en español con ORDEN_ALFABETICO_ES (UsuarioMongoAdapter).
        // Sin collation, MongoDB ordena por código Unicode: las mayúsculas antes y las vocales con tilde tras la Z.
        List<Producto> productos = mongoTemplate.find(new Query(criterios).with(PageRequest.of(
                        query.pagina(), query.tamano(), orden)),
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

    // Regex que encuentra el texto con o sin tildes en las vocales; el resto de caracteres (la ñ incluida) se
    // busca literal con Pattern.quote.
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

    // Una sola agregación para todas las categorías: agrupa los productos por categoriaId y los cuenta.
    @Override
    public Map<String, Long> contarPorCategoria() {
        Aggregation agrupar = Aggregation.newAggregation(
                Aggregation.group("categoriaId").count().as("total"));
        return mongoTemplate.aggregate(agrupar, ProductoDocument.class, Document.class).getMappedResults().stream()
                .collect(Collectors.toMap(grupo -> grupo.getString("_id"),
                        grupo -> grupo.get("total", Number.class).longValue()));
    }
}
