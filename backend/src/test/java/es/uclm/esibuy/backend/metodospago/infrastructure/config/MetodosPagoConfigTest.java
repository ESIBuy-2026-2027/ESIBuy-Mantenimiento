package es.uclm.esibuy.backend.metodospago.infrastructure.config;

import es.uclm.esibuy.backend.MongoTestcontainersConfig;
import es.uclm.esibuy.backend.metodospago.application.port.in.GestionMetodosPagoUseCase;
import es.uclm.esibuy.backend.metodospago.application.port.out.CifradoDatosPagoPort;
import es.uclm.esibuy.backend.metodospago.application.port.out.ClienteEnSesionPort;
import es.uclm.esibuy.backend.metodospago.application.port.out.MetodoPagoRepositoryPort;
import es.uclm.esibuy.backend.metodospago.application.service.GestionMetodosPagoService;
import es.uclm.esibuy.backend.metodospago.infrastructure.adapter.out.persistence.MetodoPagoMongoAdapter;
import es.uclm.esibuy.backend.metodospago.infrastructure.adapter.out.seguridad.AesGcmCifradoDatosPagoAdapter;
import es.uclm.esibuy.backend.metodospago.infrastructure.adapter.out.usuarios.ClienteEnSesionAdapter;
import es.uclm.esibuy.backend.shared.infrastructure.seguridad.CifradorAesGcm;
import java.util.Base64;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Import;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertEquals;

// Test técnico: Spring monta las piezas de los métodos de pago al arrancar (HU6). Arranca la aplicación entera
// con un MongoDB de Testcontainers: Docker Desktop debe estar abierto. La clave de cifrado la pone
// MongoTestcontainersConfig, aleatoria y solo para los tests.
@SpringBootTest
@Import(MongoTestcontainersConfig.class)
class MetodosPagoConfigTest {

    @Autowired
    private ApplicationContext contexto;

    @Test
    @DisplayName("Configuración: el contexto conecta el servicio de métodos de pago con su repositorio, su cifrado y el cliente en sesión")
    void elContextoConectaLasPiezasDeLosMetodosDePago() {
        assertInstanceOf(GestionMetodosPagoService.class, contexto.getBean(GestionMetodosPagoUseCase.class));
        assertInstanceOf(MetodoPagoMongoAdapter.class, contexto.getBean(MetodoPagoRepositoryPort.class));
        assertInstanceOf(AesGcmCifradoDatosPagoAdapter.class, contexto.getBean(CifradoDatosPagoPort.class));
        assertInstanceOf(ClienteEnSesionAdapter.class, contexto.getBean(ClienteEnSesionPort.class));
    }

    @Test
    @DisplayName("Configuración: los datos de pago se cifran con la clave de esibuy.pagos.clave-cifrado")
    void losDatosDePagoSeCifranConLaClaveConfigurada() {
        String cifrado = contexto.getBean(CifradoDatosPagoPort.class).cifrar("4111111111111111;Juan Pérez;12/27");

        // Solo quien tiene la clave de la propiedad recupera los datos
        String clave = contexto.getEnvironment().getRequiredProperty("esibuy.pagos.clave-cifrado");
        assertEquals("4111111111111111;Juan Pérez;12/27",
                new CifradorAesGcm(Base64.getDecoder().decode(clave)).descifrar(cifrado));
    }
}
