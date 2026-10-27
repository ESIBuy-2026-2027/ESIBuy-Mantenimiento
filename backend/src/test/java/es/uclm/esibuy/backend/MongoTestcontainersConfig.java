package es.uclm.esibuy.backend;

import java.security.SecureRandom;
import java.util.Base64;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.DynamicPropertyRegistrar;
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

    // Clave de cifrado de los datos de pago (HU6), aleatoria y solo para los tests: así ni el CI ni cada
    // desarrollador necesitan la variable ESIBUY_PAGOS_CLAVE_CIFRADO para pasar la suite
    @Bean
    DynamicPropertyRegistrar claveCifradoPagosDeTest() {
        byte[] clave = new byte[32];
        new SecureRandom().nextBytes(clave);
        String claveBase64 = Base64.getEncoder().encodeToString(clave);
        return registro -> registro.add("esibuy.pagos.clave-cifrado", () -> claveBase64);
    }
}
