package es.uclm.esibuy.backend.pedidos.infrastructure.config;

import es.uclm.esibuy.backend.MongoTestcontainersConfig;
import es.uclm.esibuy.backend.pedidos.application.port.in.CancelacionPedidoUseCase;
import es.uclm.esibuy.backend.pedidos.application.port.in.ConsultaComprasUseCase;
import es.uclm.esibuy.backend.pedidos.application.port.in.RealizacionPedidoUseCase;
import es.uclm.esibuy.backend.pedidos.application.port.out.CarritoPort;
import es.uclm.esibuy.backend.pedidos.application.port.out.ClienteEnSesionPort;
import es.uclm.esibuy.backend.pedidos.application.port.out.ContadorPedidosPort;
import es.uclm.esibuy.backend.pedidos.application.port.out.DireccionesPort;
import es.uclm.esibuy.backend.pedidos.application.port.out.ExistenciasPort;
import es.uclm.esibuy.backend.pedidos.application.port.out.MetodosPagoPort;
import es.uclm.esibuy.backend.pedidos.application.port.out.PedidoRepositoryPort;
import es.uclm.esibuy.backend.pedidos.application.service.CancelacionPedidoService;
import es.uclm.esibuy.backend.pedidos.application.service.ConsultaComprasService;
import es.uclm.esibuy.backend.pedidos.application.service.RealizacionPedidoService;
import es.uclm.esibuy.backend.pedidos.infrastructure.adapter.out.carrito.CarritoAdapter;
import es.uclm.esibuy.backend.pedidos.infrastructure.adapter.out.direcciones.DireccionesAdapter;
import es.uclm.esibuy.backend.pedidos.infrastructure.adapter.out.metodospago.MetodosPagoAdapter;
import es.uclm.esibuy.backend.pedidos.infrastructure.adapter.out.persistence.ContadorPedidosMongoAdapter;
import es.uclm.esibuy.backend.pedidos.infrastructure.adapter.out.persistence.PedidoMongoAdapter;
import es.uclm.esibuy.backend.pedidos.infrastructure.adapter.out.productos.ExistenciasAdapter;
import es.uclm.esibuy.backend.pedidos.infrastructure.adapter.out.usuarios.ClienteEnSesionAdapter;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Import;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;

// Test técnico: Spring monta las piezas de los pedidos al arrancar (HU10, y la consulta de compras de HU13).
// También la cancelación de pedidos (HU12, T375).
// Arranca la aplicación entera con un MongoDB de Testcontainers: Docker Desktop debe estar abierto.
@SpringBootTest
@Import(MongoTestcontainersConfig.class)
class PedidosConfigTest {

    @Autowired
    private ApplicationContext contexto;

    @Test
    @DisplayName("Configuración: el contexto conecta la realización de pedidos con su repositorio y los otros módulos")
    void elContextoConectaLasPiezasDeLosPedidos() {
        assertInstanceOf(RealizacionPedidoService.class, contexto.getBean(RealizacionPedidoUseCase.class));
        assertInstanceOf(ConsultaComprasService.class, contexto.getBean(ConsultaComprasUseCase.class));
        assertInstanceOf(CancelacionPedidoService.class, contexto.getBean(CancelacionPedidoUseCase.class));
        assertInstanceOf(PedidoMongoAdapter.class, contexto.getBean(PedidoRepositoryPort.class));
        assertInstanceOf(ContadorPedidosMongoAdapter.class, contexto.getBean(ContadorPedidosPort.class));
        assertInstanceOf(ClienteEnSesionAdapter.class, contexto.getBean(ClienteEnSesionPort.class));
        assertInstanceOf(CarritoAdapter.class, contexto.getBean(CarritoPort.class));
        assertInstanceOf(MetodosPagoAdapter.class, contexto.getBean(MetodosPagoPort.class));
        assertInstanceOf(DireccionesAdapter.class, contexto.getBean(DireccionesPort.class));
        assertInstanceOf(ExistenciasAdapter.class, contexto.getBean(ExistenciasPort.class));
    }
}
