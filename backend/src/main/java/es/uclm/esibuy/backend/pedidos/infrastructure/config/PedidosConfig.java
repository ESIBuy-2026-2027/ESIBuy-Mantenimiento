package es.uclm.esibuy.backend.pedidos.infrastructure.config;

import java.time.Clock;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.MongoTemplate;

import es.uclm.esibuy.backend.carrito.application.port.in.GestionCarritoUseCase;
import es.uclm.esibuy.backend.direcciones.application.port.in.GestionDireccionesUseCase;
import es.uclm.esibuy.backend.metodospago.application.port.in.GestionMetodosPagoUseCase;
import es.uclm.esibuy.backend.pedidos.application.port.in.RealizacionPedidoUseCase;
import es.uclm.esibuy.backend.pedidos.application.port.out.CarritoPort;
import es.uclm.esibuy.backend.pedidos.application.port.out.ClienteEnSesionPort;
import es.uclm.esibuy.backend.pedidos.application.port.out.ContadorPedidosPort;
import es.uclm.esibuy.backend.pedidos.application.port.out.DireccionesPort;
import es.uclm.esibuy.backend.pedidos.application.port.out.ExistenciasPort;
import es.uclm.esibuy.backend.pedidos.application.port.out.MetodosPagoPort;
import es.uclm.esibuy.backend.pedidos.application.port.out.PedidoRepositoryPort;
import es.uclm.esibuy.backend.pedidos.application.service.RealizacionPedidoService;
import es.uclm.esibuy.backend.pedidos.infrastructure.adapter.out.carrito.CarritoAdapter;
import es.uclm.esibuy.backend.pedidos.infrastructure.adapter.out.direcciones.DireccionesAdapter;
import es.uclm.esibuy.backend.pedidos.infrastructure.adapter.out.metodospago.MetodosPagoAdapter;
import es.uclm.esibuy.backend.pedidos.infrastructure.adapter.out.persistence.ContadorPedidosMongoAdapter;
import es.uclm.esibuy.backend.pedidos.infrastructure.adapter.out.persistence.PedidoDocumentMapper;
import es.uclm.esibuy.backend.pedidos.infrastructure.adapter.out.persistence.PedidoMongoAdapter;
import es.uclm.esibuy.backend.pedidos.infrastructure.adapter.out.persistence.PedidoMongoRepository;
import es.uclm.esibuy.backend.pedidos.infrastructure.adapter.out.productos.ExistenciasAdapter;
import es.uclm.esibuy.backend.pedidos.infrastructure.adapter.out.usuarios.ClienteEnSesionAdapter;
import es.uclm.esibuy.backend.productos.application.port.in.ExistenciasProductosUseCase;
import es.uclm.esibuy.backend.usuarios.application.port.in.AutenticacionUseCase;

/**
 * Configuración de Spring de los pedidos (HU10): conecta cada puerto con su adaptador, como CarritoConfig. Los
 * nombres de los beans llevan «pedidos» para no chocar con los puertos del mismo nombre de otros módulos.
 */
@Configuration
public class PedidosConfig {

    @Bean
    PedidoRepositoryPort pedidoRepositoryPort(PedidoMongoRepository pedidoMongoRepository) {
        return new PedidoMongoAdapter(pedidoMongoRepository, new PedidoDocumentMapper());
    }

    @Bean
    ContadorPedidosPort contadorPedidosPort(MongoTemplate mongoTemplate) {
        return new ContadorPedidosMongoAdapter(mongoTemplate);
    }

    @Bean
    ClienteEnSesionPort pedidosClienteEnSesionPort(AutenticacionUseCase autenticacionUseCase) {
        return new ClienteEnSesionAdapter(autenticacionUseCase);
    }

    @Bean
    CarritoPort pedidosCarritoPort(GestionCarritoUseCase gestionCarritoUseCase) {
        return new CarritoAdapter(gestionCarritoUseCase);
    }

    @Bean
    MetodosPagoPort pedidosMetodosPagoPort(GestionMetodosPagoUseCase gestionMetodosPagoUseCase) {
        return new MetodosPagoAdapter(gestionMetodosPagoUseCase);
    }

    @Bean
    DireccionesPort pedidosDireccionesPort(GestionDireccionesUseCase gestionDireccionesUseCase) {
        return new DireccionesAdapter(gestionDireccionesUseCase);
    }

    @Bean
    ExistenciasPort pedidosExistenciasPort(ExistenciasProductosUseCase existenciasProductosUseCase) {
        return new ExistenciasAdapter(existenciasProductosUseCase);
    }

    // El reloj es el de UsuariosConfig: con él se fecha el pedido y se elige el año de su número
    @Bean
    RealizacionPedidoUseCase realizacionPedidoUseCase(PedidoRepositoryPort pedidoRepositoryPort,
            ClienteEnSesionPort pedidosClienteEnSesionPort, CarritoPort pedidosCarritoPort,
            MetodosPagoPort pedidosMetodosPagoPort, DireccionesPort pedidosDireccionesPort,
            ExistenciasPort pedidosExistenciasPort, ContadorPedidosPort contadorPedidosPort, Clock reloj) {
        return new RealizacionPedidoService(pedidoRepositoryPort, pedidosClienteEnSesionPort, pedidosCarritoPort,
                pedidosMetodosPagoPort, pedidosDireccionesPort, pedidosExistenciasPort, contadorPedidosPort, reloj);
    }
}
