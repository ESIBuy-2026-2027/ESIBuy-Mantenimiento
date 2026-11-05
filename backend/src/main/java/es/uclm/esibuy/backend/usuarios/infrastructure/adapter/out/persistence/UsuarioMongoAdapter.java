package es.uclm.esibuy.backend.usuarios.infrastructure.adapter.out.persistence;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationOptions;
import org.springframework.data.mongodb.core.aggregation.AggregationExpression;
import org.springframework.data.mongodb.core.aggregation.ComparisonOperators;
import org.springframework.data.mongodb.core.aggregation.ConditionalOperators;
import org.springframework.data.mongodb.core.aggregation.ConditionalOperators.Switch.CaseOperator;
import org.springframework.data.mongodb.core.query.Collation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import es.uclm.esibuy.backend.usuarios.application.port.out.UsuarioRepositoryPort;
import es.uclm.esibuy.backend.categorias.application.port.in.ConsultaCategoriasUseCase;
import es.uclm.esibuy.backend.usuarios.application.port.in.ListarUsuariosQuery;
import es.uclm.esibuy.backend.usuarios.domain.exception.CorreoYaRegistradoException;
import es.uclm.esibuy.backend.usuarios.domain.exception.DniYaRegistradoException;
import es.uclm.esibuy.backend.usuarios.domain.exception.NombreComercialYaRegistradoException;
import es.uclm.esibuy.backend.usuarios.domain.model.Rol;
import es.uclm.esibuy.backend.usuarios.domain.model.Usuario;
import es.uclm.esibuy.backend.usuarios.domain.model.UsuarioPage;
import org.springframework.dao.DuplicateKeyException;

import java.util.Optional;

/**
 * Adaptador de salida que guarda usuarios en MongoDB (patrón Repository).
 *
 * <p>Implementa el puerto que necesita el núcleo ({@link UsuarioRepositoryPort}) usando el
 * repositorio de Spring Data. Así, dominio y aplicación no dependen de MongoDB.
 */
public class UsuarioMongoAdapter implements UsuarioRepositoryPort {

    private static final String CAMPO_ESTADO = "estado";
    private static final String CAMPO_RANGO_ESTADO = "rangoEstado";
    private static final String CAMPO_NOMBRE_CATEGORIA = "nombreCategoria";
    private static final String CAMPO_NOMBRE = "nombre";
    private static final String CAMPO_APELLIDOS = "apellidos";
    private static final String CAMPO_CATEGORIA_PRINCIPAL_ID = "categoriaPrincipalId";
    private static final String ESTADO_ACTIVO = "ACTIVO";

    // Orden alfabético en español, sin distinguir mayúsculas ni tildes (fallos de la demo del Sprint 1): sin collation,
    // MongoDB ordena por bytes y «Beatriz» va antes que «ana», y «Álvaro» al final.
    private static final Collation ORDEN_ALFABETICO_ES = Collation.of("es").strength(Collation.ComparisonLevel.primary());

    // Nombres de los índices únicos de DNI por rol y de nombre comercial, fijados en ColeccionesEsiBuy.
    private static final String INDICE_DNI = "rol_dni";
    private static final String INDICE_NOMBRE_COMERCIAL = "nombreComercial";

    private final UsuarioMongoRepository repository;
    private final UsuarioDocumentMapper mapper;
    private final MongoTemplate mongoTemplate;
    // Catálogo de categorías, por su puerto de entrada: el orden por categoría es por su nombre, y aquí solo hay ids.
    private final ConsultaCategoriasUseCase consultaCategorias;

    public UsuarioMongoAdapter(UsuarioMongoRepository repository, UsuarioDocumentMapper mapper,
            MongoTemplate mongoTemplate, ConsultaCategoriasUseCase consultaCategorias) {
        this.repository = repository;
        this.mapper = mapper;
        this.mongoTemplate = mongoTemplate;
        this.consultaCategorias = consultaCategorias;
    }

