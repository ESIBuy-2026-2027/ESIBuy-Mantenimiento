package es.uclm.esibuy.backend.deseos.infrastructure.config;

import es.uclm.esibuy.backend.MongoTestcontainersConfig;
import es.uclm.esibuy.backend.deseos.application.port.in.GestionListaDeseosUseCase;
import es.uclm.esibuy.backend.deseos.application.port.out.ClienteEnSesionPort;
import es.uclm.esibuy.backend.deseos.application.port.out.ListaDeseosRepositoryPort;
import es.uclm.esibuy.backend.deseos.application.port.out.ProductosPort;
import es.uclm.esibuy.backend.deseos.application.service.GestionListaDeseosService;
import es.uclm.esibuy.backend.deseos.infrastructure.adapter.out.persistence.ListaDeseosMongoAdapter;
import es.uclm.esibuy.backend.deseos.infrastructure.adapter.out.productos.ProductosAdapter;
import es.uclm.esibuy.backend.deseos.infrastructure.adapter.out.usuarios.ClienteEnSesionAdapter;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Import;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;

// Test técnico: Spring monta las piezas de la lista de deseos al arrancar (HU14). Arranca la aplicación entera con
// un MongoDB de Testcontainers: Docker Desktop debe estar abierto.
@SpringBootTest
@Import(MongoTestcontainersConfig.class)
class DeseosConfigTest {

    @Autowired
    private ApplicationContext contexto;

    @Test
    @DisplayName("Configuración: el contexto conecta el servicio de la lista de deseos con su repositorio, productos y usuarios")
    void elContextoConectaLasPiezasDeLaListaDeDeseos() {
        assertInstanceOf(GestionListaDeseosService.class, contexto.getBean(GestionListaDeseosUseCase.class));
        assertInstanceOf(ListaDeseosMongoAdapter.class, contexto.getBean(ListaDeseosRepositoryPort.class));
        assertInstanceOf(ClienteEnSesionAdapter.class, contexto.getBean(ClienteEnSesionPort.class));
        assertInstanceOf(ProductosAdapter.class, contexto.getBean(ProductosPort.class));
    }
}
