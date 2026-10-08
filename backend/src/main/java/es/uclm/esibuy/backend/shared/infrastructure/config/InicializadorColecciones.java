package es.uclm.esibuy.backend.shared.infrastructure.config;

import java.util.Set;

import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.dao.DataAccessException;
import org.springframework.data.mongodb.core.CollectionOptions;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.index.IndexResolver;
import org.springframework.data.mongodb.core.mapping.MongoMappingContext;
import org.springframework.data.mongodb.core.mapping.MongoPersistentEntity;
import org.springframework.data.mongodb.core.validation.Validator;
import org.springframework.stereotype.Component;

import com.mongodb.MongoCommandException;

import es.uclm.esibuy.backend.shared.infrastructure.config.ColeccionesEsiBuy.Coleccion;

/**
 * Prepara la base de datos al arrancar la aplicación (T210, HU36): crea las colecciones de
 * {@link ColeccionesEsiBuy} que falten, con su validación, y todos sus índices.
 *
 * <p>Se puede ejecutar en cada arranque: lo que ya existe no se toca, salvo la validación, que se
 * vuelve a aplicar igual.
 *
 * <p>Permisos: crear colecciones e índices solo necesita lectura y escritura, pero cambiar la
 * validación de una colección que ya existe necesita permiso de administración ({@code collMod}).
 * El usuario de la aplicación no lo tiene, a propósito (mínimo privilegio): en ese caso se deja un
 * aviso en el log y la aplicación arranca igual, con la validación que ya tenía la colección.
 *
 * <p>Índices: además de los de {@link ColeccionesEsiBuy}, crea los declarados con anotaciones en las
 * clases {@code @Document}. Por eso {@code spring.data.mongodb.auto-index-creation} está desactivado:
 * Spring crearía la colección al crear sus índices, antes de que aquí se cree con su validación.
 */
@Component
public class InicializadorColecciones implements ApplicationRunner {

    private static final Logger LOG = LoggerFactory.getLogger(InicializadorColecciones.class);

    // Códigos de MongoDB cuando el usuario no tiene permiso: Unauthorized (MongoDB) y AtlasError,
    // que es el que devuelve Atlas («user is not allowed to do action [collMod]»).
    private static final Set<Integer> SIN_PERMISO = Set.of(13, 8000);

    private final MongoTemplate mongoTemplate;
    private final MongoMappingContext mappingContext;

    public InicializadorColecciones(MongoTemplate mongoTemplate, MongoMappingContext mappingContext) {
        this.mongoTemplate = mongoTemplate;
        this.mappingContext = mappingContext;
    }

    @Override
    public void run(ApplicationArguments args) {
        ColeccionesEsiBuy.todas().forEach(this::preparar);
        crearIndicesDeAnotaciones();
    }

    private void preparar(Coleccion coleccion) {
        if (mongoTemplate.collectionExists(coleccion.nombre())) {
            actualizarValidacion(coleccion);
        } else {
            mongoTemplate.createCollection(coleccion.nombre(), CollectionOptions.empty()
                    .schema(coleccion.esquema()).moderateValidation().failOnValidationError());
            LOG.info("Colección {} creada con su validación", coleccion.nombre());
        }
        coleccion.indices().forEach(indice -> mongoTemplate.indexOps(coleccion.nombre()).createIndex(indice));
    }

    // Nivel "moderate": no revalida los documentos antiguos que ya no cumplían las reglas.
    private void actualizarValidacion(Coleccion coleccion) {
        Document collMod = new Document("collMod", coleccion.nombre())
                .append("validator", Validator.schema(coleccion.esquema()).toDocument())
                .append("validationLevel", "moderate")
                .append("validationAction", "error");
        try {
            mongoTemplate.executeCommand(collMod);
        } catch (DataAccessException e) {
            if (!(e.getMostSpecificCause() instanceof MongoCommandException error)
                    || !SIN_PERMISO.contains(error.getErrorCode())) {
                throw e;
            }
            LOG.warn("Sin permiso para actualizar la validación de {}: se mantiene la que tenía. "
                    + "Para aplicarla, arranca la aplicación una vez con un usuario administrador.", coleccion.nombre());
        }
    }

    private void crearIndicesDeAnotaciones() {
        IndexResolver resolver = IndexResolver.create(mappingContext);
        for (MongoPersistentEntity<?> entidad : mappingContext.getPersistentEntities()) {
            if (entidad.isAnnotationPresent(org.springframework.data.mongodb.core.mapping.Document.class)) {
                resolver.resolveIndexFor(entidad.getTypeInformation())
                        .forEach(indice -> mongoTemplate.indexOps(entidad.getCollection()).createIndex(indice));
            }
        }
    }
}
