package es.uclm.esibuy.backend.pedidos.application.service;

import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;

import es.uclm.esibuy.backend.pedidos.application.port.in.ConsultaComprasUseCase;
import es.uclm.esibuy.backend.pedidos.application.port.out.ClienteEnSesionPort;
import es.uclm.esibuy.backend.pedidos.application.port.out.PedidoRepositoryPort;
import es.uclm.esibuy.backend.pedidos.domain.exception.PedidoNoEncontradoException;
import es.uclm.esibuy.backend.pedidos.domain.model.Pedido;
import es.uclm.esibuy.backend.shared.seguridad.Permisos;

/**
 * Consultas de las compras del cliente: si ha comprado un producto (HU13), donde cuenta como compra un pedido
 * PAGADO que lo contenga, y su historial de pedidos con el detalle de cada uno (HU11).
 */
public class ConsultaComprasService implements ConsultaComprasUseCase {

    private final PedidoRepositoryPort pedidoRepository;
    private final ClienteEnSesionPort clienteEnSesion;

    public ConsultaComprasService(PedidoRepositoryPort pedidoRepository, ClienteEnSesionPort clienteEnSesion) {
        this.pedidoRepository = pedidoRepository;
        this.clienteEnSesion = clienteEnSesion;
    }

    // PENDIENTE DE DECIDIR (equipo): qué estados cuentan como compra cuando lleguen los de HU11 y HU12
    // (p. ej. ENTREGADO sí y CANCELADO no). Por ahora todos los pedidos nacen PAGADO.
    @PreAuthorize(Permisos.SOLO_CLIENTE_PROPIO)
    @Override
    public boolean haComprado(String emailCliente, String productoId) {
        String clienteId = clienteEnSesion.idPorCorreo(emailCliente);
        return pedidoRepository.existePagadoConProducto(clienteId, productoId);
    }

    // HU11-CP01: del más reciente al más antiguo (lo ordena el repositorio)
    @PreAuthorize(Permisos.SOLO_CLIENTE_PROPIO)
    @Override
    public List<Pedido> listarPedidos(String emailCliente) {
        String clienteId = clienteEnSesion.idPorCorreo(emailCliente);
        return pedidoRepository.buscarPorCliente(clienteId);
    }

    // HU11-CP02 y CP03: un pedido ajeno da la misma respuesta que uno inexistente, para no revelar qué pedidos hay
    @PreAuthorize(Permisos.SOLO_CLIENTE_PROPIO)
    @Override
    public Pedido consultarPedido(String emailCliente, String pedidoId) {
        String clienteId = clienteEnSesion.idPorCorreo(emailCliente);
        return pedidoRepository.buscarPorId(pedidoId)
                .filter(pedido -> pedido.getClienteId().equals(clienteId))
                .orElseThrow(() -> new PedidoNoEncontradoException("El pedido no existe"));
    }
}
