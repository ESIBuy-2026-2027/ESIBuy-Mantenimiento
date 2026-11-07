package es.uclm.esibuy.backend.incidencias.infrastructure.config;

import java.time.Clock;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

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
import es.uclm.esibuy.backend.incidencias.infrastructure.adapter.out.persistence.IncidenciaDocumentMapper;
import es.uclm.esibuy.backend.incidencias.infrastructure.adapter.out.persistence.IncidenciaMongoAdapter;
import es.uclm.esibuy.backend.incidencias.infrastructure.adapter.out.persistence.IncidenciaMongoRepository;
import es.uclm.esibuy.backend.incidencias.infrastructure.adapter.out.usuarios.AutorEnSesionAdapter;
import es.uclm.esibuy.backend.incidencias.infrastructure.adapter.out.usuarios.AutoresAdapter;
import es.uclm.esibuy.backend.usuarios.application.port.in.AdministracionUsuariosUseCase;
import es.uclm.esibuy.backend.usuarios.application.port.in.AutenticacionUseCase;

/**
 * Conecta las piezas del módulo de incidencias (HU44), de su supervisión (HU49) y de su resolución (HU50) por el
 * administrador: los servicios
 * de aplicación, sus adaptadores de salida y el reloj. Los servicios no llevan @Service: los crea esta configuración,
 * así dominio y aplicación no conocen Spring.
 */
@Configuration
public class IncidenciasConfig {

    @Bean
    IncidenciaRepositoryPort incidenciaRepositoryPort(IncidenciaMongoRepository incidenciaMongoRepository) {
        return new IncidenciaMongoAdapter(incidenciaMongoRepository, new IncidenciaDocumentMapper());
    }

    @Bean
    AutorEnSesionPort incidenciasAutorEnSesionPort(AutenticacionUseCase autenticacionUseCase) {
        return new AutorEnSesionAdapter(autenticacionUseCase);
    }

    @Bean
    CreacionIncidenciasUseCase creacionIncidenciasUseCase(IncidenciaRepositoryPort incidenciaRepositoryPort,
            AutorEnSesionPort incidenciasAutorEnSesionPort, Clock reloj) {
        return new CreacionIncidenciasService(incidenciaRepositoryPort, incidenciasAutorEnSesionPort, reloj);
    }

    @Bean
    ConsultaIncidenciasUseCase consultaIncidenciasUseCase(IncidenciaRepositoryPort incidenciaRepositoryPort,
            AutorEnSesionPort incidenciasAutorEnSesionPort) {
        return new ConsultaIncidenciasService(incidenciaRepositoryPort, incidenciasAutorEnSesionPort);
    }

    // Supervisión (HU49): el prefijo «incidencias» evita chocar con beans de otros módulos
    @Bean
    AutoresPort incidenciasAutoresPort(AdministracionUsuariosUseCase administracionUsuarios) {
        return new AutoresAdapter(administracionUsuarios);
    }

    @Bean
    SupervisionIncidenciasUseCase supervisionIncidenciasUseCase(IncidenciaRepositoryPort incidenciaRepositoryPort,
            AutoresPort incidenciasAutoresPort) {
        return new SupervisionIncidenciasService(incidenciaRepositoryPort, incidenciasAutoresPort);
    }

    // Resolución (HU50): abrir y cerrar incidencias
    @Bean
    ResolucionIncidenciasUseCase resolucionIncidenciasUseCase(IncidenciaRepositoryPort incidenciaRepositoryPort) {
        return new ResolucionIncidenciasService(incidenciaRepositoryPort);
    }
}
