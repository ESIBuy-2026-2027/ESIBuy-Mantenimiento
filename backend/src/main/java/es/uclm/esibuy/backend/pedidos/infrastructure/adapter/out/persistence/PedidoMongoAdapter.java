package es.uclm.esibuy.backend.pedidos.infrastructure.adapter.out.persistence;

import java.util.List;
import java.util.Optional;

import es.uclm.esibuy.backend.pedidos.application.port.out.PedidoRepositoryPort;
import es.uclm.esibuy.backend.pedidos.domain.model.EstadoPedido;
import es.uclm.esibuy.backend.pedidos.domain.model.Pedido;

/** Adaptador de salida que implementa el repositorio de pedidos con Spring Data MongoDB. */
public class PedidoMongoAdapter implements PedidoRepositoryPort {

    private final PedidoMongoRepository repository;
    private final PedidoDocumentMapper mapper;

    public PedidoMongoAdapter(PedidoMongoRepository repository, PedidoDocumentMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    public Pedido guardar(Pedido pedido) {
        return mapper.aDominio(repository.save(mapper.aDocumento(pedido)));
    }

    @Override
    public boolean existePagadoConProducto(String clienteId, String productoId) {
        return repository.existsByClienteIdAndEstadoAndLineasProductoId(clienteId, EstadoPedido.PAGADO.name(),
                productoId);
    }

    @Override
    public List<Pedido> buscarPorCliente(String clienteId) {
        return repository.findByClienteIdOrderByCreatedAtDesc(clienteId).stream().map(mapper::aDominio).toList();
    }

    @Override
    public Optional<Pedido> buscarPorId(String id) {
        return repository.findById(id).map(mapper::aDominio);
    }

    @Override
    public List<Pedido> buscarPorVendedor(String vendedorId) {
        return repository.findByVendedorId(vendedorId).stream().map(mapper::aDominio).toList();
    }
}
