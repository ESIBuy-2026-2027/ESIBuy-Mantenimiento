package es.uclm.esibuy.backend.valoraciones.infrastructure.config;

import es.uclm.esibuy.backend.MongoTestcontainersConfig;
import es.uclm.esibuy.backend.valoraciones.application.port.in.ValoracionProductosUseCase;
import es.uclm.esibuy.backend.valoraciones.application.port.out.ClienteEnSesionPort;
import es.uclm.esibuy.backend.valoraciones.application.port.out.ComprasPort;
import es.uclm.esibuy.backend.valoraciones.application.port.out.ValoracionRepositoryPort;
import es.uclm.esibuy.backend.valoraciones.application.service.ValoracionProductosService;
import es.uclm.esibuy.backend.valoraciones.infrastructure.adapter.out.pedidos.ComprasAdapter;
import es.uclm.esibuy.backend.valoraciones.infrastructure.adapter.out.persistence.ValoracionMongoAdapter;
import es.uclm.esibuy.backend.valoraciones.infrastructure.adapter.out.usuarios.ClienteEnSesionAdapter;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Import;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;

// Test técnico: Spring monta las piezas de las valoraciones al arrancar (HU13). Arranca la aplicación entera con un
// MongoDB de Testcontainers: Docker Desktop debe estar abierto.
@SpringBootTest
@Import(MongoTestcontainersConfig.class)
class ValoracionesConfigTest {

    @Autowired
    private ApplicationContext contexto;

    @Test
    @DisplayName("Configuración: el contexto conecta la valoración de productos con su repositorio y los otros módulos")
    void elContextoConectaLasPiezasDeLasValoraciones() {
        assertInstanceOf(ValoracionProductosService.class, contexto.getBean(ValoracionProductosUseCase.class));
        assertInstanceOf(ValoracionMongoAdapter.class, contexto.getBean(ValoracionRepositoryPort.class));
        assertInstanceOf(ClienteEnSesionAdapter.class, contexto.getBean(ClienteEnSesionPort.class));
        assertInstanceOf(ComprasAdapter.class, contexto.getBean(ComprasPort.class));
    }
}
