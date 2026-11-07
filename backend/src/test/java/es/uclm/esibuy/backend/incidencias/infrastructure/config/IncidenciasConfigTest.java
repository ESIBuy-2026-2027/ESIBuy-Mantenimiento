package es.uclm.esibuy.backend.incidencias.infrastructure.config;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Import;

import es.uclm.esibuy.backend.MongoTestcontainersConfig;
import es.uclm.esibuy.backend.incidencias.application.port.in.ConsultaIncidenciasUseCase;
import es.uclm.esibuy.backend.incidencias.application.port.in.CreacionIncidenciasUseCase;
import es.uclm.esibuy.backend.incidencias.application.port.in.ResolucionIncidenciasUseCase;
import es.uclm.esibuy.backend.incidencias.application.port.in.SupervisionIncidenciasUseCase;
import es.uclm.esibuy.backend.incidencias.application.port.out.AutorEnSesionPort;
import es.uclm.esibuy.backend.incidencias.application.port.out.AutoresPort;
import es.uclm.esibuy.backend.incidencias.application.port.out.IncidenciaRepositoryPort;
import es.uclm.esibuy.backend.incidencias.application.service.ConsultaIncidenciasService;
import es.uclm.esibuy.backend.incidencias.application.service.CreacionIncidenciasService;
import es.uclm.esibuy.backend.incidencias.application.service.ResolucionIncidenciasService;
import es.uclm.esibuy.backend.incidencias.application.service.SupervisionIncidenciasService;
import es.uclm.esibuy.backend.incidencias.infrastructure.adapter.out.persistence.IncidenciaMongoAdapter;
import es.uclm.esibuy.backend.incidencias.infrastructure.adapter.out.usuarios.AutorEnSesionAdapter;
import es.uclm.esibuy.backend.incidencias.infrastructure.adapter.out.usuarios.AutoresAdapter;

// Test técnico: Spring monta las piezas de las incidencias al arrancar (HU44). Arranca la aplicación entera con un
// MongoDB de Testcontainers: Docker Desktop debe estar abierto.
@SpringBootTest
@Import(MongoTestcontainersConfig.class)
class IncidenciasConfigTest {

    @Autowired
    private ApplicationContext contexto;

    @Test
    @DisplayName("Configuración: el contexto conecta la creación de incidencias con sus piezas")
    void elContextoConectaLasPiezasDeLasIncidencias() {
        assertInstanceOf(CreacionIncidenciasService.class, contexto.getBean(CreacionIncidenciasUseCase.class));
        assertInstanceOf(IncidenciaMongoAdapter.class, contexto.getBean(IncidenciaRepositoryPort.class));
        assertInstanceOf(AutorEnSesionAdapter.class, contexto.getBean(AutorEnSesionPort.class));
        assertInstanceOf(ConsultaIncidenciasService.class, contexto.getBean(ConsultaIncidenciasUseCase.class));
    }

    @Test
    @DisplayName("Configuración: el contexto conecta la supervisión de incidencias con sus piezas (HU49)")
    void elContextoConectaLaSupervisionDeIncidencias() {
        assertInstanceOf(SupervisionIncidenciasService.class, contexto.getBean(SupervisionIncidenciasUseCase.class));
        assertInstanceOf(AutoresAdapter.class, contexto.getBean(AutoresPort.class));
    }

    @Test
    @DisplayName("Configuración: el contexto conecta la resolución de incidencias (HU50)")
    void elContextoConectaLaResolucionDeIncidencias() {
        assertInstanceOf(ResolucionIncidenciasService.class, contexto.getBean(ResolucionIncidenciasUseCase.class));
    }
}