    @Override
    public void guardar(Usuario usuario) {
        UsuarioDocument documento = mapper.aDocumento(usuario);
        if (usuario.getId() != null) {
            // Actualización: el documento se rehace desde el dominio, que no conoce la fecha de creación.
            repository.findById(usuario.getId())
                    .ifPresent(guardado -> documento.setCreatedAt(guardado.getCreatedAt()));
        }
        try {
            repository.save(documento);
        } catch (DuplicateKeyException e) {
            // Un índice único lo ha rechazado (p. ej. dos registros simultáneos que pasaron a la vez la
            // comprobación de existePorCorreo o existePorDniYRol). MongoDB indica en el mensaje qué índice es;
            // se traduce a la excepción del dominio que corresponde, que la API devuelve como 409.
            String mensaje = e.getMessage() == null ? "" : e.getMessage();
            if (mensaje.contains(INDICE_DNI)) {
                throw new DniYaRegistradoException("El DNI ya está registrado");
            }
            if (mensaje.contains(INDICE_NOMBRE_COMERCIAL)) {
                throw new NombreComercialYaRegistradoException("El nombre comercial ya está registrado");
            }
            throw new CorreoYaRegistradoException("El correo ya está registrado");
        }
    }

    @Override
    public boolean existePorDniYRol(String dni, Rol rol) {
        return repository.existsByDniAndRol(dni, rol.name());
    }

    @Override
    public boolean existePorNombreComercial(String nombreComercial) {
        return repository.existsByNombreComercial(nombreComercial);
    }

    @Override
    public long contarAdministradoresActivos() {
        return repository.countByRolAndEstado("ADMINISTRADOR", ESTADO_ACTIVO);
    }

    // Solo cuentan los vendedores (HU42): la categoría principal es un dato de su ficha.
    @Override
    public boolean existeVendedorConCategoriaPrincipal(String categoriaId) {
        return repository.existsByRolAndCategoriaPrincipalId(Rol.VENDEDOR.name(), categoriaId);
    }

    @Override
    public boolean existePorCorreo(String correo) {
        return repository.existsByEmail(correo);
    }

    @Override
    public Optional<Usuario> buscarPorCorreo(String email) {
        return repository.findByEmail(email).map(mapper::aDominio);
    }

    @Override
    public Optional<Usuario> buscarPorId(String id) {
        return repository.findById(id).map(mapper::aDominio);
    }

    @Override
    public UsuarioPage listar(ListarUsuariosQuery query) {
        Criteria criterios = construirCriterios(query);
        long total = mongoTemplate.count(new Query(criterios), UsuarioDocument.class);
        List<UsuarioDocument> documentos = switch (ordenDe(query)) {
            case ESTADO -> buscarOrdenadosPorEstado(criterios, query);
            case CATEGORIA -> buscarOrdenadosPorCategoria(criterios, query);
            case NOMBRE, FECHA_ALTA -> mongoTemplate.find(new Query(criterios).collation(ORDEN_ALFABETICO_ES)
                        .with(PageRequest.of(query.pagina(), query.tamano(), ordenSimple(query))), UsuarioDocument.class);
        };

        List<Usuario> usuarios = documentos.stream()
                .map(mapper::aDominio)
                .toList();
        return new UsuarioPage(usuarios, query.pagina(), query.tamano(), total);
    }

    // Los órdenes posibles, con el de por defecto (fecha de alta) como caso propio para que el switch sea exhaustivo.
    private enum OrdenEfectivo { NOMBRE, ESTADO, CATEGORIA, FECHA_ALTA }

    private static OrdenEfectivo ordenDe(ListarUsuariosQuery query) {
        if (query.ordenarPor() == null) {
            return OrdenEfectivo.FECHA_ALTA;
        }
        return OrdenEfectivo.valueOf(query.ordenarPor().name());
    }

    // Por categoría (fallos de la demo del Sprint 1): el documento guarda el id, y el contrato pide ordenar por el
    // nombre. Se traduce con un $switch sobre el catálogo, como el rango del estado. Sin categoría, al final.
    private List<UsuarioDocument> buscarOrdenadosPorCategoria(Criteria criterios, ListarUsuariosQuery query) {
        List<CaseOperator> casos = consultaCategorias.listar().stream()
                .map(categoria -> CaseOperator.when(ComparisonOperators.valueOf(CAMPO_CATEGORIA_PRINCIPAL_ID)
                        .equalToValue(categoria.id())).then(categoria.nombre()))
                .toList();
        AggregationExpression nombreCategoria = casos.isEmpty()
                ? ConditionalOperators.ifNull(CAMPO_CATEGORIA_PRINCIPAL_ID).then("")
                : ConditionalOperators.switchCases(casos).defaultTo("\uFFFF");
        Aggregation agregacion = Aggregation.newAggregation(
                Aggregation.match(criterios),
                Aggregation.addFields().addField(CAMPO_NOMBRE_CATEGORIA).withValue(nombreCategoria).build(),
                Aggregation.sort(Sort.by(direccion(query), CAMPO_NOMBRE_CATEGORIA)
                        .and(Sort.by(Sort.Direction.ASC, CAMPO_NOMBRE, CAMPO_APELLIDOS))),
                Aggregation.skip((long) query.pagina() * query.tamano()),
                Aggregation.limit(query.tamano()))
                .withOptions(AggregationOptions.builder().collation(ORDEN_ALFABETICO_ES).build());
        return mongoTemplate.aggregate(agregacion, UsuarioDocument.class, UsuarioDocument.class).getMappedResults();
    }

