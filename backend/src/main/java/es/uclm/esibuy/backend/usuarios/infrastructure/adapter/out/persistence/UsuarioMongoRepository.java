package es.uclm.esibuy.backend.usuarios.infrastructure.adapter.out.persistence;

import org.springframework.data.mongodb.repository.MongoRepository;

/**
 * Repositorio técnico de Spring Data sobre la colección {@code usuarios}.
 *
 * <p>Solo se declara la interfaz: Spring Data genera la implementación al arrancar.
 * El resto de la aplicación no lo usa directamente, sino a través de {@code UsuarioMongoAdapter}.
 */
public interface UsuarioMongoRepository extends MongoRepository<UsuarioDocument, String> {

    // Spring Data deduce la consulta del nombre: ¿hay algún documento con ese email?
    boolean existsByEmail(String email);
}
