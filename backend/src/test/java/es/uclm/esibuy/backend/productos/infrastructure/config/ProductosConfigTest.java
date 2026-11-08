package es.uclm.esibuy.backend.productos.infrastructure.config;

import es.uclm.esibuy.backend.MongoTestcontainersConfig;
import es.uclm.esibuy.backend.productos.application.port.in.InventarioVendedorUseCase;
import es.uclm.esibuy.backend.productos.application.port.in.ValoracionesProductoUseCase;
import es.uclm.esibuy.backend.productos.application.port.out.AnalisisImagenPort;
import es.uclm.esibuy.backend.productos.application.service.InventarioVendedorService;
import es.uclm.esibuy.backend.productos.application.service.ValoracionesProductoService;
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

    @Test
    @DisplayName("Configuración: el contexto conecta la actualización de la valoración de los productos (HU13)")
    void elContextoConectaLaActualizacionDeLaValoracion() {
        assertInstanceOf(ValoracionesProductoService.class, contexto.getBean(ValoracionesProductoUseCase.class));
    }

    @Test
    @DisplayName("Configuración: el contexto conecta el inventario del vendedor para sus estadísticas (HU17)")
    void elContextoConectaElInventarioDelVendedor() {
        assertInstanceOf(InventarioVendedorService.class, contexto.getBean(InventarioVendedorUseCase.class));
    }
}
