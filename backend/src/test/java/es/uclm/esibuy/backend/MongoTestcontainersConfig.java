package es.uclm.esibuy.backend;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.testcontainers.mongodb.MongoDBContainer;
import org.testcontainers.utility.DockerImageName;

/**
 * Configuración de test compartida: arranca un MongoDB real en Docker (Testcontainers) para los
 * tests que lo necesitan. {@code @ServiceConnection} conecta Spring Boot a ese contenedor sin
 * configurar ninguna URI. Requiere Docker Desktop abierto en local (el CI de GitHub ya tiene Docker).
 *
 * <p>PENDIENTE DE DECIDIR (equipo): versión de la imagen, a igualar con la del clúster de Atlas.
 */
@TestConfiguration(proxyBeanMethods = false)
public class MongoTestcontainersConfig {

    @Bean
    @ServiceConnection
    MongoDBContainer mongoDbContainer() {
        return new MongoDBContainer(DockerImageName.parse("mongo:8.0"));
    }
}
