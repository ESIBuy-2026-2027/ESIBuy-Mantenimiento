package es.uclm.esibuy.backend.pedidos.application.service;

import java.time.Clock;

import org.springframework.security.access.prepost.PreAuthorize;

import es.uclm.esibuy.backend.pedidos.application.port.in.SeguimientoEnvioUseCase;
import es.uclm.esibuy.backend.pedidos.application.port.out.ClienteEnSesionPort;
import es.uclm.esibuy.backend.pedidos.application.port.out.PedidoRepositoryPort;
import es.uclm.esibuy.backend.pedidos.domain.exception.PedidoNoEncontradoException;
import es.uclm.esibuy.backend.pedidos.domain.model.Pedido;
import es.uclm.esibuy.backend.shared.seguridad.Permisos;

/**
 * Seguimiento del envío del cliente: consulta el estado y el histórico de su pedido y registra que lo ha recibido.
 * La regla de la transición a ENTREGADO es del dominio (Pedido.confirmarEntrega); aquí solo se orquesta.
 */
public class SeguimientoEnvioService implements SeguimientoEnvioUseCase {

    private final PedidoRepositoryPort pedidoRepository;
    private final ClienteEnSesionPort clienteEnSesion;
    private final Clock reloj;

    public SeguimientoEnvioService(PedidoRepositoryPort pedidoRepository, ClienteEnSesionPort clienteEnSesion,
            Clock reloj) {
        this.pedidoRepository = pedidoRepository;
        this.clienteEnSesion = clienteEnSesion;
        this.reloj = reloj;
    }

    @PreAuthorize(Permisos.SOLO_CLIENTE_PROPIO)
    @Override
    public Pedido consultarSeguimiento(String emailCliente, String pedidoId) {
        return pedidoDelCliente(emailCliente, pedidoId);
    }

    // Sin try/catch: si confirmarEntrega() lanza (no está enviado), no se guarda nada
    @PreAuthorize(Permisos.SOLO_CLIENTE_PROPIO)
    @Override
    public Pedido confirmarEntrega(String emailCliente, String pedidoId) {
        Pedido pedido = pedidoDelCliente(emailCliente, pedidoId);
        pedido.confirmarEntrega(reloj.instant());
        return pedidoRepository.guardar(pedido);
    }

    // Un pedido ajeno da la misma respuesta que uno inexistente, como en ConsultaComprasService
    private Pedido pedidoDelCliente(String emailCliente, String pedidoId) {
        String clienteId = clienteEnSesion.idPorCorreo(emailCliente);
        return pedidoRepository.buscarPorId(pedidoId)
                .filter(pedido -> pedido.getClienteId().equals(clienteId))
                .orElseThrow(() -> new PedidoNoEncontradoException("El pedido no existe"));
    }
}
