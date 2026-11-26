package es.uclm.esibuy.backend.productos.infrastructure.adapter.out.persistence;

import java.math.BigDecimal;
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
import es.uclm.esibuy.backend.productos.application.port.in.ListarProductosQuery;
import es.uclm.esibuy.backend.productos.application.port.out.ProductoRepositoryPort;
import es.uclm.esibuy.backend.productos.domain.exception.ProductoDuplicadoException;
import es.uclm.esibuy.backend.productos.domain.model.Producto;
import es.uclm.esibuy.backend.productos.domain.model.ProductoPage;

/**
 * Adaptador de salida que implementa el repositorio de productos con Spring
 * Data MongoDB.
 */
public class ProductoMongoAdapter implements ProductoRepositoryPort {

    private static final String CAMPO_STOCK = "stock";
    private static final String CAMPO_DISPONIBLE = "disponible";
    private static final String CAMPO_NOMBRE = "nombre";
    private static final String CAMPO_VENDEDOR_ID = "vendedorId";
    private static final String CAMPO_CATEGORIA_ID = "categoriaId";
    private static final String CAMPO_VALORACION_MEDIA = "valoracionMedia";
    private static final String CAMPO_PRECIO = "precio";

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
            // Actualización: se conservan la fecha de creación y las valoraciones del
            // documento guardado (HU13: las
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
            // El índice único (vendedorId, nombre) lo ha rechazado: dos altas simultáneas
            // con el mismo nombre.
            throw new ProductoDuplicadoException(duplicateKeyException);
        }
    }

    @Override
    public Optional<Producto> buscarPorId(String id) {
        return repository.findById(id).map(mapper::aDominio);
    }

    // La condición (stock >= cantidad) y la resta van en la misma operación de
    // MongoDB: dos compras a la vez del
    // último stock no pueden pasar las dos
    @Override
    public boolean descontarStock(String id, int cantidad) {
        Query conStockSuficiente = new Query(Criteria.where("_id").is(id)
                .and(CAMPO_STOCK).gte(cantidad)
                .and(CAMPO_DISPONIBLE).is(true));
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
        Sort orden = ordenarPor(query);
        List<Producto> productos = mongoTemplate.find(new Query(criterios).with(PageRequest.of(
                query.pagina(), query.tamano(), orden)),
                ProductoDocument.class).stream()
                .map(mapper::aDominio)
                .toList();
        return new ProductoPage(productos, query.pagina(), query.tamano(), total);
    }

    @Override
    public List<Producto> listarTodosPorVendedor(String vendedorId) {
        return mongoTemplate.find(Query.query(Criteria.where(CAMPO_VENDEDOR_ID).is(vendedorId)), ProductoDocument.class)
                .stream().map(mapper::aDominio).toList();
    }

    // Catálogo del cliente (HU7): solo productos disponibles, de todos los
    // vendedores; la búsqueda por nombre
    // no distingue mayúsculas ni tildes y trata el texto como literal.
    @Override
    public ProductoPage buscarEnCatalogo(BuscarCatalogoQuery query) {
        Criteria criterios = construirCriteriosCatalogo(query, true);
        long total = mongoTemplate.count(new Query(criterios), ProductoDocument.class);
        // Por valoración, los empates van por nombre de la A a la Z en los dos
        // sentidos.
        Sort.Direction direccion = query.descendente() ? Sort.Direction.DESC : Sort.Direction.ASC;
        Sort orden = switch (query.criterio()) {
            case VALORACION -> Sort.by(direccion, CAMPO_VALORACION_MEDIA)
                    .and(Sort.by(Sort.Direction.ASC, CAMPO_NOMBRE));
            case PRECIO -> Sort.by(direccion, CAMPO_PRECIO)
                    .and(Sort.by(Sort.Direction.ASC, CAMPO_NOMBRE));
            case NOMBRE -> Sort.by(direccion, CAMPO_NOMBRE);
        };
        // Sin collation, MongoDB ordena por código Unicode.
        List<Producto> productos = mongoTemplate.find(new Query(criterios).with(PageRequest.of(
                query.pagina(), query.tamano(), orden)),
                ProductoDocument.class).stream()
                .map(mapper::aDominio)
                .toList();
        return new ProductoPage(productos, query.pagina(), query.tamano(), total);
    }

    @Override
    public BigDecimal precioMaximoEnCatalogo(BuscarCatalogoQuery query) {
        Criteria criterios = construirCriteriosCatalogo(query, false);
        ProductoDocument masCaro = mongoTemplate.findOne(
                new Query(criterios).with(Sort.by(Sort.Direction.DESC, CAMPO_PRECIO)).limit(1),
                ProductoDocument.class);
        return masCaro == null ? BigDecimal.ZERO : mapper.aDominio(masCaro).getPrecio();
    }

    private static Criteria construirCriteriosCatalogo(BuscarCatalogoQuery query, boolean incluirRangoPrecio) {
        List<Criteria> filtros = new ArrayList<>();
        filtros.add(Criteria.where(CAMPO_DISPONIBLE).is(true));
        if (query.soloStock()) {
            filtros.add(Criteria.where(CAMPO_STOCK).gt(0));
        }
        if (query.soloOfertas()) {
            Criteria descuentoGeneral = Criteria.where("descuento.activo").is(true);
            filtros.add(query.clientePremium()
                    ? new Criteria().orOperator(descuentoGeneral,
                            Criteria.where("descuentoPremium.activo").is(true))
                    : descuentoGeneral);
        }
        if (query.busqueda() != null && !query.busqueda().isBlank()) {
            filtros.add(Criteria.where(CAMPO_NOMBRE).regex(patronSinTildes(query.busqueda()), "i"));
        }
        if (query.categoriaId() != null && !query.categoriaId().isBlank()) {
            filtros.add(Criteria.where(CAMPO_CATEGORIA_ID).is(query.categoriaId()));
        }
        if (incluirRangoPrecio && query.precioMinimo() != null) {
            filtros.add(Criteria.where(CAMPO_PRECIO).gte(query.precioMinimo()));
        }
        if (incluirRangoPrecio && query.precioMaximo() != null) {
            filtros.add(Criteria.where(CAMPO_PRECIO).lte(query.precioMaximo()));
        }
        if (query.valoracionMinima() != null) {
            filtros.add(Criteria.where(CAMPO_VALORACION_MEDIA).gte(query.valoracionMinima()));
        }
        return new Criteria().andOperator(filtros);
    }

    // Mismo vendedor y mismo nombre completo, sin distinguir mayúsculas y como
    // texto literal (Pattern.quote).
    @Override
    public boolean existePorVendedorYNombre(String vendedorId, String nombre) {
        Criteria criterios = new Criteria().andOperator(
                Criteria.where(CAMPO_VENDEDOR_ID).is(vendedorId),
                Criteria.where(CAMPO_NOMBRE).regex("^" + Pattern.quote(nombre.trim()) + "$", "i"));
        return mongoTemplate.exists(new Query(criterios), ProductoDocument.class);
    }

    // Siempre del vendedor; el nombre se busca como texto literal (Pattern.quote),
    // sin distinguir mayúsculas.
    private static Criteria construirCriterios(String vendedorId, ListarProductosQuery query) {
        List<Criteria> criterios = new ArrayList<>();
        criterios.add(Criteria.where(CAMPO_VENDEDOR_ID).is(vendedorId));
        if (query.busqueda() != null && !query.busqueda().isBlank()) {
            criterios
                    .add(Criteria.where(CAMPO_NOMBRE).regex(".*" + Pattern.quote(query.busqueda().trim()) + ".*", "i"));
        }
        if (query.categoriaId() != null && !query.categoriaId().isBlank()) {
            criterios.add(Criteria.where(CAMPO_CATEGORIA_ID).is(query.categoriaId()));
        }
        if (query.disponible() != null) {
            criterios.add(Criteria.where(CAMPO_DISPONIBLE).is(query.disponible()));
        }
        if (query.precioMinimo() != null) {
            criterios.add(Criteria.where(CAMPO_PRECIO).gte(query.precioMinimo()));
        }
        if (query.precioMaximo() != null) {
            criterios.add(Criteria.where(CAMPO_PRECIO).lte(query.precioMaximo()));
        }
        return new Criteria().andOperator(criterios);
    }

    private static Sort ordenarPor(ListarProductosQuery query) {
        Sort.Direction direccion = query.direccionOrden() != null
                && query.direccionOrden().name().equals("DESC")
                        ? Sort.Direction.DESC
                        : Sort.Direction.ASC;
        if (query.criterioOrden() != null) {
            String campo = query.criterioOrden().name().equals("PRECIO")
                    ? CAMPO_PRECIO
                    : CAMPO_STOCK;
            return Sort.by(direccion, campo);
        }
        return Sort.by(Sort.Direction.DESC, "createdAt");
    }

    // Regex que encuentra el texto con o sin tildes en las vocales; el resto de
    // caracteres (la ñ incluida) se
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

    // Una sola agregación para todas las categorías: agrupa los productos por
    // categoriaId y los cuenta.
    @Override
    public Map<String, Long> contarPorCategoria() {
        Aggregation agrupar = Aggregation.newAggregation(
                Aggregation.group(CAMPO_CATEGORIA_ID).count().as("total"));
        return mongoTemplate.aggregate(agrupar, ProductoDocument.class, Document.class).getMappedResults().stream()
                .collect(Collectors.toMap(grupo -> grupo.getString("_id"),
                        grupo -> grupo.get("total", Number.class).longValue()));
    }
}
