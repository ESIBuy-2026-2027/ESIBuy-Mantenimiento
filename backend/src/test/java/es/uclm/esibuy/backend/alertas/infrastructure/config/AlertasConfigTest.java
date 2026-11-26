package es.uclm.esibuy.backend.alertas.infrastructure.config;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Import;

import es.uclm.esibuy.backend.MongoTestcontainersConfig;
import es.uclm.esibuy.backend.alertas.application.port.in.ConsultaAlertasUseCase;
import es.uclm.esibuy.backend.alertas.application.port.in.GeneracionAlertasUseCase;
import es.uclm.esibuy.backend.alertas.application.port.in.GestionAlertasUseCase;
import es.uclm.esibuy.backend.alertas.application.port.out.AlertaRepositoryPort;
import es.uclm.esibuy.backend.alertas.application.port.out.ClienteEnSesionPort;
import es.uclm.esibuy.backend.alertas.application.port.out.FavoritosPort;
import es.uclm.esibuy.backend.alertas.application.port.out.TiposClientePort;
import es.uclm.esibuy.backend.alertas.application.service.ConsultaAlertasService;
import es.uclm.esibuy.backend.alertas.application.service.GeneracionAlertasService;
import es.uclm.esibuy.backend.alertas.application.service.GestionAlertasService;
import es.uclm.esibuy.backend.alertas.infrastructure.adapter.out.deseos.FavoritosAdapter;
import es.uclm.esibuy.backend.alertas.infrastructure.adapter.out.persistence.AlertaMongoAdapter;
import es.uclm.esibuy.backend.alertas.infrastructure.adapter.out.usuarios.ClienteEnSesionAdapter;
import es.uclm.esibuy.backend.alertas.infrastructure.adapter.out.usuarios.TiposClienteAdapter;

// Test técnico: Spring monta las piezas de las alertas al arrancar (AB#529). Arranca la aplicación entera con un
// MongoDB de Testcontainers: Docker Desktop debe estar abierto.
@SpringBootTest
@Import(MongoTestcontainersConfig.class)
class AlertasConfigTest {

    @Autowired
    private ApplicationContext contexto;

    @Test
    @DisplayName("Configuración: el contexto conecta los servicios de las alertas con su repositorio, deseos y usuarios")
    void elContextoConectaLasPiezasDeLasAlertas() {
        assertInstanceOf(GeneracionAlertasService.class, contexto.getBean(GeneracionAlertasUseCase.class));
        assertInstanceOf(ConsultaAlertasService.class, contexto.getBean(ConsultaAlertasUseCase.class));
        assertInstanceOf(GestionAlertasService.class, contexto.getBean(GestionAlertasUseCase.class));
        assertInstanceOf(AlertaMongoAdapter.class, contexto.getBean(AlertaRepositoryPort.class));
        assertInstanceOf(FavoritosAdapter.class, contexto.getBean(FavoritosPort.class));
        assertInstanceOf(TiposClienteAdapter.class, contexto.getBean(TiposClientePort.class));
        assertInstanceOf(ClienteEnSesionAdapter.class, contexto.getBean(ClienteEnSesionPort.class));
    }
}
