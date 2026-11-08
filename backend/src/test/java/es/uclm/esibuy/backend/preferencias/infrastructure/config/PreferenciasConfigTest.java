package es.uclm.esibuy.backend.preferencias.infrastructure.config;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;

import es.uclm.esibuy.backend.MongoTestcontainersConfig;
import es.uclm.esibuy.backend.preferencias.application.port.in.GestionPreferenciasUseCase;
import es.uclm.esibuy.backend.preferencias.application.port.out.CategoriasPort;
import es.uclm.esibuy.backend.preferencias.application.port.out.ClienteEnSesionPort;
import es.uclm.esibuy.backend.preferencias.application.port.out.PreferenciasRepositoryPort;
import es.uclm.esibuy.backend.preferencias.application.service.GestionPreferenciasService;
import es.uclm.esibuy.backend.preferencias.infrastructure.adapter.out.categorias.CategoriasAdapter;
import es.uclm.esibuy.backend.preferencias.infrastructure.adapter.out.persistence.PreferenciasMongoAdapter;
import es.uclm.esibuy.backend.preferencias.infrastructure.adapter.out.usuarios.ClienteEnSesionAdapter;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Import;

// Test técnico: Spring monta las piezas de las preferencias al arrancar (HU46). Arranca la aplicación entera con un
// MongoDB de Testcontainers: Docker Desktop debe estar abierto.
@SpringBootTest
@Import(MongoTestcontainersConfig.class)
class PreferenciasConfigTest {

    @Autowired
    private ApplicationContext contexto;

    @Test
    @DisplayName("Configuración: el contexto conecta el servicio de las preferencias con su repositorio, usuarios y categorías")
    void elContextoConectaLasPiezasDeLasPreferencias() {
        assertInstanceOf(GestionPreferenciasService.class, contexto.getBean(GestionPreferenciasUseCase.class));
        assertInstanceOf(PreferenciasMongoAdapter.class, contexto.getBean(PreferenciasRepositoryPort.class));
        assertInstanceOf(ClienteEnSesionAdapter.class, contexto.getBean(ClienteEnSesionPort.class));
        assertInstanceOf(CategoriasAdapter.class, contexto.getBean(CategoriasPort.class));
    }
}
