package es.uclm.esibuy.backend.carrito.infrastructure.config;

import java.time.Clock;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import es.uclm.esibuy.backend.carrito.application.port.in.GestionCarritoUseCase;
import es.uclm.esibuy.backend.carrito.application.port.out.CarritoRepositoryPort;
import es.uclm.esibuy.backend.carrito.application.port.out.ClienteEnSesionPort;
import es.uclm.esibuy.backend.carrito.application.port.out.ProductosPort;
import es.uclm.esibuy.backend.carrito.application.port.out.VendedoresPort;
import es.uclm.esibuy.backend.carrito.application.service.GestionCarritoService;
import es.uclm.esibuy.backend.carrito.infrastructure.adapter.out.persistence.CarritoDocumentMapper;
import es.uclm.esibuy.backend.carrito.infrastructure.adapter.out.persistence.CarritoMongoAdapter;
import es.uclm.esibuy.backend.carrito.infrastructure.adapter.out.persistence.CarritoMongoRepository;
import es.uclm.esibuy.backend.carrito.infrastructure.adapter.out.productos.ProductosAdapter;
import es.uclm.esibuy.backend.carrito.infrastructure.adapter.out.usuarios.ClienteEnSesionAdapter;
import es.uclm.esibuy.backend.carrito.infrastructure.adapter.out.usuarios.VendedoresAdapter;
import es.uclm.esibuy.backend.productos.application.port.in.CatalogoProductosUseCase;
import es.uclm.esibuy.backend.usuarios.application.port.in.AdministracionUsuariosUseCase;
import es.uclm.esibuy.backend.usuarios.application.port.in.AutenticacionUseCase;

/**
 * Configuración de Spring del carrito (HU9): conecta cada puerto con su adaptador, como MetodosPagoConfig. Los
 * nombres de los beans llevan «carrito» para no chocar con los puertos del mismo nombre de otros módulos.
 */
@Configuration
public class CarritoConfig {

    @Bean
    CarritoRepositoryPort carritoRepositoryPort(CarritoMongoRepository carritoMongoRepository) {
        return new CarritoMongoAdapter(carritoMongoRepository, new CarritoDocumentMapper());
    }

    @Bean
    ClienteEnSesionPort carritoClienteEnSesionPort(AutenticacionUseCase autenticacionUseCase) {
        return new ClienteEnSesionAdapter(autenticacionUseCase);
    }

    @Bean
    ProductosPort carritoProductosPort(CatalogoProductosUseCase catalogoProductosUseCase) {
        return new ProductosAdapter(catalogoProductosUseCase);
    }

    @Bean
    VendedoresPort carritoVendedoresPort(AdministracionUsuariosUseCase administracionUsuariosUseCase) {
        return new VendedoresAdapter(administracionUsuariosUseCase);
    }

    // El reloj es el de UsuariosConfig: con él se fechan las líneas del carrito
    @Bean
    GestionCarritoUseCase gestionCarritoUseCase(CarritoRepositoryPort carritoRepositoryPort,
            ClienteEnSesionPort carritoClienteEnSesionPort, ProductosPort carritoProductosPort,
            VendedoresPort carritoVendedoresPort, Clock reloj) {
        return new GestionCarritoService(carritoRepositoryPort, carritoClienteEnSesionPort, carritoProductosPort,
                carritoVendedoresPort, reloj);
    }
}
