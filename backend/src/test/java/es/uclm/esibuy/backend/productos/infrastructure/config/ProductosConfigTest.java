package es.uclm.esibuy.backend.productos.infrastructure.config;

import es.uclm.esibuy.backend.MongoTestcontainersConfig;
import es.uclm.esibuy.backend.productos.application.port.out.AnalisisImagenPort;
import es.uclm.esibuy.backend.productos.infrastructure.adapter.out.antivirus.VirusTotalAdapter;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Import;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;

// Test técnico: Spring monta las piezas de los productos al arrancar (el análisis antivirus de las imágenes, HU31,
// T397). Arranca la aplicación entera con un MongoDB de Testcontainers: Docker Desktop debe estar abierto. La API key
// de VirusTotal es falsa (MongoTestcontainersConfig) y no se llama a VirusTotal.
@SpringBootTest
@Import(MongoTestcontainersConfig.class)
class ProductosConfigTest {

    @Autowired
    private ApplicationContext contexto;

    @Test
    @DisplayName("Configuración: el contexto conecta el análisis antivirus de las imágenes con VirusTotal")
    void elContextoConectaElAnalisisDeImagenesConVirusTotal() {
        assertInstanceOf(VirusTotalAdapter.class, contexto.getBean(AnalisisImagenPort.class));
    }
}