    // Sin orden propio, por fecha de alta (HU19); por nombre, nombre y apellidos (HU26).
    private static Sort ordenSimple(ListarUsuariosQuery query) {
        if (query.ordenarPor() == ListarUsuariosQuery.Orden.NOMBRE) {
            return Sort.by(direccion(query), CAMPO_NOMBRE, CAMPO_APELLIDOS);
        }
        return Sort.by(Sort.Direction.DESC, "createdAt");
    }

    // Por estado (HU26): el orden ascendente es pendientes, activos y bloqueados, no el alfabético,
    // así que se calcula un rango con una agregación. A igualdad de estado, por nombre.
    private List<UsuarioDocument> buscarOrdenadosPorEstado(Criteria criterios, ListarUsuariosQuery query) {
        AggregationExpression rango = ConditionalOperators.switchCases(
                CaseOperator.when(ComparisonOperators.valueOf(CAMPO_ESTADO).equalToValue("PENDIENTE")).then(0),
                CaseOperator.when(ComparisonOperators.valueOf(CAMPO_ESTADO).equalToValue(ESTADO_ACTIVO)).then(1),
                CaseOperator.when(ComparisonOperators.valueOf(CAMPO_ESTADO).equalToValue("BLOQUEADO")).then(2))
                .defaultTo(3);
        Aggregation agregacion = Aggregation.newAggregation(
                Aggregation.match(criterios),
                Aggregation.addFields().addField(CAMPO_RANGO_ESTADO).withValue(rango).build(),
                Aggregation.sort(Sort.by(direccion(query), CAMPO_RANGO_ESTADO)
                        .and(Sort.by(Sort.Direction.ASC, CAMPO_NOMBRE, CAMPO_APELLIDOS))),
                Aggregation.skip((long) query.pagina() * query.tamano()),
                Aggregation.limit(query.tamano()))
                .withOptions(AggregationOptions.builder().collation(ORDEN_ALFABETICO_ES).build());
        return mongoTemplate.aggregate(agregacion, UsuarioDocument.class, UsuarioDocument.class).getMappedResults();
    }

    private static Sort.Direction direccion(ListarUsuariosQuery query) {
        return query.descendente() ? Sort.Direction.DESC : Sort.Direction.ASC;
    }

    private Criteria construirCriterios(ListarUsuariosQuery query) {
        List<Criteria> criterios = new ArrayList<>();
        if (query.busqueda() != null && !query.busqueda().isBlank()) {
            String textoEscapado = Pattern.quote(query.busqueda().trim());
            String patron = ".*" + textoEscapado + ".*";
            criterios.add(new Criteria().orOperator(
                    Criteria.where("email").regex(patron, "i"),
                    Criteria.where(CAMPO_NOMBRE).regex(patron, "i"),
                    Criteria.where(CAMPO_APELLIDOS).regex(patron, "i")
            ));
        }
        if (query.rol() != null) {
            criterios.add(Criteria.where("rol").is(query.rol().name()));
        }
        if (query.categoriaId() != null && !query.categoriaId().isBlank()) {
            criterios.add(Criteria.where(CAMPO_CATEGORIA_PRINCIPAL_ID).is(query.categoriaId()));
        }
        if (query.estado() != null) {
            criterios.add(Criteria.where(CAMPO_ESTADO).is(query.estado()));
        }
        return criterios.isEmpty() ? new Criteria() : new Criteria().andOperator(criterios);
    }

    @Override
    public void eliminarPorId(String id) {
        repository.deleteById(id);
    }
}
