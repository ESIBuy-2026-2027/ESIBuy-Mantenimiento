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

    // Provisional: MongoDB 7.0 y no 8.0. Desde la imagen ubuntu-24.04 del 25/10/2026, los runners de GitHub y
    // Azure traen un kernel Linux 6.19 o superior, y MongoDB 8.x se cierra al arrancar en los kernels 6.19 a
    // 7.0.13 (SERVER-121912 y SERVER-125742); la 7.0 sí funciona. Volver a "mongo:8.0" cuando los runners
    // tengan un kernel 7.0.14 o superior. El backend no usa nada exclusivo de MongoDB 8.
    @Bean
    @ServiceConnection
    MongoDBContainer mongoDbContainer() {
        return new MongoDBContainer(DockerImageName.parse("mongo:7.0"));
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

    // API key de VirusTotal falsa (HU31, T397): así ni el CI ni cada desarrollador necesitan VIRUSTOTAL_API_KEY para
    // pasar la suite. Ningún test llama a VirusTotal: el adaptador se prueba con MockRestServiceServer
    @Bean
    DynamicPropertyRegistrar apiKeyVirusTotalDeTest() {
        return registro -> registro.add("esibuy.virustotal.api-key", () -> "api-key-falsa-de-test");
    }
}
