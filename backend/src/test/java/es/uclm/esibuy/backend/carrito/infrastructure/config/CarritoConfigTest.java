package es.uclm.esibuy.backend.carrito.infrastructure.config;

import es.uclm.esibuy.backend.MongoTestcontainersConfig;
import es.uclm.esibuy.backend.carrito.application.port.in.GestionCarritoUseCase;
import es.uclm.esibuy.backend.carrito.application.port.out.CarritoRepositoryPort;
import es.uclm.esibuy.backend.carrito.application.port.out.ClienteEnSesionPort;
import es.uclm.esibuy.backend.carrito.application.port.out.ProductosPort;
import es.uclm.esibuy.backend.carrito.application.port.out.VendedoresPort;
import es.uclm.esibuy.backend.carrito.application.service.GestionCarritoService;
import es.uclm.esibuy.backend.carrito.infrastructure.adapter.out.persistence.CarritoMongoAdapter;
import es.uclm.esibuy.backend.carrito.infrastructure.adapter.out.productos.ProductosAdapter;
import es.uclm.esibuy.backend.carrito.infrastructure.adapter.out.usuarios.ClienteEnSesionAdapter;
import es.uclm.esibuy.backend.carrito.infrastructure.adapter.out.usuarios.VendedoresAdapter;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Import;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;

// Test técnico: Spring monta las piezas del carrito al arrancar (HU9). Arranca la aplicación entera con un MongoDB
// de Testcontainers: Docker Desktop debe estar abierto.
@SpringBootTest
@Import(MongoTestcontainersConfig.class)
class CarritoConfigTest {

    @Autowired
    private ApplicationContext contexto;

    @Test
    @DisplayName("Configuración: el contexto conecta el servicio del carrito con su repositorio, productos y usuarios")
    void elContextoConectaLasPiezasDelCarrito() {
        assertInstanceOf(GestionCarritoService.class, contexto.getBean(GestionCarritoUseCase.class));
        assertInstanceOf(CarritoMongoAdapter.class, contexto.getBean(CarritoRepositoryPort.class));
        assertInstanceOf(ClienteEnSesionAdapter.class, contexto.getBean(ClienteEnSesionPort.class));
        assertInstanceOf(ProductosAdapter.class, contexto.getBean(ProductosPort.class));
        assertInstanceOf(VendedoresAdapter.class, contexto.getBean(VendedoresPort.class));
    }
}
