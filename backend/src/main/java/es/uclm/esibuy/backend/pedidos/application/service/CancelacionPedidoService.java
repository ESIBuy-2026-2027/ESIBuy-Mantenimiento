package es.uclm.esibuy.backend.pedidos.application.service;

import org.springframework.security.access.prepost.PreAuthorize;

import es.uclm.esibuy.backend.pedidos.application.port.in.CancelacionPedidoUseCase;
import es.uclm.esibuy.backend.pedidos.application.port.out.ClienteEnSesionPort;
import es.uclm.esibuy.backend.pedidos.application.port.out.ExistenciasPort;
import es.uclm.esibuy.backend.pedidos.application.port.out.PedidoRepositoryPort;
import es.uclm.esibuy.backend.pedidos.domain.exception.PedidoNoEncontradoException;
import es.uclm.esibuy.backend.pedidos.domain.model.LineaPedido;
import es.uclm.esibuy.backend.pedidos.domain.model.Pedido;
import es.uclm.esibuy.backend.shared.seguridad.Permisos;

/**
 * Cancela un pedido del cliente (HU12): lo pasa a CANCELADO, lo guarda y después repone el stock de cada línea.
 */
public class CancelacionPedidoService implements CancelacionPedidoUseCase {

    private final PedidoRepositoryPort pedidoRepository;
    private final ClienteEnSesionPort clienteEnSesion;
    private final ExistenciasPort existencias;

    public CancelacionPedidoService(PedidoRepositoryPort pedidoRepository, ClienteEnSesionPort clienteEnSesion,
            ExistenciasPort existencias) {
        this.pedidoRepository = pedidoRepository;
        this.clienteEnSesion = clienteEnSesion;
        this.existencias = existencias;
    }

    // Sin try/catch: si cancelar() lanza (pedido ya enviado), no se guarda nada ni se repone stock
    @PreAuthorize(Permisos.SOLO_CLIENTE_PROPIO)
    @Override
    public Pedido cancelarPedido(String emailCliente, String pedidoId) {
        String clienteId = clienteEnSesion.idPorCorreo(emailCliente);
        // Un pedido ajeno da la misma respuesta que uno inexistente, como en ConsultaComprasService
        Pedido pedido = pedidoRepository.buscarPorId(pedidoId)
                .filter(encontrado -> encontrado.getClienteId().equals(clienteId))
                .orElseThrow(() -> new PedidoNoEncontradoException("El pedido no existe"));

        pedido.cancelar();
        pedidoRepository.guardar(pedido);
        for (LineaPedido linea : pedido.getLineas()) {
            existencias.reponer(linea.productoId(), linea.cantidad());
        }
        return pedido;
    }
}
