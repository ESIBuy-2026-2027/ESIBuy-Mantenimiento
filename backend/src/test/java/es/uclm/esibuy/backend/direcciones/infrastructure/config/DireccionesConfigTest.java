package es.uclm.esibuy.backend.direcciones.infrastructure.config;

import es.uclm.esibuy.backend.MongoTestcontainersConfig;
import es.uclm.esibuy.backend.direcciones.application.port.in.GestionDireccionesUseCase;
import es.uclm.esibuy.backend.direcciones.application.port.out.ClienteEnSesionPort;
import es.uclm.esibuy.backend.direcciones.application.port.out.DireccionRepositoryPort;
import es.uclm.esibuy.backend.direcciones.application.service.GestionDireccionesService;
import es.uclm.esibuy.backend.direcciones.infrastructure.adapter.out.persistence.DireccionMongoAdapter;
import es.uclm.esibuy.backend.direcciones.infrastructure.adapter.out.usuarios.ClienteEnSesionAdapter;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Import;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;

// Test técnico: Spring monta las piezas de las direcciones de envío al arrancar (HU5). Arranca la aplicación
// entera con un MongoDB de Testcontainers: Docker Desktop debe estar abierto.
@SpringBootTest
@Import(MongoTestcontainersConfig.class)
class DireccionesConfigTest {

    @Autowired
    private ApplicationContext contexto;

    @Test
    @DisplayName("Configuración: el contexto conecta el servicio de direcciones con su repositorio y el cliente en sesión")
    void elContextoConectaLasPiezasDeLasDirecciones() {
        assertInstanceOf(GestionDireccionesService.class, contexto.getBean(GestionDireccionesUseCase.class));
        assertInstanceOf(DireccionMongoAdapter.class, contexto.getBean(DireccionRepositoryPort.class));
        // Es el puerto propio de direcciones, distinto del de metodospago aunque se llame igual
        assertInstanceOf(ClienteEnSesionAdapter.class, contexto.getBean(ClienteEnSesionPort.class));
    }
}
