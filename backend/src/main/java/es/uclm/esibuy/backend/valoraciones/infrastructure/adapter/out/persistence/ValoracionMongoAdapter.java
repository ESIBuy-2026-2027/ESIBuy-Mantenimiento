package es.uclm.esibuy.backend.valoraciones.infrastructure.adapter.out.persistence;

import java.util.List;

import es.uclm.esibuy.backend.valoraciones.application.port.out.ValoracionRepositoryPort;
import es.uclm.esibuy.backend.valoraciones.domain.model.Valoracion;

/** Adaptador de salida que implementa el repositorio de valoraciones con Spring Data MongoDB. */
public class ValoracionMongoAdapter implements ValoracionRepositoryPort {

    private final ValoracionMongoRepository repository;
    private final ValoracionDocumentMapper mapper;

    public ValoracionMongoAdapter(ValoracionMongoRepository repository, ValoracionDocumentMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    public Valoracion guardar(Valoracion valoracion) {
        return mapper.aDominio(repository.save(mapper.aDocumento(valoracion)));
    }

    @Override
    public boolean existePorClienteYProducto(String clienteId, String productoId) {
        return repository.existsByUsuarioIdAndProductoId(clienteId, productoId);
    }

    @Override
    public List<Valoracion> buscarPorProducto(String productoId) {
        return repository.findByProductoId(productoId).stream().map(mapper::aDominio).toList();
    }
}