package es.uclm.esibuy.backend.usuarios.infrastructure.adapter.out.persistence;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.time.Instant;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import es.uclm.esibuy.backend.usuarios.application.port.out.UsuarioRepositoryPort;
import es.uclm.esibuy.backend.usuarios.application.port.in.ListarUsuariosQuery;
import es.uclm.esibuy.backend.usuarios.domain.exception.CorreoYaRegistradoException;
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

    private final UsuarioMongoRepository repository;
    private final UsuarioDocumentMapper mapper;
    private final MongoTemplate mongoTemplate;

    public UsuarioMongoAdapter(UsuarioMongoRepository repository, UsuarioDocumentMapper mapper,
            MongoTemplate mongoTemplate) {
        this.repository = repository;
        this.mapper = mapper;
        this.mongoTemplate = mongoTemplate;
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
            // El índice único de email lo ha rechazado (p. ej. dos registros simultáneos que pasaron
            // a la vez la comprobación de existePorCorreo). Se traduce a la excepción del dominio,
            // que la API devuelve como 409, para que el núcleo no dependa de MongoDB.
            throw new CorreoYaRegistradoException("El correo ya está registrado");
        }
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
    public UsuarioPage listar(ListarUsuariosQuery query) {
        Query mongoQuery = construirConsulta(query);
        long total = mongoTemplate.count(mongoQuery, UsuarioDocument.class);
        Pageable pageable = PageRequest.of(
                query.pagina(),
                query.tamano(),
                Sort.by(Sort.Direction.DESC, "createdAt")
        );
        mongoQuery.with(pageable);

        List<Usuario> usuarios = mongoTemplate.find(mongoQuery, UsuarioDocument.class)
                .stream()
                .map(mapper::aDominio)
                .toList();
        return new UsuarioPage(usuarios, query.pagina(), query.tamano(), total);
    }

    private Query construirConsulta(ListarUsuariosQuery query) {
        List<Criteria> criterios = new ArrayList<>();
        if (query.busqueda() != null && !query.busqueda().isBlank()) {
            String textoEscapado = Pattern.quote(query.busqueda().trim());
            String patron = ".*" + textoEscapado + ".*";
            criterios.add(new Criteria().orOperator(
                    Criteria.where("email").regex(patron, "i"),
                    Criteria.where("nombre").regex(patron, "i"),
                    Criteria.where("apellidos").regex(patron, "i")
            ));
        }
        if (query.rol() != null) {
            criterios.add(Criteria.where("rol").is(query.rol().name()));
        }
        if ("BLOQUEADO".equals(query.estado())) {
            criterios.add(new Criteria().andOperator(
                    Criteria.where("estado").is("ACTIVO"),
                    Criteria.where("lockedUntil").gt(Instant.now())
            ));
        } else if (query.estado() != null) {
            criterios.add(Criteria.where("estado").is(query.estado()));
        }
        Query mongoQuery = new Query();
        if (!criterios.isEmpty()) {
            mongoQuery.addCriteria(new Criteria().andOperator(criterios));
        }
        return mongoQuery;
    }
}
