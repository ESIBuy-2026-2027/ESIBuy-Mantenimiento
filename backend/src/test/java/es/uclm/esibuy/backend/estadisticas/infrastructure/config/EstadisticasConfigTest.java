package es.uclm.esibuy.backend.estadisticas.infrastructure.config;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Import;

import es.uclm.esibuy.backend.MongoTestcontainersConfig;
import es.uclm.esibuy.backend.estadisticas.application.port.in.EstadisticasVendedorUseCase;
import es.uclm.esibuy.backend.estadisticas.application.port.out.InventarioPort;
import es.uclm.esibuy.backend.estadisticas.application.port.out.VendedorEnSesionPort;
import es.uclm.esibuy.backend.estadisticas.application.port.out.VentasPort;
import es.uclm.esibuy.backend.estadisticas.application.service.EstadisticasVendedorService;
import es.uclm.esibuy.backend.estadisticas.infrastructure.adapter.out.pedidos.VentasAdapter;
import es.uclm.esibuy.backend.estadisticas.infrastructure.adapter.out.productos.InventarioAdapter;
import es.uclm.esibuy.backend.estadisticas.infrastructure.adapter.out.usuarios.VendedorEnSesionAdapter;

// Test técnico: Spring monta las piezas de las estadísticas del vendedor al arrancar (HU17, T279).
// Arranca la aplicación entera con un MongoDB de Testcontainers: Docker Desktop debe estar abierto.
@SpringBootTest
@Import(MongoTestcontainersConfig.class)
class EstadisticasConfigTest {

    @Autowired
    private ApplicationContext contexto;

    @Test
    @DisplayName("Configuración: el contexto conecta las estadísticas del vendedor con usuarios, pedidos y productos")
    void elContextoConectaLasPiezasDeLasEstadisticas() {
        assertInstanceOf(EstadisticasVendedorService.class, contexto.getBean(EstadisticasVendedorUseCase.class));
        assertInstanceOf(VendedorEnSesionAdapter.class, contexto.getBean(VendedorEnSesionPort.class));
        assertInstanceOf(VentasAdapter.class, contexto.getBean(VentasPort.class));
        assertInstanceOf(InventarioAdapter.class, contexto.getBean(InventarioPort.class));
    }
}
