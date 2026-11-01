package es.uclm.esibuy.backend.pedidos.infrastructure.adapter.out.persistence;

import es.uclm.esibuy.backend.pedidos.application.port.out.PedidoRepositoryPort;
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
        throw new UnsupportedOperationException("Pendiente: consulta en MongoDB (ciclo 7)");
    }
}
