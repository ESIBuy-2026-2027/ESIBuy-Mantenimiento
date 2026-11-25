package es.uclm.esibuy.backend.pedidos.application.service;

import org.springframework.security.access.prepost.PreAuthorize;

import es.uclm.esibuy.backend.pedidos.application.port.in.ConfirmacionRecepcionPedidoUseCase;
import es.uclm.esibuy.backend.pedidos.application.port.out.ClienteEnSesionPort;
import es.uclm.esibuy.backend.pedidos.application.port.out.PedidoRepositoryPort;
import es.uclm.esibuy.backend.pedidos.domain.exception.PedidoNoEncontradoException;
import es.uclm.esibuy.backend.pedidos.domain.model.Pedido;
import es.uclm.esibuy.backend.shared.seguridad.Permisos;

/**
 * Confirma la recepción de un pedido por parte del cliente: pasa el pedido a ENTREGADO y lo guarda.
 */
public class ConfirmacionRecepcionPedidoService implements ConfirmacionRecepcionPedidoUseCase {

    private final PedidoRepositoryPort pedidoRepository;
    private final ClienteEnSesionPort clienteEnSesion;

    public ConfirmacionRecepcionPedidoService(PedidoRepositoryPort pedidoRepository,
            ClienteEnSesionPort clienteEnSesion) {
        this.pedidoRepository = pedidoRepository;
        this.clienteEnSesion = clienteEnSesion;
    }

    @PreAuthorize(Permisos.SOLO_CLIENTE_PROPIO)
    @Override
    public Pedido confirmarRecepcion(String emailCliente, String pedidoId) {
        String clienteId = clienteEnSesion.idPorCorreo(emailCliente);
        // Un pedido ajeno da la misma respuesta que uno inexistente, como en ConsultaComprasService
        Pedido pedido = pedidoRepository.buscarPorId(pedidoId)
                .filter(encontrado -> encontrado.getClienteId().equals(clienteId))
                .orElseThrow(() -> new PedidoNoEncontradoException("El pedido no existe"));

        pedido.marcarEntregado();
        pedidoRepository.guardar(pedido);
        return pedido;
    }
}