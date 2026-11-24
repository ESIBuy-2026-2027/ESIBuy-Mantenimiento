package es.uclm.esibuy.backend.pedidos.application.service;

import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;

import es.uclm.esibuy.backend.pedidos.application.port.in.GestionPedidosVendedorUseCase;
import es.uclm.esibuy.backend.pedidos.application.port.out.ExistenciasPort;
import es.uclm.esibuy.backend.pedidos.application.port.out.PedidoRepositoryPort;
import es.uclm.esibuy.backend.pedidos.application.port.out.VendedorEnSesionPort;
import es.uclm.esibuy.backend.pedidos.domain.exception.PedidoNoEncontradoException;
import es.uclm.esibuy.backend.pedidos.domain.model.LineaPedido;
import es.uclm.esibuy.backend.pedidos.domain.model.Pedido;
import es.uclm.esibuy.backend.shared.seguridad.Permisos;

public class GestionPedidosVendedorService implements GestionPedidosVendedorUseCase {

    private final PedidoRepositoryPort pedidoRepository;
    private final VendedorEnSesionPort vendedorEnSesion;
    private final ExistenciasPort existencias;

    public GestionPedidosVendedorService(
            PedidoRepositoryPort pedidoRepository,
            VendedorEnSesionPort vendedorEnSesion,
            ExistenciasPort existencias) {
        this.pedidoRepository = pedidoRepository;
        this.vendedorEnSesion = vendedorEnSesion;
        this.existencias = existencias;
    }

    @PreAuthorize(Permisos.SOLO_VENDEDOR_PROPIO)
    @Override
    public List<Pedido> listarPedidos(String emailVendedor) {
        String vendedorId = vendedorEnSesion.idPorCorreo(emailVendedor);
        return pedidoRepository.buscarPorVendedor(vendedorId);
    }

    @PreAuthorize(Permisos.SOLO_VENDEDOR_PROPIO)
    @Override
    public Pedido consultarPedido(String emailVendedor, String pedidoId) {
        String vendedorId = vendedorEnSesion.idPorCorreo(emailVendedor);
        return buscarDelVendedor(vendedorId, pedidoId);
    }

    @PreAuthorize(Permisos.SOLO_VENDEDOR_PROPIO)
    @Override
    public Pedido avanzarEstado(String emailVendedor, String pedidoId) {
        String vendedorId = vendedorEnSesion.idPorCorreo(emailVendedor);
        Pedido pedido = buscarDelVendedor(vendedorId, pedidoId);
        pedido.avanzarEstadoVendedor();
        return pedidoRepository.guardar(pedido);
    }

    @PreAuthorize(Permisos.SOLO_VENDEDOR_PROPIO)
    @Override
    public Pedido retrocederEstado(String emailVendedor, String pedidoId) {
        String vendedorId = vendedorEnSesion.idPorCorreo(emailVendedor);
        Pedido pedido = buscarDelVendedor(vendedorId, pedidoId);
        pedido.retrocederEstado();
        return pedidoRepository.guardar(pedido);
    }

    @PreAuthorize(Permisos.SOLO_VENDEDOR_PROPIO)
    @Override
    public Pedido cancelarPedido(String emailVendedor, String pedidoId) {
        String vendedorId = vendedorEnSesion.idPorCorreo(emailVendedor);
        Pedido pedido = buscarDelVendedor(vendedorId, pedidoId);
        pedido.cancelar();
        Pedido guardado = pedidoRepository.guardar(pedido);
        for (LineaPedido linea : guardado.getLineas()) {
            existencias.reponer(linea.productoId(), linea.cantidad());
        }
        return guardado;
    }

    private Pedido buscarDelVendedor(String vendedorId, String pedidoId) {
        return pedidoRepository.buscarPorId(pedidoId)
                .filter(pedido -> pedido.getVendedorId().equals(vendedorId))
                .orElseThrow(() -> new PedidoNoEncontradoException("El pedido no existe"));
    }
}