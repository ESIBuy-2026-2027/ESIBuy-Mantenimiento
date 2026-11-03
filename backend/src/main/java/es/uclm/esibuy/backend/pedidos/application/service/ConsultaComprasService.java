package es.uclm.esibuy.backend.pedidos.application.service;

import org.springframework.security.access.prepost.PreAuthorize;

import es.uclm.esibuy.backend.pedidos.application.port.in.ConsultaComprasUseCase;
import es.uclm.esibuy.backend.pedidos.application.port.out.ClienteEnSesionPort;
import es.uclm.esibuy.backend.pedidos.application.port.out.PedidoRepositoryPort;
import es.uclm.esibuy.backend.shared.seguridad.Permisos;

/**
 * Consulta si el cliente ha comprado un producto (HU13): cuenta como compra un pedido PAGADO que lo contenga.
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
}
