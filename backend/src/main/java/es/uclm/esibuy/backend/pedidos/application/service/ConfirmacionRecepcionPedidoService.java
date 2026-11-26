
package es.uclm.esibuy.backend.pedidos.application.service;

import java.time.Clock;

import org.springframework.security.access.prepost.PreAuthorize;

import es.uclm.esibuy.backend.pedidos.application.port.in.ConfirmacionRecepcionPedidoUseCase;
import es.uclm.esibuy.backend.pedidos.application.port.out.ClienteEnSesionPort;
import es.uclm.esibuy.backend.pedidos.application.port.out.PedidoRepositoryPort;
import es.uclm.esibuy.backend.pedidos.domain.exception.PedidoNoEncontradoException;
import es.uclm.esibuy.backend.pedidos.domain.model.Pedido;
import es.uclm.esibuy.backend.shared.seguridad.Permisos;

/**
 * Confirma la recepción de un pedido por parte del cliente.
 */
public class ConfirmacionRecepcionPedidoService implements ConfirmacionRecepcionPedidoUseCase {

    private final PedidoRepositoryPort pedidoRepository;
    private final ClienteEnSesionPort clienteEnSesion;
    private final Clock reloj;

    public ConfirmacionRecepcionPedidoService(
            PedidoRepositoryPort pedidoRepository,
            ClienteEnSesionPort clienteEnSesion,
            Clock reloj) {
        this.pedidoRepository = pedidoRepository;
        this.clienteEnSesion = clienteEnSesion;
        this.reloj = reloj;
    }

    @PreAuthorize(Permisos.SOLO_CLIENTE_PROPIO)
    @Override
    public Pedido confirmarRecepcion(String emailCliente, String pedidoId) {
        String clienteId = clienteEnSesion.idPorCorreo(emailCliente);

        Pedido pedido = pedidoRepository.buscarPorId(pedidoId)
                .filter(encontrado -> encontrado.getClienteId().equals(clienteId))
                .orElseThrow(() -> new PedidoNoEncontradoException("El pedido no existe"));

        pedido.confirmarEntrega(reloj.instant());

        return pedidoRepository.guardar(pedido);
    }
}
